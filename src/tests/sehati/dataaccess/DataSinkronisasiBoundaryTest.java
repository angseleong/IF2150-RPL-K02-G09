package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.BundelFHIREntity;

class DataSinkronisasiBoundaryTest {

    private static final String FRASA = "frasa-sandi-uji";

    @Test
    void sqlState_diambilDariPenyebabBatch() {
        BatchUpdateException batch = new BatchUpdateException("batch gagal", null, 0, new int[0], null);
        batch.setNextException(new SQLException("duplikat", "23505"));
        assertEquals("23505", DataSinkronisasiBoundary.sqlState(batch));
    }

    @Test
    void galat_menerjemahkanSqlStateSesuaiAturan3() {
        assertInstanceOf(IllegalArgumentException.class,
                DataSinkronisasiBoundary.galat("Uji", new SQLException("unik", "23505")));
        assertInstanceOf(IllegalArgumentException.class,
                DataSinkronisasiBoundary.galat("Uji", new SQLException("cek", "23514")));
        assertInstanceOf(IllegalArgumentException.class,
                DataSinkronisasiBoundary.galat("Uji", new SQLException("json", "22P02")));
        assertInstanceOf(IllegalArgumentException.class,
                DataSinkronisasiBoundary.galat("Uji", new SQLException("fk", "23503")));
        RuntimeException putus = DataSinkronisasiBoundary.galat("Uji", new SQLException("putus", "08006"));
        assertInstanceOf(IllegalStateException.class, putus);
        assertEquals("Basis data tidak dapat dihubungi", putus.getMessage());
    }

    @Test
    void galat_meneruskanPenolakanApaAdanya() {
        IllegalArgumentException asli = new IllegalArgumentException("Bundel tidak ditemukan: 5");
        assertSame(asli, DataSinkronisasiBoundary.galat("Uji", asli));
    }

    @Test
    void simpanBundel_menyisipkanBundelTertundaDanMengisiId() {
        JdbcPalsu jdbc = new JdbcPalsu().hasil("INSERT INTO bundel_fhir", List.of(JdbcPalsu.baris("id_bundel", 7)));
        BundelFHIREntity b = new BundelFHIREntity(0, 21, "{\"resourceType\":\"Bundle\"}", null, "lama");

        new DataSinkronisasiBoundary(jdbc.koneksi()).simpanBundel(b);

        assertEquals(7, b.getIdBundel());
        assertEquals("tertunda", b.getStatus());
        assertNull(b.getAlasanGagal());
        JdbcPalsu.Jalan jalan = jdbc.dijalankanMemuat("INSERT INTO bundel_fhir").get(0);
        assertEquals(21, jalan.parameter().get(1));
        assertEquals("{\"resourceType\":\"Bundle\"}", jalan.parameter().get(2));
    }

    @Test
    void simpanBundel_kunjunganSudahPunyaBundelDitolak() {
        JdbcPalsu jdbc = new JdbcPalsu().gagal("INSERT INTO bundel_fhir", "23505", "duplikat");
        BundelFHIREntity b = new BundelFHIREntity(0, 21, "{}", null, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DataSinkronisasiBoundary(jdbc.koneksi()).simpanBundel(b));

        assertEquals("Kunjungan sudah memiliki bundel", e.getMessage());
        assertEquals(0, b.getIdBundel());
    }

    @Test
    void simpanBundel_bundelBukanJsonDitolak() {
        JdbcPalsu jdbc = new JdbcPalsu().gagal("INSERT INTO bundel_fhir", "22P02", "invalid input syntax");
        BundelFHIREntity b = new BundelFHIREntity(0, 21, "bukan json", null, null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DataSinkronisasiBoundary(jdbc.koneksi()).simpanBundel(b));

        assertEquals("Bundel FHIR bukan JSON yang utuh", e.getMessage());
    }

    @Test
    void ambilTertunda_mengembalikanDaftarTerurut() {
        JdbcPalsu jdbc = new JdbcPalsu().hasil("SELECT id_bundel, id_kunjungan", List.of(
                JdbcPalsu.baris("id_bundel", 1, "id_kunjungan", 20, "isi_bundel", "{\"a\":1}", "status", "gagal kirim",
                        "alasan_gagal", "putus"),
                JdbcPalsu.baris("id_bundel", 2, "id_kunjungan", 21, "isi_bundel", "{\"a\":2}", "status", "tertunda",
                        "alasan_gagal", null)));

        List<BundelFHIREntity> hasil = new DataSinkronisasiBoundary(jdbc.koneksi()).ambilTertunda();

        assertEquals(2, hasil.size());
        assertEquals(1, hasil.get(0).getIdBundel());
        assertEquals("gagal kirim", hasil.get(0).getStatus());
        assertEquals("putus", hasil.get(0).getAlasanGagal());
        assertEquals(2, hasil.get(1).getIdBundel());
        assertEquals("tertunda", hasil.get(1).getStatus());
        assertNull(hasil.get(1).getAlasanGagal());
    }

