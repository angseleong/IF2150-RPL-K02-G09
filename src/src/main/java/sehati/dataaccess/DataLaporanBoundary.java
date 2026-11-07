package sehati.dataaccess;

import sehati.business.entity.LaporanEntity;

import java.sql.Connection;
import java.time.LocalDate;

/**
 * C62 - DataLaporanBoundary.
 *
 * Menjalankan kueri agregasi kunjungan dan diagnosis, menyimpan LaporanEntity, dan menulis berkas PDF dan CSV ke
 * penyimpanan lokal.
 */
public class DataLaporanBoundary {

    private Connection koneksi;

    public DataLaporanBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    public LaporanEntity agregasiKunjungan(LocalDate mulai, LocalDate akhir) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanLaporan(LaporanEntity l) {
        throw new UnsupportedOperationException("TODO");
    }

    public String tulisBerkas(LaporanEntity l, String format, String direktori) {
        throw new UnsupportedOperationException("TODO");
    }
}
