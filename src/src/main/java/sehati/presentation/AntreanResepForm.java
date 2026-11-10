package sehati.presentation;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sehati.business.control.ApotekManager;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.ResepEntity;

/**
 * C09 - AntreanResepForm.
 *
 * Menampilkan antrean resep apotek terurut waktu masuk dan meneruskan pilihan resep ke ApotekManager (I-09, UC07).
 */
public class AntreanResepForm {

    private static final DateTimeFormatter JAM = DateTimeFormatter.ofPattern("HH:mm");
    private static final int DETIK_PENYEGARAN = 10;

    private final ApotekManager apotekManager = new ApotekManager();

    private List<ResepEntity> daftarTampil = List.of();
    private final Map<Integer, PasienEntity> pasienPerResep = new HashMap<>();
    private final Map<Integer, ObatEntity> obatPerId = new HashMap<>();
    // Satu detak per detik: memperbarui label "terakhir n detik lalu" dan memuat ulang antrean tiap 10 detik.
    private final Timeline penyegar = new Timeline(new KeyFrame(Duration.seconds(1), e -> detak()));
    private int detikSejakMuat;
    private boolean sedangMemuat;

    private record Antrean(List<ResepEntity> resep, Map<Integer, PasienEntity> pasien, List<ObatEntity> obat) {
    }

    private record Rincian(ResepEntity resep, List<BetsObatEntity> bets) {
    }

    @FXML private VBox akar;
    @FXML private Label labelJumlah;
    @FXML private TableView<ResepEntity> tabelAntrean;
    @FXML private TableColumn<ResepEntity, String> kolomWaktu;
    @FXML private TableColumn<ResepEntity, String> kolomPasien;
    @FXML private TableColumn<ResepEntity, String> kolomObat;
    @FXML private TableColumn<ResepEntity, String> kolomStatus;
    @FXML private TableColumn<ResepEntity, ResepEntity> kolomAksi;
    @FXML private Label labelPenyegar;
    @FXML private Label bilahPesan;
    @FXML private PanelPeringatan panelPenolakan;

