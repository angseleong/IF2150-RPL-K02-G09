package sehati.business.entity;

import java.util.Locale;

/**
 * C44 - KodeICD10Entity.
 *
 * Merepresentasikan data master kode ICD-10 yang dicari dokter saat mengisi diagnosis.
 */
public class KodeICD10Entity {

    private String kode;
    private String deskripsi;
    private String bab;

    public KodeICD10Entity() {
    }

    public KodeICD10Entity(String kode, String deskripsi, String bab) {
        this.kode = kode;
        this.deskripsi = deskripsi;
        this.bab = bab;
    }

    /**
     * Menyaring ulang saran di layar dengan aturan yang sama dengan Q-C59-11 (Algo-C44-01).
     */
    public boolean cocok(String kataKunci) {
        String teks = kataKunci == null ? "" : kataKunci.strip().toLowerCase(Locale.ROOT);
        if (teks.isEmpty()) {
            return false;
        }
        return kode.toLowerCase(Locale.ROOT).startsWith(teks) || deskripsi.toLowerCase(Locale.ROOT).contains(teks);
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getBab() {
        return bab;
    }

    public void setBab(String bab) {
        this.bab = bab;
    }
}
