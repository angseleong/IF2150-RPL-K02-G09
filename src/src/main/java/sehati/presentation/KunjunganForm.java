package sehati.presentation;

import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PoliEntity;

import java.util.List;

/**
 * C03 - KunjunganForm.
 *
 * Menampilkan pilihan poli dan tiket antrean, lalu meneruskan permintaan kunjungan ke KunjunganManager.
 */
public class KunjunganForm {

    private PasienEntity pasienTerpilih;
    private PoliEntity poliTerpilih;

    public void buka(PasienEntity pasien) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPilihanPoli(List<PoliEntity> daftarPoli) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimPermintaanKunjungan() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanTiketAntrean(KunjunganEntity kunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanKunjunganAktif(KunjunganEntity kunjungan) {
        throw new UnsupportedOperationException("TODO");
    }
}
