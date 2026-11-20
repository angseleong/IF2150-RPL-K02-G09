package sehati.uji;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import sehati.business.control.ApotekManager;
import sehati.business.control.AutentikasiManager;
import sehati.business.control.KunjunganManager;
import sehati.business.control.PasienManager;
import sehati.business.control.PemeriksaanManager;
import sehati.business.control.ResepManager;
import sehati.business.control.SkriningManager;
import sehati.business.entity.BundelFHIREntity;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.ResepEntity;
import sehati.business.entity.TandaVitalEntity;
import sehati.common.KoneksiBasisData;
import sehati.common.SesiAktif;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Uji alur utama: satu pasien dari pendaftaran sampai bundel FHIR berstatus "tertunda", memakai empat peran seed.
 * Uji DB di-gate DB_UJI_URL (tidak berjalan di CI tanpa Postgres).
 */
class AlurUtamaTest {

    private static final String SANDI = "sehati123";

    private AutentikasiManager auth = new AutentikasiManager();
    private PasienManager pasienManager = new PasienManager();
    private KunjunganManager kunjunganManager = new KunjunganManager();
    private SkriningManager skriningManager = new SkriningManager();
    private PemeriksaanManager pemeriksaanManager = new PemeriksaanManager();
    private ResepManager resepManager = new ResepManager();
    private ApotekManager apotekManager = new ApotekManager();

    private int idKunjungan;
    private int idPasien;

    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        @BeforeEach
        void masukAdmin() {
            SesiAktif.pasang(auth.autentikasi("admin", SANDI));
        }

        @AfterEach
        void bersihkan() {
            SesiAktif.lepas();
        }

        @Test
        void alurSatuPasienSampaiBundelTertunda() throws Exception {
            // 1. Daftar pasien baru (peran admin)
            String nik = "9999" + String.format("%012d", System.currentTimeMillis() % 1_000_000_000_000L);
            PasienEntity pasien = new PasienEntity(0, nik, "RM-" + nik.substring(8), "Pasien Uji " + nik.substring(8),
                    LocalDate.of(1990, 1, 1), "Laki-laki", "Jl. Uji", "081234567890");
            PasienEntity tersimpan = pasienManager.daftarkanPasien(pasien);
            assertNotNull(tersimpan);
            idPasien = tersimpan.getIdPasien();

            // 2. Buka kunjungan (admin)
            KunjunganEntity kunjungan = kunjunganManager.bukaKunjungan(idPasien, 1);
            assertNotNull(kunjungan);
            idKunjungan = kunjungan.getIdKunjungan();
            assertEquals("menunggu skrining", kunjungan.getStatus());

            // 3. Skrining (peran perawat)
            SesiAktif.pasang(auth.autentikasi("perawat", SANDI));
            skriningManager.panggilPasien();
            TandaVitalEntity tv = new TandaVitalEntity(120, 80, 60, 165, 36.5, 80, 90);
            List<String> peringatan = skriningManager.simpanSkrining(idKunjungan, "demam", tv);
            assertTrue(peringatan.isEmpty(), "skrining tidak boleh menghasilkan peringatan: " + peringatan);

            // 4. Pemeriksaan (peran dokter)
            SesiAktif.pasang(auth.autentikasi("dokter", SANDI));
            PemeriksaanEntity pem = new PemeriksaanEntity(0, idKunjungan, "anamnesis uji", "tindakan uji", false);
            DiagnosisEntity diag = new DiagnosisEntity(0, "A09", "Diare");
            pemeriksaanManager.simpanPemeriksaan(idKunjungan, pem, diag);

            // 5. Resep (dokter)
            ItemResepEntity item = resepManager.tambahItem(1, 10, "3x1", "sesudah makan");
            ResepEntity resep = resepManager.kirimKeApotek(idKunjungan, List.of(item));
            assertNotNull(resep);
            assertTrue(resep.getIdResep() > 0);

            // 6. Penyerahan (peran farmasi)
            SesiAktif.pasang(auth.autentikasi("farmasi", SANDI));
            apotekManager.layaniResep(resep.getIdResep());
            apotekManager.serahkanObat(resep.getIdResep());

            // 7. Verifikasi bundel tertunda
            try (Connection k = DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
                try (PreparedStatement ps = k.prepareStatement(
                        "SELECT status FROM bundel_fhir WHERE id_kunjungan = ?")) {
                    ps.setInt(1, idKunjungan);
                    try (ResultSet rs = ps.executeQuery()) {
                        assertTrue(rs.next(), "bundel FHIR harus ada untuk kunjungan " + idKunjungan);
                        assertEquals("tertunda", rs.getString("status"));
                    }
                }
            }
        }
    }
}
