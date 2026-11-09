package sehati.business.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * C37 - AntreanEntity.
 *
 * Merepresentasikan daftar urut pada titik layanan skrining, poli, atau apotek.
 */
public class AntreanEntity {

    private int idAntrean;
    private String jenis;
    private List<Integer> daftarEntri = new ArrayList<>();

    public AntreanEntity() {
    }

    public AntreanEntity(int idAntrean, String jenis, List<Integer> daftarEntri) {
        this.idAntrean = idAntrean;
        this.jenis = jenis;
        this.daftarEntri = daftarEntri;
    }

    /**
     * Menambah entri ke akhir antrean. Entri yang sudah ada diabaikan agar permintaan ulang tidak tampil dua kali
     * (Algo-C37-01).
     */
    public void tambahkan(int idEntri) {
        if (!daftarEntri.contains(idEntri)) {
            daftarEntri.add(idEntri);
        }
    }

    /**
     * Mengembalikan kunjungan yang hanya {@code idKunjungan}-nya terisi, atau null bila antrean kosong
     * (Algo-C37-01).
     */
    public KunjunganEntity ambilTerdepan() {
        if (daftarEntri.isEmpty()) {
            return null;
        }
        KunjunganEntity k = new KunjunganEntity();
        k.setIdKunjungan(daftarEntri.get(0));
        return k;
    }

    /**
     * Memindahkan entri ke akhir antrean (Algo-C37-01).
     */
    public void pindahkanKeBelakang(int idEntri) {
        keluarkan(idEntri);
        daftarEntri.add(idEntri);
    }

    /**
     * Menghapus entri dari antrean; entri yang tidak ada ditolak (Algo-C37-01).
     */
    public void keluarkan(int idEntri) {
        if (!daftarEntri.contains(idEntri)) {
            throw new IllegalArgumentException("Entri tidak ada pada antrean " + jenis);
        }
        daftarEntri.remove(Integer.valueOf(idEntri));
    }

    public int getIdAntrean() {
        return idAntrean;
    }

    public void setIdAntrean(int idAntrean) {
        this.idAntrean = idAntrean;
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }

    public List<Integer> getDaftarEntri() {
        return daftarEntri;
    }

    public void setDaftarEntri(List<Integer> daftarEntri) {
        this.daftarEntri = daftarEntri;
    }
}
