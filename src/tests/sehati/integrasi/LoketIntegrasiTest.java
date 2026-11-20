package sehati.integrasi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.control.KunjunganManager;
import sehati.business.control.PasienManager;
import sehati.business.control.PemeriksaanManager;
import sehati.business.control.SkriningManager;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.SesiEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;

/**
 * Pengujian integrasi modul loket (HIUPL Subbab 4.3, INT-TC07 sampai INT-TC10, INT-TC33 sampai INT-TC36). Manager
 * dijalankan dengan DataBoundary dan basis data PostgreSQL yang nyata, tanpa mock. Setiap hasil dibaca ulang lewat
 * koneksi terpisah, sehingga yang diperiksa adalah baris yang benar-benar sampai di tabel.
 */
@DisplayName("Integrasi loket dan antrean (IK-04, IK-05, IK-17, IK-18)")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class LoketIntegrasiTest {

    private final PasienManager pasien = new PasienManager();
    private final KunjunganManager kunjungan = new KunjunganManager();
    private final SkriningManager skrining = new SkriningManager();
    private final PemeriksaanManager pemeriksaan = new PemeriksaanManager();

    private Connection koneksi;
    private int auditTerakhir;
    private final List<Integer> pasienUji = new ArrayList<>();
    private final List<Integer> kunjunganUji = new ArrayList<>();
    private final List<Integer> poliUji = new ArrayList<>();

    @BeforeEach
    void siapkan() throws SQLException {
        koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        auditTerakhir = angka("SELECT coalesce(max(id_audit), 0) FROM audit");
        masuk("Petugas Administrasi", 1);
    }

    @AfterEach
    void pulihkan() throws SQLException {
        SesiAktif.lepas();
        try (Statement st = koneksi.createStatement()) {
            for (int idPasien : pasienUji) {
                st.executeUpdate("DELETE FROM daftar_pantau WHERE id_pasien = " + idPasien);
                st.executeUpdate("DELETE FROM jadwal_kontrol WHERE id_pasien = " + idPasien);
            }
            for (int idKunjungan : kunjunganUji) {
                st.executeUpdate("DELETE FROM diagnosis WHERE id_pemeriksaan IN "
                        + "(SELECT id_pemeriksaan FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan + ")");
                st.executeUpdate("DELETE FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("DELETE FROM tanda_vital WHERE id_skrining IN "
                        + "(SELECT id_skrining FROM skrining WHERE id_kunjungan = " + idKunjungan + ")");
                st.executeUpdate("DELETE FROM skrining WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("DELETE FROM penanda_risiko WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("DELETE FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("DELETE FROM antrean_entri WHERE id_entri = " + idKunjungan
                        + " AND id_antrean IN (SELECT id_antrean FROM antrean WHERE jenis = 'skrining')");
                st.executeUpdate("DELETE FROM kunjungan WHERE id_kunjungan = " + idKunjungan);
            }
            for (int idPasien : pasienUji) {
                st.executeUpdate("DELETE FROM rekam_medis WHERE id_pasien = " + idPasien);
                st.executeUpdate("DELETE FROM pasien WHERE id_pasien = " + idPasien);
            }
            for (int idPoli : poliUji) {
                st.executeUpdate("DELETE FROM poli WHERE id_poli = " + idPoli);
            }
            st.executeUpdate("DELETE FROM audit WHERE id_audit > " + auditTerakhir);
        }
        koneksi.close();
    }

    @Test
    @DisplayName("INT-TC07 IK-04 normal: pendaftaran menulis pasien dan rekam medis, pembaruan sampai ke tabel")
    void ik04_pendaftaranDanPembaruan() throws SQLException {
        String nik = nikUnik();
        PasienEntity terdaftar = pasien.daftarkanPasien(dataPasien(0, nik, "Pasien Integrasi Loket"));
        pasienUji.add(terdaftar.getIdPasien());

        assertTrue(terdaftar.getNomorRekamMedis().matches("^RM-[0-9]{6,}$"));
        assertEquals(nik + "|Pasien Integrasi Loket|" + terdaftar.getNomorRekamMedis(), teks(
                "SELECT nik || '|' || nama || '|' || nomor_rekam_medis FROM pasien WHERE id_pasien = "
                        + terdaftar.getIdPasien()));
        assertEquals(1, angka("SELECT count(*) FROM rekam_medis WHERE id_pasien = " + terdaftar.getIdPasien()));
        assertEquals("1|Pendaftaran pasien " + terdaftar.getNomorRekamMedis(), teks(
                "SELECT id_pengguna || '|' || deskripsi FROM audit WHERE id_audit > " + auditTerakhir
                        + " ORDER BY id_audit LIMIT 1"));

        PasienEntity usulan = dataPasien(terdaftar.getIdPasien(), nikUnik(), "Pasien Integrasi Diubah");
        usulan.setAlamat("Jl. Integrasi No. 9");
        pasien.perbaruiPasien(usulan);

        assertEquals(usulan.getNik() + "|Pasien Integrasi Diubah|Jl. Integrasi No. 9|"
                + terdaftar.getNomorRekamMedis(), teks(
                        "SELECT nik || '|' || nama || '|' || alamat || '|' || nomor_rekam_medis FROM pasien "
                                + "WHERE id_pasien = " + terdaftar.getIdPasien()));
        assertEquals(1, angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir
                + " AND deskripsi = 'Pembaruan data pasien " + terdaftar.getNomorRekamMedis() + "'"));
    }

    @Test
    @DisplayName("INT-TC08 IK-04 gagal: NIK ganda dan NIK tidak valid ditolak tanpa baris atau audit baru")
    void ik04_nikDitolak() throws SQLException {
        int pasienAwal = angka("SELECT count(*) FROM pasien");
        int rekamAwal = angka("SELECT count(*) FROM rekam_medis");

        IllegalArgumentException ganda = assertThrows(IllegalArgumentException.class,
                () -> pasien.daftarkanPasien(dataPasien(0, "9999010101680001", "NIK Seed")));
        IllegalArgumentException tidakValid = assertThrows(IllegalArgumentException.class,
                () -> pasien.daftarkanPasien(dataPasien(0, "999901010168000", "NIK Pendek")));

        assertEquals("NIK sudah terdaftar", ganda.getMessage());
        assertEquals("NIK tidak valid", tidakValid.getMessage());
        assertEquals(pasienAwal, angka("SELECT count(*) FROM pasien"));
        assertEquals(rekamAwal, angka("SELECT count(*) FROM rekam_medis"));
        assertEquals(0, angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir));
    }

    @Test
    @DisplayName("INT-TC09 IK-05 normal: kunjungan dan nomor antrean tersimpan, poli maju, audit tercatat")
    void ik05_bukaKunjungan() throws SQLException {
        PasienEntity terdaftar = pasien.daftarkanPasien(dataPasien(0, nikUnik(), "Pasien Antrean"));
        pasienUji.add(terdaftar.getIdPasien());
        int idPoli = poliBaru();

        KunjunganEntity dibuka = kunjungan.bukaKunjungan(terdaftar.getIdPasien(), idPoli);
        kunjunganUji.add(dibuka.getIdKunjungan());

        assertEquals("menunggu skrining|1|" + terdaftar.getIdPasien() + "|" + idPoli, teks(
                "SELECT status || '|' || nomor_antrean || '|' || id_pasien || '|' || id_poli FROM kunjungan "
                        + "WHERE id_kunjungan = " + dibuka.getIdKunjungan()));
        assertEquals(1, angka("SELECT urutan_terakhir FROM poli WHERE id_poli = " + idPoli));
        assertEquals(1, angka("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                + "WHERE a.jenis = 'skrining' AND e.id_entri = " + dibuka.getIdKunjungan()));
        assertEquals("1|Kunjungan baru, antrean 1", teks("SELECT id_pengguna || '|' || deskripsi FROM audit "
                + "WHERE id_audit > " + auditTerakhir + " ORDER BY id_audit DESC LIMIT 1"));
        assertEquals(dibuka.getIdKunjungan(), kunjungan.kunjunganAktif(terdaftar.getIdPasien()).getIdKunjungan());
    }

    @Test
    @DisplayName("INT-TC10 IK-05 gagal: pasien tanpa rekam medis membatalkan kunjungan dan nomor antrean")
    void ik05_tanpaRekamMedis() throws SQLException {
        int idPasien = pasienLangsung(false);
        int idPoli = poliBaru();
        String urutanAwal = teks("SELECT urutan_terakhir || '|' || tanggal_urutan FROM poli WHERE id_poli = " + idPoli);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> kunjungan.bukaKunjungan(idPasien, idPoli));

        assertEquals("Pasien belum memiliki rekam medis", e.getMessage());
        assertEquals(0, angka("SELECT count(*) FROM kunjungan WHERE id_pasien = " + idPasien));
        assertEquals(urutanAwal, teks("SELECT urutan_terakhir || '|' || tanggal_urutan FROM poli WHERE id_poli = "
                + idPoli));
        assertEquals(0, angka("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                + "WHERE a.jenis = 'skrining' AND e.id_entri IN "
                + "(SELECT id_kunjungan FROM kunjungan WHERE id_pasien = " + idPasien + ")"));
        assertEquals(0, angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir));
    }

    @Test
    @DisplayName("INT-TC33 IK-17 normal: dua loket bersamaan mendapat nomor berurutan tanpa kehilangan data")
    void ik17_duaLoketBersamaan() throws Exception {
        int idPasienA = pasienLangsung(true);
        int idPasienB = pasienLangsung(true);
        int idPoli = poliBaru();
        CountDownLatch mulai = new CountDownLatch(1);
        ExecutorService pelaksana = Executors.newFixedThreadPool(2);

        List<Future<KunjunganEntity>> hasil = new ArrayList<>();
        for (int idPasien : List.of(idPasienA, idPasienB)) {
            hasil.add(pelaksana.submit(() -> {
                mulai.await();
                return kunjungan.bukaKunjungan(idPasien, idPoli);
            }));
        }
        mulai.countDown();

        Set<Integer> nomor = new HashSet<>();
        for (Future<KunjunganEntity> f : hasil) {
            KunjunganEntity k = f.get();
            kunjunganUji.add(k.getIdKunjungan());
            nomor.add(k.getNomorAntrean());
        }
        pelaksana.shutdown();

        assertEquals(Set.of(1, 2), nomor);
        assertEquals(2, angka("SELECT urutan_terakhir FROM poli WHERE id_poli = " + idPoli));
        assertEquals(2, angka("SELECT count(*) FROM kunjungan WHERE id_poli = " + idPoli));
        assertEquals(2, angka("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                + "WHERE a.jenis = 'skrining' AND e.id_entri IN "
                + "(SELECT id_kunjungan FROM kunjungan WHERE id_poli = " + idPoli + ")"));
    }

    @Test
    @DisplayName("INT-TC34 IK-17 gagal: galat basis data di tengah transaksi mengembalikan nomor poli")
    void ik17_rollbackSaatGalat() throws SQLException {
        int idPasien = pasienLangsung(true);
        int idPoli = poliBaru();
        String urutanAwal = teks("SELECT urutan_terakhir || '|' || tanggal_urutan FROM poli WHERE id_poli = " + idPoli);
        String penggunaApp = penggunaAplikasi();

        try (Statement st = koneksi.createStatement()) {
            st.execute("REVOKE INSERT ON kunjungan FROM " + penggunaApp);
            try {
                IllegalStateException e = assertThrows(IllegalStateException.class,
                        () -> kunjungan.bukaKunjungan(idPasien, idPoli));
                assertEquals("Basis data tidak dapat dihubungi", e.getMessage());
            } finally {
                st.execute("GRANT INSERT ON kunjungan TO " + penggunaApp);
            }
        }

        assertEquals(0, angka("SELECT count(*) FROM kunjungan WHERE id_pasien = " + idPasien));
        assertEquals(urutanAwal, teks("SELECT urutan_terakhir || '|' || tanggal_urutan FROM poli WHERE id_poli = "
                + idPoli));
        assertEquals(0, angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir));
    }

    @Test
    @DisplayName("INT-TC35 IK-18 normal: alur loket sampai dokter memakai data yang ditulis Manager sebelumnya")
    void ik18_alurLoketSampaiDokter() throws SQLException {
        PasienEntity terdaftar = pasien.daftarkanPasien(dataPasien(0, nikUnik(), "Pasien Alur"));
        pasienUji.add(terdaftar.getIdPasien());
        int idPoli = poliBaru();
        KunjunganEntity dibuka = kunjungan.bukaKunjungan(terdaftar.getIdPasien(), idPoli);
        kunjunganUji.add(dibuka.getIdKunjungan());

        masuk("Perawat", 2);
        assertTrue(skrining.muatAntrean().stream().anyMatch(k -> k.getIdKunjungan() == dibuka.getIdKunjungan()));
        assertEquals(List.of(), skrining.simpanSkrining(dibuka.getIdKunjungan(), "Demam tiga hari",
                new TandaVitalEntity(120, 80, 60, 165, 36.5, 80, 100)));

        assertEquals("Demam tiga hari|siap diperiksa dokter", teks("SELECT s.keluhan_awal || '|' || k.status "
                + "FROM skrining s JOIN kunjungan k USING (id_kunjungan) WHERE s.id_kunjungan = "
                + dibuka.getIdKunjungan()));
        masuk("Dokter", 3);
        Map<KunjunganEntity, PasienEntity> siap = pemeriksaan.muatKunjunganSiapPeriksa();
        assertTrue(siap.entrySet().stream().anyMatch(e -> e.getKey().getIdKunjungan() == dibuka.getIdKunjungan()
                && e.getValue().getNama().equals("Pasien Alur")));
        assertNotNull(pemeriksaan.susunRingkasan(dibuka.getIdKunjungan()).getSkriningHariIni());
    }

    @Test
    @DisplayName("INT-TC36 IK-18 gagal: perawat tidak dapat melewati kunjungan yang sudah dikunci dokter")
    void ik18_kunjunganSudahDikunci() throws SQLException {
        PasienEntity terdaftar = pasien.daftarkanPasien(dataPasien(0, nikUnik(), "Pasien Terkunci"));
        pasienUji.add(terdaftar.getIdPasien());
        int idPoli = poliBaru();
        KunjunganEntity dibuka = kunjungan.bukaKunjungan(terdaftar.getIdPasien(), idPoli);
        kunjunganUji.add(dibuka.getIdKunjungan());
        int idKunjungan = dibuka.getIdKunjungan();

        masuk("Perawat", 2);
        skrining.simpanSkrining(idKunjungan, "Batuk", new TandaVitalEntity(118, 78, 58, 160, 36.8, 82, 95));
        masuk("Dokter", 3);
        pemeriksaan.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Batuk kering", "Istirahat", false),
                new DiagnosisEntity(0, "J06.9", "Infeksi saluran pernapasan akut"));
        pemeriksaan.kunciRekamMedis(idKunjungan);
        assertEquals("selesai", teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan));

        masuk("Perawat", 2);
        int auditSebelum = angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> skrining.lewatiPasien(idKunjungan));

        assertEquals("Hanya pasien yang sedang diskrining yang dapat dilewati", e.getMessage());
        assertEquals("selesai", teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan));
        assertEquals(1, angka("SELECT count(*) FROM skrining WHERE id_kunjungan = " + idKunjungan));
        assertEquals(auditSebelum, angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir));
    }

    private static void masuk(String peran, int idPengguna) {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), idPengguna, LocalDateTime.now(), true), peran);
    }

    private static PasienEntity dataPasien(int idPasien, String nik, String nama) {
        return new PasienEntity(idPasien, nik, null, nama, LocalDate.of(1990, 1, 1), "Perempuan",
                "Jl. Integrasi No. 1", "081200000000");
    }

    private static String nikUnik() {
        return "9" + String.format("%015d",
                (UUID.randomUUID().getLeastSignificantBits() & Long.MAX_VALUE) % 1_000_000_000_000_000L);
    }

    private int pasienLangsung(boolean denganRekamMedis) throws SQLException {
        int idPasien;
        try (PreparedStatement ps = koneksi.prepareStatement(
                "INSERT INTO pasien (nik, nomor_rekam_medis, nama, tanggal_lahir, jenis_kelamin, alamat) "
                        + "VALUES (?, ?, 'Pasien Uji Loket', DATE '1990-01-01', 'Perempuan', 'Jl. Uji') "
                        + "RETURNING id_pasien")) {
            ps.setString(1, nikUnik());
            ps.setString(2, "RM-9" + String.format("%06d",
                    (UUID.randomUUID().getLeastSignificantBits() & Long.MAX_VALUE) % 900000 + 100000));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                idPasien = rs.getInt(1);
            }
        }
        pasienUji.add(idPasien);
        if (denganRekamMedis) {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO rekam_medis (id_pasien) VALUES (?)")) {
                ps.setInt(1, idPasien);
                ps.executeUpdate();
            }
        }
        return idPasien;
    }

    private int poliBaru() throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "INSERT INTO poli (nama_poli) VALUES (?) RETURNING id_poli")) {
            ps.setString(1, "Poli Uji Loket " + UUID.randomUUID().toString().substring(0, 8));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int idPoli = rs.getInt(1);
                poliUji.add(idPoli);
                return idPoli;
            }
        }
    }

    private String penggunaAplikasi() throws SQLException {
        try (Connection k = KoneksiBasisData.buka();
             PreparedStatement ps = k.prepareStatement("SELECT current_user");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getString(1);
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
