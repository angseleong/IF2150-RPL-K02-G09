package sehati.business.control;

import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.SesiEntity;

/**
 * C17 - AutentikasiManager.
 *
 * Memeriksa kredensial terhadap hash kata sandi, membuka dan mengakhiri sesi (termasuk batas diam 15 menit), serta
 * memeriksa hak akses peran untuk setiap layar.
 */
public class AutentikasiManager {

    private int percobaanGagal;

    public SesiEntity autentikasi(String nama, String sandi) {
        throw new UnsupportedOperationException("TODO");
    }

    private SesiEntity bukaSesi(PenggunaEntity p) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean periksaHakAkses(String peran, String layar) {
        throw new UnsupportedOperationException("TODO");
    }
}
