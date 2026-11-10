package sehati;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.control.ApotekManager;
import sehati.business.control.KunjunganManager;
import sehati.business.control.PasienManager;
import sehati.business.control.PemeriksaanManager;
import sehati.business.control.PersediaanManager;
import sehati.business.control.ResepManager;
import sehati.business.control.SkriningManager;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.DiagnosisEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PemeriksaanEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.business.entity.TandaVitalEntity;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static sehati.DataUjiAlur.angka;
import static sehati.DataUjiAlur.masuk;

// Issue #22, KF23, KNF11: lima petugas bersamaan tidak menghasilkan nomor antrean ganda maupun lost update stok.
// Variabel DB_* harus menunjuk basis data yang sama dengan DB_UJI_URL (akun pemilik).
@EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
class KonkurensiTest {

    private static final int N = 5;

    private DataUjiAlur data;

    @BeforeEach
    void siapkan() {
        data = new DataUjiAlur();
    }

    @AfterEach
    void bersihkan() throws SQLException {
        data.hapus();
    }

    // Sesi dipasang sekali sebelum thread dilepas, karena SesiAktif berlaku untuk seluruh aplikasi
    private static List<Object> serentak(IntFunction<Object> tugas) throws Exception {
        ExecutorService ex = Executors.newFixedThreadPool(N);
        CyclicBarrier gerbang = new CyclicBarrier(N);
        List<Future<Object>> hasil = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            int n = i;
            hasil.add(ex.submit(() -> {
                gerbang.await();
                try {
                    return tugas.apply(n);
                } catch (RuntimeException e) {
                    return e;
                }
            }));
        }
        List<Object> keluaran = new ArrayList<>();
        for (Future<Object> f : hasil) {
            keluaran.add(f.get(30, TimeUnit.SECONDS));
        }
        ex.shutdown();
        return keluaran;
    }

    private List<KunjunganEntity> bukaLimaKunjungan(int poli) throws Exception {
        masuk("admin");
        List<PasienEntity> pasien = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            PasienEntity p = new PasienManager().daftarkanPasien(new PasienEntity(0, data.nik(i), null,
                    "Pasien Serentak " + i, LocalDate.of(1970 + i, 1, 1), "Laki-laki", "Jl. Serentak", null));
            data.pasien.add(p.getIdPasien());
            pasien.add(p);
        }
        List<KunjunganEntity> kunjungan = new ArrayList<>();
        for (Object o : serentak(i -> new KunjunganManager().bukaKunjungan(pasien.get(i).getIdPasien(), poli))) {
            kunjungan.add(assertInstanceOf(KunjunganEntity.class, o, String.valueOf(o)));
        }
        return kunjungan;
    }

    // Skrining, pemeriksaan final, dan resep untuk setiap kunjungan, lalu mengembalikan id resepnya
    private static List<Integer> siapkanResep(List<KunjunganEntity> kunjungan, int obat, int jumlah) {
        List<Integer> resep = new ArrayList<>();
        for (KunjunganEntity k : kunjungan) {
            int id = k.getIdKunjungan();
            masuk("perawat");
            new SkriningManager().simpanSkrining(id, "Uji", new TandaVitalEntity(120, 80, 60, 165, 36.5, 80, null));
            masuk("dokter");
            new PemeriksaanManager().simpanPemeriksaan(id, new PemeriksaanEntity(0, id, "Uji", "Uji", false),
                    new DiagnosisEntity(0, "I10", "Hipertensi esensial"));
            ResepManager rm = new ResepManager();
            resep.add(rm.kirimKeApotek(id, List.of(rm.tambahItem(obat, jumlah, "1", "1 x 1"))).getIdResep());
            new PemeriksaanManager().kunciRekamMedis(id);
        }
        return resep;
    }

    @Test
    void bukaKunjungan_limaLoketMendapatNomorBerurutanTanpaGanda() throws Exception {
        int poli = data.poliBaru();

        List<Integer> nomor = bukaLimaKunjungan(poli).stream().map(KunjunganEntity::getNomorAntrean).sorted().toList();

        assertEquals(List.of(1, 2, 3, 4, 5), nomor);
        assertEquals(N, angka("SELECT urutan_terakhir FROM poli WHERE id_poli = ?", poli));
    }

    @Test
    void serahkanObat_limaResepBerbedaMemotongStokTanpaLostUpdate() throws Exception {
        int obat = data.obatBaru(100);
        List<Integer> resep = siapkanResep(bukaLimaKunjungan(data.poliBaru()), obat, 3);
        masuk("farmasi");

        List<Object> hasil = serentak(i -> {
            new ApotekManager().serahkanObat(resep.get(i));
            return "ok";
        });

        assertEquals(List.of("ok", "ok", "ok", "ok", "ok"), hasil);
        assertEquals(85, angka("SELECT stok FROM obat WHERE id_obat = ?", obat));
        assertEquals(85, angka("SELECT sum(jumlah) FROM bets_obat WHERE id_obat = ?", obat));
    }

    @Test
    void serahkanObat_resepSamaHanyaDiserahkanSekali() throws Exception {
        int obat = data.obatBaru(100);
        int resep = siapkanResep(bukaLimaKunjungan(data.poliBaru()).subList(0, 1), obat, 4).get(0);
        masuk("farmasi");

        List<Object> hasil = serentak(i -> {
            new ApotekManager().serahkanObat(resep);
            return "ok";
        });

        assertEquals(1, hasil.stream().filter("ok"::equals).count(), hasil.toString());
        hasil.stream().filter(h -> !"ok".equals(h)).forEach(h -> assertInstanceOf(IllegalStateException.class, h));
        assertEquals(96, angka("SELECT stok FROM obat WHERE id_obat = ?", obat));
    }

    @Test
    void catatPenerimaan_limaPetugasMenambahStokTanpaLostUpdate() throws Exception {
        int obat = data.obatBaru(10);
        masuk("farmasi");

        List<Object> hasil = serentak(i -> {
            new PersediaanManager().catatPenerimaan(new PenerimaanObatEntity(0, "F-UJI-" + data.penanda, LocalDate.now(),
                    List.of(new BetsObatEntity("B-SERENTAK-" + data.penanda + "-" + i, obat, 5,
                            LocalDate.now().plusYears(1)))));
            return "ok";
        });

        assertEquals(List.of("ok", "ok", "ok", "ok", "ok"), hasil);
        assertEquals(35, angka("SELECT stok FROM obat WHERE id_obat = ?", obat));
        assertEquals(35, angka("SELECT sum(jumlah) FROM bets_obat WHERE id_obat = ?", obat));
    }
}
