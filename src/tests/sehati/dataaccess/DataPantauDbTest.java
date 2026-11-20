package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.FiksturSkrining;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.TindakLanjutEntity;

/**
 * Pengujian unit C60 DataPantauBoundary (HIUPL Subbab 4.2, C60-UT01 sampai C60-UT09, kueri Q-C60-01 sampai
 * Q-C60-07). Setiap test berjalan pada transaksi yang dibatalkan di akhir, sehingga seed tidak berubah.
 */
@DisplayName("C60 DataPantauBoundary")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class DataPantauDbTest {

    private Connection koneksi;
    private DataPantauBoundary boundary;

    @BeforeEach
    void buka() throws SQLException {
        koneksi = FiksturSkrining.pemilik();
        koneksi.setAutoCommit(false);
        boundary = new DataPantauBoundary(koneksi);
    }

    @AfterEach
    void batalkan() throws SQLException {
        koneksi.rollback();
        koneksi.close();
    }

    private long hitung(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); var rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    @Test
    @DisplayName("C60-UT01 ambilEntri normal: empat entri seed belum selesai beserta tanggal pemicunya")
    void ambilEntri_normal() {
        List<DaftarPantauEntity> entri = boundary.ambilEntri();

        assertEquals(4, entri.size());
        assertTrue(entri.stream().allMatch(e -> e.getTanggalPemicu() != null));
        assertTrue(entri.stream().anyMatch(e -> "kunjungan ulang".equals(e.getJenisRisiko())));
    }

    @Test
    @DisplayName("C60-UT02 ambilEntriSelesai negatif: seed tidak memiliki entri selesai")
    void ambilEntriSelesai_kosong() {
        assertTrue(boundary.ambilEntriSelesai().isEmpty());
    }

    @Test
    @DisplayName("C60-UT03 simpanEntri normal: entri risiko baru yang punya penanda mendapat id")
    void simpanEntri_risikoBaru() throws SQLException {
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("INSERT INTO penanda_risiko (id_kunjungan, jenis_risiko, tanggal, status) "
                    + "VALUES (2, 'obesitas', CURRENT_DATE, 'aktif')");
        }
        DaftarPantauEntity e = new DaftarPantauEntity(0, 4, "obesitas", DaftarPantauEntity.STATUS_BELUM);

        boundary.simpanEntri(e);

        assertTrue(e.getIdEntri() > 0);
        assertEquals(1, hitung("SELECT count(*) FROM daftar_pantau WHERE id_pasien = 4 AND jenis_risiko = 'obesitas'"));
    }

    @Test
    @DisplayName("C60-UT04 simpanEntri normal: entri lama hanya memutakhirkan status tindak lanjut")
    void simpanEntri_perbaruiStatus() {
        DaftarPantauEntity e = boundary.ambilEntri().stream().filter(x -> x.getIdEntri() == 1).findFirst()
                .orElseThrow();
        e.perbaruiStatus(DaftarPantauEntity.STATUS_ULANG);

        boundary.simpanEntri(e);

        assertEquals(DaftarPantauEntity.STATUS_ULANG, boundary.ambilEntri().stream()
                .filter(x -> x.getIdEntri() == 1).findFirst().orElseThrow().getStatusTindakLanjut());
    }

    @Test
    @DisplayName("C60-UT05 simpanEntri batas: jadwal yang sudah punya entri tidak dibuatkan entri kedua")
    void simpanEntri_jadwalSudahPunyaEntri() throws SQLException {
        long sebelum = hitung("SELECT count(*) FROM daftar_pantau");
        DaftarPantauEntity e = new DaftarPantauEntity(0, 1, "kunjungan ulang", DaftarPantauEntity.STATUS_BELUM);

        boundary.simpanEntri(e);

        assertEquals(0, e.getIdEntri());
        assertEquals(sebelum, hitung("SELECT count(*) FROM daftar_pantau"));
    }

    @Test
    @DisplayName("C60-UT06 simpanEntri negatif: entri risiko tanpa penanda sebagai pemicu ditolak")
    void simpanEntri_tanpaPemicu() {
        DaftarPantauEntity e = new DaftarPantauEntity(0, 4, "hipertensi", DaftarPantauEntity.STATUS_BELUM);

        IllegalStateException x = assertThrows(IllegalStateException.class, () -> boundary.simpanEntri(e));

        assertEquals("Entri daftar pantau tidak memiliki pemicu", x.getMessage());
    }

    @Test
    @DisplayName("C60-UT07 simpanTindakLanjut normal: upaya tersimpan sebagai baris baru dan mendapat id")
    void simpanTindakLanjut_normal() throws SQLException {
        TindakLanjutEntity t = new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui");

        boundary.simpanTindakLanjut(1, t);

        assertTrue(t.getIdTindakLanjut() > 0);
        assertEquals(1, hitung("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 1"));
    }

    @Test
    @DisplayName("C60-UT08 simpanTindakLanjut batas: dua upaya pada satu entri menjadi dua baris")
    void simpanTindakLanjut_duaUpaya() throws SQLException {
        boundary.simpanTindakLanjut(1, new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui"));
        boundary.simpanTindakLanjut(1, new TindakLanjutEntity(0, "pesan singkat", "terhubung", "akan datang"));

        assertEquals(2, hitung("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 1"));
    }

    @Test
    @DisplayName("C60-UT09 simpanTindakLanjut negatif: entri yang tidak ada ditolak")
    void simpanTindakLanjut_entriTidakAda() {
        TindakLanjutEntity t = new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang");

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> boundary.simpanTindakLanjut(-1, t));

        assertNotNull(e.getMessage());
    }
}
