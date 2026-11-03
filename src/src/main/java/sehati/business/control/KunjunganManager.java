package sehati.business.control;

import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PoliEntity;

import java.util.List;

/**
 * C19 - KunjunganManager.
 *
 * Membuka kunjungan, menolak kunjungan ganda, dan menerbitkan nomor antrean berurutan per poli per hari.
 */
public class KunjunganManager {

    public List<PoliEntity> daftarPoli() {
        throw new UnsupportedOperationException("TODO");
    }

    public KunjunganEntity bukaKunjungan(int idPasien, int idPoli) {
        throw new UnsupportedOperationException("TODO");
    }

    private KunjunganEntity cegahKunjunganGanda(int idPasien) {
        throw new UnsupportedOperationException("TODO");
    }

    private int terbitkanNomorAntrean(int idPoli) {
        throw new UnsupportedOperationException("TODO");
    }
}
