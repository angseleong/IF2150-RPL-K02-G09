package sehati.business.entity;

import java.time.LocalDateTime;

/**
 * C32 - AuditEntity.
 *
 * Merepresentasikan catatan jejak audit berisi pelaku, waktu, dan deskripsi perubahan.
 */
public class AuditEntity {

    private long idAudit;
    private int idPengguna;
    private LocalDateTime waktu;
    private String deskripsi;

    public AuditEntity() {
    }

    public AuditEntity(long idAudit, int idPengguna, LocalDateTime waktu, String deskripsi) {
        this.idAudit = idAudit;
        this.idPengguna = idPengguna;
        this.waktu = waktu;
        this.deskripsi = deskripsi;
    }

    public static AuditEntity buat(int idPengguna, String deskripsi) {
        throw new UnsupportedOperationException("TODO");
    }

    public long getIdAudit() {
        return idAudit;
    }

    public void setIdAudit(long idAudit) {
        this.idAudit = idAudit;
    }

    public int getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(int idPengguna) {
        this.idPengguna = idPengguna;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public void setWaktu(LocalDateTime waktu) {
        this.waktu = waktu;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
}
