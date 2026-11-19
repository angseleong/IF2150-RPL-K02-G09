package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mindrot.jbcrypt.BCrypt;

// Pengujian unit C30 PenggunaEntity (HIUPL Subbab 4.2, C30-UT01 sampai C30-UT11).
@DisplayName("C30 PenggunaEntity")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class PenggunaEntityTest {

    // Faktor biaya 4 agar test cepat; checkpw membaca faktor dari hash.
    private static final String HASH = BCrypt.hashpw("rahasia123", BCrypt.gensalt(4));

    @Test
    @DisplayName("C30-UT01 cocokkanSandi normal: sandi benar mengembalikan true")
    void cocokkanSandi_benar() {
        assertTrue(new PenggunaEntity("perawat1", HASH, "Perawat").cocokkanSandi("rahasia123"));
    }

    @Test
    @DisplayName("C30-UT02 cocokkanSandi batas: beda satu huruf kapital atau satu karakter mengembalikan false")
    void cocokkanSandi_bedaSatuKarakter() {
        PenggunaEntity p = new PenggunaEntity("perawat1", HASH, "Perawat");
        assertFalse(p.cocokkanSandi("Rahasia123"));
        assertFalse(p.cocokkanSandi("rahasia12"));
        assertFalse(p.cocokkanSandi("rahasia1234"));
    }

    @Test
    @DisplayName("C30-UT03 cocokkanSandi negatif: sandi salah, kosong, atau null mengembalikan false")
    void cocokkanSandi_salahAtauKosong() {
        PenggunaEntity p = new PenggunaEntity("perawat1", HASH, "Perawat");
        assertFalse(p.cocokkanSandi("salah"));
        assertFalse(p.cocokkanSandi(""));
        assertFalse(p.cocokkanSandi(null));
    }

    @Test
    @DisplayName("C30-UT04 cocokkanSandi negatif: hash kosong atau null mengembalikan false")
    void cocokkanSandi_hashKosong() {
        assertFalse(new PenggunaEntity("perawat1", "", "Perawat").cocokkanSandi("rahasia123"));
        assertFalse(new PenggunaEntity("perawat1", null, "Perawat").cocokkanSandi("rahasia123"));
    }

    @Test
    @DisplayName("C30-UT05 cocokkanSandi negatif: hash rusak mengembalikan false tanpa pengecualian")
    void cocokkanSandi_hashRusak() {
        assertFalse(new PenggunaEntity("a", "x", "Perawat").cocokkanSandi("rahasia123"));
        assertFalse(new PenggunaEntity("a", "$2a$xx$bukanhash", "Perawat").cocokkanSandi("rahasia123"));
        assertFalse(new PenggunaEntity("a", "$9a$10$" + "a".repeat(53), "Perawat").cocokkanSandi("rahasia123"));
    }

    @Test
    @DisplayName("C30-UT06 aktif normal: status \"aktif\" mengembalikan true")
    void aktif_statusAktif() {
        assertTrue(new PenggunaEntity(1, "admin", HASH, "Petugas Administrasi", "aktif").aktif());
    }

    @Test
    @DisplayName("C30-UT07 aktif batas: status berhuruf kapital atau null mengembalikan false")
    void aktif_statusTidakPersis() {
        assertFalse(new PenggunaEntity(1, "admin", HASH, "Petugas Administrasi", "Aktif").aktif());
        assertFalse(new PenggunaEntity(1, "admin", HASH, "Petugas Administrasi", null).aktif());
    }

    @Test
    @DisplayName("C30-UT08 aktif negatif: status \"nonaktif\" mengembalikan false")
    void aktif_statusNonaktif() {
        assertFalse(new PenggunaEntity(5, "perawat.nonaktif", HASH, "Perawat", "nonaktif").aktif());
    }

    @Test
    @DisplayName("C30-UT09 nonaktifkan normal: akun aktif menjadi nonaktif")
    void nonaktifkan_akunAktif() {
        PenggunaEntity p = new PenggunaEntity(3, "dokter1", HASH, "Dokter", "aktif");
        p.nonaktifkan();
        assertFalse(p.aktif());
        assertEquals("nonaktif", p.getStatus());
    }

    @Test
    @DisplayName("C30-UT10 nonaktifkan batas: akun baru (id 0, status aktif dari konstruktor) dapat dinonaktifkan")
    void nonaktifkan_akunBaru() {
        PenggunaEntity p = new PenggunaEntity("dokter1", HASH, "Dokter");
        assertEquals(0, p.getIdPengguna());
        assertTrue(p.aktif());
        p.nonaktifkan();
        assertEquals("nonaktif", p.getStatus());
    }

    @Test
    @DisplayName("C30-UT11 nonaktifkan negatif: akun yang sudah nonaktif ditolak")
    void nonaktifkan_sudahNonaktif() {
        PenggunaEntity p = new PenggunaEntity(1, "dokter1", HASH, "Dokter", "nonaktif");
        IllegalStateException e = assertThrows(IllegalStateException.class, p::nonaktifkan);
        assertEquals("Akun sudah nonaktif", e.getMessage());
        assertEquals("nonaktif", p.getStatus());
    }
}
