package sehati.business.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KunjunganEntityTest {

    private KunjunganEntity kunjunganMenunggu() {
        return new KunjunganEntity(1, 10, 2, LocalDate.now(), 3, "menunggu skrining");
    }

    @Test
    void perbaruiStatus_menerimaSeluruhStatusSah() {
        for (String status : new String[] {"menunggu skrining", "sedang diskrining", "tidak hadir",
                "siap diperiksa dokter", "selesai diperiksa", "selesai"}) {
            KunjunganEntity k = kunjunganMenunggu();
            k.perbaruiStatus(status);
            assertEquals(status, k.getStatus());
        }
    }

    @Test
    void perbaruiStatus_menolakStatusTidakDikenal() {
        KunjunganEntity k = kunjunganMenunggu();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> k.perbaruiStatus("sedang menunggu"));

        assertEquals("Status kunjungan tidak dikenal: sedang menunggu", e.getMessage());
    }

    @Test
    void perbaruiStatus_menolakStatusNull() {
        KunjunganEntity k = kunjunganMenunggu();

        assertThrows(IllegalArgumentException.class, () -> k.perbaruiStatus(null));
    }

    @Test
    void perbaruiStatus_kunjunganSelesaiDikunci() {
        KunjunganEntity k = kunjunganMenunggu();
        k.perbaruiStatus("selesai");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> k.perbaruiStatus("siap diperiksa dokter"));

        assertEquals("Kunjungan sudah selesai dan tidak dapat diubah", e.getMessage());
    }

    @Test
    void aktifHariIni_benarBilaTanggalHariIniDanBelumSelesai() {
        assertTrue(kunjunganMenunggu().aktifHariIni());
    }

    @Test
    void aktifHariIni_salahBilaTanggalKemarin() {
        KunjunganEntity k = new KunjunganEntity(1, 10, 2, LocalDate.now().minusDays(1), 3,
                "menunggu skrining");

        assertFalse(k.aktifHariIni());
    }

    @Test
    void aktifHariIni_salahBilaSudahSelesai() {
        KunjunganEntity k = kunjunganMenunggu();
        k.perbaruiStatus("selesai");

        assertFalse(k.aktifHariIni());
    }
}
