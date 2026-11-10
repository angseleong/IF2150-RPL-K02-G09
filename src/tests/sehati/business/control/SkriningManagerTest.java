package sehati.business.control;

import org.junit.jupiter.api.Test;
import sehati.business.entity.TandaVitalEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkriningManagerTest {

    private final SkriningManager manager = new SkriningManager();

    private static TandaVitalEntity normal() {
        return new TandaVitalEntity(120, 80, 65, 170, 36.5, 72, 100);
    }

    @Test
    void validasiRentang_nilaiNormalTidakAdaGalat() {
        assertTrue(manager.validasiRentang(normal()).isEmpty());
    }

    @Test
    void validasiRentang_suhuDiLuarRentangDitolak() {
        TandaVitalEntity tv = normal();
        tv.setSuhu(72);
        assertEquals(List.of("suhu"), manager.validasiRentang(tv));
    }

    @Test
    void validasiRentang_sistolikDiLuarRentangDitolak() {
        TandaVitalEntity tv = normal();
        tv.setSistolik(400);
        assertEquals(List.of("sistolik"), manager.validasiRentang(tv));
    }

    @Test
    void validasiRentang_batasRentangDiterima() {
        TandaVitalEntity tv = new TandaVitalEntity(300, 200, 350, 250, 45, 250, 1000);
        assertTrue(manager.validasiRentang(tv).isEmpty());
        tv.setNadi(251);
        assertEquals(List.of("nadi"), manager.validasiRentang(tv));
    }

    @Test
    void validasiRentang_gulaDarahBolehKosongTetapiTidakBolehDiLuarRentang() {
        TandaVitalEntity tv = normal();
        tv.setGulaDarah(null);
        assertTrue(manager.validasiRentang(tv).isEmpty());
        tv.setGulaDarah(5);
        assertEquals(List.of("gulaDarah"), manager.validasiRentang(tv));
    }

    @Test
    void validasiRentang_diastolikHarusLebihRendahDariSistolik() {
        TandaVitalEntity tv = normal();
        tv.setDiastolik(120);
        assertEquals(List.of("diastolik"), manager.validasiRentang(tv));
    }

    @Test
    void validasiRentang_seluruhKolomBermasalahDilaporkanSekaligus() {
        TandaVitalEntity tv = new TandaVitalEntity(0, 0, 0, 0, 0, 0, null);
        assertEquals(List.of("sistolik", "diastolik", "beratBadan", "tinggiBadan", "suhu", "nadi"),
                manager.validasiRentang(tv));
    }
}
