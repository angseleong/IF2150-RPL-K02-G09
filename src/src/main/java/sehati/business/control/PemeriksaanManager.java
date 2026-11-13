package sehati.business.control;

import sehati.business.entity.AuditEntity;
import sehati.business.entity.BundelFHIREntity;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KodeICD10Entity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.ResepEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataAkunBoundary;
import sehati.dataaccess.DataKlinisBoundary;
import sehati.dataaccess.DataKunjunganBoundary;
import sehati.dataaccess.DataFarmasiBoundary;
import sehati.dataaccess.DataPantauBoundary;
import sehati.dataaccess.DataPasienBoundary;
import sehati.dataaccess.DataSinkronisasiBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Kunjungan hari ini yang sudah diskrining dan menunggu dokter beserta pasiennya, urut nomor antrean, sebagai
     * daftar pilihan pada I-06 (tambahan implementasi, B.1).
     */
    public Map<KunjunganEntity, PasienEntity> muatKunjunganSiapPeriksa() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataPasienBoundary dataPasien = new DataPasienBoundary(koneksi);
            Map<KunjunganEntity, PasienEntity> hasil = new LinkedHashMap<>();
            for (KunjunganEntity k : new DataKunjunganBoundary(koneksi).kunjunganSiapPeriksa(LocalDate.now())) {
                hasil.put(k, dataPasien.ambilPasien(k.getIdPasien()));
            }
            return hasil;
        } catch (SQLException e) {
            throw galatTeknis("Daftar pasien gagal dimuat", e);
        }
    }

    /**
     * Menyatukan data diri, diagnosis lampau, riwayat obat, skrining hari ini, dan penanda risiko aktif satu
     * kunjungan untuk I-06 (KF09).
     */
    public RekamMedisEntity susunRingkasan(int idKunjungan) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            KunjunganEntity kunjungan = new DataKunjunganBoundary(koneksi).ambilKunjungan(idKunjungan);
            if (kunjungan == null) {
                throw new IllegalArgumentException("Kunjungan tidak ditemukan: " + idKunjungan);
            }
            DataPasienBoundary dataPasien = new DataPasienBoundary(koneksi);
            DataKlinisBoundary dataKlinis = new DataKlinisBoundary(koneksi);
            RekamMedisEntity rm = dataPasien.ambilRekamMedis(kunjungan.getIdPasien());
            // Pasien yang belum punya rekam medis tetap ditampilkan dengan riwayat kosong.
            if (rm == null) {
                rm = new RekamMedisEntity();
            }
            rm.setPasien(dataPasien.ambilPasien(kunjungan.getIdPasien()));
            rm.setSkriningHariIni(dataKlinis.ambilSkrining(idKunjungan));
            rm.setPenandaAktif(dataKlinis.penandaKunjungan(idKunjungan));
            return rm;
        } catch (SQLException e) {
            throw galatTeknis("Ringkasan rekam medis gagal dimuat", e);
        }
    }

    /**
     * Seluruh pengukuran pasien, terlama lebih dulu, sebagai seri grafik garis. Satu titik tetap dikembalikan
     * (KF08, skenario alternatif 4 UC05).
     */
    public List<TandaVitalEntity> susunTren(int idPasien) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataKlinisBoundary(koneksi).riwayatTandaVital(idPasien, 0);
        } catch (SQLException e) {
            throw galatTeknis("Tren tanda vital gagal dimuat", e);
        }
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
        SesiAktif.wajibPeran("Dokter");
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataKlinisBoundary dataKlinis = new DataKlinisBoundary(koneksi);
            PemeriksaanEntity lama = dataKlinis.ambilPemeriksaan(idKunjungan);
            if (lama != null && lama.isTerkunci()) {
                throw new IllegalStateException("Rekam medis kunjungan ini sudah dikunci");
            }
            dataKlinis.simpanPemeriksaan(p, d);
            new DataAkunBoundary(koneksi).simpanAudit(AuditEntity.buat(SesiAktif.idPenggunaAudit(),
                    "Pemeriksaan kunjungan " + idKunjungan + " disimpan"));
        } catch (SQLException e) {
            throw galatTeknis("Pemeriksaan gagal disimpan", e);
        }
    }

    /**
     * Pemeriksaan tersimpan satu kunjungan, atau null bila belum ada, untuk memuat draf pada I-07 (tambahan
     * implementasi, B.1).
     */
    public PemeriksaanEntity muatPemeriksaan(int idKunjungan) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataKlinisBoundary(koneksi).ambilPemeriksaan(idKunjungan);
        } catch (SQLException e) {
            throw galatTeknis("Pemeriksaan gagal dimuat", e);
        }
    }

    /**
     * Diagnosis tersimpan satu kunjungan, atau null bila belum ada (tambahan implementasi, B.1).
     */
    public DiagnosisEntity muatDiagnosis(int idKunjungan) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataKlinisBoundary(koneksi).ambilDiagnosis(idKunjungan);
        } catch (SQLException e) {
            throw galatTeknis("Diagnosis gagal dimuat", e);
        }
    }

    /**
     * Tanggal kontrol harus setelah hari ini (KF12). Dipanggil I-07 sebelum menyimpan apa pun, agar penolakan tanggal
     * tidak terjadi setelah pemeriksaan tersimpan.
     */
    public void validasiTanggalKontrol(LocalDate tanggal) {
        if (tanggal == null || !tanggal.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal kontrol harus setelah hari ini");
        }
    }

    /**
     * Menyimpan jadwal kunjungan ulang berstatus "terjadwal". Jadwal pasien yang sama pada tanggal yang sama tidak
     * dibuat dua kali, agar daftar pantau tidak berisi entri ganda (KF12).
     */
    public JadwalKontrolEntity jadwalkanKontrol(int idPasien, LocalDate tanggal) {
        validasiTanggalKontrol(tanggal);
        SesiAktif.wajibPeran("Dokter");
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataKlinisBoundary dataKlinis = new DataKlinisBoundary(koneksi);
            for (JadwalKontrolEntity ada : dataKlinis.ambilJadwalKontrol()) {
                if (ada.getIdPasien() == idPasien && tanggal.equals(ada.getTanggalRencana())) {
                    return ada;
                }
            }
            JadwalKontrolEntity j = new JadwalKontrolEntity(0, idPasien, tanggal,
                    JadwalKontrolEntity.STATUS_TERJADWAL);
            dataKlinis.simpanJadwalKontrol(j);
            return j;
        } catch (SQLException e) {
            throw galatTeknis("Jadwal kontrol gagal disimpan", e);
        }
    }

    /**
     * Mengunci pemeriksaan, menandai kunjungan "selesai diperiksa", dan mencatat audit dalam satu transaksi
     * (Algo-C22-02, KNF05).
     */
    public void kunciRekamMedis(int idKunjungan) {
        SesiAktif.wajibPeran("Dokter");
        int idPengguna = SesiAktif.idPenggunaSesi();
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataKlinisBoundary dataKlinis = new DataKlinisBoundary(koneksi);
            DataKunjunganBoundary dataKunjungan = new DataKunjunganBoundary(koneksi);
            koneksi.setAutoCommit(false);
            try {
                PemeriksaanEntity p = dataKlinis.ambilPemeriksaan(idKunjungan);
                if (p == null) {
                    throw new IllegalStateException("Pemeriksaan belum disimpan");
                }
                // KF10: rekam medis tidak boleh dikunci tanpa diagnosis berkode ICD-10.
                if (dataKlinis.ambilDiagnosis(idKunjungan) == null) {
                    throw new IllegalStateException("Diagnosis ICD-10 belum diisi");
                }
                p.kunci();
                dataKlinis.simpanPemeriksaan(p, null);
                KunjunganEntity kunjungan = dataKunjungan.ambilKunjungan(idKunjungan);
                kunjungan.perbaruiStatus(KunjunganEntity.STATUS_SELESAI_DIPERIKSA);
                // Tanpa resep yang menunggu di apotek tidak ada langkah apotek yang menutup kunjungan, jadi ditutup dan
                // dibundel di sini (KF14). Resep yang sudah dikembalikan apotek tidak lagi menahan kunjungan.
                boolean adaResepMenunggu = new DataFarmasiBoundary(koneksi).ambilResepKunjungan(idKunjungan).stream()
                        .anyMatch(r -> ResepEntity.STATUS_MENUNGGU.equals(r.getStatus()));
                if (!adaResepMenunggu) {
                    kunjungan.perbaruiStatus(KunjunganEntity.STATUS_SELESAI);
                    String isi = PenyusunBundelFHIR.susun(kunjungan,
                            new DataPasienBoundary(koneksi).ambilPasien(kunjungan.getIdPasien()),
                            dataKlinis.ambilSkrining(idKunjungan), dataKlinis.ambilDiagnosis(idKunjungan), null,
                            Map.of(), LocalDateTime.now());
                    new DataSinkronisasiBoundary(koneksi).simpanBundel(
                            new BundelFHIREntity(0, idKunjungan, isi, "tertunda", null));
                }
                dataKunjungan.simpanKunjungan(kunjungan);
                selesaikanJadwalKontrol(dataKlinis, new DataPantauBoundary(koneksi), kunjungan.getIdPasien());
                new DataAkunBoundary(koneksi).simpanAudit(
                        AuditEntity.buat(idPengguna, "Pemeriksaan kunjungan " + idKunjungan + " dikunci"));
                koneksi.commit();
            } catch (RuntimeException e) {
                koneksi.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw galatTeknis("Rekam medis gagal dikunci", e);
        }
    }

    // Pasien yang sudah diperiksa dokter pada atau setelah tanggal kontrolnya dianggap sudah kontrol (Subbab 5.45,
    // KF12), sehingga jadwalnya selesai dan entri "kunjungan ulang" miliknya tidak lagi meminta perawat menghubungi.
    // Dijalankan saat rekam medis dikunci, bukan saat loket membuka kunjungan, agar pasien yang akhirnya tidak hadir
    // di skrining tetap ada di daftar pantau.
    private static void selesaikanJadwalKontrol(DataKlinisBoundary dataKlinis, DataPantauBoundary dataPantau,
            int idPasien) {
        LocalDate hariIni = LocalDate.now();
        for (JadwalKontrolEntity j : dataKlinis.jadwalJatuhTempo(idPasien, hariIni)) {
            j.tandaiSelesai();
            dataKlinis.simpanJadwalKontrol(j);
        }
        for (DaftarPantauEntity e : dataPantau.ambilEntri()) {
            if (e.getIdPasien() == idPasien && RisikoManager.JENIS_KUNJUNGAN_ULANG.equals(e.getJenisRisiko())
                    && e.getTanggalPemicu() != null && !e.getTanggalPemicu().isAfter(hariIni)) {
                e.perbaruiStatus(DaftarPantauEntity.STATUS_SELESAI);
                dataPantau.simpanEntri(e);
            }
        }
    }

    private static IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ". Hubungi administrator bila berulang.", e);
    }
}
