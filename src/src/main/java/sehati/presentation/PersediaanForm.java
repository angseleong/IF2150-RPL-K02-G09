package sehati.presentation;

import sehati.business.entity.ObatEntity;
import sehati.business.entity.PenerimaanObatEntity;

import java.util.List;

/**
 * C11 - PersediaanForm.
 *
 * Menampilkan formulir obat masuk dan dasbor peringatan persediaan, lalu meneruskan penerimaan ke
 * PersediaanManager.
 */
public class PersediaanForm {

    private PenerimaanObatEntity isianPenerimaan;
    private List<String> daftarPeringatan;

    public void tampilkanFormulir() {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimPenerimaan() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanSaldo(ObatEntity obat) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPeringatan(List<String> peringatan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPesanKesalahan(List<String> kolom) {
        throw new UnsupportedOperationException("TODO");
    }
}
