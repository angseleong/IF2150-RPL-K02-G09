package sehati.dataaccess;

import sehati.business.entity.PasienEntity;
import sehati.business.entity.RekamMedisEntity;

import java.sql.Connection;
import java.util.List;

/**
 * C57 - DataPasienBoundary.
 *
 * Menjalankan pencarian parsial berindeks serta membaca dan menyimpan PasienEntity dan RekamMedisEntity.
 */
public class DataPasienBoundary {

    private Connection koneksi;

    public DataPasienBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    public List<PasienEntity> cariParsial(String kataKunci) {
        throw new UnsupportedOperationException("TODO");
    }

    public PasienEntity ambilPasien(int idPasien) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean nikTerdaftar(String nik) {
        throw new UnsupportedOperationException("TODO");
    }

    public String nomorRMBerikutnya() {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanPasien(PasienEntity p) {
        throw new UnsupportedOperationException("TODO");
    }

    public RekamMedisEntity ambilRekamMedis(int idPasien) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanRekamMedis(RekamMedisEntity rm) {
        throw new UnsupportedOperationException("TODO");
    }
}
