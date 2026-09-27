<h1>
IF2150 REKAYASA PERANGKAT LUNAK
<br>
TUGAS 5
<br>
SPESIFIKASI KEBUTUHAN PERANGKAT LUNAK (SKPL)
</h1>
<br>

## SEHATI - Sistem Elektronik Pelayanan Kesehatan Terintegrasi

### Untuk: Made Branenda Jordhy

Dipersiapkan oleh:
| Informasi | Keterangan |
| --- | --- |
| Kelas | K02 |
| Kelompok | G09  |

| NIM | Nama |
|---|---|
| 13525008 | Malik Arsyafiandra Madani |
| 13525044 | Steven Vanako |
| 13525071 | Muhammad Adnan Kurniawan |
| 13525074 | Axeleon Justin Algianto |
| 13525110 | Fachry Azriel Fajdwani |
---

## Daftar Perubahan

| Revisi | Deskripsi |
| :--- | :--- |
| A | Pembuatan awal dokumen Spesifikasi Kebutuhan Perangkat Lunak (SKPL) untuk SEHATI. |

<br>

# BAB 1: Pendahuluan

## 1.1 Tujuan Penulisan Dokumen
Dokumen Spesifikasi Kebutuhan Perangkat Lunak (SKPL) ini disusun sebagai acuan utama dalam pengembangan aplikasi SEHATI (Sistem Elektronik Pelayanan Kesehatan Terintegrasi). Tujuan dokumen ini adalah mendefinisikan dengan jelas dan spesifik seluruh kebutuhan perangkat lunak, baik fungsional maupun non-fungsional, serta batasan-batasan sistem. Dokumen ini akan digunakan oleh pengembang (*developer*) sebagai panduan implementasi, penguji (*tester*) sebagai basis pengujian kualitas, serta pemangku kepentingan (*stakeholder*) seperti pihak puskesmas untuk validasi fungsionalitas sistem akhir.

## 1.2 Lingkup Masalah
SEHATI merupakan aplikasi *desktop* pengelolaan pelayanan rawat jalan puskesmas yang menyatukan seluruh rantai pelayanan mulai dari pendaftaran, skrining, pemeriksaan, hingga farmasi. Aplikasi ini dikembangkan untuk mengatasi permasalahan fragmentasi rekam medis dan rendahnya deteksi dini pada fasilitas kesehatan dengan menerapkan pemantauan risiko kesehatan longitudinal pasien secara aktif, sembari memastikan sistem tetap berjalan penuh di lingkungan dengan keterbatasan koneksi internet (*offline-first*) dan mampu menyinkronkan data ke platform nasional SATUSEHAT.

## 1.3 Definisi, Istilah, dan Singkatan

Tabel 1.3. Definisi Istilah dan Singkatan

| Singkatan, Akronim, atau Istilah | Penjelasan |
| :--- | :--- |
| P/L | Perangkat Lunak. |
| SKPL | Spesifikasi Kebutuhan Perangkat Lunak. |
| KF | Kebutuhan Fungsional. |
| KNF | Kebutuhan Non-Fungsional. |
| UC | Use Case. |
| EARS | *Easy Approach to Requirements Syntax*, pola penulisan kebutuhan agar konsisten dan mudah diuji. |
| RME | Rekam Medis Elektronik. |
| PTM | Penyakit Tidak Menular (seperti hipertensi, obesitas, diabetes). |
| SATUSEHAT | Platform integrasi data kesehatan nasional milik Kemenkes RI. |
| HL7 FHIR | *Health Level Seven Fast Healthcare Interoperability Resources*, standar pertukaran data kesehatan yang dipakai oleh SATUSEHAT. |

## 1.4 Aturan Penomoran

Tabel 1.4. Aturan Penomoran

| Hal/Bagian | Penomoran | Keterangan |
| :--- | :--- | :--- |
| Kebutuhan Fungsional | KFXX | XX adalah dua digit angka berurutan |
| Kebutuhan Non-Fungsional | KNFXX | XX adalah dua digit angka berurutan |
| Aktor | AXX | XX adalah dua digit angka berurutan |
| Use Case | UCXX | XX adalah dua digit angka berurutan |
| Kelas | CXX | XX adalah dua digit angka berurutan |

