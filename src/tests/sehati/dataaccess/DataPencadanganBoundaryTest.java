package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import sehati.business.entity.PencadanganEntity;
import sehati.common.PengaturanAplikasi;

/**
 * pg_dump dan pg_restore diganti skrip shell palsu, jadi tes ini tidak butuh PostgreSQL terpasang.
 */
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
        return new DataPencadanganBoundary(null, media == null ? "" : media.toString(), new PengaturanAplikasi(Map.of(
                "PG_DUMP", pgDump.toString(), "PG_RESTORE", pgRestore == null ? "false" : pgRestore.toString())));
    }

    // Argumen ke-9 pg_dump adalah lokasi --file
    private static final String DUMP_SUKSES = "printf 'arsip' > \"$9\"";

    @Test
    void jalankanPgDump_berhasilMengisiUkuranDanLokasi() throws IOException {
        Path media = Files.createDirectory(tmp.resolve("media"));
        PencadanganEntity p = boundary(media, skrip("pg_dump", DUMP_SUKSES), null).jalankanPgDump(null);

        assertTrue(p.berhasil());
        assertEquals(5, p.getUkuranBerkas());
        assertTrue(p.getLokasiMedia().startsWith(media.resolve("sehati_").toString()));
        assertTrue(p.getLokasiMedia().endsWith(".dump"));
    }

    @Test
    void jalankanPgDump_mediaTidakAdaGagalTanpaMenjalankanPerintah() throws IOException {
        Path penanda = tmp.resolve("dipanggil");
        Path pgDump = skrip("pg_dump", "touch " + penanda);
        PencadanganEntity p = boundary(tmp.resolve("hdd-tidak-dicolok"), pgDump, null).jalankanPgDump(null);

        assertEquals("gagal", p.getStatus());
        assertEquals(0, p.getUkuranBerkas());
        assertFalse(Files.exists(penanda));
    }

    @Test
    void jalankanPgDump_kodeKeluarBukanNolMenghapusArsipSetengahJadi() throws IOException {
        Path media = Files.createDirectory(tmp.resolve("media"));
        PencadanganEntity p = boundary(media, skrip("pg_dump", DUMP_SUKSES + "; exit 1"), null).jalankanPgDump("");

        assertEquals("gagal", p.getStatus());
        assertFalse(Files.exists(Path.of(p.getLokasiMedia())));
    }

    @Test
    void verifikasiArsip_butuhDataPasienDanArsipTerbacaUtuh() throws IOException {
        Path arsip = Files.writeString(tmp.resolve("a.dump"), "isi");
        PencadanganEntity p = new PencadanganEntity(0, LocalDateTime.now(), "berhasil", arsip.toString(), 3);
        Path dump = skrip("pg_dump", "true");

        Path lengkap = skrip("pg_restore_ok",
                "[ \"$1\" = --list ] && echo '3; 0 1 TABLE DATA public pasien sehati'; exit 0");
        assertTrue(boundary(tmp, dump, lengkap).verifikasiArsip(p));

        Path tanpaPasien = skrip("pg_restore_tanpa", "echo '3; 0 1 TABLE DATA public obat sehati'");
        assertFalse(boundary(tmp, dump, tanpaPasien).verifikasiArsip(p));

        Path terpotong = skrip("pg_restore_potong",
                "[ \"$1\" = --list ] && echo '3; 0 1 TABLE DATA public pasien sehati' && exit 0; exit 1");
        assertFalse(boundary(tmp, dump, terpotong).verifikasiArsip(p));
    }

    @Test
    void verifikasiArsip_berkasKosongAtauHilang() throws IOException {
        Path kosong = Files.createFile(tmp.resolve("kosong.dump"));
        Path pgRestore = skrip("pg_restore", "echo 'TABLE DATA public pasien x'");
        DataPencadanganBoundary b = boundary(tmp, skrip("pg_dump", "true"), pgRestore);

        assertFalse(b.verifikasiArsip(new PencadanganEntity(0, LocalDateTime.now(), "berhasil", kosong.toString(), 0)));
        assertFalse(b.verifikasiArsip(new PencadanganEntity(0, LocalDateTime.now(), "berhasil",
                tmp.resolve("hilang.dump").toString(), 0)));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    void simpanCatatanLaluAmbilTerakhir() throws SQLException {
        try (Connection koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
            koneksi.setAutoCommit(false);
            DataPencadanganBoundary b = new DataPencadanganBoundary(koneksi, "", new PengaturanAplikasi(Map.of()));
            PencadanganEntity p = new PencadanganEntity(0, LocalDateTime.now().plusYears(50), "gagal verifikasi",
                    "/media/uji.dump", 10);
            b.simpanCatatan(p);

            PencadanganEntity terakhir = b.ambilTerakhir();
            assertEquals(p.getIdPencadangan(), terakhir.getIdPencadangan());
            assertFalse(terakhir.berhasil());
            koneksi.rollback();
        }
    }
}
