package sehati.business.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Pengujian unit entity pemeriksaan (HIUPL Subbab 4.2): C42 PemeriksaanEntity, C44 KodeICD10Entity, dan C45
 * JadwalKontrolEntity. Test berawalan "Pendukung" menguji kelas bertingkat Tidak perlu atau milik modul lain dan
 * tidak diberi ID.
 */
@DisplayName("Entity pemeriksaan (C42, C44, C45)")
@TestMethodOrder(MethodOrderer.DisplayName.class)
class EntityPemeriksaanTest {

    private final KodeICD10Entity hipertensi = new KodeICD10Entity("I10", "Hipertensi esensial (primer)",
            "Penyakit sistem sirkulasi");

    @Test
    @DisplayName("C42-UT01 kunci normal: draf pemeriksaan menjadi terkunci")
    void kunci_normal() {
        PemeriksaanEntity p = new PemeriksaanEntity(1, 1, "Pusing", "Edukasi", false);
        p.kunci();
        assertTrue(p.isTerkunci());
    }

    @Test
    @DisplayName("C42-UT02 kunci batas: penguncian tidak mengubah anamnesis dan tindakan")
    void kunci_isiTetap() {
        PemeriksaanEntity p = new PemeriksaanEntity(1, 1, "Pusing", "Edukasi", false);
        p.kunci();
        assertEquals("Pusing", p.getAnamnesis());
        assertEquals("Edukasi", p.getTindakan());
    }

    @Test
    @DisplayName("C42-UT03 kunci negatif: pemeriksaan yang sudah terkunci ditolak")
    void kunci_keduaKali() {
        PemeriksaanEntity p = new PemeriksaanEntity(1, 1, "Pusing", "Edukasi", true);
        IllegalStateException e = assertThrows(IllegalStateException.class, p::kunci);
        assertTrue(p.isTerkunci());
        assertFalse(e.getMessage().isBlank());
    }

    @Test
    @DisplayName("C44-UT01 cocok normal: awalan kode dan isi deskripsi cocok tanpa membedakan huruf")
    void cocok_normal() {
        assertTrue(hipertensi.cocok("i1"));
        assertTrue(hipertensi.cocok("ESENSIAL"));
    }

    @Test
    @DisplayName("C44-UT02 cocok batas: kode lengkap dan kata kunci berspasi tepi tetap cocok")
    void cocok_batas() {
        assertTrue(hipertensi.cocok("I10"));
        assertTrue(hipertensi.cocok(" hipertensi "));
    }

    @Test
    @DisplayName("C44-UT03 cocok negatif: potongan kode di tengah dan istilah lain tidak cocok")
    void cocok_tidakCocok() {
        assertFalse(hipertensi.cocok("10"));
        assertFalse(hipertensi.cocok("diabetes"));
    }

    @Test
    @DisplayName("C44-UT04 cocok negatif: kata kunci kosong dan null tidak cocok")
    void cocok_kosong() {
        assertFalse(hipertensi.cocok("  "));
        assertFalse(hipertensi.cocok(null));
    }

    @Test
    @DisplayName("C44-UT05 cocok batas: kode yang diisi lewat setter dibaca cocok() dan getter")
    void cocok_setter() {
        KodeICD10Entity k = new KodeICD10Entity();
        k.setKode("E11");
        k.setDeskripsi("Diabetes melitus tipe 2");
        k.setBab("Penyakit endokrin");

        assertTrue(k.cocok("e1"));
        assertEquals("E11", k.getKode());
        assertEquals("Diabetes melitus tipe 2", k.getDeskripsi());
        assertEquals("Penyakit endokrin", k.getBab());
    }

    @Test
    @DisplayName("C45-UT01 tandaiSelesai normal: jadwal terjadwal menjadi selesai")
    void tandaiSelesai_normal() {
        JadwalKontrolEntity j = new JadwalKontrolEntity(1, 1, LocalDate.now().plusDays(7),
                JadwalKontrolEntity.STATUS_TERJADWAL);
        j.tandaiSelesai();
        assertEquals("selesai", j.getStatus());
    }

    @Test
    @DisplayName("C45-UT02 tandaiSelesai batas: jadwal yang tanggalnya sudah lewat tetap dapat diselesaikan")
    void tandaiSelesai_tanggalLewat() {
        JadwalKontrolEntity j = new JadwalKontrolEntity(1, 1, LocalDate.now().minusDays(1),
                JadwalKontrolEntity.STATUS_TERJADWAL);
        j.tandaiSelesai();
        assertEquals("selesai", j.getStatus());
    }

    @Test
    @DisplayName("C45-UT03 tandaiSelesai negatif: jadwal yang sudah selesai atau tanpa status ditolak")
    void tandaiSelesai_bukanTerjadwal() {
        JadwalKontrolEntity selesai = new JadwalKontrolEntity(1, 1, LocalDate.now(), JadwalKontrolEntity.STATUS_SELESAI);
        assertThrows(IllegalStateException.class, selesai::tandaiSelesai);
        assertThrows(IllegalStateException.class, new JadwalKontrolEntity()::tandaiSelesai);
    }

    @Test
    @DisplayName("Pendukung C43 ringkas: kode dan deskripsi digabung dengan tanda hubung")
    void ringkas_kodeDanDeskripsi() {
        assertEquals("I10 - Hipertensi esensial (primer)",
                new DiagnosisEntity(1, "I10", "Hipertensi esensial (primer)").ringkas());
    }

    @Test
    @DisplayName("Pendukung C35 perbaruiStatus: status asing dan kunjungan selesai ditolak")
    void perbaruiStatus_menolakStatusAsingDanKunjunganSelesai() {
        KunjunganEntity k = new KunjunganEntity(1, 1, 1, LocalDate.now(), 1, KunjunganEntity.STATUS_SIAP_DIPERIKSA);
        k.perbaruiStatus(KunjunganEntity.STATUS_SELESAI_DIPERIKSA);
        assertEquals("selesai diperiksa", k.getStatus());
        assertTrue(k.aktifHariIni());
        assertThrows(IllegalArgumentException.class, () -> k.perbaruiStatus("dibatalkan"));

        k.perbaruiStatus(KunjunganEntity.STATUS_SELESAI);
        assertFalse(k.aktifHariIni());
        assertThrows(IllegalStateException.class, () -> k.perbaruiStatus(KunjunganEntity.STATUS_SIAP_DIPERIKSA));
    }
}
