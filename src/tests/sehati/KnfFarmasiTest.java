package sehati;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.control.ApotekManager;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

/**
 * Pengujian KNF modul farmasi (HIUPL Subbab 4.5): KNF03-TC01 (bundel FHIR tidak pernah parsial bila penyerahan obat
 * gagal di tengah proses) dan KNF13-TC01 (data tetap utuh setelah proses aplikasi dihentikan paksa saat menyimpan).
 * Memakai resep seed 4 (Siti Aminah, kunjungan 5): Metformin 60 dan Glibenklamid 30. Setiap percobaan mencetak satu
 * baris hasil ukur, dan keadaan basis data dipulihkan {@link CadanganFarmasi} setelah setiap percobaan.
 */
@DisplayName("KNF modul farmasi")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class KnfFarmasiTest {

    private static final int PENGULANGAN_KNF03 = 5;
    private static final int PENGULANGAN_KNF13 = 3;

    // Sidik keadaan farmasi: berubah bila ada satu saja baris stok, bets, resep, kunjungan, bundel, antrean, atau
    // audit yang berubah.
    private static final String SIDIK = "SELECT (SELECT string_agg(id_obat || '=' || stok, ',' ORDER BY id_obat) FROM obat)"
            + " || ' / ' || (SELECT string_agg(nomor_bets || '=' || jumlah, ',' ORDER BY nomor_bets) FROM bets_obat)"
            + " || ' / ' || (SELECT string_agg(id_resep || '=' || status, ',' ORDER BY id_resep) FROM resep)"
            + " || ' / ' || (SELECT string_agg(id_kunjungan || '=' || status, ',' ORDER BY id_kunjungan) FROM kunjungan)"
            + " || ' / bundel=' || (SELECT count(*) FROM bundel_fhir)"
            + " || ' / antrean=' || (SELECT coalesce(string_agg(e.id_entri::text, ',' ORDER BY e.urutan), '') "
            + "FROM antrean_entri e JOIN antrean a USING (id_antrean) WHERE a.jenis = 'apotek')"
            + " || ' / penerimaan=' || (SELECT count(*) FROM penerimaan_obat)"
            + " || ' / audit=' || (SELECT count(*) FROM audit)";

    private Connection koneksi;
    private CadanganFarmasi cadangan;
    private int idResep;
    private int idKunjungan;

    @BeforeEach
    void siapkan() throws SQLException {
        koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        cadangan = CadanganFarmasi.ambil(koneksi);
        idResep = angka("SELECT min(id_resep) FROM resep WHERE status = 'menunggu'");
        idKunjungan = angka("SELECT id_kunjungan FROM resep WHERE id_resep = " + idResep);
    }

    @AfterEach
    void pulihkan() throws SQLException {
        SesiAktif.lepas();
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("GRANT SELECT ON pasien TO sehati_app");
        }
        cadangan.pulihkan();
        koneksi.close();
    }

    @Test
    @DisplayName("KNF03-TC01 penyerahan yang gagal di tiga titik proses tidak meninggalkan bundel parsial (15 "
            + "percobaan)")
    void knf03_bundelTidakParsial() throws SQLException {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 4, LocalDateTime.now(), true), "Petugas Farmasi");
        String[] titik = {"potong stok obat kedua", "menyusun bundel FHIR", "menyimpan bundel FHIR"};
        int berubah = 0;
        for (int ulang = 1; ulang <= PENGULANGAN_KNF03; ulang++) {
            for (int t = 0; t < titik.length; t++) {
                siapkanKegagalan(t);
                String sebelum = teks(SIDIK);
                long mulai = System.nanoTime();
                RuntimeException e = assertThrows(RuntimeException.class,
                        () -> new ApotekManager().serahkanObat(idResep));
                long ms = (System.nanoTime() - mulai) / 1_000_000;
                String sesudah = teks(SIDIK);
                boolean sama = sebelum.equals(sesudah);
                berubah += sama ? 0 : 1;
                int bundelKunjungan = angka("SELECT count(*) FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan
                        + " AND isi_bundel <> '{}'::jsonb");
                System.out.printf("KNF03 ulang %d, gagal saat %-22s | %-45s | bundel kunjungan %d | keadaan %s | "
                        + "%d ms%n", ulang, titik[t], e.getMessage(), bundelKunjungan, sama ? "sama" : "BERUBAH", ms);
                assertEquals(0, bundelKunjungan);
                assertEquals(sebelum, sesudah);
                cadangan.pulihkan();
                try (Statement st = koneksi.createStatement()) {
                    st.executeUpdate("GRANT SELECT ON pasien TO sehati_app");
                }
            }
        }
        // Kontrol: tanpa gangguan, bundel tersusun utuh dalam transaksi yang sama.
        new ApotekManager().serahkanObat(idResep);
        String jenis = teks("SELECT string_agg(e -> 'resource' ->> 'resourceType', ',') FROM bundel_fhir b, "
                + "jsonb_array_elements(b.isi_bundel -> 'entry') e WHERE b.id_kunjungan = " + idKunjungan);
        System.out.println("KNF03 kontrol tanpa gangguan: bundel berisi " + jenis);
        System.out.printf("KNF03 ringkasan: %d percobaan gagal, %d meninggalkan perubahan atau bundel parsial%n",
                PENGULANGAN_KNF03 * titik.length, berubah);
        assertEquals(0, berubah);
        assertTrue(jenis.startsWith("Patient,Encounter") && jenis.endsWith("MedicationRequest,MedicationRequest"));
    }

    // Titik 0: komputer lain memakai 10 Glibenklamid sehingga obat kedua gagal dipotong setelah Metformin dipotong.
    // Titik 1: hak baca tabel pasien dicabut sehingga penyusunan bundel gagal setelah stok, resep, dan kunjungan
    // diubah. Titik 2: bundel lama untuk kunjungan yang sama melanggar UNIQUE saat bundel baru disimpan.
    private void siapkanKegagalan(int titik) throws SQLException {
        try (Statement st = koneksi.createStatement()) {
            switch (titik) {
                case 0 -> {
                    st.executeUpdate("UPDATE obat SET stok = stok - 10 WHERE id_obat = 5");
                    st.executeUpdate("UPDATE bets_obat SET jumlah = jumlah - 10 WHERE nomor_bets = 'GLB-A01'");
                }
                case 1 -> st.executeUpdate("REVOKE SELECT ON pasien FROM sehati_app");
                default -> st.executeUpdate("INSERT INTO bundel_fhir (id_kunjungan, isi_bundel) VALUES ("
                        + idKunjungan + ", '{}'::jsonb)");
            }
        }
    }

    @Test
    @DisplayName("KNF13-TC01 proses aplikasi dimatikan paksa (kill -9) saat menyimpan, data tetap utuh setelah "
            + "dijalankan ulang (6 percobaan)")
    void knf13_matiMendadak() throws Exception {
        for (int ulang = 1; ulang <= PENGULANGAN_KNF13; ulang++) {
            matiSaatMenyerahkan(ulang);
            cadangan.pulihkan();
            matiSaatMenerima(ulang);
            cadangan.pulihkan();
        }
    }

    // Proses anak mencatat penerimaan (selesai), lalu dimatikan saat penyerahan obat sudah memotong stok, mengubah
    // resep, dan menutup kunjungan, tetapi tertahan menyimpan bundel karena koneksi lain memegang bundel kunjungan itu.
    private void matiSaatMenyerahkan(int ulang) throws Exception {
        String faktur = "KNF13-SERAH-" + ulang;
        String bets = "KAP-KNF13-S" + ulang;
        int stokKaptopril = angka("SELECT stok FROM obat WHERE id_obat = 6");
        try (Connection penahan = DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
            penahan.setAutoCommit(false);
            try (Statement st = penahan.createStatement()) {
                st.executeUpdate("INSERT INTO bundel_fhir (id_kunjungan, isi_bundel) VALUES (" + idKunjungan
                        + ", '{}'::jsonb)");
            }
            Proses anak = Proses.jalankan("serah", String.valueOf(idResep), faktur, bets);
            anak.tunggu("MULAI_SERAH");
            String sebelumMati = teks(SIDIK);
            int pidBackend = tungguTertahan();
            String tertahanDi = teks("SELECT query FROM pg_stat_activity WHERE pid = " + pidBackend);
            String ditulis = tabelDitulis(pidBackend);
            anak.matikanPaksa();
            penahan.rollback();
            tungguBackendBerakhir(pidBackend);

            String sesudah = teks(SIDIK);
            boolean sama = sebelumMati.equals(sesudah);
            System.out.printf("KNF13 ulang %d, mati saat menyerahkan obat | sudah menulis: %s | tertahan di: %s | "
                    + "proses keluar %d | keadaan %s%n", ulang, ditulis, ringkas(tertahanDi), anak.kodeKeluar(),
                    sama ? "sama dengan sebelum mati" : "BERUBAH");
            assertEquals(sebelumMati, sesudah);
            assertEquals("menunggu", teks("SELECT status FROM resep WHERE id_resep = " + idResep));
            assertEquals(0, angka("SELECT count(*) FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
            // Penerimaan yang selesai sebelum proses dimatikan tetap tersimpan utuh.
            assertEquals(stokKaptopril + ProsesFarmasiKnf13.JUMLAH_TERIMA, angka("SELECT stok FROM obat "
                    + "WHERE id_obat = 6"));
            assertEquals(1, angka("SELECT count(*) FROM penerimaan_obat WHERE nomor_faktur = '" + faktur + "'"));
        }

        // Dijalankan ulang: penyerahan yang sama berhasil sekali dan saldo tetap sama dengan jumlah bets.
        Proses ulangi = Proses.jalankan("serah", String.valueOf(idResep), faktur + "-ULANG", bets + "U");
        assertEquals(0, ulangi.tungguSelesai());
        String hasil = teks("SELECT (SELECT status FROM resep WHERE id_resep = " + idResep + ") || '|' || "
                + "(SELECT count(*) FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan + ") || '|' || "
                + "(SELECT bool_and(o.stok = (SELECT coalesce(sum(b.jumlah), 0) FROM bets_obat b "
                + "WHERE b.id_obat = o.id_obat)) FROM obat o)");
        System.out.printf("KNF13 ulang %d, setelah dijalankan ulang | status resep|bundel|saldo = bets: %s%n", ulang,
                hasil);
        assertEquals("selesai dilayani|1|true", hasil);
    }

    // Proses anak dimatikan saat penerimaan sudah menulis faktur, bets, dan stok baru dalam transaksinya, tetapi
    // tertahan pada penulisan terakhir (audit) sebelum COMMIT. Penahannya pemicu sementara pada tabel audit yang
    // menunggu advisory lock milik koneksi test, khusus untuk faktur KNF13. Pemicu dihapus sebelum proses diulang.
    private void matiSaatMenerima(int ulang) throws Exception {
        String faktur = "KNF13-TERIMA-" + ulang;
        String bets = "KAP-KNF13-T" + ulang;
        try (Connection penahan = DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
            try (Statement st = penahan.createStatement()) {
                st.execute("CREATE OR REPLACE FUNCTION uji_knf13_tahan() RETURNS trigger LANGUAGE plpgsql AS $$ "
                        + "BEGIN IF NEW.deskripsi LIKE '%faktur KNF13-TERIMA-%' THEN PERFORM pg_advisory_lock(1313); "
                        + "PERFORM pg_advisory_unlock(1313); END IF; RETURN NEW; END $$");
                st.execute("CREATE TRIGGER uji_knf13_tahan BEFORE INSERT ON audit FOR EACH ROW "
                        + "EXECUTE FUNCTION uji_knf13_tahan()");
                st.execute("SELECT pg_advisory_lock(1313)");
            }
            String sebelumMati = teks(SIDIK);
            Proses anak = Proses.jalankan("terima", faktur, bets);
            int pidBackend;
            String tertahanDi;
            String ditulis;
            try {
                anak.tunggu("MULAI_TERIMA");
                pidBackend = tungguTertahan();
                tertahanDi = teks("SELECT query FROM pg_stat_activity WHERE pid = " + pidBackend);
                ditulis = tabelDitulis(pidBackend);
                anak.matikanPaksa();
            } finally {
                try (Statement st = penahan.createStatement()) {
                    st.execute("SELECT pg_advisory_unlock(1313)");
                    st.execute("DROP TRIGGER uji_knf13_tahan ON audit");
                    st.execute("DROP FUNCTION uji_knf13_tahan()");
                }
            }
            tungguBackendBerakhir(pidBackend);

            String sesudah = teks(SIDIK);
            boolean sama = sebelumMati.equals(sesudah);
            System.out.printf("KNF13 ulang %d, mati saat menerima obat    | sudah menulis: %s | tertahan di: %s | "
                    + "proses keluar %d | keadaan %s%n", ulang, ditulis, ringkas(tertahanDi), anak.kodeKeluar(),
                    sama ? "sama dengan sebelum mati" : "BERUBAH");
            assertEquals(sebelumMati, sesudah);
            assertEquals(0, angka("SELECT count(*) FROM penerimaan_obat WHERE nomor_faktur = '" + faktur + "'"));
        }

        int stokKaptopril = angka("SELECT stok FROM obat WHERE id_obat = 6");
        Proses ulangi = Proses.jalankan("terima", faktur, bets);
        assertEquals(0, ulangi.tungguSelesai());
        String hasil = angka("SELECT count(*) FROM penerimaan_obat WHERE nomor_faktur = '" + faktur + "'") + "|"
                + (angka("SELECT stok FROM obat WHERE id_obat = 6") - stokKaptopril);
        System.out.printf("KNF13 ulang %d, setelah dijalankan ulang | faktur tersimpan|stok bertambah: %s%n", ulang,
                hasil);
        assertEquals("1|" + ProsesFarmasiKnf13.JUMLAH_TERIMA, hasil);
    }

    // Tabel yang sudah ditulis transaksi proses anak tetapi belum di-COMMIT, dari kunci RowExclusiveLock-nya.
    private String tabelDitulis(int pid) throws SQLException {
        return teks("SELECT string_agg(c.relname, ',' ORDER BY c.relname) FROM pg_locks l JOIN pg_class c "
                + "ON c.oid = l.relation WHERE l.pid = " + pid + " AND l.mode = 'RowExclusiveLock' AND c.relkind = 'r'");
    }

    private static String ringkas(String sql) {
        String s = sql.replaceAll("\\s+", " ").strip();
        return s.length() > 60 ? s.substring(0, 60) + "..." : s;
    }

    // Menunggu sampai koneksi akun aplikasi milik proses anak tertahan kunci baris, paling lama 15 detik.
    private int tungguTertahan() throws SQLException, InterruptedException {
        for (int i = 0; i < 150; i++) {
            int pid = angka("SELECT coalesce(max(pid), 0) FROM pg_stat_activity WHERE usename = 'sehati_app' "
                    + "AND wait_event_type = 'Lock'");
            if (pid != 0) {
                return pid;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Proses anak tidak tertahan kunci baris");
    }

    private void tungguBackendBerakhir(int pid) throws SQLException, InterruptedException {
        for (int i = 0; i < 150; i++) {
            if (angka("SELECT count(*) FROM pg_stat_activity WHERE pid = " + pid) == 0) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Koneksi proses yang dimatikan tidak berakhir");
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

    /** JVM anak yang menjalankan {@link ProsesFarmasiKnf13} dengan classpath test yang sama. */
    private static final class Proses {

        private final Process proses;
        private final BlockingQueue<String> baris = new LinkedBlockingQueue<>();
        private final List<String> semua = new ArrayList<>();

        private Proses(Process proses) {
            this.proses = proses;
            Thread pembaca = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(proses.getInputStream(),
                        StandardCharsets.UTF_8))) {
                    for (String s = r.readLine(); s != null; s = r.readLine()) {
                        baris.add(s);
                    }
                } catch (IOException e) {
                    // Aliran tertutup karena proses dimatikan.
                }
            });
            pembaca.setDaemon(true);
            pembaca.start();
        }

        static Proses jalankan(String... argumen) throws IOException {
            List<String> perintah = new ArrayList<>(List.of(System.getProperty("java.home") + File.separator + "bin"
                    + File.separator + "java", "-cp", System.getProperty("java.class.path"),
                    ProsesFarmasiKnf13.class.getName()));
            perintah.addAll(List.of(argumen));
            return new Proses(new ProcessBuilder(perintah).redirectErrorStream(true).start());
        }

        void tunggu(String awalan) throws InterruptedException {
            long batas = System.currentTimeMillis() + 30_000;
            while (System.currentTimeMillis() < batas) {
                String s = baris.poll(200, TimeUnit.MILLISECONDS);
                if (s != null) {
                    semua.add(s);
                    if (s.startsWith(awalan)) {
                        return;
                    }
                }
                if (!proses.isAlive() && baris.isEmpty()) {
                    break;
                }
            }
            proses.destroyForcibly();
            throw new AssertionError("Proses anak tidak mencapai " + awalan + ": " + semua);
        }

        // Process.destroyForcibly() pada Linux dan macOS mengirim SIGKILL, sama dengan kill -9.
        void matikanPaksa() throws InterruptedException {
            proses.destroyForcibly();
            proses.waitFor(10, TimeUnit.SECONDS);
        }

        int kodeKeluar() {
            return proses.exitValue();
        }

        int tungguSelesai() throws InterruptedException {
            if (!proses.waitFor(60, TimeUnit.SECONDS)) {
                proses.destroyForcibly();
                throw new AssertionError("Proses anak tidak selesai");
            }
            Thread.sleep(100);
            baris.drainTo(semua);
            if (proses.exitValue() != 0) {
                System.out.println("Keluaran proses anak: " + semua);
            }
            return proses.exitValue();
        }
    }
}
