# Logbook Pekerjaan 

## Tugas Besar IF2150 - Rekayasa Perangkat Lunak

| Informasi | Keterangan |
|---|---|
| Kelas | K02 |
| Nomor Kelompok | G09 |
| Nama Kelompok | Cumlaude |
| Nama Perangkat Lunak | SEHATI (Sistem Elektronik Pelayanan Kesehatan Terintegrasi) |

**Anggota Kelompok:**

| NIM | Nama |
|---|---|
| 13525008 | Malik Arsyafiandra Madani |
| 13525044 | Steven Vanako |
| 13525071 | Muhammad Adnan Kurniawan |
| 13525074 | Axeleon Justin Algianto |
| 13525110 | Fachry Azriel Fajdwani |

---

### Daftar Isi
* [Milestone 1](#milestone-1)


---

### Milestone 1
**Periode:** 25-08-2026 - 30-08-2026

| Tanggal | Nama Anggota | Deskripsi Pekerjaan | Durasi (Jam) | Status | Kendala / *Blocker* | 
| :--- | :--- | :--- | :--- | :--- | :--- | 
| *25-08-2026* | *[Seluruh Anggota]* | *Rapat perdana kelompok: menyepakati SDG 3 (Good Health and Well-being) sebagai landasan solusi dan menyaring empat kandidat ide perangkat lunak* | *2* | *Done* | *-* | 
| *26-08-2026* | *[Seluruh Anggota]* | *Memilih ide final berupa sistem manajemen pelayanan puskesmas/klinik (SEHATI) dan menetapkan platform desktop beserta alasannya* | *1,5* | *Done* | *-* | 
| *26-08-2026* | *Malik Arsyafiandra Madani* | *Mengumpulkan data pendukung latar belakang: Riskesdas 2018, Profil Kesehatan Indonesia 2024, AKI Long Form SP2020, dan PMK No. 24 Tahun 2022* | *3* | *Done* | *-* | 
| *27-08-2026* | *Steven Vanako* | *Menyusun Bab 1: latar belakang masalah, keterkaitan dengan target SDG 3.1/3.4/3.8, dan urgensi* | *3,5* | *Done* | *-* | 
| *27-08-2026* | *Muhammad Adnan Kurniawan* | *Menyusun Subbab 1.2: analisis alur proses manual saat ini, identifikasi kesenjangan G-01 s.d. G-07, dan perbandingan dengan solusi eksisting* | *3* | *Done* | *-* | 
| *28-08-2026* | *Axeleon Justin Algianto* | *Menyusun Bab 2: deskripsi perangkat lunak, justifikasi platform desktop, nilai unik, serta daftar asumsi dan batasan* | *3,5* | *Done* | *-* | 
| *28-08-2026* | *Fachry Azriel Fajdwani* | *Menyusun Subbab 3.1 dan 3.2: identifikasi lima aktor dan penulisan 25 user story* | *3* | *Done* | *-* | 
| *29-08-2026* | *Malik Arsyafiandra Madani* | *Membuat activity diagram swimlane proses pelayanan rawat jalan (Subbab 3.4) beserta penjelasan alurnya* | *4* | *Done* | *Notasi percabangan awal belum konsisten; diperbaiki setelah menyamakan acuan notasi UML activity diagram* | 
| *29-08-2026* | *[Seluruh Anggota]* | *Peninjauan silang seluruh dokumen, penyelarasan penomoran kesenjangan dengan user story, serta perapian tabel dan referensi* | *2* | *Done* | *-* | 
| *29-08-2026* | *Steven Vanako* | *Merapikan struktur repository, mengganti nama berkas KXX_GYY menjadi K02_G09, dan memperbarui dokumen logbook serta AI Usage* | *1* | *Done* | *-* | 
| *31-08-2026* | *Muhammad Adnan Kurniawan* | *Menyesuaikan dokumen dengan template revisi: menambahkan Subbab 3.3 Deskripsi Aktivitas berisi 26 aktivitas (A01 s.d. A26) beserta penelusurannya ke user story, serta menggeser Model Proses Bisnis menjadi Subbab 3.4* | *2,5* | *Done* | *Template diperbarui asisten setelah draf awal selesai, sehingga penomoran subbab dan rujukan silang harus disesuaikan ulang* | 
| *31-08-2026* | *Muhammad Adnan Kurniawan* | *Memutakhirkan data pendukung Bab 1: mengganti angka Riskesdas 2018 dengan Survei Kesehatan Indonesia (SKI) 2023, menambahkan data diabetes melitus, serta melengkapi urgensi regulasi dengan surat edaran Kemenkes 15 April 2025* | *2* | *Done* | *Riskesdas 2018 ternyata sudah digantikan SKI 2023; angka lama tetap dipertahankan sebagai pembanding tren* | 
| *31-08-2026* | *[Seluruh Anggota]* | *Peninjauan menyeluruh Tugas 1 dan perbaikan hasil temuan: menyelesaikan pertentangan BR-01 dengan BR-03 dan BL-02 melalui rancangan sinkronisasi tunda (store-and-forward) ke SATUSEHAT, menambahkan modul pemantauan kesehatan ibu agar target SDG 3.1 benar-benar didukung fitur, memasukkan ASRI ke tabel perbandingan solusi, menambahkan pencadangan otomatis, serta menyusun Gambar 2 untuk alur pelaporan dan sinkronisasi* | *5* | *Done* | *Lingkup bertambah menjadi 31 user story dan 34 aktivitas, sehingga ditetapkan pembagian tahap inti dan tahap lanjutan pada Subbab 2.2* | 
| *31-08-2026* | *[Seluruh Anggota]* | *Membingkai ulang Bab 1 setelah verifikasi data digitalisasi: mengakui bahwa mayoritas puskesmas sudah terdigitalisasi (34.463 fasyankes terintegrasi SATUSEHAT per 27 Oktober 2025), memindahkan inti argumen dari 'puskesmas masih kertas' menjadi 'digitalisasi belum menutup celah deteksi dini', mengoreksi tenggat RME yang diundur ke akhir 2025, serta mengganti ASRI dengan SIKDA Generik sebagai pembanding yang tepat untuk puskesmas. Bingkai baru tersebut kemudian dirambatkan ke seluruh dokumen: tabel kesenjangan diberi kolom keberlakuan untuk memisahkan G-03, G-04, dan G-07 yang bertahan meski fasilitas sudah digital dari G-01, G-02, G-05, dan G-06 yang khas puskesmas manual dan hibrida, serta menyelaraskan Bab 2, user story, dan Subbab 3.4* | *3* | *Done* | *Sumber awal yang ditemukan berasal dari situs vendor komersial tanpa data primer, sehingga diganti dengan publikasi Kemenkes dan jurnal implementasi* | 
| *31-08-2026* | *[Seluruh Anggota]* | *Menindaklanjuti hasil asistensi: memangkas lingkup agar tidak bercabang. Aktor disederhanakan dari lima menjadi empat dengan menggabungkan Petugas Pendaftaran dan Kepala Puskesmas menjadi Petugas Administrasi, user story digabung dari 31 menjadi 18, aktivitas dari 34 menjadi 24, serta modul pemantauan kesehatan ibu beserta target SDG 3.1 dikeluarkan dari lingkup. Kedua activity diagram digambar ulang mengikuti aktor yang baru* | *4* | *Done* | *Penomoran aktivitas bergeser sehingga rujukan pada penjelasan alur Subbab 3.4 harus disesuaikan seluruhnya* | 
| *02-09-2026* | *[Seluruh Anggota]* | *Menindaklanjuti revisi asistensi kedua: memetakan aktivitas dan simpul diagram menjadi satu lawan satu sehingga seluruh 27 aktivitas (A01 s.d. A27) muncul sebagai simpul berlabel ID pada Gambar 1 dan Gambar 2, menambahkan kolom Pelaku pada tabel Deskripsi Aktivitas, menghapus lajur Platform SATUSEHAT karena merupakan aktor eksternal, serta mendeskripsikan Sistem SEHATI pada daftar aktor sebagai lajur sistem internal* | *4* | *Done* | *Beberapa deskripsi aktivitas sebelumnya menggabungkan dua langkah sekaligus sehingga harus dipecah agar berpasangan tepat dengan satu simpul* | 

**Catatan/Evaluasi Milestone 1:**
* *Kelompok menyepakati SDG 3 (Good Health and Well-being) sebagai landasan solusi, dengan fokus pada target 3.4 (penyakit tidak menular), 3.8 (cakupan kesehatan semesta), dan 3.1 (angka kematian ibu).*
* *Nilai unik yang disepakati sebagai pembeda utama adalah modul deteksi dini PTM yang bersifat aktif, bukan sekadar pencatatan administratif seperti SIMPUS pada umumnya.*
* *Seluruh data statistik yang dikutip pada Bab 1 telah ditelusuri ke sumber primernya (Kemenkes dan BPS) dan dicantumkan pada bagian Referensi.*
* *Hal yang perlu ditanyakan saat asistensi: (a) apakah lingkup lima aktor sudah proporsional untuk satu semester, (b) apakah 25 user story perlu diprioritaskan menjadi subset MVP, dan (c) apakah notasi activity diagram yang digunakan sudah sesuai ketentuan.*

---
