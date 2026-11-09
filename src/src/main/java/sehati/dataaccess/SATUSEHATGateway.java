package sehati.dataaccess;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sehati.business.entity.BundelFHIREntity;
import sehati.common.PengaturanAplikasi;

/**
 * C15 - SATUSEHATGateway.
 *
 * Memeriksa koneksi internet dan mengirim bundel ke REST API sandbox SATUSEHAT, lalu mengembalikan status
 * pengiriman ke SinkronisasiManager.
 *
 * <p>Bila {@code SATUSEHAT_URL} kosong, gateway berjalan dalam mode simulasi. Mode ini dipakai sebagai bawaan
 * supaya aplikasi bisa dicoba tanpa akun SATUSEHAT. Perilaku ini dicatat sebagai perubahan dari DPPL.
 */
public class SATUSEHATGateway {

    private static final Duration BATAS_KONEKSI = Duration.ofSeconds(3);
    private static final Duration BATAS_KIRIM = Duration.ofSeconds(30);

    private final String alamatLayanan;
    private final boolean simulasi;
    private String token;

    /**
     * Gateway untuk pemakaian aplikasi. Mode simulasi aktif bila {@code SATUSEHAT_URL} tidak diisi.
     */
    public SATUSEHATGateway() {
        PengaturanAplikasi pengaturan = PengaturanAplikasi.muat();
        this.alamatLayanan = pengaturan.ambil("SATUSEHAT_URL", "");
        this.token = pengaturan.ambil("SATUSEHAT_TOKEN", null);
        this.simulasi = alamatLayanan.isBlank();
    }

    /**
     * Gateway untuk alamat dan token tertentu. Tidak pernah masuk mode simulasi.
     */
    public SATUSEHATGateway(String alamatLayanan, String token) {
        this(alamatLayanan, token, false);
    }

    SATUSEHATGateway(String alamatLayanan, String token, boolean simulasi) {
        this.alamatLayanan = alamatLayanan;
        this.token = token;
        this.simulasi = simulasi;
    }

    /**
     * Algo-C15-01: GET {@code /metadata} dengan batas 3 detik. Status di bawah 500 dianggap terhubung.
     * Kegagalan jaringan tidak pernah dilempar sebagai exception.
     */
    public boolean periksaKoneksi() {
        if (simulasi) {
            return true;
        }
        if (alamatLayanan == null || alamatLayanan.isBlank()) {
            return false;
        }
        try {
            HttpClient klien = HttpClient.newBuilder().connectTimeout(BATAS_KONEKSI).build();
            HttpRequest permintaan = HttpRequest.newBuilder(URI.create(alamat("/metadata")))
                    .timeout(BATAS_KONEKSI)
                    .GET()
                    .build();
            HttpResponse<Void> respons = klien.send(permintaan, HttpResponse.BodyHandlers.discarding());
            return respons.statusCode() < 500;
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    /**
     * Algo-C15-02: kirim satu bundel. Token diambil ulang sekali bila menerima 401. Kegagalan jaringan
     * dikembalikan sebagai {@link ResponsKirim} gagal, bukan exception.
     */
    public ResponsKirim kirimBundel(BundelFHIREntity bundel) {
        if (bundel == null || bundel.getIsiBundel() == null) {
            return new ResponsKirim(false, "", "Bundel FHIR kosong");
        }
        if (simulasi) {
            return new ResponsKirim(true, "simulasi-" + bundel.getIdBundel(), "");
        }
        if (alamatLayanan == null || alamatLayanan.isBlank()) {
            return new ResponsKirim(false, "", "Alamat SATUSEHAT belum diisi");
        }
        if (token == null || token.isBlank()) {
            token = mintaToken();
        }

        for (int percobaan = 1; percobaan <= 2; percobaan++) {
            try {
                HttpClient klien = HttpClient.newBuilder().connectTimeout(BATAS_KIRIM).build();
                HttpRequest permintaan = HttpRequest.newBuilder(URI.create(alamatLayanan))
                        .timeout(BATAS_KIRIM)
                        .header("Content-Type", "application/fhir+json")
                        .header("Authorization", "Bearer " + (token == null ? "" : token))
                        .POST(HttpRequest.BodyPublishers.ofString(bundel.getIsiBundel(), StandardCharsets.UTF_8))
                        .build();
                HttpResponse<String> respons = klien.send(permintaan,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                int status = respons.statusCode();

                if (status == 401 && percobaan == 1) {
                    token = mintaToken();
                    continue;
                }
                if (status == 200 || status == 201) {
                    String kode = cariNilai(respons.body(), "id");
                    return new ResponsKirim(true, kode == null ? "" : kode, "");
                }
                String alasan = cariNilai(respons.body(), "diagnostics");
                if (alasan == null || alasan.isBlank()) {
                    alasan = "SATUSEHAT menolak bundel (HTTP " + status + ")";
                }
                return new ResponsKirim(false, "", alasan);
            } catch (IOException | InterruptedException | IllegalArgumentException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                return new ResponsKirim(false, "", "Koneksi ke SATUSEHAT terputus");
            }
        }
        return new ResponsKirim(false, "", "Koneksi ke SATUSEHAT terputus");
    }

    /**
     * Mengambil token dari konfigurasi. Dipisah agar bisa diganti pada pengujian.
     */
    String mintaToken() {
        return PengaturanAplikasi.muat().ambil("SATUSEHAT_TOKEN", null);
    }

    private String alamat(String jalur) {
        String dasar = alamatLayanan.endsWith("/")
                ? alamatLayanan.substring(0, alamatLayanan.length() - 1)
                : alamatLayanan;
        return dasar + jalur;
    }

    private static String cariNilai(String json, String kunci) {
        if (json == null || json.isBlank()) {
            return null;
        }
        Matcher pencocok = Pattern.compile("\"" + Pattern.quote(kunci) + "\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(json);
        return pencocok.find() ? pencocok.group(1) : null;
    }
}
