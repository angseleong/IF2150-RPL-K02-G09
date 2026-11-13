package sehati.business.entity;

/**
 * C43 - DiagnosisEntity.
 *
 * Merepresentasikan diagnosis kunjungan dalam kode ICD-10.
 */
public class DiagnosisEntity {

    private int idDiagnosis;
    private String kodeICD10;
    private String deskripsi;

    public DiagnosisEntity() {
    }

    public DiagnosisEntity(int idDiagnosis, String kodeICD10, String deskripsi) {
        this.idDiagnosis = idDiagnosis;
        this.kodeICD10 = kodeICD10;
        this.deskripsi = deskripsi;
    }

    /**
     * Satu baris "kode - deskripsi" untuk riwayat dan ringkasan pemeriksaan (KF09, KF10).
     */
    public String ringkas() {
        return kodeICD10 + " - " + deskripsi;
    }

    public int getIdDiagnosis() {
        return idDiagnosis;
    }

    public void setIdDiagnosis(int idDiagnosis) {
        this.idDiagnosis = idDiagnosis;
    }

    public String getKodeICD10() {
        return kodeICD10;
    }

    public void setKodeICD10(String kodeICD10) {
        this.kodeICD10 = kodeICD10;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
}
