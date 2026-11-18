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
| A | Versi pertama HIUPL. BAB 1 dan BAB 2 disusun dari DPPL Tugas 8 dan SKPL Tugas 5, ditambah istilah dan pola ID pengujian. BAB 3 merangkum implementasi pada tag `v9.0` (Tugas 9) beserta dua perbaikan kode hasil pengujian (Tabel 3.5). BAB 4 dan BAB 5 berisi hasil pengujian dan matriks kerunutan yang baru disusun pada dokumen ini. |

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

Gambar 2.2 memodelkan dua belas *use case* SEHATI dengan tiga aktor manusia: Petugas Administrasi (A01), Tenaga Klinis yang mencakup perawat dan dokter (A02), serta Petugas Farmasi (A03). SATUSEHAT tampil sebagai aktor sistem eksternal pada UC11. UC03 Mengelola Kunjungan Pasien selalu menjalankan UC02 Mengelola Data Pasien («include») karena kunjungan tidak dapat dibuka tanpa pasiennya. UC06 Menyusun Resep Elektronik memperluas UC05 Melakukan Pemeriksaan Medis («extend») karena resep hanya disusun bila pasien butuh obat. Kotak sistem membatasi *use case* yang dijalankan SEHATI dari aktor di luarnya.

<p align="center">
<img alt="Use Case Diagram SEHATI" src="../M5/assets/diagram/diagram-uc.svg" width="80%">
</p>
<p align="center">
<i>Gambar 2.2. Use Case Diagram SEHATI</i>
</p>

<br>

---

# BAB 3: Implementasi

Bab ini mencatat elemen SEHATI yang dibangun pada Tugas 9: lingkungan, kelas, layar, dan berkas pendukung. Seluruhnya mengacu pada tag `v9.0` (commit `4777977`). Perbedaan implementasi dari rancangan DPPL sudah dicatat lengkap pada Tabel B.1 Laporan Implementasi Tugas 9 (95 baris), sehingga tidak diulang di sini. Setelah `v9.0` ada dua jenis perubahan. Pertama, berkas pengujian dan `pom.xml` (JaCoCo dan pelapor test berbasis ID) untuk BAB 4. Kedua, dua perbaikan kode atas temuan `Fail` pada BAB 4, yang dicatat pada Tabel 3.5 karena salah satunya menambah objek pada rancangan antarmuka I-06.

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
| 15 | `src/tests/sehati/**/*Test.java` (47 kelas test) | Test JUnit per kelas pada paket yang sama dengan kelas yang diuji, test integrasi pada `src/tests/sehati/integrasi/`, serta `AlurUtamaTest` dan `KonkurensiTest` |
| 16 | `src/tests/sehati/dataaccess/JdbcPalsu.java`, `src/tests/sehati/DataUjiAlur.java`, `src/tests/sehati/PratinjauLayar.java` | Pendukung test: pengganti JDBC untuk uji tanpa basis data, penyiap data alur, dan pembuka satu layar tanpa masuk |
| 17 | `src/README.md` | Cara memasang, menjalankan aplikasi, dan menjalankan test |
| 18 | `.github/workflows/` | CI GitHub Actions yang menjalankan `mvn test` pada setiap PR |

Tabel 3.5. Perubahan Implementasi setelah `v9.0` dari Hasil Pengujian

