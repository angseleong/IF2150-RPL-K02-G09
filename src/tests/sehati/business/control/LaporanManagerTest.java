package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import sehati.business.entity.LaporanEntity;
import sehati.dataaccess.DataLaporanBoundary;

class LaporanManagerTest {

    private static final LocalDate HARI_INI = LocalDate.now();
    private static final LocalDate MULAI = HARI_INI.minusDays(7);
    private static final LocalDate AKHIR = HARI_INI;

    @Test
    void hitungRekapitulasi_memakaiBoundary() {
        LaporanEntity hasil = new LaporanEntity(0, MULAI, AKHIR, null, "total;semua kunjungan;5");
        DataLaporanPalsu data = new DataLaporanPalsu(hasil);

        assertSame(hasil, LaporanManager.hitungRekapitulasi(MULAI, AKHIR, data));
    }

    @Test
    void hitungRekapitulasi_menolakRentangTerbalik() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new LaporanManager().hitungRekapitulasi(AKHIR, MULAI));

        assertEquals("Rentang tanggal tidak valid", e.getMessage());
    }

    @Test
    void hitungRekapitulasi_menolakTanggalMasaDepan() {
        assertThrows(IllegalArgumentException.class,
                () -> new LaporanManager().hitungRekapitulasi(HARI_INI, HARI_INI.plusDays(1)));
    }

    @Test
    void hitungRekapitulasi_menolakTanggalNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new LaporanManager().hitungRekapitulasi(null, AKHIR));
    }

    @Test
    void ekspor_menolakFormatLain() {
        LaporanEntity laporan = laporan(5);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new LaporanManager().ekspor(laporan, "XLS", "/tmp"));

        assertEquals("Format laporan harus PDF atau CSV", e.getMessage());
    }

    @Test
    void ekspor_menolakLaporanKosong() {
        LaporanEntity laporan = laporan(0);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new LaporanManager().ekspor(laporan, "PDF", "/tmp"));

        assertEquals("Tidak ada data pada rentang ini untuk diekspor", e.getMessage());
    }

    @Test
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
        public LaporanEntity agregasiKunjungan(LocalDate mulai, LocalDate akhir) {
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
}
