package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// Pengujian unit C53 LaporanEntity (HIUPL Subbab 4.2, C53-UT01 sampai C53-UT06).
@DisplayName("C53 LaporanEntity")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class LaporanEntityTest {

    private static LaporanEntity laporan(String ringkasan) {
        return new LaporanEntity(0, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 8), null, ringkasan);
    }

    @Test
    @DisplayName("C53-UT01 kosong normal: ringkasan null dianggap kosong")
    void kosong_bilaRingkasanNull() {
        assertTrue(laporan(null).kosong());
    }

    @Test
    @DisplayName("C53-UT02 kosong normal: ringkasan kosong dianggap kosong")
    void kosong_bilaRingkasanKosong() {
        assertTrue(laporan("").kosong());
    }

    @Test
    @DisplayName("C53-UT03 kosong normal: tanpa baris total dianggap kosong")
    void kosong_bilaTidakAdaBarisTotal() {
        assertTrue(laporan("poli;Umum;3\nstatus;selesai;3").kosong());
    }

    @Test
    @DisplayName("C53-UT04 kosong batas: total nol dianggap kosong")
    void kosong_bilaTotalNol() {
        assertTrue(laporan("total;semua kunjungan;0\npoli;Umum;0").kosong());
    }

    @Test
    @DisplayName("C53-UT05 tidak kosong normal: total positif dianggap berisi")
    void tidakKosong_bilaTotalPositif() {
        assertFalse(laporan("total;semua kunjungan;5\npoli;Umum;3").kosong());
    }

    @Test
    @DisplayName("C53-UT06 kosong negatif: total bukan angka dianggap kosong")
    void kosong_bilaTotalBukanAngka() {
        assertTrue(laporan("total;semua kunjungan;banyak").kosong());
    }
}
