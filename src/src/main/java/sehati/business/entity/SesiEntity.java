package sehati.business.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * C31 - SesiEntity.
 *
 * Merepresentasikan sesi kerja pengguna sejak masuk sampai keluar atau habis waktu.
 */
public class SesiEntity {

    private UUID idSesi;
    private int idPengguna;
    private LocalDateTime waktuMulai;
    private boolean statusAktif;

    public SesiEntity() {
    }

    public SesiEntity(UUID idSesi, int idPengguna, LocalDateTime waktuMulai, boolean statusAktif) {
        this.idSesi = idSesi;
        this.idPengguna = idPengguna;
        this.waktuMulai = waktuMulai;
        this.statusAktif = statusAktif;
    }

    public boolean habisWaktu(int menit) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tutup() {
        throw new UnsupportedOperationException("TODO");
    }

    public UUID getIdSesi() {
        return idSesi;
    }

    public void setIdSesi(UUID idSesi) {
        this.idSesi = idSesi;
    }

    public int getIdPengguna() {
        return idPengguna;
    }

    public void setIdPengguna(int idPengguna) {
        this.idPengguna = idPengguna;
    }

    public LocalDateTime getWaktuMulai() {
        return waktuMulai;
    }

    public void setWaktuMulai(LocalDateTime waktuMulai) {
        this.waktuMulai = waktuMulai;
    }

    public boolean isStatusAktif() {
        return statusAktif;
    }

    public void setStatusAktif(boolean statusAktif) {
        this.statusAktif = statusAktif;
    }
}
