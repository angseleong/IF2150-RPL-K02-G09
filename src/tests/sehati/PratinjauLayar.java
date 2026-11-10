package sehati;

import javafx.application.Application;
import javafx.stage.Stage;
import sehati.business.control.AutentikasiManager;
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
            String layar = getParameters().getRaw().get(0);
            Navigasi.mulai(stage);
            // Navigasi menolak peran yang tidak berhak, jadi pakai peran pertama yang boleh membuka layar ini.
            AutentikasiManager autentikasi = new AutentikasiManager();
            String peran = java.util.stream.Stream.of("Petugas Administrasi", "Perawat", "Dokter", "Petugas Farmasi")
                    .filter(p -> autentikasi.periksaHakAkses(p, layar)).findFirst().orElse("Pemilik Layar");
            Navigasi.mulaiSesi("Pratinjau", peran);
            Navigasi.buka(layar);
        }
    }
}
