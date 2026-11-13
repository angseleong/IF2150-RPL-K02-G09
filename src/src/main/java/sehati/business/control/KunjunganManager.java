package sehati.business.control;

import sehati.business.entity.AntreanEntity;
import sehati.business.entity.AuditEntity;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PoliEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataAkunBoundary;
import sehati.dataaccess.DataKlinisBoundary;
import sehati.dataaccess.DataKunjunganBoundary;
import sehati.dataaccess.DataPantauBoundary;
import sehati.dataaccess.DataPasienBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C19 - KunjunganManager.
 *
 * Membuka kunjungan, menolak kunjungan ganda, dan menerbitkan nomor antrean berurutan per poli per hari.
 * Kelas ini tidak menyimpan atribut: setiap operasi membuka satu koneksi sendiri dan membentuk
 * DataBoundary di dalamnya (Tabel 5.19.2 DPPL).
 */
public class KunjunganManager {

    private static final Logger LOG = Logger.getLogger(KunjunganManager.class.getName());

    private static final String STATUS_AWAL = "menunggu skrining";
    private static final String JENIS_ANTREAN_SKRINING = "skrining";

    public List<PoliEntity> daftarPoli() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataKunjunganBoundary(koneksi).ambilSemuaPoli();
        } catch (SQLException e) {
            throw galatTeknis("Pengambilan daftar poli gagal", e);
        }
    }

    // Kunjungan pasien yang masih berjalan hari ini, atau null. Dipakai I-03 untuk UC03 alternatif 1.
    public KunjunganEntity kunjunganAktif(int idPasien) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return cegahKunjunganGanda(new DataKunjunganBoundary(koneksi), idPasien);
        } catch (SQLException e) {
            throw galatTeknis("Pemeriksaan kunjungan aktif gagal", e);
        }
    }

    /**
     * Membuka kunjungan pasien ke satu poli (Algo-C19-01). Kunjungan aktif hari ini dikembalikan apa adanya tanpa
     * nomor antrean baru, sehingga pasien tidak pernah memegang dua nomor (skenario alternatif 1 UC03).
     */
    public KunjunganEntity bukaKunjungan(int idPasien, int idPoli) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataKunjunganBoundary dataKunjungan = new DataKunjunganBoundary(koneksi);
            DataPasienBoundary dataPasien = new DataPasienBoundary(koneksi);
            DataAkunBoundary dataAkun = new DataAkunBoundary(koneksi);
            // Diperiksa sebelum transaksi agar nomor baru tidak diterbitkan untuk kunjungan yang sudah ada.
            KunjunganEntity kunjunganAktif = cegahKunjunganGanda(dataKunjungan, idPasien);
            if (kunjunganAktif != null) {
                return kunjunganAktif;
            }
            SesiAktif.wajibPeran("Petugas Administrasi");
            int idPengguna = SesiAktif.idPenggunaSesi();
            koneksi.setAutoCommit(false);
            try {
                int nomor = terbitkanNomorAntrean(dataKunjungan, idPoli);
                // Pasien tanpa rekam medis tidak dapat masuk pelayanan (Algo-C19-01, KNF07).
                if (dataPasien.ambilRekamMedis(idPasien) == null) {
                    throw new IllegalArgumentException("Pasien belum memiliki rekam medis");
                }
                KunjunganEntity kunjungan =
                        new KunjunganEntity(0, idPasien, idPoli, LocalDate.now(), nomor, STATUS_AWAL);
                dataKunjungan.simpanKunjungan(kunjungan);
                AntreanEntity antrean = dataKunjungan.ambilAntrean(JENIS_ANTREAN_SKRINING);
                antrean.tambahkan(kunjungan.getIdKunjungan());
                dataKunjungan.simpanAntrean(antrean);
                selesaikanJadwalKontrol(koneksi, idPasien);
                dataAkun.simpanAudit(AuditEntity.buat(idPengguna, "Kunjungan baru, antrean " + nomor));
                koneksi.commit();
                return kunjungan;
            } catch (RuntimeException | SQLException e) {
                batalkanDiamDiam(koneksi);
                throw e;
            }
        } catch (SQLException e) {
            throw galatTeknis("Pembukaan kunjungan gagal", e);
        }
    }

    // Pasien yang datang pada atau setelah tanggal kontrolnya dianggap sudah kontrol (Subbab 5.45, KF12), sehingga
    // jadwalnya selesai dan entri "kunjungan ulang" miliknya tidak lagi meminta perawat menghubungi.
    private void selesaikanJadwalKontrol(Connection koneksi, int idPasien) {
        LocalDate hariIni = LocalDate.now();
        DataKlinisBoundary dataKlinis = new DataKlinisBoundary(koneksi);
        for (JadwalKontrolEntity j : dataKlinis.jadwalJatuhTempo(idPasien, hariIni)) {
            j.tandaiSelesai();
            dataKlinis.simpanJadwalKontrol(j);
        }
        DataPantauBoundary dataPantau = new DataPantauBoundary(koneksi);
        for (DaftarPantauEntity e : dataPantau.ambilEntri()) {
            if (e.getIdPasien() == idPasien && RisikoManager.JENIS_KUNJUNGAN_ULANG.equals(e.getJenisRisiko())
                    && e.getTanggalPemicu() != null && !e.getTanggalPemicu().isAfter(hariIni)) {
                e.perbaruiStatus(DaftarPantauEntity.STATUS_SELESAI);
                dataPantau.simpanEntri(e);
            }
        }
    }

    private KunjunganEntity cegahKunjunganGanda(DataKunjunganBoundary dataKunjungan, int idPasien) {
        return dataKunjungan.kunjunganAktif(idPasien, LocalDate.now());
    }

    /**
     * Menerbitkan nomor antrean berikut dengan mengunci baris poli sehingga dua loket bersamaan tidak mendapat
     * nomor yang sama (Algo-C19-02, KF23). Selalu dipanggil di dalam transaksi bukaKunjungan.
     */
    private int terbitkanNomorAntrean(DataKunjunganBoundary dataKunjungan, int idPoli) {
        PoliEntity poli = dataKunjungan.kunciPoli(idPoli);
        if (poli == null) {
            throw new IllegalArgumentException("Poli tidak ditemukan");
        }
        int nomor = poli.ambilNomorBerikutnya(LocalDate.now());
        // Urutan tersimpan di basis data agar seluruh komputer loket berbagi urutan yang sama (Tabel 5.19.2).
        dataKunjungan.simpanPoli(poli);
        return nomor;
    }

    private void batalkanDiamDiam(Connection koneksi) {
        try {
            koneksi.rollback();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Rollback pembukaan kunjungan gagal", e);
        }
    }

    private IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException("Basis data tidak dapat dihubungi", e);
    }
}
