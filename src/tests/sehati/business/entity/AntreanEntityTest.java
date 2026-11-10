package sehati.business.entity;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AntreanEntityTest {

    private AntreanEntity antrean(Integer... isi) {
        return new AntreanEntity(1, "skrining", new ArrayList<>(List.of(isi)));
    }

    @Test
    void ambilTerdepan_mengembalikanEntriPertamaDanNullBilaKosong() {
        assertEquals(3, antrean(3, 2, 11).ambilTerdepan().getIdKunjungan());
        assertNull(antrean().ambilTerdepan());
    }

    @Test
    void pindahkanKeBelakang_menaruhEntriDiAkhir() {
        AntreanEntity a = antrean(3, 2, 11);

        a.pindahkanKeBelakang(3);

        assertEquals(List.of(2, 11, 3), a.getDaftarEntri());
    }

    @Test
    void keluarkan_menghapusEntri() {
        AntreanEntity a = antrean(3, 2, 11);

        a.keluarkan(2);

        assertEquals(List.of(3, 11), a.getDaftarEntri());
    }

    @Test
    void keluarkan_dan_pindahkanKeBelakang_menolakEntriYangTidakAda() {
        AntreanEntity a = antrean(3, 2);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> a.keluarkan(99));
        assertEquals("Entri tidak ada pada antrean skrining", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> a.pindahkanKeBelakang(99));
        assertEquals(List.of(3, 2), a.getDaftarEntri());
    }

    @Test
    void tambahkan_menambahkanEntriDiUjian() {
        AntreanEntity antrean = new AntreanEntity(1, "KUNJUNGAN", new ArrayList<>());

        antrean.tambahkan(10);
        antrean.tambahkan(11);

        assertEquals(List.of(10, 11), antrean.getDaftarEntri());
    }

    @Test
    void ambilTerdepan_mengembalikanEntriPalingAtas() {
        AntreanEntity antrean = new AntreanEntity(1, "KUNJUNGAN", new ArrayList<>(List.of(3, 1, 2)));

        KunjunganEntity terdepan = antrean.ambilTerdepan();

        assertEquals(3, terdepan.getIdKunjungan());
    }

    @Test
    void ambilTerdepan_antreanKosongMengembalikanNull() {
        AntreanEntity antrean = new AntreanEntity(1, "KUNJUNGAN", new ArrayList<>());

        assertNull(antrean.ambilTerdepan());
    }

    @Test
    void pindahkanKeBelakang_memindahEntriKeAkhirAntrean() {
        AntreanEntity antrean = new AntreanEntity(1, "KUNJUNGAN", new ArrayList<>(List.of(1, 2, 3)));

        antrean.pindahkanKeBelakang(1);

        assertEquals(List.of(2, 3, 1), antrean.getDaftarEntri());
    }

    @Test
    void keluarkan_menghapusEntriDariAntrean() {
        AntreanEntity antrean = new AntreanEntity(1, "KUNJUNGAN", new ArrayList<>(List.of(1, 2, 3)));

        antrean.keluarkan(2);

        assertEquals(List.of(1, 3), antrean.getDaftarEntri());
    }
}
