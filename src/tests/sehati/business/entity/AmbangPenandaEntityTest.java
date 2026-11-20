package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Pengujian unit C40 AmbangRisikoEntity (C40-UT01 sampai C40-UT05) dan C41 PenandaRisikoEntity (C41-UT01 sampai
 * C41-UT04), HIUPL Subbab 4.2.
 */
@DisplayName("C40 AmbangRisikoEntity dan C41 PenandaRisikoEntity")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class AmbangPenandaEntityTest {

    private static PenandaRisikoEntity penanda() {
        return new PenandaRisikoEntity(1, 1, "hipertensi", LocalDate.now(), PenandaRisikoEntity.STATUS_AKTIF);
    }

    @Test
    @DisplayName("C40-UT01 dilampaui normal: nilai di atas batas dianggap melampaui")
    void dilampaui_diAtasBatas() {
        assertTrue(new AmbangRisikoEntity(1, "hipertensi", 140).dilampaui(152));
    }

    @Test
    @DisplayName("C40-UT02 dilampaui batas: nilai tepat sama dengan batas dianggap melampaui")
    void dilampaui_samaDenganBatas() {
        AmbangRisikoEntity ambang = new AmbangRisikoEntity(1, "hipertensi", 140);
        assertTrue(ambang.dilampaui(140));
        assertTrue(ambang.dilampaui(141));
    }

    @Test
    @DisplayName("C40-UT03 dilampaui batas: nilai sedikit di bawah batas tidak melampaui")
    void dilampaui_sedikitDiBawahBatas() {
        AmbangRisikoEntity ambang = new AmbangRisikoEntity(1, "hipertensi", 140);
        assertFalse(ambang.dilampaui(139.9));
        assertFalse(ambang.dilampaui(139.999999));
    }

    @Test
    @DisplayName("C40-UT04 dilampaui negatif: nilai 0 dan nilai negatif tidak melampaui")
    void dilampaui_nolDanNegatif() {
        AmbangRisikoEntity ambang = new AmbangRisikoEntity(2, "obesitas", 30);
        assertFalse(ambang.dilampaui(0));
        assertFalse(ambang.dilampaui(-5));
    }

    @Test
    @DisplayName("C40-UT05 dilampaui batas: IMT 30,0 tepat melampaui ambang obesitas dan 29,9 tidak")
    void dilampaui_ambangObesitas() {
        AmbangRisikoEntity ambang = new AmbangRisikoEntity(2, "obesitas", 30);
        assertTrue(ambang.dilampaui(30.0));
        assertFalse(ambang.dilampaui(29.9));
        assertEquals("obesitas", ambang.getJenisRisiko());
        assertEquals(30, ambang.getNilaiBatas());
    }

    @Test
    @DisplayName("C41-UT01 cabut normal: penanda aktif menjadi dicabut dan riwayatnya tetap")
    void cabut_normal() {
        PenandaRisikoEntity p = penanda();

        p.cabut();

        assertEquals(PenandaRisikoEntity.STATUS_DICABUT, p.getStatus());
        assertEquals("hipertensi", p.getJenisRisiko());
    }

    @Test
    @DisplayName("C41-UT02 cabut negatif: penanda yang sudah dicabut tidak dapat dicabut lagi")
    void cabut_keduaKaliDitolak() {
        PenandaRisikoEntity p = penanda();
        p.cabut();

        IllegalStateException e = assertThrows(IllegalStateException.class, p::cabut);

        assertEquals("Penanda risiko sudah dicabut", e.getMessage());
        assertEquals(PenandaRisikoEntity.STATUS_DICABUT, p.getStatus());
    }

    @Test
    @DisplayName("C41-UT03 cabut batas: penanda baru berstatus aktif sehingga pencabutan pertama diterima")
    void cabut_penandaBaruAktif() {
        PenandaRisikoEntity p = penanda();

        assertEquals("aktif", p.getStatus());
        p.cabut();
        assertEquals("dicabut", p.getStatus());
    }

    @Test
    @DisplayName("C41-UT04 cabut batas: pencabutan satu penanda tidak memengaruhi penanda lain")
    void cabut_independen() {
        PenandaRisikoEntity a = penanda();
        PenandaRisikoEntity b = new PenandaRisikoEntity(2, 1, "obesitas", LocalDate.now(),
                PenandaRisikoEntity.STATUS_AKTIF);

        a.cabut();

        assertEquals(PenandaRisikoEntity.STATUS_AKTIF, b.getStatus());
    }
}
