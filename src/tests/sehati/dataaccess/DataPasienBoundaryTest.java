package sehati.dataaccess;

import org.junit.jupiter.api.Test;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.RekamMedisEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sehati.dataaccess.JdbcPalsu.baris;

class DataPasienBoundaryTest {

    private static PasienEntity pasienBaru() {
        return new PasienEntity(0, "9999010101010001", "RM-000010", "Dewi Lestari", LocalDate.of(1990, 5, 17),
                "Perempuan", "Jl. Kenari No. 5", null);
    }

    @Test
    void polaPencarianMeloloskanWildcardDanMemangkasSpasi() {
        assertEquals("%budi%", DataPasienBoundary.polaPencarian("  budi "));
        assertEquals("%50\\%\\_a\\\\b%", DataPasienBoundary.polaPencarian("50%_a\\b"));
        assertEquals("%%", DataPasienBoundary.polaPencarian(null));
    }

    @Test
    void pasienTerbaruUrutBaruKeLamaDanDibatasi50() {
        JdbcPalsu palsu = new JdbcPalsu().hasil("FROM pasien ORDER BY id_pasien DESC", List.of(
                baris("id_pasien", 9, "nik", "9999010101990009", "nomor_rekam_medis", "RM-000009",
                        "nama", "Pasien Baru", "tanggal_lahir", Date.valueOf("1999-08-15"),
                        "jenis_kelamin", "Perempuan", "alamat", "Jl. Uji", "nomor_telepon", null)));

        List<PasienEntity> hasil = new DataPasienBoundary(palsu.koneksi()).pasienTerbaru();

        assertEquals(1, hasil.size());
        assertEquals("RM-000009", hasil.get(0).getNomorRekamMedis());
        assertTrue(palsu.dijalankan.get(0).sql().endsWith("ORDER BY id_pasien DESC LIMIT 50"));
    }

    @Test
    void cariParsialMengikatPolaYangSamaKeTigaParameter() {
        JdbcPalsu palsu = new JdbcPalsu().hasil("FROM pasien WHERE nik LIKE", List.of(
                baris("id_pasien", 1, "nik", "9999010101680001", "nomor_rekam_medis", "RM-000001",
                        "nama", "Budi Santoso", "tanggal_lahir", Date.valueOf("1968-03-12"),
                        "jenis_kelamin", "Laki-laki", "alamat", "Jl. Melati No. 1", "nomor_telepon", null)));

        List<PasienEntity> hasil = new DataPasienBoundary(palsu.koneksi()).cariParsial("bud%");

        assertEquals(1, hasil.size());
        assertEquals("Budi Santoso", hasil.get(0).getNama());
        assertEquals(LocalDate.of(1968, 3, 12), hasil.get(0).getTanggalLahir());
        assertNull(hasil.get(0).getNomorTelepon());
        JdbcPalsu.Jalan jalan = palsu.dijalankan.get(0);
        assertEquals("%bud\\%%", jalan.parameter().get(1));
        assertEquals(jalan.parameter().get(1), jalan.parameter().get(2));
        assertEquals(jalan.parameter().get(1), jalan.parameter().get(3));
        assertFalse(jalan.sql().contains("bud"), "kata kunci tidak boleh disambung ke teks kueri");
        assertTrue(jalan.sql().contains("LIMIT 50"));
    }

    @Test
    void ambilPasienMengembalikanNullBilaTidakAda() {
        assertNull(new DataPasienBoundary(new JdbcPalsu().koneksi()).ambilPasien(99));
    }

    @Test
    void simpanPasienBaruDiLuarTransaksiMembungkusDuaKueriDalamSatuTransaksi() {
        JdbcPalsu palsu = new JdbcPalsu()
                .hasil("INSERT INTO pasien", List.of(baris("id_pasien", 7)))
                .hasil("INSERT INTO rekam_medis", List.of(baris("id_rekam_medis", 3)));
        PasienEntity p = pasienBaru();

        new DataPasienBoundary(palsu.koneksi()).simpanPasien(p);

        assertEquals(7, p.getIdPasien());
        assertEquals(List.of("autoCommit=false", "commit", "autoCommit=true"), palsu.peristiwa);
        assertEquals(1, palsu.dijalankanMemuat("INSERT INTO pasien").size());
        assertEquals(7, palsu.dijalankanMemuat("INSERT INTO rekam_medis").get(0).parameter().get(1));
        assertNull(palsu.dijalankanMemuat("INSERT INTO pasien").get(0).parameter().get(7),
                "telepon kosong disimpan sebagai NULL");
    }

    @Test
    void simpanPasienBaruTanpaNomorRMMengambilDariSequence() {
        JdbcPalsu palsu = new JdbcPalsu()
                .hasil("nextval", List.of(baris("rm", "RM-000042")))
                .hasil("INSERT INTO pasien", List.of(baris("id_pasien", 8)))
                .hasil("INSERT INTO rekam_medis", List.of(baris("id_rekam_medis", 4)));
        PasienEntity p = pasienBaru();
        p.setNomorRekamMedis(null);

        new DataPasienBoundary(palsu.koneksi()).simpanPasien(p);

        assertEquals("RM-000042", p.getNomorRekamMedis());
    }

