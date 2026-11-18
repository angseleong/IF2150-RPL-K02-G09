package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KodeICD10Entity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.SesiEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.common.SesiAktif;

/**
 * Pengujian unit C22 PemeriksaanManager (HIUPL Subbab 4.2, C22-UT01 sampai C22-UT37). Setiap test diberi
 * {@code @DisplayName} berawalan ID test case.
 */
@DisplayName("C22 PemeriksaanManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class PemeriksaanManagerTest {

    private final PemeriksaanManager manager = new PemeriksaanManager();

    @AfterEach
    void lepasSesi() {
        SesiAktif.lepas();
    }

    @Test
    @DisplayName("C22-UT10 cariKodeICD10 negatif: kata kunci kosong mengembalikan daftar kosong")
    void cariKodeICD10_kataKunciKosong() {
        assertTrue(manager.cariKodeICD10("  ").isEmpty());
        assertTrue(manager.cariKodeICD10(null).isEmpty());
    }

    @Test
    @DisplayName("C22-UT13 simpanPemeriksaan negatif: diagnosis tanpa kode ICD-10 ditolak")
    void simpanPemeriksaan_diagnosisTanpaKode() {
        PemeriksaanEntity p = new PemeriksaanEntity(0, 1, "Pusing", "Edukasi", false);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.simpanPemeriksaan(1, p, new DiagnosisEntity(0, " ", "Tanpa kode")));
        assertEquals("Diagnosis harus dipilih dari kode ICD-10", e.getMessage());
    }

    @Test
    @DisplayName("C22-UT15 simpanPemeriksaan negatif: peran Perawat ditolak (UC05 alternatif 1)")
    void simpanPemeriksaan_perawatDitolak() {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 2, LocalDateTime.now(), true), "Perawat");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.simpanPemeriksaan(1,
                new PemeriksaanEntity(0, 0, "Pusing", "Edukasi", false), new DiagnosisEntity(0, "R51", "Sakit kepala")));
        assertEquals("Peran Perawat tidak berwenang menjalankan tindakan ini", e.getMessage());
    }

    @Test
    @DisplayName("C22-UT19 jadwalkanKontrol negatif: tanggal hari ini dan kosong ditolak")
    void jadwalkanKontrol_hariIniAtauKosongDitolak() {
        assertThrows(IllegalArgumentException.class, () -> manager.jadwalkanKontrol(1, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> manager.jadwalkanKontrol(1, null));
    }

    @Test
    @DisplayName("C22-UT27 kunciRekamMedis negatif: tanpa sesi masuk ditolak")
    void kunciRekamMedis_tanpaSesi() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.kunciRekamMedis(1));
        assertEquals("Sesi sudah berakhir, silakan masuk kembali", e.getMessage());
    }

    @Test
    @DisplayName("C22-UT28 validasiTanggalKontrol normal: tanggal besok diterima")
    void validasiTanggalKontrol_besok() {
        assertDoesNotThrow(() -> manager.validasiTanggalKontrol(LocalDate.now().plusDays(1)));
    }

    @Test
    @DisplayName("C22-UT29 validasiTanggalKontrol batas: tanggal hari ini ditolak")
    void validasiTanggalKontrol_hariIni() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.validasiTanggalKontrol(LocalDate.now()));
        assertEquals("Tanggal kontrol harus setelah hari ini", e.getMessage());
    }

    @Test
    @DisplayName("C22-UT30 validasiTanggalKontrol negatif: tanggal kosong dan kemarin ditolak")
    void validasiTanggalKontrol_kosongAtauLampau() {
        assertThrows(IllegalArgumentException.class, () -> manager.validasiTanggalKontrol(null));
        assertThrows(IllegalArgumentException.class,
                () -> manager.validasiTanggalKontrol(LocalDate.now().minusDays(1)));
    }

    /**
     * Manager membuka koneksi sendiri lewat KoneksiBasisData, jadi variabel DB_* harus menunjuk basis data yang sama
     * dengan DB_UJI_URL (berisi schema.sql dan seed.sql). Data yang dibuat dihapus setelah setiap uji.
     */
    @Nested
    @DisplayName("Dengan basis data uji")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private static final LocalDate TANGGAL_UJI = LocalDate.of(2099, 1, 1);

        private Connection koneksi;
        private int idKunjungan;
        private int idPasien;
        private String statusAwal;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            // Kunjungan belum diperiksa dan belum ditutup, sehingga dapat dikunci oleh uji.
            idKunjungan = hitung("SELECT min(k.id_kunjungan) FROM kunjungan k WHERE k.status <> 'selesai' "
                    + "AND NOT EXISTS (SELECT 1 FROM pemeriksaan p WHERE p.id_kunjungan = k.id_kunjungan)");
            idPasien = hitung("SELECT id_pasien FROM kunjungan WHERE id_kunjungan = " + idKunjungan);
            try (Statement st = koneksi.createStatement();
                 var rs = st.executeQuery("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan)) {
                rs.next();
                statusAwal = rs.getString(1);
            }
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Dokter");
        }

        @AfterEach
        void bersihkan() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM diagnosis WHERE id_pemeriksaan IN "
                        + "(SELECT id_pemeriksaan FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan + ")");
                st.executeUpdate("DELETE FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("DELETE FROM resep WHERE id_kunjungan = " + idKunjungan + " AND catatan = 'uji C22'");
                st.executeUpdate("DELETE FROM daftar_pantau WHERE id_jadwal IN (SELECT id_jadwal FROM jadwal_kontrol "
                        + "WHERE id_pasien = " + idPasien + " AND tanggal_rencana IN (CURRENT_DATE, CURRENT_DATE + 1, "
                        + "DATE '2099-01-01'))");
                st.executeUpdate("DELETE FROM jadwal_kontrol WHERE id_pasien = " + idPasien
                        + " AND tanggal_rencana IN (CURRENT_DATE, CURRENT_DATE + 1, DATE '2099-01-01')");
                st.executeUpdate("DELETE FROM jadwal_kontrol WHERE tanggal_rencana = DATE '2099-01-01'");
                st.executeUpdate("DELETE FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("DELETE FROM audit WHERE deskripsi IN ('Pemeriksaan kunjungan " + idKunjungan
                        + " dikunci', 'Pemeriksaan kunjungan " + idKunjungan + " disimpan')");
                st.executeUpdate("UPDATE kunjungan SET status = '" + statusAwal + "' WHERE id_kunjungan = "
                        + idKunjungan);
            }
            koneksi.close();
        }

        @Test
        @DisplayName("C22-UT01 susunRingkasan normal: memuat data diri, skrining hari ini, dan penanda risiko")
        void susunRingkasan_normal() throws SQLException {
            int idKunjunganSkrining = hitung("SELECT min(s.id_kunjungan) FROM skrining s");
            RekamMedisEntity rm = manager.susunRingkasan(idKunjunganSkrining);

            assertNotNull(rm.getPasien());
            assertEquals(hitung("SELECT id_pasien FROM kunjungan WHERE id_kunjungan = " + idKunjunganSkrining),
                    rm.getPasien().getIdPasien());
            assertNotNull(rm.getSkriningHariIni());
            assertNotNull(rm.getPenandaAktif());
        }

        @Test
        @DisplayName("C22-UT02 susunRingkasan batas: kunjungan belum diskrining tetap tersusun tanpa skrining")
        void susunRingkasan_belumDiskrining() throws SQLException {
            int belumSkrining = hitung("SELECT min(k.id_kunjungan) FROM kunjungan k WHERE NOT EXISTS "
                    + "(SELECT 1 FROM skrining s WHERE s.id_kunjungan = k.id_kunjungan)");
            RekamMedisEntity rm = manager.susunRingkasan(belumSkrining);

            assertNotNull(rm.getPasien());
            assertNull(rm.getSkriningHariIni());
            assertTrue(rm.getPenandaAktif().isEmpty());
        }

        @Test
        @DisplayName("C22-UT03 susunRingkasan negatif: kunjungan tidak ada ditolak")
        void susunRingkasan_kunjunganTidakAda() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.susunRingkasan(-1));
            assertEquals("Kunjungan tidak ditemukan: -1", e.getMessage());
        }

        @Test
        @DisplayName("C22-UT04 susunTren normal: seluruh pengukuran terurut dari yang terlama")
        void susunTren_normal() throws SQLException {
            int pasienBanyak = hitung("SELECT k.id_pasien FROM tanda_vital t JOIN skrining s USING (id_skrining) "
                    + "JOIN kunjungan k USING (id_kunjungan) GROUP BY k.id_pasien ORDER BY count(*) DESC LIMIT 1");
            List<TandaVitalEntity> tren = manager.susunTren(pasienBanyak);

            assertTrue(tren.size() >= 2);
            for (int i = 1; i < tren.size(); i++) {
                assertFalse(tren.get(i).getWaktu().isBefore(tren.get(i - 1).getWaktu()));
            }
        }

        @Test
        @DisplayName("C22-UT05 susunTren batas: pasien dengan satu pengukuran mendapat satu titik (UC05 alt 4)")
        void susunTren_satuPengukuran() throws SQLException {
            int pasienSekali = hitung("SELECT k.id_pasien FROM tanda_vital t JOIN skrining s USING (id_skrining) "
                    + "JOIN kunjungan k USING (id_kunjungan) GROUP BY k.id_pasien HAVING count(*) = 1 "
                    + "ORDER BY k.id_pasien LIMIT 1");

            assertEquals(1, manager.susunTren(pasienSekali).size());
        }

        @Test
        @DisplayName("C22-UT06 susunTren negatif: pasien tanpa pengukuran mendapat daftar kosong")
        void susunTren_pasienTidakAda() {
            assertTrue(manager.susunTren(-1).isEmpty());
        }

        @Test
        @DisplayName("C22-UT07 cariKodeICD10 normal: awalan kode I1 menempatkan I10 di urutan pertama")
        void cariKodeICD10_normal() {
            List<KodeICD10Entity> hasil = manager.cariKodeICD10("I1");

            assertEquals("I10", hasil.get(0).getKode());
        }

        @Test
        @DisplayName("C22-UT08 cariKodeICD10 batas: kata kunci berhuruf kecil dan berspasi tepi tetap cocok")
        void cariKodeICD10_spasiDanHurufKecil() {
            List<KodeICD10Entity> hasil = manager.cariKodeICD10("  hipertensi ");

            assertTrue(hasil.stream().anyMatch(k -> k.getKode().equals("I10")));
        }

        @Test
        @DisplayName("C22-UT09 cariKodeICD10 negatif: kata kunci tidak cocok mengembalikan daftar kosong (UC05 alt 2)")
        void cariKodeICD10_tidakDitemukan() {
            assertTrue(manager.cariKodeICD10("xyzzy").isEmpty());
        }

        @Test
        @DisplayName("C22-UT11 simpanPemeriksaan normal: anamnesis, tindakan, dan diagnosis tersimpan")
        void simpanPemeriksaan_normal() throws SQLException {
            manager.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Pusing", "Edukasi", false),
                    new DiagnosisEntity(0, "R51", "Sakit kepala"));

            assertEquals(1, hitung("SELECT count(*) FROM pemeriksaan WHERE NOT terkunci AND anamnesis = 'Pusing' "
                    + "AND id_kunjungan = " + idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM diagnosis d JOIN pemeriksaan p USING (id_pemeriksaan) "
                    + "WHERE d.kode_icd10 = 'R51' AND p.id_kunjungan = " + idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_pengguna = 1 AND deskripsi = "
                    + "'Pemeriksaan kunjungan " + idKunjungan + " disimpan'"));
        }

        @Test
        @DisplayName("C22-UT12 simpanPemeriksaan batas: simpan ulang tanpa diagnosis menimpa draf, diagnosis tetap")
        void simpanPemeriksaan_timpaDraf() throws SQLException {
            PemeriksaanEntity p = new PemeriksaanEntity(0, 0, "Pusing", "Edukasi", false);
            manager.simpanPemeriksaan(idKunjungan, p, new DiagnosisEntity(0, "R51", "Sakit kepala"));
            p.setTindakan("Edukasi dan obat");
            manager.simpanPemeriksaan(idKunjungan, p, null);

            assertEquals(1, hitung("SELECT count(*) FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan
                    + " AND tindakan = 'Edukasi dan obat'"));
            assertEquals("R51", manager.muatDiagnosis(idKunjungan).getKodeICD10());
        }

        @Test
        @DisplayName("C22-UT14 simpanPemeriksaan negatif: rekam medis yang sudah dikunci ditolak")
        void simpanPemeriksaan_sudahDikunci() throws SQLException {
            PemeriksaanEntity p = new PemeriksaanEntity(0, 0, "Pusing", "Edukasi", false);
            manager.simpanPemeriksaan(idKunjungan, p, new DiagnosisEntity(0, "R51", "Sakit kepala"));
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE pemeriksaan SET terkunci = TRUE WHERE id_kunjungan = " + idKunjungan);
            }
            p.setTindakan("Diubah setelah final");

            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> manager.simpanPemeriksaan(idKunjungan, p, null));
            assertEquals("Rekam medis kunjungan ini sudah dikunci", e.getMessage());
            assertEquals("Edukasi", manager.muatPemeriksaan(idKunjungan).getTindakan());
        }

        @Test
        @DisplayName("C22-UT16 jadwalkanKontrol normal: jadwal tersimpan berstatus terjadwal")
        void jadwalkanKontrol_normal() throws SQLException {
            JadwalKontrolEntity j = manager.jadwalkanKontrol(idPasien, TANGGAL_UJI);

            assertTrue(j.getIdJadwal() > 0);
            assertEquals(1, hitung("SELECT count(*) FROM jadwal_kontrol WHERE id_jadwal = " + j.getIdJadwal()
                    + " AND status = 'terjadwal' AND id_pasien = " + idPasien));
        }

        @Test
        @DisplayName("C22-UT17 jadwalkanKontrol batas: tanggal besok (H+1) diterima")
        void jadwalkanKontrol_besok() throws SQLException {
            JadwalKontrolEntity j = manager.jadwalkanKontrol(idPasien, LocalDate.now().plusDays(1));

            assertEquals(1, hitung("SELECT count(*) FROM jadwal_kontrol WHERE tanggal_rencana = CURRENT_DATE + 1 "
                    + "AND id_jadwal = " + j.getIdJadwal()));
        }

        @Test
        @DisplayName("C22-UT18 jadwalkanKontrol batas: tanggal yang sama dua kali tidak membuat jadwal ganda")
        void jadwalkanKontrol_tidakGanda() throws SQLException {
            JadwalKontrolEntity pertama = manager.jadwalkanKontrol(idPasien, TANGGAL_UJI);
            JadwalKontrolEntity kedua = manager.jadwalkanKontrol(idPasien, TANGGAL_UJI);

            assertEquals(pertama.getIdJadwal(), kedua.getIdJadwal());
            assertEquals(1, hitung("SELECT count(*) FROM jadwal_kontrol WHERE tanggal_rencana = DATE '2099-01-01'"));
        }

        @Test
        // Fail pada v9.0 karena pencarian memakai Q-C59-17. Lulus setelah memakai Q-C59-20 (HIUPL Tabel 4.2.7).
        @DisplayName("C22-UT37 jadwalkanKontrol batas: jadwal yang sudah masuk daftar pantau tidak dibuat ulang")
        void jadwalkanKontrol_jadwalTertautTidakGanda() throws SQLException {
            int idJadwal;
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO jadwal_kontrol (id_pasien, tanggal_rencana) VALUES (" + idPasien
                        + ", DATE '2099-01-01')");
                idJadwal = hitung("SELECT max(id_jadwal) FROM jadwal_kontrol WHERE id_pasien = " + idPasien);
                // Perawat sudah membuka daftar pantau, sehingga jadwal ini sudah memicu entri kunjungan ulang.
                st.executeUpdate("INSERT INTO daftar_pantau (id_pasien, jenis_risiko, id_jadwal) VALUES (" + idPasien
                        + ", 'kunjungan ulang', " + idJadwal + ")");
            }

            JadwalKontrolEntity j = manager.jadwalkanKontrol(idPasien, TANGGAL_UJI);

            assertEquals(idJadwal, j.getIdJadwal());
            assertEquals(1, hitung("SELECT count(*) FROM jadwal_kontrol WHERE tanggal_rencana = DATE '2099-01-01' "
                    + "AND id_pasien = " + idPasien));
        }

        @Test
        @DisplayName("C22-UT20 kunciRekamMedis normal: tanpa resep, rekam medis dikunci, kunjungan ditutup, bundel dan "
                + "audit tersimpan")
        void kunciRekamMedis_normal() throws SQLException {
            simpanDrafLengkap();
            manager.kunciRekamMedis(idKunjungan);

            assertEquals(1, hitung("SELECT count(*) FROM pemeriksaan WHERE terkunci AND id_kunjungan = "
                    + idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM kunjungan WHERE status = 'selesai' "
                    + "AND id_kunjungan = " + idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM bundel_fhir b, jsonb_array_elements(b.isi_bundel -> 'entry') "
                    + "e WHERE e -> 'resource' ->> 'resourceType' = 'Condition' AND b.id_kunjungan = " + idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_pengguna = 1 AND deskripsi = "
                    + "'Pemeriksaan kunjungan " + idKunjungan + " dikunci'"));
        }

        @Test
        @DisplayName("C22-UT21 kunciRekamMedis batas: resep masih menunggu, kunjungan selesai diperiksa tanpa bundel")
        void kunciRekamMedis_resepMenunggu() throws SQLException {
            sisipResep("menunggu");
            simpanDrafLengkap();
            manager.kunciRekamMedis(idKunjungan);

            assertEquals(1, hitung("SELECT count(*) FROM kunjungan WHERE status = 'selesai diperiksa' "
                    + "AND id_kunjungan = " + idKunjungan));
            assertEquals(0, hitung("SELECT count(*) FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
        }

        @Test
        @DisplayName("C22-UT22 kunciRekamMedis batas: resep yang dikembalikan apotek tidak menahan penutupan kunjungan")
        void kunciRekamMedis_resepDikembalikan() throws SQLException {
            sisipResep("dikembalikan");
            simpanDrafLengkap();
            manager.kunciRekamMedis(idKunjungan);

            assertEquals(1, hitung("SELECT count(*) FROM kunjungan WHERE status = 'selesai' "
                    + "AND id_kunjungan = " + idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
        }

        @Test
        @DisplayName("C22-UT23 kunciRekamMedis batas: jadwal kontrol jatuh tempo hari ini dan entri pantaunya selesai")
        void kunciRekamMedis_jadwalJatuhTempo() throws SQLException {
            int idJadwal;
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO jadwal_kontrol (id_pasien, tanggal_rencana) VALUES (" + idPasien
                        + ", CURRENT_DATE)");
                idJadwal = hitung("SELECT max(id_jadwal) FROM jadwal_kontrol WHERE id_pasien = " + idPasien);
                st.executeUpdate("INSERT INTO daftar_pantau (id_pasien, jenis_risiko, id_jadwal) VALUES (" + idPasien
                        + ", 'kunjungan ulang', " + idJadwal + ")");
            }
            simpanDrafLengkap();
            manager.kunciRekamMedis(idKunjungan);

            assertEquals(1, hitung("SELECT count(*) FROM jadwal_kontrol WHERE status = 'selesai' AND id_jadwal = "
                    + idJadwal));
            assertEquals(1, hitung("SELECT count(*) FROM daftar_pantau WHERE status_tindak_lanjut = "
                    + "'selesai ditindaklanjuti' AND id_jadwal = " + idJadwal));
        }

        @Test
        @DisplayName("C22-UT24 kunciRekamMedis negatif: pemeriksaan belum disimpan ditolak")
        void kunciRekamMedis_belumAdaPemeriksaan() {
            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> manager.kunciRekamMedis(idKunjungan));
            assertEquals("Pemeriksaan belum disimpan", e.getMessage());
        }

        @Test
        @DisplayName("C22-UT25 kunciRekamMedis negatif: pemeriksaan tanpa diagnosis ICD-10 ditolak dan tidak terkunci")
        void kunciRekamMedis_tanpaDiagnosis() throws SQLException {
            manager.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Batuk", "Edukasi", false), null);

            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> manager.kunciRekamMedis(idKunjungan));
            assertEquals("Diagnosis ICD-10 belum diisi", e.getMessage());
            assertEquals(0, hitung("SELECT count(*) FROM pemeriksaan WHERE terkunci AND id_kunjungan = "
                    + idKunjungan));
        }

        @Test
        @DisplayName("C22-UT26 kunciRekamMedis negatif: penguncian kedua ditolak dan audit tidak bertambah")
        void kunciRekamMedis_keduaKali() throws SQLException {
            simpanDrafLengkap();
            manager.kunciRekamMedis(idKunjungan);

            assertThrows(IllegalStateException.class, () -> manager.kunciRekamMedis(idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Pemeriksaan kunjungan "
                    + idKunjungan + " dikunci'"));
        }

        @Test
        @DisplayName("C22-UT31 muatPemeriksaan normal: draf tersimpan dikembalikan")
        void muatPemeriksaan_normal() {
            manager.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Batuk pilek", "Obat", false), null);

            PemeriksaanEntity p = manager.muatPemeriksaan(idKunjungan);
            assertEquals("Batuk pilek", p.getAnamnesis());
            assertFalse(p.isTerkunci());
        }

        @Test
        @DisplayName("C22-UT32 muatPemeriksaan negatif: kunjungan tanpa pemeriksaan mengembalikan null")
        void muatPemeriksaan_belumAda() {
            assertNull(manager.muatPemeriksaan(idKunjungan));
        }

        @Test
        @DisplayName("C22-UT33 muatDiagnosis normal: diagnosis tersimpan dikembalikan dengan kodenya")
        void muatDiagnosis_normal() {
            simpanDrafLengkap();

            assertEquals("J00", manager.muatDiagnosis(idKunjungan).getKodeICD10());
        }

        @Test
        @DisplayName("C22-UT34 muatDiagnosis negatif: pemeriksaan tanpa diagnosis mengembalikan null")
        void muatDiagnosis_belumAda() {
            manager.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Batuk", "Obat", false), null);

            assertNull(manager.muatDiagnosis(idKunjungan));
        }

        @Test
        @DisplayName("C22-UT35 muatKunjunganSiapPeriksa normal: kunjungan siap diperiksa hari ini tampil dengan pasiennya")
        void muatKunjunganSiapPeriksa_normal() throws SQLException {
            int siap = hitung("SELECT min(id_kunjungan) FROM kunjungan WHERE status = 'siap diperiksa dokter' "
                    + "AND tanggal = CURRENT_DATE");
            Map<KunjunganEntity, PasienEntity> hasil = manager.muatKunjunganSiapPeriksa();

            assertTrue(hasil.keySet().stream().anyMatch(k -> k.getIdKunjungan() == siap));
            for (var e : hasil.entrySet()) {
                assertEquals(LocalDate.now(), e.getKey().getTanggal());
                assertEquals(e.getKey().getIdPasien(), e.getValue().getIdPasien());
            }
        }

        @Test
        @DisplayName("C22-UT36 muatKunjunganSiapPeriksa negatif: kunjungan yang belum diskrining tidak tampil")
        void muatKunjunganSiapPeriksa_belumDiskrining() throws SQLException {
            int menunggu = hitung("SELECT min(id_kunjungan) FROM kunjungan WHERE status = 'menunggu skrining' "
                    + "AND tanggal = CURRENT_DATE");

            assertTrue(manager.muatKunjunganSiapPeriksa().keySet().stream()
                    .noneMatch(k -> k.getIdKunjungan() == menunggu));
        }

        private void simpanDrafLengkap() {
            manager.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Batuk", "Obat", false),
                    new DiagnosisEntity(0, "J00", "Nasofaringitis akut (common cold)"));
        }

        private void sisipResep(String status) throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO resep (id_kunjungan, waktu_masuk, status, catatan) VALUES ("
                        + idKunjungan + ", now(), '" + status + "', 'uji C22')");
            }
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); var rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
