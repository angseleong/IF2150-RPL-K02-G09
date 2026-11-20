package sehati;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import sehati.business.control.AutentikasiManager;
import sehati.presentation.AntreanSkriningForm;
import sehati.presentation.DaftarPantauForm;
import sehati.presentation.Navigasi;

/**
 * Pemandu uji layar UC04, UC09, KNF08, dan KNF12 (HIUPL Subbab 4.4 dan 4.5). Berada di folder test agar tidak ikut ke
 * build utama. Skrip membuka {@code Navigasi} dan layar FXML asli dengan akun {@code perawat}, lalu mengirim
 * peristiwa papan ketik ke kontrol yang sedang fokus, memotret jendela 1280 x 800 piksel, dan membaca isi basis data
 * lewat koneksi terpisah. Seed dipulihkan sesudah tiap skenario. Tidak ada peristiwa tetikus yang dikirim, dan
 * jumlah peristiwa tetikus yang sampai ke jendela dihitung dan dicetak. Dari folder {@code src}, dengan basis data
 * uji menyala dan {@code DB_UJI_URL} terisi:
 *
 * <pre>
 * java --module-path &lt;javafx&gt; --add-modules javafx.controls,javafx.fxml \
 *     -cp target/classes:target/test-classes:&lt;dependensi&gt; sehati.PemanduLayarSkrining &lt;folder-keluaran&gt;
 * </pre>
 */
public final class PemanduLayarSkrining {

    private PemanduLayarSkrining() {
    }

    public static void main(String[] args) {
        Application.launch(Aplikasi.class, args);
    }

    public static final class Aplikasi extends Application {

        private Stage panggung;
        private Path keluar;
        private FiksturSkrining fiksur;
        private final AtomicInteger peristiwaTetikus = new AtomicInteger();
        private final List<String> catatan = new ArrayList<>();

        @Override
        public void start(Stage stage) throws Exception {
            panggung = stage;
            keluar = Path.of(getParameters().getRaw().get(0));
            Files.createDirectories(keluar);
            fiksur = new FiksturSkrining();
            Navigasi.mulai(stage);
            stage.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, e -> peristiwaTetikus.incrementAndGet());
            stage.getScene().addEventFilter(MouseEvent.MOUSE_CLICKED, e -> peristiwaTetikus.incrementAndGet());
            Thread t = new Thread(this::jalankan, "pemandu-layar");
            t.setDaemon(false);
            t.start();
        }