    @Test
    void simpanPasienDalamTransaksiManagerTidakMenutupTransaksi() {
        JdbcPalsu palsu = new JdbcPalsu().mulaiDalamTransaksi()
                .hasil("INSERT INTO pasien", List.of(baris("id_pasien", 7)))
                .hasil("INSERT INTO rekam_medis", List.of(baris("id_rekam_medis", 3)));

        new DataPasienBoundary(palsu.koneksi()).simpanPasien(pasienBaru());

        assertTrue(palsu.peristiwa.isEmpty(), "commit dan rollback milik Manager");
        assertFalse(palsu.autoCommit());
    }

    @Test
    void nikGandaDitolakDenganPesanJelasDanTransaksiDibatalkan() {
        JdbcPalsu palsu = new JdbcPalsu().gagal("INSERT INTO pasien", "23505",
                "ERROR: duplicate key value violates unique constraint \"pasien_nik_key\"");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DataPasienBoundary(palsu.koneksi()).simpanPasien(pasienBaru()));

        assertEquals("NIK sudah terdaftar", e.getMessage());
        assertEquals(List.of("autoCommit=false", "rollback", "autoCommit=true"), palsu.peristiwa);
    }

    @Test
    void galatKoneksiTidakMembocorkanRincianTeknis() {
        JdbcPalsu palsu = new JdbcPalsu().gagal("FROM pasien", "08006", "connection to 10.0.0.5 refused");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new DataPasienBoundary(palsu.koneksi()).ambilPasien(1));

        assertEquals("Basis data tidak dapat dihubungi", e.getMessage());
    }

    @Test
    void simpanPasienLamaTidakMenyentuhNomorRMDanMenolakPasienYangTidakAda() {
        JdbcPalsu palsu = new JdbcPalsu().terubah("UPDATE pasien", 0);
        PasienEntity p = pasienBaru();
        p.setIdPasien(5);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DataPasienBoundary(palsu.koneksi()).simpanPasien(p));

        assertEquals("Pasien tidak ditemukan", e.getMessage());
        assertFalse(palsu.dijalankanMemuat("UPDATE pasien").get(0).sql().contains("nomor_rekam_medis"));
        assertEquals(5, palsu.dijalankanMemuat("UPDATE pasien").get(0).parameter().get(7));
    }

    @Test
    void ambilRekamMedisMenyusunDiagnosisDanRiwayatObat() {
        JdbcPalsu palsu = new JdbcPalsu()
                .hasil("SELECT id_rekam_medis", List.of(baris("id_rekam_medis", 2)))
                .hasil("FROM diagnosis", List.of(baris("id_diagnosis", 1, "kode_icd10", "I10",
                        "deskripsi", "Hipertensi esensial")))
                .hasil("FROM item_resep", List.of(baris("tanggal", Date.valueOf("2026-10-12"),
                        "nama_obat", "Amlodipin 5 mg", "sediaan", "tablet", "jumlah", 30, "dosis", "1x1",
                        "aturan_pakai", "sesudah makan")));

        RekamMedisEntity rm = new DataPasienBoundary(palsu.koneksi()).ambilRekamMedis(1);

        assertEquals(2, rm.getIdRekamMedis());
        assertEquals("I10", rm.getDaftarDiagnosis().get(0).getKodeICD10());
        assertEquals(List.of("2026-10-12 Amlodipin 5 mg tablet ×30, 1x1 sesudah makan"), rm.getRiwayatObat());
    }

    @Test
    void ambilRekamMedisMengembalikanNullBilaBelumAda() {
        assertNull(new DataPasienBoundary(new JdbcPalsu().koneksi()).ambilRekamMedis(1));
    }

    @Test
    void simpanRekamMedisMenyisipkanSetiapEntriBaruLaluMengosongkannya() {
        JdbcPalsu palsu = new JdbcPalsu();
        RekamMedisEntity rm = new RekamMedisEntity();
        rm.setIdRekamMedis(2);
        rm.tambahEntri("Tindak lanjut telepon: terhubung");
        rm.tambahEntri("Pasien akan datang");

        new DataPasienBoundary(palsu.koneksi()).simpanRekamMedis(rm);

        List<JdbcPalsu.Jalan> jalan = palsu.dijalankanMemuat("catatan_rekam_medis");
        assertEquals(2, jalan.size());
        assertEquals("Pasien akan datang", jalan.get(1).parameter().get(3));
        assertTrue(rm.getEntriBaru().isEmpty());
        assertEquals(List.of("autoCommit=false", "commit", "autoCommit=true"), palsu.peristiwa);
    }

    @Test
    void simpanRekamMedisTanpaEntriBaruTidakMenyentuhBasisData() {
        JdbcPalsu palsu = new JdbcPalsu();

        new DataPasienBoundary(palsu.koneksi()).simpanRekamMedis(new RekamMedisEntity());

        assertTrue(palsu.dijalankan.isEmpty());
        assertTrue(palsu.peristiwa.isEmpty());
    }
}
