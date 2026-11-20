package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.function.Executable;

import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.business.entity.ResepEntity;

/**
 * Pengujian unit C61 DataFarmasiBoundary (HIUPL Subbab 4.2, C61-UT01 sampai C61-UT28). Setiap test diberi
 * {@code @DisplayName} berawalan ID test case. Pemakaiannya bersama ApotekManager dan PersediaanManager juga diuji
 * lewat INT-TC23 sampai INT-TC26.
 */
@DisplayName("C61 DataFarmasiBoundary")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class DataFarmasiBoundaryTest {

    @Test
    @DisplayName("C61-UT26 sqlState: kode galat diambil dari penyebab BatchUpdateException")
    void sqlState_diambilDariPenyebabBatch() {
        BatchUpdateException batch = new BatchUpdateException("batch gagal", null, 0, new int[0], null);
        batch.setNextException(new SQLException("duplikat", "23505"));
        assertEquals("23505", DataFarmasiBoundary.sqlState(batch));
        assertNull(DataFarmasiBoundary.sqlState(new SQLException("tanpa kode")));
    }

    @Test
    @DisplayName("C61-UT27 galat: UNIQUE dan CHECK menjadi IllegalArgumentException, putus koneksi menjadi "
            + "IllegalStateException")
    void galat_menerjemahkanSqlState() {
        assertInstanceOf(IllegalArgumentException.class,
                DataFarmasiBoundary.galat("Uji", new SQLException("unik", "23505")));
        assertInstanceOf(IllegalArgumentException.class,
                DataFarmasiBoundary.galat("Uji", new SQLException("cek", "23514")));
        RuntimeException putus = DataFarmasiBoundary.galat("Uji", new SQLException("putus", "08006"));
        assertInstanceOf(IllegalStateException.class, putus);
        assertEquals("Basis data tidak dapat dihubungi", putus.getMessage());
        assertEquals("Uji", DataFarmasiBoundary.galat("Uji", new SQLException("lain", "42P01")).getMessage());
    }

    @Test
    @DisplayName("C61-UT28 galat: penolakan masukan dan keadaan diteruskan apa adanya")
    void galat_meneruskanPenolakan() {
        IllegalArgumentException asli = new IllegalArgumentException("nomorBets");
        assertSame(asli, DataFarmasiBoundary.galat("Uji", asli));
    }

    /**
     * Uji terhadap basis data berisi src/db/schema.sql dan seed.sql. Aktif bila DB_UJI_URL diisi, misalnya
     * {@code jdbc:postgresql://localhost:5432/sehati?user=sehati_admin&password=...}. Setiap uji di-rollback.
     */
    @Nested
    @DisplayName("C61 DataFarmasiBoundary dengan basis data")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private DataFarmasiBoundary dataFarmasi;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = bukaKoneksi();
            koneksi.setAutoCommit(false);
            dataFarmasi = new DataFarmasiBoundary(koneksi);
        }

        @AfterEach
        void tutup() throws SQLException {
            if (!koneksi.isClosed()) {
                koneksi.rollback();
                koneksi.close();
            }
        }

        @Test
        @DisplayName("C61-UT01 ambilSemuaObat normal: seluruh obat terurut nama (Q-C61-01)")
        void ambilSemuaObat_terurutNama() throws SQLException {
            List<ObatEntity> obat = dataFarmasi.ambilSemuaObat();
            assertEquals(hitung("SELECT count(*) FROM obat"), obat.size());
            for (int i = 1; i < obat.size(); i++) {
                assertTrue(obat.get(i - 1).getNamaObat().compareTo(obat.get(i).getNamaObat()) <= 0);
            }
        }

        @Test
        @DisplayName("C61-UT02 ambilObat normal: obat 1 dimuat dengan nama, sediaan, stok, dan ambang (Q-C61-02)")
        void ambilObat_normal() {
            ObatEntity o = dataFarmasi.ambilObat(1);
            assertEquals("Parasetamol 500 mg|tablet|300|100",
                    o.getNamaObat() + "|" + o.getSediaan() + "|" + o.getStok() + "|" + o.getAmbangMinimum());
        }

        @Test
        @DisplayName("C61-UT03 ambilObat negatif: obat yang tidak ada mengembalikan null")
        void ambilObat_tidakAda() {
            assertNull(dataFarmasi.ambilObat(-1));
        }

        @Test
        @DisplayName("C61-UT04 ambilObat batas: baris obat terkunci sampai transaksi selesai (FOR UPDATE)")
        void ambilObat_menahanTransaksiLain() throws SQLException {
            int idObat = cariObat("Amlodipin 5 mg").getIdObat();
            dataFarmasi.ambilObat(idObat);

            assertTransaksiLainTertahan(lain -> lain.ambilObat(idObat));
        }

        @Test
        @DisplayName("C61-UT05 ambilBets normal: bets bersisa terurut kedaluwarsa terdekat (Q-C61-03)")
        void ambilBets_fefo() {
            List<BetsObatEntity> bets = dataFarmasi.ambilBets(cariObat("Amlodipin 5 mg").getIdObat());
            assertEquals(List.of("AML-A01", "AML-B01"), bets.stream().map(BetsObatEntity::getNomorBets).toList());
            assertFalse(bets.get(1).getTanggalKedaluwarsa().isBefore(bets.get(0).getTanggalKedaluwarsa()));
        }

        @Test
        @DisplayName("C61-UT06 ambilBets negatif: bets yang sudah habis dan obat tanpa bets tidak dimuat")
        void ambilBets_habis() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE bets_obat SET jumlah = 0 WHERE nomor_bets = 'AML-A01'");
            }
            assertEquals(List.of("AML-B01"), dataFarmasi.ambilBets(cariObat("Amlodipin 5 mg").getIdObat()).stream()
                    .map(BetsObatEntity::getNomorBets).toList());
            assertTrue(dataFarmasi.ambilBets(cariObat("Asam Mefenamat 500 mg").getIdObat()).isEmpty());
        }

        @Test
        @DisplayName("C61-UT07 simpanStok normal: nilai akhir stok dan bets ditulis (Q-C61-04, Q-C61-05)")
        void simpanStok_menulisNilaiAkhir() {
            ObatEntity obat = dataFarmasi.ambilObat(cariObat("Amlodipin 5 mg").getIdObat());
            List<BetsObatEntity> bets = dataFarmasi.ambilBets(obat.getIdObat());
            BetsObatEntity pertama = bets.get(0);
            obat.setStok(obat.getStok() - 5);
            pertama.setJumlah(pertama.getJumlah() - 5);

            dataFarmasi.simpanStok(obat, bets);

            assertEquals(obat.getStok(), dataFarmasi.ambilObat(obat.getIdObat()).getStok());
            assertEquals(pertama.getJumlah(), dataFarmasi.ambilBets(obat.getIdObat()).get(0).getJumlah());
        }

        @Test
        @DisplayName("C61-UT08 simpanStok negatif: obat yang tidak ada ditolak")
        void simpanStok_obatTidakAda() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> dataFarmasi.simpanStok(new ObatEntity(9999, "X", "tablet", 5, 1), List.of()));
            assertEquals("Obat tidak ditemukan: 9999", e.getMessage());
        }

        @Test
        @DisplayName("C61-UT09 simpanStok negatif: stok negatif ditolak CHECK tabel dan bets tidak ikut berubah")
        void simpanStok_stokNegatif() throws SQLException {
            // Dibentuk langsung, bukan lewat ambilObat(), agar baris obat tidak terkunci transaksi test ini.
            ObatEntity obat = new ObatEntity(1, "Parasetamol 500 mg", "tablet", -1, 100);
            BetsObatEntity bets = new BetsObatEntity("PCT-A01", 1, 0, LocalDate.now().plusYears(1));

            // Di luar transaksi Manager, simpanStok() membuka transaksinya sendiri dan me-rollback-nya bila gagal.
            try (Connection otomatis = bukaKoneksi()) {
                DataFarmasiBoundary data = new DataFarmasiBoundary(otomatis);
                IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                        () -> data.simpanStok(obat, List.of(bets)));
                assertEquals("Gagal menyimpan stok obat: nilai di luar batas yang diizinkan", e.getMessage());
                assertTrue(otomatis.getAutoCommit());
            }
            assertEquals(300, hitung("SELECT stok FROM obat WHERE id_obat = 1"));
            assertEquals(300, hitung("SELECT jumlah FROM bets_obat WHERE nomor_bets = 'PCT-A01'"));
        }

        @Test
        @DisplayName("C61-UT10 simpanPenerimaan normal: penerimaan dan bets tersisip dengan ID penerimaan "
                + "(Q-C61-06, Q-C61-07)")
        void simpanPenerimaan_normal() throws SQLException {
            ObatEntity obat = cariObat("Parasetamol 500 mg");
            BetsObatEntity baru = new BetsObatEntity("PCT-UJI-01", obat.getIdObat(), 50, LocalDate.now().plusYears(2));
            PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FKT-UJI-01", LocalDate.now(), List.of(baru));

            dataFarmasi.simpanPenerimaan(p);

            assertNotEquals(0, p.getIdPenerimaan());
            assertEquals(p.getIdPenerimaan(), hitung("SELECT id_penerimaan FROM bets_obat WHERE nomor_bets = "
                    + "'PCT-UJI-01'"));
            assertTrue(dataFarmasi.ambilBets(obat.getIdObat()).stream()
                    .anyMatch(b -> b.getNomorBets().equals("PCT-UJI-01") && b.getJumlah() == 50));
        }

        @Test
        @DisplayName("C61-UT11 simpanPenerimaan negatif: nomor bets yang sudah tercatat ditolak (Algo-C61-01)")
        void simpanPenerimaan_nomorBetsGanda() {
            ObatEntity obat = cariObat("Parasetamol 500 mg");
            BetsObatEntity ganda = new BetsObatEntity("PCT-A01", obat.getIdObat(), 10, LocalDate.now().plusYears(1));
            PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FKT-UJI-02", LocalDate.now(), List.of(ganda));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> dataFarmasi.simpanPenerimaan(p));
            assertEquals("nomorBets", e.getMessage());
        }

        @Test
        @DisplayName("C61-UT12 simpanPenerimaan negatif: di luar transaksi Manager tidak menyisakan faktur")
        void simpanPenerimaan_diLuarTransaksi() throws SQLException {
            ObatEntity obat = cariObat("Parasetamol 500 mg");
            BetsObatEntity ganda = new BetsObatEntity("PCT-A01", obat.getIdObat(), 10, LocalDate.now().plusYears(1));
            PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FKT-UJI-03", LocalDate.now(), List.of(ganda));

            try (Connection otomatis = bukaKoneksi()) {
                DataFarmasiBoundary data = new DataFarmasiBoundary(otomatis);
                assertThrows(IllegalArgumentException.class, () -> data.simpanPenerimaan(p));
                assertTrue(otomatis.getAutoCommit());
            }
            assertEquals(0, hitung("SELECT COUNT(*) FROM penerimaan_obat WHERE nomor_faktur = 'FKT-UJI-03'"));
        }

        @Test
        @DisplayName("C61-UT13 obatPerluPeringatan normal: stok rendah dan bets dalam 90 hari (Q-C61-08)")
        void obatPerluPeringatan_normal() {
            List<String> nama = dataFarmasi.obatPerluPeringatan(90).stream().map(ObatEntity::getNamaObat).toList();
            assertEquals(List.of("Amlodipin 5 mg", "Asam Mefenamat 500 mg", "Glibenklamid 5 mg"), nama);
        }

        @Test
        @DisplayName("C61-UT14 obatPerluPeringatan batas: 0 hari hanya memuat obat di bawah ambang")
        void obatPerluPeringatan_nolHari() {
            List<String> nama = dataFarmasi.obatPerluPeringatan(0).stream().map(ObatEntity::getNamaObat).toList();
            assertEquals(List.of("Asam Mefenamat 500 mg", "Glibenklamid 5 mg"), nama);
        }

        @Test
        @DisplayName("C61-UT15 simpanResep normal: resep baru beserta item, lalu status dan catatannya diperbarui "
                + "(Q-C61-09 s.d. Q-C61-11)")
        void simpanResep_normal() {
            ResepEntity menunggu = dataFarmasi.ambilAntreanResep().get(0);
            ResepEntity r = new ResepEntity(0, menunggu.getIdKunjungan(), LocalDateTime.now(), "menunggu", null);
            int idObat = cariObat("Parasetamol 500 mg").getIdObat();
            r.setDaftarItem(List.of(new ItemResepEntity(0, idObat, "3x1", 10, "sesudah makan")));

            dataFarmasi.simpanResep(r);

            assertNotEquals(0, r.getIdResep());
            assertNotEquals(0, r.getDaftarItem().get(0).getIdItem());
            ResepEntity tersimpan = dataFarmasi.ambilResep(r.getIdResep());
            assertEquals(1, tersimpan.getDaftarItem().size());
            assertEquals(idObat, tersimpan.getDaftarItem().get(0).getIdObat());

            tersimpan.setStatus("dikembalikan");
            tersimpan.setCatatan("Dosis perlu diperbaiki");
            dataFarmasi.simpanResep(tersimpan);
            ResepEntity diperbarui = dataFarmasi.ambilResep(r.getIdResep());
            assertEquals("dikembalikan|Dosis perlu diperbaiki", diperbarui.getStatus() + "|" + diperbarui.getCatatan());
        }

        @Test
        @DisplayName("C61-UT16 simpanResep negatif: memperbarui resep yang tidak ada ditolak")
        void simpanResep_tidakAda() {
            ResepEntity r = new ResepEntity(99999, 5, LocalDateTime.now(), "dikembalikan", "x");
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> dataFarmasi.simpanResep(r));
            assertEquals("Resep tidak ditemukan: 99999", e.getMessage());
        }

        @Test
        @DisplayName("C61-UT17 simpanResep negatif: item berobat di luar data master membatalkan resep beserta "
                + "itemnya")
        void simpanResep_obatTidakAda() throws SQLException {
            int resepAwal = hitung("SELECT count(*) FROM resep");
            ResepEntity r = new ResepEntity(0, 5, LocalDateTime.now(), "menunggu", null);
            r.setDaftarItem(List.of(new ItemResepEntity(0, 1, "3x1", 10, "a"), new ItemResepEntity(0, 9999, "1x1", 1,
                    "b")));

            try (Connection otomatis = bukaKoneksi()) {
                DataFarmasiBoundary data = new DataFarmasiBoundary(otomatis);
                assertThrows(IllegalStateException.class, () -> data.simpanResep(r));
            }
            assertEquals(0, r.getIdResep());
            assertEquals(resepAwal, hitung("SELECT count(*) FROM resep"));
        }

        @Test
        @DisplayName("C61-UT18 ambilAntreanResep normal: hanya resep menunggu beserta item (Q-C61-12, Q-C61-14)")
        void ambilAntreanResep_normal() throws SQLException {
            List<ResepEntity> antrean = dataFarmasi.ambilAntreanResep();
            assertEquals(hitung("SELECT count(*) FROM resep WHERE status = 'menunggu'"), antrean.size());
            for (ResepEntity r : antrean) {
                assertEquals("menunggu", r.getStatus());
                assertFalse(r.getDaftarItem().isEmpty());
            }
        }

        @Test
        @DisplayName("C61-UT19 ambilAntreanResep batas: tanpa resep menunggu mengembalikan daftar kosong")
        void ambilAntreanResep_kosong() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE resep SET status = 'dikembalikan' WHERE status = 'menunggu'");
            }
            assertTrue(dataFarmasi.ambilAntreanResep().isEmpty());
        }

        @Test
        @DisplayName("C61-UT20 ambilResep normal dan negatif: resep 1 dengan itemnya, resep -1 null (Q-C61-13)")
        void ambilResep() {
            ResepEntity r = dataFarmasi.ambilResep(1);
            assertEquals("selesai dilayani", r.getStatus());
            assertEquals(1, r.getDaftarItem().size());
            assertNull(dataFarmasi.ambilResep(-1));
        }

        @Test
        @DisplayName("C61-UT21 kunciResep batas: baris resep terkunci sampai transaksi selesai (Q-C61-15)")
        void kunciResep_menahanTransaksiLain() throws SQLException {
            int idResep = dataFarmasi.ambilAntreanResep().get(0).getIdResep();
            assertEquals(idResep, dataFarmasi.kunciResep(idResep).getIdResep());

            assertTransaksiLainTertahan(lain -> lain.kunciResep(idResep));
            assertNull(dataFarmasi.kunciResep(-1));
        }

        @Test
        @DisplayName("C61-UT22 ambilPasienResep normal dan batas: pasien per resep, daftar kosong tanpa kueri "
                + "(Q-C61-16)")
        void ambilPasienResep() throws SQLException {
            int idResep = dataFarmasi.ambilAntreanResep().get(0).getIdResep();
            Map<Integer, PasienEntity> pasien = dataFarmasi.ambilPasienResep(List.of(idResep, -1));

            assertEquals(1, pasien.size());
            assertEquals(teks("SELECT p.nik FROM resep r JOIN kunjungan k USING (id_kunjungan) JOIN pasien p "
                    + "USING (id_pasien) WHERE r.id_resep = " + idResep), pasien.get(idResep).getNik());
            assertTrue(dataFarmasi.ambilPasienResep(List.of()).isEmpty());
        }

        @Test
        @DisplayName("C61-UT23 ambilAsalResep normal dan batas: poli dan nomor antrean, daftar kosong (Q-C61-19)")
        void ambilAsalResep() throws SQLException {
            int idResep;
            String harapan;
            try (var st = koneksi.createStatement(); var rs = st.executeQuery("SELECT r.id_resep, po.nama_poli, "
                    + "k.nomor_antrean FROM resep r JOIN kunjungan k USING (id_kunjungan) "
                    + "JOIN poli po ON po.id_poli = k.id_poli ORDER BY r.id_resep LIMIT 1")) {
                assertTrue(rs.next());
                idResep = rs.getInt(1);
                harapan = rs.getString(2) + " / Antrean " + rs.getInt(3);
            }
            assertEquals(harapan, dataFarmasi.ambilAsalResep(List.of(idResep)).get(idResep));
            assertTrue(dataFarmasi.ambilAsalResep(null).isEmpty());
        }

        @Test
        @DisplayName("C61-UT24 ambilResepKunjungan dan tambahObat normal: resep kunjungan terlama dahulu, obat baru "
                + "berstok 0 (Q-C61-17, Q-C61-18)")
        void ambilResepKunjunganDanTambahObat() throws SQLException {
            int idKunjungan = hitung("SELECT id_kunjungan FROM resep WHERE id_resep = 1");
            assertEquals(List.of(1), dataFarmasi.ambilResepKunjungan(idKunjungan).stream()
                    .map(ResepEntity::getIdResep).toList());
            assertTrue(dataFarmasi.ambilResepKunjungan(-1).isEmpty());

            ObatEntity baru = new ObatEntity(0, "Obat Uji C61", "tablet 10 mg", 99, 15);
            dataFarmasi.tambahObat(baru);
            assertTrue(baru.getIdObat() > 0);
            assertEquals(0, baru.getStok());
            assertEquals(0, hitung("SELECT stok FROM obat WHERE id_obat = " + baru.getIdObat()));
        }

        @Test
        @DisplayName("C61-UT25 seluruh operasi negatif: koneksi tertutup ditolak 'Basis data tidak dapat dihubungi'")
        void seluruhOperasi_koneksiTertutup() throws SQLException {
            koneksi.close();
            ObatEntity obat = new ObatEntity(1, "Parasetamol 500 mg", "tablet", 300, 100);
            ResepEntity resepBaru = new ResepEntity(0, 5, LocalDateTime.now(), "menunggu", null);
            resepBaru.setDaftarItem(List.of());
            List<Executable> operasi = List.of(
                    dataFarmasi::ambilSemuaObat,
                    () -> dataFarmasi.ambilObat(1),
                    () -> dataFarmasi.ambilBets(1),
                    () -> dataFarmasi.obatPerluPeringatan(90),
                    dataFarmasi::ambilAntreanResep,
                    () -> dataFarmasi.ambilResep(1),
                    () -> dataFarmasi.kunciResep(1),
                    () -> dataFarmasi.ambilResepKunjungan(1),
                    () -> dataFarmasi.ambilPasienResep(List.of(1)),
                    () -> dataFarmasi.ambilAsalResep(List.of(1)),
                    () -> dataFarmasi.tambahObat(new ObatEntity(0, "X", "tablet", 0, 1)),
                    () -> dataFarmasi.simpanStok(obat, List.of()),
                    () -> dataFarmasi.simpanPenerimaan(new PenerimaanObatEntity(0, "F", LocalDate.now(), List.of())),
                    () -> dataFarmasi.simpanResep(resepBaru));
            for (Executable o : operasi) {
                IllegalStateException e = assertThrows(IllegalStateException.class, o);
                assertEquals("Basis data tidak dapat dihubungi", e.getMessage());
            }
        }

        // Transaksi kedua menunggu kunci baris paling lama 300 ms lalu gagal (KF23, KNF11).
        private void assertTransaksiLainTertahan(Consumer<DataFarmasiBoundary> aksi) throws SQLException {
            try (Connection lain = bukaKoneksi()) {
                lain.setAutoCommit(false);
                try (Statement st = lain.createStatement()) {
                    st.execute("SET LOCAL lock_timeout = '300ms'");
                }
                assertThrows(IllegalStateException.class, () -> aksi.accept(new DataFarmasiBoundary(lain)));
                lain.rollback();
            }
        }

        private ObatEntity cariObat(String nama) {
            return dataFarmasi.ambilSemuaObat().stream()
                    .filter(o -> o.getNamaObat().equals(nama))
                    .findFirst()
                    .orElseThrow();
        }

        private int hitung(String kueri) throws SQLException {
            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(kueri)) {
                rs.next();
                return rs.getInt(1);
            }
        }

        private String teks(String kueri) throws SQLException {
            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(kueri)) {
                rs.next();
                return rs.getString(1);
            }
        }

        private Connection bukaKoneksi() throws SQLException {
            return DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        }
    }
}
