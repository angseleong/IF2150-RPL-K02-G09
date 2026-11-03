package sehati.business.control;

import sehati.business.entity.ObatEntity;
import sehati.business.entity.PenerimaanObatEntity;

import java.util.List;

/**
 * C25 - PersediaanManager.
 *
 * Mencatat obat masuk, menambah stok, dan menerbitkan peringatan stok di bawah ambang atau bets 90 hari menjelang
 * kedaluwarsa.
 */
public class PersediaanManager {

    public List<ObatEntity> muatDataObat() {
        throw new UnsupportedOperationException("TODO");
    }

    public void catatPenerimaan(PenerimaanObatEntity p) {
        throw new UnsupportedOperationException("TODO");
    }

    private List<String> validasiPenerimaan(PenerimaanObatEntity p) {
        throw new UnsupportedOperationException("TODO");
    }

    private ObatEntity tambahStok(int idObat, int jumlah) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<String> terbitkanPeringatan() {
        throw new UnsupportedOperationException("TODO");
    }
}
