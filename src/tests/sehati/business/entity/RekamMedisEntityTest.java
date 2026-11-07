package sehati.business.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RekamMedisEntityTest {

    @Test
    void tambahEntriMenampungCatatanBerurutanTanpaMenyentuhRiwayat() {
        RekamMedisEntity rm = new RekamMedisEntity();
        rm.tambahEntri("pertama");
        rm.tambahEntri("kedua");

        assertEquals(List.of("pertama", "kedua"), rm.getEntriBaru());
        assertEquals(List.of(), rm.getRiwayatObat());
        assertEquals(List.of(), rm.getDaftarDiagnosis());
    }

    @Test
    void catatanKosongDitolak() {
        RekamMedisEntity rm = new RekamMedisEntity();

        assertThrows(IllegalArgumentException.class, () -> rm.tambahEntri(null));
        assertThrows(IllegalArgumentException.class, () -> rm.tambahEntri("   "));
        assertEquals(List.of(), rm.getEntriBaru());
    }

    @Test
    void kosongkanEntriBaruHanyaMembuangPenampung() {
        RekamMedisEntity rm = new RekamMedisEntity();
        rm.tambahEntri("catatan");

        rm.kosongkanEntriBaru();

        assertEquals(List.of(), rm.getEntriBaru());
    }
}
