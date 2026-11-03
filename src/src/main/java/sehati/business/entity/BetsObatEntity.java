package sehati.business.entity;

import java.time.LocalDate;

/**
 * C49 - BetsObatEntity.
 *
 * Merepresentasikan satu bets obat beserta jumlah dan tanggal kedaluwarsanya.
 */
public class BetsObatEntity {

    private String nomorBets;
    private int idObat;
    private int jumlah;
    private LocalDate tanggalKedaluwarsa;

    public BetsObatEntity() {
    }

    public BetsObatEntity(String nomorBets, int idObat, int jumlah, LocalDate tanggalKedaluwarsa) {
        this.nomorBets = nomorBets;
        this.idObat = idObat;
        this.jumlah = jumlah;
        this.tanggalKedaluwarsa = tanggalKedaluwarsa;
    }

    public void kurangi(int jumlah) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean mendekatiKedaluwarsa(int hari) {
        throw new UnsupportedOperationException("TODO");
    }

    public String getNomorBets() {
        return nomorBets;
    }

    public void setNomorBets(String nomorBets) {
        this.nomorBets = nomorBets;
    }

    public int getIdObat() {
        return idObat;
    }

    public void setIdObat(int idObat) {
        this.idObat = idObat;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public LocalDate getTanggalKedaluwarsa() {
        return tanggalKedaluwarsa;
    }

    public void setTanggalKedaluwarsa(LocalDate tanggalKedaluwarsa) {
        this.tanggalKedaluwarsa = tanggalKedaluwarsa;
    }
}
