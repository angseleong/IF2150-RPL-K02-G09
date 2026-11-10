package sehati.business.entity;

import java.time.LocalDate;

/**
 * C35 - KunjunganEntity.
 *
 * Merepresentasikan satu kedatangan pasien ke poli beserta nomor antrean dan statusnya.
 */
public class KunjunganEntity {

    private int idKunjungan;
    private int idPasien;
    private int idPoli;
    private LocalDate tanggal;
    private int nomorAntrean;
    private String status;

    public KunjunganEntity() {
    }

    public KunjunganEntity(
            int idKunjungan, int idPasien, int idPoli, LocalDate tanggal, int nomorAntrean, String status) {
        this.idKunjungan = idKunjungan;
        this.idPasien = idPasien;
        this.idPoli = idPoli;
        this.tanggal = tanggal;
        this.nomorAntrean = nomorAntrean;
        this.status = status;
    }

    public void perbaruiStatus(String status) {
        this.status = status;
    }

    public boolean aktifHariIni() {
        return "AKTIF".equals(status) && tanggal != null && tanggal.equals(LocalDate.now());
    }

    public int getIdKunjungan() {
        return idKunjungan;
    }

    public void setIdKunjungan(int idKunjungan) {
        this.idKunjungan = idKunjungan;
    }

    public int getIdPasien() {
        return idPasien;
    }

    public void setIdPasien(int idPasien) {
        this.idPasien = idPasien;
    }

    public int getIdPoli() {
        return idPoli;
    }

    public void setIdPoli(int idPoli) {
        this.idPoli = idPoli;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public int getNomorAntrean() {
        return nomorAntrean;
    }

    public void setNomorAntrean(int nomorAntrean) {
        this.nomorAntrean = nomorAntrean;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
