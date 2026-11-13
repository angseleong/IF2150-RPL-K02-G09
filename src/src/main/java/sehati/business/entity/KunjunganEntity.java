package sehati.business.entity;

import java.time.LocalDate;
import java.util.Set;

/**
 * C35 - KunjunganEntity.
 *
 * Merepresentasikan satu kedatangan pasien ke poli beserta nomor antrean dan statusnya.
 */
public class KunjunganEntity {

    public static final String STATUS_SIAP_DIPERIKSA = "siap diperiksa dokter";
    public static final String STATUS_SELESAI_DIPERIKSA = "selesai diperiksa";
    public static final String STATUS_SELESAI = "selesai";
    private static final Set<String> STATUS_SAH = Set.of("menunggu skrining", "sedang diskrining", "tidak hadir",
            STATUS_SIAP_DIPERIKSA, STATUS_SELESAI_DIPERIKSA, STATUS_SELESAI);

    private int idKunjungan;
    private int idPasien;
    private int idPoli;
    private LocalDate tanggal;
    private int nomorAntrean;
    private String status;

    public KunjunganEntity() {
    }

    public KunjunganEntity(
            int idKunjungan, int idPasien, int idPoli, LocalDate tanggal, int nomorAntrean, String status) {
        this.idKunjungan = idKunjungan;
        this.idPasien = idPasien;
        this.idPoli = idPoli;
        this.tanggal = tanggal;
        this.nomorAntrean = nomorAntrean;
        this.status = status;
    }

    /**
     * Mengubah status menurut alur pelayanan. Kunjungan "selesai" dikunci agar tidak menghasilkan bundel FHIR
     * kedua (Algo-C35-01).
     */
    public void perbaruiStatus(String status) {
        if (!STATUS_SAH.contains(status)) {
            throw new IllegalArgumentException("Status kunjungan tidak dikenal: " + status);
        }
        if (STATUS_SELESAI.equals(this.status)) {
            throw new IllegalStateException("Kunjungan sudah selesai dan tidak dapat diubah");
        }
        this.status = status;
    }

    public boolean aktifHariIni() {
        return LocalDate.now().equals(tanggal) && !STATUS_SELESAI.equals(status);
    }

    public int getIdKunjungan() {
        return idKunjungan;
    }

    public void setIdKunjungan(int idKunjungan) {
        this.idKunjungan = idKunjungan;
    }

    public int getIdPasien() {
        return idPasien;
    }

    public void setIdPasien(int idPasien) {
        this.idPasien = idPasien;
    }

    public int getIdPoli() {
        return idPoli;
    }

    public void setIdPoli(int idPoli) {
        this.idPoli = idPoli;
    }

    public LocalDate getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDate tanggal) {
        this.tanggal = tanggal;
    }

    public int getNomorAntrean() {
        return nomorAntrean;
    }

    public void setNomorAntrean(int nomorAntrean) {
        this.nomorAntrean = nomorAntrean;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
