package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.LaporanEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataLaporanBoundary;

// Pengujian unit C27 LaporanManager (HIUPL Subbab 4.2, C27-UT01 sampai C27-UT11).
@DisplayName("C27 LaporanManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class LaporanManagerTest {

    private static final LocalDate HARI_INI = LocalDate.now();
    private static final LocalDate MULAI = HARI_INI.minusDays(7);
    private static final LocalDate AKHIR = HARI_INI;

    @Test
    @DisplayName("C27-UT01 hitungRekapitulasi normal: memakai DataLaporanBoundary")
    void hitungRekapitulasi_memakaiBoundary() {
        LaporanEntity hasil = new LaporanEntity(0, MULAI, AKHIR, null, "total;semua kunjungan;5");
        DataLaporanPalsu data = new DataLaporanPalsu(hasil);

        assertSame(hasil, LaporanManager.hitungRekapitulasi(MULAI, AKHIR, null, data));
    }

    @Test
    @DisplayName("C27-UT11 hitungRekapitulasi batas: rentang satu hari, mulai dan akhir sama dengan hari ini")
    void hitungRekapitulasi_rentangSatuHari() {
        LaporanEntity hasil = new LaporanEntity(0, HARI_INI, HARI_INI, null, "total;semua kunjungan;1");
        DataLaporanPalsu data = new DataLaporanPalsu(hasil);

        assertSame(hasil, LaporanManager.hitungRekapitulasi(HARI_INI, HARI_INI, null, data));
    }

    @Test
    @DisplayName("C27-UT02 hitungRekapitulasi negatif: rentang terbalik ditolak")
    void hitungRekapitulasi_menolakRentangTerbalik() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new LaporanManager().hitungRekapitulasi(AKHIR, MULAI, null));

        assertEquals("Tanggal akhir lebih awal dari tanggal mulai. Perbaiki urutan tanggalnya.", e.getMessage());
    }

    @Test
    @DisplayName("C27-UT03 validasiRentang batas: pesan menyebut apa yang harus diperbaiki")
    void validasiRentang_pesanMenyebutYangHarusDiperbaiki() {
        assertEquals("Isi tanggal mulai dan tanggal akhir.", LaporanManager.validasiRentang(null, HARI_INI));
        assertEquals(null, LaporanManager.validasiRentang(HARI_INI, HARI_INI));
    }

    @Test
    @DisplayName("C27-UT04 hitungRekapitulasi negatif: tanggal akhir masa depan ditolak")
    void hitungRekapitulasi_menolakTanggalMasaDepan() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new LaporanManager().hitungRekapitulasi(HARI_INI, HARI_INI.plusDays(1), null));

        assertEquals("Tanggal akhir tidak boleh melewati hari ini.", e.getMessage());
    }

    @Test
    @DisplayName("C27-UT05 hitungRekapitulasi negatif: tanggal null ditolak")
    void hitungRekapitulasi_menolakTanggalNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new LaporanManager().hitungRekapitulasi(null, AKHIR, null));
    }

    @Test
    @DisplayName("C27-UT06 ekspor negatif: format selain PDF atau CSV ditolak")
    void ekspor_menolakFormatLain() {
        LaporanEntity laporan = laporan(5);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new LaporanManager().ekspor(laporan, "XLS", "/tmp"));

        assertEquals("Format laporan harus PDF atau CSV", e.getMessage());
    }

    @Test
    @DisplayName("C27-UT07 ekspor negatif: laporan kosong ditolak")
    void ekspor_menolakLaporanKosong() {
        LaporanEntity laporan = laporan(0);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new LaporanManager().ekspor(laporan, "PDF", "/tmp"));

        assertEquals("Tidak ada data pada rentang ini untuk diekspor", e.getMessage());
    }

    @Test
    @DisplayName("C27-UT08 ekspor normal: menulis berkas, menyimpan laporan, dan mengembalikan lokasi")
    void ekspor_menulisBerkasMenyimpanLaporanDanMengembalikanLokasi() {
        LaporanEntity laporan = laporan(5);
        DataLaporanPalsu data = new DataLaporanPalsu(null);

        String lokasi = LaporanManager.ekspor(laporan, "PDF", "/tmp", data);

        assertEquals("/tmp/laporan.pdf", lokasi);
        assertEquals("PDF", laporan.getFormat());
        assertTrue(data.tersimpan);
        assertSame(laporan, data.terakhirDisimpan);
    }

    private static LaporanEntity laporan(int total) {
        return new LaporanEntity(0, MULAI, AKHIR, null, "total;semua kunjungan;" + total);
    }

    private static final class DataLaporanPalsu extends DataLaporanBoundary {

        private final LaporanEntity hasil;
        private boolean tersimpan;
        private LaporanEntity terakhirDisimpan;

        private DataLaporanPalsu(LaporanEntity hasil) {
            super(null);
            this.hasil = hasil;
        }

        @Override
        public LaporanEntity agregasiKunjungan(LocalDate mulai, LocalDate akhir, Integer idPoli) {
            return hasil;
        }

        @Override
        public String tulisBerkas(LaporanEntity l, String format, String direktori) {
            return "/tmp/laporan.pdf";
        }

        @Override
        public void simpanLaporan(LaporanEntity l) {
            tersimpan = true;
            terakhirDisimpan = l;
        }
    }

    // Uji jalur publik yang membuka koneksi basis data. Aktif bila DB_UJI_URL diisi.
    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private int laporanBaru;

        @AfterEach
        void bersihkan() throws SQLException {
            SesiAktif.lepas();
            if (laporanBaru > 0) {
                try (Connection k = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
                        Statement st = k.createStatement()) {
                    st.executeUpdate("DELETE FROM laporan WHERE id_laporan = " + laporanBaru);
                }
                laporanBaru = 0;
            }
        }

        @Test
        @DisplayName("C27-UT09 hitungRekapitulasi normal: membuka koneksi basis data dan membaca agregasi")
        void hitungRekapitulasi_membukaKoneksiBasisData() {
            LaporanEntity laporan = new LaporanManager().hitungRekapitulasi(
                    LocalDate.now().minusDays(30), LocalDate.now(), null);

            assertNotNull(laporan.getRingkasan());
            assertTrue(laporan.getRingkasan().startsWith("total;semua kunjungan;"));
        }

        @Test
        @DisplayName("C27-UT10 ekspor normal: menulis berkas dan menyimpan laporan ke basis data")
        void ekspor_menulisBerkasDanMenyimpan() throws IOException {
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Petugas Administrasi");
            LaporanEntity laporan = new LaporanEntity(0, LocalDate.now().minusDays(1), LocalDate.now(), null,
                    "total;semua kunjungan;1");
            Path folder = Files.createTempDirectory("laporan-integrasi");
            try {
                String lokasi = new LaporanManager().ekspor(laporan, "CSV", folder.toString());

                assertTrue(Files.isRegularFile(Path.of(lokasi)));
                assertTrue(laporan.getIdLaporan() > 0);
                assertEquals("CSV", laporan.getFormat());
                laporanBaru = laporan.getIdLaporan();
            } finally {
                hapus(folder);
            }
        }

        private void hapus(Path p) throws IOException {
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
    }
}
