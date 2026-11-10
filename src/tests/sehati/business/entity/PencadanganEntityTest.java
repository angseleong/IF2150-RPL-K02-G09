package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class PencadanganEntityTest {

    private static PencadanganEntity dengan(String status) {
        return new PencadanganEntity(1, LocalDateTime.now(), status, "/media/a.dump", 1);
    }

    @Test
    void berhasil_hanyaStatusBerhasil() {
        assertTrue(dengan("berhasil").berhasil());
        assertFalse(dengan("gagal").berhasil());
        assertFalse(dengan("gagal verifikasi").berhasil());
    }
}
