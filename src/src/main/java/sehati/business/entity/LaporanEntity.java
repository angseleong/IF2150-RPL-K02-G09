package sehati.business.entity;

import java.time.LocalDate;

/**
 * C53 - LaporanEntity.
 *
 * Merepresentasikan hasil rekapitulasi beserta rentang tanggal dan format berkasnya.
 */
public class LaporanEntity {

    private int idLaporan;
    private LocalDate tanggalMulai;
    private LocalDate tanggalAkhir;
    private String format;
    private String ringkasan;

    public LaporanEntity() {
    }

    public LaporanEntity(
            int idLaporan, LocalDate tanggalMulai, LocalDate tanggalAkhir, String format, String ringkasan) {
        this.idLaporan = idLaporan;
        this.tanggalMulai = tanggalMulai;
        this.tanggalAkhir = tanggalAkhir;
        this.format = format;
        this.ringkasan = ringkasan;
    }

    public boolean kosong() {
        throw new UnsupportedOperationException("TODO");
    }

    public int getIdLaporan() {
        return idLaporan;
    }

    public void setIdLaporan(int idLaporan) {
        this.idLaporan = idLaporan;
    }

    public LocalDate getTanggalMulai() {
        return tanggalMulai;
    }

    public void setTanggalMulai(LocalDate tanggalMulai) {
        this.tanggalMulai = tanggalMulai;
    }

    public LocalDate getTanggalAkhir() {
        return tanggalAkhir;
    }

    public void setTanggalAkhir(LocalDate tanggalAkhir) {
        this.tanggalAkhir = tanggalAkhir;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getRingkasan() {
        return ringkasan;
    }

    public void setRingkasan(String ringkasan) {
        this.ringkasan = ringkasan;
    }
}
