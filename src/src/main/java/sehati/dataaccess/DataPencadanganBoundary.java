package sehati.dataaccess;

import sehati.business.entity.PencadanganEntity;
import sehati.common.PengaturanAplikasi;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C64 - DataPencadanganBoundary.
 *
 * Menjalankan `pg_dump` sesuai jadwal ke media penyimpanan terpisah dan menyimpan PencadanganEntity.
 */
public class DataPencadanganBoundary {

    private static final Logger LOG = Logger.getLogger(DataPencadanganBoundary.class.getName());

    private static final String Q_C64_01 = "SELECT id_pencadangan, waktu, status, lokasi_media, ukuran_berkas "
            + "FROM pencadangan ORDER BY waktu DESC LIMIT 1";
    private static final String Q_C64_02 = "INSERT INTO pencadangan (waktu, status, lokasi_media, ukuran_berkas) "
            + "VALUES (?, ?, ?, ?) RETURNING id_pencadangan";

    private static final DateTimeFormatter NAMA_BERKAS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final long BATAS_MENIT = 30;

    private Connection koneksi;
    private String lokasiMedia;
    private final PengaturanAplikasi pengaturan;

    public DataPencadanganBoundary(Connection koneksi, String lokasiMedia) {
        this(koneksi, lokasiMedia, PengaturanAplikasi.muat());
    }

    // Untuk tes: perintah pg_dump/pg_restore bisa diganti lewat pengaturan
    DataPencadanganBoundary(Connection koneksi, String lokasiMedia, PengaturanAplikasi pengaturan) {
        this.koneksi = koneksi;
        this.lokasiMedia = lokasiMedia;
        this.pengaturan = pengaturan;
    }

    // Q-C64-01, null kalau belum pernah ada pencadangan
    public PencadanganEntity ambilTerakhir() {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C64_01); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            return new PencadanganEntity(rs.getInt("id_pencadangan"), rs.getTimestamp("waktu").toLocalDateTime(),
                    rs.getString("status"), rs.getString("lokasi_media"), rs.getLong("ukuran_berkas"));
        } catch (SQLException e) {
            throw galat("Gagal mengambil status pencadangan", e);
        }
    }

    // Algo-C64-01. Hasilnya belum disimpan, statusnya "berhasil" atau "gagal".
    public PencadanganEntity jalankanPgDump(String lokasi) {
        String folder = lokasi == null || lokasi.isBlank() ? lokasiMedia : lokasi;
        LocalDateTime sekarang = LocalDateTime.now();
        PencadanganEntity p = new PencadanganEntity(0, sekarang, "gagal", "", 0);
        if (folder == null || folder.isBlank()) {
            LOG.warning("LOKASI_MEDIA_CADANGAN belum diisi di .env");
            return p;
        }
        Path dir = Path.of(folder);
        Path berkas = dir.resolve("sehati_" + NAMA_BERKAS.format(sekarang) + ".dump");
        p.setLokasiMedia(berkas.toString());
        // HDD eksternal belum dicolok (alternatif 4 UC12)
        if (!Files.isDirectory(dir) || !Files.isWritable(dir)) {
            LOG.warning(() -> "Media cadangan tidak dapat ditulis: " + dir);
            return p;
        }
        List<String> perintah = List.of(pengaturan.ambil("PG_DUMP", "pg_dump"),
                "--host", pengaturan.ambil("DB_HOST", "localhost"), "--port", pengaturan.ambil("DB_PORT", "5432"),
                "--username", pengaturan.ambil("DB_USER", "sehati_app"), "--format=custom",
                "--file", berkas.toString(), pengaturan.ambil("DB_NAME", "sehati"));
        if (jalankan(perintah, null) == 0 && Files.isRegularFile(berkas)) {
            p.setStatus("berhasil");
            try {
                p.setUkuranBerkas(Files.size(berkas));
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Ukuran arsip tidak terbaca", e);
            }
        } else {
            hapus(berkas);
        }
        return p;
    }

    // Algo-C64-02: daftar isi harus memuat data pasien, lalu seluruh arsip harus terbaca utuh
    public boolean verifikasiArsip(PencadanganEntity p) {
        Path berkas = Path.of(p.getLokasiMedia());
        try {
            if (!Files.isRegularFile(berkas) || Files.size(berkas) == 0) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        String pgRestore = pengaturan.ambil("PG_RESTORE", "pg_restore");
        StringBuilder daftarIsi = new StringBuilder();
        if (jalankan(List.of(pgRestore, "--list", berkas.toString()), daftarIsi) != 0
                || !daftarIsi.toString().contains("TABLE DATA public pasien ")) {
            return false;
        }
        return jalankan(List.of(pgRestore, "--file=-", berkas.toString()), null) == 0;
    }

    // Q-C64-02, yang gagal juga disimpan supaya peringatan tetap muncul
    public void simpanCatatan(PencadanganEntity p) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C64_02)) {
            ps.setTimestamp(1, Timestamp.valueOf(p.getWaktu()));
            ps.setString(2, p.getStatus());
            ps.setString(3, p.getLokasiMedia());
            ps.setLong(4, p.getUkuranBerkas());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                p.setIdPencadangan(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw galat("Gagal menyimpan catatan pencadangan", e);
        }
    }

    // Kode keluar proses, atau -1 kalau gagal dijalankan/lewat batas waktu. Keluaran disimpan bila diminta.
    private int jalankan(List<String> perintah, StringBuilder keluaran) {
        Path log = null;
        Process proses = null;
        try {
            log = Files.createTempFile("sehati-cadangan", ".log");
            ProcessBuilder pb = new ProcessBuilder(perintah).redirectErrorStream(true);
            // Sandi lewat variabel lingkungan, bukan argumen, supaya tidak kelihatan di daftar proses
            pb.environment().put("PGPASSWORD", pengaturan.ambil("DB_PASSWORD", ""));
            pb.redirectOutput(keluaran == null ? Redirect.DISCARD : Redirect.to(log.toFile()));
            proses = pb.start();
            if (!proses.waitFor(BATAS_MENIT, TimeUnit.MINUTES)) {
                proses.destroyForcibly();
                LOG.warning(() -> perintah.get(0) + " melewati " + BATAS_MENIT + " menit");
                return -1;
            }
            if (keluaran != null) {
                keluaran.append(Files.readString(log, StandardCharsets.UTF_8));
            }
            int kode = proses.exitValue();
            if (kode != 0) {
                LOG.warning(() -> perintah.get(0) + " keluar dengan kode " + kode);
            }
            return kode;
        } catch (IOException e) {
            LOG.log(Level.WARNING, perintah.get(0) + " tidak dapat dijalankan", e);
            return -1;
        } catch (InterruptedException e) {
            proses.destroyForcibly();
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            hapus(log);
        }
    }

    private static void hapus(Path berkas) {
        if (berkas == null) {
            return;
        }
        try {
            Files.deleteIfExists(berkas);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Berkas tidak dapat dihapus: " + berkas, e);
        }
    }

    private static RuntimeException galat(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException("Basis data tidak dapat dihubungi", e);
    }
}
