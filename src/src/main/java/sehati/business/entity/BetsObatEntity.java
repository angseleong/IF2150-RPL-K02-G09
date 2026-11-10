package sehati.business.entity;

import java.time.LocalDate;
import java.util.List;

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

    /**
     * Mengurangi sisa bets sebanyak yang diambil saat penyerahan obat (Algo-C49-01, KF14).
     */
    public void kurangi(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah pengambilan harus lebih dari nol");
        }
        if (jumlah > this.jumlah) {
            throw new IllegalStateException("Sisa bets " + nomorBets + " hanya " + this.jumlah);
        }
        this.jumlah -= jumlah;
    }

    /**
     * Bets yang sudah lewat tanggalnya ikut terhitung, sama dengan perbandingan tanggal pada Q-C61-08
     * (Algo-C49-01, KF17).
     */
    public boolean mendekatiKedaluwarsa(int hari) {
        return !tanggalKedaluwarsa.isAfter(LocalDate.now().plusDays(hari));
    }

    /**
     * Bets yang tanggal kedaluwarsanya sudah lewat tidak boleh diserahkan ke pasien. Bets yang kedaluwarsa hari ini
     * masih boleh dipakai.
     */
    public boolean sudahKedaluwarsa() {
        return tanggalKedaluwarsa != null && tanggalKedaluwarsa.isBefore(LocalDate.now());
    }

    /**
     * Total sisa bets yang belum kedaluwarsa, yaitu stok yang benar-benar boleh diserahkan (FEFO, KF14).
     */
    public static int jumlahLayak(List<BetsObatEntity> daftarBets) {
        int total = 0;
        for (BetsObatEntity bets : daftarBets) {
            if (!bets.sudahKedaluwarsa()) {
                total += bets.getJumlah();
            }
        }
        return total;
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
