package sehati.presentation;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import sehati.business.control.AutentikasiManager;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

/**
 * C01 - LoginForm.
 *
 * Menampilkan layar masuk, menerima nama pengguna dan kata sandi, lalu meneruskannya ke AutentikasiManager.
 */
public class LoginForm {

    // Algo-C01-01. Skrining, Pemeriksaan, Resep, dan PenyerahanObat dibuka dari layar induknya, bukan dasbor.
    static final List<String> DAFTAR_MENU = List.of("PasienForm", "KunjunganForm", "AntreanSkriningForm",
            "RekamMedisForm", "AntreanResepForm", "PersediaanForm", "DaftarPantauForm", "LaporanForm",
            "SinkronisasiForm", "KonfigurasiForm");

    private final AutentikasiManager autentikasiManager = new AutentikasiManager();

    private String namaPengguna;
    private String kataSandi;

    @FXML private TextField kolomNamaPengguna;
    @FXML private PasswordField kolomKataSandi;
    @FXML private Label labelPesanGagal;
    @FXML private Button tombolMasuk;
    @FXML private VBox panelDasbor;
    @FXML private Label labelDasbor;
    @FXML private FlowPane kartuMenu;
    @FXML private Label labelAksesDitolak;
    @FXML private PanelPeringatan panelPencadangan;

    @FXML
    private void initialize() {
        tombolMasuk.setDefaultButton(true);
        tombolMasuk.setOnAction(e -> kirimKredensial());
        labelPesanGagal.managedProperty().bind(labelPesanGagal.visibleProperty());
        labelAksesDitolak.managedProperty().bind(labelAksesDitolak.visibleProperty());
        tampilkan();
    }

    public void tampilkan() {
        kolomNamaPengguna.clear();
        kolomKataSandi.clear();
        labelPesanGagal.setVisible(false);
        labelAksesDitolak.setVisible(false);
        panelDasbor.setVisible(false);
        // runLater karena Navigasi memfokuskan akar layar setelah layar dipasang
        Platform.runLater(kolomNamaPengguna::requestFocus);
    }

    // Algo-C01-01
    public void kirimKredensial() {
        namaPengguna = kolomNamaPengguna.getText() == null ? "" : kolomNamaPengguna.getText().strip();
        kataSandi = kolomKataSandi.getText();
        if (namaPengguna.isEmpty() || kataSandi == null || kataSandi.isEmpty()) {
            tampilkanPesanGagal("Nama pengguna dan kata sandi wajib diisi");
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
            tampilkanPesanGagal("Nama pengguna atau kata sandi salah");
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
        labelDasbor.setText("Dasbor (peran " + Navigasi.peranSesi() + ")");
        kartuMenu.getChildren().clear();
        for (String layar : menu) {
            Button kartu = new Button(Navigasi.JUDUL.get(layar));
            kartu.getStyleClass().add("kartu-menu");
            kartu.setOnAction(e -> bukaLayar(layar));
            kartuMenu.getChildren().add(kartu);
        }
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

    // Subbab 5.1: sandi dikosongkan setelah autentikasi, berhasil maupun gagal
    private void hapusSandi() {
        kataSandi = "";
        kolomKataSandi.clear();
    }
}
