package sehati.business.control;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PoliEntity;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KunjunganManagerTest {

    private final KunjunganManager manager = new KunjunganManager();

    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private final List<Integer> idKunjunganUji = new ArrayList<>();
        private final List<Integer> idPasienUji = new ArrayList<>();
        private final List<Integer> idPoliUji = new ArrayList<>();
        private long auditTerakhir = -1;
        private Connection koneksi;

        @BeforeEach
        void pasang() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Petugas Administrasi");
            try (PreparedStatement ps = koneksi.prepareStatement("SELECT COALESCE(MAX(id_audit), 0) FROM audit");
                    ResultSet rs = ps.executeQuery()) {
                rs.next();
                auditTerakhir = rs.getLong(1);
            }
        }

        @AfterEach
        void bersihkan() throws SQLException {
            SesiAktif.lepas();
            // Antrean skrining dipakai bersama, jadi hanya entri milik kunjungan uji yang dihapus.
            for (int idKunjungan : idKunjunganUji) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM antrean_entri WHERE id_entri = ?")) {
                    ps.setInt(1, idKunjungan);
                    ps.executeUpdate();
                }
            }
            for (int idKunjungan : idKunjunganUji) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM kunjungan WHERE id_kunjungan = ?")) {
                    ps.setInt(1, idKunjungan);
                    ps.executeUpdate();
                }
            }
            for (int idPasien : idPasienUji) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM rekam_medis WHERE id_pasien = ?")) {
                    ps.setInt(1, idPasien);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM pasien WHERE id_pasien = ?")) {
                    ps.setInt(1, idPasien);
                    ps.executeUpdate();
                }
            }
            for (int idPoli : idPoliUji) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM poli WHERE id_poli = ?")) {
                    ps.setInt(1, idPoli);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "DELETE FROM audit WHERE id_audit > ? AND deskripsi LIKE 'Kunjungan baru,%'")) {
                ps.setLong(1, auditTerakhir);
                ps.executeUpdate();
            }
            koneksi.close();
        }

        @Test
        @DisplayName("C19-UT01 daftarPoli memuat seluruh poli seed terurut nama")
        void daftarPoli_memuatSeluruhPoliTerurutNama() {
            List<PoliEntity> poli = manager.daftarPoli();

            assertTrue(poli.size() >= 4);
            assertTrue(poli.stream().anyMatch(p -> p.getNamaPoli().equals("Poli Umum")));
            for (int i = 1; i < poli.size(); i++) {
                assertTrue(poli.get(i - 1).getNamaPoli().compareToIgnoreCase(poli.get(i).getNamaPoli()) <= 0);
            }
        }

        @Test
        @DisplayName("C19-UT02 daftarPoli memuat poli yang baru disimpan")
        void daftarPoli_memuatPoliBaru() throws SQLException {
            int idPoli = buatPoli();

            assertTrue(manager.daftarPoli().stream().anyMatch(p -> p.getIdPoli() == idPoli));
        }

        @Test
        @DisplayName("C19-UT03 daftarPoli melaporkan galat basis data dengan pesan ramah")
        void daftarPoli_melaporkanGalatBasisData() throws SQLException {
            String penggunaApp = penggunaAplikasi();
            try (Statement st = koneksi.createStatement()) {
                st.execute("REVOKE SELECT ON poli FROM " + penggunaApp);
                try {
                    IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.daftarPoli());
                    assertEquals("Basis data tidak dapat dihubungi", e.getMessage());
                } finally {
                    st.execute("GRANT SELECT ON poli TO " + penggunaApp);
                }
            }
        }

        @Test
        @DisplayName("C19-UT04 kunjunganAktif null sebelum kunjungan dibuka")
        void kunjunganAktif_nullSebelumDibuka() throws SQLException {
            int idPasien = buatPasienDenganRekamMedis();

            assertNull(manager.kunjunganAktif(idPasien));
        }

        @Test
        @DisplayName("C19-UT05 kunjunganAktif mengembalikan kunjungan yang baru dibuka")
        void kunjunganAktif_mengembalikanKunjunganYangSama() throws SQLException {
            int idPasien = buatPasienDenganRekamMedis();
            int idPoli = buatPoli();

            KunjunganEntity dibuka = manager.bukaKunjungan(idPasien, idPoli);
            idKunjunganUji.add(dibuka.getIdKunjungan());

            assertEquals(dibuka.getIdKunjungan(), manager.kunjunganAktif(idPasien).getIdKunjungan());
        }

        @Test
        @DisplayName("C19-UT06 kunjunganAktif mengabaikan kunjungan yang sudah selesai")
        void kunjunganAktif_mengabaikanKunjunganSelesai() throws SQLException {
            int idPasien = buatPasienDenganRekamMedis();
            int idPoli = buatPoli();
            KunjunganEntity dibuka = manager.bukaKunjungan(idPasien, idPoli);
            idKunjunganUji.add(dibuka.getIdKunjungan());

            try (PreparedStatement ps = koneksi.prepareStatement(
                    "UPDATE kunjungan SET status = 'selesai' WHERE id_kunjungan = ?")) {
                ps.setInt(1, dibuka.getIdKunjungan());
                ps.executeUpdate();
            }

            assertNull(manager.kunjunganAktif(idPasien));
        }

        @Test
        @DisplayName("C19-UT07 bukaKunjungan menerbitkan nomor berurutan per poli dan mencatat audit")
        void bukaKunjungan_menerbitkanNomorBerurutanPerPoliDanMencatatAudit() throws SQLException {
            int idPasien = buatPasienDenganRekamMedis();
            int idPoli = buatPoli();

            KunjunganEntity pertama = manager.bukaKunjungan(idPasien, idPoli);
            KunjunganEntity kedua = manager.bukaKunjungan(buatPasienDenganRekamMedis(), idPoli);

            idKunjunganUji.add(pertama.getIdKunjungan());
            idKunjunganUji.add(kedua.getIdKunjungan());
            assertEquals("menunggu skrining", pertama.getStatus());
            assertEquals("menunggu skrining", kedua.getStatus());
            assertEquals(1, pertama.getNomorAntrean());
            assertEquals(2, kedua.getNomorAntrean(), "nomor antrean lanjut mengurut di poli yang sama");
            assertEquals(1, jumlahKunjunganRows(pertama.getIdPasien(), idPoli));
            assertTrue(deskripsiAuditBaru().contains("Kunjungan baru, antrean 1"));
            assertTrue(deskripsiAuditBaru().contains("Kunjungan baru, antrean 2"));
        }

        @Test
        @DisplayName("C19-UT08 bukaKunjungan mengembalikan kunjungan aktif tanpa nomor baru")
        void bukaKunjungan_kunjunganAktifMengembalikanKunjunganSamaTanpaNomorBaru() throws SQLException {
            int idPasien = buatPasienDenganRekamMedis();
            int idPoli = buatPoli();
            idKunjunganUji.add(manager.bukaKunjungan(idPasien, idPoli).getIdKunjungan());

            KunjunganEntity ulang = manager.bukaKunjungan(idPasien, idPoli);

            assertEquals(1, ulang.getNomorAntrean());
            assertEquals(1, jumlahKunjunganRows(idPasien, idPoli),
                    "permintaan ulang tidak menerbitkan kunjungan maupun nomor baru (Algo-C19-01)");
        }

        @Test
        @DisplayName("C19-UT09 bukaKunjungan menolak poli yang tidak ditemukan")
        void bukaKunjungan_menolakPoliTidakDitemukan() throws SQLException {
            int idPasien = buatPasienDenganRekamMedis();

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.bukaKunjungan(idPasien, 999999));

            assertEquals("Poli tidak ditemukan", e.getMessage());
            assertEquals(0, jumlahKunjunganRows(idPasien, -1));
        }

        @Test
        @DisplayName("C19-UT10 bukaKunjungan membatalkan seluruh transaksi bila pasien tanpa rekam medis")
        void bukaKunjungan_membatalkanTransaksiTanpaRekamMedis() throws SQLException {
            int idPasien = buatPasienTanpaRekamMedis();
            int idPoli = buatPoli();
            String urutanSebelum = urutanPoli(idPoli);

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.bukaKunjungan(idPasien, idPoli));

            assertEquals("Pasien belum memiliki rekam medis", e.getMessage());
            assertEquals(0, jumlahKunjunganRows(idPasien, idPoli));
            assertEquals(urutanSebelum, urutanPoli(idPoli), "nomor antrean yang sudah terbit harus di-rollback");
            assertTrue(deskripsiAuditBaru().isEmpty());
        }

        @Test
        @DisplayName("C19-UT11 bukaKunjungan menolak tanpa sesi masuk")
        void bukaKunjungan_menolakTanpaSesi() throws SQLException {
            int idPasien = buatPasienDenganRekamMedis();
            int idPoli = buatPoli();
            SesiAktif.lepas();

            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> manager.bukaKunjungan(idPasien, idPoli));

            assertEquals("Sesi sudah berakhir, silakan masuk kembali", e.getMessage());
            assertEquals(0, jumlahKunjunganRows(idPasien, idPoli));
        }

        private String penggunaAplikasi() throws SQLException {
            try (Connection k = KoneksiBasisData.buka();
                 PreparedStatement ps = k.prepareStatement("SELECT current_user");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getString(1);
            }
        }

        private String urutanPoli(int idPoli) throws SQLException {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "SELECT urutan_terakhir, tanggal_urutan FROM poli WHERE id_poli = ?")) {
                ps.setInt(1, idPoli);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1) + "|" + rs.getDate(2);
                }
            }
        }

        private List<String> deskripsiAuditBaru() throws SQLException {
            List<String> hasil = new ArrayList<>();
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "SELECT deskripsi FROM audit WHERE id_audit > ? ORDER BY id_audit")) {
                ps.setLong(1, auditTerakhir);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        hasil.add(rs.getString(1));
                    }
                }
            }
            return hasil;
        }

        private int buatPasienDenganRekamMedis() throws SQLException {
            int idPasien = buatPasienTanpaRekamMedis();
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO rekam_medis (id_pasien) VALUES (?)")) {
                ps.setInt(1, idPasien);
                ps.executeUpdate();
            }
            return idPasien;
        }

        private int buatPasienTanpaRekamMedis() throws SQLException {
            String nik = "9" + UUID.randomUUID().toString().replace("-", "")
                    .replaceAll("[a-f]", "7").substring(0, 15);
            int idPasien;
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO pasien (nik, nomor_rekam_medis, nama, tanggal_lahir, jenis_kelamin, alamat) "
                            + "VALUES (?, ?, 'Nama Uji Kunjungan', ?, 'Perempuan', 'Jl. Uji No. 1') "
                            + "RETURNING id_pasien")) {
                ps.setString(1, nik);
                ps.setString(2, "RM-9" + String.format("%06d", (int) (Math.random() * 900000) + 100000));
                ps.setDate(3, Date.valueOf("1990-01-01"));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    idPasien = rs.getInt(1);
                }
            }
            idPasienUji.add(idPasien);
            return idPasien;
        }

        private int buatPoli() throws SQLException {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO poli (nama_poli) VALUES (?) RETURNING id_poli")) {
                ps.setString(1, "Poli Uji " + UUID.randomUUID().toString().substring(0, 8));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int idPoli = rs.getInt(1);
                    idPoliUji.add(idPoli);
                    return idPoli;
                }
            }
        }

        private int jumlahKunjunganRows(int idPasien, int idPoli) throws SQLException {
            // idPoli -1 dipakai bila penolakan terjadi sebelum ada poli, sehingga hanya jumlah milik pasien yang
            // dihitung.
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "SELECT count(*) FROM kunjungan WHERE id_pasien = ? AND (? = -1 OR id_poli = ?)")) {
                ps.setInt(1, idPasien);
                ps.setInt(2, idPoli);
                ps.setInt(3, idPoli);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }
    }
}
