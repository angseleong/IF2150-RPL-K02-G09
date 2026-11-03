package sehati.dataaccess;

import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.TindakLanjutEntity;

import java.sql.Connection;
import java.util.List;

/**
 * C60 - DataPantauBoundary.
 *
 * Membaca dan menyimpan DaftarPantauEntity dan TindakLanjutEntity beserta kueri penyaringnya.
 */
public class DataPantauBoundary {

    private Connection koneksi;

    public DataPantauBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    public List<DaftarPantauEntity> ambilEntri() {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanEntri(DaftarPantauEntity e) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanTindakLanjut(int idEntri, TindakLanjutEntity t) {
        throw new UnsupportedOperationException("TODO");
    }
}
