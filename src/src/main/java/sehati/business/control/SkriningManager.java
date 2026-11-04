package sehati.business.control;

import sehati.business.entity.AntreanEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PoliEntity;
import sehati.business.entity.SkriningEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.common.KoneksiBasisData;
import sehati.dataaccess.DataKlinisBoundary;
import sehati.dataaccess.DataKunjunganBoundary;
import sehati.dataaccess.DataPasienBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C20 - Memanggil pasien terdepan, memvalidasi rentang fisiologis tanda vital tanpa jalan pintas, menyimpan hasil
 * skrining, lalu meneruskannya ke RisikoManager (UC04).
 */
public class SkriningManager {

    public static final String STATUS_MENUNGGU = "menunggu skrining";
    public static final String STATUS_DISKRINING = "sedang diskrining";
    public static final String STATUS_TIDAK_HADIR = "tidak hadir";
    public static final String STATUS_SIAP_DIPERIKSA = "siap diperiksa dokter";

    private static final Logger LOG = Logger.getLogger(SkriningManager.class.getName());

    // Rentang pada Algo-C20-02 sengaja konstanta, bukan data master, agar tidak dapat dilonggarkan (KNF02).
    private static final double[] SISTOLIK = {50, 300};
    private static final double[] DIASTOLIK = {30, 200};
    private static final double[] BERAT_BADAN = {1, 350};
    private static final double[] TINGGI_BADAN = {30, 250};
    private static final double[] SUHU = {30, 45};
    private static final double[] NADI = {20, 250};
    private static final double[] GULA_DARAH = {20, 1000};

    private final RisikoManager risikoManager = new RisikoManager();

