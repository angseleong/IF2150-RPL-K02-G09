package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KodeICD10Entity;
import sehati.business.entity.PemeriksaanEntity;

class DataKlinisBoundaryPemeriksaanTest {

    @Test
    void loloskanPola_wildcardDibacaSebagaiHurufBiasa() {
        assertEquals("50\\%\\_a\\\\b", DataKlinisBoundary.loloskanPola("50%_a\\b"));
    }

    /**
     * Uji terhadap basis data berisi src/db/schema.sql dan seed.sql. Aktif bila DB_UJI_URL diisi, misalnya
     * {@code jdbc:postgresql://localhost:5432/sehati?user=sehati_admin&password=...}. Setiap uji di-rollback.
     */
    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private DataKlinisBoundary dataKlinis;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            koneksi.setAutoCommit(false);
            dataKlinis = new DataKlinisBoundary(koneksi);
        }

        @AfterEach
        void tutup() throws SQLException {
            koneksi.rollback();
            koneksi.close();
        }

        @Test
        void cariKodeICD10_paling20BarisDanAwalanKodeLebihDulu() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO kode_icd10 (kode, deskripsi, bab) "
                        + "SELECT 'U' || lpad(g::text, 2, '0'), 'Uji ' || g, 'Uji' FROM generate_series(1, 25) g");
            }

            assertEquals(20, dataKlinis.cariKodeICD10("U").size());
            List<KodeICD10Entity> hasil = dataKlinis.cariKodeICD10("I1");
            assertEquals("I10", hasil.get(0).getKode());
            assertTrue(dataKlinis.cariKodeICD10("hipertensi").stream().anyMatch(k -> k.getKode().equals("I10")));
            assertTrue(dataKlinis.cariKodeICD10("%").isEmpty());
        }

        @Test
        void simpanPemeriksaan_bisaDitimpaLaluMenolakSetelahDikunci() {
            int idKunjungan = idKunjunganTanpaPemeriksaan();
            assertNull(dataKlinis.ambilPemeriksaan(idKunjungan));

            PemeriksaanEntity p = new PemeriksaanEntity(0, idKunjungan, "Pusing", "Edukasi", false);
            dataKlinis.simpanPemeriksaan(p, new DiagnosisEntity(0, "R51", "Sakit kepala"));
            int idAwal = p.getIdPemeriksaan();
            p.setAnamnesis("Pusing sejak dua hari");
            dataKlinis.simpanPemeriksaan(p, new DiagnosisEntity(0, "I10", "Hipertensi esensial (primer)"));
            assertEquals(idAwal, p.getIdPemeriksaan());
            assertEquals("Pusing sejak dua hari", dataKlinis.ambilPemeriksaan(idKunjungan).getAnamnesis());

            p.setTerkunci(true);
            dataKlinis.simpanPemeriksaan(p, null);
            assertTrue(dataKlinis.ambilPemeriksaan(idKunjungan).isTerkunci());

            p.setAnamnesis("Diubah setelah final");
            assertThrows(IllegalStateException.class, () -> dataKlinis.simpanPemeriksaan(p, null));
            assertEquals("Pusing sejak dua hari", dataKlinis.ambilPemeriksaan(idKunjungan).getAnamnesis());
        }

        @Test
        void simpanPemeriksaan_menolakKodeDiLuarICD10() {
            PemeriksaanEntity p = new PemeriksaanEntity(0, idKunjunganTanpaPemeriksaan(), "Batuk", "Obat", false);

            assertThrows(IllegalStateException.class,
                    () -> dataKlinis.simpanPemeriksaan(p, new DiagnosisEntity(0, "XYZ", "Bukan kode")));
        }

        @Test
        void jadwalKontrol_munculSampaiSelesai() {
            JadwalKontrolEntity j = new JadwalKontrolEntity(0, 1, LocalDate.now().plusDays(7), "terjadwal");
            dataKlinis.simpanJadwalKontrol(j);
            assertTrue(j.getIdJadwal() > 0);
            assertTrue(ada(j.getIdJadwal()));

            j.setStatus("selesai");
            dataKlinis.simpanJadwalKontrol(j);
            assertFalse(ada(j.getIdJadwal()));
        }

        private boolean ada(int idJadwal) {
            return dataKlinis.ambilJadwalKontrol().stream().anyMatch(j -> j.getIdJadwal() == idJadwal);
        }

        private int idKunjunganTanpaPemeriksaan() {
            try (Statement st = koneksi.createStatement();
                 var rs = st.executeQuery("SELECT k.id_kunjungan FROM kunjungan k WHERE NOT EXISTS "
                         + "(SELECT 1 FROM pemeriksaan p WHERE p.id_kunjungan = k.id_kunjungan) LIMIT 1")) {
                rs.next();
                return rs.getInt(1);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
