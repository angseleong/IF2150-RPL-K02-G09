package sehati.dataaccess;

import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.business.entity.ResepEntity;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C61 - DataFarmasiBoundary.
 *
 * Membaca dan menyimpan ResepEntity, ItemResepEntity, ObatEntity, BetsObatEntity, dan PenerimaanObatEntity, serta
 * mengunci baris stok saat pemotongan agar tidak terjadi lost update.
 */
public class DataFarmasiBoundary {

    private static final Logger LOG = Logger.getLogger(DataFarmasiBoundary.class.getName());

    private static final String Q_C61_01 = "SELECT id_obat, nama_obat, sediaan, stok, ambang_minimum "
            + "FROM obat ORDER BY nama_obat";
    private static final String Q_C61_02 = "SELECT id_obat, nama_obat, sediaan, stok, ambang_minimum "
            + "FROM obat WHERE id_obat = ? FOR UPDATE";
    private static final String Q_C61_03 = "SELECT nomor_bets, id_obat, jumlah, tanggal_kedaluwarsa "
            + "FROM bets_obat WHERE id_obat = ? AND jumlah > 0 "
            + "ORDER BY tanggal_kedaluwarsa, nomor_bets FOR UPDATE";
    private static final String Q_C61_04 = "UPDATE obat SET stok = ? WHERE id_obat = ?";
    private static final String Q_C61_05 = "UPDATE bets_obat SET jumlah = ? WHERE id_obat = ? AND nomor_bets = ?";
    private static final String Q_C61_06 = "INSERT INTO penerimaan_obat (nomor_faktur, tanggal_terima) VALUES (?, ?) "
            + "RETURNING id_penerimaan";
    private static final String Q_C61_07 = "INSERT INTO bets_obat (id_obat, nomor_bets, jumlah, tanggal_kedaluwarsa, "
            + "id_penerimaan) VALUES (?, ?, ?, ?, ?)";
    private static final String Q_C61_08 = "SELECT o.id_obat, o.nama_obat, o.sediaan, o.stok, o.ambang_minimum "
            + "FROM obat o WHERE o.stok < o.ambang_minimum "
            + "OR EXISTS (SELECT 1 FROM bets_obat b WHERE b.id_obat = o.id_obat "
            + "AND b.jumlah > 0 AND b.tanggal_kedaluwarsa <= CURRENT_DATE + ?::int) "
            + "ORDER BY o.nama_obat";
    private static final String Q_C61_09 = "INSERT INTO resep (id_kunjungan, waktu_masuk, status, catatan) "
            + "VALUES (?, ?, ?, ?) RETURNING id_resep";
    private static final String Q_C61_10 = "INSERT INTO item_resep (id_resep, id_obat, dosis, jumlah, aturan_pakai) "
            + "VALUES (?, ?, ?, ?, ?) RETURNING id_item";
    private static final String Q_C61_11 = "UPDATE resep SET status = ?, catatan = ? WHERE id_resep = ?";
    private static final String Q_C61_12 = "SELECT id_resep, id_kunjungan, waktu_masuk, status, catatan FROM resep "
            + "WHERE status = 'menunggu' ORDER BY waktu_masuk, id_resep";
    private static final String Q_C61_13 = "SELECT id_resep, id_kunjungan, waktu_masuk, status, catatan "
            + "FROM resep WHERE id_resep = ?";
    private static final String Q_C61_14 = "SELECT id_item, id_resep, id_obat, dosis, jumlah, aturan_pakai "
            + "FROM item_resep WHERE id_resep = ANY (?) ORDER BY id_resep, id_obat";
    private static final String Q_C61_15 = "SELECT id_resep, id_kunjungan, waktu_masuk, status, catatan "
            + "FROM resep WHERE id_resep = ? FOR UPDATE";

    // Tambahan implementasi (B.1): identitas pasien seluruh resep antrean dalam satu kueri, bukan satu per resep.
    private static final String Q_C61_16 = "SELECT r.id_resep, p.id_pasien, p.nik, p.nomor_rekam_medis, p.nama, "
            + "p.tanggal_lahir, p.jenis_kelamin, p.alamat, p.nomor_telepon FROM resep r "
            + "JOIN kunjungan k ON k.id_kunjungan = r.id_kunjungan JOIN pasien p ON p.id_pasien = k.id_pasien "
            + "WHERE r.id_resep = ANY (?)";

