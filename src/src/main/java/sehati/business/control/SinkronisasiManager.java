package sehati.business.control;

import sehati.business.entity.BundelFHIREntity;

import java.util.List;

/**
 * C28 - SinkronisasiManager.
 *
 * Mengelola antrean bundel, memeriksa koneksi lewat SATUSEHATGateway, lalu mengirim bundel saat daring atau
 * mengekspornya sebagai berkas saat luring.
 */
public class SinkronisasiManager {

    private int jumlahTertunda;

    public List<BundelFHIREntity> muatStatus() {
        throw new UnsupportedOperationException("TODO");
    }

    public List<BundelFHIREntity> kirimSemua() {
        throw new UnsupportedOperationException("TODO");
    }

    public String eksporBundel(String lokasi) {
        throw new UnsupportedOperationException("TODO");
    }
}
