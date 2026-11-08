package sehati.business.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AntreanEntityTest {

    private AntreanEntity antrean() {
        return new AntreanEntity(1, "skrining", new ArrayList<>(List.of(10, 20, 30)));
    }

    @Test
    void tambahkan_menambahKeAkhir() {
        AntreanEntity a = antrean();

        a.tambahkan(40);

        assertEquals(List.of(10, 20, 30, 40), a.getDaftarEntri());
    }

    @Test
    void tambahkan_entriSamaTidakDitambahkanDuaKali() {
        AntreanEntity a = antrean();

        a.tambahkan(20);

        assertEquals(List.of(10, 20, 30), a.getDaftarEntri());
    }

    @Test
    void ambilTerdepan_mengembalikanKunjunganPertamaTanpaMengeluarkan() {
        AntreanEntity a = antrean();

        KunjunganEntity depan = a.ambilTerdepan();

        assertEquals(10, depan.getIdKunjungan());
        assertEquals(List.of(10, 20, 30), a.getDaftarEntri());
    }

    @Test
    void ambilTerdepan_antreanKosongMengembalikanNull() {
        AntreanEntity a = new AntreanEntity(1, "skrining", new ArrayList<>());

        assertNull(a.ambilTerdepan());
    }

    @Test
    void pindahkanKeBelakang_memindahkanEntriKeAkhir() {
        AntreanEntity a = antrean();

        a.pindahkanKeBelakang(20);

        assertEquals(List.of(10, 30, 20), a.getDaftarEntri());
    }

    @Test
    void pindahkanKeBelakang_entriTidakAdaDitolak() {
        AntreanEntity a = antrean();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> a.pindahkanKeBelakang(99));

        assertEquals("Entri tidak ada pada antrean skrining", e.getMessage());
    }

    @Test
    void keluarkan_menghapusEntri() {
        AntreanEntity a = antrean();

        a.keluarkan(20);

        assertEquals(List.of(10, 30), a.getDaftarEntri());
    }

    @Test
    void keluarkan_entriTidakAdaDitolak() {
        AntreanEntity a = antrean();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> a.keluarkan(99));

        assertEquals("Entri tidak ada pada antrean skrining", e.getMessage());
    }
}
