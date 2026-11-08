package sehati.business.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PoliEntityTest {

    @Test
    void ambilNomorBerikutnya_hariPertamaDimulaiDariSatu() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 0, LocalDate.now().minusDays(1));

        assertEquals(1, poli.ambilNomorBerikutnya(LocalDate.now()));
    }

    @Test
    void ambilNomorBerikutnya_hariSamaBertambahSatu() {
        LocalDate hariIni = LocalDate.now();
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 7, hariIni);

        assertEquals(8, poli.ambilNomorBerikutnya(hariIni));
        assertEquals(9, poli.ambilNomorBerikutnya(hariIni));
    }

    @Test
    void ambilNomorBerikutnya_hariBergantiDimulaiLagiDariSatu() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 25, LocalDate.now().minusDays(1));

        assertEquals(1, poli.ambilNomorBerikutnya(LocalDate.now()));
        assertEquals(LocalDate.now(), poli.getTanggalUrutan());
        assertEquals(1, poli.getUrutanTerakhir());
    }

    @Test
    void ambilNomorBerikutnya_tanggalUrutanKosongDianggapHariBaru() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 5, null);

        assertEquals(1, poli.ambilNomorBerikutnya(LocalDate.now()));
    }

    @Test
    void ambilNomorBerikutnya_hariIniNullDitolak() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 0, null);

        assertThrows(IllegalArgumentException.class, () -> poli.ambilNomorBerikutnya(null));
    }
}
