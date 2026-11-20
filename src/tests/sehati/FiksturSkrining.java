package sehati;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Fiksur uji modul skrining dan pemantauan. Manager membuka koneksinya sendiri dan melakukan COMMIT, sehingga data
 * seed dicatat dulu lalu dipulihkan sesudah setiap test: baris baru dihapus, status kunjungan dan entri daftar pantau
 * dikembalikan, dan isi antrean skrining ditulis ulang seperti semula.
 */
public final class FiksturSkrining implements AutoCloseable {

    private final Connection koneksi;
    private final long skrining;
    private final long penanda;
    private final long daftarPantau;
    private final long tindakLanjut;
    private final long catatan;
    private final long audit;
    private final List<Object[]> statusKunjungan = new ArrayList<>();
    private final List<Object[]> statusPantau = new ArrayList<>();
    private final List<Object[]> antrean = new ArrayList<>();

    public FiksturSkrining() throws SQLException {
        koneksi = pemilik();
        skrining = angka("SELECT coalesce(max(id_skrining), 0) FROM skrining");
        penanda = angka("SELECT coalesce(max(id_penanda), 0) FROM penanda_risiko");
        daftarPantau = angka("SELECT coalesce(max(id_entri), 0) FROM daftar_pantau");
        tindakLanjut = angka("SELECT coalesce(max(id_tindak_lanjut), 0) FROM tindak_lanjut");
        catatan = angka("SELECT coalesce(max(id_catatan), 0) FROM catatan_rekam_medis");
        audit = angka("SELECT coalesce(max(id_audit), 0) FROM audit");
        try (Statement st = koneksi.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT id_kunjungan, status FROM kunjungan")) {
                while (rs.next()) {
                    statusKunjungan.add(new Object[] {rs.getString(2), rs.getInt(1)});
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT id_entri, status_tindak_lanjut FROM daftar_pantau")) {
                while (rs.next()) {
                    statusPantau.add(new Object[] {rs.getString(2), rs.getInt(1)});
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT id_antrean, urutan, id_entri FROM antrean_entri")) {
                while (rs.next()) {
                    antrean.add(new Object[] {rs.getInt(1), rs.getInt(2), rs.getInt(3)});
                }
            }
        }
    }

    public static Connection pemilik() throws SQLException {
        return DriverManager.getConnection(System.getenv("DB_UJI_URL"));
    }

    public static long angka(String sql, Object... nilai) throws SQLException {
        try (Connection c = pemilik(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setObject(i + 1, nilai[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : -1;
            }
        }
    }

    public static String teks(String sql, Object... nilai) throws SQLException {
        try (Connection c = pemilik(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setObject(i + 1, nilai[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public static void jalankan(String sql, Object... nilai) throws SQLException {
        try (Connection c = pemilik(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setObject(i + 1, nilai[i]);
            }
            ps.executeUpdate();
        }
    }

    public void pulihkan() throws SQLException {
        hapusSesudah("DELETE FROM tindak_lanjut WHERE id_tindak_lanjut > ?", tindakLanjut);
        hapusSesudah("DELETE FROM daftar_pantau WHERE id_entri > ?", daftarPantau);
        hapusSesudah("DELETE FROM penanda_risiko WHERE id_penanda > ?", penanda);
        hapusSesudah("DELETE FROM tanda_vital WHERE id_skrining > ?", skrining);
        hapusSesudah("DELETE FROM skrining WHERE id_skrining > ?", skrining);
        hapusSesudah("DELETE FROM catatan_rekam_medis WHERE id_catatan > ?", catatan);
        hapusSesudah("DELETE FROM audit WHERE id_audit > ?", audit);
        for (Object[] k : statusKunjungan) {
            ubah("UPDATE kunjungan SET status = ? WHERE id_kunjungan = ?", k[0], k[1]);
        }
        for (Object[] p : statusPantau) {
            ubah("UPDATE daftar_pantau SET status_tindak_lanjut = ? WHERE id_entri = ?", p[0], p[1]);
        }
        ubah("DELETE FROM antrean_entri");
        for (Object[] a : antrean) {
            ubah("INSERT INTO antrean_entri (id_antrean, urutan, id_entri) VALUES (?, ?, ?)", a);
        }
    }

    private void hapusSesudah(String sql, long batas) throws SQLException {
        ubah(sql, batas);
    }

    private void ubah(String sql, Object... nilai) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setObject(i + 1, nilai[i]);
            }
            ps.executeUpdate();
        }
    }

    @Override
    public void close() throws SQLException {
        try {
            pulihkan();
        } finally {
            koneksi.close();
        }
    }
}
