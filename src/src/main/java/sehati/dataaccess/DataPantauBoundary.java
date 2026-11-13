package sehati.dataaccess;

import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.TindakLanjutEntity;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C60 - DataPantauBoundary.
 *
 * Membaca dan menyimpan DaftarPantauEntity dan TindakLanjutEntity beserta kueri penyaringnya.
 */
public class DataPantauBoundary {

    private static final Logger LOG = Logger.getLogger(DataPantauBoundary.class.getName());

    private static final String Q_C60_01 = "SELECT dp.id_entri, dp.id_pasien, dp.jenis_risiko, "
            + "dp.status_tindak_lanjut, COALESCE(pr.tanggal, jk.tanggal_rencana) AS tanggal_pemicu "
            + "FROM daftar_pantau dp "
            + "LEFT JOIN penanda_risiko pr ON pr.id_penanda = dp.id_penanda "
            + "LEFT JOIN jadwal_kontrol jk ON jk.id_jadwal = dp.id_jadwal "
            + "WHERE dp.status_tindak_lanjut <> 'selesai ditindaklanjuti' ORDER BY tanggal_pemicu, dp.id_entri";
    private static final String Q_C60_02 = "INSERT INTO daftar_pantau (id_pasien, jenis_risiko, "
            + "status_tindak_lanjut, id_penanda, id_jadwal) VALUES (?, ?, ?, ?, ?) "
            // Dua komputer yang membuka daftar pantau bersamaan tidak lagi gagal pada UNIQUE id_jadwal.
            + "ON CONFLICT (id_jadwal) DO NOTHING RETURNING id_entri";
    private static final String Q_C60_03 = "SELECT pr.id_penanda FROM penanda_risiko pr "
            + "JOIN kunjungan k ON k.id_kunjungan = pr.id_kunjungan "
            + "WHERE k.id_pasien = ? AND pr.jenis_risiko = ? ORDER BY pr.id_penanda DESC LIMIT 1";
    private static final String Q_C60_04 = "SELECT jk.id_jadwal FROM jadwal_kontrol jk "
            + "WHERE jk.id_pasien = ? AND jk.status = 'terjadwal' "
            + "AND NOT EXISTS (SELECT 1 FROM daftar_pantau dp WHERE dp.id_jadwal = jk.id_jadwal) "
            + "ORDER BY jk.tanggal_rencana, jk.id_jadwal LIMIT 1";
    private static final String Q_C60_05 = "UPDATE daftar_pantau SET status_tindak_lanjut = ? WHERE id_entri = ?";
    private static final String Q_C60_06 = "INSERT INTO tindak_lanjut (id_entri, jenis_kontak, hasil_upaya, "
            + "status_kedatangan, waktu) VALUES (?, ?, ?, ?, ?) RETURNING id_tindak_lanjut";

    private Connection koneksi;

    public DataPantauBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    /**
     * Entri yang belum selesai ditindaklanjuti beserta tanggal pemicunya (Q-C60-01, KF18).
     */
    public List<DaftarPantauEntity> ambilEntri() {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C60_01); ResultSet rs = ps.executeQuery()) {
            List<DaftarPantauEntity> hasil = new ArrayList<>();
            while (rs.next()) {
                DaftarPantauEntity e = new DaftarPantauEntity(rs.getInt("id_entri"), rs.getInt("id_pasien"),
                        rs.getString("jenis_risiko"), rs.getString("status_tindak_lanjut"));
                Date tanggal = rs.getDate("tanggal_pemicu");
                e.setTanggalPemicu(tanggal == null ? null : tanggal.toLocalDate());
                hasil.add(e);
            }
            return hasil;
        } catch (SQLException ex) {
            throw galat("Gagal mengambil daftar pantau", ex);
        }
    }

    /**
     * Entri lama hanya memutakhirkan status (Q-C60-05); entri baru mencari pemicunya sendiri (Algo-C60-01).
     */
    public void simpanEntri(DaftarPantauEntity e) {
        try {
            if (e.getIdEntri() != 0) {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C60_05)) {
                    ps.setString(1, e.getStatusTindakLanjut());
                    ps.setInt(2, e.getIdEntri());
                    ps.executeUpdate();
                }
                return;
            }
            Integer idPenanda = null;
            Integer idJadwal = null;
            if ("kunjungan ulang".equals(e.getJenisRisiko())) {
                idJadwal = cariId(Q_C60_04, e.getIdPasien(), null);
                if (idJadwal == null) {
                    // Jadwalnya sudah dibuatkan entri oleh komputer lain sesaat lalu, idEntri tetap 0.
                    return;
                }
            } else {
                idPenanda = cariId(Q_C60_03, e.getIdPasien(), e.getJenisRisiko());
            }
            if (idPenanda == null && idJadwal == null) {
                throw new IllegalStateException("Entri daftar pantau tidak memiliki pemicu");
            }
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C60_02)) {
                ps.setInt(1, e.getIdPasien());
                ps.setString(2, e.getJenisRisiko());
                ps.setString(3, e.getStatusTindakLanjut());
                ps.setObject(4, idPenanda, java.sql.Types.INTEGER);
                ps.setObject(5, idJadwal, java.sql.Types.INTEGER);
                try (ResultSet rs = ps.executeQuery()) {
                    // Tanpa baris berarti entri jadwal ini sudah dibuat komputer lain, idEntri tetap 0.
                    if (rs.next()) {
                        e.setIdEntri(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException ex) {
            throw galat("Gagal menyimpan entri daftar pantau", ex);
        }
    }

    /**
     * Setiap upaya menjadi baris baru, termasuk yang gagal (Q-C60-06, KF18).
     */
    public void simpanTindakLanjut(int idEntri, TindakLanjutEntity t) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C60_06)) {
            ps.setInt(1, idEntri);
            ps.setString(2, t.getJenisKontak());
            ps.setString(3, t.getHasilUpaya());
            ps.setString(4, t.getStatusKedatangan());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                t.setIdTindakLanjut(rs.getInt(1));
            }
        } catch (SQLException ex) {
            throw galat("Gagal menyimpan tindak lanjut", ex);
        }
    }

    private Integer cariId(String kueri, int idPasien, String jenis) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(kueri)) {
            ps.setInt(1, idPasien);
            if (jenis != null) {
                ps.setString(2, jenis);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private static RuntimeException galat(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ": " + e.getMessage(), e);
    }
}
