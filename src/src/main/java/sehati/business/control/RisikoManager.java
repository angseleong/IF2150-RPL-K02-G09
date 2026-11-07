package sehati.business.control;

import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PenandaRisikoEntity;
import sehati.business.entity.TandaVitalEntity;

import java.util.List;

/**
 * C21 - RisikoManager.
 *
 * Membandingkan tanda vital terhadap ambang dan riwayat tiga kunjungan, menerbitkan penanda risiko, dan memasukkan
 * pasien ke daftar pantau.
 */
public class RisikoManager {

    public List<PenandaRisikoEntity> evaluasiRisiko(KunjunganEntity kunjungan, TandaVitalEntity tv) {
        throw new UnsupportedOperationException("TODO");
    }

    private PenandaRisikoEntity terbitkanPenanda(int idKunjungan, String jenis) {
        throw new UnsupportedOperationException("TODO");
    }

    private void masukkanKeDaftarPantau(int idPasien, String jenis) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<PenandaRisikoEntity> ambilPenanda(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<DaftarPantauEntity> muatEntriAktif() {
        throw new UnsupportedOperationException("TODO");
    }
}
