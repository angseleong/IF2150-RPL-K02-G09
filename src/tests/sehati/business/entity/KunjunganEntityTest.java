package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KunjunganEntityTest {

    @Test
    @DisplayName("C35-UT01 perbaruiStatus mengganti status kunjungan")
    void perbaruiStatus_menggantiStatusKunjungan() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "menunggu skrining");

        kunjungan.perbaruiStatus("sedang diskrining");

        assertEquals("sedang diskrining", kunjungan.getStatus());
    }

    @Test
    @DisplayName("C35-UT02 perbaruiStatus menolak status tidak dikenal")
    void perbaruiStatus_menolakStatusTidakDikenal() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "menunggu skrining");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> kunjungan.perbaruiStatus("AKTIF"));

        assertEquals("Status kunjungan tidak dikenal: AKTIF", e.getMessage());
    }

    @Test
    @DisplayName("C35-UT03 perbaruiStatus mengunci kunjungan yang sudah selesai")
    void perbaruiStatus_mengunciKunjunganYangSudahSelesai() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "selesai");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> kunjungan.perbaruiStatus("menunggu skrining"));

        assertEquals("Kunjungan sudah selesai dan tidak dapat diubah", e.getMessage());
    }

    @Test
    @DisplayName("C35-UT04 perbaruiStatus menerima status selesai sebagai status terakhir")
    void perbaruiStatus_menerimaStatusSelesai() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "siap diperiksa dokter");

        kunjungan.perbaruiStatus("selesai");

        assertEquals("selesai", kunjungan.getStatus());
    }

    @Test
    @DisplayName("C35-UT05 perbaruiStatus menolak status null")
    void perbaruiStatus_menolakStatusNull() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "menunggu skrining");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> kunjungan.perbaruiStatus(null));

        assertEquals("Status kunjungan tidak dikenal: null", e.getMessage());
    }

    @Test
    @DisplayName("C35-UT06 aktifHariIni bernilai benar untuk status aktif pada hari berjalan")
    void aktifHariIni_statusAktifPadaHariBerjalanBernilaiAktif() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "menunggu skrining");

        assertTrue(kunjungan.aktifHariIni());
    }

    @Test
    @DisplayName("C35-UT07 aktifHariIni bernilai salah untuk kunjungan selesai hari ini")
    void aktifHariIni_statusTidakAktifPadaHariBerjalanBernilaiTidakAktif() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now(), 3, "selesai");

        assertFalse(kunjungan.aktifHariIni());
    }

    @Test
    @DisplayName("C35-UT08 aktifHariIni bernilai salah untuk kunjungan kemarin")
    void aktifHariIni_kunjunganKemarinTidakAktif() {
        KunjunganEntity kunjungan = new KunjunganEntity(1, 1, 1, LocalDate.now().minusDays(1), 3,
                "menunggu skrining");

        assertFalse(kunjungan.aktifHariIni());
    }
}
