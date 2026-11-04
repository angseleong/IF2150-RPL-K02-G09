package sehati;

import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;
import sehati.presentation.Navigasi;

/**
 * Titik masuk aplikasi SEHATI.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Navigasi.mulai(stage);
        // TODO(#10): hapus pratinjau tanpa masuk ini begitu layar masuk selesai, karena melewati autentikasi.
        // Pemilik layar: mvn javafx:run -Djavafx.args=PasienForm
        List<String> argumen = getParameters().getRaw();
        if (!argumen.isEmpty()) {
            Navigasi.mulaiSesi("Pratinjau", "Pemilik Layar");
            Navigasi.buka(argumen.get(0));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
