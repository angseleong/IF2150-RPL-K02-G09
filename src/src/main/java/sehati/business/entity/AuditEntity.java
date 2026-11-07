package sehati.business.entity;

import java.time.LocalDateTime;

/**
 * C32 - AuditEntity.
 *
 * Merepresentasikan catatan jejak audit berisi pelaku, waktu, dan deskripsi perubahan. Tidak memiliki setter agar
 * isinya tidak dapat diubah setelah dibuat (KNF05).
 */
public class AuditEntity {

    private final long idAudit;
    private final int idPengguna;
    private final LocalDateTime waktu;
    private final String deskripsi;

    public AuditEntity(long idAudit, int idPengguna, LocalDateTime waktu, String deskripsi) {
        this.idAudit = idAudit;
        this.idPengguna = idPengguna;
        this.waktu = waktu;
        this.deskripsi = deskripsi;
    }

    /**
     * Membuat catatan audit berwaktu saat ini tanpa menyimpannya (Algo-C32-01, KF16).
     */
    public static AuditEntity buat(int idPengguna, String deskripsi) {
        if (deskripsi == null || deskripsi.isBlank()) {
            throw new IllegalArgumentException("Deskripsi audit wajib diisi");
        }
        return new AuditEntity(0, idPengguna, LocalDateTime.now(), deskripsi);
    }

    public long getIdAudit() {
        return idAudit;
    }

    public int getIdPengguna() {
        return idPengguna;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public String getDeskripsi() {
        return deskripsi;
    }
}
