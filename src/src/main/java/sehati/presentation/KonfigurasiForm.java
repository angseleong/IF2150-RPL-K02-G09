package sehati.presentation;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import sehati.business.control.KonfigurasiManager;
import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PencadanganEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.PoliEntity;

/**
 * C16 - KonfigurasiForm.
 *
 * Menampilkan manajemen akun, data master, dan status pencadangan, lalu meneruskan perubahan ke
 * KonfigurasiManager.
 */
public class KonfigurasiForm {

    static final String TAB_AKUN = "Akun Pengguna";
    static final String TAB_MASTER = "Data Master";
    static final String TAB_PEMELIHARAAN = "Pemeliharaan Basis Data";

    private static final KeyCodeCombination CTRL_S = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private static final KeyCodeCombination CTRL_N = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    private static final DateTimeFormatter WAKTU = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final KonfigurasiManager konfigurasiManager = new KonfigurasiManager();

    private String tabAktif;
    private String isianData;
    private int idAkunTerpilih;
    private AmbangRisikoEntity ambangTerpilih;

    @FXML private Parent akar;
    @FXML private TabPane tabKonfigurasi;
    @FXML private TableView<PenggunaEntity> tabelAkun;
    @FXML private TableColumn<PenggunaEntity, String> kolomAkunNama;
    @FXML private TableColumn<PenggunaEntity, String> kolomAkunPeran;
    @FXML private TableColumn<PenggunaEntity, String> kolomAkunStatus;
    @FXML private TextField kolomNamaPengguna;
    @FXML private Label pesanNamaPengguna;
    @FXML private ComboBox<String> kolomPeran;
    @FXML private PasswordField kolomKataSandi;
    @FXML private Button tombolSimpanAkun;
    @FXML private Button tombolAkunBaru;
    @FXML private Button tombolNonaktifkan;
    @FXML private TableView<AmbangRisikoEntity> tabelAmbang;
    @FXML private TableColumn<AmbangRisikoEntity, String> kolomAmbangJenis;
    @FXML private TableColumn<AmbangRisikoEntity, Double> kolomAmbangNilai;
    @FXML private TextField kolomNilaiAmbang;
    @FXML private Button tombolSimpanAmbang;
    @FXML private ListView<PoliEntity> daftarPoli;
    @FXML private TextField kolomNamaPoli;
    @FXML private Button tombolSimpanPoli;
    @FXML private Button tombolPoliBaru;
    @FXML private TextField kolomNamaObat;
    @FXML private TextField kolomSediaan;
    @FXML private TextField kolomStokMinimum;
    @FXML private Button tombolTambahObat;
    @FXML private TableColumn<ObatEntity, Integer> kolomObatMinimum;
    @FXML private TableView<ObatEntity> tabelObat;
    @FXML private TableColumn<ObatEntity, String> kolomObatNama;
    @FXML private TableColumn<ObatEntity, String> kolomObatSediaan;
    @FXML private TableColumn<ObatEntity, Integer> kolomObatStok;
    @FXML private Label labelWaktuCadangan;
    @FXML private Label labelUkuranCadangan;
    @FXML private Label labelStatusCadangan;
    @FXML private Label labelLokasiCadangan;
    @FXML private PanelPeringatan peringatanCadangan;
    @FXML private Button tombolPencadangan;
    @FXML private ProgressIndicator penandaBerjalan;
    @FXML private Label bilahPesan;

