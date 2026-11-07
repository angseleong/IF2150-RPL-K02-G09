package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class SesiEntityTest {

    private static SesiEntity sesiSejak(int menitLalu) {
        return new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now().minusMinutes(menitLalu), true);
    }

    @Test
    void habisWaktu_trueTepatPadaBatas() {
        assertTrue(sesiSejak(15).habisWaktu(15));
        assertTrue(sesiSejak(30).habisWaktu(15));
    }

    @Test
    void habisWaktu_falseSebelumBatas() {
        assertFalse(sesiSejak(14).habisWaktu(15));
        assertFalse(sesiSejak(0).habisWaktu(15));
    }

    @Test
    void habisWaktu_menolakBatasNolAtauNegatif() {
        assertThrows(IllegalArgumentException.class, () -> sesiSejak(0).habisWaktu(0));
        assertThrows(IllegalArgumentException.class, () -> sesiSejak(0).habisWaktu(-1));
    }

    @Test
    void tutup_menonaktifkanSesiDanAmanDipanggilDuaKali() {
        SesiEntity s = sesiSejak(0);
        s.tutup();
        s.tutup();
        assertFalse(s.isStatusAktif());
    }
}
