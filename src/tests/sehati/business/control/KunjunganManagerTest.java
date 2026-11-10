package sehati.business.control;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true));
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
        }

        @Test
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
        void bukaKunjungan_menolakPoliTidakDitemukan() throws SQLException {
            int idPasien = buatPasienDenganRekamMedis();

            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.bukaKunjungan(idPasien, 999999));

            assertEquals("Poli tidak ditemukan", e.getMessage());
            assertEquals(0, jumlahKunjunganRows(idPasien, -1));
        }

        private int buatPasienDenganRekamMedis() throws SQLException {
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
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO rekam_medis (id_pasien) VALUES (?)")) {
                ps.setInt(1, idPasien);
                ps.executeUpdate();
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
