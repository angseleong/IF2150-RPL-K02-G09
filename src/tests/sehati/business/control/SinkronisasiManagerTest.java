package sehati.business.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import sehati.business.entity.BundelFHIREntity;
import sehati.dataaccess.DataSinkronisasiBoundary;
import sehati.dataaccess.ResponsKirim;
import sehati.dataaccess.SATUSEHATGateway;

class SinkronisasiManagerTest {

    @Test
    void kirimSemua_menolakSaatLuring() {
        GatewayPalsu gateway = new GatewayPalsu();
        gateway.daring = false;
        SinkronisasiManager manager = new SinkronisasiManager(() -> gateway);

        IllegalStateException galat = assertThrows(IllegalStateException.class, manager::kirimSemua);

        assertEquals("Koneksi internet terputus, bundel ditahan", galat.getMessage());
        assertFalse(manager.isDaring());
        assertTrue(gateway.dikirim.isEmpty());
    }

    @Test
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
    void muatStatus_mengisiJumlahDanStatusKoneksi() {
        GatewayPalsu gateway = new GatewayPalsu();
        DataPalsu data = new DataPalsu();
        data.daftar = List.of(bundel(1), bundel(2));
        SinkronisasiManager manager = new SinkronisasiManager(() -> gateway);

        List<BundelFHIREntity> hasil = manager.muatStatus(data, true);

        assertEquals(2, hasil.size());
        assertTrue(manager.isDaring());
        assertEquals(2, manager.getJumlahTertunda());
    }

    @Test
    void muatStatus_luring() {
        GatewayPalsu gateway = new GatewayPalsu();
        DataPalsu data = new DataPalsu();
        SinkronisasiManager manager = new SinkronisasiManager(() -> gateway);

        manager.muatStatus(data, false);

        assertFalse(manager.isDaring());
        assertEquals(0, manager.getJumlahTertunda());
    }

    @Test
    void eksporBundel_kosongDitolak() {
        SinkronisasiManager manager = new SinkronisasiManager(GatewayPalsu::new);
        DataPalsu data = new DataPalsu();

        IllegalStateException galat = assertThrows(IllegalStateException.class,
                () -> manager.eksporBundel("/tmp", data));

        assertEquals("Tidak ada bundel tertunda untuk diekspor", galat.getMessage());
    }

    @Test
    void eksporBundel_mengembalikanLokasi() {
        SinkronisasiManager manager = new SinkronisasiManager(GatewayPalsu::new);
        DataPalsu data = new DataPalsu();
        data.daftar = List.of(bundel(1));

        String lokasi = manager.eksporBundel("/tmp", data);

        assertEquals("/tmp/arsip.enc", lokasi);
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
        private final List<BundelFHIREntity> diperbarui = new ArrayList<>();

        DataPalsu() {
            super(null);
        }

        @Override
        public List<BundelFHIREntity> ambilTertunda() {
            return daftar;
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
}
