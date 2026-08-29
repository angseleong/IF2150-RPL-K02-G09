# Deklarasi Penggunaan AI

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
| *[NIM 5]* | *[Nama Anggota 5]* |

---

### Daftar Isi
* [Milestone 1](#milestone-1)
* Notes: Copy bagian Daftar Isi seperti Milestone 1 untuk Milestone berikutnya, contoh ``* [Milestone 2](#milestone-2)``. Ketika Daftar isi diklik maka akan langsung diarahkan ke bagian bawah sesuai dengan Milestone tujuan.

---

### Log Penggunaan AI per Milestone

Silakan catat penggunaan AI yang berdampak signifikan pada pengerjaan tugas (misal: *generate* fungsi algoritma yang kompleks, *generate* draf dokumen SKPL/DPPL, atau *debugging* error utama). 
*Penggunaan sepele seperti memperbaiki *typo* atau auto-complete satu baris kode tidak perlu dicatat.*

### Milestone 1
| Tool AI | Tujuan Penggunaan | Contoh Prompt Utama | Modifikasi & Validasi Manusia |
| :--- | :--- | :--- | :--- |
| *Claude (Claude Code)* | *Menyusun draf awal Bab 1 sampai Bab 3 dokumen Tugas 1 berdasarkan ide dan SDG yang sudah ditetapkan kelompok* | *"Kerjakan draf Tugas 1 sesuai template di folder RPL. Kelompok kami memilih SDG 3 (Good Health and Well-being) dengan konsep sistem manajemen puskesmas/klinik berbasis desktop."* | *Ide, pemilihan SDG, konsep perangkat lunak, dan platform ditetapkan oleh kelompok sebelum AI digunakan. Draf yang dihasilkan dibaca ulang per subbab, disesuaikan dengan konteks puskesmas yang kelompok pahami, dan diselaraskan penomoran kesenjangan (G-01 s.d. G-07) dengan user story terkait.* |
| *Claude (Claude Code)* | *Menelusuri data statistik pendukung latar belakang masalah* | *"Carikan data Riskesdas 2018 mengenai prevalensi hipertensi dan proporsi yang terdiagnosis, jumlah puskesmas menurut Profil Kesehatan Indonesia terbaru, serta AKI hasil Long Form SP2020."* | *Seluruh angka yang dikutip ditelusuri kembali ke sumber primer (Laporan Nasional Riskesdas 2018, Profil Kesehatan Indonesia 2024, dan tabel BPS Long Form SP2020), lalu dicantumkan pada bagian Referensi. Angka yang tidak dapat diverifikasi ke sumber primer tidak dimasukkan ke dokumen.* |
| *Claude (Claude Code)* | *Membuat rancangan awal swimlane activity diagram proses pelayanan rawat jalan* | *"Buatkan activity diagram swimlane dengan lajur Petugas Pendaftaran, Perawat, Dokter, Apoteker, dan Sistem untuk alur pendaftaran sampai penyerahan obat."* | *Urutan aktivitas dan titik percabangan ditentukan kelompok berdasarkan alur nyata di puskesmas. Hasil AI diperiksa terhadap notasi UML activity diagram, dan penempatan lajur Sistem dipindahkan ke tengah agar keterbacaan alur lebih baik. Berkas sumber diagram disimpan pada `docs/M1/assets/diagram/` agar dapat disunting ulang oleh kelompok.* |
| *Claude (Claude Code)* | *Menyusun daftar user story untuk lima aktor* | *"Susun user story dalam format Sebagai [Aktor], saya ingin [Aktivitas], sehingga [Tujuan] untuk setiap aktor yang sudah diidentifikasi."* | *Kelompok memangkas dan menyesuaikan usulan AI agar setiap user story benar-benar terhubung dengan kesenjangan yang diidentifikasi pada Subbab 1.2, serta menghapus usulan yang berada di luar batasan ruang lingkup (BL-01 s.d. BL-07).* |
| | | | | |

> **Catatan pengisian.** Kolom *Modifikasi & Validasi Manusia* di atas harus dibaca ulang dan disesuaikan oleh masing-masing anggota agar benar-benar mencerminkan validasi yang dilakukan, karena tabel ini merupakan bagian dari pernyataan integritas yang ditandatangani.

### Milestone 2
| Tool AI | Tujuan Penggunaan | Contoh Prompt Utama | Modifikasi & Validasi Manusia |
| :--- | :--- | :--- | :--- |
| | | | | |
| | | | | |

---
### Pernyataan Integritas dan Persetujuan

Kami yang bertanda tangan di bawah ini menyatakan bahwa seluruh log penggunaan AI di atas adalah benar. Kami telah memvalidasi seluruh hasil AI dan bertanggung jawab penuh atas orisinalitas, keamanan, dan kebenaran hasil akhir dari tugas ini.

| Tanda Tangan | Nama Anggota |
| :---: | :--- |
| <img src="./assets/ttd-anggota1.png" width="100"> | **[NIM - Nama Anggota 1]** |
| <img src="./assets/ttd-anggota2.png" width="100"> | **[NIM - Nama Anggota 2]** |
| <img src="./assets/ttd-anggota3.png" width="100"> | **[NIM - Nama Anggota 3]** |
| <img src="./assets/ttd-anggota4.png" width="100"> | **[NIM - Nama Anggota 4]** |
| <img src="./assets/ttd-anggota5.png" width="100"> | **[NIM - Nama Anggota 5]** |
