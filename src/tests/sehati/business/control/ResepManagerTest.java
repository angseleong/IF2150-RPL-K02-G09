package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.ResepEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.PengaturanAplikasi;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataFarmasiBoundary;

/**
 * Pengujian unit C23 ResepManager (HIUPL Subbab 4.2, C23-UT01 sampai C23-UT25). Setiap test diberi
 * {@code @DisplayName} berawalan ID test case.
 */
@DisplayName("C23 ResepManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class ResepManagerTest {

    private final ResepManager manager = new ResepManager();

    @AfterEach
    void lepasSesi() {
        SesiAktif.lepas();
    }

    @Test
    @DisplayName("C23-UT06 tambahItem negatif: jumlah nol ditolak")
    void tambahItem_jumlahNol() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.tambahItem(1, 0, "500 mg", "3 x 1"));
        assertEquals("Jumlah obat harus lebih dari nol", e.getMessage());
    }

    @Test
    @DisplayName("C23-UT07 tambahItem negatif: jumlah negatif ditolak")
    void tambahItem_jumlahNegatif() {
        assertThrows(IllegalArgumentException.class, () -> manager.tambahItem(1, -3, "500 mg", "3 x 1"));
    }

    @Test
    @DisplayName("C23-UT18 kirimKeApotek negatif: resep kosong ditolak")
    void kirimKeApotek_resepKosong() {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Dokter");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.kirimKeApotek(1, List.of()));
        assertEquals("Resep belum berisi obat", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> manager.kirimKeApotek(1, null));
    }

    @Test
    @DisplayName("C23-UT21 kirimKeApotek negatif: peran Petugas Farmasi ditolak")
    void kirimKeApotek_bukanDokter() {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 4, LocalDateTime.now(), true), "Petugas Farmasi");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.kirimKeApotek(1,
                List.of(new ItemResepEntity(0, 1, "500 mg", 1, "3 x 1"))));
        assertEquals("Peran Petugas Farmasi tidak berwenang menjalankan tindakan ini", e.getMessage());
    }

    /**
     * Memakai seed: obat 1 (Parasetamol) stok 300, obat 5 (Glibenklamid) stok 30, dan obat 12 (Asam Mefenamat)
     * stok 0. Variabel DB_* harus menunjuk basis data yang sama dengan DB_UJI_URL. Resep yang dibuat dan isi antrean
     * apotek dipulihkan setelah setiap uji.
     */
    @Nested
    @DisplayName("Dengan basis data uji")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private int idKunjungan;
        private int resepTerakhir;

        @BeforeEach
        void buka() throws SQLException {
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Dokter");
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            idKunjungan = hitung("SELECT min(id_kunjungan) FROM kunjungan WHERE status <> 'selesai'");
            resepTerakhir = hitung("SELECT coalesce(max(id_resep), 0) FROM resep");
        }

        @AfterEach
        void pulihkan() throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("DELETE FROM antrean_entri e USING antrean a WHERE e.id_antrean = a.id_antrean "
                        + "AND a.jenis = 'apotek' AND e.id_entri > " + resepTerakhir);
                st.executeUpdate("DELETE FROM item_resep WHERE id_resep > " + resepTerakhir);
                st.executeUpdate("DELETE FROM resep WHERE id_resep > " + resepTerakhir);
                st.executeUpdate("DELETE FROM audit WHERE deskripsi = 'Resep kunjungan " + idKunjungan
                        + " dikirim ke apotek'");
            }
            koneksi.close();
        }

        @Test
        @DisplayName("C23-UT01 muatDataObat normal: seluruh obat data master dimuat beserta stoknya")
        void muatDataObat_normal() throws SQLException {
            List<ObatEntity> obat = manager.muatDataObat();

            assertEquals(hitung("SELECT count(*) FROM obat"), obat.size());
            assertEquals(300, obat.stream().filter(o -> o.getIdObat() == 1).findFirst().orElseThrow().getStok());
        }

        @Test
        @DisplayName("C23-UT02 muatDataObat batas: obat dengan stok 0 tetap dimuat dengan stok 0")
        void muatDataObat_stokNol() {
            ObatEntity kosong = manager.muatDataObat().stream().filter(o -> o.getIdObat() == 12).findFirst()
                    .orElseThrow();

            assertEquals(0, kosong.getStok());
        }

        @Test
        @DisplayName("C23-UT03 tambahItem normal: item dibuat dengan dosis, jumlah, dan aturan pakai")
        void tambahItem_normal() {
            ItemResepEntity item = manager.tambahItem(1, 5, "500 mg", "3 x 1 sesudah makan");

            assertEquals(1, item.getIdObat());
            assertEquals(5, item.getJumlah());
            assertEquals("500 mg", item.getDosis());
            assertEquals("3 x 1 sesudah makan", item.getAturanPakai());
        }

        @Test
        @DisplayName("C23-UT04 tambahItem batas: jumlah 1 diterima")
        void tambahItem_jumlahSatu() {
            assertEquals(1, manager.tambahItem(1, 1, "500 mg", "1 x 1").getJumlah());
        }

        @Test
        @DisplayName("C23-UT05 tambahItem batas: obat stok 0 tetap dapat ditambahkan, stok dinilai saat dikirim")
        void tambahItem_obatStokNol() {
            assertEquals(12, manager.tambahItem(12, 1, "500 mg", "3 x 1").getIdObat());
        }

        @Test
        @DisplayName("C23-UT08 tambahItem negatif: obat di luar data master ditolak")
        void tambahItem_obatTidakAda() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.tambahItem(9999, 1, "1 tablet", "1 x 1"));
            assertEquals("Obat tidak ada pada data master", e.getMessage());
        }

        @Test
        @DisplayName("C23-UT09 validasiStok normal: jumlah di bawah stok dianggap cukup")
        void validasiStok_normal() {
            assertTrue(ResepManager.validasiStok(new ItemResepEntity(0, 1, "500 mg", 10, "3 x 1"),
                    new DataFarmasiBoundary(koneksi)));
        }

        @Test
        @DisplayName("C23-UT10 validasiStok batas: jumlah sama dengan stok cukup, lebih satu tidak cukup")
        void validasiStok_batasStok() {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);

            assertTrue(ResepManager.validasiStok(new ItemResepEntity(0, 1, "500 mg", 300, "3 x 1"), dataFarmasi));
            assertFalse(ResepManager.validasiStok(new ItemResepEntity(0, 1, "500 mg", 301, "3 x 1"), dataFarmasi));
        }

        @Test
        @DisplayName("C23-UT11 validasiStok batas: jumlah pada bets kedaluwarsa tidak dihitung sebagai stok layak")
        void validasiStok_betsKedaluwarsa() throws SQLException {
            try (Connection transaksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
                transaksi.setAutoCommit(false);
                try (Statement st = transaksi.createStatement()) {
                    // Obat 5 menjadi 40: 30 dari bets layak dan 10 dari bets yang kedaluwarsa kemarin.
                    st.executeUpdate("INSERT INTO bets_obat (id_obat, nomor_bets, jumlah, tanggal_kedaluwarsa, "
                            + "id_penerimaan) SELECT 5, 'UJI-C23', 10, CURRENT_DATE - 1, min(id_penerimaan) "
                            + "FROM penerimaan_obat");
                    st.executeUpdate("UPDATE obat SET stok = 40 WHERE id_obat = 5");
                }
                DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(transaksi);

                assertTrue(ResepManager.validasiStok(new ItemResepEntity(0, 5, "5 mg", 30, "1 x 1"), dataFarmasi));
                assertFalse(ResepManager.validasiStok(new ItemResepEntity(0, 5, "5 mg", 31, "1 x 1"), dataFarmasi));
                transaksi.rollback();
            }
        }

        @Test
        @DisplayName("C23-UT12 validasiStok negatif: obat stok 0 dan obat tidak terdaftar tidak cukup")
        void validasiStok_negatif() {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);

            assertFalse(ResepManager.validasiStok(new ItemResepEntity(0, 12, "500 mg", 1, "3 x 1"), dataFarmasi));
            assertFalse(ResepManager.validasiStok(new ItemResepEntity(0, 9999, "1 tablet", 1, "1 x 1"),
                    dataFarmasi));
        }

        @Test
        @DisplayName("C23-UT13 kirimKeApotek normal: resep tersimpan menunggu dan masuk antrean apotek")
        void kirimKeApotek_normal() throws SQLException {
            ResepEntity resep = manager.kirimKeApotek(idKunjungan, List.of(
                    new ItemResepEntity(0, 1, "500 mg", 10, "3 x 1 sesudah makan"),
                    new ItemResepEntity(0, 3, "5 mg", 30, "1 x 1 pagi")));

            assertTrue(resep.getIdResep() > resepTerakhir);
            assertEquals(1, hitung("SELECT count(*) FROM resep WHERE status = 'menunggu' AND id_resep = "
                    + resep.getIdResep()));
            assertEquals(2, hitung("SELECT count(*) FROM item_resep WHERE id_resep = " + resep.getIdResep()));
            assertEquals(1, hitung("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                    + "WHERE a.jenis = 'apotek' AND e.id_entri = " + resep.getIdResep()));
        }

        @Test
        @DisplayName("C23-UT14 kirimKeApotek batas: total dua baris obat yang sama tepat sama dengan stok diterima")
        void kirimKeApotek_totalSamaDenganStok() throws SQLException {
            ResepEntity pas = manager.kirimKeApotek(idKunjungan, List.of(
                    new ItemResepEntity(0, 5, "5 mg", 15, "1 x 1"), new ItemResepEntity(0, 5, "5 mg", 15, "1 x 1 malam")));

            assertEquals(2, hitung("SELECT count(*) FROM item_resep WHERE id_resep = " + pas.getIdResep()));
        }

        @Test
        @DisplayName("C23-UT15 kirimKeApotek batas: total dua baris obat yang sama melebihi stok ditolak")
        void kirimKeApotek_totalMelebihiStok() throws SQLException {
            // Obat 5 (Glibenklamid) stok 30: tiap baris lolos sendiri, tetapi totalnya 32.
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.kirimKeApotek(
                    idKunjungan, List.of(new ItemResepEntity(0, 5, "5 mg", 16, "1 x 1"),
                            new ItemResepEntity(0, 5, "5 mg", 16, "1 x 1 malam"))));

            assertTrue(e.getMessage().contains("Glibenklamid"));
            assertEquals(resepTerakhir, hitung("SELECT coalesce(max(id_resep), 0) FROM resep"));
        }

        @Test
        @DisplayName("C23-UT16 kirimKeApotek negatif: stok kurang ditolak dengan nama obat dan tidak tersimpan")
        void kirimKeApotek_stokKurang() throws SQLException {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.kirimKeApotek(
                    idKunjungan, List.of(new ItemResepEntity(0, 12, "500 mg", 1, "3 x 1"))));

            assertEquals("Stok tidak cukup, perbaiki resep untuk: Asam Mefenamat 500 mg", e.getMessage());
            assertEquals(resepTerakhir, hitung("SELECT coalesce(max(id_resep), 0) FROM resep"));
        }

        @Test
        @DisplayName("C23-UT17 kirimKeApotek negatif: kunjungan yang sudah selesai ditolak")
        void kirimKeApotek_kunjunganSelesai() throws SQLException {
            int selesai = hitung("SELECT min(id_kunjungan) FROM kunjungan WHERE status = 'selesai'");

            assertThrows(IllegalStateException.class,
                    () -> manager.kirimKeApotek(selesai, List.of(new ItemResepEntity(0, 1, "500 mg", 1, "3 x 1"))));
            assertEquals(resepTerakhir, hitung("SELECT coalesce(max(id_resep), 0) FROM resep"));
        }

        @Test
        @DisplayName("C23-UT19 kirimKeApotek negatif: resep kedua saat resep pertama masih menunggu ditolak")
        void kirimKeApotek_resepKedua() throws SQLException {
            manager.kirimKeApotek(idKunjungan, List.of(new ItemResepEntity(0, 1, "500 mg", 10, "3 x 1")));

            assertThrows(IllegalStateException.class, () -> manager.kirimKeApotek(idKunjungan,
                    List.of(new ItemResepEntity(0, 1, "500 mg", 5, "3 x 1"))));
            assertEquals(1, hitung("SELECT count(*) FROM resep WHERE status = 'menunggu' AND id_resep > "
                    + resepTerakhir));
        }

        @Test
        @DisplayName("C23-UT20 kirimKeApotek negatif: kunjungan yang tidak ada ditolak")
        void kirimKeApotek_kunjunganTidakAda() throws SQLException {
            assertThrows(IllegalStateException.class,
                    () -> manager.kirimKeApotek(-1, List.of(new ItemResepEntity(0, 1, "500 mg", 1, "3 x 1"))));
            assertEquals(resepTerakhir, hitung("SELECT coalesce(max(id_resep), 0) FROM resep"));
        }

        @Test
        @DisplayName("C23-UT22 alasanDikembalikan normal: catatan apotek pada resep yang dikembalikan dibaca")
        void alasanDikembalikan_normal() throws SQLException {
            sisipResep("dikembalikan", "Stok amoksisilin rusak");

            assertEquals("Stok amoksisilin rusak", manager.alasanDikembalikan(idKunjungan));
        }

        @Test
        @DisplayName("C23-UT23 alasanDikembalikan batas: dua resep dikembalikan, catatan terakhir yang dibaca")
        void alasanDikembalikan_terakhir() throws SQLException {
            sisipResep("dikembalikan", "Catatan pertama");
            sisipResep("dikembalikan", "Catatan kedua");

            assertEquals("Catatan kedua", manager.alasanDikembalikan(idKunjungan));
        }

        @Test
        @DisplayName("C23-UT24 alasanDikembalikan negatif: tanpa resep dikembalikan mengembalikan null")
        void alasanDikembalikan_tidakAda() throws SQLException {
            sisipResep("menunggu", "Bukan pengembalian");

            assertNull(manager.alasanDikembalikan(idKunjungan));
        }

        @Test
        @DisplayName("C23-UT25 muatDataObat negatif: basis data menolak akses ke tabel obat")
        void muatDataObat_aksesDitolak() throws SQLException {
            // Aplikasi masuk sebagai sehati_app (DPPL aturan 5), jadi hak bacanya dapat dicabut sementara oleh pemilik.
            assumeTrue("sehati_app".equals(PengaturanAplikasi.muat().ambil("DB_USER")));
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("REVOKE SELECT ON obat FROM sehati_app");
            }
            try {
                IllegalStateException e = assertThrows(IllegalStateException.class, manager::muatDataObat);
                // DataFarmasiBoundary mengubah SQLException kueri menjadi IllegalStateException dengan pesannya sendiri.
                assertEquals("Gagal mengambil data obat", e.getMessage());
            } finally {
                try (Statement st = koneksi.createStatement()) {
                    st.executeUpdate("GRANT SELECT ON obat TO sehati_app");
                }
            }
            assertFalse(manager.muatDataObat().isEmpty());
        }

        private void sisipResep(String status, String catatan) throws SQLException {
            try (Statement st = koneksi.createStatement()) {
                st.executeUpdate("INSERT INTO resep (id_kunjungan, waktu_masuk, status, catatan) VALUES ("
                        + idKunjungan + ", now(), '" + status + "', '" + catatan + "')");
            }
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); var rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
