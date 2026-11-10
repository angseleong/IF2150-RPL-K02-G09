package sehati.business.control;

import sehati.business.entity.AuditEntity;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataAkunBoundary;
import sehati.dataaccess.DataFarmasiBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C25 - PersediaanManager.
 *
 * Mencatat obat masuk, menambah stok, dan menerbitkan peringatan stok di bawah ambang atau bets 90 hari menjelang
 * kedaluwarsa.
 */
public class PersediaanManager {

    private static final Logger LOG = Logger.getLogger(PersediaanManager.class.getName());

    /** Batas hari menjelang kedaluwarsa yang memicu peringatan (KF17). */
    static final int HARI_PERINGATAN = 90;
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /**
     * Seluruh obat data master beserta saldo stoknya untuk pilihan obat pada PersediaanForm (KF17).
     */
    public List<ObatEntity> muatDataObat() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataFarmasiBoundary(koneksi).ambilSemuaObat();
        } catch (SQLException e) {
            throw galatTeknis("Data obat gagal dimuat", e);
        }
    }

    /**
     * Memvalidasi penerimaan, lalu menambah stok, menyimpan penerimaan beserta betsnya, dan mencatat audit dalam satu
     * transaksi (Algo-C25-01, KF17). Pesan IllegalArgumentException berisi nama kolom yang ditolak, dipisah koma.
     */
    public void catatPenerimaan(PenerimaanObatEntity p) {
        List<String> galat = validasiPenerimaan(p);
        if (!galat.isEmpty()) {
            throw new IllegalArgumentException(String.join(",", galat));
        }
        int idPengguna = SesiAktif.idPenggunaSesi();
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);
            DataAkunBoundary dataAkun = new DataAkunBoundary(koneksi);
            koneksi.setAutoCommit(false);
            try {
                int jumlah = p.totalJumlah();
                ObatEntity obat = tambahStok(p.getDaftarBets().get(0).getIdObat(), jumlah, dataFarmasi);
                dataFarmasi.simpanPenerimaan(p);
                dataFarmasi.simpanStok(obat, p.getDaftarBets());
                dataAkun.simpanAudit(AuditEntity.buat(idPengguna, "Obat masuk " + obat.getNamaObat()
                        + " sejumlah " + jumlah + ", faktur " + p.getNomorFaktur()));
                koneksi.commit();
            } catch (SQLException | RuntimeException e) {
                koneksi.rollback();
                p.setIdPenerimaan(0);
                throw e;
            }
        } catch (SQLException e) {
            throw galatTeknis("Penerimaan obat gagal disimpan", e);
        }
    }

    /**
     * Mengembalikan nama kolom formulir yang kosong atau tidak valid, tanpa duplikat dan sesuai urutan formulir
     * (Algo-C25-02, skenario alternatif 2 UC08).
     */
    private List<String> validasiPenerimaan(PenerimaanObatEntity p) {
        Set<String> galat = new LinkedHashSet<>();
        if (p.getNomorFaktur() == null || p.getNomorFaktur().isBlank()) {
            galat.add("nomorFaktur");
        }
        LocalDate tanggalTerima = p.getTanggalTerima();
        if (tanggalTerima == null || tanggalTerima.isAfter(LocalDate.now())) {
            galat.add("tanggalTerima");
        }
        List<BetsObatEntity> daftarBets = p.getDaftarBets();
        if (daftarBets == null || daftarBets.isEmpty()) {
            galat.add("nomorBets");
            return new ArrayList<>(galat);
        }
        int idObatPertama = daftarBets.get(0).getIdObat();
        for (BetsObatEntity bets : daftarBets) {
            if (bets.getNomorBets() == null || bets.getNomorBets().isBlank()) {
                galat.add("nomorBets");
            }
            if (bets.getJumlah() <= 0) {
                galat.add("jumlah");
            }
            LocalDate kedaluwarsa = bets.getTanggalKedaluwarsa();
            // Tanggal terima yang kosong sudah ditandai di atas, jadi kedaluwarsa hanya dibandingkan bila ada.
            if (kedaluwarsa == null || (tanggalTerima != null && !kedaluwarsa.isAfter(tanggalTerima))) {
                galat.add("tanggalKedaluwarsa");
            }
            if (bets.getIdObat() <= 0 || bets.getIdObat() != idObatPertama) {
                galat.add("obat");
            }
        }
        return new ArrayList<>(galat);
    }

    // Boundary ikut diteruskan agar penambahan stok memakai koneksi transaksi catatPenerimaan (aturan 1 BAB 5).
    private ObatEntity tambahStok(int idObat, int jumlah, DataFarmasiBoundary dataFarmasi) {
        ObatEntity obat = dataFarmasi.ambilObat(idObat);
        if (obat == null) {
            throw new IllegalArgumentException("obat");
        }
        obat.tambahStok(jumlah);
        return obat;
    }

    /**
     * Menyusun peringatan untuk obat yang stoknya di bawah ambang minimum dan bets bersisa yang kedaluwarsa dalam 90
     * hari atau kurang (Algo-C25-03, KF17).
     */
    public List<String> terbitkanPeringatan() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return terbitkanPeringatan(new DataFarmasiBoundary(koneksi));
        } catch (SQLException e) {
            throw galatTeknis("Peringatan persediaan gagal dimuat", e);
        }
    }

    // Dipisah agar isi dan urutan pesan dapat diuji tanpa basis data.
    static List<String> terbitkanPeringatan(DataFarmasiBoundary dataFarmasi) {
        List<String> peringatan = new ArrayList<>();
        for (ObatEntity obat : dataFarmasi.obatPerluPeringatan(HARI_PERINGATAN)) {
            if (obat.diBawahAmbang()) {
                peringatan.add("Stok " + obat.getNamaObat() + " tinggal " + obat.getStok() + ", di bawah ambang "
                        + obat.getAmbangMinimum());
            }
            for (BetsObatEntity bets : dataFarmasi.ambilBets(obat.getIdObat())) {
                // Bets yang sudah habis tidak perlu dipakai lebih dulu, jadi tidak diperingatkan.
                if (bets.getJumlah() > 0 && bets.mendekatiKedaluwarsa(HARI_PERINGATAN)) {
                    peringatan.add("Bets " + bets.getNomorBets() + " (" + obat.getNamaObat() + ") kedaluwarsa "
                            + bets.getTanggalKedaluwarsa().format(TANGGAL));
                }
            }
        }
        return peringatan;
    }

    private static IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ". Hubungi administrator bila berulang.", e);
    }
}
