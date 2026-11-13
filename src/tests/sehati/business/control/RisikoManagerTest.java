package sehati.business.control;

import org.junit.jupiter.api.Test;
import sehati.business.entity.AmbangRisikoEntity;
import sehati.business.entity.DaftarPantauEntity;
import sehati.business.entity.TandaVitalEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RisikoManagerTest {

    private static final List<AmbangRisikoEntity> AMBANG = List.of(
            new AmbangRisikoEntity(1, "hipertensi", 140),
            new AmbangRisikoEntity(2, "obesitas", 30),
            new AmbangRisikoEntity(3, "indikasi diabetes", 200));

    private static TandaVitalEntity tv(int sistolik, double berat, Integer gula) {
        return new TandaVitalEntity(sistolik, 80, berat, 170, 36.5, 72, gula);
    }

    @Test
    void tanpaRisiko_tidakAdaPenanda() {
        assertTrue(RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 65, 100)).isEmpty());
    }

    @Test
    void sistolikTepatDiBatas_dianggapHipertensi() {
        assertEquals(List.of("hipertensi"), RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(140, 65, null)));
    }

    @Test
    void imtMelampauiBatas_obesitas() {
        // 90 kg pada 170 cm: IMT sekitar 31,1.
        assertEquals(List.of("obesitas"), RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 90, null)));
    }

    @Test
    void gulaDarahKosong_tidakPernahMelampaui() {
        assertTrue(RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 65, null)).isEmpty());
        assertEquals(List.of("indikasi diabetes"),
                RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 65, 250)));
    }

    @Test
    void tigaKunjunganBeruntunDiAtasAmbang_menandaiMeskipunHariIniTurun() {
        List<TandaVitalEntity> riwayat = List.of(tv(150, 65, null), tv(145, 65, null), tv(160, 65, null));
        assertEquals(List.of("hipertensi"), RisikoManager.jenisRisikoTerpenuhi(AMBANG, riwayat, tv(120, 65, null)));
    }

    @Test
    void duaKunjunganDiAtasAmbangSaja_tidakCukup() {
        List<TandaVitalEntity> riwayat = List.of(tv(150, 65, null), tv(145, 65, null));
        assertTrue(RisikoManager.jenisRisikoTerpenuhi(AMBANG, riwayat, tv(120, 65, null)).isEmpty());
    }

    @Test
    void salahSatuDariTigaKunjunganDiBawahAmbang_memutusRentetan() {
        List<TandaVitalEntity> riwayat = List.of(tv(150, 65, null), tv(130, 65, null), tv(160, 65, null));
        assertTrue(RisikoManager.jenisRisikoTerpenuhi(AMBANG, riwayat, tv(120, 65, null)).isEmpty());
    }

    @Test
    void beberapaRisikoSekaligus_semuanyaDilaporkan() {
        assertEquals(List.of("hipertensi", "obesitas", "indikasi diabetes"),
                RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(160, 95, 300)));
    }

    @Test
    void entriTerbukaUntukPasienDanRisikoSama_tidakDimasukkanLagi() {
        List<DaftarPantauEntity> terbuka = List.of(
                new DaftarPantauEntity(1, 7, "hipertensi", DaftarPantauEntity.STATUS_ULANG));
        assertTrue(RisikoManager.sudahDipantau(terbuka, 7, "hipertensi"));
        assertFalse(RisikoManager.sudahDipantau(terbuka, 7, "obesitas"));
        assertFalse(RisikoManager.sudahDipantau(terbuka, 8, "hipertensi"));
    }
}
