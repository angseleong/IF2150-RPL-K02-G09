package sehati.dataaccess;

/**
 * Objek nilai hasil {@link SATUSEHATGateway#kirimBundel}. Tidak diberi ID kelas karena tanpa perilaku (DPPL 5.15).
 */
public record ResponsKirim(boolean berhasil, String kode, String alasan) {
}
