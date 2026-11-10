package sehati.presentation;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import sehati.business.control.ResepManager;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.ResepEntity;

/**
 * C08 - Formulir resep elektronik beserta indikator stok (I-08, UC06). Dibuka dari RekamMedisForm atau
 * PemeriksaanForm.
 */
public class ResepForm {

    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String TERSEDIA = "Tersedia";
    private static final String STOK_KURANG = "Stok Kurang";

    private final ResepManager resepManager = new ResepManager();

    private List<ItemResepEntity> isianItem = new ArrayList<>();
    private String indikatorStok;
    private KunjunganEntity kunjungan;
    private final Map<Integer, ObatEntity> obatPerId = new HashMap<>();
    private final ObservableList<ObatEntity> semuaObat = FXCollections.observableArrayList();
    private final FilteredList<ObatEntity> obatTersaring = new FilteredList<>(semuaObat);

    @FXML private Label labelPasien;
    @FXML private TextField kolomCariObat;
    @FXML private ComboBox<ObatEntity> kolomObat;
    @FXML private TextField kolomJumlah;
    @FXML private TextField kolomDosis;
    @FXML private TextField kolomAturanPakai;
    @FXML private Label pesanJumlah;
    @FXML private Label pesanDosis;
    @FXML private Label pesanAturanPakai;
    @FXML private Label lencanaStok;
    @FXML private TableView<ItemResepEntity> tabelItem;
    @FXML private TableColumn<ItemResepEntity, String> kolomTabelObat;
    @FXML private TableColumn<ItemResepEntity, Number> kolomTabelJumlah;
    @FXML private TableColumn<ItemResepEntity, String> kolomTabelDosis;
    @FXML private TableColumn<ItemResepEntity, String> kolomTabelAturan;
    @FXML private TableColumn<ItemResepEntity, ItemResepEntity> kolomTabelStok;
    @FXML private PanelPeringatan peringatanStok;
    @FXML private Label notifikasi;
    @FXML private Button tombolKirim;

