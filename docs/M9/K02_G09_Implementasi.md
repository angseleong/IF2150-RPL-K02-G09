<h1>
IF2150 REKAYASA PERANGKAT LUNAK
<br>
TUGAS 9
<br>
IMPLEMENTASI PERANGKAT LUNAK
</h1>
<br>

## SEHATI - Sistem Elektronik Pelayanan Kesehatan Terintegrasi

<p align="center">
<img alt="Logo SEHATI" src="../M8/assets/logo-sehati.svg" width="60%">
</p>

### Untuk: Made Branenda Jordhy

Dipersiapkan oleh:
| Informasi | Keterangan |
| --- | --- |
| Kelas | K02 |
| Kelompok | G09 |
| Nama Kelompok | Cumlaude |

| NIM | Nama |
|---|---|
| 13525008 | Malik Arsyafiandra Madani |
| 13525032 | Ahmad Humam Isma |
| 13525044 | Steven Vanako |
| 13525071 | Muhammad Adnan Kurniawan |
| 13525074 | Axeleon Justin Algianto |
| 13525110 | Fachry Azriel Fajdwani |
---

> **Petunjuk pengerjaan:** *[Silahkan hapus bagian ini setelah selesai mengerjakan]*
>
> Dokumen ini adalah **spesifikasi sekaligus template laporan singkat** Tugas 9. Bagian **Ketentuan Implementasi** (Bagian A) adalah aturan yang harus kalian ikuti, boleh dihapus dari laporan akhir. Bagian **Laporan Implementasi** (Bagian B) adalah yang **wajib kalian isi**. Gunakan nama, ID, kebutuhan, use case, dan kelas yang konsisten dengan SKPL, APL, dan DPPL.

---

**Deadline:** Rabu, 11 November 2026, 21.50 WIB
**Asistensi akbar:** *[Hari, DD Bulan YYYY, HH.MM WIB, lokasi/link]*

---

# BAGIAN A: Ketentuan Implementasi

## A.1 Deskripsi Tugas

Pada tugas ini, kalian mengimplementasikan perangkat lunak yang telah dirancang pada tugas-tugas sebelumnya (SKPL, APL, DPPL) menjadi aplikasi yang **dapat dijalankan**. Platform bebas (web, desktop, atau mobile) selama aplikasi dapat dijalankan ulang oleh asisten mengikuti petunjuk kalian.

Ketentuan umum:
- Setiap anggota bertanggung jawab atas **minimal satu fitur atau modul** dan harus terlihat jelas di riwayat commit/PR.
- Implementasi harus **mengacu pada DPPL**. Jika ada yang berubah (kelas, layar, basis data, dsb.), catat di Bagian B.1. Perubahan itu wajar, tetapi harus tercatat dan beralasan.
- Implementasi **tidak harus berupa kelas** (misalnya fungsi, komponen, *route/handler*, *stored procedure*, atau skrip). Namun, setiap kelas perancangan pada DPPL **wajib dapat ditelusuri** ke implementasinya, dan penelusuran ini dicatat di Bagian B.5.
- Seluruh use case pada SKPL **diprioritaskan** untuk diimplementasikan. Status tiap use case akan dilaporkan pada HIUPL (Tugas 10).
- Seluruh mahasiswa **wajib** mengikuti asistensi akbar.

## A.2 Platform, Bahasa, dan Teknologi

Kalian **bebas** memilih bahasa, framework, library, dan basis data, dengan syarat:

1. **Dapat dijalankan ulang** oleh asisten pada mesin biasa dengan mengikuti README. Versi bahasa/runtime dan seluruh dependensi harus tertulis jelas.
2. **Dependensi dikelola lewat file standar**, misalnya `package.json` + lockfile, `requirements.txt`/`pyproject.toml`, `pom.xml`/`build.gradle`, `composer.json`, `pubspec.yaml`, dan sejenisnya. Jangan meng-commit folder hasil instalasi (`node_modules/`, `venv/`, `build/`, dll.).
3. **Jika memakai layanan eksternal** (basis data cloud, API pihak ketiga, auth, dll.), sediakan cara menjalankan tanpa akun pribadi kalian, atau sediakan data/akun uji pada README. Basis data **sangat disarankan** dapat dijalankan lokal (SQLite, atau Docker Compose untuk PostgreSQL/MySQL/MongoDB).
4. **Arsitektur sesuai DPPL.** Pola arsitektur yang kalian pilih di APL/DPPL (MVC, layered, client-server, dll.) harus terlihat pada struktur kode.

