package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import sehati.business.entity.BundelFHIREntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;
import sehati.dataaccess.DataSinkronisasiBoundary;
import sehati.dataaccess.ResponsKirim;
import sehati.dataaccess.SATUSEHATGateway;

// Pengujian unit C28 SinkronisasiManager (HIUPL Subbab 4.2, C28-UT01 sampai C28-UT09).
@DisplayName("C28 SinkronisasiManager")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SinkronisasiManagerTest {

    @AfterEach
    void lepasSesi() {
        SesiAktif.lepas();
    }

    @Test
    @DisplayName("C28-UT01 kirimSemua negatif: saat luring bundel ditahan")
    void kirimSemua_menolakSaatLuring() {
        GatewayPalsu gateway = new GatewayPalsu();
        gateway.daring = false;
        SinkronisasiManager manager = new SinkronisasiManager(() -> gateway);
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Petugas Administrasi");

        IllegalStateException galat = assertThrows(IllegalStateException.class, manager::kirimSemua);

        assertEquals("Koneksi internet terputus, bundel ditahan", galat.getMessage());
        assertFalse(manager.isDaring());
        assertTrue(gateway.dikirim.isEmpty());
    }

    @Test
    @DisplayName("C28-UT02 kirimSemua normal: menandai terkirim dan gagal sesuai respons")
    void kirimSemua_menandaiTerkirimDanGagal() {
        GatewayPalsu gateway = new GatewayPalsu();
        gateway.respons.add(new ResponsKirim(true, "id-1", ""));
        gateway.respons.add(new ResponsKirim(false, "", "Ditolak"));
        DataPalsu data = new DataPalsu();
        BundelFHIREntity pertama = bundel(1);
        BundelFHIREntity kedua = bundel(2);
        data.daftar = List.of(pertama, kedua);
        SinkronisasiManager manager = new SinkronisasiManager(() -> gateway);

        List<BundelFHIREntity> hasil = manager.kirimSemua(data, gateway);

        assertEquals(List.of(pertama, kedua), hasil);
        assertEquals("terkirim", pertama.getStatus());
        assertNull(pertama.getAlasanGagal());
        assertEquals("gagal kirim", kedua.getStatus());
        assertEquals("Ditolak", kedua.getAlasanGagal());
        assertEquals(1, manager.getJumlahTertunda());
        assertEquals(List.of(pertama, kedua), data.diperbarui);
    }

    @Test
    @DisplayName("C28-UT03 muatStatus normal: mengisi jumlah tertunda dan status koneksi")
    void muatStatus_mengisiJumlahDanStatusKoneksi() {
        GatewayPalsu gateway = new GatewayPalsu();
        DataPalsu data = new DataPalsu();
        data.daftar = List.of(bundel(1), bundel(2));
        BundelFHIREntity terkirim = bundel(3);
        terkirim.tandaiTerkirim("kode-3");
        data.riwayat = List.of(terkirim);
        SinkronisasiManager manager = new SinkronisasiManager(() -> gateway);

        List<BundelFHIREntity> hasil = manager.muatStatus(data, true);

        // Bundel terkirim ikut tampil sebagai riwayat, tetapi tidak dihitung tertunda.
        assertEquals(3, hasil.size());
        assertEquals("terkirim", hasil.get(2).getStatus());
        assertTrue(manager.isDaring());
        assertEquals(2, manager.getJumlahTertunda());
    }

    @Test
    @DisplayName("C28-UT04 muatStatus negatif: luring mengosongkan jumlah tertunda")
    void muatStatus_luring() {
        GatewayPalsu gateway = new GatewayPalsu();
        DataPalsu data = new DataPalsu();
        SinkronisasiManager manager = new SinkronisasiManager(() -> gateway);

        manager.muatStatus(data, false);

        assertFalse(manager.isDaring());
        assertEquals(0, manager.getJumlahTertunda());
    }

    @Test
    @DisplayName("C28-UT05 eksporBundel negatif: tidak ada bundel tertunda ditolak")
    void eksporBundel_kosongDitolak() {
        SinkronisasiManager manager = new SinkronisasiManager(GatewayPalsu::new);
        DataPalsu data = new DataPalsu();

        IllegalStateException galat = assertThrows(IllegalStateException.class,
                () -> manager.eksporBundel("/tmp", data));

        assertEquals("Tidak ada bundel tertunda untuk diekspor", galat.getMessage());
    }

    @Test
    @DisplayName("C28-UT06 eksporBundel normal: mengembalikan lokasi arsip")
    void eksporBundel_mengembalikanLokasi() {
        SinkronisasiManager manager = new SinkronisasiManager(GatewayPalsu::new);
        DataPalsu data = new DataPalsu();
        data.daftar = List.of(bundel(1));

        String lokasi = manager.eksporBundel("/tmp", data);

        assertEquals("/tmp/arsip.enc", lokasi);
    }

    @Test
    @DisplayName("C28-UT07 periksaKoneksi normal: hasil pemeriksaan tersimpan sebagai status daring")
    void periksaKoneksi_melaporkanStatusGateway() {
        SinkronisasiManager manager = new SinkronisasiManager();

        boolean hasil = manager.periksaKoneksi();

        assertEquals(hasil, manager.isDaring());
    }

    private static BundelFHIREntity bundel(int id) {
        return new BundelFHIREntity(id, id + 19, "{}", "tertunda", null);
    }

    private static final class GatewayPalsu extends SATUSEHATGateway {
        private boolean daring = true;
        private final List<ResponsKirim> respons = new ArrayList<>();
        private final List<BundelFHIREntity> dikirim = new ArrayList<>();
        private int indeks;

        GatewayPalsu() {
            super("http://uji", "token");
        }

        @Override
        public boolean periksaKoneksi() {
            return daring;
        }

        @Override
        public ResponsKirim kirimBundel(BundelFHIREntity bundel) {
            dikirim.add(bundel);
            if (respons.isEmpty()) {
                return new ResponsKirim(true, "ok", "");
            }
            int posisi = Math.min(indeks++, respons.size() - 1);
            return respons.get(posisi);
        }
    }

    private static final class DataPalsu extends DataSinkronisasiBoundary {
        private List<BundelFHIREntity> daftar = new ArrayList<>();
        private List<BundelFHIREntity> riwayat = new ArrayList<>();
        private final List<BundelFHIREntity> diperbarui = new ArrayList<>();

        DataPalsu() {
            super(null);
        }

        @Override
        public List<BundelFHIREntity> ambilTertunda() {
            return daftar;
        }

        @Override
        public List<BundelFHIREntity> ambilTerkirimTerakhir(int batas) {
            return riwayat;
        }

        @Override
        public void perbaruiBundel(BundelFHIREntity bundel) {
            diperbarui.add(bundel);
        }

        @Override
        public String tulisArsipTerenkripsi(List<BundelFHIREntity> daftar, String lokasi) {
            return lokasi + "/arsip.enc";
        }
    }

    // Uji jalur publik yang membuka koneksi basis data. Aktif bila DB_UJI_URL diisi.
    @Nested
    @EnabledIfEnvironmentVariable(named = "DB_UJI_URL", matches = ".+")
    class DenganBasisData {

        @AfterEach
        void lepasSesi() {
            SesiAktif.lepas();
        }

        @Test
        @DisplayName("C28-UT08 muatStatus normal: membuka koneksi basis data dan memuat status")
        void muatStatus_membukaKoneksiBasisData() {
            SinkronisasiManager manager = new SinkronisasiManager();

            List<BundelFHIREntity> daftar = manager.muatStatus();

            assertNotNull(daftar);
            assertTrue(manager.isDaring());
            assertTrue(manager.getJumlahTertunda() >= 0);
        }

        @Test
        @DisplayName("C28-UT09 eksporBundel normal: menulis arsip terenkripsi dari basis data")
        void eksporBundel_menulisArsipDariBasisData() throws IOException {
            SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 1, LocalDateTime.now(), true), "Petugas Administrasi");
            Path folder = Files.createTempDirectory("sinkronisasi-arsip");
            try {
                String lokasi = new SinkronisasiManager().eksporBundel(folder.toString());

                assertTrue(Files.isRegularFile(Path.of(lokasi)));
                assertTrue(Path.of(lokasi).getFileName().toString().endsWith(".enc"));
            } finally {
                hapus(folder);
            }
        }

        private void hapus(Path p) throws IOException {
            try (var aliran = Files.walk(p)) {
                aliran.sorted(java.util.Comparator.reverseOrder()).forEach(f -> {
                    try {
                        Files.deleteIfExists(f);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
