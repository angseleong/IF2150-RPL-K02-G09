package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class EntityResepTest {

    private static ResepEntity menunggu() {
        return new ResepEntity(1, 1, LocalDateTime.now(), ResepEntity.STATUS_MENUNGGU, null);
    }

    @Test
    void cukupStok_stokSamaDenganJumlahDianggapCukup() {
        ItemResepEntity item = new ItemResepEntity(0, 1, "500 mg", 10, "3 x 1");
        assertTrue(item.cukupStok(10));
        assertTrue(item.cukupStok(11));
        assertFalse(item.cukupStok(9));
    }

    @Test
    void perbaruiStatus_menungguKeStatusAkhir() {
        ResepEntity r = menunggu();
        r.perbaruiStatus(ResepEntity.STATUS_SELESAI_DILAYANI);
        assertEquals("selesai dilayani", r.getStatus());

        ResepEntity dikembalikan = menunggu();
        dikembalikan.perbaruiStatus(ResepEntity.STATUS_DIKEMBALIKAN);
        assertEquals("dikembalikan", dikembalikan.getStatus());
    }

    @Test
    void perbaruiStatus_menolakStatusAsingDanResepYangSudahSelesai() {
        ResepEntity r = menunggu();
        assertThrows(IllegalArgumentException.class, () -> r.perbaruiStatus(ResepEntity.STATUS_MENUNGGU));
        assertThrows(IllegalArgumentException.class, () -> r.perbaruiStatus("dibatalkan"));

        r.perbaruiStatus(ResepEntity.STATUS_SELESAI_DILAYANI);
        assertThrows(IllegalStateException.class, () -> r.perbaruiStatus(ResepEntity.STATUS_DIKEMBALIKAN));
    }

    @Test
    void tambahkan_entriGandaDiabaikan() {
        AntreanEntity a = new AntreanEntity(1, "apotek", new ArrayList<>(List.of(4)));
        a.tambahkan(7);
        a.tambahkan(4);
        assertEquals(List.of(4, 7), a.getDaftarEntri());
    }
}
