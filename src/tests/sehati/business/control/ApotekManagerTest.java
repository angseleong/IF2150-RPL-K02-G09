package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.ResepEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataFarmasiBoundary;

class ApotekManagerTest {

    private final ApotekManager manager = new ApotekManager();

    private static ResepEntity resep(String status, ItemResepEntity... item) {
        ResepEntity r = new ResepEntity(9, 21, LocalDateTime.now(), status, null);
        r.setDaftarItem(List.of(item));
        return r;
    }

    @Test
    void pastikanTerkunci_menolakPemeriksaanBelumFinal() {
        ApotekManager.pastikanTerkunci(new PemeriksaanEntity(1, 1, "a", "b", true));
        assertThrows(IllegalStateException.class,
                () -> ApotekManager.pastikanTerkunci(new PemeriksaanEntity(1, 1, "a", "b", false)));
        assertThrows(IllegalStateException.class, () -> ApotekManager.pastikanTerkunci(null));
    }

    @Test
    void pastikanMenunggu_menolakResepYangSudahSelesaiAtauDikembalikan() {
        ApotekManager.pastikanMenunggu(resep(ResepEntity.STATUS_MENUNGGU));
        IllegalStateException selesai = assertThrows(IllegalStateException.class,
                () -> ApotekManager.pastikanMenunggu(resep(ResepEntity.STATUS_SELESAI_DILAYANI)));
        assertEquals("Resep sudah selesai dilayani", selesai.getMessage());
        IllegalStateException kembali = assertThrows(IllegalStateException.class,
                () -> ApotekManager.pastikanMenunggu(resep(ResepEntity.STATUS_DIKEMBALIKAN)));
        assertEquals("Resep berstatus dikembalikan dan tidak dapat diserahkan", kembali.getMessage());
    }

