package sehati.integrasi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.control.ApotekManager;
import sehati.business.control.PemeriksaanManager;
import sehati.business.control.ResepManager;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.RekamMedisEntity;
import sehati.business.entity.ResepEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

/**
 * Pengujian integrasi modul pemeriksaan dan resep (HIUPL Subbab 4.3, INT-TC17 sampai INT-TC22). Manager dijalankan
 * dengan DataBoundary dan basis data PostgreSQL yang nyata, tanpa mock. Hasilnya dibaca ulang langsung dari tabel
 * lewat koneksi terpisah, sehingga yang diperiksa adalah data yang benar-benar sampai di basis data.
 */
@DisplayName("Integrasi pemeriksaan dan resep (IK-09, IK-10, IK-11)")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class PemeriksaanResepIntegrasiTest {

    private final PemeriksaanManager pemeriksaan = new PemeriksaanManager();
    private final ResepManager resep = new ResepManager();

    private Connection koneksi;
    private int idKunjungan;
    private int idPasien;
    private String statusAwal;
    private int resepTerakhir;
    private int auditTerakhir;

    @BeforeEach
    void siapkan() throws SQLException {
        koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        // Kunjungan hari ini yang sudah diskrining dan menunggu dokter (seed.sql).
        idKunjungan = angka("SELECT min(id_kunjungan) FROM kunjungan WHERE status = 'siap diperiksa dokter'");
        idPasien = angka("SELECT id_pasien FROM kunjungan WHERE id_kunjungan = " + idKunjungan);
        statusAwal = teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan);
        resepTerakhir = angka("SELECT coalesce(max(id_resep), 0) FROM resep");
        auditTerakhir = angka("SELECT coalesce(max(id_audit), 0) FROM audit");
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Dokter");
    }

    @AfterEach
    void pulihkan() throws SQLException {
        SesiAktif.lepas();
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("DELETE FROM antrean_entri e USING antrean a WHERE e.id_antrean = a.id_antrean "
                    + "AND a.jenis = 'apotek' AND e.id_entri > " + resepTerakhir);
            st.executeUpdate("DELETE FROM item_resep WHERE id_resep > " + resepTerakhir);
            st.executeUpdate("DELETE FROM resep WHERE id_resep > " + resepTerakhir);
            st.executeUpdate("DELETE FROM diagnosis WHERE id_pemeriksaan IN "
                    + "(SELECT id_pemeriksaan FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan + ")");
            st.executeUpdate("DELETE FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan);
            st.executeUpdate("DELETE FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan);
            st.executeUpdate("DELETE FROM daftar_pantau WHERE id_jadwal IN (SELECT id_jadwal FROM jadwal_kontrol "
                    + "WHERE id_pasien = " + idPasien + " AND tanggal_rencana = CURRENT_DATE)");
            st.executeUpdate("DELETE FROM jadwal_kontrol WHERE id_pasien = " + idPasien
                    + " AND tanggal_rencana = CURRENT_DATE");
            st.executeUpdate("DELETE FROM audit WHERE id_audit > " + auditTerakhir);
            st.executeUpdate("UPDATE kunjungan SET status = '" + statusAwal + "' WHERE id_kunjungan = " + idKunjungan);
        }
        koneksi.close();
    }

    @Test
    @DisplayName("INT-TC17 IK-09 normal: ringkasan dibaca dari tiga boundary dan pemeriksaan tersimpan utuh")
    void ik09_ringkasanDanSimpanPemeriksaan() throws SQLException {
        RekamMedisEntity rm = pemeriksaan.susunRingkasan(idKunjungan);

        assertEquals(teks("SELECT nama FROM pasien WHERE id_pasien = " + idPasien), rm.getPasien().getNama());
        assertEquals(teks("SELECT keluhan_awal FROM skrining WHERE id_kunjungan = " + idKunjungan),
                rm.getSkriningHariIni().getKeluhanAwal());
        assertEquals(angka("SELECT count(*) FROM penanda_risiko WHERE status = 'aktif' AND id_kunjungan = "
                + idKunjungan), rm.getPenandaAktif().size());

        pemeriksaan.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Pusing dua hari", "Edukasi diet garam",
                false), new DiagnosisEntity(0, "I10", "Hipertensi esensial (primer)"));

        assertEquals("Pusing dua hari|Edukasi diet garam|false|I10", teks("SELECT p.anamnesis || '|' || p.tindakan "
                + "|| '|' || p.terkunci || '|' || d.kode_icd10 FROM pemeriksaan p JOIN diagnosis d "
                + "USING (id_pemeriksaan) WHERE p.id_kunjungan = " + idKunjungan));
    }

    @Test
    @DisplayName("INT-TC18 IK-09 gagal: kode di luar data master ICD-10 ditolak dan tidak ada baris yang tertinggal")
    void ik09_kodeTidakTerdaftar() throws SQLException {
        assertThrows(IllegalArgumentException.class, () -> pemeriksaan.simpanPemeriksaan(idKunjungan,
                new PemeriksaanEntity(0, 0, "Batuk", "Obat", false), new DiagnosisEntity(0, "XYZ", "Bukan kode")));

        assertEquals(0, angka("SELECT count(*) FROM pemeriksaan WHERE id_kunjungan = " + idKunjungan));
        assertEquals(0, angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir));
    }

    @Test
    @DisplayName("INT-TC19 IK-10 normal: penguncian menulis pemeriksaan, kunjungan, bundel FHIR, jadwal, pantau, "
            + "dan audit")
    void ik10_kunciRekamMedis() throws SQLException {
        int idJadwal;
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("INSERT INTO jadwal_kontrol (id_pasien, tanggal_rencana) VALUES (" + idPasien
                    + ", CURRENT_DATE)");
            idJadwal = angka("SELECT max(id_jadwal) FROM jadwal_kontrol WHERE id_pasien = " + idPasien);
            st.executeUpdate("INSERT INTO daftar_pantau (id_pasien, jenis_risiko, id_jadwal) VALUES (" + idPasien
                    + ", 'kunjungan ulang', " + idJadwal + ")");
        }
        simpanDrafLengkap();

        pemeriksaan.kunciRekamMedis(idKunjungan);

        assertTrue(angka("SELECT count(*) FROM pemeriksaan WHERE terkunci AND id_kunjungan = " + idKunjungan) == 1);
        assertEquals("selesai", teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan));
        assertEquals("tertunda|I10", teks("SELECT b.status || '|' || (e -> 'resource' -> 'code' -> 'coding' -> 0 "
                + "->> 'code') FROM bundel_fhir b, jsonb_array_elements(b.isi_bundel -> 'entry') e WHERE "
                + "e -> 'resource' ->> 'resourceType' = 'Condition' AND b.id_kunjungan = " + idKunjungan));
        assertEquals("selesai", teks("SELECT status FROM jadwal_kontrol WHERE id_jadwal = " + idJadwal));
        assertEquals("selesai ditindaklanjuti", teks("SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_jadwal = "
                + idJadwal));
        assertEquals(1, angka("SELECT count(*) FROM audit WHERE id_pengguna = 1 AND id_audit > " + auditTerakhir
                + " AND deskripsi = 'Pemeriksaan kunjungan " + idKunjungan + " dikunci'"));
    }

    @Test
    @DisplayName("INT-TC20 IK-10 gagal: kunjungan sudah ditutup, seluruh transaksi penguncian dibatalkan")
    void ik10_transaksiDibatalkan() throws SQLException {
        simpanDrafLengkap();
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("UPDATE kunjungan SET status = 'selesai' WHERE id_kunjungan = " + idKunjungan);
        }
        int auditSebelum = angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir);

        assertThrows(IllegalStateException.class, () -> pemeriksaan.kunciRekamMedis(idKunjungan));

        // Pemeriksaan sudah ditandai terkunci di dalam transaksi sebelum status kunjungan ditolak, jadi harus kembali.
        assertEquals(0, angka("SELECT count(*) FROM pemeriksaan WHERE terkunci AND id_kunjungan = " + idKunjungan));
        assertEquals(0, angka("SELECT count(*) FROM bundel_fhir WHERE id_kunjungan = " + idKunjungan));
        assertEquals(auditSebelum, angka("SELECT count(*) FROM audit WHERE id_audit > " + auditTerakhir));
    }

    @Test
    @DisplayName("INT-TC21 IK-11 normal: resep tersimpan dengan itemnya, masuk antrean, dan terbaca oleh apotek")
    void ik11_kirimResep() throws SQLException {
        ResepEntity r = resep.kirimKeApotek(idKunjungan, List.of(
                resep.tambahItem(3, 30, "5 mg", "1 x 1 pagi"), resep.tambahItem(1, 10, "500 mg", "3 x 1 bila demam")));

        assertEquals("menunggu|" + idKunjungan, teks("SELECT status || '|' || id_kunjungan FROM resep WHERE id_resep = "
                + r.getIdResep()));
        assertEquals("1:10:500 mg:3 x 1 bila demam,3:30:5 mg:1 x 1 pagi", teks("SELECT string_agg(id_obat || ':' "
                + "|| jumlah || ':' || dosis || ':' || aturan_pakai, ',' ORDER BY id_obat) FROM item_resep "
                + "WHERE id_resep = " + r.getIdResep()));
        assertEquals(r.getIdResep(), angka("SELECT e.id_entri FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                + "WHERE a.jenis = 'apotek' ORDER BY e.urutan DESC LIMIT 1"));
        assertTrue(new ApotekManager().muatAntreanResep().stream().anyMatch(x -> x.getIdResep() == r.getIdResep()));
    }

    @Test
    @DisplayName("INT-TC22 IK-11 gagal: dua dokter mengirim resep bersamaan, hanya satu yang tersimpan")
    void ik11_kirimBersamaan() throws Exception {
        CountDownLatch mulai = new CountDownLatch(1);
        Callable<ResepEntity> kirim = () -> {
            mulai.await();
            return resep.kirimKeApotek(idKunjungan, List.of(new ItemResepEntity(0, 1, "500 mg", 10, "3 x 1")));
        };
        ExecutorService pelaksana = Executors.newFixedThreadPool(2);
        List<Future<ResepEntity>> hasil = new ArrayList<>(List.of(pelaksana.submit(kirim), pelaksana.submit(kirim)));
        mulai.countDown();

        int berhasil = 0;
        List<String> ditolak = new ArrayList<>();
        for (Future<ResepEntity> f : hasil) {
            try {
                f.get();
                berhasil++;
            } catch (java.util.concurrent.ExecutionException e) {
                ditolak.add(e.getCause().getMessage());
            }
        }
        pelaksana.shutdown();

        assertEquals(1, berhasil);
        assertEquals(1, ditolak.size());
        assertTrue(ditolak.get(0).startsWith("Kunjungan ini masih memiliki resep yang menunggu di apotek"));
        assertEquals(1, angka("SELECT count(*) FROM resep WHERE id_resep > " + resepTerakhir));
        assertEquals(1, angka("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                + "WHERE a.jenis = 'apotek' AND e.id_entri > " + resepTerakhir));
        assertFalse(ditolak.get(0).isBlank());
    }

    private void simpanDrafLengkap() {
        pemeriksaan.simpanPemeriksaan(idKunjungan, new PemeriksaanEntity(0, 0, "Pusing dua hari", "Edukasi", false),
                new DiagnosisEntity(0, "I10", "Hipertensi esensial (primer)"));
    }

    private int angka(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); var rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private String teks(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); var rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        }
    }
}