## 1.5 Referensi
1. Dokumen *Topic Brainstorming* Kelompok K02 G09.
2. Dokumen *Requirement Gathering* Kelompok K02 G09.
3. Dokumen *Use Case & Scenario Use Case* Kelompok K02 G09.
4. Dokumen *Class Diagram* Kelompok K02 G09.

## 1.6 Deskripsi Umum Dokumen (Ikhtisar)
Dokumen SKPL ini disusun ke dalam beberapa bab dengan sistematika sebagai berikut:
- **BAB 1: Pendahuluan**, menguraikan tujuan dokumen, lingkup masalah, definisi istilah, aturan penomoran, referensi, dan deskripsi umum dokumen.
- **BAB 2: Deskripsi Perangkat Lunak**, menjelaskan deskripsi umum sistem, pengguna dan kebutuhan, batasan, serta lingkungan operasi perangkat lunak.
- **BAB 3: Deskripsi Kebutuhan Perangkat Lunak**, merincikan Kebutuhan Fungsional (KF) dan Kebutuhan Non-Fungsional (KNF).
- **BAB 4: Pemodelan Use Case**, memaparkan identifikasi aktor, daftar *use case*, diagram *use case*, serta skenario dari masing-masing *use case*.
- **BAB 5: Pemodelan Kelas**, menjabarkan identifikasi kelas, diagram kelas per *use case*, serta diagram kelas secara keseluruhan.
- **BAB 6: Traceability**, berisi matriks penelusuran yang menghubungkan antara Kelas, *Use Case*, dan Kebutuhan Fungsional.

---

# BAB 2: Deskripsi Perangkat Lunak

## 2.1 Deskripsi Umum Sistem
SEHATI menangani pelayanan rawat jalan dari ujung ke ujung melalui beberapa tahap layanan: 
1. **Pendaftaran di loket**: Petugas menelusuri data pasien, mendaftar, lalu membuka kunjungan antrean.
2. **Skrining**: Perawat mengukur tanda vital, lalu sistem menandai risiko kondisi pasien otomatis.
3. **Pemeriksaan**: Dokter mencatat anamnesis, diagnosis, tindakan, dan menyusun resep elektronik.
4. **Farmasi**: Petugas menyiapkan resep lalu menyerahkan obat pada pasien dengan pemotongan stok secara otomatis.
Di luar pelayanan, terdapat alur pendukung seperti penyusunan laporan, pengurusan stok, tindak lanjut daftar pantau pasien berisiko, serta sinkronisasi data rekam medis ke SATUSEHAT.

<p align="center">
<img alt="Activity Diagram Pelayanan Rawat Jalan" src="../M1/assets/diagram/diagram-act-1.avif" width="70%">
</p>
<p align="center">
<i>Gambar 1. Activity Diagram Alur Pelayanan Rawat Jalan SEHATI</i>
</p>

## 2.2 Deskripsi Umum Perangkat Lunak
SEHATI merupakan aplikasi *desktop offline-first* yang mengelola secara penuh pendaftaran, pemeriksaan, peresepan elektronik, hingga pengelolaan obat di puskesmas. Aplikasi dirancang untuk menutupi kesenjangan tidak adanya pemantauan riwayat kondisi antar-waktu dengan melacak pasien berisiko. SEHATI berinteraksi dengan API dari **SATUSEHAT** Kementerian Kesehatan; sistem bekerja mencatat rekam medis pada basis data lokal dan mengirimkan kumpulan data (bundel HL7 FHIR) secara *asynchronous* ketika koneksi internet puskesmas tersedia.

## 2.3 Pengguna dan Kebutuhan Pengguna Perangkat Lunak

| Pengguna | Kebutuhan |
| :--- | :--- |
| **Petugas Administrasi** | Membutuhkan fitur manajemen data pasien yang cepat, pendaftaran kunjungan, manajemen *master data*, pembuatan laporan otomatis, dan fasilitas kontrol sinkronisasi data ke SATUSEHAT. |
| **Tenaga Klinis** | Membutuhkan tampilan riwayat pasien yang utuh dan komprehensif pada satu layar, fasilitas peringatan dini atau penanda batas normal (*skrining*), penulisan resep dengan validasi stok obat langsung, serta fitur pemantauan pasien berisiko. |
| **Petugas Farmasi** | Membutuhkan antrean resep yang terbaca jelas dari ruang periksa, validasi status penyerahan obat, pengurusan persediaan dan inventarisasi stok (*restock*), serta peringatan obat kedaluwarsa. |

