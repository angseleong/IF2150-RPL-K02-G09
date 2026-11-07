package sehati.business.entity;

/**
 * C39 - TandaVitalEntity.
 *
 * Merepresentasikan nilai tekanan darah, berat, tinggi, suhu, nadi, dan gula darah, termasuk perhitungan IMT.
 */
public class TandaVitalEntity {

    private int sistolik;
    private int diastolik;
    private double beratBadan;
    private double tinggiBadan;
    private double suhu;
    private int nadi;
    private Integer gulaDarah;

    public TandaVitalEntity() {
    }

    public TandaVitalEntity(
            int sistolik, int diastolik, double beratBadan, double tinggiBadan, double suhu, int nadi,
            Integer gulaDarah) {
        this.sistolik = sistolik;
        this.diastolik = diastolik;
        this.beratBadan = beratBadan;
        this.tinggiBadan = tinggiBadan;
        this.suhu = suhu;
        this.nadi = nadi;
        this.gulaDarah = gulaDarah;
    }

    public double hitungIMT() {
        throw new UnsupportedOperationException("TODO");
    }

    public int getSistolik() {
        return sistolik;
    }

    public void setSistolik(int sistolik) {
        this.sistolik = sistolik;
    }

    public int getDiastolik() {
        return diastolik;
    }

    public void setDiastolik(int diastolik) {
        this.diastolik = diastolik;
    }

    public double getBeratBadan() {
        return beratBadan;
    }

    public void setBeratBadan(double beratBadan) {
        this.beratBadan = beratBadan;
    }

    public double getTinggiBadan() {
        return tinggiBadan;
    }

    public void setTinggiBadan(double tinggiBadan) {
        this.tinggiBadan = tinggiBadan;
    }

    public double getSuhu() {
        return suhu;
    }

    public void setSuhu(double suhu) {
        this.suhu = suhu;
    }

    public int getNadi() {
        return nadi;
    }

    public void setNadi(int nadi) {
        this.nadi = nadi;
    }

    public Integer getGulaDarah() {
        return gulaDarah;
    }

    public void setGulaDarah(Integer gulaDarah) {
        this.gulaDarah = gulaDarah;
    }
}
