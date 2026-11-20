package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Pengujian unit entity farmasi (HIUPL Subbab 4.2): C48 ObatEntity, C49 BetsObatEntity, dan C50
 * PenerimaanObatEntity. Seluruhnya tanpa basis data. Setiap test diberi {@code @DisplayName} berawalan ID test case.
 */
@DisplayName("Entity farmasi")
@TestClassOrder(ClassOrderer.DisplayName.class)
class EntityFarmasiTest {

    private static final LocalDate HARI_INI = LocalDate.now();

    private static ObatEntity obat(int stok, int ambang) {
        return new ObatEntity(1, "Kaptopril 25 mg", "tablet", stok, ambang);
    }

    private static BetsObatEntity bets(String nomor, int jumlah, LocalDate kedaluwarsa) {
        return new BetsObatEntity(nomor, 1, jumlah, kedaluwarsa);
    }

    @Nested
    @DisplayName("C48 ObatEntity")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    class Obat {

        @Test
        @DisplayName("C48-UT01 kurangiStok normal: stok 20 dikurangi 5 menjadi 15")
        void kurangiStok_normal() {
            ObatEntity o = obat(20, 50);
            o.kurangiStok(5);
            assertEquals(15, o.getStok());
        }

        @Test
        @DisplayName("C48-UT02 kurangiStok batas: jumlah sama dengan stok menghabiskan stok menjadi 0")
        void kurangiStok_samaDenganStok() {
            ObatEntity o = obat(20, 50);
            o.kurangiStok(20);
            assertEquals(0, o.getStok());
        }

        @Test
        @DisplayName("C48-UT03 kurangiStok negatif: jumlah melebihi stok ditolak dan stok tidak berubah")
        void kurangiStok_melebihiStok() {
            ObatEntity o = obat(5, 50);
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> o.kurangiStok(6));
            assertEquals("Stok Kaptopril 25 mg tidak cukup", e.getMessage());
            assertEquals(5, o.getStok());
        }

