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
| 13525110 | Fachry Azriel Fajdwani |

---

### Daftar Isi
* [Milestone 1](#milestone-1)
* [Milestone 2](#milestone-2)
* [Milestone 3](#milestone-3)
* [Milestone 4](#milestone-4)

---

### Log Penggunaan AI per Milestone

Silakan catat penggunaan AI yang berdampak signifikan pada pengerjaan tugas (misal: *generate* fungsi algoritma yang kompleks, *generate* draf dokumen SKPL/DPPL, atau *debugging* error utama). 
*Penggunaan sepele seperti memperbaiki *typo* atau auto-complete satu baris kode tidak perlu dicatat.*

### Milestone 1
| Tool AI | Tujuan Penggunaan | Contoh Prompt Utama | Modifikasi & Validasi Manusia |
| :--- | :--- | :--- | :--- |
| *Claude (Claude Code)* | *Meminta ide outline dan struktur poin-poin utama Bab 1 sampai Bab 3 berdasarkan topik SDG 3* | *"Berikan usulan struktur dokumen dan poin-poin utama yang perlu dibahas untuk Bab 1 sampai Bab 3. Kelompok kami memilih SDG 3 (Good Health and Well-being) dengan konsep sistem manajemen puskesmas/klinik."* | *Outline yang diberikan AI dijadikan panduan kasar. Seluruh isi paragraf draf diketik manual oleh anggota kelompok disesuaikan dengan konteks puskesmas dan penomoran kesenjangan (G-01 s.d. G-07).* |
| *Claude (Claude Code)* | *Menelusuri data statistik pendukung latar belakang masalah* | *"Carikan data Riskesdas 2018 mengenai prevalensi hipertensi dan proporsi yang terdiagnosis, jumlah puskesmas menurut Profil Kesehatan Indonesia terbaru, serta AKI hasil Long Form SP2020."* | *Seluruh angka yang dikutip ditelusuri kembali ke sumber primer (Laporan Nasional Riskesdas 2018, Profil Kesehatan Indonesia 2024, dan tabel BPS Long Form SP2020), lalu dicantumkan pada bagian Referensi. Angka yang tidak dapat diverifikasi ke sumber primer tidak dimasukkan ke dokumen.* |
| *Claude (Claude Code)* | *Meminta referensi tahapan proses pelayanan rawat jalan sebagai acuan pembuatan activity diagram* | *"Berikan contoh tahapan proses pelayanan rawat jalan yang umum di puskesmas mulai dari pendaftaran hingga penyerahan obat."* | *Urutan aktivitas yang diberikan AI didiskusikan ulang dan dirombak. Kelompok menggambar sendiri activity diagram swimlane berdasarkan kesepakatan final, memperhatikan notasi UML yang tepat.* |
| *Claude (Claude Code)* | *Meminta contoh dasar user story sebagai referensi brainstorming* | *"Berikan beberapa contoh user story untuk sistem klinik dengan format 'Sebagai [Aktor], saya ingin [Aktivitas], sehingga [Tujuan]' untuk aktor Perawat dan Dokter."* | *Contoh AI hanya dijadikan referensi format. Kelompok merumuskan seluruh user story secara manual agar benar-benar terhubung dengan kesenjangan pada Subbab 1.2 dan sesuai batasan ruang lingkup (BL-01 s.d. BL-07).* |
| *Gemini* | *Meminta tinjauan (review) tata bahasa dan saran parafrase pada beberapa paragraf agar lebih lugas* | *"Tolong tinjau paragraf berikut, apakah ada kalimat yang terlalu bertele-tele? Berikan saran perbaikannya agar lebih lugas dan teknis untuk laporan akademik."* | *Kelompok meninjau saran dari AI dan hanya menggunakan perbaikan untuk menghilangkan metafora berlebihan serta memperbaiki tanda baca. Substansi kalimat tetap sepenuhnya merupakan hasil pemikiran kelompok.* |

> **Catatan pengisian.** Kolom *Modifikasi & Validasi Manusia* di atas harus dibaca ulang dan disesuaikan oleh masing-masing anggota agar benar-benar mencerminkan validasi yang dilakukan, karena tabel ini merupakan bagian dari pernyataan integritas yang ditandatangani.

### Milestone 2
| Tool AI | Tujuan Penggunaan | Contoh Prompt Utama | Modifikasi & Validasi Manusia |
| :--- | :--- | :--- | :--- |
| *Claude (Claude Code)* | *Meminta referensi struktur pengkategorian kebutuhan sistem (User, Business, System Requirement) berdasarkan aktivitas operasional* | *"Berikan contoh pemetaan kebutuhan sistem dari aktivitas operasional klinik ke dalam kategori User Requirement, Business Requirement, dan System Requirement."* | *Contoh dari AI hanya dijadikan acuan pemahaman taksonomi kebutuhan. Seluruh rincian kebutuhan spesifik SEHATI dirumuskan secara mandiri berdasarkan 27 aktivitas dan alur pelayanan puskesmas yang telah disepakati pada Milestone 1.* |
| *Claude (Claude Code)* | *Brainstorming aspek kebutuhan non-fungsional (KNF) yang relevan untuk aplikasi kesehatan fasilitas primer* | *"Apa saja parameter kebutuhan non-fungsional yang krusial untuk sistem rekam medis puskesmas dengan arsitektur desktop dan sinkronisasi berkala?"* | *Saran aspek non-fungsional dari AI disaring dan disesuaikan dengan batasan teknis puskesmas. Nilai batas/metrik kuantitatif (kecepatan respons < 2 detik, enkripsi AES-256 untuk basis data lokal, mekanisme rollback transaksi) ditetapkan sendiri oleh kelompok.* |
| *Gemini* | *Memeriksa konsistensi matriks keterlacakan (traceability) antara Kebutuhan Fungsional dengan User Story dan Aktivitas* | *"Tolong periksa apakah ada ID aktivitas atau user story dari daftar berikut yang belum tercakup atau terlewat pada pemetaan kebutuhan fungsional."* | *Hasil pengecekan AI ditinjau ulang secara manual baris demi baris pada draf dokumen. Penyesuaian pemetaan akhir dan penggabungan kebutuhan fungsional tetap diputuskan sendiri oleh anggota kelompok.* |
| *Gemini* | *Review tata bahasa dan saran perbaikan formulasi kalimat kebutuhan fungsional agar tidak ambigu* | *"Tinjau formulasi kalimat kebutuhan fungsional berikut, berikan saran agar menggunakan pola pernyataan yang baku ('Sistem harus...') dan tidak bermakna ganda."* | *Saran perbaikan redaksional dari AI hanya diadopsi pada pemilihan kata kerja operasional yang lebih lugas. Makna klinis, batasan fitur, dan alur kerja puskesmas tetap sepenuhnya hasil rumusan kelompok.* |

### Milestone 3
| Tool AI | Tujuan Penggunaan | Contoh Prompt Utama | Modifikasi & Validasi Manusia |
| :--- | :--- | :--- | :--- |
| *Claude (Claude Code)* | *Memeriksa kelengkapan penelusuran antara Bab 2, tabel use case, skenario, dan diagram* | *"Periksa apakah ada ID KF yang belum tercakup use case mana pun, dan apakah aktor pada tabel use case sama dengan asosiasi pada diagram."* | *Hasil pemeriksaan dicocokkan ulang secara manual pada dokumen sebelum dikumpulkan, khususnya kesesuaian ID KF pada tabel Subbab 3.2 dengan rumusan KF pada Bab 2.* |

### Milestone 4
| Tool AI | Tujuan Penggunaan | Contoh Prompt Utama | Modifikasi & Validasi Manusia |
| :--- | :--- | :--- | :--- |
| *Claude (Claude Code)* | *Menyusun draf identifikasi kelas beserta atribut, metode, dan relasinya dari skenario use case Milestone 3* | *"Turunkan kandidat kelas dari skenario use case pada dokumen UC, lengkap dengan atribut, metode, dan jenis relasi antarkelasnya."* | *Draf AI dipakai sebagai titik awal. Kelompok memeriksa tiap kelas terhadap skenario Subbab 3.4, membuang kelas yang tidak berpangkal pada KF mana pun, dan menetapkan sendiri jenis relasi yang dipakai.* |
| *Claude (Claude Code)* | *Menggambar ulang 13 diagram kelas setelah model disusun ulang mengikuti kerangka ECB hasil asistensi* | *"Gambar ulang diagram kelas tiap use case memakai stereotip boundary, control, dan entity, dengan boundary hanya berhubungan lewat control."* | *Pembagian kelas ke dalam stereotip ECB dan pola penamaannya ditetapkan kelompok berdasarkan catatan asistensi. Tata letak dan hasil gambar diperiksa satu per satu, dan diagram yang garisnya menembus kotak kelas digambar ulang.* |
| *Claude (Claude Code)* | *Memeriksa kelengkapan penelusuran KF, aturan ECB, dan konsistensi antara diagram dengan tabel* | *"Periksa apakah ada KF yang belum tercakup kelas mana pun, apakah ada garis dari boundary langsung ke entity, dan apakah nomor gambar berurutan."* | *Hasil pemeriksaan dicocokkan ulang pada dokumen. Satu temuan ditindaklanjuti, yaitu KodeICD10Entity yang belum tersambung ke control; relasinya ke PemeriksaanManager ditambahkan setelah kelompok memastikan pencarian kode ICD-10 memang dijalankan manager tersebut.* |

---
### Pernyataan Integritas dan Persetujuan

Kami yang bertanda tangan di bawah ini menyatakan bahwa seluruh log penggunaan AI di atas adalah benar. Kami telah memvalidasi seluruh hasil AI dan bertanggung jawab penuh atas orisinalitas, keamanan, dan kebenaran hasil akhir dari tugas ini.

| Tanda Tangan | Nama Anggota |
| :---: | :--- |
| <img src="./assets/ttd-anggota1.png" width="100"> | **13525008 - Malik Arsyafiandra Madani** |
| <img src="./assets/ttd-anggota2.png" width="100"> | **13525044 - Steven Vanako** |
| <img src="./assets/ttd-anggota3.png" width="100"> | **13525071 - Muhammad Adnan Kurniawan** |
| <img src="./assets/ttd-anggota4.png" width="100"> | **13525074 - Axeleon Justin Algianto** |
| <img src="./assets/ttd-anggota5.png" width="100"> | **13525110 - Fachry Azriel Fajdwani** |
