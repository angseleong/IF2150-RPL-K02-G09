# Form Asistensi

## Tugas Besar IF2150 - Rekayasa Perangkat Lunak

| Informasi | Keterangan |
| --- | --- |
| **Hari** | Senin |
| **Tanggal** | 02/11/2026 |
| **Kelas** | K02 |
| **Nomor Kelompok** | G09|
| **Nama Kelompok** | Cumlaude|
| **Nama Perangkat Lunak** | SEHATI (Sistem Elektronik Pelayanan Kesehatan Terintegrasi)|
| **Dokumen** | K02_G09_Implementasi.md (Tugas 9 - Implementasi Perangkat Lunak)|

### Anggota Kelompok

| NIM | Nama |
| --- | --- |
| 13525008 | Malik Arsyafiandra Madani |
| 13525032 | Ahmad Humam Isma |
| 13525044 | Steven Vanako |
| 13525071 | Muhammad Adnan Kurniawan |
| 13525074 | Axeleon Justin Algianto |
| 13525110 | Fachry Azriel Fajdwani |

### Catatan

| Catatan |
| --- |
| 1. Asistensi akbar berlangsung pada pukul 19.10 sampai 19.45 WIB. Laporan implementasi berfungsi sekaligus sebagai spesifikasi dan harus memuat perubahan dari DPPL, bukti penerapan *best practice* Git, profiling, kendala yang perlu diketahui asisten, kontribusi anggota, serta *traceability* kelas perancangan ke implementasi. |
| 2. Seluruh kode diletakkan pada repositori yang sama di folder `src/`. Kelompok membuat `README.md` sendiri di dalam folder tersebut, dan `README.md` repositori dari asisten tidak diganti. Isinya minimal deskripsi, versi dan instalasi, konfigurasi, cara menjalankan program, akun atau data uji, pembagian tugas, dan skema basis data. Berkas `.env.example` hanya memuat nama variabel, dan paket dependensi tidak di-*commit* tetapi diabaikan lewat `.gitignore`. |
| 3. Platform, bahasa, kerangka kerja, dan arsitektur bebas selama sesuai dengan rancangan. Basis data disarankan lokal (SQLite, Docker Compose, atau PostgreSQL), dan data tiruan dalam CSV atau JSON juga diperbolehkan. Pengujian unit pada tiap modul, fungsi, atau kelas dianjurkan sebagai praktik yang baik. |
| 4. Alur kerja mengikuti Git Flow: `main` hanya untuk rilis, `develop` untuk integrasi, dan cabang fitur dibuat dari `develop`, lalu di-*pull* dari `develop` sebelum membuat *pull request*. *Pull request* harus ditinjau anggota lain dan tidak boleh ditinjau atau digabung sendiri. Pesan *commit* memakai format semantik `tipe(lingkup): deskripsi` dengan tipe `feat`, `fix`, `docs`, `style`, `refactor`, `test`, dan `chore`. |
| 5. *Issue* dipakai untuk pembagian tugas dengan judul dan kriteria penerimaan yang jelas, diberi label, dan ditautkan ke *pull request* lewat `Closes #n`. Cabang dapat dibuat langsung dari halaman *issue*. Peninjauan kode memeriksa kesesuaian dengan kebutuhan, kesalahan logika, penamaan, dan kualitas kode. Hasilnya berupa *approve*, *request changes*, atau *comment*. |
| 6. Praktik yang perlu dihindari: *god class*, nilai yang ditulis langsung di kode (gunakan berkas konfigurasi atau variabel lingkungan), penanganan galat tanpa pesan yang informatif, pencampuran tanggung jawab antarlapisan, dan gaya penulisan kode yang tidak konsisten. Optimasi dilakukan setelah fitur berjalan dan ditemukan bagian yang lambat. |
| 7. Implementasi harus berlandaskan kebutuhan fungsional yang sudah dibuat. Batas minimal kebutuhan non-fungsional akan disampaikan asisten kemudian, dan demo akan dinilai dari kedua jenis kebutuhan itu. Tenggat pengumpulan adalah Rabu, 11 November 2026. Kendala mendekati tenggat dikomunikasikan kepada asisten, dan asistensi mandiri bersifat opsional. |

## Dokumentasi

<!-- ![](./assets/foto-asistensi.jpg) -->
<p align="center">
  <img src="./assets/foto-asistensi.jpg" width="100%">
</p>

<p align="center">
  <i>Gambar 1. Dokumentasi kegiatan asistensi.</i>
</p>
