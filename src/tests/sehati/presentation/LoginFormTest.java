package sehati.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import sehati.business.control.AutentikasiManager;

class LoginFormTest {

    private final AutentikasiManager autentikasi = new AutentikasiManager();

    @Test
    void susunMenu_hanyaLayarPeranDenganUrutanDasbor() {
        assertEquals(List.of("AntreanSkriningForm", "RekamMedisForm", "DaftarPantauForm"),
                LoginForm.susunMenu(autentikasi, "Dokter"));
        assertEquals(List.of("PasienForm", "KunjunganForm", "LaporanForm", "SinkronisasiForm", "KonfigurasiForm"),
                LoginForm.susunMenu(autentikasi, "Petugas Administrasi"));
    }

    @Test
    void susunMenu_peranTidakDikenalKosong() {
        assertTrue(LoginForm.susunMenu(autentikasi, "Tamu").isEmpty());
    }

    @Test
    void daftarMenu_semuaPunyaJudul() {
        assertTrue(Navigasi.JUDUL.keySet().containsAll(LoginForm.DAFTAR_MENU));
    }
}
