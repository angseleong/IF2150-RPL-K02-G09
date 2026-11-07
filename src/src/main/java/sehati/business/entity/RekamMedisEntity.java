package sehati.business.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * C34 - RekamMedisEntity.
 *
 * Merepresentasikan riwayat klinis pasien: diagnosis lampau, riwayat obat, dan penanda risiko aktif.
 */
public class RekamMedisEntity {

    private int idRekamMedis;
    private List<DiagnosisEntity> daftarDiagnosis = new ArrayList<>();
    private List<String> riwayatObat = new ArrayList<>();

    public RekamMedisEntity() {
    }

    public RekamMedisEntity(int idRekamMedis, List<DiagnosisEntity> daftarDiagnosis, List<String> riwayatObat) {
        this.idRekamMedis = idRekamMedis;
        this.daftarDiagnosis = daftarDiagnosis;
        this.riwayatObat = riwayatObat;
    }

    public void tambahEntri(String catatan) {
        throw new UnsupportedOperationException("TODO");
    }

    public int getIdRekamMedis() {
        return idRekamMedis;
    }

    public void setIdRekamMedis(int idRekamMedis) {
        this.idRekamMedis = idRekamMedis;
    }

    public List<DiagnosisEntity> getDaftarDiagnosis() {
        return daftarDiagnosis;
    }

    public void setDaftarDiagnosis(List<DiagnosisEntity> daftarDiagnosis) {
        this.daftarDiagnosis = daftarDiagnosis;
    }

    public List<String> getRiwayatObat() {
        return riwayatObat;
    }

    public void setRiwayatObat(List<String> riwayatObat) {
        this.riwayatObat = riwayatObat;
    }
}
