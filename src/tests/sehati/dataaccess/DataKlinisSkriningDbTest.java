package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.FiksturSkrining;
import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.PenandaRisikoEntity;
import sehati.business.entity.SkriningEntity;
import sehati.business.entity.TandaVitalEntity;

/**
 * Pengujian unit C59 DataKlinisBoundary bagian skrining dan risiko (HIUPL Subbab 4.2, C59-UT01 sampai C59-UT20,
 * kueri Q-C59-01 sampai Q-C59-10). Setiap test berjalan pada transaksi yang dibatalkan di akhir, sehingga seed tidak
 * berubah.
 */
@DisplayName("C59 DataKlinisBoundary bagian skrining")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class DataKlinisSkriningDbTest {

    private Connection koneksi;
    private DataKlinisBoundary boundary;

    @BeforeEach
    void buka() throws SQLException {
        koneksi = FiksturSkrining.pemilik();
        koneksi.setAutoCommit(false);
        boundary = new DataKlinisBoundary(koneksi);
    }

    @AfterEach
    void batalkan() throws SQLException {
        koneksi.rollback();
        koneksi.close();
    }

    private static SkriningEntity skrining(int idKunjungan, Integer gulaDarah) {
        SkriningEntity s = new SkriningEntity(0, idKunjungan, "Pusing", LocalDateTime.now());
        s.lampirkanTandaVital(new TandaVitalEntity(125, 82, 66.5, 168, 36.7, 74, gulaDarah));
        return s;
    }

    @Test
    @DisplayName("C59-UT01 simpanSkrining normal: skrining dan tanda vital tersimpan lalu dapat dibaca kembali")
    void simpanSkrining_normal() {
        SkriningEntity s = skrining(2, 110);

        boundary.simpanSkrining(s);
        SkriningEntity baca = boundary.ambilSkrining(2);

        assertTrue(s.getIdSkrining() > 0);
        assertEquals("Pusing", baca.getKeluhanAwal());
        assertEquals(125, baca.getTandaVital().getSistolik());
        assertEquals(66.5, baca.getTandaVital().getBeratBadan(), 1e-9);
        assertEquals(110, baca.getTandaVital().getGulaDarah());
    }

    @Test
    @DisplayName("C59-UT02 simpanSkrining batas: gula darah kosong tersimpan sebagai null")
    void simpanSkrining_gulaDarahKosong() {
        boundary.simpanSkrining(skrining(2, null));

        assertNull(boundary.ambilSkrining(2).getTandaVital().getGulaDarah());
    }

    @Test
    @DisplayName("C59-UT03 simpanSkrining negatif: skrining tanpa tanda vital ditolak")
    void simpanSkrining_tanpaTandaVital() {
        SkriningEntity s = new SkriningEntity(0, 2, "Batuk", LocalDateTime.now());

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanSkrining(s));

        assertEquals("Tanda vital belum dilampirkan pada skrining", e.getMessage());
    }

    @Test
    @DisplayName("C59-UT04 simpanSkrining negatif: kunjungan yang sudah diskrining ditolak dengan pesan yang dapat dibaca")
    void simpanSkrining_ganda() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> boundary.simpanSkrining(skrining(9, null)));

        assertEquals("Data sudah tersimpan sebelumnya", e.getMessage());
    }

    @Test
    @DisplayName("C59-UT05 simpanSkrining negatif: nilai di luar aturan basis data ditolak")
    void simpanSkrining_nilaiDiLuarAturan() {
        SkriningEntity s = skrining(2, null);
        s.getTandaVital().setSistolik(400);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanSkrining(s));

        assertEquals("Nilai tanda vital di luar rentang yang diizinkan", e.getMessage());
    }

    @Test
    @DisplayName("C59-UT06 ambilSkrining normal: skrining seed kunjungan 9 dibaca lengkap dengan tanda vitalnya")
    void ambilSkrining_normal() {
        SkriningEntity s = boundary.ambilSkrining(9);

        assertNotNull(s);
        assertEquals(9, s.getIdKunjungan());
        assertEquals(152, s.getTandaVital().getSistolik());
        assertNull(s.getTandaVital().getGulaDarah());
        assertNotNull(s.getTandaVital().getWaktu());
    }

    @Test
    @DisplayName("C59-UT07 ambilSkrining negatif: kunjungan yang belum diskrining mengembalikan null")
    void ambilSkrining_belumDiskrining() {
        assertNull(boundary.ambilSkrining(2));
        assertNull(boundary.ambilSkrining(-1));
    }

    @Test
    @DisplayName("C59-UT08 riwayatTandaVital normal: tiga pengukuran sebelum hari ini, terbaru lebih dulu")
    void riwayatTandaVital_tigaTerakhir() {
        List<TandaVitalEntity> riwayat = boundary.riwayatTandaVital(1, 3);

        assertEquals(List.of(146, 150, 148), riwayat.stream().map(TandaVitalEntity::getSistolik).toList());
    }

    @Test
    @DisplayName("C59-UT09 riwayatTandaVital batas: batas 0 memuat seluruh pengukuran, terlama lebih dulu, berwaktu")
    void riwayatTandaVital_semua() {
        List<TandaVitalEntity> riwayat = boundary.riwayatTandaVital(1, 0);

        assertEquals(List.of(148, 150, 146, 152), riwayat.stream().map(TandaVitalEntity::getSistolik).toList());
        assertTrue(riwayat.stream().allMatch(t -> t.getWaktu() != null));
    }

    @Test
    @DisplayName("C59-UT10 riwayatTandaVital negatif: batas negatif ditolak dan pasien tanpa pengukuran kosong")
    void riwayatTandaVital_negatif() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> boundary.riwayatTandaVital(1, -1));

        assertEquals("Batas riwayat tidak boleh negatif", e.getMessage());
        assertTrue(boundary.riwayatTandaVital(-1, 3).isEmpty());
    }

    @Test
    @DisplayName("C59-UT11 ambilAmbang normal: ketiga ambang data master tersedia")
    void ambilAmbang_normal() {
        List<AmbangRisikoEntity> ambang = boundary.ambilAmbang();

        assertEquals(3, ambang.size());
        assertEquals(140, nilai(ambang, "hipertensi"));
        assertEquals(30, nilai(ambang, "obesitas"));
        assertEquals(200, nilai(ambang, "indikasi diabetes"));
    }

    private static double nilai(List<AmbangRisikoEntity> daftar, String jenis) {
        return daftar.stream().filter(a -> jenis.equals(a.getJenisRisiko())).findFirst().orElseThrow()
                .getNilaiBatas();
    }

    @Test
    @DisplayName("C59-UT12 simpanAmbang normal: nilai batas dimutakhirkan")
    void simpanAmbang_normal() {
        boundary.simpanAmbang(new AmbangRisikoEntity(1, "hipertensi", 135));

        assertEquals(135, nilai(boundary.ambilAmbang(), "hipertensi"));
    }

    @Test
    @DisplayName("C59-UT13 simpanAmbang negatif: ambang yang tidak ada ditolak")
    void simpanAmbang_tidakAda() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> boundary.simpanAmbang(new AmbangRisikoEntity(99, "hipertensi", 135)));

        assertEquals("Ambang risiko tidak ditemukan: 99", e.getMessage());
    }

    @Test
    @DisplayName("C59-UT14 simpanPenanda normal: penanda baru mendapat id dan muncul sebagai penanda aktif")
    void simpanPenanda_normal() {
        PenandaRisikoEntity p = new PenandaRisikoEntity(0, 2, "obesitas", LocalDate.now(),
                PenandaRisikoEntity.STATUS_AKTIF);

        boundary.simpanPenanda(p);

        assertTrue(p.getIdPenanda() > 0);
        assertEquals(List.of("obesitas"),
                boundary.penandaKunjungan(2).stream().map(PenandaRisikoEntity::getJenisRisiko).toList());
    }

    @Test
    @DisplayName("C59-UT15 simpanPenanda batas: penanda yang dicabut tidak lagi tampil sebagai penanda aktif")
    void simpanPenanda_dicabut() {
        PenandaRisikoEntity p = boundary.penandaKunjungan(9).get(0);
        p.cabut();

        boundary.simpanPenanda(p);

        assertTrue(boundary.penandaKunjungan(9).isEmpty());
    }

    @Test
    @DisplayName("C59-UT16 simpanPenanda negatif: penanda baru tanpa tanggal ditolak")
    void simpanPenanda_tanpaTanggal() {
        PenandaRisikoEntity p = new PenandaRisikoEntity(0, 2, "obesitas", null, PenandaRisikoEntity.STATUS_AKTIF);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanPenanda(p));

        assertEquals("Tanggal penanda risiko wajib diisi", e.getMessage());
    }

    @Test
    @DisplayName("C59-UT17 simpanPenanda negatif: pembaruan penanda yang tidak ada ditolak")
    void simpanPenanda_tidakAda() {
        PenandaRisikoEntity p = new PenandaRisikoEntity(999, 2, "obesitas", LocalDate.now(),
                PenandaRisikoEntity.STATUS_DICABUT);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanPenanda(p));

        assertEquals("Penanda risiko tidak ditemukan: 999", e.getMessage());
    }

    @Test
    @DisplayName("C59-UT18 penandaKunjungan normal: penanda aktif seed kunjungan 9 adalah hipertensi")
    void penandaKunjungan_normal() {
        List<PenandaRisikoEntity> penanda = boundary.penandaKunjungan(9);

        assertEquals(1, penanda.size());
        assertEquals("hipertensi", penanda.get(0).getJenisRisiko());
        assertEquals("aktif", penanda.get(0).getStatus());
    }

    @Test
    @DisplayName("C59-UT19 penandaKunjungan negatif: kunjungan tanpa penanda mengembalikan daftar kosong")
    void penandaKunjungan_kosong() {
        assertTrue(boundary.penandaKunjungan(2).isEmpty());
        assertTrue(boundary.penandaKunjungan(-1).isEmpty());
    }

    @Test
    @DisplayName("C59-UT20 simpanSkrining negatif: kunjungan yang tidak ada ditolak dengan pesan yang dapat dibaca")
    void simpanSkrining_kunjunganTidakAda() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> boundary.simpanSkrining(skrining(-1, null)));

        assertEquals("Kunjungan atau data terkait tidak ditemukan", e.getMessage());
    }
}
