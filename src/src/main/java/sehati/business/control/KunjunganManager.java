package sehati.business.control;

import sehati.business.entity.AntreanEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PoliEntity;
import sehati.dataaccess.DataKunjunganBoundary;

import java.time.LocalDate;
import java.util.List;

/**
 * C19 - KunjunganManager.
 *
 * Membuka kunjungan, menolak kunjungan ganda, dan menerbitkan nomor antrean berurutan per poli per hari.
 */
public class KunjunganManager {

    private static final String JENIS_KUNJUNGAN = "KUNJUNGAN";

    private final DataKunjunganBoundary dataKunjungan;

    public KunjunganManager(DataKunjunganBoundary dataKunjungan) {
        this.dataKunjungan = dataKunjungan;
    }

    public List<PoliEntity> daftarPoli() {
        return dataKunjungan.ambilSemuaPoli();
    }

    public KunjunganEntity bukaKunjungan(int idPasien, int idPoli) {
        KunjunganEntity kunjungan = cegahKunjunganGanda(idPasien);
        if (kunjungan != null) {
            return kunjungan;
        }
        int nomorAntrean = terbitkanNomorAntrean(idPoli);
        KunjunganEntity kunjunganBaru = new KunjunganEntity(0, idPasien, idPoli, LocalDate.now(), nomorAntrean, "AKTIF");
        dataKunjungan.simpanKunjungan(kunjunganBaru);
        AntreanEntity antrean = dataKunjungan.ambilAntrean(JENIS_KUNJUNGAN);
        antrean.tambahkan(kunjunganBaru.getIdKunjungan());
        dataKunjungan.simpanAntrean(antrean);
        return kunjunganBaru;
    }

    private KunjunganEntity cegahKunjunganGanda(int idPasien) {
        return dataKunjungan.kunjunganAktif(idPasien, LocalDate.now());
    }

    private int terbitkanNomorAntrean(int idPoli) {
        PoliEntity poli = dataKunjungan.kunciPoli(idPoli);
        return poli.ambilNomorBerikutnya(LocalDate.now());
    }
}