Ketentuan tambahan per platform:

| Platform | Ketentuan |
| :--- | :--- |
| **Web** | Berjalan lokal lewat satu-dua perintah yang terdokumentasi. Boleh juga di-*deploy*; jika ya, cantumkan URL, tetapi **tetap wajib bisa dijalankan lokal** (URL deploy bisa mati). |
| **Desktop** | Berjalan dari source lewat perintah yang terdokumentasi. Build/installer opsional. |
| **Mobile** | Berjalan di emulator/simulator atau perangkat. Sertakan langkah menjalankan, dan **sangat disarankan** melampirkan APK/build di Release. |

## A.3 Keamanan Repository

Repo fork kelompok bersifat **public**, sehingga:

- **Dilarang** meng-commit password, API key, token, `.env` asli, sertifikat/private key, atau data pribadi. Hal ini dapat mengurangi nilai.
- Sediakan `.env.example` berisi nama variabel tanpa nilai rahasia, dan masukkan `.env` ke `.gitignore`.
- Jika sudah terlanjur ter-commit, **cabut/ganti** kredensial tersebut. Menghapus commit saja tidak cukup.
- Data uji/seed harus berupa data fiktif.

## A.4 Struktur Folder

Seluruh kode ditempatkan di `src/` pada repo fork. Jangan mengubah folder milestone lama.

```
src/                              # SELURUH kode implementasi
	README.md                     # dokumentasi utama aplikasi (lihat A.5)
	.env.example                  # jika memakai environment variable
	<struktur kode kalian>        # bebas, mengikuti pola arsitektur DPPL
	tests/                        # test case (jika ada)
docs/
	M9/
		KXX_GYY_Implementasi.md   # laporan ini (Bagian B)
		KXX_GYY_Form-Asistensi.md
```

Catatan:
- `README.md` aplikasi diletakkan di **`src/README.md`**, bukan di root repo.
- Jika kerangka kerja kalian mewajibkan struktur tertentu (misalnya Maven `src/main/java`, Flutter `lib/`, monorepo `frontend/` dan `backend/`), letakkan semuanya **di dalam `src/`**.
- Nama file dan folder pada `docs/M9/` mengikuti aturan penamaan milestone sebelumnya (`KXX_GYY_...`).

## A.5 Isi Minimal `src/README.md`

README harus cukup bagi orang yang belum pernah melihat proyek kalian untuk menjalankan aplikasi dari nol. Minimal berisi:

1. **Deskripsi singkat** aplikasi.
2. **Teknologi yang digunakan** beserta versi (bahasa, framework, basis data).
3. **Prasyarat dan instalasi** (software yang harus ada, perintah instalasi dependensi).
4. **Konfigurasi** (environment variable, setup basis data, migrasi/seed) dan langkah demi langkah menjalankannya.
5. **Cara menjalankan aplikasi** (dan cara menjalankan test, jika ada).
6. **Akun/data uji** untuk mencoba tiap peran pengguna (jika ada login).
7. **Daftar modul/fitur yang diimplementasi** beserta pembagian anggota.
8. **Daftar tabel/koleksi basis data** beserta atributnya (atau tautan ke skema/ERD).

## A.6 Best Practices

Kalian dianjurkan menerapkan praktik berikut. Bukti penerapannya dirangkum pada Bagian B.2.

### 1. Branching
Sesuai aturan umum di README repository, `main` fork kelompok dijaga agar tetap bisa menerima sync dari asisten. Pada tugas ini **sangat disarankan** memakai branch per fitur dengan format `<tipe>/<nama-fitur>` (contoh: `feat/login-page`, `fix/null-cart`), lalu digabungkan ke `main` melalui Pull Request. Skema Gitflow penuh (dengan `develop`) boleh dipakai, tetapi tidak diwajibkan.

