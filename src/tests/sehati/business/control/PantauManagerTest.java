package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

import sehati.FiksturSkrining;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.SesiEntity;
import sehati.business.entity.TindakLanjutEntity;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataPantauBoundary;
import sehati.dataaccess.DataPasienBoundary;

/**
 * Pengujian unit C26 PantauManager (HIUPL Subbab 4.2, C26-UT01 sampai C26-UT25). Logika penyaringan dan pencatatan
 * diuji dengan DataBoundary pengganti. Alur yang membuka koneksi sendiri diuji pada kelas bersarang dengan basis data
 * uji dan memulihkan seed sesudahnya.
 */
@DisplayName("C26 PantauManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class PantauManagerTest {

    private final PantauManager manager = new PantauManager();

    /** Pengganti basis data: menyimpan hasil pemanggilan di memori. */
    private static class PantauPalsu extends DataPantauBoundary {
        final List<DaftarPantauEntity> entri = new ArrayList<>();
        final List<TindakLanjutEntity> upaya = new ArrayList<>();

        PantauPalsu(DaftarPantauEntity... awal) {
            super(null);
            entri.addAll(List.of(awal));
        }

        @Override
        public List<DaftarPantauEntity> ambilEntri() {
            return entri.stream().filter(e -> !"selesai ditindaklanjuti".equals(e.getStatusTindakLanjut())).toList();
        }

        @Override
        public void simpanEntri(DaftarPantauEntity e) {
            // Objek yang sama sudah ada di daftar, perubahan statusnya langsung tercermin.
        }

        @Override
        public void simpanTindakLanjut(int idEntri, TindakLanjutEntity t) {
            upaya.add(t);
        }
    }

    private static class PasienPalsu extends DataPasienBoundary {
        final List<String> catatan = new ArrayList<>();
        int disimpan;

        PasienPalsu() {
            super(null);
        }

        @Override
        public RekamMedisEntity ambilRekamMedis(int idPasien) {
            return new RekamMedisEntity() {
                @Override
                public void tambahEntri(String c) {
                    catatan.add(c);
                }
            };
        }

        @Override
        public void simpanRekamMedis(RekamMedisEntity rm) {
            disimpan++;
        }
    }

    private static DaftarPantauEntity entri(int id, String jenis, LocalDate tanggal) {
        DaftarPantauEntity e = new DaftarPantauEntity(id, 100 + id, jenis, DaftarPantauEntity.STATUS_BELUM);
        e.setTanggalPemicu(tanggal);
        return e;
    }

    private static List<Integer> ids(List<DaftarPantauEntity> daftar) {
        return daftar.stream().map(DaftarPantauEntity::getIdEntri).toList();
    }

    private static void masukSebagai(String peran) {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 2, LocalDateTime.now(), true), peran);
    }

    @AfterEach
    void lepasSesi() {
        SesiAktif.lepas();
    }

    @Test
    @DisplayName("C26-UT01 catat normal: tindak lanjut terhubung menutup entri dan menulis rekam medis")
    void catat_terhubung_menutupEntriDanMenulisRekamMedis() {
        PantauPalsu pantau = new PantauPalsu(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)));
        PasienPalsu pasien = new PasienPalsu();
        manager.catat(pantau, pasien, 1, new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang"));

        assertEquals("selesai ditindaklanjuti", pantau.entri.get(0).getStatusTindakLanjut());
        assertEquals(List.of("Tindak lanjut telepon: terhubung, akan datang"), pasien.catatan);
        assertEquals(1, pasien.disimpan);
        assertEquals(1, pantau.upaya.size());
    }

    @Test
    @DisplayName("C26-UT02 catat normal: tidak terhubung menandai perlu dihubungi ulang tanpa rekam medis")
    void catat_tidakTerhubung_menandaiPerluDihubungiUlangTanpaRekamMedis() {
        PantauPalsu pantau = new PantauPalsu(entri(1, "obesitas", LocalDate.of(2026, 11, 1)));
        PasienPalsu pasien = new PasienPalsu();
        manager.catat(pantau, pasien, 1,
                new TindakLanjutEntity(0, "pesan singkat", "tidak terhubung", "belum diketahui"));

        assertEquals("perlu dihubungi ulang", pantau.entri.get(0).getStatusTindakLanjut());
        assertTrue(pasien.catatan.isEmpty());
        assertEquals(0, pasien.disimpan);
        assertEquals(1, pantau.upaya.size());
    }

    @Test
    @DisplayName("C26-UT03 catat batas: entri yang perlu dihubungi ulang masih boleh ditindaklanjuti lagi")
    void catat_entriYangPerluDihubungiUlangMasihBolehDitindaklanjutiLagi() {
        PantauPalsu pantau = new PantauPalsu(entri(1, "obesitas", LocalDate.of(2026, 11, 1)));
        PasienPalsu pasien = new PasienPalsu();
        manager.catat(pantau, pasien, 1, new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui"));
        manager.catat(pantau, pasien, 1, new TindakLanjutEntity(0, "telepon", "terhubung", "sudah datang"));

        assertEquals("selesai ditindaklanjuti", pantau.entri.get(0).getStatusTindakLanjut());
        assertEquals(2, pantau.upaya.size());
    }

    @Test
    @DisplayName("C26-UT04 catat negatif: entri yang sudah selesai ditolak dan tidak ada upaya tersimpan")
    void catat_entriSudahSelesaiDitolak() {
        DaftarPantauEntity selesai = entri(1, "hipertensi", LocalDate.of(2026, 11, 1));
        selesai.setStatusTindakLanjut(DaftarPantauEntity.STATUS_SELESAI);
        PantauPalsu pantau = new PantauPalsu(selesai);
        assertThrows(IllegalStateException.class, () -> manager.catat(pantau, new PasienPalsu(), 1,
                new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang")));
        assertTrue(pantau.upaya.isEmpty());
    }

    @Test
    @DisplayName("C26-UT05 catatTindakLanjut negatif: isian di luar pilihan atau kosong ditolak sebelum basis data")
    void catatTindakLanjut_isianTidakDipilihDitolakSebelumMenyentuhBasisData() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.catatTindakLanjut(1, new TindakLanjutEntity(0, "merpati", "terhubung", "akan datang")));
        assertThrows(IllegalArgumentException.class, () -> manager.catatTindakLanjut(1, null));
    }

    @Test
    @DisplayName("C26-UT06 saring normal: status selesai hanya mengembalikan entri selesai")
    void saring_statusSelesaiHanyaEntriSelesai() {
        DaftarPantauEntity selesai = entri(1, "hipertensi", LocalDate.of(2026, 11, 1));
        selesai.perbaruiStatus(DaftarPantauEntity.STATUS_SELESAI);
        List<DaftarPantauEntity> semua = List.of(selesai, entri(2, "hipertensi", LocalDate.of(2026, 11, 2)));
        assertEquals(List.of(1), ids(PantauManager.saring(semua, null, null, null, DaftarPantauEntity.STATUS_SELESAI)));
    }

    @Test
    @DisplayName("C26-UT07 saring normal: penyaring jenis risiko hanya mengembalikan jenis itu")
    void saring_menurutJenisRisiko() {
        List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)),
                entri(2, "obesitas", LocalDate.of(2026, 11, 2)));
        assertEquals(List.of(2), ids(PantauManager.saring(semua, "obesitas", null, null, null)));
    }

    @Test
    @DisplayName("C26-UT08 saring batas: rentang tanggal pemicu mengikutkan tanggal mulai dan tanggal akhir")
    void saring_rentangTanggalMengikutkanBatas() {
        List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 10, 31)),
                entri(2, "hipertensi", LocalDate.of(2026, 11, 1)),
                entri(3, "hipertensi", LocalDate.of(2026, 11, 30)),
                entri(4, "hipertensi", LocalDate.of(2026, 12, 1)));
        assertEquals(List.of(2, 3),
                ids(PantauManager.saring(semua, null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null)));
    }

    @Test
    @DisplayName("C26-UT09 saring normal: tanpa penyaring semua entri tampil dengan entri berisiko lebih dulu")
    void saring_tanpaPenyaringMengembalikanSemuaDenganBerisikoLebihDulu() {
        List<DaftarPantauEntity> semua = List.of(entri(1, "kunjungan ulang", LocalDate.of(2026, 11, 1)),
                entri(2, "hipertensi", LocalDate.of(2026, 11, 5)),
                entri(3, "obesitas", LocalDate.of(2026, 11, 2)));
        assertEquals(List.of(3, 2, 1), ids(PantauManager.saring(semua, null, null, null, null)));
    }

    @Test
    @DisplayName("C26-UT10 saring negatif: tidak ada yang cocok mengembalikan daftar kosong")
    void saring_hasilKosongBilaTidakAdaYangCocok() {
        List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)));
        assertTrue(PantauManager.saring(semua, "obesitas", null, null, null).isEmpty());
    }

    @Test
    @DisplayName("C26-UT11 saring normal: penyaring status tindak lanjut dapat digabung dengan jenis risiko")
    void saring_menurutStatusTindakLanjut() {
        DaftarPantauEntity ulang = entri(2, "hipertensi", LocalDate.of(2026, 11, 2));
        ulang.setStatusTindakLanjut(DaftarPantauEntity.STATUS_ULANG);
        List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)), ulang);
        assertEquals(List.of(2), ids(PantauManager.saring(semua, null, null, null, DaftarPantauEntity.STATUS_ULANG)));
        assertEquals(List.of(1), ids(PantauManager.saring(semua, "hipertensi", null, null,
                DaftarPantauEntity.STATUS_BELUM)));
    }

    @Test
    @DisplayName("C26-UT12 saring batas: entri tanpa tanggal pemicu tidak lolos penyaring tanggal")
    void saring_tanpaTanggalPemicuTidakLolosPenyaringTanggal() {
        DaftarPantauEntity tanpaTanggal = entri(1, "hipertensi", null);

        assertTrue(PantauManager.saring(List.of(tanpaTanggal), null, LocalDate.of(2026, 11, 1), null, null)
                .isEmpty());
        assertEquals(List.of(1), ids(PantauManager.saring(List.of(tanpaTanggal), null, null, null, null)));
    }

    @Test
    @DisplayName("C26-UT13 saringDaftar negatif: tanggal mulai setelah tanggal akhir ditolak")
    void saringDaftar_tanggalMulaiSetelahAkhirDitolak() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> manager.saringDaftar(null, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 1), null));
        assertEquals("Tanggal mulai tidak boleh setelah tanggal akhir", e.getMessage());
    }

    @Test
    @DisplayName("C26-UT14 catatTindakLanjut negatif: tanpa sesi masuk ditolak")
    void catatTindakLanjut_tanpaSesi() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.catatTindakLanjut(2,
                new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang")));
        assertEquals("Sesi sudah berakhir, silakan masuk kembali", e.getMessage());
    }

    @Test
    @DisplayName("C26-UT15 catatTindakLanjut negatif: peran Petugas Farmasi tidak berwenang")
    void catatTindakLanjut_peranFarmasiDitolak() {
        masukSebagai("Petugas Farmasi");

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.catatTindakLanjut(2,
                new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang")));
        assertEquals("Peran Petugas Farmasi tidak berwenang menjalankan tindakan ini", e.getMessage());
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

        private TindakLanjutEntity terhubung() {
            return new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang");
        }

        @Test
        @DisplayName("C26-UT16 saringDaftar normal: tanpa penyaring empat entri tampil dengan nama pasien terisi")
        void saringDaftar_normal() {
            List<DaftarPantauEntity> hasil = manager.saringDaftar(null, null, null, null);

            assertEquals(4, hasil.size());
            assertTrue(hasil.stream().allMatch(e -> e.getNamaPasien() != null && !e.getNamaPasien().isBlank()));
            assertEquals("kunjungan ulang", hasil.get(hasil.size() - 1).getJenisRisiko());
        }

        @Test
        @DisplayName("C26-UT17 saringDaftar normal: penyaring jenis hipertensi mengembalikan satu entri")
        void saringDaftar_menurutJenis() {
            List<DaftarPantauEntity> hasil = manager.saringDaftar("hipertensi", null, null, null);

            assertEquals(List.of(2), ids(hasil));
        }

        @Test
        @DisplayName("C26-UT18 saringDaftar batas: rentang tepat hari ini hanya memuat entri berisiko yang terbit hari ini")
        void saringDaftar_rentangHariIni() {
            LocalDate hariIni = LocalDate.now();

            List<DaftarPantauEntity> hasil = manager.saringDaftar(null, hariIni, hariIni, null);

            assertEquals(List.of(1, 2), ids(hasil).stream().sorted().toList());
        }

        @Test
        @DisplayName("C26-UT19 saringDaftar negatif: status selesai tidak mengembalikan apa pun pada seed")
        void saringDaftar_selesaiKosong() {
            assertTrue(manager.saringDaftar(null, null, null, DaftarPantauEntity.STATUS_SELESAI).isEmpty());
        }

        @Test
        @DisplayName("C26-UT20 catatTindakLanjut normal: terhubung menutup entri, mencatat rekam medis, dan diaudit")
        void catatTindakLanjut_terhubung() throws SQLException {
            long catatanSebelum = FiksturSkrining.angka("SELECT count(*) FROM catatan_rekam_medis");

            manager.catatTindakLanjut(2, terhubung());

            assertEquals(DaftarPantauEntity.STATUS_SELESAI,
                    FiksturSkrining.teks("SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_entri = 2"));
            assertEquals(1, FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 2"));
            assertEquals(catatanSebelum + 1, FiksturSkrining.angka("SELECT count(*) FROM catatan_rekam_medis"));
            assertEquals(1, FiksturSkrining.angka("SELECT count(*) FROM audit "
                    + "WHERE deskripsi = 'Tindak lanjut daftar pantau 2: terhubung'"));
        }

        @Test
        @DisplayName("C26-UT21 catatTindakLanjut normal: tidak terhubung menandai perlu dihubungi ulang tanpa rekam medis")
        void catatTindakLanjut_tidakTerhubung() throws SQLException {
            long catatanSebelum = FiksturSkrining.angka("SELECT count(*) FROM catatan_rekam_medis");

            manager.catatTindakLanjut(1, new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui"));

            assertEquals(DaftarPantauEntity.STATUS_ULANG,
                    FiksturSkrining.teks("SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_entri = 1"));
            assertEquals(catatanSebelum, FiksturSkrining.angka("SELECT count(*) FROM catatan_rekam_medis"));
        }

        @Test
        @DisplayName("C26-UT22 catatTindakLanjut batas: entri yang perlu dihubungi ulang dapat ditutup pada upaya berikutnya")
        void catatTindakLanjut_ulangLaluSelesai() throws SQLException {
            long sebelum = FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 3");

            manager.catatTindakLanjut(3, terhubung());

            assertEquals(DaftarPantauEntity.STATUS_SELESAI,
                    FiksturSkrining.teks("SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_entri = 3"));
            assertEquals(sebelum + 1,
                    FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 3"));
        }

        @Test
        @DisplayName("C26-UT23 catatTindakLanjut negatif: entri yang tidak ada ditolak tanpa mengubah data")
        void catatTindakLanjut_entriTidakAda() throws SQLException {
            long sebelum = FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut");

            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> manager.catatTindakLanjut(-1, terhubung()));

            assertEquals("Entri tidak ditemukan atau sudah selesai ditindaklanjuti", e.getMessage());
            assertEquals(sebelum, FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut"));
        }

        @Test
        @DisplayName("C26-UT24 catatTindakLanjut negatif: entri yang sudah selesai tidak dapat ditindaklanjuti lagi")
        void catatTindakLanjut_sudahSelesai() throws SQLException {
            manager.catatTindakLanjut(2, terhubung());

            assertThrows(IllegalStateException.class, () -> manager.catatTindakLanjut(2, terhubung()));

            assertEquals(1, FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 2"));
        }

        @Test
        @DisplayName("C26-UT25 saringDaftar batas: entri yang baru ditutup tampil pada penyaring status selesai")
        void saringDaftar_selesaiSetelahDitutup() {
            manager.catatTindakLanjut(2, terhubung());

            List<DaftarPantauEntity> hasil = manager.saringDaftar(null, null, null, DaftarPantauEntity.STATUS_SELESAI);

            assertEquals(List.of(2), ids(hasil));
            assertNotNull(hasil.get(0).getNamaPasien());
            assertFalse(manager.saringDaftar(null, null, null, null).stream().anyMatch(e -> e.getIdEntri() == 2));
        }
    }
}
