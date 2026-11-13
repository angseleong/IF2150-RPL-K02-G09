package sehati.business.control;

import sehati.business.entity.AuditEntity;
import sehati.business.entity.PasienEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataAkunBoundary;
import sehati.dataaccess.DataPasienBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C18 - PasienManager.
 *
 * Menjalankan pencarian parsial, memvalidasi NIK 16 digit dan keunikannya, menerbitkan nomor rekam medis, dan
 * memutakhirkan data pasien.
 */
public class PasienManager {

    private static final Logger LOG = Logger.getLogger(PasienManager.class.getName());

    // Daftar awal I-02 sebelum ada kata kunci
    public List<PasienEntity> pasienTerbaru() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataPasienBoundary(koneksi).pasienTerbaru();
        } catch (SQLException e) {
            throw galatTeknis("Daftar pasien gagal dimuat", e);
        }
    }

    public List<PasienEntity> cariPasien(String kataKunci) {
        String teks = kataKunci == null ? "" : kataKunci.strip();
        if (teks.isEmpty()) {
            return List.of();
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataPasienBoundary(koneksi).cariParsial(teks);
        } catch (SQLException e) {
            throw galatTeknis("Pencarian pasien gagal", e);
        }
    }

    public PasienEntity daftarkanPasien(PasienEntity data) {
        if (data == null) {
            throw new IllegalArgumentException("Data pasien kosong");
        }
        if (!validasiNIK(data.getNik())) {
            throw new IllegalArgumentException("NIK tidak valid");
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataPasienBoundary dataPasien = new DataPasienBoundary(koneksi);
            DataAkunBoundary dataAkun = new DataAkunBoundary(koneksi);
            if (dataPasien.nikTerdaftar(data.getNik())) {
                throw new IllegalArgumentException("NIK sudah terdaftar");
            }
            int idPengguna = SesiAktif.idPenggunaSesi();
            koneksi.setAutoCommit(false);
            try {
                String nomorRM = terbitkanNomorRM(dataPasien);
                PasienEntity pasien = new PasienEntity(0, data.getNik(), nomorRM, data.getNama(),
                        data.getTanggalLahir(), data.getJenisKelamin(), data.getAlamat(), data.getNomorTelepon());
                dataPasien.simpanPasien(pasien);
                dataAkun.simpanAudit(AuditEntity.buat(idPengguna, "Pendaftaran pasien " + nomorRM));
                koneksi.commit();
                return pasien;
            } catch (RuntimeException | SQLException e) {
                batalkanDiamDiam(koneksi);
                throw e;
            }
        } catch (SQLException e) {
            throw galatTeknis("Pendaftaran pasien gagal", e);
        }
    }

    public void perbaruiPasien(PasienEntity data) {
        if (data == null) {
            throw new IllegalArgumentException("Data pasien kosong");
        }
        if (!validasiNIK(data.getNik())) {
            throw new IllegalArgumentException("NIK tidak valid");
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataPasienBoundary dataPasien = new DataPasienBoundary(koneksi);
            DataAkunBoundary dataAkun = new DataAkunBoundary(koneksi);
            PasienEntity pasien = dataPasien.ambilPasien(data.getIdPasien());
            if (pasien == null) {
                throw new IllegalArgumentException("Pasien tidak ditemukan");
            }
            if (!data.getNik().equals(pasien.getNik()) && dataPasien.nikTerdaftar(data.getNik())) {
                throw new IllegalArgumentException("NIK sudah terdaftar");
            }
            int idPengguna = SesiAktif.idPenggunaSesi();
            koneksi.setAutoCommit(false);
            try {
                pasien.perbarui(data);
                dataPasien.simpanPasien(pasien);
                dataAkun.simpanAudit(AuditEntity.buat(idPengguna,
                        "Pembaruan data pasien " + pasien.getNomorRekamMedis()));
                koneksi.commit();
            } catch (RuntimeException | SQLException e) {
                batalkanDiamDiam(koneksi);
                throw e;
            }
        } catch (SQLException e) {
            throw galatTeknis("Pembaruan pasien gagal", e);
        }
    }

    public boolean validasiNIK(String nik) {
        if (nik == null || nik.length() != 16) {
            return false;
        }
        for (int i = 0; i < nik.length(); i++) {
            if (!Character.isDigit(nik.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void batalkanDiamDiam(Connection koneksi) {
        try {
            koneksi.rollback();
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "Rollback gagal", e);
        }
    }

    private String terbitkanNomorRM(DataPasienBoundary dataPasien) {
        return dataPasien.nomorRMBerikutnya();
    }

    private IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException("Basis data tidak dapat dihubungi", e);
    }
}