## 2.4 Batasan Perangkat Lunak
1. Aplikasi harus berjalan sebagai aplikasi desktop (tanpa mewajibkan akses web bagi operasional lokal) untuk menjamin pelayanan tidak terhenti akibat ketiadaan koneksi (*offline-first*).
2. Format struktur data pengiriman rekam medis harus mengikuti standar HL7 FHIR yang ditentukan oleh regulasi SATUSEHAT Kementerian Kesehatan.
3. Penanda risiko hanya berfungsi sebagai instrumen pengingat atau deteksi dini, dan **tidak** mengambil alih kewenangan klinis tenaga kesehatan.
4. Akses basis data dibatasi oleh fitur otorisasi per *role* pengguna demi menjaga kerahasiaan rekam medis.

## 2.5 Lingkungan Operasi Perangkat Lunak

| Komponen | Spesifikasi |
| :--- | :--- |
| **Aplikasi / Client** | Aplikasi Desktop berbasis GUI (menggunakan JavaFX/Electron atau *framework desktop* sejenis). |
| **DBMS** | Basis Data Relasional yang beroperasi secara lokal (misal: PostgreSQL / MySQL). |
| **Sistem Operasi** | *Cross-platform* pada lingkungan desktop seperti Windows 10/11 atau distribusi Linux modern. |
| **Lainnya** | Komponen utilitas penjadwalan *backup* otomatis dan komponen *worker* sinkronisasi HTTP/REST API terpisah. |

---

# BAB 3: Deskripsi Kebutuhan Perangkat Lunak

## 3.1 Kebutuhan Fungsional (KF)
Salin ulang **seluruh Kebutuhan Fungsional (KF)** versi terbaru dari BAB 2.1 dokumen *Class Diagram* (sudah versi final dan sudah memakai format EARS). Pastikan ID Kebutuhan (kolom "ID Kebutuhan") juga konsisten dengan ID pada tabel Pemetaan Kebutuhan di dokumen *Requirement Gathering*.

Tabel 3.1. Kebutuhan Fungsional

| ID KF | ID Kebutuhan | Penjelasan |
| :--- | :--- | :--- |
| *KF01* | *R01* | *Ketika pelanggan membuka halaman katalog, sistem harus menampilkan daftar produk yang tersedia.* |
| *KF02* | *R02* | *Ketika pelanggan memilih "Tambah ke Keranjang" pada suatu produk, sistem harus menyimpan produk tersebut ke dalam keranjang pelanggan.* |
| *KF03* | *R03* | *Ketika pelanggan menekan tombol checkout, sistem harus menampilkan pilihan metode pembayaran yang tersedia.* |
| *KF04* | *R04* | *Ketika pelanggan memilih metode pembayaran, sistem harus mengirimkan permintaan otorisasi beserta nominal tagihan dan ID pesanan ke payment gateway (dummy).* |
| *KF05* | *R04* | *Ketika payment gateway (dummy) mengembalikan status pembayaran berhasil, sistem harus memperbarui status pesanan menjadi "Lunas" dan menampilkan notifikasi pembayaran berhasil.* |
| *KF06* | *R05* | *Ketika pelanggan membuka menu riwayat pesanan, sistem harus menampilkan daftar pesanan beserta statusnya.* |
| *KFXX* | *...* | *...* |

## 3.2 Kebutuhan Non-Fungsional (KNF)
Salin ulang Kebutuhan Non-Fungsional dari BAB 2.5 dokumen *Requirement Gathering*, sesuaikan ID Kebutuhan (kolom "ID Kebutuhan") apabila terjadi perubahan penomoran pada BAB 3.1 di atas.

Tabel 3.2. Kebutuhan Non-Fungsional

| ID KNF | ID Kebutuhan | Parameter | Deskripsi Kebutuhan |
| :--- | :--- | :--- | :--- |
| *KNF01* | *R03* | *Reliability* | *Proses transaksi pembayaran harus memenuhi prinsip ACID untuk mencegah terjadinya data tersangkut (lost update) apabila terjadi kegagalan jaringan di tengah proses.* |
| *KNF02* | *R04* | *Security* | *Sistem harus mengenkripsi PIN atau password pengguna menggunakan algoritma SHA-256 sebelum data dikirimkan ke server, serta tidak menyimpannya dalam bentuk plain-text di database.* |
| *...* | *...* | *...* | *...* |

