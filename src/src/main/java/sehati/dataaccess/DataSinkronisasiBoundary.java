package sehati.dataaccess;

import sehati.business.entity.BundelFHIREntity;
import sehati.common.PengaturanAplikasi;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * C63 - DataSinkronisasiBoundary.
 *
 * Membaca dan menyimpan BundelFHIREntity dengan isi bundel pada kolom JSONB, serta menulis berkas ekspor bundel
 * saat luring.
 */
public class DataSinkronisasiBoundary {

    private static final Logger LOG = Logger.getLogger(DataSinkronisasiBoundary.class.getName());

    private static final String Q_C63_01 = "INSERT INTO bundel_fhir (id_kunjungan, isi_bundel, status, alasan_gagal) "
            + "VALUES (?, ?::jsonb, 'tertunda', NULL) RETURNING id_bundel";
    private static final String Q_C63_02 = "SELECT id_bundel, id_kunjungan, isi_bundel::text, status, alasan_gagal "
            + "FROM bundel_fhir WHERE status IN ('tertunda', 'gagal kirim') ORDER BY id_bundel";
    private static final String Q_C63_03 = "UPDATE bundel_fhir SET status = ?, alasan_gagal = ? WHERE id_bundel = ?";
    // Tambahan implementasi (B.1): riwayat pengiriman terakhir pada I-14 (UC11 langkah 1).
    private static final String Q_C63_04 = "SELECT id_bundel, id_kunjungan, isi_bundel::text, status, alasan_gagal "
            + "FROM bundel_fhir WHERE status = 'terkirim' ORDER BY id_bundel DESC LIMIT ?";

    private static final String UNIQUE_VIOLATION = "23505";
    private static final String CHECK_VIOLATION = "23514";
    private static final String FOREIGN_KEY_VIOLATION = "23503";
    private static final String INVALID_JSON = "22P02";
    private static final String KELAS_KONEKSI = "08";

    private static final int PANJANG_GARAM = 16;
    private static final int PANJANG_IV = 12;
    private static final int TAG_BIT = 128;
    private static final int ITERASI_PBKDF2 = 600_000;
    private static final int PANJANG_KUNCI = 256;

    private Connection koneksi;

