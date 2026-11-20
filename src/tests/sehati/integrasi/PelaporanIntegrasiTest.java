package sehati.integrasi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import sehati.business.control.LaporanManager;
import sehati.business.control.SinkronisasiManager;
import sehati.business.entity.BundelFHIREntity;
import sehati.business.entity.LaporanEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;
import sehati.dataaccess.ResponsKirim;
import sehati.dataaccess.SATUSEHATGateway;

/**
 * Pengujian integrasi modul pelaporan dan sinkronisasi (HIUPL Subbab 4.3, INT-TC27 sampai INT-TC32). Manager
 * dijalankan dengan DataBoundary dan basis data PostgreSQL yang nyata, tanpa mock maupun JdbcPalsu. Hasilnya dibaca
 * ulang langsung dari tabel lewat koneksi terpisah, sehingga yang diperiksa adalah data yang benar-benar sampai di
 * basis data atau sistem berkas.
 */
@DisplayName("Integrasi pelaporan dan sinkronisasi (IK-14, IK-15, IK-16)")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class PelaporanIntegrasiTest {

    private final LaporanManager laporanManager = new LaporanManager();
    private final SinkronisasiManager sinkronisasi = new SinkronisasiManager();

    private Connection koneksi;
    private List<BundelSnapshot> bundelAwal;
    private int laporanTerakhir;

    @TempDir
    Path direktori;

    @BeforeEach
    void siapkan() throws SQLException {
        koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        // Simpan kondisi antrean bundel seed agar kirimSemua (INT-TC31) dapat dikembalikan setelah diuji.
        bundelAwal = new ArrayList<>();
        try (Statement st = koneksi.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT id_bundel, status, alasan_gagal FROM bundel_fhir ORDER BY id_bundel")) {
            while (rs.next()) {
                bundelAwal.add(new BundelSnapshot(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        }
        laporanTerakhir = angka("SELECT coalesce(max(id_laporan), 0) FROM laporan");
    }

    @AfterEach
    void pulihkan() throws SQLException {
        SesiAktif.lepas();
        try (PreparedStatement ps = koneksi.prepareStatement(
                "UPDATE bundel_fhir SET status = ?, alasan_gagal = ? WHERE id_bundel = ?")) {
            for (BundelSnapshot b : bundelAwal) {
                ps.setString(1, b.status());
                ps.setString(2, b.alasan());
                ps.setInt(3, b.id());
                ps.executeUpdate();
            }
        }
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("DELETE FROM laporan WHERE id_laporan > " + laporanTerakhir);
        }
        koneksi.close();
    }

    @Test
    @DisplayName("INT-TC27 IK-14 normal: rekapitulasi dibaca dari basis data dan diekspor ke berkas CSV yang tersimpan")
    void ik14_rekapitulasiDanEksporCsv() throws SQLException, IOException {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true),
                "Petugas Administrasi");
        LocalDate mulai = LocalDate.now().minusDays(30);
        LocalDate akhir = LocalDate.now();

        LaporanEntity laporan = laporanManager.hitungRekapitulasi(mulai, akhir, null);

        int totalHarapan = angka("SELECT count(*) FROM kunjungan WHERE tanggal BETWEEN CURRENT_DATE - 30 "
                + "AND CURRENT_DATE");
        assertEquals(totalHarapan, totalRingkasan(laporan));
        assertFalse(laporan.kosong());

        String lokasi = laporanManager.ekspor(laporan, "CSV", direktori.toString());

        assertTrue(Files.exists(Path.of(lokasi)));
        assertTrue(Files.readString(Path.of(lokasi)).startsWith("bagian;label;jumlah"));
        assertEquals("CSV", teks("SELECT format FROM laporan WHERE id_laporan = " + laporan.getIdLaporan()));
        assertEquals(mulai.toString(),
                teks("SELECT tanggal_mulai::text FROM laporan WHERE id_laporan = " + laporan.getIdLaporan()));
    }

    @Test
    @DisplayName("INT-TC28 IK-14 gagal: rentang tanpa kunjungan menghasilkan laporan kosong dan ekspor ditolak")
    void ik14_eksporLaporanKosongDitolak() throws SQLException {
        int sebelum = angka("SELECT count(*) FROM laporan");
        LocalDate mulai = LocalDate.now().minusDays(400);
        LocalDate akhir = LocalDate.now().minusDays(350);

        LaporanEntity laporan = laporanManager.hitungRekapitulasi(mulai, akhir, null);

        assertTrue(laporan.kosong());
        assertThrows(IllegalStateException.class, () -> laporanManager.ekspor(laporan, "CSV", direktori.toString()));
        assertEquals(sebelum, angka("SELECT count(*) FROM laporan"));
        assertEquals(0, jumlahBerkas());
    }

    @Test
    @DisplayName("INT-TC29 IK-15 normal: status sinkronisasi membaca bundel tertunda dan riwayat dari basis data")
    void ik15_muatStatusMembacaBundel() throws SQLException {
        int tertunda = angka("SELECT count(*) FROM bundel_fhir WHERE status IN ('tertunda', 'gagal kirim')");
        int terkirim = angka("SELECT count(*) FROM bundel_fhir WHERE status = 'terkirim'");

        List<BundelFHIREntity> daftar = sinkronisasi.muatStatus();

        assertEquals(tertunda, sinkronisasi.getJumlahTertunda());
        // RIWAYAT_TERKIRIM = 10; riwayat terkirim seed (6) lebih sedikit, jadi seluruhnya ikut termuat.
        assertEquals(tertunda + Math.min(terkirim, 10), daftar.size());
        assertTrue(sinkronisasi.isDaring());
    }

    @Test
    @DisplayName("INT-TC30 IK-15 gagal: ekspor bundel tanpa peran Petugas Administrasi ditolak")
    void ik15_eksporTanpaWewenangDitolak() throws SQLException {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 3, LocalDateTime.now(), true), "Dokter");

        IllegalStateException galat = assertThrows(IllegalStateException.class,
                () -> sinkronisasi.eksporBundel(direktori.toString()));

        assertEquals("Peran Dokter tidak berwenang menjalankan tindakan ini", galat.getMessage());
        assertEquals(0, jumlahBerkas());
    }

    @Test
    @DisplayName("INT-TC31 IK-16 normal: bundel tertunda dikirim ke SATUSEHAT simulasi dan berubah menjadi terkirim")
    void ik16_kirimSemuaSimulasi() throws SQLException {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true),
                "Petugas Administrasi");
        int tertundaAwal = angka("SELECT count(*) FROM bundel_fhir WHERE status IN ('tertunda', 'gagal kirim')");
        int terkirimAwal = (int) bundelAwal.stream().filter(b -> "terkirim".equals(b.status())).count();

        List<BundelFHIREntity> hasil = sinkronisasi.kirimSemua();

        assertEquals(tertundaAwal, hasil.size());
        for (BundelFHIREntity bundel : hasil) {
            assertEquals("terkirim", bundel.getStatus());
            assertTrue(bundel.getAlasanGagal() == null);
        }
        assertEquals(0, angka("SELECT count(*) FROM bundel_fhir WHERE status IN ('tertunda', 'gagal kirim')"));
        assertEquals(terkirimAwal + tertundaAwal,
                angka("SELECT count(*) FROM bundel_fhir WHERE status = 'terkirim'"));
    }

    @Test
    @DisplayName("INT-TC32 IK-16 gagal: SATUSEHAT menolak bundel dan alasan OperationOutcome diteruskan")
    void ik16_satusehatMenolakBundel() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            byte[] balasan = ("{\"resourceType\":\"OperationOutcome\",\"issue\":"
                    + "[{\"diagnostics\":\"Bundle gagal divalidasi\"}]}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(422, balasan.length);
            try (OutputStream keluaran = exchange.getResponseBody()) {
                keluaran.write(balasan);
            }
        });
        server.start();
        try {
            String alamat = "http://127.0.0.1:" + server.getAddress().getPort();
            BundelFHIREntity bundel = new BundelFHIREntity(1, 1, "{\"resourceType\":\"Bundle\"}", "tertunda", null);

            ResponsKirim respons = new SATUSEHATGateway(alamat, "token").kirimBundel(bundel);

            assertFalse(respons.berhasil());
            assertEquals("Bundle gagal divalidasi", respons.alasan());
        } finally {
            server.stop(0);
        }
    }

    private int jumlahBerkas() {
        java.io.File[] berkas = direktori.toFile().listFiles();
        return berkas == null ? 0 : berkas.length;
    }

    private static long totalRingkasan(LaporanEntity laporan) {
        for (String baris : laporan.getRingkasan().split("\\R")) {
            if (baris.startsWith("total;")) {
                return Long.parseLong(baris.split(";", -1)[2].trim());
            }
        }
        throw new AssertionError("Baris total tidak ditemukan: " + laporan.getRingkasan());
    }

    private int angka(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private String teks(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        }
    }

    private record BundelSnapshot(int id, String status, String alasan) {
    }
}
