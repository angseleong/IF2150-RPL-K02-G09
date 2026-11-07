package sehati.presentation;

import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PenandaRisikoEntity;
import sehati.business.entity.TandaVitalEntity;

import java.util.List;

/**
 * C05 - SkriningForm.
 *
 * Menampilkan formulir keluhan dan tanda vital, meneruskan isian ke SkriningManager, lalu meminta penanda risiko
 * dari RisikoManager dan menampilkan peringatan rentang serta penanda risikonya.
 */
public class SkriningForm {

    private String isianKeluhan;
    private TandaVitalEntity isianUkuran;

    public void tampilkanFormulir(KunjunganEntity kunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimHasilSkrining() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPeringatanRentang(List<String> kolom) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPenandaRisiko(List<PenandaRisikoEntity> penanda) {
        throw new UnsupportedOperationException("TODO");
    }
}