        private void jalankan() {
            try {
                Thread.sleep(1500);
                masukSebagaiPerawat();
                if (getParameters().getRaw().size() > 1 && "bantuan".equals(getParameters().getRaw().get(1))) {
                    knf12Bantuan();
                    return;
                }
                if (getParameters().getRaw().size() > 1 && "uc09".equals(getParameters().getRaw().get(1))) {
                    uc09Normal();
                    uc09TidakTerhubung();
                    uc09KosongDanSaringan();
                    return;
                }
                uc04Normal();
                uc04Lewati();
                uc04DiLuarRentang();
                uc04Risiko();
                knf08();
                knf12Bantuan();
                uc09Normal();
                uc09TidakTerhubung();
                uc09KosongDanSaringan();
                catat("Peristiwa tetikus yang sampai ke jendela selama seluruh skenario: " + peristiwaTetikus.get());
            } catch (Throwable e) {
                catat("GAGAL: " + e);
                e.printStackTrace();
            } finally {
                try {
                    Files.write(keluar.resolve("catatan-pemandu.txt"), catatan, StandardCharsets.UTF_8);
                    fiksur.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.exit();
            }
        }

        // ---------------------------------------------------------------- UC04

        private void uc04Normal() throws Exception {
            bukaAntrean();
            foto("uc04-tc01-1-antrean");
            catat("UC04-TC01 antrean awal (id kunjungan): " + antrean());

            tekan(KeyCode.F2, false);
            Thread.sleep(2000);
            foto("uc04-tc01-2-formulir-dipanggil");
            catat("UC04-TC01 status kunjungan 3 sesudah F2: " + status(3));

            isiFormulir("Pusing ringan dua hari", "118", "78", "62", "165", "36.6", "70", "98");
            foto("uc04-tc01-3-terisi");
            tekan(KeyCode.S, true);
            Thread.sleep(2500);
            foto("uc04-tc01-4-tersimpan");
            catat("UC04-TC01 status kunjungan 3 sesudah Ctrl+S: " + status(3) + ", skrining kunjungan 3: "
                    + FiksturSkrining.angka("SELECT count(*) FROM skrining WHERE id_kunjungan = 3")
                    + ", penanda: " + FiksturSkrining.angka("SELECT count(*) FROM penanda_risiko WHERE id_kunjungan = 3")
                    + ", antrean skrining: " + antrean());
            pulihkan();
        }

        private void uc04Lewati() throws Exception {
            bukaAntrean();
            tekan(KeyCode.F2, false);
            Thread.sleep(2000);
            catat("UC04-TC02 status kunjungan 3 sesudah dipanggil: " + status(3));
            fx(() -> {
                Navigasi.kembali();
                return null;
            });
            Thread.sleep(2500);
            foto("uc04-tc02-1-sedang-diskrining");
            tekan(KeyCode.F3, false);
            Thread.sleep(2500);
            foto("uc04-tc02-2-dilewati");
            catat("UC04-TC02 status kunjungan 3 sesudah F3: " + status(3) + ", urutan antrean: " + antrean()
                    + ", status kunjungan 2: " + status(2));
            pulihkan();
        }

        private void uc04DiLuarRentang() throws Exception {
            bukaAntrean();
            tekan(KeyCode.F2, false);
            Thread.sleep(2000);
            isiFormulir("Demam", "400", "80", "62", "165", "72", "70", "");
            long sebelum = FiksturSkrining.angka("SELECT count(*) FROM skrining");
            tekan(KeyCode.S, true);
            Thread.sleep(2500);
            foto("uc04-tc03-1-di-luar-rentang");
            catat("UC04-TC03 skrining tersimpan setelah nilai salah: "
                    + (FiksturSkrining.angka("SELECT count(*) FROM skrining") - sebelum)
                    + ", status kunjungan 3: " + status(3));
            perbaiki("kolomSistolik", "120");
            perbaiki("kolomSuhu", "36.6");
            foto("uc04-tc03-2-dikoreksi");
            tekan(KeyCode.S, true);
            Thread.sleep(2500);
            foto("uc04-tc03-3-tersimpan");
            catat("UC04-TC03 sesudah koreksi: status kunjungan 3 " + status(3) + ", skrining tersimpan: "
                    + (FiksturSkrining.angka("SELECT count(*) FROM skrining") - sebelum));
            pulihkan();
        }

        private void uc04Risiko() throws Exception {
            bukaAntrean();
            tekan(KeyCode.F2, false);
            Thread.sleep(2000);
            isiFormulir("Sering haus dan lemas", "152", "96", "90", "165", "36.8", "82", "210");
            tekan(KeyCode.S, true);
            Thread.sleep(2500);
            foto("uc04-tc04-1-risiko");
            catat("UC04-TC04 penanda kunjungan 3: " + FiksturSkrining.teks(
                    "SELECT string_agg(jenis_risiko, ', ' ORDER BY jenis_risiko) FROM penanda_risiko "
                            + "WHERE id_kunjungan = 3")
                    + "; entri daftar pantau baru: " + FiksturSkrining.angka(
                    "SELECT count(*) FROM daftar_pantau WHERE id_pasien = 3 AND id_penanda IS NOT NULL")
                    + "; status kunjungan 3: " + status(3));
            pulihkan();
        }

        // ---------------------------------------------------------------- KNF08 dan KNF12

        private void knf08() throws Exception {
            String[][] kasus = {
                {"hipertensi", "152", "62", "36.6", ""},
                {"obesitas", "118", "90", "36.6", ""},
                {"indikasi-diabetes", "118", "62", "36.6", "210"},
            };
            for (String[] k : kasus) {
                bukaAntrean();
                tekan(KeyCode.F2, false);
                Thread.sleep(2000);
                isiFormulir("Uji KNF08", k[1], "78", k[2], "165", k[3], "70", k[4]);
                int sebelum = peristiwaTetikus.get();
                tekan(KeyCode.S, true);
                Thread.sleep(2500);
                foto("knf08-tc01-" + k[0]);
                boolean panelTampil = fx(() -> cari("#panelPenanda") != null && cari("#panelPenanda").isVisible());
                catat("KNF08-TC01 kasus " + k[0] + ": panel penanda tampil=" + panelTampil + ", penanda tersimpan="
                        + FiksturSkrining.teks("SELECT string_agg(jenis_risiko, ', ') FROM penanda_risiko "
                                + "WHERE id_kunjungan = 3")
                        + ", peristiwa tetikus sesudah Simpan=" + (peristiwaTetikus.get() - sebelum));
                pulihkan();
            }
        }

        private void knf12Bantuan() throws Exception {
            bukaAntrean();
            // Layar bantuan dibuka dengan showAndWait, sehingga peristiwanya dikirim tanpa ditunggu selesai.
            Platform.runLater(() -> {
                Scene scene = panggung.getScene();
                Node sasaran = scene.getFocusOwner() != null ? scene.getFocusOwner() : scene.getRoot();
                Event.fireEvent(sasaran, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1, false, false,
                        false, false));
            });
            Thread.sleep(1500);
            fotoJendelaLain("knf12-tc01-bantuan-f1");
            Platform.runLater(() -> {
                for (javafx.stage.Window w : new ArrayList<>(javafx.stage.Window.getWindows())) {
                    if (w != panggung) {
                        w.hide();
                    }
                }
            });
            Thread.sleep(800);
            catat("KNF12-TC01 layar bantuan F1 dibuka lewat papan ketik");
        }

