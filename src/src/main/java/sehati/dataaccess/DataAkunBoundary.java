package sehati.dataaccess;

import sehati.business.entity.AuditEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.SesiEntity;

import java.sql.Connection;

/**
 * C56 - DataAkunBoundary.
 *
 * Membaca dan menyimpan PenggunaEntity, SesiEntity, dan AuditEntity. Catatan audit hanya dapat ditambah, tidak
 * dapat diubah atau dihapus.
 */
public class DataAkunBoundary {

    private Connection koneksi;

    public DataAkunBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    public PenggunaEntity cariPengguna(String nama) {
        throw new UnsupportedOperationException("TODO");
    }

    public PenggunaEntity ambilPengguna(int idPengguna) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean namaPenggunaDipakai(String nama) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanPengguna(PenggunaEntity p) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanSesi(SesiEntity s) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tutupSesiAktif(int idPengguna) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanAudit(AuditEntity a) {
        throw new UnsupportedOperationException("TODO");
    }
}
