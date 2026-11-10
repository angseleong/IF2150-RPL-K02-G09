package sehati.business.control;

import sehati.business.entity.AntreanEntity;
import sehati.business.entity.ItemResepEntity;
import sehati.business.entity.ObatEntity;
import sehati.business.entity.ResepEntity;
import sehati.common.KoneksiBasisData;
import sehati.dataaccess.DataFarmasiBoundary;
import sehati.dataaccess.DataKunjunganBoundary;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * C23 - ResepManager.
 *
 * Menyusun item resep, memeriksa kecukupan stok, dan meneruskan resep yang lolos ke antrean apotek.
 */
public class ResepManager {

    private static final Logger LOG = Logger.getLogger(ResepManager.class.getName());

    /**
     * Seluruh obat data master beserta saldo stoknya untuk pencarian obat pada ResepForm (KF11).
     */
    public List<ObatEntity> muatDataObat() {
        try (Connection koneksi = KoneksiBasisData.buka()) {
            return new DataFarmasiBoundary(koneksi).ambilSemuaObat();
        } catch (SQLException e) {
            throw galatTeknis("Data obat gagal dimuat", e);
        }
    }

    /**
     * Membuat satu item resep untuk obat yang terdaftar (Algo-C23-01). Kecukupan stok dinilai ResepForm dari total
     * seluruh item dan diperiksa ulang oleh kirimKeApotek().
     */
    public ItemResepEntity tambahItem(int idObat, int jumlah, String dosis, String aturanPakai) {
        if (jumlah <= 0) {
            throw new IllegalArgumentException("Jumlah obat harus lebih dari nol");
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);
            if (dataFarmasi.ambilObat(idObat) == null) {
                throw new IllegalArgumentException("Obat tidak ada pada data master");
            }
            return new ItemResepEntity(0, idObat, dosis, jumlah, aturanPakai);
        } catch (SQLException e) {
            throw galatTeknis("Item resep gagal ditambahkan", e);
        }
    }

    // Boundary ikut diteruskan agar satu operasi memakai satu koneksi (aturan 1 Subbab 5.56).
    static boolean validasiStok(ItemResepEntity item, DataFarmasiBoundary dataFarmasi) {
        ObatEntity obat = dataFarmasi.ambilObat(item.getIdObat());
        return obat != null && item.cukupStok(obat.getStok());
    }

    /**
     * Memeriksa ulang stok seluruh item, lalu menyimpan resep "menunggu" dan memasukkannya ke antrean apotek dalam
     * satu transaksi (Algo-C23-02).
     */
    public ResepEntity kirimKeApotek(int idKunjungan, List<ItemResepEntity> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Resep belum berisi obat");
        }
        try (Connection koneksi = KoneksiBasisData.buka()) {
            DataFarmasiBoundary dataFarmasi = new DataFarmasiBoundary(koneksi);
            DataKunjunganBoundary dataKunjungan = new DataKunjunganBoundary(koneksi);
            List<String> kurang = new ArrayList<>();
            // Obat yang sama pada beberapa baris dijumlahkan, agar totalnya tidak lolos dari pemeriksaan per baris.
            Map<Integer, Integer> totalPerObat = new LinkedHashMap<>();
            items.forEach(item -> totalPerObat.merge(item.getIdObat(), item.getJumlah(), Integer::sum));
            for (Map.Entry<Integer, Integer> total : totalPerObat.entrySet()) {
                ItemResepEntity gabungan = new ItemResepEntity(0, total.getKey(), "", total.getValue(), "");
                if (!validasiStok(gabungan, dataFarmasi)) {
                    ObatEntity obat = dataFarmasi.ambilObat(total.getKey());
                    kurang.add(obat == null ? "obat " + total.getKey() : obat.getNamaObat());
                }
            }
            if (!kurang.isEmpty()) {
                throw new IllegalStateException("Stok tidak cukup, perbaiki resep untuk: " + String.join(", ", kurang));
            }
            ResepEntity resep = new ResepEntity(0, idKunjungan, LocalDateTime.now(), ResepEntity.STATUS_MENUNGGU, null);
            resep.setDaftarItem(new ArrayList<>(items));
            koneksi.setAutoCommit(false);
            try {
                dataFarmasi.simpanResep(resep);
                AntreanEntity antrean = dataKunjungan.ambilAntrean("apotek");
                antrean.tambahkan(resep.getIdResep());
                dataKunjungan.simpanAntrean(antrean);
                koneksi.commit();
            } catch (RuntimeException e) {
                koneksi.rollback();
                resep.setIdResep(0);
                throw e;
            }
            return resep;
        } catch (SQLException e) {
            throw galatTeknis("Resep gagal dikirim ke apotek", e);
        }
    }

    private static IllegalStateException galatTeknis(String pesan, SQLException e) {
        LOG.log(Level.SEVERE, pesan, e);
        return new IllegalStateException(pesan + ". Hubungi administrator bila berulang.", e);
    }
}
