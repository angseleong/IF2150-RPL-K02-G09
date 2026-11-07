package sehati.dataaccess;

import sehati.business.entity.AntreanEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PoliEntity;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * C58 - DataKunjunganBoundary.
 *
 * Membaca dan menyimpan KunjunganEntity, PoliEntity, dan AntreanEntity, serta mengunci baris urutan poli saat
 * nomor antrean diterbitkan.
 */
public class DataKunjunganBoundary {

    private Connection koneksi;

    public DataKunjunganBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    public List<PoliEntity> ambilSemuaPoli() {
        throw new UnsupportedOperationException("TODO");
    }

    public PoliEntity kunciPoli(int idPoli) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanPoli(PoliEntity p) {
        throw new UnsupportedOperationException("TODO");
    }

    public KunjunganEntity kunjunganAktif(int idPasien, LocalDate tanggal) {
        throw new UnsupportedOperationException("TODO");
    }

    public KunjunganEntity ambilKunjungan(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanKunjungan(KunjunganEntity k) {
        throw new UnsupportedOperationException("TODO");
    }

    public AntreanEntity ambilAntrean(String jenis) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanAntrean(AntreanEntity a) {
        throw new UnsupportedOperationException("TODO");
    }
}
