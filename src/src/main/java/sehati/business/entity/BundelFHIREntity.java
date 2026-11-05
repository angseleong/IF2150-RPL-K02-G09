package sehati.business.entity;

/**
 * C54 - BundelFHIREntity.
 *
 * Merepresentasikan bundel HL7 FHIR R4 satu kunjungan beserta status pengirimannya.
 */
public class BundelFHIREntity {

    private int idBundel;
    private int idKunjungan;
    private String isiBundel;
    private String status;
    private String alasanGagal;

    public BundelFHIREntity() {
    }

    public BundelFHIREntity(int idBundel, int idKunjungan, String isiBundel, String status, String alasanGagal) {
        this.idBundel = idBundel;
        this.idKunjungan = idKunjungan;
        this.isiBundel = isiBundel;
        this.status = status;
        this.alasanGagal = alasanGagal;
    }

    public void tandaiTerkirim(String kode) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tandaiGagal(String alasan) {
        throw new UnsupportedOperationException("TODO");
    }

    public int getIdBundel() {
        return idBundel;
    }

    public void setIdBundel(int idBundel) {
        this.idBundel = idBundel;
    }

    public int getIdKunjungan() {
        return idKunjungan;
    }

    public void setIdKunjungan(int idKunjungan) {
        this.idKunjungan = idKunjungan;
    }

    public String getIsiBundel() {
        return isiBundel;
    }

    public void setIsiBundel(String isiBundel) {
        this.isiBundel = isiBundel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlasanGagal() {
        return alasanGagal;
    }

    public void setAlasanGagal(String alasanGagal) {
        this.alasanGagal = alasanGagal;
    }
}
