package sehati.dataaccess;

import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KodeICD10Entity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.PenandaRisikoEntity;
import sehati.business.entity.SkriningEntity;
import sehati.business.entity.TandaVitalEntity;

import java.sql.Connection;
import java.util.List;

/**
 * C59 - DataKlinisBoundary.
 *
 * Membaca dan menyimpan SkriningEntity, TandaVitalEntity, AmbangRisikoEntity, PenandaRisikoEntity,
 * PemeriksaanEntity, DiagnosisEntity, KodeICD10Entity, dan JadwalKontrolEntity.
 */
public class DataKlinisBoundary {

    private Connection koneksi;

    public DataKlinisBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    public void simpanSkrining(SkriningEntity s) {
        throw new UnsupportedOperationException("TODO");
    }

    public SkriningEntity ambilSkrining(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<TandaVitalEntity> riwayatTandaVital(int idPasien, int batas) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<AmbangRisikoEntity> ambilAmbang() {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanAmbang(AmbangRisikoEntity a) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanPenanda(PenandaRisikoEntity p) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<PenandaRisikoEntity> penandaKunjungan(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<KodeICD10Entity> cariKodeICD10(String kataKunci) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanPemeriksaan(PemeriksaanEntity p, DiagnosisEntity d) {
        throw new UnsupportedOperationException("TODO");
    }

    public PemeriksaanEntity ambilPemeriksaan(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanJadwalKontrol(JadwalKontrolEntity j) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<JadwalKontrolEntity> ambilJadwalKontrol() {
        throw new UnsupportedOperationException("TODO");
    }
}
