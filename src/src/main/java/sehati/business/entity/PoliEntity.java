package sehati.business.entity;

import java.time.LocalDate;

/**
 * C36 - PoliEntity.
 *
 * Merepresentasikan data master poli beserta urutan antrean terakhir hari itu.
 */
public class PoliEntity {

    private int idPoli;
    private String namaPoli;
    private int urutanTerakhir;
    private LocalDate tanggalUrutan;

    public PoliEntity() {
    }

    public PoliEntity(int idPoli, String namaPoli, int urutanTerakhir, LocalDate tanggalUrutan) {
        this.idPoli = idPoli;
        this.namaPoli = namaPoli;
        this.urutanTerakhir = urutanTerakhir;
        this.tanggalUrutan = tanggalUrutan;
    }

    public int ambilNomorBerikutnya(LocalDate hariIni) {
        if (hariIni == null) {
            throw new IllegalArgumentException("Tanggal hari ini tidak boleh kosong");
        }
        if (!hariIni.equals(tanggalUrutan)) {
            tanggalUrutan = hariIni;
            urutanTerakhir = 0;
        }
        return ++urutanTerakhir;
    }

    public int getIdPoli() {
        return idPoli;
    }

    public void setIdPoli(int idPoli) {
        this.idPoli = idPoli;
    }

    public String getNamaPoli() {
        return namaPoli;
    }

    public void setNamaPoli(String namaPoli) {
        this.namaPoli = namaPoli;
    }

    public int getUrutanTerakhir() {
        return urutanTerakhir;
    }

    public void setUrutanTerakhir(int urutanTerakhir) {
        this.urutanTerakhir = urutanTerakhir;
    }

    public LocalDate getTanggalUrutan() {
        return tanggalUrutan;
    }

    public void setTanggalUrutan(LocalDate tanggalUrutan) {
        this.tanggalUrutan = tanggalUrutan;
    }
}
