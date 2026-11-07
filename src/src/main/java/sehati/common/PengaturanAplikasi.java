package sehati.common;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Membaca konfigurasi aplikasi dari berkas {@code .env} dan variabel lingkungan. Daftar kunci ada di
 * {@code .env.example}.
 */
public final class PengaturanAplikasi {

    private static final Logger LOG = Logger.getLogger(PengaturanAplikasi.class.getName());

    private final Map<String, String> nilai;

    public PengaturanAplikasi(Map<String, String> nilai) {
        this.nilai = Map.copyOf(nilai);
    }

    /**
     * Memuat {@code .env} pada direktori kerja, lalu variabel lingkungan menimpa nilai yang sama.
     */
    public static PengaturanAplikasi muat() {
        Map<String, String> hasil = new HashMap<>();
        Path berkas = Path.of(".env");
        if (Files.exists(berkas)) {
            try {
                hasil.putAll(bacaEnv(Files.readAllLines(berkas, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Berkas .env tidak dapat dibaca", e);
            }
        }
        hasil.putAll(System.getenv());
        return new PengaturanAplikasi(hasil);
    }

    /**
     * Mengurai baris {@code KUNCI=nilai}. Baris kosong dan baris berawalan {@code #} dilewati.
     */
    static Map<String, String> bacaEnv(List<String> baris) {
        Map<String, String> hasil = new HashMap<>();
        for (String b : baris) {
            String isi = b.strip();
            int sama = isi.indexOf('=');
            if (isi.isEmpty() || isi.startsWith("#") || sama < 1) {
                continue;
            }
            hasil.put(isi.substring(0, sama).strip(), isi.substring(sama + 1).strip());
        }
        return hasil;
    }

    /**
     * Mengembalikan nilai kunci wajib. Kunci yang tidak ada atau kosong menandakan {@code .env} belum diisi.
     */
    public String ambil(String kunci) {
        String v = nilai.get(kunci);
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Konfigurasi " + kunci + " belum diisi pada .env");
        }
        return v;
    }

    public String ambil(String kunci, String bawaan) {
        String v = nilai.get(kunci);
        return v == null || v.isEmpty() ? bawaan : v;
    }
}
