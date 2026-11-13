package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataFarmasiBoundary;

class PersediaanManagerTest {

    private static final LocalDate HARI_INI = LocalDate.now();

    private final PersediaanManager manager = new PersediaanManager();

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
    void validasiPenerimaan_jumlahDanKedaluwarsaTidakValid() {
        // Skenario alternatif 2 UC08: jumlah 0 dan tanggal kedaluwarsa sebelum tanggal terima.
        assertEquals("jumlah,tanggalKedaluwarsa", kolomDitolak(penerimaan("FK-1", HARI_INI,
                bets(6, "B-1", 0, HARI_INI.minusDays(1)))));
    }

    @Test
    void validasiPenerimaan_kolomKosong() {
        assertEquals("nomorFaktur,tanggalTerima,nomorBets,tanggalKedaluwarsa,obat",
                kolomDitolak(penerimaan(" ", null, bets(0, "", 5, null))));
        assertEquals("nomorBets", kolomDitolak(penerimaan("FK-1", HARI_INI)));
    }

    @Test
    void validasiPenerimaan_tanggalTerimaDiMasaDepanDanObatBerbeda() {
        LocalDate kedaluwarsa = HARI_INI.plusYears(2);
        assertEquals("tanggalTerima", kolomDitolak(penerimaan("FK-1", HARI_INI.plusDays(1),
                bets(6, "B-1", 5, kedaluwarsa))));
        assertEquals("obat", kolomDitolak(penerimaan("FK-1", HARI_INI,
                bets(6, "B-1", 5, kedaluwarsa), bets(7, "B-2", 5, kedaluwarsa))));
    }

    @Test
    void validasiPenerimaan_kedaluwarsaSamaDenganTanggalTerimaDitolak() {
        assertEquals("tanggalKedaluwarsa", kolomDitolak(penerimaan("FK-1", HARI_INI, bets(6, "B-1", 5, HARI_INI))));
    }

    @Test
    void validasiPenerimaan_kedaluwarsaSudahLewatDitolakWalauTanggalTerimaDimundurkan() {
        assertEquals("tanggalKedaluwarsa", kolomDitolak(penerimaan("FK-1", HARI_INI.minusDays(30),
                bets(6, "B-1", 5, HARI_INI.minusDays(10)))));
        assertEquals("tanggalKedaluwarsa", kolomDitolak(penerimaan("FK-1", HARI_INI.minusDays(30),
                bets(6, "B-1", 5, HARI_INI))));
    }

    @Test
    void terbitkanPeringatan_stokDiBawahAmbangDanBetsMenjelangKedaluwarsa() {
        LocalDate dekat = HARI_INI.plusDays(48);
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu(
                List.of(new ObatEntity(6, "Kaptopril 25 mg", "tablet", 20, 50),
                        new ObatEntity(1, "Parasetamol 500 mg", "tablet", 300, 100)),
                Map.of(6, List.of(bets(6, "KAP-A01", 20, HARI_INI.plusDays(400))),
                        1, List.of(bets(1, "P-0412", 120, dekat), bets(1, "P-0500", 180, HARI_INI.plusDays(91)))));

        List<String> peringatan = PersediaanManager.terbitkanPeringatan(dataFarmasi);

        assertEquals(List.of("Stok Kaptopril 25 mg tinggal 20, di bawah ambang 50",
                "Bets P-0412 (Parasetamol 500 mg) kedaluwarsa " + dekat.format(DateTimeFormatter.ofPattern(
                        "dd-MM-yyyy"))), peringatan);
        assertEquals(90, dataFarmasi.hariDiminta);
    }

    @Test
    void terbitkanPeringatan_kosongBilaTidakAdaObatBermasalah() {
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
     * Memakai seed: obat 6 (Kaptopril 25 mg) stok 120 dengan bets KAP-A01. Variabel DB_* harus menunjuk basis data
     * yang sama dengan DB_UJI_URL. Penerimaan, bets, stok, dan audit yang dibuat dipulihkan setelah setiap uji.
     */
    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private static final int KAPTOPRIL = 6;

        private Connection koneksi;
        private int stokAwal;
        private int penerimaanTerakhir;
        private long auditTerakhir;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            stokAwal = hitung("SELECT stok FROM obat WHERE id_obat = " + KAPTOPRIL);
            penerimaanTerakhir = hitung("SELECT coalesce(max(id_penerimaan), 0) FROM penerimaan_obat");
            auditTerakhir = hitung("SELECT coalesce(max(id_audit), 0) FROM audit");
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Petugas Farmasi");
        }

        @AfterEach
        void pulihkan() throws SQLException {
            SesiAktif.lepas();
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM bets_obat WHERE id_penerimaan > " + penerimaanTerakhir);
                st.executeUpdate("DELETE FROM penerimaan_obat WHERE id_penerimaan > " + penerimaanTerakhir);
                st.executeUpdate("UPDATE obat SET stok = " + stokAwal + " WHERE id_obat = " + KAPTOPRIL);
                // Audit hanya dapat dihapus pemilik basis data (KNF05), jadi pemulihan ini butuh akun admin.
                st.executeUpdate("DELETE FROM audit WHERE id_audit > " + auditTerakhir);
            }
            koneksi.close();
        }

        @Test
        void catatPenerimaan_menambahStokMenyimpanBetsDanAudit() throws SQLException {
            PenerimaanObatEntity p = penerimaan("FK-UJI-01", HARI_INI,
                    bets(KAPTOPRIL, "KAP-UJI-1", 200, HARI_INI.plusYears(2)),
                    bets(KAPTOPRIL, "KAP-UJI-2", 100, HARI_INI.plusYears(3)));

            manager.catatPenerimaan(p);

            assertTrue(p.getIdPenerimaan() > penerimaanTerakhir);
            assertEquals(stokAwal + 300, hitung("SELECT stok FROM obat WHERE id_obat = " + KAPTOPRIL));
            assertEquals(2, hitung("SELECT count(*) FROM bets_obat WHERE id_penerimaan = " + p.getIdPenerimaan()));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir
                    + " AND deskripsi = 'Obat masuk Kaptopril 25 mg sejumlah 300, faktur FK-UJI-01'"));
        }

        @Test
        void catatPenerimaan_nomorBetsGandaDibatalkanSeluruhnya() throws SQLException {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.catatPenerimaan(penerimaan("FK-UJI-02", HARI_INI,
                            bets(KAPTOPRIL, "KAP-A01", 50, HARI_INI.plusYears(2)))));

            assertEquals("nomorBets", e.getMessage());
            assertEquals(stokAwal, hitung("SELECT stok FROM obat WHERE id_obat = " + KAPTOPRIL));
            assertEquals(penerimaanTerakhir, hitung("SELECT coalesce(max(id_penerimaan), 0) FROM penerimaan_obat"));
            assertEquals(0, hitung("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir));
        }

        @Test
        void terbitkanPeringatan_memakaiSeed() {
            List<String> peringatan = manager.terbitkanPeringatan();

            assertTrue(peringatan.stream().anyMatch(p -> p.startsWith("Stok Glibenklamid 5 mg tinggal 30")));
            assertTrue(peringatan.stream().anyMatch(p -> p.startsWith("Bets AML-A01 (Amlodipin 5 mg)")));
            assertTrue(peringatan.stream().noneMatch(p -> p.contains("AML-B01")));
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); var rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
