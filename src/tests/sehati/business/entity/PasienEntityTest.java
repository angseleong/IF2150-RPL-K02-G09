package sehati.business.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasienEntityTest {

    private PasienEntity pasienLama() {
        return new PasienEntity(7, "9999010101010001", "RM-000007", "Nama Lama",
                LocalDate.of(1990, 1, 1), "Perempuan", "Alamat Lama", "0800");
    }

    @Test
    void perbarui_menyalinDataDiriTanpaMengubahIdDanNomorRM() {
        PasienEntity lama = pasienLama();
        PasienEntity baru = new PasienEntity(99, "9999010101010002", "RM-999999", "Nama Baru",
                LocalDate.of(1991, 2, 2), "Laki-laki", "Alamat Baru", "0811");

        lama.perbarui(baru);

        assertEquals(7, lama.getIdPasien());
        assertEquals("RM-000007", lama.getNomorRekamMedis());
        assertEquals("9999010101010002", lama.getNik());
        assertEquals("Nama Baru", lama.getNama());
        assertEquals(LocalDate.of(1991, 2, 2), lama.getTanggalLahir());
        assertEquals("Laki-laki", lama.getJenisKelamin());
        assertEquals("Alamat Baru", lama.getAlamat());
        assertEquals("0811", lama.getNomorTelepon());
    }

    @Test
    void perbarui_nullDitolak() {
        PasienEntity pasien = pasienLama();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> pasien.perbarui(null));

        assertEquals("Data pasien kosong", e.getMessage());
    }
}
