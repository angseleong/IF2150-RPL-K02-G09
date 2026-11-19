package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// Pengujian unit C31 SesiEntity (HIUPL Subbab 4.2, C31-UT01 sampai C31-UT07).
@DisplayName("C31 SesiEntity")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SesiEntityTest {

    private static SesiEntity sesiSejak(LocalDateTime mulai) {
        return new SesiEntity(UUID.randomUUID(), 1, mulai, true);
    }

    private static SesiEntity sesiSejak(int menitLalu) {
        return sesiSejak(LocalDateTime.now().minusMinutes(menitLalu));
    }

    @Test
    @DisplayName("C31-UT01 habisWaktu normal: sesi 30 menit lalu sudah habis untuk batas 15 menit")
    void habisWaktu_lewatBatas() {
        assertTrue(sesiSejak(30).habisWaktu(15));
        assertFalse(sesiSejak(0).habisWaktu(15));
    }

    @Test
    @DisplayName("C31-UT02 habisWaktu batas: tepat 15 menit habis, 14 menit 59 detik belum")
    void habisWaktu_tepatPadaBatas() {
        assertTrue(sesiSejak(15).habisWaktu(15));
        assertFalse(sesiSejak(LocalDateTime.now().minusMinutes(14).minusSeconds(59)).habisWaktu(15));
        // batas terkecil yang sah
        assertTrue(sesiSejak(1).habisWaktu(1));
    }

    @Test
    @DisplayName("C31-UT03 habisWaktu negatif: batas nol atau negatif ditolak")
    void habisWaktu_batasTidakSah() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> sesiSejak(0).habisWaktu(0));
        assertEquals("Batas waktu harus lebih dari nol", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> sesiSejak(0).habisWaktu(-1));
    }

    @Test
    @DisplayName("C31-UT04 tutup normal: sesi aktif menjadi tidak aktif")
    void tutup_sesiAktif() {
        SesiEntity s = sesiSejak(0);
        s.tutup();
        assertFalse(s.isStatusAktif());
    }

    @Test
    @DisplayName("C31-UT05 tutup batas: dipanggil dua kali tetap aman")
    void tutup_duaKali() {
        SesiEntity s = sesiSejak(0);
        s.tutup();
        s.tutup();
        assertFalse(s.isStatusAktif());
    }

    @Test
    @DisplayName("C31-UT06 tutup negatif: sesi kosong (konstruktor tanpa argumen) tetap tidak aktif tanpa pengecualian")
    void tutup_sesiKosong() {
        SesiEntity s = new SesiEntity();
        assertNull(s.getIdSesi());
        s.tutup();
        assertFalse(s.isStatusAktif());
    }

    @Test
    @DisplayName("C31-UT07 atribut: nilai dari setter terbaca kembali (sesi dibaca dari basis data)")
    void atribut_setterDanGetter() {
        UUID id = UUID.randomUUID();
        LocalDateTime mulai = LocalDateTime.of(2026, 11, 19, 8, 0);
        SesiEntity s = new SesiEntity();
        s.setIdSesi(id);
        s.setIdPengguna(4);
        s.setWaktuMulai(mulai);
        s.setStatusAktif(true);

        assertEquals(id, s.getIdSesi());
        assertEquals(4, s.getIdPengguna());
        assertEquals(mulai, s.getWaktuMulai());
        assertTrue(s.isStatusAktif());
    }
}
