package sehati.business.entity;

import java.time.LocalDate;

/**
 * C41 - PenandaRisikoEntity.
 *
 * Merepresentasikan penanda risiko yang melekat pada kunjungan beserta jenisnya.
 */
public class PenandaRisikoEntity {

    public static final String STATUS_AKTIF = "aktif";
    public static final String STATUS_DICABUT = "dicabut";

    private int idPenanda;
    private int idKunjungan;
    private String jenisRisiko;
    private LocalDate tanggal;
    private String status;

    public PenandaRisikoEntity() {
    }

    public PenandaRisikoEntity(int idPenanda, int idKunjungan, String jenisRisiko, LocalDate tanggal, String status) {
        this.idPenanda = idPenanda;
        this.idKunjungan = idKunjungan;
        this.jenisRisiko = jenisRisiko;
        this.tanggal = tanggal;
        this.status = status;
    }

    /**
     * Mencabut penanda aktif. Penanda tetap tersimpan sebagai riwayat (Subbab 5.41).
     */
    public void cabut() {
        if (STATUS_DICABUT.equals(status)) {
            throw new IllegalStateException("Penanda risiko sudah dicabut");
        }
        this.status = STATUS_DICABUT;
    }

    public int getIdPenanda() {
        return idPenanda;
    }

    public void setIdPenanda(int idPenanda) {
        this.idPenanda = idPenanda;
    }

    public int getIdKunjungan() {
        return idKunjungan;
    }

    public void setIdKunjungan(int idKunjungan) {
        this.idKunjungan = idKunjungan;
    }

    public String getJenisRisiko() {
        return jenisRisiko;
    }

    public void setJenisRisiko(String jenisRisiko) {
        this.jenisRisiko = jenisRisiko;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
