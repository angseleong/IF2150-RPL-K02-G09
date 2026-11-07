package sehati.dataaccess;

import sehati.business.entity.BundelFHIREntity;

/**
 * C15 - SATUSEHATGateway.
 *
 * Memeriksa koneksi internet dan mengirim bundel ke REST API sandbox SATUSEHAT, lalu mengembalikan status
 * pengiriman ke SinkronisasiManager.
 */
public class SATUSEHATGateway {

    private String alamatLayanan;
    private String token;

    public SATUSEHATGateway(String alamatLayanan, String token) {
        this.alamatLayanan = alamatLayanan;
        this.token = token;
    }

    public boolean periksaKoneksi() {
        throw new UnsupportedOperationException("TODO");
    }

    public ResponsKirim kirimBundel(BundelFHIREntity bundel) {
        throw new UnsupportedOperationException("TODO");
    }
}
