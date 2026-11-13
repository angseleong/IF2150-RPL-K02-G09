package sehati.presentation;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;
import sehati.business.control.KunjunganManager;
import sehati.business.control.LaporanManager;
import sehati.business.entity.LaporanEntity;
import sehati.business.entity.PoliEntity;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * C13 - LaporanForm.
 *
 * Menampilkan pemilihan rentang tanggal dan pratinjau rekapitulasi, lalu meneruskan permintaan ekspor ke
 * LaporanManager.
 */
public class LaporanForm {

    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final LaporanManager laporanManager = new LaporanManager();
    // Pilihan pertama pada penyaring poli, id 0 berarti semua poli (UC10 langkah 1).
    private static final PoliEntity SEMUA_POLI = new PoliEntity(0, "Semua poli", 0, null);

    private LocalDate tanggalMulai;
    private LocalDate tanggalAkhir;
    private LaporanEntity laporanTerakhir;

    @FXML
    private VBox akar;
    @FXML
    private DatePicker tanggalMulaiPicker;
    @FXML
    private DatePicker tanggalAkhirPicker;
    @FXML
    private ComboBox<PoliEntity> pilihPoli;
    @FXML
    private PanelPeringatan panelPenolakan;
    @FXML
    private Label bilahPesan;
    @FXML
    private Label labelTotal;
    @FXML
    private Label pesanKosong;
    @FXML
    private VBox daftarPoli;
    @FXML
    private VBox daftarStatus;
    @FXML
    private VBox daftarDiagnosis;
    @FXML
    private Button tombolPdf;
    @FXML
    private Button tombolCsv;

