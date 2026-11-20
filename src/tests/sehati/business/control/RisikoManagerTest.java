package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.FiksturSkrining;
import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PenandaRisikoEntity;
import sehati.business.entity.TandaVitalEntity;

/**
 * Pengujian unit C21 RisikoManager (HIUPL Subbab 4.2, C21-UT01 sampai C21-UT18). Penentuan jenis risiko diuji tanpa
 * basis data. Penerbitan penanda dan pemuatan daftar pantau memakai basis data uji dan memulihkan seed sesudahnya.
 */
@DisplayName("C21 RisikoManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class RisikoManagerTest {

    private static final List<AmbangRisikoEntity> AMBANG = List.of(
            new AmbangRisikoEntity(1, "hipertensi", 140),
            new AmbangRisikoEntity(2, "obesitas", 30),
            new AmbangRisikoEntity(3, "indikasi diabetes", 200));

    private static TandaVitalEntity tv(int sistolik, double berat, Integer gula) {
        return new TandaVitalEntity(sistolik, 80, berat, 170, 36.5, 72, gula);
    }

    @Test
    @DisplayName("C21-UT01 jenisRisikoTerpenuhi normal: nilai normal tidak menghasilkan risiko")
    void tanpaRisiko_tidakAdaPenanda() {
        assertTrue(RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 65, 100)).isEmpty());
    }

    @Test
    @DisplayName("C21-UT02 jenisRisikoTerpenuhi batas: sistolik tepat 140 dianggap hipertensi")
    void sistolikTepatDiBatas_dianggapHipertensi() {
        assertEquals(List.of("hipertensi"), RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(140, 65, null)));
    }

    @Test
    @DisplayName("C21-UT03 jenisRisikoTerpenuhi normal: IMT melampaui 30 dianggap obesitas")
    void imtMelampauiBatas_obesitas() {
        // 90 kg pada 170 cm: IMT sekitar 31,1.
        assertEquals(List.of("obesitas"), RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 90, null)));
    }

    @Test
    @DisplayName("C21-UT04 jenisRisikoTerpenuhi batas: gula darah kosong tidak pernah melampaui, 250 melampaui")
    void gulaDarahKosong_tidakPernahMelampaui() {
        assertTrue(RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 65, null)).isEmpty());
        assertEquals(List.of("indikasi diabetes"),
                RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 65, 250)));
    }

    @Test
    @DisplayName("C21-UT05 jenisRisikoTerpenuhi normal: tiga kunjungan beruntun di atas ambang menandai meski hari ini turun")
    void tigaKunjunganBeruntunDiAtasAmbang_menandaiMeskipunHariIniTurun() {
        List<TandaVitalEntity> riwayat = List.of(tv(150, 65, null), tv(145, 65, null), tv(160, 65, null));
        assertEquals(List.of("hipertensi"), RisikoManager.jenisRisikoTerpenuhi(AMBANG, riwayat, tv(120, 65, null)));
    }

    @Test
    @DisplayName("C21-UT06 jenisRisikoTerpenuhi batas: hanya dua kunjungan di atas ambang tidak cukup")
    void duaKunjunganDiAtasAmbangSaja_tidakCukup() {
        List<TandaVitalEntity> riwayat = List.of(tv(150, 65, null), tv(145, 65, null));
        assertTrue(RisikoManager.jenisRisikoTerpenuhi(AMBANG, riwayat, tv(120, 65, null)).isEmpty());
    }

    @Test
    @DisplayName("C21-UT07 jenisRisikoTerpenuhi negatif: satu dari tiga kunjungan di bawah ambang memutus rentetan")
    void salahSatuDariTigaKunjunganDiBawahAmbang_memutusRentetan() {
        List<TandaVitalEntity> riwayat = List.of(tv(150, 65, null), tv(130, 65, null), tv(160, 65, null));
        assertTrue(RisikoManager.jenisRisikoTerpenuhi(AMBANG, riwayat, tv(120, 65, null)).isEmpty());
    }

    @Test
    @DisplayName("C21-UT08 jenisRisikoTerpenuhi normal: beberapa risiko sekaligus semuanya dilaporkan")
    void beberapaRisikoSekaligus_semuanyaDilaporkan() {
        assertEquals(List.of("hipertensi", "obesitas", "indikasi diabetes"),
                RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(160, 95, 300)));
    }

    @Test
    @DisplayName("C21-UT09 sudahDipantau negatif: entri terbuka hanya cocok untuk pasien dan jenis risiko yang sama")
    void entriTerbukaUntukPasienDanRisikoSama_tidakDimasukkanLagi() {
        List<DaftarPantauEntity> terbuka = List.of(
                new DaftarPantauEntity(1, 7, "hipertensi", DaftarPantauEntity.STATUS_ULANG));
        assertTrue(RisikoManager.sudahDipantau(terbuka, 7, "hipertensi"));
        assertFalse(RisikoManager.sudahDipantau(terbuka, 7, "obesitas"));
        assertFalse(RisikoManager.sudahDipantau(terbuka, 8, "hipertensi"));
    }

    @Test
    @DisplayName("C21-UT10 nilaiUkur negatif: jenis risiko yang tidak dikenal tidak memiliki nilai")
    void nilaiUkur_jenisTidakDikenal() {
        assertEquals(null, RisikoManager.nilaiUkur("anemia", tv(120, 65, 100)));
    }

    @Nested
    @DisplayName("dengan basis data uji")
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    class DenganBasisData {

        private final RisikoManager manager = new RisikoManager();
        private FiksturSkrining fiksur;

        @BeforeEach
        void siapkan() throws SQLException {
            fiksur = new FiksturSkrining();
        }

        @AfterEach
        void pulihkan() throws SQLException {
            fiksur.close();
        }

        private static KunjunganEntity kunjungan(int idKunjungan, int idPasien) {
            KunjunganEntity k = new KunjunganEntity();
            k.setIdKunjungan(idKunjungan);
            k.setIdPasien(idPasien);
            return k;
        }

        @Test
        @DisplayName("C21-UT11 evaluasiRisiko normal: sistolik 150 menerbitkan penanda aktif dan entri daftar pantau")
        void evaluasiRisiko_normal() throws SQLException {
            List<PenandaRisikoEntity> hasil = manager.evaluasiRisiko(kunjungan(2, 4), tv(150, 65, null));

            assertEquals(1, hasil.size());
            assertEquals("hipertensi", hasil.get(0).getJenisRisiko());
            assertTrue(hasil.get(0).getIdPenanda() > 0);
            assertEquals("aktif", FiksturSkrining.teks("SELECT status FROM penanda_risiko WHERE id_penanda = ?",
                    hasil.get(0).getIdPenanda()));
            assertEquals(1, FiksturSkrining.angka(
                    "SELECT count(*) FROM daftar_pantau WHERE id_pasien = 4 AND jenis_risiko = 'hipertensi'"));
        }

        @Test
        @DisplayName("C21-UT12 evaluasiRisiko negatif: nilai normal tidak menerbitkan penanda maupun entri pantau")
        void evaluasiRisiko_tanpaRisiko() throws SQLException {
            long penandaSebelum = FiksturSkrining.angka("SELECT count(*) FROM penanda_risiko");
            long pantauSebelum = FiksturSkrining.angka("SELECT count(*) FROM daftar_pantau");

            List<PenandaRisikoEntity> hasil = manager.evaluasiRisiko(kunjungan(3, 3), tv(120, 65, 100));

            assertTrue(hasil.isEmpty());
            assertEquals(penandaSebelum, FiksturSkrining.angka("SELECT count(*) FROM penanda_risiko"));
            assertEquals(pantauSebelum, FiksturSkrining.angka("SELECT count(*) FROM daftar_pantau"));
        }

        @Test
        @DisplayName("C21-UT13 evaluasiRisiko batas: pasien yang sudah dipantau tidak dimasukkan dua kali")
        void evaluasiRisiko_sudahDipantau() throws SQLException {
            long sebelum = FiksturSkrining.angka(
                    "SELECT count(*) FROM daftar_pantau WHERE id_pasien = 1 AND jenis_risiko = 'hipertensi'");

            List<PenandaRisikoEntity> hasil = manager.evaluasiRisiko(kunjungan(9, 1), tv(150, 65, null));

            assertEquals(1, hasil.size());
            assertEquals(sebelum, FiksturSkrining.angka(
                    "SELECT count(*) FROM daftar_pantau WHERE id_pasien = 1 AND jenis_risiko = 'hipertensi'"));
        }

        @Test
        @DisplayName("C21-UT14 evaluasiRisiko batas: tiga kunjungan sebelumnya di atas ambang menandai walau hari ini normal")
        void evaluasiRisiko_riwayatTigaKunjungan() {
            // Pasien 1 memiliki tiga pengukuran sebelumnya dengan sistolik 146 sampai 150 pada seed.
            List<PenandaRisikoEntity> hasil = manager.evaluasiRisiko(kunjungan(9, 1), tv(120, 65, null));

            assertEquals(List.of("hipertensi"), hasil.stream().map(PenandaRisikoEntity::getJenisRisiko).toList());
        }

        @Test
        @DisplayName("C21-UT15 ambilPenanda normal: penanda aktif kunjungan seed dikembalikan")
        void ambilPenanda_normal() {
            List<PenandaRisikoEntity> penanda = manager.ambilPenanda(9);

            assertEquals(1, penanda.size());
            assertEquals("hipertensi", penanda.get(0).getJenisRisiko());
            assertEquals("aktif", penanda.get(0).getStatus());
        }

        @Test
        @DisplayName("C21-UT16 ambilPenanda negatif: kunjungan tanpa penanda mengembalikan daftar kosong")
        void ambilPenanda_kosong() {
            assertTrue(manager.ambilPenanda(2).isEmpty());
            assertTrue(manager.ambilPenanda(-1).isEmpty());
        }

        @Test
        @DisplayName("C21-UT17 muatEntriAktif normal: entri risiko dan jadwal kontrol seed tergabung")
        void muatEntriAktif_normal() {
            List<DaftarPantauEntity> entri = manager.muatEntriAktif();

            assertEquals(4, entri.size());
            assertTrue(entri.stream().anyMatch(e -> "kunjungan ulang".equals(e.getJenisRisiko())
                    && e.getTanggalPemicu() != null));
            assertTrue(entri.stream().anyMatch(e -> "hipertensi".equals(e.getJenisRisiko())));
        }

        @Test
        @DisplayName("C21-UT18 muatEntriAktif batas: jadwal baru menjadi satu entri kunjungan ulang dan tidak ganda saat dibuka lagi")
        void muatEntriAktif_jadwalBaru() throws SQLException {
            long idJadwal = FiksturSkrining.angka("INSERT INTO jadwal_kontrol (id_pasien, tanggal_rencana, status) "
                    + "VALUES (4, DATE '2099-01-01', 'terjadwal') RETURNING id_jadwal");
            try {
                List<DaftarPantauEntity> pertama = manager.muatEntriAktif();
                manager.muatEntriAktif();

                assertEquals(5, pertama.size());
                DaftarPantauEntity baru = pertama.stream().filter(e -> e.getIdPasien() == 4).findFirst().orElseThrow();
                assertNotNull(baru.getTanggalPemicu());
                assertTrue(baru.getIdEntri() > 0);
                assertEquals(1, FiksturSkrining.angka("SELECT count(*) FROM daftar_pantau WHERE id_jadwal = ?",
                        idJadwal));
            } finally {
                FiksturSkrining.jalankan("DELETE FROM daftar_pantau WHERE id_jadwal = ?", idJadwal);
                FiksturSkrining.jalankan("DELETE FROM jadwal_kontrol WHERE id_jadwal = ?", idJadwal);
            }
        }
    }
}
