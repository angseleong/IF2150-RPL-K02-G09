package sehati.business.entity;

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

    public boolean cocok(String kataKunci) {
        throw new UnsupportedOperationException("TODO");
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
