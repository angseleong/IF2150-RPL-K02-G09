package sehati.presentation;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import sehati.business.control.KunjunganManager;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PoliEntity;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C03 - KunjunganForm.
 *
 * Menampilkan pilihan poli dan tiket antrean, lalu meneruskan permintaan kunjungan ke KunjunganManager.
 */
public class KunjunganForm {

    private static final Logger LOG = Logger.getLogger(KunjunganForm.class.getName());

    private static final String STATUS_AWAL = "menunggu skrining";
    private static final DateTimeFormatter FORMAT_TANGGAL = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy",
            Locale.forLanguageTag("id-ID"));

    private final KunjunganManager kunjunganManager = new KunjunganManager();

    private PasienEntity pasienTerpilih;
    private PoliEntity poliTerpilih;

    @FXML private VBox akar;
    @FXML private Label labelPasien;
    @FXML private ComboBox<PoliEntity> pilihanPoli;
    @FXML private Button tombolBukaKunjungan;
    @FXML private VBox kartuTiket;
    @FXML private Label labelJudulTiket;
    @FXML private Label labelNamaPoli;
    @FXML private Label labelNomorAntrean;
    @FXML private Label labelTanggal;
    @FXML private Label labelStatus;
    @FXML private Label labelKeterangan;
    @FXML private Button tombolCetak;
    @FXML private Button tombolKembali;
    @FXML private Label bilahNotifikasi;

    @FXML
    private void initialize() {
        // Pilihan tertampil berupa nama poli, bukan hasil toString() bawaan.
        pilihanPoli.setConverter(new StringConverter<>() {
            @Override
            public String toString(PoliEntity poli) {
                return poli == null ? "" : poli.getNamaPoli();
            }

            @Override
            public PoliEntity fromString(String teks) {
                return null;
            }
        });
        pilihanPoli.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(PoliEntity poli, boolean kosong) {
                super.updateItem(poli, kosong);
                setText(poli == null || kosong ? null : poli.getNamaPoli());
            }
        });
        pilihanPoli.getSelectionModel().selectedItemProperty().addListener((obs, lama, baru) -> {
            poliTerpilih = baru;
            // Tombol tetap nonaktif sampai satu poli dipilih (Tabel 5.3.1).
            tombolBukaKunjungan.setDisable(baru == null);
        });
        bilahNotifikasi.visibleProperty().bind(bilahNotifikasi.textProperty().isNotEmpty());
        bilahNotifikasi.managedProperty().bind(bilahNotifikasi.visibleProperty());
    }

    /**
     * Membuka layar untuk pasien terpilih, memuat daftar poli lewat KunjunganManager.daftarPoli(), lalu memanggil
     * tampilkanPilihanPoli() (Tabel 5.3.1, UC03). Pemuatan berjalan di thread latar agar layar tetap responsif.
     */
    public void buka(PasienEntity pasien) {
        pasienTerpilih = pasien;
        labelPasien.setText(String.format("Pasien %s, NIK %s", pasien.getNama(), pasien.getNik()));
        sembunyikanTiket();
        tombolBukaKunjungan.setDisable(true);
        jalankan(kunjunganManager::daftarPoli, this::tampilkanPilihanPoli, "Pengambilan daftar poli");
    }

    /**
     * Menampilkan poli sebagai pilihan tujuan; tombol "Buka Kunjungan" tetap nonaktif sampai satu poli dipilih
     * (Tabel 5.3.1).
     */
    public void tampilkanPilihanPoli(List<PoliEntity> daftarPoli) {
        pilihanPoli.getItems().setAll(daftarPoli);
        pilihanPoli.setPromptText(daftarPoli.isEmpty() ? "Belum ada poli terdaftar" : "Pilih poli tujuan");
        pilihanPoli.getSelectionModel().clearSelection();
        poliTerpilih = null;
    }

    /**
     * Memanggil KunjunganManager.bukaKunjungan() di thread latar lalu menampilkan tiket atau kunjungan aktif
     * (Algo-C03-01, KF03). Isian belum lengkap hanya mengakhiri prosedur. Tombol dinonaktifkan selama permintaan
     * berjalan agar satu klik ganda tidak menerbitkan dua nomor, dan selalu dinyalakan kembali setelahnya.
     */
    public void kirimPermintaanKunjungan() {
        if (pasienTerpilih == null || poliTerpilih == null) {
            return;
        }
        if (tombolBukaKunjungan.isDisable()) {
            // Permintaan sebelumnya masih berjalan; jangan kirim ulang.
            return;
        }
        tombolBukaKunjungan.setDisable(true);
        int idPasien = pasienTerpilih.getIdPasien();
        PoliEntity poli = poliTerpilih;
        // Kunjungan yang sudah ada dicek dulu, karena kunjungan aktif yang belum diskrining juga berstatus
        // "menunggu skrining" dan tidak bisa dibedakan dari kunjungan baru lewat statusnya (UC03 alternatif 1)
        jalankan(() -> {
                    KunjunganEntity aktif = kunjunganManager.kunjunganAktif(idPasien);
                    return aktif != null ? Map.entry(true, aktif)
                            : Map.entry(false, kunjunganManager.bukaKunjungan(idPasien, poli.getIdPoli()));
                },
                hasil -> {
                    KunjunganEntity kunjungan = hasil.getValue();
                    if (!hasil.getKey() && STATUS_AWAL.equals(kunjungan.getStatus())) {
                        tampilkanTiketAntrean(kunjungan);
                    } else {
                        tampilkanKunjunganAktif(kunjungan);
                    }
                    tombolBukaKunjungan.setDisable(false);
                }, "Pembukaan kunjungan");
    }

    /**
     * Menampilkan tiket antrean berisi nama poli, nomor antrean, dan tanggal, serta menyediakan tombol cetak
     * (Tabel 5.3.1, KF03).
     */
    public void tampilkanTiketAntrean(KunjunganEntity kunjungan) {
        tampilkanTiket(kunjungan, namaPoliKunjungan(kunjungan), "Tiket Antrean",
                "Tunjukkan nomor ini kepada petugas skrining.", false);
    }

    /**
     * Menampilkan nomor antrean dan status kunjungan yang sudah berjalan beserta keterangan bahwa kunjungan baru
     * tidak dibuat (Tabel 5.3.1, skenario alternatif 1 UC03).
     */
    public void tampilkanKunjunganAktif(KunjunganEntity kunjungan) {
        labelStatus.setText("Status saat ini: " + kunjungan.getStatus());
        tampilkanTiket(kunjungan, namaPoliKunjungan(kunjungan), "Kunjungan Sudah Berjalan",
                "Pasien sudah memiliki kunjungan hari ini, kunjungan baru tidak dibuat.", true);
    }

    private void tampilkanTiket(KunjunganEntity kunjungan, String namaPoli, String judul, String keterangan,
            boolean tampilStatus) {
        labelJudulTiket.setText(judul);
        labelNamaPoli.setText(namaPoli);
        labelNomorAntrean.setText(kunjungan.getNomorAntrean() + "");
        labelTanggal.setText(FORMAT_TANGGAL.format(kunjungan.getTanggal()));
        labelStatus.setVisible(tampilStatus);
        labelStatus.setManaged(tampilStatus);
        labelKeterangan.setText(keterangan);
        kartuTiket.setVisible(true);
        kartuTiket.setManaged(true);
        tombolCetak.setVisible(true);
        tombolCetak.setManaged(true);
    }

    private void sembunyikanTiket() {
        kartuTiket.setVisible(false);
        kartuTiket.setManaged(false);
        tombolCetak.setVisible(false);
        tombolCetak.setManaged(false);
    }

    private String namaPoliKunjungan(KunjunganEntity kunjungan) {
        if (poliTerpilih != null && poliTerpilih.getIdPoli() == kunjungan.getIdPoli()) {
            return poliTerpilih.getNamaPoli();
        }
        // Kunjungan aktif bisa berada di poli lain dari pilihan petugas
        return pilihanPoli.getItems().stream().filter(p -> p.getIdPoli() == kunjungan.getIdPoli())
                .map(PoliEntity::getNamaPoli).findFirst().orElse("Poli nomor " + kunjungan.getIdPoli());
    }

    /**
     * Mencetak kartu tiket ke printer yang dipilih pengguna (Tabel 5.3.1).
     */
    @FXML
    private void bukaDialogCetak() {
        PrinterJob pekerjaan = PrinterJob.createPrinterJob();
        if (pekerjaan != null && pekerjaan.showPrintDialog(akar.getScene().getWindow())
                && pekerjaan.printPage(kartuTiket)) {
            pekerjaan.endJob();
        }
    }

    @FXML
    private void kembaliKePasienForm() {
        Navigasi.kembali();
    }

    /**
     * Menjalankan pekerjaan yang bisa memanggil basis data di thread latar, lalu melanjutkan di JavaFX Application
     * Thread (Subbab 8 Panduan Kerja).
     */
    private <T> void jalankan(Supplier<T> pekerjaan, Consumer<T> berhasil, String keterangan) {
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
                tampilkanNotifikasi(galat.getMessage());
                pulihkanTombol();
            }
        });
        task.setOnFailed(e -> {
            Throwable galat = task.getException();
            LOG.log(Level.SEVERE, keterangan, galat);
            tampilkanNotifikasi(galat == null || galat.getMessage() == null
                    ? "Terjadi kesalahan" : galat.getMessage());
            pulihkanTombol();
        });
        new Thread(task, keterangan).start();
    }

    // Permintaan gagal tidak boleh membuat tombol mati terus
    private void pulihkanTombol() {
        tombolBukaKunjungan.setDisable(poliTerpilih == null);
    }

    private void tampilkanNotifikasi(String pesan) {
        bilahNotifikasi.getStyleClass().remove("notifikasi-hijau");
        if (!bilahNotifikasi.getStyleClass().contains("pesan-kolom")) {
            bilahNotifikasi.getStyleClass().add("pesan-kolom");
        }
        bilahNotifikasi.setText(pesan);
    }
}
