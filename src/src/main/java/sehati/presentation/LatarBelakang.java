package sehati.presentation;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javafx.application.Platform;

/**
 * Menjalankan pekerjaan yang menyentuh basis data di thread latar agar layar tidak membeku (Panduan Kerja Subbab 8),
 * lalu mengembalikan hasilnya ke JavaFX Application Thread.
 */
final class LatarBelakang {

    private LatarBelakang() {
    }

    static <T> void jalankan(Callable<T> pekerjaan, Consumer<T> berhasil, Consumer<Throwable> gagal) {
        Thread t = new Thread(() -> {
            try {
                T hasil = pekerjaan.call();
                Platform.runLater(() -> berhasil.accept(hasil));
            } catch (Throwable e) {
                Platform.runLater(() -> gagal.accept(e));
            }
        }, "sehati-latar");
        t.setDaemon(true);
        t.start();
    }
}
