package sehati.dataaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import sehati.business.entity.BundelFHIREntity;

// Pengujian unit C15 SATUSEHATGateway (HIUPL Subbab 4.2, C15-UT01 sampai C15-UT16).
@DisplayName("C15 SATUSEHATGateway")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class SATUSEHATGatewayTest {

    private HttpServer server;
    private String alamat;

    @BeforeEach
    void mulai() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        alamat = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void hentikan() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("C15-UT01 periksaKoneksi normal: status di bawah 500 dianggap terhubung")
    void periksaKoneksi_trueUntukStatusDiBawah500() {
        server.createContext("/metadata", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        assertTrue(new SATUSEHATGateway(alamat, "token").periksaKoneksi());
    }

    @Test
    @DisplayName("C15-UT02 periksaKoneksi negatif: status 500 ke atas dianggap terputus")
    void periksaKoneksi_falseUntukStatus500() {
        server.createContext("/metadata", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });

        assertFalse(new SATUSEHATGateway(alamat, "token").periksaKoneksi());
    }

    @Test
    @DisplayName("C15-UT03 periksaKoneksi negatif: alamat kosong dianggap terputus")
    void periksaKoneksi_falseSaatAlamatKosong() {
        assertFalse(new SATUSEHATGateway("", "token").periksaKoneksi());
    }

    @Test
    @DisplayName("C15-UT04 periksaKoneksi normal: mode simulasi selalu dianggap terhubung")
    void periksaKoneksi_trueSaatModeSimulasi() {
        assertTrue(new SATUSEHATGateway("", null, true).periksaKoneksi());
    }

    @Test
    @DisplayName("C15-UT05 periksaKoneksi negatif: koneksi terputus tidak melempar pengecualian")
    void periksaKoneksi_falseSaatKoneksiTerputus() {
        server.stop(0);
        server = null;

        assertFalse(new SATUSEHATGateway(alamat, "token").periksaKoneksi());
    }

    @Test
    @DisplayName("C15-UT06 periksaKoneksi batas: garis miring akhir alamat dibuang")
    void periksaKoneksi_garisMiringAkhir() {
        server.createContext("/metadata", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });

        assertTrue(new SATUSEHATGateway(alamat + "/", "token").periksaKoneksi());
    }

    @Test
    @DisplayName("C15-UT07 kirimBundel normal: bundel terkirim dan id dikembalikan")
    void kirimBundel_mengirimBundelDanMengembalikanId() {
        AtomicReference<String> metode = new AtomicReference<>();
        AtomicReference<String> tipe = new AtomicReference<>();
        AtomicReference<String> otorisasi = new AtomicReference<>();
        AtomicReference<String> isi = new AtomicReference<>();
        server.createContext("/", exchange -> {
            metode.set(exchange.getRequestMethod());
            tipe.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            otorisasi.set(exchange.getRequestHeaders().getFirst("Authorization"));
            isi.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] balasan = "{\"resourceType\":\"Bundle\",\"id\":\"bundle-123\"}"
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/fhir+json");
            exchange.sendResponseHeaders(201, balasan.length);
            try (OutputStream keluaran = exchange.getResponseBody()) {
                keluaran.write(balasan);
            }
        });

        ResponsKirim respons = new SATUSEHATGateway(alamat, "token-uji").kirimBundel(bundel());

        assertTrue(respons.berhasil());
        assertEquals("bundle-123", respons.kode());
        assertEquals("POST", metode.get());
        assertEquals("application/fhir+json", tipe.get());
        assertEquals("Bearer token-uji", otorisasi.get());
        assertEquals("{\"resourceType\":\"Bundle\"}", isi.get());
    }

    @Test
    @DisplayName("C15-UT08 kirimBundel alternatif: token diminta ulang sekali saat menerima 401")
    void kirimBundel_memintaTokenBaruSaat401() {
        AtomicInteger panggilan = new AtomicInteger();
        AtomicReference<String> otorisasiKedua = new AtomicReference<>();
        server.createContext("/", exchange -> {
            int urutan = panggilan.incrementAndGet();
            if (urutan == 1) {
                exchange.sendResponseHeaders(401, -1);
                exchange.close();
                return;
            }
            otorisasiKedua.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] balasan = "{\"id\":\"bundle-401\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, balasan.length);
            try (OutputStream keluaran = exchange.getResponseBody()) {
                keluaran.write(balasan);
            }
        });
        GatewayUji gateway = new GatewayUji(alamat, "token-lama");

        ResponsKirim respons = gateway.kirimBundel(bundel());

        assertTrue(respons.berhasil());
        assertEquals("bundle-401", respons.kode());
        assertEquals(1, gateway.tokenDiminta);
        assertEquals("Bearer token-baru", otorisasiKedua.get());
    }

    @Test
    @DisplayName("C15-UT09 kirimBundel negatif: diagnostics OperationOutcome dikembalikan sebagai alasan")
    void kirimBundel_mengembalikanDiagnosticsOperationOutcome() {
        server.createContext("/", exchange -> {
            byte[] balasan = ("{\"resourceType\":\"OperationOutcome\",\"issue\":"
                    + "[{\"diagnostics\":\"NIK tidak valid\"}]}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, balasan.length);
            try (OutputStream keluaran = exchange.getResponseBody()) {
                keluaran.write(balasan);
            }
        });

        ResponsKirim respons = new SATUSEHATGateway(alamat, "token").kirimBundel(bundel());

        assertFalse(respons.berhasil());
        assertEquals("NIK tidak valid", respons.alasan());
    }

    @Test
    @DisplayName("C15-UT10 kirimBundel negatif: koneksi terputus dikembalikan sebagai kegagalan")
    void kirimBundel_koneksiTerputus() {
        server.stop(0);
        server = null;

        ResponsKirim respons = new SATUSEHATGateway(alamat, "token").kirimBundel(bundel());

        assertFalse(respons.berhasil());
        assertEquals("Koneksi ke SATUSEHAT terputus", respons.alasan());
    }

    @Test
    @DisplayName("C15-UT11 kirimBundel negatif: status 500 tanpa body memakai pesan HTTP")
    void kirimBundel_status500TanpaBody() {
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });

        ResponsKirim respons = new SATUSEHATGateway(alamat, "token").kirimBundel(bundel());

        assertFalse(respons.berhasil());
        assertEquals("SATUSEHAT menolak bundel (HTTP 500)", respons.alasan());
    }

    @Test
    @DisplayName("C15-UT12 kirimBundel normal: mode simulasi mengembalikan kode simulasi")
    void kirimBundel_modeSimulasiBerhasil() {
        ResponsKirim respons = new SATUSEHATGateway("", null, true).kirimBundel(bundel());

        assertTrue(respons.berhasil());
        assertEquals("simulasi-7", respons.kode());
    }

    @Test
    @DisplayName("C15-UT13 kirimBundel negatif: bundel null atau isinya kosong ditolak")
    void kirimBundel_menolakBundelKosong() {
        SATUSEHATGateway gateway = new SATUSEHATGateway(alamat, "token");

        assertEquals("Bundel FHIR kosong", gateway.kirimBundel(null).alasan());
        assertEquals("Bundel FHIR kosong",
                gateway.kirimBundel(new BundelFHIREntity(7, 21, null, "tertunda", null)).alasan());
    }

    @Test
    @DisplayName("C15-UT14 kirimBundel negatif: alamat belum diisi ditolak")
    void kirimBundel_menolakAlamatBelumDiisi() {
        ResponsKirim respons = new SATUSEHATGateway("", "token").kirimBundel(bundel());

        assertFalse(respons.berhasil());
        assertEquals("Alamat SATUSEHAT belum diisi", respons.alasan());
    }

    @Test
    @DisplayName("C15-UT15 kirimBundel normal: token kosong diminta dari konfigurasi")
    void kirimBundel_memintaTokenSaatTokenKosong() {
        server.createContext("/", exchange -> {
            byte[] balasan = "{\"id\":\"bundle-tanpa-token\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, balasan.length);
            try (OutputStream keluaran = exchange.getResponseBody()) {
                keluaran.write(balasan);
            }
        });

        ResponsKirim respons = new SATUSEHATGateway(alamat, null).kirimBundel(bundel());

        assertTrue(respons.berhasil());
        assertEquals("bundle-tanpa-token", respons.kode());
    }

    @Test
    @DisplayName("C15-UT16 kirimBundel alternatif: respons 200 tanpa id mengembalikan kode kosong")
    void kirimBundel_kodeKosongBilaTanpaId() {
        server.createContext("/", exchange -> {
            byte[] balasan = "{\"resourceType\":\"Bundle\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, balasan.length);
            try (OutputStream keluaran = exchange.getResponseBody()) {
                keluaran.write(balasan);
            }
        });

        ResponsKirim respons = new SATUSEHATGateway(alamat, "token").kirimBundel(bundel());

        assertTrue(respons.berhasil());
        assertEquals("", respons.kode());
    }

    private static BundelFHIREntity bundel() {
        return new BundelFHIREntity(7, 21, "{\"resourceType\":\"Bundle\"}", "tertunda", null);
    }

    private static final class GatewayUji extends SATUSEHATGateway {
        private int tokenDiminta;

        GatewayUji(String alamat, String token) {
            super(alamat, token);
        }

        @Override
        String mintaToken() {
            tokenDiminta++;
            return "token-baru";
        }
    }
}
