package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PoliEntityTest {

    @Test
    void ambilNomorBerikutnya_mulaiDariSatuUntukHariPelayananBaru() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 0, null);

        assertEquals(1, poli.ambilNomorBerikutnya(LocalDate.of(2026, 11, 10)));
    }

    @Test
    void ambilNomorBerikutnya_bertambahBerurutan() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 3, LocalDate.of(2026, 11, 10));

        assertEquals(4, poli.ambilNomorBerikutnya(LocalDate.of(2026, 11, 10)));
        assertEquals(5, poli.ambilNomorBerikutnya(LocalDate.of(2026, 11, 10)));
    }

    @Test
    void ambilNomorBerikutnya_mulaiUlangSaatHariBerganti() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 7, LocalDate.of(2026, 11, 9));

        assertEquals(1, poli.ambilNomorBerikutnya(LocalDate.of(2026, 11, 10)));
    }
}
