package sehati.presentation;

import sehati.business.entity.DaftarPantauEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * C12 - DaftarPantauForm.
 *
 * Menampilkan daftar pantau beserta penyaringnya, meneruskan penyaring dan catatan tindak lanjut ke PantauManager,
 * serta mengambil entri risiko aktif dari RisikoManager.
 */
public class DaftarPantauForm {

    private String penyaring;
    private List<DaftarPantauEntity> daftarTampil;

    public void tampilkanDaftar() {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimPenyaring(String jenisRisiko, LocalDate mulai, LocalDate akhir) {
        throw new UnsupportedOperationException("TODO");
    }

    public void bukaFormulirTindakLanjut(int idEntri) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimTindakLanjut() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanKeterangan(String pesan) {
        throw new UnsupportedOperationException("TODO");
    }
}
