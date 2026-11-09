package sehati.business.entity;

/**
 * C47 - ItemResepEntity.
 *
 * Merepresentasikan satu obat pada resep beserta dosis, jumlah, dan aturan pakai.
 */
public class ItemResepEntity {

    private int idItem;
    private int idObat;
    private String dosis;
    private int jumlah;
    private String aturanPakai;

    public ItemResepEntity() {
    }

    public ItemResepEntity(int idItem, int idObat, String dosis, int jumlah, String aturanPakai) {
        this.idItem = idItem;
        this.idObat = idObat;
        this.dosis = dosis;
        this.jumlah = jumlah;
        this.aturanPakai = aturanPakai;
    }

    /**
     * Stok cukup bila sedikitnya sebanyak jumlah yang diresepkan (KF11, KF14).
     */
    public boolean cukupStok(int stok) {
        return stok >= jumlah;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public int getIdObat() {
        return idObat;
    }

    public void setIdObat(int idObat) {
        this.idObat = idObat;
    }

    public String getDosis() {
        return dosis;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public String getAturanPakai() {
        return aturanPakai;
    }

    public void setAturanPakai(String aturanPakai) {
        this.aturanPakai = aturanPakai;
    }
}
