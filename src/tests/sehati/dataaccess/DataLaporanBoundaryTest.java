package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.LaporanEntity;

class DataLaporanBoundaryTest {

    private static final LocalDate MULAI = LocalDate.of(2026, 11, 1);
    private static final LocalDate AKHIR = LocalDate.of(2026, 11, 8);

    @Test
    void agregasi_menyusunRingkasanDanMemulihkanKoneksi() {
        JdbcPalsu jdbc = new JdbcPalsu()
                .hasil("SELECT p.nama_poli", List.of(
                        JdbcPalsu.baris("nama_poli", "Gigi", "jumlah", 2),
                        JdbcPalsu.baris("nama_poli", "Umum", "jumlah", 3)))
                .hasil("SELECT k.status", List.of(
                        JdbcPalsu.baris("status", "selesai", "jumlah", 4),
                        JdbcPalsu.baris("status", "tidak hadir", "jumlah", 1)))
                .hasil("SELECT d.kode_icd10", List.of(
                        JdbcPalsu.baris("kode_icd10", "E11.9", "deskripsi", "Diabetes melitus", "jumlah", 2)));

        LaporanEntity laporan = new DataLaporanBoundary(jdbc.koneksi()).agregasiKunjungan(MULAI, AKHIR, null);

        assertEquals("total;semua kunjungan;5\n"
                + "poli;Gigi;2\npoli;Umum;3\n"
                + "status;selesai;4\nstatus;tidak hadir;1\n"
                + "diagnosis;E11.9 Diabetes melitus;2", laporan.getRingkasan());
        assertEquals(0, laporan.getIdLaporan());
        assertNull(laporan.getFormat());
        assertEquals(MULAI, laporan.getTanggalMulai());
        assertEquals(AKHIR, laporan.getTanggalAkhir());
        assertTrue(jdbc.peristiwa.contains("commit"));
        assertTrue(jdbc.autoCommit());
        assertFalse(jdbc.bacaSaja());
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, jdbc.isolasi());
    }

    @Test
    void agregasi_penyaringPoliDikirimKeKetigaKueri() {
        JdbcPalsu jdbc = new JdbcPalsu()
                .hasil("SELECT p.nama_poli", List.of(JdbcPalsu.baris("nama_poli", "Gigi", "jumlah", 2)))
                .hasil("SELECT k.status", List.of(JdbcPalsu.baris("status", "selesai", "jumlah", 2)))
                .hasil("SELECT d.kode_icd10", List.of());

        LaporanEntity laporan = new DataLaporanBoundary(jdbc.koneksi()).agregasiKunjungan(MULAI, AKHIR, 7);

        assertEquals("Gigi", laporan.getNamaPoli());

        for (String kueri : List.of("SELECT p.nama_poli", "SELECT k.status", "SELECT d.kode_icd10")) {
            JdbcPalsu.Jalan jalan = jdbc.dijalankanMemuat(kueri).get(0);
            assertEquals(7, jalan.parameter().get(3));
            assertEquals(7, jalan.parameter().get(4));
        }
    }

    @Test
    void agregasi_menggantiTitikKomaPadaLabel() {
        JdbcPalsu jdbc = new JdbcPalsu()
                .hasil("SELECT p.nama_poli", List.of(JdbcPalsu.baris("nama_poli", "Umum;Pagi", "jumlah", 1)))
                .hasil("SELECT k.status", List.of(JdbcPalsu.baris("status", "selesai", "jumlah", 1)))
                .hasil("SELECT d.kode_icd10", List.of());

        LaporanEntity laporan = new DataLaporanBoundary(jdbc.koneksi()).agregasiKunjungan(MULAI, AKHIR, null);

        assertTrue(laporan.getRingkasan().contains("poli;Umum,Pagi;1"));
    }

    @Test
    void agregasi_gagalKoneksiMembatalkanTransaksiDanMemulihkanKoneksi() {
        JdbcPalsu jdbc = new JdbcPalsu().gagal("SELECT p.nama_poli", "08006", "putus");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new DataLaporanBoundary(jdbc.koneksi()).agregasiKunjungan(MULAI, AKHIR, null));

        assertTrue(e.getMessage().startsWith("Gagal menghitung rekapitulasi kunjungan"));
        assertTrue(jdbc.peristiwa.contains("rollback"));
        assertTrue(jdbc.autoCommit());
        assertFalse(jdbc.bacaSaja());
        assertEquals(Connection.TRANSACTION_READ_COMMITTED, jdbc.isolasi());
    }

    @Test
    void simpanLaporan_mengisiIdDariReturning() {
        JdbcPalsu jdbc = new JdbcPalsu().hasil("INSERT INTO laporan", List.of(JdbcPalsu.baris("id_laporan", 9)));
        LaporanEntity laporan = new LaporanEntity(0, MULAI, AKHIR, "CSV", "total;semua kunjungan;0");

        new DataLaporanBoundary(jdbc.koneksi()).simpanLaporan(laporan);

        assertEquals(9, laporan.getIdLaporan());
        JdbcPalsu.Jalan jalan = jdbc.dijalankanMemuat("INSERT INTO laporan").get(0);
        assertEquals(Date.valueOf(MULAI), jalan.parameter().get(1));
        assertEquals(Date.valueOf(AKHIR), jalan.parameter().get(2));
        assertEquals("CSV", jalan.parameter().get(3));
        assertEquals("total;semua kunjungan;0", jalan.parameter().get(4));
    }

    @Test
    void tulisBerkas_csvMenulisHeaderDanRingkasan() throws IOException {
        Path folder = Files.createTempDirectory("laporan-csv");
        try {
            LaporanEntity laporan = new LaporanEntity(0, MULAI, AKHIR, null,
                    "total;semua kunjungan;5\npoli;Umum;3\nstatus;selesai;5\ndiagnosis;E11.9 Diabetes;2");

            String lokasi = new DataLaporanBoundary(null).tulisBerkas(laporan, "CSV", folder.toString());

            assertTrue(lokasi.endsWith("laporan-sehati_2026-11-01_2026-11-08.csv"));
            List<String> baris = Files.readAllLines(Path.of(lokasi), StandardCharsets.UTF_8);
            assertEquals("bagian;label;jumlah", baris.get(0));
            assertEquals("total;semua kunjungan;5", baris.get(1));
            assertEquals(5, baris.size());
        } finally {
            hapus(folder);
        }
    }

    @Test
    void tulisBerkas_namaPoliMasukNamaBerkasAgarTidakSalingMenimpa() throws IOException {
        Path folder = Files.createTempDirectory("laporan-poli");
        try {
            LaporanEntity umum = new LaporanEntity(0, MULAI, AKHIR, null, "total;semua kunjungan;3\npoli;Poli Umum;3");
            umum.setNamaPoli("Poli Umum");
            LaporanEntity gigi = new LaporanEntity(0, MULAI, AKHIR, null, "total;semua kunjungan;2\npoli;Poli Gigi;2");
            gigi.setNamaPoli("Poli Gigi");

            String lokasiUmum = new DataLaporanBoundary(null).tulisBerkas(umum, "CSV", folder.toString());
            String lokasiGigi = new DataLaporanBoundary(null).tulisBerkas(gigi, "CSV", folder.toString());

            assertTrue(lokasiUmum.endsWith("laporan-sehati_2026-11-01_2026-11-08_poli-umum.csv"));
            assertTrue(lokasiGigi.endsWith("laporan-sehati_2026-11-01_2026-11-08_poli-gigi.csv"));
            assertTrue(Files.exists(Path.of(lokasiUmum)));
        } finally {
            hapus(folder);
        }
    }

    @Test
    void tulisBerkas_pdfMenulisBerkasPdf() throws IOException {
        Path folder = Files.createTempDirectory("laporan-pdf");
        try {
            LaporanEntity laporan = new LaporanEntity(0, MULAI, AKHIR, null,
                    "total;semua kunjungan;5\npoli;Umum;3");

            String lokasi = new DataLaporanBoundary(null).tulisBerkas(laporan, "PDF", folder.toString());

            assertTrue(lokasi.endsWith("laporan-sehati_2026-11-01_2026-11-08.pdf"));
            byte[] isi = Files.readAllBytes(Path.of(lokasi));
            assertTrue(isi.length > 100);
            assertEquals('%', (char) isi[0]);
            assertEquals('P', (char) isi[1]);
            assertEquals('D', (char) isi[2]);
            assertEquals('F', (char) isi[3]);
        } finally {
            hapus(folder);
        }
    }

    @Test
    void tulisBerkas_formatTidakDikenalDitolak() {
        LaporanEntity laporan = new LaporanEntity(0, MULAI, AKHIR, null, "total;semua kunjungan;1");

        assertThrows(IllegalArgumentException.class,
                () -> new DataLaporanBoundary(null).tulisBerkas(laporan, "XLS", "/tmp"));
    }

    private static void hapus(Path p) throws IOException {
        try (var aliran = Files.walk(p)) {
            aliran.sorted(java.util.Comparator.reverseOrder()).forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private DataLaporanBoundary data;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            koneksi.setAutoCommit(false);
            data = new DataLaporanBoundary(koneksi);
        }

        @AfterEach
        void tutup() throws SQLException {
            koneksi.rollback();
            koneksi.close();
        }

        @Test
        void agregasi_mengembalikanRingkasanTotal() {
            LaporanEntity laporan = data.agregasiKunjungan(LocalDate.now().minusDays(30), LocalDate.now(), null);

            assertNotNull(laporan.getRingkasan());
            assertTrue(laporan.getRingkasan().startsWith("total;semua kunjungan;"));
        }

        @Test
        void agregasi_poliTanpaKunjunganMengembalikanNol() {
            LaporanEntity laporan = data.agregasiKunjungan(LocalDate.now().minusDays(30), LocalDate.now(), -1);

            assertEquals("total;semua kunjungan;0", laporan.getRingkasan());
        }

        @Test
        void simpanLaporan_mengisiId() {
            LaporanEntity laporan = new LaporanEntity(0, LocalDate.now().minusDays(1), LocalDate.now(), "CSV",
                    "total;semua kunjungan;1");

            data.simpanLaporan(laporan);

            assertTrue(laporan.getIdLaporan() > 0);
        }
    }
}
