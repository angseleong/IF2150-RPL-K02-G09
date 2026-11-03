package sehati.business.entity;

import java.time.LocalDateTime;

/**
 * C38 - SkriningEntity.
 *
 * Merepresentasikan hasil skrining satu kunjungan beserta keluhan awalnya.
 */
public class SkriningEntity {

    private int idSkrining;
    private int idKunjungan;
    private String keluhanAwal;
    private LocalDateTime waktu;

    public SkriningEntity() {
    }

    public SkriningEntity(int idSkrining, int idKunjungan, String keluhanAwal, LocalDateTime waktu) {
        this.idSkrining = idSkrining;
        this.idKunjungan = idKunjungan;
        this.keluhanAwal = keluhanAwal;
        this.waktu = waktu;
    }

    public void lampirkanTandaVital(TandaVitalEntity tv) {
        throw new UnsupportedOperationException("TODO");
    }

    public int getIdSkrining() {
        return idSkrining;
    }

    public void setIdSkrining(int idSkrining) {
        this.idSkrining = idSkrining;
    }

    public int getIdKunjungan() {
        return idKunjungan;
    }

    public void setIdKunjungan(int idKunjungan) {
        this.idKunjungan = idKunjungan;
    }

    public String getKeluhanAwal() {
        return keluhanAwal;
    }

    public void setKeluhanAwal(String keluhanAwal) {
        this.keluhanAwal = keluhanAwal;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public void setWaktu(LocalDateTime waktu) {
        this.waktu = waktu;
    }
}
