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
    // Penampung entri dari tambahEntri() sampai DataPasienBoundary.simpanRekamMedis() menyimpannya (Q-C57-11).
    private final List<String> entriBaru = new ArrayList<>();
    // Tiga isian berikut hanya untuk tampilan I-06, diisi PemeriksaanManager.susunRingkasan() (KF09).
    private PasienEntity pasien;
    private SkriningEntity skriningHariIni;
    private List<PenandaRisikoEntity> penandaAktif = new ArrayList<>();

    public RekamMedisEntity() {
    }

    public RekamMedisEntity(int idRekamMedis, List<DiagnosisEntity> daftarDiagnosis, List<String> riwayatObat) {
        this.idRekamMedis = idRekamMedis;
        this.daftarDiagnosis = daftarDiagnosis;
        this.riwayatObat = riwayatObat;
    }

    /**
     * Algo-C34-01: menambah satu catatan ke akhir daftar entri baru. Catatan yang sudah tersimpan tidak diubah.
     */
    public void tambahEntri(String catatan) {
        if (catatan == null || catatan.isBlank()) {
            throw new IllegalArgumentException("Catatan rekam medis wajib diisi");
        }
        entriBaru.add(catatan);
    }

    /** Entri yang ditambahkan sejak rekam medis dimuat dan belum disimpan. */
    public List<String> getEntriBaru() {
        return List.copyOf(entriBaru);
    }

    /** Dipanggil DataPasienBoundary setelah entri baru tersimpan. */
    public void kosongkanEntriBaru() {
        entriBaru.clear();
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

    public PasienEntity getPasien() {
        return pasien;
    }

    public void setPasien(PasienEntity pasien) {
        this.pasien = pasien;
    }

    public SkriningEntity getSkriningHariIni() {
        return skriningHariIni;
    }

    public void setSkriningHariIni(SkriningEntity skriningHariIni) {
        this.skriningHariIni = skriningHariIni;
    }

    public List<PenandaRisikoEntity> getPenandaAktif() {
        return penandaAktif;
    }

    public void setPenandaAktif(List<PenandaRisikoEntity> penandaAktif) {
        this.penandaAktif = penandaAktif;
    }
}