        // ---------------------------------------------------------------- UC09

        private void uc09Normal() throws Exception {
            fx(() -> {
                Navigasi.keDasbor();
                return null;
            });
            DaftarPantauForm form = fx(() -> Navigasi.<DaftarPantauForm>buka("DaftarPantauForm"));
            Thread.sleep(2500);
            foto("uc09-tc01-1-daftar");
            pilih("#pilihJenisRisiko", "hipertensi");
            tekanTombol("Terapkan", 0);
            Thread.sleep(2000);
            foto("uc09-tc01-2-disaring");
            tekanTombol("Tindak Lanjut", 0);
            Thread.sleep(600);
            pilih("#pilihJenisKontak", "telepon");
            pilih("#pilihHasilUpaya", "terhubung");
            pilih("#pilihStatusKedatangan", "akan datang");
            foto("uc09-tc01-3-formulir");
            tekan(KeyCode.S, true);
            Thread.sleep(2500);
            foto("uc09-tc01-4-tersimpan");
            catat("UC09-TC01 status entri 2: " + FiksturSkrining.teks(
                    "SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_entri = 2")
                    + ", tindak lanjut: " + FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 2")
                    + ", catatan rekam medis baru: "
                    + FiksturSkrining.teks("SELECT isi FROM catatan_rekam_medis ORDER BY id_catatan DESC LIMIT 1")
                    + ", audit: " + FiksturSkrining.teks(
                    "SELECT deskripsi FROM audit ORDER BY id_audit DESC LIMIT 1"));
            pulihkan();
        }