    @Test
    void potongStok_mengambilBetsKedaluwarsaTerdekatLebihDulu() {
        LocalDate hariIni = LocalDate.now();
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu()
                .obat(new ObatEntity(4, "Metformin 500 mg", "tablet", 50, 100),
                        new BetsObatEntity("MET-A", 4, 10, hariIni.plusDays(30)),
                        new BetsObatEntity("MET-B", 4, 40, hariIni.plusDays(400)));

        Map<Integer, ObatEntity> dipotong = ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 4, "2x1", 25, "sesudah makan")),
                dataFarmasi);

        assertEquals(25, dipotong.get(4).getStok());
        assertEquals(List.of("MET-A=0", "MET-B=25"), dataFarmasi.betsTersimpan);
        assertEquals(List.of("4=25"), dataFarmasi.stokTersimpan);
    }

    @Test
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
    void potongStok_stokLayakKurangKarenaKedaluwarsaDitolak() {
        LocalDate hariIni = LocalDate.now();
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu()
                .obat(new ObatEntity(4, "Metformin 500 mg", "tablet", 50, 100),
                        new BetsObatEntity("MET-LAMA", 4, 40, hariIni.minusDays(1)),
                        new BetsObatEntity("MET-A", 4, 10, hariIni.plusDays(30)));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 4, "2x1", 25, "sesudah makan")),
                dataFarmasi));

        assertTrue(e.getMessage().contains("belum kedaluwarsa"));
        assertTrue(dataFarmasi.stokTersimpan.isEmpty());
    }

    @Test
    void potongStok_stokTidakCukupDitolakSebelumMenulis() {
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu()
                .obat(new ObatEntity(5, "Glibenklamid 5 mg", "tablet", 20, 50),
                        new BetsObatEntity("GLB-A", 5, 20, LocalDate.now().plusDays(200)));

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 5, "1x1", 30, "pagi")), dataFarmasi));

        assertEquals("Stok Glibenklamid 5 mg tidak cukup", e.getMessage());
        assertTrue(dataFarmasi.stokTersimpan.isEmpty());
    }

    @Test
    void potongStok_betsKurangDariSaldoDitolak() {
        DataFarmasiPalsu dataFarmasi = new DataFarmasiPalsu()
                .obat(new ObatEntity(5, "Glibenklamid 5 mg", "tablet", 30, 50),
                        new BetsObatEntity("GLB-A", 5, 10, LocalDate.now().plusDays(200)));

        assertThrows(IllegalStateException.class, () -> ApotekManager.potongStok(
                resep(ResepEntity.STATUS_MENUNGGU, new ItemResepEntity(1, 5, "1x1", 30, "pagi")), dataFarmasi));
        assertTrue(dataFarmasi.stokTersimpan.isEmpty());
    }

    @Test
    void kembalikanKeDokter_alasanKosongDitolak() {
        assertThrows(IllegalArgumentException.class, () -> manager.kembalikanKeDokter(9, "  "));
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
     * Memakai seed: resep "menunggu" milik Siti Aminah berisi Metformin 60 dan Glibenklamid 30 (stok Glibenklamid
     * tepat 30). Variabel DB_* harus menunjuk basis data yang sama dengan DB_UJI_URL (akun pemilik). Stok, bets,
     * status resep dan kunjungan, antrean apotek, serta bundel dipulihkan setelah setiap uji.
     */
    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private int idResep;
        private int idKunjungan;
        private String statusKunjungan;
        private final Map<Integer, Integer> stokAwal = new LinkedHashMap<>();
        private final List<Object[]> betsAwal = new ArrayList<>();
        private final List<int[]> antreanAwal = new ArrayList<>();

        @BeforeEach
        void buka() throws SQLException {
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Petugas Farmasi");
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            idResep = hitung("SELECT min(id_resep) FROM resep WHERE status = 'menunggu'");
            idKunjungan = hitung("SELECT id_kunjungan FROM resep WHERE id_resep = " + idResep);
            statusKunjungan = teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan);
            try (Statement st = koneksi.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT o.id_obat, o.stok FROM obat o WHERE o.id_obat IN "
                        + "(SELECT id_obat FROM item_resep WHERE id_resep = " + idResep + ")")) {
                    while (rs.next()) {
                        stokAwal.put(rs.getInt(1), rs.getInt(2));
                    }
                }
                try (ResultSet rs = st.executeQuery("SELECT id_obat, nomor_bets, jumlah FROM bets_obat WHERE id_obat IN "
                        + "(SELECT id_obat FROM item_resep WHERE id_resep = " + idResep + ")")) {
                    while (rs.next()) {
                        betsAwal.add(new Object[] {rs.getInt(1), rs.getString(2), rs.getInt(3)});
                    }
                }
                try (ResultSet rs = st.executeQuery("SELECT e.urutan, e.id_entri FROM antrean_entri e "
                        + "JOIN antrean a USING (id_antrean) WHERE a.jenis = 'apotek'")) {
                    while (rs.next()) {
                        antreanAwal.add(new int[] {rs.getInt(1), rs.getInt(2)});
                    }
                }
            }
        }

        @AfterEach
        void pulihkan() throws SQLException {
            SesiAktif.lepas();
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan);
                st.executeUpdate("UPDATE resep SET status = 'menunggu', catatan = NULL WHERE id_resep = " + idResep);
                st.executeUpdate("UPDATE kunjungan SET status = '" + statusKunjungan + "' WHERE id_kunjungan = "
                        + idKunjungan);
                for (Map.Entry<Integer, Integer> e : stokAwal.entrySet()) {
                    st.executeUpdate("UPDATE obat SET stok = " + e.getValue() + " WHERE id_obat = " + e.getKey());
                }
                st.executeUpdate("DELETE FROM antrean_entri e USING antrean a WHERE e.id_antrean = a.id_antrean "
                        + "AND a.jenis = 'apotek'");
            }
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "UPDATE bets_obat SET jumlah = ? WHERE id_obat = ? AND nomor_bets = ?")) {
                for (Object[] b : betsAwal) {
                    ps.setInt(1, (Integer) b[2]);
                    ps.setInt(2, (Integer) b[0]);
                    ps.setString(3, (String) b[1]);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = koneksi.prepareStatement("INSERT INTO antrean_entri (id_antrean, urutan, "
                    + "id_entri) SELECT id_antrean, ?, ? FROM antrean WHERE jenis = 'apotek'")) {
                for (int[] e : antreanAwal) {
                    ps.setInt(1, e[0]);
                    ps.setInt(2, e[1]);
                    ps.executeUpdate();
                }
            }
            koneksi.close();
        }

        @Test
        void serahkanObat_satuTransaksiMencakupStokResepKunjunganDanBundel() throws SQLException {
            manager.serahkanObat(idResep);

            for (Map.Entry<Integer, Integer> e : stokAwal.entrySet()) {
                int jumlah = hitung("SELECT jumlah FROM item_resep WHERE id_resep = " + idResep + " AND id_obat = "
                        + e.getKey());
                assertEquals(e.getValue() - jumlah, hitung("SELECT stok FROM obat WHERE id_obat = " + e.getKey()));
                // Saldo obat tetap sama dengan jumlah bets setelah FEFO.
                assertEquals(e.getValue() - jumlah, hitung("SELECT coalesce(sum(jumlah), 0) FROM bets_obat "
                        + "WHERE id_obat = " + e.getKey()));
            }
            assertEquals("selesai dilayani", teks("SELECT status FROM resep WHERE id_resep = " + idResep));
            assertEquals("selesai", teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan));
            assertEquals("tertunda", teks("SELECT status FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
            assertEquals(stokAwal.size(), hitung("SELECT count(*) FROM bundel_fhir b, "
                    + "jsonb_array_elements(b.isi_bundel -> 'entry') e WHERE b.id_kunjungan = " + idKunjungan
                    + " AND e -> 'resource' ->> 'resourceType' = 'MedicationRequest'"));
            assertEquals(0, hitung("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                    + "WHERE a.jenis = 'apotek' AND e.id_entri = " + idResep));
        }

        @Test
        void serahkanObat_resepYangSudahSelesaiDitolakTanpaMemotongLagi() throws SQLException {
            manager.serahkanObat(idResep);
            int stokSetelahPertama = hitung("SELECT sum(stok) FROM obat");

            IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.serahkanObat(idResep));

            assertEquals("Resep sudah selesai dilayani", e.getMessage());
            assertEquals(stokSetelahPertama, hitung("SELECT sum(stok) FROM obat"));
        }

        @Test
        void serahkanObat_duaPetugasBersamaanHanyaSatuYangBerhasil() throws Exception {
            int stokTotal = hitung("SELECT sum(stok) FROM obat");
            int jumlahResep = hitung("SELECT sum(jumlah) FROM item_resep WHERE id_resep = " + idResep);
            CountDownLatch mulai = new CountDownLatch(1);
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
        void serahkanObat_bundelGagalDisimpanMembatalkanSeluruhPerubahan() throws SQLException {
            // Bundel lama untuk kunjungan yang sama melanggar UNIQUE id_kunjungan, sehingga simpanBundel() gagal.
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO bundel_fhir (id_kunjungan, isi_bundel) VALUES (" + idKunjungan
                        + ", '{}'::jsonb)");
            }
            int stokTotal = hitung("SELECT sum(stok) FROM obat");
            int jumlahBets = hitung("SELECT sum(jumlah) FROM bets_obat");

            // Pelanggaran UNIQUE diterjemahkan menjadi IllegalArgumentException (aturan 3 Subbab 5.56)
            assertThrows(IllegalArgumentException.class, () -> manager.serahkanObat(idResep));

            assertEquals(stokTotal, hitung("SELECT sum(stok) FROM obat"));
            assertEquals(jumlahBets, hitung("SELECT sum(jumlah) FROM bets_obat"));
            assertEquals("menunggu", teks("SELECT status FROM resep WHERE id_resep = " + idResep));
            assertEquals(statusKunjungan, teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan));
            assertEquals("{}", teks("SELECT isi_bundel::text FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
            assertEquals(antreanAwal.size(), hitung("SELECT count(*) FROM antrean_entri e JOIN antrean a "
                    + "USING (id_antrean) WHERE a.jenis = 'apotek'"));
        }

        @Test
        void kembalikanKeDokter_mencatatAlasanDanMengeluarkanDariAntrean() throws SQLException {
            int stokTotal = hitung("SELECT sum(stok) FROM obat");

            manager.kembalikanKeDokter(idResep, "  Stok Glibenklamid habis, mohon ganti obat ");

            assertEquals("dikembalikan", teks("SELECT status FROM resep WHERE id_resep = " + idResep));
            assertEquals("Stok Glibenklamid habis, mohon ganti obat",
                    teks("SELECT catatan FROM resep WHERE id_resep = " + idResep));
            assertEquals(statusKunjungan, teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan));
            assertEquals(stokTotal, hitung("SELECT sum(stok) FROM obat"));
            assertEquals(0, hitung("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                    + "WHERE a.jenis = 'apotek' AND e.id_entri = " + idResep));
            assertThrows(IllegalStateException.class, () -> manager.layaniResep(idResep));
        }

        @Test
        void layaniResepDanMuatBets_kedaluwarsaTerdekatLebihDulu() {
            ResepEntity resep = manager.layaniResep(idResep);
            List<BetsObatEntity> bets = manager.muatBetsResep(resep);

            assertEquals(stokAwal.size(), resep.getDaftarItem().size());
            for (int i = 1; i < bets.size(); i++) {
                BetsObatEntity sebelum = bets.get(i - 1);
                BetsObatEntity sesudah = bets.get(i);
                assertTrue(sebelum.getIdObat() != sesudah.getIdObat()
                        || !sebelum.getTanggalKedaluwarsa().isAfter(sesudah.getTanggalKedaluwarsa()));
            }
            assertEquals("Siti Aminah", manager.muatPasienResep(List.of(idResep)).get(idResep).getNama());
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
