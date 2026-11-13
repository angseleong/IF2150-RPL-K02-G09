package sehati.business.entity;

/**
 * C48 - ObatEntity.
 *
 * Merepresentasikan data master obat beserta saldo stok dan ambang minimumnya.
 */
public class ObatEntity {

    private int idObat;
    private String namaObat;
    private String sediaan;
    private int stok;
    private int ambangMinimum;

    public ObatEntity() {
    }

    public ObatEntity(int idObat, String namaObat, String sediaan, int stok, int ambangMinimum) {
        this.idObat = idObat;
        this.namaObat = namaObat;
        this.sediaan = sediaan;
        this.stok = stok;
        this.ambangMinimum = ambangMinimum;
    }

    /**
     * Mengurangi stok di memori. Penulisan ke basis data dilakukan DataFarmasiBoundary.simpanStok() (Algo-C48-01,
     * KF14).
     */
    public void kurangiStok(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah pengurangan harus lebih dari nol");
        }
        // Lapis kedua setelah ItemResepEntity.cukupStok(); lapis terakhirnya CHECK stok >= 0 pada Q-C61-04.
        if (jumlah > stok) {
            throw new IllegalStateException("Stok " + namaObat + " tidak cukup");
        }
        stok -= jumlah;
    }

    /**
     * Menambah stok di memori sebanyak obat yang diterima (Algo-C48-01, KF17).
     */
    public void tambahStok(int jumlah) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah penambahan harus lebih dari nol");
        }
        stok += jumlah;
    }

    /**
     * Perbandingannya sama dengan klausa pada Q-C61-08, sehingga dasbor dan entity selalu sepakat (KF17).
     */
    public boolean diBawahAmbang() {
        return stok < ambangMinimum;
    }

    public int getIdObat() {
        return idObat;
    }

    public void setIdObat(int idObat) {
        this.idObat = idObat;
    }

    public String getNamaObat() {
        return namaObat;
    }

    public void setNamaObat(String namaObat) {
        this.namaObat = namaObat;
    }

    public String getSediaan() {
        return sediaan;
    }

    public void setSediaan(String sediaan) {
        this.sediaan = sediaan;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }

    public int getAmbangMinimum() {
        return ambangMinimum;
    }

    public void setAmbangMinimum(int ambangMinimum) {
        this.ambangMinimum = ambangMinimum;
    }
}
