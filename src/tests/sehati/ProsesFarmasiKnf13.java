package sehati;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import sehati.business.control.ApotekManager;
import sehati.business.control.PersediaanManager;
import sehati.business.entity.BetsObatEntity;
import sehati.business.entity.PenerimaanObatEntity;
import sehati.business.entity.SesiEntity;
import sehati.common.SesiAktif;

/**
 * Proses aplikasi terpisah untuk KNF13-TC01 ({@link KnfFarmasiTest}). Dijalankan sebagai JVM anak lalu dihentikan
 * paksa ({@code kill -9}) oleh test saat sedang menyimpan. Setiap langkah dilaporkan lewat satu baris keluaran agar
 * test tahu sampai mana proses berjalan. Koneksi dibuka lewat {@code KoneksiBasisData} dengan akun aplikasi dari
 * {@code src/.env}, sama seperti aplikasi.
 *
 * <pre>
 * serah    &lt;idResep&gt; &lt;faktur&gt; &lt;nomorBets&gt;  catat penerimaan Kaptopril 10 (selesai), lalu serahkan resep
 * terima   &lt;faktur&gt; &lt;nomorBets&gt;            catat penerimaan Kaptopril 10
 * </pre>
 */
public final class ProsesFarmasiKnf13 {

    static final int KAPTOPRIL = 6;
    static final int JUMLAH_TERIMA = 10;

    private ProsesFarmasiKnf13() {
    }

    public static void main(String[] args) {
        SesiAktif.pasang(new SesiEntity(UUID.randomUUID(), 4, LocalDateTime.now(), true), "Petugas Farmasi");
        if ("serah".equals(args[0])) {
            terima(args[2], args[3]);
            System.out.println("PENERIMAAN_TERSIMPAN");
            System.out.println("MULAI_SERAH " + args[1]);
            new ApotekManager().serahkanObat(Integer.parseInt(args[1]));
            System.out.println("SERAH_SELESAI");
        } else {
            System.out.println("MULAI_TERIMA");
            terima(args[1], args[2]);
            System.out.println("TERIMA_SELESAI");
        }
    }

    private static void terima(String faktur, String nomorBets) {
        LocalDate hariIni = LocalDate.now();
        new PersediaanManager().catatPenerimaan(new PenerimaanObatEntity(0, faktur, hariIni, new ArrayList<>(List.of(
                new BetsObatEntity(nomorBets, KAPTOPRIL, JUMLAH_TERIMA, hariIni.plusYears(2))))));
    }
}
