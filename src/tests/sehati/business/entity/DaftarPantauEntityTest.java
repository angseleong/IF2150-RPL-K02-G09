package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/** Pengujian unit C51 DaftarPantauEntity (HIUPL Subbab 4.2, C51-UT01 sampai C51-UT06). */
@DisplayName("C51 DaftarPantauEntity")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class DaftarPantauEntityTest {

    private static DaftarPantauEntity entri(String status) {
        return new DaftarPantauEntity(1, 7, "hipertensi", status);
    }

    @Test
    @DisplayName("C51-UT01 perbaruiStatus normal: belum, perlu dihubungi ulang, lalu selesai ditindaklanjuti")
    void perbaruiStatus_menerimaStatusSah() {
        DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_BELUM);

        e.perbaruiStatus(DaftarPantauEntity.STATUS_ULANG);
        assertEquals("perlu dihubungi ulang", e.getStatusTindakLanjut());
        e.perbaruiStatus(DaftarPantauEntity.STATUS_SELESAI);
        assertEquals("selesai ditindaklanjuti", e.getStatusTindakLanjut());
    }

    @Test
    @DisplayName("C51-UT02 perbaruiStatus batas: dari belum langsung selesai diterima")
    void perbaruiStatus_langsungSelesai() {
        DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_BELUM);

        e.perbaruiStatus(DaftarPantauEntity.STATUS_SELESAI);

        assertEquals(DaftarPantauEntity.STATUS_SELESAI, e.getStatusTindakLanjut());
    }

    @Test
    @DisplayName("C51-UT03 perbaruiStatus batas: entri yang perlu dihubungi ulang masih boleh diubah lagi")
    void perbaruiStatus_ulangMasihBolehDiubah() {
        DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_ULANG);

        e.perbaruiStatus(DaftarPantauEntity.STATUS_ULANG);

        assertEquals(DaftarPantauEntity.STATUS_ULANG, e.getStatusTindakLanjut());
    }

    @Test
    @DisplayName("C51-UT04 perbaruiStatus negatif: status yang tidak dikenal ditolak")
    void perbaruiStatus_menolakStatusTidakDikenal() {
        DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_BELUM);

        IllegalArgumentException x = assertThrows(IllegalArgumentException.class, () -> e.perbaruiStatus("ditunda"));

        assertEquals("Status tindak lanjut tidak dikenal: ditunda", x.getMessage());
        assertEquals(DaftarPantauEntity.STATUS_BELUM, e.getStatusTindakLanjut());
    }

    @Test
    @DisplayName("C51-UT05 perbaruiStatus negatif: entri yang sudah selesai tidak dapat diubah lagi")
    void perbaruiStatus_menolakPerubahanSetelahSelesai() {
        DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_SELESAI);

        IllegalStateException x = assertThrows(IllegalStateException.class,
                () -> e.perbaruiStatus(DaftarPantauEntity.STATUS_BELUM));

        assertEquals("Entri sudah selesai ditindaklanjuti", x.getMessage());
    }

    @Test
    @DisplayName("C51-UT06 perbaruiStatus negatif: status tidak dikenal diperiksa lebih dulu daripada status selesai")
    void perbaruiStatus_statusTidakDikenalDiperiksaSebelumStatusSelesai() {
        DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_SELESAI);

        assertThrows(IllegalArgumentException.class, () -> e.perbaruiStatus("ditunda"));
    }

    @Test
    @DisplayName("C52 TindakLanjutEntity terhubung: hanya hasil terhubung yang bernilai benar")
    void terhubung_hanyaUntukHasilTerhubung() {
        assertTrue(new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang").terhubung());
        assertFalse(new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui").terhubung());
    }
}
