package sehati;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import sehati.business.control.LaporanManager;
import sehati.business.entity.LaporanEntity;
import sehati.presentation.LaporanForm;
import sehati.presentation.Navigasi;
import sehati.presentation.SinkronisasiForm;

/**
 * Mengambil tangkapan layar satu form untuk bukti pengujian use case (HIUPL Subbab 4.4 dan 4.5). Berada di folder test.
 * Pemakaian dari folder {@code src}:
 *
 * <pre>
 * mvn test-compile exec:java -Dexec.classpathScope=test -Dexec.mainClass=sehati.TangkapanLayar \
 *     -Dexec.args="laporan-normal /tmp/out.png"
 * </pre>
 */
public final class TangkapanLayar {

    private TangkapanLayar() {
    }

    public static void main(String[] args) {
        Application.launch(Aplikasi.class, args);
    }

    public static final class Aplikasi extends Application {

        private Stage stage;
        private String mode;
        private String out;

        @Override
        public void start(Stage stage) {
            this.stage = stage;
            this.mode = getParameters().getRaw().get(0);
            this.out = getParameters().getRaw().get(1);
            Navigasi.mulai(stage);
            Navigasi.mulaiSesi("Pratinjau", "Petugas Administrasi");
            jalankan();
        }

        private void jalankan() {
            switch (mode) {
                case "laporan-normal" -> laporanNormal();
                case "laporan-kosong" -> laporanKosong();
                case "laporan-invalid" -> laporanInvalid();
                case "sinkron-normal" -> sinkronNormal();
                case "sinkron-luring" -> sinkronLuring();
                case "sinkron-tolak" -> sinkronTolak();
                case "pasien-cari" -> pasienCari();
                case "pasien-daftar" -> pasienIsiLaluSimpan("9999010101990777", "Dewi Lestari", 1200);
                case "pasien-nik-ganda" -> pasienIsiLaluSimpan("9999010101750002", "Siti Duplikat", 1200);
                case "pasien-nik-salah" -> pasienIsiLaluSimpan("999901010199077", "Dewi Lestari", 600);
                case "kunjungan" -> kunjungan(getParameters().getRaw().get(2));
                default -> Platform.exit();
            }
        }

        private void laporanNormal() {
            LaporanForm form = Navigasi.buka("LaporanForm");
            LocalDate kini = LocalDate.now();
            LaporanEntity laporan = new LaporanManager().hitungRekapitulasi(kini.minusDays(30), kini, null);
            form.tampilkanRekapitulasi(laporan);
            snapshot();
        }

        private void laporanKosong() {
            LaporanForm form = Navigasi.buka("LaporanForm");
            LocalDate kini = LocalDate.now();
            LaporanEntity laporan = new LaporanManager().hitungRekapitulasi(kini.minusDays(400), kini.minusDays(350), null);
            form.tampilkanRekapitulasi(laporan);
            snapshot();
        }

