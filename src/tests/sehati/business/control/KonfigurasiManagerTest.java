package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mindrot.jbcrypt.BCrypt;

import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PencadanganEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.PengaturanAplikasi;
import sehati.common.SesiAktif;

// Pengujian unit C29 KonfigurasiManager (HIUPL Subbab 4.2, C29-UT01 sampai C29-UT37).
@DisplayName("C29 KonfigurasiManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class KonfigurasiManagerTest {

    private final KonfigurasiManager manager = new KonfigurasiManager();

    @AfterEach
    void lepasSesi() {
        SesiAktif.lepas();
    }

    private static void masukSebagai(int idPengguna) {
        masukSebagai(idPengguna, "Petugas Administrasi");
    }

    private static void masukSebagai(int idPengguna, String peran) {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), idPengguna, LocalDateTime.now(), true), peran);
    }

    @Test
    @DisplayName("C29-UT08 simpanAkun negatif: nama kosong dan peran tidak dikenal ditolak")
    void simpanAkun_namaKosongPeranTidakDikenal() {
        masukSebagai(1);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity(" ", null, "Perawat"), "rahasia123"));
        assertEquals("Nama pengguna wajib diisi", e.getMessage());
        e = assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Satpam"), "rahasia123"));
        assertEquals("Peran tidak dikenal", e.getMessage());
    }

    @Test
    @DisplayName("C29-UT09 simpanAkun negatif: sandi 7 karakter, kosong untuk akun baru, dan sandi pendek untuk akun lama")
    void simpanAkun_sandiPendek() {
        masukSebagai(1);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Perawat"), "1234567"));
        assertEquals("Kata sandi minimal 8 karakter", e.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Perawat"), ""));
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Perawat"), null));
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanAkun(new PenggunaEntity(5, "uji", null, "Perawat", "aktif"), "pendek"));
    }

    @Test
    @DisplayName("C29-UT10 simpanAkun negatif: tanpa sesi dan sesi berperan Perawat ditolak")
    void simpanAkun_tanpaHakAkses() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Perawat"), "rahasia123"));
        assertEquals("Sesi sudah berakhir, silakan masuk kembali", e.getMessage());

        masukSebagai(2, "Perawat");
        e = assertThrows(IllegalStateException.class,
                () -> manager.simpanAkun(new PenggunaEntity("uji", null, "Perawat"), "rahasia123"));
        assertEquals("Peran Perawat tidak berwenang menjalankan tindakan ini", e.getMessage());
    }

    @Test
    @DisplayName("C29-UT15 nonaktifkanAkun negatif: akun yang sedang dipakai ditolak")
    void nonaktifkanAkun_akunSendiri() {
        masukSebagai(7);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.nonaktifkanAkun(7));
        assertEquals("Akun yang sedang dipakai tidak dapat dinonaktifkan", e.getMessage());
    }

    @Test
    @DisplayName("C29-UT19 simpanDataMaster negatif: nilai nol, negatif, dan NaN ditolak")
    void simpanDataMaster_nilaiTidakSah() {
        masukSebagai(1);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.simpanDataMaster(new AmbangRisikoEntity(1, "hipertensi", 0)));
        assertEquals("Nilai ambang harus lebih dari nol", e.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanDataMaster(new AmbangRisikoEntity(1, "hipertensi", -5)));
        assertThrows(IllegalArgumentException.class,
                () -> manager.simpanDataMaster(new AmbangRisikoEntity(1, "hipertensi", Double.NaN)));
    }

    @Test
    @DisplayName("C29-UT22 simpanPoli negatif: nama kosong, spasi, atau null ditolak")
    void simpanPoli_namaKosong() {
        masukSebagai(1);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.simpanPoli(0, "  "));
        assertEquals("Nama poli wajib diisi", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> manager.simpanPoli(0, null));
    }

    @Test
    @DisplayName("C29-UT26 tambahObat negatif: sediaan kosong dan stok minimum negatif ditolak")
    void tambahObat_isianTidakSah() {
        masukSebagai(1);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.tambahObat(new ObatEntity(0, "Parasetamol", " ", 0, 10)));
        assertEquals("Nama obat dan sediaan wajib diisi", e.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> manager.tambahObat(new ObatEntity(0, null, "tablet", 0, 10)));
        e = assertThrows(IllegalArgumentException.class,
                () -> manager.tambahObat(new ObatEntity(0, "Parasetamol", "tablet 500 mg", 0, -1)));
        assertEquals("Stok minimum tidak boleh negatif", e.getMessage());
    }

    @Test
    @DisplayName("C29-UT34 jedaSampai normal: jam berikutnya pada hari yang sama")
    void jedaSampai_hariIni() {
        assertEquals(30, KonfigurasiManager.jedaSampai(21, LocalDateTime.of(2026, 11, 8, 20, 30)));
    }

    @Test
    @DisplayName("C29-UT35 jedaSampai batas: tepat pada jamnya dan jam yang sudah lewat dijadwalkan besok")
    void jedaSampai_tepatAtauLewat() {
        LocalDateTime pukul2030 = LocalDateTime.of(2026, 11, 8, 20, 30);
        assertEquals(24 * 60, KonfigurasiManager.jedaSampai(20, LocalDateTime.of(2026, 11, 8, 20, 0)));
        assertEquals(23 * 60 + 30, KonfigurasiManager.jedaSampai(20, pukul2030));
        assertEquals(3 * 60 + 30, KonfigurasiManager.jedaSampai(0, pukul2030));
    }

    @Test
    @DisplayName("C29-UT36 jedaSampai negatif: jam di luar 0 sampai 23 ditolak")
    void jedaSampai_jamTidakSah() {
        LocalDateTime t = LocalDateTime.of(2026, 11, 8, 20, 30);
        assertThrows(DateTimeException.class, () -> KonfigurasiManager.jedaSampai(24, t));
        assertThrows(DateTimeException.class, () -> KonfigurasiManager.jedaSampai(-1, t));
    }

    @Test
    @DisplayName("C29-UT37 jadwalkanPencadangan negatif: PENCADANGAN_JAM kosong tidak membuat penjadwal")
    void jadwalkanPencadangan_tidakDiisi() {
        assumeTrue(PengaturanAplikasi.muat().ambil("PENCADANGAN_JAM", "").isBlank());
        KonfigurasiManager.jadwalkanPencadangan();
        assertFalse(Thread.getAllStackTraces().keySet().stream().anyMatch(t -> t.getName().equals("pencadangan")));
    }

    // Manager buka koneksi sendiri lewat KoneksiBasisData, jadi DB_* di .env harus ke DB yang sama dengan
    // DB_UJI_URL. Data yang dibuat dihapus setelah setiap tes.
    @Nested
    @DisplayName("C29 KonfigurasiManager dengan basis data")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private static final String NAMA = "uji.konfigurasi";

        private Connection koneksi;
        private int pencadanganTerakhir;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            pencadanganTerakhir = hitung("SELECT coalesce(max(id_pencadangan), 0) FROM pencadangan");
            masukSebagai(1);
        }

        @AfterEach
        void bersihkan() throws SQLException, IOException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM audit WHERE deskripsi LIKE '%" + NAMA + "%'"
                        + " OR deskripsi LIKE 'Ambang hipertensi diubah menjadi %'");
                st.executeUpdate("DELETE FROM sesi WHERE id_pengguna IN "
                        + "(SELECT id_pengguna FROM pengguna WHERE nama_pengguna = '" + NAMA + "')");
                st.executeUpdate("DELETE FROM pengguna WHERE nama_pengguna = '" + NAMA + "'");
                st.executeUpdate("UPDATE ambang_risiko SET nilai_batas = 140 WHERE jenis_risiko = 'hipertensi'");
                st.executeUpdate("DELETE FROM audit WHERE deskripsi LIKE '%Poli Uji%' OR deskripsi LIKE '%Obat Uji%'");
                st.executeUpdate("DELETE FROM poli WHERE nama_poli LIKE 'Poli Uji%'");
                st.executeUpdate("DELETE FROM obat WHERE nama_obat = 'Obat Uji'");
                try (ResultSet rs = st.executeQuery("SELECT lokasi_media FROM pencadangan WHERE id_pencadangan > "
                        + pencadanganTerakhir)) {
                    while (rs.next()) {
                        if (!rs.getString(1).isBlank()) {
                            Files.deleteIfExists(Path.of(rs.getString(1)));
                        }
                    }
                }
                st.executeUpdate("DELETE FROM pencadangan WHERE id_pencadangan > " + pencadanganTerakhir);
            }
            koneksi.close();
        }

        @Test
        @DisplayName("C29-UT01 muatDataMaster normal: akun, poli, obat, dan ambang sama dengan isi tabel")
        void muatDataMaster_lengkap() throws SQLException {
            KonfigurasiManager.DataMaster d = manager.muatDataMaster();

            assertEquals(hitung("SELECT count(*) FROM pengguna"), d.akun().size());
            assertEquals(hitung("SELECT count(*) FROM poli"), d.poli().size());
            assertEquals(hitung("SELECT count(*) FROM obat"), d.obat().size());
            assertEquals(hitung("SELECT count(*) FROM ambang_risiko"), d.ambang().size());
        }

        @Test
        @DisplayName("C29-UT02 muatDataMaster batas: akun nonaktif tetap dimuat agar dapat dilihat admin")
        void muatDataMaster_akunNonaktif() {
            assertTrue(manager.muatDataMaster().akun().stream()
                    .anyMatch(p -> p.getNamaPengguna().equals("perawat.nonaktif") && !p.aktif()));
        }

        @Test
        @DisplayName("C29-UT03 muatDataMaster negatif: basis data menolak akses ke tabel pengguna")
        void muatDataMaster_aksesDitolak() throws SQLException {
            assumeTrue("sehati_app".equals(PengaturanAplikasi.muat().ambil("DB_USER")));
            eksekusi("REVOKE SELECT ON pengguna FROM sehati_app");
            try {
                IllegalStateException e = assertThrows(IllegalStateException.class, manager::muatDataMaster);
                assertEquals("Basis data tidak dapat dihubungi", e.getMessage());
            } finally {
                eksekusi("GRANT SELECT ON pengguna TO sehati_app");
            }
            assertFalse(manager.muatDataMaster().akun().isEmpty());
        }

        @Test
        @DisplayName("C29-UT04 simpanAkun normal: akun baru tersimpan dengan hash bcrypt dan audit")
        void simpanAkun_akunBaru() throws SQLException {
            PenggunaEntity p = new PenggunaEntity(NAMA, null, "Perawat");
            manager.simpanAkun(p, "rahasia123");

            assertTrue(p.getIdPengguna() > 0);
            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(
                    "SELECT peran, status, kata_sandi_hash FROM pengguna WHERE id_pengguna = " + p.getIdPengguna())) {
                assertTrue(rs.next());
                assertEquals("Perawat", rs.getString(1));
                assertEquals("aktif", rs.getString(2));
                assertTrue(rs.getString(3).startsWith("$2a$12$"));
                assertTrue(BCrypt.checkpw("rahasia123", rs.getString(3)));
            }
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_pengguna = 1 AND deskripsi = 'Akun baru "
                    + NAMA + " (Perawat)'"));
        }

        @Test
        @DisplayName("C29-UT05 simpanAkun batas: sandi tepat 8 karakter diterima")
        void simpanAkun_sandiDelapan() throws SQLException {
            manager.simpanAkun(new PenggunaEntity(NAMA, null, "Dokter"), "12345678");
            assertEquals(1, hitung("SELECT count(*) FROM pengguna WHERE nama_pengguna = '" + NAMA + "'"));
        }

        @Test
        @DisplayName("C29-UT06 simpanAkun batas: sunting dengan sandi kosong mengubah peran tanpa mengganti sandi")
        void simpanAkun_suntingTanpaSandi() throws SQLException {
            PenggunaEntity p = new PenggunaEntity(NAMA, null, "Perawat");
            manager.simpanAkun(p, "rahasia123");
            manager.simpanAkun(new PenggunaEntity(p.getIdPengguna(), NAMA, null, "Dokter", "aktif"), "");

            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(
                    "SELECT peran, kata_sandi_hash FROM pengguna WHERE id_pengguna = " + p.getIdPengguna())) {
                rs.next();
                assertEquals("Dokter", rs.getString(1));
                assertTrue(BCrypt.checkpw("rahasia123", rs.getString(2)));
            }
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Perubahan akun " + NAMA + "'"));
        }

        @Test
        @DisplayName("C29-UT07 simpanAkun negatif: nama pengguna yang sudah dipakai ditolak")
        void simpanAkun_namaGanda() throws SQLException {
            manager.simpanAkun(new PenggunaEntity(NAMA, null, "Perawat"), "rahasia123");
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.simpanAkun(new PenggunaEntity(NAMA, null, "Dokter"), "rahasia456"));
            assertEquals("Nama pengguna sudah dipakai", e.getMessage());
            assertEquals(1, hitung("SELECT count(*) FROM pengguna WHERE nama_pengguna = '" + NAMA + "'"));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi LIKE '%" + NAMA + "%'"));
        }

        @Test
        @DisplayName("C29-UT11 simpanAkun negatif: peran akun yang sedang dipakai tidak dapat diubah")
        void simpanAkun_peranSendiri() throws SQLException {
            PenggunaEntity p = new PenggunaEntity(NAMA, null, "Petugas Administrasi");
            manager.simpanAkun(p, "rahasia123");
            masukSebagai(p.getIdPengguna());

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.simpanAkun(
                    new PenggunaEntity(p.getIdPengguna(), NAMA, null, "Perawat", "aktif"), ""));
            assertEquals("Peran akun yang sedang dipakai tidak dapat diubah", e.getMessage());
            assertEquals(1, hitung("SELECT count(*) FROM pengguna WHERE peran = 'Petugas Administrasi'"
                    + " AND id_pengguna = " + p.getIdPengguna()));
            // Mengganti sandi sendiri dengan peran yang sama tetap boleh
            manager.simpanAkun(new PenggunaEntity(p.getIdPengguna(), NAMA, null, "Petugas Administrasi", "aktif"),
                    "rahasia456");
        }

        @Test
        @DisplayName("C29-UT12 simpanAkun negatif: menyunting akun yang tidak ada ditolak")
        void simpanAkun_akunTidakAda() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.simpanAkun(
                    new PenggunaEntity(999999, NAMA, null, "Perawat", "aktif"), ""));
            assertEquals("Akun tidak ditemukan", e.getMessage());
        }

        @Test
        @DisplayName("C29-UT13 nonaktifkanAkun normal: status nonaktif, sesi aktif ditutup, audit tercatat")
        void nonaktifkanAkun_berhasil() throws SQLException {
            PenggunaEntity p = akunUjiDenganSesi();

            manager.nonaktifkanAkun(p.getIdPengguna());

            assertEquals(1, hitung("SELECT count(*) FROM pengguna WHERE status = 'nonaktif' AND id_pengguna = "
                    + p.getIdPengguna()));
            assertEquals(0, hitung("SELECT count(*) FROM sesi WHERE status_aktif AND id_pengguna = "
                    + p.getIdPengguna()));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_pengguna = 1 AND deskripsi = 'Akun " + NAMA
                    + " dinonaktifkan'"));
        }

        @Test
        @DisplayName("C29-UT14 nonaktifkanAkun batas: akun yang sudah nonaktif ditolak tanpa audit kedua")
        void nonaktifkanAkun_duaKali() throws SQLException {
            PenggunaEntity p = akunUjiDenganSesi();
            manager.nonaktifkanAkun(p.getIdPengguna());

            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> manager.nonaktifkanAkun(p.getIdPengguna()));
            assertEquals("Akun sudah nonaktif", e.getMessage());
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Akun " + NAMA + " dinonaktifkan'"));
        }

        @Test
        @DisplayName("C29-UT16 nonaktifkanAkun negatif: akun yang tidak ada ditolak")
        void nonaktifkanAkun_akunTidakAda() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.nonaktifkanAkun(999999));
            assertEquals("Akun tidak ditemukan", e.getMessage());
        }

        @Test
        @DisplayName("C29-UT17 simpanDataMaster normal: ambang hipertensi diubah menjadi 150 dan diaudit")
        void simpanDataMaster_ubahAmbang() throws SQLException {
            AmbangRisikoEntity hipertensi = ambangHipertensi();
            hipertensi.setNilaiBatas(150);
            manager.simpanDataMaster(hipertensi);

            assertEquals(150, hitung("SELECT nilai_batas FROM ambang_risiko WHERE jenis_risiko = 'hipertensi'"));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_pengguna = 1 AND deskripsi = "
                    + "'Ambang hipertensi diubah menjadi 150.0'"));
        }

        @Test
        @DisplayName("C29-UT18 simpanDataMaster batas: nilai positif terkecil 0,1 diterima")
        void simpanDataMaster_nilaiTerkecil() throws SQLException {
            AmbangRisikoEntity hipertensi = ambangHipertensi();
            hipertensi.setNilaiBatas(0.1);
            manager.simpanDataMaster(hipertensi);

            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(
                    "SELECT nilai_batas FROM ambang_risiko WHERE jenis_risiko = 'hipertensi'")) {
                rs.next();
                assertEquals(0.1, rs.getDouble(1), 1e-9);
            }
        }

        @Test
        @DisplayName("C29-UT20 simpanPoli normal: poli baru tersimpan dengan audit")
        void simpanPoli_baru() throws SQLException {
            manager.simpanPoli(0, "  Poli Uji  ");
            assertEquals(1, hitung("SELECT count(*) FROM poli WHERE nama_poli = 'Poli Uji'"));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Poli baru Poli Uji'"));
        }

        @Test
        @DisplayName("C29-UT21 simpanPoli batas: ganti nama tidak menimpa nomor antrean terakhir")
        void simpanPoli_gantiNama() throws SQLException {
            manager.simpanPoli(0, "Poli Uji");
            int idPoli = hitung("SELECT id_poli FROM poli WHERE nama_poli = 'Poli Uji'");
            // loket sempat menerbitkan nomor 7 sebelum nama diganti
            eksekusi("UPDATE poli SET urutan_terakhir = 7 WHERE id_poli = " + idPoli);

            manager.simpanPoli(idPoli, "Poli Uji KIA");

            assertEquals(7, hitung("SELECT urutan_terakhir FROM poli WHERE nama_poli = 'Poli Uji KIA'"));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = "
                    + "'Poli Poli Uji diganti nama menjadi Poli Uji KIA'"));
        }

        @Test
        @DisplayName("C29-UT23 simpanPoli negatif: nama ganda dan poli yang tidak ada ditolak")
        void simpanPoli_gandaAtauTidakAda() throws SQLException {
            manager.simpanPoli(0, "Poli Uji");
            assertThrows(IllegalArgumentException.class, () -> manager.simpanPoli(0, "Poli Uji"));
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.simpanPoli(999999, "Poli Uji Lain"));
            assertEquals("Poli tidak ditemukan", e.getMessage());
            assertEquals(1, hitung("SELECT count(*) FROM poli WHERE nama_poli LIKE 'Poli Uji%'"));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi LIKE '%Poli Uji%'"));
        }

        @Test
        @DisplayName("C29-UT24 tambahObat normal: obat baru berstok 0 dengan ambang minimum dan audit")
        void tambahObat_baru() throws SQLException {
            ObatEntity o = new ObatEntity(0, "Obat Uji", "tablet 5 mg", 99, 20);
            manager.tambahObat(o);

            assertTrue(o.getIdObat() > 0);
            assertEquals(0, hitung("SELECT stok FROM obat WHERE id_obat = " + o.getIdObat()));
            assertEquals(20, hitung("SELECT ambang_minimum FROM obat WHERE id_obat = " + o.getIdObat()));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE deskripsi = 'Obat baru Obat Uji (tablet 5 mg)'"));
        }

        @Test
        @DisplayName("C29-UT25 tambahObat batas: ambang minimum 0 diterima dan spasi tepi dibuang")
        void tambahObat_minimumNol() throws SQLException {
            ObatEntity o = new ObatEntity(0, "  Obat Uji ", " sirup 60 ml ", 0, 0);
            manager.tambahObat(o);

            assertEquals(1, hitung("SELECT count(*) FROM obat WHERE nama_obat = 'Obat Uji' AND sediaan = 'sirup 60 ml'"
                    + " AND ambang_minimum = 0"));
        }

        @Test
        @DisplayName("C29-UT27 tambahObat negatif: nama dan sediaan yang sama (beda huruf) ditolak")
        void tambahObat_ganda() throws SQLException {
            manager.tambahObat(new ObatEntity(0, "Obat Uji", "tablet 5 mg", 0, 20));
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.tambahObat(new ObatEntity(0, "obat uji", "Tablet 5 mg", 0, 0)));
            assertEquals("Obat obat uji Tablet 5 mg sudah terdaftar", e.getMessage());
            assertEquals(1, hitung("SELECT count(*) FROM obat WHERE nama_obat = 'Obat Uji'"));
        }

        @Test
        @DisplayName("C29-UT28 muatStatusPencadangan normal: catatan pencadangan terbaru dikembalikan")
        void muatStatusPencadangan_terbaru() throws SQLException {
            int id = sisipCatatan("berhasil");
            PencadanganEntity p = manager.muatStatusPencadangan();
            assertEquals(id, p.getIdPencadangan());
            assertTrue(p.berhasil());
        }

        @Test
        @DisplayName("C29-UT29 muatStatusPencadangan batas: catatan terbaru gagal dikembalikan agar peringatan tampil")
        void muatStatusPencadangan_terakhirGagal() throws SQLException {
            sisipCatatan("berhasil");
            int idGagal = sisipCatatan("gagal");
            PencadanganEntity p = manager.muatStatusPencadangan();
            assertEquals(idGagal, p.getIdPencadangan());
            assertFalse(p.berhasil());
        }

        @Test
        @DisplayName("C29-UT30 muatStatusPencadangan negatif: belum pernah ada pencadangan mengembalikan null")
        void muatStatusPencadangan_kosong() throws SQLException {
            // Catatan lama dipindah ke tabel sementara di koneksi uji, lalu dikembalikan.
            eksekusi("CREATE TEMP TABLE pencadangan_simpan AS SELECT * FROM pencadangan");
            eksekusi("DELETE FROM pencadangan");
            try {
                assertNull(manager.muatStatusPencadangan());
            } finally {
                eksekusi("INSERT INTO pencadangan OVERRIDING SYSTEM VALUE SELECT * FROM pencadangan_simpan");
                eksekusi("DROP TABLE pencadangan_simpan");
            }
        }

        @Test
        @DisplayName("C29-UT31 jalankanPencadangan normal: pg_dump membuat arsip terverifikasi dan catatan tersimpan")
        void jalankanPencadangan_berhasil() throws SQLException {
            assumePgDumpTersedia();
            PencadanganEntity p = manager.jalankanPencadangan();

            assertEquals("berhasil", p.getStatus());
            assertTrue(Files.isRegularFile(Path.of(p.getLokasiMedia())));
            assertTrue(p.getUkuranBerkas() > 0);
            assertEquals(1, hitung("SELECT count(*) FROM pencadangan WHERE status = 'berhasil' AND id_pencadangan = "
                    + p.getIdPencadangan()));
        }

        @Test
        @DisplayName("C29-UT32 jalankanPencadangan batas: berhasil saat transaksi lain sedang terbuka (aplikasi dipakai)")
        void jalankanPencadangan_saatDipakai() throws SQLException {
            assumePgDumpTersedia();
            try (Connection lain = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
                    Statement st = lain.createStatement()) {
                lain.setAutoCommit(false);
                st.executeUpdate("UPDATE ambang_risiko SET nilai_batas = 141 WHERE jenis_risiko = 'hipertensi'");
                long mulai = System.nanoTime();
                PencadanganEntity p = manager.jalankanPencadangan();
                long detik = (System.nanoTime() - mulai) / 1_000_000_000L;
                lain.rollback();

                assertEquals("berhasil", p.getStatus());
                // pg_dump tidak menunggu kunci baris transaksi yang belum selesai
                assertTrue(detik < 60, "pencadangan butuh " + detik + " detik");
            }
        }

        @Test
        @DisplayName("C29-UT33 jalankanPencadangan negatif: pg_dump gagal, arsip setengah jadi dihapus, catatan gagal")
        void jalankanPencadangan_pgDumpGagal() throws SQLException {
            assumePgDumpTersedia();
            assumeTrue("sehati_app".equals(PengaturanAplikasi.muat().ambil("DB_USER")));
            eksekusi("REVOKE SELECT ON pasien FROM sehati_app");
            PencadanganEntity p;
            try {
                p = manager.jalankanPencadangan();
            } finally {
                eksekusi("GRANT SELECT ON pasien TO sehati_app");
            }
            assertEquals("gagal", p.getStatus());
            assertFalse(Files.exists(Path.of(p.getLokasiMedia())));
            assertEquals(1, hitung("SELECT count(*) FROM pencadangan WHERE status = 'gagal' AND id_pencadangan = "
                    + p.getIdPencadangan()));
        }

        private PenggunaEntity akunUjiDenganSesi() throws SQLException {
            PenggunaEntity p = new PenggunaEntity(NAMA, null, "Perawat");
            manager.simpanAkun(p, "rahasia123");
            eksekusi("INSERT INTO sesi (id_sesi, id_pengguna, waktu_mulai, status_aktif) VALUES ('"
                    + UUID.randomUUID() + "', " + p.getIdPengguna() + ", now(), TRUE)");
            return p;
        }

        private AmbangRisikoEntity ambangHipertensi() {
            return manager.muatDataMaster().ambang().stream()
                    .filter(a -> a.getJenisRisiko().equals("hipertensi")).findFirst().orElseThrow();
        }

        // Waktu jauh di depan supaya menjadi catatan terbaru
        private int sisipCatatan(String status) throws SQLException {
            return hitung("INSERT INTO pencadangan (waktu, status, lokasi_media, ukuran_berkas) VALUES "
                    + "(now() + interval '50 years' + (SELECT count(*) FROM pencadangan) * interval '1 minute', '"
                    + status + "', '', 0) RETURNING id_pencadangan");
        }

        private void assumePgDumpTersedia() {
            PengaturanAplikasi p = PengaturanAplikasi.muat();
            String pgDump = p.ambil("PG_DUMP", "pg_dump");
            assumeTrue(Files.isExecutable(Path.of(pgDump)) && !p.ambil("LOKASI_MEDIA_CADANGAN", "").isBlank(),
                    "PG_DUMP dan LOKASI_MEDIA_CADANGAN harus diisi di .env");
        }

        private void eksekusi(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.execute(sql);
            }
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
