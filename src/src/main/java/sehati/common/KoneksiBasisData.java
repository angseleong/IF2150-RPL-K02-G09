package sehati.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Membuka koneksi JDBC ke PostgreSQL. {@code Manager} memanggil {@link #buka()} di awal operasi lalu meneruskan
 * koneksinya ke {@code DataBoundary} (DPPL pengantar BAB 5).
 */
public final class KoneksiBasisData {

    private static final PengaturanAplikasi PENGATURAN = PengaturanAplikasi.muat();

    private KoneksiBasisData() {
    }

    public static Connection buka() throws SQLException {
        String url = "jdbc:postgresql://" + PENGATURAN.ambil("DB_HOST", "localhost") + ":"
                + PENGATURAN.ambil("DB_PORT", "5432") + "/" + PENGATURAN.ambil("DB_NAME");
        return DriverManager.getConnection(url, PENGATURAN.ambil("DB_USER"), PENGATURAN.ambil("DB_PASSWORD"));
    }
}
