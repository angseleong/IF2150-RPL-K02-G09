package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.SesiEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.common.SesiAktif;

class PemeriksaanManagerTest {

    private final PemeriksaanManager manager = new PemeriksaanManager();

    @Test
    void simpanPemeriksaan_menolakDiagnosisTanpaKode() {
        PemeriksaanEntity p = new PemeriksaanEntity(0, 1, "Pusing", "Edukasi", false);

        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanPemeriksaan(1, p, new DiagnosisEntity(0, " ", "Tanpa kode")));
    }

    @Test
    void validasiTanggalKontrol_hanyaMenerimaSetelahHariIni() {
        assertThrows(IllegalArgumentException.class, () -> manager.validasiTanggalKontrol(LocalDate.now()));
        manager.validasiTanggalKontrol(LocalDate.now().plusDays(1));
    }

    @Test
    void jadwalkanKontrol_menolakHariIniAtauKosong() {
        assertThrows(IllegalArgumentException.class, () -> manager.jadwalkanKontrol(1, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> manager.jadwalkanKontrol(1, null));
    }

    @Test
    void kunciRekamMedis_menolakTanpaSesi() {
        SesiAktif.lepas();
        assertThrows(IllegalStateException.class, () -> manager.kunciRekamMedis(1));
    }

    @Test
    void cariKodeICD10_kataKunciKosongMengembalikanDaftarKosong() {
        assertTrue(manager.cariKodeICD10("  ").isEmpty());
    }

    /**
     * Manager membuka koneksi sendiri lewat KoneksiBasisData, jadi variabel DB_* harus menunjuk basis data yang
     * sama dengan DB_UJI_URL. Data yang dibuat dihapus setelah setiap uji.
     */
    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private int idKunjungan;
        private String statusAwal;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            try (Statement st = koneksi.createStatement();
                 var rs = st.executeQuery("SELECT k.id_kunjungan FROM kunjungan k WHERE NOT EXISTS "
                         + "(SELECT 1 FROM pemeriksaan p WHERE p.id_kunjungan = k.id_kunjungan) LIMIT 1")) {
                rs.next();
                idKunjungan = rs.getInt(1);
            }
            try (Statement st = koneksi.createStatement();
                 var rs = st.executeQuery("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan)) {
                rs.next();
                statusAwal = rs.getString(1);
            }
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true));
        }

        @AfterEach
        void bersihkan() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM diagnosis WHERE id_pemeriksaan IN "
                        + "(SELECT id_pemeriksaan FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan + ")");
                st.executeUpdate("DELETE FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("DELETE FROM jadwal_kontrol WHERE tanggal_rencana = DATE '2099-01-01'");
                st.executeUpdate("DELETE FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("DELETE FROM audit WHERE deskripsi IN ('Pemeriksaan kunjungan " + idKunjungan
                        + " dikunci', 'Pemeriksaan kunjungan " + idKunjungan + " disimpan')");
                st.executeUpdate("UPDATE kunjungan SET status = '" + statusAwal + "' WHERE id_kunjungan = "
                        + idKunjungan);
            }
            SesiAktif.lepas();
            koneksi.close();
        }

        @Test
        void simpanPemeriksaan_menimpaDrafLaluMenolakSetelahDikunci() throws SQLException {
            PemeriksaanEntity p = new PemeriksaanEntity(0, 0, "Pusing", "Edukasi", false);
            manager.simpanPemeriksaan(idKunjungan, p, new DiagnosisEntity(0, "R51", "Sakit kepala"));
            p.setTindakan("Edukasi dan obat");
            manager.simpanPemeriksaan(idKunjungan, p, null);
            assertEquals(1, hitung("SELECT count(*) FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan
                    + " AND tindakan = 'Edukasi dan obat'"));

            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE pemeriksaan SET terkunci = TRUE WHERE id_kunjungan = " + idKunjungan);
            }
            assertThrows(IllegalStateException.class, () -> manager.simpanPemeriksaan(idKunjungan, p, null));
        }

        @Test
        void kunciRekamMedis_mengunciDanMencatatAudit() throws SQLException {
            assertThrows(IllegalStateException.class, () -> manager.kunciRekamMedis(idKunjungan));

            manager.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Batuk", "Obat", false),
                    new DiagnosisEntity(0, "J00", "Nasofaringitis akut (common cold)"));
            manager.kunciRekamMedis(idKunjungan);

            assertEquals(1, hitung("SELECT count(*) FROM pemeriksaan WHERE terkunci AND id_kunjungan = "
                    + idKunjungan));
            // Tanpa resep, kunjungan langsung ditutup dan dibundel lengkap dengan diagnosis
            assertEquals(1, hitung("SELECT count(*) FROM kunjungan WHERE status = 'selesai' "
                    + "AND id_kunjungan = " + idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM bundel_fhir b, jsonb_array_elements(b.isi_bundel -> 'entry') e "
                    + "WHERE e -> 'resource' ->> 'resourceType' = 'Condition' AND b.id_kunjungan = " + idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_pengguna = 1 AND deskripsi = "
                    + "'Pemeriksaan kunjungan " + idKunjungan + " dikunci'"));
            assertEquals(1, hitung("SELECT count(*) FROM diagnosis d JOIN pemeriksaan p USING (id_pemeriksaan) "
                    + "WHERE d.kode_icd10 = 'J00' AND p.id_kunjungan = " + idKunjungan));
            // Penguncian kedua ditolak dan tidak menambah audit.
            assertThrows(IllegalStateException.class, () -> manager.kunciRekamMedis(idKunjungan));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Pemeriksaan kunjungan "
                    + idKunjungan + " dikunci'"));
        }

        @Test
        void kunciRekamMedis_resepDikembalikanTidakMenahanKunjungan() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO resep (id_kunjungan, waktu_masuk, status, catatan) VALUES ("
                        + idKunjungan + ", now(), 'dikembalikan', 'uji kunci rekam medis')");
            }
            try {
                manager.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Batuk", "Edukasi", false),
                        new DiagnosisEntity(0, "J00", "Nasofaringitis akut (common cold)"));
                manager.kunciRekamMedis(idKunjungan);

                assertEquals(1, hitung("SELECT count(*) FROM kunjungan WHERE status = 'selesai' "
                        + "AND id_kunjungan = " + idKunjungan));
                assertEquals(1, hitung("SELECT count(*) FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
            } finally {
                try (Statement st = koneksi.createStatement()) {
                    st.executeUpdate("DELETE FROM resep WHERE id_kunjungan = " + idKunjungan
                            + " AND catatan = 'uji kunci rekam medis'");
                }
            }
        }

        @Test
        void susunRingkasan_memuatPasienSkriningDanPenanda() throws SQLException {
            int idKunjunganSkrining = hitung("SELECT min(s.id_kunjungan) FROM skrining s");
            RekamMedisEntity rm = manager.susunRingkasan(idKunjunganSkrining);

            assertNotNull(rm.getPasien());
            assertNotNull(rm.getSkriningHariIni());
            assertNotNull(rm.getPenandaAktif());
            assertThrows(IllegalArgumentException.class, () -> manager.susunRingkasan(-1));
        }

        @Test
        void susunTren_terlamaLebihDuluDenganWaktu() {
            List<TandaVitalEntity> tren = manager.susunTren(1);

            assertTrue(tren.size() >= 2);
            for (int i = 1; i < tren.size(); i++) {
                assertTrue(!tren.get(i).getWaktu().isBefore(tren.get(i - 1).getWaktu()));
            }
        }

        @Test
        void muatKunjunganSiapPeriksa_hanyaStatusSiapHariIni() {
            for (var e : manager.muatKunjunganSiapPeriksa().entrySet()) {
                assertEquals("siap diperiksa dokter", e.getKey().getStatus());
                assertEquals(LocalDate.now(), e.getKey().getTanggal());
                assertEquals(e.getKey().getIdPasien(), e.getValue().getIdPasien());
            }
        }

        @Test
        void jadwalkanKontrol_tersimpanTerjadwal() throws SQLException {
            JadwalKontrolEntity j = manager.jadwalkanKontrol(1, LocalDate.of(2099, 1, 1));

            assertTrue(j.getIdJadwal() > 0);
            assertEquals(1, hitung("SELECT count(*) FROM jadwal_kontrol WHERE id_jadwal = " + j.getIdJadwal()
                    + " AND status = 'terjadwal'"));
        }

        @Test
        void jadwalkanKontrol_tanggalSamaTidakMembuatJadwalGanda() throws SQLException {
            JadwalKontrolEntity pertama = manager.jadwalkanKontrol(1, LocalDate.of(2099, 1, 1));
            JadwalKontrolEntity kedua = manager.jadwalkanKontrol(1, LocalDate.of(2099, 1, 1));

            assertEquals(pertama.getIdJadwal(), kedua.getIdJadwal());
            assertEquals(1, hitung("SELECT count(*) FROM jadwal_kontrol WHERE tanggal_rencana = DATE '2099-01-01'"));
        }

        @Test
        void muatDraf_mengembalikanPemeriksaanDanDiagnosisTersimpan() {
            assertNull(manager.muatPemeriksaan(idKunjungan));
            assertNull(manager.muatDiagnosis(idKunjungan));

            manager.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Batuk", "Obat", false),
                    new DiagnosisEntity(0, "J00", "Nasofaringitis akut (common cold)"));
            manager.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Batuk pilek", "Obat", false), null);

            assertEquals("Batuk pilek", manager.muatPemeriksaan(idKunjungan).getAnamnesis());
            // Simpan tanpa diagnosis baru tidak menghapus diagnosis tersimpan.
            assertEquals("J00", manager.muatDiagnosis(idKunjungan).getKodeICD10());
        }

        @Test
        void cariKodeICD10_meneruskanKeBasisData() {
            assertEquals("I10", manager.cariKodeICD10("I1").get(0).getKode());
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); var rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
