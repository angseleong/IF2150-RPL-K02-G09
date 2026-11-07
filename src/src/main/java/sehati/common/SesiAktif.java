package sehati.common;

import sehati.business.entity.SesiEntity;

/**
 * Menyimpan SesiEntity yang dibuka AutentikasiManager di komputer ini, agar setiap Manager dapat mengambil
 * {@code idPenggunaSesi} sebagai pelaku jejak audit (DPPL kesepakatan 1 BAB 5).
 */
public final class SesiAktif {

    // volatile karena Manager dapat dipanggil dari Task di luar JavaFX Application Thread.
    private static volatile SesiEntity sesi;

    private SesiAktif() {
    }

    public static void pasang(SesiEntity s) {
        sesi = s;
    }

    public static void lepas() {
        sesi = null;
    }

    /**
     * Mengembalikan sesi yang sedang aktif, atau null bila belum ada pengguna yang masuk.
     */
    public static SesiEntity ambil() {
        SesiEntity s = sesi;
        return s != null && s.isStatusAktif() ? s : null;
    }

    /**
     * Mengembalikan idPengguna milik sesi aktif. Melempar IllegalStateException bila belum ada pengguna yang masuk.
     */
    public static int idPenggunaSesi() {
        SesiEntity s = ambil();
        if (s == null) {
            throw new IllegalStateException("Sesi sudah berakhir, silakan masuk kembali");
        }
        return s.getIdPengguna();
    }
}
