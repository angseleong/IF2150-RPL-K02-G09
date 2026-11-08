package sehati.presentation;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import sehati.business.control.AutentikasiManager;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

/**
 * Cangkang aplikasi: satu jendela, bilah aplikasi, perpindahan layar, dan pintasan global (DPPL pengantar BAB 6, KF22,
 * KF24). Setiap layar adalah satu FXML di {@code sehati/fxml} yang bernama sama dengan kelas Form-nya.
 */
public final class Navigasi {

    private static final String LAYAR_MASUK = "LoginForm";

    /** Nama layar sesuai Subbab 6.1 sampai 6.15 DPPL. */
    static final Map<String, String> JUDUL = Map.ofEntries(
            Map.entry(LAYAR_MASUK, "Masuk dan Dasbor"),
            Map.entry("PasienForm", "Pendaftaran Pasien"),
            Map.entry("KunjunganForm", "Kunjungan Pasien"),
            Map.entry("AntreanSkriningForm", "Antrean Skrining"),
            Map.entry("SkriningForm", "Formulir Skrining"),
            Map.entry("RekamMedisForm", "Rekam Medis Pasien"),
            Map.entry("PemeriksaanForm", "Formulir Pemeriksaan"),
            Map.entry("ResepForm", "Resep Elektronik"),
            Map.entry("AntreanResepForm", "Antrean Resep Apotek"),
            Map.entry("PenyerahanObatForm", "Penyerahan Obat"),
            Map.entry("PersediaanForm", "Persediaan Obat"),
            Map.entry("DaftarPantauForm", "Daftar Pantau Pasien"),
            Map.entry("LaporanForm", "Laporan Periodik"),
            Map.entry("SinkronisasiForm", "Sinkronisasi SATUSEHAT"),
            Map.entry("KonfigurasiForm", "Konfigurasi dan Basis Data"));

    private record Layar(String nama, Parent isi) {
    }

    // Puncak tumpukan adalah layar yang sedang tampil, dasarnya selalu LoginForm.
    private static final Deque<Layar> RIWAYAT = new ArrayDeque<>();

    private static BorderPane bingkai;
    private static BilahAplikasi bilah;
    private static boolean sesiAktif;
    private static String peranSesi;
    private static final AutentikasiManager AUTENTIKASI = new AutentikasiManager();

    private Navigasi() {
    }

    public static void mulai(Stage stage) {
        bingkai = new BorderPane();
        bilah = new BilahAplikasi(Navigasi::keluar);
        Scene scene = new Scene(bingkai, 1280, 800);
        scene.getStylesheets().add(Navigasi.class.getResource("/sehati/css/sehati.css").toExternalForm());
        pasangPintasan(scene);
        pasangPewaktuDiam(scene);
        stage.setTitle("SEHATI");
        stage.setScene(scene);
        buka(LAYAR_MASUK);
        stage.show();
    }

    /**
     * Membuka layar dan mengembalikan objek Form-nya, sehingga pemanggil dapat meneruskan data, misalnya
     * {@code SkriningForm f = Navigasi.buka("SkriningForm");}.
     */
    public static <T> T buka(String layar) {
        if (!JUDUL.containsKey(layar)) {
            throw new IllegalArgumentException("Layar tidak dikenal: " + layar);
        }
        // Algo-C01-02: dicek tiap buka, bukan cuma lewat menu, karena layar bisa dibuka dari layar lain
        if (!LAYAR_MASUK.equals(layar) && !AUTENTIKASI.periksaHakAkses(peranSesi(), layar)) {
            throw new IllegalStateException("Peran " + peranSesi() + " tidak berwenang membuka layar ini");
        }
        FXMLLoader loader = new FXMLLoader(Navigasi.class.getResource("/sehati/fxml/" + layar + ".fxml"));
        Parent isi;
        try {
            isi = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Layar " + JUDUL.get(layar) + " gagal dimuat", e);
        }
        RIWAYAT.push(new Layar(layar, isi));
        tampilkan(RIWAYAT.peek());
        return loader.getController();
    }

    /**
     * Kembali ke layar sebelumnya dengan isian yang masih utuh (Esc pada Tabel 6.1).
     */
    public static void kembali() {
        if (RIWAYAT.size() > 1) {
            RIWAYAT.pop();
            tampilkan(RIWAYAT.peek());
        }
    }

