package sehati.dataaccess;

import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.RekamMedisEntity;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C57 - DataPasienBoundary.
 *
 * Menjalankan pencarian parsial berindeks serta membaca dan menyimpan PasienEntity dan RekamMedisEntity.
 */
public class DataPasienBoundary {

    private static final Logger LOG = Logger.getLogger(DataPasienBoundary.class.getName());

    private static final String KOLOM_PASIEN =
            "SELECT id_pasien, nik, nomor_rekam_medis, nama, tanggal_lahir, jenis_kelamin, alamat, nomor_telepon "
                    + "FROM pasien";

    private static final String Q_C57_01 = KOLOM_PASIEN
            + " WHERE nik LIKE ? OR nomor_rekam_medis ILIKE ? OR nama ILIKE ? ORDER BY nama LIMIT 50";
    private static final String Q_C57_02 = KOLOM_PASIEN + " WHERE id_pasien = ?";
    private static final String Q_C57_03 = "SELECT EXISTS (SELECT 1 FROM pasien WHERE nik = ?)";
    private static final String Q_C57_04 = "SELECT concat('RM-', lpad(nextval('seq_nomor_rm')::text, 6, '0'))";
    private static final String Q_C57_05 = "INSERT INTO pasien (nik, nomor_rekam_medis, nama, tanggal_lahir, "
            + "jenis_kelamin, alamat, nomor_telepon) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id_pasien";
    private static final String Q_C57_06 = "INSERT INTO rekam_medis (id_pasien) VALUES (?) RETURNING id_rekam_medis";
    private static final String Q_C57_07 = "UPDATE pasien SET nik = ?, nama = ?, tanggal_lahir = ?, "
            + "jenis_kelamin = ?, alamat = ?, nomor_telepon = ? WHERE id_pasien = ?";
    private static final String Q_C57_08 = "SELECT id_rekam_medis FROM rekam_medis WHERE id_pasien = ?";
    private static final String Q_C57_09 = "SELECT d.id_diagnosis, d.kode_icd10, d.deskripsi FROM diagnosis d "
            + "JOIN pemeriksaan pm ON pm.id_pemeriksaan = d.id_pemeriksaan "
            + "JOIN kunjungan k ON k.id_kunjungan = pm.id_kunjungan "
            + "WHERE k.id_pasien = ? AND pm.terkunci ORDER BY k.tanggal DESC";
    private static final String Q_C57_10 = "SELECT k.tanggal, o.nama_obat, o.sediaan, ir.jumlah, ir.dosis, "
            + "ir.aturan_pakai FROM item_resep ir JOIN resep r ON r.id_resep = ir.id_resep "
            + "JOIN kunjungan k ON k.id_kunjungan = r.id_kunjungan JOIN obat o ON o.id_obat = ir.id_obat "
            + "WHERE k.id_pasien = ? AND r.status = 'selesai dilayani' ORDER BY k.tanggal DESC, o.nama_obat";
    private static final String Q_C57_11 = "INSERT INTO catatan_rekam_medis (id_rekam_medis, waktu, isi) "
            + "VALUES (?, ?, ?)";

    private Connection koneksi;

