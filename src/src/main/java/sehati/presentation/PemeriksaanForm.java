package sehati.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import javafx.util.StringConverter;
import sehati.business.control.PemeriksaanManager;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KodeICD10Entity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PemeriksaanEntity;

/**
 * C07 - Formulir anamnesis, diagnosis ICD-10, tindakan, dan jadwal kontrol (I-07, UC05). Dibuka dari RekamMedisForm.
 */
public class PemeriksaanForm {

    private record Draf(PemeriksaanEntity pemeriksaan, DiagnosisEntity diagnosis) {
    }

    private static final KeyCodeCombination CTRL_S = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PemeriksaanManager pemeriksaanManager = new PemeriksaanManager();
    // Pencarian dikirim setelah dokter berhenti mengetik 300 ms (KF10).
    private final PauseTransition jedaKetik = new PauseTransition(Duration.millis(300));

    private String isianAnamnesis;
    private String kataKunciDiagnosis;
    private KunjunganEntity kunjungan;
    private PasienEntity pasien;
    private DiagnosisEntity diagnosisTerpilih;
    private DiagnosisEntity diagnosisTersimpan;

    @FXML private Parent akar;
    @FXML private Label labelPasien;
    @FXML private TextArea kolomAnamnesis;
    @FXML private TextField kolomDiagnosis;
    @FXML private ListView<KodeICD10Entity> daftarSaran;
    @FXML private Label labelDiagnosis;
    @FXML private TextArea kolomTindakan;
    @FXML private DatePicker kolomKontrol;
    @FXML private Button tombolSimpan;
    @FXML private Button tombolFinal;
    @FXML private Button tombolResep;
    @FXML private Label bilahPesan;

