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

    public KunjunganEntity ambilTerdepan() {
        throw new UnsupportedOperationException("TODO");
    }

    public void pindahkanKeBelakang(int idEntri) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Menghapus entri dari antrean, misalnya resep yang dikembalikan ke dokter (Algo-C37-01, UC07).
     */
    public void keluarkan(int idEntri) {
        if (!daftarEntri.remove(Integer.valueOf(idEntri))) {
            throw new IllegalArgumentException("Entri tidak ada pada antrean " + jenis);
        }
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