    /**
     * Kembali ke dasbor, yaitu dasar tumpukan layar (Ctrl+D pada Tabel 6.1).
     */
    public static void keDasbor() {
        while (RIWAYAT.size() > 1) {
            RIWAYAT.pop();
        }
        tampilkan(RIWAYAT.peek());
    }

    /**
     * Mencatat pengguna yang tampil pada bilah aplikasi. Dipanggil setelah autentikasi berhasil.
     */
    public static void mulaiSesi(String namaPengguna, String peran) {
        sesiAktif = true;
        peranSesi = peran;
        bilah.setPengguna(namaPengguna, peran);
        bingkai.setTop(bilah);
    }

    /**
     * Peran pengguna yang sedang masuk, untuk layar yang membatasi kolom menurut peran (misalnya I-06), atau null.
     */
    public static String peranSesi() {
        return sesiAktif ? peranSesi : null;
    }

    /**
     * Mengakhiri sesi di layar dan kembali ke I-01 dengan LoginForm yang baru, sehingga isian lama tidak tertinggal.
     */
    public static void keluar() {
        akhiriSesi(false);
    }

    private static void akhiriSesi(boolean habisWaktu) {
        String galat = null;
        try {
            AUTENTIKASI.tutupSesi(habisWaktu);
        } catch (IllegalStateException e) {
            galat = e.getMessage();
        }
        sesiAktif = false;
        peranSesi = null;
        RIWAYAT.clear();
        LoginForm masuk = buka(LAYAR_MASUK);
        if (galat != null) {
            masuk.tampilkanPesanGagal(galat);
        } else if (habisWaktu) {
            masuk.tampilkanPesanGagal("Sesi berakhir setelah diam " + AutentikasiManager.BATAS_DIAM_MENIT
                    + " menit, silakan masuk kembali");
        }
    }

    /**
     * Memperbarui indikator koneksi pada bilah aplikasi (KF22). Aman dipanggil dari thread latar.
     */
    public static void aturDaring(boolean daring) {
        Platform.runLater(() -> bilah.setDaring(daring));
    }

    private static void tampilkan(Layar layar) {
        bilah.setJudul(JUDUL.get(layar.nama()));
        // Dasbor berbagi I-01 dengan layar masuk, jadi bilah mengikuti sesi agar Keluar tetap terlihat di dasbor.
        bingkai.setTop(sesiAktif ? bilah : null);
        bingkai.setCenter(layar.isi());
        // Fokus awal tidak boleh jatuh ke tombol Keluar, karena tombol Spasi akan langsung mengakhiri sesi.
        layar.isi().requestFocus();
    }

    // Subbab 5.31: tiap ketukan/klik menggeser waktuMulai di memori, lalu tiap menit dicek habisWaktu(15).
    // Waktu masuk di DB tetap, karena simpanSesi() hanya mengubah status_aktif.
    private static void pasangPewaktuDiam(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, Navigasi::catatAktivitas);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, Navigasi::catatAktivitas);
        Timeline pewaktu = new Timeline(new KeyFrame(Duration.minutes(1), e -> {
            SesiEntity sesi = SesiAktif.ambil();
            if (sesiAktif && sesi != null && sesi.habisWaktu(AutentikasiManager.BATAS_DIAM_MENIT)) {
                akhiriSesi(true);
            }
        }));
        pewaktu.setCycleCount(Timeline.INDEFINITE);
        pewaktu.play();
    }

    private static void catatAktivitas(Event e) {
        SesiEntity sesi = SesiAktif.ambil();
        if (sesi != null) {
            sesi.setWaktuMulai(LocalDateTime.now());
        }
    }

    private static void pasangPintasan(Scene scene) {
        KeyCombination ctrlD = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
        // Filter agar pintasan tetap berjalan saat fokus di kolom isian, yang memakai Ctrl+D sendiri pada macOS.
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F1) {
                TabelPintasan.tampilkan(scene);
                e.consume();
            } else if (ctrlD.match(e) && sesiAktif) {
                keDasbor();
                e.consume();
            }
        });
        // Handler, bukan filter, agar Form dan kontrol seperti ComboBox dapat memakai Esc lebih dulu untuk membatalkan.
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                kembali();
                e.consume();
            }
        });
    }
}
