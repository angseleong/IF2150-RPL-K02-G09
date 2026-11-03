package sehati.dataaccess;

import sehati.business.entity.BundelFHIREntity;

import java.sql.Connection;
import java.util.List;

/**
 * C63 - DataSinkronisasiBoundary.
 *
 * Membaca dan menyimpan BundelFHIREntity dengan isi bundel pada kolom JSONB, serta menulis berkas ekspor bundel
 * saat luring.
 */
public class DataSinkronisasiBoundary {

    private Connection koneksi;

    public DataSinkronisasiBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    public void simpanBundel(BundelFHIREntity b) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<BundelFHIREntity> ambilTertunda() {
        throw new UnsupportedOperationException("TODO");
    }

    public void perbaruiBundel(BundelFHIREntity b) {
        throw new UnsupportedOperationException("TODO");
    }

    public String tulisArsipTerenkripsi(List<BundelFHIREntity> daftar, String lokasi) {
        throw new UnsupportedOperationException("TODO");
    }
}
