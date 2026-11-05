package sehati.business.control;

import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.TindakLanjutEntity;

import java.time.LocalDate;
import java.util.List;

/**
 * C26 - PantauManager.
 *
 * Menyaring daftar pantau menurut jenis risiko, tanggal, dan status, lalu mencatat hasil tindak lanjut.
 */
public class PantauManager {

    public List<DaftarPantauEntity> saringDaftar(String jenisRisiko, LocalDate mulai, LocalDate akhir) {
        throw new UnsupportedOperationException("TODO");
    }

    public void catatTindakLanjut(int idEntri, TindakLanjutEntity t) {
        throw new UnsupportedOperationException("TODO");
    }

    private void perbaruiStatus(int idEntri, String status) {
        throw new UnsupportedOperationException("TODO");
    }
}
