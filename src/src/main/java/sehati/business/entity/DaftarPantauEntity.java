package sehati.business.entity;

/**
 * C51 - DaftarPantauEntity.
 *
 * Merepresentasikan entri pasien berisiko atau terjadwal kontrol beserta status tindak lanjutnya.
 */
public class DaftarPantauEntity {

    private int idEntri;
    private int idPasien;
    private String jenisRisiko;
    private String statusTindakLanjut;

    public DaftarPantauEntity() {
    }

    public DaftarPantauEntity(int idEntri, int idPasien, String jenisRisiko, String statusTindakLanjut) {
        this.idEntri = idEntri;
        this.idPasien = idPasien;
        this.jenisRisiko = jenisRisiko;
        this.statusTindakLanjut = statusTindakLanjut;
    }

    public void perbaruiStatus(String status) {
        throw new UnsupportedOperationException("TODO");
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
