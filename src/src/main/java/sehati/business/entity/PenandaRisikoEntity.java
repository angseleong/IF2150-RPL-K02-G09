package sehati.business.entity;

import java.time.LocalDate;

/**
 * C41 - PenandaRisikoEntity.
 *
 * Merepresentasikan penanda risiko yang melekat pada kunjungan beserta jenisnya.
 */
public class PenandaRisikoEntity {

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

    public void cabut() {
        throw new UnsupportedOperationException("TODO");
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