    @FXML
    private void initialize() {
        kolomPeran.setItems(FXCollections.observableArrayList("Petugas Administrasi", "Perawat", "Dokter",
                "Petugas Farmasi"));
        kolomAkunNama.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNamaPengguna()));
        kolomAkunPeran.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getPeran()));
        kolomAkunStatus.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStatus()));
        kolomAmbangJenis.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getJenisRisiko()));
        kolomAmbangNilai.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNilaiBatas()));
        kolomObatNama.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getNamaObat()));
        kolomObatSediaan.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getSediaan()));
        kolomObatStok.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStok()));
        kolomObatMinimum.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getAmbangMinimum()));
        daftarPoli.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(PoliEntity p, boolean kosong) {
                super.updateItem(p, kosong);
                setText(kosong || p == null ? null : p.getNamaPoli());
            }
        });
        daftarPoli.getSelectionModel().selectedItemProperty().addListener((o, lama, baru) ->
                kolomNamaPoli.setText(baru == null ? "" : baru.getNamaPoli()));
        tombolSimpanPoli.setOnAction(e -> kirimPoli());
        tombolPoliBaru.setOnAction(e -> {
            daftarPoli.getSelectionModel().clearSelection();
            kolomNamaPoli.requestFocus();
        });
        tombolTambahObat.setOnAction(e -> kirimObat());

        tabelAkun.getSelectionModel().selectedItemProperty().addListener((o, lama, baru) -> isiFormulirAkun(baru));
        tabelAmbang.getSelectionModel().selectedItemProperty().addListener((o, lama, baru) -> {
            ambangTerpilih = baru;
            kolomNilaiAmbang.setText(baru == null ? "" : String.valueOf(baru.getNilaiBatas()));
        });
        tabKonfigurasi.getSelectionModel().selectedItemProperty()
                .addListener((o, lama, baru) -> bukaTab(baru.getText()));

        tombolSimpanAkun.setOnAction(e -> kirimPerubahan());
        tombolSimpanAmbang.setOnAction(e -> kirimPerubahan());
        tombolAkunBaru.setOnAction(e -> isiFormulirAkun(null));
        tombolNonaktifkan.setOnAction(e -> kirimNonaktifkan(idAkunTerpilih));
        tombolPencadangan.setOnAction(e -> kirimPencadangan());
        akar.setOnKeyPressed(e -> {
            if (CTRL_S.match(e)) {
                kirimPerubahan();
                e.consume();
            } else if (CTRL_N.match(e) && TAB_AKUN.equals(tabAktif)) {
                isiFormulirAkun(null);
                e.consume();
            }
        });
        pesanNamaPengguna.managedProperty().bind(pesanNamaPengguna.visibleProperty());
        penandaBerjalan.setVisible(false);
        tampilkanData();
    }

    public void tampilkanData() {
        try {
            KonfigurasiManager.DataMaster data = konfigurasiManager.muatDataMaster();
            tabelAkun.setItems(FXCollections.observableArrayList(data.akun()));
            tabelAmbang.setItems(FXCollections.observableArrayList(data.ambang()));
            tabelObat.setItems(FXCollections.observableArrayList(data.obat()));
            daftarPoli.setItems(FXCollections.observableArrayList(data.poli()));
            tampilkanStatusPencadangan(konfigurasiManager.muatStatusPencadangan());
        } catch (IllegalStateException e) {
            tampilkanPesan(e.getMessage());
        }
        if (tabAktif == null) {
            bukaTab(TAB_AKUN);
        }
    }

    public void bukaTab(String tab) {
        tabAktif = tab;
        isianData = null;
        for (Tab t : tabKonfigurasi.getTabs()) {
            if (t.getText().equals(tab) && tabKonfigurasi.getSelectionModel().getSelectedItem() != t) {
                tabKonfigurasi.getSelectionModel().select(t);
            }
        }
        // Alternatif 1 UC12: status pencadangan selalu dimuat ulang tiap tab ini dibuka
        if (TAB_PEMELIHARAAN.equals(tab)) {
            try {
                tampilkanStatusPencadangan(konfigurasiManager.muatStatusPencadangan());
            } catch (IllegalStateException e) {
                tampilkanPesan(e.getMessage());
            }
        }
    }

    // Algo-C16-01
    public void kirimPerubahan() {
        tandaiNama(false, null);
        try {
            if (TAB_AKUN.equals(tabAktif)) {
                isianData = kolomNamaPengguna.getText() == null ? "" : kolomNamaPengguna.getText().strip();
                PenggunaEntity p = new PenggunaEntity(idAkunTerpilih, isianData, null, kolomPeran.getValue(), null);
                String sandi = kolomKataSandi.getText();
                kolomKataSandi.clear();
                konfigurasiManager.simpanAkun(p, sandi);
                idAkunTerpilih = p.getIdPengguna();
                tampilkanPesan("Akun disimpan");
            } else if (TAB_MASTER.equals(tabAktif)) {
                if (ambangTerpilih == null) {
                    throw new IllegalArgumentException("Pilih ambang risiko yang akan diubah");
                }
                isianData = kolomNilaiAmbang.getText();
                double nilai;
                try {
                    nilai = Double.parseDouble(isianData.strip().replace(',', '.'));
                } catch (NumberFormatException | NullPointerException e) {
                    throw new IllegalArgumentException("Nilai ambang harus berupa angka");
                }
                konfigurasiManager.simpanDataMaster(new AmbangRisikoEntity(ambangTerpilih.getIdAmbang(),
                        ambangTerpilih.getJenisRisiko(), nilai));
                tampilkanPesan("Ambang risiko disimpan");
            } else {
                return;
            }
            tampilkanData();
        } catch (IllegalArgumentException e) {
            if ("Nama pengguna sudah dipakai".equals(e.getMessage())) {
                tandaiNama(true, "Nama pengguna sudah terdaftar");
            }
            tampilkanPesan(e.getMessage());
        } catch (IllegalStateException e) {
            tampilkanPesan(e.getMessage());
        }
    }

    // Tanpa poli terpilih = poli baru
    private void kirimPoli() {
        PoliEntity terpilih = daftarPoli.getSelectionModel().getSelectedItem();
        try {
            konfigurasiManager.simpanPoli(terpilih == null ? 0 : terpilih.getIdPoli(), kolomNamaPoli.getText());
            tampilkanPesan(terpilih == null ? "Poli ditambahkan" : "Nama poli disimpan");
            kolomNamaPoli.clear();
            tampilkanData();
        } catch (IllegalArgumentException | IllegalStateException e) {
            tampilkanPesan(e.getMessage());
        }
    }

    private void kirimObat() {
        try {
            int minimum;
            try {
                String isi = kolomStokMinimum.getText() == null ? "" : kolomStokMinimum.getText().strip();
                minimum = isi.isEmpty() ? 0 : Integer.parseInt(isi);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Stok minimum harus bilangan bulat");
            }
            konfigurasiManager.tambahObat(new ObatEntity(0, kolomNamaObat.getText(), kolomSediaan.getText(), 0,
                    minimum));
            tampilkanPesan("Obat ditambahkan, stok diisi lewat Persediaan Obat");
            kolomNamaObat.clear();
            kolomSediaan.clear();
            kolomStokMinimum.clear();
            tampilkanData();
        } catch (IllegalArgumentException | IllegalStateException e) {
            tampilkanPesan(e.getMessage());
        }
    }

    // Alternatif 3 UC12, minta konfirmasi dulu
    public void kirimNonaktifkan(int idPengguna) {
        if (idPengguna == 0) {
            tampilkanPesan("Pilih akun pada tabel terlebih dahulu");
            return;
        }
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION,
                "Nonaktifkan akun " + kolomNamaPengguna.getText() + "? Sesi akun itu akan langsung ditutup.",
                ButtonType.OK, ButtonType.CANCEL);
        konfirmasi.setHeaderText(null);
        Optional<ButtonType> jawaban = konfirmasi.showAndWait();
        if (jawaban.isEmpty() || jawaban.get() != ButtonType.OK) {
            return;
        }
        try {
            konfigurasiManager.nonaktifkanAkun(idPengguna);
            tampilkanPesan("Akun dinonaktifkan");
            tampilkanData();
        } catch (IllegalArgumentException | IllegalStateException e) {
            tampilkanPesan(e.getMessage());
        }
    }

    // pg_dump bisa lama, jadi jalan di Task supaya layar tidak beku
    public void kirimPencadangan() {
        Task<PencadanganEntity> tugas = new Task<>() {
            @Override
            protected PencadanganEntity call() {
                return konfigurasiManager.jalankanPencadangan();
            }
        };
        tombolPencadangan.setDisable(true);
        penandaBerjalan.setVisible(true);
        tampilkanPesan("Pencadangan sedang berjalan...");
        tugas.setOnSucceeded(e -> {
            selesaiPencadangan();
            PencadanganEntity p = tugas.getValue();
            tampilkanStatusPencadangan(p);
            tampilkanPesan(p.berhasil() ? "Pencadangan berhasil" : "Pencadangan " + p.getStatus());
        });
        tugas.setOnFailed(e -> {
            selesaiPencadangan();
            tampilkanPesan(tugas.getException().getMessage());
        });
        Thread t = new Thread(tugas, "pencadangan-manual");
        t.setDaemon(true);
        t.start();
    }

    // Peringatan tetap muncul sampai pencadangan berikutnya berhasil (alternatif 4 UC12)
    public void tampilkanStatusPencadangan(PencadanganEntity p) {
        if (p == null) {
            labelWaktuCadangan.setText("-");
            labelUkuranCadangan.setText("-");
            labelStatusCadangan.setText("Belum pernah");
            labelLokasiCadangan.setText("-");
            peringatanCadangan.setPesan("Belum ada pencadangan basis data.");
            peringatanCadangan.setVisible(true);
            return;
        }
        labelWaktuCadangan.setText(WAKTU.format(p.getWaktu()));
        labelUkuranCadangan.setText(ukuranTerbaca(p.getUkuranBerkas()));
        labelStatusCadangan.setText(p.getStatus());
        labelLokasiCadangan.setText(p.getLokasiMedia());
        peringatanCadangan.setPesan("Pencadangan terakhir " + p.getStatus()
                + ". Periksa media cadangan lalu jalankan pencadangan lagi.");
        peringatanCadangan.setVisible(!p.berhasil());
    }

    public void tampilkanPesan(String pesan) {
        bilahPesan.setText(pesan);
    }

    static String ukuranTerbaca(long bita) {
        if (bita < 1024) {
            return bita + " B";
        }
        if (bita < 1024 * 1024) {
            return String.format("%.1f KB", bita / 1024.0);
        }
        return String.format("%.1f MB", bita / (1024.0 * 1024));
    }

    private void isiFormulirAkun(PenggunaEntity p) {
        tandaiNama(false, null);
        kolomKataSandi.clear();
        idAkunTerpilih = p == null ? 0 : p.getIdPengguna();
        kolomNamaPengguna.setText(p == null ? "" : p.getNamaPengguna());
        // Nama pengguna tidak ikut diubah saat menyunting (Q-C56-05)
        kolomNamaPengguna.setEditable(p == null);
        kolomPeran.setValue(p == null ? null : p.getPeran());
        tombolNonaktifkan.setDisable(p == null || !p.aktif());
        if (p == null) {
            tabelAkun.getSelectionModel().clearSelection();
            kolomNamaPengguna.requestFocus();
        }
    }

    private void tandaiNama(boolean bermasalah, String pesan) {
        kolomNamaPengguna.getStyleClass().remove("kolom-bermasalah");
        if (bermasalah) {
            kolomNamaPengguna.getStyleClass().add("kolom-bermasalah");
        }
        pesanNamaPengguna.setText(pesan);
        pesanNamaPengguna.setVisible(bermasalah);
    }

    private void selesaiPencadangan() {
        tombolPencadangan.setDisable(false);
        penandaBerjalan.setVisible(false);
    }
}
