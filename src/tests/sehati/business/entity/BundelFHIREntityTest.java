package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BundelFHIREntityTest {

    private static BundelFHIREntity bundel(String status) {
        return new BundelFHIREntity(1, 21, "{\"resourceType\":\"Bundle\"}", status, null);
    }

    @Test
    void tandaiTerkirim_mengubahStatusDanMengosongkanAlasan() {
        BundelFHIREntity b = bundel("gagal kirim");
        b.setAlasanGagal("Koneksi terputus");

        b.tandaiTerkirim("200");

        assertEquals("terkirim", b.getStatus());
        assertNull(b.getAlasanGagal());
    }

    @Test
    void tandaiTerkirim_menolakBundelYangSudahTerkirim() {
        BundelFHIREntity b = bundel("terkirim");
        assertThrows(IllegalStateException.class, () -> b.tandaiTerkirim("200"));
    }

    @Test
    void tandaiGagal_mengisiStatusDanAlasan() {
        BundelFHIREntity b = bundel("tertunda");

        b.tandaiGagal("HTTP 422: data tidak lengkap");

        assertEquals("gagal kirim", b.getStatus());
        assertEquals("HTTP 422: data tidak lengkap", b.getAlasanGagal());
    }

    @Test
    void tandaiGagal_alasanKosongMenjadiBawaan() {
        BundelFHIREntity b = bundel("tertunda");

        b.tandaiGagal(null);

        assertEquals("gagal kirim", b.getStatus());
        assertEquals("Ditolak tanpa alasan", b.getAlasanGagal());
    }

    @Test
    void tandaiGagal_menolakBundelYangSudahTerkirim() {
        BundelFHIREntity b = bundel("terkirim");
        assertThrows(IllegalStateException.class, () -> b.tandaiGagal("gagal"));
    }
}
