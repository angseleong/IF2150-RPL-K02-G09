package sehati.presentation;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;
import sehati.business.control.SkriningManager;
import sehati.business.entity.KunjunganEntity;

/**
 * C04 - Menampilkan antrean skrining terurut nomor antrean dan meneruskan perintah "Panggil Berikutnya" (F2) serta
 * "Lewati" (F3) ke SkriningManager (UC04, I-04).
 */
public class AntreanSkriningForm {

    private static final String GAYA_SOROT = "-fx-background-color: #FEF9C3;";

    private final SkriningManager skriningManager = new SkriningManager();
    private final Timeline penyegar = new Timeline(new KeyFrame(Duration.seconds(10), e -> tampilkanAntrean()));
    private final Timeline detikTerakhir = new Timeline(new KeyFrame(Duration.seconds(1), e -> perbaruiKeterangan()));
    private final Map<Integer, String> labelPasien = new HashMap<>();

    private List<KunjunganEntity> daftarTampil = new ArrayList<>();
    private LocalTime terakhirDimuat;

    @FXML private Parent akar;
    @FXML private Label labelRingkasan;
    @FXML private Label labelPembaruan;
    @FXML private TableView<KunjunganEntity> tabelAntrean;
    @FXML private TableColumn<KunjunganEntity, Integer> kolomNomor;
    @FXML private TableColumn<KunjunganEntity, String> kolomPasien;
    @FXML private TableColumn<KunjunganEntity, String> kolomStatus;
    @FXML private Button tombolPanggil;
    @FXML private Button tombolLewati;

    @FXML
    private void initialize() {
        kolomNomor.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(
                c.getValue().getNomorAntrean()));
        kolomPasien.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                labelPasien.getOrDefault(c.getValue().getIdKunjungan(), "")));
        kolomStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getStatus()));
        kolomPasien.setCellFactory(k -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean kosong) {
                super.updateItem(item, kosong);
                setText(kosong ? null : item);
            }
        });
        tabelAntrean.setRowFactory(t -> {
            TableRow<KunjunganEntity> baris = new TableRow<>() {
                @Override
                protected void updateItem(KunjunganEntity k, boolean kosong) {
                    super.updateItem(k, kosong);
                    boolean dilayani = !kosong && k != null && SkriningManager.STATUS_DISKRINING.equals(k.getStatus());
                    setStyle(dilayani ? GAYA_SOROT : "");
                }
            };
            baris.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !baris.isEmpty()) {
                    bukaFormulir(baris.getItem());
                }
            });
            return baris;
        });
        tabelAntrean.setPlaceholder(new Label("Tidak ada pasien yang menunggu skrining"));
        penyegar.setCycleCount(Animation.INDEFINITE);
        detikTerakhir.setCycleCount(Animation.INDEFINITE);
        tombolPanggil.setDisable(true);
        tombolLewati.setDisable(true);

        // Penyegar hanya berjalan saat layar tampil, dan antrean dimuat ulang tiap layar kembali tampil.
        akar.sceneProperty().addListener((o, lama, baru) -> {
            if (baru == null) {
                penyegar.stop();
                detikTerakhir.stop();
            } else {
                tampilkanAntrean();
                penyegar.play();
                detikTerakhir.play();
            }
        });
        akar.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F2) {
                kirimPanggilan();
                e.consume();
            } else if (e.getCode() == KeyCode.F3) {
                kirimLewati();
                e.consume();
            }
        });
        tabelAntrean.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ENTER && tabelAntrean.getSelectionModel().getSelectedItem() != null) {
                bukaFormulir(tabelAntrean.getSelectionModel().getSelectedItem());
                e.consume();
            }
        });
    }

    public void tampilkanAntrean() {
        LatarBelakang.jalankan(() -> {
            List<KunjunganEntity> antrean = skriningManager.muatAntrean();
            return new Object[] {antrean, skriningManager.labelPasien(antrean)};
        }, hasil -> {
            @SuppressWarnings("unchecked")
            Map<Integer, String> label = (Map<Integer, String>) hasil[1];
            labelPasien.putAll(label);
            @SuppressWarnings("unchecked")
            List<KunjunganEntity> antrean = (List<KunjunganEntity>) hasil[0];
            terakhirDimuat = LocalTime.now();
            perbaruiTampilan(antrean);
        }, this::tampilkanGalat);
    }

    public void kirimPanggilan() {
        if (tombolPanggil.isDisabled()) {
            return;
        }
        aturTombol(true);
        LatarBelakang.jalankan(skriningManager::panggilPasien, k -> {
            if (k == null) {
                tampilkanAntrean();
                return;
            }
            bukaFormulir(k);
        }, e -> {
            aturTombol(false);
            tampilkanGalat(e);
        });
    }

    public void kirimLewati() {
        KunjunganEntity dilayani = pasienDilayani();
        if (dilayani == null || tombolLewati.isDisabled()) {
            return;
        }
        aturTombol(true);
        LatarBelakang.jalankan(() -> skriningManager.lewatiPasien(dilayani.getIdKunjungan()),
                berikutnya -> tampilkanAntrean(), e -> {
                    aturTombol(false);
                    tampilkanGalat(e);
                });
    }

    public void perbaruiTampilan(List<KunjunganEntity> daftar) {
        this.daftarTampil = new ArrayList<>(daftar);
        tabelAntrean.getItems().setAll(daftarTampil);
        tabelAntrean.refresh();
        long menunggu = daftarTampil.stream()
                .filter(k -> SkriningManager.STATUS_MENUNGGU.equals(k.getStatus())).count();
        labelRingkasan.setText("Menunggu skrining: " + menunggu + " pasien");
        // Panggil dinonaktifkan bila antrean kosong; Lewati butuh pasien yang sedang dilayani.
        tombolPanggil.setDisable(daftarTampil.isEmpty());
        tombolLewati.setDisable(pasienDilayani() == null);
        perbaruiKeterangan();
    }

    private KunjunganEntity pasienDilayani() {
        KunjunganEntity dipilih = tabelAntrean.getSelectionModel().getSelectedItem();
        if (dipilih != null) {
            return dipilih;
        }
        return daftarTampil.stream().filter(k -> SkriningManager.STATUS_DISKRINING.equals(k.getStatus()))
                .findFirst().orElse(null);
    }

    private void bukaFormulir(KunjunganEntity kunjungan) {
        SkriningForm form = Navigasi.buka("SkriningForm");
        form.tampilkanFormulir(kunjungan);
    }

    private void aturTombol(boolean sibuk) {
        tombolPanggil.setDisable(sibuk);
        tombolLewati.setDisable(sibuk);
    }

    private void perbaruiKeterangan() {
        if (terakhirDimuat == null) {
            labelPembaruan.setText("Memuat antrean...");
            return;
        }
        long detik = ChronoUnit.SECONDS.between(terakhirDimuat, LocalTime.now());
        labelPembaruan.setText("Diperbarui otomatis tiap 10 detik, terakhir " + detik + " detik lalu");
    }

    private void tampilkanGalat(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setHeaderText("Antrean skrining tidak dapat diproses");
        alert.show();
    }
}
