package sehati.presentation;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * Menandai kolom yang isiannya ditolak dengan bingkai merah dan pesan singkat di dekatnya. Isian tidak dihapus agar
 * pengguna hanya memperbaiki yang salah (DPPL pengantar BAB 6).
 */
public final class PenandaKolom {

    private static final String BERMASALAH = "kolom-bermasalah";

    private PenandaKolom() {
    }

    public static void tandai(Node kolom, Label labelPesan, String pesan) {
        if (!kolom.getStyleClass().contains(BERMASALAH)) {
            kolom.getStyleClass().add(BERMASALAH);
        }
        labelPesan.setText(pesan);
        tampilkanLabel(labelPesan, true);
    }

    public static void bersihkan(Node kolom, Label labelPesan) {
        kolom.getStyleClass().remove(BERMASALAH);
        labelPesan.setText("");
        tampilkanLabel(labelPesan, false);
    }

    private static void tampilkanLabel(Label label, boolean tampil) {
        label.setVisible(tampil);
        label.setManaged(tampil);
    }
}
