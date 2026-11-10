package sehati.business.entity;

/**
 * C52 - TindakLanjutEntity.
 *
 * Merepresentasikan satu upaya menghubungi pasien: jenis kontak, hasil, dan status kedatangan.
 */
public class TindakLanjutEntity {

    public static final String HASIL_TERHUBUNG = "terhubung";
    public static final String HASIL_TIDAK_TERHUBUNG = "tidak terhubung";

    private int idTindakLanjut;
    private String jenisKontak;
    private String hasilUpaya;
    private String statusKedatangan;

    public TindakLanjutEntity() {
    }

    public TindakLanjutEntity(int idTindakLanjut, String jenisKontak, String hasilUpaya, String statusKedatangan) {
        this.idTindakLanjut = idTindakLanjut;
        this.jenisKontak = jenisKontak;
        this.hasilUpaya = hasilUpaya;
        this.statusKedatangan = statusKedatangan;
    }

    /**
     * True bila pasien berhasil dihubungi dan memberi keterangan (Subbab 5.52).
     */
    public boolean terhubung() {
        return HASIL_TERHUBUNG.equals(hasilUpaya);
    }

    public int getIdTindakLanjut() {
        return idTindakLanjut;
    }

    public void setIdTindakLanjut(int idTindakLanjut) {
        this.idTindakLanjut = idTindakLanjut;
    }

    public String getJenisKontak() {
        return jenisKontak;
    }

    public void setJenisKontak(String jenisKontak) {
        this.jenisKontak = jenisKontak;
    }

    public String getHasilUpaya() {
        return hasilUpaya;
    }

    public void setHasilUpaya(String hasilUpaya) {
        this.hasilUpaya = hasilUpaya;
    }

    public String getStatusKedatangan() {
        return statusKedatangan;
    }

    public void setStatusKedatangan(String statusKedatangan) {
        this.statusKedatangan = statusKedatangan;
    }
}
