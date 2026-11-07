package sehati.business.control;

import sehati.business.entity.LaporanEntity;

import java.time.LocalDate;

/**
 * C27 - LaporanManager.
 *
 * Menghitung rekapitulasi kunjungan dan diagnosis terbanyak per rentang tanggal, lalu menghasilkan berkas PDF dan
 * CSV.
 */
public class LaporanManager {

    public LaporanEntity hitungRekapitulasi(LocalDate mulai, LocalDate akhir) {
        throw new UnsupportedOperationException("TODO");
    }

    private boolean validasiRentang(LocalDate mulai, LocalDate akhir) {
        throw new UnsupportedOperationException("TODO");
    }

    public String ekspor(LaporanEntity laporan, String format, String direktori) {
        throw new UnsupportedOperationException("TODO");
    }
}
