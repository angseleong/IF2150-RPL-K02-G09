package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoliEntityTest {

    @Test
    @DisplayName("C36-UT01 ambilNomorBerikutnya mulai dari satu untuk hari pelayanan baru")
    void ambilNomorBerikutnya_mulaiDariSatuUntukHariPelayananBaru() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 0, null);

        assertEquals(1, poli.ambilNomorBerikutnya(LocalDate.of(2026, 11, 10)));
    }

    @Test
    @DisplayName("C36-UT02 ambilNomorBerikutnya bertambah berurutan")
    void ambilNomorBerikutnya_bertambahBerurutan() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 3, LocalDate.of(2026, 11, 10));

        assertEquals(4, poli.ambilNomorBerikutnya(LocalDate.of(2026, 11, 10)));
        assertEquals(5, poli.ambilNomorBerikutnya(LocalDate.of(2026, 11, 10)));
    }

    @Test
    @DisplayName("C36-UT03 ambilNomorBerikutnya mulai ulang saat hari berganti")
    void ambilNomorBerikutnya_mulaiUlangSaatHariBerganti() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 7, LocalDate.of(2026, 11, 9));

        assertEquals(1, poli.ambilNomorBerikutnya(LocalDate.of(2026, 11, 10)));
    }

    @Test
    @DisplayName("C36-UT04 ambilNomorBerikutnya menolak hari layanan null")
    void ambilNomorBerikutnya_menolakHariNull() {
        PoliEntity poli = new PoliEntity(1, "Poli Umum", 0, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> poli.ambilNomorBerikutnya(null));

        assertEquals("Hari layanan tidak boleh kosong", e.getMessage());
    }
}
