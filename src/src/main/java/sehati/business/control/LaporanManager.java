package sehati.business.control;

import sehati.business.entity.LaporanEntity;
import sehati.common.KoneksiBasisData;
import sehati.dataaccess.DataLaporanBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C27 - LaporanManager.
 *
 * Menghitung rekapitulasi kunjungan dan diagnosis terbanyak per rentang tanggal, lalu menghasilkan berkas PDF dan
 * CSV.
 */
public class LaporanManager {

    private static final Logger LOG = Logger.getLogger(LaporanManager.class.getName());

    public LaporanEntity hitungRekapitulasi(LocalDate mulai, LocalDate akhir) {
        if (!validasiRentang(mulai, akhir)) {
            throw new IllegalArgumentException("Rentang tanggal tidak valid");
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return hitungRekapitulasi(mulai, akhir, new DataLaporanBoundary(koneksi));
        } catch (SQLException e) {
            throw galatTeknis("Gagal menghitung rekapitulasi laporan", e);
        }
    }

    static LaporanEntity hitungRekapitulasi(LocalDate mulai, LocalDate akhir, DataLaporanBoundary data) {
        return data.agregasiKunjungan(mulai, akhir);
    }

    private boolean validasiRentang(LocalDate mulai, LocalDate akhir) {
        return mulai != null && akhir != null && !mulai.isAfter(akhir) && !akhir.isAfter(LocalDate.now());
    }

    public String ekspor(LaporanEntity laporan, String format, String direktori) {
        if (!"PDF".equals(format) && !"CSV".equals(format)) {
            throw new IllegalArgumentException("Format laporan harus PDF atau CSV");
        }
        if (laporan.kosong()) {
            throw new IllegalStateException("Tidak ada data pada rentang ini untuk diekspor");
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return ekspor(laporan, format, direktori, new DataLaporanBoundary(koneksi));
        } catch (SQLException e) {
            throw galatTeknis("Gagal mengekspor laporan", e);
        }
    }

    static String ekspor(LaporanEntity laporan, String format, String direktori, DataLaporanBoundary data) {
        String lokasi = data.tulisBerkas(laporan, format, direktori);
        laporan.setFormat(format);
        data.simpanLaporan(laporan);
        return lokasi;
    }

    private static IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ". Hubungi administrator bila berulang.", e);
    }
}
