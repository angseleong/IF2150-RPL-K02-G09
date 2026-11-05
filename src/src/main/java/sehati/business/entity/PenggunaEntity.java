package sehati.business.entity;

/**
 * C30 - PenggunaEntity.
 *
 * Merepresentasikan akun staf beserta hash kata sandi, peran, dan status aktifnya.
 */
public class PenggunaEntity {

    private int idPengguna;
    private String namaPengguna;
    private String kataSandiHash;
    private String peran;
    private String status;

    public PenggunaEntity() {
    }

    public PenggunaEntity(int idPengguna, String namaPengguna, String kataSandiHash, String peran, String status) {
        this.idPengguna = idPengguna;
        this.namaPengguna = namaPengguna;
        this.kataSandiHash = kataSandiHash;
        this.peran = peran;
        this.status = status;
    }

    public boolean cocokkanSandi(String sandi) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean aktif() {
        throw new UnsupportedOperationException("TODO");
    }

    public void nonaktifkan() {
        throw new UnsupportedOperationException("TODO");
    }

    public int getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(int idPengguna) {
        this.idPengguna = idPengguna;
    }

    public String getNamaPengguna() {
        return namaPengguna;
    }

    public void setNamaPengguna(String namaPengguna) {
        this.namaPengguna = namaPengguna;
    }

    public String getKataSandiHash() {
        return kataSandiHash;
    }

    public void setKataSandiHash(String kataSandiHash) {
        this.kataSandiHash = kataSandiHash;
    }

    public String getPeran() {
        return peran;
    }

    public void setPeran(String peran) {
        this.peran = peran;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
