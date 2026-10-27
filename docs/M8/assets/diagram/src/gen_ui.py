#!/usr/bin/env python3
"""Menghasilkan wireframe SVG BAB 6 DPPL2 dan tabel spesifikasi objeknya dari satu sumber.

Pemakaian: python3 gen_ui.py <folder-svg> <berkas-md>
"""
import sys
from html import escape as esc

TEAL, DARK, GRAY, LINE = "#0F766E", "#0F172A", "#64748B", "#94A3B8"
RED, GREEN, AMBER = "#DC2626", "#16A34A", "#D97706"
W = 960


class Screen:
    def __init__(self, id_, nama, form, uc, aktor, h, bar=True, w=W):
        self.id, self.nama, self.form, self.uc, self.aktor = id_, nama, form, uc, aktor
        self.h, self.w, self.bar = h, w, bar
        self.parts, self.objs = [], []
        self.n = 0

    def raw(self, s):
        self.parts.append(s)

    def obj(self, jenis, konten, ket, bx, by):
        """Mendaftarkan objek bernomor dan menggambar penanda nomornya di (bx, by)."""
        self.n += 1
        self.objs.append((f"I-{self.id:02d}-{self.n:02d}", jenis, konten, ket))
        self.raw(f'<circle cx="{bx}" cy="{by}" r="9" fill="#EA580C"/>'
                 f'<text x="{bx}" y="{by + 3.5}" font-size="10" font-weight="700" fill="#fff" text-anchor="middle">{self.n}</text>')

    # ---- bentuk dasar ----
    def text(self, x, y, t, size=13, color=DARK, bold=False, anchor="start"):
        fw = ' font-weight="700"' if bold else ""
        self.raw(f'<text x="{x}" y="{y}" font-size="{size}" fill="{color}" text-anchor="{anchor}"{fw}>{esc(t)}</text>')

    def label(self, jenis, x, y, t, ket, size=13, color=DARK, bold=False, anchor="start", konten=None):
        self.text(x, y, t, size, color, bold, anchor)
        self.obj(jenis, konten or t, ket, x - 12 if anchor == "start" else x - 40, y - 14)

    def box(self, x, y, w, h, fill="#fff", stroke=LINE, r=6, sw=1):
        self.raw(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" fill="{fill}" stroke="{stroke}" stroke-width="{sw}"/>')

    def field(self, jenis, x, y, w, caption, hint, ket, h=30, error=False, ro=False, combo=False, konten=None):
        self.text(x, y - 6, caption, 11, GRAY)
        self.box(x, y, w, h, "#F1F5F9" if ro else "#fff", RED if error else LINE, sw=2 if error else 1)
        self.text(x + 8, y + h / 2 + 4, hint, 12, "#94A3B8" if not ro else DARK)
        if combo:
            self.raw(f'<polygon points="{x + w - 20},{y + h / 2 - 3} {x + w - 10},{y + h / 2 - 3} {x + w - 15},{y + h / 2 + 4}" fill="{GRAY}"/>')
        self.obj(jenis, konten or caption, ket, x + 2, y - 4)

    def button(self, x, y, w, t, ket, style="primary", h=32, konten=None):
        fill, tc, st = {"primary": (TEAL, "#fff", TEAL), "secondary": ("#fff", TEAL, TEAL),
                        "danger": (RED, "#fff", RED), "off": ("#E2E8F0", "#94A3B8", "#E2E8F0")}[style]
        self.box(x, y, w, h, fill, st, r=6)
        self.text(x + w / 2, y + h / 2 + 4, t, 12, tc, True, "middle")
        self.obj("Button", konten or t, ket, x + 2, y - 2)

    def panel(self, jenis, x, y, w, h, title, ket, fill="#fff", stroke=LINE, tcolor=TEAL):
        self.box(x, y, w, h, fill, stroke)
        self.text(x + 12, y + 20, title, 12, tcolor, True)
        self.obj(jenis, title, ket, x + 2, y - 2)

    def table(self, x, y, cols, rows, ket, title=None, sel=None, rh=26, konten=None):
        w = sum(c[1] for c in cols)
        h = rh * (len(rows) + 1)
        self.box(x, y, w, h, "#fff")
        self.raw(f'<rect x="{x}" y="{y}" width="{w}" height="{rh}" rx="6" fill="#E2F3F1" stroke="{LINE}"/>')
        cx = x
        for name, cw in cols:
            self.text(cx + 8, y + rh / 2 + 4, name, 11, TEAL, True)
            cx += cw
        for i, row in enumerate(rows):
            ry = y + rh * (i + 1)
            if sel == i:
                self.raw(f'<rect x="{x + 1}" y="{ry}" width="{w - 2}" height="{rh}" fill="#FEF3C7"/>')
            self.raw(f'<line x1="{x}" y1="{ry}" x2="{x + w}" y2="{ry}" stroke="#E2E8F0"/>')
            cx = x
            for (name, cw), v in zip(cols, row):
                col = RED if str(v).startswith("!") else DARK
                self.text(cx + 8, ry + rh / 2 + 4, str(v).lstrip("!"), 11, col, str(v).startswith("!"))
                cx += cw
        self.obj("Table", konten or title or "Tabel", ket, x + 2, y - 2)
        return h

    def tabs(self, x, y, names, active, ket, w=150):
        for i, nm in enumerate(names):
            on = i == active
            self.box(x + i * w, y, w, 32, "#fff" if on else "#E2E8F0", TEAL if on else LINE, r=6)
            self.text(x + i * w + w / 2, y + 20, nm, 12, TEAL if on else GRAY, on, "middle")
        self.obj("Tab", " | ".join(names), ket, x + 2, y - 2)

    def badge(self, x, y, w, t, ket, color=GREEN, konten=None):
        self.box(x, y, w, 22, color, color, r=11)
        self.text(x + w / 2, y + 15, t, 11, "#fff", True, "middle")
        self.obj("Label", konten or t, ket, x + 2, y - 2)

    def warn(self, x, y, size=18):
        self.raw(f'<polygon points="{x},{y + size} {x + size},{y + size} {x + size / 2},{y}" fill="{RED}"/>'
                 f'<text x="{x + size / 2}" y="{y + size - 3}" font-size="{size * 0.6:.0f}" font-weight="700" fill="#fff" text-anchor="middle">!</text>')

    def appbar(self, ket, nama=None, peran="Perawat", luring=False):
        self.raw(f'<rect x="0" y="0" width="{self.w}" height="48" fill="{TEAL}"/>')
        self.text(16, 30, "SEHATI", 18, "#fff", True)
        self.text(130, 30, nama or self.nama, 14, "#fff")
        self.text(self.w - 300, 30, f"Dewi Lestari ({peran})", 12, "#fff", anchor="start")
        c = "#FACC15" if luring else "#4ADE80"
        self.raw(f'<circle cx="{self.w - 118}" cy="24" r="6" fill="{c}"/>')
        self.text(self.w - 106, 28, "Luring" if luring else "Daring", 11, "#fff")
        self.raw(f'<rect x="{self.w - 64}" y="12" width="52" height="24" rx="5" fill="none" stroke="#fff"/>')
        self.text(self.w - 38, 28, "Keluar", 11, "#fff", anchor="middle")
        self.obj("Panel", "Bilah aplikasi", ket, self.w - 330, 12)

    def svg(self):
        head = (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {self.w} {self.h}" width="{self.w}" height="{self.h}" '
                f'font-family="Helvetica, Arial, sans-serif" role="img" aria-label="Rancangan antarmuka I-{self.id:02d} {esc(self.nama)}">'
                f'<rect width="{self.w}" height="{self.h}" fill="#F8FAFC"/>')
        return head + "".join(self.parts) + "</svg>"


BAR = ("Menampilkan nama layar, nama dan peran pengguna sesi, indikator status koneksi, dan tombol Keluar. "
       "Indikator hijau \"Daring\" berarti internet tersedia, sedangkan kuning \"Luring\" berarti hanya LAN yang aktif dan seluruh fungsi layar tetap berjalan (KF22, KNF09). "
       "Tombol Keluar menutup sesi (SesiEntity.tutup) dan kembali ke I-01. F1 membuka layar bantuan berisi seluruh pintasan papan ketik (Tabel 6.1, KNF12).")


def build():
    S = []

    # I-01 LoginForm
    s = Screen(1, "Masuk dan Dasbor", "LoginForm", "UC01", "Seluruh pengguna", 470, bar=False)
    s.raw(f'<rect width="{s.w}" height="{s.h}" fill="#0F766E"/>')
    s.box(30, 30, 400, 410, "#fff", "#fff", r=14)
    s.label("Image", 230, 78, "SEHATI", "Logo dan nama aplikasi (logo-sehati.svg) di atas kartu masuk.", 28, TEAL, True, "middle")
    s.text(230, 100, "Sistem Elektronik Pelayanan Kesehatan Terintegrasi", 10, GRAY, anchor="middle")
    s.label("Label", 230, 140, "Masuk ke Aplikasi", "Judul kartu autentikasi. Layar ini tampil saat aplikasi dibuka dan setiap sesi berakhir, termasuk setelah diam 15 menit (LoginForm.tampilkan, KNF04).", 16, DARK, True, "middle")
    s.field("TextField", 60, 175, 340, "Nama pengguna", "nama.pengguna", "Isian atribut namaPengguna. Kursor aktif di sini saat layar dibuka. Spasi di awal dan akhir dipangkas sebelum dikirim.")
    s.field("PasswordField", 60, 240, 340, "Kata sandi", "••••••••", "Isian atribut kataSandi. Isinya ditimpa kosong segera setelah autentikasi selesai, berhasil maupun gagal (Subbab 5.1).")
    s.label("Label", 60, 305, "Nama pengguna atau kata sandi salah", "Pesan merah dari LoginForm.tampilkanPesanGagal(). Pesan sama untuk nama tak dikenal dan kata sandi salah; akun nonaktif menampilkan \"Akun nonaktif, hubungi Petugas Administrasi\" (UC01 alternatif 1 dan 2).", 12, RED)
    s.button(60, 330, 340, "Masuk", "Menjalankan LoginForm.kirimKredensial() (Algo-C01-01); dapat juga dengan tombol Enter. Bila sesi dibuka, dasbor di sisi kanan tampil sesuai peran.")
    s.box(470, 30, 460, 410, "#F8FAFC", "#F8FAFC", r=14)
    s.text(490, 62, "Dasbor (peran Dokter)", 14, TEAL, True)
    for i, (t, d) in enumerate([("Antrean Skrining", "AntreanSkriningForm"), ("Rekam Medis", "RekamMedisForm"), ("Daftar Pantau", "DaftarPantauForm")]):
        x = 490 + i * 145
        s.box(x, 85, 135, 90, "#fff", LINE)
        s.text(x + 67, 125, t, 12, DARK, True, "middle")
        s.text(x + 67, 145, d, 9, GRAY, anchor="middle")
    s.obj("Panel", "Kartu menu", "Satu kartu untuk setiap layar yang diizinkan bagi peran sesi (LoginForm.tampilkanDasbor, Algo-C01-01). Layar di luar wewenang tidak digambar. Mengklik kartu menjalankan LoginForm.bukaLayar() (Algo-C01-02), yang memeriksa hak akses sekali lagi.", 492, 83)
    s.box(490, 200, 420, 60, "#FEE2E2", RED)
    s.warn(504, 218)
    s.text(534, 235, "Pencadangan terakhir gagal. Hubungi Petugas Administrasi.", 11, RED, True)
    s.obj("Panel", "Peringatan pencadangan", "Hanya tampil pada dasbor Petugas Administrasi bila pencadangan terakhir tidak berstatus berhasil, sampai pencadangan berikutnya berhasil (KF21, I-15).", 492, 198)
    s.box(490, 290, 420, 60, "#fff", LINE)
    s.text(506, 316, "Akses ditolak: Peran Perawat tidak berwenang membuka layar ini", 11, RED)
    s.obj("Label", "Pesan akses ditolak", "Muncul dari LoginForm.tampilkanPesanGagal() bila layar dibuka lewat jalan lain dan periksaHakAkses() mengembalikan false (UC01 alternatif 3).", 492, 288)
    s.text(490, 385, "Pintasan: F1 bantuan  |  Ctrl+D kembali ke dasbor", 11, GRAY)
    S.append(s)

    # I-02 PasienForm
    s = Screen(2, "Pendaftaran Pasien", "PasienForm", "UC02", "Petugas Administrasi", 650)
    s.appbar(BAR, peran="Petugas Administrasi")
    s.field("TextField", 30, 85, 420, "Pencarian pasien", "Cari NIK, nama, atau nomor rekam medis (min. 3 karakter)", "Memanggil PasienForm.kirimPencarian() setiap isi berubah dan panjangnya sedikitnya 3 karakter, dengan jeda 250 ms (KF01, KNF01, Algo-C02-01). Pintasan Ctrl+F.")
    s.button(470, 85, 130, "Pasien Baru", "Menjalankan bukaFormulir(null): formulir dikosongkan untuk pendaftaran pasien baru. Pintasan Ctrl+N.", "secondary")
    s.table(30, 135, [("NIK", 150), ("No. RM", 90), ("Nama", 170), ("Tgl Lahir", 90), ("JK", 70)],
            [["3273010101900001", "RM-000118", "Siti Rahma", "01-01-1990", "P"], ["3273011505850002", "RM-000121", "Budi Santoso", "15-05-1985", "L"]],
            "Hasil pencarian dari tampilkanHasilCari(). Mengklik baris menjalankan bukaFormulir(pasien) sehingga formulir terisi untuk disunting. Bila kosong tampil \"Data pasien tidak ditemukan\" dan tombol Pasien Baru disorot (UC02 alternatif 2).", "Hasil pencarian", sel=0)
    s.box(630, 70, 300, 480, "#fff")
    s.text(646, 94, "Formulir Data Pasien", 13, TEAL, True)
    s.field("TextField", 646, 125, 268, "NIK (16 digit)", "3273010101900001", "Isian dataFormulir.nik. Kolom diberi tanda merah bila NIK bukan 16 digit atau sudah terdaftar (KF02).", error=True)
    s.label("Label", 646, 175, "NIK sudah terdaftar", "Pesan dari tampilkanPesanValidasi() di bawah kolom NIK. Isian lain tidak dihapus (Algo-C02-02).", 11, RED)
    s.field("TextField", 646, 205, 268, "Nama lengkap", "Siti Rahma", "Isian dataFormulir.nama. Wajib diisi.")
    s.field("DatePicker", 646, 265, 268, "Tanggal lahir", "01-01-1990", "Isian dataFormulir.tanggalLahir. Wajib diisi.", combo=True)
    s.field("ComboBox", 646, 325, 268, "Jenis kelamin", "Perempuan", "Pilihan Laki-laki atau Perempuan (dataFormulir.jenisKelamin).", combo=True)
    s.field("TextArea", 646, 385, 268, "Alamat", "Jl. Merdeka No. 10, Bandung", "Isian dataFormulir.alamat. Wajib diisi.")
    s.field("TextField", 646, 445, 268, "Nomor telepon (opsional)", "081234567890", "Isian dataFormulir.nomorTelepon. Boleh kosong.")
    s.field("TextField", 646, 505, 268, "Nomor rekam medis", "RM-000118 (otomatis)", "Hanya baca. Diterbitkan sistem saat pasien baru tersimpan dan tidak dapat diubah pada penyuntingan (KF02, Algo-C18-01).", ro=True)
    s.button(646, 560, 130, "Simpan", "Menjalankan kirimDataPasien(): daftarkanPasien() bila idPasien = 0, perbaruiPasien() bila sudah terisi (Algo-C02-02). Pintasan Ctrl+S.")
    s.button(790, 560, 124, "Buka Kunjungan", "Membuka KunjunganForm (I-03) dengan pasien pada formulir sebagai pasienTerpilih. Aktif hanya bila pasien sudah tersimpan (UC03).", "secondary")
    s.box(30, 590, 400, 36, "#DCFCE7", GREEN)
    s.text(44, 613, "Pendaftaran berhasil, nomor rekam medis RM-000118", 11, "#166534", True)
    s.obj("Label", "Notifikasi", "Notifikasi hijau sementara dari tampilkanNotifikasi(), misalnya \"Pendaftaran berhasil\" atau \"Data pasien diperbarui\".", 32, 588)
    S.append(s)

    # I-03 KunjunganForm
    s = Screen(3, "Kunjungan Pasien", "KunjunganForm", "UC03", "Petugas Administrasi", 540)
    s.appbar(BAR, peran="Petugas Administrasi")
    s.panel("Panel", 30, 75, 420, 90, "Pasien terpilih", "Identitas pasienTerpilih (nama, NIK, nomor rekam medis), dikirim dari I-02 lewat KunjunganForm.buka(). Hanya baca.")
    s.text(46, 118, "Siti Rahma", 14, DARK, True)
    s.text(46, 138, "NIK 3273010101900001  |  RM-000118", 11, GRAY)
    s.field("ComboBox", 30, 205, 420, "Poli tujuan", "Poli Umum", "Daftar poli dari tampilkanPilihanPoli() (KunjunganManager.daftarPoli()). Pilihan disimpan sebagai poliTerpilih.", combo=True)
    s.button(30, 260, 200, "Buka Kunjungan", "Menjalankan kirimPermintaanKunjungan() (Algo-C03-01). Nonaktif sampai poli dipilih dan selama permintaan berjalan, sehingga klik ganda tidak membuat dua kunjungan.")
    s.button(250, 260, 120, "Kembali", "Kembali ke I-02. Pintasan Esc.", "secondary")
    s.panel("Panel", 490, 75, 440, 200, "Tiket Antrean", "Tiket dari tampilkanTiketAntrean(): nama poli, nomor antrean, dan tanggal. Nomor berurutan per poli per hari dan mulai dari 1 saat hari berganti (KF03).", "#F0FDFA", TEAL)
    s.text(710, 160, "7", 64, TEAL, True, "middle")
    s.text(710, 190, "Nomor antrean", 12, GRAY, anchor="middle")
    s.text(710, 225, "Poli Umum  |  27-10-2026", 12, DARK, anchor="middle")
    s.button(620, 235, 180, "Cetak Tiket", "Mencetak tiket antrean ke pencetak bawaan sistem operasi.", "secondary")
    s.panel("Panel", 490, 305, 440, 120, "Kunjungan Sudah Berjalan", "Panel kuning dari tampilkanKunjunganAktif() bila pasien sudah punya kunjungan aktif hari ini: nomor antrean dan status ditampilkan, kunjungan baru tidak dibuat (UC03 alternatif 1).", "#FEF9C3", AMBER, AMBER)
    s.text(506, 352, "Pasien sudah memiliki kunjungan hari ini.", 12, DARK)
    s.text(506, 374, "Poli Gigi, antrean 3, status: menunggu skrining", 12, DARK, True)
    S.append(s)

    # I-04 AntreanSkriningForm
    s = Screen(4, "Antrean Skrining", "AntreanSkriningForm", "UC04", "Perawat", 470)
    s.appbar(BAR)
    s.label("Label", 30, 90, "Menunggu skrining: 4 pasien", "Ringkasan jumlah kunjungan pada daftarTampil yang berstatus menunggu skrining.", 14, DARK, True)
    s.table(30, 110, [("No", 70), ("Nama pasien", 240), ("Poli", 140), ("Status", 200)],
            [["1", "Budi Santoso", "Poli Umum", "sedang diskrining"], ["2", "Siti Rahma", "Poli Umum", "menunggu skrining"], ["3", "Andi Wijaya", "Poli Gigi", "menunggu skrining"], ["4", "Rina Kusuma", "Poli Umum", "menunggu skrining"]],
            "Antrean dari perbaruiTampilan(): terurut nomor antrean, pasien yang sedang dilayani disorot kuning (KF04).", "Antrean skrining", sel=0)
    s.button(30, 290, 210, "Panggil Berikutnya  (F2)", "Menjalankan kirimPanggilan(): status pasien terdepan menjadi \"sedang diskrining\" lalu SkriningForm (I-05) terbuka. Nonaktif bila daftarTampil kosong (KF04, KF24).")
    s.button(260, 290, 210, "Lewati: Tidak Hadir  (F3)", "Menjalankan kirimLewati(): pasien dipindahkan ke urutan paling belakang dan berstatus tidak hadir, tampilan beralih ke pasien berikutnya (UC04 alternatif 1).", "secondary")
    s.label("Label", 30, 360, "Diperbarui otomatis tiap 10 detik, terakhir 3 detik lalu", "Penyegar yang memuat ulang antrean lewat muatAntrean() agar pasien baru dari loket tampil tanpa tindakan perawat.", 11, GRAY)
    S.append(s)

    # I-05 SkriningForm
    s = Screen(5, "Formulir Skrining", "SkriningForm", "UC04", "Perawat", 650)
    s.appbar(BAR)
    s.panel("Panel", 30, 70, 900, 56, "Pasien: Budi Santoso  |  RM-000121  |  Poli Umum  |  Antrean 1", "Identitas kunjungan yang dipanggil, dari tampilkanFormulir(kunjungan). Hanya baca.")
    s.field("TextArea", 30, 160, 440, "Keluhan awal", "Pusing sejak dua hari", "Isian isianKeluhan, teks bebas (KF05). Kolom pertama yang difokuskan.", h=60)
    cols = [("Sistolik (mmHg)", "160", False), ("Diastolik (mmHg)", "100", False), ("Berat badan (kg)", "78", False),
            ("Tinggi badan (cm)", "165", False), ("Suhu (°C)", "72", True), ("Nadi (bpm)", "84", False), ("Gula darah (mg/dL), opsional", "", False)]
    kets = ["Tekanan darah sistolik, kolom isianUkuran.sistolik.", "Tekanan darah diastolik, isianUkuran.diastolik. Harus lebih rendah dari sistolik.",
            "Isian isianUkuran.beratBadan. Koma desimal diterima.", "Isian isianUkuran.tinggiBadan. Dipakai untuk menghitung IMT.",
            "Isian isianUkuran.suhu. Kolom ditandai merah beserta rentang wajar (30 sampai 45 °C) oleh tampilkanPeringatanRentang() bila di luar rentang, dan tidak ada yang disimpan (KF06, UC04 alternatif 2).",
            "Isian isianUkuran.nadi.", "Isian opsional isianUkuran.gulaDarah. Boleh kosong (KF05)."]
    for i, ((cap, val, err), k) in enumerate(zip(cols, kets)):
        x, y = 30 + (i % 3) * 300, 260 + (i // 3) * 70
        s.field("NumberField", x, y, 270, cap, val or "(kosong)", k, error=err)
    s.text(330, 376, "Di luar rentang wajar (30 sampai 45 °C)", 11, RED, True)
    s.box(30, 455, 900, 70, "#FEE2E2", RED, sw=2)
    s.warn(46, 473, 26)
    s.text(86, 495, "RISIKO: Hipertensi (sistolik 160 mmHg)", 14, RED, True)
    s.text(86, 515, "Pasien ditandai berisiko dan dimasukkan ke daftar pantau", 11, DARK)
    s.obj("Panel", "Penanda risiko", "Ikon peringatan dan latar merah per penanda dari tampilkanPenandaRisiko(), tanpa langkah tambahan (KF07, KNF08, UC04 alternatif 3). Tidak tampil bila tidak ada risiko.", 32, 453)
    s.button(30, 555, 190, "Simpan  (Ctrl+S)", "Menjalankan kirimHasilSkrining() (Algo-C05-01). Hanya bila seluruh nilai berada dalam rentang, skrining tersimpan dan pasien diteruskan ke antrean poli (KF05, KF06, KF24).")
    s.button(240, 555, 120, "Batal  (Esc)", "Menutup formulir tanpa menyimpan dan kembali ke I-04.", "secondary")
    s.text(30, 620, "Tab pindah ke kolom berikutnya, Shift+Tab ke kolom sebelumnya. Seluruh formulir dapat diisi tanpa tetikus (KF24, KNF12).", 11, GRAY)
    S.append(s)

    # I-06 RekamMedisForm
    s = Screen(6, "Rekam Medis Pasien", "RekamMedisForm", "UC05", "Dokter, Perawat", 640)
    s.appbar(BAR, peran="Dokter")
    s.panel("Panel", 30, 70, 330, 110, "Data diri", "Data diri pasien dari susunRingkasan() pada satu tampilan bersama diagnosis, riwayat obat, dan penanda risiko (KF09).")
    s.text(46, 112, "Budi Santoso, 41 tahun, L", 12, DARK, True)
    s.text(46, 132, "NIK 3273011505850002  |  RM-000121", 11, GRAY)
    s.text(46, 152, "Jl. Cihampelas 5, Bandung", 11, GRAY)
    s.panel("Panel", 30, 195, 330, 80, "Penanda risiko aktif", "Penanda risiko yang masih aktif, tampil dengan ikon dan warna merah (KF07, KF09).")
    s.warn(46, 225, 20)
    s.text(76, 240, "Hipertensi", 12, RED, True)
    s.panel("Panel", 30, 290, 330, 120, "Diagnosis lampau", "Daftar diagnosis lampau dari pemeriksaan yang sudah dikunci, memuat kode ICD-10 dan deskripsi (KF09).")
    s.text(46, 332, "I10 - Hipertensi esensial (primer)", 11, DARK)
    s.text(46, 352, "E11 - Diabetes melitus tipe 2", 11, DARK)
    s.panel("Panel", 30, 425, 330, 120, "Riwayat obat", "Obat yang sudah diserahkan pada kunjungan lampau, satu baris per obat (KF09).")
    s.text(46, 467, "12-10-2026 Amlodipin 5 mg x30, 1x1", 11, DARK)
    s.tabs(390, 70, ["Ringkasan", "Lihat Tren Tanda Vital"], 1, "Tab yang berpindah antara ringkasan dan grafik tren. Tab Lihat Tren Tanda Vital menjalankan tampilkanGrafikTren() (Algo-C06-01, KF08).", 180)
    s.box(390, 110, 540, 300, "#fff")
    s.raw(f'<line x1="430" y1="140" x2="430" y2="380" stroke="{GRAY}"/><line x1="430" y1="380" x2="900" y2="380" stroke="{GRAY}"/>')
    s.raw(f'<polyline points="450,250 570,230 690,200 810,170" fill="none" stroke="{RED}" stroke-width="2.5"/>'
          f'<polyline points="450,300 570,296 690,290 810,288" fill="none" stroke="{TEAL}" stroke-width="2.5"/>'
          f'<polyline points="450,330 570,320 690,340 810,310" fill="none" stroke="{AMBER}" stroke-width="2.5"/>')
    for i, d in enumerate(["01-08", "05-09", "12-10", "27-10"]):
        s.text(450 + i * 120, 398, d, 10, GRAY, anchor="middle")
    s.text(440, 160, "Sistolik", 10, RED, True)
    s.text(500, 160, "Berat badan", 10, TEAL, True)
    s.text(580, 160, "Gula darah", 10, AMBER, True)
    s.obj("Chart", "Grafik tren tanda vital", "Grafik garis tekanan darah, berat badan, dan gula darah antar-kunjungan dengan waktu di sumbu-x (JavaFX LineChart, KF08). Bila hanya ada satu titik, diganti tabel pengukuran hari ini beserta keterangan bahwa grafik butuh minimal dua kunjungan (tampilkanTabelPengukuran, UC05 alternatif 4).", 392, 108)
    s.badge(700, 74, 130, "Mode baca saja", "Lencana dari aturModeBacaSaja() untuk peran selain Dokter. Tombol Periksa Pasien dinonaktifkan dan tampil keterangan \"Peran Anda tidak berwenang mengisi diagnosis\" (UC05 alternatif 1).", AMBER)
    s.button(390, 430, 190, "Periksa Pasien", "Membuka PemeriksaanForm (I-07) bila AutentikasiManager.periksaHakAkses() mengizinkan peran. Bagi Perawat tombol nonaktif dan PemeriksaanForm.kunciKolomKlinis() dipanggil.")
    s.button(600, 430, 190, "Buat Resep", "Membuka ResepForm (I-08) bagi Dokter bila pasien memerlukan terapi obat (UC06).", "secondary")
    s.text(30, 600, "Saat internet putus, lencana Luring tampil pada bilah aplikasi dan seluruh data tetap dibaca dari basis data lokal (tampilkanIndikatorLuring, UC05 alternatif 3).", 11, GRAY)
    S.append(s)

    # I-07 PemeriksaanForm
    s = Screen(7, "Formulir Pemeriksaan", "PemeriksaanForm", "UC05", "Dokter", 640)
    s.appbar(BAR, peran="Dokter")
    s.panel("Panel", 30, 70, 900, 50, "Pasien: Budi Santoso  |  RM-000121  |  Kunjungan 27-10-2026", "Identitas kunjungan yang diperiksa. Hanya baca.")
    s.field("TextArea", 30, 155, 440, "Anamnesis", "Pusing sejak dua hari, tidak mual", "Isian isianAnamnesis, teks bebas. Wajib diisi sebelum menyimpan (Algo-C07-01). Dinonaktifkan oleh kunciKolomKlinis() bagi peran selain Dokter.", h=90)
    s.field("TextField", 30, 280, 440, "Diagnosis (cari kode atau istilah ICD-10)", "hipertensi", "Isian kataKunciDiagnosis. Pencarian dikirim lewat kirimKataKunciDiagnosis() setelah berhenti mengetik 300 ms (KF10).")
    s.box(30, 312, 440, 90, "#fff", TEAL)
    s.text(42, 334, "I10  Hipertensi esensial (primer)", 12, DARK, True)
    s.text(42, 356, "I11  Penyakit jantung hipertensif", 12, DARK)
    s.text(42, 378, "I15  Hipertensi sekunder", 12, DARK)
    s.obj("List", "Saran kode ICD-10", "Daftar saran dari tampilkanSaranKode(). Memilih satu saran menyimpan kode dan deskripsinya sebagai diagnosis. Bila kosong tampil \"Kode diagnosis ICD-10 tidak ditemukan\" (UC05 alternatif 2).", 32, 310)
    s.label("Label", 30, 425, "Diagnosis terpilih: I10 - Hipertensi esensial (primer)", "Ringkasan DiagnosisEntity.ringkas() dari saran yang dipilih. Diagnosis hanya dapat berupa kode ICD-10 standar (KF10).", 12, TEAL, True)
    s.field("TextArea", 500, 155, 430, "Tindakan", "Edukasi diet rendah garam, pengukuran ulang tekanan darah", "Isian tindakan pada PemeriksaanEntity.", h=90)
    s.field("DatePicker", 500, 280, 250, "Jadwal kontrol (opsional)", "10-11-2026", "Tanggal kunjungan ulang. Bila terisi, jadwalkanKontrol() dipanggil dan jadwal masuk ke daftar pantau (KF12). Tanggal harus setelah hari ini.", combo=True)
    s.button(30, 480, 170, "Simpan  (Ctrl+S)", "Menjalankan kirimHasilPemeriksaan() (Algo-C07-01). Draf dapat disimpan berulang kali sebelum dikunci.")
    s.button(220, 480, 210, "Simpan Final (Kunci)", "Menjalankan kirimFinal() setelah konfirmasi: rekam medis kunjungan dikunci dan seluruh kolom tidak dapat diubah lagi (UC05). Aktif setelah pemeriksaan pernah tersimpan.", "danger")
    s.button(450, 480, 150, "Buat Resep", "Membuka ResepForm (I-08) untuk kunjungan ini (UC06).", "secondary")
    s.box(30, 560, 900, 36, "#E2E8F0", LINE)
    s.text(44, 583, "Kontrol dijadwalkan 10-11-2026", 12, DARK)
    s.obj("Label", "Bilah pesan", "Pesan dari tampilkanPesan(): hasil penyimpanan atau penolakan, misalnya \"Rekam medis kunjungan ini sudah dikunci\".", 32, 558)
    S.append(s)

    # I-08 ResepForm
    s = Screen(8, "Resep Elektronik", "ResepForm", "UC06", "Dokter", 620)
    s.appbar(BAR, peran="Dokter")
    s.panel("Panel", 30, 70, 900, 50, "Pasien: Budi Santoso  |  RM-000121  |  Kunjungan 27-10-2026", "Identitas kunjungan yang diresepkan, dari tampilkanFormulir(kunjungan). Hanya baca.")
    s.field("ComboBox", 30, 155, 250, "Obat (cari dari data master)", "Amlodipin 5 mg tablet", "Pencarian obat dari ResepManager.muatDataObat() (KF11).", combo=True)
    s.field("NumberField", 295, 155, 90, "Jumlah", "30", "Jumlah item resep dalam satuan sediaan. Harus lebih dari nol.")
    s.field("TextField", 400, 155, 150, "Dosis", "5 mg", "Isian dosis. Wajib diisi.")
    s.field("TextField", 565, 155, 250, "Aturan pakai", "1 x 1 sesudah makan", "Isian aturanPakai. Wajib diisi.")
    s.button(830, 155, 100, "Tambah", "Menjalankan kirimItem() (Algo-C08-01): item masuk ke isianItem dan indikator stok diperbarui.")
    s.table(30, 220, [("Obat", 250), ("Dosis", 120), ("Jumlah", 90), ("Aturan pakai", 230), ("Stok", 200)],
            [["Amlodipin 5 mg tablet", "5 mg", "30", "1 x 1 sesudah makan", "Tersedia (sisa 240)"], ["Captopril 25 mg tablet", "25 mg", "60", "2 x 1 sesudah makan", "!Stok Kurang (sisa 20)"]],
            "Item resep yang sedang disusun (isianItem). Kolom Stok memuat indikator dari tampilkanIndikatorStok(): hijau Tersedia, merah Stok Kurang beserta sisa (KF11).", "Item resep")
    s.badge(30, 310, 90, "Tersedia", "Lencana hijau indikator stok untuk item yang cukup (tersedia = true).", GREEN)
    s.badge(140, 310, 120, "Stok Kurang", "Lencana merah indikator stok untuk item yang kurang (tersedia = false) beserta sisa stok.", RED)
    s.box(30, 350, 900, 50, "#FEE2E2", RED)
    s.warn(44, 364, 22)
    s.text(80, 381, "Stok Captopril hanya tersisa 20, perbaiki resep sebelum dikirim", 12, RED, True)
    s.obj("Label", "Peringatan stok", "Peringatan dari tampilkanPeringatanStok(). Tombol Kirim ke Apotek ditahan sampai tidak ada item yang kurang stoknya (UC06 alternatif 1).", 32, 348)
    s.button(30, 430, 190, "Kirim ke Apotek", "Menjalankan kirimResep(): ResepManager.kirimKeApotek() memeriksa ulang stok lalu resep masuk antrean apotek (Algo-C23-02). Nonaktif selama ada item berstatus Stok Kurang.", "off")
    s.button(240, 430, 120, "Batal  (Esc)", "Menutup formulir dan kembali ke I-06.", "secondary")
    S.append(s)

    # I-09 AntreanResepForm
    s = Screen(9, "Antrean Resep Apotek", "AntreanResepForm", "UC07", "Petugas Farmasi", 430)
    s.appbar(BAR, peran="Petugas Farmasi")
    s.label("Label", 30, 90, "Resep menunggu: 3", "Ringkasan jumlah resep pada daftarTampil yang berstatus menunggu.", 14, DARK, True)
    s.table(30, 110, [("Waktu masuk", 110), ("Pasien", 180), ("Obat", 380), ("Status", 120), ("", 110)],
            [["08:42", "Budi Santoso", "Amlodipin 5 mg x30; Captopril 25 mg x60", "menunggu", "Layani"], ["08:51", "Rina Kusuma", "Paracetamol 500 mg x10", "menunggu", "Layani"], ["09:03", "Andi Wijaya", "Amoksisilin 500 mg x15", "menunggu", "Layani"]],
            "Antrean resep dari tampilkanAntrean(), terurut waktu masuk, memuat nama pasien, obat, dosis, dan jumlah (KF13). Mengklik baris atau Layani menjalankan pilihResep() dan membuka I-10.", "Antrean resep", sel=0)
    s.label("Label", 30, 270, "Diperbarui otomatis tiap 10 detik, terakhir 4 detik lalu", "Penyegar yang memuat ulang antrean lewat muatAntreanResep() agar resep baru dari dokter muncul tanpa tindakan petugas.", 11, GRAY)
    S.append(s)

    # I-10 PenyerahanObatForm
    s = Screen(10, "Penyerahan Obat", "PenyerahanObatForm", "UC07", "Petugas Farmasi", 650)
    s.appbar(BAR, peran="Petugas Farmasi")
    s.panel("Panel", 30, 70, 900, 56, "Pasien: Budi Santoso  |  RM-000121  |  Resep masuk 08:42", "Identitas pasien dan waktu resep masuk, dari tampilkanRincian(resep, bets). Hanya baca.")
    s.table(30, 150, [("Obat", 280), ("Dosis", 120), ("Jumlah", 90), ("Aturan pakai", 260)],
            [["Amlodipin 5 mg tablet", "5 mg", "30", "1 x 1 sesudah makan"], ["Captopril 25 mg tablet", "25 mg", "60", "2 x 1 sesudah makan"]],
            "Item resep yang akan diserahkan (rincianTampil).", "Item resep")
    s.table(30, 250, [("Nomor bets", 160), ("Kedaluwarsa", 160), ("Sisa", 100)],
            [["B-2026-014", "12-03-2027", "120"], ["B-2026-031", "20-08-2027", "300"]],
            "Bets yang diambil lebih dulu, terurut tanggal kedaluwarsa terdekat (FEFO, Algo-C24-02).", "Bets obat")
    s.button(30, 350, 250, "Obat Diserahkan  (Ctrl+Enter)", "Menjalankan kirimKonfirmasi() (Algo-C10-01): stok dipotong, resep selesai dilayani, kunjungan ditutup, dan bundel FHIR disusun dalam satu transaksi (KF14). Nonaktif selama permintaan berjalan.")
    s.button(300, 350, 230, "Kembalikan ke Dokter", "Menjalankan bukaKolomCatatan(): kolom alasan di bawah dibuka (UC07 alternatif 1).", "danger")
    s.field("TextArea", 30, 425, 500, "Alasan pengembalian (wajib)", "Stok Captopril habis, mohon ganti obat", "Isian catatanPengembalian. Tidak boleh kosong.", h=60)
    s.button(30, 505, 200, "Kirim Pengembalian", "Menjalankan kirimPengembalian(alasan): resep dikembalikan ke dokter dan kunjungan tetap terbuka.", "secondary")
    s.box(30, 570, 900, 36, "#FEE2E2", RED)
    s.text(44, 593, "Resep sudah selesai dilayani", 12, RED, True)
    s.obj("Label", "Bilah pesan", "Pesan dari tampilkanPesan(): \"Obat diserahkan\" atau penolakan, misalnya \"Resep sudah selesai dilayani\" saat petugas lain lebih dulu (UC07 alternatif 2).", 32, 568)
    S.append(s)

    # I-11 PersediaanForm
    s = Screen(11, "Persediaan Obat", "PersediaanForm", "UC08", "Petugas Farmasi", 640)
    s.appbar(BAR, peran="Petugas Farmasi")
    s.panel("Panel", 30, 70, 900, 130, "Peringatan persediaan", "Dasbor peringatan dari tampilkanPeringatan(): stok di bawah ambang minimum dan bets yang kedaluwarsa dalam 90 hari atau kurang, dengan ikon dan warna merah (KF17, UC08 alternatif 1).", "#FEF2F2", RED, RED)
    s.warn(46, 100, 18)
    s.text(74, 114, "Captopril 25 mg: stok 20, di bawah ambang minimum 50", 12, RED, True)
    s.warn(46, 130, 18)
    s.text(74, 144, "Paracetamol 500 mg bets P-0412 kedaluwarsa 14-12-2026 (48 hari lagi)", 12, RED, True)
    s.field("ComboBox", 30, 245, 300, "Obat", "Captopril 25 mg tablet", "Pilihan obat dari muatDataObat(). Bets pada penerimaan ini milik obat yang dipilih.", combo=True)
    s.field("TextField", 350, 245, 280, "Nomor faktur", "FK-2026-1187", "Isian nomorFaktur pada isianPenerimaan.")
    s.field("DatePicker", 650, 245, 280, "Tanggal terima", "27-10-2026", "Isian tanggalTerima. Tidak boleh setelah hari ini.", combo=True)
    s.field("TextField", 30, 325, 300, "Nomor bets", "B-2026-052", "Isian nomorBets. Nomor bets yang sudah tercatat untuk obat yang sama ditolak (Algo-C61-01).")
    s.field("NumberField", 350, 325, 280, "Jumlah", "0", "Isian jumlah bets, harus lebih dari nol. Ditandai merah bila tidak valid.", error=True)
    s.field("DatePicker", 650, 325, 280, "Tanggal kedaluwarsa", "01-10-2026", "Isian tanggalKedaluwarsa, harus setelah tanggal terima. Ditandai merah bila tidak valid (UC08 alternatif 2).", error=True, combo=True)
    s.text(350, 380, "Jumlah harus lebih dari nol", 11, RED)
    s.text(650, 380, "Tanggal kedaluwarsa sudah lewat", 11, RED)
    s.obj("Label", "Pesan kolom", "Penanda merah dari tampilkanPesanKesalahan(kolom) pada setiap kolom yang tidak valid. Pesan dari catatPenerimaan() berisi nama kolom dipisah koma (Algo-C11-01).", 337, 368)
    s.button(30, 410, 170, "Simpan  (Ctrl+S)", "Menjalankan kirimPenerimaan() (Algo-C11-01): stok bertambah, penerimaan dan bets tersimpan, audit dicatat dalam satu transaksi (KF17).")
    s.panel("Panel", 30, 480, 420, 90, "Saldo stok terbaru", "Saldo hasil tampilkanSaldo(obat) setelah penerimaan tersimpan. Formulir lalu dikosongkan untuk penerimaan berikutnya.", "#F0FDFA", TEAL)
    s.text(46, 525, "Captopril 25 mg tablet: 320 (ambang minimum 50)", 12, DARK, True)
    S.append(s)

    # I-12 DaftarPantauForm
    s = Screen(12, "Daftar Pantau Pasien", "DaftarPantauForm", "UC09", "Perawat, Dokter", 690)
    s.appbar(BAR)
    s.field("ComboBox", 30, 85, 200, "Jenis risiko", "Hipertensi", "Pilihan Semua, hipertensi, obesitas, indikasi diabetes, atau kunjungan ulang. \"Semua\" dikirim sebagai null.", combo=True)
    s.field("DatePicker", 250, 85, 170, "Mulai", "01-10-2026", "Batas awal tanggal pemicu entri. Boleh kosong.", combo=True)
    s.field("DatePicker", 440, 85, 170, "Akhir", "27-10-2026", "Batas akhir tanggal pemicu entri. Tidak boleh sebelum tanggal mulai.", combo=True)
    s.button(630, 85, 130, "Terapkan", "Menjalankan kirimPenyaring() (Algo-C12-01) lalu menggambar ulang tabel dari hasil PantauManager.saringDaftar().")
    s.label("Label", 30, 150, "Menampilkan 3 entri aktif", "Keterangan dari tampilkanKeterangan(). Bila hasil penyaringan kosong teksnya menjadi \"Tidak ada pasien yang cocok dengan kriteria saringan\" dan tabel kosong (UC09 alternatif 2).", 11, GRAY)
    s.table(30, 165, [("Pasien", 200), ("Jenis risiko", 160), ("Tanggal pemicu", 140), ("Status tindak lanjut", 260), ("", 140)],
            [["Budi Santoso", "!hipertensi", "27-10-2026", "belum ditindaklanjuti", "Tindak Lanjut"], ["Siti Rahma", "!indikasi diabetes", "20-10-2026", "perlu dihubungi ulang", "Tindak Lanjut"], ["Andi Wijaya", "kunjungan ulang", "10-11-2026", "belum ditindaklanjuti", "Tindak Lanjut"]],
            "Daftar pantau dari tampilkanDaftar() dan penyaringan (KF18). Jenis risiko tampil merah dengan ikon. Tombol Tindak Lanjut menjalankan bukaFormulirTindakLanjut(idEntri).", "Daftar pantau", sel=0)
    s.box(30, 330, 900, 250, "#fff", TEAL)
    s.text(46, 354, "Formulir Tindak Lanjut: Budi Santoso (hipertensi)", 13, TEAL, True)
    s.obj("Panel", "Formulir tindak lanjut", "Panel yang dibuka bukaFormulirTindakLanjut() untuk entri terpilih (KF18).", 32, 328)
    s.field("ComboBox", 46, 395, 270, "Jenis kontak", "telepon", "Pilihan telepon, pesan singkat, atau kunjungan rumah (TindakLanjutEntity.jenisKontak).", combo=True)
    s.field("ComboBox", 336, 395, 270, "Hasil upaya", "terhubung", "Pilihan terhubung atau tidak terhubung. Bila terhubung, hasil dicatat ke rekam medis dan entri ditutup; bila tidak, entri ditandai perlu dihubungi ulang (Algo-C26-01).", combo=True)
    s.field("ComboBox", 626, 395, 290, "Status kedatangan", "akan datang", "Pilihan akan datang, sudah datang, tidak dapat datang, atau belum diketahui.", combo=True)
    s.button(46, 460, 170, "Simpan  (Ctrl+S)", "Menjalankan kirimTindakLanjut(): membentuk TindakLanjutEntity, memanggil PantauManager.catatTindakLanjut(), lalu menyegarkan tabel (UC09).")
    s.button(236, 460, 120, "Batal  (Esc)", "Menutup formulir tindak lanjut.", "secondary")
    S.append(s)

    # I-13 LaporanForm
    s = Screen(13, "Laporan Periodik", "LaporanForm", "UC10", "Petugas Administrasi", 640)
    s.appbar(BAR, peran="Petugas Administrasi")
    s.field("DatePicker", 30, 85, 220, "Tanggal mulai", "01-10-2026", "Isian tanggalMulai, terisi awal dengan tanggal 1 bulan berjalan. Pilihan dibatasi sampai hari ini.", combo=True)
    s.field("DatePicker", 270, 85, 220, "Tanggal akhir", "27-10-2026", "Isian tanggalAkhir. Bila lebih awal dari tanggal mulai, tampil \"Perbaiki urutan tanggal\" (UC10 alternatif 2).", combo=True)
    s.button(510, 85, 150, "Hitung Rekapitulasi", "Menjalankan kirimRentang(): LaporanManager.hitungRekapitulasi() lalu tampilkanRekapitulasi() (Algo-C27-01, KF19).")
    s.label("Label", 30, 150, "Total kunjungan: 128", "Jumlah seluruh kunjungan pada rentang (baris total pada ringkasan LaporanEntity). Bila nol tampil \"Tidak ada data pelayanan pada rentang tanggal terpilih\" dan tombol ekspor dikunci (UC10 alternatif 1).", 15, DARK, True)
    s.table(30, 170, [("Poli", 180), ("Jumlah", 90)], [["Poli Umum", "84"], ["Poli Gigi", "30"], ["KIA", "14"]], "Pratinjau kunjungan per poli.", "Kunjungan per poli")
    s.table(330, 170, [("Status", 180), ("Jumlah", 90)], [["selesai", "110"], ["selesai diperiksa", "10"], ["tidak hadir", "8"]], "Pratinjau kunjungan per status.", "Kunjungan per status")
    s.table(30, 290, [("Kode", 80), ("Diagnosis", 340), ("Jumlah", 80)], [["I10", "Hipertensi esensial (primer)", "31"], ["J06", "Infeksi saluran napas atas akut", "27"], ["E11", "Diabetes melitus tipe 2", "19"]], "Sepuluh diagnosis terbanyak dari pemeriksaan yang sudah dikunci (KF19). Contoh menampilkan tiga baris.", "Diagnosis terbanyak")
    s.button(30, 410, 150, "Ekspor PDF", "Menjalankan kirimPermintaanEkspor(\"PDF\", direktori) (Algo-C13-01) setelah pengguna memilih folder tujuan (KF19).")
    s.button(200, 410, 150, "Ekspor CSV", "Menjalankan kirimPermintaanEkspor(\"CSV\", direktori). Berkas memakai pemisah titik koma. Kedua tombol ekspor dikunci oleh kunciTombolEkspor() bila laporan kosong.", "secondary")
    s.box(30, 470, 900, 36, "#DCFCE7", GREEN)
    s.text(44, 493, "Berkas berhasil dibuat di D:/Laporan/laporan-sehati_2026-10-01_2026-10-27.pdf", 12, "#166534", True)
    s.obj("Label", "Bilah pesan", "Pesan dari tampilkanPesan(): lokasi berkas hasil ekspor atau penolakan.", 32, 468)
    S.append(s)

    # I-14 SinkronisasiForm
    s = Screen(14, "Sinkronisasi SATUSEHAT", "SinkronisasiForm", "UC11", "Petugas Administrasi", 560)
    s.appbar(BAR, peran="Petugas Administrasi")
    s.badge(30, 82, 110, "Daring", "Indikator koneksi internet dari SATUSEHATGateway.periksaKoneksi(). Berubah menjadi kuning \"Luring\" bila internet putus (KF22).", GREEN)
    s.badge(160, 82, 150, "Tertunda: 3", "Lencana jumlah bundel yang belum diterima SATUSEHAT (SinkronisasiManager.jumlahTertunda).", AMBER)
    s.table(30, 125, [("ID", 70), ("Kunjungan", 110), ("Pasien", 190), ("Status", 150), ("Alasan penolakan", 380)],
            [["41", "2026-10-26 #5", "Budi Santoso", "tertunda", ""], ["42", "2026-10-26 #6", "Rina Kusuma", "terkirim", ""], ["43", "2026-10-27 #1", "Siti Rahma", "!gagal kirim", "Resource.subject tidak ditemukan"]],
            "Status setiap bundel (daftarStatus) dari tampilkanStatus() dan tampilkanHasil(): tertunda, terkirim, atau gagal kirim beserta alasan penolakan SATUSEHAT (KF20, UC11 alternatif 2).", "Status bundel", sel=2)
    s.button(30, 260, 230, "Sinkronkan Sekarang", "Menjalankan kirimPermintaanSinkron() (Algo-C14-01) di thread latar; layar lain tetap dapat dipakai. Nonaktif selama berjalan (KF20).")
    s.box(280, 266, 300, 20, "#E2E8F0", LINE, r=10)
    s.box(280, 266, 170, 20, TEAL, TEAL, r=10)
    s.obj("ProgressBar", "Penanda sedang berjalan", "Penanda proses pengiriman yang tampil selama kirimSemua() berjalan.", 282, 264)
    s.button(30, 320, 230, "Ekspor Bundel", "Muncul dari tampilkanTombolEkspor() saat internet putus. Menjalankan kirimPermintaanEkspor(lokasi) yang menulis arsip terenkripsi ke lokasi pilihan, misalnya media eksternal (UC11 alternatif 1).", "secondary")
    s.box(30, 400, 900, 36, "#FEF9C3", AMBER)
    s.text(44, 423, "Koneksi internet terputus, bundel ditahan", 12, DARK, True)
    s.obj("Label", "Bilah pesan", "Pesan dari tampilkanPesan(): hasil sinkronisasi, penolakan karena luring, atau lokasi berkas ekspor.", 32, 398)
    S.append(s)

    # I-15 KonfigurasiForm
    s = Screen(15, "Konfigurasi dan Basis Data", "KonfigurasiForm", "UC12", "Petugas Administrasi", 760)
    s.appbar(BAR, peran="Petugas Administrasi")
    s.tabs(30, 70, ["Akun Pengguna", "Data Master", "Pemeliharaan Basis Data"], 0, "Tiga tab yang dipilih dengan bukaTab(). Tab Pemeliharaan Basis Data memuat status pencadangan setiap dibuka (KF15, KF21).", 200)
    s.table(30, 120, [("Nama pengguna", 170), ("Peran", 170), ("Status", 90), ("", 100)],
            [["admin01", "Petugas Administrasi", "aktif", "Nonaktifkan"], ["dewi.l", "Perawat", "aktif", "Nonaktifkan"], ["rudi.d", "Dokter", "nonaktif", "-"]],
            "Daftar akun dari tampilkanData(). Mengklik baris memuat akun ke formulir di sisi kanan. Tombol Nonaktifkan menjalankan kirimNonaktifkan(idPengguna) setelah konfirmasi, menutup sesi akun itu (UC12 alternatif 3). Akun sendiri tidak dapat dinonaktifkan.", "Daftar akun", sel=1)
    s.box(590, 110, 340, 290, "#fff")
    s.text(606, 134, "Formulir Akun", 13, TEAL, True)
    s.field("TextField", 606, 165, 308, "Nama pengguna", "dewi.l", "Isian nama akun. Nama yang sudah dipakai ditandai merah dengan pesan \"Nama pengguna sudah terdaftar\" (UC12 alternatif 2).")
    s.field("ComboBox", 606, 225, 308, "Peran", "Perawat", "Pilihan Petugas Administrasi, Perawat, Dokter, atau Petugas Farmasi.", combo=True)
    s.field("PasswordField", 606, 285, 308, "Kata sandi (kosong = tidak diganti)", "", "Isian kata sandi, minimal 8 karakter untuk akun baru. Disimpan sebagai hash bcrypt (KNF04). Dikosongkan dari layar setelah dikirim.")
    s.button(606, 340, 130, "Simpan  (Ctrl+S)", "Menjalankan kirimPerubahan() (Algo-C16-01) sesuai tab aktif.")
    s.button(750, 340, 164, "Akun Baru  (Ctrl+N)", "Mengosongkan formulir untuk akun baru (idPengguna = 0).", "secondary")
    s.box(30, 290, 540, 110, "#fff")
    s.text(46, 314, "Data Master: Ambang Risiko", 13, TEAL, True)
    s.obj("Panel", "Ambang risiko", "Isi tab Data Master untuk ambang risiko (nilai awal 140 mmHg, IMT 30, 200 mg/dL). Hanya nilai yang dapat diubah, lebih dari nol; daftar obat dan poli ditampilkan pada tab yang sama (KF15, simpanDataMaster()).", 32, 288)
    s.text(46, 342, "Hipertensi (sistolik, mmHg)   140", 12, DARK)
    s.text(46, 362, "Obesitas (IMT, kg/m2)           30", 12, DARK)
    s.text(46, 382, "Indikasi diabetes (mg/dL)      200", 12, DARK)
    s.panel("Panel", 30, 430, 900, 190, "Pemeliharaan Basis Data: Status Pencadangan", "Status pencadangan terakhir dari tampilkanStatusPencadangan(): waktu, ukuran, status, dan lokasi media (KF21).")
    s.text(46, 470, "Terakhir: 27-10-2026 02:00  |  Ukuran 184 MB  |  Lokasi: E:/cadangan/sehati_20261027-0200.dump", 12, DARK)
    s.box(46, 490, 868, 44, "#FEE2E2", RED)
    s.warn(58, 500, 22)
    s.text(94, 518, "Pencadangan terakhir gagal (gagal verifikasi). Jalankan ulang sampai berhasil.", 12, RED, True)
    s.obj("Label", "Peringatan pencadangan", "Peringatan merah selama catatan terakhir tidak berstatus berhasil, tampil juga pada dasbor administrasi (UC12 alternatif 4).", 48, 488)
    s.button(46, 555, 220, "Jalankan Pencadangan", "Menjalankan kirimPencadangan(): KonfigurasiManager.jalankanPencadangan() memakai pg_dump ke media cadangan (Algo-C29-03). Penanda sedang berjalan tampil karena prosesnya dapat lama.")
    s.text(30, 700, "Layar ini hanya dapat dibuka peran Petugas Administrasi (Algo-C17-02).", 11, GRAY)
    S.append(s)
    return S


FILES = {1: "login", 2: "pasien", 3: "kunjungan", 4: "antrean-skrining", 5: "skrining", 6: "rekam-medis", 7: "pemeriksaan",
         8: "resep", 9: "antrean-resep", 10: "penyerahan-obat", 11: "persediaan", 12: "daftar-pantau", 13: "laporan",
         14: "sinkronisasi", 15: "konfigurasi"}


def main(svg_dir, md_path):
    screens = build()
    md = []
    fig0, tab0 = 59, 2  # Gambar 58 dan Tabel 6.1 sudah dipakai sebelum bab ini
    for i, s in enumerate(screens):
        fn = f"ui-i{s.id:02d}-{FILES[s.id]}.svg"
        open(f"{svg_dir}/{fn}", "w", encoding="utf-8").write(s.svg())
        md.append(f"## 6.{s.id} Rancangan Antarmuka I-{s.id:02d}\n")
        md.append(f"**Antarmuka:** I-{s.id:02d}  \n**Nama Layar:** {s.nama}  \n**Kelas:** {s.form}  \n**Use Case:** {s.uc}  \n**Pengguna:** {s.aktor}\n")
        md.append(f'<p align="center">\n<img alt="Rancangan Antarmuka I-{s.id:02d} {s.nama}" src="./assets/diagram/{fn}" width="100%">\n</p>\n<p align="center">\n<i>Gambar {fig0 + i}. Rancangan Antarmuka I-{s.id:02d} - {s.nama}</i>\n</p>\n')
        md.append(f"Tabel 6.{tab0 + i}. Spesifikasi Objek Antarmuka I-{s.id:02d}\n")
        md.append("| Id_Objek | Jenis | Konten | Keterangan |\n| :--- | :--- | :--- | :--- |")
        for oid, j, k, ket in s.objs:
            md.append(f"| {oid} | {j} | {k.replace('|', '/')} | {ket.replace('|', '/')} |")
        md.append("")
    md.append(f"## 6.{len(screens) + 1} Rancangan Antarmuka Keseluruhan\n")
    md.append("Tabel berikut mendaftar seluruh layar dengan ID yang sama seperti subbab sebelumnya. SATUSEHATGateway (C15) tidak memiliki layar.\n")
    md.append(f"Tabel 6.{tab0 + len(screens)}. Rancangan Antarmuka Keseluruhan\n")
    md.append("| No | Tampilan Antarmuka |\n| :--- | :--- |")
    for s in screens:
        fn = f"ui-i{s.id:02d}-{FILES[s.id]}.svg"
        md.append(f'| I-{s.id:02d} {s.nama} | <img src="./assets/diagram/{fn}" alt="I-{s.id:02d} {s.nama}" width="400"> |')
    open(md_path, "w", encoding="utf-8").write("\n".join(md) + "\n")
    print(sum(len(s.objs) for s in screens), "objek")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
