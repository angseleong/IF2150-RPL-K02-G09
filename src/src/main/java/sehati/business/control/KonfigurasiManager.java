package sehati.business.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.AuditEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PencadanganEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.PoliEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.PengaturanAplikasi;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataAkunBoundary;
import sehati.dataaccess.DataFarmasiBoundary;
import sehati.dataaccess.DataKlinisBoundary;
import sehati.dataaccess.DataKunjunganBoundary;
import sehati.dataaccess.DataPencadanganBoundary;

/**
 * C29 - KonfigurasiManager.
 *
 * Mengurus akun staf, data master obat, poli, dan ambang risiko, serta menjadwalkan pencadangan dan memantau
 * hasilnya.
 */
public class KonfigurasiManager {

    private static final Logger LOG = Logger.getLogger(KonfigurasiManager.class.getName());

    // Algo-C29-01
    static final Set<String> PERAN_SAH = Set.of("Petugas Administrasi", "Perawat", "Dokter", "Petugas Farmasi");
    static final int PANJANG_SANDI_MINIMUM = 8;

    // Isi tab Akun Pengguna dan Data Master I-15
    public record DataMaster(List<PenggunaEntity> akun, List<PoliEntity> poli, List<ObatEntity> obat,
            List<AmbangRisikoEntity> ambang) {
    }

