package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// Pengujian unit C32 AuditEntity (HIUPL Subbab 4.2, C32-UT01 sampai C32-UT03).
@DisplayName("C32 AuditEntity")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class AuditEntityTest {

    @Test
    @DisplayName("C32-UT01 buat normal: pelaku, waktu saat ini, dan deskripsi terisi, idAudit 0")
    void buat_mengisiAtribut() {
        LocalDateTime sebelum = LocalDateTime.now();
        AuditEntity a = AuditEntity.buat(7, "Masuk ke aplikasi");
        assertEquals(0, a.getIdAudit());
        assertEquals(7, a.getIdPengguna());
        assertEquals("Masuk ke aplikasi", a.getDeskripsi());
        assertFalse(a.getWaktu().isBefore(sebelum));
        assertFalse(a.getWaktu().isAfter(LocalDateTime.now()));
    }

    @Test
    @DisplayName("C32-UT02 buat batas: pelaku 0 (nama tidak dikenal) dan deskripsi satu karakter diterima")
    void buat_pelakuNol() {
        AuditEntity a = AuditEntity.buat(0, "x");
        assertEquals(0, a.getIdPengguna());
        assertEquals("x", a.getDeskripsi());
    }

    @Test
    @DisplayName("C32-UT03 buat negatif: deskripsi null, kosong, atau spasi ditolak")
    void buat_deskripsiKosong() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> AuditEntity.buat(7, null));
        assertEquals("Deskripsi audit wajib diisi", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> AuditEntity.buat(7, ""));
        assertThrows(IllegalArgumentException.class, () -> AuditEntity.buat(7, "  "));
    }
}
