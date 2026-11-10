package sehati.presentation;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

/**
 * Bilah aplikasi yang tampil di atas layar selama sesi aktif, yaitu objek pertama pada tabel spesifikasi BAB 6 DPPL
 * (KF22).
 */
final class BilahAplikasi extends HBox {

    private static final PseudoClass DARING = PseudoClass.getPseudoClass("daring");

    private final Button kembali = new Button("Kembali");
    private final Label judul = new Label();
    private final Label pengguna = new Label();
    private final Label peran = new Label();
    private final Label koneksi = new Label();
    private final HBox indikator;
    private final Circle lampu = new Circle(4);

    BilahAplikasi(Runnable aksiKembali, Runnable aksiKeluar) {
        getStyleClass().add("bilah");
        setAlignment(Pos.CENTER_LEFT);

        Label namaAplikasi = new Label("SEHATI");
        namaAplikasi.getStyleClass().add("bilah-logo");
        HBox merek = new HBox(8, LogoSehati.tanda(28), namaAplikasi);
        merek.setAlignment(Pos.CENTER_LEFT);

        Region pemisah = new Region();
        pemisah.getStyleClass().add("bilah-pemisah");

        SVGPath panah = new SVGPath();
        panah.setContent("M9 2 L3 8 L9 14");
        panah.getStyleClass().add("ikon-kembali");
        kembali.setGraphic(panah);
        kembali.getStyleClass().add("tombol-kembali");
        kembali.setTooltip(new Tooltip("Kembali ke layar sebelumnya (Esc)"));
        kembali.setOnAction(e -> aksiKembali.run());
        kembali.managedProperty().bind(kembali.visibleProperty());

        judul.getStyleClass().add("bilah-judul");

        Region pengisi = new Region();
        HBox.setHgrow(pengisi, Priority.ALWAYS);

        lampu.getStyleClass().add("lampu-koneksi");
        koneksi.getStyleClass().add("teks-koneksi");
        indikator = new HBox(6, lampu, koneksi);
        indikator.getStyleClass().add("indikator-koneksi");
        indikator.setAlignment(Pos.CENTER);

        pengguna.getStyleClass().add("bilah-pengguna");
        peran.getStyleClass().add("bilah-peran");
        VBox akun = new VBox(0, pengguna, peran);
        akun.setAlignment(Pos.CENTER_RIGHT);

        Button keluar = new Button("Keluar");
        keluar.getStyleClass().add("tombol-bilah");
        keluar.setOnAction(e -> aksiKeluar.run());

        getChildren().addAll(merek, pemisah, kembali, judul, pengisi, indikator, akun, keluar);
        // Pelayanan tetap berjalan tanpa internet, jadi Luring adalah keadaan awal yang aman (KNF09).
        setDaring(false);
    }

    void setJudul(String teks) {
        judul.setText(teks);
    }

    void setBisaKembali(boolean bisa) {
        kembali.setVisible(bisa);
    }

    void setPengguna(String namaPengguna, String peranPengguna) {
        pengguna.setText(namaPengguna);
        peran.setText(peranPengguna);
    }

    void setDaring(boolean daring) {
        koneksi.setText(daring ? "Daring" : "Luring");
        indikator.pseudoClassStateChanged(DARING, daring);
        lampu.pseudoClassStateChanged(DARING, daring);
    }
}
