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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KunjunganManagerTest {

    private final KunjunganManager manager = new KunjunganManager();

    @Test
    void bukaKunjungan_menolakPasienTidakValid() {
        assertThrows(IllegalArgumentException.class, () -> manager.bukaKunjungan(0, 1));
        assertThrows(IllegalArgumentException.class, () -> manager.bukaKunjungan(-1, 1));
    }

    @Test
    void bukaKunjungan_menolakPoliTidakValid() {
        assertThrows(IllegalArgumentException.class, () -> manager.bukaKunjungan(1, 0));
        assertThrows(IllegalArgumentException.class, () -> manager.bukaKunjungan(1, -1));
    }

    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private Connection koneksi;
        private long auditTerakhir = -1;
        private int idPasienUji = -1;
        private int idPoliUji = -1;
        private final List<Integer> idKunjungan = new ArrayList<>();

        @BeforeEach
        void pasang() throws SQLException {
            koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true));
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "SELECT COALESCE(MAX(id_audit), 0) FROM audit");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                auditTerakhir = rs.getLong(1);
            }
            idPasienUji = sisipPasien();
            idPoliUji = sisipPoli();
        }

        @AfterEach
        void bersihikan() throws SQLException {
            SesiAktif.lepas();
            for (int id : idKunjungan) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM antrean_entri WHERE id_entri = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM kunjungan WHERE id_kunjungan = ?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }
            if (idPoliUji > 0) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM poli WHERE id_poli = ?")) {
                    ps.setInt(1, idPoliUji);
                    ps.executeUpdate();
                }
            }
            if (idPasienUji > 0) {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM rekam_medis WHERE id_pasien = ?")) {
                    ps.setInt(1, idPasienUji);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM pasien WHERE id_pasien = ?")) {
                    ps.setInt(1, idPasienUji);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "DELETE FROM audit WHERE id_audit > ? AND deskripsi LIKE 'Kunjungan baru, antrean %'")) {
                ps.setLong(1, auditTerakhir);
                ps.executeUpdate();
            }
            koneksi.close();
        }

        @Test
        void bukaKunjungan_menerbitkanNomorSatuLaluMencegahKunjunganGanda() {
            KunjunganEntity pertama = manager.bukaKunjungan(idPasienUji, idPoliUji);
            idKunjungan.add(pertama.getIdKunjungan());

            assertTrue(pertama.getIdKunjungan() > 0);
            assertEquals(1, pertama.getNomorAntrean());
            assertEquals("menunggu skrining", pertama.getStatus());
            assertEquals(LocalDate.now(), pertama.getTanggal());

            KunjunganEntity kedua = manager.bukaKunjungan(idPasienUji, idPoliUji);

            assertEquals(pertama.getIdKunjungan(), kedua.getIdKunjungan());
            assertEquals(pertama.getNomorAntrean(), kedua.getNomorAntrean());
        }

        @Test
        void bukaKunjungan_menerbitkanNomorBerurutanPerPoli() throws SQLException {
            int idPasienLain = sisipPasien();
            try {
                KunjunganEntity pertama = manager.bukaKunjungan(idPasienUji, idPoliUji);
                idKunjungan.add(pertama.getIdKunjungan());
                KunjunganEntity kedua = manager.bukaKunjungan(idPasienLain, idPoliUji);
                idKunjungan.add(kedua.getIdKunjungan());

                assertEquals(1, pertama.getNomorAntrean());
                assertEquals(2, kedua.getNomorAntrean());
            } finally {
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM rekam_medis WHERE id_pasien = ?")) {
                    ps.setInt(1, idPasienLain);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = koneksi.prepareStatement(
                        "DELETE FROM pasien WHERE id_pasien = ?")) {
                    ps.setInt(1, idPasienLain);
                    ps.executeUpdate();
                }
            }
        }

        @Test
        void bukaKunjungan_menolakPoliYangTidakAda() {
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                    () -> manager.bukaKunjungan(idPasienUji, 999_999));

            assertEquals("Poli tidak ditemukan", e.getMessage());
        }

        private int sisipPasien() throws SQLException {
            String angka = UUID.randomUUID().toString().replace("-", "").replaceAll("[a-f]", "7");
            String nik = "9" + angka.substring(0, 7) + "00000000";
            int id;
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO pasien (nik, nomor_rekam_medis, nama, tanggal_lahir, jenis_kelamin, alamat) "
                            + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id_pasien")) {
                ps.setString(1, nik);
                ps.setString(2, "RM-" + nik.substring(0, 9));
                ps.setString(3, "Pasien Uji Kunjungan");
                ps.setDate(4, Date.valueOf(LocalDate.of(1990, 1, 1)));
                ps.setString(5, "Perempuan");
                ps.setString(6, "Jl. Uji No. 1");
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    id = rs.getInt(1);
                }
            }
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO rekam_medis (id_pasien) VALUES (?)")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            return id;
        }

        private int sisipPoli() throws SQLException {
            try (PreparedStatement ps = koneksi.prepareStatement(
                    "INSERT INTO poli (nama_poli, urutan_terakhir, tanggal_urutan) "
                            + "VALUES (?, 0, CURRENT_DATE) RETURNING id_poli")) {
                ps.setString(1, "Poli Uji " + UUID.randomUUID().toString().substring(0, 8));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        }
    }
}
