package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/** Pengujian unit C39 TandaVitalEntity (HIUPL Subbab 4.2, C39-UT01 sampai C39-UT08). */
@DisplayName("C39 TandaVitalEntity")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class TandaVitalEntityTest {

    private static TandaVitalEntity tandaVital(double berat, double tinggi) {
        return new TandaVitalEntity(120, 80, berat, tinggi, 36.5, 72, null);
    }

    @Test
    @DisplayName("C39-UT01 hitungIMT normal: 72,25 kg pada 170 cm menghasilkan IMT 25,0")
    void hitungIMT_normal() {
        assertEquals(25.0, tandaVital(72.25, 170).hitungIMT(), 1e-9);
    }

    @Test
    @DisplayName("C39-UT02 hitungIMT batas: hasil tidak dibulatkan sehingga perbandingan ambang tidak bergeser")
    void hitungIMT_tanpaPembulatan() {
        // 84 kg pada 168 cm = 29,76190476...; pembulatan ke satu desimal menjadi 29,8
        assertEquals(84 / (1.68 * 1.68), tandaVital(84, 168).hitungIMT(), 1e-12);
    }

    @Test
    @DisplayName("C39-UT03 hitungIMT batas: tinggi di batas bawah rentang 30 cm tetap dihitung")
    void hitungIMT_tinggiBatasBawah() {
        assertEquals(10 / (0.3 * 0.3), tandaVital(10, 30).hitungIMT(), 1e-9);
    }

    @Test
    @DisplayName("C39-UT04 hitungIMT batas: tinggi positif sangat kecil tidak ditolak")
    void hitungIMT_tinggiPositifKecil() {
        assertEquals(1 / (0.01 * 0.01), tandaVital(1, 1).hitungIMT(), 1e-6);
    }

    @Test
    @DisplayName("C39-UT05 hitungIMT negatif: tinggi 0 ditolak")
    void hitungIMT_tinggiNol() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> tandaVital(70, 0).hitungIMT());
        assertEquals("Tinggi badan belum terisi", e.getMessage());
    }

    @Test
    @DisplayName("C39-UT06 hitungIMT negatif: tinggi bernilai negatif ditolak")
    void hitungIMT_tinggiNegatif() {
        assertThrows(IllegalStateException.class, () -> tandaVital(70, -170).hitungIMT());
    }

    @Test
    @DisplayName("C39-UT07 waktu normal: pengukuran baru belum berisi waktu dan hasil bacaan memuatnya")
    void waktu_kosongPadaPengukuranBaruDanTerisiBilaDibaca() {
        assertNull(tandaVital(70, 170).getWaktu());
        LocalDateTime waktu = LocalDateTime.of(2026, 11, 4, 9, 30);

        TandaVitalEntity tv = new TandaVitalEntity(120, 80, 70, 170, 36.5, 72, null, waktu);

        assertEquals(waktu, tv.getWaktu());
        assertEquals(120, tv.getSistolik());
    }

    @Test
    @DisplayName("C39-UT08 gulaDarah batas: pengukuran tanpa gula darah menyimpan null")
    void gulaDarah_boleKosong() {
        assertNull(tandaVital(70, 170).getGulaDarah());
        assertEquals(132, new TandaVitalEntity(120, 80, 70, 170, 36.5, 72, 132).getGulaDarah());
    }
}
