package sehati.business.entity;

import java.time.LocalDate;

/**
 * C33 - PasienEntity.
 *
 * Merepresentasikan data diri pasien beserta NIK dan nomor rekam medisnya.
 */
public class PasienEntity {

    private int idPasien;
    private String nik;
    private String nomorRekamMedis;
    private String nama;
    private LocalDate tanggalLahir;
    private String jenisKelamin;
    private String alamat;
    private String nomorTelepon;

    public PasienEntity() {
    }

    public PasienEntity(
            int idPasien, String nik, String nomorRekamMedis, String nama, LocalDate tanggalLahir,
            String jenisKelamin, String alamat, String nomorTelepon) {
        this.idPasien = idPasien;
        this.nik = nik;
        this.nomorRekamMedis = nomorRekamMedis;
        this.nama = nama;
        this.tanggalLahir = tanggalLahir;
        this.jenisKelamin = jenisKelamin;
        this.alamat = alamat;
        this.nomorTelepon = nomorTelepon;
    }

    public void perbarui(PasienEntity data) {
        throw new UnsupportedOperationException("TODO");
    }

    public int getIdPasien() {
        return idPasien;
    }

    public void setIdPasien(int idPasien) {
        this.idPasien = idPasien;
    }

    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getNomorRekamMedis() {
        return nomorRekamMedis;
    }

    public void setNomorRekamMedis(String nomorRekamMedis) {
        this.nomorRekamMedis = nomorRekamMedis;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public LocalDate getTanggalLahir() {
        return tanggalLahir;
    }

    public void setTanggalLahir(LocalDate tanggalLahir) {
        this.tanggalLahir = tanggalLahir;
    }

    public String getJenisKelamin() {
        return jenisKelamin;
    }

    public void setJenisKelamin(String jenisKelamin) {
        this.jenisKelamin = jenisKelamin;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        this.nomorTelepon = nomorTelepon;
    }
}
