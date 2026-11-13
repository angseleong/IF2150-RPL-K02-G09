package sehati.business.control;

import sehati.business.entity.LaporanEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;
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

    public LaporanEntity hitungRekapitulasi(LocalDate mulai, LocalDate akhir, Integer idPoli) {
        String galat = validasiRentang(mulai, akhir);
        if (galat != null) {
            throw new IllegalArgumentException(galat);
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return hitungRekapitulasi(mulai, akhir, idPoli, new DataLaporanBoundary(koneksi));
        } catch (SQLException e) {
            throw galatTeknis("Gagal menghitung rekapitulasi laporan", e);
        }
    }

    static LaporanEntity hitungRekapitulasi(LocalDate mulai, LocalDate akhir, Integer idPoli,
            DataLaporanBoundary data) {
        return data.agregasiKunjungan(mulai, akhir, idPoli);
    }

    // Pesan menyebut apa yang harus diperbaiki (UC10 alternatif 2), null bila rentang sah
    public static String validasiRentang(LocalDate mulai, LocalDate akhir) {
        if (mulai == null || akhir == null) {
            return "Isi tanggal mulai dan tanggal akhir.";
        }
        if (mulai.isAfter(akhir)) {
            return "Tanggal akhir lebih awal dari tanggal mulai. Perbaiki urutan tanggalnya.";
        }
        if (akhir.isAfter(LocalDate.now())) {
            return "Tanggal akhir tidak boleh melewati hari ini.";
        }
        return null;
    }

    public String ekspor(LaporanEntity laporan, String format, String direktori) {
        if (!"PDF".equals(format) && !"CSV".equals(format)) {
            throw new IllegalArgumentException("Format laporan harus PDF atau CSV");
        }
        if (laporan.kosong()) {
            throw new IllegalStateException("Tidak ada data pada rentang ini untuk diekspor");
        }
        SesiAktif.wajibPeran("Petugas Administrasi");
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
