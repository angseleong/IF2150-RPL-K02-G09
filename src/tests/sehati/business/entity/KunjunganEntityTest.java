package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class KunjunganEntityTest {

    @Test
    void perbaruiStatus_menggantiStatusKunjungan() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "menunggu skrining");

        kunjungan.perbaruiStatus("sedang diskrining");

        assertEquals("sedang diskrining", kunjungan.getStatus());
    }

    @Test
    void perbaruiStatus_menolakStatusTidakDikenal() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "menunggu skrining");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> kunjungan.perbaruiStatus("AKTIF"));

        assertEquals("Status kunjungan tidak dikenal: AKTIF", e.getMessage());
    }

    @Test
    void perbaruiStatus_mengunciKunjunganYangSudahSelesai() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "selesai");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> kunjungan.perbaruiStatus("menunggu skrining"));

        assertEquals("Kunjungan sudah selesai dan tidak dapat diubah", e.getMessage());
    }

    @Test
    void aktifHariIni_statusAktifPadaHariBerjalanBernilaiAktif() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "menunggu skrining");

        assertTrue(kunjungan.aktifHariIni());
    }

    @Test
    void aktifHariIni_statusTidakAktifPadaHariBerjalanBernilaiTidakAktif() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "selesai");

        assertFalse(kunjungan.aktifHariIni());
    }

    @Test
    void aktifHariIni_kunjunganKemarinTidakAktif() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now().minusDays(1), 3,
                "menunggu skrining");

        assertFalse(kunjungan.aktifHariIni());
    }
}
