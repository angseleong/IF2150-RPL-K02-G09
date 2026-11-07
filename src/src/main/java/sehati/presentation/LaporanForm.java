package sehati.presentation;

import sehati.business.entity.LaporanEntity;

import java.time.LocalDate;

/**
 * C13 - LaporanForm.
 *
 * Menampilkan pemilihan rentang tanggal dan pratinjau rekapitulasi, lalu meneruskan permintaan ekspor ke
 * LaporanManager.
 */
public class LaporanForm {

    private LocalDate tanggalMulai;
    private LocalDate tanggalAkhir;

    public void tampilkan() {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimRentang() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanRekapitulasi(LaporanEntity laporan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimPermintaanEkspor(String format, String direktori) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kunciTombolEkspor() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPesan(String pesan) {
        throw new UnsupportedOperationException("TODO");
    }
}
