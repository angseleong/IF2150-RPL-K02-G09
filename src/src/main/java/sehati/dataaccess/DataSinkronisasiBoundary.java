package sehati.dataaccess;

import sehati.business.entity.BundelFHIREntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            LOG.log(Level.SEVERE, "Gagal menyimpan bundel FHIR", e);
            throw new IllegalStateException("Bundel FHIR kunjungan " + b.getIdKunjungan() + " gagal disimpan", e);
        }
    }

    public List<BundelFHIREntity> ambilTertunda() {
        throw new UnsupportedOperationException("TODO");
    }

    public void perbaruiBundel(BundelFHIREntity b) {
        throw new UnsupportedOperationException("TODO");
    }

    public String tulisArsipTerenkripsi(List<BundelFHIREntity> daftar, String lokasi) {
        throw new UnsupportedOperationException("TODO");
    }
}
