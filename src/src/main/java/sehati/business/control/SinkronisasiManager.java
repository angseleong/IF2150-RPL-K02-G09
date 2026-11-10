package sehati.business.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import sehati.common.KoneksiBasisData;
import sehati.business.entity.BundelFHIREntity;
import sehati.dataaccess.DataSinkronisasiBoundary;
import sehati.dataaccess.ResponsKirim;
import sehati.dataaccess.SATUSEHATGateway;

/**
 * C28 - SinkronisasiManager.
 *
 * Mengelola antrean bundel, memeriksa koneksi lewat SATUSEHATGateway, lalu mengirim bundel saat daring atau
 * mengekspornya sebagai berkas saat luring.
 */
public class SinkronisasiManager {

    private static final Logger LOG = Logger.getLogger(SinkronisasiManager.class.getName());

    private final Supplier<SATUSEHATGateway> gatewaySupplier;
    private int jumlahTertunda;
    private boolean daring;

    public SinkronisasiManager() {
        this(SATUSEHATGateway::new);
    }

    SinkronisasiManager(Supplier<SATUSEHATGateway> gatewaySupplier) {
        this.gatewaySupplier = gatewaySupplier;
    }

    /**
     * Memuat bundel yang belum terkirim, sekaligus memperbarui jumlah tertunda dan status koneksi untuk form.
     */
    public List<BundelFHIREntity> muatStatus() {
        SATUSEHATGateway gateway = gatewaySupplier.get();
        boolean online = gateway.periksaKoneksi();
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return muatStatus(new DataSinkronisasiBoundary(koneksi), online);
        } catch (SQLException e) {
            throw galatTeknis("Gagal memuat status bundel", e);
        }
    }

    List<BundelFHIREntity> muatStatus(DataSinkronisasiBoundary data, boolean online) {
        daring = online;
        List<BundelFHIREntity> daftar = data.ambilTertunda();
        jumlahTertunda = daftar.size();
        return daftar;
    }

    /**
     * Algo-C28-01: kirim semua bundel tertunda. Saat luring, bundel ditahan dan pengguna diarahkan ke ekspor.
     */
    public List<BundelFHIREntity> kirimSemua() {
        SATUSEHATGateway gateway = gatewaySupplier.get();
        if (!gateway.periksaKoneksi()) {
            daring = false;
            throw new IllegalStateException("Koneksi internet terputus, bundel ditahan");
        }
        daring = true;
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return kirimSemua(new DataSinkronisasiBoundary(koneksi), gateway);
        } catch (SQLException e) {
            throw galatTeknis("Gagal mengirim bundel", e);
        }
    }

    List<BundelFHIREntity> kirimSemua(DataSinkronisasiBoundary data, SATUSEHATGateway gateway) {
        List<BundelFHIREntity> daftar = data.ambilTertunda();
        jumlahTertunda = 0;
        for (BundelFHIREntity bundel : daftar) {
            ResponsKirim respons = gateway.kirimBundel(bundel);
            if (respons.berhasil()) {
                bundel.tandaiTerkirim(respons.kode());
            } else {
                bundel.tandaiGagal(respons.alasan());
                jumlahTertunda++;
            }
            data.perbaruiBundel(bundel);
        }
        return daftar;
    }

    /**
     * Algo-C28-02: ekspor semua bundel tertunda ke satu arsip terenkripsi.
     */
    public String eksporBundel(String lokasi) {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return eksporBundel(lokasi, new DataSinkronisasiBoundary(koneksi));
        } catch (SQLException e) {
            throw galatTeknis("Gagal mengekspor bundel", e);
        }
    }

    String eksporBundel(String lokasi, DataSinkronisasiBoundary data) {
        List<BundelFHIREntity> daftar = data.ambilTertunda();
        if (daftar.isEmpty()) {
            throw new IllegalStateException("Tidak ada bundel tertunda untuk diekspor");
        }
        return data.tulisArsipTerenkripsi(daftar, lokasi);
    }

    // Pemeriksaan ringan tanpa basis data untuk indikator bilah aplikasi (KF22)
    public boolean periksaKoneksi() {
        daring = gatewaySupplier.get().periksaKoneksi();
        return daring;
    }

    public boolean isDaring() {
        return daring;
    }

    public int getJumlahTertunda() {
        return jumlahTertunda;
    }

    private static IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ". Hubungi administrator bila berulang.", e);
    }
}
