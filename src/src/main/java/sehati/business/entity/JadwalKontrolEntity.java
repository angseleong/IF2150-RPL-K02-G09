package sehati.business.entity;

import java.time.LocalDate;

/**
 * C45 - JadwalKontrolEntity.
 *
 * Merepresentasikan rencana kunjungan ulang pasien.
 */
public class JadwalKontrolEntity {

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

    public void tandaiSelesai() {
        throw new UnsupportedOperationException("TODO");
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
