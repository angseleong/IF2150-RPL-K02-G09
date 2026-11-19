package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sehati.business.entity.PencadanganEntity;
import sehati.common.PengaturanAplikasi;

/**
 * Pengujian unit C64 DataPencadanganBoundary (HIUPL Subbab 4.2, C64-UT01 sampai C64-UT16). pg_dump dan pg_restore
 * diganti skrip shell palsu, jadi tes ini tidak butuh PostgreSQL terpasang. pg_dump asli diuji pada INT-TC05.
 */
@DisplayName("C64 DataPencadanganBoundary")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisabledOnOs(OS.WINDOWS)
class DataPencadanganBoundaryTest {

    @TempDir
    Path tmp;

    private Path skrip(String nama, String isi) throws IOException {
        Path p = tmp.resolve(nama);
        Files.writeString(p, "#!/bin/sh\n" + isi + "\n");
        Files.setPosixFilePermissions(p, PosixFilePermissions.fromString("rwx------"));
        return p;
    }

    private DataPencadanganBoundary boundary(Path media, Path pgDump, Path pgRestore) {
        return boundary(media, pgDump.toString(), pgRestore);
    }

    private DataPencadanganBoundary boundary(Path media, String pgDump, Path pgRestore) {
        return new DataPencadanganBoundary(null, media == null ? "" : media.toString(), new PengaturanAplikasi(Map.of(
                "PG_DUMP", pgDump, "PG_RESTORE", pgRestore == null ? "false" : pgRestore.toString())));
    }

    private static PencadanganEntity catatan(Path arsip) {
        return new PencadanganEntity(0, LocalDateTime.now(), "berhasil", arsip.toString(), 3);
    }

    // Argumen ke-9 pg_dump adalah lokasi --file
    private static final String DUMP_SUKSES = "printf 'arsip' > \"$9\"";
    private static final String DAFTAR_PASIEN = "echo '3; 0 1 TABLE DATA public pasien sehati'";

    @Test
    @DisplayName("C64-UT01 jalankanPgDump normal: arsip dibuat, status berhasil, ukuran dan lokasi terisi")
    void jalankanPgDump_berhasil() throws IOException {
        Path media = Files.createDirectory(tmp.resolve("media"));
        PencadanganEntity p = boundary(media, skrip("pg_dump", DUMP_SUKSES), null).jalankanPgDump(null);

        assertTrue(p.berhasil());
        assertEquals(5, p.getUkuranBerkas());
        assertTrue(p.getLokasiMedia().startsWith(media.resolve("sehati_").toString()));
        assertTrue(p.getLokasiMedia().endsWith(".dump"));
        assertTrue(Files.isRegularFile(Path.of(p.getLokasiMedia())));
    }

    @Test
    @DisplayName("C64-UT02 jalankanPgDump batas: argumen lokasi dipakai menggantikan LOKASI_MEDIA_CADANGAN")
    void jalankanPgDump_lokasiArgumen() throws IOException {
        Path utama = Files.createDirectory(tmp.resolve("utama"));
        Path lain = Files.createDirectory(tmp.resolve("lain"));
        PencadanganEntity p = boundary(utama, skrip("pg_dump", DUMP_SUKSES), null).jalankanPgDump(lain.toString());

        assertTrue(p.berhasil());
        assertTrue(p.getLokasiMedia().startsWith(lain.toString()));
    }

    @Test
    @DisplayName("C64-UT03 jalankanPgDump negatif: media tidak terpasang gagal tanpa menjalankan pg_dump")
    void jalankanPgDump_mediaTidakAda() throws IOException {
        Path penanda = tmp.resolve("dipanggil");
        Path pgDump = skrip("pg_dump", "touch " + penanda);
        PencadanganEntity p = boundary(tmp.resolve("hdd-tidak-dicolok"), pgDump, null).jalankanPgDump(null);

        assertEquals("gagal", p.getStatus());
        assertEquals(0, p.getUkuranBerkas());
        assertFalse(Files.exists(penanda));
    }

