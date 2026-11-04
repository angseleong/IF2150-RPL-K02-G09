package sehati.business.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntitySkriningTest {

    private static TandaVitalEntity tandaVital(double berat, double tinggi) {
        return new TandaVitalEntity(120, 80, berat, tinggi, 36.5, 72, null);
    }

    @Test
    void hitungIMT_membagiBeratDenganTinggiMeterKuadrat() {
        assertEquals(25.0, tandaVital(72.25, 170).hitungIMT(), 1e-9);
    }

    @Test
    void hitungIMT_menolakTinggiNol() {
        assertThrows(IllegalStateException.class, () -> tandaVital(70, 0).hitungIMT());
    }

    @Test
    void dilampaui_samaDenganBatasDianggapMelampaui() {
        AmbangRisikoEntity ambang = new AmbangRisikoEntity(1, "hipertensi", 140);
        assertTrue(ambang.dilampaui(140));
        assertTrue(ambang.dilampaui(141));
        assertFalse(ambang.dilampaui(139.9));
    }

    @Test
    void cabut_mengubahStatusLaluMenolakPencabutanKedua() {
        PenandaRisikoEntity p = new PenandaRisikoEntity(1, 1, "hipertensi", LocalDate.now(),
                PenandaRisikoEntity.STATUS_AKTIF);
        p.cabut();
        assertEquals("dicabut", p.getStatus());
        assertThrows(IllegalStateException.class, p::cabut);
    }

    @Test
    void lampirkanTandaVital_menyimpanReferensi() {
        SkriningEntity s = new SkriningEntity(0, 1, "", LocalDateTime.now());
        TandaVitalEntity tv = tandaVital(60, 160);
        s.lampirkanTandaVital(tv);
        assertSame(tv, s.getTandaVital());
    }

    @Test
    void lampirkanTandaVital_menolakNull() {
        SkriningEntity s = new SkriningEntity(0, 1, "", LocalDateTime.now());
        assertThrows(IllegalArgumentException.class, () -> s.lampirkanTandaVital(null));
    }
}
