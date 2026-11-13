package sehati.business.entity;

/**
 * C40 - AmbangRisikoEntity.
 *
 * Merepresentasikan data master ambang klinis tiap jenis risiko.
 */
public class AmbangRisikoEntity {

    private int idAmbang;
    private String jenisRisiko;
    private double nilaiBatas;

    public AmbangRisikoEntity() {
    }

    public AmbangRisikoEntity(int idAmbang, String jenisRisiko, double nilaiBatas) {
        this.idAmbang = idAmbang;
        this.jenisRisiko = jenisRisiko;
        this.nilaiBatas = nilaiBatas;
    }

    /**
     * Ambang awal adalah batas bawah kategori klinis, sehingga nilai yang sama dengan batas sudah dilampaui
     * (Algo-C40-01).
     */
    public boolean dilampaui(double nilai) {
        return nilai >= nilaiBatas;
    }

    public int getIdAmbang() {
        return idAmbang;
    }

    public void setIdAmbang(int idAmbang) {
        this.idAmbang = idAmbang;
    }

    public String getJenisRisiko() {
        return jenisRisiko;
    }

    public void setJenisRisiko(String jenisRisiko) {
        this.jenisRisiko = jenisRisiko;
    }

    public double getNilaiBatas() {
        return nilaiBatas;
    }

    public void setNilaiBatas(double nilaiBatas) {
        this.nilaiBatas = nilaiBatas;
    }
}
