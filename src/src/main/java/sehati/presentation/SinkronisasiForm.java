package sehati.presentation;

import sehati.business.entity.BundelFHIREntity;

import java.util.List;

/**
 * C14 - SinkronisasiForm.
 *
 * Menampilkan status setiap bundel dan indikator koneksi, lalu meneruskan perintah sinkronisasi atau "Ekspor
 * Bundel" ke SinkronisasiManager.
 */
public class SinkronisasiForm {

    private List<BundelFHIREntity> daftarStatus;

    public void tampilkanStatus() {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimPermintaanSinkron() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanHasil(List<BundelFHIREntity> hasil) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanTombolEkspor() {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimPermintaanEkspor(String lokasi) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPesan(String pesan) {
        throw new UnsupportedOperationException("TODO");
    }
}
