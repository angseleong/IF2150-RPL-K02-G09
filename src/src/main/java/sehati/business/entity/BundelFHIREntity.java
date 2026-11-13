package sehati.business.entity;

import java.util.logging.Logger;

/**
 * C54 - BundelFHIREntity.
 *
 * Merepresentasikan bundel HL7 FHIR R4 satu kunjungan beserta status pengirimannya.
 */
public class BundelFHIREntity {

    private static final Logger LOG = Logger.getLogger(BundelFHIREntity.class.getName());

    private int idBundel;
    private int idKunjungan;
    private String isiBundel;
    private String status;
    private String alasanGagal;

    public BundelFHIREntity() {
    }

    public BundelFHIREntity(int idBundel, int idKunjungan, String isiBundel, String status, String alasanGagal) {
        this.idBundel = idBundel;
        this.idKunjungan = idKunjungan;
        this.isiBundel = isiBundel;
        this.status = status;
        this.alasanGagal = alasanGagal;
    }

    /**
     * Algo-C54-01: menandai bundel diterima SATUSEHAT. Bundel yang sudah terkirim tidak boleh ditandai ulang,
     * karena ambilTertunda() tidak pernah mengambilnya lagi (Q-C63-03).
     */
    public void tandaiTerkirim(String kode) {
        if ("terkirim".equals(status)) {
            throw new IllegalStateException("Bundel sudah terkirim");
        }
        status = "terkirim";
        alasanGagal = null;
        LOG.info(() -> "Bundel " + idBundel + " diterima SATUSEHAT, kode " + kode);
    }

    /**
     * Algo-C54-01: menandai bundel gagal dikirim dan menyimpan alasan penolakan SATUSEHAT. Bundel yang sudah
     * terkirim tidak dapat ditandai gagal.
     */
    public void tandaiGagal(String alasan) {
        if ("terkirim".equals(status)) {
            throw new IllegalStateException("Bundel yang sudah terkirim tidak dapat ditandai gagal");
        }
        status = "gagal kirim";
        alasanGagal = alasan == null || alasan.isBlank() ? "Ditolak tanpa alasan" : alasan;
    }

    public int getIdBundel() {
        return idBundel;
    }

    public void setIdBundel(int idBundel) {
        this.idBundel = idBundel;
    }

    public int getIdKunjungan() {
        return idKunjungan;
    }

    public void setIdKunjungan(int idKunjungan) {
        this.idKunjungan = idKunjungan;
    }

    public String getIsiBundel() {
        return isiBundel;
    }

    public void setIsiBundel(String isiBundel) {
        this.isiBundel = isiBundel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlasanGagal() {
        return alasanGagal;
    }

    public void setAlasanGagal(String alasanGagal) {
        this.alasanGagal = alasanGagal;
    }
}
