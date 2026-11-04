package sehati.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import sehati.business.control.PantauManager;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.TindakLanjutEntity;

/**
 * C12 - Daftar pantau pasien berisiko dan terjadwal kontrol, penyaringnya, serta formulir tindak lanjut (UC09, I-12).
 */
public class DaftarPantauForm {

    private static final String SEMUA = "Semua";
    private static final KeyCodeCombination CTRL_S = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("id", "ID"));

    private final PantauManager pantauManager = new PantauManager();

    private String penyaring = "";
    private List<DaftarPantauEntity> daftarTampil = new ArrayList<>();
    private DaftarPantauEntity entriTerpilih;

    @FXML private Parent akar;
    @FXML private ComboBox<String> pilihJenisRisiko;
    @FXML private DatePicker pilihMulai;
    @FXML private DatePicker pilihAkhir;
    @FXML private Label labelKeterangan;
    @FXML private TableView<DaftarPantauEntity> tabelPantau;
    @FXML private TableColumn<DaftarPantauEntity, String> kolomPasien;
    @FXML private TableColumn<DaftarPantauEntity, String> kolomTelepon;
    @FXML private TableColumn<DaftarPantauEntity, String> kolomJenis;
    @FXML private TableColumn<DaftarPantauEntity, String> kolomTanggal;
    @FXML private TableColumn<DaftarPantauEntity, String> kolomStatus;
    @FXML private TableColumn<DaftarPantauEntity, Void> kolomAksi;
    @FXML private VBox panelTindakLanjut;
    @FXML private Label labelEntri;
    @FXML private ComboBox<String> pilihJenisKontak;
    @FXML private ComboBox<String> pilihHasilUpaya;
    @FXML private ComboBox<String> pilihStatusKedatangan;
    @FXML private Button tombolSimpan;

    @FXML
    private void initialize() {
        pilihJenisRisiko.getItems().setAll(SEMUA, "hipertensi", "obesitas", "indikasi diabetes", "kunjungan ulang");
        pilihJenisRisiko.getSelectionModel().selectFirst();
        pilihJenisKontak.getItems().setAll("telepon", "pesan singkat", "kunjungan rumah");
        pilihHasilUpaya.getItems().setAll(TindakLanjutEntity.HASIL_TERHUBUNG,
                TindakLanjutEntity.HASIL_TIDAK_TERHUBUNG);
        pilihStatusKedatangan.getItems().setAll("akan datang", "sudah datang", "tidak dapat datang",
                "belum diketahui");

        kolomPasien.setCellValueFactory(c -> new SimpleStringProperty(nilai(c.getValue().getNamaPasien())));
        kolomTelepon.setCellValueFactory(c -> new SimpleStringProperty(nilai(c.getValue().getNomorTelepon())));
        kolomJenis.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJenisRisiko()));
        kolomTanggal.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTanggalPemicu() == null ? "" : TANGGAL.format(c.getValue().getTanggalPemicu())));
        kolomStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatusTindakLanjut()));
        kolomJenis.setCellFactory(k -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean kosong) {
                super.updateItem(item, kosong);
                boolean berisiko = !kosong && item != null && !"kunjungan ulang".equals(item);
                // Merah disertai ikon, tidak hanya warna (KNF08).
                setText(kosong ? null : (berisiko ? "⚠ " : "") + item);
                setStyle(berisiko ? "-fx-text-fill: #DC2626; -fx-font-weight: bold;" : "");
            }
        });
        kolomAksi.setCellFactory(k -> new TableCell<>() {
            private final Button tombol = new Button("Tindak Lanjut");

            {
                tombol.getStyleClass().add("tombol-kedua");
                tombol.setOnAction(e -> bukaFormulirTindakLanjut(getTableRow().getItem().getIdEntri()));
            }

            @Override
            protected void updateItem(Void item, boolean kosong) {
                super.updateItem(item, kosong);
                setGraphic(kosong ? null : tombol);
            }
        });
        tabelPantau.setPlaceholder(new Label("Tidak ada entri daftar pantau"));
        tutupFormulir();

        akar.sceneProperty().addListener((o, lama, baru) -> {
            if (baru != null) {
                tampilkanDaftar();
            }
        });
        akar.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (CTRL_S.match(e) && panelTindakLanjut.isVisible()) {
                kirimTindakLanjut();
                e.consume();
            } else if (e.getCode() == KeyCode.ESCAPE && panelTindakLanjut.isVisible()) {
                // Esc menutup formulir lebih dulu, bukan meninggalkan layar.
                tutupFormulir();
                e.consume();
            }
        });
    }

    public void tampilkanDaftar() {
        kirimPenyaring(null, null, null);
    }

    @FXML
    private void terapkanPenyaring() {
        kirimPenyaring(pilihJenisRisiko.getValue(), pilihMulai.getValue(), pilihAkhir.getValue());
    }

    public void kirimPenyaring(String jenisRisiko, LocalDate mulai, LocalDate akhir) {
        if (mulai != null && akhir != null && mulai.isAfter(akhir)) {
            tampilkanKeterangan("Tanggal mulai tidak boleh setelah tanggal akhir");
            return;
        }
        String jenis = jenisRisiko == null || SEMUA.equals(jenisRisiko) ? null : jenisRisiko;
        LatarBelakang.jalankan(() -> pantauManager.saringDaftar(jenis, mulai, akhir), hasil -> {
            daftarTampil = hasil;
            tabelPantau.getItems().setAll(daftarTampil);
            if (daftarTampil.isEmpty()) {
                tampilkanKeterangan("Tidak ada pasien yang cocok dengan kriteria saringan");
            } else {
                tampilkanKeterangan("Menampilkan " + daftarTampil.size() + " entri aktif");
            }
            penyaring = ringkasan(jenis, mulai, akhir);
        }, this::tampilkanGalat);
    }

    public void bukaFormulirTindakLanjut(int idEntri) {
        entriTerpilih = daftarTampil.stream().filter(e -> e.getIdEntri() == idEntri).findFirst().orElse(null);
        if (entriTerpilih == null) {
            return;
        }
        labelEntri.setText("Tindak lanjut: " + nilai(entriTerpilih.getNamaPasien()) + " (" +
                entriTerpilih.getJenisRisiko() + ")");
        pilihJenisKontak.getSelectionModel().clearSelection();
        pilihHasilUpaya.getSelectionModel().clearSelection();
        pilihStatusKedatangan.getSelectionModel().clearSelection();
        tombolSimpan.setDisable(false);
        panelTindakLanjut.setVisible(true);
        panelTindakLanjut.setManaged(true);
        pilihJenisKontak.requestFocus();
    }

    public void kirimTindakLanjut() {
        if (entriTerpilih == null || tombolSimpan.isDisabled()) {
            return;
        }
        if (pilihJenisKontak.getValue() == null || pilihHasilUpaya.getValue() == null
                || pilihStatusKedatangan.getValue() == null) {
            tampilkanKeterangan("Pilih jenis kontak, hasil upaya, dan status kedatangan");
            return;
        }
        TindakLanjutEntity t = new TindakLanjutEntity(0, pilihJenisKontak.getValue(), pilihHasilUpaya.getValue(),
                pilihStatusKedatangan.getValue());
        int idEntri = entriTerpilih.getIdEntri();
        tombolSimpan.setDisable(true);
        LatarBelakang.jalankan(() -> {
            pantauManager.catatTindakLanjut(idEntri, t);
            return null;
        }, hasil -> {
            tutupFormulir();
            terapkanPenyaring();
        }, e -> {
            tombolSimpan.setDisable(false);
            tampilkanGalat(e);
        });
    }

    public void tampilkanKeterangan(String pesan) {
        labelKeterangan.setText(pesan);
    }

    @FXML
    private void tutupFormulir() {
        entriTerpilih = null;
        panelTindakLanjut.setVisible(false);
        panelTindakLanjut.setManaged(false);
    }

    private String ringkasan(String jenis, LocalDate mulai, LocalDate akhir) {
        List<String> bagian = new ArrayList<>();
        bagian.add(jenis == null ? SEMUA : jenis);
        if (mulai != null || akhir != null) {
            bagian.add((mulai == null ? "awal" : TANGGAL.format(mulai)) + " sampai "
                    + (akhir == null ? "akhir" : TANGGAL.format(akhir)));
        }
        return String.join(", ", bagian);
    }

    private static String nilai(String teks) {
        return teks == null ? "" : teks;
    }

    private void tampilkanGalat(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setHeaderText("Daftar pantau tidak dapat diproses");
        alert.show();
    }
}
