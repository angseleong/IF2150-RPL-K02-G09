package sehati.integrasi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mindrot.jbcrypt.BCrypt;

import sehati.business.control.AutentikasiManager;
import sehati.business.control.KonfigurasiManager;
import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PencadanganEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.PengaturanAplikasi;
import sehati.common.SesiAktif;

/**
 * Pengujian integrasi modul akses dan administrasi (HIUPL Subbab 4.3, INT-TC01 sampai INT-TC06). Manager dijalankan
 * dengan DataBoundary, basis data PostgreSQL, serta pg_dump dan pg_restore yang nyata, tanpa mock. Hasilnya dibaca
 * ulang lewat koneksi terpisah atau langsung dari berkas arsip.
 */
@DisplayName("Integrasi akses dan administrasi (IK-01, IK-02, IK-03)")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class AksesIntegrasiTest {

    // Akun seed (src/db/seed.sql) dengan kata sandi sehati123
    private static final String SANDI_SEED = "sehati123";
    private static final String NAMA_UJI = "uji.integrasi";

    private final AutentikasiManager autentikasi = new AutentikasiManager();
    private final KonfigurasiManager konfigurasi = new KonfigurasiManager();

    private Connection koneksi;
    private int auditTerakhir;
    private int pencadanganTerakhir;
    private final Set<UUID> sesiUji = new HashSet<>();

    @BeforeEach
    void siapkan() throws SQLException {
        koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        auditTerakhir = angka("SELECT coalesce(max(id_audit), 0) FROM audit");
        pencadanganTerakhir = angka("SELECT coalesce(max(id_pencadangan), 0) FROM pencadangan");
    }

    @AfterEach
    void pulihkan() throws SQLException, IOException {
        SesiAktif.lepas();
        try (Statement st = koneksi.createStatement()) {
            for (UUID id : sesiUji) {
                st.executeUpdate("DELETE FROM sesi WHERE id_sesi = '" + id + "'");
            }
            st.executeUpdate("DELETE FROM sesi WHERE id_pengguna IN "
                    + "(SELECT id_pengguna FROM pengguna WHERE nama_pengguna = '" + NAMA_UJI + "')");
            st.executeUpdate("DELETE FROM pengguna WHERE nama_pengguna = '" + NAMA_UJI + "'");
            st.executeUpdate("DELETE FROM poli WHERE nama_poli = 'Poli Uji Integrasi'");
            st.executeUpdate("DELETE FROM obat WHERE nama_obat = 'Obat Uji Integrasi'");
            st.executeUpdate("UPDATE ambang_risiko SET nilai_batas = 30 WHERE jenis_risiko = 'obesitas'");
            st.executeUpdate("DELETE FROM audit WHERE id_audit > " + auditTerakhir);
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
    @DisplayName("INT-TC01 IK-01 normal: masuk dengan akun seed menyimpan sesi dan audit, keluar menutup sesi")
    void masukLaluKeluar() throws SQLException {
        SesiEntity sesi = autentikasi.autentikasi("admin", SANDI_SEED);
        assertNotNull(sesi);
        sesiUji.add(sesi.getIdSesi());

        assertEquals("1|true", teks("SELECT id_pengguna || '|' || status_aktif::text FROM sesi WHERE id_sesi = '"
                + sesi.getIdSesi() + "'"));
        assertEquals("Petugas Administrasi", SesiAktif.peran());
        assertEquals("1|Masuk ke aplikasi", teks("SELECT id_pengguna || '|' || deskripsi FROM audit "
                + "WHERE id_audit > " + auditTerakhir + " ORDER BY id_audit LIMIT 1"));

        autentikasi.tutupSesi(false);

        assertEquals("false", teks("SELECT status_aktif::text FROM sesi WHERE id_sesi = '" + sesi.getIdSesi() + "'"));
        assertEquals("1|Keluar dari aplikasi", teks("SELECT id_pengguna || '|' || deskripsi FROM audit "
                + "WHERE id_audit > " + auditTerakhir + " ORDER BY id_audit DESC LIMIT 1"));
    }

    @Test
    @DisplayName("INT-TC02 IK-01 gagal: sandi salah dan akun nonaktif tidak membuka sesi, percobaan tercatat")
    void masukDitolak() throws SQLException {
        int sesiAwal = angka("SELECT count(*) FROM sesi");

        assertNull(autentikasi.autentikasi("admin", "sandiSalah"));
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> autentikasi.autentikasi("perawat.nonaktif", SANDI_SEED));

        assertEquals("Akun nonaktif, hubungi Petugas Administrasi", e.getMessage());
        assertNull(SesiAktif.ambil());
        assertEquals(sesiAwal, angka("SELECT count(*) FROM sesi"));
        assertEquals("1|Masuk gagal, percobaan ke-1", teks("SELECT id_pengguna || '|' || deskripsi FROM audit "
                + "WHERE id_audit > " + auditTerakhir + " ORDER BY id_audit LIMIT 1"));
    }

    @Test
    @DisplayName("INT-TC03 IK-02 normal: data master dari empat DataBoundary dimuat dan perubahannya sampai ke tabel")
    void dataMasterLintasBoundary() throws SQLException {
        masukSebagaiAdmin();
        KonfigurasiManager.DataMaster d = konfigurasi.muatDataMaster();
        assertEquals(daftar("SELECT nama_pengguna FROM pengguna"),
                d.akun().stream().map(PenggunaEntity::getNamaPengguna).collect(Collectors.toSet()));
        assertEquals(daftar("SELECT nama_poli FROM poli"),
                d.poli().stream().map(p -> p.getNamaPoli()).collect(Collectors.toSet()));
        assertEquals(daftar("SELECT nama_obat || ' ' || sediaan FROM obat"),
                d.obat().stream().map(o -> o.getNamaObat() + " " + o.getSediaan()).collect(Collectors.toSet()));
        assertEquals(daftar("SELECT jenis_risiko FROM ambang_risiko"),
                d.ambang().stream().map(AmbangRisikoEntity::getJenisRisiko).collect(Collectors.toSet()));

        konfigurasi.simpanAkun(new PenggunaEntity(NAMA_UJI, null, "Dokter"), "integrasi123");
        konfigurasi.simpanPoli(0, "Poli Uji Integrasi");
        konfigurasi.tambahObat(new ObatEntity(0, "Obat Uji Integrasi", "tablet 10 mg", 0, 15));
        AmbangRisikoEntity obesitas = d.ambang().stream().filter(a -> a.getJenisRisiko().equals("obesitas"))
                .findFirst().orElseThrow();
        obesitas.setNilaiBatas(31);
        konfigurasi.simpanDataMaster(obesitas);

        String hash = teks("SELECT kata_sandi_hash FROM pengguna WHERE nama_pengguna = '" + NAMA_UJI + "'");
        assertTrue(BCrypt.checkpw("integrasi123", hash));
        assertEquals("Dokter|aktif", teks("SELECT peran || '|' || status FROM pengguna WHERE nama_pengguna = '"
                + NAMA_UJI + "'"));
        assertEquals(1, angka("SELECT count(*) FROM poli WHERE nama_poli = 'Poli Uji Integrasi'"));
        assertEquals("0|15", teks("SELECT stok || '|' || ambang_minimum FROM obat WHERE nama_obat = "
                + "'Obat Uji Integrasi'"));
        assertEquals(31, angka("SELECT nilai_batas FROM ambang_risiko WHERE jenis_risiko = 'obesitas'"));
        // Setiap perubahan diaudit pada transaksi yang sama, dengan pelaku admin
        assertEquals(4, angka("SELECT count(*) FROM audit WHERE id_pengguna = 1 AND id_audit > " + auditTerakhir));
    }

    @Test
    @DisplayName("INT-TC04 IK-02 gagal: penolakan dari DataBoundary membatalkan transaksi tanpa audit")
    void dataMasterDitolak() throws SQLException {
        masukSebagaiAdmin();
        int poliAwal = angka("SELECT count(*) FROM poli");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> konfigurasi.simpanDataMaster(new AmbangRisikoEntity(9999, "fiktif", 50)));
        assertEquals("Ambang risiko tidak ditemukan: 9999", e.getMessage());
        // nama poli seed, ditolak constraint UNIQUE di basis data
        assertThrows(IllegalArgumentException.class, () -> konfigurasi.simpanPoli(0, "Poli Umum"));

        assertEquals(poliAwal, angka("SELECT count(*) FROM poli"));
        assertEquals(0, angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir));
    }

    @Test
    @DisplayName("INT-TC05 IK-03 normal: pg_dump menulis arsip ke media, pg_restore membacanya, catatan tersimpan")
    void pencadanganBerhasil() throws Exception {
        PengaturanAplikasi pengaturan = pastikanPgDump();
        PencadanganEntity p = konfigurasi.jalankanPencadangan();

        assertEquals("berhasil", p.getStatus());
        Path arsip = Path.of(p.getLokasiMedia());
        assertTrue(arsip.startsWith(Path.of(pengaturan.ambil("LOKASI_MEDIA_CADANGAN"))));
        assertEquals(Files.size(arsip), p.getUkuranBerkas());
        String daftarIsi = jalankan(pengaturan.ambil("PG_RESTORE", "pg_restore"), "--list", arsip.toString());
        assertTrue(daftarIsi.contains("TABLE DATA public pasien "));
        assertTrue(daftarIsi.contains("TABLE DATA public audit "));
        assertEquals("berhasil|" + p.getUkuranBerkas(), teks("SELECT status || '|' || ukuran_berkas FROM pencadangan "
                + "WHERE id_pencadangan = " + p.getIdPencadangan()));
        assertEquals(p.getIdPencadangan(), konfigurasi.muatStatusPencadangan().getIdPencadangan());
    }

    @Test
    @DisplayName("INT-TC06 IK-03 gagal: pg_dump ditolak basis data, tidak ada arsip, catatan gagal sampai ke dasbor")
    void pencadanganGagal() throws Exception {
        PengaturanAplikasi pengaturan = pastikanPgDump();
        assumeTrue("sehati_app".equals(pengaturan.ambil("DB_USER")));
        Path media = Path.of(pengaturan.ambil("LOKASI_MEDIA_CADANGAN"));
        long arsipAwal = hitungArsip(media);

        eksekusi("REVOKE SELECT ON pasien FROM sehati_app");
        PencadanganEntity p;
        try {
            p = konfigurasi.jalankanPencadangan();
        } finally {
            eksekusi("GRANT SELECT ON pasien TO sehati_app");
        }

        assertEquals("gagal", p.getStatus());
        assertEquals(0, p.getUkuranBerkas());
        assertEquals(arsipAwal, hitungArsip(media));
        PencadanganEntity dasbor = konfigurasi.muatStatusPencadangan();
        assertEquals(p.getIdPencadangan(), dasbor.getIdPencadangan());
        assertFalse(dasbor.berhasil());
    }

    private static void masukSebagaiAdmin() {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Petugas Administrasi");
    }

    private static PengaturanAplikasi pastikanPgDump() {
        PengaturanAplikasi p = PengaturanAplikasi.muat();
        assumeTrue(Files.isExecutable(Path.of(p.ambil("PG_DUMP", "pg_dump")))
                && !p.ambil("LOKASI_MEDIA_CADANGAN", "").isBlank(), "PG_DUMP dan LOKASI_MEDIA_CADANGAN wajib diisi");
        return p;
    }

    private static long hitungArsip(Path media) throws IOException {
        try (Stream<Path> s = Files.list(media)) {
            return s.filter(f -> f.getFileName().toString().matches("sehati_\\d{8}-\\d{6}\\.dump")).count();
        }
    }

    private static String jalankan(String... perintah) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(perintah).redirectErrorStream(true).start();
        String keluaran = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(0, p.waitFor(), keluaran);
        return keluaran;
    }

    private Set<String> daftar(String sql) throws SQLException {
        Set<String> hasil = new HashSet<>();
        try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                hasil.add(rs.getString(1));
            }
        }
        return hasil;
    }

    private void eksekusi(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement()) {
            st.execute(sql);
        }
    }

    private int angka(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private String teks(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next(), sql);
            return rs.getString(1);
        }
    }
}
