package sehati.presentation;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import sehati.business.control.AutentikasiManager;
import sehati.business.control.PemeriksaanManager;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PenandaRisikoEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.TandaVitalEntity;

/**
 * C06 - Ringkasan rekam medis dan grafik tren tanda vital (I-06, UC05). Dokter memilih pasien dari daftar kunjungan
 * yang sudah diskrining, lalu membuka PemeriksaanForm dari sini.
 */
public class RekamMedisForm {

    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TANGGAL_GRAFIK = DateTimeFormatter.ofPattern("dd-MM-yy");

    private final PemeriksaanManager pemeriksaanManager = new PemeriksaanManager();
    private final AutentikasiManager autentikasiManager = new AutentikasiManager();

    private int idPasienTampil;
    private List<TandaVitalEntity> seriTren;
    private KunjunganEntity kunjungan;
    private PasienEntity pasien;
    private Map<KunjunganEntity, PasienEntity> daftarSiapPeriksa = Map.of();

    @FXML private ListView<KunjunganEntity> daftarPasien;
    @FXML private Label keteranganDaftar;
    @FXML private VBox panelRingkasan;
    @FXML private Label labelDataDiri;
    @FXML private VBox panelPenanda;
    @FXML private VBox panelDiagnosis;
    @FXML private VBox panelObat;
    @FXML private TabPane tabRingkasan;
    @FXML private Tab tabTren;
    @FXML private LineChart<String, Number> grafikTren;
    @FXML private GridPane tabelPengukuran;
    @FXML private Label keteranganTren;
    @FXML private Label lencanaBacaSaja;
    @FXML private Button tombolPeriksa;
    @FXML private Button tombolResep;

    @FXML
    private void initialize() {
        daftarPasien.setCellFactory(v -> new ListCell<>() {
            @Override
            protected void updateItem(KunjunganEntity k, boolean kosong) {
                super.updateItem(k, kosong);
                setText(kosong || k == null ? null : labelDaftar(k));
            }
        });
        daftarPasien.getSelectionModel().selectedItemProperty().addListener((o, lama, baru) -> {
            if (baru != null) {
                tampilkanRingkasan(baru);
            }
        });
        tabTren.setOnSelectionChanged(e -> {
            if (tabTren.isSelected() && kunjungan != null) {
                tampilkanGrafikTren();
            }
        });
        tampilkan(panelRingkasan, false);
        tampilkan(lencanaBacaSaja, false);
        muatDaftar();
    }

    /**
     * Menampilkan data diri, penanda risiko aktif, diagnosis lampau, dan riwayat obat satu kunjungan, lalu membatasi
     * pemeriksaan bagi peran selain Dokter (KF09, skenario alternatif 1 UC05).
     */
    public void tampilkanRingkasan(KunjunganEntity kunjungan) {
        this.kunjungan = kunjungan;
        this.idPasienTampil = kunjungan.getIdPasien();
        this.seriTren = null;
        tabRingkasan.getSelectionModel().selectFirst();
        labelDataDiri.setText("Memuat rekam medis...");
        // Tombol ditahan selama memuat agar pemeriksaan tidak terbuka dengan identitas pasien sebelumnya.
        tombolPeriksa.setDisable(true);
        tombolResep.setDisable(true);
        LatarBelakang.jalankan(() -> pemeriksaanManager.susunRingkasan(kunjungan.getIdKunjungan()), rm -> {
            // Hasil milik pasien yang sudah tidak dipilih dibuang.
            if (kunjungan != this.kunjungan) {
                return;
            }
            isiRingkasan(rm);
            tampilkan(panelRingkasan, true);
            if (!bolehMemeriksa()) {
                aturModeBacaSaja();
            } else {
                tampilkan(lencanaBacaSaja, false);
                tombolPeriksa.setDisable(false);
                tombolResep.setDisable(false);
            }
        }, this::tampilkanGalat);
    }