    @FXML
    private void initialize() {
        jedaKetik.setOnFinished(e -> kirimKataKunciDiagnosis(kolomDiagnosis.getText()));
        kolomDiagnosis.textProperty().addListener((o, lama, baru) -> {
            // Teks yang berubah setelah saran dipilih membatalkan pilihan, agar kode lama tidak ikut tersimpan.
            if (diagnosisTerpilih != null && baru.equals(diagnosisTerpilih.ringkas())) {
                return;
            }
            diagnosisTerpilih = null;
            tampilkanDiagnosis();
            jedaKetik.playFromStart();
        });
        daftarSaran.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(KodeICD10Entity k, boolean kosong) {
                super.updateItem(k, kosong);
                setText(kosong || k == null ? null : k.getKode() + "  " + k.getDeskripsi());
            }
        });
        daftarSaran.setOnMouseClicked(e -> pilihSaran());
        daftarSaran.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                pilihSaran();
            }
        });
        // Panah bawah dari kolom diagnosis masuk ke daftar saran, agar pilihan bisa dibuat tanpa tetikus (KF24).
        kolomDiagnosis.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DOWN && !daftarSaran.getItems().isEmpty()) {
                daftarSaran.requestFocus();
                daftarSaran.getSelectionModel().selectFirst();
            }
        });
        // Tab memindahkan fokus, bukan menyisipkan tabulasi, agar formulir tuntas dengan papan ketik (KNF12).
        pindahFokusDenganTab(kolomAnamnesis, kolomDiagnosis, tombolSimpan);
        pindahFokusDenganTab(kolomTindakan, kolomKontrol, kolomDiagnosis);
        akar.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (CTRL_S.match(e)) {
                kirimHasilPemeriksaan();
                e.consume();
            }
        });
        tampilkan(daftarSaran, false);
        tombolFinal.setDisable(true);
        bilahPesan.setText("");
        // Bilah pesan hanya tampil bila ada pesan, agar tidak tersisa kotak kosong.
        bilahPesan.visibleProperty().bind(bilahPesan.textProperty().isNotEmpty());
        bilahPesan.managedProperty().bind(bilahPesan.visibleProperty());
        kolomKontrol.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate tanggal) {
                return tanggal == null ? "" : tanggal.format(TANGGAL);
            }

            @Override
            public LocalDate fromString(String teks) {
                try {
                    return teks == null || teks.isBlank() ? null : LocalDate.parse(teks.strip(), TANGGAL);
                } catch (DateTimeParseException e) {
                    return null;
                }
            }
        });
    }

    /**
     * Mengisi identitas kunjungan yang diperiksa (I-07-02) dan memuat draf tersimpan, agar isi lama tidak tertimpa
     * formulir kosong. Dipanggil RekamMedisForm saat Periksa Pasien ditekan (tambahan implementasi, B.1).
     */
    public void buka(KunjunganEntity kunjungan, PasienEntity pasien) {
        this.kunjungan = kunjungan;
        this.pasien = pasien;
        String identitas = pasien == null ? "Pasien " + kunjungan.getIdPasien()
                : pasien.getNama() + "  /  " + pasien.getNomorRekamMedis();
        labelPasien.setText("Pasien: " + identitas + "  /  Kunjungan " + kunjungan.getTanggal().format(TANGGAL));
        aturKolomKlinis(true);
        int idKunjungan = kunjungan.getIdKunjungan();
        LatarBelakang.jalankan(() -> new Draf(pemeriksaanManager.muatPemeriksaan(idKunjungan),
                pemeriksaanManager.muatDiagnosis(idKunjungan)), this::isiDraf, this::tampilkanGalat);
    }

    public void kirimKataKunciDiagnosis(String kataKunci) {
        kataKunciDiagnosis = kataKunci == null ? "" : kataKunci.strip();
        if (kataKunciDiagnosis.isEmpty()) {
            tampilkan(daftarSaran, false);
            return;
        }
        String dikirim = kataKunciDiagnosis;
        LatarBelakang.jalankan(() -> pemeriksaanManager.cariKodeICD10(dikirim), daftar -> {
            // Hasil kata kunci lama yang tiba belakangan dibuang.
            if (dikirim.equals(kataKunciDiagnosis)) {
                tampilkanSaranKode(daftar);
            }
        }, this::tampilkanGalat);
    }

    public void tampilkanSaranKode(List<KodeICD10Entity> daftar) {
        daftarSaran.getItems().setAll(daftar);
        tampilkan(daftarSaran, !daftar.isEmpty());
        if (daftar.isEmpty()) {
            tampilkanPesanGagal("Kode diagnosis ICD-10 tidak ditemukan");
        }
    }

    /**
     * Menyimpan draf pemeriksaan dan, bila diisi, jadwal kontrol (Algo-C07-01).
     */
    public void kirimHasilPemeriksaan() {
        if (kunjungan == null || tombolSimpan.isDisabled()) {
            return;
        }
        isianAnamnesis = kolomAnamnesis.getText() == null ? "" : kolomAnamnesis.getText().strip();
        if (isianAnamnesis.isEmpty()) {
            tampilkanPesanGagal("Anamnesis wajib diisi");
            kolomAnamnesis.requestFocus();
            return;
        }
        LocalDate kontrol;
        try {
            kontrol = bacaTanggalKontrol();
        } catch (IllegalArgumentException e) {
            tampilkanPesanGagal(e.getMessage());
            kolomKontrol.requestFocus();
            return;
        }
        // UC05 alternatif 2: teks yang diketik bebas tidak boleh lolos sebagai diagnosis (KF10).
        String teksDiagnosis = kolomDiagnosis.getText() == null ? "" : kolomDiagnosis.getText().strip();
        if (!teksDiagnosis.isEmpty() && diagnosisTerpilih == null
                && (diagnosisTersimpan == null || !teksDiagnosis.equals(diagnosisTersimpan.ringkas()))) {
            tampilkanPesanGagal("Pilih kode ICD-10 dari daftar saran");
            kolomDiagnosis.requestFocus();
            return;
        }
        String tindakan = kolomTindakan.getText() == null ? "" : kolomTindakan.getText().strip();
        PemeriksaanEntity p = new PemeriksaanEntity(0, kunjungan.getIdKunjungan(), isianAnamnesis, tindakan, false);
        DiagnosisEntity d = diagnosisTerpilih;
        tombolSimpan.setDisable(true);
        LatarBelakang.jalankan(() -> {
            pemeriksaanManager.simpanPemeriksaan(kunjungan.getIdKunjungan(), p, d);
            return p;
        }, tersimpan -> {
            tombolFinal.setDisable(false);
            if (d != null) {
                diagnosisTersimpan = d;
            }
            if (kontrol == null) {
                tombolSimpan.setDisable(false);
                tampilkanPesan("Pemeriksaan tersimpan");
            } else {
                kirimJadwalKontrol(kontrol);
            }
        }, e -> {
            tombolSimpan.setDisable(false);
            tampilkanPenolakan(e);
        });
    }

    /**
     * Mengunci rekam medis kunjungan setelah konfirmasi dokter, lalu seluruh kolom tidak dapat disunting (UC05).
     */
    public void kirimFinal() {
        if (kunjungan == null || tombolFinal.isDisabled()) {
            return;
        }
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION,
                "Setelah dikunci, hasil pemeriksaan tidak dapat diubah lagi. Lanjutkan?", ButtonType.OK,
                ButtonType.CANCEL);
        konfirmasi.setHeaderText("Simpan final dan kunci rekam medis");
        if (konfirmasi.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        int idKunjungan = kunjungan.getIdKunjungan();
        tombolFinal.setDisable(true);
        LatarBelakang.jalankan(() -> {
            pemeriksaanManager.kunciRekamMedis(idKunjungan);
            return null;
        }, selesai -> {
            kunciKolomKlinis();
            tampilkanPesan("Rekam medis kunjungan ini sudah dikunci");
        }, e -> {
            tombolFinal.setDisable(false);
            tampilkanPenolakan(e);
        });
    }

    /**
     * Menonaktifkan kolom klinis dan kedua tombol simpan (skenario alternatif 1 UC05 dan setelah dikunci).
     */
    public void kunciKolomKlinis() {
        aturKolomKlinis(true);
    }

    public void tampilkanPesan(String pesan) {
        aturPesan(pesan, false);
    }

    // Penolakan tampil merah, bukan dengan gaya notifikasi berhasil (sama dengan PasienForm).
    private void tampilkanPesanGagal(String pesan) {
        aturPesan(pesan, true);
    }

    private void aturPesan(String pesan, boolean gagal) {
        bilahPesan.getStyleClass().removeAll("notifikasi-hijau", "pesan-kolom");
        bilahPesan.getStyleClass().add(gagal ? "pesan-kolom" : "notifikasi-hijau");
        bilahPesan.setText(pesan);
    }

    @FXML
    private void simpan() {
        kirimHasilPemeriksaan();
    }

    @FXML
    private void simpanFinal() {
        kirimFinal();
    }

    @FXML
    private void buatResep() {
        if (kunjungan == null) {
            return;
        }
        ResepForm form = Navigasi.buka("ResepForm");
        form.tampilkanFormulir(kunjungan, pasien);
    }

    private void isiDraf(Draf draf) {
        PemeriksaanEntity p = draf.pemeriksaan();
        diagnosisTersimpan = draf.diagnosis();
        tampilkanDiagnosis();
        if (p != null) {
            kolomAnamnesis.setText(p.getAnamnesis());
            kolomTindakan.setText(p.getTindakan());
        }
        if (p != null && p.isTerkunci()) {
            tampilkanPesan("Rekam medis kunjungan ini sudah dikunci");
            return;
        }
        aturKolomKlinis(false);
        // Tombol final baru aktif setelah pemeriksaan pernah tersimpan (Subbab 5.7.1).
        tombolFinal.setDisable(p == null);
        kolomAnamnesis.requestFocus();
    }

    private void kirimJadwalKontrol(LocalDate tanggal) {
        LatarBelakang.jalankan(() -> pemeriksaanManager.jadwalkanKontrol(kunjungan.getIdPasien(), tanggal),
                (JadwalKontrolEntity jadwal) -> {
                    tombolSimpan.setDisable(false);
                    // Tanggal dikosongkan agar simpan berikutnya tidak menjadwalkan kontrol lagi.
                    kolomKontrol.setValue(null);
                    kolomKontrol.getEditor().clear();
                    tampilkanPesan("Kontrol dijadwalkan " + jadwal.getTanggalRencana().format(TANGGAL));
                }, e -> {
                    tombolSimpan.setDisable(false);
                    tampilkanPesanGagal("Pemeriksaan tersimpan, tetapi jadwal kontrol gagal: " + e.getMessage());
                });
    }

    // Membaca teks kolom, bukan nilainya, karena tanggal yang baru diketik belum masuk ke nilai saat Ctrl+S.
    private LocalDate bacaTanggalKontrol() {
        String teks = kolomKontrol.getEditor().getText();
        if (teks == null || teks.isBlank()) {
            return null;
        }
        LocalDate tanggal;
        try {
            tanggal = LocalDate.parse(teks.strip(), TANGGAL);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Tanggal kontrol harus berformat dd-mm-yyyy");
        }
        pemeriksaanManager.validasiTanggalKontrol(tanggal);
        return tanggal;
    }

    private void aturKolomKlinis(boolean nonaktif) {
        for (Node kolom : List.of(kolomAnamnesis, kolomDiagnosis, daftarSaran, kolomTindakan, kolomKontrol,
                tombolSimpan, tombolFinal)) {
            kolom.setDisable(nonaktif);
        }
    }

    // Tanpa pilihan baru, diagnosis tersimpan tetap dipakai karena simpanPemeriksaan(p, null) tidak mengubahnya.
    private void tampilkanDiagnosis() {
        if (diagnosisTerpilih != null) {
            labelDiagnosis.setText("Diagnosis terpilih: " + diagnosisTerpilih.ringkas());
        } else if (diagnosisTersimpan != null) {
            labelDiagnosis.setText("Diagnosis tersimpan: " + diagnosisTersimpan.ringkas());
        } else {
            labelDiagnosis.setText("Diagnosis terpilih: belum ada");
        }
    }

    private void pilihSaran() {
        KodeICD10Entity k = daftarSaran.getSelectionModel().getSelectedItem();
        if (k == null) {
            return;
        }
        // Deskripsi disalin dari data master agar ringkasan tetap terbaca bila master berubah (Subbab 5.43).
        diagnosisTerpilih = new DiagnosisEntity(0, k.getKode(), k.getDeskripsi());
        kolomDiagnosis.setText(diagnosisTerpilih.ringkas());
        jedaKetik.stop();
        // Hasil pencarian yang masih berjalan tidak boleh memunculkan daftar saran lagi.
        kataKunciDiagnosis = "";
        tampilkanDiagnosis();
        tampilkan(daftarSaran, false);
        kolomTindakan.requestFocus();
    }

    private static void pindahFokusDenganTab(TextArea area, Node berikutnya, Node sebelumnya) {
        area.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB) {
                (e.isShiftDown() ? sebelumnya : berikutnya).requestFocus();
                e.consume();
            }
        });
    }

    // Penolakan aturan (misalnya sudah dikunci) tampil di bilah pesan; galat teknis memakai dialog.
    private void tampilkanPenolakan(Throwable e) {
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            tampilkanPesanGagal(e.getMessage());
        } else {
            tampilkanGalat(e);
        }
    }

    private static void tampilkan(Node node, boolean tampil) {
        node.setVisible(tampil);
        node.setManaged(tampil);
    }

    private void tampilkanGalat(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setHeaderText("Pemeriksaan tidak dapat diproses");
        alert.show();
    }
}