    @FXML
    private void initialize() {
        kolomObat.setItems(obatTersaring);
        kolomObat.setConverter(new StringConverter<>() {
            @Override
            public String toString(ObatEntity o) {
                return o == null ? "" : o.getNamaObat() + " (" + o.getSediaan() + "), stok " + o.getStok();
            }

            @Override
            public ObatEntity fromString(String teks) {
                return kolomObat.getValue();
            }
        });
        // Pencarian obat menyaring daftar data master tanpa kueri baru (KF11).
        kolomCariObat.textProperty().addListener((o, lama, baru) -> {
            String teks = baru == null ? "" : baru.strip().toLowerCase(Locale.ROOT);
            obatTersaring.setPredicate(obat -> obat.getNamaObat().toLowerCase(Locale.ROOT).contains(teks));
            if (!teks.isEmpty() && !obatTersaring.isEmpty()) {
                kolomObat.getSelectionModel().selectFirst();
            }
        });
        kolomTabelObat.setCellValueFactory(c -> new SimpleStringProperty(namaObat(c.getValue().getIdObat())));
        kolomTabelJumlah.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getJumlah()));
        kolomTabelDosis.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDosis()));
        kolomTabelAturan.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAturanPakai()));
        kolomTabelStok.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        kolomTabelStok.setCellFactory(k -> new TableCell<>() {
            @Override
            protected void updateItem(ItemResepEntity item, boolean kosong) {
                super.updateItem(item, kosong);
                setGraphic(kosong || item == null ? null : lencana(cukup(item), stok(item)));
            }
        });
        tabelItem.setPlaceholder(new Label("Belum ada obat pada resep"));
        tampilkan(lencanaStok, false);
        peringatanStok.setVisible(false);
        tampilkan(notifikasi, false);
        for (Label pesan : List.of(pesanJumlah, pesanDosis, pesanAturanPakai)) {
            pesan.setText("");
        }
        tombolKirim.setDisable(true);
    }

    /**
     * Memuat daftar obat dan membuka formulir resep kosong untuk kunjungan terpilih (KF11, UC06).
     */
    public void tampilkanFormulir(KunjunganEntity kunjungan) {
        this.kunjungan = kunjungan;
        isianItem = new ArrayList<>();
        tabelItem.getItems().clear();
        perbaruiTombolKirim();
        LatarBelakang.jalankan(resepManager::muatDataObat, daftar -> {
            obatPerId.clear();
            daftar.forEach(o -> obatPerId.put(o.getIdObat(), o));
            semuaObat.setAll(daftar);
            kolomCariObat.requestFocus();
        }, this::tampilkanGalat);
    }

    /**
     * Identitas kunjungan pada I-08-02, lalu tampilkanFormulir() (tambahan implementasi, B.1).
     */
    public void tampilkanFormulir(KunjunganEntity kunjungan, PasienEntity pasien) {
        String identitas = pasien == null ? "Pasien " + kunjungan.getIdPasien()
                : pasien.getNama() + "  /  " + pasien.getNomorRekamMedis();
        labelPasien.setText("Pasien: " + identitas + "  /  Kunjungan " + kunjungan.getTanggal().format(TANGGAL));
        tampilkanFormulir(kunjungan);
    }

    /**
     * Menambah satu obat ke daftar item lalu menampilkan indikator stoknya (Algo-C08-01).
     */
    public void kirimItem(int idObat, int jumlah, String dosis, String aturanPakai) {
        boolean valid = true;
        valid &= tandai(kolomJumlah, pesanJumlah, jumlah > 0, "Jumlah harus lebih dari nol");
        valid &= tandai(kolomDosis, pesanDosis, !dosis.isBlank(), "Dosis wajib diisi");
        valid &= tandai(kolomAturanPakai, pesanAturanPakai, !aturanPakai.isBlank(), "Aturan pakai wajib diisi");
        if (!valid) {
            return;
        }
        LatarBelakang.jalankan(() -> resepManager.tambahItem(idObat, jumlah, dosis, aturanPakai), item -> {
            ObatEntity obat = obatPerId.get(idObat);
            isianItem.add(item);
            tabelItem.getItems().add(item);
            // Baris lain dengan obat yang sama ikut berubah indikatornya.
            tabelItem.refresh();
            tampilkanIndikatorStok(cukup(item), obat.getStok());
            if (!cukup(item)) {
                tampilkanPeringatanStok(obat.getStok());
            }
            kolomJumlah.clear();
            kolomDosis.clear();
            kolomAturanPakai.clear();
            kolomCariObat.clear();
            kolomObat.setValue(null);
            kolomCariObat.requestFocus();
            perbaruiTombolKirim();
        }, this::tampilkanPenolakan);
    }

    public void tampilkanIndikatorStok(boolean tersedia, int sisa) {
        indikatorStok = tersedia ? TERSEDIA : STOK_KURANG;
        Label l = lencana(tersedia, sisa);
        lencanaStok.setText(l.getText());
        lencanaStok.getStyleClass().setAll(l.getStyleClass());
        tampilkan(lencanaStok, true);
    }

    /**
     * Mengirim seluruh item ke apotek, lalu mengosongkan formulir bila resep masuk antrean (Algo-C23-02).
     */
    public void kirimResep() {
        if (kunjungan == null || tombolKirim.isDisabled()) {
            return;
        }
        List<ItemResepEntity> items = List.copyOf(isianItem);
        tombolKirim.setDisable(true);
        LatarBelakang.jalankan(() -> resepManager.kirimKeApotek(kunjungan.getIdKunjungan(), items),
                (ResepEntity resep) -> {
                    isianItem = new ArrayList<>();
                    tabelItem.getItems().clear();
                    tampilkan(lencanaStok, false);
                    peringatanStok.setVisible(false);
                    notifikasi.setText("Resep masuk antrean apotek");
                    tampilkan(notifikasi, true);
                    perbaruiTombolKirim();
                }, e -> {
                    perbaruiTombolKirim();
                    tampilkanPenolakan(e);
                });
    }

    /**
     * Menahan tombol kirim sampai item yang stoknya kurang diperbaiki (skenario alternatif 1 UC06).
     */
    public void tampilkanPeringatanStok(int sisa) {
        peringatanStok.setPesan("Stok hanya tersisa " + sisa + ", perbaiki resep");
        peringatanStok.setVisible(true);
        tombolKirim.setDisable(true);
    }

    @FXML
    private void tambah() {
        ObatEntity obat = kolomObat.getValue();
        if (obat == null) {
            tampilkanPesan("Pilih obat dari daftar terlebih dahulu");
            return;
        }
        int jumlah;
        try {
            jumlah = Integer.parseInt(kolomJumlah.getText().strip());
        } catch (NumberFormatException e) {
            jumlah = 0;
        }
        kirimItem(obat.getIdObat(), jumlah, kolomDosis.getText(), kolomAturanPakai.getText());
    }

    @FXML
    private void hapusItem() {
        ItemResepEntity item = tabelItem.getSelectionModel().getSelectedItem();
        if (item == null) {
            return;
        }
        isianItem.remove(item);
        tabelItem.getItems().remove(item);
        tabelItem.refresh();
        tampilkan(lencanaStok, false);
        peringatanStok.setVisible(isianItem.stream().anyMatch(i -> !cukup(i)));
        perbaruiTombolKirim();
    }

    @FXML
    private void kirim() {
        kirimResep();
    }

    @FXML
    private void batal() {
        Navigasi.kembali();
    }

    private void perbaruiTombolKirim() {
        boolean semuaCukup = isianItem.stream().allMatch(this::cukup);
        tombolKirim.setDisable(isianItem.isEmpty() || !semuaCukup);
    }

    // Stok dibandingkan dengan total seluruh baris obat yang sama, bukan per baris.
    private boolean cukup(ItemResepEntity item) {
        int total = isianItem.stream().filter(i -> i.getIdObat() == item.getIdObat())
                .mapToInt(ItemResepEntity::getJumlah).sum();
        return stok(item) >= total;
    }

    private int stok(ItemResepEntity item) {
        ObatEntity obat = obatPerId.get(item.getIdObat());
        return obat == null ? 0 : obat.getStok();
    }

    private String namaObat(int idObat) {
        ObatEntity obat = obatPerId.get(idObat);
        return obat == null ? "Obat " + idObat : obat.getNamaObat() + " (" + obat.getSediaan() + ")";
    }

    private static Label lencana(boolean tersedia, int sisa) {
        Label l = new Label(tersedia ? TERSEDIA : STOK_KURANG + " (sisa " + sisa + ")");
        l.getStyleClass().add(tersedia ? "lencana-hijau" : "lencana-merah");
        return l;
    }

    private static boolean tandai(TextField kolom, Label pesan, boolean benar, String teksSalah) {
        if (benar) {
            PenandaKolom.bersihkan(kolom, pesan);
        } else {
            PenandaKolom.tandai(kolom, pesan, teksSalah);
        }
        return benar;
    }

    private void tampilkanPesan(String pesan) {
        notifikasi.setText(pesan);
        tampilkan(notifikasi, true);
    }

    // Penolakan aturan (stok berubah, obat tidak ada) tampil di layar; galat teknis memakai dialog.
    private void tampilkanPenolakan(Throwable e) {
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            peringatanStok.setPesan(e.getMessage());
            peringatanStok.setVisible(true);
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
        alert.setHeaderText("Resep tidak dapat diproses");
        alert.show();
    }
}
