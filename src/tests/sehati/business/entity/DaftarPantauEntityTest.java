package sehati.business.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DaftarPantauEntityTest {

    private static DaftarPantauEntity entri(String status) {
        return new DaftarPantauEntity(1, 7, "hipertensi", status);
    }

    @Test
    void perbaruiStatus_menerimaStatusSah() {
        DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_BELUM);
        e.perbaruiStatus(DaftarPantauEntity.STATUS_ULANG);
        assertEquals("perlu dihubungi ulang", e.getStatusTindakLanjut());
        e.perbaruiStatus(DaftarPantauEntity.STATUS_SELESAI);
        assertEquals("selesai ditindaklanjuti", e.getStatusTindakLanjut());
    }

    @Test
    void perbaruiStatus_menolakStatusTidakDikenal() {
        assertThrows(IllegalArgumentException.class, () -> entri("belum ditindaklanjuti").perbaruiStatus("ditunda"));
    }

    @Test
    void perbaruiStatus_menolakPerubahanSetelahSelesai() {
        DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_SELESAI);
        assertThrows(IllegalStateException.class, () -> e.perbaruiStatus(DaftarPantauEntity.STATUS_BELUM));
    }

    @Test
    void perbaruiStatus_statusTidakDikenalDiperiksaSebelumStatusSelesai() {
        DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_SELESAI);
        assertThrows(IllegalArgumentException.class, () -> e.perbaruiStatus("ditunda"));
    }

    @Test
    void terhubung_hanyaUntukHasilTerhubung() {
        assertTrue(new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang").terhubung());
        assertFalse(new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui").terhubung());
    }
}
