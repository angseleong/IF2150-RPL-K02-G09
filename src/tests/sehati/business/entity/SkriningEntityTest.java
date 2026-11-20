package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/** Pengujian unit C38 SkriningEntity (HIUPL Subbab 4.2, C38-UT01 sampai C38-UT03). */
@DisplayName("C38 SkriningEntity")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SkriningEntityTest {

    @Test
    @DisplayName("C38-UT01 lampirkanTandaVital normal: tanda vital disimpan sebagai referensi")
    void lampirkanTandaVital_menyimpanReferensi() {
        SkriningEntity s = new SkriningEntity(0, 1, "Pusing", LocalDateTime.now());
        TandaVitalEntity tv = new TandaVitalEntity(120, 80, 60, 160, 36.5, 72, null);

        s.lampirkanTandaVital(tv);

        assertSame(tv, s.getTandaVital());
    }

    @Test
    @DisplayName("C38-UT02 lampirkanTandaVital negatif: tanda vital kosong ditolak dan referensi lama tetap")
    void lampirkanTandaVital_menolakNull() {
        SkriningEntity s = new SkriningEntity(0, 1, "Pusing", LocalDateTime.now());
        TandaVitalEntity tv = new TandaVitalEntity(120, 80, 60, 160, 36.5, 72, null);
        s.lampirkanTandaVital(tv);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> s.lampirkanTandaVital(null));

        assertEquals("Tanda vital tidak boleh kosong", e.getMessage());
        assertSame(tv, s.getTandaVital());
    }

    @Test
    @DisplayName("C38-UT03 konstruktor batas: skrining baru belum memiliki tanda vital")
    void konstruktor_belumAdaTandaVital() {
        LocalDateTime waktu = LocalDateTime.of(2026, 11, 4, 9, 30);

        SkriningEntity s = new SkriningEntity(7, 3, "", waktu);

        assertNull(s.getTandaVital());
        assertEquals(7, s.getIdSkrining());
        assertEquals(3, s.getIdKunjungan());
        assertEquals(waktu, s.getWaktu());
    }
}