    @FXML
    private void initialize() {
        kolomWaktu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getWaktuMasuk().format(JAM)));
        kolomPasien.setCellValueFactory(c -> new SimpleStringProperty(namaPasien(c.getValue())));
        kolomObat.setCellValueFactory(c -> new SimpleStringProperty(ringkasItem(c.getValue())));
        // Satu baris per obat, jadi tinggi sel mengikuti jumlah item agar baris berikutnya tidak terpotong.
        kolomObat.setCellFactory(k -> new TableCell<>() {
            private final Text isi = new Text();

            @Override
            protected void updateItem(String teks, boolean kosong) {
                super.updateItem(teks, kosong);
                isi.setText(kosong ? null : teks);
                setGraphic(kosong ? null : isi);
            }
        });
        kolomStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        kolomAksi.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        kolomAksi.setCellFactory(k -> new TableCell<>() {
            private final Button layani = new Button("Layani");

            {
                layani.getStyleClass().add("tombol-utama");
            }

            @Override
            protected void updateItem(ResepEntity resep, boolean kosong) {
                super.updateItem(resep, kosong);
                if (kosong || resep == null) {
                    setGraphic(null);
                } else {
                    layani.setOnAction(e -> pilihResep(resep.getIdResep()));
                    setGraphic(layani);
                }
            }
        });
        tabelAntrean.setRowFactory(t -> {
            TableRow<ResepEntity> baris = new TableRow<>();
            baris.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !baris.isEmpty()) {
                    pilihResep(baris.getItem().getIdResep());
                }
            });
            return baris;
        });
        tabelAntrean.setOnKeyPressed(e -> {
            ResepEntity terpilih = tabelAntrean.getSelectionModel().getSelectedItem();
            if (e.getCode() == KeyCode.ENTER && terpilih != null) {
                pilihResep(terpilih.getIdResep());
                e.consume();
            }
        });
        tabelAntrean.setPlaceholder(new Label("Belum ada resep menunggu"));
        bilahPesan.setText("");
        // Bilah pesan hanya tampil bila ada pesan, agar tidak tersisa kotak kosong.
        bilahPesan.visibleProperty().bind(bilahPesan.textProperty().isNotEmpty());
        bilahPesan.managedProperty().bind(bilahPesan.visibleProperty());
        panelPenolakan.setVisible(false);
        penyegar.setCycleCount(Animation.INDEFINITE);
        // Penyegar hanya berjalan selama layar tampil. Saat kembali dari I-10, antrean langsung dimuat ulang.
        akar.sceneProperty().addListener((o, lama, baru) -> {
            if (baru == null) {
                penyegar.stop();
            } else {
                tampilkanAntrean();
                penyegar.play();
            }
        });
        tampilkanAntrean();
        penyegar.play();
    }

    /**
     * Memuat antrean resep menunggu beserta pasien dan nama obatnya, lalu menampilkannya terurut waktu masuk (KF13).
     */
    public void tampilkanAntrean() {
        if (sedangMemuat) {
            return;
        }
        sedangMemuat = true;
        LatarBelakang.jalankan(() -> {
            List<ResepEntity> resep = apotekManager.muatAntreanResep();
            List<Integer> daftarId = resep.stream().map(ResepEntity::getIdResep).toList();
            return new Antrean(resep, apotekManager.muatPasienResep(daftarId), apotekManager.muatDataObat());
        }, antrean -> {
            sedangMemuat = false;
            detikSejakMuat = 0;
            pasienPerResep.clear();
            pasienPerResep.putAll(antrean.pasien());
            obatPerId.clear();
            antrean.obat().forEach(o -> obatPerId.put(o.getIdObat(), o));
            daftarTampil = antrean.resep();
            tabelAntrean.getItems().setAll(daftarTampil);
            labelJumlah.setText("Resep menunggu: " + daftarTampil.size());
            perbaruiLabelPenyegar();
        }, e -> {
            sedangMemuat = false;
            tolak(e.getMessage());
        });
    }

    /**
     * Mengambil rincian resep dan bets obatnya, lalu membuka PenyerahanObatForm (UC07).
     */
    public void pilihResep(int idResep) {
        LatarBelakang.jalankan(() -> {
            ResepEntity resep = apotekManager.layaniResep(idResep);
            return new Rincian(resep, apotekManager.muatBetsResep(resep));
        }, rincian -> {
            bilahPesan.setText("");
            panelPenolakan.setVisible(false);
            PenyerahanObatForm form = Navigasi.buka("PenyerahanObatForm");
            form.aturKonteks(pasienPerResep.get(idResep), Map.copyOf(obatPerId), this::tampilkanBerhasil);
            form.tampilkanRincian(rincian.resep(), rincian.bets());
        }, e -> {
            // Resep yang baru saja dilayani petugas lain ditolak layaniResep(), jadi antrean ikut diperbarui.
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
                tolak(e.getMessage());
                tampilkanAntrean();
            } else {
                tampilkanGalat(e);
            }
        });
    }

    private void tampilkanBerhasil(String pesan) {
        panelPenolakan.setVisible(false);
        bilahPesan.setText(pesan);
    }

    // Penolakan memakai panel merah berikon agar tidak tertukar dengan pesan berhasil (KNF08).
    private void tolak(String pesan) {
        bilahPesan.setText("");
        panelPenolakan.setPesan(pesan);
        panelPenolakan.setVisible(true);
    }

    private void detak() {
        detikSejakMuat++;
        if (detikSejakMuat >= DETIK_PENYEGARAN) {
            tampilkanAntrean();
        }
        perbaruiLabelPenyegar();
    }

    private void perbaruiLabelPenyegar() {
        labelPenyegar.setText("Diperbarui otomatis tiap " + DETIK_PENYEGARAN + " detik, terakhir "
                + Math.min(detikSejakMuat, DETIK_PENYEGARAN) + " detik lalu");
    }

    private String namaPasien(ResepEntity resep) {
        PasienEntity pasien = pasienPerResep.get(resep.getIdResep());
        return pasien == null ? "Kunjungan " + resep.getIdKunjungan() : pasien.getNama();
    }

    private String ringkasItem(ResepEntity resep) {
        return resep.getDaftarItem().stream()
                .map(i -> namaObat(i.getIdObat()) + " " + i.getDosis() + " x" + i.getJumlah())
                .collect(Collectors.joining("\n"));
    }

    private String namaObat(int idObat) {
        ObatEntity obat = obatPerId.get(idObat);
        return obat == null ? "Obat " + idObat : obat.getNamaObat();
    }

    private void tampilkanGalat(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setHeaderText("Antrean resep tidak dapat diproses");
        alert.show();
    }
}
