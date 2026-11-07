package sehati.presentation;

import sehati.business.entity.KodeICD10Entity;

import java.util.List;

/**
 * C07 - PemeriksaanForm.
 *
 * Menampilkan formulir anamnesis, diagnosis ICD-10, tindakan, dan jadwal kontrol, lalu meneruskan isiannya ke
 * PemeriksaanManager.
 */
public class PemeriksaanForm {

    private String isianAnamnesis;
    private String kataKunciDiagnosis;

    public void kirimKataKunciDiagnosis(String kataKunci) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanSaranKode(List<KodeICD10Entity> daftar) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimHasilPemeriksaan() {
        throw new UnsupportedOperationException("TODO");
    }

    public void kirimFinal() {
        throw new UnsupportedOperationException("TODO");
    }

    public void kunciKolomKlinis() {
        throw new UnsupportedOperationException("TODO");
    }

    public void tampilkanPesan(String pesan) {
        throw new UnsupportedOperationException("TODO");
    }
}
