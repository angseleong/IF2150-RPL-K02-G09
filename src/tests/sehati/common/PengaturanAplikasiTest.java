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
    void ambil_menolakKunciKosong() {
        PengaturanAplikasi p = new PengaturanAplikasi(Map.of("DB_NAME", ""));

        assertThrows(IllegalStateException.class, () -> p.ambil("DB_NAME"));
        assertEquals("sehati", p.ambil("DB_NAME", "sehati"));
    }
}
