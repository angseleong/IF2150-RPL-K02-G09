package sehati.dataaccess;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import sehati.business.entity.LaporanEntity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C62 - DataLaporanBoundary.
 *
 * Menjalankan kueri agregasi kunjungan dan diagnosis, menyimpan LaporanEntity, dan menulis berkas PDF dan CSV ke
 * penyimpanan lokal.
 */
public class DataLaporanBoundary {

    private static final Logger LOG = Logger.getLogger(DataLaporanBoundary.class.getName());

    private static final String Q_C62_01 = "SELECT p.nama_poli, COUNT(*) AS jumlah FROM kunjungan k "
            + "JOIN poli p ON p.id_poli = k.id_poli WHERE k.tanggal BETWEEN ? AND ? "
            + "GROUP BY p.nama_poli ORDER BY p.nama_poli";
    private static final String Q_C62_02 = "SELECT k.status, COUNT(*) AS jumlah FROM kunjungan k "
            + "WHERE k.tanggal BETWEEN ? AND ? GROUP BY k.status ORDER BY k.status";
    private static final String Q_C62_03 = "SELECT d.kode_icd10, i.deskripsi, COUNT(*) AS jumlah FROM diagnosis d "
            + "JOIN pemeriksaan pm ON pm.id_pemeriksaan = d.id_pemeriksaan "
            + "JOIN kunjungan k ON k.id_kunjungan = pm.id_kunjungan "
            + "JOIN kode_icd10 i ON i.kode = d.kode_icd10 "
            + "WHERE k.tanggal BETWEEN ? AND ? AND pm.terkunci "
            + "GROUP BY d.kode_icd10, i.deskripsi ORDER BY jumlah DESC, d.kode_icd10 LIMIT 10";
    private static final String Q_C62_04 = "INSERT INTO laporan (tanggal_mulai, tanggal_akhir, format, ringkasan) "
            + "VALUES (?, ?, ?, ?) RETURNING id_laporan";

    private Connection koneksi;

    public DataLaporanBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    /**
     * Algo-C62-01: membaca agregasi kunjungan per poli, per status, dan sepuluh diagnosis terbanyak dalam satu
     * transaksi hanya-baca REPEATABLE READ, lalu menyusun ringkasan berformat bagian;label;jumlah.
     */
    public LaporanEntity agregasiKunjungan(LocalDate mulai, LocalDate akhir) {
        boolean autoCommit = true;
        int isolasi = Connection.TRANSACTION_READ_COMMITTED;
        boolean bacaSaja = false;
        try {
            autoCommit = koneksi.getAutoCommit();
            isolasi = koneksi.getTransactionIsolation();
            bacaSaja = koneksi.isReadOnly();

            koneksi.setAutoCommit(false);
            koneksi.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            koneksi.setReadOnly(true);

            List<Baris> poli = bacaSatuKolom(Q_C62_01, mulai, akhir);
            List<Baris> status = bacaSatuKolom(Q_C62_02, mulai, akhir);
            List<Baris> diagnosis = bacaDiagnosis(mulai, akhir);

            koneksi.commit();

            long total = 0;
            for (Baris b : status) {
                total += b.jumlah();
            }

            StringBuilder ringkasan = new StringBuilder();
            ringkasan.append("total;semua kunjungan;").append(total);
            for (Baris b : poli) {
                ringkasan.append('\n').append("poli;").append(label(b.label())).append(';').append(b.jumlah());
            }
            for (Baris b : status) {
                ringkasan.append('\n').append("status;").append(label(b.label())).append(';').append(b.jumlah());
            }
            for (Baris b : diagnosis) {
                ringkasan.append('\n').append("diagnosis;").append(label(b.label())).append(';').append(b.jumlah());
            }
            return new LaporanEntity(0, mulai, akhir, null, ringkasan.toString());
        } catch (SQLException e) {
            batalkan();
            throw galat("Gagal menghitung rekapitulasi kunjungan", e);
        } finally {
            pulihkan(autoCommit, isolasi, bacaSaja);
        }
    }

