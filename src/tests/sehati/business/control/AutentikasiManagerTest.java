package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mindrot.jbcrypt.BCrypt;

import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

// Pengujian unit C17 AutentikasiManager (HIUPL Subbab 4.2, C17-UT01 sampai C17-UT17).
@DisplayName("C17 AutentikasiManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class AutentikasiManagerTest {

    private final AutentikasiManager manager = new AutentikasiManager();

    @Test
    @DisplayName("C17-UT07 periksaHakAkses normal: setiap peran boleh membuka layar utamanya")
    void periksaHakAkses_layarSendiri() {
        assertTrue(manager.periksaHakAkses("Petugas Administrasi", "PasienForm"));
        assertTrue(manager.periksaHakAkses("Petugas Administrasi", "KonfigurasiForm"));
        assertTrue(manager.periksaHakAkses("Perawat", "SkriningForm"));
        assertTrue(manager.periksaHakAkses("Dokter", "ResepForm"));
        assertTrue(manager.periksaHakAkses("Petugas Farmasi", "PersediaanForm"));
    }

    @Test
    @DisplayName("C17-UT08 periksaHakAkses batas: layar bersama Perawat dan Dokter, PemeriksaanForm hanya Dokter")
    void periksaHakAkses_layarBersama() {
        assertTrue(manager.periksaHakAkses("Perawat", "RekamMedisForm"));
        assertTrue(manager.periksaHakAkses("Dokter", "RekamMedisForm"));
        assertTrue(manager.periksaHakAkses("Dokter", "PemeriksaanForm"));
        assertFalse(manager.periksaHakAkses("Perawat", "PemeriksaanForm"));
    }

    @Test
    @DisplayName("C17-UT09 periksaHakAkses negatif: layar di luar peran ditolak")
    void periksaHakAkses_layarPeranLain() {
        assertFalse(manager.periksaHakAkses("Perawat", "ResepForm"));
        assertFalse(manager.periksaHakAkses("Petugas Farmasi", "PasienForm"));
        assertFalse(manager.periksaHakAkses("Dokter", "KonfigurasiForm"));
        assertFalse(manager.periksaHakAkses("Petugas Administrasi", "PemeriksaanForm"));
    }

    @Test
    @DisplayName("C17-UT10 periksaHakAkses negatif: peran null, tidak dikenal, atau beda huruf, dan layar LoginForm")
    void periksaHakAkses_peranTidakSah() {
        assertFalse(manager.periksaHakAkses(null, "PasienForm"));
        assertFalse(manager.periksaHakAkses("Pemilik Layar", "PasienForm"));
        assertFalse(manager.periksaHakAkses("dokter", "ResepForm"));
        assertFalse(manager.periksaHakAkses("Petugas Administrasi", "LoginForm"));
    }

    @Test
    @DisplayName("C17-UT17 periksaHakAkses negatif: nama layar null mengembalikan false")
    void periksaHakAkses_layarNull() {
        assertFalse(manager.periksaHakAkses("Dokter", null));
        assertFalse(manager.periksaHakAkses(null, null));
    }

    @Test
    @DisplayName("C17-UT13 tutupSesi negatif: tanpa sesi tidak melakukan apa pun")
    void tutupSesi_tanpaSesi() {
        SesiAktif.lepas();
        manager.tutupSesi(false);
        assertNull(SesiAktif.ambil());
    }

    @Test
    @DisplayName("C17-UT16 sesiDiputus negatif: tanpa sesi mengembalikan false tanpa membuka koneksi")
    void sesiDiputus_tanpaSesi() {
        SesiAktif.lepas();
        assertFalse(manager.sesiDiputus());
    }

    // Manager buka koneksi sendiri lewat KoneksiBasisData, jadi DB_* di .env harus ke DB yang sama dengan
    // DB_UJI_URL. Akun uji dibuat dan dihapus di setiap tes.
    @Nested
    @DisplayName("C17 AutentikasiManager dengan basis data")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private static final String NAMA = "uji.autentikasi";
        private static final String SANDI = "rahasia123";

        private Connection koneksi;
        private int idPengguna;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            try (PreparedStatement ps = koneksi.prepareStatement("INSERT INTO pengguna (nama_pengguna, "
                    + "kata_sandi_hash, peran, status) VALUES (?, ?, 'Perawat', 'aktif') RETURNING id_pengguna")) {
                ps.setString(1, NAMA);
                ps.setString(2, BCrypt.hashpw(SANDI, BCrypt.gensalt(4)));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    idPengguna = rs.getInt(1);
                }
            }
        }

        @AfterEach
        void bersihkan() throws SQLException {
            SesiAktif.lepas();
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM audit WHERE id_pengguna = " + idPengguna
                        + " OR deskripsi LIKE 'Masuk gagal: nama ''uji.tidak.ada''%'");
                st.executeUpdate("DELETE FROM sesi WHERE id_pengguna = " + idPengguna);
                st.executeUpdate("DELETE FROM pengguna WHERE id_pengguna = " + idPengguna);
            }
            koneksi.close();
        }

        @Test
        @DisplayName("C17-UT01 autentikasi normal: kredensial benar membuka sesi, mencatat sesi dan audit")
        void autentikasi_berhasil() throws SQLException {
            SesiEntity sesi = manager.autentikasi(NAMA, SANDI);

            assertNotNull(sesi);
            assertTrue(sesi.isStatusAktif());
            assertEquals(idPengguna, SesiAktif.idPenggunaSesi());
            assertEquals("Perawat", SesiAktif.peran());
            assertEquals(1, hitung("SELECT count(*) FROM sesi WHERE status_aktif AND id_sesi = '" + sesi.getIdSesi()
                    + "' AND id_pengguna = " + idPengguna));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Masuk ke aplikasi' AND id_pengguna = "
                    + idPengguna));
        }

        @Test
        @DisplayName("C17-UT02 autentikasi batas: masuk berhasil setelah gagal mengatur ulang hitungan percobaan")
        void autentikasi_hitunganDiaturUlang() throws SQLException {
            assertNull(manager.autentikasi(NAMA, "salah"));
            assertNotNull(manager.autentikasi(NAMA, SANDI));
            SesiAktif.lepas();
            assertNull(manager.autentikasi(NAMA, "salah lagi"));

            // percobaan setelah berhasil kembali dihitung dari 1
            assertEquals(2, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Masuk gagal, percobaan ke-1' "
                    + "AND id_pengguna = " + idPengguna));
            assertEquals(0, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Masuk gagal, percobaan ke-2' "
                    + "AND id_pengguna = " + idPengguna));
        }

        @Test
        @DisplayName("C17-UT03 autentikasi negatif: sandi salah mengembalikan null, tanpa sesi, audit gagal tercatat")
        void autentikasi_sandiSalah() throws SQLException {
            assertNull(manager.autentikasi(NAMA, "salah"));

            assertNull(SesiAktif.ambil());
            assertEquals(0, hitung("SELECT count(*) FROM sesi WHERE id_pengguna = " + idPengguna));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Masuk gagal, percobaan ke-1' "
                    + "AND id_pengguna = " + idPengguna));
        }

        @Test
        @DisplayName("C17-UT04 autentikasi negatif: nama tidak dikenal mengembalikan null, audit berpelaku 0")
        void autentikasi_namaTidakDikenal() throws SQLException {
            assertNull(manager.autentikasi(NAMA, "salah"));
            assertNull(manager.autentikasi("uji.tidak.ada", SANDI));

            // percobaanGagal lanjut menghitung walau namanya beda
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_pengguna = 0 AND deskripsi = "
                    + "'Masuk gagal: nama ''uji.tidak.ada'' tidak dikenal, percobaan ke-2'"));
        }

        @Test
        @DisplayName("C17-UT05 autentikasi negatif: akun nonaktif ditolak walau sandi benar")
        void autentikasi_akunNonaktif() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE pengguna SET status = 'nonaktif' WHERE id_pengguna = " + idPengguna);
            }
            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> manager.autentikasi(NAMA, SANDI));
            assertEquals("Akun nonaktif, hubungi Petugas Administrasi", e.getMessage());
            assertEquals(0, hitung("SELECT count(*) FROM sesi WHERE id_pengguna = " + idPengguna));
        }

        @Test
        @DisplayName("C17-UT06 autentikasi batas: sandi kosong atau null mengembalikan null tanpa pengecualian")
        void autentikasi_sandiKosong() throws SQLException {
            assertNull(manager.autentikasi(NAMA, ""));
            assertNull(manager.autentikasi(NAMA, null));
            assertEquals(0, hitung("SELECT count(*) FROM sesi WHERE id_pengguna = " + idPengguna));
        }

        @Test
        @DisplayName("C17-UT11 tutupSesi normal: tombol Keluar menutup sesi dan mencatat \"Keluar dari aplikasi\"")
        void tutupSesi_keluar() throws SQLException {
            SesiEntity sesi = manager.autentikasi(NAMA, SANDI);
            manager.tutupSesi(false);

            assertNull(SesiAktif.ambil());
            assertFalse(sesi.isStatusAktif());
            assertEquals(0, hitung("SELECT count(*) FROM sesi WHERE status_aktif AND id_pengguna = " + idPengguna));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Keluar dari aplikasi' "
                    + "AND id_pengguna = " + idPengguna));
        }

        @Test
        @DisplayName("C17-UT12 tutupSesi batas: sesi diam 15 menit ditutup dengan audit habis waktu")
        void tutupSesi_habisWaktu() throws SQLException {
            manager.autentikasi(NAMA, SANDI);
            manager.tutupSesi(true);

            assertNull(SesiAktif.ambil());
            assertEquals(0, hitung("SELECT count(*) FROM sesi WHERE status_aktif AND id_pengguna = " + idPengguna));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Sesi berakhir setelah diam 15 menit' "
                    + "AND id_pengguna = " + idPengguna));
        }

        @Test
        @DisplayName("C17-UT14 sesiDiputus normal: sesi ditutup dari komputer lain mengembalikan true")
        void sesiDiputus_ditutupDariLuar() throws SQLException {
            manager.autentikasi(NAMA, SANDI);
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE sesi SET status_aktif = FALSE WHERE id_pengguna = " + idPengguna);
            }
            assertTrue(manager.sesiDiputus());
        }

        @Test
        @DisplayName("C17-UT15 sesiDiputus batas: sesi yang masih aktif di basis data mengembalikan false")
        void sesiDiputus_masihAktif() {
            manager.autentikasi(NAMA, SANDI);
            assertFalse(manager.sesiDiputus());
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
