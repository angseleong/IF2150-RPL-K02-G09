package sehati.business.control;

import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.TandaVitalEntity;

import java.util.List;

/**
 * C20 - SkriningManager.
 *
 * Memanggil pasien terdepan, memvalidasi rentang fisiologis tanda vital tanpa jalan pintas, menyimpan hasil
 * skrining, lalu meneruskannya ke RisikoManager.
 */
public class SkriningManager {

    public List<KunjunganEntity> muatAntrean() {
        throw new UnsupportedOperationException("TODO");
    }

    public KunjunganEntity panggilPasien() {
        throw new UnsupportedOperationException("TODO");
    }

    public KunjunganEntity lewatiPasien(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<String> simpanSkrining(int idKunjungan, String keluhan, TandaVitalEntity tv) {
        throw new UnsupportedOperationException("TODO");
    }

    private List<String> validasiRentang(TandaVitalEntity tv) {
        throw new UnsupportedOperationException("TODO");
    }
}
