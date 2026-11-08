package sehati.business.control;

import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.ResepEntity;
import sehati.business.entity.SkriningEntity;
import sehati.business.entity.TandaVitalEntity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Menyusun isi BundelFHIREntity: bundel HL7 FHIR R4 berjenis transaction untuk satu kunjungan (KF14, KNF07). Dipakai
 * ApotekManager.susunBundel() agar ApotekManager tidak memuat rincian format FHIR (tambahan implementasi, B.1).
 */
final class PenyusunBundelFHIR {

    private static final String SISTEM_NIK = "https://fhir.kemkes.go.id/id/nik";
    private static final String SISTEM_LOINC = "http://loinc.org";
    private static final String SISTEM_UCUM = "http://unitsofmeasure.org";

    private PenyusunBundelFHIR() {
    }

    /**
     * Skrining boleh null bila kunjungan tidak melalui skrining. Obat dipetakan dengan idObat untuk nama sediaan.
     */
    static String susun(KunjunganEntity kunjungan, PasienEntity pasien, SkriningEntity skrining, ResepEntity resep,
            Map<Integer, ObatEntity> obat, LocalDateTime waktuSerah) {
        if (kunjungan == null || pasien == null || resep == null) {
            throw new IllegalStateException("Data kunjungan tidak lengkap untuk menyusun bundel FHIR");
        }
        int id = kunjungan.getIdKunjungan();
        String urnPasien = urn(id, "pasien");
        String urnKunjungan = urn(id, "kunjungan");

        List<Object> entri = new ArrayList<>();
        entri.add(entri(urnPasien, "Patient", pasien(pasien)));
        entri.add(entri(urnKunjungan, "Encounter", kunjungan(kunjungan, pasien, urnPasien)));
        if (skrining != null && skrining.getTandaVital() != null) {
            TandaVitalEntity tv = skrining.getTandaVital();
            String waktu = skrining.getWaktu().toString();
            tambahObservasi(entri, id, "8480-6", "Systolic blood pressure", tv.getSistolik(), "mm[Hg]", waktu,
                    urnPasien, urnKunjungan);
            tambahObservasi(entri, id, "8462-4", "Diastolic blood pressure", tv.getDiastolik(), "mm[Hg]", waktu,
                    urnPasien, urnKunjungan);
            tambahObservasi(entri, id, "29463-7", "Body weight", tv.getBeratBadan(), "kg", waktu, urnPasien,
                    urnKunjungan);
            tambahObservasi(entri, id, "8302-2", "Body height", tv.getTinggiBadan(), "cm", waktu, urnPasien,
                    urnKunjungan);
            tambahObservasi(entri, id, "8310-5", "Body temperature", tv.getSuhu(), "Cel", waktu, urnPasien,
                    urnKunjungan);
            tambahObservasi(entri, id, "8867-4", "Heart rate", tv.getNadi(), "/min", waktu, urnPasien,
                    urnKunjungan);
            if (tv.getGulaDarah() != null) {
                tambahObservasi(entri, id, "2339-0", "Glucose [Mass/volume] in Blood", tv.getGulaDarah(), "mg/dL",
                        waktu, urnPasien, urnKunjungan);
            }
        }
        for (ItemResepEntity item : resep.getDaftarItem()) {
            entri.add(entri(urn(id, "obat-" + item.getIdObat()), "MedicationDispense",
                    penyerahan(item, obat.get(item.getIdObat()), waktuSerah, urnPasien, urnKunjungan)));
        }

        Map<String, Object> bundel = new LinkedHashMap<>();
        bundel.put("resourceType", "Bundle");
        bundel.put("type", "transaction");
        bundel.put("entry", entri);
        return tulis(bundel);
    }

