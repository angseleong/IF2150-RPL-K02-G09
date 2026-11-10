package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.ResepEntity;
import sehati.business.entity.SkriningEntity;
import sehati.business.entity.TandaVitalEntity;

class PenyusunBundelFHIRTest {

    private static final LocalDateTime WAKTU = LocalDateTime.of(2026, 11, 8, 9, 15);

    private final KunjunganEntity kunjungan = new KunjunganEntity(21, 2, 1, LocalDate.of(2026, 11, 8), 2,
            KunjunganEntity.STATUS_SELESAI);
    private final PasienEntity pasien = new PasienEntity(2, "9999010101750002", "RM-000002", "Siti \"Ami\" Aminah",
            LocalDate.of(1975, 7, 21), "Perempuan", "Jl. Mawar No. 2", null);
    private final Map<Integer, ObatEntity> obat = Map.of(4, new ObatEntity(4, "Metformin 500 mg", "tablet", 190, 100));

    private ResepEntity resep() {
        ResepEntity r = new ResepEntity(9, 21, WAKTU.minusMinutes(30), ResepEntity.STATUS_SELESAI_DILAYANI, null);
        r.setDaftarItem(List.of(new ItemResepEntity(1, 4, "2x1", 60, "sesudah makan")));
        return r;
    }

    private static int hitung(String teks, String potongan) {
        return teks.split(java.util.regex.Pattern.quote(potongan), -1).length - 1;
    }

    @Test
    void susun_bundelTransactionBerisiPasienKunjunganObservasiDanPenyerahan() {
        SkriningEntity skrining = new SkriningEntity(5, 21, "Sering haus", WAKTU.minusHours(1));
        skrining.lampirkanTandaVital(new TandaVitalEntity(150, 95, 70.5, 158, 36.8, 88, 210, WAKTU.minusHours(1)));

        String json = PenyusunBundelFHIR.susun(kunjungan, pasien, skrining, resep(), obat, WAKTU);

        assertTrue(json.startsWith("{\"resourceType\":\"Bundle\",\"type\":\"transaction\",\"entry\":["));
        assertEquals(1, hitung(json, "\"resourceType\":\"Patient\""));
        assertEquals(1, hitung(json, "\"resourceType\":\"Encounter\""));
        // Enam tanda vital wajib ditambah gula darah yang terisi.
        assertEquals(7, hitung(json, "\"resourceType\":\"Observation\""));
        assertEquals(1, hitung(json, "\"resourceType\":\"MedicationDispense\""));
        assertTrue(json.contains("\"system\":\"https://fhir.kemkes.go.id/id/nik\",\"value\":\"9999010101750002\""));
        assertTrue(json.contains("\"gender\":\"female\""));
        assertTrue(json.contains("\"text\":\"Siti \\\"Ami\\\" Aminah\""));
        assertTrue(json.contains("\"medicationCodeableConcept\":{\"text\":\"Metformin 500 mg tablet\"}"));
        assertTrue(json.contains("\"whenHandedOver\":\"2026-11-08T09:15\""));
    }

    @Test
    void susun_tanpaSkriningTidakMemuatObservasiDanIdentitasStabil() {
        String pertama = PenyusunBundelFHIR.susun(kunjungan, pasien, null, resep(), obat, WAKTU);
        String kedua = PenyusunBundelFHIR.susun(kunjungan, pasien, null, resep(), obat, WAKTU);

        assertFalse(pertama.contains("Observation"));
        assertEquals(pertama, kedua);
    }

    @Test
    void susun_dataTidakLengkapDitolak() {
        assertThrows(IllegalStateException.class,
                () -> PenyusunBundelFHIR.susun(kunjungan, null, null, resep(), obat, WAKTU));
    }

    @Test
    void tulis_meloloskanKarakterKhusus() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("a", "baris\nbaru\t\"kutip\" \\ \u0001");
        m.put("b", List.of(1, 2.5, true));
        m.put("c", null);
        assertEquals("{\"a\":\"baris\\nbaru\\t\\\"kutip\\\" \\\\ \\u0001\",\"b\":[1,2.5,true],\"c\":null}",
                PenyusunBundelFHIR.tulis(m));
    }
}