### 2. Semantic Commit
Gunakan format `<tipe>(<scope>): <deskripsi>`, misalnya `feat(auth): tambah validasi email`.

| Tipe | Kegunaan |
| :--- | :--- |
| `feat` | Menambahkan fitur baru |
| `fix` | Memperbaiki bug |
| `docs` | Mengubah dokumentasi |
| `style` | Perubahan format/spasi, tanpa mengubah logika |
| `refactor` | Perubahan kode yang tidak menambah fitur atau memperbaiki bug |
| `test` | Menambah/memperbaiki pengujian |
| `chore` | Tugas rutin (build, dependensi, konfigurasi) |

### 3. Track Tasks
Gunakan **GitHub Issues** (dan opsional GitHub Projects) untuk membagi tugas dan melacak progres.
- Pecah fitur besar menjadi issue yang lebih kecil dan tetapkan penanggung jawabnya.
- Hubungkan PR dengan issue (`Closes #12`) dan tutup issue ketika selesai.

### 4. Pull Request dan Code Review
- Setiap PR berisi penjelasan deskriptif tentang apa yang dikerjakan, beserta bukti (tangkapan layar/rekaman/hasil test).
- Setiap PR di-*review* minimal **1 anggota lain**, dengan komentar konstruktif atau approval.

### 5. Pengujian dan Optimasi Performa
- Tulis pengujian untuk logika inti (unit test dan/atau pengujian alur utama). Cakupan penuh tidak diwajibkan, tetapi pengujian pada logika utama dinilai.
- Lakukan **profiling** pada setidaknya satu fitur yang berat (query besar, proses pengolahan data, render list panjang, dsb.), lalu catat temuan dan perbaikannya. Contoh alat:

| Platform | Contoh alat |
| :--- | :--- |
| Web (frontend) | Chrome DevTools (Performance/Network), Lighthouse |
| Web (backend) | Profiler bawaan bahasa, `EXPLAIN` pada query basis data, logging waktu respons |
| Python | `cProfile`, `PyInstrument` |
| Java | VisualVM, JProfiler |
| Mobile | Android Studio Profiler, Flutter DevTools, Xcode Instruments |

### 6. Penggunaan AI
Penggunaan AI **tetap harus dicatat** pada `docs/KXX_GYY_AI-Usage.md` sesuai ketentuan yang sudah berlaku.

## A.7 Pengumpulan

Pengumpulan dinilai dari **tag release**, bukan commit terakhir.

1. Pastikan seluruh pekerjaan sudah di-*commit* dan di-*push* ke `main` fork kelompok.
2. Buat release dengan tag **`v9.0`** (revisi berikutnya `v9.1`, `v9.2`, dst.) dengan target branch `main`.
3. Yang dinilai adalah **tag terakhir yang dibuat sebelum deadline**.

## A.8 Aturan Tanya Jawab

Agar semua kelompok mendapat jawaban yang sama, pertanyaan dibagi menjadi dua jalur:

| Jenis Pertanyaan | Jalur |
| :--- | :--- |
| **Ketentuan tugas**, misalnya teknologi yang diperbolehkan, struktur folder, format pengumpulan, dan penilaian | **Sheets QnA:** *[tautan sheets QnA]* |
| **Kendala teknis kelompok sendiri**, misalnya error, setup, dan penggunaan git | Grup kelompok bersama asisten masing-masing |

Ketentuan:
1. Pertanyaan tentang ketentuan tugas **wajib** diajukan melalui Sheets QnA. Periksa terlebih dahulu apakah pertanyaan serupa sudah dijawab.
2. Jika pertanyaan tentang ketentuan tugas tetap diajukan langsung kepada asisten kelompok, asisten **berhak menolak menjawab** dan meminta kelompok memasukkan pertanyaannya ke Sheets QnA.
3. Jika pertanyaan di Sheets QnA **belum dijawab dalam 24 jam**, kelompok boleh menghubungi asisten kelompok masing-masing. Cantumkan pertanyaan yang sudah diajukan di sheets tersebut.
4. Jawaban pada Sheets QnA berlaku untuk **seluruh kelompok** dan menjadi acuan jika terjadi perbedaan pemahaman.

