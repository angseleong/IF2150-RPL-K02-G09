package sehati;

import javafx.application.Application;
import javafx.stage.Stage;
import sehati.presentation.Navigasi;

/**
 * Membuka satu layar tanpa masuk bagi pemilik layar. Berada di folder test agar tidak ikut ke build utama. Dari folder
 * {@code src}:
 *
 * <pre>
 * mvn test-compile exec:java -Dexec.classpathScope=test \
 *     -Dexec.mainClass=sehati.PratinjauLayar -Dexec.args=PasienForm
 * </pre>
 */
public final class PratinjauLayar {

    private PratinjauLayar() {
    }

    public static void main(String[] args) {
        Application.launch(Aplikasi.class, args);
    }

    public static final class Aplikasi extends Application {

        @Override
        public void start(Stage stage) {
            Navigasi.mulai(stage);
            Navigasi.mulaiSesi("Pratinjau", "Pemilik Layar");
            Navigasi.buka(getParameters().getRaw().get(0));
        }
    }
}
