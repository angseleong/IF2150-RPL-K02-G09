package sehati.dataaccess;

import org.junit.jupiter.api.Test;
import sehati.business.entity.AntreanEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PoliEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sehati.dataaccess.JdbcPalsu.baris;

class DataKunjunganBoundaryTest {

    private static final LocalDate HARI_INI = LocalDate.of(2026, 11, 7);

    @Test
    void kunciPoliMemakaiForUpdateDanMemetakanBaris() {
        JdbcPalsu palsu = new JdbcPalsu().hasil("FROM poli WHERE id_poli", List.of(
                baris("id_poli", 2, "nama_poli", "Poli Gigi", "urutan_terakhir", 4,
                        "tanggal_urutan", Date.valueOf("2026-11-07"))));

        PoliEntity p = new DataKunjunganBoundary(palsu.koneksi()).kunciPoli(2);

        assertEquals("Poli Gigi", p.getNamaPoli());
        assertEquals(4, p.getUrutanTerakhir());
        assertEquals(HARI_INI, p.getTanggalUrutan());
        assertTrue(palsu.dijalankan.get(0).sql().endsWith("FOR UPDATE"));
    }

    @Test
    void kunciPoliYangTidakAdaMengembalikanNull() {
        assertNull(new DataKunjunganBoundary(new JdbcPalsu().koneksi()).kunciPoli(99));
    }

    @Test
    void simpanPoliBaruMengisiIdDariBasisData() {
        JdbcPalsu palsu = new JdbcPalsu().hasil("INSERT INTO poli", List.of(baris("id_poli", 5)));
        PoliEntity p = new PoliEntity(0, "Poli Mata", 0, HARI_INI);

        new DataKunjunganBoundary(palsu.koneksi()).simpanPoli(p);

        assertEquals(5, p.getIdPoli());
    }

    @Test
    void simpanPoliLamaMemutakhirkanNamaDanUrutan() {
        JdbcPalsu palsu = new JdbcPalsu();

        new DataKunjunganBoundary(palsu.koneksi()).simpanPoli(new PoliEntity(3, "Poli KIA", 7, HARI_INI));

        JdbcPalsu.Jalan jalan = palsu.dijalankanMemuat("UPDATE poli").get(0);
        assertEquals("Poli KIA", jalan.parameter().get(1));
        assertEquals(7, jalan.parameter().get(2));
        assertEquals(3, jalan.parameter().get(4));
    }

    @Test
    void namaPoliGandaDitolakDenganPesanJelas() {
        JdbcPalsu palsu = new JdbcPalsu().gagal("INSERT INTO poli", "23505",
                "ERROR: duplicate key value violates unique constraint \"poli_nama_poli_key\"");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DataKunjunganBoundary(palsu.koneksi()).simpanPoli(new PoliEntity(0, "Poli Umum", 0, HARI_INI)));