        private void laporanInvalid() {
            LaporanForm form = Navigasi.buka("LaporanForm");
            LocalDate kini = LocalDate.now();
            try {
                Field mulaiF = LaporanForm.class.getDeclaredField("tanggalMulaiPicker");
                Field akhirF = LaporanForm.class.getDeclaredField("tanggalAkhirPicker");
                mulaiF.setAccessible(true);
                akhirF.setAccessible(true);
                DatePicker mulai = (DatePicker) mulaiF.get(form);
                DatePicker akhir = (DatePicker) akhirF.get(form);
                mulai.setValue(kini);
                akhir.setValue(kini.minusDays(5));
                Method kirim = LaporanForm.class.getDeclaredMethod("kirimRentang");
                kirim.setAccessible(true);
                kirim.invoke(form);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
            snapshot();
        }

        private void sinkronNormal() {
            Navigasi.buka("SinkronisasiForm");
            // initialize() memanggil tampilkanStatus() secara asinkron; tunggu hingga selesai.
            tundaSnap(1800);
        }

        private void sinkronLuring() {
            Navigasi.buka("SinkronisasiForm");
            tundaSnap(1800);
        }

        private void sinkronTolak() {
            Navigasi.buka("SinkronisasiForm");
            tundaSnap(1800);
        }

        // Skenario loket UC02/UC03 memakai sesi Petugas Administrasi sungguhan agar Manager menulis ke basis data uji.
        private void masukAdmin() {
            new sehati.business.control.AutentikasiManager().autentikasi("admin", "sehati123");
            Navigasi.mulaiSesi("admin", sehati.common.SesiAktif.peran());
        }

        private void pasienCari() {
            masukAdmin();
            sehati.presentation.PasienForm form = Navigasi.buka("PasienForm");
            ((javafx.scene.control.TextField) ambil(form, "kolomPencarian")).setText("Ami");
            form.kirimPencarian("Ami");
            tundaSnap(1500);
        }

        private void pasienIsiLaluSimpan(String nik, String nama, double tunda) {
            masukAdmin();
            sehati.presentation.PasienForm form = Navigasi.buka("PasienForm");
            form.bukaFormulir(null);
            ((javafx.scene.control.TextField) ambil(form, "kolomNik")).setText(nik);
            ((javafx.scene.control.TextField) ambil(form, "kolomNama")).setText(nama);
            ((DatePicker) ambil(form, "kolomTanggalLahir")).setValue(LocalDate.of(1999, 4, 17));
            @SuppressWarnings("unchecked")
            javafx.scene.control.ComboBox<String> jk = (javafx.scene.control.ComboBox<String>) ambil(form, "kolomJenisKelamin");
            jk.setValue("Perempuan");
            ((javafx.scene.control.TextArea) ambil(form, "kolomAlamat")).setText("Jl. Cempaka No. 7");
            ((javafx.scene.control.TextField) ambil(form, "kolomTelepon")).setText("081234567890");
            panggil(form, "kirimDataPasien");
            tundaSnap(tunda);
        }

        private void kunjungan(String nomorRM) {
            masukAdmin();
            sehati.presentation.KunjunganForm form = Navigasi.buka("KunjunganForm");
            sehati.business.entity.PasienEntity pasien = new sehati.business.control.PasienManager().cariPasien(nomorRM).get(0);
            form.buka(pasien);
            PauseTransition pilih = new PauseTransition(Duration.millis(1200));
            pilih.setOnFinished(e -> {
                @SuppressWarnings("unchecked")
                javafx.scene.control.ComboBox<sehati.business.entity.PoliEntity> poli =
                        (javafx.scene.control.ComboBox<sehati.business.entity.PoliEntity>) ambil(form, "pilihanPoli");
                poli.getSelectionModel().selectFirst();
                form.kirimPermintaanKunjungan();
                tundaSnap(1500);
            });
            pilih.play();
        }

        private static Object ambil(Object form, String nama) {
            try {
                Field f = form.getClass().getDeclaredField(nama);
                f.setAccessible(true);
                return f.get(form);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        private static void panggil(Object form, String nama) {
            try {
                Method m = form.getClass().getDeclaredMethod(nama);
                m.setAccessible(true);
                m.invoke(form);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }

        private void tundaSnap(double milidetik) {
            PauseTransition tunda = new PauseTransition(Duration.millis(milidetik));
            tunda.setOnFinished(e -> snapshot());
            tunda.play();
        }

        private static BufferedImage keBuffered(WritableImage gambar) {
            int lebar = (int) gambar.getWidth();
            int tinggi = (int) gambar.getHeight();
            BufferedImage hasil = new BufferedImage(lebar, tinggi, BufferedImage.TYPE_INT_ARGB);
            PixelReader baca = gambar.getPixelReader();
            for (int y = 0; y < tinggi; y++) {
                for (int x = 0; x < lebar; x++) {
                    hasil.setRGB(x, y, baca.getArgb(x, y));
                }
            }
            return hasil;
        }

        private void snapshot() {
            PauseTransition tunda = new PauseTransition(Duration.millis(600));
            tunda.setOnFinished(e -> {
                try {
                    Scene scene = stage.getScene();
                    WritableImage gambar = scene.snapshot(null);
                    File tujuan = new File(out);
                    if (tujuan.getParentFile() != null) {
                        tujuan.getParentFile().mkdirs();
                    }
                    ImageIO.write(keBuffered(gambar), "png", tujuan);
                    System.out.println("Tersimpan: " + tujuan.getAbsolutePath());
                } catch (Exception ex) {
                    System.err.println("Gagal menyimpan: " + ex.getMessage());
                } finally {
                    Platform.exit();
                }
            });
            tunda.play();
        }
    }
}
