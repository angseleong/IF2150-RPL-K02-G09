package sehati.business.control;

import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.ResepEntity;

import java.util.List;

/**
 * C23 - ResepManager.
 *
 * Menyusun item resep, memeriksa kecukupan stok, dan meneruskan resep yang lolos ke antrean apotek.
 */
public class ResepManager {

    public List<ObatEntity> muatDataObat() {
        throw new UnsupportedOperationException("TODO");
    }

    public ItemResepEntity tambahItem(int idObat, int jumlah, String dosis, String aturanPakai) {
        throw new UnsupportedOperationException("TODO");
    }

    private boolean validasiStok(ItemResepEntity item) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResepEntity kirimKeApotek(int idKunjungan, List<ItemResepEntity> items) {
        throw new UnsupportedOperationException("TODO");
    }
}
