package sehati.presentation;

import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.KunjunganEntity;

import java.util.List;

/**
 * C08 - ResepForm.
 *
 * Menampilkan formulir resep beserta indikator stok, lalu meneruskan item resep ke ResepManager.
 */
public class ResepForm {

    private List<ItemResepEntity> isianItem;
    private String indikatorStok;

    public void tampilkanFormulir(KunjunganEntity kunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimItem(int idObat, int jumlah, String dosis, String aturanPakai) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanIndikatorStok(boolean tersedia, int sisa) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimResep() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPeringatanStok(int sisa) {
        throw new UnsupportedOperationException("TODO");
    }
}
