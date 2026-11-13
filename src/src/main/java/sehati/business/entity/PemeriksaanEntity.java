package sehati.business.entity;

/**
 * C42 - PemeriksaanEntity.
 *
 * Merepresentasikan anamnesis, tindakan, dan status kunci hasil pemeriksaan dokter.
 */
public class PemeriksaanEntity {

    private int idPemeriksaan;
    private int idKunjungan;
    private String anamnesis;
    private String tindakan;
    private boolean terkunci;

    public PemeriksaanEntity() {
    }

    public PemeriksaanEntity(int idPemeriksaan, int idKunjungan, String anamnesis, String tindakan, boolean terkunci) {
        this.idPemeriksaan = idPemeriksaan;
        this.idKunjungan = idKunjungan;
        this.anamnesis = anamnesis;
        this.tindakan = tindakan;
        this.terkunci = terkunci;
    }

    /**
     * Mengunci hasil pemeriksaan sehingga tidak dapat ditimpa lagi (Subbab 5.42, UC05).
     */
    public void kunci() {
        if (terkunci) {
            throw new IllegalStateException("Rekam medis kunjungan ini sudah dikunci");
        }
        terkunci = true;
    }

    public int getIdPemeriksaan() {
        return idPemeriksaan;
    }

    public void setIdPemeriksaan(int idPemeriksaan) {
        this.idPemeriksaan = idPemeriksaan;
    }

    public int getIdKunjungan() {
        return idKunjungan;
    }

    public void setIdKunjungan(int idKunjungan) {
        this.idKunjungan = idKunjungan;
    }

    public String getAnamnesis() {
        return anamnesis;
    }

    public void setAnamnesis(String anamnesis) {
        this.anamnesis = anamnesis;
    }

    public String getTindakan() {
        return tindakan;
    }

    public void setTindakan(String tindakan) {
        this.tindakan = tindakan;
    }

    public boolean isTerkunci() {
        return terkunci;
    }

    public void setTerkunci(boolean terkunci) {
        this.terkunci = terkunci;
    }
}
