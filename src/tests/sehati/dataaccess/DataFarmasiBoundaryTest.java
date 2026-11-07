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
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.business.entity.ResepEntity;

class DataFarmasiBoundaryTest {

    @Test
    void sqlState_diambilDariPenyebabBatch() {
        BatchUpdateException batch = new BatchUpdateException("batch gagal", null, 0, new int[0], null);
        batch.setNextException(new SQLException("duplikat", "23505"));
        assertEquals("23505", DataFarmasiBoundary.sqlState(batch));
    }

    @Test
    void galat_menerjemahkanSqlStateSesuaiAturan3() {
        assertInstanceOf(IllegalArgumentException.class,
                DataFarmasiBoundary.galat("Uji", new SQLException("unik", "23505")));
        assertInstanceOf(IllegalArgumentException.class,
                DataFarmasiBoundary.galat("Uji", new SQLException("cek", "23514")));
        RuntimeException putus = DataFarmasiBoundary.galat("Uji", new SQLException("putus", "08006"));
        assertInstanceOf(IllegalStateException.class, putus);
        assertEquals("Basis data tidak dapat dihubungi", putus.getMessage());
    }

    @Test
    void galat_meneruskanPenolakanApaAdanya() {
        IllegalArgumentException asli = new IllegalArgumentException("nomorBets");
        assertSame(asli, DataFarmasiBoundary.galat("Uji", asli));
    }

    /**
     * Uji terhadap basis data berisi src/db/schema.sql dan seed.sql. Aktif bila DB_UJI_URL diisi, misalnya
     * {@code jdbc:postgresql://localhost:5432/sehati?user=sehati_admin&password=...}. Setiap uji di-rollback.
     */
    @Nested
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
            koneksi.rollback();
            koneksi.close();
        }

        @Test
        void ambilSemuaObat_terurutNama() {
            List<ObatEntity> obat = dataFarmasi.ambilSemuaObat();
            assertFalse(obat.isEmpty());
            for (int i = 1; i < obat.size(); i++) {
                assertTrue(obat.get(i - 1).getNamaObat().compareTo(obat.get(i).getNamaObat()) <= 0);
            }
        }

        @Test
        void ambilObat_nullBilaTidakAda() {
            assertNull(dataFarmasi.ambilObat(-1));
        }

        @Test
        void ambilBets_kedaluwarsaTerdekatLebihDulu() {
            ObatEntity amlodipin = cariObat("Amlodipin 5 mg");
            List<BetsObatEntity> bets = dataFarmasi.ambilBets(amlodipin.getIdObat());
            assertTrue(bets.size() >= 2);
            for (int i = 1; i < bets.size(); i++) {
                assertFalse(bets.get(i).getTanggalKedaluwarsa().isBefore(bets.get(i - 1).getTanggalKedaluwarsa()));
            }
        }

        @Test
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
        void simpanPenerimaan_menyisipkanPenerimaanDanBets() {
            ObatEntity obat = cariObat("Parasetamol 500 mg");
            BetsObatEntity baru = new BetsObatEntity("PCT-UJI-01", obat.getIdObat(), 50, LocalDate.now().plusYears(2));
            PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FKT-UJI-01", LocalDate.now(), List.of(baru));

            dataFarmasi.simpanPenerimaan(p);

            assertNotEquals(0, p.getIdPenerimaan());
            assertTrue(dataFarmasi.ambilBets(obat.getIdObat()).stream()
                    .anyMatch(b -> b.getNomorBets().equals("PCT-UJI-01") && b.getJumlah() == 50));
        }

        @Test
        void simpanPenerimaan_menolakNomorBetsGanda() {
            ObatEntity obat = cariObat("Parasetamol 500 mg");
            BetsObatEntity ganda = new BetsObatEntity("PCT-A01", obat.getIdObat(), 10, LocalDate.now().plusYears(1));
            PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FKT-UJI-02", LocalDate.now(), List.of(ganda));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> dataFarmasi.simpanPenerimaan(p));
            assertEquals("nomorBets", e.getMessage());
        }

        @Test
        void simpanPenerimaan_diLuarTransaksiTidakMenyisakanFaktur() throws SQLException {
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
        void obatPerluPeringatan_stokRendahDanMenjelangKedaluwarsa() {
            List<String> nama = dataFarmasi.obatPerluPeringatan(90).stream().map(ObatEntity::getNamaObat).toList();
            assertTrue(nama.contains("Glibenklamid 5 mg"));
            assertTrue(nama.contains("Amlodipin 5 mg"));
            assertFalse(nama.contains("Parasetamol 500 mg"));
        }

        @Test
        void simpanResep_baruBesertaItemLaluStatusnyaDiperbarui() {
            ResepEntity menunggu = dataFarmasi.ambilAntreanResep().get(0);
            ResepEntity r = new ResepEntity(0, menunggu.getIdKunjungan(), LocalDateTime.now(), "menunggu", null);
            int idObat = cariObat("Parasetamol 500 mg").getIdObat();
            r.setDaftarItem(List.of(new ItemResepEntity(0, idObat, "3x1", 10, "sesudah makan")));

            dataFarmasi.simpanResep(r);

            assertNotEquals(0, r.getIdResep());
            ResepEntity tersimpan = dataFarmasi.ambilResep(r.getIdResep());
            assertEquals(1, tersimpan.getDaftarItem().size());
            assertEquals(idObat, tersimpan.getDaftarItem().get(0).getIdObat());

            tersimpan.setStatus("dikembalikan");
            tersimpan.setCatatan("Dosis perlu diperbaiki");
            dataFarmasi.simpanResep(tersimpan);
            assertEquals("dikembalikan", dataFarmasi.ambilResep(r.getIdResep()).getStatus());
        }

        @Test
        void ambilAntreanResep_hanyaMenungguBesertaItem() {
            List<ResepEntity> antrean = dataFarmasi.ambilAntreanResep();
            assertFalse(antrean.isEmpty());
            for (ResepEntity r : antrean) {
                assertEquals("menunggu", r.getStatus());
                assertFalse(r.getDaftarItem().isEmpty());
            }
        }

        @Test
        void kunciResep_menahanTransaksiLain() throws SQLException {
            int idResep = dataFarmasi.ambilAntreanResep().get(0).getIdResep();
            dataFarmasi.kunciResep(idResep);

            assertTransaksiLainTertahan(lain -> lain.kunciResep(idResep));
        }

        @Test
        void ambilObat_menahanTransaksiLain() throws SQLException {
            int idObat = cariObat("Amlodipin 5 mg").getIdObat();
            dataFarmasi.ambilObat(idObat);

            assertTransaksiLainTertahan(lain -> lain.ambilObat(idObat));
        }

        // Transaksi kedua menunggu kunci baris paling lama 300 ms lalu gagal (KF23, KNF11).
        private void assertTransaksiLainTertahan(Consumer<DataFarmasiBoundary> aksi)
                throws SQLException {
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

        private Connection bukaKoneksi() throws SQLException {
            return DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        }
    }
}
