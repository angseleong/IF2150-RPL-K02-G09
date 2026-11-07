package sehati.dataaccess;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pengganti JDBC untuk menguji DataBoundary tanpa PostgreSQL: mencatat kueri, parameter, dan peristiwa transaksi,
 * serta mengembalikan baris yang disiapkan per potongan teks kueri. Kueri sungguhan diuji lewat uji-kueri.sql.
 */
final class JdbcPalsu {

    /** Satu pernyataan yang dijalankan beserta parameternya (indeks mulai 1). */
    record Jalan(String sql, Map<Integer, Object> parameter) {
    }

    final List<String> peristiwa = new ArrayList<>();
    final List<Jalan> dijalankan = new ArrayList<>();
    private final Map<String, List<Map<String, Object>>> baris = new LinkedHashMap<>();
    private final Map<String, SQLException> galat = new LinkedHashMap<>();
    private final Map<String, Integer> barisTerubah = new LinkedHashMap<>();
    private boolean autoCommit = true;

    /** Baris yang dikembalikan kueri yang memuat potongan teks ini. */
    JdbcPalsu hasil(String potonganSql, List<Map<String, Object>> isi) {
        baris.put(potonganSql, isi);
        return this;
    }

    /** Kueri yang memuat potongan teks ini gagal dengan SQLState dan pesan tertentu. */
    JdbcPalsu gagal(String potonganSql, String sqlState, String pesan) {
        galat.put(potonganSql, new SQLException(pesan, sqlState));
        return this;
    }

    /** Jumlah baris yang dilaporkan executeUpdate untuk kueri yang memuat potongan teks ini. */
    JdbcPalsu terubah(String potonganSql, int jumlah) {
        barisTerubah.put(potonganSql, jumlah);
        return this;
    }

    JdbcPalsu mulaiDalamTransaksi() {
        autoCommit = false;
        return this;
    }

    boolean autoCommit() {
        return autoCommit;
    }

    List<Jalan> dijalankanMemuat(String potongan) {
        return dijalankan.stream().filter(j -> j.sql().contains(potongan)).toList();
    }

    static Map<String, Object> baris(Object... pasangan) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pasangan.length; i += 2) {
            m.put((String) pasangan[i], pasangan[i + 1]);
        }
        return m;
    }

    Connection koneksi() {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Connection.class},
                (proxy, metode, args) -> switch (metode.getName()) {
                    case "getAutoCommit" -> autoCommit;
                    case "setAutoCommit" -> {
                        autoCommit = (Boolean) args[0];
                        peristiwa.add("autoCommit=" + autoCommit);
                        yield null;
                    }
                    case "commit" -> {
                        peristiwa.add("commit");
                        yield null;
                    }
                    case "rollback" -> {
                        peristiwa.add("rollback");
                        yield null;
                    }
                    case "prepareStatement" -> pernyataan((String) args[0]);
                    default -> throw new UnsupportedOperationException(metode.getName());
                });
    }

    private PreparedStatement pernyataan(String sql) {
        Map<Integer, Object> parameter = new LinkedHashMap<>();
        List<Map<Integer, Object>> batch = new ArrayList<>();
        return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[] {PreparedStatement.class}, (proxy, metode, args) -> {
                    String nama = metode.getName();
                    if (nama.equals("setNull")) {
                        parameter.put((Integer) args[0], null);
                        return null;
                    }
                    if (nama.startsWith("set") && args != null && args.length >= 2) {
                        parameter.put((Integer) args[0], args[1]);
                        return null;
                    }
                    switch (nama) {
                        case "addBatch" -> {
                            batch.add(new LinkedHashMap<>(parameter));
                            return null;
                        }
                        case "executeBatch" -> {
                            for (Map<Integer, Object> p : batch) {
                                dijalankan.add(new Jalan(sql, p));
                            }
                            gagalBila(sql);
                            return new int[batch.size()];
                        }
                        case "executeQuery" -> {
                            dijalankan.add(new Jalan(sql, new LinkedHashMap<>(parameter)));
                            gagalBila(sql);
                            return hasilSet(cariBaris(sql));
                        }
                        case "executeUpdate" -> {
                            dijalankan.add(new Jalan(sql, new LinkedHashMap<>(parameter)));
                            gagalBila(sql);
                            return jumlahTerubah(sql);
                        }
                        case "close" -> {
                            return null;
                        }
                        default -> throw new UnsupportedOperationException(nama);
                    }
                });
    }

    private void gagalBila(String sql) throws SQLException {
        for (Map.Entry<String, SQLException> e : galat.entrySet()) {
            if (sql.contains(e.getKey())) {
                throw e.getValue();
            }
        }
    }

    private int jumlahTerubah(String sql) {
        for (Map.Entry<String, Integer> e : barisTerubah.entrySet()) {
            if (sql.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return 1;
    }

    private List<Map<String, Object>> cariBaris(String sql) {
        for (Map.Entry<String, List<Map<String, Object>>> e : baris.entrySet()) {
            if (sql.contains(e.getKey())) {
                return e.getValue();
            }
        }
        return List.of();
    }

    private static ResultSet hasilSet(List<Map<String, Object>> isi) {
        int[] posisi = {-1};
        return (ResultSet) Proxy.newProxyInstance(JdbcPalsu.class.getClassLoader(), new Class<?>[] {ResultSet.class},
                (proxy, metode, args) -> {
                    switch (metode.getName()) {
                        case "next" -> {
                            posisi[0]++;
                            return posisi[0] < isi.size();
                        }
                        case "close" -> {
                            return null;
                        }
                        default -> {
                            Map<String, Object> baris = isi.get(posisi[0]);
                            Object nilai = args[0] instanceof Integer i
                                    ? new ArrayList<>(baris.values()).get(i - 1) : baris.get((String) args[0]);
                            return switch (metode.getName()) {
                                case "getInt" -> ((Number) nilai).intValue();
                                case "getBoolean" -> (Boolean) nilai;
                                case "getString" -> nilai == null ? null : nilai.toString();
                                case "getDate" -> nilai;
                                default -> throw new UnsupportedOperationException(metode.getName());
                            };
                        }
                    }
                });
    }
}
