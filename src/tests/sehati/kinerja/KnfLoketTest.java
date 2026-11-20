package sehati.kinerja;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

/**
 * Pengujian KNF01, KNF10, dan KNF11 modul loket (HIUPL Subbab 4.5). KNF01 dan KNF11 memerlukan basis data uji berisi
 * 50.000 pasien ({@code src/db/seed_besar.sql}); tanpa itu test dilewati agar tetap dapat dijalankan di CI.
 */
@DisplayName("KNF loket: waktu tanggap pencarian, portabilitas, dan konkurensi nomor antrean")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class KnfLoketTest {

    private final PasienManager pasien = new PasienManager();
    private final KunjunganManager kunjungan = new KunjunganManager();

    private Connection koneksi;
    private int auditTerakhir;
    private final List<Integer> pasienUji = new ArrayList<>();
    private final List<Integer> kunjunganUji = new ArrayList<>();
    private final List<Integer> poliUji = new ArrayList<>();

    @BeforeEach
    void siapkan() throws SQLException {
        String url = System.getenv("DB_UJI_URL");
        assumeTrue(url != null && !url.isBlank(), "DB_UJI_URL wajib diisi");
        koneksi = DriverManager.getConnection(url);
        auditTerakhir = angka("SELECT coalesce(max(id_audit), 0) FROM audit");
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Petugas Administrasi");
    }

    @AfterEach
    void pulihkan() throws SQLException {
        SesiAktif.lepas();
        if (koneksi == null) {
            return;
        }
        try (Statement st = koneksi.createStatement()) {
            for (int idKunjungan : kunjunganUji) {
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
    @DisplayName("KNF10-TC01 lingkungan 64-bit Windows/Linux dengan RAM dan disk yang memadai")
    void knf10_lingkungan() {
        assertEquals("64", System.getProperty("sun.arch.data.model"), "JVM harus 64-bit");
        String os = System.getProperty("os.name");
        assertTrue(os.contains("Windows") || os.contains("Linux"), "OS harus Windows atau Linux: " + os);
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        long totalMemori = ((com.sun.management.OperatingSystemMXBean) osBean).getTotalMemorySize();
        long diskBebas = new File(".").getUsableSpace();
        assertTrue(totalMemori >= 4L * 1024 * 1024 * 1024, "RAM minimal 4 GB, aktual " + totalMemori);
        assertTrue(diskBebas >= 2L * 1024 * 1024 * 1024, "Disk bebas minimal 2 GB, aktual " + diskBebas);
        System.out.printf("[KNF10] %s %s, JVM %s, RAM %.1f GB, disk bebas %.1f GB%n",
                os, System.getProperty("os.arch"), System.getProperty("java.version"),
                totalMemori / 1024.0 / 1024 / 1024, diskBebas / 1024.0 / 1024 / 1024);
    }

    @Test
    @DisplayName("KNF01-TC01 pencarian tiga karakter pada 50.000 pasien selesai di bawah 2 detik")
    void knf01_waktuTanggapPencarian() throws SQLException {
        int jumlah = angka("SELECT count(*) FROM pasien");
        assumeTrue(jumlah >= 50000, "butuh seed_besar.sql (50.000 pasien), aktual " + jumlah);

        pasien.cariPasien("Pasien Uji");
        long maksimum = 0;
        long total = 0;
        int ulangan = 10;
        for (int i = 0; i < ulangan; i++) {
            long mulai = System.nanoTime();
            int hasil = pasien.cariPasien("Pasien Uji").size();
            long milidetik = (System.nanoTime() - mulai) / 1_000_000;
            maksimum = Math.max(maksimum, milidetik);
            total += milidetik;
            assertTrue(hasil > 0, "kata kunci harus menemukan pasien");
        }
        System.out.printf("[KNF01] %d ulangan, rata-rata %d ms, maksimum %d ms%n", ulangan, total / ulangan, maksimum);
        assertTrue(maksimum < 2000, "waktu tanggap maksimum " + maksimum + " ms melewati 2 detik");
    }

    @Test
    @DisplayName("KNF11-TC01 lima loket bersamaan menerima nomor antrean 1 sampai 5 tanpa duplikat")
    void knf11_limaLoketBersamaan() throws Exception {
        int idPoli = poliBaru();
        List<Integer> idPasien = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            idPasien.add(pasienLangsung());
        }
        CountDownLatch mulai = new CountDownLatch(1);
        ExecutorService pelaksana = Executors.newFixedThreadPool(5);
        List<Future<KunjunganEntity>> hasil = new ArrayList<>();
        for (int id : idPasien) {
            hasil.add(pelaksana.submit(() -> {
                mulai.await();
                return kunjungan.bukaKunjungan(id, idPoli);
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

        assertEquals(Set.of(1, 2, 3, 4, 5), nomor);
        assertEquals(5, angka("SELECT urutan_terakhir FROM poli WHERE id_poli = " + idPoli));
        assertEquals(5, angka("SELECT count(*) FROM kunjungan WHERE id_poli = " + idPoli));
        System.out.printf("[KNF11] 5 loket, nomor %s, urutan_terakhir 5%n", nomor);
    }

    private int pasienLangsung() throws SQLException {
        int idPasien;
        try (PreparedStatement ps = koneksi.prepareStatement(
                "INSERT INTO pasien (nik, nomor_rekam_medis, nama, tanggal_lahir, jenis_kelamin, alamat) "
                        + "VALUES (?, ?, 'Pasien KNF', DATE '1990-01-01', 'Perempuan', 'Jl. Uji') "
                        + "RETURNING id_pasien")) {
            ps.setString(1, "9" + String.format("%015d",
                    (UUID.randomUUID().getLeastSignificantBits() & Long.MAX_VALUE) % 1_000_000_000_000_000L));
            ps.setString(2, "RM-7" + String.format("%06d",
                    (UUID.randomUUID().getLeastSignificantBits() & Long.MAX_VALUE) % 900000 + 100000));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                idPasien = rs.getInt(1);
            }
        }
        pasienUji.add(idPasien);
        try (PreparedStatement ps = koneksi.prepareStatement(
                "INSERT INTO rekam_medis (id_pasien) VALUES (?)")) {
            ps.setInt(1, idPasien);
            ps.executeUpdate();
        }
        return idPasien;
    }

    private int poliBaru() throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(
                "INSERT INTO poli (nama_poli) VALUES (?) RETURNING id_poli")) {
            ps.setString(1, "Poli KNF " + UUID.randomUUID().toString().substring(0, 8));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int idPoli = rs.getInt(1);
                poliUji.add(idPoli);
                return idPoli;
            }
        }
    }

    private int angka(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
