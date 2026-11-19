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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KodeICD10Entity;
import sehati.business.entity.PemeriksaanEntity;

/**
 * Pengujian unit C59 DataKlinisBoundary bagian pemeriksaan (HIUPL Subbab 4.2, C59-UT21 sampai C59-UT32). Nomor
 * C59-UT01 sampai C59-UT20 dipakai bagian skrining.
 */
@DisplayName("C59 DataKlinisBoundary bagian pemeriksaan")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class DataKlinisBoundaryPemeriksaanTest {

    @Test
    @DisplayName("C59-UT21 loloskanPola normal: %, _, dan \\ dibaca sebagai huruf biasa")
    void loloskanPola_wildcardDibacaSebagaiHurufBiasa() {
        assertEquals("50\\%\\_a\\\\b", DataKlinisBoundary.loloskanPola("50%_a\\b"));
    }

    /**
     * Uji terhadap basis data berisi src/db/schema.sql dan seed.sql. Aktif bila DB_UJI_URL diisi, misalnya
     * {@code jdbc:postgresql://localhost:5432/sehati?user=sehati_admin&password=...}. Setiap uji di-rollback.
     */
    @Nested
    @DisplayName("Dengan basis data uji")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
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
        @DisplayName("C59-UT22 cariKodeICD10 normal: awalan kode lebih dulu, deskripsi juga dicari")
        void cariKodeICD10_normal() {
            List<KodeICD10Entity> hasil = dataKlinis.cariKodeICD10("I1");

            assertEquals("I10", hasil.get(0).getKode());
            assertTrue(dataKlinis.cariKodeICD10("hipertensi").stream().anyMatch(k -> k.getKode().equals("I10")));
        }

        @Test
        @DisplayName("C59-UT23 cariKodeICD10 batas: hasil dibatasi paling banyak 20 baris")
        void cariKodeICD10_paling20Baris() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO kode_icd10 (kode, deskripsi, bab) "
                        + "SELECT 'U' || lpad(g::text, 2, '0'), 'Uji ' || g, 'Uji' FROM generate_series(1, 25) g");
            }

            assertEquals(20, dataKlinis.cariKodeICD10("U").size());
        }

        @Test
        @DisplayName("C59-UT24 cariKodeICD10 negatif: tanda % tidak menjadi wildcard, kata kunci null ditolak")
        void cariKodeICD10_wildcardDanNull() {
            assertTrue(dataKlinis.cariKodeICD10("%").isEmpty());
            assertThrows(IllegalArgumentException.class, () -> dataKlinis.cariKodeICD10(null));
        }

        @Test
        @DisplayName("C59-UT25 simpanPemeriksaan normal: pemeriksaan disisipkan lalu ditimpa dengan ID yang sama")
        void simpanPemeriksaan_sisipLaluTimpa() {
            int idKunjungan = idKunjunganTanpaPemeriksaan();
            assertNull(dataKlinis.ambilPemeriksaan(idKunjungan));

            PemeriksaanEntity p = new PemeriksaanEntity(0, idKunjungan, "Pusing", "Edukasi", false);
            dataKlinis.simpanPemeriksaan(p, new DiagnosisEntity(0, "R51", "Sakit kepala"));
            int idAwal = p.getIdPemeriksaan();
            p.setAnamnesis("Pusing sejak dua hari");
            dataKlinis.simpanPemeriksaan(p, new DiagnosisEntity(0, "I10", "Hipertensi esensial (primer)"));

            assertEquals(idAwal, p.getIdPemeriksaan());
            assertEquals("Pusing sejak dua hari", dataKlinis.ambilPemeriksaan(idKunjungan).getAnamnesis());
            assertEquals("I10", dataKlinis.ambilDiagnosis(idKunjungan).getKodeICD10());
        }

        @Test
        @DisplayName("C59-UT26 simpanPemeriksaan negatif: kode di luar data master ICD-10 ditolak")
        void simpanPemeriksaan_kodeDiLuarICD10() {
            PemeriksaanEntity p = new PemeriksaanEntity(0, idKunjunganTanpaPemeriksaan(), "Batuk", "Obat", false);

            assertThrows(IllegalArgumentException.class,
                    () -> dataKlinis.simpanPemeriksaan(p, new DiagnosisEntity(0, "XYZ", "Bukan kode")));
        }

        @Test
        @DisplayName("C59-UT27 simpanPemeriksaan negatif: pemeriksaan terkunci tidak dapat diubah")
        void simpanPemeriksaan_setelahDikunci() {
            int idKunjungan = idKunjunganTanpaPemeriksaan();
            PemeriksaanEntity p = new PemeriksaanEntity(0, idKunjungan, "Pusing", "Edukasi", false);
            dataKlinis.simpanPemeriksaan(p, new DiagnosisEntity(0, "R51", "Sakit kepala"));
            p.setTerkunci(true);
            dataKlinis.simpanPemeriksaan(p, null);
            assertTrue(dataKlinis.ambilPemeriksaan(idKunjungan).isTerkunci());

            p.setAnamnesis("Diubah setelah final");
            assertThrows(IllegalStateException.class, () -> dataKlinis.simpanPemeriksaan(p, null));
            assertEquals("Pusing", dataKlinis.ambilPemeriksaan(idKunjungan).getAnamnesis());
        }

        @Test
        @DisplayName("C59-UT28 simpanJadwalKontrol normal: jadwal terjadwal muncul pada ambilJadwalKontrol")
        void simpanJadwalKontrol_normal() {
            JadwalKontrolEntity j = new JadwalKontrolEntity(0, 1, LocalDate.now().plusDays(7), "terjadwal");
            dataKlinis.simpanJadwalKontrol(j);

            assertTrue(j.getIdJadwal() > 0);
            assertTrue(ada(j.getIdJadwal()));
        }

        @Test
        @DisplayName("C59-UT29 simpanJadwalKontrol batas: jadwal yang diubah menjadi selesai tidak dimuat lagi")
        void simpanJadwalKontrol_selesai() {
            JadwalKontrolEntity j = new JadwalKontrolEntity(0, 1, LocalDate.now().plusDays(7), "terjadwal");
            dataKlinis.simpanJadwalKontrol(j);
            j.setStatus("selesai");
            dataKlinis.simpanJadwalKontrol(j);

            assertFalse(ada(j.getIdJadwal()));
        }

        @Test
        @DisplayName("C59-UT30 jadwalJatuhTempo normal: jadwal hari ini termasuk, jadwal besok belum")
        void jadwalJatuhTempo_normal() {
            JadwalKontrolEntity hariIni = new JadwalKontrolEntity(0, 1, LocalDate.now(), "terjadwal");
            JadwalKontrolEntity besok = new JadwalKontrolEntity(0, 1, LocalDate.now().plusDays(1), "terjadwal");
            dataKlinis.simpanJadwalKontrol(hariIni);
            dataKlinis.simpanJadwalKontrol(besok);

            List<JadwalKontrolEntity> jatuhTempo = dataKlinis.jadwalJatuhTempo(1, LocalDate.now());
            assertTrue(jatuhTempo.stream().anyMatch(j -> j.getIdJadwal() == hariIni.getIdJadwal()));
            assertTrue(jatuhTempo.stream().noneMatch(j -> j.getIdJadwal() == besok.getIdJadwal()));
        }

        @Test
        @DisplayName("C59-UT31 jadwalPadaTanggal normal: jadwal yang sudah masuk daftar pantau tetap ditemukan")
        void jadwalPadaTanggal_termasukYangSudahDipantau() throws SQLException {
            JadwalKontrolEntity j = new JadwalKontrolEntity(0, 1, LocalDate.of(2099, 1, 1), "terjadwal");
            dataKlinis.simpanJadwalKontrol(j);
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO daftar_pantau (id_pasien, jenis_risiko, id_jadwal) VALUES (1, "
                        + "'kunjungan ulang', " + j.getIdJadwal() + ")");
            }

            assertFalse(ada(j.getIdJadwal()));
            assertEquals(List.of(j.getIdJadwal()), dataKlinis.jadwalPadaTanggal(1, LocalDate.of(2099, 1, 1)).stream()
                    .map(JadwalKontrolEntity::getIdJadwal).toList());
        }

        @Test
        @DisplayName("C59-UT32 jadwalPadaTanggal negatif: jadwal selesai dan tanggal lain tidak ditemukan")
        void jadwalPadaTanggal_selesaiAtauTanggalLain() {
            JadwalKontrolEntity j = new JadwalKontrolEntity(0, 1, LocalDate.of(2099, 1, 1), "terjadwal");
            dataKlinis.simpanJadwalKontrol(j);

            assertTrue(dataKlinis.jadwalPadaTanggal(1, LocalDate.of(2099, 1, 2)).isEmpty());
            j.setStatus("selesai");
            dataKlinis.simpanJadwalKontrol(j);
            assertTrue(dataKlinis.jadwalPadaTanggal(1, LocalDate.of(2099, 1, 1)).isEmpty());
        }

        private boolean ada(int idJadwal) {
            return dataKlinis.ambilJadwalKontrol().stream().anyMatch(j -> j.getIdJadwal() == idJadwal);
        }

        private int idKunjunganTanpaPemeriksaan() {
            try (Statement st = koneksi.createStatement();
                 var rs = st.executeQuery("SELECT min(k.id_kunjungan) FROM kunjungan k WHERE NOT EXISTS "
                         + "(SELECT 1 FROM pemeriksaan p WHERE p.id_kunjungan = k.id_kunjungan)")) {
                rs.next();
                return rs.getInt(1);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
