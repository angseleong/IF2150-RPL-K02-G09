package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.PemeriksaanEntity;

class PemeriksaanManagerTest {

    private final PemeriksaanManager manager = new PemeriksaanManager();

    @Test
    void simpanPemeriksaan_menolakDiagnosisTanpaKode() {
        PemeriksaanEntity p = new PemeriksaanEntity(0, 1, "Pusing", "Edukasi", false);

        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanPemeriksaan(1, p, new DiagnosisEntity(0, " ", "Tanpa kode")));
    }

    @Test
    void jadwalkanKontrol_menolakHariIniAtauKosong() {
        assertThrows(IllegalArgumentException.class, () -> manager.jadwalkanKontrol(1, LocalDate.now()));
        assertThrows(IllegalArgumentException.class, () -> manager.jadwalkanKontrol(1, null));
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

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            try (Statement st = koneksi.createStatement();
                 var rs = st.executeQuery("SELECT k.id_kunjungan FROM kunjungan k WHERE NOT EXISTS "
                         + "(SELECT 1 FROM pemeriksaan p WHERE p.id_kunjungan = k.id_kunjungan) LIMIT 1")) {
                rs.next();
                idKunjungan = rs.getInt(1);
            }
        }

        @AfterEach
        void bersihkan() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM diagnosis WHERE id_pemeriksaan IN "
                        + "(SELECT id_pemeriksaan FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan + ")");
                st.executeUpdate("DELETE FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("DELETE FROM jadwal_kontrol WHERE tanggal_rencana = DATE '2099-01-01'");
            }
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
        void jadwalkanKontrol_tersimpanTerjadwal() throws SQLException {
            JadwalKontrolEntity j = manager.jadwalkanKontrol(1, LocalDate.of(2099, 1, 1));

            assertTrue(j.getIdJadwal() > 0);
            assertEquals(1, hitung("SELECT count(*) FROM jadwal_kontrol WHERE id_jadwal = " + j.getIdJadwal()
                    + " AND status = 'terjadwal'"));
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
