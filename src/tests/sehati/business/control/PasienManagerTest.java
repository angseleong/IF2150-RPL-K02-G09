package sehati.business.control;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasienManagerTest {

    private final PasienManager manager = new PasienManager();

    @Test
    @DisplayName("C18-UT01 validasiNIK menerima tepat 16 digit")
    void validasiNIK_menerimaTepat16Digit() {
        assertTrue(manager.validasiNIK("9999010101010001"));
    }

    @Test
    @DisplayName("C18-UT02 validasiNIK menolak kurang dari 16 digit")
    void validasiNIK_menolakKurangDari16Digit() {
        assertFalse(manager.validasiNIK("999901010101001"));
    }

    @Test
    @DisplayName("C18-UT03 validasiNIK menolak lebih dari 16 digit")
    void validasiNIK_menolakLebihDari16Digit() {
        assertFalse(manager.validasiNIK("99990101010100017"));
    }

    @Test
    @DisplayName("C18-UT04 validasiNIK menolak karakter bukan digit")
    void validasiNIK_menolakBukanDigit() {
        assertFalse(manager.validasiNIK("999901010101000A"));
    }

    @Test
    @DisplayName("C18-UT05 validasiNIK menolak null")
    void validasiNIK_menolakNull() {
        assertFalse(manager.validasiNIK(null));
    }

    @Test
    @DisplayName("C18-UT06 cariPasien mengembalikan daftar kosong untuk kata kunci kosong")
    void cariPasien_kataKunciKosongMengembalikanDaftarKosong() {
        assertEquals(List.of(), manager.cariPasien("   "));
        assertEquals(List.of(), manager.cariPasien(null));
    }

    @Test
    @DisplayName("C18-UT12 daftarkanPasien menolak NIK tidak valid")
    void daftarkanPasien_menolakNIKTidakValid() {
        PasienEntity data = dataPasien(0, "999901010101001");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.daftarkanPasien(data));

        assertEquals("NIK tidak valid", e.getMessage());
    }

    @Test
    @DisplayName("C18-UT13 daftarkanPasien menolak data pasien null")
    void daftarkanPasien_menolakDataNull() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.daftarkanPasien(null));

        assertEquals("Data pasien kosong", e.getMessage());
    }

    @Test
    @DisplayName("C18-UT18 perbaruiPasien menolak data pasien null")
    void perbaruiPasien_menolakDataNull() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.perbaruiPasien(null));

        assertEquals("Data pasien kosong", e.getMessage());
    }

    @Test
    @DisplayName("C18-UT19 perbaruiPasien menolak NIK tidak valid")
    void perbaruiPasien_menolakNIKTidakValid() {
        PasienEntity data = dataPasien(1, "123");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.perbaruiPasien(data));

        assertEquals("NIK tidak valid", e.getMessage());
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
        @DisplayName("C18-UT07 cariPasien menemukan pasien seed dari nama parsial")
        void cariPasien_menemukanPasienDariNamaParsial() {
            List<PasienEntity> hasil = manager.cariPasien("  budi ");

            assertTrue(hasil.stream().anyMatch(p -> p.getNama().equals("Budi Santoso")));
            assertTrue(hasil.stream().allMatch(p -> p.getNama().toLowerCase().contains("budi")
                    || p.getNik().contains("budi") || p.getNomorRekamMedis().toLowerCase().contains("budi")));
        }

        @Test
        @DisplayName("C18-UT08 cariPasien memperlakukan wildcard sebagai teks biasa")
        void cariPasien_memperlakukanWildcardSebagaiTeksBiasa() {
            assertEquals(List.of(), manager.cariPasien("%"));
            assertEquals(List.of(), manager.cariPasien("_"));
        }

        @Test
        @DisplayName("C18-UT09 cariPasien mengembalikan daftar kosong bila tidak ada yang cocok")
        void cariPasien_tidakAdaYangCocok() {
            assertEquals(List.of(), manager.cariPasien("zzzz-tidak-ada"));
        }

        @Test
        @DisplayName("C18-UT10 daftarkanPasien menerbitkan tepat satu nomor RM dan mencatat audit")
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
        @DisplayName("C18-UT11 daftarkanPasien menerima data opsional kosong dan menyimpannya NULL")
        void daftarkanPasien_menerimaDataOpsionalKosong() throws SQLException {
            String nik = nikFiktif();
            nikUji.add(nik);

            PasienEntity tersimpan = manager.daftarkanPasien(dataPasien(0, nik));

            assertNull(tersimpan.getNomorTelepon());
            assertNull(kolom("SELECT nomor_telepon FROM pasien WHERE id_pasien = ?", tersimpan.getIdPasien()));
        }

        @Test
        @DisplayName("C18-UT14 daftarkanPasien menolak NIK duplikat")
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
        @DisplayName("C18-UT15 daftarkanPasien menolak tanpa sesi masuk")
        void daftarkanPasien_menolakTanpaSesi() {
            String nik = nikFiktif();
            nikUji.add(nik);
            SesiAktif.lepas();

            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> manager.daftarkanPasien(dataPasien(0, nik)));

            assertEquals("Sesi sudah berakhir, silakan masuk kembali", e.getMessage());
        }

        @Test
        @DisplayName("C18-UT16 perbaruiPasien mengubah data tanpa mengubah nomor RM dan mencatat audit")
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
        @DisplayName("C18-UT17 perbaruiPasien menerima NIK yang tidak berubah")
        void perbaruiPasien_menerimaNIKYangTidakBerubah() throws SQLException {
            String nik = nikFiktif();
            nikUji.add(nik);
            PasienEntity tersimpan = manager.daftarkanPasien(dataPasien(0, nik));

            PasienEntity usulan = new PasienEntity(tersimpan.getIdPasien(), nik, null, "Nama Tetap NIK",
                    LocalDate.of(1990, 1, 1), "Perempuan", "Jl. Uji No. 1", null);

            manager.perbaruiPasien(usulan);

            assertEquals("Nama Tetap NIK",
                    kolom("SELECT nama FROM pasien WHERE id_pasien = ?", tersimpan.getIdPasien()));
        }

        @Test
        @DisplayName("C18-UT20 perbaruiPasien menolak pasien yang tidak ditemukan")
        void perbaruiPasien_menolakPasienTidakDitemukan() {
            PasienEntity usulan = dataPasien(999999, nikFiktif());

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.perbaruiPasien(usulan));

            assertEquals("Pasien tidak ditemukan", e.getMessage());
        }

        @Test
        @DisplayName("C18-UT21 perbaruiPasien menolak NIK milik pasien lain")
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

        @Test
        @DisplayName("C18-UT22 pasienTerbaru memuat daftar pasien terurut dari yang terbaru")
        void pasienTerbaru_memuatDaftarTerurut() {
            List<PasienEntity> hasil = manager.pasienTerbaru();

            assertTrue(hasil.size() >= 8, "seed berisi sedikitnya 8 pasien");
            assertTrue(hasil.size() <= 50);
            assertTrue(hasil.stream().anyMatch(p -> p.getNama().equals("Budi Santoso")));
            for (int i = 1; i < hasil.size(); i++) {
                assertTrue(hasil.get(i - 1).getIdPasien() >= hasil.get(i).getIdPasien());
            }
        }

        @Test
        @DisplayName("C18-UT23 pasienTerbaru dibatasi 50 baris")
        void pasienTerbaru_dibatasiLimaPuluhBaris() throws SQLException {
            int maksimal = 0;
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO pasien (nik, nomor_rekam_medis, nama, tanggal_lahir, jenis_kelamin, alamat) "
                            + "VALUES (?, ?, ?, DATE '1990-01-01', 'Perempuan', 'Jl. Uji') RETURNING id_pasien")) {
                for (int i = 0; i < 55; i++) {
                    ps.setString(1, "8888" + String.format("%012d", i));
                    ps.setString(2, "RM-88" + String.format("%04d", i));
                    ps.setString(3, "Pasien Batas " + i);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        maksimal = Math.max(maksimal, rs.getInt(1));
                    }
                }
            }
            try {
                List<PasienEntity> hasil = manager.pasienTerbaru();

                assertEquals(50, hasil.size());
                assertEquals(maksimal, hasil.get(0).getIdPasien());
            } finally {
                try (PreparedStatement ps = koneksi.prepareStatement("DELETE FROM pasien WHERE nik LIKE '8888%'")) {
                    ps.executeUpdate();
                }
            }
        }

        @Test
        @DisplayName("C18-UT24 pasienTerbaru melaporkan galat basis data dengan pesan ramah")
        void pasienTerbaru_melaporkanGalatBasisData() throws SQLException {
            String penggunaApp = penggunaAplikasi();
            try (Statement st = koneksi.createStatement()) {
                st.execute("REVOKE SELECT ON pasien FROM " + penggunaApp);
                try {
                    IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.pasienTerbaru());
                    assertEquals("Basis data tidak dapat dihubungi", e.getMessage());
                } finally {
                    st.execute("GRANT SELECT ON pasien TO " + penggunaApp);
                }
            }
        }

        private String penggunaAplikasi() throws SQLException {
            try (Connection k = KoneksiBasisData.buka();
                 PreparedStatement ps = k.prepareStatement("SELECT current_user");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
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
