package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class EntityPemeriksaanTest {

    private final KodeICD10Entity hipertensi = new KodeICD10Entity("I10", "Hipertensi esensial (primer)",
            "Penyakit sistem sirkulasi");

    @Test
    void kunci_mengunciLaluMenolakPenguncianKedua() {
        PemeriksaanEntity p = new PemeriksaanEntity(1, 1, "Pusing", "Edukasi", false);
        p.kunci();
        assertTrue(p.isTerkunci());
        assertThrows(IllegalStateException.class, p::kunci);
    }

    @Test
    void ringkas_kodeDanDeskripsi() {
        assertEquals("I10 - Hipertensi esensial (primer)",
                new DiagnosisEntity(1, "I10", "Hipertensi esensial (primer)").ringkas());
    }

    @Test
    void cocok_awalanKodeAtauIsiDeskripsiTanpaBedaHuruf() {
        assertTrue(hipertensi.cocok("i1"));
        assertTrue(hipertensi.cocok(" ESENSIAL "));
        assertFalse(hipertensi.cocok("10"));
        assertFalse(hipertensi.cocok("diabetes"));
    }

    @Test
    void cocok_kataKunciKosongTidakCocok() {
        assertFalse(hipertensi.cocok("  "));
        assertFalse(hipertensi.cocok(null));
    }

    @Test
    void tandaiSelesai_mengubahStatusLaluMenolakYangSudahSelesai() {
        JadwalKontrolEntity j = new JadwalKontrolEntity(1, 1, LocalDate.now().plusDays(7),
                JadwalKontrolEntity.STATUS_TERJADWAL);
        j.tandaiSelesai();
        assertEquals("selesai", j.getStatus());
        assertThrows(IllegalStateException.class, j::tandaiSelesai);
    }

    @Test
    void perbaruiStatus_menolakStatusAsingDanKunjunganSelesai() {
        KunjunganEntity k = new KunjunganEntity(1, 1, 1, LocalDate.now(), 1, KunjunganEntity.STATUS_SIAP_DIPERIKSA);
        k.perbaruiStatus(KunjunganEntity.STATUS_SELESAI_DIPERIKSA);
        assertEquals("selesai diperiksa", k.getStatus());
        assertTrue(k.aktifHariIni());
        assertThrows(IllegalArgumentException.class, () -> k.perbaruiStatus("dibatalkan"));

        k.perbaruiStatus(KunjunganEntity.STATUS_SELESAI);
        assertFalse(k.aktifHariIni());
        assertThrows(IllegalStateException.class, () -> k.perbaruiStatus(KunjunganEntity.STATUS_SIAP_DIPERIKSA));
    }
}