    /**
     * Grafik garis sistolik, diastolik, berat badan, dan gula darah antar-kunjungan dengan tanggal pada sumbu-x.
     * Kurang dari dua titik diganti tabel pengukuran hari ini (Algo-C06-01, KF08).
     */
    public void tampilkanGrafikTren() {
        int idPasien = idPasienTampil;
        LatarBelakang.jalankan(() -> pemeriksaanManager.susunTren(idPasien), seri -> {
            if (idPasien != idPasienTampil) {
                return;
            }
            seriTren = seri;
            grafikTren.getData().clear();
            if (seriTren.size() < 2) {
                tampilkan(grafikTren, false);
                if (!seriTren.isEmpty()) {
                    tampilkanTabelPengukuran(seriTren.get(0));
                } else {
                    tampilkan(tabelPengukuran, false);
                    keteranganTren.setText("Belum ada pengukuran tanda vital.");
                }
                return;
            }
            tampilkan(tabelPengukuran, false);
            tampilkan(grafikTren, true);
            keteranganTren.setText("");
            grafikTren.getData().add(garis("Sistolik", TandaVitalEntity::getSistolik));
            grafikTren.getData().add(garis("Diastolik", TandaVitalEntity::getDiastolik));
            grafikTren.getData().add(garis("Berat badan", TandaVitalEntity::getBeratBadan));
            grafikTren.getData().add(garis("Gula darah", TandaVitalEntity::getGulaDarah));
        }, this::tampilkanGalat);
    }

    /**
     * Pengukuran satu kunjungan dalam bentuk tabel, untuk pasien yang baru sekali diukur (skenario alternatif 4 UC05).
     */
    public void tampilkanTabelPengukuran(TandaVitalEntity nilai) {
        tabelPengukuran.getChildren().clear();
        String gula = nilai.getGulaDarah() == null ? "-" : nilai.getGulaDarah() + " mg/dL";
        String[][] baris = {
            {"Tekanan darah", nilai.getSistolik() + "/" + nilai.getDiastolik() + " mmHg"},
            {"Berat badan", nilai.getBeratBadan() + " kg"},
            {"Tinggi badan", nilai.getTinggiBadan() + " cm"},
            {"IMT", String.format("%.1f", nilai.hitungIMT())},
            {"Suhu", nilai.getSuhu() + " °C"},
            {"Nadi", nilai.getNadi() + " bpm"},
            {"Gula darah", gula},
        };
        for (int i = 0; i < baris.length; i++) {
            Label nama = new Label(baris[i][0]);
            nama.getStyleClass().add("label-kolom");
            tabelPengukuran.addRow(i, nama, new Label(baris[i][1]));
        }
        tampilkan(tabelPengukuran, true);
        keteranganTren.setText("Grafik tren terbentuk setelah pasien diukur pada dua kunjungan atau lebih.");
    }

    /**
     * Ringkasan tetap dapat dibaca, tetapi pemeriksaan tidak dapat dibuka (KF10, skenario alternatif 1 UC05).
     */
    public void aturModeBacaSaja() {
        lencanaBacaSaja.setText("Mode baca saja: Peran Anda tidak berwenang mengisi diagnosis");
        tampilkan(lencanaBacaSaja, true);
        tombolPeriksa.setDisable(true);
        tombolResep.setDisable(true);
    }

    /**
     * Data layar ini dibaca dari basis data lokal, jadi saat internet putus cukup lencana bilah yang berubah (KF22,
     * skenario alternatif 3 UC05).
     */
    public void tampilkanIndikatorLuring() {
        Navigasi.aturDaring(false);
    }

    @FXML
    private void muatDaftar() {
        keteranganDaftar.setText("Memuat daftar pasien...");
        LatarBelakang.jalankan(pemeriksaanManager::muatKunjunganSiapPeriksa, daftar -> {
            daftarSiapPeriksa = daftar;
            daftarPasien.getItems().setAll(daftar.keySet());
            keteranganDaftar.setText(daftar.isEmpty() ? "Belum ada pasien yang siap diperiksa hari ini." : "");
        }, this::tampilkanGalat);
    }

