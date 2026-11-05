package sehati.presentation;

import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.TandaVitalEntity;

import java.util.List;

/**
 * C06 - RekamMedisForm.
 *
 * Menampilkan ringkasan rekam medis dan grafik tren tanda vital yang disusun PemeriksaanManager.
 */
public class RekamMedisForm {

    private int idPasienTampil;
    private List<TandaVitalEntity> seriTren;

    public void tampilkanRingkasan(KunjunganEntity kunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanGrafikTren() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanTabelPengukuran(TandaVitalEntity nilai) {
        throw new UnsupportedOperationException("TODO");
    }

    public void aturModeBacaSaja() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanIndikatorLuring() {
        throw new UnsupportedOperationException("TODO");
    }
}
