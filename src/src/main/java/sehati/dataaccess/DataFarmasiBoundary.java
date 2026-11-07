package sehati.dataaccess;

import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.business.entity.ResepEntity;

import java.sql.Connection;
import java.util.List;

/**
 * C61 - DataFarmasiBoundary.
 *
 * Membaca dan menyimpan ResepEntity, ItemResepEntity, ObatEntity, BetsObatEntity, dan PenerimaanObatEntity, serta
 * mengunci baris stok saat pemotongan agar tidak terjadi lost update.
 */
public class DataFarmasiBoundary {

    private Connection koneksi;

    public DataFarmasiBoundary(Connection koneksi) {
        this.koneksi = koneksi;
    }

    public List<ObatEntity> ambilSemuaObat() {
        throw new UnsupportedOperationException("TODO");
    }

    public ObatEntity ambilObat(int idObat) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<BetsObatEntity> ambilBets(int idObat) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanStok(ObatEntity o, List<BetsObatEntity> bets) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanPenerimaan(PenerimaanObatEntity p) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<ObatEntity> obatPerluPeringatan(int hari) {
        throw new UnsupportedOperationException("TODO");
    }

    public void simpanResep(ResepEntity r) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<ResepEntity> ambilAntreanResep() {
        throw new UnsupportedOperationException("TODO");
    }

    public ResepEntity ambilResep(int idResep) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResepEntity kunciResep(int idResep) {
        throw new UnsupportedOperationException("TODO");
    }
}