    private static final String UNIQUE_VIOLATION = "23505";
    private static final String CHECK_VIOLATION = "23514";
    private static final String KELAS_KONEKSI = "08";

    private Connection koneksi;

    public DataFarmasiBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    /**
     * Mengambil seluruh obat beserta saldo stoknya, terurut nama (Q-C61-01, KF11, KF15, KF17).
     */
    public List<ObatEntity> ambilSemuaObat() {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_01); ResultSet rs = ps.executeQuery()) {
            List<ObatEntity> hasil = new ArrayList<>();
            while (rs.next()) {
                hasil.add(bacaObat(rs));
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil data obat", e);
        }
    }

    /**
     * Mengambil satu obat sambil mengunci barisnya, atau null bila tidak ada (Q-C61-02). Di dalam transaksi
     * potongStok() dan tambahStok(), kunci ini mencegah lost update pada saldo stok (KF23, KNF11).
     */
    public ObatEntity ambilObat(int idObat) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_02)) {
            ps.setInt(1, idObat);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? bacaObat(rs) : null;
            }
        } catch (SQLException e) {
            throw galat("Gagal mengambil obat", e);
        }
    }

    /**
     * Mengambil bets yang masih bersisa, kedaluwarsa terdekat lebih dulu, sebagai dasar FEFO (Q-C61-03,
     * Algo-C24-02). Baris bets ikut terkunci bila dipanggil di dalam transaksi.
     */
    public List<BetsObatEntity> ambilBets(int idObat) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_03)) {
            ps.setInt(1, idObat);
            List<BetsObatEntity> hasil = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new BetsObatEntity(rs.getString("nomor_bets"), rs.getInt("id_obat"),
                            rs.getInt("jumlah"), rs.getDate("tanggal_kedaluwarsa").toLocalDate()));
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil bets obat", e);
        }
    }

    /**
     * Menulis saldo stok obat dan jumlah setiap bets dalam satu transaksi (Q-C61-04, Q-C61-05). Nilai yang ditulis
     * adalah nilai akhir pada objek, bukan selisih.
     */
    public void simpanStok(ObatEntity o, List<BetsObatEntity> bets) {
        boolean transaksiSendiri = mulaiTransaksi();
        try {
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_04)) {
                ps.setInt(1, o.getStok());
                ps.setInt(2, o.getIdObat());
                if (ps.executeUpdate() == 0) {
                    throw new IllegalArgumentException("Obat tidak ditemukan: " + o.getIdObat());
                }
            }
            if (bets != null && !bets.isEmpty()) {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_05)) {
                    for (BetsObatEntity b : bets) {
                        ps.setInt(1, b.getJumlah());
                        ps.setInt(2, b.getIdObat());
                        ps.setString(3, b.getNomorBets());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            selesaiTransaksi(transaksiSendiri, true);
        } catch (SQLException | RuntimeException e) {
            selesaiTransaksi(transaksiSendiri, false);
            throw galat("Gagal menyimpan stok obat", e);
        } finally {
            akhiriTransaksi(transaksiSendiri);
        }
    }

    /**
     * Menyisipkan catatan penerimaan dan setiap bets yang diterima (Q-C61-06, Q-C61-07, Algo-C61-01, KF17).
     * Nomor bets yang sudah tercatat untuk obat yang sama ditolak dengan IllegalArgumentException("nomorBets").
     */
    public void simpanPenerimaan(PenerimaanObatEntity p) {
        boolean transaksiSendiri = mulaiTransaksi();
        try {
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_06)) {
                ps.setString(1, p.getNomorFaktur());
                ps.setDate(2, Date.valueOf(p.getTanggalTerima()));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    p.setIdPenerimaan(rs.getInt(1));
                }
            }
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_07)) {
                for (BetsObatEntity b : p.getDaftarBets()) {
                    ps.setInt(1, b.getIdObat());
                    ps.setString(2, b.getNomorBets());
                    ps.setInt(3, b.getJumlah());
                    ps.setDate(4, Date.valueOf(b.getTanggalKedaluwarsa()));
                    ps.setInt(5, p.getIdPenerimaan());
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (SQLException e) {
                if (UNIQUE_VIOLATION.equals(sqlState(e))) {
                    // Nama kolom menjadi pesan agar PersediaanForm menandai kolom nomor bets (Algo-C61-01).
                    throw new IllegalArgumentException("nomorBets", e);
                }
                throw e;
            }
            selesaiTransaksi(transaksiSendiri, true);
        } catch (SQLException | RuntimeException e) {
            selesaiTransaksi(transaksiSendiri, false);
            throw galat("Gagal menyimpan penerimaan obat", e);
        } finally {
            akhiriTransaksi(transaksiSendiri);
        }
    }

    /**
     * Mengambil obat yang stoknya di bawah ambang minimum atau memiliki bets bersisa yang kedaluwarsa dalam hari
     * hari atau kurang (Q-C61-08, KF17).
     */
    public List<ObatEntity> obatPerluPeringatan(int hari) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_08)) {
            ps.setInt(1, hari);
            List<ObatEntity> hasil = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(bacaObat(rs));
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil peringatan persediaan", e);
        }
    }

    /**
     * Resep baru: menyisipkan resep beserta seluruh itemnya (Q-C61-09, Q-C61-10). Resep lama: memutakhirkan
     * status dan catatannya (Q-C61-11). Item resep tidak berubah setelah resep dikirim.
     */
    public void simpanResep(ResepEntity r) {
        if (r.getIdResep() != 0) {
            perbaruiResep(r);
            return;
        }
        boolean transaksiSendiri = mulaiTransaksi();
        try {
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_09)) {
                ps.setInt(1, r.getIdKunjungan());
                ps.setTimestamp(2, Timestamp.valueOf(r.getWaktuMasuk()));
                ps.setString(3, r.getStatus());
                ps.setString(4, r.getCatatan());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    r.setIdResep(rs.getInt(1));
                }
            }
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_10)) {
                for (ItemResepEntity item : r.getDaftarItem()) {
                    ps.setInt(1, r.getIdResep());
                    ps.setInt(2, item.getIdObat());
                    ps.setString(3, item.getDosis());
                    ps.setInt(4, item.getJumlah());
                    ps.setString(5, item.getAturanPakai());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        item.setIdItem(rs.getInt(1));
                    }
                }
            }
            selesaiTransaksi(transaksiSendiri, true);
        } catch (SQLException | RuntimeException e) {
            selesaiTransaksi(transaksiSendiri, false);
            r.setIdResep(0);
            throw galat("Gagal menyimpan resep", e);
        } finally {
            akhiriTransaksi(transaksiSendiri);
        }
    }

    /**
     * Mengambil resep berstatus "menunggu" terurut waktu masuk beserta itemnya (Q-C61-12, Q-C61-14, KF13).
     */
    public List<ResepEntity> ambilAntreanResep() {
        try {
            List<ResepEntity> hasil = new ArrayList<>();
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_12); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(bacaResep(rs));
                }
            }
            isiItem(hasil);
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil antrean resep", e);
        }
    }

    /**
     * Mengambil satu resep beserta itemnya tanpa mengunci, atau null bila tidak ada (Q-C61-13, Q-C61-14).
     */
    public ResepEntity ambilResep(int idResep) {
        return ambilSatuResep(Q_C61_13, idResep, "Gagal mengambil resep");
    }

    /**
     * Mengambil satu resep beserta itemnya sambil mengunci baris resep, atau null bila tidak ada (Q-C61-15,
     * Q-C61-14). Hanya dipanggil di dalam transaksi serahkanObat() (Algo-C24-01, skenario alternatif 2 UC07).
     */
    public ResepEntity kunciResep(int idResep) {
        return ambilSatuResep(Q_C61_15, idResep, "Gagal mengunci resep");
    }

    /**
     * Mengambil pasien pemilik setiap resep pada daftar, dipetakan dengan idResep (Q-C61-16, tambahan implementasi
     * untuk I-09 dan I-10).
     */
    public Map<Integer, PasienEntity> ambilPasienResep(List<Integer> daftarIdResep) {
        Map<Integer, PasienEntity> hasil = new HashMap<>();
        if (daftarIdResep == null || daftarIdResep.isEmpty()) {
            return hasil;
        }
        try {
            Array larikId = koneksi.createArrayOf("integer", daftarIdResep.toArray(new Integer[0]));
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_16)) {
                ps.setArray(1, larikId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Date lahir = rs.getDate("tanggal_lahir");
                        hasil.put(rs.getInt("id_resep"), new PasienEntity(rs.getInt("id_pasien"), rs.getString("nik"),
                                rs.getString("nomor_rekam_medis"), rs.getString("nama"),
                                lahir == null ? null : lahir.toLocalDate(), rs.getString("jenis_kelamin"),
                                rs.getString("alamat"), rs.getString("nomor_telepon")));
                    }
                }
            } finally {
                larikId.free();
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil pasien pemilik resep", e);
        }
    }

    private void perbaruiResep(ResepEntity r) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_11)) {
            ps.setString(1, r.getStatus());
            ps.setString(2, r.getCatatan());
            ps.setInt(3, r.getIdResep());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Resep tidak ditemukan: " + r.getIdResep());
            }
        } catch (SQLException e) {
            throw galat("Gagal memperbarui resep", e);
        }
    }

    private ResepEntity ambilSatuResep(String kueri, int idResep, String pesanGagal) {
        try {
            ResepEntity resep;
            try (PreparedStatement ps = koneksi.prepareStatement(kueri)) {
                ps.setInt(1, idResep);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    resep = bacaResep(rs);
                }
            }
            isiItem(List.of(resep));
            return resep;
        } catch (SQLException e) {
            throw galat(pesanGagal, e);
        }
    }

    // Seluruh item untuk beberapa resep diambil dengan satu kueri berparameter larik (Q-C61-14).
    private void isiItem(List<ResepEntity> daftarResep) throws SQLException {
        if (daftarResep.isEmpty()) {
            return;
        }
        Map<Integer, List<ItemResepEntity>> itemPerResep = new LinkedHashMap<>();
        for (ResepEntity r : daftarResep) {
            itemPerResep.put(r.getIdResep(), new ArrayList<>());
        }
        Array larikId = koneksi.createArrayOf("integer", itemPerResep.keySet().toArray(new Integer[0]));
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C61_14)) {
            ps.setArray(1, larikId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    itemPerResep.get(rs.getInt("id_resep")).add(new ItemResepEntity(rs.getInt("id_item"),
                            rs.getInt("id_obat"), rs.getString("dosis"), rs.getInt("jumlah"),
                            rs.getString("aturan_pakai")));
                }
            }
        } finally {
            larikId.free();
        }
        for (ResepEntity r : daftarResep) {
            r.setDaftarItem(itemPerResep.get(r.getIdResep()));
        }
    }

    private static ObatEntity bacaObat(ResultSet rs) throws SQLException {
        return new ObatEntity(rs.getInt("id_obat"), rs.getString("nama_obat"), rs.getString("sediaan"),
                rs.getInt("stok"), rs.getInt("ambang_minimum"));
    }

    private static ResepEntity bacaResep(ResultSet rs) throws SQLException {
        return new ResepEntity(rs.getInt("id_resep"), rs.getInt("id_kunjungan"),
                rs.getTimestamp("waktu_masuk").toLocalDateTime(), rs.getString("status"), rs.getString("catatan"));
    }

    // Pola transaksiSendiri: operasi dengan lebih dari satu kueri tulis membungkus dirinya sendiri bila dipanggil
    // di luar transaksi Manager (aturan 1 Subbab 5.56, Algo-C59-02).
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

    /**
     * SQLState pertama yang terisi pada rantai pengecualian. BatchUpdateException dari JDBC menyimpan penyebab
     * aslinya pada getNextException().
     */
    static String sqlState(SQLException e) {
        for (SQLException s = e; s != null; s = s.getNextException()) {
            if (s.getSQLState() != null) {
                return s.getSQLState();
            }
        }
        return null;
    }

    // Menerjemahkan galat sesuai aturan 3 Subbab 5.56. Galat masukan dan keadaan diteruskan apa adanya.
    static RuntimeException galat(String pesan, Exception e) {
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            return (RuntimeException) e;
        }
        LOG.log(Level.SEVERE, pesan, e);
        String state = e instanceof SQLException sql ? sqlState(sql) : null;
        if (UNIQUE_VIOLATION.equals(state)) {
            return new IllegalArgumentException(pesan + ": data sudah tercatat", e);
        }
        if (CHECK_VIOLATION.equals(state)) {
            return new IllegalArgumentException(pesan + ": nilai di luar batas yang diizinkan", e);
        }
        if (state != null && state.startsWith(KELAS_KONEKSI)) {
            return new IllegalStateException("Basis data tidak dapat dihubungi", e);
        }
        return new IllegalStateException(pesan, e);
    }
}
