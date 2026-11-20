package sehati.business.entity;

import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("C37-UT01 tambahkan menaruh entri baru di akhir antrean")
    void tambahkan_menaruhEntriDiAkhir() {
        AntreanEntity a = antrean(3, 2);

        a.tambahkan(11);

        assertEquals(List.of(3, 2, 11), a.getDaftarEntri());
    }

    @Test
    @DisplayName("C37-UT02 tambahkan mengabaikan entri yang sudah ada")
    void tambahkan_mengabaikanEntriGanda() {
        AntreanEntity a = antrean(3, 2);

        a.tambahkan(3);

        assertEquals(List.of(3, 2), a.getDaftarEntri());
    }

    @Test
    @DisplayName("C37-UT03 tambahkan menerima entri pertama pada antrean kosong")
    void tambahkan_menerimaEntriPertama() {
        AntreanEntity a = antrean();

        a.tambahkan(7);

        assertEquals(List.of(7), a.getDaftarEntri());
    }

    @Test
    @DisplayName("C37-UT04 ambilTerdepan mengembalikan entri pertama")
    void ambilTerdepan_mengembalikanEntriPertama() {
        assertEquals(3, antrean(3, 2, 11).ambilTerdepan().getIdKunjungan());
    }

    @Test
    @DisplayName("C37-UT05 ambilTerdepan mengembalikan null untuk antrean kosong")
    void ambilTerdepan_kosongMengembalikanNull() {
        assertNull(antrean().ambilTerdepan());
    }

    @Test
    @DisplayName("C37-UT06 ambilTerdepan hanya mengisi id kunjungan")
    void ambilTerdepan_hanyaMengisiIdKunjungan() {
        KunjunganEntity terdepan = antrean(3, 2).ambilTerdepan();

        assertEquals(3, terdepan.getIdKunjungan());
        assertEquals(0, terdepan.getIdPasien());
        assertNull(terdepan.getStatus());
    }

    @Test
    @DisplayName("C37-UT07 pindahkanKeBelakang menaruh entri di akhir")
    void pindahkanKeBelakang_menaruhEntriDiAkhir() {
        AntreanEntity a = antrean(3, 2, 11);

        a.pindahkanKeBelakang(3);

        assertEquals(List.of(2, 11, 3), a.getDaftarEntri());
    }

    @Test
    @DisplayName("C37-UT08 pindahkanKeBelakang membiarkan entri terakhir tetap di belakang")
    void pindahkanKeBelakang_entriTerakhirTetap() {
        AntreanEntity a = antrean(3, 2, 11);

        a.pindahkanKeBelakang(11);

        assertEquals(List.of(3, 2, 11), a.getDaftarEntri());
    }

    @Test
    @DisplayName("C37-UT09 pindahkanKeBelakang menolak entri yang tidak ada")
    void pindahkanKeBelakang_menolakEntriTidakAda() {
        AntreanEntity a = antrean(3, 2);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> a.pindahkanKeBelakang(99));

        assertEquals("Entri tidak ada pada antrean skrining", e.getMessage());
        assertEquals(List.of(3, 2), a.getDaftarEntri());
    }

    @Test
    @DisplayName("C37-UT10 keluarkan menghapus entri")
    void keluarkan_menghapusEntri() {
        AntreanEntity a = antrean(3, 2, 11);

        a.keluarkan(2);

        assertEquals(List.of(3, 11), a.getDaftarEntri());
    }

    @Test
    @DisplayName("C37-UT11 keluarkan menghapus entri tanpa mengubah urutan lain")
    void keluarkan_menghapusEntriTanpaMengubahUrutanLain() {
        AntreanEntity a = new AntreanEntity(2, "apotek", new ArrayList<>(List.of(10, 11, 12)));

        a.keluarkan(11);

        assertEquals(List.of(10, 12), a.getDaftarEntri());
    }

    @Test
    @DisplayName("C37-UT12 keluarkan menolak entri yang tidak ada")
    void keluarkan_menolakEntriYangTidakAda() {
        AntreanEntity a = new AntreanEntity(2, "apotek", new ArrayList<>(List.of(10)));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> a.keluarkan(99));

        assertEquals("Entri tidak ada pada antrean apotek", e.getMessage());
        assertEquals(List.of(10), a.getDaftarEntri());
    }

    @Test
    @DisplayName("C37-UT13 keluarkan batas: mengeluarkan satu-satunya entri membuat antrean kosong")
    void keluarkan_satuSatunyaEntriMembuatAntreanKosong() {
        AntreanEntity a = antrean(5);

        a.keluarkan(5);

        assertEquals(List.of(), a.getDaftarEntri());
        assertNull(a.ambilTerdepan());
    }
}