    public DataPasienBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    /**
     * Mencari pasien yang NIK, nomor rekam medis, atau namanya memuat kata kunci, paling banyak 50 baris
     * terurut nama (Q-C57-01, Algo-C57-01).
     */
    public List<PasienEntity> cariParsial(String kataKunci) {
        String pola = polaPencarian(kataKunci);
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_01)) {
            ps.setString(1, pola);
            ps.setString(2, pola);
            ps.setString(3, pola);
            try (ResultSet rs = ps.executeQuery()) {
                List<PasienEntity> hasil = new ArrayList<>();
                while (rs.next()) {
                    hasil.add(petakanPasien(rs));
                }
                return hasil;
            }
        } catch (SQLException e) {
            throw galat("Gagal mencari pasien", e);
        }
    }

    /**
     * Mengambil satu pasien, atau null bila tidak ada (Q-C57-02).
     */
    public PasienEntity ambilPasien(int idPasien) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_02)) {
            ps.setInt(1, idPasien);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? petakanPasien(rs) : null;
            }
        } catch (SQLException e) {
            throw galat("Gagal mengambil pasien", e);
        }
    }

    /**
     * Mengembalikan true bila NIK sudah dipakai pasien mana pun (Q-C57-03).
     */
    public boolean nikTerdaftar(String nik) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_03)) {
            ps.setString(1, nik);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw galat("Gagal memeriksa NIK", e);
        }
    }

    /**
     * Mengambil nomor rekam medis berikutnya dari sequence, misalnya RM-000123 (Q-C57-04).
     */
    public String nomorRMBerikutnya() {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_04);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getString(1);
        } catch (SQLException e) {
            throw galat("Gagal mengambil nomor rekam medis", e);
        }
    }

    /**
     * Pasien baru (idPasien 0) disisipkan beserta baris rekam medisnya dalam satu transaksi (Q-C57-05, Q-C57-06).
     * Pasien lama dimutakhirkan tanpa menyentuh nomor rekam medis (Q-C57-07).
     */
    public void simpanPasien(PasienEntity p) {
        boolean baru = p.getIdPasien() == 0;
        boolean transaksiSendiri = mulaiTransaksi();
        try {
            if (baru) {
                sisipkanPasien(p);
            } else {
                mutakhirkanPasien(p);
            }
            selesaiTransaksi(transaksiSendiri, true);
        } catch (SQLException | RuntimeException e) {
            selesaiTransaksi(transaksiSendiri, false);
            throw galat("Gagal menyimpan pasien", e);
        } finally {
            akhiriTransaksi(transaksiSendiri);
        }
    }

    /**
     * Mengambil rekam medis pasien beserta diagnosis lampau dan riwayat obatnya, atau null bila pasien belum
     * punya rekam medis (Q-C57-08 sampai Q-C57-10).
     */
    public RekamMedisEntity ambilRekamMedis(int idPasien) {
        try {
            int idRekamMedis;
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_08)) {
                ps.setInt(1, idPasien);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }
                    idRekamMedis = rs.getInt("id_rekam_medis");
                }
            }
            List<DiagnosisEntity> diagnosis = new ArrayList<>();
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_09)) {
                ps.setInt(1, idPasien);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        diagnosis.add(new DiagnosisEntity(
                                rs.getInt("id_diagnosis"), rs.getString("kode_icd10"), rs.getString("deskripsi")));
                    }
                }
            }
            List<String> riwayatObat = new ArrayList<>();
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_10)) {
                ps.setInt(1, idPasien);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        riwayatObat.add(susunRiwayatObat(
                                rs.getDate("tanggal").toLocalDate(), rs.getString("nama_obat"),
                                rs.getString("sediaan"), rs.getInt("jumlah"), rs.getString("dosis"),
                                rs.getString("aturan_pakai")));
                    }
                }
            }
            return new RekamMedisEntity(idRekamMedis, diagnosis, riwayatObat);
        } catch (SQLException e) {
            throw galat("Gagal mengambil rekam medis", e);
        }
    }

    /**
     * Menyisipkan setiap entri baru hasil RekamMedisEntity.tambahEntri(), lalu mengosongkan penampungnya
     * (Q-C57-11). Entri yang sudah tersimpan tidak pernah diubah.
     */
    public void simpanRekamMedis(RekamMedisEntity rm) {
        List<String> entri = rm.getEntriBaru();
        if (entri.isEmpty()) {
            return;
        }
        boolean transaksiSendiri = mulaiTransaksi();
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_11)) {
            Timestamp waktu = Timestamp.valueOf(LocalDateTime.now());
            for (String isi : entri) {
                ps.setInt(1, rm.getIdRekamMedis());
                ps.setTimestamp(2, waktu);
                ps.setString(3, isi);
                ps.executeUpdate();
            }
            selesaiTransaksi(transaksiSendiri, true);
            rm.kosongkanEntriBaru();
        } catch (SQLException | RuntimeException e) {
            selesaiTransaksi(transaksiSendiri, false);
            throw galat("Gagal menyimpan rekam medis", e);
        } finally {
            akhiriTransaksi(transaksiSendiri);
        }
    }

    private void sisipkanPasien(PasienEntity p) throws SQLException {
        if (p.getNomorRekamMedis() == null || p.getNomorRekamMedis().isBlank()) {
            p.setNomorRekamMedis(nomorRMBerikutnya());
        }
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_05)) {
            ps.setString(1, p.getNik());
            ps.setString(2, p.getNomorRekamMedis());
            ps.setString(3, p.getNama());
            ps.setDate(4, p.getTanggalLahir() == null ? null : Date.valueOf(p.getTanggalLahir()));
            ps.setString(5, p.getJenisKelamin());
            ps.setString(6, p.getAlamat());
            isiTeksBolehKosong(ps, 7, p.getNomorTelepon());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                p.setIdPasien(rs.getInt(1));
            }
        }
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_06)) {
            ps.setInt(1, p.getIdPasien());
            ps.executeQuery().close();
        }
    }

    private void mutakhirkanPasien(PasienEntity p) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C57_07)) {
            ps.setString(1, p.getNik());
            ps.setString(2, p.getNama());
            ps.setDate(3, p.getTanggalLahir() == null ? null : Date.valueOf(p.getTanggalLahir()));
            ps.setString(4, p.getJenisKelamin());
            ps.setString(5, p.getAlamat());
            isiTeksBolehKosong(ps, 6, p.getNomorTelepon());
            ps.setInt(7, p.getIdPasien());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Pasien tidak ditemukan");
            }
        }
    }

    private static void isiTeksBolehKosong(PreparedStatement ps, int indeks, String nilai) throws SQLException {
        if (nilai == null || nilai.isBlank()) {
            ps.setNull(indeks, Types.VARCHAR);
        } else {
            ps.setString(indeks, nilai);
        }
    }

    private static PasienEntity petakanPasien(ResultSet rs) throws SQLException {
        Date lahir = rs.getDate("tanggal_lahir");
        return new PasienEntity(rs.getInt("id_pasien"), rs.getString("nik"), rs.getString("nomor_rekam_medis"),
                rs.getString("nama"), lahir == null ? null : lahir.toLocalDate(), rs.getString("jenis_kelamin"),
                rs.getString("alamat"), rs.getString("nomor_telepon"));
    }

    /**
     * Algo-C57-01: kata kunci dipangkas, "\", "%", dan "_" dianggap huruf biasa, lalu diapit "%".
     */
    static String polaPencarian(String kataKunci) {
        String teks = kataKunci == null ? "" : kataKunci.strip();
        StringBuilder pola = new StringBuilder("%");
        for (char c : teks.toCharArray()) {
            if (c == '\\' || c == '%' || c == '_') {
                pola.append('\\');
            }
            pola.append(c);
        }
        return pola.append('%').toString();
    }

    /**
     * Satu baris riwayat obat, misalnya "2026-10-12 Amlodipin 5 mg tablet ×30, 1x1 sesudah makan".
     */
    static String susunRiwayatObat(
            LocalDate tanggal, String namaObat, String sediaan, int jumlah, String dosis, String aturanPakai) {
        return tanggal + " " + namaObat + " " + sediaan + " ×" + jumlah + ", " + dosis + " " + aturanPakai;
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
                return new IllegalArgumentException(rincian.contains("nomor_rekam_medis")
                        ? "Nomor rekam medis sudah dipakai" : "NIK sudah terdaftar", e);
            }
            if ("23514".equals(state)) {
                return new IllegalArgumentException(
                        "Data pasien tidak valid: NIK harus 16 digit angka dan isian wajib harus terisi", e);
            }
            if ("23502".equals(state)) {
                return new IllegalArgumentException("Isian wajib data pasien belum lengkap", e);
            }
        }
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException("Basis data tidak dapat dihubungi", e);
    }
}
