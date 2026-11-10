package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class EntityFarmasiTest {

    private static ObatEntity obat(int stok, int ambang) {
        return new ObatEntity(1, "Kaptopril 25 mg", "tablet", stok, ambang);
    }

    private static BetsObatEntity bets(String nomor, int jumlah, LocalDate kedaluwarsa) {
        return new BetsObatEntity(nomor, 1, jumlah, kedaluwarsa);
    }

    @Test
    void kurangiStok_mengurangiSaldo() {
        ObatEntity o = obat(20, 50);
        o.kurangiStok(20);
        assertEquals(0, o.getStok());
    }

    @Test
    void kurangiStok_menolakMelebihiStokDanJumlahTidakPositif() {
        ObatEntity o = obat(5, 50);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> o.kurangiStok(6));
        assertEquals("Stok Kaptopril 25 mg tidak cukup", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> o.kurangiStok(0));
        assertEquals(5, o.getStok());
    }

    @Test
    void tambahStok_menambahSaldoDanMenolakJumlahTidakPositif() {
        ObatEntity o = obat(20, 50);
        o.tambahStok(300);
        assertEquals(320, o.getStok());
        assertThrows(IllegalArgumentException.class, () -> o.tambahStok(-1));
        assertEquals(320, o.getStok());
    }

    @Test
    void diBawahAmbang_hanyaBilaStokKurangDariAmbang() {
        assertTrue(obat(49, 50).diBawahAmbang());
        assertFalse(obat(50, 50).diBawahAmbang());
    }

    @Test
    void kurangiBets_menolakMelebihiSisa() {
        BetsObatEntity b = bets("KAP-A01", 10, LocalDate.now().plusYears(1));
        b.kurangi(4);
        assertEquals(6, b.getJumlah());
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> b.kurangi(7));
        assertEquals("Sisa bets KAP-A01 hanya 6", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> b.kurangi(0));
    }

    @Test
    void mendekatiKedaluwarsa_batas90HariTermasukYangSudahLewat() {
        LocalDate hariIni = LocalDate.now();
        assertTrue(bets("A", 1, hariIni.plusDays(90)).mendekatiKedaluwarsa(90));
        assertFalse(bets("B", 1, hariIni.plusDays(91)).mendekatiKedaluwarsa(90));
        assertTrue(bets("C", 1, hariIni.minusDays(3)).mendekatiKedaluwarsa(90));
    }

    @Test
    void totalJumlah_menjumlahkanSeluruhBets() {
        LocalDate kedaluwarsa = LocalDate.now().plusYears(1);
        PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FK-1", LocalDate.now(),
                List.of(bets("A", 100, kedaluwarsa), bets("B", 200, kedaluwarsa)));
        assertEquals(300, p.totalJumlah());
        assertEquals(0, new PenerimaanObatEntity().totalJumlah());
    }
}
