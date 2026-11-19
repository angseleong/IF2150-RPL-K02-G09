package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
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

import sehati.business.entity.AuditEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.SesiEntity;

/**
 * Pengujian unit C56 DataAkunBoundary (HIUPL Subbab 4.2, C56-UT01 sampai C56-UT10) terhadap basis data berisi
 * src/db/schema.sql. Aktif bila DB_UJI_URL diisi dengan akun pemilik basis data. Setiap uji di-rollback.
 */
@DisplayName("C56 DataAkunBoundary")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class DataAkunBoundaryTest {

    private Connection koneksi;
    private DataAkunBoundary dataAkun;

    @BeforeEach
    void buka() throws SQLException {
        koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        koneksi.setAutoCommit(false);
        dataAkun = new DataAkunBoundary(koneksi);
    }

    @AfterEach
    void tutup() throws SQLException {
        if (!koneksi.isClosed()) {
            koneksi.rollback();
            koneksi.close();
        }
    }

    private PenggunaEntity simpanBaru(String nama) {
        PenggunaEntity p = new PenggunaEntity(nama, "$2a$04$" + "a".repeat(53), "Perawat");
        dataAkun.simpanPengguna(p);
        return p;
    }

    @Test
    @DisplayName("C56-UT01 simpanPengguna, cariPengguna, ambilPengguna normal: sisip lalu mutakhirkan akun")
    void simpanPengguna_sisipLaluMutakhirkan() {
        PenggunaEntity p = simpanBaru("uji_akun");
        assertTrue(p.getIdPengguna() > 0);
        assertTrue(dataAkun.namaPenggunaDipakai("uji_akun"));
        assertEquals("Perawat", dataAkun.cariPengguna("uji_akun").getPeran());

        p.setPeran("Dokter");
        p.nonaktifkan();
        dataAkun.simpanPengguna(p);

        PenggunaEntity dibaca = dataAkun.ambilPengguna(p.getIdPengguna());
        assertEquals("Dokter", dibaca.getPeran());
        assertFalse(dibaca.aktif());
    }

    @Test
    @DisplayName("C56-UT02 cariPengguna, ambilPengguna, namaPenggunaDipakai negatif: akun tidak terdaftar")
    void cariPengguna_tidakTerdaftar() {
        assertNull(dataAkun.cariPengguna("tidak_ada_" + UUID.randomUUID()));
        assertNull(dataAkun.ambilPengguna(-1));
        assertFalse(dataAkun.namaPenggunaDipakai("tidak_ada_" + UUID.randomUUID()));
        // pencarian nama persis sama, tidak membedakan huruf tidak berlaku
        assertNull(dataAkun.cariPengguna("ADMIN"));
    }

    @Test
    @DisplayName("C56-UT03 simpanPengguna negatif: nama ganda dan peran tidak dikenal ditolak basis data")
    void simpanPengguna_namaGandaPeranTidakDikenal() throws SQLException {
        simpanBaru("uji_ganda");
        Savepoint titik = koneksi.setSavepoint();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> simpanBaru("uji_ganda"));
        assertEquals("Nama pengguna sudah dipakai", e.getMessage());
        koneksi.rollback(titik);

        PenggunaEntity p = new PenggunaEntity("uji_peran", "$2a$04$" + "a".repeat(53), "Satpam");
        e = assertThrows(IllegalArgumentException.class, () -> dataAkun.simpanPengguna(p));
        assertEquals("Peran atau status akun tidak valid", e.getMessage());
    }

    @Test
    @DisplayName("C56-UT04 simpanSesi normal: sesi baru tersimpan lalu ditutup")
    void simpanSesi_baruLaluDitutup() throws SQLException {
        PenggunaEntity p = simpanBaru("uji_sesi");
        SesiEntity s = new SesiEntity(UUID.randomUUID(), p.getIdPengguna(), LocalDateTime.now(), true);
        dataAkun.simpanSesi(s);
        assertTrue(statusSesi(s.getIdSesi()));

        s.tutup();
        dataAkun.simpanSesi(s);
        assertFalse(statusSesi(s.getIdSesi()));
    }

    @Test
    @DisplayName("C56-UT05 tutupSesiAktif normal: seluruh sesi aktif satu akun ditutup")
    void tutupSesiAktif_seluruhSesi() throws SQLException {
        PenggunaEntity p = simpanBaru("uji_tutup");
        SesiEntity s1 = new SesiEntity(UUID.randomUUID(), p.getIdPengguna(), LocalDateTime.now(), true);
        SesiEntity s2 = new SesiEntity(UUID.randomUUID(), p.getIdPengguna(), LocalDateTime.now(), true);
        dataAkun.simpanSesi(s1);
        dataAkun.simpanSesi(s2);

        dataAkun.tutupSesiAktif(p.getIdPengguna());

        assertFalse(statusSesi(s1.getIdSesi()));
        assertFalse(statusSesi(s2.getIdSesi()));
    }

    @Test
    @DisplayName("C56-UT06 sesiMasihAktif normal dan negatif: mengikuti status, false bila sesi tidak tercatat")
    void sesiMasihAktif_mengikutiStatus() {
        PenggunaEntity p = simpanBaru("uji_status_sesi");
        SesiEntity s = new SesiEntity(UUID.randomUUID(), p.getIdPengguna(), LocalDateTime.now(), true);
        dataAkun.simpanSesi(s);
        assertTrue(dataAkun.sesiMasihAktif(s.getIdSesi()));

        dataAkun.tutupSesiAktif(p.getIdPengguna());
        assertFalse(dataAkun.sesiMasihAktif(s.getIdSesi()));
        assertFalse(dataAkun.sesiMasihAktif(UUID.randomUUID()));
    }

    @Test
    @DisplayName("C56-UT07 simpanAudit normal: catatan bertambah, termasuk pelaku 0 untuk nama tidak dikenal")
    void simpanAudit_menambahCatatan() throws SQLException {
        String deskripsi = "Uji audit " + UUID.randomUUID();
        dataAkun.simpanAudit(AuditEntity.buat(0, deskripsi));

        try (PreparedStatement ps = koneksi.prepareStatement("SELECT id_pengguna FROM audit WHERE deskripsi = ?")) {
            ps.setString(1, deskripsi);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    @Test
    @DisplayName("C56-UT08 audit negatif: sehati_app tidak dapat mengubah atau menghapus audit (KNF05)")
    void audit_tidakDapatDiubahAtauDihapus() throws SQLException {
        dataAkun.simpanAudit(AuditEntity.buat(0, "Uji hak akses audit"));
        try (Statement st = koneksi.createStatement()) {
            st.execute("SET LOCAL ROLE sehati_app");
            for (String sql : new String[] {"UPDATE audit SET deskripsi = 'ubah'", "DELETE FROM audit"}) {
                Savepoint titik = koneksi.setSavepoint();
                SQLException e = assertThrows(SQLException.class, () -> st.executeUpdate(sql));
                // 42501 = insufficient_privilege (aturan 5 Subbab 5.56, KNF05).
                assertEquals("42501", e.getSQLState());
                koneksi.rollback(titik);
            }
        }
    }

    @Test
    @DisplayName("C56-UT09 semuaPengguna normal: seluruh akun termasuk yang nonaktif, terurut nama")
    void semuaPengguna_terurut() throws SQLException {
        simpanBaru("aaa_uji_urut");
        List<PenggunaEntity> semua = dataAkun.semuaPengguna();

        assertEquals(hitung("SELECT count(*) FROM pengguna"), semua.size());
        assertEquals("aaa_uji_urut", semua.get(0).getNamaPengguna());
        assertTrue(semua.stream().anyMatch(p -> !p.aktif()));
        for (int i = 1; i < semua.size(); i++) {
            assertTrue(semua.get(i - 1).getNamaPengguna().compareTo(semua.get(i).getNamaPengguna()) <= 0);
        }
    }

    @Test
    @DisplayName("C56-UT10 seluruh operasi negatif: koneksi terputus menjadi IllegalStateException")
    void koneksiTerputus() throws SQLException {
        koneksi.close();
        SesiEntity s = new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true);
        List<Runnable> operasi = List.of(() -> dataAkun.cariPengguna("admin"), () -> dataAkun.ambilPengguna(1),
                dataAkun::semuaPengguna, () -> dataAkun.namaPenggunaDipakai("admin"),
                () -> dataAkun.simpanPengguna(new PenggunaEntity("x", "h", "Perawat")),
                () -> dataAkun.simpanSesi(s), () -> dataAkun.tutupSesiAktif(1),
                () -> dataAkun.sesiMasihAktif(s.getIdSesi()), () -> dataAkun.simpanAudit(AuditEntity.buat(1, "x")));
        for (Runnable r : operasi) {
            IllegalStateException e = assertThrows(IllegalStateException.class, r::run);
            assertEquals("Basis data tidak dapat dihubungi", e.getMessage());
        }
    }

    private int hitung(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private boolean statusSesi(UUID idSesi) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement("SELECT status_aktif FROM sesi WHERE id_sesi = ?")) {
            ps.setObject(1, idSesi);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                return rs.getBoolean(1);
            }
        }
    }
}
