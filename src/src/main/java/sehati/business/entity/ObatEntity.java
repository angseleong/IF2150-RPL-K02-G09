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

    public void kurangiStok(int jumlah) {
        throw new UnsupportedOperationException("TODO");
    }

    public void tambahStok(int jumlah) {
        throw new UnsupportedOperationException("TODO");
    }

    public boolean diBawahAmbang() {
        throw new UnsupportedOperationException("TODO");
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
