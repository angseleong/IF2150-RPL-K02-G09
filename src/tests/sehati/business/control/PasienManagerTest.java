package sehati.business.control;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasienManagerTest {

    private final PasienManager manager = new PasienManager();

    @Test
    void validasiNIK_menerimaTepat16Digit() {
        assertTrue(manager.validasiNIK("9999010101010001"));
    }

    @Test
    void validasiNIK_menolakKurangDari16Digit() {
        assertFalse(manager.validasiNIK("999901010101001"));
    }

    @Test
    void validasiNIK_menolakLebihDari16Digit() {
        assertFalse(manager.validasiNIK("99990101010100017"));
    }

    @Test
    void validasiNIK_menolakBukanDigit() {
        assertFalse(manager.validasiNIK("999901010101000A"));
    }

    @Test
    void validasiNIK_menolakNull() {
        assertFalse(manager.validasiNIK(null));
    }

    @Test
    void cariPasien_kataKunciKosongMengembalikanDaftarKosong() {
        assertEquals(List.of(), manager.cariPasien("   "));
        assertEquals(List.of(), manager.cariPasien(null));
    }

    @Test
    void daftarkanPasien_menolakNIKTidakValid() {
        PasienEntity data = dataPasien(0, "999901010101001");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.daftarkanPasien(data));

        assertEquals("NIK tidak valid", e.getMessage());
    }

    @Test
    void perbaruiPasien_menolakDataNull() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.perbaruiPasien(null));

        assertEquals("Data pasien kosong", e.getMessage());
    }

    private static PasienEntity dataPasien(int idPasien, String nik) {
        return new PasienEntity(idPasien, nik, null, "Nama Uji", LocalDate.of(1990, 1, 1),
                "Perempuan", "Jl. Uji No. 1", null);
    }

    private static String nikFiktif() {
        String angka = UUID.randomUUID().toString().replace("-", "").replaceAll("[a-f]", "7");
        // 9 + 7 digit, seluruh karakter digit
        return "9" + angka.substring(0, 7) + "00000000";
    }

    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private long auditTerakhir = -1;
        private boolean pasangBerhasil;
        private final List<String> nikUji = new ArrayList<>();

        @BeforeEach
        void pasang() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Petugas Administrasi");
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "SELECT COALESCE(MAX(id_audit), 0) FROM audit");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                auditTerakhir = rs.getLong(1);
            }
            pasangBerhasil = true;
        }

        @AfterEach
        void bersihikan() throws SQLException {
            SesiAktif.lepas();
            if (koneksi == null) {
                return;
            }
            for (String nik : nikUji) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM rekam_medis WHERE id_pasien = (SELECT id_pasien FROM pasien WHERE nik = ?)")) {
                    ps.setString(1, nik);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = koneksi.prepareStatement("DELETE FROM pasien WHERE nik = ?")) {
                    ps.setString(1, nik);
                    ps.executeUpdate();
                }
            }
            if (pasangBerhasil) {
                // Hanya baris audit yang dibuat pengujian ini, bukan seluruh isi tabel atau baris bersamaan.
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM audit WHERE id_audit > ? AND (deskripsi LIKE 'Pendaftaran pasien %' "
                                + "OR deskripsi LIKE 'Pembaruan data pasien %')")) {
                    ps.setLong(1, auditTerakhir);
                    ps.executeUpdate();
                }
            }
            koneksi.close();
        }

        @Test
        void daftarkanPasien_menerbitkanTepatSatuNomorRMDanMencatatAudit() throws SQLException {
            String nik = nikFiktif();
            nikUji.add(nik);
            long posisiSebelum = posisiEfektifSeqNomorRM();

            PasienEntity tersimpan = manager.daftarkanPasien(dataPasien(0, nik));

            assertTrue(tersimpan.getIdPasien() > 0);
            assertTrue(tersimpan.getNomorRekamMedis().matches("^RM-[0-9]{6,}$"));
            assertEquals(1, posisiEfektifSeqNomorRM() - posisiSebelum,
                    "nomor rekam medis harus maju tepat satu");
            assertTrue(deskripsiAuditBaru().stream()
                    .anyMatch(d -> d.equals("Pendaftaran pasien " + tersimpan.getNomorRekamMedis())));
        }

        @Test
        void daftarkanPasien_menolakNIKDuplikat() throws SQLException {
            String nik = nikFiktif();
            nikUji.add(nik);
            manager.daftarkanPasien(dataPasien(0, nik));

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.daftarkanPasien(dataPasien(0, nik)));

            assertEquals("NIK sudah terdaftar", e.getMessage());
            assertEquals(1, jumlahBaris("SELECT count(*) FROM pasien WHERE nik = ?", nik));
        }

        @Test
        void perbaruiPasien_mengubahDataTanpaMengubahNomorRMDanMencatatAudit() throws SQLException {
            String nik = nikFiktif();
            nikUji.add(nik);
            PasienEntity tersimpan = manager.daftarkanPasien(dataPasien(0, nik));

            PasienEntity usulan = new PasienEntity(tersimpan.getIdPasien(), nikFiktif(), "RM-999999",
                    "Nama Diubah", LocalDate.of(1992, 2, 2), "Laki-laki", "Alamat Diubah", "0812");
            nikUji.add(usulan.getNik());

            manager.perbaruiPasien(usulan);

            try (PreparedStatement ps = koneksi.prepareStatement(
                    "SELECT nik, nama, tanggal_lahir, jenis_kelamin, alamat, nomor_telepon, nomor_rekam_medis "
                            + "FROM pasien WHERE id_pasien = ?")) {
                ps.setInt(1, tersimpan.getIdPasien());
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals(usulan.getNik(), rs.getString("nik"));
                    assertEquals("Nama Diubah", rs.getString("nama"));
                    assertEquals(Date.valueOf(LocalDate.of(1992, 2, 2)), rs.getDate("tanggal_lahir"));
                    assertEquals("Laki-laki", rs.getString("jenis_kelamin"));
                    assertEquals("Alamat Diubah", rs.getString("alamat"));
                    assertEquals("0812", rs.getString("nomor_telepon"));
                    assertEquals(tersimpan.getNomorRekamMedis(), rs.getString("nomor_rekam_medis"));
                }
            }
            assertTrue(deskripsiAuditBaru().stream()
                    .anyMatch(d -> d.equals("Pembaruan data pasien " + tersimpan.getNomorRekamMedis())));
        }

        @Test
        void perbaruiPasien_menolakNIKMilikPasienLain() throws SQLException {
            String nikA = nikFiktif();
            String nikB = nikFiktif();
            nikUji.add(nikA);
            nikUji.add(nikB);
            PasienEntity a = manager.daftarkanPasien(dataPasien(0, nikA));
            manager.daftarkanPasien(dataPasien(0, nikB));

            PasienEntity usulan = new PasienEntity(a.getIdPasien(), nikB, null, "Nama A",
                    LocalDate.of(1990, 1, 1), "Perempuan", "Alamat A", null);

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.perbaruiPasien(usulan));

            assertEquals("NIK sudah terdaftar", e.getMessage());
            assertEquals(nikA, kolom("SELECT nik FROM pasien WHERE id_pasien = ?", a.getIdPasien()));
        }

        private long posisiEfektifSeqNomorRM() throws SQLException {
            try (PreparedStatement ps = koneksi.prepareStatement("SELECT last_value, is_called FROM seq_nomor_rm");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean("is_called") ? rs.getLong("last_value") : 0L;
            }
        }

        private List<String> deskripsiAuditBaru() throws SQLException {
            List<String> hasil = new ArrayList<>();
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "SELECT deskripsi FROM audit WHERE id_audit > ?")) {
                ps.setLong(1, auditTerakhir);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        hasil.add(rs.getString(1));
                    }
                }
            }
            return hasil;
        }

        private int jumlahBaris(String sql, String param) throws SQLException {
            try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                ps.setString(1, param);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }

        private String kolom(String sql, int idPasien) throws SQLException {
            try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                ps.setInt(1, idPasien);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getString(1);
                }
            }
        }
    }
}
