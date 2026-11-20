package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.FiksturSkrining;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.SesiEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.common.SesiAktif;

/**
 * Pengujian unit C20 SkriningManager (HIUPL Subbab 4.2, C20-UT01 sampai C20-UT32). Test tanpa basis data menguji
 * validasi rentang dan pemeriksaan peran. Test pada kelas bersarang memakai basis data uji dan memulihkan seed
 * sesudahnya lewat {@link FiksturSkrining}.
 */
@DisplayName("C20 SkriningManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SkriningManagerTest {

    private final SkriningManager manager = new SkriningManager();

    private static TandaVitalEntity normal() {
        return new TandaVitalEntity(120, 80, 65, 170, 36.5, 72, 100);
    }

    private static void masukSebagai(String peran) {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 2, LocalDateTime.now(), true), peran);
    }

    @AfterEach
    void lepasSesi() {
        SesiAktif.lepas();
    }

    @Test
    @DisplayName("C20-UT01 validasiRentang normal: seluruh nilai dalam rentang tidak menghasilkan galat")
    void validasiRentang_nilaiNormalTidakAdaGalat() {
        assertTrue(manager.validasiRentang(normal()).isEmpty());
    }

    @Test
    @DisplayName("C20-UT02 validasiRentang batas: nilai tepat di batas rentang diterima dan satu di luarnya ditolak")
    void validasiRentang_batasRentangDiterima() {
        TandaVitalEntity tv = new TandaVitalEntity(300, 200, 350, 250, 45, 250, 1000);
        assertTrue(manager.validasiRentang(tv).isEmpty());
        tv.setNadi(251);
        assertEquals(List.of("nadi"), manager.validasiRentang(tv));
    }

    @Test
    @DisplayName("C20-UT03 validasiRentang negatif: suhu 72 di luar rentang ditolak")
    void validasiRentang_suhuDiLuarRentangDitolak() {
        TandaVitalEntity tv = normal();
        tv.setSuhu(72);
        assertEquals(List.of("suhu"), manager.validasiRentang(tv));
    }

    @Test
    @DisplayName("C20-UT04 validasiRentang negatif: sistolik 400 di luar rentang ditolak")
    void validasiRentang_sistolikDiLuarRentangDitolak() {
        TandaVitalEntity tv = normal();
        tv.setSistolik(400);
        assertEquals(List.of("sistolik"), manager.validasiRentang(tv));
    }

    @Test
    @DisplayName("C20-UT05 validasiRentang batas: gula darah boleh kosong tetapi tidak boleh di luar rentang")
    void validasiRentang_gulaDarahBolehKosongTetapiTidakBolehDiLuarRentang() {
        TandaVitalEntity tv = normal();
        tv.setGulaDarah(null);
        assertTrue(manager.validasiRentang(tv).isEmpty());
        tv.setGulaDarah(5);
        assertEquals(List.of("gulaDarah"), manager.validasiRentang(tv));
    }

    @Test
    @DisplayName("C20-UT06 validasiRentang negatif: diastolik tidak lebih rendah dari sistolik ditolak")
    void validasiRentang_diastolikHarusLebihRendahDariSistolik() {
        TandaVitalEntity tv = normal();
        tv.setDiastolik(120);
        assertEquals(List.of("diastolik"), manager.validasiRentang(tv));
    }

    @Test
    @DisplayName("C20-UT07 validasiRentang negatif: seluruh kolom bermasalah dilaporkan sekaligus")
    void validasiRentang_seluruhKolomBermasalahDilaporkanSekaligus() {
        TandaVitalEntity tv = new TandaVitalEntity(0, 0, 0, 0, 0, 0, null);
        assertEquals(List.of("sistolik", "diastolik", "beratBadan", "tinggiBadan", "suhu", "nadi"),
                manager.validasiRentang(tv));
    }

    @Test
    @DisplayName("C20-UT08 simpanSkrining negatif: nilai di luar rentang dikembalikan tanpa menyentuh basis data")
    void simpanSkrining_diLuarRentang() {
        TandaVitalEntity tv = normal();
        tv.setSuhu(72);

        assertEquals(List.of("suhu"), manager.simpanSkrining(2, "Demam", tv));
    }

    @Test
    @DisplayName("C20-UT09 simpanSkrining negatif: tanpa sesi masuk ditolak")
    void simpanSkrining_tanpaSesi() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> manager.simpanSkrining(2, "Batuk", normal()));
        assertEquals("Sesi sudah berakhir, silakan masuk kembali", e.getMessage());
    }

    @Test
    @DisplayName("C20-UT10 simpanSkrining negatif: peran Petugas Farmasi tidak berwenang")
    void simpanSkrining_peranFarmasiDitolak() {
        masukSebagai("Petugas Farmasi");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> manager.simpanSkrining(2, "Batuk", normal()));
        assertEquals("Peran Petugas Farmasi tidak berwenang menjalankan tindakan ini", e.getMessage());
    }

    @Test
    @DisplayName("C20-UT11 panggilPasien negatif: tanpa sesi masuk ditolak")
    void panggilPasien_tanpaSesi() {
        IllegalStateException e = assertThrows(IllegalStateException.class, manager::panggilPasien);
        assertEquals("Sesi sudah berakhir, silakan masuk kembali", e.getMessage());
    }

    @Test
    @DisplayName("C20-UT12 lewatiPasien negatif: peran Petugas Administrasi tidak berwenang")
    void lewatiPasien_peranAdministrasiDitolak() {
        masukSebagai("Petugas Administrasi");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.lewatiPasien(3));
        assertEquals("Peran Petugas Administrasi tidak berwenang menjalankan tindakan ini", e.getMessage());
    }

    @Test
    @DisplayName("C20-UT13 galatTeknis negatif: pesan untuk pengguna tidak membocorkan detail basis data")
    void galatTeknis_pesanUmum() {
        SQLException sebab = new SQLException("connection refused to 10.0.0.5");

        IllegalStateException e = SkriningManager.galatTeknis("Antrean skrining gagal dimuat", sebab);

        assertEquals("Antrean skrining gagal dimuat. Hubungi administrator bila berulang.", e.getMessage());
        assertFalse(e.getMessage().contains("10.0.0.5"));
        assertEquals(sebab, e.getCause());
    }

    @Nested
    @DisplayName("dengan basis data uji")
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    class DenganBasisData {

        private FiksturSkrining fiksur;

        @BeforeEach
        void siapkan() throws SQLException {
            fiksur = new FiksturSkrining();
            masukSebagai("Perawat");
        }

        @AfterEach
        void pulihkan() throws SQLException {
            fiksur.close();
        }

        private List<Integer> idAntrean() {
            return manager.muatAntrean().stream().map(KunjunganEntity::getIdKunjungan).toList();
        }

        @Test
        @DisplayName("C20-UT14 muatAntrean normal: tiga kunjungan seed hari ini tampil menurut urutan antrean")
        void muatAntrean_normal() {
            assertEquals(List.of(3, 2, 11), idAntrean());
        }

        @Test
        @DisplayName("C20-UT15 muatAntrean batas: kunjungan dari hari sebelumnya tidak tampil")
        void muatAntrean_kunjunganKemarinTidakTampil() throws SQLException {
            FiksturSkrining.jalankan("UPDATE kunjungan SET tanggal = CURRENT_DATE - 1 WHERE id_kunjungan = 3");
            try {
                assertEquals(List.of(2, 11), idAntrean());
            } finally {
                FiksturSkrining.jalankan("UPDATE kunjungan SET tanggal = CURRENT_DATE WHERE id_kunjungan = 3");
            }
        }

        @Test
        @DisplayName("C20-UT16 muatAntrean batas: kunjungan selesai diperiksa tidak tampil")
        void muatAntrean_statusSelesaiTidakTampil() throws SQLException {
            FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'selesai diperiksa' WHERE id_kunjungan = 2");

            assertEquals(List.of(3, 11), idAntrean());
        }

        @Test
        @DisplayName("C20-UT17 muatAntrean negatif: antrean kosong mengembalikan daftar kosong")
        void muatAntrean_kosong() throws SQLException {
            FiksturSkrining.jalankan("DELETE FROM antrean_entri");

            assertTrue(idAntrean().isEmpty());
        }

        @Test
        @DisplayName("C20-UT18 panggilPasien normal: kunjungan terdepan menjadi sedang diskrining")
        void panggilPasien_normal() throws SQLException {
            KunjunganEntity k = manager.panggilPasien();

            assertEquals(3, k.getIdKunjungan());
            assertEquals("sedang diskrining", k.getStatus());
            assertEquals("sedang diskrining", FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 3"));
        }

        @Test
        @DisplayName("C20-UT19 panggilPasien batas: pasien tidak hadir dipanggil bila tidak ada yang menunggu")
        void panggilPasien_tidakHadirDipanggilLagi() throws SQLException {
            FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'tidak hadir' WHERE id_kunjungan = 3");
            FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'selesai diperiksa' WHERE id_kunjungan IN (2, 11)");

            KunjunganEntity k = manager.panggilPasien();

            assertEquals(3, k.getIdKunjungan());
            assertEquals("sedang diskrining", k.getStatus());
        }

        @Test
        @DisplayName("C20-UT20 panggilPasien negatif: tanpa pasien menunggu mengembalikan null")
        void panggilPasien_antreanKosong() throws SQLException {
            FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'selesai diperiksa' WHERE id_kunjungan IN (2, 3, 11)");

            assertNull(manager.panggilPasien());
        }

        @Test
        @DisplayName("C20-UT21 lewatiPasien normal: pasien dilewati pindah ke belakang dan pasien berikutnya dikembalikan")
        void lewatiPasien_normal() throws SQLException {
            manager.panggilPasien();

            KunjunganEntity berikutnya = manager.lewatiPasien(3);

            assertEquals(2, berikutnya.getIdKunjungan());
            assertEquals("tidak hadir", FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 3"));
            assertEquals(List.of(2, 11, 3), idAntrean());
        }

        @Test
        @DisplayName("C20-UT22 lewatiPasien batas: pasien yang dilewati dapat dipanggil lagi sesudah yang menunggu")
        void lewatiPasien_dapatDipanggilLagi() throws SQLException {
            manager.panggilPasien();
            manager.lewatiPasien(3);
            FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'selesai diperiksa' WHERE id_kunjungan IN (2, 11)");

            KunjunganEntity k = manager.panggilPasien();

            assertEquals(3, k.getIdKunjungan());
        }

        @Test
        @DisplayName("C20-UT23 lewatiPasien negatif: pasien yang belum dipanggil ditolak dan antrean tidak berubah")
        void lewatiPasien_belumDipanggil() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.lewatiPasien(2));

            assertEquals("Hanya pasien yang sedang diskrining yang dapat dilewati", e.getMessage());
            assertEquals(List.of(3, 2, 11), idAntrean());
        }

        @Test
        @DisplayName("C20-UT24 lewatiPasien negatif: kunjungan yang tidak ada ditolak")
        void lewatiPasien_tidakAda() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.lewatiPasien(-1));

            assertEquals("Kunjungan tidak ditemukan: -1", e.getMessage());
        }

        @Test
        @DisplayName("C20-UT25 simpanSkrining normal: tersimpan, status siap diperiksa, keluar dari antrean, dan diaudit")
        void simpanSkrining_normal() throws SQLException {
            List<String> galat = manager.simpanSkrining(2, "Pusing ringan", normal());

            assertTrue(galat.isEmpty());
            assertEquals(1, FiksturSkrining.angka("SELECT count(*) FROM skrining WHERE id_kunjungan = 2"));
            assertEquals("siap diperiksa dokter",
                    FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 2"));
            assertEquals(0, FiksturSkrining.angka("SELECT count(*) FROM penanda_risiko WHERE id_kunjungan = 2"));
            assertEquals(List.of(3, 11), idAntrean());
            assertEquals(1, FiksturSkrining.angka(
                    "SELECT count(*) FROM audit WHERE deskripsi = 'Skrining kunjungan 2 disimpan'"));
        }

        @Test
        @DisplayName("C20-UT26 simpanSkrining batas: sistolik tepat 140 menerbitkan penanda hipertensi dan entri pantau")
        void simpanSkrining_sistolikTepatDiAmbang() throws SQLException {
            TandaVitalEntity tv = new TandaVitalEntity(140, 90, 65, 170, 36.5, 72, 100);

            assertTrue(manager.simpanSkrining(11, "Sakit kepala", tv).isEmpty());

            assertEquals("hipertensi", FiksturSkrining.teks(
                    "SELECT jenis_risiko FROM penanda_risiko WHERE id_kunjungan = 11"));
            assertEquals(1, FiksturSkrining.angka(
                    "SELECT count(*) FROM daftar_pantau WHERE id_pasien = 5 AND jenis_risiko = 'hipertensi'"));
        }

        @Test
        @DisplayName("C20-UT27 simpanSkrining batas: tiga jenis risiko terbit sekaligus dalam satu transaksi")
        void simpanSkrining_tigaRisikoSekaligus() throws SQLException {
            TandaVitalEntity tv = new TandaVitalEntity(150, 95, 100, 165, 36.8, 80, 250);

            assertTrue(manager.simpanSkrining(2, "Lemas", tv).isEmpty());

            assertEquals(3, FiksturSkrining.angka("SELECT count(*) FROM penanda_risiko WHERE id_kunjungan = 2"));
        }

        @Test
        @DisplayName("C20-UT28 simpanSkrining negatif: kunjungan yang tidak ada ditolak dan tidak ada data tersimpan")
        void simpanSkrining_kunjunganTidakAda() throws SQLException {
            long sebelum = FiksturSkrining.angka("SELECT count(*) FROM skrining");

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.simpanSkrining(-1, "Batuk", normal()));

            assertEquals("Kunjungan tidak ditemukan: -1", e.getMessage());
            assertEquals(sebelum, FiksturSkrining.angka("SELECT count(*) FROM skrining"));
        }

        @Test
        @DisplayName("C20-UT29 simpanSkrining negatif: skrining ganda gagal dan seluruh transaksi dibatalkan")
        void simpanSkrining_gandaDibatalkan() throws SQLException {
            long sebelum = FiksturSkrining.angka("SELECT count(*) FROM audit");
            TandaVitalEntity tv = new TandaVitalEntity(150, 95, 100, 165, 36.8, 80, 250);

            assertThrows(IllegalArgumentException.class, () -> manager.simpanSkrining(9, "Ulang", tv));

            assertEquals(1, FiksturSkrining.angka("SELECT count(*) FROM skrining WHERE id_kunjungan = 9"));
            assertEquals(1, FiksturSkrining.angka("SELECT count(*) FROM penanda_risiko WHERE id_kunjungan = 9"));
            assertEquals(sebelum, FiksturSkrining.angka("SELECT count(*) FROM audit"));
            assertEquals("siap diperiksa dokter",
                    FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 9"));
        }

        @Test
        @DisplayName("C20-UT30 labelPasien normal: label memuat nama, nomor RM, poli, dan nomor antrean")
        void labelPasien_normal() throws SQLException {
            List<KunjunganEntity> antrean = manager.muatAntrean();

            Map<Integer, String> label = manager.labelPasien(antrean);

            String nama = FiksturSkrining.teks("SELECT nama FROM pasien WHERE id_pasien = 3");
            assertTrue(label.get(3).startsWith(nama + "  /  RM-"));
            assertTrue(label.get(3).endsWith("/  Antrean 3"));
            assertEquals(3, label.size());
        }

        @Test
        @DisplayName("C20-UT31 labelPasien batas: daftar kosong menghasilkan peta kosong")
        void labelPasien_daftarKosong() {
            assertTrue(manager.labelPasien(List.of()).isEmpty());
        }

        @Test
        @DisplayName("C20-UT32 panggilPasien normal: pemanggilan berurutan memanggil pasien berbeda")
        void panggilPasien_berurutan() {
            KunjunganEntity pertama = manager.panggilPasien();
            KunjunganEntity kedua = manager.panggilPasien();

            assertNotNull(kedua);
            assertFalse(pertama.getIdKunjungan() == kedua.getIdKunjungan());
        }
    }
}
