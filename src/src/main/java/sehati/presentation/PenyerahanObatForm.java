package sehati.presentation;

import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ResepEntity;

import java.util.List;

/**
 * C10 - PenyerahanObatForm.
 *
 * Menampilkan rincian resep dan meneruskan konfirmasi "Obat Diserahkan" ke ApotekManager.
 */
public class PenyerahanObatForm {

    private ResepEntity rincianTampil;
    private String catatanPengembalian;

    public void tampilkanRincian(ResepEntity resep, List<BetsObatEntity> bets) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimKonfirmasi() {
        throw new UnsupportedOperationException("TODO");
    }

    public void bukaKolomCatatan() {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimPengembalian(String alasan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPesan(String pesan) {
        throw new UnsupportedOperationException("TODO");
    }
}