    @Test
    @DisplayName("C64-UT04 jalankanPgDump negatif: kode keluar bukan nol menghapus arsip setengah jadi")
    void jalankanPgDump_kodeKeluarBukanNol() throws IOException {
        Path media = Files.createDirectory(tmp.resolve("media"));
        PencadanganEntity p = boundary(media, skrip("pg_dump", DUMP_SUKSES + "; exit 1"), null).jalankanPgDump("");

        assertEquals("gagal", p.getStatus());
        assertFalse(Files.exists(Path.of(p.getLokasiMedia())));
    }

    @Test
    @DisplayName("C64-UT05 jalankanPgDump negatif: LOKASI_MEDIA_CADANGAN kosong dan lokasi null gagal")
    void jalankanPgDump_lokasiKosong() throws IOException {
        PencadanganEntity p = boundary(null, skrip("pg_dump", DUMP_SUKSES), null).jalankanPgDump(null);
        assertEquals("gagal", p.getStatus());
        assertEquals("", p.getLokasiMedia());
    }

    @Test
    @DisplayName("C64-UT06 jalankanPgDump negatif: perintah pg_dump tidak ditemukan dicatat gagal")
    void jalankanPgDump_perintahTidakAda() throws IOException {
        Path media = Files.createDirectory(tmp.resolve("media"));
        PencadanganEntity p = boundary(media, tmp.resolve("tidak-ada/pg_dump").toString(), null).jalankanPgDump(null);
        assertEquals("gagal", p.getStatus());
    }

    @Test
    @DisplayName("C64-UT07 jalankanPgDump batas: kode keluar nol tetapi arsip tidak terbentuk dicatat gagal")
    void jalankanPgDump_arsipTidakTerbentuk() throws IOException {
        Path media = Files.createDirectory(tmp.resolve("media"));
        PencadanganEntity p = boundary(media, skrip("pg_dump", "exit 0"), null).jalankanPgDump(null);
        assertEquals("gagal", p.getStatus());
        assertEquals(0, p.getUkuranBerkas());
    }

    @Test
    @DisplayName("C64-UT08 verifikasiArsip normal: daftar isi memuat data pasien dan arsip terbaca utuh")
    void verifikasiArsip_lengkap() throws IOException {
        Path arsip = Files.writeString(tmp.resolve("a.dump"), "isi");
        Path lengkap = skrip("pg_restore_ok", "[ \"$1\" = --list ] && " + DAFTAR_PASIEN + "; exit 0");
        assertTrue(boundary(tmp, skrip("pg_dump", "true"), lengkap).verifikasiArsip(catatan(arsip)));
    }

    @Test
    @DisplayName("C64-UT09 verifikasiArsip negatif: arsip tanpa data pasien ditolak")
    void verifikasiArsip_tanpaPasien() throws IOException {
        Path arsip = Files.writeString(tmp.resolve("a.dump"), "isi");
        Path tanpaPasien = skrip("pg_restore_tanpa", "echo '3; 0 1 TABLE DATA public obat sehati'");
        assertFalse(boundary(tmp, skrip("pg_dump", "true"), tanpaPasien).verifikasiArsip(catatan(arsip)));
    }

    @Test
    @DisplayName("C64-UT10 verifikasiArsip negatif: arsip terpotong (pg_restore gagal membaca utuh) ditolak")
    void verifikasiArsip_terpotong() throws IOException {
        Path arsip = Files.writeString(tmp.resolve("a.dump"), "isi");
        Path terpotong = skrip("pg_restore_potong", "[ \"$1\" = --list ] && " + DAFTAR_PASIEN + " && exit 0; exit 1");
        assertFalse(boundary(tmp, skrip("pg_dump", "true"), terpotong).verifikasiArsip(catatan(arsip)));
    }

