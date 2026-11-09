package sehati.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import sehati.business.control.PersediaanManager;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PenerimaanObatEntity;

/**
 * C11 - PersediaanForm.
 *
 * Menampilkan formulir obat masuk dan dasbor peringatan persediaan, lalu meneruskan penerimaan ke
 * PersediaanManager (I-11, UC08).
 */
public class PersediaanForm {

    private static final KeyCodeCombination CTRL_S = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PersediaanManager persediaanManager = new PersediaanManager();

    private PenerimaanObatEntity isianPenerimaan;
    private List<String> daftarPeringatan = new ArrayList<>();
    // Nama kolom pada pesan catatPenerimaan() dipetakan ke kolom dan label pesannya (Algo-C11-01).
    private Map<String, Kolom> kolomPerNama;

    private record Kolom(Node isian, Label pesan, String teks) {
    }

    private record DataAwal(List<ObatEntity> obat, List<String> peringatan) {
    }

    private record HasilSimpan(ObatEntity obat, List<String> peringatan) {
    }

    @FXML private VBox akar;
    @FXML private VBox panelPeringatan;
    @FXML private ComboBox<ObatEntity> kolomObat;
    @FXML private TextField kolomFaktur;
    @FXML private DatePicker kolomTanggalTerima;
    @FXML private TextField kolomNomorBets;
    @FXML private TextField kolomJumlah;
    @FXML private DatePicker kolomKedaluwarsa;
    @FXML private Label pesanObat;
    @FXML private Label pesanFaktur;
    @FXML private Label pesanTanggalTerima;
    @FXML private Label pesanNomorBets;
    @FXML private Label pesanJumlah;
    @FXML private Label pesanKedaluwarsa;
    @FXML private Button tombolSimpan;
    @FXML private VBox panelSaldo;
    @FXML private Label labelSaldo;
    @FXML private Label bilahPesan;
    @FXML private PanelPeringatan panelPenolakan;

