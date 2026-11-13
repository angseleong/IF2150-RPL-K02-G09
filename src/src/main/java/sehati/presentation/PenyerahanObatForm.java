package sehati.presentation;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import sehati.business.control.ApotekManager;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.ResepEntity;

/**
 * C10 - PenyerahanObatForm.
 *
 * Menampilkan rincian resep dan meneruskan konfirmasi "Obat Diserahkan" ke ApotekManager (I-10, UC07).
 */
public class PenyerahanObatForm {

    private static final KeyCodeCombination CTRL_ENTER =
            new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);
    private static final DateTimeFormatter JAM = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final ApotekManager apotekManager = new ApotekManager();

    private ResepEntity rincianTampil;
    private String catatanPengembalian;
    private PasienEntity pasien;
    private Map<Integer, ObatEntity> obatPerId = Map.of();
    // Pesan hasil diteruskan ke AntreanResepForm karena layar ini langsung ditutup setelah berhasil.
    private Consumer<String> pesanKeAntrean = pesan -> { };

    private record Rincian(ResepEntity resep, List<BetsObatEntity> bets) {
    }

    @FXML private VBox akar;
    @FXML private Label labelPasien;
    @FXML private TableView<ItemResepEntity> tabelItem;
    @FXML private TableColumn<ItemResepEntity, String> kolomItemObat;
    @FXML private TableColumn<ItemResepEntity, String> kolomItemDosis;
    @FXML private TableColumn<ItemResepEntity, Number> kolomItemJumlah;
    @FXML private TableColumn<ItemResepEntity, String> kolomItemAturan;
    @FXML private TableView<BetsObatEntity> tabelBets;
    @FXML private TableColumn<BetsObatEntity, String> kolomBetsObat;
    @FXML private TableColumn<BetsObatEntity, String> kolomBetsNomor;
    @FXML private TableColumn<BetsObatEntity, String> kolomBetsKedaluwarsa;
    @FXML private TableColumn<BetsObatEntity, Number> kolomBetsSisa;
    @FXML private Button tombolSerahkan;
    @FXML private Button tombolKembalikan;
    @FXML private VBox panelPengembalian;
    @FXML private TextArea kolomAlasan;
    @FXML private Label pesanAlasan;
    @FXML private Button tombolKirimPengembalian;
    @FXML private Label bilahPesan;
    @FXML private PanelPeringatan panelPenolakan;

    @FXML
    private void initialize() {
        kolomItemObat.setCellValueFactory(c -> new SimpleStringProperty(namaObat(c.getValue().getIdObat())));
        kolomItemDosis.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDosis()));
        kolomItemJumlah.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getJumlah()));
        kolomItemAturan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAturanPakai()));
        kolomBetsObat.setCellValueFactory(c -> new SimpleStringProperty(namaObat(c.getValue().getIdObat())));
        kolomBetsNomor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNomorBets()));
        kolomBetsKedaluwarsa.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getTanggalKedaluwarsa().format(TANGGAL)));
        kolomBetsSisa.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getJumlah()));
        tabelBets.setPlaceholder(new Label("Tidak ada bets bersisa untuk obat pada resep ini"));
        // Filter agar Ctrl+Enter tetap berjalan saat fokus di tabel, kecuali saat menulis alasan pengembalian.
        akar.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (CTRL_ENTER.match(e) && !kolomAlasan.isFocused()) {
                kirimKonfirmasi();
                e.consume();
            }
        });
        tampilkan(panelPengembalian, false);
        panelPenolakan.setVisible(false);
        PenandaKolom.bersihkan(kolomAlasan, pesanAlasan);
        bilahPesan.setText("");
        // Bilah pesan hanya tampil bila ada pesan, agar tidak tersisa kotak kosong.
        bilahPesan.visibleProperty().bind(bilahPesan.textProperty().isNotEmpty());
        bilahPesan.managedProperty().bind(bilahPesan.visibleProperty());
    }

    /**
     * Identitas pasien, nama obat, dan penerima pesan hasil dari AntreanResepForm (tambahan implementasi, B.1).
     */
    public void aturKonteks(PasienEntity pasien, Map<Integer, ObatEntity> obatPerId, Consumer<String> pesanKeAntrean) {
        this.pasien = pasien;
        this.obatPerId = obatPerId;
        this.pesanKeAntrean = pesanKeAntrean;
    }

    /**
     * Menampilkan identitas pasien, item resep, dan bets terurut kedaluwarsa terdekat (KF13, UC07).
     */
    public void tampilkanRincian(ResepEntity resep, List<BetsObatEntity> bets) {
        rincianTampil = resep;
        String identitas = pasien == null ? "Kunjungan " + resep.getIdKunjungan()
                : pasien.getNama() + "  /  " + pasien.getNomorRekamMedis();
        labelPasien.setText("Pasien: " + identitas + "  /  Resep masuk " + resep.getWaktuMasuk().format(JAM));
        tabelItem.getItems().setAll(resep.getDaftarItem());
        tabelBets.getItems().setAll(bets);
        aktifkanTindakan(true);
        tombolSerahkan.requestFocus();
    }

    /**
     * Menyerahkan obat lewat ApotekManager. Terhubung ke tombol Obat Diserahkan dan Ctrl+Enter (Algo-C10-01, KF14).
     */
    public void kirimKonfirmasi() {
        if (rincianTampil == null || tombolSerahkan.isDisabled()) {
            return;
        }
        // Tombol dinonaktifkan agar klik ganda tidak mengirim dua permintaan.
        aktifkanTindakan(false);
        int idResep = rincianTampil.getIdResep();
        LatarBelakang.jalankan(() -> {
            apotekManager.serahkanObat(idResep);
            return null;
        }, hasil -> {
            tampilkanPesan("Obat diserahkan");
            pesanKeAntrean.accept("Obat diserahkan untuk " + namaPasien());
            Navigasi.kembali();
        }, this::tanganiPenolakan);
    }

    /**
     * Membuka kolom alasan dan tombol Kirim Pengembalian (skenario alternatif 1 UC07).
     */
    public void bukaKolomCatatan() {
        tampilkan(panelPengembalian, true);
        kolomAlasan.requestFocus();
    }

    /**
     * Mengembalikan resep ke dokter dengan alasan yang tidak boleh kosong, lalu menutup rincian resep.
     */
    public void kirimPengembalian(String alasan) {
        catatanPengembalian = alasan == null ? "" : alasan.strip();
        if (catatanPengembalian.isEmpty()) {
            PenandaKolom.tandai(kolomAlasan, pesanAlasan, "Alasan pengembalian wajib diisi");
            kolomAlasan.requestFocus();
            return;
        }
        PenandaKolom.bersihkan(kolomAlasan, pesanAlasan);
        aktifkanTindakan(false);
        int idResep = rincianTampil.getIdResep();
        String catatan = catatanPengembalian;
        LatarBelakang.jalankan(() -> {
            apotekManager.kembalikanKeDokter(idResep, catatan);
            return null;
        }, hasil -> {
            tampilkanPesan("Resep dikembalikan ke dokter");
            pesanKeAntrean.accept("Resep " + namaPasien() + " dikembalikan ke dokter");
            Navigasi.kembali();
        }, this::tanganiPenolakan);
    }

    public void tampilkanPesan(String pesan) {
        panelPenolakan.setVisible(false);
        bilahPesan.setText(pesan);
    }

    // Penolakan memakai panel merah berikon agar tidak tertukar dengan pesan berhasil (KNF08).
    private void tolak(String pesan) {
        bilahPesan.setText("");
        panelPenolakan.setPesan(pesan);
        panelPenolakan.setVisible(true);
    }

    @FXML
    private void serahkan() {
        kirimKonfirmasi();
    }

    @FXML
    private void bukaPengembalian() {
        bukaKolomCatatan();
    }

    @FXML
    private void kirimAlasan() {
        kirimPengembalian(kolomAlasan.getText());
    }

    @FXML
    private void kembali() {
        Navigasi.kembali();
    }

    // Penolakan (resep sudah selesai, stok tidak cukup) tampil di layar, lalu rincian dimuat ulang. Bila resep sudah
    // tidak menunggu, tindakan tetap nonaktif dan petugas kembali ke antrean dengan Esc (Algo-C10-01).
    private void tanganiPenolakan(Throwable e) {
        if (!(e instanceof IllegalArgumentException || e instanceof IllegalStateException)) {
            aktifkanTindakan(true);
            tampilkanGalat(e);
            return;
        }
        tolak(e.getMessage());
        int idResep = rincianTampil.getIdResep();
        LatarBelakang.jalankan(() -> {
            ResepEntity resep = apotekManager.layaniResep(idResep);
            return new Rincian(resep, apotekManager.muatBetsResep(resep));
        }, rincian -> {
            tampilkanRincian(rincian.resep(), rincian.bets());
            tolak(e.getMessage());
        }, galatMuatUlang -> aktifkanTindakan(false));
    }

    private void aktifkanTindakan(boolean aktif) {
        tombolSerahkan.setDisable(!aktif);
        tombolKembalikan.setDisable(!aktif);
        tombolKirimPengembalian.setDisable(!aktif);
    }

    private String namaPasien() {
        return pasien == null ? "kunjungan " + rincianTampil.getIdKunjungan() : pasien.getNama();
    }

    private String namaObat(int idObat) {
        ObatEntity obat = obatPerId.get(idObat);
        return obat == null ? "Obat " + idObat : obat.getNamaObat() + " " + obat.getSediaan();
    }

    private static void tampilkan(Node node, boolean tampil) {
        node.setVisible(tampil);
        node.setManaged(tampil);
    }

    private void tampilkanGalat(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setHeaderText("Penyerahan obat tidak dapat diproses");
        alert.show();
    }
}
