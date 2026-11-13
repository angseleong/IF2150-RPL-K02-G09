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
    private TandaVitalEntity tandaVital;

    public SkriningEntity() {
    }

    public SkriningEntity(int idSkrining, int idKunjungan, String keluhanAwal, LocalDateTime waktu) {
        this.idSkrining = idSkrining;
        this.idKunjungan = idKunjungan;
        this.keluhanAwal = keluhanAwal;
        this.waktu = waktu;
    }

    /**
     * Menautkan hasil pengukuran sebelum skrining disimpan (Subbab 5.38, KF05).
     */
    public void lampirkanTandaVital(TandaVitalEntity tv) {
        if (tv == null) {
            throw new IllegalArgumentException("Tanda vital tidak boleh kosong");
        }
        this.tandaVital = tv;
    }

    public TandaVitalEntity getTandaVital() {
        return tandaVital;
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