    @Test
    @DisplayName("C64-UT11 verifikasiArsip batas: arsip 0 bait atau berkas hilang ditolak tanpa pg_restore")
    void verifikasiArsip_kosongAtauHilang() throws IOException {
        Path kosong = Files.createFile(tmp.resolve("kosong.dump"));
        Path pgRestore = skrip("pg_restore", "echo 'TABLE DATA public pasien x'");
        DataPencadanganBoundary b = boundary(tmp, skrip("pg_dump", "true"), pgRestore);

        assertFalse(b.verifikasiArsip(catatan(kosong)));
        assertFalse(b.verifikasiArsip(catatan(tmp.resolve("hilang.dump"))));
    }

    @Test
    @DisplayName("C64-UT12 verifikasiArsip negatif: pg_restore --list keluar bukan nol ditolak")
    void verifikasiArsip_daftarGagal() throws IOException {
        Path arsip = Files.writeString(tmp.resolve("a.dump"), "isi");
        Path gagal = skrip("pg_restore_gagal", DAFTAR_PASIEN + "; exit 1");
        assertFalse(boundary(tmp, skrip("pg_dump", "true"), gagal).verifikasiArsip(catatan(arsip)));
    }

    @Test
    @DisplayName("C64-UT15 simpanCatatan dan ambilTerakhir negatif: koneksi terputus menjadi IllegalStateException")
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    void koneksiTerputus() throws SQLException {
        Connection koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        koneksi.close();
        DataPencadanganBoundary b = new DataPencadanganBoundary(koneksi, "", new PengaturanAplikasi(Map.of()));
        IllegalStateException e = assertThrows(IllegalStateException.class, b::ambilTerakhir);
        assertEquals("Basis data tidak dapat dihubungi", e.getMessage());
        assertThrows(IllegalStateException.class, () -> b.simpanCatatan(catatan(tmp)));
    }

    @Test
    @DisplayName("C64-UT13 simpanCatatan dan ambilTerakhir normal: catatan tersimpan menjadi yang terbaru")
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    void simpanCatatanLaluAmbilTerakhir() throws SQLException {
        try (Connection koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
            koneksi.setAutoCommit(false);
            DataPencadanganBoundary b = new DataPencadanganBoundary(koneksi, "", new PengaturanAplikasi(Map.of()));
            PencadanganEntity p = new PencadanganEntity(0, LocalDateTime.now().plusYears(50), "gagal verifikasi",
                    "/media/uji.dump", 10);
            b.simpanCatatan(p);

            PencadanganEntity terakhir = b.ambilTerakhir();
            assertTrue(p.getIdPencadangan() > 0);
            assertEquals(p.getIdPencadangan(), terakhir.getIdPencadangan());
            assertEquals("/media/uji.dump", terakhir.getLokasiMedia());
            assertEquals(10, terakhir.getUkuranBerkas());
            assertFalse(terakhir.berhasil());
            koneksi.rollback();
        }
    }

    @Test
    @DisplayName("C64-UT14 ambilTerakhir batas: tabel pencadangan kosong mengembalikan null")
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    void ambilTerakhir_kosong() throws SQLException {
        try (Connection koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
            koneksi.setAutoCommit(false);
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM pencadangan");
            }
            assertNull(new DataPencadanganBoundary(koneksi, "", new PengaturanAplikasi(Map.of())).ambilTerakhir());
            koneksi.rollback();
        }
    }

    @Test
    @DisplayName("C64-UT16 simpanCatatan negatif: status di luar daftar ditolak basis data")
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    void simpanCatatan_statusTidakSah() throws SQLException {
        try (Connection koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
            koneksi.setAutoCommit(false);
            DataPencadanganBoundary b = new DataPencadanganBoundary(koneksi, "", new PengaturanAplikasi(Map.of()));
            PencadanganEntity p = new PencadanganEntity(0, LocalDateTime.now(), "selesai", "/media/uji.dump", 0);
            assertThrows(IllegalStateException.class, () -> b.simpanCatatan(p));
            koneksi.rollback();
        }
    }
}