    private static Map<String, Object> pasien(PasienEntity p) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("resourceType", "Patient");
        r.put("identifier", List.of(peta("use", "official", "system", SISTEM_NIK, "value", p.getNik())));
        r.put("name", List.of(peta("use", "official", "text", p.getNama())));
        r.put("gender", "Perempuan".equals(p.getJenisKelamin()) ? "female" : "male");
        if (p.getTanggalLahir() != null) {
            r.put("birthDate", p.getTanggalLahir().toString());
        }
        return r;
    }

    private static Map<String, Object> kunjungan(KunjunganEntity k, PasienEntity p, String urnPasien) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("resourceType", "Encounter");
        r.put("identifier", List.of(peta("system", "urn:sehati:kunjungan", "value",
                String.valueOf(k.getIdKunjungan()))));
        r.put("status", "finished");
        r.put("class", peta("system", "http://terminology.hl7.org/CodeSystem/v3-ActCode", "code", "AMB",
                "display", "ambulatory"));
        r.put("subject", peta("reference", urnPasien, "display", p.getNama()));
        r.put("period", peta("start", k.getTanggal().toString()));
        return r;
    }

    private static void tambahObservasi(List<Object> entri, int idKunjungan, String kode, String nama, Number nilai,
            String satuan, String waktu, String urnPasien, String urnKunjungan) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("resourceType", "Observation");
        r.put("status", "final");
        r.put("category", List.of(peta("coding", List.of(peta("system",
                "http://terminology.hl7.org/CodeSystem/observation-category", "code", "vital-signs")))));
        r.put("code", peta("coding", List.of(peta("system", SISTEM_LOINC, "code", kode, "display", nama))));
        r.put("subject", peta("reference", urnPasien));
        r.put("encounter", peta("reference", urnKunjungan));
        r.put("effectiveDateTime", waktu);
        r.put("valueQuantity", peta("value", nilai, "unit", satuan, "system", SISTEM_UCUM, "code", satuan));
        entri.add(entri(urn(idKunjungan, "observasi-" + kode), "Observation", r));
    }

    private static Map<String, Object> penyerahan(ItemResepEntity item, ObatEntity obat, LocalDateTime waktuSerah,
            String urnPasien, String urnKunjungan) {
        String namaObat = obat == null ? "Obat " + item.getIdObat() : obat.getNamaObat() + " " + obat.getSediaan();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("resourceType", "MedicationDispense");
        r.put("status", "completed");
        r.put("medicationCodeableConcept", peta("text", namaObat));
        r.put("subject", peta("reference", urnPasien));
        r.put("context", peta("reference", urnKunjungan));
        r.put("quantity", peta("value", item.getJumlah()));
        r.put("whenHandedOver", waktuSerah.withNano(0).toString());
        r.put("dosageInstruction", List.of(peta("text", item.getDosis() + ", " + item.getAturanPakai())));
        return r;
    }

    private static Map<String, Object> entri(String fullUrl, String jenis, Map<String, Object> sumber) {
        return peta("fullUrl", fullUrl, "resource", sumber, "request", peta("method", "POST", "url", jenis));
    }

    // UUID berbasis nama: menyusun ulang kunjungan yang sama menghasilkan identitas sumber yang sama.
    private static String urn(int idKunjungan, String bagian) {
        String nama = "sehati/kunjungan/" + idKunjungan + "/" + bagian;
        return "urn:uuid:" + UUID.nameUUIDFromBytes(nama.getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, Object> peta(Object... pasangan) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pasangan.length; i += 2) {
            m.put((String) pasangan[i], pasangan[i + 1]);
        }
        return m;
    }

    /**
     * Menulis Map, List, String, Number, Boolean, dan null sebagai teks JSON.
     */
    static String tulis(Object nilai) {
        StringBuilder sb = new StringBuilder();
        tulis(nilai, sb);
        return sb.toString();
    }

    private static void tulis(Object nilai, StringBuilder sb) {
        if (nilai == null) {
            sb.append("null");
        } else if (nilai instanceof String s) {
            tulisTeks(s, sb);
        } else if (nilai instanceof Number || nilai instanceof Boolean) {
            sb.append(nilai);
        } else if (nilai instanceof Map<?, ?> m) {
            sb.append('{');
            boolean pertama = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!pertama) {
                    sb.append(',');
                }
                pertama = false;
                tulisTeks(String.valueOf(e.getKey()), sb);
                sb.append(':');
                tulis(e.getValue(), sb);
            }
            sb.append('}');
        } else if (nilai instanceof List<?> l) {
            sb.append('[');
            for (int i = 0; i < l.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                tulis(l.get(i), sb);
            }
            sb.append(']');
        } else {
            throw new IllegalArgumentException("Tipe tidak didukung JSON: " + nilai.getClass().getName());
        }
    }

    private static void tulisTeks(String s, StringBuilder sb) {
        sb.append('"');
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }
}
