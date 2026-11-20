package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.CadanganFarmasi;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.ResepEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.PengaturanAplikasi;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataFarmasiBoundary;

/**
 * Pengujian unit C24 ApotekManager (HIUPL Subbab 4.2, C24-UT01 sampai C24-UT37). Setiap test diberi
 * {@code @DisplayName} berawalan ID test case.
 */
@DisplayName("C24 ApotekManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ApotekManagerTest {

    private final ApotekManager manager = new ApotekManager();

    @AfterEach
    void lepasSesi() {
        SesiAktif.lepas();
    }

    private static ResepEntity resep(String status, ItemResepEntity... item) {
        ResepEntity r = new ResepEntity(9, 21, LocalDateTime.now(), status, null);
        r.setDaftarItem(List.of(item));
        return r;
    }

    private static void masukSebagai(String peran) {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 4, LocalDateTime.now(), true), peran);
    }

    @Test
    @DisplayName("C24-UT15 serahkanObat negatif: peran Dokter ditolak sebelum basis data disentuh")
    void serahkanObat_peranDokter() {
        masukSebagai("Dokter");
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.serahkanObat(4));
        assertEquals("Peran Dokter tidak berwenang menjalankan tindakan ini", e.getMessage());
    }

    @Test
    @DisplayName("C24-UT17 potongStok normal: bets kedaluwarsa terdekat diambil lebih dulu (FEFO), bets terjauh "
            + "tidak disentuh")
    void potongStok_fefo() {
        LocalDate hariIni = LocalDate.now();
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu()
                .obat(new ObatEntity(4, "Metformin 500 mg", "tablet", 100, 100),
                        new BetsObatEntity("MET-A", 4, 10, hariIni.plusDays(30)),
                        new BetsObatEntity("MET-B", 4, 40, hariIni.plusDays(400)),
                        new BetsObatEntity("MET-C", 4, 50, hariIni.plusDays(800)));

        Map<Integer, ObatEntity> dipotong = ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 4, "2x1", 25, "sesudah makan")),
                dataFarmasi);

        assertEquals(75, dipotong.get(4).getStok());
        assertEquals(List.of("MET-A=0", "MET-B=25", "MET-C=50"), dataFarmasi.betsTersimpan);
        assertEquals(List.of("4=75"), dataFarmasi.stokTersimpan);
    }

    @Test
    @DisplayName("C24-UT18 potongStok batas: bets yang kedaluwarsa kemarin dilewati, bets hari berikutnya dipakai")
    void potongStok_betsKedaluwarsaDilewati() {
        LocalDate hariIni = LocalDate.now();
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu()
                .obat(new ObatEntity(4, "Metformin 500 mg", "tablet", 50, 100),
                        new BetsObatEntity("MET-LAMA", 4, 20, hariIni.minusDays(1)),
                        new BetsObatEntity("MET-A", 4, 30, hariIni.plusDays(30)));

        ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 4, "2x1", 25, "sesudah makan")),
                dataFarmasi);

        assertEquals(List.of("MET-LAMA=20", "MET-A=5"), dataFarmasi.betsTersimpan);
        assertEquals(List.of("4=25"), dataFarmasi.stokTersimpan);
    }

    @Test
    @DisplayName("C24-UT19 potongStok negatif: saldo obat kurang dari jumlah resep ditolak sebelum menulis")
    void potongStok_stokTidakCukup() {
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu()
                .obat(new ObatEntity(5, "Glibenklamid 5 mg", "tablet", 20, 50),
                        new BetsObatEntity("GLB-A", 5, 20, LocalDate.now().plusDays(200)));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 5, "1x1", 30, "pagi")), dataFarmasi));

        assertEquals("Stok Glibenklamid 5 mg tidak cukup", e.getMessage());
        assertTrue(dataFarmasi.stokTersimpan.isEmpty());
    }

    @Test
    @DisplayName("C24-UT20 potongStok negatif: saldo cukup tetapi bets yang belum kedaluwarsa kurang ditolak")
    void potongStok_stokLayakKurang() {
        LocalDate hariIni = LocalDate.now();
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu()
                .obat(new ObatEntity(4, "Metformin 500 mg", "tablet", 50, 100),
                        new BetsObatEntity("MET-LAMA", 4, 40, hariIni.minusDays(1)),
                        new BetsObatEntity("MET-A", 4, 10, hariIni.plusDays(30)));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 4, "2x1", 25, "sesudah makan")),
                dataFarmasi));

        assertEquals("Stok Metformin 500 mg yang belum kedaluwarsa tidak cukup, periksa persediaan", e.getMessage());
        assertTrue(dataFarmasi.stokTersimpan.isEmpty());
    }

    @Test
    @DisplayName("C24-UT21 potongStok negatif: jumlah bets kurang dari saldo obat (data stok rusak) ditolak")
    void potongStok_betsKurangDariSaldo() {
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu()
                .obat(new ObatEntity(5, "Glibenklamid 5 mg", "tablet", 30, 50),
                        new BetsObatEntity("GLB-A", 5, 10, LocalDate.now().plusDays(200)));

        assertThrows(IllegalStateException.class, () -> ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 5, "1x1", 30, "pagi")), dataFarmasi));
        assertTrue(dataFarmasi.stokTersimpan.isEmpty());
    }

    @Test
    @DisplayName("C24-UT22 potongStok negatif: obat yang tidak ada pada data master ditolak")
    void potongStok_obatTidakAda() {
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu();

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 99, "1x1", 5, "pagi")), dataFarmasi));

        assertEquals("Obat 99 tidak ada pada data master", e.getMessage());
        assertTrue(dataFarmasi.stokTersimpan.isEmpty());
    }

    @Test
    @DisplayName("C24-UT25 kembalikanKeDokter negatif: alasan kosong, berspasi, dan null ditolak")
    void kembalikanKeDokter_alasanKosong() {
        masukSebagai("Petugas Farmasi");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.kembalikanKeDokter(4, ""));
        assertEquals("Alasan pengembalian wajib diisi", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> manager.kembalikanKeDokter(4, "   "));
        assertThrows(IllegalArgumentException.class, () -> manager.kembalikanKeDokter(4, null));
    }

    @Test
    @DisplayName("C24-UT27 kembalikanKeDokter negatif: peran Perawat ditolak sebelum basis data disentuh")
    void kembalikanKeDokter_peranPerawat() {
        masukSebagai("Perawat");
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> manager.kembalikanKeDokter(4, "Kemasan rusak"));
        assertEquals("Peran Perawat tidak berwenang menjalankan tindakan ini", e.getMessage());
    }

    @Test
    @DisplayName("C24-UT28 pastikanMenunggu negatif: resep selesai dan dikembalikan ditolak dengan pesan berbeda")
    void pastikanMenunggu() {
        ApotekManager.pastikanMenunggu(resep(ResepEntity.STATUS_MENUNGGU));
        IllegalStateException selesai = assertThrows(IllegalStateException.class,
                () -> ApotekManager.pastikanMenunggu(resep(ResepEntity.STATUS_SELESAI_DILAYANI)));
        assertEquals("Resep sudah selesai dilayani", selesai.getMessage());
        IllegalStateException kembali = assertThrows(IllegalStateException.class,
                () -> ApotekManager.pastikanMenunggu(resep(ResepEntity.STATUS_DIKEMBALIKAN)));
        assertEquals("Resep berstatus dikembalikan dan tidak dapat diserahkan", kembali.getMessage());
    }

    @Test
    @DisplayName("C24-UT29 pastikanTerkunci negatif: pemeriksaan belum final dan tanpa pemeriksaan ditolak")
    void pastikanTerkunci() {
        ApotekManager.pastikanTerkunci(new PemeriksaanEntity(1, 1, "a", "b", true));
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> ApotekManager.pastikanTerkunci(new PemeriksaanEntity(1, 1, "a", "b", false)));
        assertEquals("Dokter belum menyimpan final pemeriksaan pasien ini", e.getMessage());
        assertThrows(IllegalStateException.class, () -> ApotekManager.pastikanTerkunci(null));
    }

    /** Pengganti DataFarmasiBoundary yang menyimpan stok dan bets di memori. */
    private static final class DataFarmasiPalsu extends DataFarmasiBoundary {

        private final Map<Integer, ObatEntity> daftarObat = new LinkedHashMap<>();
        private final Map<Integer, List<BetsObatEntity>> daftarBets = new LinkedHashMap<>();
        private final List<String> stokTersimpan = new ArrayList<>();
        private final List<String> betsTersimpan = new ArrayList<>();

        DataFarmasiPalsu() {
            super(null);
        }

        DataFarmasiPalsu obat(ObatEntity o, BetsObatEntity... bets) {
            daftarObat.put(o.getIdObat(), o);
            daftarBets.put(o.getIdObat(), new ArrayList<>(List.of(bets)));
            return this;
        }

        @Override
        public ObatEntity ambilObat(int idObat) {
            return daftarObat.get(idObat);
        }

        @Override
        public List<BetsObatEntity> ambilBets(int idObat) {
            return daftarBets.getOrDefault(idObat, List.of());
        }

        @Override
        public void simpanStok(ObatEntity o, List<BetsObatEntity> bets) {
            stokTersimpan.add(o.getIdObat() + "=" + o.getStok());
            bets.forEach(b -> betsTersimpan.add(b.getNomorBets() + "=" + b.getJumlah()));
        }
    }

    /**
     * Memakai seed: resep 4 "menunggu" milik Siti Aminah (kunjungan 5, pemeriksaan terkunci) berisi Metformin 60 dan
     * Glibenklamid 30 (stok Glibenklamid tepat 30), resep 1 "selesai dilayani", Amlodipin dengan dua bets (AML-A01
     * sisa 40, AML-B01 sisa 150), dan Asam Mefenamat berstok 0 tanpa bets bersisa. DB_UJI_URL harus memakai akun
     * pemilik basis data. Seluruh perubahan dipulihkan {@link CadanganFarmasi} setelah setiap test.
     */
    @Nested
    @DisplayName("C24 ApotekManager dengan basis data")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private static final int METFORMIN = 4;
        private static final int GLIBENKLAMID = 5;
        private static final int AMLODIPIN = 3;
        private static final int ASAM_MEFENAMAT = 12;

        private Connection koneksi;
        private CadanganFarmasi cadangan;
        private int idResep;
        private int idKunjungan;
        private String statusKunjungan;

        @BeforeEach
        void buka() throws SQLException {
            masukSebagai("Petugas Farmasi");
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            cadangan = CadanganFarmasi.ambil(koneksi);
            idResep = hitung("SELECT min(id_resep) FROM resep WHERE status = 'menunggu'");
            idKunjungan = hitung("SELECT id_kunjungan FROM resep WHERE id_resep = " + idResep);
            statusKunjungan = teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan);
        }

        @AfterEach
        void pulihkan() throws SQLException {
            SesiAktif.lepas();
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("GRANT SELECT ON resep TO sehati_app");
            }
            cadangan.pulihkan();
            koneksi.close();
        }

        @Test
        @DisplayName("C24-UT01 muatAntreanResep normal: resep menunggu dimuat beserta seluruh itemnya")
        void muatAntreanResep_normal() throws SQLException {
            List<ResepEntity> antrean = manager.muatAntreanResep();

            assertEquals(hitung("SELECT count(*) FROM resep WHERE status = 'menunggu'"), antrean.size());
            ResepEntity r = antrean.get(0);
            assertEquals(idResep, r.getIdResep());
            assertEquals("menunggu", r.getStatus());
            assertEquals(List.of("4x60", "5x30"), r.getDaftarItem().stream()
                    .map(i -> i.getIdObat() + "x" + i.getJumlah()).toList());
        }

        @Test
        @DisplayName("C24-UT02 muatAntreanResep batas: resep yang masuk lebih awal tampil lebih dulu walau disimpan "
                + "belakangan")
        void muatAntreanResep_urutanWaktuMasuk() throws SQLException {
            int idBaru = sisipkanResep(LocalDate.now().atTime(0, 1), AMLODIPIN, 10);

            List<Integer> urutan = manager.muatAntreanResep().stream().map(ResepEntity::getIdResep).toList();

            assertEquals(List.of(idBaru, idResep), urutan);
        }

        @Test
        @DisplayName("C24-UT03 muatAntreanResep negatif: basis data menolak akses ke tabel resep")
        void muatAntreanResep_aksesDitolak() throws SQLException {
            assumeTrue("sehati_app".equals(PengaturanAplikasi.muat().ambil("DB_USER")));
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("REVOKE SELECT ON resep FROM sehati_app");
            }
            IllegalStateException e = assertThrows(IllegalStateException.class, manager::muatAntreanResep);
            assertEquals("Gagal mengambil antrean resep", e.getMessage());
        }

        @Test
        @DisplayName("C24-UT04 layaniResep normal: rincian resep menunggu dikembalikan beserta itemnya")
        void layaniResep_normal() {
            ResepEntity r = manager.layaniResep(idResep);

            assertEquals(idKunjungan, r.getIdKunjungan());
            assertEquals(2, r.getDaftarItem().size());
            assertEquals("sesudah makan", r.getDaftarItem().get(0).getAturanPakai());
        }

        @Test
        @DisplayName("C24-UT05 layaniResep batas: rincian dibuka dua kali tanpa mengubah status maupun stok")
        void layaniResep_tidakMengubahData() throws SQLException {
            int stokTotal = hitung("SELECT sum(stok) FROM obat");

            manager.layaniResep(idResep);
            manager.layaniResep(idResep);

            assertEquals("menunggu", teks("SELECT status FROM resep WHERE id_resep = " + idResep));
            assertEquals(stokTotal, hitung("SELECT sum(stok) FROM obat"));
        }

        @Test
        @DisplayName("C24-UT06 layaniResep negatif: resep yang tidak ada ditolak")
        void layaniResep_tidakAda() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.layaniResep(-1));
            assertEquals("Resep tidak ditemukan", e.getMessage());
        }

        @Test
        @DisplayName("C24-UT07 layaniResep negatif: resep yang sudah selesai dilayani ditolak lebih awal")
        void layaniResep_sudahSelesai() throws SQLException {
            int selesai = hitung("SELECT min(id_resep) FROM resep WHERE status = 'selesai dilayani'");
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.layaniResep(selesai));
            assertEquals("Resep sudah selesai dilayani", e.getMessage());
        }

        @Test
        @DisplayName("C24-UT08 serahkanObat normal: satu transaksi mencakup stok, resep, antrean, kunjungan, bundel, "
                + "dan audit")
        void serahkanObat_normal() throws SQLException {
            int stokMetformin = stok(METFORMIN);
            int stokGlibenklamid = stok(GLIBENKLAMID);

            manager.serahkanObat(idResep);

            assertEquals(stokMetformin - 60, stok(METFORMIN));
            assertEquals(stokGlibenklamid - 30, stok(GLIBENKLAMID));
            // Saldo obat tetap sama dengan jumlah bets setelah FEFO.
            for (int idObat : List.of(METFORMIN, GLIBENKLAMID)) {
                assertEquals(stok(idObat), hitung("SELECT coalesce(sum(jumlah), 0) FROM bets_obat WHERE id_obat = "
                        + idObat));
            }
            assertEquals("selesai dilayani", teks("SELECT status FROM resep WHERE id_resep = " + idResep));
            assertEquals("selesai", teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan));
            assertEquals("tertunda", teks("SELECT status FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
            assertEquals(2, hitung("SELECT count(*) FROM bundel_fhir b, jsonb_array_elements(b.isi_bundel -> 'entry') e "
                    + "WHERE b.id_kunjungan = " + idKunjungan
                    + " AND e -> 'resource' ->> 'resourceType' = 'MedicationRequest'"));
            assertEquals(0, hitung("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                    + "WHERE a.jenis = 'apotek' AND e.id_entri = " + idResep));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_audit > " + cadangan.auditTerakhir()
                    + " AND id_pengguna = 4 AND deskripsi = 'Obat resep " + idResep + " diserahkan'"));
        }

        @Test
        @DisplayName("C24-UT09 serahkanObat batas: jumlah sama dengan stok menghabiskan stok dan bets menjadi 0")
        void serahkanObat_stokHabisTepat() throws SQLException {
            assertEquals(30, stok(GLIBENKLAMID));

            manager.serahkanObat(idResep);

            assertEquals(0, stok(GLIBENKLAMID));
            assertEquals(0, hitung("SELECT jumlah FROM bets_obat WHERE nomor_bets = 'GLB-A01'"));
            try (Connection lain = DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
                assertTrue(new DataFarmasiBoundary(lain).ambilBets(GLIBENKLAMID).isEmpty());
            }
        }

        @Test
        @DisplayName("C24-UT10 serahkanObat batas: jumlah melebihi bets terdekat diambil dari dua bets secara FEFO")
        void serahkanObat_fefoDuaBets() throws SQLException {
            // AML-A01 (sisa 40) kedaluwarsa lebih dulu dari AML-B01 (sisa 150).
            int idBaru = sisipkanResep(LocalDateTime.now(), AMLODIPIN, 50);

            manager.serahkanObat(idBaru);

            assertEquals(0, hitung("SELECT jumlah FROM bets_obat WHERE nomor_bets = 'AML-A01'"));
            assertEquals(140, hitung("SELECT jumlah FROM bets_obat WHERE nomor_bets = 'AML-B01'"));
            assertEquals(140, stok(AMLODIPIN));
        }

        @Test
        @DisplayName("C24-UT11 serahkanObat batas: dua petugas bersamaan, hanya satu yang berhasil (UC07 alt 2)")
        void serahkanObat_duaPetugasBersamaan() throws Exception {
            int stokTotal = hitung("SELECT sum(stok) FROM obat");
            int jumlahResep = hitung("SELECT sum(jumlah) FROM item_resep WHERE id_resep = " + idResep);
            CountDownLatch mulai = new CountDownLatch(1);
            // SesiAktif bersifat global, jadi kedua thread memakai sesi Petugas Farmasi yang dipasang buka().
            Callable<String> petugas = () -> {
                mulai.await();
                try {
                    new ApotekManager().serahkanObat(idResep);
                    return "berhasil";
                } catch (IllegalStateException e) {
                    return e.getMessage();
                }
            };
            ExecutorService pelaksana = Executors.newFixedThreadPool(2);
            try {
                Future<String> satu = pelaksana.submit(petugas);
                Future<String> dua = pelaksana.submit(petugas);
                mulai.countDown();
                List<String> hasil = List.of(satu.get(), dua.get());

                assertTrue(hasil.contains("berhasil"));
                assertTrue(hasil.contains("Resep sudah selesai dilayani"));
            } finally {
                pelaksana.shutdownNow();
            }
            assertEquals(stokTotal - jumlahResep, hitung("SELECT sum(stok) FROM obat"));
            assertEquals(1, hitung("SELECT count(*) FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
        }

        @Test
        @DisplayName("C24-UT12 serahkanObat negatif: resep yang sudah selesai ditolak tanpa memotong stok lagi")
        void serahkanObat_sudahSelesai() throws SQLException {
            manager.serahkanObat(idResep);
            int stokSetelahPertama = hitung("SELECT sum(stok) FROM obat");

            IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.serahkanObat(idResep));

            assertEquals("Resep sudah selesai dilayani", e.getMessage());
            assertEquals(stokSetelahPertama, hitung("SELECT sum(stok) FROM obat"));
        }

        @Test
        @DisplayName("C24-UT13 serahkanObat negatif: pemeriksaan yang belum dikunci dokter ditolak tanpa perubahan")
        void serahkanObat_pemeriksaanBelumDikunci() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("UPDATE pemeriksaan SET terkunci = false WHERE id_kunjungan = " + idKunjungan);
            }
            int stokTotal = hitung("SELECT sum(stok) FROM obat");

            IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.serahkanObat(idResep));

            assertEquals("Dokter belum menyimpan final pemeriksaan pasien ini", e.getMessage());
            assertEquals(stokTotal, hitung("SELECT sum(stok) FROM obat"));
            assertEquals("menunggu", teks("SELECT status FROM resep WHERE id_resep = " + idResep));
            assertEquals(0, hitung("SELECT count(*) FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
        }

        @Test
        @DisplayName("C24-UT14 serahkanObat negatif: resep yang tidak ada ditolak")
        void serahkanObat_tidakAda() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.serahkanObat(-1));
            assertEquals("Resep tidak ditemukan", e.getMessage());
        }

        @Test
        @DisplayName("C24-UT16 serahkanObat negatif: bundel gagal disimpan membatalkan seluruh perubahan (KNF03)")
        void serahkanObat_bundelGagal() throws SQLException {
            // Bundel lama untuk kunjungan yang sama melanggar UNIQUE id_kunjungan, sehingga simpanBundel() gagal.
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO bundel_fhir (id_kunjungan, isi_bundel) VALUES (" + idKunjungan
                        + ", '{}'::jsonb)");
            }
            int stokTotal = hitung("SELECT sum(stok) FROM obat");
            int jumlahBets = hitung("SELECT sum(jumlah) FROM bets_obat");
            int jumlahAntrean = hitung("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                    + "WHERE a.jenis = 'apotek'");

            // Pelanggaran UNIQUE diterjemahkan menjadi IllegalArgumentException (aturan 3 Subbab 5.56)
            assertThrows(IllegalArgumentException.class, () -> manager.serahkanObat(idResep));

            assertEquals(stokTotal, hitung("SELECT sum(stok) FROM obat"));
            assertEquals(jumlahBets, hitung("SELECT sum(jumlah) FROM bets_obat"));
            assertEquals("menunggu", teks("SELECT status FROM resep WHERE id_resep = " + idResep));
            assertEquals(statusKunjungan, teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan));
            assertEquals("{}", teks("SELECT isi_bundel::text FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
            assertEquals(jumlahAntrean, hitung("SELECT count(*) FROM antrean_entri e JOIN antrean a "
                    + "USING (id_antrean) WHERE a.jenis = 'apotek'"));
        }

        @Test
        @DisplayName("C24-UT23 kembalikanKeDokter normal: alasan dicatat, resep keluar antrean, kunjungan tetap "
                + "terbuka")
        void kembalikanKeDokter_normal() throws SQLException {
            int stokTotal = hitung("SELECT sum(stok) FROM obat");

            manager.kembalikanKeDokter(idResep, "Stok Glibenklamid rusak, mohon ganti obat");

            assertEquals("dikembalikan", teks("SELECT status FROM resep WHERE id_resep = " + idResep));
            assertEquals("Stok Glibenklamid rusak, mohon ganti obat",
                    teks("SELECT catatan FROM resep WHERE id_resep = " + idResep));
            assertEquals(statusKunjungan, teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan));
            assertEquals(stokTotal, hitung("SELECT sum(stok) FROM obat"));
            assertEquals(0, hitung("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                    + "WHERE a.jenis = 'apotek' AND e.id_entri = " + idResep));
            assertEquals(1, hitung("SELECT count(*) FROM audit WHERE id_audit > " + cadangan.auditTerakhir()
                    + " AND deskripsi = 'Resep " + idResep + " dikembalikan ke dokter: Stok Glibenklamid rusak, "
                    + "mohon ganti obat'"));
            assertThrows(IllegalStateException.class, () -> manager.layaniResep(idResep));
        }

        @Test
        @DisplayName("C24-UT24 kembalikanKeDokter batas: alasan berspasi tepi disimpan tanpa spasi tepi")
        void kembalikanKeDokter_alasanDipangkas() throws SQLException {
            manager.kembalikanKeDokter(idResep, "   Kemasan rusak \n");

            assertEquals("Kemasan rusak", teks("SELECT catatan FROM resep WHERE id_resep = " + idResep));
        }

        @Test
        @DisplayName("C24-UT26 kembalikanKeDokter negatif: resep yang sudah selesai dilayani tidak dapat "
                + "dikembalikan")
        void kembalikanKeDokter_sudahSelesai() throws SQLException {
            int selesai = hitung("SELECT min(id_resep) FROM resep WHERE status = 'selesai dilayani'");

            assertThrows(IllegalStateException.class, () -> manager.kembalikanKeDokter(selesai, "Terlambat"));

            assertEquals("selesai dilayani", teks("SELECT status FROM resep WHERE id_resep = " + selesai));
            assertNull(teks("SELECT catatan FROM resep WHERE id_resep = " + selesai));
            assertEquals(0, hitung("SELECT count(*) FROM audit WHERE id_audit > " + cadangan.auditTerakhir()));
        }

        @Test
        @DisplayName("C24-UT37 kembalikanKeDokter negatif: resep yang tidak ada ditolak tanpa audit")
        void kembalikanKeDokter_tidakAda() throws SQLException {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.kembalikanKeDokter(-1, "Kemasan rusak"));
            assertEquals("Resep tidak ditemukan", e.getMessage());
            assertEquals(0, hitung("SELECT count(*) FROM audit WHERE id_audit > " + cadangan.auditTerakhir()));
        }

        @Test
        @DisplayName("C24-UT30 muatBetsResep normal: bets setiap obat resep terurut kedaluwarsa terdekat")
        void muatBetsResep_normal() {
            ResepEntity r = manager.layaniResep(idResep);
            List<BetsObatEntity> bets = manager.muatBetsResep(r);

            assertEquals(List.of("MET-A01", "GLB-A01"), bets.stream().map(BetsObatEntity::getNomorBets).toList());
            for (int i = 1; i < bets.size(); i++) {
                BetsObatEntity sebelum = bets.get(i - 1);
                BetsObatEntity sesudah = bets.get(i);
                assertTrue(sebelum.getIdObat() != sesudah.getIdObat()
                        || !sebelum.getTanggalKedaluwarsa().isAfter(sesudah.getTanggalKedaluwarsa()));
            }
        }

        @Test
        @DisplayName("C24-UT31 muatBetsResep batas: obat tanpa bets bersisa menghasilkan daftar kosong")
        void muatBetsResep_tanpaBets() {
            ResepEntity r = resep(ResepEntity.STATUS_MENUNGGU,
                    new ItemResepEntity(0, ASAM_MEFENAMAT, "500 mg", 10, "bila nyeri"));
            assertTrue(manager.muatBetsResep(r).isEmpty());
        }

        @Test
        @DisplayName("C24-UT32 muatPasienResep normal: pasien pemilik resep dipetakan dengan ID resep")
        void muatPasienResep_normal() throws SQLException {
            Map<Integer, PasienEntity> pasien = manager.muatPasienResep(List.of(idResep));

            assertEquals(1, pasien.size());
            assertEquals(teks("SELECT p.nama FROM pasien p JOIN kunjungan k USING (id_pasien) WHERE k.id_kunjungan = "
                    + idKunjungan), pasien.get(idResep).getNama());
        }

        @Test
        @DisplayName("C24-UT33 muatPasienResep batas: daftar resep kosong dan null mengembalikan peta kosong")
        void muatPasienResep_kosong() {
            assertTrue(manager.muatPasienResep(List.of()).isEmpty());
            assertTrue(manager.muatPasienResep(null).isEmpty());
        }

        @Test
        @DisplayName("C24-UT35 muatAsalResep batas: daftar resep kosong dan null mengembalikan peta kosong")
        void muatAsalResep_kosong() {
            assertTrue(manager.muatAsalResep(List.of()).isEmpty());
            assertTrue(manager.muatAsalResep(null).isEmpty());
        }

        @Test
        @DisplayName("C24-UT34 muatAsalResep normal: poli asal dan nomor antrean kunjungan resep")
        void muatAsalResep_normal() throws SQLException {
            String harapan = teks("SELECT po.nama_poli || ' / Antrean ' || k.nomor_antrean FROM kunjungan k "
                    + "JOIN poli po USING (id_poli) WHERE k.id_kunjungan = " + idKunjungan);
            assertEquals(harapan, manager.muatAsalResep(List.of(idResep)).get(idResep));
        }

        @Test
        @DisplayName("C24-UT36 muatDataObat normal: seluruh obat data master dimuat terurut nama")
        void muatDataObat_normal() throws SQLException {
            List<ObatEntity> obat = manager.muatDataObat();

            assertEquals(hitung("SELECT count(*) FROM obat"), obat.size());
            for (int i = 1; i < obat.size(); i++) {
                assertFalse(obat.get(i - 1).getNamaObat().compareTo(obat.get(i).getNamaObat()) > 0);
            }
        }

        // Resep "menunggu" baru untuk kunjungan resep seed, dengan satu item. Dihapus CadanganFarmasi.
        private int sisipkanResep(LocalDateTime waktuMasuk, int idObat, int jumlah) throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO resep (id_kunjungan, waktu_masuk, status) VALUES (" + idKunjungan
                        + ", '" + waktuMasuk + "', 'menunggu')");
                int id = hitung("SELECT max(id_resep) FROM resep");
                st.executeUpdate("INSERT INTO item_resep (id_resep, id_obat, dosis, jumlah, aturan_pakai) VALUES ("
                        + id + ", " + idObat + ", '1x1', " + jumlah + ", 'sesudah makan')");
                return id;
            }
        }

        private int stok(int idObat) throws SQLException {
            return hitung("SELECT stok FROM obat WHERE id_obat = " + idObat);
        }

        private int hitung(String sql) throws SQLException {
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
    }
}