---

# BAGIAN B: Laporan Implementasi

## B.1 Perubahan dari DPPL

Catat semua perbedaan antara perancangan (DPPL) dan implementasi akhir. Jika tidak ada perubahan, tuliskan "Tidak ada perubahan".

Tabel B.1. Perubahan dari DPPL

| No | Bagian DPPL | Rancangan Awal | Implementasi Akhir | Alasan |
| :--- | :--- | :--- | :--- | :--- |
| 1 | C38 SkriningEntity | Atribut idSkrining, idKunjungan, keluhanAwal, waktu | Ditambah atribut `tandaVital` dan `getTandaVital()` | Subbab 5.38 menyebut tanda vital dipegang sebagai referensi, tetapi atributnya tidak ada di Tabel 4.1, sedangkan `simpanSkrining()` membutuhkannya |
| 2 | C39 TandaVitalEntity, C59 Q-C59-03 dan Q-C59-05 | Tidak ada atribut waktu, `s.waktu` dipilih tetapi tidak disimpan | Ditambah atribut `waktu: LocalDateTime` dengan getter dan setter, diisi dari `s.waktu` pada Q-C59-03 dan Q-C59-05 | Grafik tren UC05 (KF08) memakai waktu pengukuran sebagai sumbu-x dan `RekamMedisForm.seriTren` hanya memegang `List<TandaVitalEntity>`. Atas usul Adnan pada review #29 |
| 3 | C20 SkriningManager | `validasiRentang` private | Package-private | Agar dapat diuji unit tanpa refleksi |
| 4 | C20 SkriningManager | Tidak ada operasi identitas pasien | Tambahan `labelPasien(List<KunjunganEntity>)` | Form tidak boleh membaca DataBoundary, sedangkan I-04 dan I-05 menampilkan nama, RM, dan poli |
| 5 | C20 SkriningManager | `panggilPasien` memakai `AntreanEntity.ambilTerdepan()` | Pasien terdepan dicari dari antrean dan status kunjungan di manager | `ambilTerdepan()` mengembalikan KunjunganEntity dari daftar ID, padahal entity tidak dapat memuat dari basis data |
| 6 | C21 RisikoManager | `evaluasiRisiko(kunjungan, tv)` | Ditambah overload package-private yang menerima `Connection` | Penanda dan entri daftar pantau harus satu transaksi dengan penyimpanan skrining |
| 7 | C51 DaftarPantauEntity | Empat atribut pada Tabel 4.1 | Ditambah atribut tampilan `tanggalPemicu`, `namaPasien`, `nomorTelepon` | `saringDaftar()` perlu tanggal pemicu (Q-C60-01) dan identitas pasien, sedangkan entity tidak memuatnya |
| 8 | C12 DaftarPantauForm | `tampilkanDaftar()` memanggil `RisikoManager.muatEntriAktif()` | Memanggil `PantauManager.saringDaftar(null, null, null)` | Tabel membutuhkan nama dan telepon pasien yang dimuat `saringDaftar()` |
| *...* | *...* | *...* | *...* | *...* |

## B.2 Bukti Penerapan Best Practices

Tabel B.2. Bukti Best Practices

| Praktik | Bukti | Keterangan |
| :--- | :--- | :--- |
| Branching | *[Tautan ke daftar branch / graph commit]* | *[Skema yang dipakai]* |
| Semantic commit | *[Tautan ke riwayat commit]* | *[Ringkasan]* |
| Track tasks | *[Tautan ke Issues/Projects]* | *[Jumlah issue, pembagian]* |
| Pull request dan review | *[Tautan ke 2-3 PR representatif]* | *[Siapa me-review siapa]* |
| Pengujian | *[Tautan ke `src/tests/` dan hasil test]* | *[Apa yang diuji, cara menjalankan]* |

### B.2.1 Profiling dan Optimasi

Pilih minimal satu fitur yang diprofil.

| Informasi | Keterangan |
| :--- | :--- |
| Fitur / proses yang diprofil | *[Nama fitur]* |
| Alat | *[mis. Chrome DevTools, cProfile, EXPLAIN]* |
| Temuan (sebelum) | *[Bottleneck yang ditemukan, sertakan angka/tangkapan layar]* |
| Perbaikan | *[Yang diubah]* |
| Hasil (sesudah) | *[Perbandingan dengan sebelumnya]* |

