package sehati.presentation;

import sehati.business.entity.PasienEntity;

import java.util.List;

/**
 * C02 - PasienForm.
 *
 * Menampilkan kolom pencarian dan formulir data pasien di loket, lalu meneruskan pencarian, pendaftaran, dan
 * penyuntingan ke PasienManager.
 */
public class PasienForm {

    private String kataKunci;
    private PasienEntity dataFormulir;

    public void tampilkan() {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimPencarian(String kataKunci) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanHasilCari(List<PasienEntity> hasil) {
        throw new UnsupportedOperationException("TODO");
    }

    public void bukaFormulir(PasienEntity pasien) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimDataPasien() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPesanValidasi(String pesan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanNotifikasi(String pesan) {
        throw new UnsupportedOperationException("TODO");
    }
}
