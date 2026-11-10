package sehati.business.control;

import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.JadwalKontrolEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PenandaRisikoEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.common.KoneksiBasisData;
import sehati.dataaccess.DataKlinisBoundary;
import sehati.dataaccess.DataPantauBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * C21 - Membandingkan tanda vital terhadap ambang dan riwayat tiga kunjungan, menerbitkan penanda risiko, dan
 * memasukkan pasien ke daftar pantau (UC04, UC09).
 */
public class RisikoManager {

    static final String JENIS_KUNJUNGAN_ULANG = "kunjungan ulang";
    private static final int JUMLAH_RIWAYAT = 3;

    /**
     * Evaluasi pada koneksi sendiri (Algo-C21-01).
     */
    public List<PenandaRisikoEntity> evaluasiRisiko(KunjunganEntity kunjungan, TandaVitalEntity tv) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return SkriningManager.dalamTransaksi(koneksi, () -> evaluasiRisiko(koneksi, kunjungan, tv));
        } catch (SQLException e) {
            throw SkriningManager.galatTeknis("Risiko gagal dievaluasi", e);
        }
    }

    /**
     * Evaluasi pada koneksi milik pemanggil agar penanda ikut satu transaksi dengan skrining (Algo-C20-01).
     */
    List<PenandaRisikoEntity> evaluasiRisiko(Connection koneksi, KunjunganEntity kunjungan, TandaVitalEntity tv) {
        DataKlinisBoundary dataKlinis = new DataKlinisBoundary(koneksi);
        DataPantauBoundary dataPantau = new DataPantauBoundary(koneksi);
        List<String> jenis = jenisRisikoTerpenuhi(dataKlinis.ambilAmbang(),
                dataKlinis.riwayatTandaVital(kunjungan.getIdPasien(), JUMLAH_RIWAYAT), tv);
        List<PenandaRisikoEntity> penanda = new ArrayList<>();
        for (String j : jenis) {
            penanda.add(terbitkanPenanda(dataKlinis, kunjungan.getIdKunjungan(), j));
            masukkanKeDaftarPantau(dataPantau, kunjungan.getIdPasien(), j);
        }
        return penanda;
    }

    /**
     * Inti KF07 tanpa akses data: risiko terpenuhi bila nilai hari ini melampaui ambang, atau tiga pengukuran
     * sebelumnya berturut-turut melampauinya. Gula darah kosong tidak pernah melampaui.
     */
    static List<String> jenisRisikoTerpenuhi(
            List<AmbangRisikoEntity> daftarAmbang, List<TandaVitalEntity> riwayat, TandaVitalEntity tv) {
        List<String> hasil = new ArrayList<>();
        for (AmbangRisikoEntity ambang : daftarAmbang) {
            Double kini = nilaiUkur(ambang.getJenisRisiko(), tv);
            boolean lampauKini = kini != null && ambang.dilampaui(kini);
            boolean lampauBeruntun = riwayat.size() == JUMLAH_RIWAYAT;
            for (TandaVitalEntity r : riwayat) {
                Double nilai = nilaiUkur(ambang.getJenisRisiko(), r);
                lampauBeruntun = lampauBeruntun && nilai != null && ambang.dilampaui(nilai);
            }
            if (lampauKini || lampauBeruntun) {
                hasil.add(ambang.getJenisRisiko());
            }
        }
        return hasil;
    }

    static Double nilaiUkur(String jenisRisiko, TandaVitalEntity tv) {
        return switch (jenisRisiko) {
            case "hipertensi" -> (double) tv.getSistolik();
            case "obesitas" -> tv.hitungIMT();
            case "indikasi diabetes" -> tv.getGulaDarah() == null ? null : tv.getGulaDarah().doubleValue();
            default -> null;
        };
    }

    private PenandaRisikoEntity terbitkanPenanda(DataKlinisBoundary dataKlinis, int idKunjungan, String jenis) {
        PenandaRisikoEntity p = new PenandaRisikoEntity(0, idKunjungan, jenis, LocalDate.now(),
                PenandaRisikoEntity.STATUS_AKTIF);
        dataKlinis.simpanPenanda(p);
        return p;
    }

    private void masukkanKeDaftarPantau(DataPantauBoundary dataPantau, int idPasien, String jenis) {
        dataPantau.simpanEntri(new DaftarPantauEntity(0, idPasien, jenis, DaftarPantauEntity.STATUS_BELUM));
    }

    /**
     * Penanda aktif satu kunjungan untuk ikon dan warna merah pada SkriningForm.
     */
    public List<PenandaRisikoEntity> ambilPenanda(int idKunjungan) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataKlinisBoundary(koneksi).penandaKunjungan(idKunjungan);
        } catch (SQLException e) {
            throw SkriningManager.galatTeknis("Penanda risiko gagal dimuat", e);
        }
    }

    /**
     * Menggabungkan entri risiko yang belum selesai dengan jadwal kontrol yang belum punya entri (Algo-C21-02).
     */
    public List<DaftarPantauEntity> muatEntriAktif() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return SkriningManager.dalamTransaksi(koneksi, () -> muatEntriAktif(koneksi));
        } catch (SQLException e) {
            throw SkriningManager.galatTeknis("Daftar pantau gagal dimuat", e);
        }
    }

    List<DaftarPantauEntity> muatEntriAktif(Connection koneksi) {
        DataPantauBoundary dataPantau = new DataPantauBoundary(koneksi);
        List<DaftarPantauEntity> entri = dataPantau.ambilEntri();
        // Q-C59-17 hanya mengembalikan jadwal terjadwal yang belum memicu entri, sehingga tidak ada entri ganda.
        for (JadwalKontrolEntity j : new DataKlinisBoundary(koneksi).ambilJadwalKontrol()) {
            DaftarPantauEntity e = new DaftarPantauEntity(0, j.getIdPasien(), JENIS_KUNJUNGAN_ULANG,
                    DaftarPantauEntity.STATUS_BELUM);
            // Disimpan agar punya idEntri untuk tindak lanjut.
            dataPantau.simpanEntri(e);
            e.setTanggalPemicu(j.getTanggalRencana());
            entri.add(e);
        }
        return entri;
    }
}
