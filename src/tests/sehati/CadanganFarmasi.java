package sehati;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Menyimpan keadaan tabel farmasi pada basis data uji sebelum test, lalu memulihkannya setelah test: stok obat, sisa
 * bets, status dan catatan resep, status kunjungan, kunci pemeriksaan, antrean apotek, serta baris baru pada resep,
 * penerimaan, bets, bundel FHIR, dan audit. Dipakai test modul farmasi yang menjalankan Manager dengan koneksinya
 * sendiri sehingga tidak dapat di-rollback. Koneksi harus memakai akun pemilik basis data (DB_UJI_URL) karena audit
 * hanya dapat dihapus pemilik (KNF05).
 */
public final class CadanganFarmasi {

    private final Connection koneksi;
    private final Map<Integer, Integer> stok = new LinkedHashMap<>();
    private final Map<String, Integer> bets = new LinkedHashMap<>();
    private final Map<Integer, String[]> resep = new LinkedHashMap<>();
    private final Map<Integer, String> kunjungan = new LinkedHashMap<>();
    private final Map<Integer, Boolean> pemeriksaan = new LinkedHashMap<>();
    private final List<int[]> antreanApotek = new ArrayList<>();
    private long resepTerakhir;
    private long penerimaanTerakhir;
    private long bundelTerakhir;
    private long auditTerakhir;

    private CadanganFarmasi(Connection koneksi) {
        this.koneksi = koneksi;
    }

    public static CadanganFarmasi ambil(Connection koneksi) throws SQLException {
        CadanganFarmasi c = new CadanganFarmasi(koneksi);
        try (Statement st = koneksi.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT id_obat, stok FROM obat")) {
                while (rs.next()) {
                    c.stok.put(rs.getInt(1), rs.getInt(2));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT id_obat, nomor_bets, jumlah FROM bets_obat")) {
                while (rs.next()) {
                    c.bets.put(rs.getInt(1) + "|" + rs.getString(2), rs.getInt(3));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT id_resep, status, catatan FROM resep")) {
                while (rs.next()) {
                    c.resep.put(rs.getInt(1), new String[] {rs.getString(2), rs.getString(3)});
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT id_kunjungan, status FROM kunjungan")) {
                while (rs.next()) {
                    c.kunjungan.put(rs.getInt(1), rs.getString(2));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT id_kunjungan, terkunci FROM pemeriksaan")) {
                while (rs.next()) {
                    c.pemeriksaan.put(rs.getInt(1), rs.getBoolean(2));
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT e.urutan, e.id_entri FROM antrean_entri e "
                    + "JOIN antrean a USING (id_antrean) WHERE a.jenis = 'apotek'")) {
                while (rs.next()) {
                    c.antreanApotek.add(new int[] {rs.getInt(1), rs.getInt(2)});
                }
            }
            c.resepTerakhir = angka(st, "SELECT coalesce(max(id_resep), 0) FROM resep");
            c.penerimaanTerakhir = angka(st, "SELECT coalesce(max(id_penerimaan), 0) FROM penerimaan_obat");
            c.bundelTerakhir = angka(st, "SELECT coalesce(max(id_bundel), 0) FROM bundel_fhir");
            c.auditTerakhir = angka(st, "SELECT coalesce(max(id_audit), 0) FROM audit");
        }
        return c;
    }

    public long auditTerakhir() {
        return auditTerakhir;
    }

    public long resepTerakhir() {
        return resepTerakhir;
    }

    public long penerimaanTerakhir() {
        return penerimaanTerakhir;
    }

    public void pulihkan() throws SQLException {
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("DELETE FROM bundel_fhir WHERE id_bundel > " + bundelTerakhir);
            st.executeUpdate("DELETE FROM antrean_entri e USING antrean a WHERE e.id_antrean = a.id_antrean "
                    + "AND a.jenis = 'apotek'");
            st.executeUpdate("DELETE FROM item_resep WHERE id_resep > " + resepTerakhir);
            st.executeUpdate("DELETE FROM resep WHERE id_resep > " + resepTerakhir);
            st.executeUpdate("DELETE FROM bets_obat WHERE id_penerimaan > " + penerimaanTerakhir);
            st.executeUpdate("DELETE FROM penerimaan_obat WHERE id_penerimaan > " + penerimaanTerakhir);
            st.executeUpdate("DELETE FROM audit WHERE id_audit > " + auditTerakhir);
        }
        jalankan("UPDATE obat SET stok = ? WHERE id_obat = ?", stok, (ps, id, nilai) -> {
            ps.setInt(1, nilai);
            ps.setInt(2, id);
        });
        jalankan("UPDATE bets_obat SET jumlah = ? WHERE id_obat = ? AND nomor_bets = ?", bets, (ps, kunci, nilai) -> {
            String[] bagian = kunci.split("\\|", 2);
            ps.setInt(1, nilai);
            ps.setInt(2, Integer.parseInt(bagian[0]));
            ps.setString(3, bagian[1]);
        });
        jalankan("UPDATE resep SET status = ?, catatan = ? WHERE id_resep = ?", resep, (ps, id, nilai) -> {
            ps.setString(1, nilai[0]);
            ps.setString(2, nilai[1]);
            ps.setInt(3, id);
        });
        jalankan("UPDATE kunjungan SET status = ? WHERE id_kunjungan = ?", kunjungan, (ps, id, nilai) -> {
            ps.setString(1, nilai);
            ps.setInt(2, id);
        });
        jalankan("UPDATE pemeriksaan SET terkunci = ? WHERE id_kunjungan = ?", pemeriksaan, (ps, id, nilai) -> {
            ps.setBoolean(1, nilai);
            ps.setInt(2, id);
        });
        try (PreparedStatement ps = koneksi.prepareStatement("INSERT INTO antrean_entri (id_antrean, urutan, "
                + "id_entri) SELECT id_antrean, ?, ? FROM antrean WHERE jenis = 'apotek'")) {
            for (int[] e : antreanApotek) {
                ps.setInt(1, e[0]);
                ps.setInt(2, e[1]);
                ps.executeUpdate();
            }
        }
    }

    private interface Pengisi<K, V> {
        void isi(PreparedStatement ps, K kunci, V nilai) throws SQLException;
    }

    private <K, V> void jalankan(String sql, Map<K, V> data, Pengisi<K, V> pengisi) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            for (Map.Entry<K, V> e : data.entrySet()) {
                pengisi.isi(ps, e.getKey(), e.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private static long angka(Statement st, String sql) throws SQLException {
        try (ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }
}
