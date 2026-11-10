package sehati.business.entity;

import java.time.LocalDate;
import java.util.Set;

/**
 * C51 - DaftarPantauEntity.
 *
 * Merepresentasikan entri pasien berisiko atau terjadwal kontrol beserta status tindak lanjutnya.
 */
public class DaftarPantauEntity {

    public static final String STATUS_BELUM = "belum ditindaklanjuti";
    public static final String STATUS_ULANG = "perlu dihubungi ulang";
    public static final String STATUS_SELESAI = "selesai ditindaklanjuti";
    private static final Set<String> STATUS_SAH = Set.of(STATUS_BELUM, STATUS_ULANG, STATUS_SELESAI);

    private int idEntri;
    private int idPasien;
    private String jenisRisiko;
    private String statusTindakLanjut;
    // Data tampilan yang diisi PantauManager.saringDaftar(); bukan kolom tabel daftar_pantau.
    private LocalDate tanggalPemicu;
    private String namaPasien;
    private String nomorTelepon;

    public DaftarPantauEntity() {
    }

    public DaftarPantauEntity(int idEntri, int idPasien, String jenisRisiko, String statusTindakLanjut) {
        this.idEntri = idEntri;
        this.idPasien = idPasien;
        this.jenisRisiko = jenisRisiko;
        this.statusTindakLanjut = statusTindakLanjut;
    }

    /**
     * Mengubah status ke status sah; entri yang sudah selesai tidak dapat diubah lagi (Algo-C51-01).
     */
    public void perbaruiStatus(String status) {
        if (!STATUS_SAH.contains(status)) {
            throw new IllegalArgumentException("Status tindak lanjut tidak dikenal: " + status);
        }
        if (STATUS_SELESAI.equals(statusTindakLanjut)) {
            throw new IllegalStateException("Entri sudah selesai ditindaklanjuti");
        }
        this.statusTindakLanjut = status;
    }

    public LocalDate getTanggalPemicu() {
        return tanggalPemicu;
    }

    public void setTanggalPemicu(LocalDate tanggalPemicu) {
        this.tanggalPemicu = tanggalPemicu;
    }

    public String getNamaPasien() {
        return namaPasien;
    }

    public void setNamaPasien(String namaPasien) {
        this.namaPasien = namaPasien;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public void setNomorTelepon(String nomorTelepon) {
        this.nomorTelepon = nomorTelepon;
    }

    public int getIdEntri() {
        return idEntri;
    }

    public void setIdEntri(int idEntri) {
        this.idEntri = idEntri;
    }

    public int getIdPasien() {
        return idPasien;
    }

    public void setIdPasien(int idPasien) {
        this.idPasien = idPasien;
    }

    public String getJenisRisiko() {
        return jenisRisiko;
    }

    public void setJenisRisiko(String jenisRisiko) {
        this.jenisRisiko = jenisRisiko;
    }

    public String getStatusTindakLanjut() {
        return statusTindakLanjut;
    }

    public void setStatusTindakLanjut(String statusTindakLanjut) {
        this.statusTindakLanjut = statusTindakLanjut;
    }
}
