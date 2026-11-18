package sehati.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sehati.business.entity.SkriningEntity;
import sehati.business.entity.TandaVitalEntity;

/**
 * C06 bertingkat Tidak perlu (teruji lewat UC05-TC01). Test ini hanya menjaga teks panel skrining hari ini.
 */
@DisplayName("Pendukung C06 RekamMedisForm")
class RekamMedisFormTest {

    @Test
    @DisplayName("Pendukung C06 teksSkrining: keluhan dan tanda vital, tanpa skrining, dan tanpa gula darah")
    void teksSkrining() {
        SkriningEntity s = new SkriningEntity(1, 9, "Pusing", LocalDateTime.now());
        s.lampirkanTandaVital(new TandaVitalEntity(152, 96, 84.0, 170.0, 36.8, 88, 140));

        assertEquals("Keluhan awal: Pusing\nTekanan darah 152/96 mmHg  |  Berat 84.0 kg  |  Tinggi 170.0 cm  |  IMT "
                + String.format("%.1f", 84.0 / (1.7 * 1.7)) + "\nSuhu 36.8 °C  |  Nadi 88 bpm  |  Gula darah 140 mg/dL",
                RekamMedisForm.teksSkrining(s));
        assertEquals("Pasien belum diskrining pada kunjungan ini", RekamMedisForm.teksSkrining(null));

        s.lampirkanTandaVital(new TandaVitalEntity(118, 76, 52.0, 158.0, 37.8, 92, null));
        assertTrue(RekamMedisForm.teksSkrining(s).endsWith("Gula darah -"));
    }
}
