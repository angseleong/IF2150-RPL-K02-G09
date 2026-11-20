package sehati.uji;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import sehati.business.control.AutentikasiManager;
import sehati.business.control.KunjunganManager;
import sehati.business.control.PasienManager;
import sehati.business.control.PersediaanManager;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.KunjunganEntity;
import sehati.business.entity.PasienEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.common.SesiAktif;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KonkurensiTest {

    private static final String SANDI = "sehati123";

    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        private AutentikasiManager auth = new AutentikasiManager();
        private PasienManager pasienManager = new PasienManager();
        private KunjunganManager kunjunganManager = new KunjunganManager();
        private PersediaanManager persediaanManager = new PersediaanManager();

        private List<Integer> idPasienList = new ArrayList<>();

        @BeforeEach
        void setup() {
            SesiAktif.pasang(auth.autentikasi("admin", SANDI));
            // daftar 5 pasien unik
            for (int i = 0; i < 5; i++) {
                String nik = "9998" + String.format("%012d", (System.currentTimeMillis() + i) % 1_000_000_000_000L);
                PasienEntity p = pasienManager.daftarkanPasien(new PasienEntity(0, nik, "RMK" + i,
                        "Pasien Konk " + i, LocalDate.of(1990, 1, 1), "Laki-laki", "Jl", "0812"));
                idPasienList.add(p.getIdPasien());
            }
            SesiAktif.lepas();
        }

        @AfterEach
        void bersihkan() {
            SesiAktif.lepas();
        }

        @Test
        void nomorAntreanTidakGanda() throws Exception {
            int n = 5;
            ExecutorService pool = Executors.newFixedThreadPool(n);
            CountDownLatch mulai = new CountDownLatch(1);
            CountDownLatch selesai = new CountDownLatch(n);
            List<Integer> hasil = java.util.Collections.synchronizedList(new ArrayList<>());
            for (int i = 0; i < n; i++) {
                final int idx = i;
                pool.submit(() -> {
                    try {
                        mulai.await();
                        SesiAktif.pasang(auth.autentikasi("admin", SANDI));
                        KunjunganEntity k = kunjunganManager.bukaKunjungan(idPasienList.get(idx), 1);
                        hasil.add(k.getNomorAntrean());
                    } catch (Exception e) {
                        // abaikan: dicatat lewat ukuran hasil
                    } finally {
                        selesai.countDown();
                    }
                });
            }
            mulai.countDown();
            selesai.await();
            pool.shutdown();

            assertEquals(n, hasil.size(), "nomor antrean harus lengkap: " + hasil);
            assertEquals(n, new java.util.HashSet<>(hasil).size(), "nomor antrean harus unik: " + hasil);
        }

        @Test
        void stokTidakLostUpdate() throws Exception {
            // stok awal obat 1
            int stokAwal;
            try (java.sql.Connection k = java.sql.DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
                try (java.sql.PreparedStatement ps = k.prepareStatement("SELECT stok FROM obat WHERE id_obat = 1")) {
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        stokAwal = rs.getInt("stok");
                    }
                }
            }

            int n = 5;
            ExecutorService pool = Executors.newFixedThreadPool(n);
            CountDownLatch mulai = new CountDownLatch(1);
            CountDownLatch selesai = new CountDownLatch(n);
            AtomicInteger gagal = new AtomicInteger();

            for (int i = 0; i < n; i++) {
                final int idx = i;
                pool.submit(() -> {
                    try {
                        mulai.await();
                        SesiAktif.pasang(auth.autentikasi("farmasi", SANDI));
                        PenerimaanObatEntity p = new PenerimaanObatEntity();
                        p.setNomorFaktur("FKT-KONK-" + idx + "-" + System.currentTimeMillis());
                        p.setTanggalTerima(LocalDate.now());
                        BetsObatEntity bet = new BetsObatEntity("BET-KONK-" + idx + "-" + System.nanoTime(), 1, 5,
                                LocalDate.now().plusDays(365));
                        p.getDaftarBets().add(bet);
                        persediaanManager.catatPenerimaan(p);
                    } catch (Exception e) {
                        gagal.incrementAndGet();
                    } finally {
                        selesai.countDown();
                    }
                });
            }
            mulai.countDown();
            selesai.await();
            pool.shutdown();

            assertEquals(0, gagal.get(), "tidak boleh ada penerimaan yang gagal");

            int stokAkhir;
            try (java.sql.Connection k = java.sql.DriverManager.getConnection(System.getenv("DB_UJI_URL"))) {
                try (java.sql.PreparedStatement ps = k.prepareStatement("SELECT stok FROM obat WHERE id_obat = 1")) {
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        stokAkhir = rs.getInt("stok");
                    }
                }
            }
            assertEquals(stokAwal + 25, stokAkhir, "stok harus bertambah tepat 25 tanpa lost update");
        }
    }
}
