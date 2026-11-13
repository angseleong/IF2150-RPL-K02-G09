package sehati.business.control;

import sehati.common.SesiAktif;
import sehati.dataaccess.DataAkunBoundary;
import sehati.business.entity.AuditEntity;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.TindakLanjutEntity;
import sehati.common.KoneksiBasisData;
import sehati.dataaccess.DataPantauBoundary;
import sehati.dataaccess.DataPasienBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * C26 - Menyaring daftar pantau menurut jenis risiko dan tanggal, lalu mencatat hasil tindak lanjut (UC09).
 */
public class PantauManager {

    private static final Set<String> JENIS_KONTAK = Set.of("telepon", "pesan singkat", "kunjungan rumah");
    private static final Set<String> HASIL_UPAYA = Set.of(TindakLanjutEntity.HASIL_TERHUBUNG,
            TindakLanjutEntity.HASIL_TIDAK_TERHUBUNG);
    private static final Set<String> STATUS_KEDATANGAN = Set.of("akan datang", "sudah datang", "tidak dapat datang",
            "belum diketahui");

    /**
     * Entri aktif yang lolos penyaring, dilengkapi identitas dan kontak pasien. Parameter null berarti penyaring itu
     * tidak dipakai; tanggal yang disaring adalah tanggal pemicu entri (KF18). Entri berisiko tampil lebih dulu.
     */
    public List<DaftarPantauEntity> saringDaftar(String jenisRisiko, LocalDate mulai, LocalDate akhir,
            String statusTindakLanjut) {
        if (mulai != null && akhir != null && mulai.isAfter(akhir)) {
            throw new IllegalArgumentException("Tanggal mulai tidak boleh setelah tanggal akhir");
        }
        List<DaftarPantauEntity> semua = new RisikoManager().muatEntriAktif();
        List<DaftarPantauEntity> lolos = saring(semua, jenisRisiko, mulai, akhir, statusTindakLanjut);
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataPasienBoundary dataPasien = new DataPasienBoundary(koneksi);
            for (DaftarPantauEntity e : lolos) {
                PasienEntity pasien = dataPasien.ambilPasien(e.getIdPasien());
                e.setNamaPasien(pasien.getNama());
                e.setNomorTelepon(pasien.getNomorTelepon());
            }
        } catch (SQLException ex) {
            throw SkriningManager.galatTeknis("Identitas pasien gagal dimuat", ex);
        }
        return lolos;
    }

    /**
     * Penyaringan murni tanpa akses data: jenis, rentang tanggal pemicu (batas ikut), lalu urutan.
     */
    static List<DaftarPantauEntity> saring(
            List<DaftarPantauEntity> entri, String jenisRisiko, LocalDate mulai, LocalDate akhir,
            String statusTindakLanjut) {
        List<DaftarPantauEntity> hasil = new ArrayList<>();
        for (DaftarPantauEntity e : entri) {
            LocalDate tanggal = e.getTanggalPemicu();
            boolean isianCocok = (jenisRisiko == null || jenisRisiko.equals(e.getJenisRisiko()))
                    && (statusTindakLanjut == null || statusTindakLanjut.equals(e.getStatusTindakLanjut()));
            boolean sesudahMulai = mulai == null || (tanggal != null && !tanggal.isBefore(mulai));
            boolean sebelumAkhir = akhir == null || (tanggal != null && !tanggal.isAfter(akhir));
            if (isianCocok && sesudahMulai && sebelumAkhir) {
                hasil.add(e);
            }
        }
        hasil.sort(Comparator
                .comparing((DaftarPantauEntity e) -> RisikoManager.JENIS_KUNJUNGAN_ULANG.equals(e.getJenisRisiko()))
                .thenComparing(DaftarPantauEntity::getTanggalPemicu, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(DaftarPantauEntity::getIdEntri));
        return hasil;
    }

    /**
     * Menyimpan upaya menghubungi pasien dalam satu transaksi (Algo-C26-01, skenario alternatif 1 UC09).
     */
    public void catatTindakLanjut(int idEntri, TindakLanjutEntity t) {
        periksaIsian(t);
        SesiAktif.wajibPeran("Perawat", "Dokter");
        try (Connection koneksi = KoneksiBasisData.buka()) {
            SkriningManager.dalamTransaksi(koneksi, () -> {
                catat(new DataPantauBoundary(koneksi), new DataPasienBoundary(koneksi), idEntri, t);
                new DataAkunBoundary(koneksi).simpanAudit(AuditEntity.buat(SesiAktif.idPenggunaAudit(),
                        "Tindak lanjut daftar pantau " + idEntri + ": " + t.getHasilUpaya()));
                return null;
            });
        } catch (SQLException e) {
            throw SkriningManager.galatTeknis("Tindak lanjut gagal disimpan", e);
        }
    }

    void catat(DataPantauBoundary dataPantau, DataPasienBoundary dataPasien, int idEntri, TindakLanjutEntity t) {
        DaftarPantauEntity entri = cariEntri(dataPantau, idEntri);
        dataPantau.simpanTindakLanjut(idEntri, t);
        if (t.terhubung()) {
            RekamMedisEntity rm = dataPasien.ambilRekamMedis(entri.getIdPasien());
            if (rm == null) {
                throw new IllegalStateException("Rekam medis pasien tidak ditemukan: " + entri.getIdPasien());
            }
            rm.tambahEntri("Tindak lanjut " + t.getJenisKontak() + ": " + t.getHasilUpaya() + ", "
                    + t.getStatusKedatangan());
            dataPasien.simpanRekamMedis(rm);
            perbaruiStatus(dataPantau, entri, DaftarPantauEntity.STATUS_SELESAI);
        } else {
            perbaruiStatus(dataPantau, entri, DaftarPantauEntity.STATUS_ULANG);
        }
    }

    private void perbaruiStatus(DataPantauBoundary dataPantau, DaftarPantauEntity entri, String status) {
        entri.perbaruiStatus(status);
        dataPantau.simpanEntri(entri);
    }

    private static DaftarPantauEntity cariEntri(DataPantauBoundary dataPantau, int idEntri) {
        for (DaftarPantauEntity e : dataPantau.ambilEntri()) {
            if (e.getIdEntri() == idEntri) {
                return e;
            }
        }
        // ambilEntri() tidak mengembalikan entri yang sudah selesai.
        throw new IllegalStateException("Entri tidak ditemukan atau sudah selesai ditindaklanjuti");
    }

    // Isian dibatasi daftar pilihan agar laporan tindak lanjut seragam (Subbab 5.52).
    private static void periksaIsian(TindakLanjutEntity t) {
        if (t == null || !JENIS_KONTAK.contains(t.getJenisKontak()) || !HASIL_UPAYA.contains(t.getHasilUpaya())
                || !STATUS_KEDATANGAN.contains(t.getStatusKedatangan())) {
            throw new IllegalArgumentException("Jenis kontak, hasil upaya, dan status kedatangan wajib dipilih");
        }
    }
}
