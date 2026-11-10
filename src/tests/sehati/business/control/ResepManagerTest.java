package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ResepEntity;
import sehati.dataaccess.DataFarmasiBoundary;

class ResepManagerTest {

    private final ResepManager manager = new ResepManager();

    @Test
    void tambahItem_menolakJumlahNol() {
        assertThrows(IllegalArgumentException.class, () -> manager.tambahItem(1, 0, "500 mg", "3 x 1"));
    }

    @Test
    void kirimKeApotek_menolakResepKosong() {
        assertThrows(IllegalArgumentException.class, () -> manager.kirimKeApotek(1, List.of()));
    }

    /**
     * Memakai seed: obat 1 (Parasetamol) stok 300 dan obat 12 (Asam Mefenamat) stok 0. Variabel DB_* harus menunjuk
     * basis data yang sama dengan DB_UJI_URL. Resep yang dibuat dan isi antrean apotek dipulihkan setelah setiap uji.
     */
    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private int idKunjungan;
        private int resepTerakhir;

        @BeforeEach
        void buka() throws SQLException {
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
            }
            koneksi.close();
        }

        @Test
        void validasiStok_membandingkanStokTerkini() {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);
            assertTrue(ResepManager.validasiStok(new ItemResepEntity(0, 1, "500 mg", 300, "3 x 1"), dataFarmasi));
            assertFalse(ResepManager.validasiStok(new ItemResepEntity(0, 1, "500 mg", 301, "3 x 1"), dataFarmasi));
            assertFalse(ResepManager.validasiStok(new ItemResepEntity(0, 12, "500 mg", 1, "3 x 1"), dataFarmasi));
        }

        @Test
        void kirimKeApotek_kunjunganSelesaiDitolak() throws SQLException {
            int selesai = hitung("SELECT min(id_kunjungan) FROM kunjungan WHERE status = 'selesai'");
            assertThrows(IllegalStateException.class,
                    () -> manager.kirimKeApotek(selesai, List.of(manager.tambahItem(1, 1, "500 mg", "3 x 1"))));
            assertEquals(resepTerakhir, hitung("SELECT coalesce(max(id_resep), 0) FROM resep"));
        }

        @Test
        void tambahItem_menolakObatDiLuarDataMaster() {
            assertThrows(IllegalArgumentException.class, () -> manager.tambahItem(9999, 1, "1 tablet", "1 x 1"));
            assertEquals(5, manager.tambahItem(1, 5, "500 mg", "3 x 1").getJumlah());
        }

        @Test
        void kirimKeApotek_menyimpanResepDanMasukAntreanApotek() throws SQLException {
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
        void kirimKeApotek_resepKeduaSaatMasihMenungguDitolak() throws SQLException {
            manager.kirimKeApotek(idKunjungan, List.of(new ItemResepEntity(0, 1, "500 mg", 10, "3 x 1")));

            assertThrows(IllegalStateException.class, () -> manager.kirimKeApotek(idKunjungan,
                    List.of(new ItemResepEntity(0, 1, "500 mg", 5, "3 x 1"))));
            assertEquals(1, hitung("SELECT count(*) FROM resep WHERE status = 'menunggu' AND id_resep > "
                    + resepTerakhir));
        }

        @Test
        void kirimKeApotek_stokKurangDitolakTanpaMenyimpan() throws SQLException {
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.kirimKeApotek(
                    idKunjungan, List.of(new ItemResepEntity(0, 12, "500 mg", 1, "3 x 1"))));

            assertTrue(e.getMessage().contains("Asam Mefenamat"));
            assertEquals(resepTerakhir, hitung("SELECT coalesce(max(id_resep), 0) FROM resep"));
        }

        @Test
        void kirimKeApotek_barisObatSamaDihitungTotalnya() throws SQLException {
            // Obat 5 (Glibenklamid) stok 30: tiap baris lolos sendiri, tetapi totalnya 32.
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.kirimKeApotek(
                    idKunjungan, List.of(new ItemResepEntity(0, 5, "5 mg", 16, "1 x 1"),
                            new ItemResepEntity(0, 5, "5 mg", 16, "1 x 1 malam"))));

            assertTrue(e.getMessage().contains("Glibenklamid"));
            assertEquals(resepTerakhir, hitung("SELECT coalesce(max(id_resep), 0) FROM resep"));

            ResepEntity pas = manager.kirimKeApotek(idKunjungan, List.of(new ItemResepEntity(0, 5, "5 mg", 15, "1 x 1"),
                    new ItemResepEntity(0, 5, "5 mg", 15, "1 x 1 malam")));
            assertEquals(2, hitung("SELECT count(*) FROM item_resep WHERE id_resep = " + pas.getIdResep()));
        }

        private int hitung(String sql) throws SQLException {
            try (Statement st = koneksi.createStatement(); var rs = st.executeQuery(sql)) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