<sub>*Silakan pilih parameter yang relevan dengan P/L kalian (Availability, Reliability, Ergonomy, Portability, Memory, Response time, Safety, Security, dsb), tidak perlu semua parameter diisi. Lihat kembali dokumen Requirement Gathering untuk penjelasan tiap parameter.*<sub>

---

# BAB 4: Pemodelan Use Case

## 4.1 Identifikasi Aktor
Salin ulang daftar aktor final dari BAB 3.1 dokumen *Use Case & Scenario Use Case* atau *Class Diagram*. Tambahkan ID Aktor mengikuti Aturan Penomoran pada 1.4.

| ID Aktor | Aktor | Deskripsi |
| :--- | :--- | :--- |
| *A01* | *Pelanggan* | *Pengguna yang memesan produk, mengelola keranjang, dan menyelesaikan pembayaran melalui sistem.* |
| *...* | *...* | *...* |

## 4.2 Identifikasi Use Case
Salin ulang daftar Use Case versi terbaru dari BAB 3.2 dokumen *Class Diagram*, pastikan seluruh ID KF yang dirujuk sudah sesuai dengan tabel pada 3.1.

| ID UC | Nama Use Case | Deskripsi Singkat | Aktor | ID KF |
| :--- | :--- | :--- | :--- | :--- |
| *UC01* | *Memesan Produk* | *Pelanggan memilih produk hingga pesanan tersimpan di sistem.* | *Pelanggan* | *KF01, KF02* |
| *UC02* | *Melihat Keranjang* | *Pelanggan melihat daftar item yang telah dipilih sebelum checkout.* | *Pelanggan* | *KF02* |
| *UC03* | *Melakukan Pembayaran* | *Pelanggan menyelesaikan pembayaran atas pesanan yang dibuat.* | *Pelanggan* | *KF03, KF04, KF05* |
| *UC04* | *Memilih Metode Pembayaran* | *Pelanggan memilih metode pembayaran alternatif (kartu atau e-wallet).* | *Pelanggan* | *KF03* |
| *UC05* | *Melihat Riwayat Pesanan* | *Pelanggan melihat daftar pesanan yang pernah dibuat beserta statusnya.* | *Pelanggan* | *KF06* |
| *...* | *...* | *...* | *...* | *...* |

## 4.3 Use Case Diagram
Salin ulang Use Case Diagram dari BAB 3.3 dokumen *Use Case & Scenario Use Case* atau *Class Diagram* (gunakan versi paling akhir/terbaru apabila terdapat perubahan).

<p align="center">
<img alt="Contoh Use Case Diagram" src="./assets/diagram/contoh-uc-diagram.webp" width="70%">
</p>
<p align="center">
<i>Gambar 2. Contoh Use Case Diagram</i>
</p>

## 4.4 Skenario Use Case
Salin ulang skenario **setiap** use case (skenario normal dan alternatif) dari BAB 3.4 dokumen *Use Case & Scenario Use Case*, sesuaikan dengan daftar UC final pada 4.2. Jika use case melibatkan lebih dari satu aktor manusia yang benar-benar berinteraksi langsung (misalnya *Kasir* yang memverifikasi transaksi setelah *Pelanggan* membayar), tambahkan kolom aksi tersendiri untuk aktor tersebut di samping kolom "Reaksi Perangkat Lunak". Sistem eksternal otomatis seperti *payment gateway* **bukan aktor**, sehingga interaksinya cukup dituliskan sebagai bagian dari "Reaksi Perangkat Lunak", bukan kolom aktor terpisah.

### 4.4.1 Skenario UC01

**Nama Use Case:** *Memesan Produk*

**Skenario Normal**

| No | Aksi Aktor | Reaksi Perangkat Lunak |
| :--- | :--- | :--- |
| 1 | *Pelanggan memilih produk dari katalog* | *Sistem menampilkan detail produk dan menambahkannya ke keranjang* |
| 2 | *Pelanggan menekan tombol checkout* | *Sistem membuat pesanan baru dari isi keranjang dan menampilkan ringkasan pesanan* |
| ... | *...* | *...* |