| No | Bagian DPPL | Rancangan / Implementasi `v9.0` | Implementasi Akhir | Alasan |
| :--- | :--- | :--- | :--- | :--- |
| 1 | C22 `jadwalkanKontrol()`, C59 (kueri baru Q-C59-20) | Jadwal pasien pada tanggal yang sama dicari lewat `ambilJadwalKontrol()` (Q-C59-17), yang hanya memuat jadwal yang belum memicu entri daftar pantau | Dicari lewat `DataKlinisBoundary.jadwalPadaTanggal()` dengan kueri baru Q-C59-20: jadwal "terjadwal" berdasarkan `id_pasien` dan `tanggal_rencana`, tanpa syarat daftar pantau | Temuan C22-UT37 dan UC05-TC08: jadwal kontrol dan entri daftar pantau dapat ganda (KF12) |
| 2 | C06 RekamMedisForm, I-06 (DPPL Subbab 6.6) | I-06 hanya memuat panel data diri, penanda risiko, diagnosis lampau, dan riwayat obat | Panel "Skrining hari ini" ditambahkan di bawah data diri, berisi keluhan awal dan tanda vital dari `RekamMedisEntity.getSkriningHariIni()` | Temuan UC05-TC01: SKPL UC05 langkah 1 meminta hasil skrining hari itu tampil pada ringkasan (KF09) |

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
| C01 | LoginForm | Tidak perlu | Kelas tampilan layar I-01, meneruskan kredensial ke AutentikasiManager | - (teruji lewat UC01) |
| C02 | PasienForm | Tidak perlu | Kelas tampilan layar I-02, meneruskan pencarian dan isian ke PasienManager | - (teruji lewat UC02) |
| C03 | KunjunganForm | Tidak perlu | Kelas tampilan layar I-03, meneruskan pilihan poli ke KunjunganManager | - (teruji lewat UC03) |
| C04 | AntreanSkriningForm | Tidak perlu | Kelas tampilan layar I-04, menampilkan antrean dari SkriningManager | - (teruji lewat UC04) |
| C05 | SkriningForm | Tidak perlu | Kelas tampilan layar I-05; validasi rentang dijalankan SkriningManager | - (teruji lewat UC04) |
| C06 | RekamMedisForm | Tidak perlu | Kelas tampilan layar I-06, menampilkan ringkasan dan tren dari PemeriksaanManager | - (teruji lewat UC05-TC01, UC05-TC02, UC05-TC04, UC05-TC06, UC05-TC07) |
| C07 | PemeriksaanForm | Tidak perlu | Kelas tampilan layar I-07, meneruskan isian ke PemeriksaanManager | - (teruji lewat UC05-TC03, UC05-TC05, UC05-TC06, UC05-TC08) |
| C08 | ResepForm | Tidak perlu | Kelas tampilan layar I-08, meneruskan item ke ResepManager | - (teruji lewat UC06-TC01, UC06-TC02) |
| C09 | AntreanResepForm | Tidak perlu | Kelas tampilan layar I-09, menampilkan antrean dari ApotekManager | - (teruji lewat UC07) |
| C10 | PenyerahanObatForm | Tidak perlu | Kelas tampilan layar I-10, meneruskan konfirmasi ke ApotekManager | - (teruji lewat UC07) |
| C11 | PersediaanForm | Tidak perlu | Kelas tampilan layar I-11, meneruskan penerimaan ke PersediaanManager | - (teruji lewat UC08) |
| C12 | DaftarPantauForm | Tidak perlu | Kelas tampilan layar I-12, meneruskan penyaring dan tindak lanjut ke PantauManager | - (teruji lewat UC09) |
| C13 | LaporanForm | Tidak perlu | Kelas tampilan layar I-13, meneruskan rentang dan ekspor ke LaporanManager | - (teruji lewat UC10) |
| C14 | SinkronisasiForm | Tidak perlu | Kelas tampilan layar I-14, meneruskan perintah ke SinkronisasiManager | - (teruji lewat UC11) |
| C15 | SATUSEHATGateway | Mendalam | Integrasi eksternal ke REST API SATUSEHAT, termasuk mode simulasi dan penanganan galat jaringan (Algo-C15-01, Algo-C15-02) | *[C15-UT01 s.d. C15-UT.., diisi Fachry]* |
| C16 | KonfigurasiForm | Tidak perlu | Kelas tampilan layar I-15, meneruskan perubahan ke KonfigurasiManager | - (teruji lewat UC12) |
| C17 | AutentikasiManager | Mendalam | Keamanan: verifikasi kredensial bcrypt, sesi, batas diam 15 menit, dan hak akses peran (Algo-C17-01, Algo-C17-02) | *[C17-UT01 s.d. C17-UT.., diisi Axeleon]* |
| C18 | PasienManager | Mendalam | Validasi NIK 16 digit dan keunikannya serta penerbitan nomor rekam medis (Algo-C18-01, Algo-C18-02) | *[C18-UT01 s.d. C18-UT.., diisi Steven]* |
| C19 | KunjunganManager | Mendalam | Penolakan kunjungan ganda dan penerbitan nomor antrean per poli per hari dalam transaksi (Algo-C19-01, Algo-C19-02) | *[C19-UT01 s.d. C19-UT.., diisi Steven]* |
| C20 | SkriningManager | Mendalam | Validasi rentang fisiologis tanpa jalan pintas dan alur antrean skrining (Algo-C20-01 s.d. Algo-C20-03) | *[C20-UT01 s.d. C20-UT.., diisi Humam]* |
| C21 | RisikoManager | Mendalam | Perhitungan risiko terhadap ambang dan riwayat tiga kunjungan (Algo-C21-01, Algo-C21-02) | *[C21-UT01 s.d. C21-UT.., diisi Humam]* |
| C22 | PemeriksaanManager | Mendalam | Validasi diagnosis ICD-10 dan tanggal kontrol, pembatasan peran Dokter, serta transaksi penguncian rekam medis (Algo-C22-01, Algo-C22-02) | C22-UT01 s.d. C22-UT37, INT-TC17 s.d. INT-TC20 |
| C23 | ResepManager | Mendalam | Validasi kecukupan stok per obat (termasuk bets kedaluwarsa) dan transaksi pengiriman resep (Algo-C23-01, Algo-C23-02) | C23-UT01 s.d. C23-UT24, INT-TC21, INT-TC22 |
| C24 | ApotekManager | Mendalam | Transaksi penyerahan obat: potong stok FEFO, tutup kunjungan, dan susun bundel FHIR (Algo-C24-01, Algo-C24-02) | *[C24-UT01 s.d. C24-UT.., diisi Malik]* |
| C25 | PersediaanManager | Mendalam | Validasi penerimaan obat, penambahan stok, dan peringatan stok serta kedaluwarsa (Algo-C25-01 s.d. Algo-C25-03) | *[C25-UT01 s.d. C25-UT.., diisi Malik]* |
| C26 | PantauManager | Mendalam | Penyaringan daftar pantau dan perubahan status tindak lanjut (Algo-C26-01) | *[C26-UT01 s.d. C26-UT.., diisi Humam]* |
| C27 | LaporanManager | Mendalam | Perhitungan rekapitulasi dan validasi rentang tanggal (Algo-C27-01, Algo-C27-02) | *[C27-UT01 s.d. C27-UT.., diisi Fachry]* |
| C28 | SinkronisasiManager | Mendalam | Aturan kirim saat daring serta tahan dan ekspor saat luring (Algo-C28-01, Algo-C28-02) | *[C28-UT01 s.d. C28-UT.., diisi Fachry]* |
| C29 | KonfigurasiManager | Mendalam | Validasi akun (nama unik, sandi minimal 8 karakter, peran), data master, dan pencadangan (Algo-C29-01 s.d. Algo-C29-03) | *[C29-UT01 s.d. C29-UT.., diisi Axeleon]* |
| C30 | PenggunaEntity | Mendalam | Keamanan: pencocokan kata sandi bcrypt dan status aktif akun (Algo-C30-01) | *[C30-UT01 s.d. C30-UT.., diisi Axeleon]* |
| C31 | SesiEntity | Mendalam | Keamanan: batas diam sesi pada habisWaktu() (Algo-C31-01) | *[C31-UT01 s.d. C31-UT.., diisi Axeleon]* |
| C32 | AuditEntity | Sedang | buat() hanya menolak deskripsi kosong lalu membentuk catatan (Algo-C32-01) | *[C32-UT.. atau INT-TC.., diisi Axeleon]* |
| C33 | PasienEntity | Sedang | perbarui() hanya menolak data kosong lalu menyalin atribut (Algo-C33-01) | *[C33-UT.. atau INT-TC.., diisi Steven]* |
| C34 | RekamMedisEntity | Sedang | tambahEntri() hanya menolak catatan kosong (Algo-C34-01) | *[C34-UT.. atau INT-TC.., diisi Steven]* |
| C35 | KunjunganEntity | Mendalam | Aturan transisi status kunjungan dan penguncian status selesai (Algo-C35-01) | *[C35-UT01 s.d. C35-UT.., diisi Steven]* |
| C36 | PoliEntity | Mendalam | Perhitungan nomor antrean berikutnya yang dimulai ulang saat hari berganti (Algo-C36-01) | *[C36-UT01 s.d. C36-UT.., diisi Steven]* |
| C37 | AntreanEntity | Mendalam | Aturan urutan antrean: terdepan, pindah ke belakang, dan keluarkan (Algo-C37-01) | *[C37-UT01 s.d. C37-UT.., diisi Steven]* |
| C38 | SkriningEntity | Sedang | lampirkanTandaVital() hanya menolak tanda vital kosong | *[C38-UT.. atau INT-TC.., diisi Humam]* |
| C39 | TandaVitalEntity | Mendalam | Perhitungan IMT dan penolakan tinggi badan kosong (Algo-C39-01) | *[C39-UT01 s.d. C39-UT.., diisi Humam]* |
| C40 | AmbangRisikoEntity | Mendalam | Perbandingan nilai terhadap ambang klinis tepat di nilai batas (Algo-C40-01) | *[C40-UT01 s.d. C40-UT.., diisi Humam]* |
| C41 | PenandaRisikoEntity | Mendalam | Aturan status: penanda yang sudah dicabut tidak dapat dicabut lagi | *[C41-UT01 s.d. C41-UT.., diisi Humam]* |
| C42 | PemeriksaanEntity | Mendalam | Aturan status: pemeriksaan yang sudah dikunci tidak dapat dikunci lagi | C42-UT01 s.d. C42-UT03 |
| C43 | DiagnosisEntity | Tidak perlu | Hanya menyimpan diagnosis; ringkas() menggabungkan kode dan deskripsi tanpa percabangan | - (teruji lewat UC05-TC01, UC05-TC03) |
| C44 | KodeICD10Entity | Mendalam | Pencocokan kata kunci ICD-10 tanpa membedakan huruf (Algo-C44-01) | C44-UT01 s.d. C44-UT05 |
| C45 | JadwalKontrolEntity | Mendalam | Aturan status: hanya jadwal terjadwal yang dapat ditandai selesai | C45-UT01 s.d. C45-UT03 |
| C46 | ResepEntity | Mendalam | Aturan transisi status resep menunggu, selesai dilayani, dan dikembalikan (Algo-C46-01) | C46-UT01 s.d. C46-UT04 |
| C47 | ItemResepEntity | Sedang | cukupStok() berisi satu aturan stok ≥ jumlah tanpa percabangan; aturan lengkapnya diuji pada C23 | C47-UT01 s.d. C47-UT03, INT-TC21 |
| C48 | ObatEntity | Mendalam | Validasi pengurangan dan penambahan stok serta ambang minimum (Algo-C48-01) | *[C48-UT01 s.d. C48-UT.., diisi Malik]* |
| C49 | BetsObatEntity | Mendalam | Pengurangan bets dan batas 90 hari menjelang kedaluwarsa (Algo-C49-01) | *[C49-UT01 s.d. C49-UT.., diisi Malik]* |
| C50 | PenerimaanObatEntity | Mendalam | Perhitungan total jumlah seluruh bets (Algo-C50-01) | *[C50-UT01 s.d. C50-UT.., diisi Malik]* |
| C51 | DaftarPantauEntity | Mendalam | Aturan transisi status tindak lanjut (Algo-C51-01) | *[C51-UT01 s.d. C51-UT.., diisi Humam]* |
| C52 | TindakLanjutEntity | Tidak perlu | Hanya menyimpan data tindak lanjut; terhubung() membandingkan satu atribut | - (teruji lewat UC09) |
| C53 | LaporanEntity | Mendalam | kosong() mengurai ringkasan rekapitulasi dengan beberapa cabang (Algo-C53-01) | *[C53-UT01 s.d. C53-UT.., diisi Fachry]* |
| C54 | BundelFHIREntity | Mendalam | Aturan status kirim: bundel terkirim tidak dapat ditandai ulang (Algo-C54-01) | *[C54-UT01 s.d. C54-UT.., diisi Fachry]* |
| C55 | PencadanganEntity | Tidak perlu | Hanya menyimpan catatan pencadangan; berhasil() membaca satu status | - (teruji lewat UC12) |
| C56 | DataAkunBoundary | Sedang | Kueri akun, sesi, dan audit (Q-C56-01 s.d. Q-C56-10) | *[C56-UT.. atau INT-TC.., diisi Axeleon]* |
| C57 | DataPasienBoundary | Sedang | Kueri pasien dan pencarian parsial (Q-C57-01 s.d. Q-C57-12, Algo-C57-01) | *[C57-UT.. atau INT-TC.., diisi Steven]* |
| C58 | DataKunjunganBoundary | Sedang | Kueri kunjungan, poli, dan antrean dengan penguncian baris (Q-C58-01 s.d. Q-C58-13) | *[C58-UT.. atau INT-TC.., diisi Steven]* |
| C59 | DataKlinisBoundary | Sedang | Kueri skrining, risiko, pemeriksaan, ICD-10, dan jadwal kontrol (Q-C59-01 s.d. Q-C59-20) | C59-UT21 s.d. C59-UT32, INT-TC17 s.d. INT-TC20 (bagian pemeriksaan); *[C59-UT01 s.d. C59-UT20 bagian skrining, diisi Humam]* |
| C60 | DataPantauBoundary | Sedang | Kueri daftar pantau dan tindak lanjut (Q-C60-01 s.d. Q-C60-07) | *[C60-UT.. atau INT-TC.., diisi Humam]* |
| C61 | DataFarmasiBoundary | Sedang | Kueri obat, bets, dan resep dengan penguncian stok (Q-C61-01 s.d. Q-C61-19) | *[C61-UT.. atau INT-TC.., diisi Malik]* |
| C62 | DataLaporanBoundary | Sedang | Kueri agregasi dan penulisan berkas PDF/CSV (Q-C62-01 s.d. Q-C62-04); logika rekapitulasi diuji pada C27 | *[C62-UT.. atau INT-TC.., diisi Fachry]* |
| C63 | DataSinkronisasiBoundary | Mendalam | Keamanan: enkripsi arsip ekspor bundel (Algo-C63-01) di samping penyimpanan bundel | *[C63-UT01 s.d. C63-UT.., diisi Fachry]* |
| C64 | DataPencadanganBoundary | Mendalam | Integrasi proses eksternal pg_dump dan verifikasi arsip (Algo-C64-01, Algo-C64-02) | *[C64-UT01 s.d. C64-UT.., diisi Axeleon]* |

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