        private void uc09TidakTerhubung() throws Exception {
            fx(() -> {
                Navigasi.keDasbor();
                return null;
            });
            DaftarPantauForm form = fx(() -> Navigasi.<DaftarPantauForm>buka("DaftarPantauForm"));
            Thread.sleep(2500);
            tekanTombol("Tindak Lanjut", 0);
            Thread.sleep(600);
            pilih("#pilihJenisKontak", "telepon");
            pilih("#pilihHasilUpaya", "tidak terhubung");
            pilih("#pilihStatusKedatangan", "belum diketahui");
            foto("uc09-tc02-1-formulir");
            tekan(KeyCode.S, true);
            Thread.sleep(2500);
            foto("uc09-tc02-2-tersimpan");
            catat("UC09-TC02 status entri 1: " + FiksturSkrining.teks(
                    "SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_entri = 1")
                    + ", tindak lanjut: " + FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 1"));
            pulihkan();
        }

        private void uc09KosongDanSaringan() throws Exception {
            fx(() -> {
                Navigasi.keDasbor();
                return null;
            });
            DaftarPantauForm form = fx(() -> Navigasi.<DaftarPantauForm>buka("DaftarPantauForm"));
            Thread.sleep(2500);
            pilih("#pilihJenisRisiko", "obesitas");
            tekanTombol("Terapkan", 0);
            Thread.sleep(2000);
            foto("uc09-tc03-1-kosong");
            catat("UC09-TC03 penyaring obesitas pada seed: tabel kosong dengan keterangan tampil");
        }

        // ---------------------------------------------------------------- bantuan

        private void masukSebagaiPerawat() throws Exception {
            if (new AutentikasiManager().autentikasi("perawat", "sehati123") == null) {
                throw new IllegalStateException("Akun perawat tidak dapat masuk");
            }
            fx(() -> {
                Navigasi.mulaiSesi("perawat", "Perawat");
                return null;
            });
            Thread.sleep(800);
        }

        private void bukaAntrean() throws Exception {
            fx(() -> {
                Navigasi.keDasbor();
                return null;
            });
            fx(() -> Navigasi.<AntreanSkriningForm>buka("AntreanSkriningForm"));
            Thread.sleep(2500);
        }

        private void isiFormulir(String keluhan, String sistolik, String diastolik, String berat, String tinggi,
                String suhu, String nadi, String gula) throws Exception {
            ketik(keluhan);
            for (String nilai : new String[] {sistolik, diastolik, berat, tinggi, suhu, nadi, gula}) {
                tekan(KeyCode.TAB, false);
                ketik(nilai);
            }
            Thread.sleep(300);
        }

        // Mengganti isi satu kolom yang sedang salah: kolom dikosongkan, lalu nilai baru diketik lewat papan ketik.
        private void perbaiki(String idKolom, String nilai) throws Exception {
            fx(() -> {
                Node kolom = cari("#" + idKolom);
                ((TextInputControl) kolom).clear();
                kolom.requestFocus();
                return null;
            });
            ketik(nilai);
        }

        private void pilih(String selektor, String nilai) throws Exception {
            fx(() -> {
                @SuppressWarnings("unchecked")
                ComboBox<String> kombo = (ComboBox<String>) cari(selektor);
                kombo.getSelectionModel().select(nilai);
                return null;
            });
        }

        // Menekan tombol (fire) yang ke-urutan bertuliskan teks itu, setara dengan klik pada kontrolnya.
        private void tekanTombol(String teks, int urutan) throws Exception {
            fx(() -> {
                List<javafx.scene.control.Button> cocok = panggung.getScene().getRoot().lookupAll(".button").stream()
                        .filter(n -> n instanceof javafx.scene.control.Button b && teks.equals(b.getText()))
                        .map(n -> (javafx.scene.control.Button) n).toList();
                cocok.get(urutan).fire();
                return null;
            });
        }

        private Node cari(String selektor) {
            return panggung.getScene().getRoot().lookup(selektor);
        }

        private void tekan(KeyCode kode, boolean ctrl) throws Exception {
            fx(() -> {
                Scene scene = panggung.getScene();
                Node sasaran = scene.getFocusOwner() != null ? scene.getFocusOwner() : scene.getRoot();
                Event.fireEvent(sasaran, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", kode, false, ctrl, false, false));
                return null;
            });
            Thread.sleep(150);
        }

        private void ketik(String teks) throws Exception {
            for (char c : teks.toCharArray()) {
                fx(() -> {
                    Scene scene = panggung.getScene();
                    Node sasaran = scene.getFocusOwner() != null ? scene.getFocusOwner() : scene.getRoot();
                    Event.fireEvent(sasaran, new KeyEvent(KeyEvent.KEY_TYPED, String.valueOf(c), "",
                            KeyCode.UNDEFINED, false, false, false, false));
                    return null;
                });
            }
            Thread.sleep(100);
        }

        // Jendela bantuan (F1) adalah jendela terpisah, jadi yang dipotret adalah scene jendela itu.
        private void fotoJendelaLain(String nama) throws Exception {
            Scene scene = fx(() -> javafx.stage.Window.getWindows().stream()
                    .filter(w -> w != panggung && w.isShowing() && w.getScene() != null)
                    .map(javafx.stage.Window::getScene).findFirst().orElse(null));
            if (scene == null) {
                throw new IllegalStateException("Jendela bantuan tidak tampil");
            }
            simpanFoto(nama, scene);
        }

        private void foto(String nama) throws Exception {
            simpanFoto(nama, panggung.getScene());
        }

        private void simpanFoto(String nama, Scene sumber) throws Exception {
            BufferedImage gambar = fx(() -> {
                WritableImage img = sumber.snapshot(null);
                PixelReader pembaca = img.getPixelReader();
                BufferedImage bi = new BufferedImage((int) img.getWidth(), (int) img.getHeight(),
                        BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < bi.getHeight(); y++) {
                    for (int x = 0; x < bi.getWidth(); x++) {
                        bi.setRGB(x, y, pembaca.getArgb(x, y));
                    }
                }
                return bi;
            });
            javax.imageio.ImageIO.write(gambar, "png", new File(keluar.toFile(), nama + ".png"));
            catat("Foto " + nama + ".png (" + gambar.getWidth() + " x " + gambar.getHeight() + ")");
        }

        private <T> T fx(Callable<T> kerja) throws Exception {
            FutureTask<T> tugas = new FutureTask<>(kerja);
            Platform.runLater(tugas);
            return tugas.get();
        }

        private void pulihkan() throws Exception {
            Thread.sleep(400);
            fiksur.pulihkan();
        }

        private static String status(int idKunjungan) throws Exception {
            return FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = ?", idKunjungan);
        }

        private static String antrean() throws Exception {
            return FiksturSkrining.teks("SELECT string_agg(e.id_entri::text, ',' ORDER BY e.urutan) "
                    + "FROM antrean_entri e JOIN antrean a USING (id_antrean) WHERE a.jenis = 'skrining'");
        }

        private void catat(String baris) {
            System.out.println(baris);
            catatan.add(baris);
        }
    }
}
