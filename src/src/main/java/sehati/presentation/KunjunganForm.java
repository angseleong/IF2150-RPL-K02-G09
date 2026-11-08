package sehati.presentation;

import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import sehati.business.control.KunjunganManager;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PoliEntity;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C03 - KunjunganForm.
 *
 * Menampilkan pilihan poli dan tiket antrean, lalu meneruskan permintaan kunjungan ke KunjunganManager.
 */
public class KunjunganForm {

    private static final Logger LOG = Logger.getLogger(KunjunganForm.class.getName());
    private static final DateTimeFormatter FORMAT_TANGGAL =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.forLanguageTag("id"));

    private final KunjunganManager kunjunganManager = new KunjunganManager();

    private PasienEntity pasienTerpilih;
    private PoliEntity poliTerpilih;
    private List<PoliEntity> daftarPoli = List.of();

    @FXML private Label labelPasien;
    @FXML private ComboBox<PoliEntity> comboPoli;
    @FXML private Button tombolBukaKunjungan;
    @FXML private VBox panelTiket;
    @FXML private Label labelTiketPoli;
    @FXML private Label labelTiketNomor;
    @FXML private Label labelTiketTanggal;
    @FXML private Button tombolCetakTiket;
    @FXML private VBox panelAktif;
    @FXML private Label labelAktif;
    @FXML private Label labelPesan;

    @FXML
    private void initialize() {
        comboPoli.setConverter(new StringConverter<>() {
            @Override
            public String toString(PoliEntity p) {
                return p == null ? "" : p.getNamaPoli();
            }

            @Override
            public PoliEntity fromString(String s) {
                return null;
            }
        });
        comboPoli.getSelectionModel().selectedItemProperty().addListener((obs, lama, baru) -> {
            poliTerpilih = baru;
            tombolBukaKunjungan.setDisable(baru == null);
        });
        labelPesan.visibleProperty().bind(labelPesan.textProperty().isNotEmpty());
        labelPesan.managedProperty().bind(labelPesan.visibleProperty());
    }

    public void buka(PasienEntity pasien) {
        this.pasienTerpilih = pasien;
        this.poliTerpilih = null;
        labelPasien.setText(pasien.getNama() + "  ·  NIK " + pasien.getNik() + "  ·  " + pasien.getNomorRekamMedis());
        labelPesan.setText("");
        panelTiket.setVisible(false);
        panelTiket.setManaged(false);
        panelAktif.setVisible(false);
        panelAktif.setManaged(false);
        try {
            tampilkanPilihanPoli(kunjunganManager.daftarPoli());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Daftar poli gagal dimuat", e);
            comboPoli.setDisable(true);
            tampilkanPesan(e.getMessage());
        }
    }

    public void tampilkanPilihanPoli(List<PoliEntity> daftarPoli) {
        comboPoli.getItems().setAll(daftarPoli);
        this.daftarPoli = daftarPoli == null ? List.of() : List.copyOf(daftarPoli);
        comboPoli.getSelectionModel().clearSelection();
        tombolBukaKunjungan.setDisable(true);
    }

    @FXML
    public void kirimPermintaanKunjungan() {
        if (pasienTerpilih == null || poliTerpilih == null) {
            return;
        }
        // Cegah dua permintaan dari satu klik ganda (Algo-C03-01).
        tombolBukaKunjungan.setDisable(true);
        try {
            KunjunganEntity kunjungan = kunjunganManager.bukaKunjungan(
                    pasienTerpilih.getIdPasien(), poliTerpilih.getIdPoli());
            if ("menunggu skrining".equals(kunjungan.getStatus())) {
                tampilkanTiketAntrean(kunjungan);
            } else {
                tampilkanKunjunganAktif(kunjungan);
            }
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Pembukaan kunjungan gagal", e);
            tampilkanPesan(e.getMessage());
        } finally {
            tombolBukaKunjungan.setDisable(poliTerpilih == null);
        }
    }

    public void tampilkanTiketAntrean(KunjunganEntity kunjungan) {
        panelAktif.setVisible(false);
        panelAktif.setManaged(false);
        labelTiketPoli.setText(namaPoli(kunjungan));
        labelTiketNomor.setText(String.valueOf(kunjungan.getNomorAntrean()));
        labelTiketTanggal.setText(kunjungan.getTanggal().format(FORMAT_TANGGAL));
        panelTiket.setVisible(true);
        panelTiket.setManaged(true);
    }

    public void tampilkanKunjunganAktif(KunjunganEntity kunjungan) {
        panelTiket.setVisible(false);
        panelTiket.setManaged(false);
        labelAktif.setText("Nomor antrean " + kunjungan.getNomorAntrean() + " · status: " + kunjungan.getStatus()
                + " · " + kunjungan.getTanggal().format(FORMAT_TANGGAL));
        panelAktif.setVisible(true);
        panelAktif.setManaged(true);
    }

    @FXML
    private void cetakTiket() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            tampilkanPesan("Tidak ada pencetak yang tersedia");
            return;
        }
        if (job.printPage(panelTiket)) {
            job.endJob();
        } else {
            tampilkanPesan("Tiket gagal dicetak");
        }
    }

    @FXML
    private void kembali() {
        Navigasi.kembali();
    }

    private String namaPoli(KunjunganEntity kunjungan) {
        return daftarPoli.stream()
                .filter(p -> p.getIdPoli() == kunjungan.getIdPoli())
                .map(PoliEntity::getNamaPoli)
                .findFirst()
                .orElse("Poli " + kunjungan.getIdPoli());
    }

    private void tampilkanPesan(String pesan) {
        labelPesan.setText(pesan == null ? "" : pesan);
    }
}