    /**
     * Q-C62-04: menyimpan ringkasan laporan dan mengisi id laporan hasil RETURNING.
     */
    public void simpanLaporan(LaporanEntity l) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C62_04)) {
            ps.setDate(1, Date.valueOf(l.getTanggalMulai()));
            ps.setDate(2, Date.valueOf(l.getTanggalAkhir()));
            ps.setString(3, l.getFormat());
            ps.setString(4, l.getRingkasan());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                l.setIdLaporan(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw galat("Gagal menyimpan laporan", e);
        }
    }

    /**
     * Algo-C62-02: menulis ringkasan ke berkas CSV atau PDF di direktori tujuan secara atomik.
     */
    public String tulisBerkas(LaporanEntity l, String format, String direktori) {
        if (!"PDF".equals(format) && !"CSV".equals(format)) {
            throw new IllegalArgumentException("Format laporan harus PDF atau CSV");
        }
        String nama = "laporan-sehati_" + l.getTanggalMulai() + "_" + l.getTanggalAkhir() + "."
                + format.toLowerCase(Locale.ROOT);
        Path folder = Path.of(direktori);
        Path sementara = null;
        try {
            Files.createDirectories(folder);
            Path tujuan = folder.resolve(nama);
            sementara = Files.createTempFile(folder, "laporan-sehati_", ".tmp");
            if ("CSV".equals(format)) {
                tulisCsv(l, sementara);
            } else {
                tulisPdf(l, sementara);
            }
            Files.move(sementara, tujuan, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            sementara = null;
            return tujuan.toString();
        } catch (IOException e) {
            throw new UncheckedIOException("Berkas laporan tidak dapat ditulis ke " + direktori, e);
        } finally {
            if (sementara != null) {
                try {
                    Files.deleteIfExists(sementara);
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Berkas sementara laporan tidak dapat dihapus", e);
                }
            }
        }
    }

    private List<Baris> bacaSatuKolom(String sql, LocalDate mulai, LocalDate akhir) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(mulai));
            ps.setDate(2, Date.valueOf(akhir));
            try (ResultSet rs = ps.executeQuery()) {
                List<Baris> hasil = new ArrayList<>();
                while (rs.next()) {
                    hasil.add(new Baris(rs.getString(1), rs.getInt("jumlah")));
                }
                return hasil;
            }
        }
    }

    private List<Baris> bacaDiagnosis(LocalDate mulai, LocalDate akhir) throws SQLException {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C62_03)) {
            ps.setDate(1, Date.valueOf(mulai));
            ps.setDate(2, Date.valueOf(akhir));
            try (ResultSet rs = ps.executeQuery()) {
                List<Baris> hasil = new ArrayList<>();
                while (rs.next()) {
                    hasil.add(new Baris(rs.getString(1) + " " + rs.getString(2), rs.getInt("jumlah")));
                }
                return hasil;
            }
        }
    }

    private static String label(String teks) {
        return teks == null ? "" : teks.replace(';', ',');
    }

    private static void tulisCsv(LaporanEntity l, Path berkas) throws IOException {
        try (BufferedWriter penulis = Files.newBufferedWriter(berkas, StandardCharsets.UTF_8)) {
            penulis.write("bagian;label;jumlah");
            penulis.newLine();
            String ringkasan = l.getRingkasan() == null ? "" : l.getRingkasan();
            for (String baris : ringkasan.split("\\R")) {
                if (!baris.isEmpty()) {
                    penulis.write(baris);
                    penulis.newLine();
                }
            }
        }
    }

    private static void tulisPdf(LaporanEntity l, Path berkas) throws IOException {
        List<String> baris = new ArrayList<>();
        baris.add("Laporan Pelayanan SEHATI");
        baris.add("Rentang: " + l.getTanggalMulai() + " s.d. " + l.getTanggalAkhir());
        baris.add("");
        String ringkasan = l.getRingkasan() == null ? "" : l.getRingkasan();
        for (String b : ringkasan.split("\\R")) {
            if (!b.isEmpty()) {
                baris.add(b);
            }
        }

        try (PDDocument dokumen = new PDDocument()) {
            PDFont biasa = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont tebal = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            int perHalaman = 45;
            for (int mulai = 0; mulai < baris.size(); mulai += perHalaman) {
                PDPage halaman = new PDPage(PDRectangle.A4);
                dokumen.addPage(halaman);
                try (PDPageContentStream isi = new PDPageContentStream(dokumen, halaman)) {
                    float y = PDRectangle.A4.getHeight() - 50;
                    int akhir = Math.min(mulai + perHalaman, baris.size());
                    for (int i = mulai; i < akhir; i++) {
                        String teks = baris.get(i);
                        if (teks.isEmpty()) {
                            y -= 14;
                            continue;
                        }
                        boolean judul = i == 0;
                        isi.beginText();
                        isi.setFont(judul ? tebal : biasa, judul ? 16 : 10);
                        isi.newLineAtOffset(50, y);
                        isi.showText(sanitasi(teks));
                        isi.endText();
                        y -= judul ? 24 : 14;
                    }
                }
            }
            dokumen.save(berkas.toFile());
        }
    }

    private static String sanitasi(String teks) {
        StringBuilder aman = new StringBuilder();
        for (char c : teks.toCharArray()) {
            if (c == '\\' || c == '(' || c == ')') {
                aman.append('\\').append(c);
            } else if (c < 32 || c > 126) {
                aman.append('?');
            } else {
                aman.append(c);
            }
        }
        return aman.toString();
    }

    private void batalkan() {
        try {
            koneksi.rollback();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Gagal membatalkan transaksi laporan", e);
        }
    }

    private void pulihkan(boolean autoCommit, int isolasi, boolean bacaSaja) {
        try {
            koneksi.setReadOnly(bacaSaja);
            koneksi.setTransactionIsolation(isolasi);
            koneksi.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Gagal memulihkan pengaturan koneksi laporan", e);
        }
    }

    private static IllegalStateException galat(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ". Hubungi administrator bila berulang.", e);
    }

    private record Baris(String label, int jumlah) {
    }
}
