package sehati.presentation;

import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import sehati.business.control.PasienManager;
import sehati.business.entity.PasienEntity;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C02 - PasienForm.
 *
 * Menampilkan kolom pencarian dan formulir data pasien di loket, lalu meneruskan pencarian, pendaftaran, dan
 * penyuntingan ke PasienManager.
 */
public class PasienForm {

    private static final Logger LOG = Logger.getLogger(PasienForm.class.getName());

    private static final KeyCombination CTRL_S = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination CTRL_F = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
    private static final KeyCombination CTRL_N = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);

    private final PasienManager pasienManager = new PasienManager();
    private final PauseTransition jedaKetik = new PauseTransition(Duration.millis(250));
    private final PauseTransition jedaNotifikasi = new PauseTransition(Duration.seconds(4));

    private String kataKunci;
    private PasienEntity dataFormulir;

    @FXML private VBox akar;
    @FXML private TextField kolomPencarian;
    @FXML private Button tombolPasienBaru;
    @FXML private TableView<PasienEntity> tabelHasil;
    @FXML private TableColumn<PasienEntity, String> kolomNikTabel;
    @FXML private TableColumn<PasienEntity, String> kolomRmTabel;
    @FXML private TableColumn<PasienEntity, String> kolomNamaTabel;
    @FXML private TableColumn<PasienEntity, String> kolomLahirTabel;
    @FXML private Label labelKosong;
    @FXML private TextField kolomNik;
    @FXML private TextField kolomRm;
    @FXML private TextField kolomNama;
    @FXML private DatePicker kolomTanggalLahir;
    @FXML private ComboBox<String> kolomJenisKelamin;
    @FXML private TextArea kolomAlamat;
    @FXML private TextField kolomTelepon;
    @FXML private Label pesanNik;
    @FXML private Button tombolBukaKunjungan;
    @FXML private Button tombolSimpan;
    @FXML private Label bilahNotifikasi;

    @FXML
    private void initialize() {
        kolomJenisKelamin.setItems(FXCollections.observableArrayList("Laki-laki", "Perempuan"));

        kolomNikTabel.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getNik()));
        kolomRmTabel.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getNomorRekamMedis()));
        kolomNamaTabel.setCellValueFactory(p -> new ReadOnlyStringWrapper(p.getValue().getNama()));
        kolomLahirTabel.setCellValueFactory(p ->
                new ReadOnlyStringWrapper(p.getValue().getTanggalLahir() == null
                        ? "" : p.getValue().getTanggalLahir().toString()));

        tabelHasil.getSelectionModel().selectedItemProperty().addListener((obs, lama, baru) -> {
            if (baru != null) {
                bukaFormulir(baru);
            }
        });

        jedaKetik.setOnFinished(e -> kirimPencarian(kolomPencarian.getText()));
        kolomPencarian.textProperty().addListener((obs, lama, baru) -> {
            if (baru == null || baru.isBlank()) {
                this.kataKunci = "";
                tabelHasil.getItems().clear();
                labelKosong.setVisible(false);
                labelKosong.setManaged(false);
                tombolPasienBaru.setDefaultButton(false);
                return;
            }
            jedaKetik.playFromStart();
        });
        kolomNik.textProperty().addListener((obs, lama, baru) -> hapusMasalahNik());
        jedaNotifikasi.setOnFinished(e -> tampilkanNotifikasi(""));

        bilahNotifikasi.visibleProperty().bind(bilahNotifikasi.textProperty().isNotEmpty());
        bilahNotifikasi.managedProperty().bind(bilahNotifikasi.visibleProperty());
        pesanNik.visibleProperty().bind(pesanNik.textProperty().isNotEmpty());
        pesanNik.managedProperty().bind(pesanNik.visibleProperty());

        akar.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (CTRL_S.match(e)) {
                kirimDataPasien();
                e.consume();
            } else if (CTRL_F.match(e)) {
                kolomPencarian.requestFocus();
                kolomPencarian.selectAll();
                e.consume();
            } else if (CTRL_N.match(e)) {
                bukaFormulir(null);
                e.consume();
            }
        });
        tampilkan();
    }

    public void tampilkan() {
        kataKunci = "";
        kolomPencarian.requestFocus();
        tampilkanHasilCari(List.of());
    }

    /**
     * Meneruskan kata kunci ke PasienManager.cariPasien() di thread latar, sehingga layar tetap responsif
     * (Algo-C02-01).
     */
    public void kirimPencarian(String kataKunci) {
        this.kataKunci = kataKunci == null ? "" : kataKunci.strip();
        if (this.kataKunci.length() < 3) {
            tampilkanHasilCari(List.of());
            return;
        }
        String dikirim = this.kataKunci;
        jalankan(() -> pasienManager.cariPasien(dikirim),
                hasil -> {
                    if (dikirim.equals(this.kataKunci)) {
                        tampilkanHasilCari(hasil);
                    }
                },
                "Pencarian pasien", false);
    }

    public void tampilkanHasilCari(List<PasienEntity> hasil) {
        tabelHasil.getItems().setAll(hasil);
        boolean kosong = hasil.isEmpty() && kataKunci != null && kataKunci.length() >= 3;
        labelKosong.setVisible(kosong);
        labelKosong.setManaged(kosong);
        tombolPasienBaru.setDefaultButton(kosong);
    }

    public void bukaFormulir(PasienEntity pasien) {
        dataFormulir = pasien == null ? new PasienEntity() : salin(pasien);
        kolomNik.setText(dataFormulir.getNik() == null ? "" : dataFormulir.getNik());
        kolomRm.setText(dataFormulir.getNomorRekamMedis() == null ? "" : dataFormulir.getNomorRekamMedis());
        kolomNama.setText(dataFormulir.getNama() == null ? "" : dataFormulir.getNama());
        kolomTanggalLahir.setValue(dataFormulir.getTanggalLahir());
        kolomJenisKelamin.setValue(dataFormulir.getJenisKelamin());
        kolomAlamat.setText(dataFormulir.getAlamat() == null ? "" : dataFormulir.getAlamat());
        kolomTelepon.setText(dataFormulir.getNomorTelepon() == null ? "" : dataFormulir.getNomorTelepon());
        hapusMasalahNik();
        tampilkanNotifikasi("");
        // Tombol Buka Kunjungan aktif hanya untuk pasien yang sudah tersimpan (C02 → C03).
        tombolBukaKunjungan.setDisable(pasien == null || dataFormulir.getIdPasien() == 0);
        if (pasien == null) {
            kolomNik.requestFocus();
        } else {
            kolomNama.requestFocus();
        }
    }

    @FXML
    private void bukaPasienBaru() {
        bukaFormulir(null);
    }

    @FXML
    private void kirimDataPasien() {
        if (tombolSimpan.isDisable()) {
            // Permintaan sebelumnya masih berjalan; jangan kirim ulang.
            return;
        }
        if (dataFormulir == null) {
            dataFormulir = new PasienEntity();
        }
        if (tampilkanJikaIsianKosong()) {
            return;
        }
        PasienEntity data = isianKeData();
        tombolSimpan.setDisable(true);
        if (data.getIdPasien() == 0) {
            jalankan(() -> pasienManager.daftarkanPasien(data), pasien -> {
                dataFormulir = pasien;
                kolomRm.setText(pasien.getNomorRekamMedis());
                tampilkanNotifikasi("Pendaftaran berhasil, nomor rekam medis " + pasien.getNomorRekamMedis());
                tombolSimpan.setDisable(false);
                tombolBukaKunjungan.setDisable(false);
                kirimPencarian(kolomPencarian.getText());
            }, "Pendaftaran pasien", true);
        } else {
            jalankan(() -> {
                pasienManager.perbaruiPasien(data);
                return null;
            }, abaikan -> {
                dataFormulir = data;
                tampilkanNotifikasi("Data pasien diperbarui");
                tombolSimpan.setDisable(false);
                kirimPencarian(kolomPencarian.getText());
            }, "Perbarui pasien", true);
        }
    }

    public void tampilkanPesanValidasi(String pesan) {
        pesanNik.setText(pesan);
        if (!kolomNik.getStyleClass().contains("kolom-bermasalah")) {
            kolomNik.getStyleClass().add("kolom-bermasalah");
        }
    }

    public void tampilkanNotifikasi(String pesan) {
        if (pesan == null || pesan.isEmpty()) {
            bilahNotifikasi.setText("");
            return;
        }
        bilahNotifikasi.setStyle("");
        bilahNotifikasi.getStyleClass().remove("pesan-kolom");
        if (!bilahNotifikasi.getStyleClass().contains("notifikasi-hijau")) {
            bilahNotifikasi.getStyleClass().add("notifikasi-hijau");
        }
        bilahNotifikasi.setText(pesan);
        jedaNotifikasi.playFromStart();
    }

    @FXML
    private void bukaKunjungan() {
        if (dataFormulir == null || dataFormulir.getIdPasien() == 0) {
            tampilkanNotifikasi("Simpan data pasien terlebih dahulu.");
            return;
        }
        // C03 membuka layar kunjungan untuk pasien yang sedang tampil (Algo-C03-01).
        KunjunganForm kunjunganForm = Navigasi.buka("KunjunganForm");
        kunjunganForm.buka(dataFormulir);
    }

    private boolean tampilkanJikaIsianKosong() {
        boolean bermasalah = false;
        if (kosong(kolomNama)) {
            tandaiKolomBermasalah(kolomNama);
            bermasalah = true;
        }
        if (kolomTanggalLahir.getValue() == null) {
            tandaiTanggalBermasalah();
            bermasalah = true;
        }
        if (kolomJenisKelamin.getValue() == null) {
            tandaiJenisKelaminBermasalah();
            bermasalah = true;
        }
        if (kosong(kolomAlamat)) {
            kolomAlamat.getStyleClass().add("kolom-bermasalah");
            bermasalah = true;
        }
        if (bermasalah) {
            // Isian kosong hanya menghalangi pengiriman; nilai lain dibiarkan utuh.
            tampilkanNotifikasi("Isi nama, tanggal lahir, jenis kelamin, dan alamat.");
            return true;
        }
        kolomNama.getStyleClass().remove("kolom-bermasalah");
        kolomTanggalLahir.getStyleClass().remove("kolom-bermasalah");
        kolomJenisKelamin.getStyleClass().remove("kolom-bermasalah");
        kolomAlamat.getStyleClass().remove("kolom-bermasalah");
        return false;
    }

    private PasienEntity isianKeData() {
        String telepon = kolomTelepon.getText() == null ? "" : kolomTelepon.getText().strip();
        return new PasienEntity(
                dataFormulir.getIdPasien(),
                kolomNik.getText() == null ? "" : kolomNik.getText().strip(),
                dataFormulir.getNomorRekamMedis(),
                kolomNama.getText() == null ? "" : kolomNama.getText().strip(),
                kolomTanggalLahir.getValue(),
                kolomJenisKelamin.getValue(),
                kolomAlamat.getText() == null ? "" : kolomAlamat.getText().strip(),
                telepon.isEmpty() ? null : telepon);
    }

    private void hapusMasalahNik() {
        pesanNik.setText("");
        kolomNik.getStyleClass().remove("kolom-bermasalah");
    }

    private void tandaiKolomBermasalah(TextField kolom) {
        if (!kolom.getStyleClass().contains("kolom-bermasalah")) {
            kolom.getStyleClass().add("kolom-bermasalah");
        }
    }

    private void tandaiTanggalBermasalah() {
        if (!kolomTanggalLahir.getStyleClass().contains("kolom-bermasalah")) {
            kolomTanggalLahir.getStyleClass().add("kolom-bermasalah");
        }
    }

    private void tandaiJenisKelaminBermasalah() {
        if (!kolomJenisKelamin.getStyleClass().contains("kolom-bermasalah")) {
            kolomJenisKelamin.getStyleClass().add("kolom-bermasalah");
        }
    }

    /**
     * Menjalankan pekerjaan yang bisa memanggil basis data di thread latar, lalu melanjutkan di JavaFX Application
     * Thread.
     */
    private <T> void jalankan(Supplier<T> pekerjaan, Consumer<T> berhasil, String keterangan,
            boolean nyalakanTombolSimpan) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return pekerjaan.get();
            }
        };
        task.setOnSucceeded(e -> {
            try {
                berhasil.accept(task.getValue());
            } catch (RuntimeException galat) {
                LOG.log(Level.SEVERE, keterangan, galat);
                if (galat instanceof IllegalArgumentException salah) {
                    tampilkanPesanValidasi(salah.getMessage());
                } else {
                    tampilkanNotifikasiGagal(galat.getMessage());
                }
                tombolSimpan.setDisable(false);
            }
        });
        task.setOnFailed(e -> {
            Throwable galat = task.getException();
            LOG.log(Level.SEVERE, keterangan, galat);
            if (galat instanceof IllegalArgumentException salah) {
                tampilkanPesanValidasi(salah.getMessage());
            } else {
                tampilkanNotifikasiGagal(galat == null || galat.getMessage() == null
                        ? "Terjadi kesalahan" : galat.getMessage());
            }
            if (nyalakanTombolSimpan) {
                tombolSimpan.setDisable(false);
            }
        });
        new Thread(task, keterangan).start();
    }

    private void tampilkanNotifikasiGagal(String pesan) {
        bilahNotifikasi.setStyle("");
        bilahNotifikasi.getStyleClass().remove("notifikasi-hijau");
        if (!bilahNotifikasi.getStyleClass().contains("pesan-kolom")) {
            bilahNotifikasi.getStyleClass().add("pesan-kolom");
        }
        bilahNotifikasi.setText(pesan);
        jedaNotifikasi.playFromStart();
    }

    private static boolean kosong(TextInputControl kolom) {
        return kolom.getText() == null || kolom.getText().strip().isEmpty();
    }

    private static PasienEntity salin(PasienEntity p) {
        return new PasienEntity(p.getIdPasien(), p.getNik(), p.getNomorRekamMedis(), p.getNama(),
                p.getTanggalLahir(), p.getJenisKelamin(), p.getAlamat(), p.getNomorTelepon());
    }
}
