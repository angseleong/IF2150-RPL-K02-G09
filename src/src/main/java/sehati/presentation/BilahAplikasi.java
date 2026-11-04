package sehati.presentation;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;

/**
 * Bilah hijau di atas setiap layar selain I-01, yaitu objek pertama pada setiap tabel spesifikasi BAB 6 DPPL (KF22).
 */
final class BilahAplikasi extends HBox {

    private static final PseudoClass DARING = PseudoClass.getPseudoClass("daring");

    private final Label judul = new Label();
    private final Label pengguna = new Label();
    private final Label koneksi = new Label();
    private final Circle lampu = new Circle(6);

    BilahAplikasi(Runnable aksiKeluar) {
        getStyleClass().add("bilah");
        setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("SEHATI");
        logo.getStyleClass().add("bilah-logo");
        judul.getStyleClass().add("bilah-judul");
        pengguna.getStyleClass().add("bilah-teks");
        koneksi.getStyleClass().add("bilah-teks");
        lampu.getStyleClass().add("lampu-koneksi");

        Region pengisi = new Region();
        HBox.setHgrow(pengisi, Priority.ALWAYS);

        Button keluar = new Button("Keluar");
        keluar.getStyleClass().add("tombol-bilah");
        keluar.setOnAction(e -> aksiKeluar.run());

        HBox indikator = new HBox(6, lampu, koneksi);
        indikator.setAlignment(Pos.CENTER);
        getChildren().addAll(logo, judul, pengisi, pengguna, indikator, keluar);
        // Pelayanan tetap berjalan tanpa internet, jadi Luring adalah keadaan awal yang aman (KNF09).
        setDaring(false);
    }

    void setJudul(String teks) {
        judul.setText(teks);
    }

    void setPengguna(String namaPengguna, String peran) {
        pengguna.setText(namaPengguna + " (" + peran + ")");
    }

    void setDaring(boolean daring) {
        koneksi.setText(daring ? "Daring" : "Luring");
        lampu.pseudoClassStateChanged(DARING, daring);
    }
}
