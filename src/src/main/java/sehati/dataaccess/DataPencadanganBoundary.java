package sehati.dataaccess;

import sehati.business.entity.PencadanganEntity;

import java.sql.Connection;

/**
 * C64 - DataPencadanganBoundary.
 *
 * Menjalankan `pg_dump` sesuai jadwal ke media penyimpanan terpisah dan menyimpan PencadanganEntity.
 */
public class DataPencadanganBoundary {

    private Connection koneksi;
    private String lokasiMedia;

    public DataPencadanganBoundary(Connection koneksi, String lokasiMedia) {
        this.koneksi = koneksi;
        this.lokasiMedia = lokasiMedia;
    }

    public PencadanganEntity ambilTerakhir() {
        throw new UnsupportedOperationException("TODO");
    }

    public PencadanganEntity jalankanPgDump(String lokasi) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean verifikasiArsip(PencadanganEntity p) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanCatatan(PencadanganEntity p) {
        throw new UnsupportedOperationException("TODO");
    }
}
