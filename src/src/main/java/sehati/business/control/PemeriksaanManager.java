package sehati.business.control;

import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KodeICD10Entity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.common.KoneksiBasisData;
import sehati.dataaccess.DataKlinisBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C22 - PemeriksaanManager.
 *
 * Menyusun ringkasan rekam medis dan seri tren, menyimpan pemeriksaan dan diagnosis ICD-10 khusus dokter, serta
 * menjadwalkan kontrol.
 */
public class PemeriksaanManager {

    private static final Logger LOG = Logger.getLogger(PemeriksaanManager.class.getName());

    public RekamMedisEntity susunRingkasan(int idKunjungan) {
        // TODO(#14): isi setelah DataPasienBoundary dan DataKunjunganBoundary (#5) masuk.
        throw new UnsupportedOperationException("TODO");
    }

    public List<TandaVitalEntity> susunTren(int idPasien) {
        // TODO(#14): isi setelah TandaVitalEntity memiliki waktu pengukuran (#29).
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Mencari kode ICD-10 untuk kolom diagnosis. Daftar kosong berarti kode tidak ditemukan (KF10, skenario
     * alternatif 2 UC05).
     */
    public List<KodeICD10Entity> cariKodeICD10(String kataKunci) {
        // Kata kunci kosong akan cocok dengan semua kode, jadi tidak perlu ke basis data.
        if (kataKunci == null || kataKunci.isBlank()) {
            return List.of();
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataKlinisBoundary(koneksi).cariKodeICD10(kataKunci);
        } catch (SQLException e) {
            throw galatTeknis("Kode ICD-10 gagal dicari", e);
        }
    }

    /**
     * Menyimpan draf pemeriksaan beserta diagnosisnya, menimpa draf sebelumnya. Ditolak bila rekam medis kunjungan
     * sudah dikunci (Algo-C22-01).
     */
    public void simpanPemeriksaan(int idKunjungan, PemeriksaanEntity p, DiagnosisEntity d) {
        // Masukan diperiksa lebih dulu agar penolakan tidak menunggu koneksi basis data.
        if (d != null && (d.getKodeICD10() == null || d.getKodeICD10().isBlank())) {
            throw new IllegalArgumentException("Diagnosis harus dipilih dari kode ICD-10");
        }
        p.setIdKunjungan(idKunjungan);
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataKlinisBoundary dataKlinis = new DataKlinisBoundary(koneksi);
            PemeriksaanEntity lama = dataKlinis.ambilPemeriksaan(idKunjungan);
            if (lama != null && lama.isTerkunci()) {
                throw new IllegalStateException("Rekam medis kunjungan ini sudah dikunci");
            }
            dataKlinis.simpanPemeriksaan(p, d);
        } catch (SQLException e) {
            throw galatTeknis("Pemeriksaan gagal disimpan", e);
        }
    }

    /**
     * Menyimpan jadwal kunjungan ulang berstatus "terjadwal" pada tanggal setelah hari ini (KF12).
     */
    public JadwalKontrolEntity jadwalkanKontrol(int idPasien, LocalDate tanggal) {
        if (tanggal == null || !tanggal.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal kontrol harus setelah hari ini");
        }
        JadwalKontrolEntity j = new JadwalKontrolEntity(0, idPasien, tanggal, JadwalKontrolEntity.STATUS_TERJADWAL);
        try (Connection koneksi = KoneksiBasisData.buka()) {
            new DataKlinisBoundary(koneksi).simpanJadwalKontrol(j);
            return j;
        } catch (SQLException e) {
            throw galatTeknis("Jadwal kontrol gagal disimpan", e);
        }
    }

    public void kunciRekamMedis(int idKunjungan) {
        // TODO(#14): isi setelah DataKunjunganBoundary (#5), simpanAudit (#4), dan idPengguna sesi (#10) tersedia.
        throw new UnsupportedOperationException("TODO");
    }

    private static IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ". Hubungi administrator bila berulang.", e);
    }
}
