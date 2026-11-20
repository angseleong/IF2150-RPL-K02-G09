package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

// Pengujian unit C54 BundelFHIREntity (HIUPL Subbab 4.2, C54-UT01 sampai C54-UT05).
@DisplayName("C54 BundelFHIREntity")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class BundelFHIREntityTest {

    private static BundelFHIREntity bundel(String status) {
        return new BundelFHIREntity(1, 21, "{\"resourceType\":\"Bundle\"}", status, null);
    }

    @Test
    @DisplayName("C54-UT01 tandaiTerkirim normal: mengubah status dan mengosongkan alasan")
    void tandaiTerkirim_mengubahStatusDanMengosongkanAlasan() {
        BundelFHIREntity b = bundel("gagal kirim");
        b.setAlasanGagal("Koneksi terputus");

        b.tandaiTerkirim("200");

        assertEquals("terkirim", b.getStatus());
        assertNull(b.getAlasanGagal());
    }

    @Test
    @DisplayName("C54-UT02 tandaiTerkirim negatif: bundel yang sudah terkirim ditolak")
    void tandaiTerkirim_menolakBundelYangSudahTerkirim() {
        BundelFHIREntity b = bundel("terkirim");
        assertThrows(IllegalStateException.class, () -> b.tandaiTerkirim("200"));
    }

    @Test
    @DisplayName("C54-UT03 tandaiGagal normal: mengisi status dan alasan")
    void tandaiGagal_mengisiStatusDanAlasan() {
        BundelFHIREntity b = bundel("tertunda");

        b.tandaiGagal("HTTP 422: data tidak lengkap");

        assertEquals("gagal kirim", b.getStatus());
        assertEquals("HTTP 422: data tidak lengkap", b.getAlasanGagal());
    }

    @Test
    @DisplayName("C54-UT04 tandaiGagal batas: alasan kosong menjadi bawaan")
    void tandaiGagal_alasanKosongMenjadiBawaan() {
        BundelFHIREntity b = bundel("tertunda");

        b.tandaiGagal(null);

        assertEquals("gagal kirim", b.getStatus());
        assertEquals("Ditolak tanpa alasan", b.getAlasanGagal());
    }

    @Test
    @DisplayName("C54-UT05 tandaiGagal negatif: bundel yang sudah terkirim ditolak")
    void tandaiGagal_menolakBundelYangSudahTerkirim() {
        BundelFHIREntity b = bundel("terkirim");
        assertThrows(IllegalStateException.class, () -> b.tandaiGagal("gagal"));
    }
}