Jika tidak ada perbaikan yang perlu dilakukan, jelaskan hasil profiling dan alasan kenapa fitur tersebut sudah memadai.

## B.3 Kendala dan Catatan Penting

Tuliskan kendala yang ditemui, fitur yang belum sempurna (*known issues*), serta hal yang perlu diketahui asisten saat menjalankan atau menilai aplikasi (misalnya akun uji khusus, urutan menjalankan, atau batasan).

**Modul skrining dan pemantauan (Humam):**
- Kueri C59 (Q-C59-01 s.d. 10) dan C60 (Q-C60-01 s.d. 06) ditulis menurut DPPL dan dijalankan ke PostgreSQL setelah skema dari issue #2 tersedia. Hasil pengujiannya dicatat di sini.
- JDK yang tersedia di perangkat Humam adalah 17, sehingga pengujian lokal memakai `-Dmaven.compiler.release=17`. Pada JDK 21 tidak ada perbedaan perilaku.

## B.4 Kontribusi Anggota

Tabel B.4. Kontribusi Anggota

| NIM | Nama | Modul / Fitur yang Dikerjakan | Persentase Kontribusi |
| :--- | :--- | :--- | :--- |
| 13525008 | Malik Arsyafiandra Madani | Farmasi (UC07, UC08) | *[%]* |
| 13525032 | Ahmad Humam Isma | Skrining dan pemantauan (UC04, UC09) | *[%]* |
| 13525044 | Steven Vanako | Loket (UC02, UC03) | *[%]* |
| 13525071 | Muhammad Adnan Kurniawan | Pemeriksaan dan resep (UC05, UC06) | *[%]* |
| 13525074 | Axeleon Justin Algianto | Akses dan administrasi (UC01, UC12) | *[%]* |
| 13525110 | Fachry Azriel Fajdwani | Pelaporan dan integrasi (UC10, UC11) | *[%]* |

## B.5 Traceability Kelas Perancangan ke Implementasi

Pemetaan dari **setiap kelas perancangan** pada DPPL (Tabel 4.1) ke wujud implementasinya. Bentuk implementasi bebas (kelas, fungsi, komponen, *route/handler*, *stored procedure*, skrip, dsb.), tetapi tiap kelas perancangan harus muncul pada tabel ini. Jika implementasi berupa kelas dengan nama yang sama, tetap dicantumkan.

Tabel B.5. Traceability Kelas Perancangan ke Implementasi

