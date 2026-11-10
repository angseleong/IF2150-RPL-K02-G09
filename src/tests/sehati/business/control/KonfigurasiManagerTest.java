package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mindrot.jbcrypt.BCrypt;

import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

class KonfigurasiManagerTest {

    private final KonfigurasiManager manager = new KonfigurasiManager();

    @AfterEach
    void lepasSesi() {
        SesiAktif.lepas();
    }

    private static void masukSebagai(int idPengguna) {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), idPengguna, LocalDateTime.now(), true),
                "Petugas Administrasi");
    }

    @Test
    void simpanAkun_menolakNamaKosongDanPeranTidakDikenal() {
        masukSebagai(1);
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity(" ", null, "Perawat"), "rahasia123"));
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Satpam"), "rahasia123"));
    }

    @Test
    void simpanAkun_menolakSandiKurangDariDelapan() {
        masukSebagai(1);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Perawat"), "1234567"));
        assertEquals("Kata sandi minimal 8 karakter", e.getMessage());
        // akun baru wajib punya sandi
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Perawat"), ""));
        // sandi baru untuk akun lama juga dicek
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity(5, "uji", null, "Perawat", "aktif"), "pendek"));
    }

    @Test
    void simpanAkun_menolakTanpaSesi() {
        assertThrows(IllegalStateException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Perawat"), "rahasia123"));
    }

    @Test
    void nonaktifkanAkun_menolakAkunSendiri() {
        masukSebagai(7);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.nonaktifkanAkun(7));
        assertEquals("Akun yang sedang dipakai tidak dapat dinonaktifkan", e.getMessage());
    }

    @Test
    void simpanDataMaster_menolakNilaiNolAtauNegatif() {
        masukSebagai(1);
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanDataMaster(new AmbangRisikoEntity(1, "hipertensi", 0)));
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanDataMaster(new AmbangRisikoEntity(1, "hipertensi", -5)));
    }

    @Test
    void simpanPoli_menolakNamaKosong() {
        masukSebagai(1);
        assertThrows(IllegalArgumentException.class, () -> manager.simpanPoli(0, "  "));
        assertThrows(IllegalArgumentException.class, () -> manager.simpanPoli(0, null));
    }

    @Test
    void tambahObat_menolakIsianKosongDanMinimumNegatif() {
        masukSebagai(1);
        assertThrows(IllegalArgumentException.class,
                () -> manager.tambahObat(new ObatEntity(0, "Parasetamol", " ", 0, 10)));
        assertThrows(IllegalArgumentException.class,
                () -> manager.tambahObat(new ObatEntity(0, "Parasetamol", "tablet 500 mg", 0, -1)));
    }

    @Test
    void jedaSampai_hariIniAtauBesok() {
        LocalDateTime pagi = LocalDateTime.of(2026, 11, 8, 20, 30);
        assertEquals(30, KonfigurasiManager.jedaSampai(21, pagi));
        assertEquals(23 * 60 + 30, KonfigurasiManager.jedaSampai(20, pagi));
    }

    // Manager buka koneksi sendiri lewat KoneksiBasisData, jadi DB_* di .env harus ke DB yang sama dengan
    // DB_UJI_URL. Akun uji dihapus setelah setiap tes.
    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private static final String NAMA = "uji.konfigurasi";

        private Connection koneksi;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            masukSebagai(1);
        }

        @AfterEach
        void bersihkan() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM audit WHERE deskripsi LIKE '%" + NAMA + "%'"
                        + " OR deskripsi = 'Ambang hipertensi diubah menjadi 150.0'"
                        + " OR deskripsi = 'Ambang hipertensi diubah menjadi 140.0'");
                st.executeUpdate("DELETE FROM sesi WHERE id_pengguna IN "
                        + "(SELECT id_pengguna FROM pengguna WHERE nama_pengguna = '" + NAMA + "')");
                st.executeUpdate("DELETE FROM pengguna WHERE nama_pengguna = '" + NAMA + "'");
                st.executeUpdate("UPDATE ambang_risiko SET nilai_batas = 140 WHERE jenis_risiko = 'hipertensi'");
                st.executeUpdate("DELETE FROM audit WHERE deskripsi LIKE '%Poli Uji%' OR deskripsi LIKE '%Obat Uji%'");
                st.executeUpdate("DELETE FROM poli WHERE nama_poli LIKE 'Poli Uji%'");
                st.executeUpdate("DELETE FROM obat WHERE nama_obat = 'Obat Uji'");
            }
            koneksi.close();
        }

        @Test
        void simpanAkun_baruLaluSuntingDanTolakNamaGanda() throws SQLException {
            PenggunaEntity p = new PenggunaEntity(NAMA, null, "Perawat");
            manager.simpanAkun(p, "rahasia123");
            assertTrue(p.getIdPengguna() > 0);

            assertThrows(IllegalArgumentException.class,
                    () -> manager.simpanAkun(new PenggunaEntity(NAMA, null, "Dokter"), "rahasia456"));

            manager.simpanAkun(new PenggunaEntity(p.getIdPengguna(), NAMA, null, "Dokter", "aktif"), "");
            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(
                    "SELECT peran, kata_sandi_hash FROM pengguna WHERE id_pengguna = " + p.getIdPengguna())) {
                rs.next();
                assertEquals("Dokter", rs.getString(1));
                // sandi kosong saat menyunting tidak mengganti sandi lama
                assertTrue(BCrypt.checkpw("rahasia123", rs.getString(2)));
            }
            assertEquals(2, hitung("SELECT count(*) FROM audit WHERE id_pengguna = 1 AND deskripsi LIKE '%" + NAMA
                    + "%'"));
        }

        @Test
        void nonaktifkanAkun_menutupSesiAktifDanMencatatAudit() throws SQLException {
            PenggunaEntity p = new PenggunaEntity(NAMA, null, "Perawat");
            manager.simpanAkun(p, "rahasia123");
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO sesi (id_sesi, id_pengguna, waktu_mulai, status_aktif) VALUES ('"
                        + UUID.randomUUID() + "', " + p.getIdPengguna() + ", now(), TRUE)");
            }

            manager.nonaktifkanAkun(p.getIdPengguna());

            assertEquals(1, hitung("SELECT count(*) FROM pengguna WHERE status = 'nonaktif' AND id_pengguna = "
                    + p.getIdPengguna()));
            assertEquals(0, hitung("SELECT count(*) FROM sesi WHERE status_aktif AND id_pengguna = "
                    + p.getIdPengguna()));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Akun " + NAMA
                    + " dinonaktifkan'"));
            // sudah nonaktif, jadi ditolak dan tidak ada audit kedua
            assertThrows(IllegalStateException.class, () -> manager.nonaktifkanAkun(p.getIdPengguna()));
        }

        @Test
        void simpanDataMaster_mengubahAmbang() throws SQLException {
            AmbangRisikoEntity hipertensi = manager.muatDataMaster().ambang().stream()
                    .filter(a -> a.getJenisRisiko().equals("hipertensi")).findFirst().orElseThrow();
            hipertensi.setNilaiBatas(150);
            manager.simpanDataMaster(hipertensi);

            assertEquals(1, hitung("SELECT count(*) FROM ambang_risiko WHERE jenis_risiko = 'hipertensi' "
                    + "AND nilai_batas = 150"));
            assertFalse(manager.muatDataMaster().akun().isEmpty());
        }

        @Test
        void simpanPoli_baruLaluGantiNamaTanpaMenimpaUrutan() throws SQLException {
            manager.simpanPoli(0, "Poli Uji");
            int idPoli = hitung("SELECT id_poli FROM poli WHERE nama_poli = 'Poli Uji'");
            // loket sempat menerbitkan nomor 7 sebelum nama diganti
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE poli SET urutan_terakhir = 7 WHERE id_poli = " + idPoli);
            }

            manager.simpanPoli(idPoli, "Poli Uji KIA");

            assertEquals(7, hitung("SELECT urutan_terakhir FROM poli WHERE nama_poli = 'Poli Uji KIA'"));
            assertThrows(IllegalArgumentException.class, () -> manager.simpanPoli(0, "Poli Uji KIA"));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = "
                    + "'Poli Poli Uji diganti nama menjadi Poli Uji KIA'"));
        }

        @Test
        void tambahObat_stokNolDanTolakGanda() throws SQLException {
            ObatEntity o = new ObatEntity(0, " Obat Uji ", "tablet 5 mg", 99, 20);
            manager.tambahObat(o);

            assertTrue(o.getIdObat() > 0);
            assertEquals(0, hitung("SELECT stok FROM obat WHERE id_obat = " + o.getIdObat()));
            assertEquals(20, hitung("SELECT ambang_minimum FROM obat WHERE id_obat = " + o.getIdObat()));
            assertThrows(IllegalArgumentException.class,
                    () -> manager.tambahObat(new ObatEntity(0, "obat uji", "Tablet 5 mg", 0, 0)));
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