    @FXML
    private void initialize() {
        StringConverter<LocalDate> konverter = new StringConverter<>() {
            @Override
            public String toString(LocalDate tanggal) {
                return tanggal == null ? "" : TANGGAL.format(tanggal);
            }

            @Override
            public LocalDate fromString(String teks) {
                if (teks == null || teks.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(teks.trim(), TANGGAL);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        };
        tanggalMulaiPicker.setConverter(konverter);
        tanggalAkhirPicker.setConverter(konverter);
        pilihPoli.setConverter(new StringConverter<>() {
            @Override
            public String toString(PoliEntity poli) {
                return poli == null ? "" : poli.getNamaPoli();
            }

            @Override
            public PoliEntity fromString(String teks) {
                return null;
            }
        });
        pilihPoli.getItems().setAll(SEMUA_POLI);
        pilihPoli.getSelectionModel().selectFirst();
        jalankan(() -> new KunjunganManager().daftarPoli(), poli -> pilihPoli.getItems().addAll(poli),
                this::tampilkanPenolakan);

        panelPenolakan.setVisible(false);
        bilahPesan.setText("");
        bilahPesan.visibleProperty().bind(bilahPesan.textProperty().isNotEmpty());
        bilahPesan.managedProperty().bind(bilahPesan.visibleProperty());
        pesanKosong.visibleProperty().bind(pesanKosong.textProperty().isNotEmpty());
        pesanKosong.managedProperty().bind(pesanKosong.visibleProperty());

        tampilkan();
    }

    /**
     * Membuka formulir dengan rentang awal bulan berjalan dan pratinjau kosong.
     */
    public void tampilkan() {
        LocalDate hariIni = LocalDate.now();
        tanggalMulai = hariIni.withDayOfMonth(1);
        tanggalAkhir = hariIni;
        tanggalMulaiPicker.setValue(tanggalMulai);
        tanggalAkhirPicker.setValue(tanggalAkhir);
        bersihkanPratinjau();
        tampilkanPesan("");
        panelPenolakan.setVisible(false);
    }

    @FXML
    private void kirimRentang() {
        LocalDate mulai = bacaTanggal(tanggalMulaiPicker);
        LocalDate akhir = bacaTanggal(tanggalAkhirPicker);
        if (mulai == null || akhir == null) {
            tampilkanPesan("Isi tanggal mulai dan tanggal akhir.");
            return;
        }
        tanggalMulai = mulai;
        tanggalAkhir = akhir;
        tampilkanPesan("");
        panelPenolakan.setVisible(false);
        PoliEntity poli = pilihPoli.getValue();
        Integer idPoli = poli == null || poli.getIdPoli() == 0 ? null : poli.getIdPoli();
        jalankan(() -> laporanManager.hitungRekapitulasi(mulai, akhir, idPoli),
                this::tampilkanRekapitulasi,
                this::tampilkanPenolakan);
    }

    /**
     * Menampilkan total, rincian per poli, per status, dan diagnosis terbanyak. Laporan kosong mengunci tombol
     * ekspor.
     */
    public void tampilkanRekapitulasi(LaporanEntity laporan) {
        laporanTerakhir = laporan;
        daftarPoli.getChildren().clear();
        daftarStatus.getChildren().clear();
        daftarDiagnosis.getChildren().clear();

        if (laporan.kosong()) {
            labelTotal.setText("Total: 0 kunjungan");
            pesanKosong.setText("Tidak ada data pelayanan pada rentang tanggal terpilih");
            kunciTombolEkspor();
            return;
        }

        pesanKosong.setText("");
        String ringkasan = laporan.getRingkasan() == null ? "" : laporan.getRingkasan();
        for (String baris : ringkasan.split("\\R")) {
            String[] kolom = baris.split(";", -1);
            if (kolom.length < 3) {
                continue;
            }
            switch (kolom[0]) {
                case "total" -> labelTotal.setText("Total: " + kolom[2] + " kunjungan");
                case "poli" -> daftarPoli.getChildren().add(barisLaporan(kolom[1], kolom[2]));
                case "status" -> daftarStatus.getChildren().add(barisLaporan(kolom[1], kolom[2]));
                case "diagnosis" -> daftarDiagnosis.getChildren().add(barisLaporan(kolom[1], kolom[2]));
                default -> {
                }
            }
        }
        tombolPdf.setDisable(false);
        tombolCsv.setDisable(false);
    }

    /**
     * Algo-C13-01: meminta folder bila belum diberikan, lalu mengekspor laporan terakhir di latar belakang.
     */
    public void kirimPermintaanEkspor(String format, String direktori) {
        if (laporanTerakhir == null) {
            tampilkanPesan("Hitung rekapitulasi terlebih dahulu.");
            return;
        }
        String folder = direktori;
        if (folder == null || folder.isBlank()) {
            DirectoryChooser pemilih = new DirectoryChooser();
            pemilih.setTitle("Pilih folder penyimpanan laporan");
            Window pemilik = akar.getScene() == null ? null : akar.getScene().getWindow();
            File dipilih = pemilih.showDialog(pemilik);
            if (dipilih == null) {
                return;
            }
            folder = dipilih.getAbsolutePath();
        }
        String tujuan = folder;
        tampilkanPesan("");
        jalankan(() -> laporanManager.ekspor(laporanTerakhir, format, tujuan),
                lokasi -> tampilkanPesan("Berkas berhasil dibuat di " + lokasi),
                e -> tampilkanPesan(pesanKesalahan(e)));
    }

    public void kunciTombolEkspor() {
        tombolPdf.setDisable(true);
        tombolCsv.setDisable(true);
    }

    public void tampilkanPesan(String pesan) {
        bilahPesan.setText(pesan == null ? "" : pesan);
    }

    @FXML
    private void eksporPdf() {
        kirimPermintaanEkspor("PDF", null);
    }

    @FXML
    private void eksporCsv() {
        kirimPermintaanEkspor("CSV", null);
    }

    @FXML
    private void kembali() {
        Navigasi.kembali();
    }

    private void bersihkanPratinjau() {
        laporanTerakhir = null;
        labelTotal.setText("");
        daftarPoli.getChildren().clear();
        daftarStatus.getChildren().clear();
        daftarDiagnosis.getChildren().clear();
        pesanKosong.setText("");
        kunciTombolEkspor();
    }

    private static LocalDate bacaTanggal(DatePicker pemilih) {
        String teks = pemilih.getEditor().getText();
        if (teks != null && !teks.isBlank()) {
            return pemilih.getConverter().fromString(teks);
        }
        return pemilih.getValue();
    }

    private static Label barisLaporan(String label, String jumlah) {
        Label baris = new Label(label + " - " + jumlah);
        baris.setMaxWidth(Double.MAX_VALUE);
        return baris;
    }

    private void tampilkanPenolakan(Throwable e) {
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            panelPenolakan.setPesan(e.getMessage());
            panelPenolakan.setVisible(true);
            return;
        }
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.setTitle("Laporan Periodik");
        dialog.setHeaderText("Terjadi kesalahan");
        dialog.setContentText(pesanKesalahan(e));
        dialog.showAndWait();
    }

    private static String pesanKesalahan(Throwable e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            return e.getMessage();
        }
        Throwable akar = e;
        while (akar.getCause() != null) {
            akar = akar.getCause();
        }
        return akar.getMessage() == null ? "Terjadi kesalahan." : akar.getMessage();
    }

    private <T> void jalankan(Callable<T> kerja, Consumer<T> sukses, Consumer<Throwable> gagal) {
        Task<T> tugas = new Task<>() {
            @Override
            protected T call() throws Exception {
                return kerja.call();
            }
        };
        tugas.setOnSucceeded(e -> sukses.accept(tugas.getValue()));
        tugas.setOnFailed(e -> gagal.accept(tugas.getException()));
        Thread benang = new Thread(tugas, "sehati-laporan");
        benang.setDaemon(true);
        benang.start();
    }
}