    public DataSinkronisasiBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    /**
     * Memasukkan bundel baru ke antrean sinkronisasi dengan status "tertunda" (Q-C63-01, KF14). Dipanggil di dalam
     * transaksi serahkanObat(), sehingga bundel yang bukan JSON utuh ikut membatalkan transaksi itu (KNF03).
     */
    public void simpanBundel(BundelFHIREntity b) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C63_01)) {
            ps.setInt(1, b.getIdKunjungan());
            ps.setString(2, b.getIsiBundel());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                b.setIdBundel(rs.getInt(1));
            }
            b.setStatus("tertunda");
            b.setAlasanGagal(null);
        } catch (SQLException e) {
            throw galat("Gagal menyimpan bundel FHIR", e);
        }
    }

    /**
     * Mengambil bundel berstatus "tertunda" atau "gagal kirim" terurut id (Q-C63-02, KF20).
     */
    public List<BundelFHIREntity> ambilTertunda() {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C63_02); ResultSet rs = ps.executeQuery()) {
            List<BundelFHIREntity> hasil = new ArrayList<>();
            while (rs.next()) {
                hasil.add(bacaBundel(rs));
            }
            return hasil;
        } catch (SQLException e) {
            throw galat("Gagal mengambil antrean bundel", e);
        }
    }

    /**
     * Mengambil bundel terkirim terbaru, paling baru lebih dulu (Q-C63-04).
     */
    public List<BundelFHIREntity> ambilTerkirimTerakhir(int batas) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C63_04)) {
            ps.setInt(1, batas);
            try (ResultSet rs = ps.executeQuery()) {
                List<BundelFHIREntity> hasil = new ArrayList<>();
                while (rs.next()) {
                    hasil.add(bacaBundel(rs));
                }
                return hasil;
            }
        } catch (SQLException e) {
            throw galat("Gagal mengambil riwayat pengiriman", e);
        }
    }

    /**
     * Menyimpan status dan alasan gagal satu bundel setelah respons SATUSEHAT diterima (Q-C63-03, skenario
     * alternatif 2 UC11).
     */
    public void perbaruiBundel(BundelFHIREntity b) {
        try (PreparedStatement ps = koneksi.prepareStatement(Q_C63_03)) {
            ps.setString(1, b.getStatus());
            ps.setString(2, b.getAlasanGagal());
            ps.setInt(3, b.getIdBundel());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException("Bundel tidak ditemukan: " + b.getIdBundel());
            }
        } catch (SQLException e) {
            throw galat("Gagal memperbarui bundel", e);
        }
    }

    /**
     * Algo-C63-01: menulis seluruh bundel pada daftar ke satu berkas terenkripsi AES-GCM di lokasi, lalu
     * mengembalikan lokasi lengkap berkasnya. Frasa sandi ekspor dibaca dari berkas konfigurasi SEHATI.
     */
    public String tulisArsipTerenkripsi(List<BundelFHIREntity> daftar, String lokasi) {
        return tulisArsipTerenkripsi(daftar, lokasi, frasaSandi());
    }

    /**
     * Bagian murni Algo-C63-01 yang menerima frasa sandi secara eksplisit agar dapat diuji tanpa berkas .env.
     */
    static String tulisArsipTerenkripsi(List<BundelFHIREntity> daftar, String lokasi, String frasa) {
        String isi = daftar.stream().map(BundelFHIREntity::getIsiBundel)
                .collect(Collectors.joining(",", "[", "]"));
        try {
            byte[] garam = new byte[PANJANG_GARAM];
            byte[] iv = new byte[PANJANG_IV];
            SecureRandom acak = new SecureRandom();
            acak.nextBytes(garam);
            acak.nextBytes(iv);

            SecretKeyFactory pabrik = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spek = new PBEKeySpec(frasa.toCharArray(), garam, ITERASI_PBKDF2, PANJANG_KUNCI);
            SecretKey kunci = new SecretKeySpec(pabrik.generateSecret(spek).getEncoded(), "AES");

            Cipher sandi = Cipher.getInstance("AES/GCM/NoPadding");
            sandi.init(Cipher.ENCRYPT_MODE, kunci, new GCMParameterSpec(TAG_BIT, iv));
            byte[] terenkripsi = sandi.doFinal(isi.getBytes(StandardCharsets.UTF_8));

            String nama = "sehati-bundel_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                    + ".enc";
            Path folder = Path.of(lokasi);
            Files.createDirectories(folder);
            Path tujuan = folder.resolve(nama);
            Path sementara = Files.createTempFile(folder, "sehati-bundel_", ".tmp");
            try {
                try (OutputStream out = Files.newOutputStream(sementara)) {
                    out.write(garam);
                    out.write(iv);
                    out.write(terenkripsi);
                }
                Files.move(sementara, tujuan, StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(sementara);
            }
            return tujuan.toString();
        } catch (GeneralSecurityException | java.io.IOException e) {
            LOG.log(Level.SEVERE, "Gagal menulis arsip bundel", e);
            throw new IllegalStateException("Gagal menulis arsip bundel", e);
        }
    }

    private static BundelFHIREntity bacaBundel(ResultSet rs) throws SQLException {
        return new BundelFHIREntity(rs.getInt("id_bundel"), rs.getInt("id_kunjungan"), rs.getString("isi_bundel"),
                rs.getString("status"), rs.getString("alasan_gagal"));
    }

    private static String frasaSandi() {
        return PengaturanAplikasi.muat().ambil("EKSPOR_FRASA_SANDI");
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
            return new IllegalArgumentException("Kunjungan sudah memiliki bundel", e);
        }
        if (CHECK_VIOLATION.equals(state)) {
            return new IllegalArgumentException("Status bundel tidak valid", e);
        }
        if (FOREIGN_KEY_VIOLATION.equals(state)) {
            return new IllegalArgumentException("Kunjungan tidak ditemukan", e);
        }
        if (INVALID_JSON.equals(state)) {
            return new IllegalArgumentException("Bundel FHIR bukan JSON yang utuh", e);
        }
        if (state != null && state.startsWith(KELAS_KONEKSI)) {
            return new IllegalStateException("Basis data tidak dapat dihubungi", e);
        }
        return new IllegalStateException(pesan, e);
    }
}
