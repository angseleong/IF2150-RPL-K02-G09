package sehati.business.entity;

import java.time.Duration;
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

    /**
     * Mengembalikan true bila sejak waktuMulai sudah lewat paling sedikit {@code menit} menit (Algo-C31-01, KNF04).
     */
    public boolean habisWaktu(int menit) {
        if (menit <= 0) {
            throw new IllegalArgumentException("Batas waktu harus lebih dari nol");
        }
        return Duration.between(waktuMulai, LocalDateTime.now()).toMinutes() >= menit;
    }

    public void tutup() {
        statusAktif = false;
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
