package sehati.integrasi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.CadanganFarmasi;
import sehati.business.control.ApotekManager;
import sehati.business.control.PersediaanManager;
import sehati.business.control.SinkronisasiManager;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.business.entity.ResepEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

/**
 * Pengujian integrasi modul farmasi (HIUPL Subbab 4.3, INT-TC23 sampai INT-TC26). ApotekManager dan
 * PersediaanManager dijalankan dengan DataBoundary dan basis data PostgreSQL yang nyata, tanpa mock. Hasilnya dibaca
 * ulang langsung dari tabel lewat koneksi terpisah, sehingga yang diperiksa adalah data yang benar-benar sampai di
 * basis data. Memakai resep seed 4 (Siti Aminah, kunjungan 5): Metformin 60 dan Glibenklamid 30.
 */
@DisplayName("Integrasi farmasi (IK-12, IK-13)")
@TestMethodOrder(MethodOrderer.DisplayName.class)
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class FarmasiIntegrasiTest {

    private static final int METFORMIN = 4;
    private static final int GLIBENKLAMID = 5;

    private final ApotekManager apotek = new ApotekManager();
    private final PersediaanManager persediaan = new PersediaanManager();

    private Connection koneksi;
    private CadanganFarmasi cadangan;
    private int idResep;
    private int idKunjungan;
    private String statusKunjungan;

    @BeforeEach
    void siapkan() throws SQLException {
        koneksi = DriverManager.getConnection(System.getenv("DB_UJI_URL"));
        cadangan = CadanganFarmasi.ambil(koneksi);
        idResep = angka("SELECT min(id_resep) FROM resep WHERE status = 'menunggu'");
        idKunjungan = angka("SELECT id_kunjungan FROM resep WHERE id_resep = " + idResep);
        statusKunjungan = teks("SELECT status FROM kunjungan WHERE id_kunjungan = " + idKunjungan);
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 4, LocalDateTime.now(), true), "Petugas Farmasi");
    }

    @AfterEach
    void pulihkan() throws SQLException {
        SesiAktif.lepas();
        cadangan.pulihkan();
        koneksi.close();
    }

    @Test
    @DisplayName("INT-TC23 IK-12 normal: resep dari antrean diserahkan, stok FEFO, kunjungan, dan bundel sampai di "
            + "tabelnya")
    void ik12_serahkanObat() throws SQLException {
        int stokMetformin = angka("SELECT stok FROM obat WHERE id_obat = " + METFORMIN);
        int stokGlibenklamid = angka("SELECT stok FROM obat WHERE id_obat = " + GLIBENKLAMID);

        ResepEntity dariAntrean = apotek.muatAntreanResep().stream()
                .filter(r -> r.getIdResep() == idResep).findFirst().orElseThrow();
        ResepEntity rincian = apotek.layaniResep(dariAntrean.getIdResep());
        List<BetsObatEntity> bets = apotek.muatBetsResep(rincian);
        apotek.serahkanObat(rincian.getIdResep());

        assertEquals(List.of("MET-A01", "GLB-A01"), bets.stream().map(BetsObatEntity::getNomorBets).toList());
        assertEquals((stokMetformin - 60) + "|" + (stokGlibenklamid - 30),
                teks("SELECT string_agg(stok::text, '|' ORDER BY id_obat) FROM obat WHERE id_obat IN (4, 5)"));
        assertEquals((stokMetformin - 60) + "|0", teks("SELECT string_agg(jumlah::text, '|' ORDER BY id_obat) "
                + "FROM bets_obat WHERE nomor_bets IN ('MET-A01', 'GLB-A01')"));
        assertEquals("selesai dilayani|selesai", teks("SELECT r.status || '|' || k.status FROM resep r "
                + "JOIN kunjungan k USING (id_kunjungan) WHERE r.id_resep = " + idResep));
        assertEquals(0, angka("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                + "WHERE a.jenis = 'apotek' AND e.id_entri = " + idResep));
        // Bundel memuat NIK pasien dan dua MedicationRequest, lalu terbaca SinkronisasiManager sebagai tertunda.
        assertEquals("tertunda|2|" + teks("SELECT p.nik FROM kunjungan k JOIN pasien p USING (id_pasien) "
                + "WHERE k.id_kunjungan = " + idKunjungan), teks("SELECT b.status || '|' || (SELECT count(*) "
                + "FROM jsonb_array_elements(b.isi_bundel -> 'entry') e WHERE e -> 'resource' ->> 'resourceType' "
                + "= 'MedicationRequest') || '|' || (SELECT e -> 'resource' -> 'identifier' -> 0 ->> 'value' "
                + "FROM jsonb_array_elements(b.isi_bundel -> 'entry') e WHERE e -> 'resource' ->> 'resourceType' "
                + "= 'Patient') FROM bundel_fhir b WHERE b.id_kunjungan = " + idKunjungan));
        assertTrue(new SinkronisasiManager().muatStatus().stream()
                .anyMatch(b -> b.getIdKunjungan() == idKunjungan && "tertunda".equals(b.getStatus())));
        assertEquals("4|Obat resep " + idResep + " diserahkan", teks("SELECT id_pengguna || '|' || deskripsi "
                + "FROM audit WHERE id_audit > " + cadangan.auditTerakhir()));
    }

    @Test
    @DisplayName("INT-TC24 IK-12 gagal: stok Glibenklamid dipakai komputer lain, seluruh transaksi penyerahan "
            + "dibatalkan")
    void ik12_stokBerubahDibatalkan() throws SQLException {
        // Komputer lain memakai 10 Glibenklamid setelah resep dikirim, sehingga stok tinggal 20 dari 30 yang diminta.
        try (Statement st = koneksi.createStatement()) {
            st.executeUpdate("UPDATE obat SET stok = stok - 10 WHERE id_obat = " + GLIBENKLAMID);
            st.executeUpdate("UPDATE bets_obat SET jumlah = jumlah - 10 WHERE nomor_bets = 'GLB-A01'");
        }
        String stokSebelum = teks("SELECT string_agg(stok::text, '|' ORDER BY id_obat) FROM obat");
        String betsSebelum = teks("SELECT string_agg(jumlah::text, '|' ORDER BY id_obat, nomor_bets) FROM bets_obat");
        int bundelSebelum = angka("SELECT count(*) FROM bundel_fhir");

        // Metformin (id 4) dipotong lebih dulu dalam transaksi, lalu Glibenklamid (id 5) gagal.
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> apotek.serahkanObat(idResep));

        assertEquals("Stok Glibenklamid 5 mg tidak cukup", e.getMessage());
        assertEquals(stokSebelum, teks("SELECT string_agg(stok::text, '|' ORDER BY id_obat) FROM obat"));
        assertEquals(betsSebelum, teks("SELECT string_agg(jumlah::text, '|' ORDER BY id_obat, nomor_bets) "
                + "FROM bets_obat"));
        assertEquals("menunggu|" + statusKunjungan, teks("SELECT r.status || '|' || k.status FROM resep r "
                + "JOIN kunjungan k USING (id_kunjungan) WHERE r.id_resep = " + idResep));
        assertEquals(bundelSebelum, angka("SELECT count(*) FROM bundel_fhir"));
        assertEquals(1, angka("SELECT count(*) FROM antrean_entri e JOIN antrean a USING (id_antrean) "
                + "WHERE a.jenis = 'apotek' AND e.id_entri = " + idResep));
        assertEquals(0, angka("SELECT count(*) FROM audit WHERE id_audit > " + cadangan.auditTerakhir()));
    }

    @Test
    @DisplayName("INT-TC25 IK-13 normal: penerimaan dua bets menambah stok, tercatat, dan terbaca oleh apotek")
    void ik13_catatPenerimaan() throws SQLException {
        LocalDate hariIni = LocalDate.now();
        PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FKT-INT-25", hariIni, new ArrayList<>(List.of(
                new BetsObatEntity("GLB-INT-2", GLIBENKLAMID, 30, hariIni.plusYears(2)),
                new BetsObatEntity("GLB-INT-1", GLIBENKLAMID, 40, hariIni.plusDays(400)))));
        assertTrue(persediaan.terbitkanPeringatan().stream().anyMatch(s -> s.startsWith("Stok Glibenklamid")));

        persediaan.catatPenerimaan(p);

        assertEquals(100, angka("SELECT stok FROM obat WHERE id_obat = " + GLIBENKLAMID));
        assertEquals("FKT-INT-25|" + hariIni, teks("SELECT nomor_faktur || '|' || tanggal_terima FROM penerimaan_obat "
                + "WHERE id_penerimaan = " + p.getIdPenerimaan()));
        assertEquals("GLB-INT-1:40,GLB-INT-2:30", teks("SELECT string_agg(nomor_bets || ':' || jumlah, ',' "
                + "ORDER BY nomor_bets) FROM bets_obat WHERE id_penerimaan = " + p.getIdPenerimaan()));
        assertEquals(angka("SELECT sum(jumlah) FROM bets_obat WHERE id_obat = " + GLIBENKLAMID),
                angka("SELECT stok FROM obat WHERE id_obat = " + GLIBENKLAMID));
        assertEquals("4|Obat masuk Glibenklamid 5 mg sejumlah 70, faktur FKT-INT-25", teks("SELECT id_pengguna "
                + "|| '|' || deskripsi FROM audit WHERE id_audit > " + cadangan.auditTerakhir()));
        // Data yang sama dibaca komponen lain: dasbor persediaan dan bets FEFO pada penyerahan obat.
        assertFalse(persediaan.terbitkanPeringatan().stream().anyMatch(s -> s.startsWith("Stok Glibenklamid")));
        assertEquals(List.of("MET-A01", "GLB-A01", "GLB-INT-1", "GLB-INT-2"), apotek.muatBetsResep(
                apotek.layaniResep(idResep)).stream().map(BetsObatEntity::getNomorBets).toList());
    }

    @Test
    @DisplayName("INT-TC26 IK-13 gagal: bets kedua bernomor ganda, penerimaan dan bets pertama dibatalkan")
    void ik13_nomorBetsGandaDibatalkan() throws SQLException {
        LocalDate hariIni = LocalDate.now();
        PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FKT-INT-26", hariIni, new ArrayList<>(List.of(
                new BetsObatEntity("GLB-INT-3", GLIBENKLAMID, 40, hariIni.plusYears(2)),
                new BetsObatEntity("GLB-A01", GLIBENKLAMID, 30, hariIni.plusYears(2)))));
        int penerimaanSebelum = angka("SELECT count(*) FROM penerimaan_obat");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> persediaan.catatPenerimaan(p));

        assertEquals("nomorBets", e.getMessage());
        assertEquals(0, p.getIdPenerimaan());
        assertEquals(30, angka("SELECT stok FROM obat WHERE id_obat = " + GLIBENKLAMID));
        assertEquals(penerimaanSebelum, angka("SELECT count(*) FROM penerimaan_obat"));
        assertEquals(0, angka("SELECT count(*) FROM penerimaan_obat WHERE nomor_faktur = 'FKT-INT-26'"));
        assertEquals(0, angka("SELECT count(*) FROM bets_obat WHERE nomor_bets = 'GLB-INT-3'"));
        assertEquals(30, angka("SELECT jumlah FROM bets_obat WHERE nomor_bets = 'GLB-A01'"));
        assertEquals(0, angka("SELECT count(*) FROM audit WHERE id_audit > " + cadangan.auditTerakhir()));
    }

    private int angka(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private String teks(String sql) throws SQLException {
        try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getString(1);
        }
    }
}