*[Diisi Axeleon: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

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

*[Diisi Humam: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.6 Kelas RisikoManager

**ID Kelas:** C21  
**Nama Kelas:** RisikoManager  
**Tingkat:** Mendalam

*[Diisi Humam: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

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

`muatDataObat()` tidak menerima masukan, sehingga hanya punya kasus normal dan batas. Kasus negatifnya (basis data tidak dapat dihubungi) tidak dapat dipicu tanpa mengubah konfigurasi koneksi, yang dibaca sekali saat kelas `KoneksiBasisData` dimuat. `validasiStok()` bervisibilitas private pada DPPL dan package-private pada implementasi (Tabel B.1 nomor 56), lalu diuji langsung karena memuat inti Algo-C23-01. `alasanDikembalikan()` adalah operasi public tambahan (Tabel B.1 nomor 70).

Tabel 4.2.8b. Ringkasan Cakupan Kelas C23

| Informasi | Keterangan |
| :--- | :--- |
| Daftar operasi public pada DPPL | `muatDataObat()`, `tambahItem()`, `kirimKeApotek()` (ditambah `validasiStok()` yang private) |
| Operasi yang diuji | Ketiga operasi public DPPL (3 dari 3), `validasiStok()`, dan operasi tambahan `alasanDikembalikan()` |
| ID Algoritma yang tercakup | Algo-C23-01 (penambahan item dan pemeriksaan stok: C23-UT03 s.d. C23-UT12), Algo-C23-02 (pengiriman resep ke apotek: C23-UT13 s.d. C23-UT21) |
| Coverage (wajib ≥ 70%) | 85,5% baris (59 dari 69) dan 95,8% cabang (23 dari 24), JaCoCo 0.8.14 dari seluruh `mvn test`. Bukti pada Subbab 4.6 |

### 4.2.9 Kelas ApotekManager

**ID Kelas:** C24  
**Nama Kelas:** ApotekManager  
**Tingkat:** Mendalam

*[Diisi Malik: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.10 Kelas PersediaanManager

**ID Kelas:** C25  
**Nama Kelas:** PersediaanManager  
**Tingkat:** Mendalam

*[Diisi Malik: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.11 Kelas PantauManager

**ID Kelas:** C26  
**Nama Kelas:** PantauManager  
**Tingkat:** Mendalam

*[Diisi Humam: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

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

*[Diisi Axeleon: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.15 Kelas PenggunaEntity

**ID Kelas:** C30  
**Nama Kelas:** PenggunaEntity  
**Tingkat:** Mendalam

*[Diisi Axeleon: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.16 Kelas SesiEntity

**ID Kelas:** C31  
**Nama Kelas:** SesiEntity  
**Tingkat:** Mendalam

*[Diisi Axeleon: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.17 Kelas AuditEntity

**ID Kelas:** C32  
**Nama Kelas:** AuditEntity  
**Tingkat:** Sedang

*[Diisi Axeleon: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

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

*[Diisi Humam: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

### 4.2.24 Kelas TandaVitalEntity

**ID Kelas:** C39  
**Nama Kelas:** TandaVitalEntity  
**Tingkat:** Mendalam

*[Diisi Humam: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.25 Kelas AmbangRisikoEntity

**ID Kelas:** C40  
**Nama Kelas:** AmbangRisikoEntity  
**Tingkat:** Mendalam

*[Diisi Humam: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.26 Kelas PenandaRisikoEntity

**ID Kelas:** C41  
**Nama Kelas:** PenandaRisikoEntity  
**Tingkat:** Mendalam

*[Diisi Humam: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

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

*[Diisi Malik: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.33 Kelas BetsObatEntity

**ID Kelas:** C49  
**Nama Kelas:** BetsObatEntity  
**Tingkat:** Mendalam

*[Diisi Malik: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.34 Kelas PenerimaanObatEntity

**ID Kelas:** C50  
**Nama Kelas:** PenerimaanObatEntity  
**Tingkat:** Mendalam

*[Diisi Malik: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

### 4.2.35 Kelas DaftarPantauEntity

**ID Kelas:** C51  
**Nama Kelas:** DaftarPantauEntity  
**Tingkat:** Mendalam

*[Diisi Humam: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

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

*[Diisi Axeleon: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

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

*[Diisi Humam: baris C59-UT01 sampai C59-UT20 untuk bagian skrining dan risiko.]*

Bagian pemeriksaan diuji pada berkas `src/tests/sehati/dataaccess/DataKlinisBoundaryPemeriksaanTest.java`. Setiap test berjalan dalam transaksi yang di-*rollback*. Penyimpanan pemeriksaan bersama `PemeriksaanManager` juga diuji lewat INT-TC17 sampai INT-TC20.

Tabel 4.2.41. Hasil Pengujian Unit Kelas C59 (Bagian Pemeriksaan)

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

*[Diisi Humam: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

### 4.2.43 Kelas DataFarmasiBoundary

**ID Kelas:** C61  
**Nama Kelas:** DataFarmasiBoundary  
**Tingkat:** Sedang

*[Diisi Malik: tabel hasil pengujian unit, mengikuti pola Subbab 4.2.7.]*

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

*[Diisi Axeleon: tabel hasil pengujian unit dan tabel ringkasan cakupan, mengikuti pola Subbab 4.2.7.]*

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
| INT-TC01 | IK-01 | *[Diisi Axeleon]* | | | | |
| INT-TC02 | IK-01 | *[Diisi Axeleon]* | | | | |
| INT-TC03 | IK-02 | *[Diisi Axeleon]* | | | | |
| INT-TC04 | IK-02 | *[Diisi Axeleon]* | | | | |
| INT-TC05 | IK-03 | *[Diisi Axeleon]* | | | | |
| INT-TC06 | IK-03 | *[Diisi Axeleon]* | | | | |
| INT-TC07 | IK-04 | *[Diisi Steven]* | | | | |
| INT-TC08 | IK-04 | *[Diisi Steven]* | | | | |
| INT-TC09 | IK-05 | *[Diisi Steven]* | | | | |
| INT-TC10 | IK-05 | *[Diisi Steven]* | | | | |
| INT-TC11 | IK-06 | *[Diisi Humam]* | | | | |
| INT-TC12 | IK-06 | *[Diisi Humam]* | | | | |
| INT-TC13 | IK-07 | *[Diisi Humam]* | | | | |
| INT-TC14 | IK-07 | *[Diisi Humam]* | | | | |
| INT-TC15 | IK-08 | *[Diisi Humam]* | | | | |
| INT-TC16 | IK-08 | *[Diisi Humam]* | | | | |
| INT-TC17 | IK-09 | Normal: ringkasan rekam medis dan penyimpanan pemeriksaan | Kunjungan seed "siap diperiksa dokter" (id 9). Panggil `PemeriksaanManager.susunRingkasan(9)`, lalu `simpanPemeriksaan(9, anamnesis "Pusing dua hari", tindakan "Edukasi diet garam", diagnosis I10)`. Bandingkan dengan isi tabel `pasien`, `skrining`, `penanda_risiko`, `pemeriksaan`, dan `diagnosis` | Nama pasien, keluhan skrining, dan jumlah penanda aktif sama dengan isi tabel. Baris pemeriksaan tersimpan utuh dengan diagnosis I10 dan belum terkunci | Nama pasien, keluhan awal, dan jumlah penanda aktif sama dengan basis data. Kueri gabungan `pemeriksaan` dan `diagnosis` mengembalikan `Pusing dua hari\|Edukasi diet garam\|false\|I10` | Pass |
| INT-TC18 | IK-09 | Gagal: kode diagnosis di luar data master ICD-10 | `simpanPemeriksaan(9, ..., diagnosis "XYZ")` dengan sesi Dokter | `DataKlinisBoundary` menolak dengan `IllegalArgumentException`. Transaksi dibatalkan, sehingga tidak ada baris `pemeriksaan` maupun `audit` baru | Pengecualian dilempar. Jumlah baris `pemeriksaan` kunjungan 9 tetap 0 dan tidak ada audit baru | Pass |
| INT-TC19 | IK-10 | Normal: penguncian rekam medis tanpa resep | Siapkan jadwal kontrol pasien kunjungan 9 bertanggal hari ini beserta entri daftar pantau "kunjungan ulang"-nya, simpan draf berdiagnosis I10, lalu panggil `kunciRekamMedis(9)` | Dalam satu transaksi: pemeriksaan terkunci, status kunjungan "selesai", bundel FHIR "tertunda" berisi resource `Condition` berkode I10, jadwal kontrol "selesai", entri pantau "selesai ditindaklanjuti", dan audit "Pemeriksaan kunjungan 9 dikunci" | Keenam perubahan terbaca dari tabel `pemeriksaan`, `kunjungan`, `bundel_fhir` (`tertunda\|I10`), `jadwal_kontrol`, `daftar_pantau`, dan `audit` | Pass |
| INT-TC20 | IK-10 | Gagal: kunjungan sudah ditutup saat dokter mengunci | Simpan draf berdiagnosis, ubah status kunjungan 9 menjadi "selesai", lalu panggil `kunciRekamMedis(9)` | `IllegalStateException` dari `KunjunganEntity.perbaruiStatus()`. Seluruh transaksi di-*rollback*: pemeriksaan tetap belum terkunci, tidak ada bundel, dan tidak ada audit penguncian | Pengecualian dilempar. Pemeriksaan tidak terkunci, `bundel_fhir` kunjungan 9 kosong, dan jumlah audit tidak bertambah | Pass |
| INT-TC21 | IK-11 | Normal: resep dikirim ke apotek | `tambahItem(3, 30, "5 mg", "1 x 1 pagi")` dan `tambahItem(1, 10, "500 mg", "3 x 1 bila demam")`, lalu `kirimKeApotek(9, items)`. Setelah itu `ApotekManager.muatAntreanResep()` | Resep "menunggu" untuk kunjungan 9, dua baris `item_resep` dengan jumlah, dosis, dan aturan pakai yang sama, entri terakhir antrean apotek adalah resep ini, dan resep terbaca oleh `ApotekManager` | Resep `menunggu\|9`, item `1:10:500 mg:3 x 1 bila demam,3:30:5 mg:1 x 1 pagi`, entri antrean terakhir sama dengan ID resep, dan resep ada pada antrean `ApotekManager` | Pass |
| INT-TC22 | IK-11 | Gagal: dua dokter mengirim resep bersamaan untuk kunjungan yang sama | Dua *thread* memanggil `kirimKeApotek(9, Parasetamol x10)` pada saat yang sama | Penguncian baris antrean apotek (`FOR UPDATE`, Q-C58-09) membuat kedua transaksi bergantian. Satu berhasil, satu ditolak dengan pesan "Kunjungan ini masih memiliki resep yang menunggu di apotek...". Hanya satu resep dan satu entri antrean tersimpan | Satu berhasil dan satu ditolak dengan pesan tersebut. Satu baris `resep` dan satu entri antrean baru | Pass |
| INT-TC23 | IK-12 | *[Diisi Malik]* | | | | |
| INT-TC24 | IK-12 | *[Diisi Malik]* | | | | |
| INT-TC25 | IK-13 | *[Diisi Malik]* | | | | |
| INT-TC26 | IK-13 | *[Diisi Malik]* | | | | |
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

*[Diisi Axeleon: UC01-TC01 sampai UC01-TC04 untuk skenario normal dan alternatif 1 sampai 3.]*

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

*[Diisi Humam: UC04-TC01 sampai UC04-TC04 untuk skenario normal dan alternatif 1 sampai 3.]*

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

*[Diisi Malik: UC07-TC01 sampai UC07-TC03 untuk skenario normal dan alternatif 1 sampai 2.]*

### 4.4.8 Use Case Mengelola Persediaan Obat

**ID Use Case:** UC08  
**Nama Use Case:** Mengelola Persediaan Obat

*[Diisi Malik: UC08-TC01 sampai UC08-TC03 untuk skenario normal dan alternatif 1 sampai 2.]*

### 4.4.9 Use Case Mengelola Daftar Pantau Pasien

**ID Use Case:** UC09  
**Nama Use Case:** Mengelola Daftar Pantau Pasien

*[Diisi Humam: UC09-TC01 sampai UC09-TC03 untuk skenario normal dan alternatif 1 sampai 2.]*

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

*[Diisi Axeleon: UC12-TC01 sampai UC12-TC05 untuk skenario normal dan alternatif 1 sampai 4.]*

<br>

## 4.5 Pengujian Kebutuhan Non-Fungsional (KNF)

Setiap KNF pada Tabel 3.2 SKPL diuji secara terukur berdasarkan parameternya. Setiap pengujian memuat target terukur, satu metode beserta alat dan jumlah pengulangannya, serta hasil aktual berupa angka atau data. KNF yang belum dipenuhi atau tidak dapat diukur penuh tetap dicantumkan beserta alasan dan rencana perbaikannya.

Tabel 4.5. Hasil Pengujian KNF

| ID Pengujian | ID KNF | Parameter | Target Terukur | Metode dan Alat | Hasil Aktual | Kesimpulan |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| KNF01-TC01 | KNF01 | Response time | *[Diisi Steven]* | | | |
| KNF02-TC01 | KNF02 | Reliability | *[Diisi Humam]* | | | |
| KNF03-TC01 | KNF03 | Reliability | *[Diisi Malik]* | | | |
| KNF04-TC01 | KNF04 | Security | *[Diisi Axeleon]* | | | |
| KNF05-TC01 | KNF05 | Security | *[Diisi Axeleon]* | | | |
| KNF06-TC01 | KNF06 | Reliability | *[Diisi Axeleon]* | | | |
| KNF07-TC01 | KNF07 | Portability | *[Diisi Fachry]* | | | |
| KNF08-TC01 | KNF08 | Ergonomy | *[Diisi Humam]* | | | |
| KNF09-TC01 | KNF09 | Availability | *[Diisi Fachry]* | | | |
| KNF10-TC01 | KNF10 | Portability | *[Diisi Steven]* | | | |
| KNF11-TC01 | KNF11 | Reliability | *[Diisi Steven]* | | | |
| KNF12-TC01 | KNF12 | Ergonomy | *[Diisi Humam]* | | | |
| KNF13-TC01 | KNF13 | Reliability | *[Diisi Malik]* | | | |

<br>

## 4.6 Tangkapan Layar dan Bukti Hasil Pengujian

Bukti pengujian dilampirkan sesuai ID Pengujian pada Subbab 4.2 sampai 4.5. Keluaran *test runner* memakai format pohon dengan ID *test case* pada setiap baris: `[OK]` berarti lulus dan `[XX]` gagal. Untuk temuan yang sudah diperbaiki, bukti `v9.0` dan bukti uji ulang dilampirkan berdampingan. Laporan *coverage* diambil dari halaman kelas pada laporan JaCoCo (`src/target/site/jacoco/`). Kolom "Lines" dan "Missed" pada laporan itu menjadi dasar persentase baris pada Subbab 4.2. Tangkapan layar aplikasi diambil dari jendela SEHATI ukuran 1280 × 800 piksel.

Tabel 4.6. Bukti Hasil Pengujian

| ID Pengujian | Bukti Hasil Pengujian |
| :--- | :--- |
| C22-UT01 – C22-UT37 | Keluaran `mvn test -Dtest=PemeriksaanManagerTest` setelah perbaikan: 37 test `[OK]`<br><img src="./assets/pemeriksaan/c22-unit.png" width="100%"> |
| C22-UT37 (`v9.0`, Fail) | Test yang sama pada kode `v9.0`: C22-UT37 `[XX]`, `expected: <7> but was: <8>`<br><img src="./assets/pemeriksaan/c22-ut37-fail-v9.png" width="100%"> |
| C22 (coverage) | Total 20 dari 125 baris dan 4 dari 46 cabang terlewat<br><img src="./assets/pemeriksaan/c22-coverage.png" width="100%"> |
| C23-UT01 – C23-UT24 | Keluaran `mvn test -Dtest=ResepManagerTest`: 24 test `[OK]`<br><img src="./assets/pemeriksaan/c23-unit.png" width="100%"> |
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
| *[ID pengujian milik Axeleon]* | *[Diisi Axeleon: keluaran test runner per kelas, coverage kelas Mendalam, integrasi, use case, dan KNF]* |
| *[ID pengujian milik Steven]* | *[Diisi Steven: keluaran test runner per kelas, coverage kelas Mendalam, integrasi, use case, dan KNF]* |
| *[ID pengujian milik Humam]* | *[Diisi Humam: keluaran test runner per kelas, coverage kelas Mendalam, integrasi, use case, dan KNF]* |
| *[ID pengujian milik Malik]* | *[Diisi Malik: keluaran test runner per kelas, coverage kelas Mendalam, integrasi, use case, dan KNF]* |
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
| KF04 | - | UC04 | C04 - AntreanSkriningForm<br>C20 - SkriningManager<br>C37 - AntreanEntity<br>C58 - DataKunjunganBoundary | `presentation/AntreanSkriningForm.java`<br>`business/control/SkriningManager.java`<br>`business/entity/AntreanEntity.java` | *[UC04-TC.., diisi Humam]* | *[C20-UT.., C37-UT.., INT-TC11; diisi Humam]* |
| KF05 | - | UC04 | C05 - SkriningForm<br>C20 - SkriningManager<br>C38 - SkriningEntity<br>C39 - TandaVitalEntity<br>C59 - DataKlinisBoundary | `presentation/SkriningForm.java`<br>`business/control/SkriningManager.java`<br>`dataaccess/DataKlinisBoundary.java` | *[UC04-TC.., diisi Humam]* | *[C20-UT.., C59-UT01 s.d. C59-UT20, INT-TC11, INT-TC12; diisi Humam]* |
| KF06 | KNF02 | UC04 | C05 - SkriningForm<br>C20 - SkriningManager<br>C39 - TandaVitalEntity | `presentation/SkriningForm.java`<br>`business/control/SkriningManager.java` | *[UC04-TC.., diisi Humam]* | *[C20-UT.., KNF02-TC01; diisi Humam]* |
| KF07 | KNF08 | UC04 | C05 - SkriningForm<br>C21 - RisikoManager<br>C40 - AmbangRisikoEntity<br>C41 - PenandaRisikoEntity<br>C51 - DaftarPantauEntity<br>C60 - DataPantauBoundary | `presentation/SkriningForm.java`<br>`business/control/RisikoManager.java`<br>`dataaccess/DataPantauBoundary.java` | *[UC04-TC.., diisi Humam]* | *[C21-UT.., C40-UT.., C41-UT.., INT-TC13, INT-TC14, KNF08-TC01; diisi Humam]* |
| KF08 | - | UC05 | C06 - RekamMedisForm<br>C22 - PemeriksaanManager<br>C39 - TandaVitalEntity<br>C59 - DataKlinisBoundary | `presentation/RekamMedisForm.java`<br>`business/control/PemeriksaanManager.java` | UC05-TC02, UC05-TC07 | C22-UT04 s.d. C22-UT06 |
| KF09 | - | UC05 | C06 - RekamMedisForm<br>C22 - PemeriksaanManager<br>C34 - RekamMedisEntity<br>C57 - DataPasienBoundary<br>C59 - DataKlinisBoundary | `presentation/RekamMedisForm.java`<br>`business/control/PemeriksaanManager.java`<br>`dataaccess/DataPasienBoundary.java` | UC05-TC01 (Fail pada `v9.0`, Pass setelah perbaikan) | C22-UT01 s.d. C22-UT03, C22-UT35, C22-UT36, INT-TC17 |
| KF10 | KNF05 | UC05 | C07 - PemeriksaanForm<br>C22 - PemeriksaanManager<br>C42 - PemeriksaanEntity<br>C43 - DiagnosisEntity<br>C44 - KodeICD10Entity<br>C59 - DataKlinisBoundary | `presentation/PemeriksaanForm.java`<br>`business/control/PemeriksaanManager.java`<br>`business/entity/KodeICD10Entity.java`<br>`dataaccess/DataKlinisBoundary.java` | UC05-TC03, UC05-TC04, UC05-TC05 | C22-UT07 s.d. C22-UT15, C22-UT20 s.d. C22-UT27, C22-UT31 s.d. C22-UT34, C42-UT01 s.d. C42-UT03, C44-UT01 s.d. C44-UT05, C59-UT21 s.d. C59-UT27, INT-TC17 s.d. INT-TC20 |
| KF11 | - | UC06 | C08 - ResepForm<br>C23 - ResepManager<br>C46 - ResepEntity<br>C47 - ItemResepEntity<br>C48 - ObatEntity<br>C61 - DataFarmasiBoundary | `presentation/ResepForm.java`<br>`business/control/ResepManager.java`<br>`business/entity/ResepEntity.java`<br>`dataaccess/DataFarmasiBoundary.java` | UC06-TC01, UC06-TC02 | C23-UT01 s.d. C23-UT24, C46-UT01 s.d. C46-UT04, C47-UT01 s.d. C47-UT03, INT-TC21, INT-TC22 |
| KF12 | - | UC05<br>UC09 | C07 - PemeriksaanForm<br>C22 - PemeriksaanManager<br>C45 - JadwalKontrolEntity<br>C21 - RisikoManager<br>C26 - PantauManager<br>C51 - DaftarPantauEntity<br>C59 - DataKlinisBoundary<br>C60 - DataPantauBoundary | `presentation/PemeriksaanForm.java`<br>`business/control/PemeriksaanManager.java`<br>`business/control/RisikoManager.java`<br>`dataaccess/DataKlinisBoundary.java` | UC05-TC03, UC05-TC08 (Fail pada `v9.0`, Pass setelah perbaikan); *[UC09-TC.., diisi Humam]* | C22-UT16 s.d. C22-UT19, C22-UT23, C22-UT28 s.d. C22-UT30, C22-UT37 (Fail pada `v9.0`, Pass setelah perbaikan), C45-UT01 s.d. C45-UT03, C59-UT28 s.d. C59-UT32, INT-TC19; *[C21-UT.., C26-UT.., INT-TC15, INT-TC16; diisi Humam]* |
| KF13 | - | UC07 | C09 - AntreanResepForm<br>C10 - PenyerahanObatForm<br>C24 - ApotekManager<br>C46 - ResepEntity<br>C61 - DataFarmasiBoundary | `presentation/AntreanResepForm.java`<br>`presentation/PenyerahanObatForm.java`<br>`business/control/ApotekManager.java` | *[UC07-TC.., diisi Malik]* | C46-UT01 s.d. C46-UT04; *[C24-UT.., INT-TC23, INT-TC24; diisi Malik]* |
| KF14 | KNF03<br>KNF07 | UC07 | C10 - PenyerahanObatForm<br>C24 - ApotekManager<br>C48 - ObatEntity<br>C49 - BetsObatEntity<br>C54 - BundelFHIREntity<br>C61 - DataFarmasiBoundary<br>C63 - DataSinkronisasiBoundary | `business/control/ApotekManager.java`<br>`business/control/PenyusunBundelFHIR.java`<br>`dataaccess/DataFarmasiBoundary.java` | *[UC07-TC.., diisi Malik]* | C22-UT20, INT-TC19 (bundel kunjungan tanpa resep); *[C24-UT.., C48-UT.., C49-UT.., INT-TC23, INT-TC24, KNF03-TC01, KNF07-TC01; diisi Malik dan Fachry]* |
| KF15 | - | UC12 | C16 - KonfigurasiForm<br>C29 - KonfigurasiManager<br>C30 - PenggunaEntity<br>C36 - PoliEntity<br>C40 - AmbangRisikoEntity<br>C48 - ObatEntity<br>C56 - DataAkunBoundary | `presentation/KonfigurasiForm.java`<br>`business/control/KonfigurasiManager.java`<br>`dataaccess/DataAkunBoundary.java` | *[UC12-TC.., diisi Axeleon]* | *[C29-UT.., C30-UT.., INT-TC03, INT-TC04; diisi Axeleon]* |
| KF16 | KNF04<br>KNF05 | UC01 | C01 - LoginForm<br>C17 - AutentikasiManager<br>C30 - PenggunaEntity<br>C31 - SesiEntity<br>C32 - AuditEntity<br>C56 - DataAkunBoundary | `presentation/LoginForm.java`<br>`presentation/Navigasi.java`<br>`business/control/AutentikasiManager.java`<br>`common/SesiAktif.java` | *[UC01-TC.., diisi Axeleon]* | C22-UT15, C22-UT27, C23-UT21 (pembatasan peran pada operasi tulis); *[C17-UT.., C30-UT.., C31-UT.., INT-TC01, INT-TC02, KNF04-TC01, KNF05-TC01; diisi Axeleon]* |
| KF17 | - | UC08 | C11 - PersediaanForm<br>C25 - PersediaanManager<br>C48 - ObatEntity<br>C49 - BetsObatEntity<br>C50 - PenerimaanObatEntity<br>C61 - DataFarmasiBoundary | `presentation/PersediaanForm.java`<br>`business/control/PersediaanManager.java`<br>`dataaccess/DataFarmasiBoundary.java` | *[UC08-TC.., diisi Malik]* | *[C25-UT.., C48-UT.., C49-UT.., C50-UT.., INT-TC25, INT-TC26; diisi Malik]* |
| KF18 | - | UC09 | C12 - DaftarPantauForm<br>C26 - PantauManager<br>C21 - RisikoManager<br>C51 - DaftarPantauEntity<br>C52 - TindakLanjutEntity<br>C60 - DataPantauBoundary | `presentation/DaftarPantauForm.java`<br>`business/control/PantauManager.java`<br>`dataaccess/DataPantauBoundary.java` | *[UC09-TC.., diisi Humam]* | *[C26-UT.., C51-UT.., INT-TC15, INT-TC16; diisi Humam]* |
| KF19 | - | UC10 | C13 - LaporanForm<br>C27 - LaporanManager<br>C53 - LaporanEntity<br>C62 - DataLaporanBoundary | `presentation/LaporanForm.java`<br>`business/control/LaporanManager.java`<br>`dataaccess/DataLaporanBoundary.java` | *[UC10-TC.., diisi Fachry]* | *[C27-UT.., C53-UT.., INT-TC27, INT-TC28; diisi Fachry]* |
| KF20 | KNF07 | UC11 | C14 - SinkronisasiForm<br>C15 - SATUSEHATGateway<br>C28 - SinkronisasiManager<br>C54 - BundelFHIREntity<br>C63 - DataSinkronisasiBoundary | `presentation/SinkronisasiForm.java`<br>`business/control/SinkronisasiManager.java`<br>`dataaccess/SATUSEHATGateway.java`<br>`dataaccess/DataSinkronisasiBoundary.java` | *[UC11-TC.., diisi Fachry]* | *[C15-UT.., C28-UT.., C54-UT.., C63-UT.., INT-TC29 s.d. INT-TC32, KNF07-TC01; diisi Fachry]* |
| KF21 | KNF06 | UC12 | C16 - KonfigurasiForm<br>C29 - KonfigurasiManager<br>C55 - PencadanganEntity<br>C64 - DataPencadanganBoundary | `presentation/KonfigurasiForm.java`<br>`business/control/KonfigurasiManager.java`<br>`dataaccess/DataPencadanganBoundary.java` | *[UC12-TC.., diisi Axeleon]* | *[C29-UT.., C64-UT.., INT-TC05, INT-TC06, KNF06-TC01; diisi Axeleon]* |
| KF22 | KNF09<br>KNF10<br>KNF13 | UC05<br>UC11 | C14 - SinkronisasiForm<br>C15 - SATUSEHATGateway<br>C28 - SinkronisasiManager | `presentation/BilahAplikasi.java`<br>`presentation/Navigasi.java`<br>`business/control/SinkronisasiManager.java`<br>`dataaccess/SATUSEHATGateway.java` | UC05-TC06; *[UC11-TC.., diisi Fachry]* | *[C15-UT.., C28-UT.., KNF09-TC01, KNF10-TC01, KNF13-TC01; diisi Fachry, Steven, dan Malik]* |
| KF23 | KNF11 | UC03<br>UC07 | C19 - KunjunganManager<br>C24 - ApotekManager<br>C36 - PoliEntity<br>C58 - DataKunjunganBoundary<br>C61 - DataFarmasiBoundary | `business/control/KunjunganManager.java`<br>`business/control/ApotekManager.java`<br>`dataaccess/DataKunjunganBoundary.java`<br>`dataaccess/DataFarmasiBoundary.java` | *[UC03-TC.., UC07-TC.., diisi Steven dan Malik]* | INT-TC22 (penguncian antrean apotek); *[INT-TC33, INT-TC34, KNF11-TC01; diisi Steven]* |
| KF24 | KNF12 | UC04<br>UC05 | C05 - SkriningForm<br>C07 - PemeriksaanForm | `presentation/SkriningForm.java`<br>`presentation/PemeriksaanForm.java`<br>`presentation/Navigasi.java`<br>`presentation/TabelPintasan.java` | UC05-TC03 (Ctrl+S); *[UC04-TC.., diisi Humam]* | *[KNF12-TC01; diisi Humam]* |

Pada `v9.0`, dua KF belum terpenuhi karena memiliki *test case* `Fail`:

- **KF09** (UC05-TC01): hasil skrining hari itu belum tampil pada ringkasan rekam medis I-06.
- **KF12** (UC05-TC08, C22-UT37): jadwal kontrol pada tanggal yang sama dapat dibuat dua kali setelah jadwal pertama masuk daftar pantau.

Keduanya sudah diperbaiki (Tabel 3.5) dan diuji ulang dengan hasil `Pass`. Bagian KF12 milik UC09 masih menunggu hasil pengujian Humam. KF lain menunggu hasil pengujian setiap modul.

Tabel 5.2. Matriks Kerunutan KNF terhadap Pengujian

| ID KNF | Parameter | KF/UC Terkait | Test Case |
| :--- | :--- | :--- | :--- |
| KNF01 | Response time | KF01 / UC02 | KNF01-TC01 |
| KNF02 | Reliability | KF06 / UC04 | KNF02-TC01 |
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
