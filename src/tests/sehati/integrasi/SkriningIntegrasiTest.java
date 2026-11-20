package sehati.integrasi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.FiksturSkrining;
import sehati.business.control.PantauManager;
import sehati.business.control.SkriningManager;
import sehati.business.entity.SesiEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.business.entity.TindakLanjutEntity;
import sehati.common.SesiAktif;

/**
 * Pengujian integrasi modul skrining dan pemantauan (HIUPL Subbab 4.3, INT-TC11 sampai INT-TC16) serta pengujian
 * KNF02. Manager dijalankan dengan DataBoundary dan PostgreSQL yang nyata, tanpa mock. Hasilnya dibaca ulang lewat
 * koneksi terpisah, sehingga yang diperiksa adalah data yang benar-benar sampai di basis data. Kasus gagal memasang
 * pemicu sementara yang menolak baris audit di langkah terakhir transaksi, setelah langkah sebelumnya selesai;
 * pemicu dibuang lagi sesudah test.
 */
@DisplayName("Integrasi skrining dan pemantauan (IK-06, IK-07, IK-08) dan KNF02")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class SkriningIntegrasiTest {

    private final SkriningManager skrining = new SkriningManager();
    private final PantauManager pantau = new PantauManager();
    private FiksturSkrining fiksur;

    @BeforeEach
    void siapkan() throws SQLException {
        fiksur = new FiksturSkrining();
        masuk(2);
    }

    @AfterEach
    void pulihkan() throws SQLException {
        SesiAktif.lepas();
        FiksturSkrining.jalankan("DROP TRIGGER IF EXISTS uji_gagal_audit ON audit");
        FiksturSkrining.jalankan("DROP FUNCTION IF EXISTS uji_gagal_audit()");
        fiksur.close();
    }

    // Menolak baris audit milik skrining dan tindak lanjut, yang ditulis di langkah terakhir transaksi Manager.
    private static void pasangGalatAudit() throws SQLException {
        FiksturSkrining.jalankan("CREATE FUNCTION uji_gagal_audit() RETURNS trigger LANGUAGE plpgsql AS "
                + "'BEGIN RAISE EXCEPTION ''galat uji pada audit''; END'");
        FiksturSkrining.jalankan("CREATE TRIGGER uji_gagal_audit BEFORE INSERT ON audit FOR EACH ROW "
                + "WHEN (NEW.deskripsi LIKE 'Skrining kunjungan %' OR NEW.deskripsi LIKE 'Tindak lanjut daftar pantau %') "
                + "EXECUTE FUNCTION uji_gagal_audit()");
    }

    private static void masuk(int idPengguna) {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), idPengguna, LocalDateTime.now(), true), "Perawat");
    }

    private static TandaVitalEntity normal() {
        return new TandaVitalEntity(118, 78, 62, 165, 36.6, 70, 98);
    }

    private static TandaVitalEntity berisiko() {
        return new TandaVitalEntity(152, 96, 90, 165, 36.8, 82, 210);
    }

    private static long hitung(String sql, Object... nilai) throws SQLException {
        return FiksturSkrining.angka(sql, nilai);
    }

    @Test
    @DisplayName("INT-TC11 IK-06 normal: skrining tersimpan, status kunjungan berubah, dan kunjungan keluar dari antrean")
    void skriningTersimpanLintasBoundary() throws SQLException {
        assertTrue(skrining.simpanSkrining(2, "Pusing ringan", normal()).isEmpty());

        assertEquals(1, hitung("SELECT count(*) FROM skrining WHERE id_kunjungan = 2"));
        assertEquals(118, hitung("SELECT tv.sistolik FROM tanda_vital tv JOIN skrining s USING (id_skrining) "
                + "WHERE s.id_kunjungan = 2"));
        assertEquals("siap diperiksa dokter", FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 2"));
        assertEquals(0, hitung("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                + "WHERE a.jenis = 'skrining' AND e.id_entri = 2"));
    }

    @Test
    @DisplayName("INT-TC12 IK-06 gagal: skrining ganda ditolak dan basis data tidak berubah")
    void skriningGandaDitolak() throws SQLException {
        long skriningSebelum = hitung("SELECT count(*) FROM skrining");
        long vitalSebelum = hitung("SELECT count(*) FROM tanda_vital");

        assertThrows(IllegalArgumentException.class, () -> skrining.simpanSkrining(9, "Ulang", normal()));

        assertEquals(skriningSebelum, hitung("SELECT count(*) FROM skrining"));
        assertEquals(vitalSebelum, hitung("SELECT count(*) FROM tanda_vital"));
        assertEquals("siap diperiksa dokter", FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 9"));
    }

    @Test
    @DisplayName("INT-TC13 IK-07 normal: skrining berisiko menerbitkan tiga penanda dan tiga entri daftar pantau")
    void skriningBerisikoMenerbitkanPenandaDanEntriPantau() throws SQLException {
        assertTrue(skrining.simpanSkrining(2, "Lemas", berisiko()).isEmpty());

        assertEquals(3, hitung("SELECT count(*) FROM penanda_risiko WHERE id_kunjungan = 2 AND status = 'aktif'"));
        assertEquals(3, hitung("SELECT count(*) FROM daftar_pantau WHERE id_pasien = 4 AND id_penanda IS NOT NULL"));
        assertEquals(3, hitung("SELECT count(*) FROM daftar_pantau d JOIN penanda_risiko p ON p.id_penanda = d.id_penanda "
                + "WHERE p.id_kunjungan = 2"));
    }

    @Test
    @DisplayName("INT-TC14 IK-07 gagal: kegagalan di langkah terakhir membatalkan skrining, penanda, dan entri pantau")
    void kegagalanMembatalkanSeluruhTransaksi() throws SQLException {
        pasangGalatAudit();
        long audit = hitung("SELECT count(*) FROM audit");

        assertThrows(RuntimeException.class, () -> skrining.simpanSkrining(2, "Lemas", berisiko()));

        assertEquals(0, hitung("SELECT count(*) FROM skrining WHERE id_kunjungan = 2"));
        assertEquals(0, hitung("SELECT count(*) FROM penanda_risiko WHERE id_kunjungan = 2"));
        assertEquals(0, hitung("SELECT count(*) FROM daftar_pantau WHERE id_pasien = 4"));
        assertEquals("menunggu skrining", FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 2"));
        assertEquals(audit, hitung("SELECT count(*) FROM audit"));
    }

    @Test
    @DisplayName("INT-TC15 IK-08 normal: tindak lanjut terhubung tersimpan, entri selesai, dan rekam medis bertambah")
    void tindakLanjutTerhubungLintasBoundary() throws SQLException {
        long catatan = hitung("SELECT count(*) FROM catatan_rekam_medis");

        pantau.catatTindakLanjut(2, new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang"));

        assertEquals("selesai ditindaklanjuti",
                FiksturSkrining.teks("SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_entri = 2"));
        assertEquals("terhubung", FiksturSkrining.teks("SELECT hasil_upaya FROM tindak_lanjut WHERE id_entri = 2"));
        assertEquals(catatan + 1, hitung("SELECT count(*) FROM catatan_rekam_medis"));
        assertTrue(FiksturSkrining.teks("SELECT isi FROM catatan_rekam_medis ORDER BY id_catatan DESC LIMIT 1")
                .startsWith("Tindak lanjut telepon: terhubung"));
    }

    @Test
    @DisplayName("INT-TC16 IK-08 gagal: kegagalan audit membatalkan tindak lanjut, status entri, dan rekam medis")
    void tindakLanjutDibatalkanBilaGagal() throws SQLException {
        pasangGalatAudit();
        long catatan = hitung("SELECT count(*) FROM catatan_rekam_medis");
        long upaya = hitung("SELECT count(*) FROM tindak_lanjut");

        assertThrows(RuntimeException.class,
                () -> pantau.catatTindakLanjut(2, new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang")));

        assertEquals("belum ditindaklanjuti",
                FiksturSkrining.teks("SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_entri = 2"));
        assertEquals(upaya, hitung("SELECT count(*) FROM tindak_lanjut"));
        assertEquals(catatan, hitung("SELECT count(*) FROM catatan_rekam_medis"));
    }

    @Test
    @DisplayName("KNF02-TC01 reliability: lima nilai di luar rentang ditolak tanpa satu baris pun tersimpan")
    void nilaiDiLuarRentangTidakMenambahBaris() throws SQLException {
        long skriningSebelum = hitung("SELECT count(*) FROM skrining");
        long vitalSebelum = hitung("SELECT count(*) FROM tanda_vital");
        long penandaSebelum = hitung("SELECT count(*) FROM penanda_risiko");
        List<TandaVitalEntity> salah = List.of(
                new TandaVitalEntity(400, 80, 62, 165, 36.6, 70, null),
                new TandaVitalEntity(120, 80, 62, 165, 72, 70, null),
                new TandaVitalEntity(120, 80, 62, 165, 36.6, 300, null),
                new TandaVitalEntity(120, 80, 62, 20, 36.6, 70, null),
                new TandaVitalEntity(120, 80, 62, 165, 36.6, 70, 5));

        for (TandaVitalEntity tv : salah) {
            assertFalse(skrining.simpanSkrining(2, "Uji", tv).isEmpty());
        }

        assertEquals(skriningSebelum, hitung("SELECT count(*) FROM skrining"));
        assertEquals(vitalSebelum, hitung("SELECT count(*) FROM tanda_vital"));
        assertEquals(penandaSebelum, hitung("SELECT count(*) FROM penanda_risiko"));
    }

    @Test
    @DisplayName("KNF02-TC02 reliability: kegagalan di tengah penyimpanan tidak meninggalkan data setengah tersimpan")
    void kegagalanTidakMeninggalkanDataParsial() throws SQLException {
        pasangGalatAudit();
        long skriningSebelum = hitung("SELECT count(*) FROM skrining");
        long vitalSebelum = hitung("SELECT count(*) FROM tanda_vital");
        long penandaSebelum = hitung("SELECT count(*) FROM penanda_risiko");
        long pantauSebelum = hitung("SELECT count(*) FROM daftar_pantau");

        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () -> skrining.simpanSkrining(2, "Uji", berisiko()));
        }

        assertEquals(skriningSebelum, hitung("SELECT count(*) FROM skrining"));
        assertEquals(vitalSebelum, hitung("SELECT count(*) FROM tanda_vital"));
        assertEquals(penandaSebelum, hitung("SELECT count(*) FROM penanda_risiko"));
        assertEquals(pantauSebelum, hitung("SELECT count(*) FROM daftar_pantau"));
    }
}
