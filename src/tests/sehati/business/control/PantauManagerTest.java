package sehati.business.control;

import org.junit.jupiter.api.Test;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.TindakLanjutEntity;
import sehati.dataaccess.DataPantauBoundary;
import sehati.dataaccess.DataPasienBoundary;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PantauManagerTest {

    private final PantauManager manager = new PantauManager();

    /** Pengganti basis data: menyimpan hasil pemanggilan di memori. */
    private static class PantauPalsu extends DataPantauBoundary {
        final List<DaftarPantauEntity> entri = new ArrayList<>();
        final List<TindakLanjutEntity> upaya = new ArrayList<>();

        PantauPalsu(DaftarPantauEntity... awal) {
            super(null);
            entri.addAll(List.of(awal));
        }

        @Override
        public List<DaftarPantauEntity> ambilEntri() {
            return entri.stream().filter(e -> !"selesai ditindaklanjuti".equals(e.getStatusTindakLanjut())).toList();
        }

        @Override
        public void simpanEntri(DaftarPantauEntity e) {
            // Objek yang sama sudah ada di daftar, perubahan statusnya langsung tercermin.
        }

        @Override
        public void simpanTindakLanjut(int idEntri, TindakLanjutEntity t) {
            upaya.add(t);
        }
    }

    private static class PasienPalsu extends DataPasienBoundary {
        final List<String> catatan = new ArrayList<>();
        int disimpan;

        PasienPalsu() {
            super(null);
        }

        @Override
        public RekamMedisEntity ambilRekamMedis(int idPasien) {
            return new RekamMedisEntity() {
                @Override
                public void tambahEntri(String c) {
                    catatan.add(c);
                }
            };
        }

        @Override
        public void simpanRekamMedis(RekamMedisEntity rm) {
            disimpan++;
        }
    }

    private static DaftarPantauEntity entri(int id, String jenis, LocalDate tanggal) {
        DaftarPantauEntity e = new DaftarPantauEntity(id, 100 + id, jenis, DaftarPantauEntity.STATUS_BELUM);
        e.setTanggalPemicu(tanggal);
        return e;
    }

    @Test
    void catat_terhubung_menutupEntriDanMenulisRekamMedis() {
        PantauPalsu pantau = new PantauPalsu(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)));
        PasienPalsu pasien = new PasienPalsu();
        manager.catat(pantau, pasien, 1, new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang"));

        assertEquals("selesai ditindaklanjuti", pantau.entri.get(0).getStatusTindakLanjut());
        assertEquals(List.of("Tindak lanjut telepon: terhubung, akan datang"), pasien.catatan);
        assertEquals(1, pasien.disimpan);
        assertEquals(1, pantau.upaya.size());
    }

    @Test
    void catat_tidakTerhubung_menandaiPerluDihubungiUlangTanpaRekamMedis() {
        PantauPalsu pantau = new PantauPalsu(entri(1, "obesitas", LocalDate.of(2026, 11, 1)));
        PasienPalsu pasien = new PasienPalsu();
        manager.catat(pantau, pasien, 1,
                new TindakLanjutEntity(0, "pesan singkat", "tidak terhubung", "belum diketahui"));

        assertEquals("perlu dihubungi ulang", pantau.entri.get(0).getStatusTindakLanjut());
        assertTrue(pasien.catatan.isEmpty());
        assertEquals(0, pasien.disimpan);
        assertEquals(1, pantau.upaya.size());
    }

    @Test
    void catat_entriYangPerluDihubungiUlangMasihBolehDitindaklanjutiLagi() {
        PantauPalsu pantau = new PantauPalsu(entri(1, "obesitas", LocalDate.of(2026, 11, 1)));
        PasienPalsu pasien = new PasienPalsu();
        manager.catat(pantau, pasien, 1, new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui"));
        manager.catat(pantau, pasien, 1, new TindakLanjutEntity(0, "telepon", "terhubung", "sudah datang"));

        assertEquals("selesai ditindaklanjuti", pantau.entri.get(0).getStatusTindakLanjut());
        assertEquals(2, pantau.upaya.size());
    }

    @Test
    void catat_entriSudahSelesaiDitolak() {
        DaftarPantauEntity selesai = entri(1, "hipertensi", LocalDate.of(2026, 11, 1));
        selesai.setStatusTindakLanjut(DaftarPantauEntity.STATUS_SELESAI);
        PantauPalsu pantau = new PantauPalsu(selesai);
        assertThrows(IllegalStateException.class, () -> manager.catat(pantau, new PasienPalsu(), 1,
                new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang")));
        assertTrue(pantau.upaya.isEmpty());
    }

    @Test
    void catatTindakLanjut_isianTidakDipilihDitolakSebelumMenyentuhBasisData() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.catatTindakLanjut(1, new TindakLanjutEntity(0, "merpati", "terhubung", "akan datang")));
        assertThrows(IllegalArgumentException.class, () -> manager.catatTindakLanjut(1, null));
    }

    @Test
    void saring_menurutJenisRisiko() {
        List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)),
                entri(2, "obesitas", LocalDate.of(2026, 11, 2)));
        assertEquals(List.of(2), ids(PantauManager.saring(semua, "obesitas", null, null, null)));
    }

    @Test
    void saring_rentangTanggalMengikutkanBatas() {
        List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 10, 31)),
                entri(2, "hipertensi", LocalDate.of(2026, 11, 1)),
                entri(3, "hipertensi", LocalDate.of(2026, 11, 30)),
                entri(4, "hipertensi", LocalDate.of(2026, 12, 1)));
        assertEquals(List.of(2, 3),
                ids(PantauManager.saring(semua, null, LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null)));
    }

    @Test
    void saring_tanpaPenyaringMengembalikanSemuaDenganBerisikoLebihDulu() {
        List<DaftarPantauEntity> semua = List.of(entri(1, "kunjungan ulang", LocalDate.of(2026, 11, 1)),
                entri(2, "hipertensi", LocalDate.of(2026, 11, 5)),
                entri(3, "obesitas", LocalDate.of(2026, 11, 2)));
        assertEquals(List.of(3, 2, 1), ids(PantauManager.saring(semua, null, null, null, null)));
    }

    @Test
    void saring_hasilKosongBilaTidakAdaYangCocok() {
        List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)));
        assertTrue(PantauManager.saring(semua, "obesitas", null, null, null).isEmpty());
    }

    @Test
    void saring_menurutStatusTindakLanjut() {
        DaftarPantauEntity ulang = entri(2, "hipertensi", LocalDate.of(2026, 11, 2));
        ulang.setStatusTindakLanjut(DaftarPantauEntity.STATUS_ULANG);
        List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)), ulang);
        assertEquals(List.of(2), ids(PantauManager.saring(semua, null, null, null, DaftarPantauEntity.STATUS_ULANG)));
        assertEquals(List.of(1), ids(PantauManager.saring(semua, "hipertensi", null, null,
                DaftarPantauEntity.STATUS_BELUM)));
    }

    @Test
    void saringDaftar_tanggalMulaiSetelahAkhirDitolak() {
        assertThrows(IllegalArgumentException.class,
                () -> manager.saringDaftar(null, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 1), null));
    }

    private static List<Integer> ids(List<DaftarPantauEntity> daftar) {
        return daftar.stream().map(DaftarPantauEntity::getIdEntri).toList();
    }
}
