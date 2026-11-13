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
     * Memuat {@code .env} (lihat {@link #cariEnv}), lalu variabel lingkungan menimpa nilai yang sama.
     */
    public static PengaturanAplikasi muat() {
        List<String> baris = List.of();
        Path dirKerja = Path.of("").toAbsolutePath();
        Path berkas = cariEnv(dirKerja);
        if (berkas == null) {
            LOG.warning(() -> "Berkas .env tidak ditemukan di " + dirKerja + " maupun " + dirKerja.resolve("src"));
        } else {
            try {
                baris = Files.readAllLines(berkas, StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Berkas " + berkas + " tidak dapat dibaca", e);
            }
        }
        return gabung(baris, System.getenv());
    }

    /**
     * Mencari {@code .env} di direktori kerja, lalu di {@code src/}. Yang kedua terjadi bila aplikasi dijalankan
     * dari root repo, misalnya lewat IntelliJ. Mengembalikan {@code null} bila keduanya tidak ada.
     */
    static Path cariEnv(Path dirKerja) {
        for (Path p : List.of(dirKerja.resolve(".env"), dirKerja.resolve("src").resolve(".env"))) {
            if (Files.isRegularFile(p)) {
                return p;
            }
        }
        return null;
    }

    static PengaturanAplikasi gabung(List<String> barisEnv, Map<String, String> lingkungan) {
        Map<String, String> hasil = bacaEnv(barisEnv);
        // Variabel kosong dari IDE atau CI tidak boleh menghapus nilai yang sudah diisi di .env.
        lingkungan.forEach((k, v) -> {
            if (!v.isEmpty()) {
                hasil.put(k, v);
            }
        });
        return new PengaturanAplikasi(hasil);
    }

    /**
     * Mengurai baris {@code KUNCI=nilai} dengan aturan yang sama dengan docker-compose: awalan {@code export},
     * nilai berkutip, dan komentar di akhir baris didukung. Baris kosong dan baris berawalan {@code #} dilewati.
     */
    static Map<String, String> bacaEnv(List<String> baris) {
        Map<String, String> hasil = new HashMap<>();
        for (String b : baris) {
            // BOM UTF-8 dari Notepad Windows ikut terbaca sebagai bagian kunci pertama.
            String isi = b.replace("﻿", "").strip();
            if (isi.startsWith("export ")) {
                isi = isi.substring("export ".length()).strip();
            }
            int sama = isi.indexOf('=');
            if (isi.isEmpty() || isi.startsWith("#") || sama < 1) {
                continue;
            }
            hasil.put(isi.substring(0, sama).strip(), nilaiEnv(isi.substring(sama + 1).strip()));
        }
        return hasil;
    }

    private static String nilaiEnv(String v) {
        if (v.length() >= 2 && (v.charAt(0) == '"' || v.charAt(0) == '\'')) {
            int tutup = v.indexOf(v.charAt(0), 1);
            if (tutup > 0) {
                return v.substring(1, tutup);
            }
        }
        return v.split("\\s+#", 2)[0].strip();
    }

    /**
     * Mengembalikan nilai kunci wajib. Kunci yang tidak ada atau kosong menandakan {@code .env} belum diisi.
     */
    public String ambil(String kunci) {
        String v = nilai.get(kunci);
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Konfigurasi " + kunci
                    + " belum diisi. Salin src/.env.example menjadi src/.env lalu isi nilainya.");
        }
        return v;
    }

    public String ambil(String kunci, String bawaan) {
        String v = nilai.get(kunci);
        return v == null || v.isEmpty() ? bawaan : v;
    }
}
