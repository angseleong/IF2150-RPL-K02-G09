package sehati.dataaccess;

import sehati.business.entity.AuditEntity;
import sehati.business.entity.PenggunaEntity;
import sehati.business.entity.SesiEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C56 - DataAkunBoundary.
 *
 * Membaca dan menyimpan PenggunaEntity, SesiEntity, dan AuditEntity. Catatan audit hanya dapat ditambah, tidak
 * dapat diubah atau dihapus (KF16, KNF05), sehingga kelas ini sengaja tidak memiliki operasi untuk keduanya.
 */
public class DataAkunBoundary {

    private static final Logger LOG = Logger.getLogger(DataAkunBoundary.class.getName());

    private static final String Q_C56_01 = "SELECT id_pengguna, nama_pengguna, kata_sandi_hash, peran, status "
            + "FROM pengguna WHERE nama_pengguna = ?";
    private static final String Q_C56_02 = "SELECT id_pengguna, nama_pengguna, kata_sandi_hash, peran, status "
            + "FROM pengguna WHERE id_pengguna = ?";
    private static final String Q_C56_03 = "SELECT EXISTS (SELECT 1 FROM pengguna WHERE nama_pengguna = ?)";
    private static final String Q_C56_04 = "INSERT INTO pengguna (nama_pengguna, kata_sandi_hash, peran, status) "
            + "VALUES (?, ?, ?, ?) RETURNING id_pengguna";
    private static final String Q_C56_05 = "UPDATE pengguna SET kata_sandi_hash = ?, peran = ?, status = ? "
            + "WHERE id_pengguna = ?";
    private static final String Q_C56_06 = "INSERT INTO sesi (id_sesi, id_pengguna, waktu_mulai, status_aktif) "
            + "VALUES (?, ?, ?, ?) ON CONFLICT (id_sesi) DO UPDATE SET status_aktif = EXCLUDED.status_aktif";
    private static final String Q_C56_07 = "UPDATE sesi SET status_aktif = FALSE WHERE id_pengguna = ? AND status_aktif";
    private static final String Q_C56_08 = "INSERT INTO audit (id_pengguna, waktu, deskripsi) VALUES (?, ?, ?)";

    private Connection koneksi;

    public DataAkunBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    /**
     * Mengambil akun dengan nama persis sama, atau null bila tidak terdaftar (Q-C56-01, UC01).
     */
    public PenggunaEntity cariPengguna(String nama) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C56_01)) {
            ps.setString(1, nama);
            return bacaPengguna(ps);
        } catch (SQLException e) {
            throw galat("Gagal mencari pengguna", e);
        }
    }

    /**
     * Mengambil akun berdasarkan ID, atau null bila tidak ada (Q-C56-02, UC12).
     */
    public PenggunaEntity ambilPengguna(int idPengguna) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C56_02)) {
            ps.setInt(1, idPengguna);
            return bacaPengguna(ps);
        } catch (SQLException e) {
            throw galat("Gagal mengambil pengguna", e);
        }
    }

    /**
     * Mengembalikan true bila nama sudah dipakai akun lain, aktif maupun nonaktif (Q-C56-03, UC12).
     */
    public boolean namaPenggunaDipakai(String nama) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C56_03)) {
            ps.setString(1, nama);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw galat("Gagal memeriksa nama pengguna", e);
        }
    }

    /**
     * Menyisipkan akun baru lalu mengisi idPengguna (Q-C56-04), atau memutakhirkan hash, peran, dan status akun lama
     * (Q-C56-05). Nama pengguna tidak ikut diubah.
     */
    public void simpanPengguna(PenggunaEntity p) {
        try {
            if (p.getIdPengguna() == 0) {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C56_04)) {
                    ps.setString(1, p.getNamaPengguna());
                    ps.setString(2, p.getKataSandiHash());
                    ps.setString(3, p.getPeran());
                    ps.setString(4, p.getStatus());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        p.setIdPengguna(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C56_05)) {
                    ps.setString(1, p.getKataSandiHash());
                    ps.setString(2, p.getPeran());
                    ps.setString(3, p.getStatus());
                    ps.setInt(4, p.getIdPengguna());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw galat("Gagal menyimpan pengguna", e);
        }
    }

    /**
     * Menyimpan sesi baru atau memutakhirkan status sesi yang ditutup (Q-C56-06).
     */
    public void simpanSesi(SesiEntity s) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C56_06)) {
            ps.setObject(1, s.getIdSesi());
            ps.setInt(2, s.getIdPengguna());
            ps.setTimestamp(3, Timestamp.valueOf(s.getWaktuMulai()));
            ps.setBoolean(4, s.isStatusAktif());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw galat("Gagal menyimpan sesi", e);
        }
    }

    /**
     * Menutup seluruh sesi aktif milik satu akun saat akun dinonaktifkan (Q-C56-07, UC12).
     */
    public void tutupSesiAktif(int idPengguna) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C56_07)) {
            ps.setInt(1, idPengguna);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw galat("Gagal menutup sesi aktif", e);
        }
    }

    /**
     * Menambah satu catatan jejak audit (Q-C56-08, KF16).
     */
    public void simpanAudit(AuditEntity a) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C56_08)) {
            ps.setInt(1, a.getIdPengguna());
            ps.setTimestamp(2, Timestamp.valueOf(a.getWaktu()));
            ps.setString(3, a.getDeskripsi());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw galat("Gagal menyimpan audit", e);
        }
    }

    private static PenggunaEntity bacaPengguna(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                return null;
            }
            return new PenggunaEntity(rs.getInt("id_pengguna"), rs.getString("nama_pengguna"),
                    rs.getString("kata_sandi_hash"), rs.getString("peran"), rs.getString("status"));
        }
    }

    /**
     * Menerjemahkan SQLException sesuai aturan 3 Subbab 5.56. Kata sandi asli tidak pernah sampai ke kelas ini, hanya
     * hash bcrypt, sehingga rincian galat aman dicatat ke log (KNF04).
     */
    private static RuntimeException galat(String pesan, SQLException e) {
        String state = e.getSQLState();
        if ("23505".equals(state)) {
            return new IllegalArgumentException("Nama pengguna sudah dipakai", e);
        }
        if ("23514".equals(state)) {
            return new IllegalArgumentException("Peran atau status akun tidak valid", e);
        }
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException("Basis data tidak dapat dihubungi", e);
    }
}
