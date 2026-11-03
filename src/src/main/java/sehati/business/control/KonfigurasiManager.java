package sehati.business.control;

import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.PencadanganEntity;
import sehati.business.entity.PenggunaEntity;

/**
 * C29 - KonfigurasiManager.
 *
 * Mengurus akun staf, data master obat, poli, dan ambang risiko, serta menjadwalkan pencadangan dan memantau
 * hasilnya.
 */
public class KonfigurasiManager {

    public void muatDataMaster() {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanAkun(PenggunaEntity p, String sandi) {
        throw new UnsupportedOperationException("TODO");
    }

    public void nonaktifkanAkun(int idPengguna) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanDataMaster(AmbangRisikoEntity a) {
        throw new UnsupportedOperationException("TODO");
    }

    public PencadanganEntity muatStatusPencadangan() {
        throw new UnsupportedOperationException("TODO");
    }

    public PencadanganEntity jalankanPencadangan() {
        throw new UnsupportedOperationException("TODO");
    }
}
