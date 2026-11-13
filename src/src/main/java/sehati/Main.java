package sehati;

import javafx.application.Application;
import javafx.stage.Stage;
import sehati.business.control.KonfigurasiManager;
import sehati.presentation.Navigasi;

/**
 * Titik masuk aplikasi SEHATI.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        KonfigurasiManager.jadwalkanPencadangan();
        Navigasi.mulai(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
