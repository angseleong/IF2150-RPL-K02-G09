package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AuditEntityTest {

    @Test
    void buat_mengisiPelakuWaktuDanDeskripsi() {
        AuditEntity a = AuditEntity.buat(7, "Masuk ke aplikasi");
        assertEquals(0, a.getIdAudit());
        assertEquals(7, a.getIdPengguna());
        assertEquals("Masuk ke aplikasi", a.getDeskripsi());
        assertNotNull(a.getWaktu());
    }

    @Test
    void buat_menolakDeskripsiKosong() {
        assertThrows(IllegalArgumentException.class, () -> AuditEntity.buat(7, null));
        assertThrows(IllegalArgumentException.class, () -> AuditEntity.buat(7, "  "));
    }
}
