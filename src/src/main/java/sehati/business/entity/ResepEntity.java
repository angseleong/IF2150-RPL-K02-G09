package sehati.business.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * C46 - ResepEntity.
 *
 * Merepresentasikan resep elektronik satu kunjungan beserta status pelayanannya.
 */
public class ResepEntity {

    private int idResep;
    private int idKunjungan;
    private LocalDateTime waktuMasuk;
    private String status;
    private String catatan;
    // Komposisi ResepEntity-ItemResepEntity, bukan kolom. Disimpan DataFarmasiBoundary ke item_resep (Q-C61-10).
    private List<ItemResepEntity> daftarItem = new ArrayList<>();

    public ResepEntity() {
    }

    public ResepEntity(int idResep, int idKunjungan, LocalDateTime waktuMasuk, String status, String catatan) {
        this.idResep = idResep;
        this.idKunjungan = idKunjungan;
        this.waktuMasuk = waktuMasuk;
        this.status = status;
        this.catatan = catatan;
    }

    public void perbaruiStatus(String status) {
        throw new UnsupportedOperationException("TODO");
    }

    public int getIdResep() {
        return idResep;
    }

    public void setIdResep(int idResep) {
        this.idResep = idResep;
    }

    public int getIdKunjungan() {
        return idKunjungan;
    }

    public void setIdKunjungan(int idKunjungan) {
        this.idKunjungan = idKunjungan;
    }

    public LocalDateTime getWaktuMasuk() {
        return waktuMasuk;
    }

    public void setWaktuMasuk(LocalDateTime waktuMasuk) {
        this.waktuMasuk = waktuMasuk;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCatatan() {
        return catatan;
    }

    public void setCatatan(String catatan) {
        this.catatan = catatan;
    }

    public List<ItemResepEntity> getDaftarItem() {
        return daftarItem;
    }

    public void setDaftarItem(List<ItemResepEntity> daftarItem) {
        this.daftarItem = new ArrayList<>(daftarItem);
    }
}