    @Test
    void ambilTerkirimTerakhir_mengirimBatasDanMembacaBundel() {
        JdbcPalsu jdbc = new JdbcPalsu().hasil("WHERE status = 'terkirim'", List.of(
                JdbcPalsu.baris("id_bundel", 9, "id_kunjungan", 30, "isi_bundel", "{}", "status", "terkirim",
                        "alasan_gagal", null)));

        List<BundelFHIREntity> hasil = new DataSinkronisasiBoundary(jdbc.koneksi()).ambilTerkirimTerakhir(10);

        assertEquals(1, hasil.size());
        assertEquals("terkirim", hasil.get(0).getStatus());
        assertEquals(10, jdbc.dijalankanMemuat("WHERE status = 'terkirim'").get(0).parameter().get(1));
    }

    @Test
    void ambilTertunda_galatKoneksiMenjadiIllegalStateException() {
        JdbcPalsu jdbc = new JdbcPalsu().gagal("SELECT id_bundel", "08006", "putus");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> new DataSinkronisasiBoundary(jdbc.koneksi()).ambilTertunda());

        assertEquals("Basis data tidak dapat dihubungi", e.getMessage());
    }

    @Test
    void perbaruiBundel_menulisStatusDanAlasan() {
        JdbcPalsu jdbc = new JdbcPalsu();
        BundelFHIREntity b = new BundelFHIREntity(5, 21, "{}", "terkirim", null);

        new DataSinkronisasiBoundary(jdbc.koneksi()).perbaruiBundel(b);

        JdbcPalsu.Jalan jalan = jdbc.dijalankanMemuat("UPDATE bundel_fhir").get(0);
        assertEquals("terkirim", jalan.parameter().get(1));
        assertNull(jalan.parameter().get(2));
        assertEquals(5, jalan.parameter().get(3));
    }

    @Test
    void perbaruiBundel_bundelTidakDitemukanDitolak() {
        JdbcPalsu jdbc = new JdbcPalsu().terubah("UPDATE bundel_fhir", 0);
        BundelFHIREntity b = new BundelFHIREntity(5, 21, "{}", "terkirim", null);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new DataSinkronisasiBoundary(jdbc.koneksi()).perbaruiBundel(b));

        assertEquals("Bundel tidak ditemukan: 5", e.getMessage());
    }

    @Test
    void tulisArsipTerenkripsi_menulisBerkasTerEnkripsiDanDapatDibukaKembali() throws Exception {
        Path lokasi = Files.createTempDirectory("sehati-arsip");
        try {
            List<BundelFHIREntity> daftar = List.of(
                    new BundelFHIREntity(1, 20, "{\"resourceType\":\"Bundle\",\"id\":\"a\"}", "tertunda", null),
                    new BundelFHIREntity(2, 21, "{\"resourceType\":\"Bundle\",\"id\":\"b\"}", "gagal kirim", "putus"));

            String berkas = DataSinkronisasiBoundary.tulisArsipTerenkripsi(daftar, lokasi.toString(), FRASA);

            Path p = Path.of(berkas);
            assertTrue(Files.isRegularFile(p));
            assertTrue(p.getFileName().toString().startsWith("sehati-bundel_"));
            assertTrue(p.getFileName().toString().endsWith(".enc"));

            String isiBuka = bukaArsip(p, FRASA);
            assertEquals("[{\"resourceType\":\"Bundle\",\"id\":\"a\"},"
                    + "{\"resourceType\":\"Bundle\",\"id\":\"b\"}]", isiBuka);
        } finally {
            hapus(lokasi);
        }
    }

    @Test
    void tulisArsipTerenkripsi_daftarKosongMenjadiLarikKosong() throws Exception {
        Path lokasi = Files.createTempDirectory("sehati-arsip");
        try {
            String berkas = DataSinkronisasiBoundary.tulisArsipTerenkripsi(List.of(), lokasi.toString(), FRASA);
            assertEquals("[]", bukaArsip(Path.of(berkas), FRASA));
        } finally {
            hapus(lokasi);
        }
    }

    @Test
    void bundelFhirContoh_berisiBundelTransaction() throws IOException {
        String json = bacaContoh();
        assertTrue(json.contains("\"resourceType\": \"Bundle\""));
        assertTrue(json.contains("\"type\": \"transaction\""));
        assertTrue(json.contains("\"resourceType\": \"Patient\""));
        assertTrue(json.contains("\"resourceType\": \"Encounter\""));
        assertTrue(json.contains("\"resourceType\": \"MedicationRequest\""));
    }

    // Membuka arsip: garam (16) + IV (12) + teks sandi AES-GCM.
    private static String bukaArsip(Path p, String frasa) throws Exception {
        byte[] utuh = Files.readAllBytes(p);
        byte[] garam = java.util.Arrays.copyOfRange(utuh, 0, 16);
        byte[] iv = java.util.Arrays.copyOfRange(utuh, 16, 28);
        byte[] sandi = java.util.Arrays.copyOfRange(utuh, 28, utuh.length);

        SecretKeyFactory pabrik = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spek = new PBEKeySpec(frasa.toCharArray(), garam, 600_000, 256);
        SecretKey kunci = new SecretKeySpec(pabrik.generateSecret(spek).getEncoded(), "AES");
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, kunci, new GCMParameterSpec(128, iv));
        return new String(c.doFinal(sandi), StandardCharsets.UTF_8);
    }

    private static String bacaContoh() throws IOException {
        for (String jalur : List.of("tests/sehati/dataaccess/bundel-fhir-contoh.json",
                "src/tests/sehati/dataaccess/bundel-fhir-contoh.json")) {
            Path p = Path.of(jalur);
            if (Files.isRegularFile(p)) {
                return Files.readString(p, StandardCharsets.UTF_8);
            }
        }
        throw new IllegalStateException("bundel-fhir-contoh.json tidak ditemukan");
    }

    private static void hapus(Path p) throws IOException {
        try (var aliran = Files.walk(p)) {
            aliran.sorted(java.util.Comparator.reverseOrder()).forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Uji terhadap basis data berisi src/db/schema.sql dan seed.sql. Aktif bila DB_UJI_URL diisi. Setiap uji
     * di-rollback.
     */
    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private DataSinkronisasiBoundary data;

        @BeforeEach
        void buka() throws SQLException {
            koneksi = bukaKoneksi();
            koneksi.setAutoCommit(false);
            data = new DataSinkronisasiBoundary(koneksi);
        }

        @AfterEach
        void tutup() throws SQLException {
            koneksi.rollback();
            koneksi.close();
        }

        @Test
        void ambilTertunda_mengambilTertundaDanGagalKirimTerurut() {
            List<BundelFHIREntity> daftar = data.ambilTertunda();
            assertFalse(daftar.isEmpty());
            int idSebelum = -1;
            for (BundelFHIREntity b : daftar) {
                assertTrue(List.of("tertunda", "gagal kirim").contains(b.getStatus()));
                assertTrue(b.getIdBundel() > idSebelum);
                idSebelum = b.getIdBundel();
                assertNotNull(b.getIsiBundel());
            }
        }

        @Test
        void simpanLaluPerbarui_bundelKeluarDariAntrean() throws SQLException {
            int idKunjungan = idKunjunganTanpaBundel();
            BundelFHIREntity b = new BundelFHIREntity(0, idKunjungan, "{\"resourceType\":\"Bundle\"}", null, null);

            data.simpanBundel(b);
            assertTrue(b.getIdBundel() > 0);
            assertEquals("tertunda", b.getStatus());

            assertTrue(data.ambilTertunda().stream().anyMatch(x -> x.getIdBundel() == b.getIdBundel()));

            b.setStatus("terkirim");
            data.perbaruiBundel(b);

            assertTrue(data.ambilTertunda().stream().noneMatch(x -> x.getIdBundel() == b.getIdBundel()));
        }

        @Test
        void simpanBundel_menolakKunjunganYangSudahPunyaBundel() throws SQLException {
            int idKunjungan = idKunjunganBermandul();
            BundelFHIREntity b = new BundelFHIREntity(0, idKunjungan, "{}", null, null);

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> data.simpanBundel(b));

            assertEquals("Kunjungan sudah memiliki bundel", e.getMessage());
        }

        private int idKunjunganTanpaBundel() throws SQLException {
            return satuNilai("SELECT k.id_kunjungan FROM kunjungan k "
                    + "LEFT JOIN bundel_fhir b ON b.id_kunjungan = k.id_kunjungan "
                    + "WHERE b.id_kunjungan IS NULL LIMIT 1");
        }

        private int idKunjunganBermandul() throws SQLException {
            return satuNilai("SELECT id_kunjungan FROM bundel_fhir LIMIT 1");
        }

        private int satuNilai(String kueri) throws SQLException {
            try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(kueri)) {
                rs.next();
                return rs.getInt(1);
            }
        }

        private Connection bukaKoneksi() throws SQLException {
            return DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        }
    }
}