**Skenario Alternatif 1: Produk Tidak Tersedia**

| No | Aksi Aktor | Reaksi Perangkat Lunak |
| :--- | :--- | :--- |
| 1 | *Pelanggan memilih produk dari katalog* | *Sistem menampilkan pesan "Produk tidak tersedia" karena stok habis* |
| 2 | *Pelanggan memilih produk lain* | *Sistem kembali ke langkah 1 skenario normal* |
| ... | *...* | *...* |

<sub>*Lanjutkan pola 4.4.x ini untuk setiap ID UC pada 4.2, sampai seluruh use case memiliki skenarionya masing-masing.*<sub>

---

# BAB 5: Pemodelan Kelas

## 5.1 Identifikasi Kelas
Salin ulang seluruh kelas yang telah diidentifikasi dari BAB 4.1 dokumen *Class Diagram*.

| ID Kelas | Nama Kelas | Deskripsi Kelas | ID Use Case |
| :--- | :--- | :--- | :--- |
| *C01* | *Pelanggan* | *Menyimpan data akun pelanggan yang membuat pesanan.* | *UC01, UC05* |
| *C02* | *Pesanan* | *Menyimpan data pesanan beserta status pembayarannya.* | *UC01, UC03, UC05* |
| *C03* | *Keranjang* | *Menyimpan sementara item yang dipilih sebelum checkout.* | *UC01, UC02* |
| *...* | *...* | *...* | *...* |

## 5.2 Diagram Kelas per Use Case
Salin ulang diagram kelas untuk setiap use case dari BAB 4.2 dokumen *Class Diagram*, lengkap dengan tabel atribut dan metode/operasinya.

### 5.2.1 Use Case UC01

**Nama Use Case:** *Memesan Produk*

<p align="center">
<img alt="Contoh Class Diagram" src="./assets/diagram/contoh-class-diagram.webp" width="70%">
</p>
<p align="center">
<i>Gambar 3. Contoh Diagram Kelas Use Case UC01</i>
</p>

| ID Kelas | Nama Kelas | Atribut | Metode/Operasi |
| :--- | :--- | :--- | :--- |
| *C02* | *Pesanan* | *idPesanan, total, status* | *buatPesanan(), hitungTotal()* |
| *C03* | *Keranjang* | *daftarItem* | *tambahItem(), checkout()* |
| *...* | *...* | *...* | *...* |

> Lanjutkan pola **5.2.x** untuk setiap use case pada 4.2.

## 5.3 Diagram Kelas Keseluruhan
Gabungkan seluruh kelas dan hubungan antarkelas dari BAB 4.3 dokumen *Class Diagram* menjadi satu diagram kelas keseluruhan. Pastikan tidak ada kelas yang terduplikasi atau tertinggal.

<p align="center">
<img alt="Contoh Class Diagram Keseluruhan" src="./assets/diagram/contoh-class-diagram.webp" width="70%">
</p>
<p align="center">
<i>Gambar 4. Contoh Diagram Kelas Keseluruhan</i>
</p>

| ID Kelas | Nama Kelas | Atribut | Metode/Operasi |
| :--- | :--- | :--- | :--- |
| *C01* | *Pelanggan* | *idPelanggan, nama, email* | *lihatRiwayatPesanan()* |
| *C02* | *Pesanan* | *idPesanan, total, status* | *hitungTotal(), perbaruiStatus()* |
| *...* | *...* | *...* | *...* |

---

# BAB 6: Traceability
Salin ulang tabel Traceability dari BAB 5 dokumen *Class Diagram*, cocokkan setiap Kebutuhan Fungsional, Use Case, dan Kelas yang saling terkait.

| ID Kelas | ID Use Case | ID KF |
| :--- | :--- | :--- |
| *C01* | *UC01, UC05* | *KF01, KF06* |
| *C02* | *UC01, UC03, UC05* | *KF01, KF02, KF05, KF06* |
| *C03* | *UC01, UC02* | *KF01, KF02* |
| *...* | *...* | *...* |

---

# Referensi
- Diagram UML: [https://www.drawio.com/](https://www.drawio.com/), [https://staruml.io/](https://staruml.io/)
