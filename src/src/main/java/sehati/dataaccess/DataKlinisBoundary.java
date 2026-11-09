package sehati.dataaccess;

import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KodeICD10Entity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.PenandaRisikoEntity;
import sehati.business.entity.SkriningEntity;
import sehati.business.entity.TandaVitalEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C59 - DataKlinisBoundary.
 *
 * Membaca dan menyimpan SkriningEntity, TandaVitalEntity, AmbangRisikoEntity, PenandaRisikoEntity,
 * PemeriksaanEntity, DiagnosisEntity, KodeICD10Entity, dan JadwalKontrolEntity.
 */
public class DataKlinisBoundary {

    private static final Logger LOG = Logger.getLogger(DataKlinisBoundary.class.getName());

    private static final String Q_C59_01 = "INSERT INTO skrining (id_kunjungan, keluhan_awal, waktu) VALUES (?, ?, ?) "
            + "RETURNING id_skrining";
    private static final String Q_C59_02 = "INSERT INTO tanda_vital (id_skrining, sistolik, diastolik, berat_badan, "
            + "tinggi_badan, suhu, nadi, gula_darah) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String Q_C59_03 = "SELECT s.id_skrining, s.id_kunjungan, s.keluhan_awal, s.waktu, "
            + "tv.sistolik, tv.diastolik, tv.berat_badan, tv.tinggi_badan, tv.suhu, tv.nadi, tv.gula_darah "
            + "FROM skrining s JOIN tanda_vital tv ON tv.id_skrining = s.id_skrining WHERE s.id_kunjungan = ?";
    private static final String Q_C59_04 = "SELECT tv.sistolik, tv.diastolik, tv.berat_badan, tv.tinggi_badan, "
            + "tv.suhu, tv.nadi, tv.gula_darah FROM tanda_vital tv "
            + "JOIN skrining s ON s.id_skrining = tv.id_skrining "
            + "JOIN kunjungan k ON k.id_kunjungan = s.id_kunjungan "
            + "WHERE k.id_pasien = ? AND k.tanggal < CURRENT_DATE ORDER BY s.waktu DESC LIMIT ?";
    private static final String Q_C59_05 = "SELECT s.waktu, tv.sistolik, tv.diastolik, tv.berat_badan, "
            + "tv.tinggi_badan, tv.suhu, tv.nadi, tv.gula_darah FROM tanda_vital tv "
            + "JOIN skrining s ON s.id_skrining = tv.id_skrining "
            + "JOIN kunjungan k ON k.id_kunjungan = s.id_kunjungan WHERE k.id_pasien = ? ORDER BY s.waktu";
    private static final String Q_C59_06 = "SELECT id_ambang, jenis_risiko, nilai_batas FROM ambang_risiko "
            + "ORDER BY jenis_risiko";
    private static final String Q_C59_07 = "UPDATE ambang_risiko SET nilai_batas = ? WHERE id_ambang = ?";
    private static final String Q_C59_08 = "INSERT INTO penanda_risiko (id_kunjungan, jenis_risiko, tanggal, status) "
            + "VALUES (?, ?, ?, ?) RETURNING id_penanda";
    private static final String Q_C59_09 = "UPDATE penanda_risiko SET status = ? WHERE id_penanda = ?";
    private static final String Q_C59_10 = "SELECT id_penanda, id_kunjungan, jenis_risiko, tanggal, status "
            + "FROM penanda_risiko WHERE id_kunjungan = ? AND status = 'aktif' ORDER BY jenis_risiko";
    private static final String Q_C59_11 = "SELECT kode, deskripsi, bab FROM kode_icd10 "
            + "WHERE kode ILIKE ? OR deskripsi ILIKE ? ORDER BY (kode ILIKE ?) DESC, kode LIMIT 20";
    private static final String Q_C59_12 = "INSERT INTO pemeriksaan (id_kunjungan, anamnesis, tindakan, terkunci) "
            + "VALUES (?, ?, ?, ?) ON CONFLICT (id_kunjungan) DO UPDATE "
            + "SET anamnesis = EXCLUDED.anamnesis, tindakan = EXCLUDED.tindakan, terkunci = EXCLUDED.terkunci "
            + "WHERE pemeriksaan.terkunci = FALSE RETURNING id_pemeriksaan";
    private static final String Q_C59_13 = "INSERT INTO diagnosis (id_pemeriksaan, kode_icd10, deskripsi) "
            + "VALUES (?, ?, ?) ON CONFLICT (id_pemeriksaan) DO UPDATE "
            + "SET kode_icd10 = EXCLUDED.kode_icd10, deskripsi = EXCLUDED.deskripsi";
    private static final String Q_C59_14 = "SELECT id_pemeriksaan, id_kunjungan, anamnesis, tindakan, terkunci "
            + "FROM pemeriksaan WHERE id_kunjungan = ?";
    private static final String Q_C59_15 = "INSERT INTO jadwal_kontrol (id_pasien, tanggal_rencana, status) "
            + "VALUES (?, ?, ?) RETURNING id_jadwal";
    private static final String Q_C59_16 = "UPDATE jadwal_kontrol SET status = ? WHERE id_jadwal = ?";
    // Tambahan implementasi (B.1): diagnosis tersimpan satu kunjungan, untuk memuat draf pada I-07.
    private static final String Q_C59_18 = "SELECT d.id_diagnosis, d.kode_icd10, d.deskripsi FROM diagnosis d "
            + "JOIN pemeriksaan p ON p.id_pemeriksaan = d.id_pemeriksaan WHERE p.id_kunjungan = ?";
    private static final String Q_C59_17 = "SELECT jk.id_jadwal, jk.id_pasien, jk.tanggal_rencana, jk.status "
            + "FROM jadwal_kontrol jk WHERE jk.status = 'terjadwal' "
            + "AND NOT EXISTS (SELECT 1 FROM daftar_pantau dp WHERE dp.id_jadwal = jk.id_jadwal) "
            + "ORDER BY jk.tanggal_rencana, jk.id_jadwal";

