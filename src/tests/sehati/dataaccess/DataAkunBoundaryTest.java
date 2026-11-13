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
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.AuditEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.SesiEntity;

/**
 * Uji terhadap basis data berisi src/db/schema.sql. Aktif bila DB_UJI_URL diisi dengan akun pemilik basis data,
 * misalnya {@code jdbc:postgresql://localhost:5432/sehati?user=sehati_admin&password=...}. Setiap uji di-rollback.
 */
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
        koneksi.rollback();
        koneksi.close();
    }

    private PenggunaEntity simpanBaru(String nama) {
        PenggunaEntity p = new PenggunaEntity(nama, "$2a$04$" + "a".repeat(53), "Perawat");
        dataAkun.simpanPengguna(p);
        return p;
    }

    @Test
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
    void cariPengguna_namaTidakDikenalMengembalikanNull() {
        assertNull(dataAkun.cariPengguna("tidak_ada_" + UUID.randomUUID()));
        assertNull(dataAkun.ambilPengguna(-1));
        assertFalse(dataAkun.namaPenggunaDipakai("tidak_ada_" + UUID.randomUUID()));
    }

    @Test
    void simpanPengguna_menolakNamaGandaDanPeranTidakDikenal() throws SQLException {
        simpanBaru("uji_ganda");
        Savepoint titik = koneksi.setSavepoint();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> simpanBaru("uji_ganda"));
        assertEquals("Nama pengguna sudah dipakai", e.getMessage());
        koneksi.rollback(titik);

        PenggunaEntity p = new PenggunaEntity("uji_peran", "$2a$04$" + "a".repeat(53), "Satpam");
        assertThrows(IllegalArgumentException.class, () -> dataAkun.simpanPengguna(p));
    }

    @Test
    void simpanSesi_sesiBaruLaluDitutup() throws SQLException {
        PenggunaEntity p = simpanBaru("uji_sesi");
        SesiEntity s = new SesiEntity(UUID.randomUUID(), p.getIdPengguna(), LocalDateTime.now(), true);
        dataAkun.simpanSesi(s);
        assertTrue(statusSesi(s.getIdSesi()));

        s.tutup();
        dataAkun.simpanSesi(s);
        assertFalse(statusSesi(s.getIdSesi()));
    }

    @Test
    void tutupSesiAktif_menutupSeluruhSesiAkun() throws SQLException {
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
    void sesiMasihAktif_mengikutiStatusDanFalseBilaTidakTercatat() throws SQLException {
        PenggunaEntity p = simpanBaru("uji_status_sesi");
        SesiEntity s = new SesiEntity(UUID.randomUUID(), p.getIdPengguna(), LocalDateTime.now(), true);
        dataAkun.simpanSesi(s);
        assertTrue(dataAkun.sesiMasihAktif(s.getIdSesi()));

        dataAkun.tutupSesiAktif(p.getIdPengguna());
        assertFalse(dataAkun.sesiMasihAktif(s.getIdSesi()));
        assertFalse(dataAkun.sesiMasihAktif(UUID.randomUUID()));
    }

    @Test
    void simpanAudit_menambahCatatanTermasukPelakuTidakDikenal() throws SQLException {
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
    void audit_tidakDapatDiubahAtauDihapusOlehSehatiApp() throws SQLException {
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
