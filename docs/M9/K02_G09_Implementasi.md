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
| C02 | PasienForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/PasienForm.java` - `PasienForm`, `src/src/main/resources/sehati/fxml/PasienForm.fxml` | `kirimPencarian()` memakai `PauseTransition` 250 ms dan thread latar (Algo-C02-01). `kirimDataPasien()` mewujudkan Algo-C02-02. Pintasan Ctrl+S/F/N terpasang. Tombol Buka Kunjungan nonaktif dengan TODO sampai #12. |
| C03 | KunjunganForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C04 | AntreanSkriningForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C05 | SkriningForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C06 | RekamMedisForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/RekamMedisForm.java`, `src/src/main/resources/sehati/fxml/RekamMedisForm.fxml` - `RekamMedisForm` | Atribut `idPasienTampil` dan `seriTren` dipakai `tampilkanGrafikTren()`. Kelima operasi DPPL diwujudkan apa adanya: `tampilkanGrafikTren()` memakai `LineChart` dengan tanggal kunjungan pada sumbu-x (Algo-C06-01), dan `aturModeBacaSaja()` menonaktifkan Periksa Pasien bagi peran selain Dokter. Tambahan: daftar pasien siap diperiksa di sisi kiri (B.1). |
| C07 | PemeriksaanForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/PemeriksaanForm.java`, `src/src/main/resources/sehati/fxml/PemeriksaanForm.fxml` - `PemeriksaanForm` | Atribut `isianAnamnesis` dan `kataKunciDiagnosis`. `kirimKataKunciDiagnosis()` dijalankan `PauseTransition` 300 ms, `kirimHasilPemeriksaan()` mewujudkan Algo-C07-01 dan terhubung ke Ctrl+S, `kirimFinal()` meminta konfirmasi lalu memanggil `kunciRekamMedis()`. Tambahan: `buka(kunjungan, pasien)` menerima kunjungan dari I-06 dan memuat draf tersimpan (B.1). |
| C08 | ResepForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/ResepForm.java`, `src/src/main/resources/sehati/fxml/ResepForm.fxml` - `ResepForm` | Atribut `isianItem` menampung item sampai dikirim, dan `indikatorStok` menyimpan indikator terakhir. `kirimItem()` mewujudkan Algo-C08-01, dan tombol Kirim ke Apotek aktif hanya bila semua item cukup stoknya. Tambahan: pencarian obat menyaring daftar `muatDataObat()`, tombol Hapus Item untuk memperbaiki resep, dan `tampilkanFormulir(kunjungan, pasien)` (B.1). |
| C09 | AntreanResepForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/AntreanResepForm.java`, `src/src/main/resources/sehati/fxml/AntreanResepForm.fxml` - `AntreanResepForm` | Atribut `daftarTampil`. `tampilkanAntrean()` dimuat ulang `Timeline` tiap 10 detik selama layar tampil, dan `pilihResep()` dipanggil tombol Layani, klik ganda, atau Enter. Tambahan: nama pasien dan obat dimuat sekali per penyegaran lewat `muatPasienResep()` dan `muatDataObat()` (B.1). |
| C10 | PenyerahanObatForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/PenyerahanObatForm.java`, `src/src/main/resources/sehati/fxml/PenyerahanObatForm.fxml` - `PenyerahanObatForm` | Atribut `rincianTampil` dan `catatanPengembalian`. `kirimKonfirmasi()` mewujudkan Algo-C10-01 dan terhubung ke Ctrl+Enter. Penolakan tampil di `PanelPeringatan`, lalu rincian dimuat ulang. Tambahan: `aturKonteks()` menerima identitas pasien, nama obat, dan penerima pesan dari I-09 (B.1). |
| C11 | PersediaanForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/PersediaanForm.java`, `src/src/main/resources/sehati/fxml/PersediaanForm.fxml` - `PersediaanForm` | Atribut `isianPenerimaan` dan `daftarPeringatan`. `kirimPenerimaan()` mewujudkan Algo-C11-01 dan terhubung ke tombol Simpan serta Ctrl+S. `tampilkanPesanKesalahan()` memetakan nama kolom dari pesan `catatPenerimaan()` ke `PenandaKolom`, dan `tampilkanPeringatan()` memakai `PanelPeringatan` berikon (KNF08). Pekerjaan basis data berjalan di `Task`. |
| C12 | DaftarPantauForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C13 | LaporanForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C14 | SinkronisasiForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C15 | SATUSEHATGateway | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C16 | KonfigurasiForm | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C17 | AutentikasiManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C18 | PasienManager | Kelas Java | `src/src/main/java/sehati/business/control/PasienManager.java` - `PasienManager` | `cariPasien()`, `daftarkanPasien()` (Algo-C18-01), `perbaruiPasien()` (Algo-C18-02), `validasiNIK()`. Satu koneksi per operasi. Audit lewat `SesiAktif.idPenggunaSesi()`. `terbitkanNomorRM(DataPasienBoundary)` agar nomor terbit sekali per transaksi. |
| C19 | KunjunganManager | Kelas Java | `src/src/main/java/sehati/business/control/KunjunganManager.java` - `KunjunganManager` | `bukaKunjungan()` mewujudkan Algo-C19-01: cek kunjungan aktif ganda lewat `DataKunjunganBoundary.kunjunganAktif()`, terbitkan nomor antrean lewat `PoliEntity.ambilNomorBerikutnya()` (Algo-C19-02), lalu simpan kunjungan dan masukkan entri ke antrean jenis KUNJUNGAN. Manager menerima `DataKunjunganBoundary` lewat konstruktor. |
| C20 | SkriningManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C21 | RisikoManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C22 | PemeriksaanManager | Kelas Java | `src/src/main/java/sehati/business/control/PemeriksaanManager.java` - `PemeriksaanManager` | Tanpa atribut, sesuai DPPL. Setiap operasi membuka koneksi lewat `KoneksiBasisData.buka()`. `simpanPemeriksaan()` mewujudkan Algo-C22-01, dan `kunciRekamMedis()` mewujudkan Algo-C22-02 dalam satu transaksi dengan pelaku audit dari `SesiAktif.idPenggunaSesi()`. Tambahan: `muatKunjunganSiapPeriksa()`, `muatPemeriksaan()`, `muatDiagnosis()`, dan `validasiTanggalKontrol()` (B.1). |
| C23 | ResepManager | Kelas Java | `src/src/main/java/sehati/business/control/ResepManager.java` - `ResepManager` | Tanpa atribut. `tambahItem()` mewujudkan Algo-C23-01. `kirimKeApotek()` mewujudkan Algo-C23-02: memeriksa ulang stok, lalu `simpanResep()` dan memasukkan resep ke antrean apotek dalam satu transaksi. `validasiStok()` menerima `DataFarmasiBoundary` agar satu operasi memakai satu koneksi, dan pada `kirimKeApotek()` memeriksa total jumlah per obat (B.1). |
| C24 | ApotekManager | Kelas Java | `src/src/main/java/sehati/business/control/ApotekManager.java` - `ApotekManager` | Tanpa atribut. `serahkanObat()` mewujudkan Algo-C24-01 dalam satu transaksi dengan `kunciResep()`, dan `potongStok()` mewujudkan Algo-C24-02 (FEFO). `susunBundel()` memakai `PenyusunBundelFHIR` untuk isi JSON FHIR R4. Tambahan: `muatPasienResep()`, `muatDataObat()`, `muatBetsResep()`, resep yang selesai ikut dikeluarkan dari antrean apotek, dan `kembalikanKeDokter()` memakai `kunciResep()` (B.1). |
| C25 | PersediaanManager | Kelas Java | `src/src/main/java/sehati/business/control/PersediaanManager.java` - `PersediaanManager` | Tanpa atribut. `catatPenerimaan()` mewujudkan Algo-C25-01 dalam satu transaksi dengan pelaku audit dari `SesiAktif.idPenggunaSesi()`, `validasiPenerimaan()` mewujudkan Algo-C25-02, dan `terbitkanPeringatan()` mewujudkan Algo-C25-03. `tambahStok()` menerima `DataFarmasiBoundary` agar memakai koneksi transaksi (B.1). |
| C26 | PantauManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C27 | LaporanManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C28 | SinkronisasiManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C29 | KonfigurasiManager | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C30 | PenggunaEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PenggunaEntity.java` - `PenggunaEntity` | Algo-C30-01 lewat jBCrypt, ditambah konstruktor akun baru berstatus "aktif" |
| C31 | SesiEntity | Kelas Java | `src/src/main/java/sehati/business/entity/SesiEntity.java` - `SesiEntity` | Algo-C31-01. Sesi yang sedang aktif dipegang `sehati.common.SesiAktif` |
| C32 | AuditEntity | Kelas Java | `src/src/main/java/sehati/business/entity/AuditEntity.java` - `AuditEntity` | Algo-C32-01. Tanpa setter agar catatan tidak dapat diubah (KNF05) |
| C33 | PasienEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PasienEntity.java` - `PasienEntity` | Algo-C33-01 pada `perbarui()`; `idPasien` dan `nomorRekamMedis` tidak ikut berubah. |
| C34 | RekamMedisEntity | Kelas | `src/src/main/java/sehati/business/entity/RekamMedisEntity.java` | Algo-C34-01 diisi. Ditambah `getEntriBaru()` dan `kosongkanEntriBaru()` agar `simpanRekamMedis()` dapat membaca dan mengosongkan penampung entri baru. |
| C35 | KunjunganEntity | Kelas Java | `src/src/main/java/sehati/business/entity/KunjunganEntity.java` - `KunjunganEntity` | `perbaruiStatus()` mewujudkan Algo-C35-01 (validasi status dan kunci status selesai), dan `aktifHariIni()` menentukan kunjungan aktif hari ini. |
| C36 | PoliEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PoliEntity.java` - `PoliEntity` | `ambilNomorBerikutnya()` mewujudkan algoritma nomor antrean berurutan per poli per hari dengan mulai ulang saat hari berganti (KF03). |
| C37 | AntreanEntity | Kelas Java | `src/src/main/java/sehati/business/entity/AntreanEntity.java` - `AntreanEntity` | `tambahkan()` idempoten (Algo-C37-01), `ambilTerdepan()`, `pindahkanKeBelakang()`, dan `keluarkan()` (penolakan `IllegalArgumentException` bila entri tidak ada). |
| C38 | SkriningEntity | Kelas | `src/src/main/java/sehati/business/entity/SkriningEntity.java` | Ditambah atribut `tandaVital` dan `getTandaVital()` (Tabel B.1). |
| C39 | TandaVitalEntity | Kelas | `src/src/main/java/sehati/business/entity/TandaVitalEntity.java` | Algo-C39-01. Ditambah atribut `waktu` (Tabel B.1). |
| C40 | AmbangRisikoEntity | Kelas | `src/src/main/java/sehati/business/entity/AmbangRisikoEntity.java` | Algo-C40-01. |
| C41 | PenandaRisikoEntity | Kelas | `src/src/main/java/sehati/business/entity/PenandaRisikoEntity.java` | Konstanta `STATUS_AKTIF` dan `STATUS_DICABUT` ditambahkan. |
| C42 | PemeriksaanEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PemeriksaanEntity.java` - `PemeriksaanEntity` | Atribut sesuai Tabel 4.1 dengan getter dan setter. `kunci()` menolak penguncian kedua dengan `IllegalStateException`. |
| C43 | DiagnosisEntity | Kelas Java | `src/src/main/java/sehati/business/entity/DiagnosisEntity.java` - `DiagnosisEntity` | Atribut sesuai Tabel 4.1. `ringkas()` mengembalikan "kode - deskripsi". |
| C44 | KodeICD10Entity | Kelas Java | `src/src/main/java/sehati/business/entity/KodeICD10Entity.java` - `KodeICD10Entity` | Atribut sesuai Tabel 4.1. `cocok()` mewujudkan Algo-C44-01 tanpa membedakan huruf besar dan kecil. |
| C45 | JadwalKontrolEntity | Kelas Java | `src/src/main/java/sehati/business/entity/JadwalKontrolEntity.java` - `JadwalKontrolEntity` | Atribut sesuai Tabel 4.1, ditambah konstanta `STATUS_TERJADWAL` dan `STATUS_SELESAI`. `tandaiSelesai()` menolak jadwal yang tidak berstatus terjadwal. |
| C46 | ResepEntity | Kelas Java | `src/src/main/java/sehati/business/entity/ResepEntity.java` - `ResepEntity` | Atribut sesuai Tabel 4.1, dengan komposisi `daftarItem`. `perbaruiStatus()` mewujudkan Algo-C46-01 dengan konstanta `STATUS_MENUNGGU`, `STATUS_SELESAI_DILAYANI`, dan `STATUS_DIKEMBALIKAN`. |
| C47 | ItemResepEntity | Kelas Java | `src/src/main/java/sehati/business/entity/ItemResepEntity.java` - `ItemResepEntity` | Atribut sesuai Tabel 4.1. `cukupStok()` mengembalikan `stok >= jumlah`. |
| C48 | ObatEntity | Kelas Java | `src/src/main/java/sehati/business/entity/ObatEntity.java` - `ObatEntity` | Atribut sesuai Tabel 4.1. `kurangiStok()`, `tambahStok()`, dan `diBawahAmbang()` mewujudkan Algo-C48-01 dan hanya mengubah objek di memori; penulisan ke basis data lewat `DataFarmasiBoundary.simpanStok()`. |
| C49 | BetsObatEntity | Kelas Java | `src/src/main/java/sehati/business/entity/BetsObatEntity.java` - `BetsObatEntity` | Atribut sesuai Tabel 4.1. `kurangi()` dan `mendekatiKedaluwarsa()` mewujudkan Algo-C49-01; bets yang sudah lewat tanggalnya ikut terhitung menjelang kedaluwarsa. |
| C50 | PenerimaanObatEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PenerimaanObatEntity.java` - `PenerimaanObatEntity` | Atribut sesuai Tabel 4.1, dengan agregasi `daftarBets`. `totalJumlah()` mewujudkan Algo-C50-01. |
| C51 | DaftarPantauEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C52 | TindakLanjutEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C53 | LaporanEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C54 | BundelFHIREntity | Kelas Java | `src/src/main/java/sehati/business/entity/BundelFHIREntity.java` - `BundelFHIREntity` | Algo-C54-01: `tandaiTerkirim()` dan `tandaiGagal()` menolak bundel yang sudah terkirim |
| C55 | PencadanganEntity | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C56 | DataAkunBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataAkunBoundary.java` - `DataAkunBoundary` | Q-C56-01 sampai Q-C56-08 sebagai konstanta `Q_C56_xx` |
| C57 | DataPasienBoundary | Kelas | `src/src/main/java/sehati/dataaccess/DataPasienBoundary.java` | Q-C57-01 s.d. Q-C57-11 sebagai konstanta `Q_C57_xx`, Algo-C57-01 pada `polaPencarian()`. Pengerjaan Humam menggantikan Steven (#5). |
| C58 | DataKunjunganBoundary | Kelas | `src/src/main/java/sehati/dataaccess/DataKunjunganBoundary.java` | Q-C58-01 s.d. Q-C58-12 sebagai konstanta `Q_C58_xx`, Algo-C58-01 pada `ambilAntrean()` dan `simpanAntrean()`. Pengerjaan Humam menggantikan Steven (#5). |
| C59 | DataKlinisBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataKlinisBoundary.java` - `DataKlinisBoundary` | Atribut `koneksi` diterima lewat konstruktor dari `Manager`. Bagian pemeriksaan: `cariKodeICD10`, `simpanPemeriksaan`, `ambilPemeriksaan`, `simpanJadwalKontrol`, dan `ambilJadwalKontrol` menjalankan konstanta `Q_C59_11` sampai `Q_C59_17` lewat `PreparedStatement`. `simpanPemeriksaan` mewujudkan Algo-C59-02 dan melempar `IllegalStateException` bila baris lama sudah terkunci. Tambahan: `ambilDiagnosis()` dengan kueri Q-C59-18 untuk memuat draf (B.1). |
| C60 | DataPantauBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C61 | DataFarmasiBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataFarmasiBoundary.java` - `DataFarmasiBoundary` | Atribut `koneksi` diterima lewat konstruktor dari `Manager`. Sepuluh operasi publik menjalankan konstanta `Q_C61_01` sampai `Q_C61_15` lewat `PreparedStatement`. `ambilObat`, `ambilBets`, dan `kunciResep` memakai `FOR UPDATE` (KF23, KNF11). `simpanPenerimaan` mewujudkan Algo-C61-01 dan melempar `IllegalArgumentException("nomorBets")` untuk bets ganda. `simpanStok`, `simpanPenerimaan`, dan `simpanResep` memakai pola `transaksiSendiri` (aturan 1 Subbab 5.56). Item resep dibaca ke `ResepEntity.daftarItem`. Tambahan UC07: `ambilPasienResep()` menjalankan `Q_C61_16` agar identitas pasien seluruh antrean diambil dalam satu kueri (B.1). |
| C62 | DataLaporanBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |
| C63 | DataSinkronisasiBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataSinkronisasiBoundary.java` - `DataSinkronisasiBoundary` | Q-C63-01 s.d. Q-C63-03 sebagai konstanta `Q_C63_xx`; `simpanBundel`, `ambilTertunda`, `perbaruiBundel` lewat `PreparedStatement`; `tulisArsipTerenkripsi` mewujudkan Algo-C63-01 (PBKDF2 600k + AES-GCM) |
| C64 | DataPencadanganBoundary | *[Bentuk]* | *[File - nama]* | *[Keterangan]* |

Catatan:
- Satu kelas perancangan boleh diwujudkan oleh beberapa file/unit, dan satu file/unit boleh mewujudkan beberapa kelas perancangan. Tuliskan semuanya.
- Kelas perancangan yang **tidak diimplementasikan** atau **digabung/dipecah** tetap dicantumkan, dan alasannya dicatat di Bagian B.1.