    private Connection koneksi;

    public DataKlinisBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    /**
     * Menyimpan skrining beserta tanda vitalnya dalam satu transaksi (Q-C59-01, Q-C59-02, KF05).
     */
    public void simpanSkrining(SkriningEntity s) {
        TandaVitalEntity tv = s.getTandaVital();
        if (tv == null) {
            throw new IllegalArgumentException("Tanda vital belum dilampirkan pada skrining");
        }
        boolean transaksiSendiri = mulaiTransaksi();
        try {
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_01)) {
                ps.setInt(1, s.getIdKunjungan());
                ps.setString(2, s.getKeluhanAwal());
                ps.setTimestamp(3, Timestamp.valueOf(s.getWaktu()));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    s.setIdSkrining(rs.getInt(1));
                }
            }
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_02)) {
                ps.setInt(1, s.getIdSkrining());
                ps.setInt(2, tv.getSistolik());
                ps.setInt(3, tv.getDiastolik());
                ps.setDouble(4, tv.getBeratBadan());
                ps.setDouble(5, tv.getTinggiBadan());
                ps.setDouble(6, tv.getSuhu());
                ps.setInt(7, tv.getNadi());
                if (tv.getGulaDarah() == null) {
                    ps.setNull(8, Types.INTEGER);
                } else {
                    ps.setInt(8, tv.getGulaDarah());
                }
                ps.executeUpdate();
            }
            selesaiTransaksi(transaksiSendiri, true);
        } catch (SQLException | RuntimeException e) {
            selesaiTransaksi(transaksiSendiri, false);
            throw galat("Gagal menyimpan skrining", e);
        } finally {
            akhiriTransaksi(transaksiSendiri);
        }
    }

    /**
     * Mengambil skrining satu kunjungan, atau null bila belum diskrining (Q-C59-03).
     */
    public SkriningEntity ambilSkrining(int idKunjungan) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_03)) {
            ps.setInt(1, idKunjungan);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                SkriningEntity s = new SkriningEntity(rs.getInt("id_skrining"), rs.getInt("id_kunjungan"),
                        rs.getString("keluhan_awal"), rs.getTimestamp("waktu").toLocalDateTime());
                s.lampirkanTandaVital(bacaTandaVital(rs, true));
                return s;
            }
        } catch (SQLException e) {
            throw galat("Gagal mengambil skrining", e);
        }
    }

    /**
     * batas > 0: pengukuran terakhir sebelum hari ini, terbaru lebih dulu (Q-C59-04). batas = 0: seluruh
     * pengukuran terlama lebih dulu (Q-C59-05) (Algo-C59-01, KF07, KF08).
     */
    public List<TandaVitalEntity> riwayatTandaVital(int idPasien, int batas) {
        if (batas < 0) {
            throw new IllegalArgumentException("Batas riwayat tidak boleh negatif");
        }
        try (PreparedStatement ps = koneksi.prepareStatement(batas > 0 ? Q_C59_04 : Q_C59_05)) {
            ps.setInt(1, idPasien);
            if (batas > 0) {
                ps.setInt(2, batas);
            }
            List<TandaVitalEntity> hasil = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(bacaTandaVital(rs, batas == 0));
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil riwayat tanda vital", e);
        }
    }

    /**
     * Mengambil ambang ketiga jenis risiko dari data master (Q-C59-06).
     */
    public List<AmbangRisikoEntity> ambilAmbang() {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_06); ResultSet rs = ps.executeQuery()) {
            List<AmbangRisikoEntity> hasil = new ArrayList<>();
            while (rs.next()) {
                hasil.add(new AmbangRisikoEntity(rs.getInt("id_ambang"), rs.getString("jenis_risiko"),
                        rs.getDouble("nilai_batas")));
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil ambang risiko", e);
        }
    }

    /**
     * Memutakhirkan nilai batas satu ambang. Jenis risiko tetap tiga (Q-C59-07, KF15).
     */
    public void simpanAmbang(AmbangRisikoEntity a) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_07)) {
            ps.setDouble(1, a.getNilaiBatas());
            ps.setInt(2, a.getIdAmbang());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Ambang risiko tidak ditemukan: " + a.getIdAmbang());
            }
        } catch (SQLException e) {
            throw galat("Gagal menyimpan ambang risiko", e);
        }
    }

    /**
     * Menyisipkan penanda baru (Q-C59-08) atau memutakhirkan status penanda yang dicabut (Q-C59-09).
     */
    public void simpanPenanda(PenandaRisikoEntity p) {
        try {
            if (p.getIdPenanda() == 0) {
                if (p.getTanggal() == null) {
                    throw new IllegalArgumentException("Tanggal penanda risiko wajib diisi");
                }
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_08)) {
                    ps.setInt(1, p.getIdKunjungan());
                    ps.setString(2, p.getJenisRisiko());
                    ps.setDate(3, Date.valueOf(p.getTanggal()));
                    ps.setString(4, p.getStatus());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        p.setIdPenanda(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_09)) {
                    ps.setString(1, p.getStatus());
                    ps.setInt(2, p.getIdPenanda());
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalArgumentException("Penanda risiko tidak ditemukan: " + p.getIdPenanda());
                    }
                }
            }
        } catch (SQLException | RuntimeException e) {
            throw galat("Gagal menyimpan penanda risiko", e);
        }
    }

    /**
     * Mengambil penanda risiko aktif satu kunjungan (Q-C59-10, KF07, KF09).
     */
    public List<PenandaRisikoEntity> penandaKunjungan(int idKunjungan) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_10)) {
            ps.setInt(1, idKunjungan);
            List<PenandaRisikoEntity> hasil = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new PenandaRisikoEntity(rs.getInt("id_penanda"), rs.getInt("id_kunjungan"),
                            rs.getString("jenis_risiko"), rs.getDate("tanggal").toLocalDate(),
                            rs.getString("status")));
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil penanda risiko", e);
        }
    }

    /**
     * Mencari kode ICD-10 berawalan kata kunci atau berdeskripsi memuat kata kunci, paling banyak 20 baris
     * (Q-C59-11, KF10).
     */
    public List<KodeICD10Entity> cariKodeICD10(String kataKunci) {
        if (kataKunci == null) {
            throw new IllegalArgumentException("Kata kunci diagnosis wajib diisi");
        }
        String teks = loloskanPola(kataKunci.strip());
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_11)) {
            ps.setString(1, teks + "%");
            ps.setString(2, "%" + teks + "%");
            ps.setString(3, teks + "%");
            List<KodeICD10Entity> hasil = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new KodeICD10Entity(rs.getString("kode"), rs.getString("deskripsi"),
                            rs.getString("bab")));
                }
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mencari kode ICD-10", e);
        }
    }

    /**
     * Menyisipkan atau menimpa pemeriksaan satu kunjungan beserta diagnosisnya. Menolak pemeriksaan yang sudah
     * dikunci (Q-C59-12, Q-C59-13, Algo-C59-02).
     */
    public void simpanPemeriksaan(PemeriksaanEntity p, DiagnosisEntity d) {
        boolean transaksiSendiri = mulaiTransaksi();
        try {
            try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_12)) {
                ps.setInt(1, p.getIdKunjungan());
                ps.setString(2, p.getAnamnesis());
                ps.setString(3, p.getTindakan());
                ps.setBoolean(4, p.isTerkunci());
                try (ResultSet rs = ps.executeQuery()) {
                    // Tanpa baris berarti klausa WHERE membatalkan UPDATE karena baris lama sudah terkunci.
                    if (!rs.next()) {
                        throw new IllegalStateException("Rekam medis kunjungan ini sudah dikunci");
                    }
                    p.setIdPemeriksaan(rs.getInt(1));
                }
            }
            if (d != null) {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_13)) {
                    ps.setInt(1, p.getIdPemeriksaan());
                    ps.setString(2, d.getKodeICD10());
                    ps.setString(3, d.getDeskripsi());
                    ps.executeUpdate();
                }
            }
            selesaiTransaksi(transaksiSendiri, true);
        } catch (SQLException | RuntimeException e) {
            selesaiTransaksi(transaksiSendiri, false);
            throw galat("Gagal menyimpan pemeriksaan", e);
        } finally {
            akhiriTransaksi(transaksiSendiri);
        }
    }

    /**
     * Mengambil pemeriksaan satu kunjungan, atau null bila belum ada (Q-C59-14).
     */
    public PemeriksaanEntity ambilPemeriksaan(int idKunjungan) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_14)) {
            ps.setInt(1, idKunjungan);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new PemeriksaanEntity(rs.getInt("id_pemeriksaan"), rs.getInt("id_kunjungan"),
                        rs.getString("anamnesis"), rs.getString("tindakan"), rs.getBoolean("terkunci"));
            }
        } catch (SQLException e) {
            throw galat("Gagal mengambil pemeriksaan", e);
        }
    }

    /**
     * Mengambil diagnosis tersimpan satu kunjungan, atau null bila belum ada (Q-C59-18).
     */
    public DiagnosisEntity ambilDiagnosis(int idKunjungan) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_18)) {
            ps.setInt(1, idKunjungan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new DiagnosisEntity(rs.getInt("id_diagnosis"), rs.getString("kode_icd10"),
                        rs.getString("deskripsi")) : null;
            }
        } catch (SQLException e) {
            throw galat("Gagal mengambil diagnosis", e);
        }
    }

    /**
     * Menyisipkan jadwal kontrol baru (Q-C59-15, KF12) atau memutakhirkan statusnya (Q-C59-16).
     */
    public void simpanJadwalKontrol(JadwalKontrolEntity j) {
        try {
            if (j.getIdJadwal() == 0) {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_15)) {
                    ps.setInt(1, j.getIdPasien());
                    ps.setDate(2, Date.valueOf(j.getTanggalRencana()));
                    ps.setString(3, j.getStatus());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        j.setIdJadwal(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_16)) {
                    ps.setString(1, j.getStatus());
                    ps.setInt(2, j.getIdJadwal());
                    if (ps.executeUpdate() == 0) {
                        throw new IllegalArgumentException("Jadwal kontrol tidak ditemukan: " + j.getIdJadwal());
                    }
                }
            }
        } catch (SQLException e) {
            throw galat("Gagal menyimpan jadwal kontrol", e);
        }
    }

    /**
     * Mengambil jadwal "terjadwal" yang belum memicu entri daftar pantau, untuk Algo-C21-02 (Q-C59-17).
     */
    public List<JadwalKontrolEntity> ambilJadwalKontrol() {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C59_17); ResultSet rs = ps.executeQuery()) {
            List<JadwalKontrolEntity> hasil = new ArrayList<>();
            while (rs.next()) {
                hasil.add(new JadwalKontrolEntity(rs.getInt("id_jadwal"), rs.getInt("id_pasien"),
                        rs.getDate("tanggal_rencana").toLocalDate(), rs.getString("status")));
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil jadwal kontrol", e);
        }
    }

    // \, %, dan _ yang diketik dokter dibaca sebagai huruf biasa, bukan wildcard LIKE (Algo-C57-01).
    static String loloskanPola(String teks) {
        return teks.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    // Q-C59-03 dan Q-C59-05 memilih s.waktu untuk grafik tren (KF08); Q-C59-04 tidak.
    private static TandaVitalEntity bacaTandaVital(ResultSet rs, boolean denganWaktu) throws SQLException {
        int gula = rs.getInt("gula_darah");
        Integer gulaDarah = rs.wasNull() ? null : gula;
        TandaVitalEntity tv = new TandaVitalEntity(rs.getInt("sistolik"), rs.getInt("diastolik"),
                rs.getDouble("berat_badan"), rs.getDouble("tinggi_badan"), rs.getDouble("suhu"), rs.getInt("nadi"),
                gulaDarah);
        if (denganWaktu) {
            tv.setWaktu(rs.getTimestamp("waktu").toLocalDateTime());
        }
        return tv;
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
                return new IllegalArgumentException("Data sudah tersimpan sebelumnya", e);
            }
            if ("23514".equals(state)) {
                return new IllegalArgumentException(rincian.contains("tanda_vital")
                        ? "Nilai tanda vital di luar rentang yang diizinkan" : "Nilai tidak memenuhi aturan data", e);
            }
            if ("23503".equals(state)) {
                return new IllegalArgumentException("Kunjungan atau data terkait tidak ditemukan", e);
            }
        }
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException("Basis data tidak dapat dihubungi", e);
    }
}