    /**
     * Antrean skrining terurut nomor antrean, hanya pasien yang menunggu atau sedang diskrining (KF04).
     */
    public List<KunjunganEntity> muatAntrean() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return kunjunganTertunda(new DataKunjunganBoundary(koneksi));
        } catch (SQLException e) {
            throw galatTeknis("Antrean skrining gagal dimuat", e);
        }
    }

    /**
     * Memanggil kunjungan terdepan yang menunggu, atau null bila antrean kosong (KF04).
     */
    public KunjunganEntity panggilPasien() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataKunjunganBoundary data = new DataKunjunganBoundary(koneksi);
            return dalamTransaksi(koneksi, () -> {
                // Antrean dikunci agar dua perawat tidak memanggil pasien yang sama.
                AntreanEntity antrean = data.ambilAntrean("skrining");
                KunjunganEntity terdepan = pertamaMenunggu(data, antrean);
                if (terdepan == null) {
                    return null;
                }
                terdepan.perbaruiStatus(STATUS_DISKRINING);
                data.simpanKunjungan(terdepan);
                return terdepan;
            });
        } catch (SQLException e) {
            throw galatTeknis("Pasien gagal dipanggil", e);
        }
    }

    /**
     * Menandai pasien tidak hadir, memindahkannya ke belakang, lalu mengembalikan pasien berikutnya
     * (Algo-C20-03).
     */
    public KunjunganEntity lewatiPasien(int idKunjungan) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataKunjunganBoundary data = new DataKunjunganBoundary(koneksi);
            return dalamTransaksi(koneksi, () -> {
                AntreanEntity antrean = data.ambilAntrean("skrining");
                antrean.pindahkanKeBelakang(idKunjungan);
                KunjunganEntity kunjungan = data.ambilKunjungan(idKunjungan);
                if (kunjungan == null) {
                    throw new IllegalArgumentException("Kunjungan tidak ditemukan: " + idKunjungan);
                }
                kunjungan.perbaruiStatus(STATUS_TIDAK_HADIR);
                data.simpanKunjungan(kunjungan);
                data.simpanAntrean(antrean);
                return pertamaMenunggu(data, antrean);
            });
        } catch (SQLException e) {
            throw galatTeknis("Pasien gagal dilewati", e);
        }
    }

    /**
     * Menyimpan skrining bila seluruh nilai dalam rentang, lalu mengevaluasi risiko. Mengembalikan kolom yang
     * di luar rentang; daftar kosong berarti tersimpan (Algo-C20-01, KF05-KF07).
     */
    public List<String> simpanSkrining(int idKunjungan, String keluhan, TandaVitalEntity tv) {
        List<String> galat = validasiRentang(tv);
        if (!galat.isEmpty()) {
            return galat;
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataKlinisBoundary dataKlinis = new DataKlinisBoundary(koneksi);
            DataKunjunganBoundary dataKunjungan = new DataKunjunganBoundary(koneksi);
            dalamTransaksi(koneksi, () -> {
                KunjunganEntity kunjungan = dataKunjungan.ambilKunjungan(idKunjungan);
                if (kunjungan == null) {
                    throw new IllegalArgumentException("Kunjungan tidak ditemukan: " + idKunjungan);
                }
                SkriningEntity skrining = new SkriningEntity(0, idKunjungan, keluhan, LocalDateTime.now());
                skrining.lampirkanTandaVital(tv);
                dataKlinis.simpanSkrining(skrining);
                risikoManager.evaluasiRisiko(koneksi, kunjungan, tv);
                kunjungan.perbaruiStatus(STATUS_SIAP_DIPERIKSA);
                dataKunjungan.simpanKunjungan(kunjungan);
                return null;
            });
        } catch (SQLException e) {
            throw galatTeknis("Skrining gagal disimpan", e);
        }
        return new ArrayList<>();
    }

    /**
     * Teks identitas untuk layar, misalnya "Budi Santoso  /  RM-000121  /  Poli Umum  /  Antrean 1" (I-05-02).
     * Operasi tambahan di luar Tabel 4.1 karena Form tidak boleh membaca DataBoundary.
     */
    public String labelPasien(KunjunganEntity kunjungan) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            PasienEntity pasien = new DataPasienBoundary(koneksi).ambilPasien(kunjungan.getIdPasien());
            String poli = "-";
            for (PoliEntity p : new DataKunjunganBoundary(koneksi).ambilSemuaPoli()) {
                if (p.getIdPoli() == kunjungan.getIdPoli()) {
                    poli = p.getNamaPoli();
                }
            }
            return pasien.getNama() + "  /  " + pasien.getNomorRekamMedis() + "  /  " + poli + "  /  Antrean "
                    + kunjungan.getNomorAntrean();
        } catch (SQLException e) {
            throw galatTeknis("Identitas pasien gagal dimuat", e);
        }
    }

    /**
     * Mengembalikan nama kolom yang kosong (selain gula darah) atau di luar rentang (Algo-C20-02).
     */
    List<String> validasiRentang(TandaVitalEntity tv) {
        List<String> galat = new ArrayList<>();
        periksa(galat, "sistolik", tv.getSistolik(), SISTOLIK);
        periksa(galat, "diastolik", tv.getDiastolik(), DIASTOLIK);
        periksa(galat, "beratBadan", tv.getBeratBadan(), BERAT_BADAN);
        periksa(galat, "tinggiBadan", tv.getTinggiBadan(), TINGGI_BADAN);
        periksa(galat, "suhu", tv.getSuhu(), SUHU);
        periksa(galat, "nadi", tv.getNadi(), NADI);
        if (tv.getGulaDarah() != null) {
            periksa(galat, "gulaDarah", tv.getGulaDarah(), GULA_DARAH);
        }
        if (!galat.contains("sistolik") && !galat.contains("diastolik") && tv.getDiastolik() >= tv.getSistolik()) {
            galat.add("diastolik");
        }
        return galat;
    }

    private static void periksa(List<String> galat, String kolom, double nilai, double[] rentang) {
        if (nilai < rentang[0] || nilai > rentang[1]) {
            galat.add(kolom);
        }
    }

    private List<KunjunganEntity> kunjunganTertunda(DataKunjunganBoundary data) {
        List<KunjunganEntity> hasil = new ArrayList<>();
        for (int id : data.ambilAntrean("skrining").getDaftarEntri()) {
            KunjunganEntity k = data.ambilKunjungan(id);
            if (k != null && statusAktif(k)) {
                hasil.add(k);
            }
        }
        hasil.sort((a, b) -> Integer.compare(a.getNomorAntrean(), b.getNomorAntrean()));
        return hasil;
    }

    // Pasien yang dilewati tetap boleh dipanggil lagi bila akhirnya hadir, tetapi diurutkan setelah yang menunggu.
    private static KunjunganEntity pertamaMenunggu(DataKunjunganBoundary data, AntreanEntity antrean) {
        KunjunganEntity tidakHadir = null;
        for (int id : antrean.getDaftarEntri()) {
            KunjunganEntity k = data.ambilKunjungan(id);
            if (k == null) {
                continue;
            }
            if (STATUS_MENUNGGU.equals(k.getStatus())) {
                return k;
            }
            if (tidakHadir == null && STATUS_TIDAK_HADIR.equals(k.getStatus())) {
                tidakHadir = k;
            }
        }
        return tidakHadir;
    }

    private static boolean statusAktif(KunjunganEntity k) {
        return STATUS_MENUNGGU.equals(k.getStatus()) || STATUS_DISKRINING.equals(k.getStatus())
                || STATUS_TIDAK_HADIR.equals(k.getStatus());
    }

    @FunctionalInterface
    interface Operasi<T> {
        T jalankan();
    }

    // Satu transaksi: COMMIT bila berhasil, ROLLBACK bila gagal (aturan 1 Subbab 5.56).
    static <T> T dalamTransaksi(Connection koneksi, Operasi<T> operasi) throws SQLException {
        koneksi.setAutoCommit(false);
        try {
            T hasil = operasi.jalankan();
            koneksi.commit();
            return hasil;
        } catch (RuntimeException e) {
            koneksi.rollback();
            throw e;
        }
    }

    static IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ". Hubungi administrator bila berulang.", e);
    }
}
