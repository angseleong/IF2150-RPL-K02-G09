package sehati.presentation;

import java.util.List;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 * Layar bantuan F1 berisi Tabel 6.1 DPPL (KF24, KNF12).
 */
final class TabelPintasan {

    private static final String[] KEPALA = {"Pintasan", "Tindakan", "Layar"};

    private static final List<String[]> BARIS = List.of(
            new String[] {"Tab / Shift+Tab", "Pindah ke kolom berikutnya atau sebelumnya", "Seluruh layar"},
            new String[] {"Ctrl+S", "Menyimpan atau mengirim formulir aktif", "I-02, I-05, I-07, I-11, I-12, I-15"},
            new String[] {"Ctrl+F", "Memfokuskan kolom pencarian", "I-02"},
            new String[] {"Ctrl+N", "Membuka formulir baru", "I-02, I-15"},
            new String[] {"F2", "Panggil Berikutnya", "I-04"},
            new String[] {"F3", "Lewati pasien yang tidak hadir", "I-04"},
            new String[] {"Ctrl+Enter", "Konfirmasi Obat Diserahkan", "I-10"},
            new String[] {"Esc", "Batal atau kembali ke layar sebelumnya", "Seluruh layar"},
            new String[] {"Ctrl+D", "Kembali ke dasbor", "Seluruh layar setelah masuk"},
            new String[] {"F1", "Membuka layar bantuan berisi daftar pintasan", "Seluruh layar"});

    private TabelPintasan() {
    }

    static void tampilkan(Scene pemilik) {
        GridPane tabel = new GridPane();
        tabel.getStyleClass().add("tabel-pintasan");
        for (int kolom = 0; kolom < KEPALA.length; kolom++) {
            Label kepala = new Label(KEPALA[kolom]);
            kepala.getStyleClass().add("judul-panel");
            tabel.add(kepala, kolom, 0);
            for (int baris = 0; baris < BARIS.size(); baris++) {
                tabel.add(new Label(BARIS.get(baris)[kolom]), kolom, baris + 1);
            }
        }

        // Tombol berjenis CANCEL_CLOSE agar Esc menutup bantuan tanpa ikut mengembalikan layar di belakangnya.
        Alert bantuan = new Alert(Alert.AlertType.NONE, "",
                new ButtonType("Tutup", ButtonBar.ButtonData.CANCEL_CLOSE));
        bantuan.initOwner(pemilik.getWindow());
        bantuan.setTitle("Bantuan");
        bantuan.setHeaderText("Pintasan Papan Ketik");
        bantuan.getDialogPane().setContent(tabel);
        bantuan.getDialogPane().getStylesheets().addAll(pemilik.getStylesheets());
        bantuan.showAndWait();
    }
}
