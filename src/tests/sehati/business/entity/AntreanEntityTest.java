package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class AntreanEntityTest {

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
