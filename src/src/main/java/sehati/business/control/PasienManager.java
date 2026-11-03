package sehati.business.control;

import sehati.business.entity.PasienEntity;

import java.util.List;

/**
 * C18 - PasienManager.
 *
 * Menjalankan pencarian parsial, memvalidasi NIK 16 digit dan keunikannya, menerbitkan nomor rekam medis, dan
 * memutakhirkan data pasien.
 */
public class PasienManager {

    public List<PasienEntity> cariPasien(String kataKunci) {
        throw new UnsupportedOperationException("TODO");
    }

    public PasienEntity daftarkanPasien(PasienEntity data) {
        throw new UnsupportedOperationException("TODO");
    }

    public void perbaruiPasien(PasienEntity data) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean validasiNIK(String nik) {
        throw new UnsupportedOperationException("TODO");
    }

    private String terbitkanNomorRM() {
        throw new UnsupportedOperationException("TODO");
    }
}
