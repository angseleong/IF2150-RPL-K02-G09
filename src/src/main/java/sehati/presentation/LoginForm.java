package sehati.presentation;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sehati.business.control.AutentikasiManager;
import sehati.business.control.KonfigurasiManager;
import sehati.business.entity.PencadanganEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

/**
 * C01 - LoginForm.
 *
 * Menampilkan layar masuk, menerima nama pengguna dan kata sandi, lalu meneruskannya ke AutentikasiManager.
 */
public class LoginForm {

    private static final DateTimeFormatter WAKTU_CADANGAN = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Algo-C01-01. Kunjungan, Skrining, Pemeriksaan, Resep, dan PenyerahanObat dibuka dari layar induknya, bukan
    // dasbor. Kunjungan butuh pasien terpilih dari PasienForm (UC03 include UC02).
    static final List<String> DAFTAR_MENU = List.of("PasienForm", "AntreanSkriningForm",
            "RekamMedisForm", "AntreanResepForm", "PersediaanForm", "DaftarPantauForm", "LaporanForm",
            "SinkronisasiForm", "KonfigurasiForm");

    static final Map<String, String> DESKRIPSI = Map.of(
            "PasienForm", "Cari, daftarkan, dan perbarui data pasien.",
            "AntreanSkriningForm", "Panggil pasien dan catat tanda vital.",
            "RekamMedisForm", "Lihat riwayat pasien, lalu mulai pemeriksaan.",
            "AntreanResepForm", "Siapkan resep yang dikirim dokter.",
            "PersediaanForm", "Catat obat masuk dan pantau stok.",
            "DaftarPantauForm", "Hubungi pasien berisiko yang perlu tindak lanjut.",
            "LaporanForm", "Susun rekap kunjungan per periode.",
            "SinkronisasiForm", "Kirim data kunjungan ke SATUSEHAT.",
            "KonfigurasiForm", "Atur akun, data master, dan pencadangan.");

    private final AutentikasiManager autentikasiManager = new AutentikasiManager();

    private String namaPengguna;
    private String kataSandi;

    @FXML private TextField kolomNamaPengguna;
    @FXML private PasswordField kolomKataSandi;
    @FXML private Label labelPesanGagal;
    @FXML private Button tombolMasuk;
    @FXML private VBox kartuMasuk;
    @FXML private HBox merekMasuk;
    @FXML private VBox panelDasbor;
    @FXML private Label labelPeranDasbor;
    @FXML private Label labelDasbor;
    @FXML private FlowPane kartuMenu;
    @FXML private Label labelAksesDitolak;
    @FXML private PanelPeringatan panelPencadangan;
    @FXML private Label labelPencadangan;

    @FXML
    private void initialize() {
        tombolMasuk.setDefaultButton(true);
        tombolMasuk.setOnAction(e -> kirimKredensial());
        labelPesanGagal.managedProperty().bind(labelPesanGagal.visibleProperty());
        labelAksesDitolak.managedProperty().bind(labelAksesDitolak.visibleProperty());
        kartuMasuk.managedProperty().bind(kartuMasuk.visibleProperty());
        panelDasbor.managedProperty().bind(panelDasbor.visibleProperty());
        panelPencadangan.managedProperty().bind(panelPencadangan.visibleProperty());
        labelPencadangan.managedProperty().bind(labelPencadangan.textProperty().isNotEmpty());
        merekMasuk.getChildren().add(0, LogoSehati.tanda(48));
        tampilkan();
    }

    public void tampilkan() {
        kolomNamaPengguna.clear();
        kolomKataSandi.clear();
        labelPesanGagal.setVisible(false);
        labelAksesDitolak.setVisible(false);
        panelDasbor.setVisible(false);
        kartuMasuk.setVisible(true);
        // runLater karena Navigasi memfokuskan akar layar setelah layar dipasang
        Platform.runLater(kolomNamaPengguna::requestFocus);
    }

    // Algo-C01-01
    public void kirimKredensial() {
        namaPengguna = kolomNamaPengguna.getText() == null ? "" : kolomNamaPengguna.getText().strip();
        kataSandi = kolomKataSandi.getText();
        if (namaPengguna.isEmpty() || kataSandi == null || kataSandi.isEmpty()) {
            tampilkanPesanGagal("Isi nama pengguna dan kata sandi.");
            return;
        }
        SesiEntity sesi;
        try {
            sesi = autentikasiManager.autentikasi(namaPengguna, kataSandi);
        } catch (IllegalStateException e) {
            hapusSandi();
            tampilkanPesanGagal(e.getMessage());
            return;
        }
        hapusSandi();
        if (sesi == null) {
            tampilkanPesanGagal("Nama pengguna atau kata sandi salah.");
            return;
        }
        String peran = SesiAktif.peran();
        Navigasi.mulaiSesi(namaPengguna, peran);
        tampilkanDasbor(susunMenu(autentikasiManager, peran));
    }

