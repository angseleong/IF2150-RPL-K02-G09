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
    // Tambahan implementasi (B.1): nama poli yang disaring, null berarti semua poli (UC10 langkah 1).
    private String namaPoli;

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
        if (ringkasan == null || ringkasan.isEmpty()) {
            return true;
        }
        for (String baris : ringkasan.split("\\R")) {
            if (!baris.startsWith("total;")) {
                continue;
            }
            String[] kolom = baris.split(";", -1);
            if (kolom.length < 3) {
                return true;
            }
            try {
                return Long.parseLong(kolom[2].trim()) == 0L;
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return true;
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

    public String getNamaPoli() {
        return namaPoli;
    }

    public void setNamaPoli(String namaPoli) {
        this.namaPoli = namaPoli;
    }
}
