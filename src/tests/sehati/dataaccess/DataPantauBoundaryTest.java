package sehati.dataaccess;

import org.junit.jupiter.api.Test;
import sehati.business.entity.DaftarPantauEntity;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sehati.dataaccess.JdbcPalsu.baris;

class DataPantauBoundaryTest {

    @Test
    void ambilEntriSelesaiHanyaStatusSelesaiTerbaruDulu() {
        JdbcPalsu palsu = new JdbcPalsu().hasil("status_tindak_lanjut = 'selesai ditindaklanjuti'", List.of(
                baris("id_entri", 7, "id_pasien", 1, "jenis_risiko", "hipertensi",
                        "status_tindak_lanjut", DaftarPantauEntity.STATUS_SELESAI,
                        "tanggal_pemicu", Date.valueOf("2026-11-13"))));

        List<DaftarPantauEntity> hasil = new DataPantauBoundary(palsu.koneksi()).ambilEntriSelesai();

        assertEquals(1, hasil.size());
        assertEquals(DaftarPantauEntity.STATUS_SELESAI, hasil.get(0).getStatusTindakLanjut());
        assertEquals(LocalDate.of(2026, 11, 13), hasil.get(0).getTanggalPemicu());
        assertTrue(palsu.dijalankan.get(0).sql().endsWith("ORDER BY dp.id_entri DESC LIMIT 100"));
    }
}
