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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mindrot.jbcrypt.BCrypt;

import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

class AutentikasiManagerTest {

    private final AutentikasiManager manager = new AutentikasiManager();

    @Test
    void periksaHakAkses_dokterBolehResepPerawatTidak() {
        assertTrue(manager.periksaHakAkses("Dokter", "ResepForm"));
        assertFalse(manager.periksaHakAkses("Perawat", "ResepForm"));
    }

    @Test
    void periksaHakAkses_farmasiTidakBolehPasien() {
        assertTrue(manager.periksaHakAkses("Petugas Farmasi", "PersediaanForm"));
        assertFalse(manager.periksaHakAkses("Petugas Farmasi", "PasienForm"));
    }

    @Test
    void periksaHakAkses_menolakPeranKosongAtauTidakDikenal() {
        assertFalse(manager.periksaHakAkses(null, "PasienForm"));
        assertFalse(manager.periksaHakAkses("Pemilik Layar", "PasienForm"));
        assertFalse(manager.periksaHakAkses("Petugas Administrasi", "LoginForm"));
    }

    @Test
    void tutupSesi_tanpaSesiTidakMelakukanApaPun() {
        SesiAktif.lepas();
        manager.tutupSesi(false);
        assertNull(SesiAktif.ambil());
        assertFalse(manager.sesiDiputus());
    }

    // Manager buka koneksi sendiri lewat KoneksiBasisData, jadi DB_* di .env harus ke DB yang sama dengan
    // DB_UJI_URL. Akun uji dibuat dan dihapus di setiap tes.
    @Nested
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
        void autentikasi_berhasilMembukaSesiDanMencatatAudit() throws SQLException {
            SesiEntity sesi = manager.autentikasi(NAMA, SANDI);

            assertNotNull(sesi);
            assertEquals(idPengguna, SesiAktif.idPenggunaSesi());
            assertEquals("Perawat", SesiAktif.peran());
            assertEquals(1, hitung("SELECT count(*) FROM sesi WHERE status_aktif AND id_pengguna = " + idPengguna));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Masuk ke aplikasi' AND id_pengguna = "
                    + idPengguna));
        }

        @Test
        void autentikasi_sandiSalahDanNamaTidakDikenalMengembalikanNull() throws SQLException {
            assertNull(manager.autentikasi(NAMA, "salah"));
            assertNull(manager.autentikasi("uji.tidak.ada", SANDI));

            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Masuk gagal, percobaan ke-1' "
                    + "AND id_pengguna = " + idPengguna));
            // percobaanGagal lanjut menghitung walau namanya beda
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_pengguna = 0 AND deskripsi = "
                    + "'Masuk gagal: nama ''uji.tidak.ada'' tidak dikenal, percobaan ke-2'"));
        }

        @Test
        void autentikasi_menolakAkunNonaktif() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE pengguna SET status = 'nonaktif' WHERE id_pengguna = " + idPengguna);
            }
            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> manager.autentikasi(NAMA, SANDI));
            assertEquals("Akun nonaktif, hubungi Petugas Administrasi", e.getMessage());
        }

        @Test
        void sesiDiputus_setelahAkunDinonaktifkanDiKomputerLain() throws SQLException {
            manager.autentikasi(NAMA, SANDI);
            assertFalse(manager.sesiDiputus());

            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE sesi SET status_aktif = FALSE WHERE id_pengguna = " + idPengguna);
            }
            assertTrue(manager.sesiDiputus());
            manager.tutupSesi(false);
        }

        @Test
        void tutupSesi_menyimpanStatusDanAudit() throws SQLException {
            manager.autentikasi(NAMA, SANDI);
            manager.tutupSesi(true);

            assertNull(SesiAktif.ambil());
            assertEquals(0, hitung("SELECT count(*) FROM sesi WHERE status_aktif AND id_pengguna = " + idPengguna));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Sesi berakhir setelah diam 15 menit' "
                    + "AND id_pengguna = " + idPengguna));
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
