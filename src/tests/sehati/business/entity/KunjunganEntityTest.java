package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class KunjunganEntityTest {

    @Test
    void perbaruiStatus_menggantiStatusKunjungan() {
        KunjunganEntity kunjungan = new KunjunganEntity();

        kunjungan.perbaruiStatus("SELESAI");

        assertEquals("SELESAI", kunjungan.getStatus());
    }

    @Test
    void aktifHariIni_statusAktifPadaHariBerjalanBernilaiAktif() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "AKTIF");

        assertTrue(kunjungan.aktifHariIni());
    }

    @Test
    void aktifHariIni_statusTidakAktifPadaHariBerjalanBernilaiTidakAktif() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "SELESAI");

        assertFalse(kunjungan.aktifHariIni());
    }

    @Test
    void aktifHariIni_kunjunganKemarinTidakAktif() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now().minusDays(1), 3, "AKTIF");

        assertFalse(kunjungan.aktifHariIni());
    }
}
