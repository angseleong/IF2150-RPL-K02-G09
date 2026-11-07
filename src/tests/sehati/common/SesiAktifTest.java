package sehati.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import sehati.business.entity.SesiEntity;

class SesiAktifTest {

    @AfterEach
    void bersihkan() {
        SesiAktif.lepas();
    }

    @Test
    void idPenggunaSesi_mengembalikanPemilikSesi() {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 3, LocalDateTime.now(), true));
        assertEquals(3, SesiAktif.idPenggunaSesi());
    }

    @Test
    void idPenggunaSesi_menolakBilaBelumMasukAtauSesiDitutup() {
        assertThrows(IllegalStateException.class, SesiAktif::idPenggunaSesi);

        SesiEntity s = new SesiEntity(UUID.randomUUID(), 3, LocalDateTime.now(), true);
        SesiAktif.pasang(s);
        s.tutup();
        assertNull(SesiAktif.ambil());
        assertThrows(IllegalStateException.class, SesiAktif::idPenggunaSesi);
    }
}
