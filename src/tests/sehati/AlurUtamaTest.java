package sehati;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.control.ApotekManager;
import sehati.business.control.KunjunganManager;
import sehati.business.control.PantauManager;
import sehati.business.control.PasienManager;
import sehati.business.control.PemeriksaanManager;
import sehati.business.control.ResepManager;
import sehati.business.control.SkriningManager;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.TandaVitalEntity;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sehati.DataUjiAlur.angka;
import static sehati.DataUjiAlur.masuk;
import static sehati.DataUjiAlur.teks;

// Issue #22: satu pasien dari loket sampai bundel FHIR tertunda memakai empat akun seed.
// Variabel DB_* harus menunjuk basis data yang sama dengan DB_UJI_URL (akun pemilik).
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class AlurUtamaTest {

    private DataUjiAlur data;

    @BeforeEach
    void siapkan() {
        data = new DataUjiAlur();
    }

    @AfterEach
    void bersihkan() throws SQLException {
        data.hapus();
    }

    @Test
    void satuPasienDariLoketSampaiBundelTertunda() throws SQLException {
        int poli = data.poliBaru();
        int obat = data.obatBaru(50);

        masuk("admin");
        PasienEntity pasien = new PasienManager().daftarkanPasien(new PasienEntity(0, data.nik(1), null,
                "Pasien Alur Utama", LocalDate.of(1980, 5, 1), "Perempuan", "Jl. Uji Alur 1", "081200000001"));
        data.pasien.add(pasien.getIdPasien());
        KunjunganEntity kunjungan = new KunjunganManager().bukaKunjungan(pasien.getIdPasien(), poli);
        int id = kunjungan.getIdKunjungan();
        assertEquals(1, kunjungan.getNomorAntrean());
        assertEquals("menunggu skrining", kunjungan.getStatus());

        masuk("perawat");
        assertTrue(new SkriningManager().simpanSkrining(id, "Batuk dan pusing",
                new TandaVitalEntity(120, 80, 60, 165, 36.8, 80, null)).isEmpty());
        assertEquals("siap diperiksa dokter", status(id));

        masuk("dokter");
        PemeriksaanManager pemeriksaan = new PemeriksaanManager();
        pemeriksaan.simpanPemeriksaan(id, new PemeriksaanEntity(0, id, "Batuk tiga hari", "Edukasi", false),
                new DiagnosisEntity(0, "J00", "Nasofaringitis akut"));
        ResepManager resep = new ResepManager();
        int idResep = resep.kirimKeApotek(id, List.of(resep.tambahItem(obat, 10, "500 mg", "3 x 1"))).getIdResep();

        // Apotek menunggu dokter menyimpan final
        masuk("farmasi");
        assertThrows(IllegalStateException.class, () -> new ApotekManager().serahkanObat(idResep));

        masuk("dokter");
        pemeriksaan.kunciRekamMedis(id);
        assertEquals("selesai diperiksa", status(id));

        masuk("farmasi");
        new ApotekManager().serahkanObat(idResep);

        assertEquals("selesai", status(id));
        assertEquals(40, angka("SELECT stok FROM obat WHERE id_obat = ?", obat));
        assertEquals(40, angka("SELECT sum(jumlah) FROM bets_obat WHERE id_obat = ?", obat));
        assertEquals("tertunda", teks("SELECT status FROM bundel_fhir WHERE id_kunjungan = ?", id));
        String sumber = teks("SELECT string_agg(DISTINCT e -> 'resource' ->> 'resourceType', ',') FROM bundel_fhir b, "
                + "jsonb_array_elements(b.isi_bundel -> 'entry') e WHERE b.id_kunjungan = ?", id);
        assertEquals("Condition,Encounter,MedicationRequest,Observation,Patient", sumber);
    }

    @Test
    void kunjunganBerisikoBerulangHanyaSatuEntriPantau() throws SQLException {
        int poli = data.poliBaru();
        masuk("admin");
        int idPasien = pasienBaru(2);
        for (int i = 0; i < 2; i++) {
            masuk("admin");
            int id = new KunjunganManager().bukaKunjungan(idPasien, poli).getIdKunjungan();
            masuk("perawat");
            new SkriningManager().simpanSkrining(id, "Pusing", new TandaVitalEntity(170, 95, 70, 165, 36.8, 80, null));
            masuk("dokter");
            PemeriksaanManager pemeriksaan = new PemeriksaanManager();
            pemeriksaan.simpanPemeriksaan(id, new PemeriksaanEntity(0, id, "Pusing", "Edukasi", false),
                    new DiagnosisEntity(0, "I10", "Hipertensi esensial"));
            pemeriksaan.kunciRekamMedis(id);
        }
        assertEquals(2, angka("SELECT count(*) FROM penanda_risiko pr JOIN kunjungan k USING (id_kunjungan) "
                + "WHERE k.id_pasien = ? AND pr.jenis_risiko = 'hipertensi'", idPasien));
        assertEquals(1, angka("SELECT count(*) FROM daftar_pantau WHERE id_pasien = ? AND jenis_risiko = 'hipertensi'",
                idPasien));
    }

    @Test
    void pasienDatangKontrolMenyelesaikanJadwalDanEntriPantau() throws SQLException {
        int poli = data.poliBaru();
        masuk("admin");
        int idPasien = pasienBaru(3);
        masuk("dokter");
        int idJadwal = new PemeriksaanManager().jadwalkanKontrol(idPasien, LocalDate.now().plusDays(7)).getIdJadwal();
        // Perawat membuka daftar pantau sehingga entri "kunjungan ulang" terbentuk, lalu tanggal kontrol tiba.
        masuk("perawat");
        new PantauManager().saringDaftar(null, null, null, null);
        try (var c = DataUjiAlur.pemilik(); var st = c.prepareStatement(
                "UPDATE jadwal_kontrol SET tanggal_rencana = CURRENT_DATE WHERE id_jadwal = ?")) {
            st.setInt(1, idJadwal);
            st.executeUpdate();
        }

        masuk("admin");
        int id = new KunjunganManager().bukaKunjungan(idPasien, poli).getIdKunjungan();
        // Baru dibuka di loket: pasien bisa saja tidak hadir di skrining, jadi jadwal belum selesai.
        assertEquals("terjadwal", teks("SELECT status FROM jadwal_kontrol WHERE id_jadwal = ?", idJadwal));

        masuk("perawat");
        new SkriningManager().simpanSkrining(id, "Kontrol", new TandaVitalEntity(120, 80, 60, 165, 36.5, 80, null));
        masuk("dokter");
        PemeriksaanManager pemeriksaan = new PemeriksaanManager();
        pemeriksaan.simpanPemeriksaan(id, new PemeriksaanEntity(0, id, "Kontrol rutin", "Edukasi", false),
                new DiagnosisEntity(0, "I10", "Hipertensi esensial"));
        pemeriksaan.kunciRekamMedis(id);

        assertEquals("selesai", teks("SELECT status FROM jadwal_kontrol WHERE id_jadwal = ?", idJadwal));
        assertEquals("selesai ditindaklanjuti",
                teks("SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_jadwal = ?", idJadwal));
    }

    private int pasienBaru(int urutan) {
        PasienEntity pasien = new PasienManager().daftarkanPasien(new PasienEntity(0, data.nik(urutan), null,
                "Pasien Alur " + urutan, LocalDate.of(1970, 1, 1), "Laki-laki", "Jl. Uji Alur", "081200000002"));
        data.pasien.add(pasien.getIdPasien());
        return pasien.getIdPasien();
    }

    private static String status(int idKunjungan) throws SQLException {
        return teks("SELECT status FROM kunjungan WHERE id_kunjungan = ?", idKunjungan);
    }
}
