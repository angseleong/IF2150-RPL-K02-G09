package sehati.business.entity;

import org.mindrot.jbcrypt.BCrypt;

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

    /**
     * Akun baru berstatus "aktif" dengan idPengguna 0 sampai disimpan (Subbab 5.30).
     */
    public PenggunaEntity(String namaPengguna, String kataSandiHash, String peran) {
        this(0, namaPengguna, kataSandiHash, peran, "aktif");
    }

    public PenggunaEntity(int idPengguna, String namaPengguna, String kataSandiHash, String peran, String status) {
        this.idPengguna = idPengguna;
        this.namaPengguna = namaPengguna;
        this.kataSandiHash = kataSandiHash;
        this.peran = peran;
        this.status = status;
    }

    /**
     * Mencocokkan sandi dengan kataSandiHash lewat bcrypt (Algo-C30-01, KNF04).
     */
    public boolean cocokkanSandi(String sandi) {
        if (sandi == null || sandi.isEmpty() || kataSandiHash == null || kataSandiHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(sandi, kataSandiHash);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            // Hash rusak diperlakukan sama dengan sandi salah; jbcrypt melempar IndexOutOfBounds untuk hash pendek.
            return false;
        }
    }

    public boolean aktif() {
        return "aktif".equals(status);
    }

    /**
     * Menonaktifkan akun (Algo-C30-01, UC12).
     */
    public void nonaktifkan() {
        if ("nonaktif".equals(status)) {
            throw new IllegalStateException("Akun sudah nonaktif");
        }
        status = "nonaktif";
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
