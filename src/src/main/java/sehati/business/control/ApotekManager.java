package sehati.business.control;

import sehati.business.entity.BundelFHIREntity;
import sehati.business.entity.ResepEntity;

import java.util.List;

/**
 * C24 - ApotekManager.
 *
 * Melayani resep, lalu dalam satu transaksi memotong stok, menandai resep selesai, menutup kunjungan, dan menyusun
 * bundel FHIR.
 */
public class ApotekManager {

    public List<ResepEntity> muatAntreanResep() {
        throw new UnsupportedOperationException("TODO");
    }

    public ResepEntity layaniResep(int idResep) {
        throw new UnsupportedOperationException("TODO");
    }

    public void serahkanObat(int idResep) {
        throw new UnsupportedOperationException("TODO");
    }

    private void potongStok(ResepEntity resep) {
        throw new UnsupportedOperationException("TODO");
    }

    private void tutupKunjungan(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    private BundelFHIREntity susunBundel(int idKunjungan) {
        throw new UnsupportedOperationException("TODO");
    }

    public void kembalikanKeDokter(int idResep, String alasan) {
        throw new UnsupportedOperationException("TODO");
    }
}
