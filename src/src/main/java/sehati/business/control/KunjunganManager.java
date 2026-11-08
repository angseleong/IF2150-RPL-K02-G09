package sehati.business.control;

import sehati.business.entity.AntreanEntity;
import sehati.business.entity.AuditEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PoliEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataAkunBoundary;
import sehati.dataaccess.DataKunjunganBoundary;
import sehati.dataaccess.DataPasienBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C19 - KunjunganManager.
 *
 * Membuka kunjungan, menolak kunjungan ganda, dan menerbitkan nomor antrean berurutan per poli per hari.
 */
public class KunjunganManager {

    private static final Logger LOG = Logger.getLogger(KunjunganManager.class.getName());

    public List<PoliEntity> daftarPoli() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataKunjunganBoundary(koneksi).ambilSemuaPoli();
        } catch (SQLException e) {
            throw galatTeknis("Pengambilan daftar poli gagal", e);
        }
    }

    public KunjunganEntity bukaKunjungan(int idPasien, int idPoli) {
        if (idPasien <= 0) {
            throw new IllegalArgumentException("Pasien tidak valid");
        }
        if (idPoli <= 0) {
            throw new IllegalArgumentException("Poli tidak valid");
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataKunjunganBoundary dataKunjungan = new DataKunjunganBoundary(koneksi);
            DataPasienBoundary dataPasien = new DataPasienBoundary(koneksi);
            DataAkunBoundary dataAkun = new DataAkunBoundary(koneksi);
            KunjunganEntity kunjunganAktif = cegahKunjunganGanda(dataKunjungan, idPasien);
            if (kunjunganAktif != null) {
                // Skenario alternatif 1 UC03: kunjungan yang sama dikembalikan, tanpa nomor antrean baru.
                return kunjunganAktif;
            }
            int idPengguna = SesiAktif.idPenggunaSesi();
            koneksi.setAutoCommit(false);
            try {
                int nomor = terbitkanNomorAntrean(dataKunjungan, idPoli);
                KunjunganEntity kunjungan = new KunjunganEntity(0, idPasien, idPoli, LocalDate.now(), nomor,
                        "menunggu skrining");
                // Memastikan pasien punya rekam medis sebelum kunjungan dibuka.
                if (dataPasien.ambilRekamMedis(idPasien) == null) {
                    throw new IllegalArgumentException("Pasien belum memiliki rekam medis");
                }
                dataKunjungan.simpanKunjungan(kunjungan);
                AntreanEntity antrean = dataKunjungan.ambilAntrean("skrining");
                antrean.tambahkan(kunjungan.getIdKunjungan());
                dataKunjungan.simpanAntrean(antrean);
                dataAkun.simpanAudit(AuditEntity.buat(idPengguna, "Kunjungan baru, antrean " + nomor));
                koneksi.commit();
                return kunjungan;
            } catch (RuntimeException | SQLException e) {
                batalkanDiamDiam(koneksi);
                throw e;
            }
        } catch (SQLException e) {
            throw galatTeknis("Pembukaan kunjungan gagal", e);
        }
    }

    private KunjunganEntity cegahKunjunganGanda(DataKunjunganBoundary dataKunjungan, int idPasien) {
        return dataKunjungan.kunjunganAktif(idPasien, LocalDate.now());
    }

    private int terbitkanNomorAntrean(DataKunjunganBoundary dataKunjungan, int idPoli) {
        PoliEntity poli = dataKunjungan.kunciPoli(idPoli);
        if (poli == null) {
            throw new IllegalArgumentException("Poli tidak ditemukan");
        }
        int nomor = poli.ambilNomorBerikutnya(LocalDate.now());
        dataKunjungan.simpanPoli(poli);
        return nomor;
    }

    private void batalkanDiamDiam(Connection koneksi) {
        try {
            koneksi.rollback();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Rollback gagal", e);
        }
    }

    private IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException("Basis data tidak dapat dihubungi", e);
    }
}
