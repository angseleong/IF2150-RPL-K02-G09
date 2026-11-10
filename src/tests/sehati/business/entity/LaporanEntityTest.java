package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class LaporanEntityTest {

    private static LaporanEntity laporan(String ringkasan) {
        return new LaporanEntity(0, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 8), null, ringkasan);
    }

    @Test
    void kosong_bilaRingkasanNull() {
        assertTrue(laporan(null).kosong());
    }

    @Test
    void kosong_bilaRingkasanKosong() {
        assertTrue(laporan("").kosong());
    }

    @Test
    void kosong_bilaTidakAdaBarisTotal() {
        assertTrue(laporan("poli;Umum;3\nstatus;selesai;3").kosong());
    }

    @Test
    void kosong_bilaTotalNol() {
        assertTrue(laporan("total;semua kunjungan;0\npoli;Umum;0").kosong());
    }

    @Test
    void tidakKosong_bilaTotalPositif() {
        assertFalse(laporan("total;semua kunjungan;5\npoli;Umum;3").kosong());
    }

    @Test
    void kosong_bilaTotalBukanAngka() {
        assertTrue(laporan("total;semua kunjungan;banyak").kosong());
    }
}