    @FXML
    private void periksaPasien() {
        if (kunjungan == null || tombolPeriksa.isDisabled()) {
            return;
        }
        PemeriksaanForm form = Navigasi.buka("PemeriksaanForm");
        form.buka(kunjungan, pasien);
    }

    @FXML
    private void buatResep() {
        if (kunjungan == null || tombolResep.isDisabled()) {
            return;
        }
        ResepForm form = Navigasi.buka("ResepForm");
        form.tampilkanFormulir(kunjungan, pasien);
    }

    private boolean bolehMemeriksa() {
        try {
            return autentikasiManager.periksaHakAkses(Navigasi.peranSesi(), "PemeriksaanForm");
        } catch (UnsupportedOperationException e) {
            // shortcut: hak akses dicek manual sampai periksaHakAkses() terisi di #10, hapus blok ini setelah itu.
            return "Dokter".equals(Navigasi.peranSesi());
        }
    }

    private void isiRingkasan(RekamMedisEntity rm) {
        pasien = rm.getPasien();
        labelDataDiri.setText(pasien == null ? "Data pasien tidak ditemukan" : dataDiri(pasien));
        panelPenanda.getChildren().clear();
        for (PenandaRisikoEntity p : rm.getPenandaAktif()) {
            PanelPeringatan peringatan = new PanelPeringatan();
            peringatan.setPesan("Risiko " + p.getJenisRisiko() + " sejak " + p.getTanggal().format(TANGGAL));
            panelPenanda.getChildren().add(peringatan);
        }
        if (rm.getPenandaAktif().isEmpty()) {
            panelPenanda.getChildren().add(new Label("Tidak ada penanda risiko aktif"));
        }
        isiDaftar(panelDiagnosis, rm.getDaftarDiagnosis().stream().map(DiagnosisEntity::ringkas).toList(),
                "Belum ada diagnosis lampau");
        isiDaftar(panelObat, rm.getRiwayatObat(), "Belum ada riwayat obat");
    }

    private static String dataDiri(PasienEntity p) {
        int umur = Period.between(p.getTanggalLahir(), LocalDate.now()).getYears();
        return p.getNama() + ", " + umur + " tahun, " + p.getJenisKelamin() + "\n"
                + "NIK " + p.getNik() + "  |  " + p.getNomorRekamMedis() + "\n" + p.getAlamat();
    }

    private String labelDaftar(KunjunganEntity k) {
        PasienEntity p = daftarSiapPeriksa.get(k);
        String nama = p == null ? "Pasien " + k.getIdPasien() : p.getNama() + "  |  " + p.getNomorRekamMedis();
        return "Antrean " + k.getNomorAntrean() + "  -  " + nama;
    }

    private XYChart.Series<String, Number> garis(String nama, Function<TandaVitalEntity, Number> nilai) {
        XYChart.Series<String, Number> seri = new XYChart.Series<>();
        seri.setName(nama);
        for (TandaVitalEntity tv : seriTren) {
            Number n = nilai.apply(tv);
            // Gula darah opsional: kunjungan tanpa nilai dilewati, bukan digambar sebagai nol.
            if (n != null) {
                seri.getData().add(new XYChart.Data<>(tv.getWaktu().format(TANGGAL_GRAFIK), n));
            }
        }
        return seri;
    }

    private static void isiDaftar(VBox panel, List<String> isi, String kosong) {
        panel.getChildren().clear();
        if (isi.isEmpty()) {
            panel.getChildren().add(new Label(kosong));
        }
        for (String baris : isi) {
            Label l = new Label(baris);
            l.setWrapText(true);
            panel.getChildren().add(l);
        }
    }

    private static void tampilkan(Node node, boolean tampil) {
        node.setVisible(tampil);
        node.setManaged(tampil);
    }

    private void tampilkanGalat(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setHeaderText("Rekam medis tidak dapat dimuat");
        alert.show();
    }
}
