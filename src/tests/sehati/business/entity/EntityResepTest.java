package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Pengujian unit entity resep (HIUPL Subbab 4.2): C46 ResepEntity dan C47 ItemResepEntity. Test berawalan
 * "Pendukung" menguji kelas milik modul lain dan tidak diberi ID.
 */
@DisplayName("Entity resep (C46, C47)")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class EntityResepTest {

    private static ResepEntity menunggu() {
        return new ResepEntity(1, 1, LocalDateTime.now(), ResepEntity.STATUS_MENUNGGU, null);
    }

    @Test
    @DisplayName("C46-UT01 perbaruiStatus normal: resep menunggu menjadi selesai dilayani")
    void perbaruiStatus_selesai() {
        ResepEntity r = menunggu();
        r.perbaruiStatus(ResepEntity.STATUS_SELESAI_DILAYANI);
        assertEquals("selesai dilayani", r.getStatus());
    }

    @Test
    @DisplayName("C46-UT02 perbaruiStatus batas: resep menunggu dikembalikan ke dokter")
    void perbaruiStatus_dikembalikan() {
        ResepEntity r = menunggu();
        r.perbaruiStatus(ResepEntity.STATUS_DIKEMBALIKAN);
        assertEquals("dikembalikan", r.getStatus());
    }

    @Test
    @DisplayName("C46-UT03 perbaruiStatus negatif: status asing dan kembali ke menunggu ditolak")
    void perbaruiStatus_statusAsing() {
        ResepEntity r = menunggu();
        assertThrows(IllegalArgumentException.class, () -> r.perbaruiStatus(ResepEntity.STATUS_MENUNGGU));
        assertThrows(IllegalArgumentException.class, () -> r.perbaruiStatus("dibatalkan"));
        assertEquals("menunggu", r.getStatus());
    }

    @Test
    @DisplayName("C46-UT04 perbaruiStatus negatif: resep yang sudah selesai dilayani tidak dapat dikembalikan")
    void perbaruiStatus_sudahSelesai() {
        ResepEntity r = menunggu();
        r.perbaruiStatus(ResepEntity.STATUS_SELESAI_DILAYANI);
        assertThrows(IllegalStateException.class, () -> r.perbaruiStatus(ResepEntity.STATUS_DIKEMBALIKAN));
        assertEquals("selesai dilayani", r.getStatus());
    }

    @Test
    @DisplayName("C47-UT01 cukupStok normal: stok lebih dari jumlah dianggap cukup")
    void cukupStok_normal() {
        assertTrue(new ItemResepEntity(0, 1, "500 mg", 10, "3 x 1").cukupStok(11));
    }

    @Test
    @DisplayName("C47-UT02 cukupStok batas: stok sama dengan jumlah dianggap cukup")
    void cukupStok_sama() {
        assertTrue(new ItemResepEntity(0, 1, "500 mg", 10, "3 x 1").cukupStok(10));
    }

    @Test
    @DisplayName("C47-UT03 cukupStok negatif: stok kurang satu atau nol tidak cukup")
    void cukupStok_kurang() {
        ItemResepEntity item = new ItemResepEntity(0, 1, "500 mg", 10, "3 x 1");
        assertFalse(item.cukupStok(9));
        assertFalse(item.cukupStok(0));
    }

    @Test
    @DisplayName("Pendukung C37 tambahkan: entri ganda pada antrean diabaikan")
    void tambahkan_entriGandaDiabaikan() {
        AntreanEntity a = new AntreanEntity(1, "apotek", new ArrayList<>(List.of(4)));
        a.tambahkan(7);
        a.tambahkan(4);
        assertEquals(List.of(4, 7), a.getDaftarEntri());
    }
}
