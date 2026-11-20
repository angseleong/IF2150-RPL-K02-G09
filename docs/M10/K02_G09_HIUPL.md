<h1>
IF2150 REKAYASA PERANGKAT LUNAK
<br>
TUGAS 10
<br>
HASIL IMPLEMENTASI DAN UJI PERANGKAT LUNAK (HIUPL)
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

## Daftar Isi

- [BAB 1: Pendahuluan](#bab-1-pendahuluan)
  - [1.1 Tujuan Penulisan Dokumen](#11-tujuan-penulisan-dokumen)
  - [1.2 Lingkup Masalah](#12-lingkup-masalah)
  - [1.3 Definisi, Istilah, dan Singkatan](#13-definisi-istilah-dan-singkatan)
  - [1.4 Aturan Penomoran](#14-aturan-penomoran)
  - [1.5 Referensi](#15-referensi)
  - [1.6 Deskripsi Umum Dokumen (Ikhtisar)](#16-deskripsi-umum-dokumen-ikhtisar)
- [BAB 2: Deskripsi Umum Perangkat Lunak](#bab-2-deskripsi-umum-perangkat-lunak)
- [BAB 3: Implementasi](#bab-3-implementasi)
  - [3.1 Lingkungan Implementasi](#31-lingkungan-implementasi)
  - [3.2 Implementasi Modul](#32-implementasi-modul)
  - [3.3 Implementasi Antarmuka](#33-implementasi-antarmuka)
  - [3.4 File Lain](#34-file-lain)
- [BAB 4: Hasil Pengujian](#bab-4-hasil-pengujian)
  - [4.1 Strategi dan Kedalaman Pengujian Kelas](#41-strategi-dan-kedalaman-pengujian-kelas)
  - [4.2 Pengujian Unit Kelas](#42-pengujian-unit-kelas)
  - [4.3 Pengujian Integrasi](#43-pengujian-integrasi)
  - [4.4 Pengujian Use Case](#44-pengujian-use-case)
  - [4.5 Pengujian Kebutuhan Non-Fungsional (KNF)](#45-pengujian-kebutuhan-non-fungsional-knf)
  - [4.6 Tangkapan Layar dan Bukti Hasil Pengujian](#46-tangkapan-layar-dan-bukti-hasil-pengujian)
- [BAB 5: Traceability](#bab-5-traceability)

---

## Daftar Perubahan

| Revisi | Deskripsi |
| :--- | :--- |
| A | Versi pertama HIUPL. BAB 1 dan BAB 2 disusun dari DPPL Tugas 8 dan SKPL Tugas 5, ditambah istilah dan pola ID pengujian. BAB 3 merangkum implementasi pada tag `v9.0` (Tugas 9) beserta perbaikan kode hasil pengujian (Tabel 3.5). BAB 4 dan BAB 5 berisi hasil pengujian dan matriks kerunutan yang baru disusun pada dokumen ini. |

---

# BAB 1: Pendahuluan

## 1.1 Tujuan Penulisan Dokumen
Dokumen ini memuat Hasil Implementasi dan Uji Perangkat Lunak (HIUPL) SEHATI. HIUPL melanjutkan DPPL (Tugas 7 dan 8) dan Laporan Implementasi (Tugas 9): dokumen ini mencatat elemen perangkat lunak yang sudah dibangun, lalu membuktikan lewat pengujian unit, integrasi, *use case*, dan kebutuhan non-fungsional bahwa hasilnya sesuai dengan SKPL dan DPPL. Setiap hasil pengujian disertai bukti, dan setiap KF ditelusuri sampai ke kelas, berkas, dan *test case* yang memverifikasinya. Pembaca dokumen ini adalah asisten dan dosen IF2150 yang menilai kesesuaian implementasi, anggota kelompok yang memperbaiki temuan pengujian, serta pengembang lanjutan yang perlu mengetahui bagian SEHATI yang sudah teruji.

## 1.2 Lingkup Masalah
SEHATI adalah aplikasi *desktop* untuk pelayanan rawat jalan puskesmas, dari pendaftaran di loket sampai penyerahan obat di apotek. Riwayat pasien yang tersebar di berkas kertas dihimpun ke satu rekam medis elektronik, hasil pengukuran pasien dibandingkan antar-kunjungan, dan pasien yang mengarah ke hipertensi, obesitas, atau diabetes dimasukkan ke daftar pantau. Data disimpan di basis data PostgreSQL lokal puskesmas agar pelayanan tetap berjalan tanpa internet. SEHATI berhubungan dengan dua sistem di luarnya: platform SATUSEHAT milik Kementerian Kesehatan, yang menerima bundel HL7 FHIR ketika koneksi tersedia, dan media penyimpanan terpisah tempat arsip pencadangan `pg_dump` disimpan.

## 1.3 Definisi, Istilah, dan Singkatan

Tabel 1.3. Definisi Istilah dan Singkatan

| Singkatan, Akronim, atau Istilah | Penjelasan |
| :--- | :--- |
| P/L | Perangkat Lunak. |
| SKPL | Spesifikasi Kebutuhan Perangkat Lunak (Tugas 5), dokumen yang merangkum kebutuhan yang harus dipenuhi SEHATI. |
| APL | Arsitektur Perangkat Lunak (Tugas 6). |
| DPPL | Deskripsi Perancangan Perangkat Lunak (Tugas 7 dan 8), dokumen yang menjelaskan rancangan arsitektur, kelas, algoritma, kueri, dan antarmuka SEHATI. |
| HIUPL | Hasil Implementasi dan Uji Perangkat Lunak, dokumen ini. |
| KF / KNF | Kebutuhan Fungsional / Kebutuhan Non-Fungsional. |
| UC | *Use Case*. |
| TC | *Test Case*, satu kasus uji dengan prosedur, masukan, dan hasil yang diharapkan untuk satu skenario. |
| UT | *Unit Test*, *test case* tingkat kelas yang menguji satu operasi. |
| INT | *Integration Test*, *test case* yang menguji interaksi antarkomponen melewati batas lapisan, misalnya `Manager` ke `DataBoundary` ke basis data. |
| IK | Antarmuka Komponen, titik pertemuan dua komponen yang diuji pada pengujian integrasi. Dipisahkan dari I-XX yang sudah dipakai untuk layar. |
| Normal / Batas / Negatif | Jenis kasus uji unit. Normal memakai masukan biasa, batas memakai nilai tepat di ambang aturan (misalnya stok sama dengan jumlah), negatif memakai masukan yang harus ditolak. |
| *Boundary* | Kasus uji pada nilai batas masukan, misalnya nilai minimum, maksimum, atau tepat di ambang aturan. Berbeda dengan kelas `DataBoundary` pada arsitektur SEHATI. |
| *Coverage* | Persentase baris (*line*) dan cabang (*branch*) kode yang dieksekusi oleh pengujian, diukur dengan JaCoCo. |
| Mendalam / Sedang / Tidak perlu | Tingkat kedalaman pengujian kelas pada Subbab 4.1. |
| *Mock* | Objek tiruan pengganti komponen asli. Pengujian integrasi SEHATI tidak memakai *mock*. |
| Basis data uji | Basis data PostgreSQL yang dibuat dari `src/db/schema.sql` dan `src/db/seed.sql` khusus untuk pengujian, terpisah dari basis data pemakaian. |
| JUnit 5 | Kerangka pengujian unit untuk Java yang dipakai seluruh test SEHATI. |
| JaCoCo | *Java Code Coverage*, alat pengukur *coverage* yang terpasang pada `pom.xml`. |
| JDBC | *Java Database Connectivity*, antarmuka Java untuk menjalankan kueri SQL ke DBMS. |
| Transaksi | Rangkaian kueri yang dijalankan sebagai satu kesatuan: seluruhnya disimpan (COMMIT) atau seluruhnya dibatalkan (ROLLBACK). |
| FEFO | *First Expired, First Out*, aturan pengeluaran obat yang mendahulukan bets dengan tanggal kedaluwarsa paling dekat. |
| bcrypt | Fungsi *hash* kata sandi yang sengaja lambat dan memakai *salt*, dipakai untuk memenuhi KNF04. |
| SATUSEHAT | Platform integrasi data kesehatan nasional milik Kemenkes RI. |
| HL7 FHIR | *Health Level Seven Fast Healthcare Interoperability Resources*, standar pertukaran data kesehatan yang dipakai SATUSEHAT. |
| ICD-10 | *International Classification of Diseases, 10th Revision*, sistem kode diagnosis standar WHO. |
| NIK | Nomor Induk Kependudukan, nomor identitas 16 digit pada KTP. |

## 1.4 Aturan Penomoran
Dokumen ini memakai pola ID yang sama dengan SKPL, APL, dan DPPL. Pola pengujian baru muncul pada dokumen ini. ID *test case* unit memuat ID kelasnya dan ID *test case* use case memuat ID *use case*-nya, sehingga nomor tidak bertabrakan walaupun bagian yang berbeda dikerjakan terpisah. Antarmuka antarkomponen untuk pengujian integrasi diberi pola IK-XX karena pola I-XX sudah dipakai untuk layar pada DPPL BAB 6.

Tabel 1.4. Aturan Penomoran

| Hal/Bagian | Penomoran | Keterangan |
| :--- | :--- | :--- |
| Kebutuhan Fungsional | KFXX | Sesuai SKPL |
| Kebutuhan Non-Fungsional | KNFXX | Sesuai SKPL |
| Aktor | AXX | Sesuai SKPL |
| *Use Case* | UCXX | Sesuai SKPL |
| Kelas | CXX | Sesuai DPPL; C01 sampai C55 dari SKPL, C56 sampai C64 untuk kelas akses data |
| Algoritma | Algo-CXX-YY | Sesuai DPPL |
| Kueri | Q-CXX-YY | Sesuai DPPL |
| Layar antarmuka | I-XX | Sesuai DPPL BAB 6 |
| Antarmuka antarkomponen | IK-XX | XX adalah nomor urut antarmuka pada Tabel 4.3.1 |
| *Test Case* Unit | CXX-UTYY | XX adalah nomor kelas, YY adalah nomor urut *test case* unit pada kelas tersebut |
| *Test Case* Integrasi | INT-TCYY | YY adalah nomor urut *test case* integrasi. Setiap IK mendapat nomor berurutan pada Tabel 4.3.1 |
| *Test Case* Use Case | UCXX-TCYY | XX adalah nomor *use case*, YY adalah nomor urut *test case* pada *use case* tersebut |
| *Test Case* KNF | KNFXX-TCYY | XX adalah nomor KNF, YY adalah nomor urut *test case* pada KNF tersebut |

## 1.5 Referensi
1. Kelompok K02-G09. (2026). *Spesifikasi Kebutuhan Perangkat Lunak SEHATI* (Tugas 5), `docs/M5/K02_G09_SKPL.md`. Sumber KF, KNF, *use case*, dan skenario yang diuji pada Subbab 4.4 dan 4.5.
2. Kelompok K02-G09. (2026). *Arsitektur Perangkat Lunak SEHATI* (Tugas 6), `docs/M6/K02_G09_APL.md`. Sumber *Layered Architecture*, *Logical View*, dan *Physical View*.
3. Kelompok K02-G09. (2026). *Deskripsi Perancangan Perangkat Lunak SEHATI* (Tugas 8), `docs/M8/K02_G09_DPPL2.md`. Sumber Tabel 2.3 komponen, Tabel 4.1 kelas perancangan, algoritma, kueri, dan rancangan antarmuka I-01 sampai I-15.
4. Kelompok K02-G09. (2026). *Implementasi Perangkat Lunak SEHATI* (Tugas 9), `docs/M9/K02_G09_Implementasi.md`. Sumber Tabel B.1 perubahan dari DPPL dan Tabel B.5 kerunutan kelas ke berkas implementasi.
5. Sommerville, I. (2016). *Software Engineering* (10th ed.). Pearson. Bab 8 (*Software Testing*).
6. JUnit Team. *JUnit 5 User Guide*. https://junit.org/junit5/docs/current/user-guide/. Acuan anotasi `@Test`, `@Nested`, `@DisplayName`, dan `@TestMethodOrder`.
7. Mountainminds GmbH & Co. KG dan kontributor. *JaCoCo Documentation*. https://www.jacoco.org/jacoco/trunk/doc/. Acuan definisi *line* dan *branch coverage*.
8. Apache Software Foundation. *Maven Surefire Plugin*. https://maven.apache.org/surefire/maven-surefire-plugin/. Menjalankan test dan menyusun laporannya.
9. Dokumentasi teknologi: OpenJFX, *JavaFX 21 Documentation* (https://openjfx.io/); Oracle, *Java SE 21 API Specification* (https://docs.oracle.com/en/java/javase/21/docs/api/); PostgreSQL Global Development Group, *PostgreSQL 15 Documentation* (https://www.postgresql.org/docs/15/); HL7, *FHIR R4* (https://hl7.org/fhir/R4/); Kemenkes RI, *SATUSEHAT Platform* (https://satusehat.kemkes.go.id/platform/docs/id/fhir/).

## 1.6 Deskripsi Umum Dokumen (Ikhtisar)
HIUPL ini terdiri atas lima bab. BAB 1 memuat tujuan, lingkup, istilah, aturan penomoran termasuk pola ID pengujian, dan referensi. BAB 2 menggambarkan SEHATI beserta batasnya terhadap SATUSEHAT dan media cadangan, lalu memodelkan dua belas *use case*-nya. BAB 3 mencatat lingkungan implementasi, 64 kelas perancangan, 15 layar, dan berkas pendukung beserta lokasinya. BAB 4 menetapkan tingkat kedalaman pengujian setiap kelas, lalu melaporkan hasil pengujian unit, integrasi, *use case*, dan KNF beserta buktinya. BAB 5 menelusuri setiap KF dan KNF ke *use case*, kelas, berkas, dan *test case* sehingga terlihat KF mana yang sudah terbukti terpenuhi.

<br>

---

# BAB 2: Deskripsi Umum Perangkat Lunak

SEHATI adalah aplikasi *desktop offline-first* untuk pelayanan rawat jalan puskesmas. Puskesmas memasangnya pada komputer loket, ruang periksa, dan apotek, dan seluruhnya memakai satu basis data PostgreSQL pada komputer server di jaringan lokal. Pasien melewati empat tahap layanan: pendaftaran di loket oleh Petugas Administrasi, skrining tanda vital oleh perawat, pemeriksaan dan peresepan oleh dokter, lalu penyerahan obat oleh Petugas Farmasi. Di luar jam pelayanan, petugas menyusun laporan periodik, mencatat obat masuk, menindaklanjuti pasien di daftar pantau, dan mengelola akun serta data master.

Gambar 2.1 memperlihatkan batas sistem SEHATI. Bagian di dalam batas adalah tiga jenis komputer *client* yang menjalankan aplikasi JavaFX dan satu komputer server yang menjalankan PostgreSQL 15, seluruhnya terhubung lewat LAN puskesmas. Pelayanan hanya membutuhkan LAN, sehingga tetap berjalan ketika internet putus (KF22, KNF09). Di luar batas ada dua sistem eksternal yang digambar dengan garis putus-putus. Pertama, platform SATUSEHAT yang hanya dihubungi `SATUSEHATGateway` lewat HTTPS saat sinkronisasi (KF20). Kedua, media penyimpanan terpisah tempat `DataPencadanganBoundary` menulis arsip `pg_dump` (KF21). Selama pengujian, SATUSEHAT dijalankan dalam mode simulasi karena kelompok belum memiliki kredensial *sandbox*. Mode ini aktif bila `SATUSEHAT_URL` kosong (Tabel B.1 nomor 63 Laporan Implementasi).

<p align="center">
<img alt="System Overview SEHATI (Physical View)" src="../M8/assets/diagram/diagram-physical-view.svg" width="100%">
</p>
<p align="center">
<i>Gambar 2.1. System Overview SEHATI: komputer client, server basis data, dan sistem eksternal (Physical View DPPL)</i>
</p>

Gambar 2.2 memodelkan dua belas *use case* SEHATI dengan tiga aktor manusia: Petugas Administrasi (A01), Tenaga Klinis yang mencakup perawat dan dokter (A02), serta Petugas Farmasi (A03). Diagram ini digambar ulang dari Gambar 3 SKPL tanpa mengubah aktor maupun *use case*-nya, lalu ditambah SATUSEHAT sebagai sistem eksternal («system», kotak putus-putus) di luar batas SEHATI. SATUSEHAT hanya terhubung ke UC11 karena hanya sinkronisasi yang mengirim data ke luar. UC03 Mengelola Kunjungan Pasien selalu menjalankan UC02 Mengelola Data Pasien («include») karena kunjungan tidak dapat dibuka tanpa pasiennya. UC06 Menyusun Resep Elektronik memperluas UC05 Melakukan Pemeriksaan Medis («extend») karena resep hanya disusun bila pasien butuh obat. Kotak sistem membatasi *use case* yang dijalankan SEHATI dari aktor di luarnya.

<p align="center">
<img alt="Use Case Diagram SEHATI" src="./assets/diagram-uc-hiupl.svg" width="80%">
</p>
<p align="center">
<i>Gambar 2.2. Use Case Diagram SEHATI</i>
</p>

<br>

---

# BAB 3: Implementasi

Bab ini mencatat elemen SEHATI yang dibangun pada Tugas 9: lingkungan, kelas, layar, dan berkas pendukung. Seluruhnya mengacu pada tag `v9.0` (commit `4777977`). Perbedaan implementasi dari rancangan DPPL sudah dicatat lengkap pada Tabel B.1 Laporan Implementasi Tugas 9 (95 baris), sehingga tidak diulang di sini. Setelah `v9.0` ada dua jenis perubahan. Pertama, berkas pengujian dan `pom.xml` (JaCoCo dan pelapor test berbasis ID) untuk BAB 4. Kedua, perbaikan kode atas temuan `Fail` pada BAB 4, yang dicatat pada Tabel 3.5 karena salah satunya menambah objek pada rancangan antarmuka I-06.

## 3.1 Lingkungan Implementasi

Lingkungan implementasi sama dengan Subbab 2.1 DPPL. Perbedaannya hanya pada versi pustaka yang kini sudah pasti dan alat pengujian yang ditambahkan untuk HIUPL.

Tabel 3.1. Lingkungan Implementasi

| Aspek | Keterangan |
| :--- | :--- |
| *Hardware* | Komputer pengembangan dan pengujian: Intel Core i5-12450H (12 *thread*), RAM 16 GB. Target pemakaian tetap sesuai KNF10: RAM 4 GB dan ruang kosong 2 GB. |
| *Operating System* | Pengujian HIUPL dijalankan pada Ubuntu 26.04 LTS 64-bit. Target pemakaian Windows 10/11 64-bit dan Linux 64-bit (KNF10). |
| *Bahasa Pemrograman* | Java 21 LTS (OpenJDK 21.0.13), FXML dan CSS untuk tata letak layar JavaFX, serta SQL dialek PostgreSQL. |
| *Framework/Library* | JavaFX 21.0.5 (`javafx-controls`, `javafx-fxml`), PostgreSQL JDBC Driver 42.7.4, Apache PDFBox 3.0.3 untuk laporan PDF, jBCrypt 0.4 untuk *hash* kata sandi. |
| *DBMS* | PostgreSQL 15 (citra Docker `postgres:15`, versi 15.19 saat pengujian) lewat `src/docker-compose.yml`. |
| *Development Tools* | Apache Maven 3.9 dengan `maven-compiler-plugin` 3.13.0 dan `javafx-maven-plugin` 0.0.8, IntelliJ IDEA Community Edition, pgAdmin 4 dan `psql`, Docker Compose. |
| *Version Control* | Git dan GitHub (`angseleong/IF2150-RPL-K02-G09`), *branch* `main`, `develop`, dan *branch* fitur, dengan GitHub Actions yang menjalankan `mvn test` pada setiap PR. |
| *Testing Tools* | JUnit Jupiter 5.11.3; Maven Surefire 3.5.2 dengan `maven-surefire-junit5-tree-reporter` 1.4.0 agar keluaran test menampilkan ID *test case*; JaCoCo 0.8.14 untuk *coverage* baris dan cabang; basis data uji PostgreSQL 15 dari `schema.sql` dan `seed.sql`; `psql` untuk memeriksa isi basis data; `EXPLAIN (ANALYZE)` PostgreSQL dan data `seed_besar.sql` (50.000 rekam medis) untuk pengukuran KNF. |

## 3.2 Implementasi Modul

Tabel berikut memuat seluruh 64 kelas perancangan DPPL Tabel 4.1 dengan ID dan nama yang sama, konsisten dengan Tabel B.5 Laporan Implementasi. Lokasi berkas ditulis relatif terhadap `src/src/main/java/sehati/`, sedangkan berkas FXML berada di `src/src/main/resources/sehati/fxml/`. Seluruh kelas dikemas Maven menjadi satu berkas `sehati-9.0.jar` (perintah `mvn package` dari folder `src/`), dan aplikasi dijalankan dengan `mvn javafx:run` yang memanggil kelas utama `sehati.Main`. Kelas pendukung di luar DPPL dicantumkan pada Subbab 3.4.

Tabel 3.2. Implementasi Modul

| No | ID Kelas | Nama Modul/Kelas | Nama File Fisik | Nama File Executable |
| :--- | :--- | :--- | :--- | :--- |
| 1 | C01 | LoginForm | `presentation/LoginForm.java`, `LoginForm.fxml` | `sehati-9.0.jar` |
| 2 | C02 | PasienForm | `presentation/PasienForm.java`, `PasienForm.fxml` | `sehati-9.0.jar` |
| 3 | C03 | KunjunganForm | `presentation/KunjunganForm.java`, `KunjunganForm.fxml` | `sehati-9.0.jar` |
| 4 | C04 | AntreanSkriningForm | `presentation/AntreanSkriningForm.java`, `AntreanSkriningForm.fxml` | `sehati-9.0.jar` |
| 5 | C05 | SkriningForm | `presentation/SkriningForm.java`, `SkriningForm.fxml` | `sehati-9.0.jar` |
| 6 | C06 | RekamMedisForm | `presentation/RekamMedisForm.java`, `RekamMedisForm.fxml` | `sehati-9.0.jar` |
| 7 | C07 | PemeriksaanForm | `presentation/PemeriksaanForm.java`, `PemeriksaanForm.fxml` | `sehati-9.0.jar` |
| 8 | C08 | ResepForm | `presentation/ResepForm.java`, `ResepForm.fxml` | `sehati-9.0.jar` |
| 9 | C09 | AntreanResepForm | `presentation/AntreanResepForm.java`, `AntreanResepForm.fxml` | `sehati-9.0.jar` |
| 10 | C10 | PenyerahanObatForm | `presentation/PenyerahanObatForm.java`, `PenyerahanObatForm.fxml` | `sehati-9.0.jar` |
| 11 | C11 | PersediaanForm | `presentation/PersediaanForm.java`, `PersediaanForm.fxml` | `sehati-9.0.jar` |
| 12 | C12 | DaftarPantauForm | `presentation/DaftarPantauForm.java`, `DaftarPantauForm.fxml` | `sehati-9.0.jar` |
| 13 | C13 | LaporanForm | `presentation/LaporanForm.java`, `LaporanForm.fxml` | `sehati-9.0.jar` |
| 14 | C14 | SinkronisasiForm | `presentation/SinkronisasiForm.java`, `SinkronisasiForm.fxml` | `sehati-9.0.jar` |
| 15 | C15 | SATUSEHATGateway | `dataaccess/SATUSEHATGateway.java` | `sehati-9.0.jar` |
| 16 | C16 | KonfigurasiForm | `presentation/KonfigurasiForm.java`, `KonfigurasiForm.fxml` | `sehati-9.0.jar` |
| 17 | C17 | AutentikasiManager | `business/control/AutentikasiManager.java` | `sehati-9.0.jar` |
| 18 | C18 | PasienManager | `business/control/PasienManager.java` | `sehati-9.0.jar` |
| 19 | C19 | KunjunganManager | `business/control/KunjunganManager.java` | `sehati-9.0.jar` |
| 20 | C20 | SkriningManager | `business/control/SkriningManager.java` | `sehati-9.0.jar` |
| 21 | C21 | RisikoManager | `business/control/RisikoManager.java` | `sehati-9.0.jar` |
| 22 | C22 | PemeriksaanManager | `business/control/PemeriksaanManager.java` | `sehati-9.0.jar` |
| 23 | C23 | ResepManager | `business/control/ResepManager.java` | `sehati-9.0.jar` |
| 24 | C24 | ApotekManager | `business/control/ApotekManager.java` | `sehati-9.0.jar` |
| 25 | C25 | PersediaanManager | `business/control/PersediaanManager.java` | `sehati-9.0.jar` |
| 26 | C26 | PantauManager | `business/control/PantauManager.java` | `sehati-9.0.jar` |
| 27 | C27 | LaporanManager | `business/control/LaporanManager.java` | `sehati-9.0.jar` |
| 28 | C28 | SinkronisasiManager | `business/control/SinkronisasiManager.java` | `sehati-9.0.jar` |
| 29 | C29 | KonfigurasiManager | `business/control/KonfigurasiManager.java` | `sehati-9.0.jar` |
| 30 | C30 | PenggunaEntity | `business/entity/PenggunaEntity.java` | `sehati-9.0.jar` |
| 31 | C31 | SesiEntity | `business/entity/SesiEntity.java` | `sehati-9.0.jar` |
| 32 | C32 | AuditEntity | `business/entity/AuditEntity.java` | `sehati-9.0.jar` |
| 33 | C33 | PasienEntity | `business/entity/PasienEntity.java` | `sehati-9.0.jar` |
| 34 | C34 | RekamMedisEntity | `business/entity/RekamMedisEntity.java` | `sehati-9.0.jar` |
| 35 | C35 | KunjunganEntity | `business/entity/KunjunganEntity.java` | `sehati-9.0.jar` |
| 36 | C36 | PoliEntity | `business/entity/PoliEntity.java` | `sehati-9.0.jar` |
| 37 | C37 | AntreanEntity | `business/entity/AntreanEntity.java` | `sehati-9.0.jar` |
| 38 | C38 | SkriningEntity | `business/entity/SkriningEntity.java` | `sehati-9.0.jar` |
| 39 | C39 | TandaVitalEntity | `business/entity/TandaVitalEntity.java` | `sehati-9.0.jar` |
| 40 | C40 | AmbangRisikoEntity | `business/entity/AmbangRisikoEntity.java` | `sehati-9.0.jar` |
| 41 | C41 | PenandaRisikoEntity | `business/entity/PenandaRisikoEntity.java` | `sehati-9.0.jar` |
| 42 | C42 | PemeriksaanEntity | `business/entity/PemeriksaanEntity.java` | `sehati-9.0.jar` |
| 43 | C43 | DiagnosisEntity | `business/entity/DiagnosisEntity.java` | `sehati-9.0.jar` |
| 44 | C44 | KodeICD10Entity | `business/entity/KodeICD10Entity.java` | `sehati-9.0.jar` |
| 45 | C45 | JadwalKontrolEntity | `business/entity/JadwalKontrolEntity.java` | `sehati-9.0.jar` |
| 46 | C46 | ResepEntity | `business/entity/ResepEntity.java` | `sehati-9.0.jar` |
| 47 | C47 | ItemResepEntity | `business/entity/ItemResepEntity.java` | `sehati-9.0.jar` |
| 48 | C48 | ObatEntity | `business/entity/ObatEntity.java` | `sehati-9.0.jar` |
| 49 | C49 | BetsObatEntity | `business/entity/BetsObatEntity.java` | `sehati-9.0.jar` |
| 50 | C50 | PenerimaanObatEntity | `business/entity/PenerimaanObatEntity.java` | `sehati-9.0.jar` |
| 51 | C51 | DaftarPantauEntity | `business/entity/DaftarPantauEntity.java` | `sehati-9.0.jar` |
| 52 | C52 | TindakLanjutEntity | `business/entity/TindakLanjutEntity.java` | `sehati-9.0.jar` |
| 53 | C53 | LaporanEntity | `business/entity/LaporanEntity.java` | `sehati-9.0.jar` |
| 54 | C54 | BundelFHIREntity | `business/entity/BundelFHIREntity.java` | `sehati-9.0.jar` |
| 55 | C55 | PencadanganEntity | `business/entity/PencadanganEntity.java` | `sehati-9.0.jar` |
| 56 | C56 | DataAkunBoundary | `dataaccess/DataAkunBoundary.java` | `sehati-9.0.jar` |
| 57 | C57 | DataPasienBoundary | `dataaccess/DataPasienBoundary.java` | `sehati-9.0.jar` |
| 58 | C58 | DataKunjunganBoundary | `dataaccess/DataKunjunganBoundary.java` | `sehati-9.0.jar` |
| 59 | C59 | DataKlinisBoundary | `dataaccess/DataKlinisBoundary.java` | `sehati-9.0.jar` |
| 60 | C60 | DataPantauBoundary | `dataaccess/DataPantauBoundary.java` | `sehati-9.0.jar` |
| 61 | C61 | DataFarmasiBoundary | `dataaccess/DataFarmasiBoundary.java` | `sehati-9.0.jar` |
| 62 | C62 | DataLaporanBoundary | `dataaccess/DataLaporanBoundary.java` | `sehati-9.0.jar` |
| 63 | C63 | DataSinkronisasiBoundary | `dataaccess/DataSinkronisasiBoundary.java` | `sehati-9.0.jar` |
| 64 | C64 | DataPencadanganBoundary | `dataaccess/DataPencadanganBoundary.java` | `sehati-9.0.jar` |

## 3.3 Implementasi Antarmuka

Kelima belas layar memakai ID dan nama layar DPPL BAB 6. Setiap layar terdiri atas satu berkas FXML untuk tata letak dan satu kelas `Form` sebagai pengendalinya. Bilah aplikasi yang sama di setiap layar dibangun oleh `presentation/BilahAplikasi.java`.

Tabel 3.3. Implementasi Antarmuka

| No | ID Antarmuka | Nama Antarmuka | Nama File Fisik | Nama File Executable |
| :--- | :--- | :--- | :--- | :--- |
| 1 | I-01 | Masuk dan Dasbor | `src/src/main/resources/sehati/fxml/LoginForm.fxml`, `src/src/main/java/sehati/presentation/LoginForm.java` | `sehati-9.0.jar` |
| 2 | I-02 | Pendaftaran Pasien | `src/src/main/resources/sehati/fxml/PasienForm.fxml`, `src/src/main/java/sehati/presentation/PasienForm.java` | `sehati-9.0.jar` |
| 3 | I-03 | Kunjungan Pasien | `src/src/main/resources/sehati/fxml/KunjunganForm.fxml`, `src/src/main/java/sehati/presentation/KunjunganForm.java` | `sehati-9.0.jar` |
| 4 | I-04 | Antrean Skrining | `src/src/main/resources/sehati/fxml/AntreanSkriningForm.fxml`, `src/src/main/java/sehati/presentation/AntreanSkriningForm.java` | `sehati-9.0.jar` |
| 5 | I-05 | Formulir Skrining | `src/src/main/resources/sehati/fxml/SkriningForm.fxml`, `src/src/main/java/sehati/presentation/SkriningForm.java` | `sehati-9.0.jar` |
| 6 | I-06 | Rekam Medis Pasien | `src/src/main/resources/sehati/fxml/RekamMedisForm.fxml`, `src/src/main/java/sehati/presentation/RekamMedisForm.java` | `sehati-9.0.jar` |
| 7 | I-07 | Formulir Pemeriksaan | `src/src/main/resources/sehati/fxml/PemeriksaanForm.fxml`, `src/src/main/java/sehati/presentation/PemeriksaanForm.java` | `sehati-9.0.jar` |
| 8 | I-08 | Resep Elektronik | `src/src/main/resources/sehati/fxml/ResepForm.fxml`, `src/src/main/java/sehati/presentation/ResepForm.java` | `sehati-9.0.jar` |
| 9 | I-09 | Antrean Resep Apotek | `src/src/main/resources/sehati/fxml/AntreanResepForm.fxml`, `src/src/main/java/sehati/presentation/AntreanResepForm.java` | `sehati-9.0.jar` |
| 10 | I-10 | Penyerahan Obat | `src/src/main/resources/sehati/fxml/PenyerahanObatForm.fxml`, `src/src/main/java/sehati/presentation/PenyerahanObatForm.java` | `sehati-9.0.jar` |
| 11 | I-11 | Persediaan Obat | `src/src/main/resources/sehati/fxml/PersediaanForm.fxml`, `src/src/main/java/sehati/presentation/PersediaanForm.java` | `sehati-9.0.jar` |
| 12 | I-12 | Daftar Pantau Pasien | `src/src/main/resources/sehati/fxml/DaftarPantauForm.fxml`, `src/src/main/java/sehati/presentation/DaftarPantauForm.java` | `sehati-9.0.jar` |
| 13 | I-13 | Laporan Periodik | `src/src/main/resources/sehati/fxml/LaporanForm.fxml`, `src/src/main/java/sehati/presentation/LaporanForm.java` | `sehati-9.0.jar` |
| 14 | I-14 | Sinkronisasi SATUSEHAT | `src/src/main/resources/sehati/fxml/SinkronisasiForm.fxml`, `src/src/main/java/sehati/presentation/SinkronisasiForm.java` | `sehati-9.0.jar` |
| 15 | I-15 | Konfigurasi dan Basis Data | `src/src/main/resources/sehati/fxml/KonfigurasiForm.fxml`, `src/src/main/java/sehati/presentation/KonfigurasiForm.java` | `sehati-9.0.jar` |

## 3.4 File Lain

Tabel 3.4. File Lain

| No | Nama File | Keterangan |
| :--- | :--- | :--- |
| 1 | `src/pom.xml` | Konfigurasi Maven: versi Java 21, dependensi, kelas utama, Surefire dengan pelapor pohon, dan JaCoCo |
| 2 | `src/.env.example` | Contoh variabel lingkungan (koneksi basis data, SATUSEHAT, frasa sandi ekspor, lokasi media cadangan, jam pencadangan). Salinannya `src/.env` tidak masuk repositori |
| 3 | `src/docker-compose.yml` | Menjalankan PostgreSQL 15 dan memuat skema serta seed saat volume kosong |
| 4 | `src/db/schema.sql` | Skema basis data: 28 tabel beserta *foreign key*, `UNIQUE`, `CHECK`, indeks, dan peran `sehati_app` |
| 5 | `src/db/seed.sql` | Data fiktif untuk setiap tahap pelayanan, dengan tanggal relatif terhadap hari volume dibuat. Dipakai seluruh pengujian BAB 4 |
| 6 | `src/db/seed_besar.sql` | 50.000 rekam medis sintetis untuk profiling dan pengukuran KNF01 |
| 7 | `src/db/uji-kueri.sql` | Uji cepat 78 kueri DPPL terhadap skema |
| 8 | `src/db/sandi-aplikasi.sh` | Mengatur kata sandi peran `sehati_app` dari `.env` saat basis data pertama kali dibuat |
| 9 | `src/src/main/resources/sehati/css/sehati.css` | Tema seluruh layar, termasuk warna peringatan merah dan lencana stok |
| 10 | `src/src/main/java/sehati/Main.java` | Kelas utama aplikasi JavaFX yang membuka Navigasi dan penjadwal pencadangan |
| 11 | `src/src/main/java/sehati/common/KoneksiBasisData.java`, `PengaturanAplikasi.java`, `SesiAktif.java` | Pembuka koneksi JDBC, pembaca `.env`, dan pemegang sesi beserta pemeriksa peran `wajibPeran()` |
| 12 | `src/src/main/java/sehati/presentation/Navigasi.java`, `BilahAplikasi.java`, `LatarBelakang.java`, `LogoSehati.java`, `PanelPeringatan.java`, `PenandaKolom.java`, `TabelPintasan.java` | Pendukung lapisan presentasi: perpindahan layar dan cek hak akses, bilah aplikasi, tugas latar, logo, panel peringatan merah, penanda kolom bermasalah, dan layar bantuan pintasan |
| 13 | `src/src/main/java/sehati/business/control/PenyusunBundelFHIR.java` | Penyusun isi bundel HL7 FHIR R4 yang dipakai `ApotekManager` dan `PemeriksaanManager` (Tabel B.1 nomor 47) |
| 14 | `src/src/main/java/sehati/dataaccess/ResponsKirim.java` | Record hasil pengiriman bundel dari `SATUSEHATGateway` |
| 15 | `src/tests/sehati/**/*Test.java` (51 kelas test) | Test JUnit per kelas pada paket yang sama dengan kelas yang diuji, test integrasi pada `src/tests/sehati/integrasi/`, serta `AlurUtamaTest`, `KonkurensiTest`, dan `KnfFarmasiTest` (KNF03 dan KNF13) |
| 16 | `src/tests/sehati/dataaccess/JdbcPalsu.java`, `src/tests/sehati/DataUjiAlur.java`, `src/tests/sehati/PratinjauLayar.java`, `src/tests/sehati/CadanganFarmasi.java`, `src/tests/sehati/ProsesFarmasiKnf13.java` | Pendukung test: pengganti JDBC untuk uji tanpa basis data, penyiap data alur, pembuka satu layar tanpa masuk, penyimpan dan pemulih keadaan tabel farmasi, serta proses aplikasi terpisah yang dimatikan paksa pada KNF13-TC01 |
| 17 | `src/README.md` | Cara memasang, menjalankan aplikasi, dan menjalankan test |
| 18 | `.github/workflows/` | CI GitHub Actions yang menjalankan `mvn test` pada setiap PR |

Tabel 3.5. Perubahan Implementasi setelah `v9.0` dari Hasil Pengujian

| No | Bagian DPPL | Rancangan / Implementasi `v9.0` | Implementasi Akhir | Alasan |
| :--- | :--- | :--- | :--- | :--- |
| 1 | C22 `jadwalkanKontrol()`, C59 (kueri baru Q-C59-20) | Jadwal pasien pada tanggal yang sama dicari lewat `ambilJadwalKontrol()` (Q-C59-17), yang hanya memuat jadwal yang belum memicu entri daftar pantau | Dicari lewat `DataKlinisBoundary.jadwalPadaTanggal()` dengan kueri baru Q-C59-20: jadwal "terjadwal" berdasarkan `id_pasien` dan `tanggal_rencana`, tanpa syarat daftar pantau | Temuan C22-UT37 dan UC05-TC08: jadwal kontrol dan entri daftar pantau dapat ganda (KF12) |
| 2 | C06 RekamMedisForm, I-06 (DPPL Subbab 6.6) | I-06 hanya memuat panel data diri, penanda risiko, diagnosis lampau, dan riwayat obat | Panel "Skrining hari ini" ditambahkan di bawah data diri, berisi keluhan awal dan tanda vital dari `RekamMedisEntity.getSkriningHariIni()` | Temuan UC05-TC01: SKPL UC05 langkah 1 meminta hasil skrining hari itu tampil pada ringkasan (KF09) |
| 3 | C17 `periksaHakAkses()` (Algo-C17-02) | `layarPeran.contains(layar)` langsung dipanggil pada himpunan `Set.of()`, yang melempar `NullPointerException` bila `layar` bernilai `null` | Layar `null` langsung menghasilkan `false` sebelum `contains()` dipanggil | Temuan C17-UT17: DPPL Subbab 5.17 menyatakan operasi mengembalikan `true` hanya bila peran boleh membuka layar, jadi masukan tidak sah harus `false`, bukan pengecualian (KF16) |

<br>

---

# BAB 4: Hasil Pengujian

Bab ini melaporkan hasil pengujian SEHATI pada level unit, integrasi, dan sistem. Pengujian dijalankan pada kode tag `v9.0` (commit `4777977`). Hanya berkas test yang ditambah atau diubah. Hasil aktual diisi dari pengamatan saat pengujian. Kesimpulan diisi `Pass` atau `Fail`, dan setiap `Fail` dilengkapi penyebab serta usulan perbaikan. Hasil pada `v9.0` tetap dicatat apa adanya. Bila temuan `Fail` sudah diperbaiki, baris yang sama juga memuat hasil uji ulang setelah perbaikan (Tabel 3.5), lengkap dengan bukti sebelum dan sesudahnya.

Seluruh pengujian memakai basis data uji yang dibuat dari `src/db/schema.sql` dan `src/db/seed.sql`. Data seed bertanggal relatif terhadap hari basis data dibuat. Basis data uji untuk bagian modul pemeriksaan dan resep dibuat pada 18 November 2026, sehingga "hari ini" pada hasil pengujian adalah 18-11-2026. Pengujian otomatis (unit dan integrasi) dijalankan dengan Maven, sedangkan pengujian *use case* dan sebagian KNF dijalankan pada aplikasi dan dibuktikan dengan tangkapan layar.

## 4.1 Strategi dan Kedalaman Pengujian Kelas

Setiap kelas perancangan pada Tabel 4.1 DPPL diklasifikasikan ke dalam tiga tingkat kedalaman pengujian berdasarkan isi kelasnya:

- **Mendalam** untuk kelas yang memuat logika bisnis, perhitungan, aturan status atau transisi, algoritma bercabang pada BAB 5 DPPL, keamanan, atau integrasi dengan sistem di luar proses aplikasi. Setiap operasi public diuji minimal dengan kasus normal, batas, dan negatif, dan *coverage* baris serta cabang kelas minimal 70%.
- **Sedang** untuk kelas yang mendelegasikan atau menjalankan operasi baca-tulis standar (seluruh `DataBoundary` kecuali yang menangani enkripsi atau proses eksternal) dan entity yang hanya memeriksa masukan kosong. Operasi public utama diuji minimal dengan kasus normal dan negatif, boleh melalui pengujian integrasi.
- **Tidak perlu** untuk kelas tampilan (`Form`) dan entity yang hanya menyimpan data atau berisi satu perbandingan tanpa percabangan. Kelas ini tidak memiliki test unit khusus dan teruji tidak langsung lewat *test case use case*.

Dari 64 kelas, 34 kelas bertingkat Mendalam, 12 kelas Sedang, dan 18 kelas Tidak perlu.

Tabel 4.1.1. Identifikasi Tingkat Pengujian Kelas

| ID Kelas | Nama Kelas | Tingkat | Alasan | Test Case Terkait |
| :--- | :--- | :--- | :--- | :--- |
| C01 | LoginForm | Tidak perlu | Kelas tampilan layar I-01, meneruskan kredensial ke AutentikasiManager | - (teruji lewat UC01-TC01 s.d. UC01-TC04) |
| C02 | PasienForm | Tidak perlu | Kelas tampilan layar I-02, meneruskan pencarian dan isian ke PasienManager | - (teruji lewat UC02) |
| C03 | KunjunganForm | Tidak perlu | Kelas tampilan layar I-03, meneruskan pilihan poli ke KunjunganManager | - (teruji lewat UC03) |
| C04 | AntreanSkriningForm | Tidak perlu | Kelas tampilan layar I-04, menampilkan antrean dari SkriningManager | - (teruji lewat UC04) |
| C05 | SkriningForm | Tidak perlu | Kelas tampilan layar I-05; validasi rentang dijalankan SkriningManager | - (teruji lewat UC04) |
| C06 | RekamMedisForm | Tidak perlu | Kelas tampilan layar I-06, menampilkan ringkasan dan tren dari PemeriksaanManager | - (teruji lewat UC05-TC01, UC05-TC02, UC05-TC04, UC05-TC06, UC05-TC07) |
| C07 | PemeriksaanForm | Tidak perlu | Kelas tampilan layar I-07, meneruskan isian ke PemeriksaanManager | - (teruji lewat UC05-TC03, UC05-TC05, UC05-TC06, UC05-TC08) |
| C08 | ResepForm | Tidak perlu | Kelas tampilan layar I-08, meneruskan item ke ResepManager | - (teruji lewat UC06-TC01, UC06-TC02) |
| C09 | AntreanResepForm | Tidak perlu | Kelas tampilan layar I-09, menampilkan antrean dari ApotekManager | - (teruji lewat UC07-TC01 s.d. UC07-TC03) |
| C10 | PenyerahanObatForm | Tidak perlu | Kelas tampilan layar I-10, meneruskan konfirmasi ke ApotekManager | - (teruji lewat UC07-TC01 s.d. UC07-TC03) |
| C11 | PersediaanForm | Tidak perlu | Kelas tampilan layar I-11, meneruskan penerimaan ke PersediaanManager | - (teruji lewat UC08-TC01 s.d. UC08-TC03) |
| C12 | DaftarPantauForm | Tidak perlu | Kelas tampilan layar I-12, meneruskan penyaring dan tindak lanjut ke PantauManager | - (teruji lewat UC09) |
| C13 | LaporanForm | Tidak perlu | Kelas tampilan layar I-13, meneruskan rentang dan ekspor ke LaporanManager | - (teruji lewat UC10) |
| C14 | SinkronisasiForm | Tidak perlu | Kelas tampilan layar I-14, meneruskan perintah ke SinkronisasiManager | - (teruji lewat UC11) |
| C15 | SATUSEHATGateway | Mendalam | Integrasi eksternal ke REST API SATUSEHAT, termasuk mode simulasi dan penanganan galat jaringan (Algo-C15-01, Algo-C15-02) | *[C15-UT01 s.d. C15-UT.., diisi Fachry]* |
| C16 | KonfigurasiForm | Tidak perlu | Kelas tampilan layar I-15, meneruskan perubahan ke KonfigurasiManager | - (teruji lewat UC12-TC01 s.d. UC12-TC05) |
| C17 | AutentikasiManager | Mendalam | Keamanan: verifikasi kredensial bcrypt, sesi, batas diam 15 menit, dan hak akses peran (Algo-C17-01, Algo-C17-02) | C17-UT01 s.d. C17-UT17, INT-TC01, INT-TC02 |
| C18 | PasienManager | Mendalam | Validasi NIK 16 digit dan keunikannya serta penerbitan nomor rekam medis (Algo-C18-01, Algo-C18-02) | *[C18-UT01 s.d. C18-UT.., diisi Steven]* |
| C19 | KunjunganManager | Mendalam | Penolakan kunjungan ganda dan penerbitan nomor antrean per poli per hari dalam transaksi (Algo-C19-01, Algo-C19-02) | *[C19-UT01 s.d. C19-UT.., diisi Steven]* |
| C20 | SkriningManager | Mendalam | Validasi rentang fisiologis tanpa jalan pintas dan alur antrean skrining (Algo-C20-01 s.d. Algo-C20-03) | C20-UT01 s.d. C20-UT32, INT-TC11, INT-TC12, KNF02-TC01, KNF02-TC02 |
| C21 | RisikoManager | Mendalam | Perhitungan risiko terhadap ambang dan riwayat tiga kunjungan (Algo-C21-01, Algo-C21-02) | C21-UT01 s.d. C21-UT18, INT-TC13, INT-TC14 |
| C22 | PemeriksaanManager | Mendalam | Validasi diagnosis ICD-10 dan tanggal kontrol, pembatasan peran Dokter, serta transaksi penguncian rekam medis (Algo-C22-01, Algo-C22-02) | C22-UT01 s.d. C22-UT37, INT-TC17 s.d. INT-TC20 |
| C23 | ResepManager | Mendalam | Validasi kecukupan stok per obat (termasuk bets kedaluwarsa) dan transaksi pengiriman resep (Algo-C23-01, Algo-C23-02) | C23-UT01 s.d. C23-UT25, INT-TC21, INT-TC22 |
| C24 | ApotekManager | Mendalam | Transaksi penyerahan obat: potong stok FEFO, tutup kunjungan, dan susun bundel FHIR (Algo-C24-01, Algo-C24-02) | C24-UT01 s.d. C24-UT37, INT-TC23, INT-TC24, KNF03-TC01, KNF13-TC01 |
| C25 | PersediaanManager | Mendalam | Validasi penerimaan obat, penambahan stok, dan peringatan stok serta kedaluwarsa (Algo-C25-01 s.d. Algo-C25-03) | C25-UT01 s.d. C25-UT20, INT-TC25, INT-TC26, KNF13-TC01 |
| C26 | PantauManager | Mendalam | Penyaringan daftar pantau dan perubahan status tindak lanjut (Algo-C26-01) | C26-UT01 s.d. C26-UT26, INT-TC15, INT-TC16 |
| C27 | LaporanManager | Mendalam | Perhitungan rekapitulasi dan validasi rentang tanggal (Algo-C27-01, Algo-C27-02) | *[C27-UT01 s.d. C27-UT.., diisi Fachry]* |
| C28 | SinkronisasiManager | Mendalam | Aturan kirim saat daring serta tahan dan ekspor saat luring (Algo-C28-01, Algo-C28-02) | *[C28-UT01 s.d. C28-UT.., diisi Fachry]* |
| C29 | KonfigurasiManager | Mendalam | Validasi akun (nama unik, sandi minimal 8 karakter, peran), data master, dan pencadangan (Algo-C29-01 s.d. Algo-C29-03) | C29-UT01 s.d. C29-UT37, INT-TC03 s.d. INT-TC06 |
| C30 | PenggunaEntity | Mendalam | Keamanan: pencocokan kata sandi bcrypt dan status aktif akun (Algo-C30-01) | C30-UT01 s.d. C30-UT11 |
| C31 | SesiEntity | Mendalam | Keamanan: batas diam sesi pada habisWaktu() (Algo-C31-01) | C31-UT01 s.d. C31-UT07 |
| C32 | AuditEntity | Sedang | buat() hanya menolak deskripsi kosong lalu membentuk catatan (Algo-C32-01) | C32-UT01 s.d. C32-UT03 |
| C33 | PasienEntity | Sedang | perbarui() hanya menolak data kosong lalu menyalin atribut (Algo-C33-01) | *[C33-UT.. atau INT-TC.., diisi Steven]* |
| C34 | RekamMedisEntity | Sedang | tambahEntri() hanya menolak catatan kosong (Algo-C34-01) | *[C34-UT.. atau INT-TC.., diisi Steven]* |
| C35 | KunjunganEntity | Mendalam | Aturan transisi status kunjungan dan penguncian status selesai (Algo-C35-01) | *[C35-UT01 s.d. C35-UT.., diisi Steven]* |
| C36 | PoliEntity | Mendalam | Perhitungan nomor antrean berikutnya yang dimulai ulang saat hari berganti (Algo-C36-01) | *[C36-UT01 s.d. C36-UT.., diisi Steven]* |
| C37 | AntreanEntity | Mendalam | Aturan urutan antrean: terdepan, pindah ke belakang, dan keluarkan (Algo-C37-01) | *[C37-UT01 s.d. C37-UT.., diisi Steven]* |
| C38 | SkriningEntity | Sedang | lampirkanTandaVital() hanya menolak tanda vital kosong | C38-UT01 s.d. C38-UT04 |
| C39 | TandaVitalEntity | Mendalam | Perhitungan IMT dan penolakan tinggi badan kosong (Algo-C39-01) | C39-UT01 s.d. C39-UT08 |
| C40 | AmbangRisikoEntity | Mendalam | Perbandingan nilai terhadap ambang klinis tepat di nilai batas (Algo-C40-01) | C40-UT01 s.d. C40-UT06 |
| C41 | PenandaRisikoEntity | Mendalam | Aturan status: penanda yang sudah dicabut tidak dapat dicabut lagi | C41-UT01 s.d. C41-UT05 |
| C42 | PemeriksaanEntity | Mendalam | Aturan status: pemeriksaan yang sudah dikunci tidak dapat dikunci lagi | C42-UT01 s.d. C42-UT03 |
| C43 | DiagnosisEntity | Tidak perlu | Hanya menyimpan diagnosis; ringkas() menggabungkan kode dan deskripsi tanpa percabangan | - (teruji lewat UC05-TC01, UC05-TC03) |
| C44 | KodeICD10Entity | Mendalam | Pencocokan kata kunci ICD-10 tanpa membedakan huruf (Algo-C44-01) | C44-UT01 s.d. C44-UT05 |
| C45 | JadwalKontrolEntity | Mendalam | Aturan status: hanya jadwal terjadwal yang dapat ditandai selesai | C45-UT01 s.d. C45-UT03 |
| C46 | ResepEntity | Mendalam | Aturan transisi status resep menunggu, selesai dilayani, dan dikembalikan (Algo-C46-01) | C46-UT01 s.d. C46-UT04 |
| C47 | ItemResepEntity | Sedang | cukupStok() berisi satu aturan stok ≥ jumlah tanpa percabangan; aturan lengkapnya diuji pada C23 | C47-UT01 s.d. C47-UT03, INT-TC21 |
| C48 | ObatEntity | Mendalam | Validasi pengurangan dan penambahan stok serta ambang minimum (Algo-C48-01) | C48-UT01 s.d. C48-UT10 |
| C49 | BetsObatEntity | Mendalam | Pengurangan bets dan batas 90 hari menjelang kedaluwarsa (Algo-C49-01) | C49-UT01 s.d. C49-UT13 |
| C50 | PenerimaanObatEntity | Mendalam | Perhitungan total jumlah seluruh bets (Algo-C50-01) | C50-UT01 s.d. C50-UT03 |
| C51 | DaftarPantauEntity | Mendalam | Aturan transisi status tindak lanjut (Algo-C51-01) | C51-UT01 s.d. C51-UT06 |
| C52 | TindakLanjutEntity | Tidak perlu | Hanya menyimpan data tindak lanjut; terhubung() membandingkan satu atribut | - (teruji lewat UC09) |
| C53 | LaporanEntity | Mendalam | kosong() mengurai ringkasan rekapitulasi dengan beberapa cabang (Algo-C53-01) | *[C53-UT01 s.d. C53-UT.., diisi Fachry]* |
| C54 | BundelFHIREntity | Mendalam | Aturan status kirim: bundel terkirim tidak dapat ditandai ulang (Algo-C54-01) | *[C54-UT01 s.d. C54-UT.., diisi Fachry]* |
| C55 | PencadanganEntity | Tidak perlu | Hanya menyimpan catatan pencadangan; berhasil() membaca satu status | - (teruji lewat UC12-TC02, UC12-TC05) |
| C56 | DataAkunBoundary | Sedang | Kueri akun, sesi, dan audit (Q-C56-01 s.d. Q-C56-10) | C56-UT01 s.d. C56-UT10, INT-TC01 s.d. INT-TC03 |
| C57 | DataPasienBoundary | Sedang | Kueri pasien dan pencarian parsial (Q-C57-01 s.d. Q-C57-12, Algo-C57-01) | *[C57-UT.. atau INT-TC.., diisi Steven]* |
| C58 | DataKunjunganBoundary | Sedang | Kueri kunjungan, poli, dan antrean dengan penguncian baris (Q-C58-01 s.d. Q-C58-13) | *[C58-UT.. atau INT-TC.., diisi Steven]* |
| C59 | DataKlinisBoundary | Sedang | Kueri skrining, risiko, pemeriksaan, ICD-10, dan jadwal kontrol (Q-C59-01 s.d. Q-C59-20) | C59-UT21 s.d. C59-UT32, INT-TC17 s.d. INT-TC20 (bagian pemeriksaan); C59-UT01 s.d. C59-UT20, INT-TC11 s.d. INT-TC14 (bagian skrining dan risiko) |
| C60 | DataPantauBoundary | Sedang | Kueri daftar pantau dan tindak lanjut (Q-C60-01 s.d. Q-C60-07) | C60-UT01 s.d. C60-UT09, INT-TC15, INT-TC16 |
| C61 | DataFarmasiBoundary | Sedang | Kueri obat, bets, dan resep dengan penguncian stok (Q-C61-01 s.d. Q-C61-19) | C61-UT01 s.d. C61-UT28, INT-TC23 s.d. INT-TC26 |
| C62 | DataLaporanBoundary | Sedang | Kueri agregasi dan penulisan berkas PDF/CSV (Q-C62-01 s.d. Q-C62-04); logika rekapitulasi diuji pada C27 | *[C62-UT.. atau INT-TC.., diisi Fachry]* |
| C63 | DataSinkronisasiBoundary | Mendalam | Keamanan: enkripsi arsip ekspor bundel (Algo-C63-01) di samping penyimpanan bundel | *[C63-UT01 s.d. C63-UT.., diisi Fachry]* |
| C64 | DataPencadanganBoundary | Mendalam | Integrasi proses eksternal pg_dump dan verifikasi arsip (Algo-C64-01, Algo-C64-02) | C64-UT01 s.d. C64-UT16, INT-TC05, INT-TC06 |

## 4.2 Pengujian Unit Kelas

Pengujian unit dilakukan pada kelas bertingkat Mendalam dan Sedang. Kelas Mendalam diuji pada seluruh operasi public dengan kasus normal, batas, dan negatif, serta mencapai *coverage* minimal 70%. Kelas Sedang diuji minimal dengan satu kasus normal dan satu kasus negatif pada operasi public utama.

Setiap method test diberi anotasi `@DisplayName` yang diawali ID *test case*, misalnya `C22-UT04`, sehingga keluaran *test runner* langsung menunjukkan ID yang sama dengan tabel pada subbab ini. Kelas `Manager` membuka koneksinya sendiri lewat `KoneksiBasisData`, jadi operasi yang membaca atau menyimpan data diuji terhadap basis data uji. Setiap test menghapus atau me-*rollback* data yang dibuatnya agar test lain tidak terpengaruh. Kasus yang ditolak sebelum basis data disentuh (validasi masukan dan pemeriksaan peran) diuji tanpa basis data. *Coverage* diukur JaCoCo dari seluruh test (`mvn test`).

Cara menjalankan unit test, dari folder `src/` dengan basis data uji menyala (`docker compose up -d`):

```bash
export DB_UJI_URL="jdbc:postgresql://localhost:5432/sehati?user=sehati_admin&password=<DB_ADMIN_PASSWORD>"
mvn test                                  # seluruh test, laporan coverage di target/site/jacoco/index.html
mvn test -Dtest=PemeriksaanManagerTest    # satu kelas test
```

Tanpa `DB_UJI_URL`, test yang membutuhkan basis data dilewati.

Lokasi file test: `src/tests/`, dengan paket yang sama dengan kelas yang diuji (misalnya `src/tests/sehati/business/control/PemeriksaanManagerTest.java` untuk C22).

### 4.2.1 Kelas SATUSEHATGateway

**ID Kelas:** C15  
**Nama Kelas:** SATUSEHATGateway  
**Tingkat:** Mendalam

*[Diisi Fachry: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.2 Kelas AutentikasiManager

**ID Kelas:** C17  
**Nama Kelas:** AutentikasiManager  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/control/AutentikasiManagerTest.java`. Test yang membutuhkan basis data membuat akun uji `uji.autentikasi` (Perawat, aktif, sandi `rahasia123`) sebelum setiap test, lalu menghapus akun, sesi, dan audit miliknya setelah test.

Tabel 4.2.2. Hasil Pengujian Unit Kelas C17

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C17-UT01 | `autentikasi(nama: String, sandi: String): SesiEntity` | Normal | `uji.autentikasi`, `rahasia123` | Sesi aktif dikembalikan, `SesiAktif` berisi pengguna dan peran Perawat, satu baris `sesi` aktif dan audit "Masuk ke aplikasi" | Sesi aktif, `SesiAktif.peran()` = "Perawat", baris `sesi` dan audit ada | Pass |
| C17-UT02 | `autentikasi(nama: String, sandi: String): SesiEntity` | Batas | Sandi salah, lalu sandi benar, lalu sandi salah lagi | Masuk berhasil mengatur ulang hitungan percobaan, sehingga kegagalan setelahnya dicatat lagi sebagai "percobaan ke-1" | Dua audit "Masuk gagal, percobaan ke-1" dan tidak ada "percobaan ke-2" | Pass |
| C17-UT03 | `autentikasi(nama: String, sandi: String): SesiEntity` | Negatif | `uji.autentikasi`, sandi "salah" | `null`, tanpa sesi, audit "Masuk gagal, percobaan ke-1" berpelaku akun itu | `null`, tidak ada baris `sesi`, satu audit sesuai | Pass |
| C17-UT04 | `autentikasi(nama: String, sandi: String): SesiEntity` | Negatif | Sandi salah, lalu nama `uji.tidak.ada` | `null`, audit berpelaku 0 "Masuk gagal: nama 'uji.tidak.ada' tidak dikenal, percobaan ke-2" | `null` dan audit sesuai | Pass |
| C17-UT05 | `autentikasi(nama: String, sandi: String): SesiEntity` | Negatif | Akun uji diubah menjadi nonaktif, sandi benar | `IllegalStateException` "Akun nonaktif, hubungi Petugas Administrasi", tanpa sesi | Pengecualian dan pesan sesuai, tidak ada baris `sesi` | Pass |
| C17-UT06 | `autentikasi(nama: String, sandi: String): SesiEntity` | Batas | Sandi "" dan `null` | `null` tanpa pengecualian dan tanpa sesi | `null` untuk keduanya, tidak ada baris `sesi` | Pass |
| C17-UT07 | `periksaHakAkses(peran: String, layar: String): boolean` | Normal | Admin-PasienForm, Admin-KonfigurasiForm, Perawat-SkriningForm, Dokter-ResepForm, Farmasi-PersediaanForm | `true` untuk kelimanya | `true` | Pass |
| C17-UT08 | `periksaHakAkses(peran: String, layar: String): boolean` | Batas | RekamMedisForm untuk Perawat dan Dokter, PemeriksaanForm untuk Dokter dan Perawat | Layar bersama boleh untuk keduanya, PemeriksaanForm hanya Dokter | `true`, `true`, `true`, `false` | Pass |
| C17-UT09 | `periksaHakAkses(peran: String, layar: String): boolean` | Negatif | Perawat-ResepForm, Farmasi-PasienForm, Dokter-KonfigurasiForm, Admin-PemeriksaanForm | `false` untuk keempatnya | `false` | Pass |
| C17-UT10 | `periksaHakAkses(peran: String, layar: String): boolean` | Negatif | Peran `null`, "Pemilik Layar", "dokter" (huruf kecil), dan Admin-LoginForm | `false` untuk semuanya | `false` | Pass |
| C17-UT17 | `periksaHakAkses(peran: String, layar: String): boolean` | Negatif | Dokter dengan layar `null`, serta peran dan layar `null` | `false` (DPPL 5.17: `true` hanya bila peran boleh membuka layar) | Pada `v9.0`: `NullPointerException` dari `ImmutableCollections$SetN.contains()`. Setelah perbaikan: `false` untuk keduanya | Fail pada `v9.0`, Pass setelah perbaikan. Penyebab: daftar layar per peran dibuat dengan `Set.of()`, yang melempar `NullPointerException` untuk `contains(null)`. Seluruh pemanggil saat ini (`LoginForm`, `Navigasi`, `RekamMedisForm`) selalu mengirim nama layar, jadi cacat ini tidak terlihat di aplikasi, tetapi operasi melanggar kontraknya. Perbaikan: layar `null` langsung ditolak sebelum `contains()` (Tabel 3.5 nomor 3). Bukti sebelum dan sesudah pada Subbab 4.6 |
| C17-UT11 | `tutupSesi(habisWaktu: boolean): void` | Normal | Masuk, lalu `tutupSesi(false)` (tombol Keluar) | Sesi di memori dan basis data tidak aktif, audit "Keluar dari aplikasi" | `SesiAktif.ambil()` `null`, `status_aktif` false, audit ada | Pass |
| C17-UT12 | `tutupSesi(habisWaktu: boolean): void` | Batas | Masuk, lalu `tutupSesi(true)` (batas diam 15 menit) | Sesi ditutup dengan audit "Sesi berakhir setelah diam 15 menit" | Sesi ditutup dan audit sesuai | Pass |
| C17-UT13 | `tutupSesi(habisWaktu: boolean): void` | Negatif | Tanpa sesi (tanpa basis data) | Tidak terjadi apa pun dan tidak ada pengecualian | Tidak ada pengecualian, `SesiAktif.ambil()` tetap `null` | Pass |
| C17-UT14 | `sesiDiputus(): boolean` | Normal | Masuk, lalu `status_aktif` sesi diubah menjadi false dari koneksi lain (akun dinonaktifkan di komputer lain) | `true` | `true` | Pass |
| C17-UT15 | `sesiDiputus(): boolean` | Batas | Masuk, sesi masih aktif di basis data | `false` | `false` | Pass |
| C17-UT16 | `sesiDiputus(): boolean` | Negatif | Tanpa sesi (tanpa basis data) | `false` tanpa membuka koneksi | `false` | Pass |

C17-UT17 ditambahkan setelah nomor C17-UT16, sehingga barisnya diletakkan bersama operasi yang sama. `tutupSesi()` dan `sesiDiputus()` adalah operasi public tambahan implementasi (Tabel B.1 nomor 23 dan 80). Operasi private `bukaSesi()` teruji lewat C17-UT01.

Tabel 4.2.2b. Ringkasan Cakupan Kelas C17

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `autentikasi()`, `periksaHakAkses()` (ditambah `bukaSesi()` yang private) |
| Operasi yang diuji | Kedua operasi public DPPL (2 dari 2) dengan kasus normal, batas, dan negatif, ditambah operasi tambahan `tutupSesi()` dan `sesiDiputus()` dengan ketiga jenis kasus |
| ID Algoritma yang tercakup | Algo-C17-01 (autentikasi: C17-UT01 s.d. C17-UT06), Algo-C17-02 (hak akses peran: C17-UT07 s.d. C17-UT10, C17-UT17), Algo-C31-01 bagian penutupan sesi (C17-UT11 s.d. C17-UT13) |
| Coverage (wajib ≥ 70%) | 85,7% baris (48 dari 56) dan 92,3% cabang (24 dari 26), diukur setelah perbaikan Tabel 3.5 dengan JaCoCo 0.8.14 dari seluruh `mvn test`. Baris yang tidak tercakup adalah penanganan `SQLException` saat basis data tidak dapat dihubungi. Bukti pada Subbab 4.6 |

### 4.2.3 Kelas PasienManager

**ID Kelas:** C18  
**Nama Kelas:** PasienManager  
**Tingkat:** Mendalam

*[Diisi Steven: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.4 Kelas KunjunganManager

**ID Kelas:** C19  
**Nama Kelas:** KunjunganManager  
**Tingkat:** Mendalam

*[Diisi Steven: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.5 Kelas SkriningManager

**ID Kelas:** C20  
**Nama Kelas:** SkriningManager  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/control/SkriningManagerTest.java`. C20-UT01 s.d. C20-UT13 berjalan tanpa basis data (validasi rentang, pemeriksaan peran, dan pesan galat). C20-UT14 s.d. C20-UT32 memakai antrean skrining seed hari ini (kunjungan 3, 2, dan 11). Test yang membutuhkan basis data memakai sesi Perawat dan `FiksturSkrining`, yang mencatat seed sebelum tiap test lalu memulihkan status kunjungan, status entri daftar pantau, isi antrean skrining, dan baris baru (skrining, penanda, entri pantau, tindak lanjut, rekam medis, audit) sesudahnya.

Tabel 4.2.5. Hasil Pengujian Unit Kelas C20

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C20-UT01 | `validasiRentang(tv: TandaVitalEntity): List<String>` | Normal | `manager.validasiRentang(normal()).isEmpty()` | Seluruh nilai dalam rentang tidak menghasilkan galat. | benar | Pass |
| C20-UT02 | `validasiRentang(tv: TandaVitalEntity): List<String>` | Batas | `TandaVitalEntity tv = new TandaVitalEntity(300, 200, 350, 250, 45, 250, 1000); manager.validasiRentang(tv).isEmpty(); manager.validasiRentang(tv)` | Nilai tepat di batas rentang diterima dan satu di luarnya ditolak. | benar; `List.of("nadi")` | Pass |
| C20-UT03 | `validasiRentang(tv: TandaVitalEntity): List<String>` | Negatif | `TandaVitalEntity tv = normal(); tv.setSuhu(72); manager.validasiRentang(tv)` | Suhu 72 di luar rentang ditolak. | `List.of("suhu")` | Pass |
| C20-UT04 | `validasiRentang(tv: TandaVitalEntity): List<String>` | Negatif | `TandaVitalEntity tv = normal(); tv.setSistolik(400); manager.validasiRentang(tv)` | Sistolik 400 di luar rentang ditolak. | `List.of("sistolik")` | Pass |
| C20-UT05 | `validasiRentang(tv: TandaVitalEntity): List<String>` | Batas | `TandaVitalEntity tv = normal(); tv.setGulaDarah(null); manager.validasiRentang(tv).isEmpty(); manager.validasiRentang(tv)` | Gula darah boleh kosong tetapi tidak boleh di luar rentang. | benar; `List.of("gulaDarah")` | Pass |
| C20-UT06 | `validasiRentang(tv: TandaVitalEntity): List<String>` | Negatif | `TandaVitalEntity tv = normal(); tv.setDiastolik(120); manager.validasiRentang(tv)` | Diastolik tidak lebih rendah dari sistolik ditolak. | `List.of("diastolik")` | Pass |
| C20-UT07 | `validasiRentang(tv: TandaVitalEntity): List<String>` | Negatif | `TandaVitalEntity tv = new TandaVitalEntity(0, 0, 0, 0, 0, 0, null); manager.validasiRentang(tv)` | Seluruh kolom bermasalah dilaporkan sekaligus. | `List.of("sistolik", "diastolik", "beratBadan", "tinggiBadan", "suhu",…` | Pass |
| C20-UT08 | `simpanSkrining(idKunjungan: int, keluhan: String, tv: TandaVitalEntity): List<String>` | Negatif | `TandaVitalEntity tv = normal(); tv.setSuhu(72); manager.simpanSkrining(2, "Demam", tv)` | Nilai di luar rentang dikembalikan tanpa menyentuh basis data. | `List.of("suhu")` | Pass |
| C20-UT09 | `simpanSkrining(idKunjungan: int, keluhan: String, tv: TandaVitalEntity): List<String>` | Negatif | `IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.simpanSkrining(2, "Batuk", normal())); e.getMessage()` | Tanpa sesi masuk ditolak. | `"Sesi sudah berakhir, silakan masuk kembali"` | Pass |
| C20-UT10 | `simpanSkrining(idKunjungan: int, keluhan: String, tv: TandaVitalEntity): List<String>` | Negatif | `masukSebagai("Petugas Farmasi"); IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.simpanSkrining(2, "Batuk", normal())); e.getMessage()` | Peran Petugas Farmasi tidak berwenang. | `"Peran Petugas Farmasi tidak berwenang menjalankan tindakan ini"` | Pass |
| C20-UT11 | `panggilPasien(): KunjunganEntity` | Negatif | `IllegalStateException e = assertThrows(IllegalStateException.class, manager::panggilPasien); e.getMessage()` | Tanpa sesi masuk ditolak. | `"Sesi sudah berakhir, silakan masuk kembali"` | Pass |
| C20-UT12 | `lewatiPasien(idKunjungan: int): KunjunganEntity` | Negatif | `masukSebagai("Petugas Administrasi"); IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.lewatiPasien(3)); e.getMessage()` | Peran Petugas Administrasi tidak berwenang. | `"Peran Petugas Administrasi tidak berwenang menjalankan tindakan ini"` | Pass |
| C20-UT13 | `galatTeknis(pesan: String, e: SQLException): IllegalStateException` | Negatif | `SQLException sebab = new SQLException("connection refused to 10.0.0.5"); IllegalStateException e = SkriningManager.galatTeknis("Antrean skrining gagal dimuat", sebab); e.getMessage(); e.getMessage().contains("10.0.0.5"); e.getCause()` | Pesan untuk pengguna tidak membocorkan detail basis data. | `"Antrean skrining gagal dimuat. Hubungi administrator bila berulang."`; salah; `sebab` | Pass |
| C20-UT14 | `muatAntrean(): List<KunjunganEntity>` | Normal | `idAntrean()` | Tiga kunjungan seed hari ini tampil menurut urutan antrean. | `List.of(3, 2, 11)` | Pass |
| C20-UT15 | `muatAntrean(): List<KunjunganEntity>` | Batas | `FiksturSkrining.jalankan("UPDATE kunjungan SET tanggal = CURRENT_DATE - 1 WHERE id_kunjungan = 3")` | Kunjungan dari hari sebelumnya tidak tampil. | Tidak ada pengecualian | Pass |
| C20-UT16 | `muatAntrean(): List<KunjunganEntity>` | Batas | `FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'selesai diperiksa' WHERE id_kunjungan = 2"); idAntrean()` | Kunjungan selesai diperiksa tidak tampil. | `List.of(3, 11)` | Pass |
| C20-UT17 | `muatAntrean(): List<KunjunganEntity>` | Negatif | `FiksturSkrining.jalankan("DELETE FROM antrean_entri"); idAntrean().isEmpty()` | Antrean kosong mengembalikan daftar kosong. | benar | Pass |
| C20-UT18 | `panggilPasien(): KunjunganEntity` | Normal | `KunjunganEntity k = manager.panggilPasien(); k.getIdKunjungan(); k.getStatus(); FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 3")` | Kunjungan terdepan menjadi sedang diskrining. | `3`; `"sedang diskrining"`; `"sedang diskrining"` | Pass |
| C20-UT19 | `panggilPasien(): KunjunganEntity` | Batas | `FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'tidak hadir' WHERE id_kunjungan = 3"); FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'selesai diperiksa' WHERE id_kunjungan IN (2, 11)"); KunjunganEntity k = manager.panggilPasien(); k.getIdKu…` | Pasien tidak hadir dipanggil bila tidak ada yang menunggu. | `3`; `"sedang diskrining"` | Pass |
| C20-UT20 | `panggilPasien(): KunjunganEntity` | Negatif | `FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'selesai diperiksa' WHERE id_kunjungan IN (2, 3, 11)"); manager.panggilPasien()` | Tanpa pasien menunggu mengembalikan null. | null | Pass |
| C20-UT21 | `lewatiPasien(idKunjungan: int): KunjunganEntity` | Normal | `manager.panggilPasien(); KunjunganEntity berikutnya = manager.lewatiPasien(3); berikutnya.getIdKunjungan(); FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 3"); idAntrean()` | Pasien dilewati pindah ke belakang dan pasien berikutnya dikembalikan. | `2`; `"tidak hadir"`; `List.of(2, 11, 3)` | Pass |
| C20-UT22 | `lewatiPasien(idKunjungan: int): KunjunganEntity` | Batas | `manager.panggilPasien(); manager.lewatiPasien(3); FiksturSkrining.jalankan("UPDATE kunjungan SET status = 'selesai diperiksa' WHERE id_kunjungan IN (2, 11)"); KunjunganEntity k = manager.panggilPasien(); k.getIdKunjungan()` | Pasien yang dilewati dapat dipanggil lagi sesudah yang menunggu. | `3` | Pass |
| C20-UT23 | `lewatiPasien(idKunjungan: int): KunjunganEntity` | Negatif | `IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.lewatiPasien(2)); e.getMessage(); idAntrean()` | Pasien yang belum dipanggil ditolak dan antrean tidak berubah. | `"Hanya pasien yang sedang diskrining yang dapat dilewati"`; `List.of(3, 2, 11)` | Pass |
| C20-UT24 | `lewatiPasien(idKunjungan: int): KunjunganEntity` | Negatif | `IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.lewatiPasien(-1)); e.getMessage()` | Kunjungan yang tidak ada ditolak. | `"Kunjungan tidak ditemukan: -1"` | Pass |
| C20-UT25 | `simpanSkrining(idKunjungan: int, keluhan: String, tv: TandaVitalEntity): List<String>` | Normal | `List<String> galat = manager.simpanSkrining(2, "Pusing ringan", normal()); galat.isEmpty(); FiksturSkrining.angka("SELECT count(*) FROM skrining WHERE id_kunjungan = 2"); FiksturSkrining.teks("SELECT status FROM kunjungan WHERE id_kunjungan = 2")` | Tersimpan, status siap diperiksa, keluar dari antrean, dan diaudit. | benar; `1`; `"siap diperiksa dokter"`; dan 3 pemeriksaan serupa lainnya | Pass |
| C20-UT26 | `simpanSkrining(idKunjungan: int, keluhan: String, tv: TandaVitalEntity): List<String>` | Batas | `TandaVitalEntity tv = new TandaVitalEntity(140, 90, 65, 170, 36.5, 72, 100); manager.simpanSkrining(11, "Sakit kepala", tv).isEmpty(); FiksturSkrining.teks( "SELECT jenis_risiko FROM penanda_risiko WHERE id_kunjungan = 11"); FiksturSkrining.angka( "SELECT cou…` | Sistolik tepat 140 menerbitkan penanda hipertensi dan entri pantau. | benar; `"hipertensi"`; `1` | Pass |
| C20-UT27 | `simpanSkrining(idKunjungan: int, keluhan: String, tv: TandaVitalEntity): List<String>` | Batas | `TandaVitalEntity tv = new TandaVitalEntity(150, 95, 100, 165, 36.8, 80, 250); manager.simpanSkrining(2, "Lemas", tv).isEmpty(); FiksturSkrining.angka("SELECT count(*) FROM penanda_risiko WHERE id_kunjungan = 2")` | Tiga jenis risiko terbit sekaligus dalam satu transaksi. | benar; `3` | Pass |
| C20-UT28 | `simpanSkrining(idKunjungan: int, keluhan: String, tv: TandaVitalEntity): List<String>` | Negatif | `long sebelum = FiksturSkrining.angka("SELECT count(*) FROM skrining"); IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.simpanSkrining(-1, "Batuk", normal())); e.getMessage(); FiksturSkrining.angka("SELECT count(*) FROM …` | Kunjungan yang tidak ada ditolak dan tidak ada data tersimpan. | `"Kunjungan tidak ditemukan: -1"`; `sebelum` | Pass |
| C20-UT29 | `simpanSkrining(idKunjungan: int, keluhan: String, tv: TandaVitalEntity): List<String>` | Negatif | `long sebelum = FiksturSkrining.angka("SELECT count(*) FROM audit"); TandaVitalEntity tv = new TandaVitalEntity(150, 95, 100, 165, 36.8, 80, 250); manager.simpanSkrining(9, "Ulang", tv); FiksturSkrining.angka("SELECT count(*) FROM skrining WHERE id_kunjungan =…` | Skrining ganda gagal dan seluruh transaksi dibatalkan. | melempar `IllegalArgumentException`; `1`; `1`; dan 2 pemeriksaan serupa lainnya | Pass |
| C20-UT30 | `labelPasien(daftar: List<KunjunganEntity>): Map<Integer, String>` | Normal | `List<KunjunganEntity> antrean = manager.muatAntrean(); Map<Integer, String> label = manager.labelPasien(antrean); String nama = FiksturSkrining.teks("SELECT nama FROM pasien WHERE id_pasien = 3"); label.get(3).startsWith(nama + " / RM-"); label.get(3).endsWit…` | Label memuat nama, nomor RM, poli, dan nomor antrean. | benar; benar; `3` | Pass |
| C20-UT31 | `labelPasien(daftar: List<KunjunganEntity>): Map<Integer, String>` | Batas | `manager.labelPasien(List.of()).isEmpty()` | Daftar kosong menghasilkan peta kosong. | benar | Pass |
| C20-UT32 | `panggilPasien(): KunjunganEntity` | Normal | `KunjunganEntity pertama = manager.panggilPasien(); KunjunganEntity kedua = manager.panggilPasien(); kedua; pertama.getIdKunjungan() == kedua.getIdKunjungan()` | Pemanggilan berurutan memanggil pasien berbeda. | tidak null; salah | Pass |

Operasi `validasiRentang()` bersifat *private* pada DPPL dan dibuat *package-private* agar dapat diuji (Tabel B.1 Laporan Implementasi). `labelPasien()` adalah operasi public tambahan implementasi sehingga ikut diuji, dan `galatTeknis()` diuji karena memastikan pesan untuk pengguna tidak membocorkan rincian basis data.

Tabel 4.2.5b. Ringkasan Cakupan Kelas C20

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `muatAntrean()`, `panggilPasien()`, `lewatiPasien()`, `simpanSkrining()`, `validasiRentang()` |
| Operasi yang diuji | Kelima operasi DPPL (5 dari 5) memiliki kasus normal, batas, dan negatif, ditambah operasi public tambahan `labelPasien()` (normal dan batas) |
| ID Algoritma yang tercakup | Algo-C20-01 (`simpanSkrining()`: C20-UT08 s.d. C20-UT10 dan C20-UT25 s.d. C20-UT29), Algo-C20-02 (`validasiRentang()`: C20-UT01 s.d. C20-UT07), Algo-C20-03 (`lewatiPasien()`: C20-UT12 dan C20-UT21 s.d. C20-UT24) |
| Coverage (wajib ≥ 70%) | 91,5% baris (119 dari 130) dan 88,9% cabang (48 dari 54), diukur dengan JaCoCo 0.8.14 dari seluruh `mvn test` dengan basis data uji. Baris yang tidak tercakup adalah sepuluh baris penanganan `SQLException` pada lima operasi yang membuka koneksi, yang hanya terjadi bila basis data tidak dapat dihubungi, dan satu baris pada `pertamaMenunggu()`. |

### 4.2.6 Kelas RisikoManager

**ID Kelas:** C21  
**Nama Kelas:** RisikoManager  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/control/RisikoManagerTest.java`. C21-UT01 s.d. C21-UT10 menguji penentuan jenis risiko (`jenisRisikoTerpenuhi()`) tanpa basis data. C21-UT11 s.d. C21-UT18 memakai basis data uji: penerbitan penanda dan entri daftar pantau pada satu transaksi, serta pemuatan daftar pantau. Test yang membutuhkan basis data memakai sesi Perawat dan `FiksturSkrining`, yang mencatat seed sebelum tiap test lalu memulihkan status kunjungan, status entri daftar pantau, isi antrean skrining, dan baris baru (skrining, penanda, entri pantau, tindak lanjut, rekam medis, audit) sesudahnya.

Tabel 4.2.6. Hasil Pengujian Unit Kelas C21

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C21-UT01 | `jenisRisikoTerpenuhi(daftarAmbang, riwayat, tv): List<String>` | Normal | `RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 65, 100)).isEmpty()` | Nilai normal tidak menghasilkan risiko. | benar | Pass |
| C21-UT02 | `jenisRisikoTerpenuhi(daftarAmbang, riwayat, tv): List<String>` | Batas | `RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(140, 65, null))` | Sistolik tepat 140 dianggap hipertensi. | `List.of("hipertensi")` | Pass |
| C21-UT03 | `jenisRisikoTerpenuhi(daftarAmbang, riwayat, tv): List<String>` | Normal | `RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 90, null))` | IMT melampaui 30 dianggap obesitas. | `List.of("obesitas")` | Pass |
| C21-UT04 | `jenisRisikoTerpenuhi(daftarAmbang, riwayat, tv): List<String>` | Batas | `RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 65, null)).isEmpty(); RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(120, 65, 250))` | Gula darah kosong tidak pernah melampaui, 250 melampaui. | benar; `List.of("indikasi diabetes")` | Pass |
| C21-UT05 | `jenisRisikoTerpenuhi(daftarAmbang, riwayat, tv): List<String>` | Normal | `List<TandaVitalEntity> riwayat = List.of(tv(150, 65, null), tv(145, 65, null), tv(160, 65, null)); RisikoManager.jenisRisikoTerpenuhi(AMBANG, riwayat, tv(120, 65, null))` | Tiga kunjungan beruntun di atas ambang menandai meski hari ini turun. | `List.of("hipertensi")` | Pass |
| C21-UT06 | `jenisRisikoTerpenuhi(daftarAmbang, riwayat, tv): List<String>` | Batas | `List<TandaVitalEntity> riwayat = List.of(tv(150, 65, null), tv(145, 65, null)); RisikoManager.jenisRisikoTerpenuhi(AMBANG, riwayat, tv(120, 65, null)).isEmpty()` | Hanya dua kunjungan di atas ambang tidak cukup. | benar | Pass |
| C21-UT07 | `jenisRisikoTerpenuhi(daftarAmbang, riwayat, tv): List<String>` | Negatif | `List<TandaVitalEntity> riwayat = List.of(tv(150, 65, null), tv(130, 65, null), tv(160, 65, null)); RisikoManager.jenisRisikoTerpenuhi(AMBANG, riwayat, tv(120, 65, null)).isEmpty()` | Satu dari tiga kunjungan di bawah ambang memutus rentetan. | benar | Pass |
| C21-UT08 | `jenisRisikoTerpenuhi(daftarAmbang, riwayat, tv): List<String>` | Normal | `RisikoManager.jenisRisikoTerpenuhi(AMBANG, List.of(), tv(160, 95, 300))` | Beberapa risiko sekaligus semuanya dilaporkan. | `List.of("hipertensi", "obesitas", "indikasi diabetes")` | Pass |
| C21-UT09 | `sudahDipantau(terbuka, idPasien, jenis): boolean` | Negatif | `List<DaftarPantauEntity> terbuka = List.of( new DaftarPantauEntity(1, 7, "hipertensi", DaftarPantauEntity.STATUS_ULANG)); RisikoManager.sudahDipantau(terbuka, 7, "hipertensi"); RisikoManager.sudahDipantau(terbuka, 7, "obesitas"); RisikoManager.sudahDipantau(t…` | Entri terbuka hanya cocok untuk pasien dan jenis risiko yang sama. | benar; salah; salah | Pass |
| C21-UT10 | `nilaiUkur(jenisRisiko: String, tv: TandaVitalEntity): Double` | Negatif | `RisikoManager.nilaiUkur("anemia", tv(120, 65, 100))` | Jenis risiko yang tidak dikenal tidak memiliki nilai. | `null` | Pass |
| C21-UT11 | `evaluasiRisiko(kunjungan: KunjunganEntity, tv: TandaVitalEntity): List<PenandaRisikoEntity>` | Normal | `List<PenandaRisikoEntity> hasil = manager.evaluasiRisiko(kunjungan(2, 4), tv(150, 65, null)); hasil.size(); hasil.get(0).getJenisRisiko(); hasil.get(0).getIdPenanda() > 0` | Sistolik 150 menerbitkan penanda aktif dan entri daftar pantau. | `1`; `"hipertensi"`; benar; dan 2 pemeriksaan serupa lainnya | Pass |
| C21-UT12 | `evaluasiRisiko(kunjungan: KunjunganEntity, tv: TandaVitalEntity): List<PenandaRisikoEntity>` | Negatif | `long penandaSebelum = FiksturSkrining.angka("SELECT count(*) FROM penanda_risiko"); long pantauSebelum = FiksturSkrining.angka("SELECT count(*) FROM daftar_pantau"); List<PenandaRisikoEntity> hasil = manager.evaluasiRisiko(kunjungan(3, 3), tv(120, 65, 100)); …` | Nilai normal tidak menerbitkan penanda maupun entri pantau. | benar; `penandaSebelum`; `pantauSebelum` | Pass |
| C21-UT13 | `evaluasiRisiko(kunjungan: KunjunganEntity, tv: TandaVitalEntity): List<PenandaRisikoEntity>` | Batas | `long sebelum = FiksturSkrining.angka( "SELECT count(*) FROM daftar_pantau WHERE id_pasien = 1 AND jenis_risiko = 'hipertensi'"); List<PenandaRisikoEntity> hasil = manager.evaluasiRisiko(kunjungan(9, 1), tv(150, 65, null)); hasil.size(); FiksturSkrining.angka(…` | Pasien yang sudah dipantau tidak dimasukkan dua kali. | `1`; `sebelum` | Pass |
| C21-UT14 | `evaluasiRisiko(kunjungan: KunjunganEntity, tv: TandaVitalEntity): List<PenandaRisikoEntity>` | Batas | `List<PenandaRisikoEntity> hasil = manager.evaluasiRisiko(kunjungan(9, 1), tv(120, 65, null)); hasil.stream().map(PenandaRisikoEntity::getJenisRisiko).toList()` | Tiga kunjungan sebelumnya di atas ambang menandai walau hari ini normal. | `List.of("hipertensi")` | Pass |
| C21-UT15 | `ambilPenanda(idKunjungan: int): List<PenandaRisikoEntity>` | Normal | `List<PenandaRisikoEntity> penanda = manager.ambilPenanda(9); penanda.size(); penanda.get(0).getJenisRisiko(); penanda.get(0).getStatus()` | Penanda aktif kunjungan seed dikembalikan. | `1`; `"hipertensi"`; `"aktif"` | Pass |
| C21-UT16 | `ambilPenanda(idKunjungan: int): List<PenandaRisikoEntity>` | Negatif | `manager.ambilPenanda(2).isEmpty(); manager.ambilPenanda(-1).isEmpty()` | Kunjungan tanpa penanda mengembalikan daftar kosong. | benar; benar | Pass |
| C21-UT17 | `muatEntriAktif(): List<DaftarPantauEntity>` | Normal | `List<DaftarPantauEntity> entri = manager.muatEntriAktif(); entri.size(); entri.stream().anyMatch(e -> "kunjungan ulang".equals(e.getJenisRisiko()) && e.getTanggalPemicu() != null); entri.stream().anyMatch(e -> "hipertensi".equals(e.getJenisRisiko()))` | Entri risiko dan jadwal kontrol seed tergabung. | `4`; benar; benar | Pass |
| C21-UT18 | `muatEntriAktif(): List<DaftarPantauEntity>` | Batas | `long idJadwal = FiksturSkrining.angka("INSERT INTO jadwal_kontrol (id_pasien, tanggal_rencana, status) " + "VALUES (4, DATE '2099-01-01', 'terjadwal') RETURNING id_jadwal")` | Jadwal baru menjadi satu entri kunjungan ulang dan tidak ganda saat dibuka lagi. | Tidak ada pengecualian | Pass |

`ambilPenanda()` adalah operasi public tambahan implementasi (untuk ikon dan warna merah pada `SkriningForm`), sedangkan `jenisRisikoTerpenuhi()`, `sudahDipantau()`, dan `nilaiUkur()` adalah pecahan *package-private* dari Algo-C21-01 agar dapat diuji tanpa basis data.

Tabel 4.2.6b. Ringkasan Cakupan Kelas C21

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `evaluasiRisiko()`, `muatEntriAktif()` |
| Operasi yang diuji | Kedua operasi DPPL (2 dari 2) memiliki kasus normal, batas, dan negatif, ditambah operasi tambahan `ambilPenanda()` (normal dan negatif) |
| ID Algoritma yang tercakup | Algo-C21-01 (`evaluasiRisiko()`: C21-UT01 s.d. C21-UT14), Algo-C21-02 (`muatEntriAktif()`: C21-UT17 dan C21-UT18) |
| Coverage (wajib ≥ 70%) | 90,0% baris (54 dari 60) dan 100,0% cabang (40 dari 40), diukur dengan JaCoCo 0.8.14 dari seluruh `mvn test` dengan basis data uji. Baris yang tidak tercakup adalah enam baris penanganan `SQLException` pada tiga operasi yang membuka koneksi. Seluruh cabang tercakup. |

### 4.2.7 Kelas PemeriksaanManager

**ID Kelas:** C22  
**Nama Kelas:** PemeriksaanManager  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/control/PemeriksaanManagerTest.java`. Test yang membutuhkan basis data memakai sesi Dokter (pengguna 1) dan kunjungan seed pertama yang belum diperiksa dan belum ditutup (kunjungan 2, pasien "menunggu skrining" hari ini), kecuali bila disebut lain.

Tabel 4.2.7. Hasil Pengujian Unit Kelas C22

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C22-UT01 | `susunRingkasan(idKunjungan: int): RekamMedisEntity` | Normal | Kunjungan seed pertama yang sudah diskrining (kunjungan 1) | Ringkasan memuat pasien pemilik kunjungan, skrining kunjungan itu, dan daftar penanda risiko | Pasien yang dimuat sama dengan `id_pasien` kunjungan 1, skrining dan daftar penanda terisi | Pass |
| C22-UT02 | `susunRingkasan(idKunjungan: int): RekamMedisEntity` | Batas | Kunjungan hari ini yang belum diskrining (kunjungan 2) | Ringkasan tetap tersusun dengan skrining `null` dan tanpa penanda | Pasien terisi, skrining `null`, daftar penanda kosong | Pass |
| C22-UT03 | `susunRingkasan(idKunjungan: int): RekamMedisEntity` | Negatif | `susunRingkasan(-1)` | `IllegalArgumentException` "Kunjungan tidak ditemukan: -1" | Pengecualian dan pesan sesuai | Pass |
| C22-UT04 | `susunTren(idPasien: int): List<TandaVitalEntity>` | Normal | Pasien dengan pengukuran terbanyak pada seed | Lebih dari satu titik, terurut dari waktu terlama | Lebih dari satu titik dan waktu setiap titik tidak lebih awal dari titik sebelumnya | Pass |
| C22-UT05 | `susunTren(idPasien: int): List<TandaVitalEntity>` | Batas | Pasien seed dengan tepat satu pengukuran (UC05 alternatif 4) | Tepat satu titik tetap dikembalikan | Satu titik | Pass |
| C22-UT06 | `susunTren(idPasien: int): List<TandaVitalEntity>` | Negatif | `susunTren(-1)` | Daftar kosong, tanpa pengecualian | Daftar kosong | Pass |
| C22-UT07 | `cariKodeICD10(kataKunci: String): List<KodeICD10Entity>` | Normal | Kata kunci "I1" | Kode berawalan "I1" didahulukan, I10 di urutan pertama | I10 di urutan pertama | Pass |
| C22-UT08 | `cariKodeICD10(kataKunci: String): List<KodeICD10Entity>` | Batas | Kata kunci "  hipertensi " (huruf kecil, berspasi tepi) | Spasi diabaikan dan pencarian tidak membedakan huruf, sehingga I10 ditemukan | Hasil memuat I10 | Pass |
| C22-UT09 | `cariKodeICD10(kataKunci: String): List<KodeICD10Entity>` | Negatif | Kata kunci "xyzzy" (UC05 alternatif 2) | Daftar kosong | Daftar kosong | Pass |
| C22-UT10 | `cariKodeICD10(kataKunci: String): List<KodeICD10Entity>` | Negatif | Kata kunci "  " dan `null`, tanpa basis data | Daftar kosong tanpa membuka koneksi | Daftar kosong untuk keduanya | Pass |
| C22-UT11 | `simpanPemeriksaan(idKunjungan: int, p: PemeriksaanEntity, d: DiagnosisEntity): void` | Normal | Kunjungan 2, anamnesis "Pusing", tindakan "Edukasi", diagnosis R51 | Baris pemeriksaan belum terkunci, diagnosis R51, dan audit "Pemeriksaan kunjungan 2 disimpan" oleh pengguna 1 | Ketiga baris ada di basis data | Pass |
| C22-UT12 | `simpanPemeriksaan(idKunjungan: int, p: PemeriksaanEntity, d: DiagnosisEntity): void` | Batas | Simpan dengan R51, lalu simpan ulang dengan tindakan "Edukasi dan obat" dan `d = null` | Tetap satu baris pemeriksaan, tindakan berubah, diagnosis lama tidak terhapus | Satu baris dengan tindakan baru, `muatDiagnosis()` tetap R51 | Pass |
| C22-UT13 | `simpanPemeriksaan(idKunjungan: int, p: PemeriksaanEntity, d: DiagnosisEntity): void` | Negatif | Diagnosis dengan kode " " (tanpa basis data) | `IllegalArgumentException` "Diagnosis harus dipilih dari kode ICD-10" sebelum koneksi dibuka | Pengecualian dan pesan sesuai | Pass |
| C22-UT14 | `simpanPemeriksaan(idKunjungan: int, p: PemeriksaanEntity, d: DiagnosisEntity): void` | Negatif | Pemeriksaan yang sudah dikunci disimpan ulang dengan tindakan lain | `IllegalStateException` "Rekam medis kunjungan ini sudah dikunci" dan isi lama tidak berubah | Pengecualian dan pesan sesuai, tindakan tetap "Edukasi" | Pass |
| C22-UT15 | `simpanPemeriksaan(idKunjungan: int, p: PemeriksaanEntity, d: DiagnosisEntity): void` | Negatif | Sesi berperan Perawat (UC05 alternatif 1) | `IllegalStateException` "Peran Perawat tidak berwenang menjalankan tindakan ini" | Pengecualian dan pesan sesuai | Pass |
| C22-UT16 | `jadwalkanKontrol(idPasien: int, tanggal: LocalDate): JadwalKontrolEntity` | Normal | Pasien kunjungan 2, tanggal 01-01-2099 | Jadwal baru dengan ID > 0 dan status "terjadwal" | Baris `jadwal_kontrol` baru berstatus "terjadwal" | Pass |
| C22-UT17 | `jadwalkanKontrol(idPasien: int, tanggal: LocalDate): JadwalKontrolEntity` | Batas | Tanggal besok (H+1), batas terdekat yang sah | Diterima dan tersimpan | Tersimpan dengan tanggal besok | Pass |
| C22-UT18 | `jadwalkanKontrol(idPasien: int, tanggal: LocalDate): JadwalKontrolEntity` | Batas | Tanggal 01-01-2099 dijadwalkan dua kali berturut-turut | ID jadwal kedua sama dengan yang pertama, hanya satu baris | ID sama, satu baris | Pass |
| C22-UT19 | `jadwalkanKontrol(idPasien: int, tanggal: LocalDate): JadwalKontrolEntity` | Negatif | Tanggal hari ini dan `null` | `IllegalArgumentException` untuk keduanya | Keduanya ditolak | Pass |
| C22-UT20 | `kunciRekamMedis(idKunjungan: int): void` | Normal | Draf berdiagnosis J00, kunjungan tanpa resep | Pemeriksaan terkunci, kunjungan "selesai", bundel FHIR berisi resource `Condition`, dan audit "Pemeriksaan kunjungan 2 dikunci" | Keempatnya terbaca dari basis data | Pass |
| C22-UT21 | `kunciRekamMedis(idKunjungan: int): void` | Batas | Kunjungan masih punya resep "menunggu" di apotek | Kunjungan "selesai diperiksa" dan belum dibundel, karena apotek yang akan menutupnya | Status "selesai diperiksa", tidak ada bundel | Pass |
| C22-UT22 | `kunciRekamMedis(idKunjungan: int): void` | Batas | Satu-satunya resep berstatus "dikembalikan" | Resep yang dikembalikan tidak menahan, sehingga kunjungan "selesai" dan dibundel | Status "selesai", satu bundel | Pass |
| C22-UT23 | `kunciRekamMedis(idKunjungan: int): void` | Batas | Pasien punya jadwal kontrol bertanggal hari ini beserta entri daftar pantau "kunjungan ulang" | Jadwal menjadi "selesai" dan entrinya "selesai ditindaklanjuti" | Keduanya berubah | Pass |
| C22-UT24 | `kunciRekamMedis(idKunjungan: int): void` | Negatif | Kunjungan tanpa pemeriksaan | `IllegalStateException` "Pemeriksaan belum disimpan" | Pengecualian dan pesan sesuai | Pass |
| C22-UT25 | `kunciRekamMedis(idKunjungan: int): void` | Negatif | Pemeriksaan tanpa diagnosis | `IllegalStateException` "Diagnosis ICD-10 belum diisi" dan pemeriksaan tidak terkunci | Pengecualian dan pesan sesuai, tidak terkunci | Pass |
| C22-UT26 | `kunciRekamMedis(idKunjungan: int): void` | Negatif | Penguncian kedua pada kunjungan yang sama | Ditolak dan audit penguncian tetap satu | Ditolak, satu audit | Pass |
| C22-UT27 | `kunciRekamMedis(idKunjungan: int): void` | Negatif | Tanpa sesi masuk | `IllegalStateException` "Sesi sudah berakhir, silakan masuk kembali" | Pengecualian dan pesan sesuai | Pass |
| C22-UT28 | `validasiTanggalKontrol(tanggal: LocalDate): void` | Normal | Tanggal besok | Tidak ada pengecualian | Tidak ada pengecualian | Pass |
| C22-UT29 | `validasiTanggalKontrol(tanggal: LocalDate): void` | Batas | Tanggal hari ini | `IllegalArgumentException` "Tanggal kontrol harus setelah hari ini" | Pengecualian dan pesan sesuai | Pass |
| C22-UT30 | `validasiTanggalKontrol(tanggal: LocalDate): void` | Negatif | `null` dan tanggal kemarin | `IllegalArgumentException` untuk keduanya | Keduanya ditolak | Pass |
| C22-UT31 | `muatPemeriksaan(idKunjungan: int): PemeriksaanEntity` | Normal | Setelah draf "Batuk pilek" disimpan | Draf dikembalikan dan belum terkunci | Anamnesis "Batuk pilek", `terkunci = false` | Pass |
| C22-UT32 | `muatPemeriksaan(idKunjungan: int): PemeriksaanEntity` | Negatif | Kunjungan tanpa pemeriksaan | `null` | `null` | Pass |
| C22-UT33 | `muatDiagnosis(idKunjungan: int): DiagnosisEntity` | Normal | Draf berdiagnosis J00 | Diagnosis J00 | J00 | Pass |
| C22-UT34 | `muatDiagnosis(idKunjungan: int): DiagnosisEntity` | Negatif | Pemeriksaan tanpa diagnosis | `null` | `null` | Pass |
| C22-UT35 | `muatKunjunganSiapPeriksa(): Map<KunjunganEntity, PasienEntity>` | Normal | Seed berisi kunjungan "siap diperiksa dokter" hari ini (kunjungan 9) | Kunjungan 9 tampil. Setiap kunjungan bertanggal hari ini dan dipasangkan dengan pasiennya | Sesuai | Pass |
| C22-UT36 | `muatKunjunganSiapPeriksa(): Map<KunjunganEntity, PasienEntity>` | Negatif | Kunjungan "menunggu skrining" hari ini (kunjungan 2) | Tidak tampil | Tidak tampil | Pass |
| C22-UT37 | `jadwalkanKontrol(idPasien: int, tanggal: LocalDate): JadwalKontrolEntity` | Batas | Pasien sudah punya jadwal 01-01-2099 yang sudah memicu entri daftar pantau (perawat sudah membuka daftar pantau), lalu dijadwalkan lagi pada tanggal yang sama | Jadwal lama dikembalikan dan tetap satu baris (Tabel B.1 nomor 53) | Pada `v9.0`: jadwal baru dibuat. Test mengharapkan ID 7 tetapi mendapat ID 8, sehingga ada dua jadwal pada tanggal yang sama. Setelah perbaikan: jadwal lama dikembalikan dan tetap satu baris | Fail pada `v9.0`, Pass setelah perbaikan. Penyebab: pencarian jadwal lama memakai `DataKlinisBoundary.ambilJadwalKontrol()` (Q-C59-17), yang hanya memuat jadwal "terjadwal" yang belum memicu entri daftar pantau, sehingga pasien muncul dua kali di daftar pantau (UC05-TC08). Perbaikan: pencarian memakai kueri baru Q-C59-20 per pasien dan tanggal (Tabel 3.5 nomor 1). Bukti sebelum dan sesudah pada Subbab 4.6 |

Operasi `validasiTanggalKontrol()`, `muatPemeriksaan()`, `muatDiagnosis()`, dan `muatKunjunganSiapPeriksa()` adalah operasi public tambahan implementasi (Tabel B.1 nomor 50, 52, dan 53), sehingga ikut diuji. Operasi private `selesaikanJadwalKontrol()` teruji lewat C22-UT23.

Tabel 4.2.7b. Ringkasan Cakupan Kelas C22

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `susunRingkasan()`, `susunTren()`, `cariKodeICD10()`, `simpanPemeriksaan()`, `jadwalkanKontrol()`, `kunciRekamMedis()` |
| Operasi yang diuji | Keenam operasi DPPL (6 dari 6), ditambah empat operasi public tambahan implementasi. Setiap operasi DPPL memiliki kasus normal, batas, dan negatif |
| ID Algoritma yang tercakup | Algo-C22-01 (penyimpanan pemeriksaan: C22-UT11 s.d. C22-UT15), Algo-C22-02 (penguncian rekam medis: C22-UT20 s.d. C22-UT27) |
| Coverage (wajib ≥ 70%) | 84,0% baris (105 dari 125) dan 91,3% cabang (42 dari 46), diukur setelah perbaikan Tabel 3.5 dengan JaCoCo 0.8.14 dari seluruh `mvn test`. Baris yang tidak tercakup adalah penanganan `SQLException` yang hanya terjadi saat basis data gagal. Bukti pada Subbab 4.6 |

### 4.2.8 Kelas ResepManager

**ID Kelas:** C23  
**Nama Kelas:** ResepManager  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/control/ResepManagerTest.java`. Test memakai sesi Dokter dan data seed: obat 1 Parasetamol (stok 300), obat 5 Glibenklamid (stok 30), dan obat 12 Asam Mefenamat (stok 0). Kunjungan uji adalah kunjungan seed pertama yang belum ditutup. Resep, item, antrean apotek, dan audit yang dibuat dihapus setelah setiap test.

Tabel 4.2.8. Hasil Pengujian Unit Kelas C23

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C23-UT01 | `muatDataObat(): List<ObatEntity>` | Normal | Panggil tanpa masukan | Seluruh obat data master dimuat beserta stoknya | Jumlah obat sama dengan `count(*)` tabel `obat`, Parasetamol berstok 300 | Pass |
| C23-UT02 | `muatDataObat(): List<ObatEntity>` | Batas | Obat dengan stok 0 (Asam Mefenamat) | Tetap dimuat dengan stok 0 agar dokter melihat stok habis | Dimuat dengan stok 0 | Pass |
| C23-UT25 | `muatDataObat(): List<ObatEntity>` | Negatif | Hak baca tabel `obat` untuk akun aplikasi `sehati_app` dicabut sementara oleh akun pemilik, lalu dipulihkan setelah pemanggilan | Pemuatan ditolak dengan `IllegalStateException`, tanpa mengembalikan daftar kosong yang menyesatkan. Setelah hak baca dipulihkan, data obat kembali termuat | `IllegalStateException` "Gagal mengambil data obat" dari `DataFarmasiBoundary`, lalu pemanggilan berikutnya mengembalikan daftar obat | Pass |
| C23-UT03 | `tambahItem(idObat: int, jumlah: int, dosis: String, aturanPakai: String): ItemResepEntity` | Normal | Obat 1, jumlah 5, "500 mg", "3 x 1 sesudah makan" | Item dengan obat, jumlah, dosis, dan aturan pakai yang sama | Keempat nilai sesuai | Pass |
| C23-UT04 | `tambahItem(idObat: int, jumlah: int, dosis: String, aturanPakai: String): ItemResepEntity` | Batas | Jumlah 1 | Diterima | Item berjumlah 1 | Pass |
| C23-UT05 | `tambahItem(idObat: int, jumlah: int, dosis: String, aturanPakai: String): ItemResepEntity` | Batas | Obat 12 (stok 0), jumlah 1 | Item tetap dibuat, karena kecukupan stok dinilai ResepForm dan diperiksa ulang `kirimKeApotek()` | Item untuk obat 12 dibuat | Pass |
| C23-UT06 | `tambahItem(idObat: int, jumlah: int, dosis: String, aturanPakai: String): ItemResepEntity` | Negatif | Jumlah 0 (tanpa basis data) | `IllegalArgumentException` "Jumlah obat harus lebih dari nol" | Pengecualian dan pesan sesuai | Pass |
| C23-UT07 | `tambahItem(idObat: int, jumlah: int, dosis: String, aturanPakai: String): ItemResepEntity` | Negatif | Jumlah -3 (tanpa basis data) | `IllegalArgumentException` | Ditolak | Pass |
| C23-UT08 | `tambahItem(idObat: int, jumlah: int, dosis: String, aturanPakai: String): ItemResepEntity` | Negatif | Obat 9999 (tidak ada di data master) | `IllegalArgumentException` "Obat tidak ada pada data master" | Pengecualian dan pesan sesuai | Pass |
| C23-UT09 | `validasiStok(item: ItemResepEntity, dataFarmasi: DataFarmasiBoundary): boolean` | Normal | Parasetamol × 10 (stok 300) | `true` | `true` | Pass |
| C23-UT10 | `validasiStok(item: ItemResepEntity, dataFarmasi: DataFarmasiBoundary): boolean` | Batas | Parasetamol × 300 dan × 301 | `true` untuk 300 (sama dengan stok), `false` untuk 301 | `true` dan `false` | Pass |
| C23-UT11 | `validasiStok(item: ItemResepEntity, dataFarmasi: DataFarmasiBoundary): boolean` | Batas | Dalam transaksi yang di-*rollback*: Glibenklamid diberi bets kedaluwarsa kemarin sebanyak 10 sehingga saldo 40, lalu diuji × 30 dan × 31 | Bets kedaluwarsa tidak dihitung, jadi stok layak 30: `true` untuk 30, `false` untuk 31 | `true` dan `false` | Pass |
| C23-UT12 | `validasiStok(item: ItemResepEntity, dataFarmasi: DataFarmasiBoundary): boolean` | Negatif | Asam Mefenamat × 1 (stok 0) dan obat 9999 × 1 | `false` untuk keduanya | `false` untuk keduanya | Pass |
| C23-UT13 | `kirimKeApotek(idKunjungan: int, items: List<ItemResepEntity>): ResepEntity` | Normal | Parasetamol × 10 dan Amlodipin × 30 | Resep "menunggu" dengan dua item tersimpan dan masuk antrean apotek | Resep "menunggu", dua baris `item_resep`, satu entri antrean apotek | Pass |
| C23-UT14 | `kirimKeApotek(idKunjungan: int, items: List<ItemResepEntity>): ResepEntity` | Batas | Dua baris Glibenklamid × 15 (total 30 = stok) | Diterima | Resep tersimpan dengan dua item | Pass |
| C23-UT15 | `kirimKeApotek(idKunjungan: int, items: List<ItemResepEntity>): ResepEntity` | Batas | Dua baris Glibenklamid × 16 (masing-masing lolos, total 32 > stok) | Ditolak karena total per obat melebihi stok, pesan memuat "Glibenklamid", tidak ada resep tersimpan | Ditolak dengan nama obat, jumlah resep tidak berubah | Pass |
| C23-UT16 | `kirimKeApotek(idKunjungan: int, items: List<ItemResepEntity>): ResepEntity` | Negatif | Asam Mefenamat × 1 (stok 0) | `IllegalStateException` "Stok tidak cukup, perbaiki resep untuk: Asam Mefenamat 500 mg", tidak tersimpan | Pengecualian dan pesan sesuai, tidak tersimpan | Pass |
| C23-UT17 | `kirimKeApotek(idKunjungan: int, items: List<ItemResepEntity>): ResepEntity` | Negatif | Kunjungan berstatus "selesai" | `IllegalStateException`, tidak tersimpan | Ditolak, tidak tersimpan | Pass |
| C23-UT18 | `kirimKeApotek(idKunjungan: int, items: List<ItemResepEntity>): ResepEntity` | Negatif | Daftar item kosong dan `null` (tanpa basis data) | `IllegalArgumentException` "Resep belum berisi obat" | Keduanya ditolak dengan pesan sesuai | Pass |
| C23-UT19 | `kirimKeApotek(idKunjungan: int, items: List<ItemResepEntity>): ResepEntity` | Negatif | Resep kedua dikirim saat resep pertama kunjungan yang sama masih "menunggu" | Ditolak, hanya satu resep menunggu | Ditolak, satu resep | Pass |
| C23-UT20 | `kirimKeApotek(idKunjungan: int, items: List<ItemResepEntity>): ResepEntity` | Negatif | Kunjungan -1 (tidak ada) | `IllegalStateException`, tidak tersimpan | Ditolak, tidak tersimpan | Pass |
| C23-UT21 | `kirimKeApotek(idKunjungan: int, items: List<ItemResepEntity>): ResepEntity` | Negatif | Sesi berperan Petugas Farmasi (tanpa basis data) | `IllegalStateException` "Peran Petugas Farmasi tidak berwenang menjalankan tindakan ini" | Pengecualian dan pesan sesuai | Pass |
| C23-UT22 | `alasanDikembalikan(idKunjungan: int): String` | Normal | Resep "dikembalikan" dengan catatan "Stok amoksisilin rusak" | Catatan tersebut dikembalikan | "Stok amoksisilin rusak" | Pass |
| C23-UT23 | `alasanDikembalikan(idKunjungan: int): String` | Batas | Dua resep "dikembalikan" dengan catatan berbeda | Catatan resep terakhir | "Catatan kedua" | Pass |
| C23-UT24 | `alasanDikembalikan(idKunjungan: int): String` | Negatif | Hanya ada resep "menunggu" | `null` | `null` | Pass |

`muatDataObat()` tidak menerima masukan, jadi kasus negatifnya (C23-UT25) adalah basis data yang menolak akses. Kasus ini ditambahkan setelah nomor C23-UT24 sehingga barisnya diletakkan bersama operasi yang sama. `validasiStok()` bervisibilitas private pada DPPL dan package-private pada implementasi (Tabel B.1 nomor 56), lalu diuji langsung karena memuat inti Algo-C23-01. `alasanDikembalikan()` adalah operasi public tambahan (Tabel B.1 nomor 70).

Tabel 4.2.8b. Ringkasan Cakupan Kelas C23

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `muatDataObat()`, `tambahItem()`, `kirimKeApotek()` (ditambah `validasiStok()` yang private) |
| Operasi yang diuji | Ketiga operasi public DPPL (3 dari 3) masing-masing dengan kasus normal, batas, dan negatif, `validasiStok()`, dan operasi tambahan `alasanDikembalikan()` |
| ID Algoritma yang tercakup | Algo-C23-01 (penambahan item dan pemeriksaan stok: C23-UT03 s.d. C23-UT12), Algo-C23-02 (pengiriman resep ke apotek: C23-UT13 s.d. C23-UT21) |
| Coverage (wajib ≥ 70%) | 85,5% baris (59 dari 69) dan 95,8% cabang (23 dari 24), JaCoCo 0.8.14 dari seluruh `mvn test`. Bukti pada Subbab 4.6 |

### 4.2.9 Kelas ApotekManager

**ID Kelas:** C24  
**Nama Kelas:** ApotekManager  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/control/ApotekManagerTest.java`. Test yang membutuhkan basis data memakai sesi Petugas Farmasi (pengguna 4) dan data seed: resep 4 "menunggu" milik Siti Aminah (kunjungan 5, pemeriksaan sudah dikunci) berisi Metformin 500 mg × 60 dan Glibenklamid 5 mg × 30 (stok Glibenklamid tepat 30), resep 1 "selesai dilayani", Amlodipin dengan dua bets (AML-A01 sisa 40 dan AML-B01 sisa 150), dan Asam Mefenamat berstok 0 tanpa bets bersisa. `ApotekManager` membuka koneksinya sendiri, jadi perubahan tidak dapat di-*rollback*. Kelas pendukung test `src/tests/sehati/CadanganFarmasi.java` menyimpan stok, bets, status resep dan kunjungan, antrean apotek, serta nomor baris terakhir sebelum setiap test, lalu memulihkannya setelah test. `potongStok()` diuji tanpa basis data dengan pengganti `DataFarmasiBoundary` yang menyimpan stok dan bets di memori.

Tabel 4.2.9. Hasil Pengujian Unit Kelas C24

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C24-UT01 | `muatAntreanResep(): List<ResepEntity>` | Normal | Panggil tanpa masukan | Seluruh resep "menunggu" dimuat beserta itemnya | Jumlah resep sama dengan `count(*)` resep "menunggu". Resep 4 berada di urutan pertama dengan item Metformin × 60 dan Glibenklamid × 30 | Pass |
| C24-UT02 | `muatAntreanResep(): List<ResepEntity>` | Batas | Resep baru disisipkan belakangan dengan waktu masuk pukul 00.01 hari ini, lebih awal dari resep 4 (08.45) | Urutan mengikuti waktu masuk, bukan urutan penyimpanan | Resep baru di urutan pertama, resep 4 di urutan kedua | Pass |
| C24-UT03 | `muatAntreanResep(): List<ResepEntity>` | Negatif | Hak baca tabel `resep` untuk akun aplikasi `sehati_app` dicabut sementara oleh akun pemilik, lalu dipulihkan setelah test | Pemuatan ditolak dengan `IllegalStateException`, tanpa mengembalikan antrean kosong yang menyesatkan | `IllegalStateException` "Gagal mengambil antrean resep" dari `DataFarmasiBoundary` | Pass |
| C24-UT04 | `layaniResep(idResep: int): ResepEntity` | Normal | Resep 4 | Rincian resep kunjungan 5 beserta kedua itemnya | Kunjungan 5, dua item, aturan pakai item pertama "sesudah makan" | Pass |
| C24-UT05 | `layaniResep(idResep: int): ResepEntity` | Batas | Rincian resep 4 dibuka dua kali berturut-turut | Status dan stok tidak berubah karena operasi ini hanya membaca, sehingga dua petugas boleh membuka resep yang sama | Status tetap "menunggu" dan jumlah stok seluruh obat tetap | Pass |
| C24-UT06 | `layaniResep(idResep: int): ResepEntity` | Negatif | `layaniResep(-1)` | `IllegalArgumentException` "Resep tidak ditemukan" | Pengecualian dan pesan sesuai | Pass |
| C24-UT07 | `layaniResep(idResep: int): ResepEntity` | Negatif | Resep seed yang sudah "selesai dilayani" | Ditolak lebih awal dengan `IllegalStateException` "Resep sudah selesai dilayani" | Pengecualian dan pesan sesuai | Pass |
| C24-UT08 | `serahkanObat(idResep: int): void` | Normal | Resep 4 | Dalam satu transaksi: stok Metformin berkurang 60 dan Glibenklamid 30, saldo setiap obat tetap sama dengan jumlah betsnya, resep "selesai dilayani", kunjungan "selesai", bundel FHIR "tertunda" berisi dua `MedicationRequest`, resep keluar dari antrean apotek, dan audit "Obat resep 4 diserahkan" oleh pengguna 4 | Ketujuh perubahan terbaca dari tabel `obat`, `bets_obat`, `resep`, `kunjungan`, `bundel_fhir`, `antrean_entri`, dan `audit` | Pass |
| C24-UT09 | `serahkanObat(idResep: int): void` | Batas | Glibenklamid berstok 30 diminta tepat 30 | Stok dan bets GLB-A01 menjadi 0, lalu bets yang habis tidak lagi dimuat `ambilBets()` | Stok 0, GLB-A01 sisa 0, `ambilBets(5)` kosong | Pass |
| C24-UT10 | `serahkanObat(idResep: int): void` | Batas | Resep baru Amlodipin × 50, melebihi sisa AML-A01 (40) yang kedaluwarsa lebih dulu | FEFO lintas bets: 40 diambil dari AML-A01 dan 10 dari AML-B01 | AML-A01 sisa 0, AML-B01 sisa 140, stok Amlodipin 140 | Pass |
| C24-UT11 | `serahkanObat(idResep: int): void` | Batas | Dua *thread* memanggil `serahkanObat(4)` pada saat yang sama (UC07 alternatif 2) | Satu berhasil, satu ditolak "Resep sudah selesai dilayani". Stok terpotong tepat sekali dan hanya ada satu bundel | Satu "berhasil" dan satu pesan penolakan. Total stok berkurang sebesar jumlah item resep satu kali, satu bundel | Pass |
| C24-UT12 | `serahkanObat(idResep: int): void` | Negatif | Resep 4 diserahkan, lalu diserahkan lagi | Penyerahan kedua ditolak "Resep sudah selesai dilayani" tanpa memotong stok lagi | Pengecualian dan pesan sesuai, total stok sama dengan setelah penyerahan pertama | Pass |
| C24-UT13 | `serahkanObat(idResep: int): void` | Negatif | Pemeriksaan kunjungan 5 diubah menjadi belum terkunci | `IllegalStateException` "Dokter belum menyimpan final pemeriksaan pasien ini" (Tabel B.1 nomor 65), tanpa perubahan data | Pengecualian dan pesan sesuai. Stok, status resep "menunggu", dan jumlah bundel tetap | Pass |
| C24-UT14 | `serahkanObat(idResep: int): void` | Negatif | `serahkanObat(-1)` | `IllegalArgumentException` "Resep tidak ditemukan" | Pengecualian dan pesan sesuai | Pass |
| C24-UT15 | `serahkanObat(idResep: int): void` | Negatif | Sesi berperan Dokter (tanpa basis data) | `IllegalStateException` "Peran Dokter tidak berwenang menjalankan tindakan ini" sebelum koneksi dibuka | Pengecualian dan pesan sesuai | Pass |
| C24-UT16 | `serahkanObat(idResep: int): void` | Negatif | Bundel lama `{}` untuk kunjungan 5 disisipkan lebih dulu, sehingga `simpanBundel()` melanggar UNIQUE `id_kunjungan` pada langkah terakhir | `IllegalArgumentException` dan seluruh transaksi di-*rollback* (KNF03): stok, bets, status resep dan kunjungan, serta antrean apotek tetap, dan bundel lama tidak tertimpa | Pengecualian dilempar. Total stok dan bets tetap, `menunggu`, status kunjungan tetap "selesai diperiksa", bundel lama tetap `{}`, jumlah entri antrean tetap | Pass |
| C24-UT17 | `potongStok(resep: ResepEntity, dataFarmasi: DataFarmasiBoundary): Map<Integer, ObatEntity>` | Normal | Metformin stok 100 dengan bets MET-A 10 (30 hari lagi), MET-B 40 (400 hari), dan MET-C 50 (800 hari), jumlah 25 | FEFO: MET-A habis, sisanya dari MET-B, MET-C tidak disentuh. Stok menjadi 75 | MET-A = 0, MET-B = 25, MET-C = 50, stok tersimpan 75 | Pass |
| C24-UT18 | `potongStok(resep: ResepEntity, dataFarmasi: DataFarmasiBoundary): Map<Integer, ObatEntity>` | Batas | MET-LAMA 20 kedaluwarsa kemarin dan MET-A 30 (30 hari lagi), jumlah 25 | Bets yang kedaluwarsa dilewati walau paling dekat tanggalnya | MET-LAMA tetap 20, MET-A sisa 5 | Pass |
| C24-UT19 | `potongStok(resep: ResepEntity, dataFarmasi: DataFarmasiBoundary): Map<Integer, ObatEntity>` | Negatif | Glibenklamid stok 20, jumlah 30 | `IllegalStateException` "Stok Glibenklamid 5 mg tidak cukup" sebelum ada yang ditulis | Pengecualian dan pesan sesuai, `simpanStok()` tidak dipanggil | Pass |
| C24-UT20 | `potongStok(resep: ResepEntity, dataFarmasi: DataFarmasiBoundary): Map<Integer, ObatEntity>` | Negatif | Saldo Metformin 50 tetapi 40 di antaranya bets kedaluwarsa, jumlah 25 | `IllegalStateException` "Stok Metformin 500 mg yang belum kedaluwarsa tidak cukup, periksa persediaan" | Pengecualian dan pesan sesuai, tidak ada yang ditulis | Pass |
| C24-UT21 | `potongStok(resep: ResepEntity, dataFarmasi: DataFarmasiBoundary): Map<Integer, ObatEntity>` | Negatif | Saldo Glibenklamid 30 tetapi jumlah bets hanya 10 (data stok rusak, Tabel B.1 nomor 46) | Ditolak dengan `IllegalStateException` tanpa menulis | Ditolak, tidak ada yang ditulis | Pass |
| C24-UT22 | `potongStok(resep: ResepEntity, dataFarmasi: DataFarmasiBoundary): Map<Integer, ObatEntity>` | Negatif | Item berobat 99 yang tidak ada pada data master | `IllegalStateException` "Obat 99 tidak ada pada data master" | Pengecualian dan pesan sesuai | Pass |
| C24-UT23 | `kembalikanKeDokter(idResep: int, alasan: String): void` | Normal | Resep 4, alasan "Stok Glibenklamid rusak, mohon ganti obat" | Resep "dikembalikan" dengan alasan tersebut, keluar dari antrean apotek, kunjungan tetap terbuka, stok tidak berubah, audit "Resep 4 dikembalikan ke dokter: ..." tercatat, dan resep tidak dapat dilayani lagi | Seluruhnya sesuai. Status kunjungan tetap "selesai diperiksa" dan `layaniResep(4)` ditolak | Pass |
| C24-UT24 | `kembalikanKeDokter(idResep: int, alasan: String): void` | Batas | Alasan `"   Kemasan rusak \n"` (berspasi dan berganti baris di tepi) | Disimpan tanpa spasi tepi | Catatan "Kemasan rusak" | Pass |
| C24-UT25 | `kembalikanKeDokter(idResep: int, alasan: String): void` | Negatif | Alasan `""`, `"   "`, dan `null` (tanpa basis data) | `IllegalArgumentException` "Alasan pengembalian wajib diisi" untuk ketiganya | Ketiganya ditolak dengan pesan sesuai | Pass |
| C24-UT26 | `kembalikanKeDokter(idResep: int, alasan: String): void` | Negatif | Resep seed yang sudah "selesai dilayani" | Ditolak `ResepEntity.perbaruiStatus()` dengan `IllegalStateException`, status dan catatan tidak berubah, tanpa audit | Ditolak. Status tetap "selesai dilayani", catatan tetap `null`, 0 audit baru | Pass |
| C24-UT37 | `kembalikanKeDokter(idResep: int, alasan: String): void` | Negatif | `kembalikanKeDokter(-1, "Kemasan rusak")` | `IllegalArgumentException` "Resep tidak ditemukan", tanpa audit | Pengecualian dan pesan sesuai, 0 audit baru | Pass |
| C24-UT27 | `kembalikanKeDokter(idResep: int, alasan: String): void` | Negatif | Sesi berperan Perawat (tanpa basis data) | `IllegalStateException` "Peran Perawat tidak berwenang menjalankan tindakan ini" | Pengecualian dan pesan sesuai | Pass |
| C24-UT28 | `pastikanMenunggu(resep: ResepEntity): void` | Negatif | Resep "menunggu", "selesai dilayani", dan "dikembalikan" | "Menunggu" lolos. Dua status lain ditolak dengan pesan yang berbeda | Lolos, "Resep sudah selesai dilayani", dan "Resep berstatus dikembalikan dan tidak dapat diserahkan" | Pass |
| C24-UT29 | `pastikanTerkunci(pemeriksaan: PemeriksaanEntity): void` | Negatif | Pemeriksaan terkunci, belum terkunci, dan `null` | Terkunci lolos. Dua lainnya ditolak "Dokter belum menyimpan final pemeriksaan pasien ini" | Sesuai | Pass |
| C24-UT30 | `muatBetsResep(resep: ResepEntity): List<BetsObatEntity>` | Normal | Rincian resep 4 | Bets setiap obat resep, kedaluwarsa terdekat lebih dulu untuk I-10-04 | MET-A01, lalu GLB-A01. Dalam satu obat, tanggal kedaluwarsa tidak menurun | Pass |
| C24-UT31 | `muatBetsResep(resep: ResepEntity): List<BetsObatEntity>` | Batas | Resep berisi Asam Mefenamat (stok 0, tanpa bets bersisa) | Daftar kosong tanpa pengecualian | Daftar kosong | Pass |
| C24-UT32 | `muatPasienResep(daftarIdResep: List<Integer>): Map<Integer, PasienEntity>` | Normal | `[4]` | Pasien pemilik resep 4 dipetakan dengan ID resep | Satu entri, nama "Siti Aminah" sama dengan isi tabel `pasien` | Pass |
| C24-UT33 | `muatPasienResep(daftarIdResep: List<Integer>): Map<Integer, PasienEntity>` | Batas | Daftar kosong dan `null` | Peta kosong tanpa pengecualian | Kosong untuk keduanya | Pass |
| C24-UT34 | `muatAsalResep(daftarIdResep: List<Integer>): Map<Integer, String>` | Normal | `[4]` | Nama poli dan nomor antrean kunjungan resep | "Poli Umum / Antrean 2", sama dengan hasil kueri gabungan `kunjungan` dan `poli` | Pass |
| C24-UT35 | `muatAsalResep(daftarIdResep: List<Integer>): Map<Integer, String>` | Batas | Daftar kosong dan `null` | Peta kosong tanpa pengecualian | Kosong untuk keduanya | Pass |
| C24-UT36 | `muatDataObat(): List<ObatEntity>` | Normal | Panggil tanpa masukan | Seluruh obat data master, terurut nama | 12 obat sesuai `count(*)` tabel `obat`, terurut nama | Pass |

C24-UT37 ditambahkan setelah nomor C24-UT36 untuk mencakup resep yang tidak ada pada `kembalikanKeDokter()`, sehingga barisnya diletakkan bersama operasi yang sama. Pada C24-UT17, bets ketiga (MET-C) ditambahkan setelah pengukuran *coverage* pertama agar cabang berhenti lebih awal pada FEFO ikut berjalan. `potongStok()` bervisibilitas private pada DPPL. Pada implementasi ia package-private dan menerima `DataFarmasiBoundary` milik transaksi `serahkanObat()`, lalu diuji langsung karena memuat inti Algo-C24-02. `pastikanMenunggu()` dan `pastikanTerkunci()` adalah pemeriksa status package-private (Tabel B.1 nomor 65). `muatBetsResep()`, `muatPasienResep()`, `muatDataObat()`, dan `muatAsalResep()` adalah operasi public tambahan implementasi (Tabel B.1 nomor 42 dan 90). Operasi private `tutupKunjungan()`, `susunBundel()`, dan `keluarkanDariAntrean()` teruji lewat C24-UT08.

Tabel 4.2.9b. Ringkasan Cakupan Kelas C24

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `muatAntreanResep()`, `layaniResep()`, `serahkanObat()`, `kembalikanKeDokter()` (ditambah `potongStok()`, `tutupKunjungan()`, dan `susunBundel()` yang private) |
| Operasi yang diuji | Keempat operasi public DPPL (4 dari 4), masing-masing dengan kasus normal, batas, dan negatif, ditambah `potongStok()`, kedua pemeriksa status, dan empat operasi public tambahan implementasi |
| ID Algoritma yang tercakup | Algo-C24-01 (penyerahan obat: C24-UT08 s.d. C24-UT16), Algo-C24-02 (pemotongan stok FEFO: C24-UT09, C24-UT10, C24-UT17 s.d. C24-UT22) |
| Coverage (wajib ≥ 70%) | 86,3% baris (126 dari 146) dan 93,2% cabang (41 dari 44), JaCoCo 0.8.14 dari seluruh `mvn test`. Baris yang tidak tercakup adalah penanganan `SQLException` saat koneksi gagal dibuka (`galatTeknis()`), serta dua pengaman yang tidak dapat dicapai lewat data sah: kunjungan resep yang hilang pada `tutupKunjungan()` (dicegah *foreign key*) dan sisa bets setelah FEFO (dicegah pemeriksaan `jumlahLayak()` sebelumnya). Bukti pada Subbab 4.6 |

### 4.2.10 Kelas PersediaanManager

**ID Kelas:** C25  
**Nama Kelas:** PersediaanManager  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/control/PersediaanManagerTest.java`. Test yang membutuhkan basis data memakai sesi Petugas Farmasi (pengguna 4) dan data seed: Kaptopril 25 mg (obat 6) berstok 120 dengan bets KAP-A01, Glibenklamid berstok 30 di bawah ambang 50, Asam Mefenamat berstok 0, dan bets AML-A01 Amlodipin yang kedaluwarsa kurang dari 90 hari lagi. Penerimaan, bets, stok, dan audit yang dibuat dipulihkan `CadanganFarmasi` setelah setiap test. Penolakan validasi terjadi sebelum basis data disentuh, sehingga kasus itu berjalan tanpa basis data. `terbitkanPeringatan()` diuji tanpa basis data lewat versi package-private yang menerima pengganti `DataFarmasiBoundary`, lalu sekali terhadap seed (C25-UT20).

Tabel 4.2.10. Hasil Pengujian Unit Kelas C25

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C25-UT01 | `muatDataObat(): List<ObatEntity>` | Normal | Panggil tanpa masukan | Seluruh obat beserta saldo stoknya, terurut nama | 12 obat, Kaptopril berstok 120, terurut nama | Pass |
| C25-UT02 | `muatDataObat(): List<ObatEntity>` | Batas | Asam Mefenamat berstok 0 | Tetap dimuat agar dapat dipilih saat obatnya masuk | Dimuat dengan stok 0 | Pass |
| C25-UT03 | `muatDataObat(): List<ObatEntity>` | Negatif | Hak baca tabel `obat` untuk `sehati_app` dicabut sementara oleh akun pemilik | Ditolak dengan `IllegalStateException` | `IllegalStateException` "Gagal mengambil data obat" | Pass |
| C25-UT04 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Normal | Kaptopril, faktur "FK-UJI-01", tanggal terima hari ini, bets KAP-UJI-1 × 200 (kedaluwarsa 2 tahun lagi) dan KAP-UJI-2 × 100 (3 tahun lagi) | Stok bertambah 300, penerimaan tersimpan dengan ID baru, kedua bets tertaut ke penerimaan itu, dan audit "Obat masuk Kaptopril 25 mg sejumlah 300, faktur FK-UJI-01" oleh pengguna 4 | Stok 420, satu baris `penerimaan_obat` bertanggal hari ini, dua baris `bets_obat` ber-`id_penerimaan` sama, audit sesuai | Pass |
| C25-UT05 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Batas | Jumlah 1 dan kedaluwarsa besok, batas terdekat yang sah | Diterima | Stok 121, bets KAP-UJI-3 berjumlah 1 | Pass |
| C25-UT06 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Batas | Tanggal terima 30 hari lalu (faktur yang dicatat terlambat) | Diterima dengan tanggal terima tersebut | Penerimaan tersimpan bertanggal `CURRENT_DATE - 30` | Pass |
| C25-UT07 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Batas | Kedaluwarsa sama dengan tanggal terima, yaitu hari ini | Ditolak pada kolom kedaluwarsa | `IllegalArgumentException` "tanggalKedaluwarsa" | Pass |
| C25-UT08 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Jumlah 0 dan kedaluwarsa kemarin (UC08 alternatif 2) | Kedua kolom ditolak sesuai urutan formulir | "jumlah,tanggalKedaluwarsa" | Pass |
| C25-UT09 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Jumlah -5 | Ditolak pada kolom jumlah | "jumlah" | Pass |
| C25-UT10 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Faktur `" "`, tanggal terima `null`, nomor bets "", obat 0, kedaluwarsa `null` | Seluruh kolom ditandai sesuai urutan formulir | "nomorFaktur,tanggalTerima,nomorBets,tanggalKedaluwarsa,obat" | Pass |
| C25-UT11 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Penerimaan dengan daftar bets kosong dan `null` | Ditolak pada kolom nomor bets | "nomorBets" untuk keduanya | Pass |
| C25-UT12 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Tanggal terima besok | Ditolak pada kolom tanggal terima | "tanggalTerima" | Pass |
| C25-UT13 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Dua bets milik obat 6 dan obat 7 dalam satu penerimaan | Ditolak karena satu penerimaan hanya untuk satu obat (DPPL Subbab 5.25) | "obat" | Pass |
| C25-UT14 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Tanggal terima 30 hari lalu dengan kedaluwarsa 10 hari lalu, lalu kedaluwarsa hari ini | Obat kedaluwarsa ditolak walau tanggal terimanya dimundurkan (Tabel B.1 nomor 79) | "tanggalKedaluwarsa" untuk keduanya | Pass |
| C25-UT15 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Sesi berperan Dokter dengan isian yang valid (tanpa basis data) | `IllegalStateException` "Peran Dokter tidak berwenang menjalankan tindakan ini" | Pengecualian dan pesan sesuai | Pass |
| C25-UT16 | `catatPenerimaan(p: PenerimaanObatEntity): void` | Negatif | (a) Nomor bets KAP-A01 yang sudah tercatat untuk Kaptopril. (b) Obat 9999 yang tidak ada pada data master | (a) "nomorBets" (Algo-C61-01), (b) "obat". Kedua transaksi di-*rollback* dan `idPenerimaan` kembali 0 | Kedua pesan sesuai, `idPenerimaan` 0, stok Kaptopril tetap, ID penerimaan terakhir tetap, 0 audit baru | Pass |
| C25-UT17 | `terbitkanPeringatan(): List<String>` | Normal | Pengganti `DataFarmasiBoundary`: Kaptopril stok 20 ambang 50, Parasetamol 300 ambang 100 dengan bets P-0412 (48 hari lagi) dan P-0500 (91 hari lagi) | Peringatan stok Kaptopril dan bets P-0412 bertanggal `dd-MM-yyyy` (Tabel B.1 nomor 41), batas hari yang diminta 90 | "Stok Kaptopril 25 mg tinggal 20, di bawah ambang 50" dan "Bets P-0412 (Parasetamol 500 mg) kedaluwarsa ..." saja, batas hari 90 | Pass |
| C25-UT18 | `terbitkanPeringatan(): List<String>` | Batas | Glibenklamid stok 49 ambang 50 dengan bets 91 hari lagi, Amlodipin stok 60 ambang 60 dengan bets habis (jumlah 0) 10 hari lagi dan bets AML-A tepat 90 hari lagi | Stok 49 dan bets tepat 90 hari diperingatkan. Bets 91 hari, stok sama dengan ambang, dan bets yang sudah habis tidak | Hanya "Stok Glibenklamid 5 mg tinggal 49, di bawah ambang 50" dan "Bets AML-A (Amlodipin 5 mg) kedaluwarsa ..." | Pass |
| C25-UT19 | `terbitkanPeringatan(): List<String>` | Negatif | Tidak ada obat bermasalah | Daftar kosong | Daftar kosong | Pass |
| C25-UT20 | `terbitkanPeringatan(): List<String>` | Normal | Basis data seed | Peringatan stok Glibenklamid dan Asam Mefenamat serta bets AML-A01, tanpa AML-B01 maupun Kaptopril | Sesuai | Pass |

Operasi private `validasiPenerimaan()` (Algo-C25-02) teruji lewat C25-UT07 sampai C25-UT15, dan `tambahStok()` lewat C25-UT04 sampai C25-UT06 serta C25-UT16.

Tabel 4.2.10b. Ringkasan Cakupan Kelas C25

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `muatDataObat()`, `catatPenerimaan()`, `terbitkanPeringatan()` (ditambah `validasiPenerimaan()` dan `tambahStok()` yang private) |
| Operasi yang diuji | Ketiga operasi public DPPL (3 dari 3), masing-masing dengan kasus normal, batas, dan negatif |
| ID Algoritma yang tercakup | Algo-C25-01 (pencatatan obat masuk: C25-UT04 s.d. C25-UT06, C25-UT16), Algo-C25-02 (validasi penerimaan: C25-UT07 s.d. C25-UT15), Algo-C25-03 (peringatan persediaan: C25-UT17 s.d. C25-UT20) |
| Coverage (wajib ≥ 70%) | 89,9% baris (71 dari 79) dan 91,3% cabang (42 dari 46), JaCoCo 0.8.14 dari seluruh `mvn test`. Baris yang tidak tercakup adalah penanganan `SQLException` saat koneksi gagal dibuka. Bukti pada Subbab 4.6 |

### 4.2.11 Kelas PantauManager

**ID Kelas:** C26  
**Nama Kelas:** PantauManager  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/control/PantauManagerTest.java`. C26-UT01 s.d. C26-UT15 dan C26-UT26 berjalan tanpa basis data: pencatatan dan penyaringan diuji dengan `DataBoundary` pengganti, ditambah pemeriksaan peran dan masukan. C26-UT16 s.d. C26-UT25 memakai basis data uji dengan empat entri daftar pantau seed. Test yang membutuhkan basis data memakai sesi Perawat dan `FiksturSkrining`, yang mencatat seed sebelum tiap test lalu memulihkan status kunjungan, status entri daftar pantau, isi antrean skrining, dan baris baru (skrining, penanda, entri pantau, tindak lanjut, rekam medis, audit) sesudahnya.

Tabel 4.2.11. Hasil Pengujian Unit Kelas C26

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C26-UT01 | `catat(dataPantau, dataPasien, idEntri: int, t: TindakLanjutEntity): void` | Normal | `PantauPalsu pantau = new PantauPalsu(entri(1, "hipertensi", LocalDate.of(2026, 11, 1))); PasienPalsu pasien = new PasienPalsu(); manager.catat(pantau, pasien, 1, new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang")); pantau.entri.get(0).getStatusT…` | Tindak lanjut terhubung menutup entri dan menulis rekam medis. | `"selesai ditindaklanjuti"`; `List.of("Tindak lanjut telepon: terhubung, akan datang")`; `1`; dan 1 pemeriksaan serupa lainnya | Pass |
| C26-UT02 | `catat(dataPantau, dataPasien, idEntri: int, t: TindakLanjutEntity): void` | Normal | `PantauPalsu pantau = new PantauPalsu(entri(1, "obesitas", LocalDate.of(2026, 11, 1))); PasienPalsu pasien = new PasienPalsu(); manager.catat(pantau, pasien, 1, new TindakLanjutEntity(0, "pesan singkat", "tidak terhubung", "belum diketahui")); pantau.entri.get…` | Tidak terhubung menandai perlu dihubungi ulang tanpa rekam medis. | `"perlu dihubungi ulang"`; benar; `0`; dan 1 pemeriksaan serupa lainnya | Pass |
| C26-UT03 | `catat(dataPantau, dataPasien, idEntri: int, t: TindakLanjutEntity): void` | Batas | `PantauPalsu pantau = new PantauPalsu(entri(1, "obesitas", LocalDate.of(2026, 11, 1))); PasienPalsu pasien = new PasienPalsu(); manager.catat(pantau, pasien, 1, new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui")); manager.catat(pantau, …` | Entri yang perlu dihubungi ulang masih boleh ditindaklanjuti lagi. | `"selesai ditindaklanjuti"`; `2` | Pass |
| C26-UT04 | `catat(dataPantau, dataPasien, idEntri: int, t: TindakLanjutEntity): void` | Negatif | `DaftarPantauEntity selesai = entri(1, "hipertensi", LocalDate.of(2026, 11, 1)); selesai.setStatusTindakLanjut(DaftarPantauEntity.STATUS_SELESAI); PantauPalsu pantau = new PantauPalsu(selesai); manager.catat(pantau, new PasienPalsu(), 1, new TindakLanjutEntity…` | Entri yang sudah selesai ditolak dan tidak ada upaya tersimpan. | melempar `IllegalStateException`; benar | Pass |
| C26-UT05 | `catatTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Negatif | `manager.catatTindakLanjut(1, new TindakLanjutEntity(0, "merpati", "terhubung", "akan datang")); manager.catatTindakLanjut(1, null)` | Isian di luar pilihan atau kosong ditolak sebelum basis data. | melempar `IllegalArgumentException`; melempar `IllegalArgumentException` | Pass |
| C26-UT06 | `saring(entri, jenisRisiko, mulai, akhir, statusTindakLanjut): List<DaftarPantauEntity>` | Normal | `DaftarPantauEntity selesai = entri(1, "hipertensi", LocalDate.of(2026, 11, 1)); selesai.perbaruiStatus(DaftarPantauEntity.STATUS_SELESAI); List<DaftarPantauEntity> semua = List.of(selesai, entri(2, "hipertensi", LocalDate.of(2026, 11, 2))); ids(PantauManager.…` | Status selesai hanya mengembalikan entri selesai. | `List.of(1)` | Pass |
| C26-UT07 | `saring(entri, jenisRisiko, mulai, akhir, statusTindakLanjut): List<DaftarPantauEntity>` | Normal | `List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)), entri(2, "obesitas", LocalDate.of(2026, 11, 2))); ids(PantauManager.saring(semua, "obesitas", null, null, null))` | Penyaring jenis risiko hanya mengembalikan jenis itu. | `List.of(2)` | Pass |
| C26-UT08 | `saring(entri, jenisRisiko, mulai, akhir, statusTindakLanjut): List<DaftarPantauEntity>` | Batas | `List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 10, 31)), entri(2, "hipertensi", LocalDate.of(2026, 11, 1)), entri(3, "hipertensi", LocalDate.of(2026, 11, 30)), entri(4, "hipertensi", LocalDate.of(2026, 12, 1))); ids(PantauM…` | Rentang tanggal pemicu mengikutkan tanggal mulai dan tanggal akhir. | `List.of(2, 3)` | Pass |
| C26-UT09 | `saring(entri, jenisRisiko, mulai, akhir, statusTindakLanjut): List<DaftarPantauEntity>` | Normal | `List<DaftarPantauEntity> semua = List.of(entri(1, "kunjungan ulang", LocalDate.of(2026, 11, 1)), entri(2, "hipertensi", LocalDate.of(2026, 11, 5)), entri(3, "obesitas", LocalDate.of(2026, 11, 2))); ids(PantauManager.saring(semua, null, null, null, null))` | Tanpa penyaring semua entri tampil dengan entri berisiko lebih dulu. | `List.of(3, 2, 1)` | Pass |
| C26-UT10 | `saring(entri, jenisRisiko, mulai, akhir, statusTindakLanjut): List<DaftarPantauEntity>` | Negatif | `List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 11, 1))); PantauManager.saring(semua, "obesitas", null, null, null).isEmpty()` | Tidak ada yang cocok mengembalikan daftar kosong. | benar | Pass |
| C26-UT11 | `saring(entri, jenisRisiko, mulai, akhir, statusTindakLanjut): List<DaftarPantauEntity>` | Normal | `DaftarPantauEntity ulang = entri(2, "hipertensi", LocalDate.of(2026, 11, 2)); ulang.setStatusTindakLanjut(DaftarPantauEntity.STATUS_ULANG); List<DaftarPantauEntity> semua = List.of(entri(1, "hipertensi", LocalDate.of(2026, 11, 1)), ulang); ids(PantauManager.s…` | Penyaring status tindak lanjut dapat digabung dengan jenis risiko. | `List.of(2)`; `List.of(1)` | Pass |
| C26-UT12 | `saring(entri, jenisRisiko, mulai, akhir, statusTindakLanjut): List<DaftarPantauEntity>` | Batas | `DaftarPantauEntity tanpaTanggal = entri(1, "hipertensi", null); PantauManager.saring(List.of(tanpaTanggal), null, LocalDate.of(2026, 11, 1), null, null) .isEmpty(); ids(PantauManager.saring(List.of(tanpaTanggal), null, null, null, null))` | Entri tanpa tanggal pemicu tidak lolos penyaring tanggal. | benar; `List.of(1)` | Pass |
| C26-UT13 | `saringDaftar(jenisRisiko: String, mulai: LocalDate, akhir: LocalDate, statusTindakLanjut: String): List<DaftarPantauEntity>` | Negatif | `IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.saringDaftar(null, LocalDate.of(2026, 11, 5), LocalDate.of(2026, 11, 1), null)); e.getMessage()` | Tanggal mulai setelah tanggal akhir ditolak. | `"Tanggal mulai tidak boleh setelah tanggal akhir"` | Pass |
| C26-UT14 | `catatTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Negatif | `IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.catatTindakLanjut(2, new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang"))); e.getMessage()` | Tanpa sesi masuk ditolak. | `"Sesi sudah berakhir, silakan masuk kembali"` | Pass |
| C26-UT15 | `catatTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Negatif | `masukSebagai("Petugas Farmasi"); IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.catatTindakLanjut(2, new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang"))); e.getMessage()` | Peran Petugas Farmasi tidak berwenang. | `"Peran Petugas Farmasi tidak berwenang menjalankan tindakan ini"` | Pass |
| C26-UT16 | `saringDaftar(jenisRisiko: String, mulai: LocalDate, akhir: LocalDate, statusTindakLanjut: String): List<DaftarPantauEntity>` | Normal | `List<DaftarPantauEntity> hasil = manager.saringDaftar(null, null, null, null); hasil.size(); hasil.stream().allMatch(e -> e.getNamaPasien() != null && !e.getNamaPasien().isBlank()); hasil.get(hasil.size() - 1).getJenisRisiko()` | Tanpa penyaring empat entri tampil dengan nama pasien terisi. | `4`; benar; `"kunjungan ulang"` | Pass |
| C26-UT17 | `saringDaftar(jenisRisiko: String, mulai: LocalDate, akhir: LocalDate, statusTindakLanjut: String): List<DaftarPantauEntity>` | Normal | `List<DaftarPantauEntity> hasil = manager.saringDaftar("hipertensi", null, null, null); ids(hasil)` | Penyaring jenis hipertensi mengembalikan satu entri. | `List.of(2)` | Pass |
| C26-UT18 | `saringDaftar(jenisRisiko: String, mulai: LocalDate, akhir: LocalDate, statusTindakLanjut: String): List<DaftarPantauEntity>` | Batas | `LocalDate hariIni = LocalDate.now(); List<DaftarPantauEntity> hasil = manager.saringDaftar(null, hariIni, hariIni, null); ids(hasil).stream().sorted().toList()` | Rentang tepat hari ini hanya memuat entri berisiko yang terbit hari ini. | `List.of(1, 2)` | Pass |
| C26-UT19 | `saringDaftar(jenisRisiko: String, mulai: LocalDate, akhir: LocalDate, statusTindakLanjut: String): List<DaftarPantauEntity>` | Negatif | `manager.saringDaftar(null, null, null, DaftarPantauEntity.STATUS_SELESAI).isEmpty()` | Status selesai tidak mengembalikan apa pun pada seed. | benar | Pass |
| C26-UT20 | `catatTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Normal | `long catatanSebelum = FiksturSkrining.angka("SELECT count(*) FROM catatan_rekam_medis"); manager.catatTindakLanjut(2, terhubung()); FiksturSkrining.teks("SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_entri = 2"); FiksturSkrining.angka("SELECT count(…` | Terhubung menutup entri, mencatat rekam medis, dan diaudit. | `DaftarPantauEntity.STATUS_SELESAI`; `1`; `catatanSebelum + 1`; dan 1 pemeriksaan serupa lainnya | Pass |
| C26-UT21 | `catatTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Normal | `long catatanSebelum = FiksturSkrining.angka("SELECT count(*) FROM catatan_rekam_medis"); manager.catatTindakLanjut(1, new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui")); FiksturSkrining.teks("SELECT status_tindak_lanjut FROM daftar_pa…` | Tidak terhubung menandai perlu dihubungi ulang tanpa rekam medis. | `DaftarPantauEntity.STATUS_ULANG`; `catatanSebelum` | Pass |
| C26-UT22 | `catatTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Batas | `long sebelum = FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 3"); manager.catatTindakLanjut(3, terhubung()); FiksturSkrining.teks("SELECT status_tindak_lanjut FROM daftar_pantau WHERE id_entri = 3"); FiksturSkrining.angka("SELECT …` | Entri yang perlu dihubungi ulang dapat ditutup pada upaya berikutnya. | `DaftarPantauEntity.STATUS_SELESAI`; `sebelum + 1` | Pass |
| C26-UT23 | `catatTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Negatif | `long sebelum = FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut"); IllegalStateException e = assertThrows(IllegalStateException.class, () -> manager.catatTindakLanjut(-1, terhubung())); e.getMessage(); FiksturSkrining.angka("SELECT count(*) FROM tind…` | Entri yang tidak ada ditolak tanpa mengubah data. | `"Entri tidak ditemukan atau sudah selesai ditindaklanjuti"`; `sebelum` | Pass |
| C26-UT24 | `catatTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Negatif | `manager.catatTindakLanjut(2, terhubung()); manager.catatTindakLanjut(2, terhubung()); FiksturSkrining.angka("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 2")` | Entri yang sudah selesai tidak dapat ditindaklanjuti lagi. | melempar `IllegalStateException`; `1` | Pass |
| C26-UT25 | `saringDaftar(jenisRisiko: String, mulai: LocalDate, akhir: LocalDate, statusTindakLanjut: String): List<DaftarPantauEntity>` | Batas | `manager.catatTindakLanjut(2, terhubung()); List<DaftarPantauEntity> hasil = manager.saringDaftar(null, null, null, DaftarPantauEntity.STATUS_SELESAI); ids(hasil); hasil.get(0).getNamaPasien(); manager.saringDaftar(null, null, null, null).stream().anyMatch(e -…` | Entri yang baru ditutup tampil pada penyaring status selesai. | `List.of(2)`; tidak null; salah | Pass |
| C26-UT26 | `catat(dataPantau, dataPasien, idEntri: int, t: TindakLanjutEntity): void` | Negatif | `PantauPalsu pantau = new PantauPalsu(entri(1, "hipertensi", LocalDate.of(2026, 11, 1))); DataPasienBoundary tanpaRekamMedis = new DataPasienBoundary(null) { @Override public RekamMedisEntity ambilRekamMedis(int idPasien) { return null; } }; IllegalStateExcept…` | Pasien tanpa rekam medis ditolak dengan pesan yang jelas. | `"Rekam medis pasien tidak ditemukan: 101"` | Pass |

`saring()` dan `catat()` adalah pecahan *package-private* dari Algo-C26-01 dan penyaringan yang dibuat agar dapat diuji tanpa basis data (Tabel B.1 Laporan Implementasi).

Tabel 4.2.11b. Ringkasan Cakupan Kelas C26

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `saringDaftar()`, `catatTindakLanjut()` |
| Operasi yang diuji | Kedua operasi DPPL (2 dari 2) memiliki kasus normal, batas, dan negatif |
| ID Algoritma yang tercakup | Algo-C26-01 (`catatTindakLanjut()` dan `catat()`: C26-UT01 s.d. C26-UT05, C26-UT14, C26-UT15, C26-UT20 s.d. C26-UT24, C26-UT26) |
| Coverage (wajib ≥ 70%) | 94,5% baris (69 dari 73) dan 92,9% cabang (52 dari 56), diukur dengan JaCoCo 0.8.14 dari seluruh `mvn test` dengan basis data uji. Baris yang tidak tercakup adalah empat baris penanganan `SQLException` pada `saringDaftar()` dan `catatTindakLanjut()`. Empat cabang yang belum tercakup berada pada kondisi gabungan `periksaIsian()`, `saring()`, dan `saringDaftar()`. |

### 4.2.12 Kelas LaporanManager

**ID Kelas:** C27  
**Nama Kelas:** LaporanManager  
**Tingkat:** Mendalam

*[Diisi Fachry: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.13 Kelas SinkronisasiManager

**ID Kelas:** C28  
**Nama Kelas:** SinkronisasiManager  
**Tingkat:** Mendalam

*[Diisi Fachry: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.14 Kelas KonfigurasiManager

**ID Kelas:** C29  
**Nama Kelas:** KonfigurasiManager  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/control/KonfigurasiManagerTest.java`. Test memakai sesi Petugas Administrasi (pengguna 1) kecuali bila disebut lain. Akun `uji.konfigurasi`, poli "Poli Uji...", obat "Obat Uji", catatan pencadangan, arsip pencadangan, dan audit yang dibuat dihapus setelah setiap test, dan ambang hipertensi dikembalikan ke 140. Test pencadangan memakai `pg_dump` dan `pg_restore` asli sesuai `PG_DUMP`, `PG_RESTORE`, dan `LOKASI_MEDIA_CADANGAN` pada `.env`.

Tabel 4.2.14. Hasil Pengujian Unit Kelas C29

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C29-UT01 | `muatDataMaster(): DataMaster` | Normal | Panggil tanpa masukan | Jumlah akun, poli, obat, dan ambang sama dengan isi tabelnya | Keempat jumlah sama dengan `count(*)` tabel `pengguna`, `poli`, `obat`, dan `ambang_risiko` | Pass |
| C29-UT02 | `muatDataMaster(): DataMaster` | Batas | Akun seed `perawat.nonaktif` | Akun nonaktif tetap dimuat agar terlihat admin | Dimuat dengan `aktif()` false | Pass |
| C29-UT03 | `muatDataMaster(): DataMaster` | Negatif | Hak baca tabel `pengguna` untuk `sehati_app` dicabut sementara, lalu dipulihkan | `IllegalStateException` "Basis data tidak dapat dihubungi". Setelah dipulihkan, data kembali termuat | Pengecualian dan pesan sesuai, lalu daftar akun termuat lagi | Pass |
| C29-UT04 | `simpanAkun(p: PenggunaEntity, sandi: String): void` | Normal | Akun baru `uji.konfigurasi`, Perawat, sandi `rahasia123` | ID terisi, status "aktif", hash bcrypt `$2a$12$` yang cocok dengan sandi, audit "Akun baru uji.konfigurasi (Perawat)" | Seluruhnya sesuai | Pass |
| C29-UT05 | `simpanAkun(p: PenggunaEntity, sandi: String): void` | Batas | Sandi tepat 8 karakter `12345678` | Diterima | Satu akun tersimpan | Pass |
| C29-UT06 | `simpanAkun(p: PenggunaEntity, sandi: String): void` | Batas | Akun yang sudah ada disunting menjadi Dokter dengan sandi kosong | Peran berubah, sandi lama tetap berlaku, audit "Perubahan akun uji.konfigurasi" | Peran "Dokter", `rahasia123` masih cocok, audit ada | Pass |
| C29-UT07 | `simpanAkun(p: PenggunaEntity, sandi: String): void` | Negatif | Akun baru dengan nama yang sudah dipakai (UC12 alternatif 2) | `IllegalArgumentException` "Nama pengguna sudah dipakai", tidak ada akun maupun audit kedua | Pengecualian dan pesan sesuai, tetap satu akun dan satu audit | Pass |
| C29-UT08 | `simpanAkun(p: PenggunaEntity, sandi: String): void` | Negatif | Nama " " dan peran "Satpam" (tanpa basis data) | `IllegalArgumentException` "Nama pengguna wajib diisi" dan "Peran tidak dikenal" | Kedua pesan sesuai | Pass |
| C29-UT09 | `simpanAkun(p: PenggunaEntity, sandi: String): void` | Negatif | Sandi 7 karakter, "" dan `null` untuk akun baru, serta "pendek" untuk akun lama (tanpa basis data) | `IllegalArgumentException` "Kata sandi minimal 8 karakter" untuk keempatnya | Keempatnya ditolak | Pass |
| C29-UT10 | `simpanAkun(p: PenggunaEntity, sandi: String): void` | Negatif | Tanpa sesi, lalu sesi berperan Perawat (tanpa basis data) | `IllegalStateException` "Sesi sudah berakhir, silakan masuk kembali" dan "Peran Perawat tidak berwenang menjalankan tindakan ini" | Kedua pesan sesuai | Pass |
| C29-UT11 | `simpanAkun(p: PenggunaEntity, sandi: String): void` | Negatif | Admin uji yang sedang masuk mengubah perannya sendiri menjadi Perawat | `IllegalArgumentException` "Peran akun yang sedang dipakai tidak dapat diubah", peran tetap. Mengganti sandi sendiri dengan peran sama tetap boleh | Pengecualian dan pesan sesuai, peran tetap, ganti sandi berhasil | Pass |
| C29-UT12 | `simpanAkun(p: PenggunaEntity, sandi: String): void` | Negatif | Menyunting akun ID 999999 | `IllegalArgumentException` "Akun tidak ditemukan" | Pengecualian dan pesan sesuai | Pass |
| C29-UT13 | `nonaktifkanAkun(idPengguna: int): void` | Normal | Akun uji dengan satu sesi aktif (UC12 alternatif 3) | Status "nonaktif", sesi aktif ditutup, audit "Akun uji.konfigurasi dinonaktifkan" | Seluruhnya sesuai | Pass |
| C29-UT14 | `nonaktifkanAkun(idPengguna: int): void` | Batas | Akun yang sama dinonaktifkan dua kali | Panggilan kedua ditolak `IllegalStateException` "Akun sudah nonaktif" tanpa audit kedua | Ditolak, tetap satu audit | Pass |
| C29-UT15 | `nonaktifkanAkun(idPengguna: int): void` | Negatif | Admin (pengguna 7) menonaktifkan akunnya sendiri (tanpa basis data) | `IllegalStateException` "Akun yang sedang dipakai tidak dapat dinonaktifkan" | Pengecualian dan pesan sesuai | Pass |
| C29-UT16 | `nonaktifkanAkun(idPengguna: int): void` | Negatif | ID 999999 | `IllegalArgumentException` "Akun tidak ditemukan" | Pengecualian dan pesan sesuai | Pass |
| C29-UT17 | `simpanDataMaster(a: AmbangRisikoEntity): void` | Normal | Ambang hipertensi diubah menjadi 150 | Nilai tersimpan dan audit "Ambang hipertensi diubah menjadi 150.0" | Nilai 150 dan audit ada | Pass |
| C29-UT18 | `simpanDataMaster(a: AmbangRisikoEntity): void` | Batas | Nilai 0,1 (positif terkecil yang diuji) | Diterima | Tersimpan 0,1 | Pass |
| C29-UT19 | `simpanDataMaster(a: AmbangRisikoEntity): void` | Negatif | Nilai 0, -5, dan NaN (tanpa basis data) | `IllegalArgumentException` "Nilai ambang harus lebih dari nol" | Ketiganya ditolak | Pass |
| C29-UT20 | `simpanPoli(idPoli: int, namaPoli: String): void` | Normal | Poli baru "  Poli Uji  " | Tersimpan sebagai "Poli Uji" dengan audit "Poli baru Poli Uji" | Sesuai | Pass |
| C29-UT21 | `simpanPoli(idPoli: int, namaPoli: String): void` | Batas | Poli uji diberi `urutan_terakhir` 7, lalu diganti nama menjadi "Poli Uji KIA" | Nomor antrean terakhir tidak tertimpa, audit penggantian nama | `urutan_terakhir` tetap 7, audit ada | Pass |
| C29-UT22 | `simpanPoli(idPoli: int, namaPoli: String): void` | Negatif | Nama "  " dan `null` (tanpa basis data) | `IllegalArgumentException` "Nama poli wajib diisi" | Keduanya ditolak | Pass |
| C29-UT23 | `simpanPoli(idPoli: int, namaPoli: String): void` | Negatif | Nama yang sama dua kali, lalu poli ID 999999 | Nama ganda ditolak constraint UNIQUE, poli tidak ada ditolak "Poli tidak ditemukan". Hanya satu poli dan satu audit | Keduanya ditolak, satu poli, satu audit | Pass |
| C29-UT24 | `tambahObat(o: ObatEntity): void` | Normal | "Obat Uji", "tablet 5 mg", stok 99, minimum 20 | Tersimpan dengan stok 0 (stok hanya lewat UC08) dan minimum 20, audit "Obat baru Obat Uji (tablet 5 mg)" | Stok 0, minimum 20, audit ada | Pass |
| C29-UT25 | `tambahObat(o: ObatEntity): void` | Batas | Minimum 0, nama dan sediaan berspasi tepi | Diterima, spasi tepi dibuang | Tersimpan "Obat Uji", "sirup 60 ml", minimum 0 | Pass |
| C29-UT26 | `tambahObat(o: ObatEntity): void` | Negatif | Sediaan " ", nama `null`, dan minimum -1 (tanpa basis data) | `IllegalArgumentException` "Nama obat dan sediaan wajib diisi" dan "Stok minimum tidak boleh negatif" | Ketiganya ditolak dengan pesan sesuai | Pass |
| C29-UT27 | `tambahObat(o: ObatEntity): void` | Negatif | "obat uji" "Tablet 5 mg" setelah "Obat Uji" "tablet 5 mg" ada | Ditolak tanpa membedakan huruf: "Obat obat uji Tablet 5 mg sudah terdaftar" | Ditolak dengan pesan sesuai, tetap satu obat | Pass |
| C29-UT28 | `muatStatusPencadangan(): PencadanganEntity` | Normal | Catatan "berhasil" terbaru | Catatan itu dikembalikan | ID sama, `berhasil()` true | Pass |
| C29-UT29 | `muatStatusPencadangan(): PencadanganEntity` | Batas | Catatan "berhasil" lalu "gagal" yang lebih baru | Catatan gagal dikembalikan agar peringatan tampil (UC12 alternatif 4) | ID catatan gagal, `berhasil()` false | Pass |
| C29-UT30 | `muatStatusPencadangan(): PencadanganEntity` | Negatif | Tabel `pencadangan` dikosongkan sementara, lalu dipulihkan | `null` | `null` | Pass |
| C29-UT31 | `jalankanPencadangan(): PencadanganEntity` | Normal | `pg_dump` asli ke `LOKASI_MEDIA_CADANGAN` | Status "berhasil", arsip ada dan berukuran lebih dari 0, catatan tersimpan | Sesuai | Pass |
| C29-UT32 | `jalankanPencadangan(): PencadanganEntity` | Batas | Koneksi lain sedang memegang transaksi terbuka yang mengubah ambang hipertensi | Pencadangan tidak menunggu kunci baris dan tetap berhasil | "berhasil" dalam kurang dari 1 detik | Pass |
| C29-UT33 | `jalankanPencadangan(): PencadanganEntity` | Negatif | Hak baca tabel `pasien` untuk `sehati_app` dicabut sementara sehingga `pg_dump` gagal | Status "gagal", arsip setengah jadi dihapus, catatan "gagal" tetap disimpan | Sesuai | Pass |
| C29-UT34 | `jedaSampai(jam: int, sekarang: LocalDateTime): long` | Normal | Jam 21, sekarang 20.30 | 30 menit | 30 | Pass |
| C29-UT35 | `jedaSampai(jam: int, sekarang: LocalDateTime): long` | Batas | Jam 20 tepat pukul 20.00, jam 20 pukul 20.30, dan jam 0 pukul 20.30 | Jam yang sudah tiba atau lewat dijadwalkan besok: 1440, 1410, dan 210 menit | 1440, 1410, 210 | Pass |
| C29-UT36 | `jedaSampai(jam: int, sekarang: LocalDateTime): long` | Negatif | Jam 24 dan -1 | `DateTimeException` | Keduanya ditolak | Pass |
| C29-UT37 | `jadwalkanPencadangan(): void` | Negatif | `PENCADANGAN_JAM` kosong pada `.env` | Tidak ada penjadwal (thread "pencadangan") yang dibuat | Tidak ada thread "pencadangan" | Pass |

`simpanPoli()`, `tambahObat()`, dan `jadwalkanPencadangan()` adalah operasi public tambahan implementasi (Tabel B.1 nomor 27 dan 28). `jedaSampai()` bervisibilitas package-private dan diuji langsung karena menentukan waktu pencadangan terjadwal. Penjadwal yang benar-benar berjalan diuji lewat UC12-TC05.

Tabel 4.2.14b. Ringkasan Cakupan Kelas C29

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `muatDataMaster()`, `simpanAkun()`, `nonaktifkanAkun()`, `simpanDataMaster()`, `muatStatusPencadangan()`, `jalankanPencadangan()` |
| Operasi yang diuji | Keenam operasi public DPPL (6 dari 6) dengan kasus normal, batas, dan negatif, ditambah `simpanPoli()`, `tambahObat()`, `jedaSampai()`, dan `jadwalkanPencadangan()` |
| ID Algoritma yang tercakup | Algo-C29-01 (penyimpanan akun: C29-UT04 s.d. C29-UT12), Algo-C29-02 (penonaktifan akun: C29-UT13 s.d. C29-UT16), Algo-C29-03 (pencadangan: C29-UT28 s.d. C29-UT37) |
| Coverage (wajib ≥ 70%) | 81,1% baris (129 dari 159) dan 87,5% cabang (63 dari 72), JaCoCo 0.8.14 dari seluruh `mvn test`. Baris yang tidak tercakup adalah isi penjadwal harian (`jadwalkanPencadangan()` saat `PENCADANGAN_JAM` diisi, diuji lewat UC12-TC05) dan penanganan `SQLException` saat basis data tidak dapat dihubungi. Bukti pada Subbab 4.6 |

### 4.2.15 Kelas PenggunaEntity

**ID Kelas:** C30  
**Nama Kelas:** PenggunaEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/PenggunaEntityTest.java`, tanpa basis data. Hash uji dibuat dari sandi `rahasia123` dengan faktor biaya 4 agar test cepat (`checkpw()` membaca faktor dari hash).

Tabel 4.2.15. Hasil Pengujian Unit Kelas C30

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C30-UT01 | `cocokkanSandi(sandi: String): boolean` | Normal | `rahasia123` | `true` | `true` | Pass |
| C30-UT02 | `cocokkanSandi(sandi: String): boolean` | Batas | `Rahasia123` (satu huruf kapital), `rahasia12` (kurang satu), `rahasia1234` (lebih satu) | `false` untuk ketiganya | `false` | Pass |
| C30-UT03 | `cocokkanSandi(sandi: String): boolean` | Negatif | "salah", "", dan `null` | `false` | `false` | Pass |
| C30-UT04 | `cocokkanSandi(sandi: String): boolean` | Negatif | Hash kosong dan `null` | `false` | `false` | Pass |
| C30-UT05 | `cocokkanSandi(sandi: String): boolean` | Negatif | Hash rusak: "x", `$2a$xx$bukanhash`, dan awalan `$9a$` | `false` tanpa pengecualian | `false` | Pass |
| C30-UT06 | `aktif(): boolean` | Normal | Status "aktif" | `true` | `true` | Pass |
| C30-UT07 | `aktif(): boolean` | Batas | Status "Aktif" (huruf kapital) dan `null` | `false` | `false` | Pass |
| C30-UT08 | `aktif(): boolean` | Negatif | Status "nonaktif" | `false` | `false` | Pass |
| C30-UT09 | `nonaktifkan(): void` | Normal | Akun aktif | Status menjadi "nonaktif" | "nonaktif" | Pass |
| C30-UT10 | `nonaktifkan(): void` | Batas | Akun baru dari konstruktor `PenggunaEntity(nama, hash, peran)` (ID 0, status "aktif") | Dapat dinonaktifkan | ID 0, aktif, lalu "nonaktif" | Pass |
| C30-UT11 | `nonaktifkan(): void` | Negatif | Akun yang sudah nonaktif | `IllegalStateException` "Akun sudah nonaktif", status tetap | Pengecualian dan pesan sesuai | Pass |

Tabel 4.2.15b. Ringkasan Cakupan Kelas C30

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `cocokkanSandi()`, `aktif()`, `nonaktifkan()` |
| Operasi yang diuji | Ketiga operasi (3 dari 3) dengan kasus normal, batas, dan negatif |
| ID Algoritma yang tercakup | Algo-C30-01 (C30-UT01 s.d. C30-UT11) |
| Coverage (wajib ≥ 70%) | 83,3% baris (30 dari 36) dan 100% cabang (10 dari 10), JaCoCo 0.8.14 dari seluruh `mvn test`. Baris yang tidak tercakup adalah konstruktor kosong dan *setter* yang tidak dipakai aplikasi. Bukti pada Subbab 4.6 |

### 4.2.16 Kelas SesiEntity

**ID Kelas:** C31  
**Nama Kelas:** SesiEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/SesiEntityTest.java`, tanpa basis data. Waktu mulai sesi uji diatur mundur dari waktu saat test berjalan.

Tabel 4.2.16. Hasil Pengujian Unit Kelas C31

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C31-UT01 | `habisWaktu(menit: int): boolean` | Normal | Sesi mulai 30 menit lalu dan baru mulai, batas 15 | `true` dan `false` | `true` dan `false` | Pass |
| C31-UT02 | `habisWaktu(menit: int): boolean` | Batas | Mulai tepat 15 menit lalu, 14 menit 59 detik lalu (batas 15), dan 1 menit lalu (batas 1) | `true`, `false`, `true` | `true`, `false`, `true` | Pass |
| C31-UT03 | `habisWaktu(menit: int): boolean` | Negatif | Batas 0 dan -1 | `IllegalArgumentException` "Batas waktu harus lebih dari nol" | Keduanya ditolak dengan pesan sesuai | Pass |
| C31-UT04 | `tutup(): void` | Normal | Sesi aktif | `statusAktif` false | false | Pass |
| C31-UT05 | `tutup(): void` | Batas | `tutup()` dua kali | Tetap false tanpa pengecualian | false | Pass |
| C31-UT06 | `tutup(): void` | Negatif | Sesi kosong dari konstruktor tanpa argumen | Tidak ada pengecualian, status false | Sesuai | Pass |
| C31-UT07 | Atribut (*setter* dan *getter*) | Normal | Sesi kosong diisi lewat *setter*, seperti saat dibaca dari basis data | Seluruh nilai terbaca kembali | Sesuai | Pass |

C31-UT07 ditambahkan karena pada pengukuran 18 November 2026 *coverage* baris C31 baru 60,0%: *setter* dan konstruktor kosong belum tersentuh test.

Tabel 4.2.16b. Ringkasan Cakupan Kelas C31

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `habisWaktu()`, `tutup()` |
| Operasi yang diuji | Kedua operasi (2 dari 2) dengan kasus normal, batas, dan negatif |
| ID Algoritma yang tercakup | Algo-C31-01 (C31-UT01 s.d. C31-UT03; pemanggilannya oleh pewaktu diam diuji lewat KNF04-TC01) |
| Coverage (wajib ≥ 70%) | 100% baris (25 dari 25) dan 100% cabang (4 dari 4), JaCoCo 0.8.14 dari seluruh `mvn test`. Bukti pada Subbab 4.6 |

### 4.2.17 Kelas AuditEntity

**ID Kelas:** C32  
**Nama Kelas:** AuditEntity  
**Tingkat:** Sedang

Berkas test: `src/tests/sehati/business/entity/AuditEntityTest.java`, tanpa basis data. Penyimpanan audit ke basis data diuji pada C56-UT07 dan C56-UT08.

Tabel 4.2.17. Hasil Pengujian Unit Kelas C32

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C32-UT01 | `buat(idPengguna: int, deskripsi: String)$: AuditEntity` | Normal | Pengguna 7, "Masuk ke aplikasi" | `idAudit` 0, pelaku 7, deskripsi sama, waktu di antara sebelum dan sesudah pemanggilan | Seluruhnya sesuai | Pass |
| C32-UT02 | `buat(idPengguna: int, deskripsi: String)$: AuditEntity` | Batas | Pelaku 0 (nama tidak dikenal, Algo-C17-01) dan deskripsi satu karakter | Diterima | Catatan terbentuk | Pass |
| C32-UT03 | `buat(idPengguna: int, deskripsi: String)$: AuditEntity` | Negatif | Deskripsi `null`, "", dan "  " | `IllegalArgumentException` "Deskripsi audit wajib diisi" | Ketiganya ditolak dengan pesan sesuai | Pass |

### 4.2.18 Kelas PasienEntity

**ID Kelas:** C33  
**Nama Kelas:** PasienEntity  
**Tingkat:** Sedang

*[Diisi Steven: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

### 4.2.19 Kelas RekamMedisEntity

**ID Kelas:** C34  
**Nama Kelas:** RekamMedisEntity  
**Tingkat:** Sedang

*[Diisi Steven: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

### 4.2.20 Kelas KunjunganEntity

**ID Kelas:** C35  
**Nama Kelas:** KunjunganEntity  
**Tingkat:** Mendalam

*[Diisi Steven: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.21 Kelas PoliEntity

**ID Kelas:** C36  
**Nama Kelas:** PoliEntity  
**Tingkat:** Mendalam

*[Diisi Steven: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.22 Kelas AntreanEntity

**ID Kelas:** C37  
**Nama Kelas:** AntreanEntity  
**Tingkat:** Mendalam

*[Diisi Steven: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.23 Kelas SkriningEntity

**ID Kelas:** C38  
**Nama Kelas:** SkriningEntity  
**Tingkat:** Sedang

Berkas test: `src/tests/sehati/business/entity/SkriningEntityTest.java`.

Tabel 4.2.23. Hasil Pengujian Unit Kelas C38

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C38-UT01 | `lampirkanTandaVital(tv: TandaVitalEntity): void` | Normal | `SkriningEntity s = new SkriningEntity(0, 1, "Pusing", LocalDateTime.now()); TandaVitalEntity tv = new TandaVitalEntity(120, 80, 60, 160, 36.5, 72, null); s.lampirkanTandaVital(tv); s.getTandaVital()` | Tanda vital disimpan sebagai referensi. | objek yang sama dengan `tv` | Pass |
| C38-UT02 | `lampirkanTandaVital(tv: TandaVitalEntity): void` | Negatif | `SkriningEntity s = new SkriningEntity(0, 1, "Pusing", LocalDateTime.now()); TandaVitalEntity tv = new TandaVitalEntity(120, 80, 60, 160, 36.5, 72, null); s.lampirkanTandaVital(tv); IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -…` | Tanda vital kosong ditolak dan referensi lama tetap. | `"Tanda vital tidak boleh kosong"`; objek yang sama dengan `tv` | Pass |
| C38-UT03 | `SkriningEntity(idSkrining, idKunjungan, keluhanAwal, waktu)` | Batas | `LocalDateTime waktu = LocalDateTime.of(2026, 11, 4, 9, 30); SkriningEntity s = new SkriningEntity(7, 3, "", waktu); s.getTandaVital(); s.getIdSkrining(); s.getIdKunjungan()` | Skrining baru belum memiliki tanda vital. | null; `7`; `3`; dan 1 pemeriksaan serupa lainnya | Pass |
| C38-UT04 | `getter dan setter seluruh atribut` | Normal | `SkriningEntity s = new SkriningEntity(); LocalDateTime waktu = LocalDateTime.of(2026, 11, 5, 8, 15); s.setIdSkrining(4); s.setIdKunjungan(2); s.setKeluhanAwal("Demam"); s.setWaktu(waktu); s.getIdSkrining(); s.getIdKunjungan(); s.getKeluhanAwal()` | Seluruh atribut dapat diubah lewat konstruktor kosong. | `4`; `2`; `"Demam"`; dan 1 pemeriksaan serupa lainnya | Pass |

Coverage kelas ini 100,0% baris (25 dari 25) dan 100,0% cabang (2 dari 2). Kelas Sedang tidak memiliki target coverage.

### 4.2.24 Kelas TandaVitalEntity

**ID Kelas:** C39  
**Nama Kelas:** TandaVitalEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/TandaVitalEntityTest.java`.

Tabel 4.2.24. Hasil Pengujian Unit Kelas C39

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C39-UT01 | `hitungIMT(): double` | Normal | `tandaVital(72.25, 170).hitungIMT()` | 72,25 kg pada 170 cm menghasilkan IMT 25,0. | `25.0` | Pass |
| C39-UT02 | `hitungIMT(): double` | Batas | `pembulatan ke satu desimal menjadi 29,8 assertEquals(84 / (1.68 * 1.68), tandaVital(84, 168).hitungIMT(), 1e-12)` | Hasil tidak dibulatkan sehingga perbandingan ambang tidak bergeser. | Tidak ada pengecualian | Pass |
| C39-UT03 | `hitungIMT(): double` | Batas | `tandaVital(10, 30).hitungIMT()` | Tinggi di batas bawah rentang 30 cm tetap dihitung. | `10 / (0.3 * 0.3)` | Pass |
| C39-UT04 | `hitungIMT(): double` | Batas | `tandaVital(1, 1).hitungIMT()` | Tinggi positif sangat kecil tidak ditolak. | `1 / (0.01 * 0.01)` | Pass |
| C39-UT05 | `hitungIMT(): double` | Negatif | `IllegalStateException e = assertThrows(IllegalStateException.class, () -> tandaVital(70, 0).hitungIMT()); e.getMessage()` | Tinggi 0 ditolak. | `"Tinggi badan belum terisi"` | Pass |
| C39-UT06 | `hitungIMT(): double` | Negatif | `tandaVital(70, -170).hitungIMT()` | Tinggi bernilai negatif ditolak. | melempar `IllegalStateException` | Pass |
| C39-UT07 | `atribut waktu: LocalDateTime` | Normal | `tandaVital(70, 170).getWaktu(); tv.getWaktu(); tv.getSistolik()` | Pengukuran baru belum berisi waktu dan hasil bacaan memuatnya. | null; `waktu`; `120` | Pass |
| C39-UT08 | `atribut gulaDarah: Integer` | Batas | `tandaVital(70, 170).getGulaDarah(); new TandaVitalEntity(120, 80, 70, 170, 36.5, 72, 132).getGulaDarah()` | Pengukuran tanpa gula darah menyimpan null. | null; `132` | Pass |

Tabel 4.2.24b. Ringkasan Cakupan Kelas C39

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `hitungIMT()` |
| Operasi yang diuji | Satu operasi DPPL (1 dari 1) memiliki kasus normal, batas, dan negatif, ditambah atribut `waktu` dan `gulaDarah` yang ditambahkan pada implementasi |
| ID Algoritma yang tercakup | Algo-C39-01 (`hitungIMT()`: C39-UT01 s.d. C39-UT06) |
| Coverage (wajib ≥ 70%) | 85,7% baris (36 dari 42) dan 100,0% cabang (2 dari 2), diukur dengan JaCoCo 0.8.14 dari seluruh `mvn test` dengan basis data uji. Baris yang tidak tercakup adalah konstruktor kosong dan dua pengubah atribut (`setBeratBadan()` dan `setTinggiBadan()`) yang tidak dipakai test kelas ini. |

### 4.2.25 Kelas AmbangRisikoEntity

**ID Kelas:** C40  
**Nama Kelas:** AmbangRisikoEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/AmbangPenandaEntityTest.java` (bersama C41).

Tabel 4.2.25. Hasil Pengujian Unit Kelas C40

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C40-UT01 | `dilampaui(nilai: double): boolean` | Normal | `new AmbangRisikoEntity(1, "hipertensi", 140).dilampaui(152)` | Nilai di atas batas dianggap melampaui. | benar | Pass |
| C40-UT02 | `dilampaui(nilai: double): boolean` | Batas | `AmbangRisikoEntity ambang = new AmbangRisikoEntity(1, "hipertensi", 140); ambang.dilampaui(140); ambang.dilampaui(141)` | Nilai tepat sama dengan batas dianggap melampaui. | benar; benar | Pass |
| C40-UT03 | `dilampaui(nilai: double): boolean` | Batas | `AmbangRisikoEntity ambang = new AmbangRisikoEntity(1, "hipertensi", 140); ambang.dilampaui(139.9); ambang.dilampaui(139.999999)` | Nilai sedikit di bawah batas tidak melampaui. | salah; salah | Pass |
| C40-UT04 | `dilampaui(nilai: double): boolean` | Negatif | `AmbangRisikoEntity ambang = new AmbangRisikoEntity(2, "obesitas", 30); ambang.dilampaui(0); ambang.dilampaui(-5)` | Nilai 0 dan nilai negatif tidak melampaui. | salah; salah | Pass |
| C40-UT05 | `dilampaui(nilai: double): boolean` | Batas | `AmbangRisikoEntity ambang = new AmbangRisikoEntity(2, "obesitas", 30); ambang.dilampaui(30.0); ambang.dilampaui(29.9); ambang.getJenisRisiko()` | IMT 30,0 tepat melampaui ambang obesitas dan 29,9 tidak. | benar; salah; `"obesitas"`; dan 1 pemeriksaan serupa lainnya | Pass |
| C40-UT06 | `getter dan setter seluruh atribut` | Normal | `AmbangRisikoEntity ambang = new AmbangRisikoEntity(); ambang.setIdAmbang(3); ambang.setJenisRisiko("indikasi diabetes"); ambang.setNilaiBatas(200); ambang.getIdAmbang(); ambang.getJenisRisiko(); ambang.getNilaiBatas()` | Seluruh atribut ambang dapat diubah lewat konstruktor kosong. | `3`; `"indikasi diabetes"`; `200`; dan 1 pemeriksaan serupa lainnya | Pass |

Tabel 4.2.25b. Ringkasan Cakupan Kelas C40

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `dilampaui()` |
| Operasi yang diuji | Satu operasi DPPL (1 dari 1) memiliki kasus normal, batas, dan negatif, ditambah pembaca dan pengubah atribut |
| ID Algoritma yang tercakup | Algo-C40-01 (`dilampaui()`: C40-UT01 s.d. C40-UT05) |
| Coverage (wajib ≥ 70%) | 100,0% baris (17 dari 17) dan 100,0% cabang (2 dari 2), diukur dengan JaCoCo 0.8.14 dari seluruh `mvn test` dengan basis data uji. Seluruh baris dan cabang tercakup. |

### 4.2.26 Kelas PenandaRisikoEntity

**ID Kelas:** C41  
**Nama Kelas:** PenandaRisikoEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/AmbangPenandaEntityTest.java` (bersama C40).

Tabel 4.2.26. Hasil Pengujian Unit Kelas C41

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C41-UT01 | `cabut(): void` | Normal | `PenandaRisikoEntity p = penanda(); p.cabut(); p.getStatus(); p.getJenisRisiko()` | Penanda aktif menjadi dicabut dan riwayatnya tetap. | `PenandaRisikoEntity.STATUS_DICABUT`; `"hipertensi"` | Pass |
| C41-UT02 | `cabut(): void` | Negatif | `PenandaRisikoEntity p = penanda(); p.cabut(); IllegalStateException e = assertThrows(IllegalStateException.class, p::cabut); e.getMessage(); p.getStatus()` | Penanda yang sudah dicabut tidak dapat dicabut lagi. | `"Penanda risiko sudah dicabut"`; `PenandaRisikoEntity.STATUS_DICABUT` | Pass |
| C41-UT03 | `cabut(): void` | Batas | `PenandaRisikoEntity p = penanda(); p.getStatus(); p.getStatus()` | Penanda baru berstatus aktif sehingga pencabutan pertama diterima. | `"aktif"`; `"dicabut"` | Pass |
| C41-UT04 | `cabut(): void` | Batas | `PenandaRisikoEntity a = penanda(); PenandaRisikoEntity b = new PenandaRisikoEntity(2, 1, "obesitas", LocalDate.now(), PenandaRisikoEntity.STATUS_AKTIF); a.cabut(); b.getStatus()` | Pencabutan satu penanda tidak memengaruhi penanda lain. | `PenandaRisikoEntity.STATUS_AKTIF` | Pass |
| C41-UT05 | `getter dan setter seluruh atribut` | Normal | `PenandaRisikoEntity p = new PenandaRisikoEntity(); LocalDate tanggal = LocalDate.of(2026, 11, 4); p.setIdPenanda(5); p.setIdKunjungan(9); p.setJenisRisiko("obesitas"); p.setTanggal(tanggal); p.setStatus(PenandaRisikoEntity.STATUS_AKTIF); p.getIdPenanda(); p.g…` | Seluruh atribut penanda dapat diubah lewat konstruktor kosong. | `5`; `9`; `"obesitas"`; dan 2 pemeriksaan serupa lainnya | Pass |

Tabel 4.2.26b. Ringkasan Cakupan Kelas C41

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `cabut()` |
| Operasi yang diuji | Satu operasi DPPL (1 dari 1) memiliki kasus normal, batas, dan negatif, ditambah pembaca dan pengubah atribut |
| ID Algoritma yang tercakup | Aturan status Subbab 5.41 (`cabut()`: C41-UT01 s.d. C41-UT04) |
| Coverage (wajib ≥ 70%) | 100,0% baris (28 dari 28) dan 100,0% cabang (2 dari 2), diukur dengan JaCoCo 0.8.14 dari seluruh `mvn test` dengan basis data uji. Seluruh baris dan cabang tercakup. |

### 4.2.27 Kelas PemeriksaanEntity

**ID Kelas:** C42  
**Nama Kelas:** PemeriksaanEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/EntityPemeriksaanTest.java` (tanpa basis data).

Tabel 4.2.27. Hasil Pengujian Unit Kelas C42

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C42-UT01 | `kunci(): void` | Normal | Draf pemeriksaan dengan `terkunci = false` | `terkunci` menjadi `true` | `isTerkunci()` bernilai `true` | Pass |
| C42-UT02 | `kunci(): void` | Batas | Draf beranamnesis "Pusing" dan bertindakan "Edukasi" dikunci | Hanya status kunci yang berubah, isi pemeriksaan tetap | Anamnesis dan tindakan tidak berubah | Pass |
| C42-UT03 | `kunci(): void` | Negatif | Pemeriksaan yang sudah `terkunci = true` | `IllegalStateException` dan tetap terkunci | Pengecualian dilempar, tetap terkunci | Pass |

Tabel 4.2.27b. Ringkasan Cakupan Kelas C42

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `kunci()` |
| Operasi yang diuji | `kunci()` (1 dari 1) |
| ID Algoritma yang tercakup | - (DPPL Subbab 5.42 tidak memuat algoritma terpisah; aturan kunci ada pada Tabel 5.42.1) |
| Coverage (wajib ≥ 70%) | 92,9% baris (26 dari 28) dan 100% cabang (2 dari 2), JaCoCo 0.8.14 dari seluruh `mvn test`. Bukti pada Subbab 4.6 |

### 4.2.28 Kelas KodeICD10Entity

**ID Kelas:** C44  
**Nama Kelas:** KodeICD10Entity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/EntityPemeriksaanTest.java` (tanpa basis data). Objek uji adalah kode I10 "Hipertensi esensial (primer)".

Tabel 4.2.28. Hasil Pengujian Unit Kelas C44

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C44-UT01 | `cocok(kataKunci: String): boolean` | Normal | "i1" (awalan kode, huruf kecil) dan "ESENSIAL" (isi deskripsi, huruf besar) | `true` untuk keduanya | `true` untuk keduanya | Pass |
| C44-UT02 | `cocok(kataKunci: String): boolean` | Batas | "I10" (kode lengkap) dan " hipertensi " (berspasi tepi) | `true` untuk keduanya | `true` untuk keduanya | Pass |
| C44-UT03 | `cocok(kataKunci: String): boolean` | Negatif | "10" (potongan kode yang bukan awalan) dan "diabetes" | `false` untuk keduanya | `false` untuk keduanya | Pass |
| C44-UT04 | `cocok(kataKunci: String): boolean` | Negatif | "  " dan `null` | `false` untuk keduanya, tanpa pengecualian | `false` untuk keduanya | Pass |
| C44-UT05 | `cocok(kataKunci: String): boolean` | Batas | Objek dari konstruktor kosong yang diisi lewat setter (E11, "Diabetes melitus tipe 2"), lalu `cocok("e1")` | `true`, dan getter mengembalikan nilai yang diisi | `true`, ketiga getter sesuai | Pass |

Tabel 4.2.28b. Ringkasan Cakupan Kelas C44

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `cocok()` |
| Operasi yang diuji | `cocok()` (1 dari 1) |
| ID Algoritma yang tercakup | Algo-C44-01 (pencocokan kata kunci) |
| Coverage (wajib ≥ 70%) | 100% baris (20 dari 20) dan 100% cabang (8 dari 8), JaCoCo 0.8.14 dari seluruh `mvn test`. Bukti pada Subbab 4.6 |

### 4.2.29 Kelas JadwalKontrolEntity

**ID Kelas:** C45  
**Nama Kelas:** JadwalKontrolEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/EntityPemeriksaanTest.java` (tanpa basis data).

Tabel 4.2.29. Hasil Pengujian Unit Kelas C45

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C45-UT01 | `tandaiSelesai(): void` | Normal | Jadwal "terjadwal" tujuh hari lagi | Status menjadi "selesai" | "selesai" | Pass |
| C45-UT02 | `tandaiSelesai(): void` | Batas | Jadwal "terjadwal" yang tanggalnya sudah lewat kemarin | Tetap dapat diselesaikan, karena pasien boleh datang setelah tanggal rencana | "selesai" | Pass |
| C45-UT03 | `tandaiSelesai(): void` | Negatif | Jadwal yang sudah "selesai" dan jadwal tanpa status | `IllegalStateException` untuk keduanya | Keduanya ditolak | Pass |

Tabel 4.2.29b. Ringkasan Cakupan Kelas C45

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `tandaiSelesai()` |
| Operasi yang diuji | `tandaiSelesai()` (1 dari 1). Pemanggilnya pada alur nyata diuji lewat C22-UT23 |
| ID Algoritma yang tercakup | - (aturan status pada Tabel 5.45.1 DPPL) |
| Coverage (wajib ≥ 70%) | 83,3% baris (20 dari 24) dan 100% cabang (2 dari 2), JaCoCo 0.8.14 dari seluruh `mvn test`. Bukti pada Subbab 4.6 |

### 4.2.30 Kelas ResepEntity

**ID Kelas:** C46  
**Nama Kelas:** ResepEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/EntityResepTest.java` (tanpa basis data).

Tabel 4.2.30. Hasil Pengujian Unit Kelas C46

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C46-UT01 | `perbaruiStatus(status: String): void` | Normal | Resep "menunggu" → "selesai dilayani" | Status berubah | "selesai dilayani" | Pass |
| C46-UT02 | `perbaruiStatus(status: String): void` | Batas | Resep "menunggu" → "dikembalikan", status akhir kedua yang sah | Status berubah | "dikembalikan" | Pass |
| C46-UT03 | `perbaruiStatus(status: String): void` | Negatif | Resep "menunggu" → "menunggu" dan → "dibatalkan" | `IllegalArgumentException` untuk keduanya, status tetap "menunggu" | Keduanya ditolak, status tetap | Pass |
| C46-UT04 | `perbaruiStatus(status: String): void` | Negatif | Resep "selesai dilayani" → "dikembalikan" | `IllegalStateException`, status tetap "selesai dilayani" | Ditolak, status tetap | Pass |

Tabel 4.2.30b. Ringkasan Cakupan Kelas C46

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `perbaruiStatus()` |
| Operasi yang diuji | `perbaruiStatus()` (1 dari 1) |
| ID Algoritma yang tercakup | Algo-C46-01 (perubahan status resep) |
| Coverage (wajib ≥ 70%) | 82,4% baris (28 dari 34) dan 100% cabang (6 dari 6), JaCoCo 0.8.14 dari seluruh `mvn test`. Bukti pada Subbab 4.6 |

### 4.2.31 Kelas ItemResepEntity

**ID Kelas:** C47  
**Nama Kelas:** ItemResepEntity  
**Tingkat:** Sedang

Berkas test: `src/tests/sehati/business/entity/EntityResepTest.java` (tanpa basis data). Kelas bertingkat Sedang, tetapi kasus batas tetap diuji karena aturan "stok sama dengan jumlah dianggap cukup" menentukan indikator stok UC06.

Tabel 4.2.31. Hasil Pengujian Unit Kelas C47

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C47-UT01 | `cukupStok(stok: int): boolean` | Normal | Item berjumlah 10, stok 11 | `true` | `true` | Pass |
| C47-UT02 | `cukupStok(stok: int): boolean` | Batas | Item berjumlah 10, stok 10 | `true` | `true` | Pass |
| C47-UT03 | `cukupStok(stok: int): boolean` | Negatif | Item berjumlah 10, stok 9 dan stok 0 | `false` untuk keduanya | `false` untuk keduanya | Pass |

### 4.2.32 Kelas ObatEntity

**ID Kelas:** C48  
**Nama Kelas:** ObatEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/EntityFarmasiTest.java`, kelompok `C48 ObatEntity` (tanpa basis data). Obat uji adalah "Kaptopril 25 mg" dengan stok dan ambang sesuai kolom masukan.

Tabel 4.2.32. Hasil Pengujian Unit Kelas C48

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C48-UT01 | `kurangiStok(jumlah: int): void` | Normal | Stok 20, dikurangi 5 | Stok 15 | 15 | Pass |
| C48-UT02 | `kurangiStok(jumlah: int): void` | Batas | Stok 20, dikurangi 20 | Stok habis menjadi 0 | 0 | Pass |
| C48-UT03 | `kurangiStok(jumlah: int): void` | Negatif | Stok 5, dikurangi 6 | `IllegalStateException` "Stok Kaptopril 25 mg tidak cukup", stok tetap 5 | Pengecualian dan pesan sesuai, stok 5 | Pass |
| C48-UT04 | `kurangiStok(jumlah: int): void` | Negatif | Dikurangi 0 dan -1 | `IllegalArgumentException` "Jumlah pengurangan harus lebih dari nol", stok tetap | Keduanya ditolak, stok tetap 5 | Pass |
| C48-UT05 | `tambahStok(jumlah: int): void` | Normal | Stok 20, ditambah 300 | Stok 320 | 320 | Pass |
| C48-UT06 | `tambahStok(jumlah: int): void` | Batas | Stok 0, ditambah jumlah terkecil 1 | Stok 1 | 1 | Pass |
| C48-UT07 | `tambahStok(jumlah: int): void` | Negatif | Ditambah 0 dan -1 | `IllegalArgumentException` "Jumlah penambahan harus lebih dari nol", stok tetap | Keduanya ditolak, stok tetap 20 | Pass |
| C48-UT08 | `diBawahAmbang(): boolean` | Normal | Stok 20, ambang 50 | `true` | `true` | Pass |
| C48-UT09 | `diBawahAmbang(): boolean` | Batas | Stok 49 dan stok 50, ambang 50 | `true` untuk 49, `false` untuk 50 (sama dengan klausa `stok < ambang_minimum` pada Q-C61-08) | `true` dan `false` | Pass |
| C48-UT10 | `diBawahAmbang(): boolean` | Negatif | Stok 300 ambang 100, serta stok 0 ambang 0 | `false` untuk keduanya | `false` untuk keduanya | Pass |

Tabel 4.2.32b. Ringkasan Cakupan Kelas C48

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `kurangiStok()`, `tambahStok()`, `diBawahAmbang()` |
| Operasi yang diuji | Ketiga operasi (3 dari 3), masing-masing dengan kasus normal, batas, dan negatif |
| ID Algoritma yang tercakup | Algo-C48-01 (perubahan stok: C48-UT01 s.d. C48-UT07) |
| Coverage (wajib ≥ 70%) | 88,6% baris (31 dari 35) dan 100% cabang (8 dari 8), JaCoCo 0.8.14 dari seluruh `mvn test`. Baris yang tidak tercakup adalah konstruktor kosong dan `setAmbangMinimum()` yang tidak dipanggil aplikasi. Bukti pada Subbab 4.6 |

### 4.2.33 Kelas BetsObatEntity

**ID Kelas:** C49  
**Nama Kelas:** BetsObatEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/EntityFarmasiTest.java`, kelompok `C49 BetsObatEntity` (tanpa basis data). Tanggal kedaluwarsa dihitung relatif terhadap hari test dijalankan.

Tabel 4.2.33. Hasil Pengujian Unit Kelas C49

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C49-UT01 | `kurangi(jumlah: int): void` | Normal | Bets KAP-A01 sisa 10, diambil 4 | Sisa 6 | 6 | Pass |
| C49-UT02 | `kurangi(jumlah: int): void` | Batas | Sisa 10, diambil 10 | Bets habis, sisa 0 | 0 | Pass |
| C49-UT03 | `kurangi(jumlah: int): void` | Negatif | Sisa 6, diambil 7 | `IllegalStateException` "Sisa bets KAP-A01 hanya 6", sisa tetap | Pengecualian dan pesan sesuai, sisa 6 | Pass |
| C49-UT04 | `kurangi(jumlah: int): void` | Negatif | Diambil 0 dan -2 | `IllegalArgumentException` "Jumlah pengambilan harus lebih dari nol", sisa tetap | Keduanya ditolak, sisa 6 | Pass |
| C49-UT05 | `mendekatiKedaluwarsa(hari: int): boolean` | Normal | Kedaluwarsa 30 hari lagi dan 400 hari lagi, batas 90 hari | `true` lalu `false` | `true` dan `false` | Pass |
| C49-UT06 | `mendekatiKedaluwarsa(hari: int): boolean` | Batas | Kedaluwarsa tepat 90 hari lagi dan 91 hari lagi | `true` untuk 90 hari (KF17: "90 hari atau kurang"), `false` untuk 91 hari | `true` dan `false` | Pass |
| C49-UT07 | `mendekatiKedaluwarsa(hari: int): boolean` | Negatif | Bets yang sudah lewat 3 hari (batas 90) dan lewat 1 tahun (batas 0) | Tetap `true`, karena bets yang sudah lewat tanggalnya ikut diperingatkan (DPPL Subbab 5.49) | `true` untuk keduanya | Pass |
| C49-UT08 | `sudahKedaluwarsa(): boolean` | Normal | Kedaluwarsa kemarin | `true` | `true` | Pass |
| C49-UT09 | `sudahKedaluwarsa(): boolean` | Batas | Kedaluwarsa hari ini | `false`, bets masih boleh diserahkan pada hari kedaluwarsanya | `false` | Pass |
| C49-UT10 | `sudahKedaluwarsa(): boolean` | Negatif | Tanggal kedaluwarsa `null` dan besok | `false` untuk keduanya tanpa pengecualian | `false` untuk keduanya | Pass |
| C49-UT11 | `jumlahLayak(daftarBets: List<BetsObatEntity>): int` | Normal | Bets 20 (kedaluwarsa kemarin), 10 (30 hari lagi), dan 20 (400 hari lagi) | 30, karena bets kedaluwarsa tidak dihitung | 30 | Pass |
| C49-UT12 | `jumlahLayak(daftarBets: List<BetsObatEntity>): int` | Batas | Satu bets 15 yang kedaluwarsa hari ini | 15 | 15 | Pass |
| C49-UT13 | `jumlahLayak(daftarBets: List<BetsObatEntity>): int` | Negatif | Daftar kosong, serta satu bets 40 yang kedaluwarsa kemarin | 0 untuk keduanya | 0 untuk keduanya | Pass |

`sudahKedaluwarsa()` dan `jumlahLayak()` adalah operasi public tambahan implementasi. Keduanya dipakai `ApotekManager.potongStok()` dan `ResepManager.validasiStok()` agar bets kedaluwarsa tidak pernah diserahkan maupun dihitung sebagai stok layak (C24-UT18, C24-UT20, C23-UT11).

Tabel 4.2.33b. Ringkasan Cakupan Kelas C49

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `kurangi()`, `mendekatiKedaluwarsa()` |
| Operasi yang diuji | Kedua operasi DPPL (2 dari 2), ditambah dua operasi public tambahan implementasi, masing-masing dengan kasus normal, batas, dan negatif |
| ID Algoritma yang tercakup | Algo-C49-01 (pengurangan dan pemeriksaan kedaluwarsa: C49-UT01 s.d. C49-UT07) |
| Coverage (wajib ≥ 70%) | 76,5% baris (26 dari 34) dan 100% cabang (14 dari 14), JaCoCo 0.8.14 dari seluruh `mvn test`. Baris yang tidak tercakup adalah konstruktor kosong serta *setter* `setNomorBets()`, `setIdObat()`, dan `setTanggalKedaluwarsa()` yang tidak dipanggil aplikasi. Bukti pada Subbab 4.6 |

### 4.2.34 Kelas PenerimaanObatEntity

**ID Kelas:** C50  
**Nama Kelas:** PenerimaanObatEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/EntityFarmasiTest.java`, kelompok `C50 PenerimaanObatEntity` (tanpa basis data). Penerimaan dengan isian tidak valid ditolak lebih dulu oleh `PersediaanManager` (C25-UT07 sampai C25-UT15), sehingga `totalJumlah()` hanya menjumlahkan.

Tabel 4.2.34. Hasil Pengujian Unit Kelas C50

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C50-UT01 | `totalJumlah(): int` | Normal | Dua bets berjumlah 100 dan 200 | 300 | 300 | Pass |
| C50-UT02 | `totalJumlah(): int` | Batas | Satu bets berjumlah 1 | 1 | 1 | Pass |
| C50-UT03 | `totalJumlah(): int` | Negatif | Penerimaan baru tanpa bets (konstruktor kosong dan daftar kosong) | 0 tanpa pengecualian | 0 untuk keduanya | Pass |

Tabel 4.2.34b. Ringkasan Cakupan Kelas C50

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `totalJumlah()` |
| Operasi yang diuji | `totalJumlah()` (1 dari 1) dengan kasus normal, batas, dan negatif. Pemakaiannya pada penambahan stok diuji lewat C25-UT04 dan INT-TC25 |
| ID Algoritma yang tercakup | Algo-C50-01 (penjumlahan penerimaan: C50-UT01 s.d. C50-UT03) |
| Coverage (wajib ≥ 70%) | 76,9% baris (20 dari 26) dan 100% cabang (2 dari 2), JaCoCo 0.8.14 dari seluruh `mvn test`. Baris yang tidak tercakup adalah *setter* `setNomorFaktur()`, `setTanggalTerima()`, dan `setDaftarBets()` yang tidak dipanggil aplikasi. Bukti pada Subbab 4.6 |

### 4.2.35 Kelas DaftarPantauEntity

**ID Kelas:** C51  
**Nama Kelas:** DaftarPantauEntity  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/business/entity/DaftarPantauEntityTest.java`. Berkas yang sama memuat satu test untuk `TindakLanjutEntity.terhubung()` (C52, tingkat "Tidak perlu") yang menjadi bagian UC09 dan tidak dimasukkan ke tabel ini.

Tabel 4.2.35. Hasil Pengujian Unit Kelas C51

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C51-UT01 | `perbaruiStatus(status: String): void` | Normal | `DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_BELUM); e.perbaruiStatus(DaftarPantauEntity.STATUS_ULANG); e.getStatusTindakLanjut(); e.getStatusTindakLanjut()` | Belum, perlu dihubungi ulang, lalu selesai ditindaklanjuti. | `"perlu dihubungi ulang"`; `"selesai ditindaklanjuti"` | Pass |
| C51-UT02 | `perbaruiStatus(status: String): void` | Batas | `DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_BELUM); e.perbaruiStatus(DaftarPantauEntity.STATUS_SELESAI); e.getStatusTindakLanjut()` | Dari belum langsung selesai diterima. | `DaftarPantauEntity.STATUS_SELESAI` | Pass |
| C51-UT03 | `perbaruiStatus(status: String): void` | Batas | `DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_ULANG); e.perbaruiStatus(DaftarPantauEntity.STATUS_ULANG); e.getStatusTindakLanjut()` | Entri yang perlu dihubungi ulang masih boleh diubah lagi. | `DaftarPantauEntity.STATUS_ULANG` | Pass |
| C51-UT04 | `perbaruiStatus(status: String): void` | Negatif | `DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_BELUM); IllegalArgumentException x = assertThrows(IllegalArgumentException.class, () -> e.perbaruiStatus("ditunda")); x.getMessage(); e.getStatusTindakLanjut()` | Status yang tidak dikenal ditolak. | `"Status tindak lanjut tidak dikenal: ditunda"`; `DaftarPantauEntity.STATUS_BELUM` | Pass |
| C51-UT05 | `perbaruiStatus(status: String): void` | Negatif | `DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_SELESAI); IllegalStateException x = assertThrows(IllegalStateException.class, () -> e.perbaruiStatus(DaftarPantauEntity.STATUS_BELUM)); x.getMessage()` | Entri yang sudah selesai tidak dapat diubah lagi. | `"Entri sudah selesai ditindaklanjuti"` | Pass |
| C51-UT06 | `perbaruiStatus(status: String): void` | Negatif | `DaftarPantauEntity e = entri(DaftarPantauEntity.STATUS_SELESAI); e.perbaruiStatus("ditunda")` | Status tidak dikenal diperiksa lebih dulu daripada status selesai. | melempar `IllegalArgumentException` | Pass |

Tabel 4.2.35b. Ringkasan Cakupan Kelas C51

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `perbaruiStatus()` |
| Operasi yang diuji | Satu operasi DPPL (1 dari 1) memiliki kasus normal, batas, dan negatif |
| ID Algoritma yang tercakup | Algo-C51-01 (`perbaruiStatus()`: C51-UT01 s.d. C51-UT06) |
| Coverage (wajib ≥ 70%) | 80,6% baris (29 dari 36) dan 100,0% cabang (4 dari 4), diukur dengan JaCoCo 0.8.14 dari seluruh `mvn test` dengan basis data uji. Baris yang tidak tercakup adalah konstruktor kosong dan tiga pembaca atau pengubah atribut yang tidak dipakai test kelas ini (`getNomorTelepon()`, `setIdPasien()`, dan `setJenisRisiko()`). |

### 4.2.36 Kelas LaporanEntity

**ID Kelas:** C53  
**Nama Kelas:** LaporanEntity  
**Tingkat:** Mendalam

*[Diisi Fachry: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.37 Kelas BundelFHIREntity

**ID Kelas:** C54  
**Nama Kelas:** BundelFHIREntity  
**Tingkat:** Mendalam

*[Diisi Fachry: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.38 Kelas DataAkunBoundary

**ID Kelas:** C56  
**Nama Kelas:** DataAkunBoundary  
**Tingkat:** Sedang

Berkas test: `src/tests/sehati/dataaccess/DataAkunBoundaryTest.java`. Setiap test berjalan dalam transaksi yang di-*rollback*. Pemakaiannya oleh `AutentikasiManager` dan `KonfigurasiManager` juga diuji lewat INT-TC01 sampai INT-TC03.

Tabel 4.2.38. Hasil Pengujian Unit Kelas C56

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C56-UT01 | `simpanPengguna(p: PenggunaEntity): void`, `cariPengguna(nama: String): PenggunaEntity`, `ambilPengguna(idPengguna: int): PenggunaEntity` (Q-C56-01, 02, 04, 05) | Normal | Akun baru `uji_akun` disimpan, lalu peran diubah menjadi Dokter dan dinonaktifkan | ID terisi, nama terpakai, akun terbaca dengan peran Dokter dan nonaktif | Sesuai | Pass |
| C56-UT02 | `cariPengguna()`, `ambilPengguna()`, `namaPenggunaDipakai(nama: String): boolean` (Q-C56-01 s.d. 03) | Negatif | Nama acak, ID -1, dan "ADMIN" (huruf kapital) | `null`, `null`, `false`, dan `null` (nama dicocokkan persis) | Sesuai | Pass |
| C56-UT03 | `simpanPengguna(p: PenggunaEntity): void` | Negatif | Nama `uji_ganda` dua kali, lalu peran "Satpam" | `IllegalArgumentException` "Nama pengguna sudah dipakai" (SQLState 23505) dan "Peran atau status akun tidak valid" (23514) | Kedua pesan sesuai | Pass |
| C56-UT04 | `simpanSesi(s: SesiEntity): void` (Q-C56-06) | Normal | Sesi baru disimpan, lalu ditutup dan disimpan lagi | `status_aktif` true lalu false pada baris yang sama | Sesuai | Pass |
| C56-UT05 | `tutupSesiAktif(idPengguna: int): void` (Q-C56-07) | Normal | Dua sesi aktif satu akun | Keduanya tidak aktif | Sesuai | Pass |
| C56-UT06 | `sesiMasihAktif(idSesi: UUID): boolean` (Q-C56-10) | Normal dan negatif | Sesi aktif, sesi yang sudah ditutup, dan UUID acak | `true`, `false`, `false` | Sesuai | Pass |
| C56-UT07 | `simpanAudit(a: AuditEntity): void` (Q-C56-08) | Normal | Audit berpelaku 0 dengan deskripsi unik | Baris audit bertambah dengan pelaku 0 | Sesuai | Pass |
| C56-UT08 | Hak akses tabel `audit` | Negatif | `SET LOCAL ROLE sehati_app`, lalu `UPDATE` dan `DELETE` pada `audit` (KNF05) | Keduanya ditolak SQLState 42501 (*insufficient_privilege*) | Keduanya 42501 | Pass |
| C56-UT09 | `semuaPengguna(): List<PenggunaEntity>` (Q-C56-09) | Normal | Akun `aaa_uji_urut` ditambahkan | Seluruh akun termasuk yang nonaktif, terurut nama, `aaa_uji_urut` pertama | Sesuai | Pass |
| C56-UT10 | Seluruh operasi | Negatif | Koneksi ditutup sebelum sembilan operasi dipanggil | Setiap operasi melempar `IllegalStateException` "Basis data tidak dapat dihubungi" | Kesembilannya sesuai | Pass |

*Coverage* (bukan syarat kelas Sedang): 100% baris (93 dari 93) dan 93,8% cabang (15 dari 16).

### 4.2.39 Kelas DataPasienBoundary

**ID Kelas:** C57  
**Nama Kelas:** DataPasienBoundary  
**Tingkat:** Sedang

*[Diisi Steven: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

### 4.2.40 Kelas DataKunjunganBoundary

**ID Kelas:** C58  
**Nama Kelas:** DataKunjunganBoundary  
**Tingkat:** Sedang

*[Diisi Steven: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

### 4.2.41 Kelas DataKlinisBoundary

**ID Kelas:** C59  
**Nama Kelas:** DataKlinisBoundary  
**Tingkat:** Sedang

Kelas ini dipegang dua anggota sesuai pembagian metode pada Tugas 9: bagian skrining dan risiko (Q-C59-01 sampai Q-C59-10) oleh Humam memakai C59-UT01 sampai C59-UT20, dan bagian pemeriksaan (Q-C59-11 sampai Q-C59-20) oleh Adnan memakai C59-UT21 ke atas.

Bagian skrining dan risiko diuji pada berkas `src/tests/sehati/dataaccess/DataKlinisSkriningDbTest.java`. Setiap test berjalan dalam transaksi yang di-*rollback*, sehingga seed tidak berubah. Penyimpanan skrining bersama `SkriningManager` juga diuji lewat INT-TC11 sampai INT-TC14.

Tabel 4.2.41a. Hasil Pengujian Unit Kelas C59 (Bagian Skrining dan Risiko)

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C59-UT01 | `simpanSkrining(s: SkriningEntity): void` | Normal | `SkriningEntity s = skrining(2, 110); boundary.simpanSkrining(s); SkriningEntity baca = boundary.ambilSkrining(2); s.getIdSkrining() > 0; baca.getKeluhanAwal(); baca.getTandaVital().getSistolik()` | Skrining dan tanda vital tersimpan lalu dapat dibaca kembali. | benar; `"Pusing"`; `125`; dan 2 pemeriksaan serupa lainnya | Pass |
| C59-UT02 | `simpanSkrining(s: SkriningEntity): void` | Batas | `boundary.simpanSkrining(skrining(2, null)); boundary.ambilSkrining(2).getTandaVital().getGulaDarah()` | Gula darah kosong tersimpan sebagai null. | null | Pass |
| C59-UT03 | `simpanSkrining(s: SkriningEntity): void` | Negatif | `SkriningEntity s = new SkriningEntity(0, 2, "Batuk", LocalDateTime.now()); IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanSkrining(s)); e.getMessage()` | Skrining tanpa tanda vital ditolak. | `"Tanda vital belum dilampirkan pada skrining"` | Pass |
| C59-UT04 | `simpanSkrining(s: SkriningEntity): void` | Negatif | `IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanSkrining(skrining(9, null))); e.getMessage()` | Kunjungan yang sudah diskrining ditolak dengan pesan yang dapat dibaca. | `"Data sudah tersimpan sebelumnya"` | Pass |
| C59-UT05 | `simpanSkrining(s: SkriningEntity): void` | Negatif | `SkriningEntity s = skrining(2, null); s.getTandaVital().setSistolik(400); IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanSkrining(s)); e.getMessage()` | Nilai di luar aturan basis data ditolak. | `"Nilai tanda vital di luar rentang yang diizinkan"` | Pass |
| C59-UT06 | `ambilSkrining(idKunjungan: int): SkriningEntity` | Normal | `SkriningEntity s = boundary.ambilSkrining(9); s; s.getIdKunjungan(); s.getTandaVital().getSistolik()` | Skrining seed kunjungan 9 dibaca lengkap dengan tanda vitalnya. | tidak null; `9`; `152`; dan 2 pemeriksaan serupa lainnya | Pass |
| C59-UT07 | `ambilSkrining(idKunjungan: int): SkriningEntity` | Negatif | `boundary.ambilSkrining(2); boundary.ambilSkrining(-1)` | Kunjungan yang belum diskrining mengembalikan null. | null; null | Pass |
| C59-UT08 | `riwayatTandaVital(idPasien: int, batas: int): List<TandaVitalEntity>` | Normal | `List<TandaVitalEntity> riwayat = boundary.riwayatTandaVital(1, 3); riwayat.stream().map(TandaVitalEntity::getSistolik).toList()` | Tiga pengukuran sebelum hari ini, terbaru lebih dulu. | `List.of(146, 150, 148)` | Pass |
| C59-UT09 | `riwayatTandaVital(idPasien: int, batas: int): List<TandaVitalEntity>` | Batas | `List<TandaVitalEntity> riwayat = boundary.riwayatTandaVital(1, 0); riwayat.stream().map(TandaVitalEntity::getSistolik).toList(); riwayat.stream().allMatch(t -> t.getWaktu() != null)` | Batas 0 memuat seluruh pengukuran, terlama lebih dulu, berwaktu. | `List.of(148, 150, 146, 152)`; benar | Pass |
| C59-UT10 | `riwayatTandaVital(idPasien: int, batas: int): List<TandaVitalEntity>` | Negatif | `IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.riwayatTandaVital(1, -1)); e.getMessage(); boundary.riwayatTandaVital(-1, 3).isEmpty()` | Batas negatif ditolak dan pasien tanpa pengukuran kosong. | `"Batas riwayat tidak boleh negatif"`; benar | Pass |
| C59-UT11 | `ambilAmbang(): List<AmbangRisikoEntity>` | Normal | `List<AmbangRisikoEntity> ambang = boundary.ambilAmbang(); ambang.size(); nilai(ambang, "hipertensi"); nilai(ambang, "obesitas")` | Ketiga ambang data master tersedia. | `3`; `140`; `30`; dan 1 pemeriksaan serupa lainnya | Pass |
| C59-UT12 | `simpanAmbang(a: AmbangRisikoEntity): void` | Normal | `boundary.simpanAmbang(new AmbangRisikoEntity(1, "hipertensi", 135)); nilai(boundary.ambilAmbang(), "hipertensi")` | Nilai batas dimutakhirkan. | `135` | Pass |
| C59-UT13 | `simpanAmbang(a: AmbangRisikoEntity): void` | Negatif | `IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanAmbang(new AmbangRisikoEntity(99, "hipertensi", 135))); e.getMessage()` | Ambang yang tidak ada ditolak. | `"Ambang risiko tidak ditemukan: 99"` | Pass |
| C59-UT14 | `simpanPenanda(p: PenandaRisikoEntity): void` | Normal | `PenandaRisikoEntity p = new PenandaRisikoEntity(0, 2, "obesitas", LocalDate.now(), PenandaRisikoEntity.STATUS_AKTIF); boundary.simpanPenanda(p); p.getIdPenanda() > 0; boundary.penandaKunjungan(2).stream().map(PenandaRisikoEntity::getJenisRisiko).toList()` | Penanda baru mendapat id dan muncul sebagai penanda aktif. | benar; `List.of("obesitas")` | Pass |
| C59-UT15 | `simpanPenanda(p: PenandaRisikoEntity): void` | Batas | `PenandaRisikoEntity p = boundary.penandaKunjungan(9).get(0); p.cabut(); boundary.simpanPenanda(p); boundary.penandaKunjungan(9).isEmpty()` | Penanda yang dicabut tidak lagi tampil sebagai penanda aktif. | benar | Pass |
| C59-UT16 | `simpanPenanda(p: PenandaRisikoEntity): void` | Negatif | `PenandaRisikoEntity p = new PenandaRisikoEntity(0, 2, "obesitas", null, PenandaRisikoEntity.STATUS_AKTIF); IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanPenanda(p)); e.getMessage()` | Penanda baru tanpa tanggal ditolak. | `"Tanggal penanda risiko wajib diisi"` | Pass |
| C59-UT17 | `simpanPenanda(p: PenandaRisikoEntity): void` | Negatif | `PenandaRisikoEntity p = new PenandaRisikoEntity(999, 2, "obesitas", LocalDate.now(), PenandaRisikoEntity.STATUS_DICABUT); IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanPenanda(p)); e.getMessage()` | Pembaruan penanda yang tidak ada ditolak. | `"Penanda risiko tidak ditemukan: 999"` | Pass |
| C59-UT18 | `penandaKunjungan(idKunjungan: int): List<PenandaRisikoEntity>` | Normal | `List<PenandaRisikoEntity> penanda = boundary.penandaKunjungan(9); penanda.size(); penanda.get(0).getJenisRisiko(); penanda.get(0).getStatus()` | Penanda aktif seed kunjungan 9 adalah hipertensi. | `1`; `"hipertensi"`; `"aktif"` | Pass |
| C59-UT19 | `penandaKunjungan(idKunjungan: int): List<PenandaRisikoEntity>` | Negatif | `boundary.penandaKunjungan(2).isEmpty(); boundary.penandaKunjungan(-1).isEmpty()` | Kunjungan tanpa penanda mengembalikan daftar kosong. | benar; benar | Pass |
| C59-UT20 | `simpanSkrining(s: SkriningEntity): void` | Negatif | `IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> boundary.simpanSkrining(skrining(-1, null))); e.getMessage()` | Kunjungan yang tidak ada ditolak dengan pesan yang dapat dibaca. | `"Kunjungan atau data terkait tidak ditemukan"` | Pass |

Bagian pemeriksaan diuji pada berkas `src/tests/sehati/dataaccess/DataKlinisBoundaryPemeriksaanTest.java`. Setiap test berjalan dalam transaksi yang di-*rollback*. Penyimpanan pemeriksaan bersama `PemeriksaanManager` juga diuji lewat INT-TC17 sampai INT-TC20.

Tabel 4.2.41b. Hasil Pengujian Unit Kelas C59 (Bagian Pemeriksaan)

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C59-UT21 | `loloskanPola(teks: String): String` (pendukung Algo-C57-01) | Normal | `50%_a\b` | Tanda `%`, `_`, dan `\` diloloskan menjadi huruf biasa | `50\%\_a\\b` | Pass |
| C59-UT22 | `cariKodeICD10(kataKunci: String): List<KodeICD10Entity>` | Normal | "I1" dan "hipertensi" (Q-C59-11) | I10 di urutan pertama untuk "I1" dan termasuk hasil "hipertensi" | Keduanya sesuai | Pass |
| C59-UT23 | `cariKodeICD10(kataKunci: String): List<KodeICD10Entity>` | Batas | 25 kode uji U01 sampai U25 disisipkan, lalu dicari "U" | Paling banyak 20 baris | 20 baris | Pass |
| C59-UT24 | `cariKodeICD10(kataKunci: String): List<KodeICD10Entity>` | Negatif | "%" dan `null` | "%" tidak menjadi *wildcard* sehingga hasil kosong, `null` ditolak dengan `IllegalArgumentException` | Hasil kosong dan pengecualian sesuai | Pass |
| C59-UT25 | `simpanPemeriksaan(p: PemeriksaanEntity, d: DiagnosisEntity): void` | Normal | Sisipkan dengan R51, lalu simpan ulang anamnesis baru dengan I10 (Q-C59-12, Q-C59-13) | ID pemeriksaan tetap, anamnesis dan diagnosis berganti | ID sama, anamnesis "Pusing sejak dua hari", diagnosis I10 | Pass |
| C59-UT26 | `simpanPemeriksaan(p: PemeriksaanEntity, d: DiagnosisEntity): void` | Negatif | Diagnosis berkode "XYZ" | `IllegalArgumentException` karena kode tidak ada di data master | Ditolak | Pass |
| C59-UT27 | `simpanPemeriksaan(p: PemeriksaanEntity, d: DiagnosisEntity): void` | Negatif | Pemeriksaan dikunci, lalu anamnesis diubah dan disimpan ulang | `IllegalStateException` dan isi lama tetap | Ditolak, anamnesis tetap "Pusing" | Pass |
| C59-UT28 | `simpanJadwalKontrol(j: JadwalKontrolEntity): void` | Normal | Jadwal "terjadwal" tujuh hari lagi (Q-C59-15) | ID terisi dan jadwal muncul pada `ambilJadwalKontrol()` | ID > 0, jadwal muncul | Pass |
| C59-UT29 | `simpanJadwalKontrol(j: JadwalKontrolEntity): void` | Batas | Jadwal yang sama disimpan ulang berstatus "selesai" (Q-C59-16) | Tidak lagi dimuat `ambilJadwalKontrol()` (Q-C59-17) | Tidak dimuat | Pass |
| C59-UT30 | `jadwalJatuhTempo(idPasien: int, tanggal: LocalDate): List<JadwalKontrolEntity>` | Normal | Jadwal pasien 1 hari ini dan besok (Q-C59-19) | Jadwal hari ini termasuk, jadwal besok belum | Sesuai | Pass |
| C59-UT31 | `jadwalPadaTanggal(idPasien: int, tanggal: LocalDate): List<JadwalKontrolEntity>` (Q-C59-20, perbaikan Tabel 3.5) | Normal | Jadwal pasien 1 pada 01-01-2099 yang sudah memicu entri daftar pantau | Tidak dimuat `ambilJadwalKontrol()`, tetapi ditemukan `jadwalPadaTanggal()` | Sesuai, hanya jadwal tersebut yang dikembalikan | Pass |
| C59-UT32 | `jadwalPadaTanggal(idPasien: int, tanggal: LocalDate): List<JadwalKontrolEntity>` | Negatif | Tanggal lain (02-01-2099), lalu jadwal yang sama diubah menjadi "selesai" | Daftar kosong untuk keduanya | Kosong untuk keduanya | Pass |

### 4.2.42 Kelas DataPantauBoundary

**ID Kelas:** C60  
**Nama Kelas:** DataPantauBoundary  
**Tingkat:** Sedang

Berkas test: `src/tests/sehati/dataaccess/DataPantauDbTest.java`. Setiap test berjalan dalam transaksi yang di-*rollback*, sehingga seed tidak berubah. Penyimpanan tindak lanjut bersama `PantauManager` juga diuji lewat INT-TC15 dan INT-TC16.

Tabel 4.2.42. Hasil Pengujian Unit Kelas C60

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C60-UT01 | `ambilEntri(): List<DaftarPantauEntity>` | Normal | `List<DaftarPantauEntity> entri = boundary.ambilEntri(); entri.size(); entri.stream().allMatch(e -> e.getTanggalPemicu() != null); entri.stream().anyMatch(e -> "kunjungan ulang".equals(e.getJenisRisiko()))` | Empat entri seed belum selesai beserta tanggal pemicunya. | `4`; benar; benar | Pass |
| C60-UT02 | `ambilEntriSelesai(): List<DaftarPantauEntity>` | Negatif | `boundary.ambilEntriSelesai().isEmpty()` | Seed tidak memiliki entri selesai. | benar | Pass |
| C60-UT03 | `simpanEntri(e: DaftarPantauEntity): void` | Normal | `boundary.simpanEntri(e); e.getIdEntri() > 0; hitung("SELECT count(*) FROM daftar_pantau WHERE id_pasien = 4 AND jenis_risiko = 'obesitas'")` | Entri risiko baru yang punya penanda mendapat id. | benar; `1` | Pass |
| C60-UT04 | `simpanEntri(e: DaftarPantauEntity): void` | Normal | `DaftarPantauEntity e = boundary.ambilEntri().stream().filter(x -> x.getIdEntri() == 1).findFirst() .orElseThrow(); e.perbaruiStatus(DaftarPantauEntity.STATUS_ULANG); boundary.simpanEntri(e); boundary.ambilEntri().stream() .filter(x -> x.getIdEntri() == 1).fin…` | Entri lama hanya memutakhirkan status tindak lanjut. | `DaftarPantauEntity.STATUS_ULANG` | Pass |
| C60-UT05 | `simpanEntri(e: DaftarPantauEntity): void` | Batas | `long sebelum = hitung("SELECT count(*) FROM daftar_pantau"); DaftarPantauEntity e = new DaftarPantauEntity(0, 1, "kunjungan ulang", DaftarPantauEntity.STATUS_BELUM); boundary.simpanEntri(e); e.getIdEntri(); hitung("SELECT count(*) FROM daftar_pantau")` | Jadwal yang sudah punya entri tidak dibuatkan entri kedua. | `0`; `sebelum` | Pass |
| C60-UT06 | `simpanEntri(e: DaftarPantauEntity): void` | Negatif | `DaftarPantauEntity e = new DaftarPantauEntity(0, 4, "hipertensi", DaftarPantauEntity.STATUS_BELUM); IllegalStateException x = assertThrows(IllegalStateException.class, () -> boundary.simpanEntri(e)); x.getMessage()` | Entri risiko tanpa penanda sebagai pemicu ditolak. | `"Entri daftar pantau tidak memiliki pemicu"` | Pass |
| C60-UT07 | `simpanTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Normal | `TindakLanjutEntity t = new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui"); boundary.simpanTindakLanjut(1, t); t.getIdTindakLanjut() > 0; hitung("SELECT count(*) FROM tindak_lanjut WHERE id_entri = 1")` | Upaya tersimpan sebagai baris baru dan mendapat id. | benar; `1` | Pass |
| C60-UT08 | `simpanTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Batas | `boundary.simpanTindakLanjut(1, new TindakLanjutEntity(0, "telepon", "tidak terhubung", "belum diketahui")); boundary.simpanTindakLanjut(1, new TindakLanjutEntity(0, "pesan singkat", "terhubung", "akan datang")); hitung("SELECT count(*) FROM tindak_lanjut WHER…` | Dua upaya pada satu entri menjadi dua baris. | `2` | Pass |
| C60-UT09 | `simpanTindakLanjut(idEntri: int, t: TindakLanjutEntity): void` | Negatif | `TindakLanjutEntity t = new TindakLanjutEntity(0, "telepon", "terhubung", "akan datang"); IllegalStateException e = assertThrows(IllegalStateException.class, () -> boundary.simpanTindakLanjut(-1, t)); e.getMessage()` | Entri yang tidak ada ditolak. | tidak null | Pass |

Coverage kelas ini 94,0% baris (63 dari 67) dan 95,0% cabang (19 dari 20). Kelas Sedang tidak memiliki target coverage.

### 4.2.43 Kelas DataFarmasiBoundary

**ID Kelas:** C61  
**Nama Kelas:** DataFarmasiBoundary  
**Tingkat:** Sedang

Berkas test: `src/tests/sehati/dataaccess/DataFarmasiBoundaryTest.java`. Setiap test yang membutuhkan basis data berjalan dalam transaksi yang di-*rollback*. Kasus yang harus membuktikan *rollback* milik `DataFarmasiBoundary` sendiri (C61-UT09, C61-UT12, C61-UT17) memakai koneksi kedua yang masih *auto-commit*. Pemakaiannya bersama `ApotekManager` dan `PersediaanManager` juga diuji lewat INT-TC23 sampai INT-TC26.

Tabel 4.2.43. Hasil Pengujian Unit Kelas C61

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C61-UT01 | `ambilSemuaObat(): List<ObatEntity>` (Q-C61-01) | Normal | Panggil tanpa masukan | Seluruh obat terurut nama | 12 obat, terurut nama | Pass |
| C61-UT02 | `ambilObat(idObat: int): ObatEntity` (Q-C61-02) | Normal | Obat 1 | Nama, sediaan, stok, dan ambang terisi | `Parasetamol 500 mg\|tablet\|300\|100` | Pass |
| C61-UT03 | `ambilObat(idObat: int): ObatEntity` | Negatif | Obat -1 | `null` | `null` | Pass |
| C61-UT04 | `ambilObat(idObat: int): ObatEntity` | Batas | Baris Amlodipin diambil dalam transaksi, lalu transaksi kedua (`lock_timeout` 300 ms) mengambil baris yang sama | Transaksi kedua tertahan kunci `FOR UPDATE` lalu gagal (KF23, KNF11) | Transaksi kedua gagal dengan `IllegalStateException` | Pass |
| C61-UT05 | `ambilBets(idObat: int): List<BetsObatEntity>` (Q-C61-03) | Normal | Amlodipin | Bets bersisa terurut kedaluwarsa terdekat | AML-A01, lalu AML-B01 | Pass |
| C61-UT06 | `ambilBets(idObat: int): List<BetsObatEntity>` | Negatif | AML-A01 diubah menjadi 0 dalam transaksi, lalu Asam Mefenamat (tanpa bets) | Bets habis tidak dimuat dan obat tanpa bets menghasilkan daftar kosong | Hanya AML-B01, lalu daftar kosong | Pass |
| C61-UT07 | `simpanStok(o: ObatEntity, bets: List<BetsObatEntity>): void` (Q-C61-04, Q-C61-05) | Normal | Stok Amlodipin dan bets pertamanya dikurangi 5 di memori, lalu disimpan | Nilai akhir pada objek ditulis | Stok dan sisa bets terbaca sama dengan objek | Pass |
| C61-UT08 | `simpanStok(o: ObatEntity, bets: List<BetsObatEntity>): void` | Negatif | Obat 9999 | `IllegalArgumentException` "Obat tidak ditemukan: 9999" | Pengecualian dan pesan sesuai | Pass |
| C61-UT09 | `simpanStok(o: ObatEntity, bets: List<BetsObatEntity>): void` | Negatif | Stok Parasetamol -1 dan bets PCT-A01 0, disimpan lewat koneksi *auto-commit* | CHECK `stok >= 0` menolak, transaksi milik operasi di-*rollback*, sehingga bets tidak ikut berubah | `IllegalArgumentException` "Gagal menyimpan stok obat: nilai di luar batas yang diizinkan". Stok dan PCT-A01 tetap 300, *auto-commit* kembali aktif | Pass |
| C61-UT10 | `simpanPenerimaan(p: PenerimaanObatEntity): void` (Q-C61-06, Q-C61-07) | Normal | Faktur "FKT-UJI-01", bets PCT-UJI-01 × 50 | ID penerimaan terisi dan bets tertaut ke penerimaan itu | ID terisi, `id_penerimaan` bets sama, bets muncul pada `ambilBets()` | Pass |
| C61-UT11 | `simpanPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Nomor bets PCT-A01 yang sudah tercatat | `IllegalArgumentException` "nomorBets" (Algo-C61-01) | Pengecualian dan pesan sesuai | Pass |
| C61-UT12 | `simpanPenerimaan(p: PenerimaanObatEntity): void` | Negatif | Kasus C61-UT11 lewat koneksi *auto-commit* | Faktur yang sudah tersisip ikut dibatalkan | 0 baris faktur "FKT-UJI-03", *auto-commit* kembali aktif | Pass |
| C61-UT13 | `obatPerluPeringatan(hari: int): List<ObatEntity>` (Q-C61-08) | Normal | 90 hari | Obat di bawah ambang atau memiliki bets bersisa dalam 90 hari | Amlodipin 5 mg, Asam Mefenamat 500 mg, Glibenklamid 5 mg | Pass |
| C61-UT14 | `obatPerluPeringatan(hari: int): List<ObatEntity>` | Batas | 0 hari | Hanya obat di bawah ambang | Asam Mefenamat 500 mg, Glibenklamid 5 mg | Pass |
| C61-UT15 | `simpanResep(r: ResepEntity): void` (Q-C61-09 s.d. Q-C61-11) | Normal | Resep baru berisi Parasetamol × 10, lalu status diubah "dikembalikan" dengan catatan "Dosis perlu diperbaiki" | ID resep dan ID item terisi, item terbaca, lalu status dan catatan termutakhirkan | Sesuai, `dikembalikan\|Dosis perlu diperbaiki` | Pass |
| C61-UT16 | `simpanResep(r: ResepEntity): void` | Negatif | Memperbarui resep 99999 | `IllegalArgumentException` "Resep tidak ditemukan: 99999" | Pengecualian dan pesan sesuai | Pass |
| C61-UT17 | `simpanResep(r: ResepEntity): void` | Negatif | Resep baru dengan item Parasetamol dan item berobat 9999 (melanggar *foreign key*), lewat koneksi *auto-commit* | Resep beserta item pertamanya dibatalkan dan ID resep kembali 0 | `IllegalStateException`, ID 0, jumlah baris `resep` tetap | Pass |
| C61-UT18 | `ambilAntreanResep(): List<ResepEntity>` (Q-C61-12, Q-C61-14) | Normal | Panggil tanpa masukan | Hanya resep "menunggu", masing-masing beserta itemnya | Jumlah sama dengan `count(*)`, seluruhnya "menunggu" dan berisi item | Pass |
| C61-UT19 | `ambilAntreanResep(): List<ResepEntity>` | Batas | Seluruh resep "menunggu" diubah dalam transaksi | Daftar kosong | Daftar kosong | Pass |
| C61-UT20 | `ambilResep(idResep: int): ResepEntity` (Q-C61-13) | Normal dan negatif | Resep 1 dan resep -1 | Resep 1 "selesai dilayani" dengan satu item, resep -1 `null` | Sesuai | Pass |
| C61-UT21 | `kunciResep(idResep: int): ResepEntity` (Q-C61-15) | Batas dan negatif | Resep menunggu dikunci, lalu transaksi kedua mengunci resep yang sama. Terakhir resep -1 | Transaksi kedua tertahan lalu gagal, resep -1 `null` | Sesuai | Pass |
| C61-UT22 | `ambilPasienResep(daftarIdResep: List<Integer>): Map<Integer, PasienEntity>` (Q-C61-16) | Normal dan batas | `[resep menunggu, -1]`, lalu daftar kosong | Hanya resep yang ada yang dipetakan ke pasiennya, daftar kosong mengembalikan peta kosong | Satu entri dengan NIK sama dengan tabel `pasien`, lalu peta kosong | Pass |
| C61-UT23 | `ambilAsalResep(daftarIdResep: List<Integer>): Map<Integer, String>` (Q-C61-19) | Normal dan batas | Resep pertama, lalu `null` | "nama poli / Antrean n", lalu peta kosong | Sesuai | Pass |
| C61-UT24 | `ambilResepKunjungan(idKunjungan: int)` (Q-C61-18), `tambahObat(o: ObatEntity)` (Q-C61-17) | Normal | Kunjungan resep 1 dan kunjungan -1, lalu obat baru "Obat Uji C61" dengan stok 99 di objek | `[1]` lalu kosong. Obat baru mendapat ID dan stok awal 0 | Sesuai, stok di objek dan di tabel 0 | Pass |
| C61-UT25 | Seluruh operasi | Negatif | Koneksi ditutup sebelum keempat belas operasi dipanggil | Setiap operasi melempar `IllegalStateException` "Basis data tidak dapat dihubungi" | Keempat belas operasi sesuai | Pass |
| C61-UT26 | `sqlState(e: SQLException): String` (pendukung) | Normal | `BatchUpdateException` dengan penyebab 23505, lalu pengecualian tanpa kode | 23505 dari penyebab, lalu `null` | Sesuai | Pass |
| C61-UT27 | `galat(pesan: String, e: Exception): RuntimeException` (pendukung) | Normal | SQLState 23505, 23514, 08006, dan 42P01 | 23505 dan 23514 menjadi `IllegalArgumentException`, 08006 menjadi "Basis data tidak dapat dihubungi", lainnya `IllegalStateException` berpesan asli | Sesuai | Pass |
| C61-UT28 | `galat(pesan: String, e: Exception): RuntimeException` | Negatif | `IllegalArgumentException("nomorBets")` | Diteruskan apa adanya (objek yang sama) | Objek yang sama | Pass |

*Coverage* (bukan syarat kelas Sedang): 96,9% baris (252 dari 260) dan 88,1% cabang (74 dari 84).

### 4.2.44 Kelas DataLaporanBoundary

**ID Kelas:** C62  
**Nama Kelas:** DataLaporanBoundary  
**Tingkat:** Sedang

*[Diisi Fachry: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

### 4.2.45 Kelas DataSinkronisasiBoundary

**ID Kelas:** C63  
**Nama Kelas:** DataSinkronisasiBoundary  
**Tingkat:** Mendalam

*[Diisi Fachry: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.46 Kelas DataPencadanganBoundary

**ID Kelas:** C64  
**Nama Kelas:** DataPencadanganBoundary  
**Tingkat:** Mendalam

Berkas test: `src/tests/sehati/dataaccess/DataPencadanganBoundaryTest.java`. Pada C64-UT01 sampai C64-UT12, `pg_dump` dan `pg_restore` diganti skrip *shell* palsu di folder sementara agar setiap cabang (berhasil, kode keluar bukan nol, arsip tidak terbentuk, isi arsip tidak lengkap) dapat dipicu. `pg_dump` dan `pg_restore` asli diuji pada C29-UT31 sampai C29-UT33 serta INT-TC05 dan INT-TC06. C64-UT13 sampai C64-UT16 memakai basis data uji dalam transaksi yang di-*rollback*.

Tabel 4.2.46. Hasil Pengujian Unit Kelas C64

| ID Pengujian | Operasi yang Diuji | Jenis Kasus | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| C64-UT01 | `jalankanPgDump(lokasi: String): PencadanganEntity` | Normal | Media ada, `pg_dump` palsu menulis 5 bait, `lokasi` `null` | Status "berhasil", ukuran 5, lokasi `<media>/sehati_yyyyMMdd-HHmmss.dump`, berkas ada | Sesuai | Pass |
| C64-UT02 | `jalankanPgDump(lokasi: String): PencadanganEntity` | Batas | `lokasi` diisi folder lain dari `LOKASI_MEDIA_CADANGAN` | Arsip ditulis ke folder argumen | Lokasi berawalan folder argumen | Pass |
| C64-UT03 | `jalankanPgDump(lokasi: String): PencadanganEntity` | Negatif | Folder media tidak ada (HDD belum dicolok, UC12 alternatif 4) | "gagal", ukuran 0, `pg_dump` tidak dijalankan | Sesuai, berkas penanda tidak dibuat | Pass |
| C64-UT04 | `jalankanPgDump(lokasi: String): PencadanganEntity` | Negatif | `pg_dump` menulis arsip lalu keluar dengan kode 1 | "gagal" dan arsip setengah jadi dihapus | Sesuai | Pass |
| C64-UT05 | `jalankanPgDump(lokasi: String): PencadanganEntity` | Negatif | `LOKASI_MEDIA_CADANGAN` kosong dan `lokasi` `null` | "gagal" dengan lokasi kosong | Sesuai | Pass |
| C64-UT06 | `jalankanPgDump(lokasi: String): PencadanganEntity` | Negatif | `PG_DUMP` menunjuk berkas yang tidak ada | "gagal" tanpa pengecualian | Sesuai | Pass |
| C64-UT07 | `jalankanPgDump(lokasi: String): PencadanganEntity` | Batas | `pg_dump` keluar dengan kode 0 tetapi tidak membuat arsip | "gagal", ukuran 0 | Sesuai | Pass |
| C64-UT08 | `verifikasiArsip(p: PencadanganEntity): boolean` | Normal | `pg_restore --list` memuat "TABLE DATA public pasien" dan pembacaan penuh berhasil | `true` | `true` | Pass |
| C64-UT09 | `verifikasiArsip(p: PencadanganEntity): boolean` | Negatif | Daftar isi tanpa data pasien | `false` | `false` | Pass |
| C64-UT10 | `verifikasiArsip(p: PencadanganEntity): boolean` | Negatif | Daftar isi lengkap tetapi pembacaan penuh gagal (arsip terpotong) | `false` | `false` | Pass |
| C64-UT11 | `verifikasiArsip(p: PencadanganEntity): boolean` | Batas | Arsip 0 bait dan arsip yang tidak ada | `false` tanpa menjalankan `pg_restore` | `false` | Pass |
| C64-UT12 | `verifikasiArsip(p: PencadanganEntity): boolean` | Negatif | `pg_restore --list` keluar dengan kode 1 | `false` | `false` | Pass |
| C64-UT13 | `simpanCatatan(p: PencadanganEntity): void`, `ambilTerakhir(): PencadanganEntity` | Normal | Catatan "gagal verifikasi" bertanggal 50 tahun ke depan | ID terisi, catatan itu menjadi yang terakhir dengan lokasi dan ukuran yang sama | Sesuai | Pass |
| C64-UT14 | `ambilTerakhir(): PencadanganEntity` | Batas | Tabel `pencadangan` dikosongkan dalam transaksi | `null` | `null` | Pass |
| C64-UT15 | `simpanCatatan()`, `ambilTerakhir()` | Negatif | Koneksi ditutup | `IllegalStateException` "Basis data tidak dapat dihubungi" | Sesuai | Pass |
| C64-UT16 | `simpanCatatan(p: PencadanganEntity): void` | Negatif | Status "selesai" (di luar CHECK tabel) | `IllegalStateException` | Ditolak | Pass |

Tabel 4.2.46b. Ringkasan Cakupan Kelas C64

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `ambilTerakhir()`, `jalankanPgDump()`, `verifikasiArsip()`, `simpanCatatan()` |
| Operasi yang diuji | Keempat operasi (4 dari 4). `jalankanPgDump()` dan `verifikasiArsip()` dengan kasus normal, batas, dan negatif. `ambilTerakhir()` dan `simpanCatatan()` dengan kasus normal, batas, dan negatif melalui C64-UT13 sampai C64-UT16 |
| ID Algoritma yang tercakup | Algo-C64-01 (C64-UT01 s.d. C64-UT07), Algo-C64-02 (C64-UT08 s.d. C64-UT12), Q-C64-01 dan Q-C64-02 (C64-UT13 s.d. C64-UT16) |
| Coverage (wajib ≥ 70%) | 85,7% baris (84 dari 98) dan 85,7% cabang (36 dari 42), JaCoCo 0.8.14 dari seluruh `mvn test`. Pada pengukuran 18 November 2026 cabangnya baru 69,0%, lalu C64-UT02, C64-UT05 sampai C64-UT07, C64-UT12, C64-UT14 sampai C64-UT16 ditambahkan. Baris yang tidak tercakup adalah batas waktu 30 menit, interupsi *thread*, dan galat baca ukuran arsip. Bukti pada Subbab 4.6 |

<br>

## 4.3 Pengujian Integrasi

Pengujian integrasi memeriksa interaksi antarkomponen yang melewati batas lapisan SEHATI (DPPL Tabel 2.3 dan *Logical View*): `Manager` (*control*) ke `DataBoundary`, aplikasi ke basis data PostgreSQL, dan aplikasi ke sistem eksternal (SATUSEHAT, `pg_dump`, sistem berkas). Setiap pengujian memakai komponen nyata, yaitu `Manager`, `DataBoundary`, dan basis data uji, tanpa *mock* maupun `JdbcPalsu`. Hasilnya dibaca ulang langsung dari tabel lewat koneksi terpisah. Dengan begitu yang diperiksa adalah aliran data (data sampai di tujuan), kecocokan antarmuka (parameter dan format), dan protokol komunikasi (transaksi, penguncian baris, serta penanganan galat). Hubungan `Form` ke `Manager` diuji lewat pengujian *use case* pada Subbab 4.4 karena melibatkan layar.

Test integrasi berada di `src/tests/sehati/integrasi/`. Daftar antarmuka berikut diturunkan dari Tabel 2.4 DPPL (pemetaan *use case* ke komponen) dan *Logical View*, dengan dua *test case* untuk setiap antarmuka: satu skenario normal dan satu skenario gagal.

Tabel 4.3.1. Daftar Antarmuka Antarkomponen

| ID Antarmuka | Komponen Asal | Komponen Tujuan | Jenis Batas | Test Case |
| :--- | :--- | :--- | :--- | :--- |
| IK-01 | AutentikasiManager | DataAkunBoundary, BasisDataSEHATI | *Control* - *Data Access* - Basis data | INT-TC01, INT-TC02 |
| IK-02 | KonfigurasiManager | DataAkunBoundary, DataKunjunganBoundary, DataKlinisBoundary, DataFarmasiBoundary | *Control* - *Data Access* - Basis data | INT-TC03, INT-TC04 |
| IK-03 | KonfigurasiManager, DataPencadanganBoundary | `pg_dump` dan media cadangan | Aplikasi - Sistem eksternal | INT-TC05, INT-TC06 |
| IK-04 | PasienManager | DataPasienBoundary, BasisDataSEHATI | *Control* - *Data Access* - Basis data | INT-TC07, INT-TC08 |
| IK-05 | KunjunganManager | DataKunjunganBoundary, DataPasienBoundary | *Control* - *Data Access* - Basis data | INT-TC09, INT-TC10 |
| IK-06 | SkriningManager | DataKunjunganBoundary, DataKlinisBoundary | *Control* - *Data Access* - Basis data | INT-TC11, INT-TC12 |
| IK-07 | SkriningManager, RisikoManager | DataKlinisBoundary, DataPantauBoundary | *Control* - *Control* - *Data Access* | INT-TC13, INT-TC14 |
| IK-08 | PantauManager | DataPantauBoundary, BasisDataSEHATI | *Control* - *Data Access* - Basis data | INT-TC15, INT-TC16 |
| IK-09 | PemeriksaanManager | DataPasienBoundary, DataKlinisBoundary, DataKunjunganBoundary | *Control* - *Data Access* - Basis data | INT-TC17, INT-TC18 |
| IK-10 | PemeriksaanManager | DataFarmasiBoundary, DataSinkronisasiBoundary, DataPantauBoundary, DataAkunBoundary (satu transaksi) | *Control* - *Data Access* - Basis data | INT-TC19, INT-TC20 |
| IK-11 | ResepManager | DataFarmasiBoundary, DataKunjunganBoundary | *Control* - *Data Access* - Basis data | INT-TC21, INT-TC22 |
| IK-12 | ApotekManager | DataFarmasiBoundary, DataKunjunganBoundary, DataSinkronisasiBoundary | *Control* - *Data Access* - Basis data | INT-TC23, INT-TC24 |
| IK-13 | PersediaanManager | DataFarmasiBoundary, BasisDataSEHATI | *Control* - *Data Access* - Basis data | INT-TC25, INT-TC26 |
| IK-14 | LaporanManager, DataLaporanBoundary | Sistem berkas (PDF, CSV) | Aplikasi - Sistem eksternal | INT-TC27, INT-TC28 |
| IK-15 | SinkronisasiManager | DataSinkronisasiBoundary, BasisDataSEHATI | *Control* - *Data Access* - Basis data | INT-TC29, INT-TC30 |
| IK-16 | SinkronisasiManager, SATUSEHATGateway | SATUSEHAT (mode simulasi atau *sandbox*) | Aplikasi - Sistem eksternal | INT-TC31, INT-TC32 |
| IK-17 | Seluruh `DataBoundary` | BasisDataSEHATI (transaksi dan penguncian baris) | Aplikasi - Basis data | INT-TC33, INT-TC34 |
| IK-18 | PasienManager, KunjunganManager, SkriningManager, PemeriksaanManager, ResepManager, ApotekManager | Antar-`Manager` lewat basis data (alur loket sampai sinkronisasi) | *Control* - *Control* | INT-TC35, INT-TC36 |

Tabel 4.3.2. Hasil Pengujian Integrasi

| ID Pengujian | ID Antarmuka | Skenario | Prosedur/Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| INT-TC01 | IK-01 | Normal: masuk dan keluar dengan akun seed | `AutentikasiManager.autentikasi("admin", "sehati123")`, lalu `tutupSesi(false)`. Baris `sesi` dan `audit` dibaca ulang lewat koneksi terpisah | Baris `sesi` berpemilik pengguna 1 dan aktif, `SesiAktif` berperan Petugas Administrasi, audit pertama "Masuk ke aplikasi". Setelah keluar, sesi yang sama tidak aktif dan audit terakhir "Keluar dari aplikasi" | Sesi `1\|true`, lalu `false`. Audit `1\|Masuk ke aplikasi` dan `1\|Keluar dari aplikasi` | Pass |
| INT-TC02 | IK-01 | Gagal: sandi salah dan akun nonaktif | `autentikasi("admin", "sandiSalah")`, lalu `autentikasi("perawat.nonaktif", "sehati123")` | `null` untuk yang pertama, `IllegalStateException` "Akun nonaktif, hubungi Petugas Administrasi" untuk yang kedua. Jumlah baris `sesi` tidak bertambah dan audit "Masuk gagal, percobaan ke-1" berpelaku pengguna 1 tersimpan | `null` dan pengecualian dengan pesan sesuai. Jumlah `sesi` tetap, audit `1\|Masuk gagal, percobaan ke-1` | Pass |
| INT-TC03 | IK-02 | Normal: data master lintas empat `DataBoundary` | Sesi admin. `muatDataMaster()`, lalu `simpanAkun(uji.integrasi, Dokter, "integrasi123")`, `simpanPoli(0, "Poli Uji Integrasi")`, `tambahObat("Obat Uji Integrasi", "tablet 10 mg", minimum 15)`, dan ambang obesitas menjadi 31 | Nama akun, poli, obat, dan jenis ambang yang dimuat sama dengan isi tabel `pengguna`, `poli`, `obat`, dan `ambang_risiko`. Keempat perubahan sampai ke tabelnya masing-masing, masing-masing dengan satu audit berpelaku admin | Keempat himpunan nama sama. Hash akun cocok dengan sandi, `Dokter\|aktif`, poli ada, obat `0\|15`, obesitas 31, dan 4 audit baru berpelaku 1 | Pass |
| INT-TC04 | IK-02 | Gagal: penolakan dari `DataBoundary` membatalkan transaksi | Sesi admin. `simpanDataMaster(ambang ID 9999)`, lalu `simpanPoli(0, "Poli Umum")` (nama seed) | `DataKlinisBoundary` menolak "Ambang risiko tidak ditemukan: 9999" dan constraint UNIQUE menolak nama poli. Kedua transaksi di-*rollback*: jumlah poli tetap dan tidak ada audit baru | Kedua `IllegalArgumentException` dilempar, jumlah poli tetap, 0 audit baru | Pass |
| INT-TC05 | IK-03 | Normal: `pg_dump` ke media dan verifikasi `pg_restore` | `KonfigurasiManager.jalankanPencadangan()` dengan `pg_dump` 18.6 ke `LOKASI_MEDIA_CADANGAN`, lalu `pg_restore --list` dijalankan terpisah pada arsipnya | Status "berhasil", arsip berada di folder media dengan ukuran sama dengan catatan, daftar isi memuat data tabel `pasien` dan `audit`, catatan tersimpan dan dibaca `muatStatusPencadangan()` | Seluruhnya sesuai | Pass |
| INT-TC06 | IK-03 | Gagal: `pg_dump` ditolak basis data | Hak baca `pasien` untuk `sehati_app` dicabut sementara, lalu `jalankanPencadangan()` | Status "gagal" dengan ukuran 0, jumlah arsip `sehati_*.dump` di folder media tidak bertambah, dan catatan gagal itulah yang dibaca dasbor (`muatStatusPencadangan()`) | Sesuai, jumlah arsip tetap dan `berhasil()` false pada catatan terakhir | Pass |
| INT-TC07 | IK-04 | *[Diisi Steven]* | | | | |
| INT-TC08 | IK-04 | *[Diisi Steven]* | | | | |
| INT-TC09 | IK-05 | *[Diisi Steven]* | | | | |
| INT-TC10 | IK-05 | *[Diisi Steven]* | | | | |
| INT-TC11 | IK-06 | Normal: skrining tersimpan lintas boundary | Sesi Perawat. `SkriningManager.simpanSkrining(2, "Pusing ringan", tanda vital normal)`, lalu baris `skrining`, `tanda_vital`, `kunjungan`, dan `antrean_entri` dibaca ulang lewat koneksi terpisah | Satu baris `skrining` dengan tanda vital, status kunjungan 2 menjadi "siap diperiksa dokter", dan kunjungan 2 keluar dari antrean skrining | Sesuai: satu baris `skrining` dan `tanda_vital` (sistolik 118), status "siap diperiksa dokter", dan tidak ada lagi baris antrean skrining untuk kunjungan 2 | Pass |
| INT-TC12 | IK-06 | Gagal: skrining ganda untuk kunjungan yang sudah diskrining | Sesi Perawat. `simpanSkrining(9, "Ulang", tanda vital normal)` pada kunjungan seed 9 yang sudah memiliki skrining | `IllegalArgumentException` karena `UNIQUE` pada `skrining`. Jumlah baris `skrining` dan `tanda_vital` tidak berubah dan status kunjungan 9 tetap | Sesuai: pengecualian dilempar, jumlah baris sama dengan sebelumnya, status tetap "siap diperiksa dokter" | Pass |
| INT-TC13 | IK-07 | Normal: skrining berisiko menerbitkan penanda dan entri daftar pantau | Sesi Perawat. `simpanSkrining(2, "Lemas", ...)` dengan sistolik 152, berat 90 kg, tinggi 165 cm, dan gula darah 210. Penanda dan entri pantau dibaca ulang lewat koneksi terpisah | Dalam satu transaksi terbit tiga penanda aktif (hipertensi, obesitas, indikasi diabetes) dan tiga entri daftar pantau yang tertaut ke penandanya | Sesuai: 3 baris `penanda_risiko` aktif dan 3 baris `daftar_pantau` bertaut penanda untuk pasien kunjungan 2 | Pass |
| INT-TC14 | IK-07 | Gagal: kegagalan di langkah terakhir membatalkan seluruh transaksi | Pemicu sementara pada tabel `audit` menolak baris audit skrining, yang ditulis setelah skrining, penanda, dan entri pantau selesai. `simpanSkrining(2, "Lemas", ...)` dengan nilai berisiko | Pengecualian dilempar. Tidak ada baris `skrining`, `penanda_risiko`, dan `daftar_pantau` baru, status kunjungan 2 tetap "menunggu skrining", dan `audit` tidak bertambah | Sesuai: seluruh pembacaan ulang menunjukkan 0 baris baru dan status tetap "menunggu skrining". Pemicu dibuang kembali sesudah test | Pass |
| INT-TC15 | IK-08 | Normal: tindak lanjut terhubung tersimpan lintas boundary | Sesi Perawat. `PantauManager.catatTindakLanjut(2, telepon, terhubung, akan datang)`, lalu `daftar_pantau`, `tindak_lanjut`, dan `catatan_rekam_medis` dibaca ulang | Status entri 2 menjadi "selesai ditindaklanjuti", satu baris `tindak_lanjut` berhasil "terhubung", dan satu catatan rekam medis baru berawalan "Tindak lanjut telepon: terhubung" | Sesuai: status "selesai ditindaklanjuti", hasil upaya "terhubung", dan catatan rekam medis bertambah satu | Pass |
| INT-TC16 | IK-08 | Gagal: kegagalan audit membatalkan tindak lanjut | Pemicu sementara menolak baris audit tindak lanjut. `catatTindakLanjut(2, telepon, terhubung, akan datang)` | Pengecualian dilempar. Status entri 2 tetap "belum ditindaklanjuti", dan baris `tindak_lanjut` serta catatan rekam medis tidak bertambah | Sesuai: status tetap, jumlah `tindak_lanjut` dan `catatan_rekam_medis` sama dengan sebelumnya | Pass |
| INT-TC17 | IK-09 | Normal: ringkasan rekam medis dan penyimpanan pemeriksaan | Kunjungan seed "siap diperiksa dokter" (id 9). Panggil `PemeriksaanManager.susunRingkasan(9)`, lalu `simpanPemeriksaan(9, anamnesis "Pusing dua hari", tindakan "Edukasi diet garam", diagnosis I10)`. Bandingkan dengan isi tabel `pasien`, `skrining`, `penanda_risiko`, `pemeriksaan`, dan `diagnosis` | Nama pasien, keluhan skrining, dan jumlah penanda aktif sama dengan isi tabel. Baris pemeriksaan tersimpan utuh dengan diagnosis I10 dan belum terkunci | Nama pasien, keluhan awal, dan jumlah penanda aktif sama dengan basis data. Kueri gabungan `pemeriksaan` dan `diagnosis` mengembalikan `Pusing dua hari\|Edukasi diet garam\|false\|I10` | Pass |
| INT-TC18 | IK-09 | Gagal: kode diagnosis di luar data master ICD-10 | `simpanPemeriksaan(9, ..., diagnosis "XYZ")` dengan sesi Dokter | `DataKlinisBoundary` menolak dengan `IllegalArgumentException`. Transaksi dibatalkan, sehingga tidak ada baris `pemeriksaan` maupun `audit` baru | Pengecualian dilempar. Jumlah baris `pemeriksaan` kunjungan 9 tetap 0 dan tidak ada audit baru | Pass |
| INT-TC19 | IK-10 | Normal: penguncian rekam medis tanpa resep | Siapkan jadwal kontrol pasien kunjungan 9 bertanggal hari ini beserta entri daftar pantau "kunjungan ulang"-nya, simpan draf berdiagnosis I10, lalu panggil `kunciRekamMedis(9)` | Dalam satu transaksi: pemeriksaan terkunci, status kunjungan "selesai", bundel FHIR "tertunda" berisi resource `Condition` berkode I10, jadwal kontrol "selesai", entri pantau "selesai ditindaklanjuti", dan audit "Pemeriksaan kunjungan 9 dikunci" | Keenam perubahan terbaca dari tabel `pemeriksaan`, `kunjungan`, `bundel_fhir` (`tertunda\|I10`), `jadwal_kontrol`, `daftar_pantau`, dan `audit` | Pass |
| INT-TC20 | IK-10 | Gagal: kunjungan sudah ditutup saat dokter mengunci | Simpan draf berdiagnosis, ubah status kunjungan 9 menjadi "selesai", lalu panggil `kunciRekamMedis(9)` | `IllegalStateException` dari `KunjunganEntity.perbaruiStatus()`. Seluruh transaksi di-*rollback*: pemeriksaan tetap belum terkunci, tidak ada bundel, dan tidak ada audit penguncian | Pengecualian dilempar. Pemeriksaan tidak terkunci, `bundel_fhir` kunjungan 9 kosong, dan jumlah audit tidak bertambah | Pass |
| INT-TC21 | IK-11 | Normal: resep dikirim ke apotek | `tambahItem(3, 30, "5 mg", "1 x 1 pagi")` dan `tambahItem(1, 10, "500 mg", "3 x 1 bila demam")`, lalu `kirimKeApotek(9, items)`. Setelah itu `ApotekManager.muatAntreanResep()` | Resep "menunggu" untuk kunjungan 9, dua baris `item_resep` dengan jumlah, dosis, dan aturan pakai yang sama, entri terakhir antrean apotek adalah resep ini, dan resep terbaca oleh `ApotekManager` | Resep `menunggu\|9`, item `1:10:500 mg:3 x 1 bila demam,3:30:5 mg:1 x 1 pagi`, entri antrean terakhir sama dengan ID resep, dan resep ada pada antrean `ApotekManager` | Pass |
| INT-TC22 | IK-11 | Gagal: dua dokter mengirim resep bersamaan untuk kunjungan yang sama | Dua *thread* memanggil `kirimKeApotek(9, Parasetamol x10)` pada saat yang sama | Penguncian baris antrean apotek (`FOR UPDATE`, Q-C58-09) membuat kedua transaksi bergantian. Satu berhasil, satu ditolak dengan pesan "Kunjungan ini masih memiliki resep yang menunggu di apotek...". Hanya satu resep dan satu entri antrean tersimpan | Satu berhasil dan satu ditolak dengan pesan tersebut. Satu baris `resep` dan satu entri antrean baru | Pass |
| INT-TC23 | IK-12 | Normal: resep dari antrean diserahkan sampai bundel tersedia untuk sinkronisasi | Sesi Petugas Farmasi. `ApotekManager.muatAntreanResep()`, lalu `layaniResep(4)`, `muatBetsResep()`, dan `serahkanObat(4)` pada resep seed Siti Aminah (Metformin × 60, Glibenklamid × 30). Hasil dibaca ulang dari tabel lewat koneksi terpisah, lalu lewat `SinkronisasiManager.muatStatus()` | Bets diambil MET-A01 lalu GLB-A01. Dalam satu transaksi: stok dan bets kedua obat berkurang, resep "selesai dilayani", kunjungan "selesai", resep keluar dari antrean apotek, bundel "tertunda" berisi NIK pasien dan dua `MedicationRequest`, dan audit oleh pengguna 4. Bundel itu terbaca `SinkronisasiManager` sebagai tertunda | Stok `190\|0` dan bets `190\|0` (Metformin\|Glibenklamid), `selesai dilayani\|selesai`, 0 entri antrean untuk resep 4, bundel `tertunda\|2\|9999010101750002`, bundel kunjungan 5 ada pada daftar `SinkronisasiManager`, audit `4\|Obat resep 4 diserahkan` | Pass |
| INT-TC24 | IK-12 | Gagal: stok Glibenklamid dipakai komputer lain sebelum penyerahan | Koneksi lain mengurangi stok Glibenklamid dan bets GLB-A01 sebanyak 10 (sisa 20 dari 30 yang diminta resep), lalu `serahkanObat(4)` | `DataFarmasiBoundary` mengembalikan stok terkini dari basis data, sehingga `ApotekManager` menolak "Stok Glibenklamid 5 mg tidak cukup" setelah Metformin sempat dipotong di dalam transaksi. Seluruh transaksi di-*rollback*: stok dan bets seluruh obat, status resep dan kunjungan, jumlah bundel, antrean apotek, dan audit tetap | `IllegalStateException` dengan pesan tersebut. Daftar stok dan bets seluruh obat sama dengan sebelum pemanggilan, `menunggu\|selesai diperiksa`, jumlah bundel tetap, resep 4 masih di antrean apotek, 0 audit baru | Pass |
| INT-TC25 | IK-13 | Normal: penerimaan dua bets sampai ke stok, kartu inventaris, dan apotek | Sesi Petugas Farmasi. `PersediaanManager.catatPenerimaan()` untuk Glibenklamid (stok 30) dengan faktur "FKT-INT-25", bets GLB-INT-2 × 30 (kedaluwarsa 2 tahun lagi) dan GLB-INT-1 × 40 (400 hari lagi). `terbitkanPeringatan()` dipanggil sebelum dan sesudah, lalu `ApotekManager.muatBetsResep()` untuk resep 4 | Stok menjadi 100, penerimaan dan kedua bets tersimpan bertaut ID penerimaan, saldo sama dengan jumlah bets, dan audit oleh pengguna 4. Peringatan stok Glibenklamid hilang, dan bets baru terbaca apotek dalam urutan FEFO | Stok 100, `FKT-INT-25\|2026-11-20`, bets `GLB-INT-1:40,GLB-INT-2:30`, saldo sama dengan jumlah bets, audit `4\|Obat masuk Glibenklamid 5 mg sejumlah 70, faktur FKT-INT-25`. Peringatan "Stok Glibenklamid..." ada sebelum dan hilang sesudah. Bets apotek MET-A01, GLB-A01, GLB-INT-1, GLB-INT-2 | Pass |
| INT-TC26 | IK-13 | Gagal: bets kedua bernomor ganda di tengah penyimpanan | `catatPenerimaan()` dengan faktur "FKT-INT-26", bets GLB-INT-3 × 40, lalu GLB-A01 × 30 (sudah tercatat) | *Batch* sisipan bets ditolak kunci primer `(id_obat, nomor_bets)` dan `DataFarmasiBoundary` menerjemahkannya menjadi `IllegalArgumentException` "nomorBets". Seluruh transaksi di-*rollback*: faktur tidak tersimpan, bets pertama tidak tersisa, stok tetap 30, tanpa audit | Pengecualian dan pesan sesuai, `idPenerimaan` 0, jumlah `penerimaan_obat` tetap, 0 baris faktur "FKT-INT-26" maupun bets GLB-INT-3, GLB-A01 tetap 30, stok 30, 0 audit baru | Pass |
| INT-TC27 | IK-14 | *[Diisi Fachry]* | | | | |
| INT-TC28 | IK-14 | *[Diisi Fachry]* | | | | |
| INT-TC29 | IK-15 | *[Diisi Fachry]* | | | | |
| INT-TC30 | IK-15 | *[Diisi Fachry]* | | | | |
| INT-TC31 | IK-16 | *[Diisi Fachry]* | | | | |
| INT-TC32 | IK-16 | *[Diisi Fachry]* | | | | |
| INT-TC33 | IK-17 | *[Diisi Steven]* | | | | |
| INT-TC34 | IK-17 | *[Diisi Steven]* | | | | |
| INT-TC35 | IK-18 | *[Diisi Steven]* | | | | |
| INT-TC36 | IK-18 | *[Diisi Steven]* | | | | |

<br>

## 4.4 Pengujian Use Case

Pengujian *use case* dilakukan pada seluruh skenario normal dan alternatif Subbab 4.4 SKPL, dengan minimal satu *test case* untuk setiap skenario. Pengujian dijalankan pada aplikasi (`mvn javafx:run`) yang terhubung ke basis data uji, memakai akun seed dengan kata sandi `sehati123`: `admin` (Petugas Administrasi), `perawat`, `dokter`, dan `farmasi` (Petugas Farmasi). Kolom ID KF menunjukkan KF yang diverifikasi oleh *test case* tersebut.

### 4.4.1 Use Case Masuk ke Aplikasi

**ID Use Case:** UC01  
**Nama Use Case:** Masuk ke Aplikasi

UC01 dan UC12 diuji pada 19 November 2026 di aplikasi yang dijalankan dari kode setelah perbaikan Tabel 3.5. Pengujian memakai skrip yang membuka `Navigasi` dan layar FXML asli, mengisi kolom dan menekan tombol lewat kontrol JavaFX, lalu menyimpan *snapshot* jendela 1280 × 800 piksel. Isi basis data setelah pengujian diperiksa lewat `psql`.

Tabel 4.4.1. Hasil Pengujian Use Case UC01

| ID Pengujian | ID KF | Skenario | Prosedur Pengujian | Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UC01-TC01 | KF16 | Normal | 1. Jalankan aplikasi.<br>2. Isi nama pengguna dan kata sandi, tekan "Masuk".<br>3. Tekan "Keluar", lalu masuk sebagai perawat. | `admin` / `sehati123`, lalu `perawat` / `sehati123` | Layar autentikasi berisi kolom nama pengguna dan kata sandi. Setelah masuk, sesi dibuka, peristiwa masuk tercatat di jejak audit, dan dasbor hanya memuat menu peran akun tersebut | Layar "Masuk" tampil dengan kolom "Nama pengguna" dan "Kata sandi". Dasbor admin menampilkan "Selamat datang, admin", "Masuk sebagai Petugas Administrasi", informasi pencadangan terakhir, dan empat kartu: Pendaftaran Pasien, Laporan Periodik, Sinkronisasi SATUSEHAT, serta Konfigurasi dan Basis Data. Dasbor perawat hanya memuat Antrean Skrining, Rekam Medis Pasien, dan Daftar Pantau Pasien. Basis data: baris `sesi` pengguna 1 dan 2, audit 379 dan 381 "Masuk ke aplikasi" | Pass |
| UC01-TC02 | KF16 | Alternatif 1: kredensial tidak cocok | 1. Isi kata sandi yang salah, tekan "Masuk".<br>2. Isi ulang kredensial yang benar. | `admin` / `salah123`, lalu `admin` / `sehati123` | Pesan "Nama pengguna atau kata sandi salah", kolom kata sandi dikosongkan, percobaan gagal tercatat di jejak audit, lalu kembali ke langkah 2 skenario normal | Pesan merah "Nama pengguna atau kata sandi salah." tampil di bawah kolom kata sandi dan kolom kata sandi kosong (isinya terbaca ""). Basis data: audit 383 "Masuk gagal, percobaan ke-1" berpelaku pengguna 1 tanpa baris `sesi` baru. Kredensial benar membuka dasbor admin (audit 384) | Pass |
| UC01-TC03 | KF16 | Alternatif 2: akun nonaktif | Masuk dengan akun seed yang berstatus nonaktif | `perawat.nonaktif` / `sehati123` | Permintaan masuk ditolak, pengguna diberi tahu akunnya nonaktif dan diminta menghubungi Petugas Administrasi | Pesan merah "Akun nonaktif, hubungi Petugas Administrasi" tampil, layar tetap di "Masuk", kolom kata sandi kosong, dan tidak ada baris `sesi` untuk akun itu | Pass |
| UC01-TC04 | KF16 | Alternatif 3: membuka modul di luar hak akses | 1. Masuk sebagai perawat dan periksa menu dasbor.<br>2. Kirim perintah buka layar Formulir Pemeriksaan (milik dokter). Karena dasbor perawat tidak punya menunya, perintah ini dikirim skrip uji lewat `LoginForm.bukaLayar("PemeriksaanForm")`, yaitu penangan yang sama dengan kartu menu. | Akun `perawat`; layar `PemeriksaanForm` | Menu di luar wewenang tidak tampil, dan perintah akses langsung ditolak dengan keterangan bahwa peran itu tidak berwenang | Dasbor perawat tidak memuat Formulir Pemeriksaan maupun Konfigurasi. Perintah buka ditolak dengan pesan merah "Akses ditolak: Peran Perawat tidak berwenang membuka layar ini" dan layar tetap di dasbor | Pass |

### 4.4.2 Use Case Mengelola Data Pasien

**ID Use Case:** UC02  
**Nama Use Case:** Mengelola Data Pasien

*[Diisi Steven: UC02-TC01 sampai UC02-TC03 untuk skenario normal dan alternatif 1 sampai 2.]*

### 4.4.3 Use Case Mengelola Kunjungan Pasien

**ID Use Case:** UC03  
**Nama Use Case:** Mengelola Kunjungan Pasien

*[Diisi Steven: UC03-TC01 sampai UC03-TC04 untuk skenario normal dan alternatif 1 sampai 3.]*

### 4.4.4 Use Case Melakukan Skrining Awal

**ID Use Case:** UC04  
**Nama Use Case:** Melakukan Skrining Awal

UC04 dan UC09 diuji pada 20 November 2026 di aplikasi yang dijalankan dari kode `main` dengan PostgreSQL lokal (port 5433, `schema.sql` dan `seed.sql` dari repositori). Pengujian memakai skrip `src/tests/sehati/PemanduLayarSkrining.java` yang membuka `Navigasi` dan layar FXML asli dengan akun `perawat`, mengirim peristiwa papan ketik (F2, F3, Tab, Ctrl+S) atau menekan tombol lewat kontrol JavaFX, lalu menyimpan *snapshot* jendela 1280 × 800 piksel. Isi basis data dibaca lewat koneksi terpisah sesudah tiap skenario (catatan skrip ada pada Tabel 4.6), dan seed dipulihkan sesudahnya. Isian teks diketik lewat peristiwa papan ketik.

Tabel 4.4.4. Hasil Pengujian Use Case UC04

| ID Pengujian | ID KF | Skenario | Prosedur Pengujian | Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UC04-TC01 | KF04, KF05, KF24 | Normal | 1. Masuk sebagai perawat dan buka layar Antrean Skrining.<br>2. Tekan F2 (Panggil Berikutnya).<br>3. Isi formulir hanya dengan papan ketik, berpindah kolom dengan Tab.<br>4. Tekan Ctrl+S. | Keluhan "Pusing ringan dua hari"; sistolik 118, diastolik 78, berat 62 kg, tinggi 165 cm, suhu 36,6 °C, nadi 70, gula darah 98 | Daftar pasien menunggu terurut menurut nomor antrean. Pasien terdepan berstatus "sedang diskrining" dan formulir terbuka. Fokus berpindah antar-kolom tanpa tetikus. Setelah simpan, skrining tersimpan, status menjadi "siap diperiksa dokter", dan pasien keluar dari antrean | Antrean menampilkan tiga pasien (Rina Kartika antrean 3, Agus Pratama antrean 4, Dewi Lestari antrean 1). F2 membuka formulir Rina Kartika (RM-000003) dengan status "sedang diskrining". Seluruh kolom terisi dengan Tab. Setelah Ctrl+S tampil "Skrining tersimpan, pasien diteruskan ke antrean poli" tanpa penanda risiko. Basis data: status kunjungan 3 "siap diperiksa dokter", satu baris skrining, 0 penanda, antrean tinggal 2 dan 11 | Pass |
| UC04-TC02 | KF04 | Alternatif 1: pasien tidak hadir saat dipanggil | 1. Tekan F2 untuk memanggil pasien terdepan, lalu kembali ke layar antrean (Esc).<br>2. Tekan F3 (Lewati: Tidak Hadir). | Pasien terdepan (kunjungan 3) | Status pasien menjadi "tidak hadir", nomor antreannya pindah ke urutan terbawah, dan pasien berikutnya menjadi terdepan | Sebelum F3 baris Rina Kartika berstatus "sedang diskrining". Sesudah F3 daftar menjadi Agus Pratama, Dewi Lestari, lalu Rina Kartika berstatus "tidak hadir" di baris terakhir, dengan ringkasan "Menunggu skrining: 2 pasien". Basis data: status kunjungan 3 "tidak hadir", urutan antrean 2, 11, 3 | Pass |
| UC04-TC03 | KF06 | Alternatif 2: nilai di luar rentang fisiologis | 1. Panggil pasien (F2) dan isi formulir dengan nilai salah.<br>2. Tekan Ctrl+S.<br>3. Koreksi kedua nilai, lalu tekan Ctrl+S lagi. | Sistolik 400 dan suhu 72 °C, lalu dikoreksi menjadi 120 dan 36,6 °C | Penyimpanan ditolak, kolom bermasalah bertanda merah dengan pesan rentang, dan isian lain tetap. Setelah koreksi, penyimpanan berhasil | Kolom Sistolik dan Suhu bertanda merah dengan pesan "Rentang wajar 50 sampai 300 mmHg" dan "Rentang wajar 30 sampai 45 °C". Kolom lain dan keluhan tetap terisi. Basis data: 0 baris skrining baru dan status kunjungan 3 tetap "sedang diskrining". Setelah koreksi dan Ctrl+S, status menjadi "siap diperiksa dokter" dengan satu baris skrining | Pass |
| UC04-TC04 | KF07 | Alternatif 3: hasil pengukuran melampaui ambang risiko | 1. Panggil pasien (F2) dan isi formulir dengan nilai di atas ambang.<br>2. Tekan Ctrl+S. | Sistolik 152, diastolik 96, berat 90 kg, tinggi 165 cm (IMT 33,1), suhu 36,8 °C, nadi 82, gula darah 210 | Kunjungan ditandai label risiko yang sesuai, peringatan ikon dan warna merah tampil, pasien masuk daftar pantau, dan diteruskan ke antrean poli | Tiga peringatan merah berikon tampil: "Risiko hipertensi terdeteksi", "Risiko indikasi diabetes terdeteksi", dan "Risiko obesitas terdeteksi". Basis data: tiga penanda aktif (hipertensi, indikasi diabetes, obesitas), tiga entri daftar pantau baru, status kunjungan "siap diperiksa dokter" | Pass |

### 4.4.5 Use Case Melakukan Pemeriksaan Medis

**ID Use Case:** UC05  
**Nama Use Case:** Melakukan Pemeriksaan Medis

Skenario normal UC05 memiliki lima langkah dengan hasil yang berbeda-beda, sehingga dipecah menjadi tiga *test case*: ringkasan rekam medis (langkah 1), grafik tren (langkah 2), serta diagnosis, kontrol, dan penguncian (langkah 3 sampai 5). Dengan begitu kegagalan satu langkah tidak menutupi langkah lain. UC05-TC08 adalah kasus tambahan pada langkah 4 setelah C22-UT37 menemukan cacat jadwal kontrol. Kecuali UC05-TC06 dan UC05-TC07, seluruh *test case* memakai kunjungan seed "Antrean 1 - Budi Santoso | RM-000001" (kunjungan 9, pasien 58 tahun dengan empat pengukuran dan penanda hipertensi aktif).

Tabel 4.4.5. Hasil Pengujian Use Case UC05

| ID Pengujian | ID KF | Skenario | Prosedur Pengujian | Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UC05-TC01 | KF09 | Normal, langkah 1: ringkasan rekam medis | 1. Masuk sebagai dokter.<br>2. Pilih kartu "Rekam Medis Pasien" pada dasbor.<br>3. Pilih pasien pada daftar "Pasien siap diperiksa hari ini". | Akun `dokter`; pasien Budi Santoso (kunjungan 9) | Satu layar memuat data diri, hasil skrining hari itu, daftar diagnosis lampau, riwayat obat, dan penanda risiko aktif (SKPL 4.4.5 langkah 1) | Satu layar menampilkan data diri (Budi Santoso, 58 tahun, NIK, RM-000001, alamat), penanda "Risiko hipertensi sejak 18-11-2026" berwarna merah, tiga diagnosis lampau I10, dan tiga riwayat Amlodipin. **Hasil skrining hari itu (keluhan awal dan tanda vital 18-11-2026) tidak ditampilkan**; nilainya hanya muncul sebagai titik terakhir pada grafik tren UC05-TC02. Uji ulang setelah perbaikan: panel "Skrining hari ini" tampil di bawah data diri dengan keluhan "Pusing dan tengkuk terasa berat", tekanan darah 152/96 mmHg, berat 84.0 kg, tinggi 168.0 cm, IMT 29.8, suhu 36.8 °C, nadi 88 bpm, dan gula darah "-" | Fail pada `v9.0`, Pass setelah perbaikan. Penyebab: `PemeriksaanManager.susunRingkasan()` sudah memuat `skriningHariIni` (C22-UT01), tetapi `RekamMedisForm.isiRingkasan()` tidak menampilkannya, dan rancangan I-06 (DPPL Subbab 6.6) memang tidak memiliki panel skrining. Perbaikan: panel "Skrining hari ini" pada I-06 diisi dari `RekamMedisEntity.getSkriningHariIni()` (Tabel 3.5 nomor 2) |
| UC05-TC02 | KF08 | Normal, langkah 2: grafik tren tanda vital | 1. Lanjutan UC05-TC01.<br>2. Tekan tab "Lihat Tren Tanda Vital". | Pasien dengan empat pengukuran (20-08, 19-09, 19-10, dan 18-11-2026) | Grafik garis tekanan darah, berat badan, dan gula darah antar-kunjungan dengan waktu pada sumbu-x | Grafik garis "Tren tanda vital" tampil dengan empat tanggal pada sumbu-x (20-08-26 sampai 18-11-26) dan seri Sistolik, Diastolik, Berat badan, serta Gula darah. Gula darah hanya satu titik karena hanya diukur pada 19-10-2026 | Pass |
| UC05-TC03 | KF10, KF12, KF24 | Normal, langkah 3 sampai 5: diagnosis ICD-10, jadwal kontrol, dan penguncian | 1. Lanjutan UC05-TC02, tekan "Periksa Pasien".<br>2. Isi anamnesis, ketik "hipertensi" pada kolom diagnosis, lalu pilih I10 dengan Enter.<br>3. Isi tindakan dan jadwal kontrol, lalu tekan Ctrl+S.<br>4. Susun resep lewat "Buat Resep" (UC06-TC01), lalu kembali.<br>5. Tekan "Simpan Final (Kunci)" dan pilih OK.<br>6. Masuk sebagai perawat dan buka "Daftar Pantau Pasien". | Anamnesis "Pusing dan tengkuk terasa berat sejak 3 hari"; diagnosis "hipertensi" → I10; tindakan "Edukasi diet rendah garam dan aktivitas fisik"; jadwal kontrol 09-12-2026 | Kode dan deskripsi ICD-10 tampil selagi diketik. Diagnosis tersimpan sebagai kode standar dan rencana kontrol tertaut ke daftar pantau. Setelah dikunci, rekam medis tidak dapat disunting, transaksi tercatat di jejak audit, dan status kunjungan berubah | Saran "I10 Hipertensi esensial (primer)" muncul. Ctrl+S menampilkan "Kontrol dijadwalkan 09-12-2026". Setelah OK pada dialog konfirmasi, pesan "Rekam medis kunjungan ini sudah dikunci" tampil dan seluruh kolom serta tombol simpan nonaktif. Daftar pantau perawat memuat entri "kunjungan ulang" Budi Santoso bertanggal 9 Des 2026. Basis data: pemeriksaan kunjungan 9 terkunci dengan diagnosis I10, status kunjungan "selesai diperiksa" (resep masih menunggu di apotek), dan audit "Pemeriksaan kunjungan 9 disimpan" serta "dikunci" oleh pengguna 3 (dokter) | Pass |
| UC05-TC04 | KF10 | Alternatif 1: pengguna bukan dokter | 1. Masuk sebagai perawat.<br>2. Buka "Rekam Medis Pasien" dan pilih pasien. | Akun `perawat`; kunjungan 9 | Rekam medis tampil dalam mode baca saja, kolom diagnosis dan tindakan terkunci, disertai keterangan batas wewenang | Ringkasan tetap tampil dengan lencana "Mode baca saja: Peran Anda tidak berwenang mengisi diagnosis". Tombol "Periksa Pasien" dan "Buat Resep" nonaktif, sehingga formulir diagnosis dan tindakan tidak dapat dibuka | Pass |
| UC05-TC05 | KF10 | Alternatif 2: kode ICD-10 tidak ditemukan | 1. Pada formulir pemeriksaan, ketik kata kunci yang tidak cocok.<br>2. Ganti dengan istilah yang tepat. | "xyzzy", lalu "hipertensi" | Pesan "Kode diagnosis ICD-10 tidak ditemukan", lalu kembali ke langkah 4 skenario normal setelah istilah diganti | Pesan merah "Kode diagnosis ICD-10 tidak ditemukan" tampil dan diagnosis terpilih tetap "belum ada". Setelah diganti "hipertensi", daftar saran muncul kembali dan I10 dapat dipilih | Pass |
| UC05-TC06 | KF22 | Alternatif 3: pemeriksaan saat internet terputus | 1. Jalankan aplikasi dengan alamat SATUSEHAT yang tidak dapat dijangkau (`SATUSEHAT_URL=http://127.0.0.1:9`) sebagai simulasi internet padam. LAN ke basis data tetap menyala.<br>2. Masuk sebagai dokter, buka rekam medis, grafik tren, dan formulir pemeriksaan.<br>3. Pilih diagnosis lalu tekan Ctrl+S. | Akun `dokter`; pasien Dewi Lestari (kunjungan 11); anamnesis "Demam dan batuk berdahak sejak kemarin"; diagnosis "nasofaringitis" → J00 | Penelusuran rekam medis, tampilan tren, dan penyimpanan diagnosis tetap berjalan lewat basis data lokal, dan bilah aplikasi menampilkan "Luring" | Bilah aplikasi menampilkan indikator kuning "Luring". Ringkasan, tab tren, dan formulir pemeriksaan terbuka. Ctrl+S menampilkan "Pemeriksaan tersimpan", dan basis data berisi pemeriksaan kunjungan 11 dengan diagnosis J00 | Pass |
| UC05-TC07 | KF08 | Alternatif 4: pasien baru pertama kali berkunjung | 1. Prasyarat: pasien seed Dewi Lestari (RM-000005, belum pernah diukur) diskrining hari ini lewat `SkriningManager.simpanSkrining()` dengan sesi perawat.<br>2. Dokter membuka rekam medisnya dan menekan "Lihat Tren Tanda Vital". | Tanda vital 118/76 mmHg, 52 kg, 158 cm, 37,8 °C, nadi 92, tanpa gula darah | Nilai pengukuran hari itu tampil dalam bentuk tabel, disertai keterangan bahwa grafik tren baru terbentuk setelah minimal dua kunjungan | Grafik tidak ditampilkan. Tabel memuat tekanan darah 118/76 mmHg, berat 52.0 kg, tinggi 158.0 cm, IMT 20.8, suhu 37.8 °C, nadi 92 bpm, dan gula darah "-", disertai keterangan "Grafik tren terbentuk setelah pasien diukur pada dua kunjungan atau lebih." | Pass |
| UC05-TC08 | KF12 | Normal, langkah 4 (kasus tambahan): tanggal kontrol yang sudah terjadwal | 1. Perawat membuka "Daftar Pantau Pasien" sehingga jadwal kontrol seed Budi Santoso (2 Des 2026) masuk daftar pantau.<br>2. Dokter mengisi jadwal kontrol 02-12-2026 pada formulir pemeriksaan kunjungan 9 lalu menekan Ctrl+S.<br>3. Perawat membuka daftar pantau lagi. | Jadwal kontrol 02-12-2026 (sudah ada di seed dan sudah tertaut ke daftar pantau) | Jadwal pasien yang sama pada tanggal yang sama tidak dibuat dua kali (Tabel B.1 nomor 53), sehingga daftar pantau tetap memuat satu entri untuk 2 Des 2026 | Pesan "Kontrol dijadwalkan 02-12-2026" tampil, tetapi daftar pantau kini memuat **dua** entri "kunjungan ulang" Budi Santoso bertanggal 2 Des 2026. Basis data berisi jadwal 1 dan jadwal 4 untuk pasien 1 pada 2026-12-02, masing-masing dengan entri daftar pantau (entri 4 dan 5). Uji ulang setelah perbaikan dengan langkah yang sama: daftar pantau tetap memuat satu entri 2 Des 2026 (dan satu entri 9 Des 2026 dari UC05-TC03), dan pasien 1 hanya punya jadwal 1 (2026-12-02) dan jadwal 3 (2026-12-09) | Fail pada `v9.0`, Pass setelah perbaikan. Penyebab sama dengan C22-UT37: `jadwalkanKontrol()` mencari jadwal lama lewat `ambilJadwalKontrol()` (Q-C59-17) yang hanya memuat jadwal yang belum tertaut ke daftar pantau. Perbaikan: pencarian per pasien dan tanggal lewat Q-C59-20 (Tabel 3.5 nomor 1) |

### 4.4.6 Use Case Menyusun Resep Elektronik

**ID Use Case:** UC06  
**Nama Use Case:** Menyusun Resep Elektronik

Kedua *test case* dijalankan pada kunjungan 9 setelah langkah 3 UC05-TC03, karena UC06 memperluas UC05. UC06-TC02 dijalankan lebih dulu, lalu item yang sudah diperbaiki dilanjutkan oleh UC06-TC01.

Tabel 4.4.6. Hasil Pengujian Use Case UC06

| ID Pengujian | ID KF | Skenario | Prosedur Pengujian | Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UC06-TC01 | KF11 | Normal | 1. Pada formulir pemeriksaan, tekan "Buat Resep".<br>2. Ketik nama obat, isi jumlah, dosis, dan aturan pakai, lalu tekan "Tambah" untuk setiap obat.<br>3. Tekan "Kirim ke Apotek". | Glibenklamid 5 mg × 30, "1 x 1 pagi sebelum makan" (hasil perbaikan UC06-TC02); Parasetamol 500 mg × 10, "3 x 1 bila demam" | Formulir tersambung ke data master obat. Indikator hijau "Tersedia" tampil, resep diperiksa ulang, lalu diteruskan ke antrean apotek | Pilihan obat memuat nama, sediaan, dan stok dari data master. Kedua item berlencana hijau "Tersedia" dan tombol "Kirim ke Apotek" aktif. Setelah dikirim, notifikasi "Resep masuk antrean apotek" tampil dan tabel item dikosongkan. Basis data: resep 10 berstatus "menunggu" dengan dua item tersebut dan masuk urutan ke-2 antrean apotek | Pass |
| UC06-TC02 | KF11 | Alternatif 1: jumlah melampaui stok | 1. Tambahkan obat dengan jumlah melebihi sisa stok.<br>2. Pilih item, tekan "Hapus Item", lalu tambahkan lagi dengan jumlah yang memadai. | Glibenklamid 5 mg (stok 30) × 40, lalu × 30 | Penerusan resep ditahan, indikator merah "Stok Kurang", dan peringatan berisi sisa stok. Setelah diperbaiki kembali ke langkah 3 skenario normal | Lencana merah "Stok Kurang (sisa 30)" tampil pada isian dan tabel, peringatan merah "Stok hanya tersisa 30, perbaiki resep" muncul, dan tombol "Kirim ke Apotek" nonaktif. Setelah item diganti jumlah 30, lencana menjadi hijau "Tersedia", peringatan hilang, dan tombol kirim aktif kembali | Pass |

### 4.4.7 Use Case Melayani Resep Obat

**ID Use Case:** UC07  
**Nama Use Case:** Melayani Resep Obat

UC07 dan UC08 diuji pada 20 November 2026 di aplikasi yang dijalankan dari kode setelah perbaikan Tabel 3.5. Kode modul farmasi (C09 sampai C11, C24, C25, C48 sampai C50, dan C61) tidak berubah sejak `v9.0`. Basis data uji dibuat ulang dari `schema.sql` dan `seed.sql` pada hari yang sama, sehingga "hari ini" pada hasil pengujian adalah 20-11-2026. Pengujian memakai skrip yang membuka `Navigasi` dan layar FXML asli, mengisi kolom dan menekan tombol lewat kontrol JavaFX, lalu menyimpan *snapshot* jendela 1280 × 800 piksel, sama seperti UC01. Isi basis data setelah pengujian diperiksa lewat `psql`.

Prasyarat UC07: antrean apotek seed hanya berisi resep Siti Aminah, jadi dokter lebih dulu memeriksa Budi Santoso (kunjungan 9), mengirim resep Parasetamol 500 mg × 10 dan Amlodipin 5 mg × 30, lalu mengunci rekam medis lewat `PemeriksaanManager` dan `ResepManager` dengan sesi dokter. UC07-TC03 memakai resep revisi yang dokter terbitkan setelah UC07-TC02 (Parasetamol 500 mg × 10 dan Kaptopril 25 mg × 30), dan dijalankan dengan dua proses aplikasi seperti UC12-TC04.

Tabel 4.4.7. Hasil Pengujian Use Case UC07

| ID Pengujian | ID KF | Skenario | Prosedur Pengujian | Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UC07-TC01 | KF13, KF14 | Normal | 1. Masuk sebagai Petugas Farmasi dan pilih kartu "Antrean Resep Apotek".<br>2. Tekan "Layani" pada resep Siti Aminah.<br>3. Tekan "Obat Diserahkan (Ctrl+Enter)". | Akun `farmasi`; resep 4 Siti Aminah (Metformin 500 mg × 60, Glibenklamid 5 mg × 30) | Antrean terurut waktu masuk lengkap dengan nama pasien, nomor antrean, poli asal, serta nama obat, dosis, dan jumlah. Rincian resep tampil beserta nomor bets. Setelah penyerahan dikonfirmasi, status resep menjadi "selesai dilayani", stok setiap obat terpotong, kunjungan ditutup, dan bundel HL7 FHIR masuk antrean sinkronisasi | Antrean "Resep menunggu: 2": 08:45 Siti Aminah, "Poli Umum / Antrean 2", "Metformin 500 mg 2x1 x60" dan "Glibenklamid 5 mg 1x1 x30", lalu 13:36 Budi Santoso, "Poli Umum / Antrean 1", "Parasetamol 500 mg 500 mg x10" dan "Amlodipin 5 mg 5 mg x30", keduanya berstatus "menunggu" dengan tombol Layani. Layar Penyerahan Obat menampilkan "Pasien: Siti Aminah / RM-000002 / Resep masuk 08:45", kedua item beserta aturan pakainya, dan bets MET-A01 (kedaluwarsa 13-02-2028, sisa 250) serta GLB-A01 (20-11-2027, sisa 30). Setelah tombol ditekan, layar kembali ke antrean "Resep menunggu: 1" dengan notifikasi hijau "Obat diserahkan untuk Siti Aminah". Basis data: resep 4 "selesai dilayani", stok Metformin 190 dan Glibenklamid 0 (bets MET-A01 190 dan GLB-A01 0), kunjungan 5 "selesai", bundel kunjungan 5 "tertunda" dengan dua `MedicationRequest`, dan audit 5 "Obat resep 4 diserahkan" oleh pengguna 4 | Pass. Catatan: konfirmasi penyerahan adalah tombol "Obat Diserahkan" itu sendiri, sesuai rancangan I-10-05 dan Algo-C10-01 DPPL, tanpa dialog konfirmasi terpisah |
| UC07-TC02 | KF13 | Alternatif 1: sediaan obat bermasalah saat disiapkan | 1. Tekan "Layani" pada resep Budi Santoso.<br>2. Tekan "Kembalikan ke Dokter", lalu "Kirim Pengembalian" tanpa mengisi alasan.<br>3. Isi alasan, lalu tekan "Kirim Pengembalian".<br>4. Keluar, masuk sebagai dokter, buka "Rekam Medis Pasien", pilih pasien, lalu tekan "Buat Resep". | Resep 10 Budi Santoso; alasan "Strip Amlodipin 5 mg bets AML-A01 basah dan rusak, mohon ganti obat antihipertensi" | Kolom catatan yang wajib diisi terbuka. Setelah dikonfirmasi, resep kembali ke layar dokter pemeriksa, keluar dari antrean aktif apotek, dan penutupan kunjungan ditahan sampai dokter menerbitkan resep revisi | "Kembalikan ke Dokter" membuka kotak "Alasan pengembalian (wajib)". Kirim tanpa alasan membuat kolom berbingkai merah dengan keterangan "Alasan pengembalian wajib diisi", dan resep belum dikembalikan. Setelah alasan diisi dan dikirim, antrean menjadi "Resep menunggu: 0" ("Belum ada resep menunggu") dengan notifikasi "Resep Budi Santoso dikembalikan ke dokter". Di akun dokter, daftar "Pasien siap diperiksa hari ini" memuat "Antrean 1 - Budi Santoso \| RM-000001 (resep dikembalikan)", dan layar Resep Elektronik menampilkan "Resep sebelumnya dikembalikan apotek: Strip Amlodipin 5 mg bets AML-A01 basah dan rusak, mohon ganti obat antihipertensi". Basis data: resep 10 "dikembalikan" dengan catatan tersebut, stok Amlodipin tetap 190 (AML-A01 40, AML-B01 150), dan audit 6 "Resep 10 dikembalikan ke dokter: ...". Kunjungan 9 tidak ditutup apotek, karena daftar dokter (Q-C58-13) hanya memuat kunjungan "selesai diperiksa" yang resepnya dikembalikan dan belum diganti. Kunjungan baru menjadi "selesai" setelah resep revisi diserahkan pada UC07-TC03 | Pass |
| UC07-TC03 | KF14, KF23 | Alternatif 2: dua petugas mengonfirmasi penyerahan bersamaan | 1. Jalankan dua proses aplikasi (loket A dan loket B), keduanya masuk sebagai Petugas Farmasi.<br>2. Kedua loket membuka resep revisi Budi Santoso.<br>3. Kedua loket menekan "Obat Diserahkan" pada waktu yang sama (dijadwalkan skrip pada milidetik yang sama). | Akun `farmasi` di dua proses; resep 11 (Parasetamol 500 mg × 10, Kaptopril 25 mg × 30) | Konfirmasi pertama diproses secara atomik dan konfirmasi kedua ditolak dengan pesan bahwa resep sudah selesai dilayani, sehingga stok terpotong tepat satu kali | Kedua loket menekan tombol pada epoch 1795156631007 ms. Loket A kembali ke antrean dengan notifikasi "Obat diserahkan untuk Budi Santoso". Loket B tetap di layar Penyerahan Obat dengan panel merah berikon "Resep sudah selesai dilayani", tombol "Obat Diserahkan" dan "Kembalikan ke Dokter" nonaktif, dan tabel masih menampilkan rincian saat layar dibuka (PCT-A01 sisa 300, KAP-A01 sisa 120). Basis data: resep 11 "selesai dilayani", stok Parasetamol 300 menjadi 290 dan Kaptopril 120 menjadi 90 (masing-masing berkurang sekali), satu bundel kunjungan 9, kunjungan 9 "selesai", dan hanya satu audit "Obat resep 11 diserahkan" (audit 12) | Pass |

### 4.4.8 Use Case Mengelola Persediaan Obat

**ID Use Case:** UC08  
**Nama Use Case:** Mengelola Persediaan Obat

Ketiga *test case* dijalankan berurutan pada satu sesi Petugas Farmasi setelah UC07, sehingga stok Glibenklamid sudah habis (0) karena diserahkan pada UC07-TC01. UC08-TC02 dijalankan lebih dulu karena memeriksa peringatan saat layar baru dibuka.

Tabel 4.4.8. Hasil Pengujian Use Case UC08

| ID Pengujian | ID KF | Skenario | Prosedur Pengujian | Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UC08-TC01 | KF17 | Normal | 1. Pilih kartu "Persediaan Obat".<br>2. Pilih obat, isi nomor faktur, nomor bets, jumlah, dan tanggal kedaluwarsa (tanggal terima terisi hari ini).<br>3. Tekan "Simpan (Ctrl+S)". | Glibenklamid 5 mg; faktur "FKT-GFK-2026-0118"; bets "GLB-B02"; jumlah 100; kedaluwarsa 30-06-2028 | Formulir berisi pilihan obat dari data master, nomor bets, jumlah, dan tanggal kedaluwarsa. Setelah disimpan, kelengkapan diperiksa, stok bertambah, bets dan kedaluwarsa tercatat, jejak audit tercatat, dan saldo pada dasbor apotek termutakhirkan | Formulir "Penerimaan obat masuk" memuat Obat (pilihan dari data master), Nomor faktur, Tanggal terima (20-11-2026), Nomor bets, Jumlah, dan Tanggal kedaluwarsa. Setelah simpan, notifikasi hijau "Penerimaan obat tersimpan" dan panel "Saldo stok terbaru" berisi "Glibenklamid 5 mg tablet: 100 (ambang minimum 50)" tampil, formulir dikosongkan, dan peringatan "Stok Glibenklamid 5 mg tinggal 0" hilang dari panel peringatan. Basis data: penerimaan 3 "FKT-GFK-2026-0118" bertanggal 2026-11-20, bets GLB-B02 berjumlah 100 dengan kedaluwarsa 2028-06-30 bertaut penerimaan 3, stok Glibenklamid 100, dan audit 14 "Obat masuk Glibenklamid 5 mg sejumlah 100, faktur FKT-GFK-2026-0118" oleh pengguna 4 | Pass |
| UC08-TC02 | KF17 | Alternatif 1: stok menipis atau bets mendekati kedaluwarsa | Buka "Persediaan Obat" saat ada obat di bawah ambang dan bets yang kedaluwarsa dalam 90 hari atau kurang | Seed setelah UC07: Glibenklamid 0 dan Asam Mefenamat 0 (ambang 50), bets AML-A01 kedaluwarsa 19-01-2027 (60 hari lagi) | Peringatan persediaan merinci obat yang menipis dan bets yang mendekati kedaluwarsa | Panel "Peringatan persediaan" di bagian atas layar langsung menampilkan tiga peringatan merah berikon tanpa klik tambahan: "Bets AML-A01 (Amlodipin 5 mg) kedaluwarsa 19-01-2027", "Stok Asam Mefenamat 500 mg tinggal 0, di bawah ambang 50", dan "Stok Glibenklamid 5 mg tinggal 0, di bawah ambang 50". Bets AML-B01 (kedaluwarsa 03-04-2028) dan obat dengan stok di atas ambang tidak diperingatkan | Pass. Catatan: peringatan tampil pada panel I-11-02 layar Persediaan Obat, yang dirancang DPPL sebagai dasbor peringatan apotek. Dasbor menu setelah masuk tidak memuat peringatan |
| UC08-TC03 | KF17 | Alternatif 2: jumlah atau tanggal kedaluwarsa tidak valid | 1. Pilih obat, isi faktur dan nomor bets, isi jumlah 0 dan tanggal kedaluwarsa kemarin, lalu tekan "Simpan".<br>2. Perbaiki jumlah dan tanggal, lalu tekan "Simpan" lagi. | Kaptopril 25 mg; faktur "FKT-GFK-2026-0119"; bets "KAP-B02"; jumlah 0 dan kedaluwarsa 19-11-2026, lalu jumlah 200 dan kedaluwarsa 31-12-2028 | Pencatatan ditolak dengan pesan kesalahan pada kolom yang tidak memenuhi syarat. Setelah diperbaiki, kembali ke langkah 2 skenario normal | Kolom Jumlah dan Tanggal kedaluwarsa berbingkai merah dengan keterangan "Jumlah harus lebih dari nol" dan "Tanggal kedaluwarsa harus setelah tanggal terima dan hari ini". Isian lain tetap utuh dan tidak ada notifikasi tersimpan. Panel "Saldo stok terbaru" masih menampilkan saldo Glibenklamid dari UC08-TC01. Setelah diperbaiki dan disimpan, "Penerimaan obat tersimpan" tampil dengan saldo "Kaptopril 25 mg tablet: 290 (ambang minimum 50)". Basis data: hanya satu penerimaan berfaktur "FKT-GFK-2026-0119" (penerimaan 4) dengan bets KAP-B02 berjumlah 200 dan kedaluwarsa 2028-12-31, stok Kaptopril 290 (90 sisa UC07-TC03 ditambah 200), dan audit 15 | Pass |

### 4.4.9 Use Case Mengelola Daftar Pantau Pasien

**ID Use Case:** UC09  
**Nama Use Case:** Mengelola Daftar Pantau Pasien

UC09 diuji dengan skrip dan cara yang sama seperti UC04 (lihat Subbab 4.4.4). Penyaring dipilih lewat kombo dan tombol "Terapkan", dan tombol "Tindak Lanjut" ditekan lewat kontrolnya.

Tabel 4.4.9. Hasil Pengujian Use Case UC09

| ID Pengujian | ID KF | Skenario | Prosedur Pengujian | Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UC09-TC01 | KF12, KF18 | Normal | 1. Masuk sebagai perawat dan buka Daftar Pantau Pasien.<br>2. Pilih jenis risiko "hipertensi", tekan "Terapkan".<br>3. Tekan "Tindak Lanjut" pada entri Budi Santoso.<br>4. Isi formulir, tekan Ctrl+S. | Jenis kontak telepon, hasil upaya terhubung, status kedatangan akan datang | Daftar memuat pasien berisiko dan pasien yang dijadwalkan kunjungan ulang. Tabel mengikuti penyaring. Formulir tindak lanjut terbuka, lalu catatan tersimpan ke rekam medis, status entri menjadi "selesai ditindaklanjuti", dan jejak audit tercatat | Daftar menampilkan empat entri: indikasi diabetes (Siti Aminah) dan hipertensi (Budi Santoso) bertanda ⚠ merah, serta dua kunjungan ulang (Hendra Wijaya 27 Nov 2026 dan Budi Santoso 4 Des 2026) dengan nama, telepon, tanggal pemicu, dan status. Penyaring "hipertensi" menyisakan satu entri. Setelah simpan tampil "Tindak lanjut Budi Santoso tersimpan, status: selesai ditindaklanjuti" dan daftar menjadi tiga entri. Basis data: status "selesai ditindaklanjuti", satu baris tindak lanjut, catatan rekam medis "Tindak lanjut telepon: terhubung, akan datang", audit "Tindak lanjut daftar pantau 2: terhubung" | Pass |
| UC09-TC02 | KF18 | Alternatif 1: pasien belum dapat dihubungi | 1. Buka Daftar Pantau Pasien.<br>2. Tekan "Tindak Lanjut" pada entri Siti Aminah.<br>3. Isi formulir, tekan Ctrl+S. | Jenis kontak telepon, hasil upaya tidak terhubung, status kedatangan belum diketahui | Riwayat upaya tersimpan, status entri menjadi "perlu dihubungi ulang", dan pasien tetap berada pada daftar pantau | Tampil "Tindak lanjut Siti Aminah tersimpan, status: perlu dihubungi ulang" dan entri tetap ada dengan status itu. Basis data: status "perlu dihubungi ulang", satu baris tindak lanjut tersimpan | Pass |
| UC09-TC03 | KF18 | Alternatif 2: penyaringan tidak menemukan pasien | 1. Buka Daftar Pantau Pasien.<br>2. Pilih jenis risiko "obesitas", tekan "Terapkan". | Jenis risiko obesitas (tidak ada pada seed) | Tabel kosong disertai keterangan bahwa tidak ada pasien yang cocok dengan kriteria saringan | Keterangan "Tidak ada pasien yang cocok dengan kriteria saringan (obesitas)" tampil di atas tabel yang kosong | Pass |

### 4.4.10 Use Case Menyusun Laporan Periodik

**ID Use Case:** UC10  
**Nama Use Case:** Menyusun Laporan Periodik

*[Diisi Fachry: UC10-TC01 sampai UC10-TC03 untuk skenario normal dan alternatif 1 sampai 2.]*

### 4.4.11 Use Case Melakukan Sinkronisasi SATUSEHAT

**ID Use Case:** UC11  
**Nama Use Case:** Melakukan Sinkronisasi SATUSEHAT

*[Diisi Fachry: UC11-TC01 sampai UC11-TC03 untuk skenario normal dan alternatif 1 sampai 2.]*

### 4.4.12 Use Case Mengelola Konfigurasi & Basis Data Sistem

**ID Use Case:** UC12  
**Nama Use Case:** Mengelola Konfigurasi & Basis Data Sistem

Seluruh *test case* dijalankan dengan akun `admin` dengan cara yang sama seperti UC01. Nama tombol mengikuti rancangan I-15 (DPPL Subbab 6.15): "Akun Baru (Ctrl+N)" untuk "Tambah Pengguna Baru", "Simpan (Ctrl+S)" untuk "Simpan" dan "Simpan Pengaturan", "Nonaktifkan" untuk "Nonaktifkan Akun", dan "Jalankan Pencadangan" untuk "Cadangkan Basis Data Sekarang". Formulir akun I-15 juga tidak memiliki kolom nama lengkap.

Tabel 4.4.12. Hasil Pengujian Use Case UC12

| ID Pengujian | ID KF | Skenario | Prosedur Pengujian | Masukan | Hasil yang Diharapkan | Hasil Pengujian | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| UC12-TC01 | KF15 | Normal: akun baru dan ambang risiko | 1. Pilih kartu "Konfigurasi dan Basis Data".<br>2. Tekan "Akun Baru (Ctrl+N)", isi formulir, tekan "Simpan (Ctrl+S)".<br>3. Buka tab "Data Master", pilih ambang hipertensi, isi nilai baru, tekan "Simpan (Ctrl+S)". | Akun `perawat.baru`, peran Perawat, sandi `perawatbaru1`; ambang hipertensi 145 | Tab Akun Pengguna, Data Master, dan Pemeliharaan Basis Data tampil. Akun tersimpan dengan sandi ber-*hash*, tercatat di jejak audit, dan daftar akun termutakhirkan. Ambang baru tersimpan dan tercatat di jejak audit | Ketiga tab tampil. Setelah simpan, bilah pesan "Akun disimpan" tampil dan tabel akun memuat `perawat.baru` (Perawat, aktif). Setelah ambang disimpan, "Ambang risiko disimpan" tampil dan tabel ambang memuat hipertensi 145.0. Basis data: akun 134 ber-*hash* `$2a$12$`, ambang hipertensi 145, audit 387 "Akun baru perawat.baru (Perawat)" dan 388 "Ambang hipertensi diubah menjadi 145.0" | Pass |
| UC12-TC02 | KF21 | Alternatif 1: memantau dan menjalankan pencadangan | 1. Buka tab "Pemeliharaan Basis Data".<br>2. Tekan "Jalankan Pencadangan". | `LOKASI_MEDIA_CADANGAN=/tmp` | Pencadangan terakhir tampil dengan tanggal, ukuran, status, dan lokasi media. Basis data disalin ke media terpisah (arsip `pg_dump --format=custom` yang terkompresi tanpa enkripsi, Tabel B.1 nomor 34), keutuhannya diperiksa, lalu catatan terakhir termutakhirkan | Sebelum: 12-11-2026 21:00, 1,0 MB, berhasil, `/media/cadangan/sehati_20261112-2100.dump`. Setelah tombol ditekan: "Pencadangan berhasil", 19-11-2026 14:46, 88,2 KB, berhasil, `/tmp/sehati_20261119-144615.dump`. Basis data: catatan 34 "berhasil" berukuran 90291 bait | Pass |
| UC12-TC03 | KF15 | Alternatif 2: nama pengguna sudah dipakai | 1. Tekan "Akun Baru (Ctrl+N)", isi nama yang sudah ada, tekan "Simpan (Ctrl+S)".<br>2. Ganti nama, isi ulang sandi, simpan lagi. | `perawat`, lalu `perawat.dua`; peran Perawat, sandi `perawat2026` | Pendaftaran ditolak, kolom nama pengguna ditandai, dan petugas diminta memakai nama yang belum terdaftar. Setelah diganti, kembali ke langkah 3 skenario normal | Kolom nama pengguna berbingkai merah dengan keterangan "Nama pengguna sudah terdaftar" dan bilah pesan "Nama pengguna sudah dipakai". Setelah diganti, "Akun disimpan" tampil dan `perawat.dua` masuk tabel (audit 389) | Pass |
| UC12-TC04 | KF15, KF16 | Alternatif 3: menonaktifkan akun yang sedang dipakai | 1. Komputer B: masuk sebagai `perawat.baru`.<br>2. Komputer A (admin): pilih `perawat.baru` pada tabel, tekan "Nonaktifkan", pilih OK.<br>3. Amati komputer B.<br>4. Komputer A: keluar, lalu coba masuk sebagai `perawat.baru`. | Akun `perawat.baru` / `perawatbaru1`; dua proses aplikasi di komputer yang sama | Status akun menjadi nonaktif, sesi aktif pengguna itu terputus, percobaan masuk berikutnya ditolak, dan jejak audit tercatat | Dialog "Nonaktifkan akun perawat.baru? Sesi akun itu akan langsung ditutup." tampil. Setelah OK, "Akun dinonaktifkan" tampil dan status di tabel menjadi nonaktif (14:46:47). Komputer B kembali ke layar "Masuk" dengan pesan "Sesi diakhiri karena akun Anda dinonaktifkan. Hubungi Petugas Administrasi." pada 14:47:35, 48 detik kemudian (pewaktu memeriksa setiap menit). Percobaan masuk berikutnya ditolak "Akun nonaktif, hubungi Petugas Administrasi". Basis data: sesi akun 134 tidak aktif, audit 392 "Akun perawat.baru dinonaktifkan" | Pass |
| UC12-TC05 | KF21 | Alternatif 4: pencadangan terjadwal gagal | 1. Jalankan aplikasi dengan `PENCADANGAN_JAM=15` dan `LOKASI_MEDIA_CADANGAN=/Volumes/SEHATI-CADANGAN` (HDD tidak terpasang), lalu biarkan penjadwal berjalan.<br>2. Masuk sebagai admin dan buka dasbor serta tab Pemeliharaan.<br>3. Jalankan ulang aplikasi dengan media yang terpasang (`/tmp`), tekan "Jalankan Pencadangan", lalu kembali ke dasbor. | Jadwal pukul 15.00; media tidak terpasang, lalu terpasang | Dasbor menampilkan peringatan kegagalan pencadangan dan mempertahankannya sampai pencadangan berikutnya berhasil. Setelah media disambungkan, kembali ke langkah 2 alternatif 1 | Penjadwal berjalan pukul 14:59:08 dan mencatat "gagal" (catatan 40). Dasbor admin menampilkan peringatan merah "Pencadangan terakhir gagal. Jalankan ulang dari layar Konfigurasi dan Basis Data." dan informasi "19-11-2026 14:59 (gagal) di /Volumes/SEHATI-CADANGAN/...". Peringatan masih tampil setelah aplikasi dibuka ulang. Setelah "Jalankan Pencadangan" dengan media terpasang, "Pencadangan berhasil" (catatan 41) dan peringatan dasbor hilang | Pass. Catatan: penjadwal berjalan 52 detik lebih awal dari pukul 15.00 karena `KonfigurasiManager.jedaSampai()` menghitung jeda dalam menit penuh dan membuang detiknya. Tidak melanggar SKPL, tetapi disarankan memakai jeda dalam detik |

<br>

## 4.5 Pengujian Kebutuhan Non-Fungsional (KNF)

Setiap KNF pada Tabel 3.2 SKPL diuji secara terukur berdasarkan parameternya. Setiap pengujian memuat target terukur, satu metode beserta alat dan jumlah pengulangannya, serta hasil aktual berupa angka atau data. KNF yang belum dipenuhi atau tidak dapat diukur penuh tetap dicantumkan beserta alasan dan rencana perbaikannya.

Tabel 4.5. Hasil Pengujian KNF

| ID Pengujian | ID KNF | Parameter | Target Terukur | Metode dan Alat | Hasil Aktual | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| KNF01-TC01 | KNF01 | Response time | *[Diisi Steven]* | | | |
| KNF02-TC01 | KNF02 | Reliability | Lima nilai di luar rentang fisiologis (sistolik 400, suhu 72, nadi 300, tinggi 20 cm, gula darah 5) ditolak 100%, dan 0 baris `skrining`, `tanda_vital`, serta `penanda_risiko` bertambah | `SkriningIntegrasiTest` (KNF02-TC01): `SkriningManager.simpanSkrining()` dipanggil lima kali dengan masing-masing satu nilai salah, jumlah baris dihitung sebelum dan sesudah lewat koneksi terpisah. Layar I-05 pada UC04-TC03 sebagai pembanding. 5 pengulangan | 5 dari 5 ditolak dengan daftar kolom yang bermasalah. Jumlah baris `skrining`, `tanda_vital`, dan `penanda_risiko` sama dengan sebelumnya (selisih 0). Pada layar, kolom sistolik dan suhu bertanda merah dan 0 baris tersimpan | Pass |
| KNF02-TC02 | KNF02 | Reliability | Bila penyimpanan gagal di tengah transaksi, 0 baris `skrining`, `tanda_vital`, `penanda_risiko`, dan `daftar_pantau` tersisa (tanpa data setengah tersimpan) | `SkriningIntegrasiTest` (KNF02-TC02): pemicu sementara pada tabel `audit` menolak baris audit di langkah terakhir, lalu `simpanSkrining()` dengan nilai berisiko dipanggil. 3 pengulangan, jumlah baris dibaca lewat koneksi terpisah | 3 dari 3 pemanggilan gagal dengan pengecualian. Jumlah baris keempat tabel sama dengan sebelumnya (selisih 0) setelah ketiganya | Pass |
| KNF03-TC01 | KNF03 | Reliability | 0 dari 15 penyerahan obat yang digagalkan di tengah proses meninggalkan bundel FHIR parsial maupun perubahan stok, resep, kunjungan, antrean, atau audit. Penyerahan tanpa gangguan menghasilkan satu bundel lengkap | Test JUnit `KnfFarmasiTest` menjalankan `ApotekManager.serahkanObat()` pada resep seed 4 yang digagalkan di tiga titik: (1) saat memotong obat kedua, karena stok Glibenklamid dikurangi koneksi lain, (2) saat menyusun bundel FHIR, karena hak baca tabel `pasien` untuk `sehati_app` dicabut sementara, dan (3) saat menyimpan bundel, karena bundel lama kunjungan yang sama melanggar UNIQUE. Sebelum dan sesudah setiap percobaan dibandingkan sidik basis data: stok seluruh obat, sisa seluruh bets, status seluruh resep dan kunjungan, jumlah bundel, antrean apotek, jumlah penerimaan, dan jumlah audit. Pengulangan: 5 kali per titik (15 percobaan), ditambah satu penyerahan kontrol tanpa gangguan. Alat: JUnit 5, PostgreSQL 15, laporan Surefire | 15 dari 15 percobaan ditolak dalam 8 sampai 53 ms: titik 1 "Stok Glibenklamid 5 mg tidak cukup", titik 2 "Basis data tidak dapat dihubungi", titik 3 "Kunjungan sudah memiliki bundel". Pada 15 dari 15 percobaan tidak ada bundel untuk kunjungan 5 dan sidik basis data sama persis sebelum dan sesudah. Penyerahan kontrol menghasilkan satu bundel lengkap berisi `Patient`, `Encounter`, tujuh `Observation`, `Condition`, dan dua `MedicationRequest`. Hasil yang sama ditunjukkan C24-UT16 dan INT-TC24 | Pass. Catatan: pesan titik 2 berasal dari `DataPasienBoundary` (C57), yang menerjemahkan semua galat SQL yang tidak dikenalinya, termasuk hak akses ditolak (SQLState 42501), menjadi "Basis data tidak dapat dihubungi". Atomisitas tidak terpengaruh, tetapi pesannya dapat menyesatkan petugas. Temuan ini disampaikan ke pemilik C57 |
| KNF04-TC01 | KNF04 | Security | (1) 100% kata sandi tersimpan sebagai *hash* bcrypt dan 0 berupa teks polos. (2) 0 layar terbuka tanpa autentikasi. (3) Sesi diam berakhir paling cepat 15 menit dan paling lambat 16 menit setelah aktivitas terakhir (pewaktu memeriksa setiap menit) | (1) Inspeksi kolom `kata_sandi_hash` seluruh akun lewat `psql` (pola `^\$2a\$[0-9]{2}\$.{53}$`). (2) Aplikasi dibuka tanpa sesi, lalu masuk dengan kredensial salah dan akun nonaktif (UC01-TC02, UC01-TC03); `periksaHakAkses()` dengan peran `null` (C17-UT10). (3) Masuk sebagai `perawat` lalu dibiarkan tanpa ketukan atau klik, waktu dibaca dari audit. Pengulangan: (1) 7 akun, (2) 3 kasus, (3) 1 kali selama 16 menit | (1) 7 dari 7 akun ber-*hash* bcrypt (`$2a$10$` untuk akun seed, `$2a$12$` untuk akun dari UC12), 0 teks polos. (2) Aplikasi selalu dibuka di layar Masuk dan seluruh layar lain ditolak tanpa peran. (3) Masuk 14:44:40 (audit 378), sesi berakhir 15:00:38 (audit 479 "Sesi berakhir setelah diam 15 menit"), yaitu 15 menit 58 detik, dan layar Masuk menampilkan "Anda tidak aktif selama 15 menit. Masuk lagi untuk melanjutkan." | Pass. Batas: pengukuran sesi diam hanya satu kali karena satu pengulangan butuh 16 menit; ketelitian pewaktu dibatasi pemeriksaan per menit |
| KNF05-TC01 | KNF05 | Security | (1) 0 layar di luar peran yang dapat dibuka. (2) Operasi tulis tanpa sesi atau dengan peran lain ditolak di `Manager`. (3) 0 dari 3 perintah ubah/hapus jejak audit berhasil dengan akun aplikasi `sehati_app` | (1) Dasbor tiap peran dan perintah buka langsung (UC01-TC01, UC01-TC04), matriks `periksaHakAkses()` (C17-UT07 s.d. C17-UT10). (2) Inspeksi kode: pemanggilan `SesiAktif.wajibPeran()` pada `Manager`, diuji lewat C29-UT10, C22-UT15, dan C23-UT21. (3) `psql` sebagai `sehati_app`: `DELETE`, `UPDATE`, dan `TRUNCATE` pada `audit`, masing-masing sekali | (1) Dasbor perawat hanya memuat 3 layar perawat, dan perintah buka Formulir Pemeriksaan ditolak "Akses ditolak: Peran Perawat tidak berwenang membuka layar ini". (2) 22 pemanggilan `wajibPeran()` pada 11 `Manager`. Ketiga test menolak sesi berperan lain dan sesi kosong. (3) Ketiga perintah ditolak "permission denied for table audit", jumlah audit tetap 231 | Pass. Catatan: `KonfigurasiManager.jalankanPencadangan()` sengaja tidak memanggil `wajibPeran()` karena juga dijalankan penjadwal tanpa sesi. Tombolnya hanya ada pada I-15 yang terbatas untuk Petugas Administrasi |
| KNF06-TC01 | KNF06 | Reliability | (1) 5 dari 5 pencadangan berhasil dan lolos verifikasi arsip selagi aplikasi dipakai. (2) 0 operasi pengguna gagal dan waktu tanggap maksimum di bawah 1 detik selama pencadangan. (3) Arsip berupa *snapshot* konsisten: perubahan yang belum di-*commit* tidak ikut tersalin | Program uji `UjiKnf06` (di luar repo) menjalankan `KonfigurasiManager.jalankanPencadangan()` 5 kali dengan `pg_dump` 18.6, sementara *thread* lain terus-menerus masuk, memuat data master, dan keluar lewat `AutentikasiManager` dan `KonfigurasiManager`. Pada pengulangan pertama, koneksi lain memegang transaksi belum di-*commit* yang mengubah ambang hipertensi menjadi 999. Setiap arsip diperiksa dengan `pg_restore --list` dan `pg_restore --data-only --table=ambang_risiko` | (1) 5 dari 5 "berhasil", 136 sampai 247 ms per pencadangan, setiap arsip memuat 28 data tabel. (2) 42 operasi pengguna, 0 gagal, rata-rata 107,1 ms, maksimum 311 ms. (3) Kelima arsip memuat ambang hipertensi 145 (nilai ter-*commit*), bukan 999 | Pass. Temuan: nama arsip hanya sampai detik (Tabel B.1 nomor 33), sehingga dua pencadangan pada detik yang sama menulis ke berkas yang sama dan arsip pertama tertimpa (catatan 35 dan 36, 38 dan 39). Pada aplikasi, tombol pencadangan nonaktif selama proses berjalan, tetapi penjadwal dan tombol masih dapat bertabrakan. Disarankan menambah milidetik atau penanda unik pada nama berkas |
| KNF07-TC01 | KNF07 | Portability | *[Diisi Fachry]* | | | |
| KNF08-TC01 | KNF08 | Ergonomy | Untuk 3 dari 3 jenis risiko (hipertensi, obesitas, indikasi diabetes), peringatan merah dengan ikon tampil segera setelah Simpan dengan 0 langkah tambahan dan 0 peristiwa tetikus | `PemanduLayarSkrining`: formulir diisi dengan papan ketik, Ctrl+S ditekan, panel penanda (`panelPenanda`) diperiksa tampil dan jendela dipotret, serta penanda tersimpan dibaca dari basis data. 3 pengulangan, satu jenis risiko per pengulangan | 3 dari 3 panel penanda tampil dengan ikon dan teks "Risiko ... terdeteksi" berwarna merah. Penanda tersimpan sesuai (hipertensi, obesitas, indikasi diabetes). Peristiwa tetikus sesudah Simpan: 0 | Pass |
| KNF09-TC01 | KNF09 | Availability | *[Diisi Fachry]* | | | |
| KNF10-TC01 | KNF10 | Portability | *[Diisi Steven]* | | | |
| KNF11-TC01 | KNF11 | Reliability | *[Diisi Steven]* | | | |
| KNF12-TC01 | KNF12 | Ergonomy | Satu formulir skrining tuntas dengan 0 klik tetikus, dan layar bantuan F1 memuat pintasan skrining (F2, F3, Tab, Ctrl+S) | `PemanduLayarSkrining` mengirim hanya peristiwa papan ketik (F2, Tab, ketikan, Ctrl+S) pada enam pengisian formulir (UC04-TC01, UC04-TC03, UC04-TC04, dan tiga kasus KNF08-TC01) dan menghitung peristiwa `MOUSE_PRESSED` dan `MOUSE_CLICKED` yang sampai ke jendela. F1 ditekan pada layar antrean. 6 pengulangan. Keterbatasan: peristiwa papan ketik dikirim skrip, bukan diketik manusia | 6 dari 6 formulir tersimpan tanpa tetikus. Peristiwa tetikus yang sampai ke jendela selama seluruh skenario: 0. F1 membuka "Pintasan Papan Ketik" yang memuat Tab, Ctrl+S, F2, F3, Esc, Ctrl+D, dan F1 beserta layarnya | Pass |
| KNF13-TC01 | KNF13 | Reliability | Setelah proses aplikasi dihentikan paksa (`kill -9`) di tengah penyimpanan: (1) 0 perubahan setengah jadi tersimpan, (2) 100% data yang sudah tersimpan sebelum proses mati tetap utuh, dan (3) aplikasi yang dijalankan ulang berhasil mengulang penyimpanan yang sama dengan saldo stok tetap sama dengan jumlah bets | Test JUnit `KnfFarmasiTest` menjalankan aplikasi sebagai JVM terpisah (`ProsesFarmasiKnf13`) dengan akun `sehati_app` dari `.env`, lalu mematikannya dengan `Process.destroyForcibly()` (SIGKILL, sama dengan `kill -9`) saat transaksinya tertahan di tengah penyimpanan. Skenario A: proses mencatat penerimaan Kaptopril sampai selesai, lalu menyerahkan obat resep 4 dan tertahan saat menyisipkan bundel FHIR, karena koneksi test memegang bundel kunjungan yang sama. Skenario B: proses mencatat penerimaan dan tertahan pada sisipan audit, penulisan terakhir sebelum COMMIT, oleh pemicu uji sementara yang menunggu *advisory lock*. Tabel yang sudah ditulis transaksi dibaca dari `pg_locks`, dan sidik basis data dibandingkan sebelum dan sesudah proses mati. Setelah itu proses dijalankan ulang untuk operasi yang sama. Pengulangan: 3 kali per skenario (6 kali mati paksa dan 6 kali jalan ulang) | 6 dari 6 proses keluar dengan kode 137 (SIGKILL). Saat dimatikan, transaksi skenario A sudah memegang kunci tulis pada `obat`, `bets_obat`, `resep`, `antrean_entri`, `kunjungan`, dan `bundel_fhir`, sedangkan skenario B pada `obat`, `bets_obat`, `penerimaan_obat`, dan `audit`. Pada 6 dari 6 percobaan sidik basis data sama dengan sebelum proses mati: resep 4 tetap "menunggu", tidak ada bundel, dan tidak ada faktur setengah jadi. Penerimaan Kaptopril yang selesai sebelum proses mati pada skenario A tetap tersimpan, dengan stok bertambah 10. Setelah dijalankan ulang, 6 dari 6 operasi berhasil: resep 4 "selesai dilayani" dengan tepat satu bundel dan saldo seluruh obat sama dengan jumlah bets (A), serta faktur tersimpan sekali dengan stok bertambah 10 (B) | Pass. Batas: proses dimatikan pada titik tunggu yang dibuat test, bukan pada saat acak, dan yang dimatikan adalah proses aplikasi. Mati listrik pada server basis data tidak diuji karena ditangani *write-ahead log* PostgreSQL |

<br>

## 4.6 Tangkapan Layar dan Bukti Hasil Pengujian

Bukti pengujian dilampirkan sesuai ID Pengujian pada Subbab 4.2 sampai 4.5. Keluaran *test runner* memakai format pohon dengan ID *test case* pada setiap baris: `[OK]` berarti lulus dan `[XX]` gagal. Untuk temuan yang sudah diperbaiki, bukti `v9.0` dan bukti uji ulang dilampirkan berdampingan. Laporan *coverage* diambil dari halaman kelas pada laporan JaCoCo (`src/target/site/jacoco/`). Kolom "Lines" dan "Missed" pada laporan itu menjadi dasar persentase baris pada Subbab 4.2. Tangkapan layar aplikasi diambil dari jendela SEHATI ukuran 1280 × 800 piksel.

Tabel 4.6. Bukti Hasil Pengujian

| ID Pengujian | Bukti Hasil Pengujian |
| :--- | :--- |
| C22-UT01 – C22-UT37 | Keluaran `mvn test -Dtest=PemeriksaanManagerTest` setelah perbaikan: 37 test `[OK]`<br><img src="./assets/pemeriksaan/c22-unit.png" width="100%"> |
| C22-UT37 (`v9.0`, Fail) | Test yang sama pada kode `v9.0`: C22-UT37 `[XX]`, `expected: <7> but was: <8>`<br><img src="./assets/pemeriksaan/c22-ut37-fail-v9.png" width="100%"> |
| C22 (coverage) | Total 20 dari 125 baris dan 4 dari 46 cabang terlewat<br><img src="./assets/pemeriksaan/c22-coverage.png" width="100%"> |
| C23-UT01 – C23-UT25 | Keluaran `mvn test -Dtest=ResepManagerTest`: 25 test `[OK]`<br><img src="./assets/pemeriksaan/c23-unit.png" width="100%"> |
| C23 (coverage) | Total 10 dari 69 baris dan 1 dari 24 cabang terlewat<br><img src="./assets/pemeriksaan/c23-coverage.png" width="100%"> |
| C42-UT01 – C42-UT03, C44-UT01 – C44-UT05, C45-UT01 – C45-UT03 | Keluaran `mvn test -Dtest=EntityPemeriksaanTest`. Baris berawalan "Pendukung" bukan milik kelas bertingkat Mendalam/Sedang modul ini<br><img src="./assets/pemeriksaan/c42-c44-c45-unit.png" width="100%"> |
| C42 (coverage) | Total 2 dari 28 baris terlewat, cabang 100%<br><img src="./assets/pemeriksaan/c42-coverage.png" width="100%"> |
| C44 (coverage) | Seluruh baris dan cabang tercakup<br><img src="./assets/pemeriksaan/c44-coverage.png" width="100%"> |
| C45 (coverage) | Total 4 dari 24 baris terlewat, cabang 100%<br><img src="./assets/pemeriksaan/c45-coverage.png" width="100%"> |
| C46-UT01 – C46-UT04, C47-UT01 – C47-UT03 | Keluaran `mvn test -Dtest=EntityResepTest`<br><img src="./assets/pemeriksaan/c46-c47-unit.png" width="100%"> |
| C46 (coverage) | Total 6 dari 34 baris terlewat, cabang 100%<br><img src="./assets/pemeriksaan/c46-coverage.png" width="100%"> |
| C59-UT21 – C59-UT32 | Keluaran `mvn test -Dtest=DataKlinisBoundaryPemeriksaanTest`<br><img src="./assets/pemeriksaan/c59-unit.png" width="100%"> |
| INT-TC17 – INT-TC22 | Keluaran `mvn test -Dtest=PemeriksaanResepIntegrasiTest`<br><img src="./assets/pemeriksaan/int-tc17-tc22.png" width="100%"> |
| UC05-TC01 | `v9.0`: ringkasan I-06 tanpa bagian skrining hari ini (Fail). Uji ulang: panel "Skrining hari ini" tampil (Pass)<br><img src="./assets/uc05/uc05-tc01-v9-ringkasan.png" width="100%"><br><img src="./assets/uc05/uc05-tc01-ulang-ringkasan.png" width="100%"> |
| UC05-TC02 | Grafik tren empat kunjungan<br><img src="./assets/uc05/uc05-tc02-tren.png" width="100%"> |
| UC05-TC03 | Berurutan: saran ICD-10, isian lengkap, simpan dengan jadwal kontrol, dialog konfirmasi, rekam medis terkunci, daftar pantau perawat (entri 9 Des 2026), dan isi basis data setelah pengujian<br><img src="./assets/uc05/uc05-tc03-1-saran.png" width="100%"><br><img src="./assets/uc05/uc05-tc03-2-isian.png" width="100%"><br><img src="./assets/uc05/uc05-tc03-3-tersimpan.png" width="100%"><br><img src="./assets/uc05/uc05-tc03-4-konfirmasi.png" width="100%"><br><img src="./assets/uc05/uc05-tc03-5-terkunci.png" width="100%"><br><img src="./assets/uc05/uc05-tc03-6-daftar-pantau.png" width="100%"><br><img src="./assets/uc05/uc05-v9-hasil-basis-data.png" width="100%"> |
| UC05-TC04 | Mode baca saja untuk perawat<br><img src="./assets/uc05/uc05-tc04-baca-saja.png" width="100%"> |
| UC05-TC05 | Pesan kode tidak ditemukan, lalu saran muncul setelah istilah diganti<br><img src="./assets/uc05/uc05-tc05-1-tidak-ditemukan.png" width="100%"><br><img src="./assets/uc05/uc05-tc03-1-saran.png" width="100%"> |
| UC05-TC06 | Indikator "Luring" pada bilah aplikasi dan pemeriksaan tetap tersimpan<br><img src="./assets/uc05/uc05-tc06-1-ringkasan-luring.png" width="100%"><br><img src="./assets/uc05/uc05-tc06-2-tersimpan-luring.png" width="100%"> |
| UC05-TC07 | Tabel pengukuran pasien yang baru sekali diukur<br><img src="./assets/uc05/uc05-tc07-tabel-pengukuran.png" width="100%"> |
| UC05-TC08 | `v9.0`: sebelum, simpan jadwal 02-12-2026, lalu dua entri 2 Des 2026 (Fail). Isi tabel `jadwal_kontrol` dan `daftar_pantau` `v9.0` ada pada bukti basis data UC05-TC03<br><img src="./assets/uc05/uc05-tc08-v9-1-pantau-sebelum.png" width="100%"><br><img src="./assets/uc05/uc05-tc08-v9-2-simpan-tanggal-sama.png" width="100%"><br><img src="./assets/uc05/uc05-tc08-v9-3-daftar-pantau.png" width="100%"><br>Uji ulang setelah perbaikan: langkah yang sama, daftar pantau tetap satu entri 2 Des 2026, dan isi basis data (Pass)<br><img src="./assets/uc05/uc05-tc08-ulang-1-pantau-sebelum.png" width="100%"><br><img src="./assets/uc05/uc05-tc08-ulang-2-simpan-tanggal-sama.png" width="100%"><br><img src="./assets/uc05/uc05-tc08-ulang-3-daftar-pantau.png" width="100%"><br><img src="./assets/uc05/uc05-tc08-ulang-hasil-basis-data.png" width="100%"> |
| UC06-TC01 | Dua item "Tersedia", resep terkirim, dan isi `resep`, `item_resep`, serta antrean apotek<br><img src="./assets/uc06/uc06-tc01-1-tersedia.png" width="100%"><br><img src="./assets/uc06/uc06-tc01-2-terkirim.png" width="100%"><br><img src="./assets/uc06/uc06-hasil-basis-data.png" width="100%"> |
| UC06-TC02 | Stok kurang dengan tombol kirim nonaktif, lalu setelah jumlah diperbaiki<br><img src="./assets/uc06/uc06-tc02-1-stok-kurang.png" width="100%"><br><img src="./assets/uc06/uc06-tc02-2-diperbaiki.png" width="100%"> |
| C17-UT01 – C17-UT17 | Keluaran `mvn test -Dtest=AutentikasiManagerTest` setelah perbaikan: 17 test `[OK]`<br><img src="./assets/akses/c17-unit.png" width="100%"> |
| C17-UT17 (`v9.0`, Fail) | Test yang sama pada kode `v9.0`: C17-UT17 `[XX]`, `NullPointer`<br><img src="./assets/akses/c17-ut17-fail-v9.png" width="100%"> |
| C17 (coverage) | Total 8 dari 56 baris dan 2 dari 26 cabang terlewat<br><img src="./assets/akses/c17-coverage.png" width="100%"> |
| C29-UT01 – C29-UT37 | Keluaran `mvn test -Dtest=KonfigurasiManagerTest`: 37 test `[OK]`<br><img src="./assets/akses/c29-unit.png" width="100%"> |
| C29 (coverage) | Total 30 dari 159 baris dan 9 dari 72 cabang terlewat<br><img src="./assets/akses/c29-coverage.png" width="100%"> |
| C30-UT01 – C30-UT11, C31-UT01 – C31-UT07, C32-UT01 – C32-UT03 | Keluaran `mvn test -Dtest=PenggunaEntityTest,SesiEntityTest,AuditEntityTest`: 21 test `[OK]`<br><img src="./assets/akses/c30-c31-c32-unit.png" width="100%"> |
| C30 (coverage) | Total 6 dari 36 baris terlewat, cabang 100%<br><img src="./assets/akses/c30-coverage.png" width="100%"> |
| C31 (coverage) | Seluruh baris dan cabang tercakup<br><img src="./assets/akses/c31-coverage.png" width="100%"> |
| C56-UT01 – C56-UT10 | Keluaran `mvn test -Dtest=DataAkunBoundaryTest`: 10 test `[OK]`<br><img src="./assets/akses/c56-unit.png" width="100%"> |
| C64-UT01 – C64-UT16 | Keluaran `mvn test -Dtest=DataPencadanganBoundaryTest`: 16 test `[OK]`<br><img src="./assets/akses/c64-unit.png" width="100%"> |
| C64 (coverage) | Total 14 dari 98 baris dan 6 dari 42 cabang terlewat<br><img src="./assets/akses/c64-coverage.png" width="100%"> |
| INT-TC01 – INT-TC06 | Keluaran `mvn test -Dtest=AksesIntegrasiTest`: 6 test `[OK]`<br><img src="./assets/akses/int-tc01-tc06.png" width="100%"> |
| UC01-TC01 | Layar masuk, dasbor admin, dan dasbor perawat<br><img src="./assets/uc01/uc01-tc01-1-layar-masuk.png" width="100%"><br><img src="./assets/uc01/uc01-tc01-2-dasbor-admin.png" width="100%"><br><img src="./assets/uc01/uc01-tc01-3-dasbor-perawat.png" width="100%"> |
| UC01-TC02 | Pesan kredensial salah dengan kolom sandi kosong, lalu dasbor setelah masuk ulang<br><img src="./assets/uc01/uc01-tc02-1-sandi-salah.png" width="100%"><br><img src="./assets/uc01/uc01-tc02-2-masuk-ulang.png" width="100%"> |
| UC01-TC03 | Pesan akun nonaktif<br><img src="./assets/uc01/uc01-tc03-akun-nonaktif.png" width="100%"> |
| UC01-TC04 | Dasbor perawat dan pesan akses ditolak<br><img src="./assets/uc01/uc01-tc04-akses-ditolak.png" width="100%"> |
| UC01 (basis data) | Audit dan sesi UC01-TC01 sampai UC01-TC03<br><img src="./assets/uc01/uc01-hasil-basis-data.png" width="100%"> |
| UC12-TC01 | Tab Akun Pengguna, isian akun, akun tersimpan, isian ambang, dan ambang tersimpan<br><img src="./assets/uc12/uc12-tc01-1-tab-akun.png" width="100%"><br><img src="./assets/uc12/uc12-tc01-2-isian-akun.png" width="100%"><br><img src="./assets/uc12/uc12-tc01-3-akun-tersimpan.png" width="100%"><br><img src="./assets/uc12/uc12-tc01-4-isian-ambang.png" width="100%"><br><img src="./assets/uc12/uc12-tc01-5-ambang-tersimpan.png" width="100%"> |
| UC12-TC02 | Status pencadangan sebelum dan sesudah "Jalankan Pencadangan"<br><img src="./assets/uc12/uc12-tc02-1-status-pencadangan.png" width="100%"><br><img src="./assets/uc12/uc12-tc02-2-pencadangan-berhasil.png" width="100%"> |
| UC12-TC03 | Nama pengguna yang sudah dipakai ditandai, lalu nama baru tersimpan<br><img src="./assets/uc12/uc12-tc03-1-nama-dipakai.png" width="100%"><br><img src="./assets/uc12/uc12-tc03-2-nama-baru.png" width="100%"> |
| UC12-TC04 | Berurutan: komputer B masuk sebagai `perawat.baru`, dialog konfirmasi di komputer A, akun nonaktif, komputer B terputus, dan percobaan masuk ditolak<br><img src="./assets/uc12/uc12-tc04-0-sesi-perawat-baru.png" width="100%"><br><img src="./assets/uc12/uc12-tc04-1-konfirmasi.png" width="100%"><br><img src="./assets/uc12/uc12-tc04-2-akun-nonaktif.png" width="100%"><br><img src="./assets/uc12/uc12-tc04-3-sesi-terputus.png" width="100%"><br><img src="./assets/uc12/uc12-tc04-4-masuk-ditolak.png" width="100%"> |
| UC12-TC05 | Berurutan: dasbor dan tab Pemeliharaan setelah jadwal gagal, dasbor setelah aplikasi dibuka ulang, pencadangan ulang berhasil, dan dasbor tanpa peringatan<br><img src="./assets/uc12/uc12-tc05-1-dasbor.png" width="100%"><br><img src="./assets/uc12/uc12-tc05-2-pemeliharaan.png" width="100%"><br><img src="./assets/uc12/uc12-tc05-3-ulang-1-dasbor.png" width="100%"><br><img src="./assets/uc12/uc12-tc05-3-ulang-3-hasil.png" width="100%"><br><img src="./assets/uc12/uc12-tc05-3-ulang-4-dasbor.png" width="100%"><br><img src="./assets/uc12/uc12-tc05-hasil-basis-data.png" width="100%"> |
| UC12 (basis data) | Akun, ambang, audit, sesi, dan catatan pencadangan setelah UC12-TC01 sampai UC12-TC04<br><img src="./assets/uc12/uc12-hasil-basis-data.png" width="100%"> |
| KNF04-TC01 | *Hash* seluruh akun, masuk sebagai perawat, layar setelah 15 menit diam, dan audit masuk serta sesi berakhir<br><img src="./assets/akses/knf04-hash-bcrypt.png" width="100%"><br><img src="./assets/akses/knf04-1-masuk.png" width="100%"><br><img src="./assets/akses/knf04-2-sesi-berakhir.png" width="100%"><br><img src="./assets/akses/knf04-3-audit-sesi-diam.png" width="100%"> |
| KNF05-TC01 | Perintah ubah dan hapus audit sebagai `sehati_app` ditolak (dasbor per peran pada UC01-TC01 dan UC01-TC04)<br><img src="./assets/akses/knf05-audit-sehati-app.png" width="100%"> |
| KNF06-TC01 | Keluaran `UjiKnf06`: lima pencadangan selagi aplikasi dipakai<br><img src="./assets/akses/knf06-pencadangan-saat-dipakai.png" width="100%"> |
| *[ID pengujian milik Steven]* | *[Diisi Steven: keluaran test runner per kelas, coverage kelas Mendalam, integrasi, use case, dan KNF]* |
| C20-UT01 – C20-UT32 | Keluaran `mvn test -Dtest=SkriningManagerTest`: 32 test `[OK]`<br><img src="./assets/skrining/c20-unit.png" width="100%"> |
| C20 (coverage) | Total 11 dari 130 baris dan 6 dari 54 cabang terlewat<br><img src="./assets/skrining/c20-coverage.png" width="100%"> |
| C21-UT01 – C21-UT18 | Keluaran `mvn test -Dtest=RisikoManagerTest`: 18 test `[OK]`<br><img src="./assets/skrining/c21-unit.png" width="100%"> |
| C21 (coverage) | Total 6 dari 60 baris dan 0 dari 40 cabang terlewat<br><img src="./assets/skrining/c21-coverage.png" width="100%"> |
| C26-UT01 – C26-UT26 | Keluaran `mvn test -Dtest=PantauManagerTest`: 26 test `[OK]`<br><img src="./assets/skrining/c26-unit.png" width="100%"> |
| C26 (coverage) | Total 4 dari 73 baris dan 4 dari 56 cabang terlewat<br><img src="./assets/skrining/c26-coverage.png" width="100%"> |
| C38-UT01 – C38-UT04 | Keluaran `mvn test -Dtest=SkriningEntityTest`: 4 test `[OK]`<br><img src="./assets/skrining/c38-unit.png" width="100%"> |
| C39-UT01 – C39-UT08 | Keluaran `mvn test -Dtest=TandaVitalEntityTest`: 8 test `[OK]`<br><img src="./assets/skrining/c39-unit.png" width="100%"> |
| C39 (coverage) | Total 6 dari 42 baris dan 0 dari 2 cabang terlewat<br><img src="./assets/skrining/c39-coverage.png" width="100%"> |
| C40-UT01 – C40-UT06, C41-UT01 – C41-UT05 | Keluaran `mvn test -Dtest=AmbangPenandaEntityTest`: 11 test `[OK]`<br><img src="./assets/skrining/c40-c41-unit.png" width="100%"> |
| C40 (coverage) | Seluruh baris dan cabang tercakup<br><img src="./assets/skrining/c40-coverage.png" width="100%"> |
| C41 (coverage) | Seluruh baris dan cabang tercakup<br><img src="./assets/skrining/c41-coverage.png" width="100%"> |
| C51-UT01 – C51-UT06 | Keluaran `mvn test -Dtest=DaftarPantauEntityTest`: 6 test C51 dan 1 test `TindakLanjutEntity.terhubung()` `[OK]`<br><img src="./assets/skrining/c51-unit.png" width="100%"> |
| C51 (coverage) | Total 7 dari 36 baris dan 0 dari 4 cabang terlewat<br><img src="./assets/skrining/c51-coverage.png" width="100%"> |
| C59-UT01 – C59-UT20 | Keluaran `mvn test -Dtest=DataKlinisSkriningDbTest`: 20 test `[OK]`<br><img src="./assets/skrining/c59-unit.png" width="100%"> |
| C60-UT01 – C60-UT09 | Keluaran `mvn test -Dtest=DataPantauDbTest`: 9 test `[OK]`<br><img src="./assets/skrining/c60-unit.png" width="100%"> |
| INT-TC11 – INT-TC16, KNF02-TC01, KNF02-TC02 | Keluaran `mvn test -Dtest=SkriningIntegrasiTest`: 8 test `[OK]`<br><img src="./assets/skrining/int-tc11-tc16.png" width="100%"> |
| UC04-TC01 | Antrean, formulir setelah F2, formulir terisi dengan papan ketik, dan hasil simpan<br><img src="./assets/uc04/uc04-tc01-1-antrean.png" width="100%"><br><img src="./assets/uc04/uc04-tc01-2-formulir-dipanggil.png" width="100%"><br><img src="./assets/uc04/uc04-tc01-3-terisi.png" width="100%"><br><img src="./assets/uc04/uc04-tc01-4-tersimpan.png" width="100%"> |
| UC04-TC02 | Pasien sedang diskrining, lalu sesudah F3 pasien dipindah ke belakang berstatus tidak hadir<br><img src="./assets/uc04/uc04-tc02-1-sedang-diskrining.png" width="100%"><br><img src="./assets/uc04/uc04-tc02-2-dilewati.png" width="100%"> |
| UC04-TC03 | Kolom di luar rentang bertanda merah, isian setelah koreksi, dan hasil simpan<br><img src="./assets/uc04/uc04-tc03-1-di-luar-rentang.png" width="100%"><br><img src="./assets/uc04/uc04-tc03-2-dikoreksi.png" width="100%"><br><img src="./assets/uc04/uc04-tc03-3-tersimpan.png" width="100%"> |
| UC04-TC04 | Tiga peringatan risiko merah berikon setelah simpan<br><img src="./assets/uc04/uc04-tc04-1-risiko.png" width="100%"> |
| UC09-TC01 | Daftar pantau, penyaring hipertensi, formulir tindak lanjut, dan hasil simpan<br><img src="./assets/uc09/uc09-tc01-1-daftar.png" width="100%"><br><img src="./assets/uc09/uc09-tc01-2-disaring.png" width="100%"><br><img src="./assets/uc09/uc09-tc01-3-formulir.png" width="100%"><br><img src="./assets/uc09/uc09-tc01-4-tersimpan.png" width="100%"> |
| UC09-TC02 | Formulir tindak lanjut tidak terhubung dan hasilnya (perlu dihubungi ulang)<br><img src="./assets/uc09/uc09-tc02-1-formulir.png" width="100%"><br><img src="./assets/uc09/uc09-tc02-2-tersimpan.png" width="100%"> |
| UC09-TC03 | Penyaring tanpa hasil dengan keterangan tabel kosong<br><img src="./assets/uc09/uc09-tc03-1-kosong.png" width="100%"> |
| KNF08-TC01 | Peringatan merah berikon untuk hipertensi, obesitas, dan indikasi diabetes<br><img src="./assets/skrining/knf08-tc01-hipertensi.png" width="100%"><br><img src="./assets/skrining/knf08-tc01-obesitas.png" width="100%"><br><img src="./assets/skrining/knf08-tc01-indikasi-diabetes.png" width="100%"> |
| KNF12-TC01 | Layar bantuan F1 (Pintasan Papan Ketik)<br><img src="./assets/skrining/knf12-tc01-bantuan-f1.png" width="100%"> |
| UC04, UC09, KNF08, KNF12 (catatan skrip) | Isi basis data yang dibaca skrip sesudah tiap skenario dan jumlah peristiwa tetikus (0)<br><img src="./assets/skrining/catatan-pemandu-layar.png" width="100%"> |
| C24-UT01 – C24-UT37 | Keluaran `mvn test -Dtest=ApotekManagerTest`: 37 test `[OK]`<br><img src="./assets/farmasi/c24-unit.png" width="100%"> |
| C24 (coverage) | Total 20 dari 146 baris dan 3 dari 44 cabang terlewat<br><img src="./assets/farmasi/c24-coverage.png" width="100%"> |
| C25-UT01 – C25-UT20 | Keluaran `mvn test -Dtest=PersediaanManagerTest`: 20 test `[OK]`<br><img src="./assets/farmasi/c25-unit.png" width="100%"> |
| C25 (coverage) | Total 8 dari 79 baris dan 4 dari 46 cabang terlewat<br><img src="./assets/farmasi/c25-coverage.png" width="100%"> |
| C48-UT01 – C48-UT10, C49-UT01 – C49-UT13, C50-UT01 – C50-UT03 | Keluaran `mvn test -Dtest=EntityFarmasiTest`: 26 test `[OK]`<br><img src="./assets/farmasi/c48-c49-c50-unit.png" width="100%"> |
| C48 (coverage) | Total 4 dari 35 baris terlewat, cabang 100%<br><img src="./assets/farmasi/c48-coverage.png" width="100%"> |
| C49 (coverage) | Total 8 dari 34 baris terlewat, cabang 100%<br><img src="./assets/farmasi/c49-coverage.png" width="100%"> |
| C50 (coverage) | Total 6 dari 26 baris terlewat, cabang 100%<br><img src="./assets/farmasi/c50-coverage.png" width="100%"> |
| C61-UT01 – C61-UT28 | Keluaran `mvn test -Dtest=DataFarmasiBoundaryTest`: 28 test `[OK]`. *Coverage* kelas Sedang ini dilampirkan sebagai informasi: 8 dari 260 baris dan 10 dari 84 cabang terlewat<br><img src="./assets/farmasi/c61-unit.png" width="100%"><br><img src="./assets/farmasi/c61-coverage.png" width="100%"> |
| INT-TC23 – INT-TC26 | Keluaran `mvn test -Dtest=FarmasiIntegrasiTest`: 4 test `[OK]`<br><img src="./assets/farmasi/int-tc23-tc26.png" width="100%"> |
| UC07-TC01 | Berurutan: dasbor Petugas Farmasi, antrean dua resep, rincian resep Siti Aminah beserta bets, dan antrean setelah obat diserahkan<br><img src="./assets/uc07/uc07-tc01-0-dasbor-farmasi.png" width="100%"><br><img src="./assets/uc07/uc07-tc01-1-antrean.png" width="100%"><br><img src="./assets/uc07/uc07-tc01-2-rincian.png" width="100%"><br><img src="./assets/uc07/uc07-tc01-3-diserahkan.png" width="100%"> |
| UC07-TC02 | Berurutan: kolom alasan terbuka, alasan kosong ditolak, alasan diisi, antrean setelah resep dikembalikan, daftar pasien dokter dengan tanda "(resep dikembalikan)", dan layar Resep Elektronik dokter yang memuat alasan pengembalian<br><img src="./assets/uc07/uc07-tc02-1-kolom-alasan.png" width="100%"><br><img src="./assets/uc07/uc07-tc02-2-alasan-wajib.png" width="100%"><br><img src="./assets/uc07/uc07-tc02-3-alasan-diisi.png" width="100%"><br><img src="./assets/uc07/uc07-tc02-4-dikembalikan.png" width="100%"><br><img src="./assets/uc07/uc07-tc02-5-dokter-rekam-medis.png" width="100%"><br><img src="./assets/uc07/uc07-tc02-6-dokter-resep.png" width="100%"> |
| UC07-TC03 | Loket A: rincian resep revisi, lalu antrean setelah obat diserahkan. Loket B: rincian yang sama, lalu panel "Resep sudah selesai dilayani" dengan tombol nonaktif<br><img src="./assets/uc07/uc07-tc03-a-1-rincian.png" width="100%"><br><img src="./assets/uc07/uc07-tc03-a-2-hasil.png" width="100%"><br><img src="./assets/uc07/uc07-tc03-b-1-rincian.png" width="100%"><br><img src="./assets/uc07/uc07-tc03-b-2-hasil.png" width="100%"> |
| UC07 (basis data) | Resep, kunjungan, stok dan bets, bundel FHIR, antrean apotek, dan audit setelah UC07-TC01 sampai UC07-TC03<br><img src="./assets/uc07/uc07-hasil-basis-data.png" width="100%"> |
| UC08-TC01 | Isian penerimaan Glibenklamid, lalu notifikasi tersimpan dan saldo terbaru dengan peringatan Glibenklamid hilang<br><img src="./assets/uc08/uc08-tc01-1-isian.png" width="100%"><br><img src="./assets/uc08/uc08-tc01-2-tersimpan.png" width="100%"> |
| UC08-TC02 | Panel peringatan persediaan saat layar dibuka<br><img src="./assets/uc08/uc08-tc02-peringatan.png" width="100%"> |
| UC08-TC03 | Kolom jumlah dan tanggal kedaluwarsa ditandai merah, isian setelah diperbaiki, dan penerimaan tersimpan<br><img src="./assets/uc08/uc08-tc03-1-ditolak.png" width="100%"><br><img src="./assets/uc08/uc08-tc03-2-diperbaiki-isian.png" width="100%"><br><img src="./assets/uc08/uc08-tc03-3-tersimpan.png" width="100%"> |
| UC08 (basis data) | Penerimaan, bets, stok, dan audit setelah UC08-TC01 sampai UC08-TC03<br><img src="./assets/uc08/uc08-hasil-basis-data.png" width="100%"> |
| KNF03-TC01 | Keluaran `mvn test -Dtest=KnfFarmasiTest` dan hasil ukur 15 percobaan gagal beserta penyerahan kontrol dari laporan Surefire<br><img src="./assets/farmasi/knf03-tc01.png" width="100%"> |
| KNF13-TC01 | Hasil ukur enam kali proses dimatikan paksa beserta hasil setelah dijalankan ulang, dari laporan Surefire test yang sama<br><img src="./assets/farmasi/knf13-tc01.png" width="100%"> |
| *[ID pengujian milik Fachry]* | *[Diisi Fachry: keluaran test runner per kelas, coverage kelas Mendalam, integrasi, use case, dan KNF]* |

<br>

---

# BAB 5: Traceability

Tabel 5.1 memetakan setiap KF ke KNF, *use case*, kelas perancangan, berkas implementasi, dan *test case*. Berkas ditulis relatif terhadap `src/src/main/java/sehati/`. Setiap KF memiliki minimal satu *test case* dan dinyatakan terpenuhi apabila **seluruh** *test case* terkaitnya berkesimpulan `Pass`. Seluruh 24 KF diimplementasikan pada tag `v9.0`, sehingga tidak ada baris "tidak diimplementasikan".

Tabel 5.1. Matriks Kerunutan KF terhadap Implementasi dan Pengujian

| KF ID | KNF Terkait | Use Case ID | Kelas Terkait | Implementasi File Terkait | Test Case (UC) | Test Case (Unit/Integrasi/KNF) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| KF01 | KNF01 | UC02<br>UC03 | C02 - PasienForm<br>C18 - PasienManager<br>C33 - PasienEntity<br>C57 - DataPasienBoundary | `presentation/PasienForm.java`<br>`business/control/PasienManager.java`<br>`dataaccess/DataPasienBoundary.java` | *[UC02-TC.., diisi Steven]* | *[C18-UT.., INT-TC07, INT-TC08, KNF01-TC01; diisi Steven]* |
| KF02 | - | UC02 | C02 - PasienForm<br>C18 - PasienManager<br>C33 - PasienEntity<br>C34 - RekamMedisEntity<br>C57 - DataPasienBoundary | `presentation/PasienForm.java`<br>`business/control/PasienManager.java`<br>`business/entity/PasienEntity.java`<br>`dataaccess/DataPasienBoundary.java` | *[UC02-TC.., diisi Steven]* | *[C18-UT.., C33-UT.., C34-UT.., INT-TC07, INT-TC08; diisi Steven]* |
| KF03 | KNF11 | UC03 | C03 - KunjunganForm<br>C19 - KunjunganManager<br>C35 - KunjunganEntity<br>C36 - PoliEntity<br>C58 - DataKunjunganBoundary | `presentation/KunjunganForm.java`<br>`business/control/KunjunganManager.java`<br>`business/entity/PoliEntity.java`<br>`dataaccess/DataKunjunganBoundary.java` | *[UC03-TC.., diisi Steven]* | *[C19-UT.., C36-UT.., INT-TC09, INT-TC10; diisi Steven]* |
| KF04 | - | UC04 | C04 - AntreanSkriningForm<br>C20 - SkriningManager<br>C37 - AntreanEntity<br>C58 - DataKunjunganBoundary | `presentation/AntreanSkriningForm.java`<br>`business/control/SkriningManager.java`<br>`business/entity/AntreanEntity.java` | UC04-TC01, UC04-TC02 | C20-UT11, C20-UT14 s.d. C20-UT24, C20-UT32, INT-TC11; *[C37-UT.., diisi Steven]* |
| KF05 | - | UC04 | C05 - SkriningForm<br>C20 - SkriningManager<br>C38 - SkriningEntity<br>C39 - TandaVitalEntity<br>C59 - DataKlinisBoundary | `presentation/SkriningForm.java`<br>`business/control/SkriningManager.java`<br>`dataaccess/DataKlinisBoundary.java` | UC04-TC01, UC04-TC03 | C20-UT08 s.d. C20-UT10, C20-UT25 s.d. C20-UT29, C38-UT01 s.d. C38-UT04, C39-UT01 s.d. C39-UT08, C59-UT01 s.d. C59-UT20, INT-TC11, INT-TC12 |
| KF06 | KNF02 | UC04 | C05 - SkriningForm<br>C20 - SkriningManager<br>C39 - TandaVitalEntity | `presentation/SkriningForm.java`<br>`business/control/SkriningManager.java` | UC04-TC03 | C20-UT01 s.d. C20-UT10, C20-UT28, C20-UT29, KNF02-TC01, KNF02-TC02 |
| KF07 | KNF08 | UC04 | C05 - SkriningForm<br>C21 - RisikoManager<br>C40 - AmbangRisikoEntity<br>C41 - PenandaRisikoEntity<br>C51 - DaftarPantauEntity<br>C60 - DataPantauBoundary | `presentation/SkriningForm.java`<br>`business/control/RisikoManager.java`<br>`dataaccess/DataPantauBoundary.java` | UC04-TC04 | C20-UT26, C20-UT27, C21-UT01 s.d. C21-UT16, C40-UT01 s.d. C40-UT06, C41-UT01 s.d. C41-UT05, C60-UT03, INT-TC13, INT-TC14, KNF08-TC01 |
| KF08 | - | UC05 | C06 - RekamMedisForm<br>C22 - PemeriksaanManager<br>C39 - TandaVitalEntity<br>C59 - DataKlinisBoundary | `presentation/RekamMedisForm.java`<br>`business/control/PemeriksaanManager.java` | UC05-TC02, UC05-TC07 | C22-UT04 s.d. C22-UT06 |
| KF09 | - | UC05 | C06 - RekamMedisForm<br>C22 - PemeriksaanManager<br>C34 - RekamMedisEntity<br>C57 - DataPasienBoundary<br>C59 - DataKlinisBoundary | `presentation/RekamMedisForm.java`<br>`business/control/PemeriksaanManager.java`<br>`dataaccess/DataPasienBoundary.java` | UC05-TC01 (Fail pada `v9.0`, Pass setelah perbaikan) | C22-UT01 s.d. C22-UT03, C22-UT35, C22-UT36, INT-TC17 |
| KF10 | KNF05 | UC05 | C07 - PemeriksaanForm<br>C22 - PemeriksaanManager<br>C42 - PemeriksaanEntity<br>C43 - DiagnosisEntity<br>C44 - KodeICD10Entity<br>C59 - DataKlinisBoundary | `presentation/PemeriksaanForm.java`<br>`business/control/PemeriksaanManager.java`<br>`business/entity/KodeICD10Entity.java`<br>`dataaccess/DataKlinisBoundary.java` | UC05-TC03, UC05-TC04, UC05-TC05 | C22-UT07 s.d. C22-UT15, C22-UT20 s.d. C22-UT27, C22-UT31 s.d. C22-UT34, C42-UT01 s.d. C42-UT03, C44-UT01 s.d. C44-UT05, C59-UT21 s.d. C59-UT27, INT-TC17 s.d. INT-TC20 |
| KF11 | - | UC06 | C08 - ResepForm<br>C23 - ResepManager<br>C46 - ResepEntity<br>C47 - ItemResepEntity<br>C48 - ObatEntity<br>C61 - DataFarmasiBoundary | `presentation/ResepForm.java`<br>`business/control/ResepManager.java`<br>`business/entity/ResepEntity.java`<br>`dataaccess/DataFarmasiBoundary.java` | UC06-TC01, UC06-TC02 | C23-UT01 s.d. C23-UT25, C46-UT01 s.d. C46-UT04, C47-UT01 s.d. C47-UT03, INT-TC21, INT-TC22 |
| KF12 | - | UC05<br>UC09 | C07 - PemeriksaanForm<br>C22 - PemeriksaanManager<br>C45 - JadwalKontrolEntity<br>C21 - RisikoManager<br>C26 - PantauManager<br>C51 - DaftarPantauEntity<br>C59 - DataKlinisBoundary<br>C60 - DataPantauBoundary | `presentation/PemeriksaanForm.java`<br>`business/control/PemeriksaanManager.java`<br>`business/control/RisikoManager.java`<br>`dataaccess/DataKlinisBoundary.java` | UC05-TC03, UC05-TC08 (Fail pada `v9.0`, Pass setelah perbaikan); UC09-TC01 | C22-UT16 s.d. C22-UT19, C22-UT23, C22-UT28 s.d. C22-UT30, C22-UT37 (Fail pada `v9.0`, Pass setelah perbaikan), C45-UT01 s.d. C45-UT03, C59-UT28 s.d. C59-UT32, INT-TC19; C21-UT17, C21-UT18, C26-UT16 s.d. C26-UT26, C60-UT01 s.d. C60-UT09, INT-TC15, INT-TC16 |
| KF13 | - | UC07 | C09 - AntreanResepForm<br>C10 - PenyerahanObatForm<br>C24 - ApotekManager<br>C46 - ResepEntity<br>C61 - DataFarmasiBoundary | `presentation/AntreanResepForm.java`<br>`presentation/PenyerahanObatForm.java`<br>`business/control/ApotekManager.java` | UC07-TC01, UC07-TC02 | C24-UT01 s.d. C24-UT08, C24-UT23 s.d. C24-UT37, C46-UT01 s.d. C46-UT04, C61-UT15 s.d. C61-UT23, INT-TC23, INT-TC24 |
| KF14 | KNF03<br>KNF07 | UC07 | C10 - PenyerahanObatForm<br>C24 - ApotekManager<br>C48 - ObatEntity<br>C49 - BetsObatEntity<br>C54 - BundelFHIREntity<br>C61 - DataFarmasiBoundary<br>C63 - DataSinkronisasiBoundary | `business/control/ApotekManager.java`<br>`business/control/PenyusunBundelFHIR.java`<br>`dataaccess/DataFarmasiBoundary.java` | UC07-TC01, UC07-TC03 | C24-UT08 s.d. C24-UT22, C48-UT01 s.d. C48-UT04, C49-UT01 s.d. C49-UT04, C49-UT08 s.d. C49-UT13, C61-UT04 s.d. C61-UT09, C22-UT20, INT-TC19 (bundel kunjungan tanpa resep), INT-TC23, INT-TC24, KNF03-TC01; *[KNF07-TC01; diisi Fachry]* |
| KF15 | - | UC12 | C16 - KonfigurasiForm<br>C29 - KonfigurasiManager<br>C30 - PenggunaEntity<br>C36 - PoliEntity<br>C40 - AmbangRisikoEntity<br>C48 - ObatEntity<br>C56 - DataAkunBoundary | `presentation/KonfigurasiForm.java`<br>`business/control/KonfigurasiManager.java`<br>`dataaccess/DataAkunBoundary.java` | UC12-TC01, UC12-TC03, UC12-TC04 | C29-UT01 s.d. C29-UT27, C30-UT01 s.d. C30-UT11, C56-UT01 s.d. C56-UT03, C56-UT09, INT-TC03, INT-TC04 |
| KF16 | KNF04<br>KNF05 | UC01 | C01 - LoginForm<br>C17 - AutentikasiManager<br>C30 - PenggunaEntity<br>C31 - SesiEntity<br>C32 - AuditEntity<br>C56 - DataAkunBoundary | `presentation/LoginForm.java`<br>`presentation/Navigasi.java`<br>`business/control/AutentikasiManager.java`<br>`common/SesiAktif.java` | UC01-TC01 s.d. UC01-TC04, UC12-TC04 | C17-UT01 s.d. C17-UT17 (C17-UT17 Fail pada `v9.0`, Pass setelah perbaikan), C30-UT01 s.d. C30-UT05, C31-UT01 s.d. C31-UT07, C32-UT01 s.d. C32-UT03, C56-UT04 s.d. C56-UT08, C22-UT15, C22-UT27, C23-UT21, C29-UT10 (pembatasan peran pada operasi tulis), INT-TC01, INT-TC02, KNF04-TC01, KNF05-TC01 |
| KF17 | - | UC08 | C11 - PersediaanForm<br>C25 - PersediaanManager<br>C48 - ObatEntity<br>C49 - BetsObatEntity<br>C50 - PenerimaanObatEntity<br>C61 - DataFarmasiBoundary | `presentation/PersediaanForm.java`<br>`business/control/PersediaanManager.java`<br>`dataaccess/DataFarmasiBoundary.java` | UC08-TC01 s.d. UC08-TC03 | C25-UT01 s.d. C25-UT20, C48-UT05 s.d. C48-UT10, C49-UT05 s.d. C49-UT07, C50-UT01 s.d. C50-UT03, C61-UT10 s.d. C61-UT14, INT-TC25, INT-TC26 |
| KF18 | - | UC09 | C12 - DaftarPantauForm<br>C26 - PantauManager<br>C21 - RisikoManager<br>C51 - DaftarPantauEntity<br>C52 - TindakLanjutEntity<br>C60 - DataPantauBoundary | `presentation/DaftarPantauForm.java`<br>`business/control/PantauManager.java`<br>`dataaccess/DataPantauBoundary.java` | UC09-TC01 s.d. UC09-TC03 | C26-UT01 s.d. C26-UT26, C51-UT01 s.d. C51-UT06, C60-UT01 s.d. C60-UT09, C21-UT17, C21-UT18, INT-TC15, INT-TC16 |
| KF19 | - | UC10 | C13 - LaporanForm<br>C27 - LaporanManager<br>C53 - LaporanEntity<br>C62 - DataLaporanBoundary | `presentation/LaporanForm.java`<br>`business/control/LaporanManager.java`<br>`dataaccess/DataLaporanBoundary.java` | *[UC10-TC.., diisi Fachry]* | *[C27-UT.., C53-UT.., INT-TC27, INT-TC28; diisi Fachry]* |
| KF20 | KNF07 | UC11 | C14 - SinkronisasiForm<br>C15 - SATUSEHATGateway<br>C28 - SinkronisasiManager<br>C54 - BundelFHIREntity<br>C63 - DataSinkronisasiBoundary | `presentation/SinkronisasiForm.java`<br>`business/control/SinkronisasiManager.java`<br>`dataaccess/SATUSEHATGateway.java`<br>`dataaccess/DataSinkronisasiBoundary.java` | *[UC11-TC.., diisi Fachry]* | *[C15-UT.., C28-UT.., C54-UT.., C63-UT.., INT-TC29 s.d. INT-TC32, KNF07-TC01; diisi Fachry]* |
| KF21 | KNF06 | UC12 | C16 - KonfigurasiForm<br>C29 - KonfigurasiManager<br>C55 - PencadanganEntity<br>C64 - DataPencadanganBoundary | `presentation/KonfigurasiForm.java`<br>`business/control/KonfigurasiManager.java`<br>`dataaccess/DataPencadanganBoundary.java` | UC12-TC02, UC12-TC05 | C29-UT28 s.d. C29-UT37, C64-UT01 s.d. C64-UT16, INT-TC05, INT-TC06, KNF06-TC01 |
| KF22 | KNF09<br>KNF10<br>KNF13 | UC05<br>UC11 | C14 - SinkronisasiForm<br>C15 - SATUSEHATGateway<br>C28 - SinkronisasiManager | `presentation/BilahAplikasi.java`<br>`presentation/Navigasi.java`<br>`business/control/SinkronisasiManager.java`<br>`dataaccess/SATUSEHATGateway.java` | UC05-TC06; *[UC11-TC.., diisi Fachry]* | KNF13-TC01; *[C15-UT.., C28-UT.., KNF09-TC01, KNF10-TC01; diisi Fachry dan Steven]* |
| KF23 | KNF11 | UC03<br>UC07 | C19 - KunjunganManager<br>C24 - ApotekManager<br>C36 - PoliEntity<br>C58 - DataKunjunganBoundary<br>C61 - DataFarmasiBoundary | `business/control/KunjunganManager.java`<br>`business/control/ApotekManager.java`<br>`dataaccess/DataKunjunganBoundary.java`<br>`dataaccess/DataFarmasiBoundary.java` | UC07-TC03; *[UC03-TC.., diisi Steven]* | C24-UT11, C61-UT04, C61-UT21 (penguncian stok dan resep), INT-TC22 (penguncian antrean apotek); *[INT-TC33, INT-TC34, KNF11-TC01; diisi Steven]* |
| KF24 | KNF12 | UC04<br>UC05 | C05 - SkriningForm<br>C07 - PemeriksaanForm | `presentation/SkriningForm.java`<br>`presentation/PemeriksaanForm.java`<br>`presentation/Navigasi.java`<br>`presentation/TabelPintasan.java` | UC05-TC03 (Ctrl+S); UC04-TC01 (F2, Tab, Ctrl+S), UC04-TC02 (F3) | KNF12-TC01 |

Pada `v9.0`, tiga KF belum terpenuhi karena memiliki *test case* `Fail`:

- **KF09** (UC05-TC01): hasil skrining hari itu belum tampil pada ringkasan rekam medis I-06.
- **KF12** (UC05-TC08, C22-UT37): jadwal kontrol pada tanggal yang sama dapat dibuat dua kali setelah jadwal pertama masuk daftar pantau.
- **KF16** (C17-UT17): `periksaHakAkses()` melempar `NullPointerException` untuk nama layar `null` alih-alih menolak akses.

Ketiganya sudah diperbaiki (Tabel 3.5) dan diuji ulang dengan hasil `Pass`. Bagian KF12 milik UC09 sudah diuji (UC09-TC01) dan berkesimpulan `Pass`. Seluruh *test case* modul farmasi berkesimpulan `Pass`, sehingga KF13 dan KF17 terpenuhi, sedangkan KF14 dan KF23 masih menunggu KNF07-TC01 dan pengujian loket. KF lain menunggu hasil pengujian setiap modul.

Tabel 5.2. Matriks Kerunutan KNF terhadap Pengujian

| ID KNF | Parameter | KF/UC Terkait | Test Case |
| :--- | :--- | :--- | :--- |
| KNF01 | Response time | KF01 / UC02 | KNF01-TC01 |
| KNF02 | Reliability | KF06 / UC04 | KNF02-TC01, KNF02-TC02 |
| KNF03 | Reliability | KF14 / UC07 | KNF03-TC01 |
| KNF04 | Security | KF16 / UC01 | KNF04-TC01 |
| KNF05 | Security | KF10, KF16 / UC01, UC05, seluruh UC dengan perubahan data | KNF05-TC01 |
| KNF06 | Reliability | KF21 / UC12 | KNF06-TC01 |
| KNF07 | Portability | KF14, KF20 / UC07, UC11 | KNF07-TC01 |
| KNF08 | Ergonomy | KF07 / UC04 | KNF08-TC01 |
| KNF09 | Availability | KF22 / UC05, UC11 | KNF09-TC01 |
| KNF10 | Portability | KF22 / - | KNF10-TC01 |
| KNF11 | Reliability | KF23 / UC03, UC07 | KNF11-TC01 |
| KNF12 | Ergonomy | KF24 / UC04 | KNF12-TC01 |
| KNF13 | Reliability | KF22 / - | KNF13-TC01 |
