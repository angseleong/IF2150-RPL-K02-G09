package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.CadanganFarmasi;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.PengaturanAplikasi;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataFarmasiBoundary;

/**
 * Pengujian unit C25 PersediaanManager (HIUPL Subbab 4.2, C25-UT01 sampai C25-UT20). Setiap test diberi
 * {@code @DisplayName} berawalan ID test case. Penolakan validasi terjadi sebelum basis data disentuh, sehingga
 * kasus itu berjalan tanpa basis data.
 */
@DisplayName("C25 PersediaanManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class PersediaanManagerTest {

    private static final LocalDate HARI_INI = LocalDate.now();
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PersediaanManager manager = new PersediaanManager();

    @AfterEach
    void lepasSesi() {
        SesiAktif.lepas();
    }

    private static PenerimaanObatEntity penerimaan(String faktur, LocalDate terima, BetsObatEntity... bets) {
        return new PenerimaanObatEntity(0, faktur, terima, new ArrayList<>(List.of(bets)));
    }

    private static BetsObatEntity bets(int idObat, String nomor, int jumlah, LocalDate kedaluwarsa) {
        return new BetsObatEntity(nomor, idObat, jumlah, kedaluwarsa);
    }

    private String kolomDitolak(PenerimaanObatEntity p) {
        return assertThrows(IllegalArgumentException.class, () -> manager.catatPenerimaan(p)).getMessage();
    }

    @Test
    @DisplayName("C25-UT07 catatPenerimaan batas: kedaluwarsa sama dengan tanggal terima hari ini ditolak")
    void catatPenerimaan_kedaluwarsaHariIni() {
        assertEquals("tanggalKedaluwarsa", kolomDitolak(penerimaan("FK-1", HARI_INI, bets(6, "B-1", 5, HARI_INI))));
    }

    @Test
    @DisplayName("C25-UT08 catatPenerimaan negatif: jumlah 0 dan kedaluwarsa kemarin ditolak (UC08 alt 2)")
    void catatPenerimaan_jumlahDanKedaluwarsaTidakValid() {
        assertEquals("jumlah,tanggalKedaluwarsa", kolomDitolak(penerimaan("FK-1", HARI_INI,
                bets(6, "B-1", 0, HARI_INI.minusDays(1)))));
    }

    @Test
    @DisplayName("C25-UT09 catatPenerimaan negatif: jumlah negatif ditolak pada kolom jumlah")
    void catatPenerimaan_jumlahNegatif() {
        assertEquals("jumlah", kolomDitolak(penerimaan("FK-1", HARI_INI, bets(6, "B-1", -5, HARI_INI.plusYears(1)))));
    }

    @Test
    @DisplayName("C25-UT10 catatPenerimaan negatif: seluruh kolom kosong ditandai sesuai urutan formulir")
    void catatPenerimaan_kolomKosong() {
        assertEquals("nomorFaktur,tanggalTerima,nomorBets,tanggalKedaluwarsa,obat",
                kolomDitolak(penerimaan(" ", null, bets(0, "", 5, null))));
    }

    @Test
    @DisplayName("C25-UT11 catatPenerimaan negatif: penerimaan tanpa bets ditolak pada kolom nomor bets")
    void catatPenerimaan_tanpaBets() {
        assertEquals("nomorBets", kolomDitolak(penerimaan("FK-1", HARI_INI)));
        assertEquals("nomorBets", kolomDitolak(new PenerimaanObatEntity(0, "FK-1", HARI_INI, null)));
    }

    @Test
    @DisplayName("C25-UT12 catatPenerimaan negatif: tanggal terima setelah hari ini ditolak")
    void catatPenerimaan_tanggalTerimaMasaDepan() {
        assertEquals("tanggalTerima", kolomDitolak(penerimaan("FK-1", HARI_INI.plusDays(1),
                bets(6, "B-1", 5, HARI_INI.plusYears(2)))));
    }

    @Test
    @DisplayName("C25-UT13 catatPenerimaan negatif: bets milik dua obat berbeda pada satu penerimaan ditolak")
    void catatPenerimaan_obatBerbeda() {
        LocalDate kedaluwarsa = HARI_INI.plusYears(2);
        assertEquals("obat", kolomDitolak(penerimaan("FK-1", HARI_INI,
                bets(6, "B-1", 5, kedaluwarsa), bets(7, "B-2", 5, kedaluwarsa))));
    }

    @Test
    @DisplayName("C25-UT14 catatPenerimaan negatif: kedaluwarsa lewat ditolak walau tanggal terima dimundurkan")
    void catatPenerimaan_kedaluwarsaLewat() {
        assertEquals("tanggalKedaluwarsa", kolomDitolak(penerimaan("FK-1", HARI_INI.minusDays(30),
                bets(6, "B-1", 5, HARI_INI.minusDays(10)))));
        assertEquals("tanggalKedaluwarsa", kolomDitolak(penerimaan("FK-1", HARI_INI.minusDays(30),
                bets(6, "B-1", 5, HARI_INI))));
    }

    @Test
    @DisplayName("C25-UT15 catatPenerimaan negatif: peran Dokter ditolak setelah isian lolos validasi")
    void catatPenerimaan_peranDokter() {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 3, LocalDateTime.now(), true), "Dokter");
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.catatPenerimaan(
                penerimaan("FK-1", HARI_INI, bets(6, "B-1", 5, HARI_INI.plusYears(1)))));
        assertEquals("Peran Dokter tidak berwenang menjalankan tindakan ini", e.getMessage());
    }

    @Test
    @DisplayName("C25-UT17 terbitkanPeringatan normal: stok di bawah ambang dan bets menjelang kedaluwarsa")
    void terbitkanPeringatan_normal() {
        LocalDate dekat = HARI_INI.plusDays(48);
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu(
                List.of(new ObatEntity(6, "Kaptopril 25 mg", "tablet", 20, 50),
                        new ObatEntity(1, "Parasetamol 500 mg", "tablet", 300, 100)),
                Map.of(6, List.of(bets(6, "KAP-A01", 20, HARI_INI.plusDays(400))),
                        1, List.of(bets(1, "P-0412", 120, dekat), bets(1, "P-0500", 180, HARI_INI.plusDays(91)))));

        List<String> peringatan = PersediaanManager.terbitkanPeringatan(dataFarmasi);

        assertEquals(List.of("Stok Kaptopril 25 mg tinggal 20, di bawah ambang 50",
                "Bets P-0412 (Parasetamol 500 mg) kedaluwarsa " + dekat.format(TANGGAL)), peringatan);
        assertEquals(90, dataFarmasi.hariDiminta);
    }

    @Test
    @DisplayName("C25-UT18 terbitkanPeringatan batas: tepat 90 hari dan stok di bawah ambang 1 diperingatkan, "
            + "91 hari, stok sama dengan ambang, dan bets habis tidak")
    void terbitkanPeringatan_batas() {
        LocalDate tepat = HARI_INI.plusDays(90);
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu(
                List.of(new ObatEntity(5, "Glibenklamid 5 mg", "tablet", 49, 50),
                        new ObatEntity(3, "Amlodipin 5 mg", "tablet", 60, 60)),
                Map.of(5, List.of(bets(5, "GLB-A", 49, HARI_INI.plusDays(91))),
                        3, List.of(bets(3, "AML-HABIS", 0, HARI_INI.plusDays(10)), bets(3, "AML-A", 60, tepat))));

        assertEquals(List.of("Stok Glibenklamid 5 mg tinggal 49, di bawah ambang 50",
                "Bets AML-A (Amlodipin 5 mg) kedaluwarsa " + tepat.format(TANGGAL)),
                PersediaanManager.terbitkanPeringatan(dataFarmasi));
    }

    @Test
    @DisplayName("C25-UT19 terbitkanPeringatan negatif: tidak ada obat bermasalah menghasilkan daftar kosong")
    void terbitkanPeringatan_kosong() {
        assertTrue(PersediaanManager.terbitkanPeringatan(new DataFarmasiPalsu(List.of(), Map.of())).isEmpty());
    }

    /** Pengganti DataFarmasiBoundary yang mengembalikan data siap pakai tanpa koneksi. */
    private static final class DataFarmasiPalsu extends DataFarmasiBoundary {

        private final List<ObatEntity> obat;
        private final Map<Integer, List<BetsObatEntity>> betsPerObat;
        private int hariDiminta;

        DataFarmasiPalsu(List<ObatEntity> obat, Map<Integer, List<BetsObatEntity>> betsPerObat) {
            super(null);
            this.obat = obat;
            this.betsPerObat = betsPerObat;
        }

        @Override
        public List<ObatEntity> obatPerluPeringatan(int hari) {
            hariDiminta = hari;
            return obat;
        }

        @Override
        public List<BetsObatEntity> ambilBets(int idObat) {
            return betsPerObat.getOrDefault(idObat, List.of());
        }
    }

    /**
     * Memakai seed: obat 6 Kaptopril 25 mg stok 120 dengan bets KAP-A01, obat 5 Glibenklamid stok 30 di bawah
     * ambang 50, bets AML-A01 Amlodipin yang kedaluwarsa kurang dari 90 hari lagi, dan obat 12 Asam Mefenamat stok
     * 0. DB_UJI_URL harus memakai akun pemilik basis data. Seluruh perubahan dipulihkan {@link CadanganFarmasi}.
     */
    @Nested
    @DisplayName("C25 PersediaanManager dengan basis data")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private static final int KAPTOPRIL = 6;

        private Connection koneksi;
        private CadanganFarmasi cadangan;
        private int stokAwal;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            cadangan = CadanganFarmasi.ambil(koneksi);
            stokAwal = hitung("SELECT stok FROM obat WHERE id_obat = " + KAPTOPRIL);
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 4, LocalDateTime.now(), true), "Petugas Farmasi");
        }

        @AfterEach
        void pulihkan() throws SQLException {
            SesiAktif.lepas();
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("GRANT SELECT ON obat TO sehati_app");
            }
            cadangan.pulihkan();
            koneksi.close();
        }

        @Test
        @DisplayName("C25-UT01 muatDataObat normal: seluruh obat dimuat beserta saldo stok, terurut nama")
        void muatDataObat_normal() throws SQLException {
            List<ObatEntity> obat = manager.muatDataObat();

            assertEquals(hitung("SELECT count(*) FROM obat"), obat.size());
            assertEquals(stokAwal, obat.stream().filter(o -> o.getIdObat() == KAPTOPRIL).findFirst().orElseThrow()
                    .getStok());
            for (int i = 1; i < obat.size(); i++) {
                assertFalse(obat.get(i - 1).getNamaObat().compareTo(obat.get(i).getNamaObat()) > 0);
            }
        }

        @Test
        @DisplayName("C25-UT02 muatDataObat batas: obat berstok 0 tetap dimuat agar dapat dipilih saat obat masuk")
        void muatDataObat_stokNol() {
            assertTrue(manager.muatDataObat().stream()
                    .anyMatch(o -> o.getNamaObat().equals("Asam Mefenamat 500 mg") && o.getStok() == 0));
        }

        @Test
        @DisplayName("C25-UT03 muatDataObat negatif: basis data menolak akses ke tabel obat")
        void muatDataObat_aksesDitolak() throws SQLException {
            assumeTrue("sehati_app".equals(PengaturanAplikasi.muat().ambil("DB_USER")));
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("REVOKE SELECT ON obat FROM sehati_app");
            }
            IllegalStateException e = assertThrows(IllegalStateException.class, manager::muatDataObat);
            assertEquals("Gagal mengambil data obat", e.getMessage());
        }

        @Test
        @DisplayName("C25-UT04 catatPenerimaan normal: stok bertambah, penerimaan, dua bets, dan audit tersimpan")
        void catatPenerimaan_normal() throws SQLException {
            PenerimaanObatEntity p = penerimaan("FK-UJI-01", HARI_INI,
                    bets(KAPTOPRIL, "KAP-UJI-1", 200, HARI_INI.plusYears(2)),
                    bets(KAPTOPRIL, "KAP-UJI-2", 100, HARI_INI.plusYears(3)));

            manager.catatPenerimaan(p);

            assertTrue(p.getIdPenerimaan() > cadangan.penerimaanTerakhir());
            assertEquals(stokAwal + 300, hitung("SELECT stok FROM obat WHERE id_obat = " + KAPTOPRIL));
            assertEquals(2, hitung("SELECT count(*) FROM bets_obat WHERE id_penerimaan = " + p.getIdPenerimaan()));
            assertEquals(1, hitung("SELECT count(*) FROM penerimaan_obat WHERE id_penerimaan = " + p.getIdPenerimaan()
                    + " AND nomor_faktur = 'FK-UJI-01' AND tanggal_terima = CURRENT_DATE"));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_audit > " + cadangan.auditTerakhir()
                    + " AND id_pengguna = 4 AND deskripsi = 'Obat masuk Kaptopril 25 mg sejumlah 300, faktur FK-UJI-01'"));
        }

        @Test
        @DisplayName("C25-UT05 catatPenerimaan batas: jumlah 1 dan kedaluwarsa besok, batas terdekat yang sah")
        void catatPenerimaan_batasTerdekat() throws SQLException {
            PenerimaanObatEntity p = penerimaan("FK-UJI-02", HARI_INI,
                    bets(KAPTOPRIL, "KAP-UJI-3", 1, HARI_INI.plusDays(1)));

            manager.catatPenerimaan(p);

            assertEquals(stokAwal + 1, hitung("SELECT stok FROM obat WHERE id_obat = " + KAPTOPRIL));
            assertEquals(1, hitung("SELECT jumlah FROM bets_obat WHERE nomor_bets = 'KAP-UJI-3'"));
        }

        @Test
        @DisplayName("C25-UT06 catatPenerimaan batas: tanggal terima mundur 30 hari tetap diterima")
        void catatPenerimaan_tanggalTerimaLampau() throws SQLException {
            PenerimaanObatEntity p = penerimaan("FK-UJI-03", HARI_INI.minusDays(30),
                    bets(KAPTOPRIL, "KAP-UJI-4", 10, HARI_INI.plusYears(1)));

            manager.catatPenerimaan(p);

            assertEquals(1, hitung("SELECT count(*) FROM penerimaan_obat WHERE nomor_faktur = 'FK-UJI-03' "
                    + "AND tanggal_terima = CURRENT_DATE - 30"));
        }

        @Test
        @DisplayName("C25-UT16 catatPenerimaan negatif: nomor bets ganda dan obat di luar data master dibatalkan "
                + "seluruhnya")
        void catatPenerimaan_ditolakBasisData() throws SQLException {
            IllegalArgumentException ganda = assertThrows(IllegalArgumentException.class,
                    () -> manager.catatPenerimaan(penerimaan("FK-UJI-04", HARI_INI,
                            bets(KAPTOPRIL, "KAP-A01", 50, HARI_INI.plusYears(2)))));
            PenerimaanObatEntity tanpaObat = penerimaan("FK-UJI-05", HARI_INI,
                    bets(9999, "X-1", 50, HARI_INI.plusYears(2)));
            IllegalArgumentException obat = assertThrows(IllegalArgumentException.class,
                    () -> manager.catatPenerimaan(tanpaObat));

            assertEquals("nomorBets", ganda.getMessage());
            assertEquals("obat", obat.getMessage());
            assertEquals(0, tanpaObat.getIdPenerimaan());
            assertEquals(stokAwal, hitung("SELECT stok FROM obat WHERE id_obat = " + KAPTOPRIL));
            assertEquals(cadangan.penerimaanTerakhir(),
                    hitung("SELECT coalesce(max(id_penerimaan), 0) FROM penerimaan_obat"));
            assertEquals(0, hitung("SELECT count(*) FROM audit WHERE id_audit > " + cadangan.auditTerakhir()));
        }

        @Test
        @DisplayName("C25-UT20 terbitkanPeringatan normal: peringatan dari seed memuat Glibenklamid dan AML-A01 saja")
        void terbitkanPeringatan_seed() {
            List<String> peringatan = manager.terbitkanPeringatan();

            assertTrue(peringatan.stream().anyMatch(p -> p.startsWith("Stok Glibenklamid 5 mg tinggal 30")));
            assertTrue(peringatan.stream().anyMatch(p -> p.startsWith("Stok Asam Mefenamat 500 mg tinggal 0")));
            assertTrue(peringatan.stream().anyMatch(p -> p.startsWith("Bets AML-A01 (Amlodipin 5 mg)")));
            assertTrue(peringatan.stream().noneMatch(p -> p.contains("AML-B01") || p.contains("Kaptopril")));
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); var rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
