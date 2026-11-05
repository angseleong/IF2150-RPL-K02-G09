package sehati.business.entity;

import java.time.LocalDateTime;

/**
 * C55 - PencadanganEntity.
 *
 * Merepresentasikan catatan satu pencadangan beserta waktu, status, dan lokasi medianya.
 */
public class PencadanganEntity {

    private int idPencadangan;
    private LocalDateTime waktu;
    private String status;
    private String lokasiMedia;
    private long ukuranBerkas;

    public PencadanganEntity() {
    }

    public PencadanganEntity(
            int idPencadangan, LocalDateTime waktu, String status, String lokasiMedia, long ukuranBerkas) {
        this.idPencadangan = idPencadangan;
        this.waktu = waktu;
        this.status = status;
        this.lokasiMedia = lokasiMedia;
        this.ukuranBerkas = ukuranBerkas;
    }

    public boolean berhasil() {
        throw new UnsupportedOperationException("TODO");
    }

    public int getIdPencadangan() {
        return idPencadangan;
    }

    public void setIdPencadangan(int idPencadangan) {
        this.idPencadangan = idPencadangan;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public void setWaktu(LocalDateTime waktu) {
        this.waktu = waktu;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLokasiMedia() {
        return lokasiMedia;
    }

    public void setLokasiMedia(String lokasiMedia) {
        this.lokasiMedia = lokasiMedia;
    }

    public long getUkuranBerkas() {
        return ukuranBerkas;
    }

    public void setUkuranBerkas(long ukuranBerkas) {
        this.ukuranBerkas = ukuranBerkas;
    }
}
