package sehati.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class PengaturanAplikasiTest {

    @Test
    void bacaEnv_melewatiKomentarDanBarisKosong() {
        Map<String, String> hasil = PengaturanAplikasi.bacaEnv(List.of(
                "# komentar", "", "DB_HOST = db.lokal", "DB_PASSWORD=a=b", "TANPA_SAMA_DENGAN"));

        assertEquals(Map.of("DB_HOST", "db.lokal", "DB_PASSWORD", "a=b"), hasil);
    }

    @Test
    void bacaEnv_membuangKutipKomentarExportDanBom() {
        Map<String, String> hasil = PengaturanAplikasi.bacaEnv(List.of(
                "﻿DB_HOST=db.lokal", "export DB_USER=sehati_app", "DB_PASSWORD=\"ra # hasia\" # komentar",
                "DB_NAME='sehati'", "DB_PORT=5432 # bawaan"));

        assertEquals(Map.of("DB_HOST", "db.lokal", "DB_USER", "sehati_app", "DB_PASSWORD", "ra # hasia",
                "DB_NAME", "sehati", "DB_PORT", "5432"), hasil);
    }

    @Test
    void gabung_variabelKosongTidakMenimpaEnv() {
        PengaturanAplikasi p = PengaturanAplikasi.gabung(
                List.of("DB_NAME=sehati", "DB_HOST=localhost"), Map.of("DB_NAME", "", "DB_HOST", "db.lokal"));

        assertEquals("sehati", p.ambil("DB_NAME"));
        assertEquals("db.lokal", p.ambil("DB_HOST"));
    }

    @Test
    void ambil_menolakKunciKosong() {
        PengaturanAplikasi p = new PengaturanAplikasi(Map.of("DB_NAME", ""));

        assertThrows(IllegalStateException.class, () -> p.ambil("DB_NAME"));
        assertEquals("sehati", p.ambil("DB_NAME", "sehati"));
    }
}
