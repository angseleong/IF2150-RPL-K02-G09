package sehati.business.entity;

import java.time.LocalDate;

/**
 * C45 - JadwalKontrolEntity.
 *
 * Merepresentasikan rencana kunjungan ulang pasien.
 */
public class JadwalKontrolEntity {

    public static final String STATUS_TERJADWAL = "terjadwal";
    public static final String STATUS_SELESAI = "selesai";

    private int idJadwal;
    private int idPasien;
    private LocalDate tanggalRencana;
    private String status;

    public JadwalKontrolEntity() {
    }

    public JadwalKontrolEntity(int idJadwal, int idPasien, LocalDate tanggalRencana, String status) {
        this.idJadwal = idJadwal;
        this.idPasien = idPasien;
        this.tanggalRencana = tanggalRencana;
        this.status = status;
    }

    /**
     * Menandai jadwal selesai saat pasien datang kontrol (Subbab 5.45, KF12).
     */
    public void tandaiSelesai() {
        if (!STATUS_TERJADWAL.equals(status)) {
            throw new IllegalStateException("Jadwal kontrol tidak berstatus terjadwal");
        }
        status = STATUS_SELESAI;
    }

    public int getIdJadwal() {
        return idJadwal;
    }

    public void setIdJadwal(int idJadwal) {
        this.idJadwal = idJadwal;
    }

    public int getIdPasien() {
        return idPasien;
    }

    public void setIdPasien(int idPasien) {
        this.idPasien = idPasien;
    }

    public LocalDate getTanggalRencana() {
        return tanggalRencana;
    }

    public void setTanggalRencana(LocalDate tanggalRencana) {
        this.tanggalRencana = tanggalRencana;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