    static List<String> susunMenu(AutentikasiManager autentikasi, String peran) {
        List<String> menu = new ArrayList<>();
        for (String layar : DAFTAR_MENU) {
            if (autentikasi.periksaHakAkses(peran, layar)) {
                menu.add(layar);
            }
        }
        return menu;
    }

    // I-01-07: layar di luar wewenang tidak digambar sama sekali
    public void tampilkanDasbor(List<String> menu) {
        labelPesanGagal.setVisible(false);
        labelAksesDitolak.setVisible(false);
        labelDasbor.setText(namaPengguna == null ? "Dasbor" : "Selamat datang, " + namaPengguna);
        labelPeranDasbor.setText("Masuk sebagai " + Navigasi.peranSesi() + ". Pilih layar untuk mulai bekerja.");
        kartuMenu.getChildren().clear();
        for (String layar : menu) {
            Label namaLayar = new Label(Navigasi.JUDUL.get(layar));
            namaLayar.getStyleClass().add("kartu-menu-judul");
            Label keterangan = new Label(DESKRIPSI.getOrDefault(layar, ""));
            keterangan.getStyleClass().add("kartu-menu-teks");
            keterangan.setWrapText(true);
            // Lebar tetap agar tinggi label yang terbungkus terhitung benar dan semua kartu sama tinggi.
            keterangan.setPrefWidth(208);
            Button kartu = new Button(null, new VBox(6, namaLayar, keterangan));
            kartu.setAccessibleText(namaLayar.getText());
            kartu.getStyleClass().add("kartu-menu");
            kartu.setOnAction(e -> bukaLayar(layar));
            kartuMenu.getChildren().add(kartu);
        }
        tampilkanPeringatanPencadangan();
        kartuMasuk.setVisible(false);
        panelDasbor.setVisible(true);
        if (!kartuMenu.getChildren().isEmpty()) {
            Platform.runLater(kartuMenu.getChildren().get(0)::requestFocus);
        }
    }

    // Algo-C01-02. Cek hak aksesnya ada di Navigasi.buka() supaya berlaku juga dari layar lain.
    public void bukaLayar(String layar) {
        labelAksesDitolak.setVisible(false);
        try {
            Navigasi.buka(layar);
        } catch (IllegalStateException e) {
            tampilkanPesanGagal(e.getMessage());
        }
    }

    // Sebelum masuk pesannya di bawah kata sandi (I-01-05), sesudah masuk di dasbor (I-01-09).
    public void tampilkanPesanGagal(String pesan) {
        Label sasaran = panelDasbor.isVisible() ? labelAksesDitolak : labelPesanGagal;
        sasaran.setText(panelDasbor.isVisible() ? "Akses ditolak: " + pesan : pesan);
        sasaran.setVisible(true);
    }

    // I-01-08: hanya untuk Petugas Administrasi, hilang setelah pencadangan berikutnya berhasil
    private void tampilkanPeringatanPencadangan() {
        boolean tampil = false;
        String info = "";
        if ("Petugas Administrasi".equals(Navigasi.peranSesi())) {
            try {
                PencadanganEntity p = new KonfigurasiManager().muatStatusPencadangan();
                tampil = p == null || !p.berhasil();
                // KF21: informasi pencadangan terakhir selalu tampil di dasbor administrasi
                info = p == null ? "Belum pernah ada pencadangan."
                        : "Pencadangan terakhir: " + WAKTU_CADANGAN.format(p.getWaktu()) + " (" + p.getStatus()
                                + ") di " + p.getLokasiMedia();
            } catch (IllegalStateException e) {
                // status tidak terbaca bukan berarti gagal, jadi peringatan tidak dipaksa muncul
                tampil = false;
                info = "Status pencadangan tidak dapat dibaca.";
            }
        }
        panelPencadangan.setVisible(tampil);
        labelPencadangan.setText(info);
    }

    // Subbab 5.1: sandi dikosongkan setelah autentikasi, berhasil maupun gagal
    private void hapusSandi() {
        kataSandi = "";
        kolomKataSandi.clear();
    }
}
