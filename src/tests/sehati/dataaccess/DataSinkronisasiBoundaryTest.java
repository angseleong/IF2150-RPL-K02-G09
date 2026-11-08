package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import sehati.business.entity.BundelFHIREntity;

/**
 * Hanya simpanBundel() yang diisi pada UC07 karena dipakai ApotekManager.serahkanObat(). Operasi C63 lainnya
 * menyusul di #9.
 */
class DataSinkronisasiBoundaryTest {

    @Test
    void simpanBundel_menyisipkanBundelTertundaDanMengisiId() {
        JdbcPalsu jdbc = new JdbcPalsu().hasil("INSERT INTO bundel_fhir", List.of(JdbcPalsu.baris("id_bundel", 7)));
        BundelFHIREntity b = new BundelFHIREntity(0, 21, "{\"resourceType\":\"Bundle\"}", null, "lama");

        new DataSinkronisasiBoundary(jdbc.koneksi()).simpanBundel(b);

        assertEquals(7, b.getIdBundel());
        assertEquals("tertunda", b.getStatus());
        assertNull(b.getAlasanGagal());
        JdbcPalsu.Jalan jalan = jdbc.dijalankanMemuat("INSERT INTO bundel_fhir").get(0);
        assertEquals(21, jalan.parameter().get(1));
        assertEquals("{\"resourceType\":\"Bundle\"}", jalan.parameter().get(2));
    }

    @Test
    void simpanBundel_galatBasisDataMenjadiIllegalStateException() {
        JdbcPalsu jdbc = new JdbcPalsu().gagal("INSERT INTO bundel_fhir", "23505", "duplikat");
        BundelFHIREntity b = new BundelFHIREntity(0, 21, "{}", null, null);

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new DataSinkronisasiBoundary(jdbc.koneksi()).simpanBundel(b));

        assertEquals("Bundel FHIR kunjungan 21 gagal disimpan", e.getMessage());
        assertEquals(0, b.getIdBundel());
    }
}
