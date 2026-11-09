package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AntreanEntityTest {

    @Test
    void keluarkan_menghapusEntriTanpaMengubahUrutanLain() {
        AntreanEntity a = new AntreanEntity(2, "apotek", new ArrayList<>(List.of(10, 11, 12)));
        a.keluarkan(11);
        assertEquals(List.of(10, 12), a.getDaftarEntri());
    }

    @Test
    void keluarkan_menolakEntriYangTidakAda() {
        AntreanEntity a = new AntreanEntity(2, "apotek", new ArrayList<>(List.of(10)));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> a.keluarkan(99));
        assertEquals("Entri tidak ada pada antrean apotek", e.getMessage());
        assertEquals(List.of(10), a.getDaftarEntri());
    }
}
