package sehati.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import sehati.business.control.RisikoManager;
import sehati.business.control.SkriningManager;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PenandaRisikoEntity;
import sehati.business.entity.TandaVitalEntity;

/**
 * C05 - Formulir keluhan dan tanda vital (I-05). Isian diteruskan ke SkriningManager, lalu penanda risiko diminta
 * dari RisikoManager dan ditampilkan bersama peringatan rentang (UC04, Algo-C05-01).
 */
public class SkriningForm {

    private static final KeyCodeCombination CTRL_S = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);

    // Nama kolom Algo-C20-02, label rentang yang ditampilkan di samping kolom, dan apakah nilainya bilangan bulat.
    private static final String[] KOLOM = {"sistolik", "diastolik", "beratBadan", "tinggiBadan", "suhu", "nadi",
        "gulaDarah"};
    private static final Map<String, String> RENTANG = Map.of(
            "sistolik", "Rentang wajar 50 sampai 300 mmHg",
            "diastolik", "Rentang wajar 30 sampai 200 mmHg dan harus lebih rendah dari sistolik",
            "beratBadan", "Rentang wajar 1 sampai 350 kg",
            "tinggiBadan", "Rentang wajar 30 sampai 250 cm",
            "suhu", "Rentang wajar 30 sampai 45 °C",
            "nadi", "Rentang wajar 20 sampai 250 bpm",
            "gulaDarah", "Rentang wajar 20 sampai 1000 mg/dL");
    private static final List<String> BULAT = List.of("sistolik", "diastolik", "nadi", "gulaDarah");

    private record HasilSimpan(List<String> galat, List<PenandaRisikoEntity> penanda) {
    }

    private final SkriningManager skriningManager = new SkriningManager();
    private final RisikoManager risikoManager = new RisikoManager();

    private KunjunganEntity kunjungan;
    private String isianKeluhan;
    private TandaVitalEntity isianUkuran;

    @FXML private Parent akar;
    @FXML private Label labelPasien;
    @FXML private TextArea kolomKeluhan;
    @FXML private TextField kolomSistolik;
    @FXML private TextField kolomDiastolik;
    @FXML private TextField kolomBeratBadan;
    @FXML private TextField kolomTinggiBadan;
    @FXML private TextField kolomSuhu;
    @FXML private TextField kolomNadi;
    @FXML private TextField kolomGulaDarah;
    @FXML private Label pesanSistolik;
    @FXML private Label pesanDiastolik;
    @FXML private Label pesanBeratBadan;
    @FXML private Label pesanTinggiBadan;
    @FXML private Label pesanSuhu;
    @FXML private Label pesanNadi;
    @FXML private Label pesanGulaDarah;
    @FXML private VBox panelPenanda;
    @FXML private Label notifikasi;
    @FXML private Button tombolSimpan;

    @FXML
    private void initialize() {
        // Tab pada TextArea secara bawaan menyisipkan tabulasi; di sini ia memindahkan fokus agar formulir tuntas
        // dengan papan ketik saja (KF24, KNF12).
        kolomKeluhan.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.TAB) {
                (e.isShiftDown() ? tombolSimpan : kolomSistolik).requestFocus();
                e.consume();
            }
        });
        akar.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (CTRL_S.match(e)) {
                kirimHasilSkrining();
                e.consume();
            }
        });
        for (String kolom : KOLOM) {
            TextField isian = isian(kolom);
            // Kolom yang diperbaiki langsung kehilangan tanda merahnya.
            isian.textProperty().addListener((o, lama, baru) -> PenandaKolom.bersihkan(isian, pesan(kolom)));
            PenandaKolom.bersihkan(isian, pesan(kolom));
        }
        panelPenanda.setVisible(false);
        panelPenanda.setManaged(false);
        notifikasi.setVisible(false);
        notifikasi.setManaged(false);
    }

    public void tampilkanFormulir(KunjunganEntity kunjungan) {
        this.kunjungan = kunjungan;
        labelPasien.setText("Memuat identitas pasien...");
        LatarBelakang.jalankan(() -> skriningManager.labelPasien(List.of(kunjungan)).get(kunjungan.getIdKunjungan()),
                labelPasien::setText, this::tampilkanGalat);
        Platform.runLater(() -> kolomKeluhan.requestFocus());
    }

    public void kirimHasilSkrining() {
        if (kunjungan == null || tombolSimpan.isDisabled()) {
            return;
        }
        TandaVitalEntity tv = new TandaVitalEntity();
        List<String> tidakTerbaca = new ArrayList<>();
        for (String kolom : KOLOM) {
            bacaKolom(kolom, tv, tidakTerbaca);
        }
        if (!tidakTerbaca.isEmpty()) {
            tampilkanPeringatanRentang(tidakTerbaca);
            return;
        }
        isianKeluhan = kolomKeluhan.getText() == null ? "" : kolomKeluhan.getText().trim();
        isianUkuran = tv;
        int idKunjungan = kunjungan.getIdKunjungan();
        tombolSimpan.setDisable(true);
        LatarBelakang.jalankan(() -> {
            List<String> galat = skriningManager.simpanSkrining(idKunjungan, isianKeluhan, isianUkuran);
            return new HasilSimpan(galat, galat.isEmpty() ? risikoManager.ambilPenanda(idKunjungan) : List.of());
        }, hasil -> {
            if (!hasil.galat().isEmpty()) {
                // Tidak ada yang disimpan dan isian lain tetap ada (KF06).
                tombolSimpan.setDisable(false);
                tampilkanPeringatanRentang(hasil.galat());
                return;
            }
            tampilkanPenandaRisiko(hasil.penanda());
            kunciFormulir();
            notifikasi.setText("Skrining tersimpan, pasien diteruskan ke antrean poli");
            notifikasi.setVisible(true);
            notifikasi.setManaged(true);
        }, e -> {
            tombolSimpan.setDisable(false);
            tampilkanGalat(e);
        });
    }

    public void tampilkanPeringatanRentang(List<String> kolom) {
        for (String nama : kolom) {
            PenandaKolom.tandai(isian(nama), pesan(nama), RENTANG.get(nama));
        }
        if (!kolom.isEmpty()) {
            isian(kolom.get(0)).requestFocus();
        }
    }

    public void tampilkanPenandaRisiko(List<PenandaRisikoEntity> penanda) {
        panelPenanda.getChildren().clear();
        for (PenandaRisikoEntity p : penanda) {
            PanelPeringatan panel = new PanelPeringatan();
            panel.setPesan("Risiko " + p.getJenisRisiko() + " terdeteksi");
            panelPenanda.getChildren().add(panel);
        }
        panelPenanda.setVisible(!penanda.isEmpty());
        panelPenanda.setManaged(!penanda.isEmpty());
    }

    // Teks kosong atau bukan angka dianggap tidak terbaca, kecuali gula darah yang kosong berarti tidak diukur.
    private void bacaKolom(String kolom, TandaVitalEntity tv, List<String> tidakTerbaca) {
        String teks = isian(kolom).getText() == null ? "" : isian(kolom).getText().trim().replace(',', '.');
        if (teks.isEmpty()) {
            if (!"gulaDarah".equals(kolom)) {
                tidakTerbaca.add(kolom);
            }
            return;
        }
        double nilai;
        try {
            nilai = Double.parseDouble(teks);
        } catch (NumberFormatException e) {
            tidakTerbaca.add(kolom);
            return;
        }
        if (Double.isNaN(nilai) || Double.isInfinite(nilai) || (BULAT.contains(kolom) && nilai != Math.rint(nilai))) {
            tidakTerbaca.add(kolom);
            return;
        }
        switch (kolom) {
            case "sistolik" -> tv.setSistolik((int) nilai);
            case "diastolik" -> tv.setDiastolik((int) nilai);
            case "beratBadan" -> tv.setBeratBadan(nilai);
            case "tinggiBadan" -> tv.setTinggiBadan(nilai);
            case "suhu" -> tv.setSuhu(nilai);
            case "nadi" -> tv.setNadi((int) nilai);
            default -> tv.setGulaDarah((int) nilai);
        }
    }

    private void kunciFormulir() {
        kolomKeluhan.setEditable(false);
        for (String kolom : KOLOM) {
            isian(kolom).setEditable(false);
        }
        tombolSimpan.setDisable(true);
    }

    private TextField isian(String kolom) {
        return switch (kolom) {
            case "sistolik" -> kolomSistolik;
            case "diastolik" -> kolomDiastolik;
            case "beratBadan" -> kolomBeratBadan;
            case "tinggiBadan" -> kolomTinggiBadan;
            case "suhu" -> kolomSuhu;
            case "nadi" -> kolomNadi;
            default -> kolomGulaDarah;
        };
    }

    private Label pesan(String kolom) {
        return switch (kolom) {
            case "sistolik" -> pesanSistolik;
            case "diastolik" -> pesanDiastolik;
            case "beratBadan" -> pesanBeratBadan;
            case "tinggiBadan" -> pesanTinggiBadan;
            case "suhu" -> pesanSuhu;
            case "nadi" -> pesanNadi;
            default -> pesanGulaDarah;
        };
    }

    @FXML
    private void batal() {
        Navigasi.kembali();
    }

    private void tampilkanGalat(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
        alert.setHeaderText("Skrining tidak dapat diproses");
        alert.show();
    }
}