    @FXML
    private void initialize() {
        kolomPerNama = Map.of(
                "obat", new Kolom(kolomObat, pesanObat, "Pilih obat dari daftar"),
                "nomorFaktur", new Kolom(kolomFaktur, pesanFaktur, "Nomor faktur wajib diisi"),
                "tanggalTerima", new Kolom(kolomTanggalTerima, pesanTanggalTerima,
                        "Tanggal terima wajib diisi dan tidak boleh setelah hari ini"),
                "nomorBets", new Kolom(kolomNomorBets, pesanNomorBets,
                        "Nomor bets wajib diisi dan belum pernah tercatat untuk obat ini"),
                "jumlah", new Kolom(kolomJumlah, pesanJumlah, "Jumlah harus lebih dari nol"),
                "tanggalKedaluwarsa", new Kolom(kolomKedaluwarsa, pesanKedaluwarsa,
                        "Tanggal kedaluwarsa harus setelah tanggal terima"));
        kolomObat.setConverter(new StringConverter<>() {
            @Override
            public String toString(ObatEntity o) {
                return o == null ? "" : o.getNamaObat() + " " + o.getSediaan();
            }

            @Override
            public ObatEntity fromString(String teks) {
                return kolomObat.getValue();
            }
        });
        for (DatePicker kolom : List.of(kolomTanggalTerima, kolomKedaluwarsa)) {
            kolom.setConverter(new StringConverter<>() {
                @Override
                public String toString(LocalDate tanggal) {
                    return tanggal == null ? "" : tanggal.format(TANGGAL);
                }

                @Override
                public LocalDate fromString(String teks) {
                    return teks == null || teks.isBlank() ? null : LocalDate.parse(teks.strip(), TANGGAL);
                }
            });
        }
        akar.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (CTRL_S.match(e)) {
                kirimPenerimaan();
                e.consume();
            }
        });
        bersihkanPenanda();
        tampilkan(panelSaldo, false);
        panelPenolakan.setVisible(false);
        bilahPesan.setText("");
        // Bilah pesan hanya tampil bila ada pesan, agar tidak tersisa kotak kosong.
        bilahPesan.visibleProperty().bind(bilahPesan.textProperty().isNotEmpty());
        bilahPesan.managedProperty().bind(bilahPesan.visibleProperty());
        tampilkanFormulir();
    }

    /**
     * Memuat daftar obat, menyiapkan formulir penerimaan kosong, dan memuat peringatan persediaan (KF17, UC08).
     */
    public void tampilkanFormulir() {
        kosongkanFormulir();
        jalankan(() -> new DataAwal(persediaanManager.muatDataObat(), persediaanManager.terbitkanPeringatan()),
                awal -> {
                    kolomObat.getItems().setAll(awal.obat());
                    tampilkanPeringatan(awal.peringatan());
                    kolomObat.requestFocus();
                }, this::tampilkanGalat);
    }

    /**
     * Membentuk penerimaan dari isian lalu mencatatnya. Dipanggil tombol Simpan dan Ctrl+S (Algo-C11-01).
     */
    public void kirimPenerimaan() {
        if (tombolSimpan.isDisabled()) {
            return;
        }
        bersihkanPenanda();
        bilahPesan.setText("");
        panelPenolakan.setVisible(false);
        ObatEntity obat = kolomObat.getValue();
        int idObat = obat == null ? 0 : obat.getIdObat();
        BetsObatEntity bets = new BetsObatEntity(teks(kolomNomorBets), idObat, bacaJumlah(),
                bacaTanggal(kolomKedaluwarsa));
        isianPenerimaan = new PenerimaanObatEntity(0, teks(kolomFaktur), bacaTanggal(kolomTanggalTerima),
                new ArrayList<>(List.of(bets)));
        PenerimaanObatEntity p = isianPenerimaan;
        tombolSimpan.setDisable(true);
        jalankan(() -> {
            persediaanManager.catatPenerimaan(p);
            ObatEntity terbaru = persediaanManager.muatDataObat().stream()
                    .filter(o -> o.getIdObat() == idObat).findFirst().orElse(obat);
            return new HasilSimpan(terbaru, persediaanManager.terbitkanPeringatan());
        }, hasil -> {
            tombolSimpan.setDisable(false);
            tampilkanSaldo(hasil.obat());
            tampilkanPeringatan(hasil.peringatan());
        }, e -> {
            tombolSimpan.setDisable(false);
            tampilkanPenolakan(e);
        });
    }

    /**
     * Menampilkan saldo stok obat yang baru diterima, lalu mengosongkan formulir untuk penerimaan berikutnya.
     */
    public void tampilkanSaldo(ObatEntity obat) {
        labelSaldo.setText(obat.getNamaObat() + " " + obat.getSediaan() + ": " + obat.getStok()
                + " (ambang minimum " + obat.getAmbangMinimum() + ")");
        tampilkan(panelSaldo, true);
        // Daftar obat ikut diperbarui agar pilihan berikutnya menampilkan stok yang sama dengan saldo.
        kolomObat.getItems().replaceAll(o -> o.getIdObat() == obat.getIdObat() ? obat : o);
        kosongkanFormulir();
        bilahPesan.setText("Penerimaan obat tersimpan");
        kolomObat.requestFocus();
    }

    /**
     * Menampilkan setiap peringatan dengan ikon dan warna merah (KF17, skenario alternatif 1 UC08).
     */
    public void tampilkanPeringatan(List<String> peringatan) {
        daftarPeringatan = new ArrayList<>(peringatan);
        panelPeringatan.getChildren().clear();
        for (String pesan : daftarPeringatan) {
            PanelPeringatan panel = new PanelPeringatan();
            panel.setPesan(pesan);
            panelPeringatan.getChildren().add(panel);
        }
        if (daftarPeringatan.isEmpty()) {
            panelPeringatan.getChildren().add(new Label("Tidak ada peringatan persediaan"));
        }
    }

    /**
     * Menandai merah setiap kolom formulir pada daftar nama kolom (skenario alternatif 2 UC08).
     */
    public void tampilkanPesanKesalahan(List<String> kolom) {
        for (String nama : kolom) {
            Kolom k = kolomPerNama.get(nama.strip());
            if (k != null) {
                PenandaKolom.tandai(k.isian(), k.pesan(), k.teks());
            }
        }
    }

    @FXML
    private void simpan() {
        kirimPenerimaan();
    }

    @FXML
    private void kembali() {
        Navigasi.kembali();
    }

    // Pesan berisi nama kolom ditandai pada formulir. Penolakan lain, misalnya sesi berakhir atau basis data tidak
    // terhubung, tampil di panel agar petugas tahu penerimaan belum tersimpan.
    private void tampilkanPenolakan(Throwable e) {
        if (e instanceof IllegalArgumentException && semuaNamaKolom(e.getMessage())) {
            tampilkanPesanKesalahan(List.of(e.getMessage().split(",")));
        } else if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            // Panel merah berikon agar tidak tertukar dengan pesan berhasil (KNF08).
            panelPenolakan.setPesan(e.getMessage());
            panelPenolakan.setVisible(true);
        } else {
            tampilkanGalat(e);
        }
    }

    private boolean semuaNamaKolom(String pesan) {
        if (pesan == null || pesan.isBlank()) {
            return false;
        }
        for (String nama : pesan.split(",")) {
            if (!kolomPerNama.containsKey(nama.strip())) {
                return false;
            }
        }
        return true;
    }

    private void kosongkanFormulir() {
        kolomObat.setValue(null);
        kolomFaktur.clear();
        kolomTanggalTerima.setValue(LocalDate.now());
        kolomNomorBets.clear();
        kolomJumlah.clear();
        kolomKedaluwarsa.setValue(null);
        kolomKedaluwarsa.getEditor().clear();
    }

    private void bersihkanPenanda() {
        for (Kolom k : kolomPerNama.values()) {
            PenandaKolom.bersihkan(k.isian(), k.pesan());
        }
    }

    // Angka yang tidak terbaca dikirim sebagai 0 agar ditolak validasiPenerimaan() pada kolom jumlah.
    private int bacaJumlah() {
        try {
            return Integer.parseInt(teks(kolomJumlah));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Teks yang diketik tanpa berpindah kolom belum masuk ke nilai DatePicker, jadi teks editor dibaca langsung.
    private static LocalDate bacaTanggal(DatePicker kolom) {
        try {
            LocalDate tanggal = kolom.getConverter().fromString(kolom.getEditor().getText());
            kolom.setValue(tanggal);
            return tanggal;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String teks(TextField kolom) {
        return kolom.getText() == null ? "" : kolom.getText().strip();
    }

    // Pekerjaan basis data berjalan di Task agar layar tidak membeku (Panduan Kerja Subbab 8).
    private static <T> void jalankan(Callable<T> pekerjaan, Consumer<T> berhasil, Consumer<Throwable> gagal) {
        Task<T> tugas = new Task<>() {
            @Override
            protected T call() throws Exception {
                return pekerjaan.call();
            }
        };
        tugas.setOnSucceeded(e -> berhasil.accept(tugas.getValue()));
        tugas.setOnFailed(e -> gagal.accept(tugas.getException()));
        Thread t = new Thread(tugas, "sehati-persediaan");
        t.setDaemon(true);
        t.start();
    }

    private static void tampilkan(Node node, boolean tampil) {
        node.setVisible(tampil);
        node.setManaged(tampil);
    }

    private void tampilkanGalat(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setHeaderText("Persediaan obat tidak dapat diproses");
        alert.show();
    }
}
