package sehati.business.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import sehati.business.entity.AuditEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataAkunBoundary;

/**
 * C17 - AutentikasiManager.
 *
 * Memeriksa kredensial terhadap hash kata sandi, membuka dan mengakhiri sesi (termasuk batas diam 15 menit), serta
 * memeriksa hak akses peran untuk setiap layar.
 */
public class AutentikasiManager {

    private static final Logger LOG = Logger.getLogger(AutentikasiManager.class.getName());

    // KNF04
    public static final int BATAS_DIAM_MENIT = 15;

    // Algo-C17-02. LoginForm tidak ada di sini karena dibuka sebelum pengguna punya peran.
    private static final Map<String, Set<String>> HAK_AKSES = Map.of(
            "Petugas Administrasi", Set.of("PasienForm", "KunjunganForm", "LaporanForm", "SinkronisasiForm",
                    "KonfigurasiForm"),
            "Perawat", Set.of("AntreanSkriningForm", "SkriningForm", "RekamMedisForm", "DaftarPantauForm"),
            "Dokter", Set.of("AntreanSkriningForm", "SkriningForm", "RekamMedisForm", "PemeriksaanForm", "ResepForm",
                    "DaftarPantauForm"),
            "Petugas Farmasi", Set.of("AntreanResepForm", "PenyerahanObatForm", "PersediaanForm"));

    private int percobaanGagal;

    // Algo-C17-01. null = nama/sandi salah, IllegalStateException = akun nonaktif.
    public SesiEntity autentikasi(String nama, String sandi) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataAkunBoundary dataAkun = new DataAkunBoundary(koneksi);
            PenggunaEntity pengguna = dataAkun.cariPengguna(nama);
            if (pengguna == null) {
                percobaanGagal++;
                // id 0 dipakai untuk nama yang tidak terdaftar
                dataAkun.simpanAudit(AuditEntity.buat(0,
                        "Masuk gagal: nama '" + nama + "' tidak dikenal, percobaan ke-" + percobaanGagal));
                return null;
            }
            if (!pengguna.aktif()) {
                throw new IllegalStateException("Akun nonaktif, hubungi Petugas Administrasi");
            }
            if (!pengguna.cocokkanSandi(sandi)) {
                percobaanGagal++;
                dataAkun.simpanAudit(AuditEntity.buat(pengguna.getIdPengguna(),
                        "Masuk gagal, percobaan ke-" + percobaanGagal));
                return null;
            }
            percobaanGagal = 0;
            SesiEntity sesi = bukaSesi(pengguna, dataAkun);
            dataAkun.simpanAudit(AuditEntity.buat(pengguna.getIdPengguna(), "Masuk ke aplikasi"));
            return sesi;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Koneksi autentikasi gagal", e);
            throw new IllegalStateException("Basis data tidak dapat dihubungi", e);
        }
    }

    // dataAkun ikut dioper supaya sesi disimpan di koneksi yang sama, tidak buka koneksi baru
    private SesiEntity bukaSesi(PenggunaEntity p, DataAkunBoundary dataAkun) {
        SesiEntity sesi = new SesiEntity(UUID.randomUUID(), p.getIdPengguna(), LocalDateTime.now(), true);
        dataAkun.simpanSesi(sesi);
        SesiAktif.pasang(sesi, p.getPeran());
        return sesi;
    }

    public boolean periksaHakAkses(String peran, String layar) {
        Set<String> layarPeran = peran == null ? null : HAK_AKSES.get(peran);
        return layarPeran != null && layarPeran.contains(layar);
    }

    // true bila sesi di komputer ini sudah ditutup dari luar, misalnya akunnya dinonaktifkan admin (UC12 alternatif 3).
    // Galat basis data tidak dihitung sebagai diputus, supaya pengguna tidak keluar hanya karena jaringan putus.
    public boolean sesiDiputus() {
        SesiEntity sesi = SesiAktif.ambil();
        if (sesi == null) {
            return false;
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return !new DataAkunBoundary(koneksi).sesiMasihAktif(sesi.getIdSesi());
        } catch (SQLException | RuntimeException e) {
            LOG.log(Level.WARNING, "Status sesi tidak terbaca", e);
            return false;
        }
    }

    // Dipanggil saat tombol Keluar ditekan atau sesi diam lewat batas (Algo-C31-01).
    public void tutupSesi(boolean habisWaktu) {
        SesiEntity sesi = SesiAktif.ambil();
        if (sesi == null) {
            return;
        }
        // Lepas dulu dari memori, jadi pengguna tetap keluar walaupun DB mati.
        sesi.tutup();
        SesiAktif.lepas();
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataAkunBoundary dataAkun = new DataAkunBoundary(koneksi);
            dataAkun.simpanSesi(sesi);
            dataAkun.simpanAudit(AuditEntity.buat(sesi.getIdPengguna(),
                    habisWaktu ? "Sesi berakhir setelah diam " + BATAS_DIAM_MENIT + " menit" : "Keluar dari aplikasi"));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Penutupan sesi gagal disimpan", e);
            throw new IllegalStateException("Sesi ditutup, tetapi basis data tidak dapat dihubungi", e);
        }
    }
}