        assertEquals("Nama poli sudah dipakai", e.getMessage());
    }

    @Test
    void kunjunganAktifMengembalikanNullBilaTidakAda() {
        assertNull(new DataKunjunganBoundary(new JdbcPalsu().koneksi()).kunjunganAktif(1, HARI_INI));
    }

    @Test
    void kunjunganAktifHanyaMencariYangBelumSelesaiPadaTanggalItu() {
        JdbcPalsu palsu = new JdbcPalsu().hasil("status <> 'selesai'", List.of(
                baris("id_kunjungan", 9, "id_pasien", 1, "id_poli", 2, "tanggal", Date.valueOf("2026-11-07"),
                        "nomor_antrean", 3, "status", "menunggu skrining")));

        KunjunganEntity k = new DataKunjunganBoundary(palsu.koneksi()).kunjunganAktif(1, HARI_INI);

        assertEquals(9, k.getIdKunjungan());
        assertEquals("menunggu skrining", k.getStatus());
        assertEquals(1, palsu.dijalankan.get(0).parameter().get(1));
        assertEquals(Date.valueOf(HARI_INI), palsu.dijalankan.get(0).parameter().get(2));
    }

    @Test
    void simpanKunjunganBaruMengisiIdDanKunjunganLamaHanyaMengubahStatus() {
        JdbcPalsu palsu = new JdbcPalsu().hasil("INSERT INTO kunjungan", List.of(baris("id_kunjungan", 11)));
        DataKunjunganBoundary boundary = new DataKunjunganBoundary(palsu.koneksi());
        KunjunganEntity k = new KunjunganEntity(0, 1, 2, HARI_INI, 5, "menunggu skrining");

        boundary.simpanKunjungan(k);
        assertEquals(11, k.getIdKunjungan());

        k.setStatus("sedang diskrining");
        boundary.simpanKunjungan(k);
        JdbcPalsu.Jalan ubah = palsu.dijalankanMemuat("UPDATE kunjungan").get(0);
        assertEquals("UPDATE kunjungan SET status = ? WHERE id_kunjungan = ?", ubah.sql());
        assertEquals("sedang diskrining", ubah.parameter().get(1));
        assertEquals(11, ubah.parameter().get(2));
    }

    @Test
    void kunjunganAktifGandaDitolakDenganPesanJelas() {
        JdbcPalsu palsu = new JdbcPalsu().gagal("INSERT INTO kunjungan", "23505",
                "ERROR: duplicate key value violates unique constraint \"uq_kunjungan_aktif\"");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DataKunjunganBoundary(palsu.koneksi())
                        .simpanKunjungan(new KunjunganEntity(0, 1, 2, HARI_INI, 5, "menunggu skrining")));

        assertEquals("Pasien sudah memiliki kunjungan aktif hari ini", e.getMessage());
    }

    @Test
    void ambilAntreanMembacaIsiTerurutDenganKunci() {
        JdbcPalsu palsu = new JdbcPalsu()
                .hasil("FROM antrean WHERE jenis", List.of(baris("id_antrean", 1, "jenis", "skrining")))
                .hasil("FROM antrean_entri", List.of(baris("id_entri", 4), baris("id_entri", 2), baris("id_entri", 9)));

        AntreanEntity a = new DataKunjunganBoundary(palsu.koneksi()).ambilAntrean("skrining");

        assertEquals("skrining", a.getJenis());
        assertEquals(List.of(4, 2, 9), a.getDaftarEntri());
        assertTrue(palsu.dijalankan.get(0).sql().endsWith("FOR UPDATE"));
        assertTrue(palsu.dijalankan.get(1).sql().endsWith("ORDER BY urutan"));
    }

    @Test
    void jenisAntreanTidakDikenalDitolak() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DataKunjunganBoundary(new JdbcPalsu().koneksi()).ambilAntrean("lab"));

        assertEquals("Jenis antrean tidak dikenal: lab", e.getMessage());
    }

    @Test
    void simpanAntreanMenulisUlangDenganUrutanSatuSampaiNDalamSatuTransaksi() {
        JdbcPalsu palsu = new JdbcPalsu();
        AntreanEntity a = new AntreanEntity(1, "skrining", new ArrayList<>(List.of(8, 3, 5)));

        new DataKunjunganBoundary(palsu.koneksi()).simpanAntrean(a);

        assertTrue(palsu.dijalankan.get(0).sql().startsWith("DELETE FROM antrean_entri"));
        List<JdbcPalsu.Jalan> sisip = palsu.dijalankanMemuat("INSERT INTO antrean_entri");
        assertEquals(3, sisip.size());
        for (int i = 0; i < 3; i++) {
            assertEquals(1, sisip.get(i).parameter().get(1));
            assertEquals(i + 1, sisip.get(i).parameter().get(2));
            assertEquals(a.getDaftarEntri().get(i), sisip.get(i).parameter().get(3));
        }
        assertEquals(List.of("autoCommit=false", "commit", "autoCommit=true"), palsu.peristiwa);
    }

    @Test
    void simpanAntreanYangGagalDibatalkanSeluruhnya() {
        JdbcPalsu palsu = new JdbcPalsu().gagal("INSERT INTO antrean_entri", "08006", "putus");

        assertThrows(IllegalStateException.class, () -> new DataKunjunganBoundary(palsu.koneksi())
                .simpanAntrean(new AntreanEntity(1, "skrining", new ArrayList<>(List.of(1)))));

        assertEquals(List.of("autoCommit=false", "rollback", "autoCommit=true"), palsu.peristiwa);
    }
}
