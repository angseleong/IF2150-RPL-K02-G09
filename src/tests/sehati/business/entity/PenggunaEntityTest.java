package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

class PenggunaEntityTest {

    // Faktor biaya 4 agar test cepat; checkpw membaca faktor dari hash.
    private static final String HASH = BCrypt.hashpw("rahasia123", BCrypt.gensalt(4));

    @Test
    void cocokkanSandi_benarMengembalikanTrue() {
        assertTrue(new PenggunaEntity("perawat1", HASH, "Perawat").cocokkanSandi("rahasia123"));
    }

    @Test
    void cocokkanSandi_salahAtauKosongMengembalikanFalse() {
        PenggunaEntity p = new PenggunaEntity("perawat1", HASH, "Perawat");
        assertFalse(p.cocokkanSandi("rahasia124"));
        assertFalse(p.cocokkanSandi(""));
        assertFalse(p.cocokkanSandi(null));
        assertFalse(new PenggunaEntity("perawat1", "", "Perawat").cocokkanSandi("rahasia123"));
        assertFalse(new PenggunaEntity("perawat1", null, "Perawat").cocokkanSandi("rahasia123"));
    }

    @Test
    void cocokkanSandi_hashRusakMengembalikanFalse() {
        assertFalse(new PenggunaEntity("a", "x", "Perawat").cocokkanSandi("rahasia123"));
        assertFalse(new PenggunaEntity("a", "$2a$xx$bukanhash", "Perawat").cocokkanSandi("rahasia123"));
        assertFalse(new PenggunaEntity("a", "$9a$10$" + "a".repeat(53), "Perawat").cocokkanSandi("rahasia123"));
    }

    @Test
    void konstruktorAkunBaru_statusAktifDanIdNol() {
        PenggunaEntity p = new PenggunaEntity("dokter1", HASH, "Dokter");
        assertEquals(0, p.getIdPengguna());
        assertTrue(p.aktif());
    }

    @Test
    void nonaktifkan_mengubahStatus() {
        PenggunaEntity p = new PenggunaEntity("dokter1", HASH, "Dokter");
        p.nonaktifkan();
        assertFalse(p.aktif());
        assertEquals("nonaktif", p.getStatus());
    }

    @Test
    void nonaktifkan_menolakAkunYangSudahNonaktif() {
        PenggunaEntity p = new PenggunaEntity(1, "dokter1", HASH, "Dokter", "nonaktif");
        assertThrows(IllegalStateException.class, p::nonaktifkan);
    }
}