    public DataMaster muatDataMaster() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataMaster(new DataAkunBoundary(koneksi).semuaPengguna(),
                    new DataKunjunganBoundary(koneksi).ambilSemuaPoli(),
                    new DataFarmasiBoundary(koneksi).ambilSemuaObat(),
                    new DataKlinisBoundary(koneksi).ambilAmbang());
        } catch (SQLException e) {
            throw galatTeknis("Data master gagal dimuat", e);
        }
    }

    // Algo-C29-01. idPengguna 0 = akun baru. Sandi kosong saat menyunting = tidak diganti.
    public void simpanAkun(PenggunaEntity p, String sandi) {
        if (p.getNamaPengguna() == null || p.getNamaPengguna().isBlank()) {
            throw new IllegalArgumentException("Nama pengguna wajib diisi");
        }
        if (!PERAN_SAH.contains(p.getPeran())) {
            throw new IllegalArgumentException("Peran tidak dikenal");
        }
        boolean adaSandi = sandi != null && !sandi.isEmpty();
        // Dicek juga saat ganti sandi akun lama, bukan cuma akun baru
        if ((p.getIdPengguna() == 0 || adaSandi) && (sandi == null || sandi.length() < PANJANG_SANDI_MINIMUM)) {
            throw new IllegalArgumentException("Kata sandi minimal " + PANJANG_SANDI_MINIMUM + " karakter");
        }
        SesiAktif.wajibPeran("Petugas Administrasi");
        int pelaku = SesiAktif.idPenggunaSesi();
        transaksi("Akun gagal disimpan", koneksi -> {
            DataAkunBoundary dataAkun = new DataAkunBoundary(koneksi);
            PenggunaEntity pengguna;
            String deskripsi;
            if (p.getIdPengguna() == 0) {
                if (dataAkun.namaPenggunaDipakai(p.getNamaPengguna())) {
                    throw new IllegalArgumentException("Nama pengguna sudah dipakai");
                }
                pengguna = new PenggunaEntity(p.getNamaPengguna(), BCrypt.hashpw(sandi, BCrypt.gensalt(12)),
                        p.getPeran());
                deskripsi = "Akun baru " + p.getNamaPengguna() + " (" + p.getPeran() + ")";
            } else {
                pengguna = dataAkun.ambilPengguna(p.getIdPengguna());
                if (pengguna == null) {
                    throw new IllegalArgumentException("Akun tidak ditemukan");
                }
                // Sama dengan nonaktifkanAkun(): admin tidak boleh mengunci dirinya sendiri dari layar ini.
                if (pengguna.getIdPengguna() == pelaku && !pengguna.getPeran().equals(p.getPeran())) {
                    throw new IllegalArgumentException("Peran akun yang sedang dipakai tidak dapat diubah");
                }
                pengguna.setPeran(p.getPeran());
                if (adaSandi) {
                    pengguna.setKataSandiHash(BCrypt.hashpw(sandi, BCrypt.gensalt(12)));
                }
                deskripsi = "Perubahan akun " + pengguna.getNamaPengguna();
            }
            dataAkun.simpanPengguna(pengguna);
            p.setIdPengguna(pengguna.getIdPengguna());
            dataAkun.simpanAudit(AuditEntity.buat(pelaku, deskripsi));
        });
    }

    // Algo-C29-02. Dinonaktifkan, bukan dihapus, supaya audit yang menunjuk akun ini tetap utuh.
    public void nonaktifkanAkun(int idPengguna) {
        SesiAktif.wajibPeran("Petugas Administrasi");
        int pelaku = SesiAktif.idPenggunaSesi();
        if (idPengguna == pelaku) {
            throw new IllegalStateException("Akun yang sedang dipakai tidak dapat dinonaktifkan");
        }
        transaksi("Akun gagal dinonaktifkan", koneksi -> {
            DataAkunBoundary dataAkun = new DataAkunBoundary(koneksi);
            PenggunaEntity pengguna = dataAkun.ambilPengguna(idPengguna);
            if (pengguna == null) {
                throw new IllegalArgumentException("Akun tidak ditemukan");
            }
            pengguna.nonaktifkan();
            dataAkun.simpanPengguna(pengguna);
            dataAkun.tutupSesiAktif(idPengguna);
            dataAkun.simpanAudit(AuditEntity.buat(pelaku, "Akun " + pengguna.getNamaPengguna() + " dinonaktifkan"));
        });
    }

    // Hanya nilai batas ambang yang bisa diubah (I-15-09)
    public void simpanDataMaster(AmbangRisikoEntity a) {
        if (!(a.getNilaiBatas() > 0)) {
            throw new IllegalArgumentException("Nilai ambang harus lebih dari nol");
        }
        SesiAktif.wajibPeran("Petugas Administrasi");
        int pelaku = SesiAktif.idPenggunaSesi();
        transaksi("Ambang risiko gagal disimpan", koneksi -> {
            new DataKlinisBoundary(koneksi).simpanAmbang(a);
            new DataAkunBoundary(koneksi).simpanAudit(AuditEntity.buat(pelaku,
                    "Ambang " + a.getJenisRisiko() + " diubah menjadi " + a.getNilaiBatas()));
        });
    }

    // Poli baru (idPoli 0) atau ganti nama. Baris poli dikunci supaya nomor antrean yang sedang
    // diterbitkan loket tidak tertimpa nilai lama (Q-C58-04 ikut menulis urutan_terakhir).
    public void simpanPoli(int idPoli, String namaPoli) {
        String nama = namaPoli == null ? "" : namaPoli.strip();
        if (nama.isEmpty()) {
            throw new IllegalArgumentException("Nama poli wajib diisi");
        }
        SesiAktif.wajibPeran("Petugas Administrasi");
        int pelaku = SesiAktif.idPenggunaSesi();
        transaksi("Poli gagal disimpan", koneksi -> {
            DataKunjunganBoundary dataKunjungan = new DataKunjunganBoundary(koneksi);
            PoliEntity poli;
            String deskripsi;
            if (idPoli == 0) {
                poli = new PoliEntity(0, nama, 0, null);
                deskripsi = "Poli baru " + nama;
            } else {
                poli = dataKunjungan.kunciPoli(idPoli);
                if (poli == null) {
                    throw new IllegalArgumentException("Poli tidak ditemukan");
                }
                deskripsi = "Poli " + poli.getNamaPoli() + " diganti nama menjadi " + nama;
                poli.setNamaPoli(nama);
            }
            dataKunjungan.simpanPoli(poli);
            new DataAkunBoundary(koneksi).simpanAudit(AuditEntity.buat(pelaku, deskripsi));
        });
    }

    // Jenis obat baru. Stok tidak diisi di sini supaya setiap perubahan stok tercatat lewat UC08.
    public void tambahObat(ObatEntity o) {
        String nama = o.getNamaObat() == null ? "" : o.getNamaObat().strip();
        String sediaan = o.getSediaan() == null ? "" : o.getSediaan().strip();
        if (nama.isEmpty() || sediaan.isEmpty()) {
            throw new IllegalArgumentException("Nama obat dan sediaan wajib diisi");
        }
        if (o.getAmbangMinimum() < 0) {
            throw new IllegalArgumentException("Stok minimum tidak boleh negatif");
        }
        o.setNamaObat(nama);
        o.setSediaan(sediaan);
        SesiAktif.wajibPeran("Petugas Administrasi");
        int pelaku = SesiAktif.idPenggunaSesi();
        transaksi("Obat gagal ditambahkan", koneksi -> {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);
            // Tabel obat tidak punya UNIQUE, jadi nama + sediaan ganda dicek di sini
            for (ObatEntity ada : dataFarmasi.ambilSemuaObat()) {
                if (ada.getNamaObat().equalsIgnoreCase(nama) && ada.getSediaan().equalsIgnoreCase(sediaan)) {
                    throw new IllegalArgumentException("Obat " + nama + " " + sediaan + " sudah terdaftar");
                }
            }
            dataFarmasi.tambahObat(o);
            new DataAkunBoundary(koneksi).simpanAudit(AuditEntity.buat(pelaku,
                    "Obat baru " + nama + " (" + sediaan + ")"));
        });
    }

    // null kalau belum pernah ada pencadangan
    public PencadanganEntity muatStatusPencadangan() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataPencadanganBoundary(koneksi, lokasiMedia()).ambilTerakhir();
        } catch (SQLException e) {
            throw galatTeknis("Status pencadangan gagal dimuat", e);
        }
    }

    // Algo-C29-03. Bisa lama, jadi jangan dipanggil dari JavaFX Application Thread.
    public PencadanganEntity jalankanPencadangan() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataPencadanganBoundary dataPencadangan = new DataPencadanganBoundary(koneksi, lokasiMedia());
            PencadanganEntity p = dataPencadangan.jalankanPgDump(null);
            if (p.berhasil() && !dataPencadangan.verifikasiArsip(p)) {
                p.setStatus("gagal verifikasi");
            }
            dataPencadangan.simpanCatatan(p);
            return p;
        } catch (SQLException e) {
            throw galatTeknis("Pencadangan gagal dicatat", e);
        }
    }

    // Pencadangan harian jam PENCADANGAN_JAM. Kosong = tidak terjadwal, cukup diisi di komputer server.
    public static void jadwalkanPencadangan() {
        String jam = PengaturanAplikasi.muat().ambil("PENCADANGAN_JAM", "");
        if (jam.isBlank()) {
            return;
        }
        int jamMulai;
        try {
            jamMulai = Integer.parseInt(jam.strip());
        } catch (NumberFormatException e) {
            jamMulai = -1;
        }
        if (jamMulai < 0 || jamMulai > 23) {
            LOG.warning(() -> "PENCADANGAN_JAM harus 0 sampai 23, bukan " + jam);
            return;
        }
        ScheduledExecutorService penjadwal = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "pencadangan");
            t.setDaemon(true);
            return t;
        });
        penjadwal.scheduleAtFixedRate(() -> {
            try {
                PencadanganEntity p = new KonfigurasiManager().jalankanPencadangan();
                LOG.info(() -> "Pencadangan terjadwal: " + p.getStatus() + " " + p.getLokasiMedia());
            } catch (RuntimeException e) {
                LOG.log(Level.SEVERE, "Pencadangan terjadwal gagal", e);
            }
        }, jedaSampai(jamMulai, LocalDateTime.now()), TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);
    }

    // Menit dari sekarang sampai jam berikutnya
    static long jedaSampai(int jam, LocalDateTime sekarang) {
        LocalDateTime berikutnya = sekarang.toLocalDate().atTime(jam, 0);
        if (!berikutnya.isAfter(sekarang)) {
            berikutnya = berikutnya.plusDays(1);
        }
        return Duration.between(sekarang, berikutnya).toMinutes();
    }

    private static String lokasiMedia() {
        return PengaturanAplikasi.muat().ambil("LOKASI_MEDIA_CADANGAN", "");
    }

    // Kesepakatan 2 BAB 5: satu koneksi, commit kalau semua langkah berhasil, rollback kalau ada yang gagal
    private static void transaksi(String pesanGagal, Consumer<Connection> langkah) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            koneksi.setAutoCommit(false);
            try {
                langkah.accept(koneksi);
                koneksi.commit();
            } catch (RuntimeException e) {
                koneksi.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw galatTeknis(pesanGagal, e);
        }
    }

    private static IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ": basis data tidak dapat dihubungi", e);
    }
}
