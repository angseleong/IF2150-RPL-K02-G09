package sehati.business.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * C50 - PenerimaanObatEntity.
 *
 * Merepresentasikan catatan obat masuk beserta nomor faktur dan tanggal terimanya.
 */
public class PenerimaanObatEntity {

    private int idPenerimaan;
    private String nomorFaktur;
    private LocalDate tanggalTerima;
    private List<BetsObatEntity> daftarBets = new ArrayList<>();

    public PenerimaanObatEntity() {
    }

    public PenerimaanObatEntity(
            int idPenerimaan, String nomorFaktur, LocalDate tanggalTerima, List<BetsObatEntity> daftarBets) {
        this.idPenerimaan = idPenerimaan;
        this.nomorFaktur = nomorFaktur;
        this.tanggalTerima = tanggalTerima;
        this.daftarBets = daftarBets;
    }

    /**
     * Jumlah seluruh bets pada penerimaan, yaitu penambahan stok obat (Algo-C50-01, KF17).
     */
    public int totalJumlah() {
        int total = 0;
        for (BetsObatEntity b : daftarBets) {
            total += b.getJumlah();
        }
        return total;
    }

    public int getIdPenerimaan() {
        return idPenerimaan;
    }

    public void setIdPenerimaan(int idPenerimaan) {
        this.idPenerimaan = idPenerimaan;
    }

    public String getNomorFaktur() {
        return nomorFaktur;
    }

    public void setNomorFaktur(String nomorFaktur) {
        this.nomorFaktur = nomorFaktur;
    }

    public LocalDate getTanggalTerima() {
        return tanggalTerima;
    }

    public void setTanggalTerima(LocalDate tanggalTerima) {
        this.tanggalTerima = tanggalTerima;
    }

    public List<BetsObatEntity> getDaftarBets() {
        return daftarBets;
    }

    public void setDaftarBets(List<BetsObatEntity> daftarBets) {
        this.daftarBets = daftarBets;
    }
}
