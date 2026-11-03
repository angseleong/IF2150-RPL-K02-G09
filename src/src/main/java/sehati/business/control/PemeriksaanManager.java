package sehati.business.control;

import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KodeICD10Entity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.TandaVitalEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * C22 - PemeriksaanManager.
 *
 * Menyusun ringkasan rekam medis dan seri tren, menyimpan pemeriksaan dan diagnosis ICD-10 khusus dokter, serta
 * menjadwalkan kontrol.
 */
public class PemeriksaanManager {

    public RekamMedisEntity susunRingkasan(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<TandaVitalEntity> susunTren(int idPasien) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<KodeICD10Entity> cariKodeICD10(String kataKunci) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanPemeriksaan(int idKunjungan, PemeriksaanEntity p, DiagnosisEntity d) {
        throw new UnsupportedOperationException("TODO");
    }

    public JadwalKontrolEntity jadwalkanKontrol(int idPasien, LocalDate tanggal) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kunciRekamMedis(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }
}
