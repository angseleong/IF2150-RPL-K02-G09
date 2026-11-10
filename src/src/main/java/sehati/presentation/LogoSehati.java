package sehati.presentation;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * Tanda logo SEHATI (kotak teal, hati, dan garis EKG) digambar dari bentuk yang sama dengan
 * docs/M8/assets/logo-sehati.svg, sehingga tidak perlu berkas gambar terpisah.
 */
final class LogoSehati {

    private static final Color TEAL = Color.web("#0F766E");

    private LogoSehati() {
    }

    /** Tanda logo berukuran {@code ukuran} x {@code ukuran} piksel. */
    static Node tanda(double ukuran) {
        Rectangle kotak = new Rectangle(10, 10, 140, 140);
        kotak.setArcWidth(64);
        kotak.setArcHeight(64);
        kotak.setFill(TEAL);

        SVGPath hati = new SVGPath();
        hati.setContent("M80 122 L38 80 A23 23 0 0 1 80 48 A23 23 0 0 1 122 80 Z");
        hati.setFill(Color.WHITE);

        Polyline ekg = new Polyline(30, 84, 56, 84, 66, 66, 78, 102, 90, 58, 100, 84, 130, 84);
        ekg.setFill(null);
        ekg.setStroke(TEAL);
        ekg.setStrokeWidth(7);
        ekg.setStrokeLineCap(StrokeLineCap.ROUND);
        ekg.setStrokeLineJoin(StrokeLineJoin.ROUND);

        Group gambar = new Group(kotak, hati, ekg);
        double skala = ukuran / 140;
        gambar.setScaleX(skala);
        gambar.setScaleY(skala);
        // Group diskalakan dari pusatnya, jadi dibungkus agar ukuran tata letaknya ikut mengecil.
        Group tanda = new Group(gambar);
        tanda.setAccessibleText("Logo SEHATI");
        return tanda;
    }

    /** Ikon jendela. Harus dipanggil di thread JavaFX. */
    static Image ikon(double ukuran) {
        SnapshotParameters latar = new SnapshotParameters();
        latar.setFill(Color.TRANSPARENT);
        return tanda(ukuran).snapshot(latar, null);
    }
}