        @Test
        @DisplayName("C48-UT04 kurangiStok negatif: jumlah nol dan negatif ditolak")
        void kurangiStok_tidakPositif() {
            ObatEntity o = obat(5, 50);
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> o.kurangiStok(0));
            assertEquals("Jumlah pengurangan harus lebih dari nol", e.getMessage());
            assertThrows(IllegalArgumentException.class, () -> o.kurangiStok(-1));
            assertEquals(5, o.getStok());
        }

        @Test
        @DisplayName("C48-UT05 tambahStok normal: stok 20 ditambah 300 menjadi 320")
        void tambahStok_normal() {
            ObatEntity o = obat(20, 50);
            o.tambahStok(300);
            assertEquals(320, o.getStok());
        }

        @Test
        @DisplayName("C48-UT06 tambahStok batas: stok 0 ditambah jumlah terkecil 1")
        void tambahStok_jumlahSatu() {
            ObatEntity o = obat(0, 50);
            o.tambahStok(1);
            assertEquals(1, o.getStok());
        }

        @Test
        @DisplayName("C48-UT07 tambahStok negatif: jumlah nol dan negatif ditolak dan stok tidak berubah")
        void tambahStok_tidakPositif() {
            ObatEntity o = obat(20, 50);
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> o.tambahStok(0));
            assertEquals("Jumlah penambahan harus lebih dari nol", e.getMessage());
            assertThrows(IllegalArgumentException.class, () -> o.tambahStok(-1));
            assertEquals(20, o.getStok());
        }

        @Test
        @DisplayName("C48-UT08 diBawahAmbang normal: stok 20 dengan ambang 50 bernilai true")
        void diBawahAmbang_normal() {
            assertTrue(obat(20, 50).diBawahAmbang());
        }

        @Test
        @DisplayName("C48-UT09 diBawahAmbang batas: stok 49 true, stok tepat 50 false")
        void diBawahAmbang_batas() {
            assertTrue(obat(49, 50).diBawahAmbang());
            assertFalse(obat(50, 50).diBawahAmbang());
        }

        @Test
        @DisplayName("C48-UT10 diBawahAmbang negatif: stok di atas ambang dan ambang 0 bernilai false")
        void diBawahAmbang_tidakDiBawah() {
            assertFalse(obat(300, 100).diBawahAmbang());
            assertFalse(obat(0, 0).diBawahAmbang());
        }
    }

    @Nested
    @DisplayName("C49 BetsObatEntity")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    class Bets {

        @Test
        @DisplayName("C49-UT01 kurangi normal: sisa 10 diambil 4 menjadi 6")
        void kurangi_normal() {
            BetsObatEntity b = bets("KAP-A01", 10, HARI_INI.plusYears(1));
            b.kurangi(4);
            assertEquals(6, b.getJumlah());
        }

        @Test
        @DisplayName("C49-UT02 kurangi batas: seluruh sisa diambil sehingga bets habis")
        void kurangi_seluruhSisa() {
            BetsObatEntity b = bets("KAP-A01", 10, HARI_INI.plusYears(1));
            b.kurangi(10);
            assertEquals(0, b.getJumlah());
        }

        @Test
        @DisplayName("C49-UT03 kurangi negatif: pengambilan melebihi sisa ditolak dan sisa tidak berubah")
        void kurangi_melebihiSisa() {
            BetsObatEntity b = bets("KAP-A01", 6, HARI_INI.plusYears(1));
            IllegalStateException e = assertThrows(IllegalStateException.class, () -> b.kurangi(7));
            assertEquals("Sisa bets KAP-A01 hanya 6", e.getMessage());
            assertEquals(6, b.getJumlah());
        }

        @Test
        @DisplayName("C49-UT04 kurangi negatif: jumlah nol dan negatif ditolak")
        void kurangi_tidakPositif() {
            BetsObatEntity b = bets("KAP-A01", 6, HARI_INI.plusYears(1));
            IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> b.kurangi(0));
            assertEquals("Jumlah pengambilan harus lebih dari nol", e.getMessage());
            assertThrows(IllegalArgumentException.class, () -> b.kurangi(-2));
            assertEquals(6, b.getJumlah());
        }

        @Test
        @DisplayName("C49-UT05 mendekatiKedaluwarsa normal: 30 hari lagi true, 400 hari lagi false")
        void mendekatiKedaluwarsa_normal() {
            assertTrue(bets("A", 1, HARI_INI.plusDays(30)).mendekatiKedaluwarsa(90));
            assertFalse(bets("B", 1, HARI_INI.plusDays(400)).mendekatiKedaluwarsa(90));
        }

        @Test
        @DisplayName("C49-UT06 mendekatiKedaluwarsa batas: tepat 90 hari true, 91 hari false")
        void mendekatiKedaluwarsa_batas90Hari() {
            assertTrue(bets("A", 1, HARI_INI.plusDays(90)).mendekatiKedaluwarsa(90));
            assertFalse(bets("B", 1, HARI_INI.plusDays(91)).mendekatiKedaluwarsa(90));
        }

        @Test
        @DisplayName("C49-UT07 mendekatiKedaluwarsa negatif: bets yang sudah lewat tanggalnya tetap diperingatkan")
        void mendekatiKedaluwarsa_sudahLewat() {
            assertTrue(bets("C", 1, HARI_INI.minusDays(3)).mendekatiKedaluwarsa(90));
            assertTrue(bets("D", 1, HARI_INI.minusYears(1)).mendekatiKedaluwarsa(0));
        }

        @Test
        @DisplayName("C49-UT08 sudahKedaluwarsa normal: bets kedaluwarsa kemarin bernilai true")
        void sudahKedaluwarsa_normal() {
            assertTrue(bets("A", 1, HARI_INI.minusDays(1)).sudahKedaluwarsa());
        }

        @Test
        @DisplayName("C49-UT09 sudahKedaluwarsa batas: bets kedaluwarsa hari ini masih boleh dipakai")
        void sudahKedaluwarsa_hariIni() {
            assertFalse(bets("A", 1, HARI_INI).sudahKedaluwarsa());
        }

        @Test
        @DisplayName("C49-UT10 sudahKedaluwarsa negatif: tanggal kosong dan tanggal besok bernilai false")
        void sudahKedaluwarsa_tanggalKosong() {
            assertFalse(bets("A", 1, null).sudahKedaluwarsa());
            assertFalse(bets("B", 1, HARI_INI.plusDays(1)).sudahKedaluwarsa());
        }

        @Test
        @DisplayName("C49-UT11 jumlahLayak normal: bets kedaluwarsa tidak ikut dijumlahkan")
        void jumlahLayak_normal() {
            assertEquals(30, BetsObatEntity.jumlahLayak(List.of(bets("A", 20, HARI_INI.minusDays(1)),
                    bets("B", 10, HARI_INI.plusDays(30)), bets("C", 20, HARI_INI.plusDays(400)))));
        }

        @Test
        @DisplayName("C49-UT12 jumlahLayak batas: bets yang kedaluwarsa hari ini masih dihitung")
        void jumlahLayak_hariIni() {
            assertEquals(15, BetsObatEntity.jumlahLayak(List.of(bets("A", 15, HARI_INI))));
        }

        @Test
        @DisplayName("C49-UT13 jumlahLayak negatif: daftar kosong dan seluruh bets kedaluwarsa menghasilkan 0")
        void jumlahLayak_kosong() {
            assertEquals(0, BetsObatEntity.jumlahLayak(List.of()));
            assertEquals(0, BetsObatEntity.jumlahLayak(List.of(bets("A", 40, HARI_INI.minusDays(1)))));
        }
    }

    @Nested
    @DisplayName("C50 PenerimaanObatEntity")
    @TestMethodOrder(MethodOrderer.DisplayName.class)
    class Penerimaan {

        @Test
        @DisplayName("C50-UT01 totalJumlah normal: bets 100 dan 200 berjumlah 300")
        void totalJumlah_normal() {
            LocalDate kedaluwarsa = HARI_INI.plusYears(1);
            PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FK-1", HARI_INI,
                    List.of(bets("A", 100, kedaluwarsa), bets("B", 200, kedaluwarsa)));
            assertEquals(300, p.totalJumlah());
        }

        @Test
        @DisplayName("C50-UT02 totalJumlah batas: satu bets berjumlah 1")
        void totalJumlah_satuBets() {
            PenerimaanObatEntity p = new PenerimaanObatEntity(0, "FK-1", HARI_INI,
                    new ArrayList<>(List.of(bets("A", 1, HARI_INI.plusYears(1)))));
            assertEquals(1, p.totalJumlah());
        }

        @Test
        @DisplayName("C50-UT03 totalJumlah negatif: penerimaan tanpa bets berjumlah 0")
        void totalJumlah_tanpaBets() {
            assertEquals(0, new PenerimaanObatEntity().totalJumlah());
            assertEquals(0, new PenerimaanObatEntity(0, "FK-1", HARI_INI, new ArrayList<>()).totalJumlah());
        }
    }
}
