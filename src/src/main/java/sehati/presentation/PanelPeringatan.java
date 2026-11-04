package sehati.presentation;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;

/**
 * Peringatan risiko, stok, dan pencadangan: merah dengan ikon segitiga tanda seru, tidak hanya warna, agar tetap
 * terbaca oleh pengguna yang sulit membedakan warna (KNF08). Pada FXML: {@code <PanelPeringatan pesan="..."/>}.
 */
public class PanelPeringatan extends HBox {

    private static final String IKON_SEGITIGA = "M12 2 L23 21 H1 Z M11 9 H13 V15 H11 Z M11 17 H13 V19 H11 Z";

    private final Label pesan = new Label();

    public PanelPeringatan() {
        getStyleClass().add("panel-peringatan");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        // Panel yang disembunyikan tidak menyisakan ruang kosong pada layar.
        managedProperty().bind(visibleProperty());

        SVGPath ikon = new SVGPath();
        ikon.setContent(IKON_SEGITIGA);
        ikon.setFillRule(FillRule.EVEN_ODD);
        ikon.getStyleClass().add("ikon-peringatan");
        pesan.getStyleClass().add("teks-peringatan");
        pesan.setWrapText(true);
        getChildren().addAll(ikon, pesan);
    }

    public String getPesan() {
        return pesan.getText();
    }

    public void setPesan(String teks) {
        pesan.setText(teks);
    }
}
