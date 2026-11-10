package sehati.business.control;

import sehati.common.SesiAktif;
import sehati.dataaccess.DataAkunBoundary;
import sehati.business.entity.AuditEntity;
import sehati.business.entity.AntreanEntity;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.BundelFHIREntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.ResepEntity;
import sehati.common.KoneksiBasisData;
import sehati.dataaccess.DataFarmasiBoundary;
import sehati.dataaccess.DataKlinisBoundary;
import sehati.dataaccess.DataKunjunganBoundary;
import sehati.dataaccess.DataPasienBoundary;
import sehati.dataaccess.DataSinkronisasiBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C24 - ApotekManager.
 *
 * Melayani resep, lalu dalam satu transaksi memotong stok, menandai resep selesai, menutup kunjungan, dan menyusun
 * bundel FHIR.
 */
public class ApotekManager {

    private static final Logger LOG = Logger.getLogger(ApotekManager.class.getName());
    private static final String ANTREAN_APOTEK = "apotek";

    /**
     * Resep berstatus "menunggu" terurut waktu masuk beserta itemnya untuk AntreanResepForm (KF13).
     */
    public List<ResepEntity> muatAntreanResep() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataFarmasiBoundary(koneksi).ambilAntreanResep();
        } catch (SQLException e) {
            throw galatTeknis("Antrean resep gagal dimuat", e);
        }
    }

    /**
     * Pasien pemilik setiap resep, dipetakan dengan idResep, dalam satu kueri (tambahan implementasi, B.1).
     */
    public Map<Integer, PasienEntity> muatPasienResep(List<Integer> daftarIdResep) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataFarmasiBoundary(koneksi).ambilPasienResep(daftarIdResep);
        } catch (SQLException e) {
            throw galatTeknis("Data pasien resep gagal dimuat", e);
        }
    }

    /**
     * Seluruh obat data master untuk menampilkan nama sediaan pada antrean dan rincian resep (tambahan implementasi,
     * B.1).
     */
    public List<ObatEntity> muatDataObat() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataFarmasiBoundary(koneksi).ambilSemuaObat();
        } catch (SQLException e) {
            throw galatTeknis("Data obat gagal dimuat", e);
        }
    }

    /**
     * Mengambil rincian resep untuk PenyerahanObatForm. Resep yang sudah tidak menunggu ditolak lebih awal agar
     * petugas tidak menyiapkan obat untuk resep yang sudah dilayani petugas lain.
     */
    public ResepEntity layaniResep(int idResep) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            ResepEntity resep = new DataFarmasiBoundary(koneksi).ambilResep(idResep);
            if (resep == null) {
                throw new IllegalArgumentException("Resep tidak ditemukan");
            }
            pastikanMenunggu(resep);
            return resep;
        } catch (SQLException e) {
            throw galatTeknis("Rincian resep gagal dimuat", e);
        }
    }

    /**
     * Bets bersisa setiap obat pada resep, kedaluwarsa terdekat lebih dulu (FEFO), untuk I-10-04 (tambahan
     * implementasi, B.1).
     */
    public List<BetsObatEntity> muatBetsResep(ResepEntity resep) {
        Set<Integer> daftarIdObat = new LinkedHashSet<>();
        for (ItemResepEntity item : resep.getDaftarItem()) {
            daftarIdObat.add(item.getIdObat());
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);
            List<BetsObatEntity> hasil = new ArrayList<>();
            for (int idObat : daftarIdObat) {
                hasil.addAll(dataFarmasi.ambilBets(idObat));
            }
            return hasil;
        } catch (SQLException e) {
            throw galatTeknis("Bets obat gagal dimuat", e);
        }
    }

    /**
     * Dalam satu transaksi: memotong stok, menandai resep selesai dilayani, menutup kunjungan, dan menyimpan bundel
     * FHIR ke antrean sinkronisasi (Algo-C24-01, KF13, KF14, KF23). Bila satu langkah gagal, seluruhnya dibatalkan
     * sehingga tidak ada bundel parsial (KNF03).
     */
    public void serahkanObat(int idResep) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);
            DataKunjunganBoundary dataKunjungan = new DataKunjunganBoundary(koneksi);
            DataSinkronisasiBoundary dataSinkronisasi = new DataSinkronisasiBoundary(koneksi);
            koneksi.setAutoCommit(false);
            try {
                // Petugas kedua tertahan di sini sampai transaksi pertama selesai, lalu membaca status barunya.
                ResepEntity resep = dataFarmasi.kunciResep(idResep);
                if (resep == null) {
                    throw new IllegalArgumentException("Resep tidak ditemukan");
                }
                pastikanMenunggu(resep);
                DataKlinisBoundary dataKlinis = new DataKlinisBoundary(koneksi);
                pastikanTerkunci(dataKlinis.ambilPemeriksaan(resep.getIdKunjungan()));
                Map<Integer, ObatEntity> obat = potongStok(resep, dataFarmasi);
                resep.perbaruiStatus(ResepEntity.STATUS_SELESAI_DILAYANI);
                dataFarmasi.simpanResep(resep);
                keluarkanDariAntrean(idResep, dataKunjungan);
                KunjunganEntity kunjungan = tutupKunjungan(resep.getIdKunjungan(), dataKunjungan);
                BundelFHIREntity bundel = susunBundel(kunjungan, resep, obat, new DataPasienBoundary(koneksi),
                        dataKlinis);
                dataSinkronisasi.simpanBundel(bundel);
                new DataAkunBoundary(koneksi).simpanAudit(AuditEntity.buat(SesiAktif.idPenggunaAudit(),
                        "Obat resep " + idResep + " diserahkan"));
                koneksi.commit();
            } catch (SQLException | RuntimeException e) {
                koneksi.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw galatTeknis("Penyerahan obat gagal disimpan", e);
        }
    }

    /**
     * Resep yang sudah selesai atau dikembalikan ditolak dengan pesan yang ditampilkan PenyerahanObatForm
     * (Algo-C24-01, skenario alternatif 2 UC07).
     */
    static void pastikanMenunggu(ResepEntity resep) {
        if (ResepEntity.STATUS_SELESAI_DILAYANI.equals(resep.getStatus())) {
            throw new IllegalStateException("Resep sudah selesai dilayani");
        }
        if (!ResepEntity.STATUS_MENUNGGU.equals(resep.getStatus())) {
            throw new IllegalStateException("Resep berstatus " + resep.getStatus() + " dan tidak dapat diserahkan");
        }
    }

    // Kunjungan ditutup saat obat diserahkan, jadi rekam medis harus sudah final agar tidak bisa diubah lagi
    static void pastikanTerkunci(PemeriksaanEntity pemeriksaan) {
        if (pemeriksaan == null || !pemeriksaan.isTerkunci()) {
            throw new IllegalStateException("Dokter belum menyimpan final pemeriksaan pasien ini");
        }
    }

    /**
     * Memotong stok setiap obat dan mengurangi betsnya dengan aturan FEFO (Algo-C24-02). Dipanggil di dalam
     * transaksi serahkanObat(), jadi boundary memakai koneksi transaksi itu. Mengembalikan obat yang dipotong,
     * dipetakan dengan idObat, untuk nama sediaan pada bundel FHIR.
     */
    static Map<Integer, ObatEntity> potongStok(ResepEntity resep, DataFarmasiBoundary dataFarmasi) {
        Map<Integer, ObatEntity> dipotong = new LinkedHashMap<>();
        // Item terurut idObat (Q-C61-14), sehingga baris obat selalu dikunci dalam urutan yang sama (tanpa deadlock).
        for (ItemResepEntity item : resep.getDaftarItem()) {
            ObatEntity obat = dataFarmasi.ambilObat(item.getIdObat());
            if (obat == null) {
                throw new IllegalStateException("Obat " + item.getIdObat() + " tidak ada pada data master");
            }
            if (!item.cukupStok(obat.getStok())) {
                throw new IllegalStateException("Stok " + obat.getNamaObat() + " tidak cukup");
            }
            obat.kurangiStok(item.getJumlah());
            List<BetsObatEntity> daftarBets = dataFarmasi.ambilBets(item.getIdObat());
            int sisa = item.getJumlah();
            for (BetsObatEntity bets : daftarBets) {
                if (sisa == 0) {
                    break;
                }
                int ambil = Math.min(sisa, bets.getJumlah());
                bets.kurangi(ambil);
                sisa -= ambil;
            }
            // Saldo obat selalu sama dengan jumlah bets (Subbab 5.48), jadi selisih berarti data stok rusak.
            if (sisa > 0) {
                throw new IllegalStateException("Bets " + obat.getNamaObat() + " tidak mencukupi, periksa persediaan");
            }
            dataFarmasi.simpanStok(obat, daftarBets);
            dipotong.put(obat.getIdObat(), obat);
        }
        return dipotong;
    }

    private KunjunganEntity tutupKunjungan(int idKunjungan, DataKunjunganBoundary dataKunjungan) {
        KunjunganEntity kunjungan = dataKunjungan.ambilKunjungan(idKunjungan);
        if (kunjungan == null) {
            throw new IllegalStateException("Kunjungan resep tidak ditemukan");
        }
        kunjungan.perbaruiStatus(KunjunganEntity.STATUS_SELESAI);
        dataKunjungan.simpanKunjungan(kunjungan);
        return kunjungan;
    }

    // Bundel berstatus "tertunda" sampai dikirim SinkronisasiManager (KF14, KNF07).
    private BundelFHIREntity susunBundel(KunjunganEntity kunjungan, ResepEntity resep, Map<Integer, ObatEntity> obat,
            DataPasienBoundary dataPasien, DataKlinisBoundary dataKlinis) {
        PasienEntity pasien = dataPasien.ambilPasien(kunjungan.getIdPasien());
        String isi = PenyusunBundelFHIR.susun(kunjungan, pasien, dataKlinis.ambilSkrining(kunjungan.getIdKunjungan()),
                dataKlinis.ambilDiagnosis(kunjungan.getIdKunjungan()), resep, obat,
                resep.getWaktuMasuk() == null ? LocalDateTime.now() : resep.getWaktuMasuk());
        return new BundelFHIREntity(0, kunjungan.getIdKunjungan(), isi, "tertunda", null);
    }

    /**
     * Menandai resep "dikembalikan" beserta alasannya dan mengeluarkannya dari antrean apotek, sementara kunjungan
     * tetap terbuka (skenario alternatif 1 UC07).
     */
    public void kembalikanKeDokter(int idResep, String alasan) {
        if (alasan == null || alasan.isBlank()) {
            throw new IllegalArgumentException("Alasan pengembalian wajib diisi");
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);
            DataKunjunganBoundary dataKunjungan = new DataKunjunganBoundary(koneksi);
            koneksi.setAutoCommit(false);
            try {
                // Dikunci seperti serahkanObat() agar pengembalian tidak menimpa resep yang baru saja diserahkan.
                ResepEntity resep = dataFarmasi.kunciResep(idResep);
                if (resep == null) {
                    throw new IllegalArgumentException("Resep tidak ditemukan");
                }
                resep.perbaruiStatus(ResepEntity.STATUS_DIKEMBALIKAN);
                resep.setCatatan(alasan.strip());
                dataFarmasi.simpanResep(resep);
                keluarkanDariAntrean(idResep, dataKunjungan);
                new DataAkunBoundary(koneksi).simpanAudit(AuditEntity.buat(SesiAktif.idPenggunaAudit(),
                        "Resep " + idResep + " dikembalikan ke dokter: " + alasan.strip()));
                koneksi.commit();
            } catch (SQLException | RuntimeException e) {
                koneksi.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw galatTeknis("Resep gagal dikembalikan ke dokter", e);
        }
    }

    // Resep yang tidak lagi menunggu dikeluarkan dari antrean apotek agar antrean_entri tidak terus bertambah.
    // Resep lama yang tidak tercatat di antrean dilewati, bukan ditolak.
    private void keluarkanDariAntrean(int idResep, DataKunjunganBoundary dataKunjungan) {
        AntreanEntity antrean = dataKunjungan.ambilAntrean(ANTREAN_APOTEK);
        if (antrean.getDaftarEntri().contains(idResep)) {
            antrean.keluarkan(idResep);
            dataKunjungan.simpanAntrean(antrean);
        }
    }

    private static IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ". Hubungi administrator bila berulang.", e);
    }
}
