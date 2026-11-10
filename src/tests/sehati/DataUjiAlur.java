package sehati;

import sehati.business.control.AutentikasiManager;
import sehati.business.control.PersediaanManager;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.common.SesiAktif;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Data milik uji alur dan konkurensi: poli dan obat sendiri agar data seed tidak berubah, lalu dihapus lagi
final class DataUjiAlur {

    static final String SANDI = "sehati123";

    final String penanda = String.valueOf(System.nanoTime() % 1_000_000_000L);
    final List<Integer> pasien = new ArrayList<>();
    final List<Integer> obat = new ArrayList<>();
    final List<Integer> poli = new ArrayList<>();

    static Connection pemilik() throws SQLException {
        return DriverManager.getConnection(System.getenv("DB_UJI_URL"));
    }

    static long angka(String sql, Object... nilai) throws SQLException {
        try (Connection c = pemilik(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setObject(i + 1, nilai[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : -1;
            }
        }
    }

    static String teks(String sql, Object... nilai) throws SQLException {
        try (Connection c = pemilik(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < nilai.length; i++) {
                ps.setObject(i + 1, nilai[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    static void masuk(String akun) {
        SesiAktif.lepas();
        if (new AutentikasiManager().autentikasi(akun, SANDI) == null) {
            throw new IllegalStateException("Akun seed " + akun + " tidak dapat masuk");
        }
    }

    // NIK 16 digit unik per pemanggilan
    String nik(int urutan) {
        return "3278" + String.format("%09d", Long.parseLong(penanda)) + String.format("%03d", urutan);
    }

    int poliBaru() throws SQLException {
        int id = (int) angka("INSERT INTO poli (nama_poli) VALUES (?) RETURNING id_poli", "Poli Uji Alur " + penanda);
        poli.add(id);
        return id;
    }

    // Obat baru dengan satu bets lewat penerimaan obat (UC08), sehingga stok dan bets selalu sama
    int obatBaru(int stok) throws SQLException {
        int id = (int) angka("INSERT INTO obat (nama_obat, sediaan, stok, ambang_minimum) VALUES (?, 'tablet', 0, 0) "
                + "RETURNING id_obat", "Obat Uji Alur " + penanda);
        obat.add(id);
        masuk("farmasi");
        new PersediaanManager().catatPenerimaan(new PenerimaanObatEntity(0, "F-UJI-" + penanda, LocalDate.now(),
                List.of(new BetsObatEntity("B-UJI-" + penanda, id, stok, LocalDate.now().plusYears(1)))));
        return id;
    }

    void hapus() throws SQLException {
        String k = "SELECT id_kunjungan FROM kunjungan WHERE id_pasien = ANY (?)";
        String r = "SELECT id_resep FROM resep WHERE id_kunjungan IN (" + k + ")";
        String[] perintah = {
            "DELETE FROM bundel_fhir WHERE id_kunjungan IN (" + k + ")",
            "DELETE FROM antrean_entri e USING antrean a WHERE e.id_antrean = a.id_antrean AND ((a.jenis = 'skrining'"
                    + " AND e.id_entri IN (" + k + ")) OR (a.jenis = 'apotek' AND e.id_entri IN (" + r + ")))",
            "DELETE FROM item_resep WHERE id_resep IN (" + r + ")",
            "DELETE FROM resep WHERE id_kunjungan IN (" + k + ")",
            "DELETE FROM diagnosis WHERE id_pemeriksaan IN (SELECT id_pemeriksaan FROM pemeriksaan WHERE id_kunjungan IN ("
                    + k + "))",
            "DELETE FROM pemeriksaan WHERE id_kunjungan IN (" + k + ")",
            "DELETE FROM tindak_lanjut WHERE id_entri IN (SELECT id_entri FROM daftar_pantau WHERE id_pasien = ANY (?))",
            "DELETE FROM daftar_pantau WHERE id_pasien = ANY (?)",
            "DELETE FROM penanda_risiko WHERE id_kunjungan IN (" + k + ")",
            "DELETE FROM tanda_vital WHERE id_skrining IN (SELECT id_skrining FROM skrining WHERE id_kunjungan IN ("
                    + k + "))",
            "DELETE FROM skrining WHERE id_kunjungan IN (" + k + ")",
            "DELETE FROM kunjungan WHERE id_pasien = ANY (?)",
            "DELETE FROM jadwal_kontrol WHERE id_pasien = ANY (?)",
            "DELETE FROM catatan_rekam_medis WHERE id_rekam_medis IN "
                    + "(SELECT id_rekam_medis FROM rekam_medis WHERE id_pasien = ANY (?))",
            "DELETE FROM rekam_medis WHERE id_pasien = ANY (?)",
            "DELETE FROM pasien WHERE id_pasien = ANY (?)",
        };
        try (Connection c = pemilik()) {
            var daftarPasien = c.createArrayOf("integer", pasien.toArray());
            for (String sql : perintah) {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    // Setiap parameter adalah daftar id pasien uji
                    for (int i = 1; i <= sql.chars().filter(ch -> ch == '?').count(); i++) {
                        ps.setArray(i, daftarPasien);
                    }
                    ps.executeUpdate();
                }
            }
            for (String sql : List.of("DELETE FROM bets_obat WHERE id_obat = ANY (?)",
                    "DELETE FROM obat WHERE id_obat = ANY (?)", "DELETE FROM poli WHERE id_poli = ANY (?)")) {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setArray(1, c.createArrayOf("integer",
                            (sql.contains("poli") ? poli : obat).toArray()));
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM penerimaan_obat WHERE nomor_faktur = ?")) {
                ps.setString(1, "F-UJI-" + penanda);
                ps.executeUpdate();
            }
        }
        SesiAktif.lepas();
    }
}
