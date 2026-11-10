package sehati.dataaccess;

import sehati.business.entity.AntreanEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PoliEntity;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C58 - DataKunjunganBoundary.
 *
 * Membaca dan menyimpan KunjunganEntity, PoliEntity, dan AntreanEntity, serta mengunci baris urutan poli saat
 * nomor antrean diterbitkan.
 */
public class DataKunjunganBoundary {

    private static final Logger LOG = Logger.getLogger(DataKunjunganBoundary.class.getName());

    private static final String KOLOM_POLI = "SELECT id_poli, nama_poli, urutan_terakhir, tanggal_urutan FROM poli";
    private static final String KOLOM_KUNJUNGAN =
            "SELECT id_kunjungan, id_pasien, id_poli, tanggal, nomor_antrean, status FROM kunjungan";

    private static final String Q_C58_01 = KOLOM_POLI + " ORDER BY nama_poli";
    private static final String Q_C58_02 = KOLOM_POLI + " WHERE id_poli = ? FOR UPDATE";
    private static final String Q_C58_03 = "INSERT INTO poli (nama_poli, urutan_terakhir, tanggal_urutan) "
            + "VALUES (?, 0, CURRENT_DATE) RETURNING id_poli";
    private static final String Q_C58_04 = "UPDATE poli SET nama_poli = ?, urutan_terakhir = ?, tanggal_urutan = ? "
            + "WHERE id_poli = ?";
    private static final String Q_C58_05 = KOLOM_KUNJUNGAN
            + " WHERE id_pasien = ? AND tanggal = ? AND status <> 'selesai'";
    private static final String Q_C58_06 = KOLOM_KUNJUNGAN + " WHERE id_kunjungan = ?";
    private static final String Q_C58_07 = "INSERT INTO kunjungan (id_pasien, id_poli, tanggal, nomor_antrean, "
            + "status) VALUES (?, ?, ?, ?, ?) RETURNING id_kunjungan";
    private static final String Q_C58_08 = "UPDATE kunjungan SET status = ? WHERE id_kunjungan = ?";
    private static final String Q_C58_09 = "SELECT id_antrean, jenis FROM antrean WHERE jenis = ? FOR UPDATE";
    private static final String Q_C58_10 = "SELECT id_entri FROM antrean_entri WHERE id_antrean = ? ORDER BY urutan";
    private static final String Q_C58_11 = "DELETE FROM antrean_entri WHERE id_antrean = ?";
    private static final String Q_C58_12 = "INSERT INTO antrean_entri (id_antrean, urutan, id_entri) VALUES (?, ?, ?)";
    // Tambahan implementasi (B.1): daftar pasien untuk dokter pada I-06, urut nomor antrean.
    // Ditambah kunjungan yang resepnya dikembalikan apotek dan belum diganti resep revisi (UC07 alternatif 1)
    private static final String Q_C58_13 = KOLOM_KUNJUNGAN + " k WHERE tanggal = ? AND (status = 'siap diperiksa dokter'"
            + " OR (status = 'selesai diperiksa'"
            + " AND EXISTS (SELECT 1 FROM resep r WHERE r.id_kunjungan = k.id_kunjungan AND r.status = 'dikembalikan')"
            + " AND NOT EXISTS (SELECT 1 FROM resep r WHERE r.id_kunjungan = k.id_kunjungan AND r.status = 'menunggu')))"
            + " ORDER BY nomor_antrean, id_kunjungan";

    private Connection koneksi;