| ID Kelas | Nama Kelas Perancangan | Bentuk Implementasi | Lokasi (File dan Nama) | Keterangan |
| :--- | :--- | :--- | :--- | :--- |
| C01 | LoginForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C02 | PasienForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C03 | KunjunganForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C04 | AntreanSkriningForm | Kelas JavaFX controller dengan FXML | `src/src/main/java/sehati/presentation/AntreanSkriningForm.java`, `src/src/main/resources/sehati/fxml/AntreanSkriningForm.fxml` | Antrean menyegar tiap 10 detik, F2 memanggil, F3 melewati. Akses data lewat `SkriningManager`. |
| C05 | SkriningForm | Kelas JavaFX controller dengan FXML | `src/src/main/java/sehati/presentation/SkriningForm.java`, `src/src/main/resources/sehati/fxml/SkriningForm.fxml` | Ctrl+S menyimpan, kolom bermasalah ditandai merah, penanda risiko memakai `PanelPeringatan`. |
| C06 | RekamMedisForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C07 | PemeriksaanForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C08 | ResepForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C09 | AntreanResepForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C10 | PenyerahanObatForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C11 | PersediaanForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C12 | DaftarPantauForm | Kelas JavaFX controller dengan FXML | `src/src/main/java/sehati/presentation/DaftarPantauForm.java`, `src/src/main/resources/sehati/fxml/DaftarPantauForm.fxml` | Penyaring, tabel, dan panel tindak lanjut pada satu layar. Esc menutup panel lebih dulu. |
| C13 | LaporanForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C14 | SinkronisasiForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C15 | SATUSEHATGateway | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C16 | KonfigurasiForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C17 | AutentikasiManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C18 | PasienManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C19 | KunjunganManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C20 | SkriningManager | Kelas | `src/src/main/java/sehati/business/control/SkriningManager.java` | Validasi rentang Algo-C20-02 berupa konstanta kelas. Tambahan `labelPasien()`. |
| C21 | RisikoManager | Kelas | `src/src/main/java/sehati/business/control/RisikoManager.java` | Inti penilaian risiko dipisah menjadi `jenisRisikoTerpenuhi()` agar dapat diuji tanpa basis data. |
| C22 | PemeriksaanManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C23 | ResepManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C24 | ApotekManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C25 | PersediaanManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C26 | PantauManager | Kelas | `src/src/main/java/sehati/business/control/PantauManager.java` | Penyaringan murni dipisah menjadi `saring()`; `catat()` dapat diuji dengan pengganti DataBoundary. |
| C27 | LaporanManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C28 | SinkronisasiManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C29 | KonfigurasiManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C30 | PenggunaEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C31 | SesiEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C32 | AuditEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C33 | PasienEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C34 | RekamMedisEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C35 | KunjunganEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C36 | PoliEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C37 | AntreanEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C38 | SkriningEntity | Kelas | `src/src/main/java/sehati/business/entity/SkriningEntity.java` | Ditambah atribut `tandaVital` dan `getTandaVital()` (Tabel B.1). |
| C39 | TandaVitalEntity | Kelas | `src/src/main/java/sehati/business/entity/TandaVitalEntity.java` | Algo-C39-01. Ditambah atribut `waktu` (Tabel B.1). |
| C40 | AmbangRisikoEntity | Kelas | `src/src/main/java/sehati/business/entity/AmbangRisikoEntity.java` | Algo-C40-01. |
| C41 | PenandaRisikoEntity | Kelas | `src/src/main/java/sehati/business/entity/PenandaRisikoEntity.java` | Konstanta `STATUS_AKTIF` dan `STATUS_DICABUT` ditambahkan. |
| C42 | PemeriksaanEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C43 | DiagnosisEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C44 | KodeICD10Entity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C45 | JadwalKontrolEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C46 | ResepEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C47 | ItemResepEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C48 | ObatEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C49 | BetsObatEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C50 | PenerimaanObatEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C51 | DaftarPantauEntity | Kelas | `src/src/main/java/sehati/business/entity/DaftarPantauEntity.java` | Algo-C51-01. Ditambah atribut tampilan `tanggalPemicu`, `namaPasien`, `nomorTelepon` (Tabel B.1). |
| C52 | TindakLanjutEntity | Kelas | `src/src/main/java/sehati/business/entity/TindakLanjutEntity.java` | `terhubung()`. |
| C53 | LaporanEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C54 | BundelFHIREntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C55 | PencadanganEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C56 | DataAkunBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C57 | DataPasienBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C58 | DataKunjunganBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C59 | DataKlinisBoundary | Kelas | `src/src/main/java/sehati/dataaccess/DataKlinisBoundary.java` | Q-C59-01 s.d. 10 oleh Humam, Q-C59-11 s.d. 17 oleh Adnan (Subbab 5.1 Pembagian Tugas). |
| C60 | DataPantauBoundary | Kelas | `src/src/main/java/sehati/dataaccess/DataPantauBoundary.java` | Q-C60-01 s.d. Q-C60-06 sebagai konstanta `Q_C60_xx`, Algo-C60-01. |
| C61 | DataFarmasiBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C62 | DataLaporanBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C63 | DataSinkronisasiBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C64 | DataPencadanganBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |

Catatan:
- Satu kelas perancangan boleh diwujudkan oleh beberapa file/unit, dan satu file/unit boleh mewujudkan beberapa kelas perancangan. Tuliskan semuanya.
- Kelas perancangan yang **tidak diimplementasikan** atau **digabung/dipecah** tetap dicantumkan, dan alasannya dicatat di Bagian B.1.
