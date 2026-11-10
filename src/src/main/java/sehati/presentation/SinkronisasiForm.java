package sehati.presentation;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import sehati.business.control.SinkronisasiManager;
import sehati.business.entity.BundelFHIREntity;

/**
 * C14 - SinkronisasiForm.
 *
 * Menampilkan status setiap bundel dan indikator koneksi, lalu meneruskan perintah sinkronisasi atau "Ekspor
 * Bundel" ke SinkronisasiManager.
 */
public class SinkronisasiForm {

    private static final PseudoClass DARING = PseudoClass.getPseudoClass("daring");

    private final SinkronisasiManager sinkronisasiManager = new SinkronisasiManager();
    private List<BundelFHIREntity> daftarStatus;

    @FXML private VBox akar;
    @FXML private PanelPeringatan panelPenolakan;
    @FXML private Label bilahPesan;
    @FXML private Label labelKoneksi;
    @FXML private Label labelTertunda;
    @FXML private Button tombolSinkron;
    @FXML private Button tombolEkspor;
    @FXML private ProgressBar indikatorProses;
    @FXML private TableView<BundelFHIREntity> tabelStatus;
    @FXML private TableColumn<BundelFHIREntity, String> kolomId;
    @FXML private TableColumn<BundelFHIREntity, String> kolomStatus;
    @FXML private TableColumn<BundelFHIREntity, String> kolomAlasan;

    @FXML
    private void initialize() {
        kolomId.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(String.valueOf(data.getValue().getIdBundel())));
        kolomStatus.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(labelStatus(data.getValue())));
        kolomAlasan.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getAlasanGagal() == null ? "" : data.getValue().getAlasanGagal()));

        panelPenolakan.setVisible(false);
        bilahPesan.setText("");
        bilahPesan.visibleProperty().bind(bilahPesan.textProperty().isNotEmpty());
        bilahPesan.managedProperty().bind(bilahPesan.visibleProperty());
        tampilkan(tombolEkspor, false);
        tampilkan(indikatorProses, false);
        perbaruiKoneksi(false);
        tampilkanStatus();
    }

    /**
     * Memuat status bundel dan indikator koneksi dari manager.
     */
    public void tampilkanStatus() {
        jalankan(sinkronisasiManager::muatStatus, hasil -> {
            daftarStatus = hasil;
            tabelStatus.getItems().setAll(hasil);
            labelTertunda.setText("Tertunda: " + sinkronisasiManager.getJumlahTertunda());
            perbaruiKoneksi(sinkronisasiManager.isDaring());
            tampilkanTombolEkspor();
        }, this::tampilkanPenolakan, () -> { });
    }

    /**
     * Algo-C14-01: jalankan pengiriman di background supaya UI tidak membeku.
     */
    @FXML
    public void kirimPermintaanSinkron() {
        tombolSinkron.setDisable(true);
        tampilkan(indikatorProses, true);
        panelPenolakan.setVisible(false);

        jalankan(sinkronisasiManager::kirimSemua, this::tampilkanHasil, galat -> {
            if (galat instanceof IllegalStateException) {
                tampilkanPesan(galat.getMessage());
                tampilkanTombolEkspor();
            } else {
                tampilkanPenolakan(galat);
            }
        }, () -> {
            tombolSinkron.setDisable(false);
            tampilkan(indikatorProses, false);
        });
    }

    /**
     * Menampilkan hasil pengiriman per bundel.
     */
    public void tampilkanHasil(List<BundelFHIREntity> hasil) {
        daftarStatus = hasil;
        tabelStatus.getItems().setAll(hasil);
        labelTertunda.setText("Tertunda: " + sinkronisasiManager.getJumlahTertunda());
        if (hasil.isEmpty()) {
            tampilkanPesan("Tidak ada bundel tertunda.");
        } else {
            tampilkanPesan("Sinkronisasi selesai. " + hasil.size() + " bundel diproses.");
        }
        tampilkanTombolEkspor();
    }

    /**
     * Tombol ekspor hanya muncul saat luring.
     */
    public void tampilkanTombolEkspor() {
        tampilkan(tombolEkspor, !sinkronisasiManager.isDaring());
    }

    /**
     * Mengekspor bundel tertunda ke folder pilihan pengguna.
     */
    public void kirimPermintaanEkspor(String lokasi) {
        String tujuan = lokasi;
        if (tujuan == null || tujuan.isBlank()) {
            DirectoryChooser pemilih = new DirectoryChooser();
            pemilih.setTitle("Pilih folder ekspor bundel");
            Window jendela = akar == null || akar.getScene() == null
                    ? null
                    : akar.getScene().getWindow();
            File folder = pemilih.showDialog(jendela);
            if (folder == null) {
                return;
            }
            tujuan = folder.getAbsolutePath();
        }
        String folder = tujuan;
        jalankan(() -> sinkronisasiManager.eksporBundel(folder),
                berkas -> tampilkanPesan("Bundel diekspor ke " + berkas),
                this::tampilkanPenolakan,
                () -> { });
    }

    public void tampilkanPesan(String pesan) {
        bilahPesan.setText(pesan == null ? "" : pesan);
    }

    @FXML
    private void eksporBundel() {
        kirimPermintaanEkspor(null);
    }

    @FXML
    private void kembali() {
        Navigasi.kembali();
    }

    private void perbaruiKoneksi(boolean daring) {
        labelKoneksi.setText(daring ? "Daring" : "Luring");
        labelKoneksi.pseudoClassStateChanged(DARING, daring);
        Navigasi.aturDaring(daring);
    }

    private static String labelStatus(BundelFHIREntity bundel) {
        String status = bundel.getStatus();
        if ("terkirim".equals(status)) {
            return "Terkirim";
        }
        if ("gagal kirim".equals(status)) {
            return "Gagal Kirim";
        }
        return "Tertunda";
    }

    private void tampilkanPenolakan(Throwable galat) {
        if (galat instanceof IllegalArgumentException || galat instanceof IllegalStateException) {
            panelPenolakan.setPesan(galat.getMessage() == null ? "Terjadi kesalahan" : galat.getMessage());
            panelPenolakan.setVisible(true);
        } else {
            Alert peringatan = new Alert(Alert.AlertType.ERROR);
            peringatan.setTitle("Sinkronisasi SATUSEHAT");
            peringatan.setHeaderText("Terjadi kesalahan");
            peringatan.setContentText(galat.getMessage());
            peringatan.showAndWait();
        }
    }

    private <T> void jalankan(Callable<T> kerja, Consumer<T> sukses, Consumer<Throwable> gagal,
            Runnable selesai) {
        Task<T> tugas = new Task<>() {
            @Override
            protected T call() throws Exception {
                return kerja.call();
            }
        };
        tugas.setOnSucceeded(e -> {
            try {
                sukses.accept(tugas.getValue());
            } finally {
                selesai.run();
            }
        });
        tugas.setOnFailed(e -> {
            try {
                gagal.accept(tugas.getException());
            } finally {
                selesai.run();
            }
        });
        Thread utas = new Thread(tugas, "sehati-sinkronisasi");
        utas.setDaemon(true);
        utas.start();
    }

    private static void tampilkan(Node node, boolean tampil) {
        node.setVisible(tampil);
        node.setManaged(tampil);
    }
}