    public DataKunjunganBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    /**
     * Mengambil seluruh poli terurut nama (Q-C58-01).
     */
    public List<PoliEntity> ambilSemuaPoli() {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_01);
                ResultSet rs = ps.executeQuery()) {
            List<PoliEntity> hasil = new ArrayList<>();
            while (rs.next()) {
                hasil.add(petakanPoli(rs));
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil daftar poli", e);
        }
    }

    /**
     * Mengambil satu poli sambil mengunci barisnya sampai transaksi Manager selesai (Q-C58-02). Null bila poli
     * tidak ada.
     */
    public PoliEntity kunciPoli(int idPoli) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_02)) {
            ps.setInt(1, idPoli);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakanPoli(rs) : null;
            }
        } catch (SQLException e) {
            throw galat("Gagal mengunci poli", e);
        }
    }

    /**
     * Poli baru (idPoli 0) disisipkan (Q-C58-03), poli lama dimutakhirkan nama dan urutannya (Q-C58-04).
     */
    public void simpanPoli(PoliEntity p) {
        try {
            if (p.getIdPoli() == 0) {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_03)) {
                    ps.setString(1, p.getNamaPoli());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        p.setIdPoli(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_04)) {
                    ps.setString(1, p.getNamaPoli());
                    ps.setInt(2, p.getUrutanTerakhir());
                    ps.setDate(3, Date.valueOf(p.getTanggalUrutan()));
                    ps.setInt(4, p.getIdPoli());
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalArgumentException("Poli tidak ditemukan");
                    }
                }
            }
        } catch (SQLException | RuntimeException e) {
            throw galat("Gagal menyimpan poli", e);
        }
    }

    /**
     * Kunjungan pasien pada tanggal tersebut yang belum "selesai", atau null bila tidak ada (Q-C58-05).
     */
    public KunjunganEntity kunjunganAktif(int idPasien, LocalDate tanggal) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_05)) {
            ps.setInt(1, idPasien);
            ps.setDate(2, Date.valueOf(tanggal));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakanKunjungan(rs) : null;
            }
        } catch (SQLException e) {
            throw galat("Gagal mengambil kunjungan aktif", e);
        }
    }

    /**
     * Mengambil satu kunjungan, atau null bila tidak ada (Q-C58-06).
     */
    public KunjunganEntity ambilKunjungan(int idKunjungan) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_06)) {
            ps.setInt(1, idKunjungan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakanKunjungan(rs) : null;
            }
        } catch (SQLException e) {
            throw galat("Gagal mengambil kunjungan", e);
        }
    }

    /**
     * Mengambil kunjungan pada tanggal tertentu yang sudah diskrining dan menunggu dokter (Q-C58-13, UC05).
     */
    public List<KunjunganEntity> kunjunganSiapPeriksa(LocalDate tanggal) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_13)) {
            ps.setDate(1, Date.valueOf(tanggal));
            List<KunjunganEntity> hasil = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(petakanKunjungan(rs));
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil kunjungan siap diperiksa", e);
        }
    }

    /**
     * Kunjungan baru (idKunjungan 0) disisipkan (Q-C58-07), kunjungan lama hanya berubah statusnya (Q-C58-08).
     */
    public void simpanKunjungan(KunjunganEntity k) {
        try {
            if (k.getIdKunjungan() == 0) {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_07)) {
                    ps.setInt(1, k.getIdPasien());
                    ps.setInt(2, k.getIdPoli());
                    ps.setDate(3, Date.valueOf(k.getTanggal()));
                    ps.setInt(4, k.getNomorAntrean());
                    ps.setString(5, k.getStatus());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        k.setIdKunjungan(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_08)) {
                    ps.setString(1, k.getStatus());
                    ps.setInt(2, k.getIdKunjungan());
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalArgumentException("Kunjungan tidak ditemukan");
                    }
                }
            }
        } catch (SQLException | RuntimeException e) {
            throw galat("Gagal menyimpan kunjungan", e);
        }
    }

    /**
     * Algo-C58-01: mengambil antrean "skrining" atau "apotek" beserta isinya sambil mengunci baris antrean
     * (Q-C58-09, Q-C58-10).
     */
    public AntreanEntity ambilAntrean(String jenis) {
        try {
            AntreanEntity a;
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_09)) {
                ps.setString(1, jenis);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Jenis antrean tidak dikenal: " + jenis);
                    }
                    a = new AntreanEntity(rs.getInt("id_antrean"), rs.getString("jenis"), new ArrayList<>());
                }
            }
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_10)) {
                ps.setInt(1, a.getIdAntrean());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        a.getDaftarEntri().add(rs.getInt("id_entri"));
                    }
                }
            }
            return a;
        } catch (SQLException | RuntimeException e) {
            throw galat("Gagal mengambil antrean", e);
        }
    }

    /**
     * Algo-C58-01: menulis ulang seluruh isi antrean sesuai urutan daftarEntri (Q-C58-11, Q-C58-12). Dua kueri
     * tulis, jadi dibungkus transaksi sendiri bila dipanggil di luar transaksi Manager.
     */
    public void simpanAntrean(AntreanEntity a) {
        boolean transaksiSendiri = mulaiTransaksi();
        try {
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_11)) {
                ps.setInt(1, a.getIdAntrean());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C58_12)) {
                int urutan = 1;
                for (int idEntri : a.getDaftarEntri()) {
                    ps.setInt(1, a.getIdAntrean());
                    ps.setInt(2, urutan++);
                    ps.setInt(3, idEntri);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            selesaiTransaksi(transaksiSendiri, true);
        } catch (SQLException | RuntimeException e) {
            selesaiTransaksi(transaksiSendiri, false);
            throw galat("Gagal menyimpan antrean", e);
        } finally {
            akhiriTransaksi(transaksiSendiri);
        }
    }

    private static PoliEntity petakanPoli(ResultSet rs) throws SQLException {
        return new PoliEntity(rs.getInt("id_poli"), rs.getString("nama_poli"), rs.getInt("urutan_terakhir"),
                rs.getDate("tanggal_urutan").toLocalDate());
    }

    private static KunjunganEntity petakanKunjungan(ResultSet rs) throws SQLException {
        return new KunjunganEntity(rs.getInt("id_kunjungan"), rs.getInt("id_pasien"), rs.getInt("id_poli"),
                rs.getDate("tanggal").toLocalDate(), rs.getInt("nomor_antrean"), rs.getString("status"));
    }

    // Transaksi sendiri hanya bila dipanggil di luar transaksi Manager (aturan 1 Subbab 5.56, Algo-C59-02).
    private boolean mulaiTransaksi() {
        try {
            boolean sendiri = koneksi.getAutoCommit();
            if (sendiri) {
                koneksi.setAutoCommit(false);
            }
            return sendiri;
        } catch (SQLException e) {
            throw galat("Gagal memulai transaksi", e);
        }
    }

    private void selesaiTransaksi(boolean transaksiSendiri, boolean berhasil) {
        if (!transaksiSendiri) {
            return;
        }
        try {
            if (berhasil) {
                koneksi.commit();
            } else {
                koneksi.rollback();
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Gagal menutup transaksi", e);
        }
    }

    private void akhiriTransaksi(boolean transaksiSendiri) {
        if (!transaksiSendiri) {
            return;
        }
        try {
            koneksi.setAutoCommit(true);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Gagal mengembalikan auto-commit", e);
        }
    }

    // Aturan 3 Subbab 5.56: pelanggaran constraint menjadi IllegalArgumentException berpesan jelas, galat lain
    // menjadi IllegalStateException tanpa rincian teknis; rinciannya hanya masuk log.
    private static RuntimeException galat(String pesan, Exception e) {
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            return (RuntimeException) e;
        }
        if (e instanceof SQLException sql) {
            String state = sql.getSQLState();
            String rincian = sql.getMessage() == null ? "" : sql.getMessage();
            if ("23505".equals(state)) {
                if (rincian.contains("uq_kunjungan_aktif")) {
                    return new IllegalArgumentException("Pasien sudah memiliki kunjungan aktif hari ini", e);
                }
                if (rincian.contains("nama_poli")) {
                    return new IllegalArgumentException("Nama poli sudah dipakai", e);
                }
                return new IllegalArgumentException("Nomor antrean sudah terpakai, silakan ulangi", e);
            }
            if ("23514".equals(state)) {
                return new IllegalArgumentException("Data kunjungan atau poli tidak valid", e);
            }
            if ("23503".equals(state)) {
                return new IllegalArgumentException("Pasien atau poli tidak ditemukan", e);
            }
        }
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException("Basis data tidak dapat dihubungi", e);
    }
}
