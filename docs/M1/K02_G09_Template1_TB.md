<h1>
IF2150 REKAYASA PERANGKAT LUNAK
<br>
TUGAS 1
<br>
TOPIC BRAINSTORMING
</h1>
<br>

## SEHATI - Sistem Elektronik Pelayanan Kesehatan Terintegrasi

### Untuk: *[Nama Asisten]*

Dipersiapkan oleh:
| Informasi | Keterangan |
| --- | --- |
| Kelas | K02 |
| Kelompok | G09 |

| NIM | Nama |
|---|---|
| 13525008 | Malik Arsyafiandra Madani |
| 13525044 | Steven Vanako |
| 13525071 | Muhammad Adnan Kurniawan |
| 13525074 | Axeleon Justin Algianto |
| 13525110 | Fachry Azriel Fajdwani |
---

<br>
<br>

# BAB 1: Analisis Permasalahan

## 1.1 Latar Belakang Masalah

Pusat Kesehatan Masyarakat (Puskesmas) merupakan ujung tombak Fasilitas Kesehatan Tingkat Pertama (FKTP) di Indonesia. Berdasarkan Profil Kesehatan Indonesia 2024 yang diterbitkan Kementerian Kesehatan, terdapat 10.268 puskesmas yang tersebar di seluruh Indonesia, terdiri atas 6.016 puskesmas non rawat inap dan 4.252 puskesmas rawat inap. Fasilitas inilah yang menjadi titik kontak pertama masyarakat dengan layanan kesehatan, sekaligus tempat sebagian besar kegiatan promotif dan preventif dijalankan.

Digitalisasi puskesmas sudah berjalan jauh. PMK Nomor 24 Tahun 2022 mewajibkan seluruh fasilitas pelayanan kesehatan menyelenggarakan Rekam Medis Elektronik yang terhubung ke platform nasional SATUSEHAT. Per 27 Oktober 2025, Kementerian Kesehatan mencatat 34.463 fasilitas sudah terintegrasi, dan pada 1 September 2026 diresmikan SATUSEHAT RME yang menyatukan data klinis sekitar 280 juta penduduk. Mayoritas puskesmas hari ini tidak lagi mencatat secara manual. Kemajuan itu menyisakan dua persoalan yang menjadi titik berangkat perangkat lunak ini.

**Persoalan pertama, sebaran digitalisasi belum merata.** Kementerian Kesehatan mengakui hambatan yang tersisa berupa keterbatasan infrastruktur jaringan di daerah dan kesiapan sumber daya manusia. Puskesmas di wilayah terpencil masih mencatat di kertas atau menjalankan sistem hibrida, sebagian data di komputer dan sebagian di lembar kertas. Model hibrida menyulitkan penelusuran riwayat karena catatan seorang pasien terbelah di dua media.

**Persoalan kedua, dan ini yang lebih mendasar, digitalisasi belum menutup celah deteksi dini.** Sistem informasi puskesmas yang tersedia bersifat pasif: menyimpan apa yang diketik, menampilkannya kembali bila diminta, tanpa pernah membandingkan hasil pemeriksaan seorang pasien antar-waktu. Angka pemeriksaan sudah tersimpan elektronik, tetapi data untuk mengenali pasien berisiko tidak pernah terbentuk.

Dampak paling nyata dari terputusnya riwayat kesehatan pasien terlihat pada penanganan **Penyakit Tidak Menular (PTM)**. Survei Kesehatan Indonesia (SKI) 2023 mencatat prevalensi hipertensi pada penduduk berusia 18 tahun ke atas sebesar **30,8%** berdasarkan hasil pengukuran tekanan darah, sementara prevalensi berdasarkan diagnosis dokter hanya **8,6%**. Selisih lebih dari 22 poin persen ini berarti sebagian besar penderita hipertensi di Indonesia tidak pernah tahu bahwa dirinya sakit, sehingga tidak pernah menjalani pengobatan. Pola yang sama terjadi pada diabetes melitus: prevalensi berdasarkan pemeriksaan gula darah pada penduduk usia 15 tahun ke atas mencapai 11,7%, sedangkan yang terdiagnosis dokter hanya 2,2%.

Celah itu nyaris tidak bergerak dalam lima tahun. Riskesdas 2018 mencatat selisih sekitar 25 poin persen, yaitu 34,1% berdasarkan pengukuran berbanding 8,8% berdasarkan diagnosis, dan pada SKI 2023 selisihnya masih sekitar 22 poin persen. Penurunan prevalensi hipertensi tidak diiringi perbaikan kemampuan mendeteksi penderitanya. Beban itu berlanjut sampai tahap pengobatan: dari penduduk yang sudah terdiagnosis hipertensi, hanya 46,7% meminum obat secara teratur.

Sebagian besar penderita ini pernah datang ke puskesmas dan pernah diukur tekanan darahnya. Hasil pengukuran itu diperlakukan sebagai catatan kunjungan hari itu saja, baik ketika ditulis di lembar kertas maupun ketika diketik ke sistem elektronik yang tidak membandingkan nilai antar-waktu. Tidak ada mekanisme yang menghubungkan angka tinggi hari ini dengan angka tinggi tiga bulan lalu.

### Keterkaitan dengan SDGs

Kelompok kami memilih **SDG 3: *Good Health and Well-being* (Kehidupan Sehat dan Sejahtera)** sebagai landasan solusi. Agar lingkup pengerjaan tetap terkendali, perangkat lunak yang kami usulkan menyasar dua target berikut secara terfokus, bukan menyentuh banyak target sekaligus secara dangkal:

| Target SDG 3 | Rumusan Target | Keterkaitan dengan Solusi |
| :--- | :--- | :--- |
| **3.4** | Mengurangi sepertiga angka kematian dini akibat penyakit tidak menular melalui pencegahan dan pengobatan | Sistem menyimpan riwayat tanda vital lintas kunjungan dan secara otomatis menandai pasien yang hasil pengukurannya berada di luar ambang normal, sehingga deteksi dini tidak lagi bergantung pada ingatan petugas |
| **3.8** | Mencapai cakupan kesehatan semesta (*Universal Health Coverage*), termasuk akses pelayanan kesehatan dasar yang bermutu | Sistem memangkas waktu administrasi di FKTP sehingga waktu tenaga kesehatan dapat dialihkan untuk pelayanan, dan mutu pencatatan menjadi seragam |

### Urgensi

**Pertama, kewajiban regulasi sudah berjalan penuh, dan pemenuhannya tidak mudah.** PMK 24/2022 menetapkan tenggat penyelenggaraan RME pada 31 Desember 2023. Tenggat itu diundur hingga akhir 2025 karena hambatan implementasi, dan Kementerian Kesehatan masih perlu menerbitkan surat edaran penegasan pada 15 April 2025. Dengan diresmikannya SATUSEHAT RME pada 1 September 2026, setiap puskesmas wajib memiliki sistem pencatatan elektronik yang mampu mengirim data ke platform nasional. Pengunduran tenggat selama dua tahun menunjukkan bahwa menyediakan sistem yang dapat dipakai di lapangan bukan perkara sepele, terutama bagi fasilitas dengan infrastruktur terbatas.

**Kedua, dan ini yang paling menentukan, digitalisasi tidak dengan sendirinya menutup celah deteksi dini.** Antara 2018 dan 2023 terjadi digitalisasi besar-besaran: PMK 24/2022 terbit, SATUSEHAT diluncurkan, ribuan fasilitas terintegrasi. Celah antara prevalensi terukur dan terdiagnosis hanya bergerak dari 25 menjadi 22 poin persen. SKI 2023 diukur setelah kewajiban RME berlaku, sehingga angka itu memotret sistem kesehatan yang sudah mulai terdigitalisasi, bukan puskesmas serba kertas. Memindahkan catatan dari kertas ke layar belum cukup.

**Ketiga, solusi yang tersedia belum menjawab kedua persoalan itu sekaligus.** Sistem Informasi Manajemen Puskesmas (SIMPUS) komersial membebankan biaya langganan dan bergantung pada internet yang stabil, sementara sistem yang disediakan pemerintah menghadapi kendala pembaruan dan tata kelola sebagaimana dilaporkan sejumlah penelitian implementasi. Yang lebih penting, hampir seluruhnya dirancang sebagai sistem pencatatan administratif, bukan sebagai sistem yang aktif memantau perubahan kondisi pasien antar-waktu.

## 1.2 Analisis Kondisi Saat Ini

### Alur proses yang berjalan saat ini

Kewajiban RME pada PMK 24/2022 kini berjalan penuh. Pada **1 September 2026** Kementerian Kesehatan meresmikan **SATUSEHAT RME**, yang menyatukan data klinis sekitar 280 juta penduduk antar-fasilitas sehingga pasien tidak perlu membawa berkas fisik ketika dirujuk. Alur di bawah ini karena itu menggambarkan puskesmas yang **sudah** menjalankan rekam medis elektronik. Menggambarkan kondisi saat ini sebagai era berkas kertas tidak lagi mencerminkan lapangan.

1. **Pendaftaran.** Pasien mengambil nomor antrean, lalu petugas mencari data pasien pada aplikasi rekam medis berdasarkan NIK atau nomor rekam medis. Data pasien lama langsung tampil, sedangkan pasien baru diinput saat itu juga. Kunjungan hari itu dibuat sebagai entri baru pada basis data.
2. **Skrining awal.** Perawat memanggil pasien, mengukur tanda vital, lalu mengetikkan hasilnya ke formulir kunjungan pada aplikasi. Nilai tersebut tersimpan sebagai bagian dari entri kunjungan hari itu.
3. **Pemeriksaan.** Dokter membuka entri kunjungan, mencatat anamnesis, diagnosis berkode, dan tindakan. Resep disusun pada modul yang sama atau dicetak untuk diserahkan kepada pasien.
4. **Farmasi.** Apoteker menerima resep, menyiapkan obat, lalu mencatat pengeluaran obat. Pada sebagian sistem, pencatatan persediaan masih dilakukan terpisah dari modul pelayanan.
5. **Pelaporan dan pengiriman data.** Sistem menyusun rekapitulasi kunjungan, dan data kunjungan dikirimkan ke SATUSEHAT sesuai kewajiban interoperabilitas.

Seluruh langkah di atas sudah elektronik. Satu hal **tidak berubah** sejak era kertas: setiap kunjungan tetap diperlakukan sebagai entri yang berdiri sendiri. Tekanan darah tinggi hari ini tersimpan rapi dan terkirim ke SATUSEHAT, tetapi tidak ada proses yang membandingkannya dengan pengukuran tiga bulan lalu lalu menyimpulkan bahwa pasien tersebut perlu ditindaklanjuti. Riwayat dapat dibuka satu per satu, namun menarik kesimpulan darinya tetap menjadi pekerjaan petugas yang sedang melayani puluhan pasien.

Sebagian puskesmas di wilayah dengan keterbatasan jaringan dan sumber daya manusia masih menjalankan pencatatan kertas atau hibrida. Pada kelompok ini beban administratif lama masih ditanggung penuh, dan model hibrida menyulitkan penelusuran riwayat karena catatan seorang pasien terbelah di dua media.

### Kesenjangan (*gap*) yang teridentifikasi

Dari alur di atas, kami mengidentifikasi tujuh kesenjangan yang akan menjadi sasaran perangkat lunak kami.

Kolom terakhir menandai kepada siapa kesenjangan tersebut berlaku. Sebagian kesenjangan hilang ketika puskesmas beralih ke rekam medis elektronik, sebagian lain bertahan pada puskesmas yang sudah digital sekalipun. Kelompok kedua itulah sasaran utama perangkat lunak ini.

| Kode | Kesenjangan | Penjelasan | Dampak | Berlaku pada |
| :--- | :--- | :--- | :--- | :--- |
| **G-01** | Pencarian data rekam medis lambat | Berkas dicari manual di rak, atau riwayat pasien harus dirangkai dari dua media pada puskesmas hibrida | Waktu tunggu pasien bertambah, antrean menumpuk di jam sibuk | Manual dan hibrida |
| **G-02** | Berkas hilang, rusak, atau tidak terbaca | Kertas rentan terhadap kelembapan dan kehilangan; tulisan tangan dokter sering sulit dibaca oleh apoteker | Risiko kesalahan pemberian obat dan riwayat pasien yang tidak dapat direkonstruksi | Manual dan hibrida |
| **G-03** | Riwayat kesehatan tidak berkesinambungan | Setiap kunjungan diperlakukan sebagai catatan yang berdiri sendiri, tanpa mekanisme membandingkan hasil antar-waktu, baik pada lembar kertas maupun pada basis data | Tren kondisi pasien seperti tekanan darah yang terus naik tidak pernah terlihat | **Seluruhnya, termasuk yang sudah digital** |
| **G-04** | Tidak ada mekanisme deteksi dini | Penandaan pasien berisiko sepenuhnya bergantung pada kejelian dan ingatan petugas, karena sistem tidak pernah memunculkannya sendiri | Pasien berisiko PTM lolos dari pemantauan, sejalan dengan celah 22 poin persen pada SKI 2023 | **Seluruhnya, termasuk yang sudah digital** |
| **G-05** | Stok obat tidak terpantau secara *real-time* | Pengecekan stok dilakukan secara fisik dan pencatatan dilakukan menyusul | Terjadi kekosongan obat mendadak dan obat kedaluwarsa yang tidak terdeteksi | Manual dan hibrida |
| **G-06** | Rekapitulasi laporan lambat dan rawan salah | Data harus dibaca ulang dari lembar kunjungan lalu diketik ulang ke format laporan | Laporan terlambat, rawan salah hitung, dan menyita waktu tenaga kesehatan | Manual dan hibrida |
| **G-07** | Solusi digital eksisting bersifat administratif dan mengandaikan jaringan stabil | Sistem yang tersedia berfokus pada penyimpanan dan penampilan ulang data, bukan pada pemantauan antar-waktu; sebagian besar juga berbasis awan atau berlangganan | Celah deteksi dini tetap terbuka meskipun fasilitas sudah terdigitalisasi, sementara puskesmas berjaringan terbatas kesulitan mengadopsinya sama sekali | **Seluruhnya, termasuk yang sudah digital** |

**G-03, G-04, dan G-07 tidak hilang meskipun puskesmas sudah digital.** Ketiganya inti permasalahan yang diangkat. G-01, G-02, G-05, dan G-06 merupakan beban tambahan yang masih ditanggung puskesmas manual dan hibrida.

### Perbandingan dengan solusi yang sudah ada

| Solusi | Kelebihan | Keterbatasan terhadap konteks masalah |
| :--- | :--- | :--- |
| **SATUSEHAT RME** (diresmikan 1 September 2026) | Mengintegrasikan data klinis sekitar 280 juta penduduk antar-fasilitas, memungkinkan pasien melihat riwayatnya tanpa membawa berkas fisik, dengan pengamanan berlapis setara sistem perbankan | Merupakan lapisan integrasi nasional dan kanal akses bagi pasien, bukan aplikasi operasional yang menjalankan antrean, pemeriksaan, peresepan, dan farmasi di dalam puskesmas. Fasilitas tetap wajib memiliki sistem pencatatannya sendiri untuk dapat mengirim data ke sini, dan pengirimannya menuntut koneksi internet |
| **SIMPUS atau RME komersial berbasis awan** | Fitur lengkap, sudah terhubung ke SATUSEHAT, tersedia dukungan teknis | Berbayar per bulan, membutuhkan internet stabil, dan pelayanan terhenti saat koneksi putus (G-07) |
| **SIKDA Generik** (sistem gratis Kemenkes untuk puskesmas) | Gratis, resmi dari Kementerian Kesehatan, dan dirancang khusus untuk alur kerja puskesmas | Penelitian implementasi melaporkan kendala berupa keterbatasan sumber daya manusia terlatih, keterbatasan infrastruktur, pembaruan sistem yang lambat, dan tata kelola yang belum mapan. Sebagaimana SIMPUS pada umumnya, sistem ini bersifat administratif dan tidak melakukan pemantauan risiko antar-waktu (G-03, G-04) |
| **ASRI** (RME gratis Kemenkes) | Gratis dan sudah terhubung ke SATUSEHAT sejak awal | Diperuntukkan bagi Tempat Praktik Mandiri Dokter dan Dokter Gigi, bukan untuk alur pelayanan puskesmas yang melibatkan banyak peran sekaligus |
| **Pencatatan manual (kertas)** | Tidak butuh perangkat, tidak butuh pelatihan, tidak bergantung listrik | Menanggung seluruh kesenjangan G-01 sampai G-06 sekaligus, dan sejak PMK 24/2022 tidak lagi memenuhi kewajiban regulasi |
| **Spreadsheet mandiri (Excel)** | Gratis dan sudah dikuasai sebagian petugas | Tidak ada validasi data, relasi antar-tabel, maupun kendali akses; tidak dapat mengirim data ke SATUSEHAT sehingga tidak memenuhi kewajiban RME; tidak menyelesaikan G-03 dan G-04 |

Seluruh alternatif di atas, termasuk yang disediakan pemerintah, dirancang sebagai **sistem pencatatan dan pertukaran data yang pasif**. Semuanya menyimpan hasil pemeriksaan, mengirimkannya ke tingkat nasional, dan menampilkannya kembali bila diminta, tanpa membandingkan hasil seorang pasien antar-waktu lalu memunculkannya kepada petugas sebagai pasien yang perlu ditindaklanjuti. Inilah sebabnya digitalisasi yang sudah berjalan luas belum menutup celah deteksi pada SKI 2023.

Kesimpulannya, celah yang belum terisi adalah **sistem yang secara aktif memantau perubahan kondisi pasien antar-waktu dan mengubahnya menjadi daftar tindak lanjut bagi petugas, tetap berjalan penuh tanpa internet sehingga dapat dipakai di fasilitas dengan jaringan terbatas, namun tetap memenuhi kewajiban pengiriman data ke SATUSEHAT**. Celah inilah yang akan diisi oleh perangkat lunak kami.

---

# BAB 2: Analisis Solusi

## 2.1 Deskripsi Perangkat Lunak

**SEHATI (Sistem Elektronik Pelayanan Kesehatan Terintegrasi)** adalah aplikasi *desktop* pengelolaan pelayanan rawat jalan untuk puskesmas. SEHATI menyatukan seluruh rantai pelayanan, mulai dari pendaftaran pasien, skrining tanda vital, pemeriksaan dokter, peresepan elektronik, penyerahan obat, hingga rekapitulasi laporan, ke dalam satu aplikasi yang dipakai bersama oleh seluruh petugas di satu fasilitas.

Pembedanya terletak pada apa yang dilakukan sistem terhadap data yang terkumpul. SEHATI membandingkan hasil pengukuran seorang pasien antar-waktu, menandai yang polanya mengarah ke risiko, lalu menyusunnya menjadi **daftar pantau yang ditindaklanjuti petugas**. Kelengkapan alur pelayanan menjadi prasyaratnya, sebab tanpa data yang terkumpul rapi sebagai efek samping pekerjaan harian, tidak ada yang bisa dibandingkan.

### Gambaran dari sudut pandang pengguna

**Petugas Administrasi** menemukan data pasien secara instan lewat NIK atau nomor rekam medis, lalu menerbitkan nomor antrean, tanpa perlu membongkar rak berkas maupun merangkai riwayat dari dua media seperti pada puskesmas hibrida. Di luar jam pelayanan, peran yang sama menyusun rekapitulasi kunjungan, mengekspor laporan, dan menjalankan sinkronisasi data ke SATUSEHAT, semuanya tanpa mengetik ulang apa pun.

**Perawat** memasukkan hasil skrining tanda vital ke formulir digital, dan sistem langsung membandingkannya dengan ambang klinis sekaligus dengan riwayat pengukuran pasien tersebut. Perawat juga memegang daftar pantau: melihat siapa yang perlu dihubungi, lalu mencatat hasil upaya tersebut. Inilah titik tempat pemantauan benar-benar berlanjut, bukan berhenti sebagai penanda di layar.

Bagi **Dokter**, riwayat kesehatan pasien mulai dari diagnosis lampau, obat yang pernah diberikan, hingga tren tekanan darah tersaji dalam satu layar sebelum pemeriksaan dimulai, lengkap dengan penanda risiko bila ada. Resep disusun secara elektronik dan ketersediaan stoknya tervalidasi saat itu juga.

**Apoteker** menerima resep dalam bentuk teks yang tidak mungkin salah baca beserta status stok yang sudah tervalidasi sejak di ruang periksa, serta mengelola persediaan obat masuk beserta peringatan stok menipis dan mendekati kedaluwarsa.

### Target platform dan alasan pemilihannya

SEHATI dikembangkan sebagai **aplikasi desktop** yang dipasang pada komputer di lingkungan puskesmas. Pemilihan ini didasarkan pada empat pertimbangan.

1. **Kemandirian terhadap koneksi internet.** Kesenjangan G-07 menunjukkan ketergantungan pada jaringan stabil menjadi penghalang adopsi di daerah, sejalan dengan pengakuan Kementerian Kesehatan mengenai keterbatasan infrastruktur. Aplikasi desktop dengan basis data lokal tetap berfungsi saat jaringan terputus, dan pelayanan kesehatan tidak boleh berhenti karena hal itu.
2. **Kesesuaian dengan perangkat yang sudah tersedia.** Puskesmas umumnya sudah memiliki komputer atau laptop di loket pendaftaran dan ruang periksa. Aplikasi desktop yang ringan dapat memanfaatkan perangkat tersebut tanpa pengadaan baru.
3. **Kesesuaian dengan pola kerja.** Pendaftaran, pemeriksaan, dan farmasi dilakukan di meja kerja tetap dengan intensitas pengetikan yang tinggi. Antarmuka desktop dengan papan ketik penuh dan pintasan papan ketik lebih efisien untuk pola kerja seperti ini dibandingkan antarmuka sentuh.
4. **Kendali atas data sensitif.** Data rekam medis tersimpan di dalam lingkungan fasilitas itu sendiri, sehingga kendali dan tanggung jawab penyimpanan tetap berada di tangan fasilitas, sejalan dengan kewajiban menjaga kerahasiaan rekam medis.

### Nilai unik dan inovasi inti

Pembeda utama SEHATI dari SIMPUS pada umumnya terletak pada tiga hal berikut.

**1. Pemantauan longitudinal dengan daftar pantau yang tertutup siklusnya (inovasi inti).**
Kekuatan modul ini terletak pada apa yang terjadi sesudah ambang klinis terlampaui. Setiap hasil pengukuran disimpan sebagai bagian dari rangkaian riwayat pasien. Sistem membandingkan nilai terbaru terhadap ambang klinis sekaligus terhadap pola pengukuran pasien pada kunjungan sebelumnya, sehingga tekanan darah tinggi yang berulang dapat dibedakan dari lonjakan sesaat. Pasien yang polanya mengarah ke risiko masuk ke **daftar pantau**. Petugas kemudian mencatat upaya tindak lanjut dan status kedatangan pasien pada kunjungan ulang, sehingga siklus pemantauan tertutup. Inilah cara SEHATI menutup celah antara pasien yang pernah diukur dan yang terdiagnosis lalu tertangani, yaitu celah 30,8% berbanding 8,6% pada SKI 2023.

**2. Arsitektur *offline-first* dengan sinkronisasi tunda ke SATUSEHAT.**
Fasilitas dengan jaringan terbatas terjebak di antara dua pilihan buruk: sistem berbasis awan yang patuh regulasi tetapi berhenti melayani ketika jaringan putus, atau pencatatan lokal seadanya yang selalu jalan tetapi tidak memenuhi kewajiban pelaporan. SEHATI menjalankan seluruh fungsi pelayanan di atas basis data lokal tanpa memerlukan internet. Setiap kunjungan yang ditutup disusun menjadi data berstandar **HL7 FHIR** lalu masuk ke **antrean sinkronisasi**. Ketika koneksi tersedia, termasuk di luar jam pelayanan, antrean dikirim ke SATUSEHAT dan ditandai selesai. Bila koneksi tidak kunjung ada, antrean diekspor sebagai berkas untuk diunggah dari lokasi lain. Pelayanan tidak berhenti karena internet, dan kewajiban PMK 24/2022 tetap terpenuhi.

**3. Alur kerja terpadu dari pendaftaran hingga farmasi.**
Data mengalir dalam satu aplikasi tanpa penyalinan ulang antar-tahap. Resep yang disusun dokter langsung tervalidasi terhadap stok apotek pada saat penyusunan, sehingga penggantian obat karena stok kosong terjadi di ruang periksa, bukan setelah pasien mengantre di apotek.

### Ketahanan data

Menyimpan seluruh rekam medis pada satu basis data lokal memunculkan risiko yang perlu dijawab. Arsip kertas hilang satu per satu, sedangkan kerusakan satu media penyimpanan dapat melenyapkan seluruh rekam medis sekaligus. Karena itu pencadangan tidak diserahkan pada kedisiplinan petugas. SEHATI mencadangkan basis data menurut jadwal ke media penyimpanan terpisah, menampilkan status pencadangan terakhir, dan memperingatkan pengguna bila pencadangan gagal. Antrean sinkronisasi ke SATUSEHAT berperan sebagai salinan kedua di luar fasilitas.

### Pemetaan kesenjangan terhadap solusi

Tabel berikut menelusuri setiap kesenjangan pada Subbab 1.2 menuju bagian solusi yang menjawabnya, sekaligus menuju aktivitas yang mewujudkannya pada Subbab 3.3. Tidak ada kesenjangan yang dibiarkan tanpa jawaban, dan sebaliknya tidak ada fitur yang tidak berpangkal pada kesenjangan.

| Kesenjangan | Bagian solusi yang menjawab | Aktivitas |
| :--- | :--- | :--- |
| **G-01** Pencarian data rekam medis lambat | Alur kerja terpadu: data pasien ditelusuri sistem berdasarkan NIK, dan antrean diterbitkan otomatis, bukan dicari di rak atau dirangkai dari dua media | A01, A02, A03, A04 |
| **G-02** Berkas hilang, rusak, atau tidak terbaca | Seluruh pencatatan bersifat elektronik, diagnosis dan resep berupa teks digital yang tidak mungkin salah baca, riwayat dapat ditelusuri utuh, dan ditopang pencadangan terjadwal | A02, A09, A10, A11, A14, A15, A27 |
| **G-03** Riwayat kesehatan tidak berkesinambungan | Pemantauan longitudinal: pengukuran disimpan sebagai satu rangkaian riwayat, dibandingkan antar-waktu, lalu disajikan sebagai tren, bukan sebagai catatan lepas per kunjungan | A05, A06, A08 |
| **G-04** Tidak ada mekanisme deteksi dini | Inovasi inti: evaluasi ambang otomatis, penandaan kunjungan berisiko, penyampaian penanda kepada dokter, penjadwalan kunjungan ulang, daftar pantau, dan pencatatan hasil tindak lanjut | A06, A07, A09, A13, A21, A22 |
| **G-05** Stok obat tidak terpantau *real-time* | Validasi stok saat resep disusun, pemotongan stok otomatis saat obat diserahkan, serta pencatatan obat masuk dengan peringatan ambang | A12, A16, A18, A19, A20 |
| **G-06** Rekapitulasi laporan lambat dan rawan salah | Rekapitulasi dihitung sistem dari data kunjungan lalu diekspor sebagai berkas siap unggah | A23 |
| **G-07** Solusi eksisting pasif dan mengandaikan jaringan stabil | Dijawab dua arah: sifat pasif dijawab oleh inovasi inti pada G-04, sedangkan ketergantungan jaringan dijawab arsitektur *offline-first* dengan sinkronisasi tunda | A06, A07, A21, A22, A24, A25, A26 |

Satu aktivitas berada di luar tabel ini, yaitu **A17 Mengelola Akun Pengguna dan Data Master**. Aktivitas tersebut tidak berpangkal pada kesenjangan di Subbab 1.2, melainkan pada batasan regulasi BR-02 yang mengharuskan akses data pasien dibatasi berdasarkan peran, sekaligus menjadi prasyarat agar ambang nilai risiko dan daftar obat dapat disesuaikan dengan kebijakan fasilitas.

**G-03, G-04, dan G-07 bertahan meskipun sebuah puskesmas sudah menjalankan rekam medis elektronik.** Ketiganya dijawab nilai unik pertama dan kedua, dan di situlah alasan keberadaan perangkat lunak ini. G-01, G-02, G-05, dan G-06 dijawab nilai unik ketiga, yang manfaatnya dirasakan puskesmas manual dan hibrida.

## 2.2 Asumsi dan Batasan

### Asumsi

**Asumsi teknis**

| Kode | Asumsi |
| :--- | :--- |
| AT-01 | Fasilitas kesehatan memiliki minimal satu komputer atau laptop dengan sistem operasi Windows/Linux 64-bit, RAM minimal 4 GB, dan ruang penyimpanan kosong minimal 2 GB |
| AT-02 | Perangkat memperoleh pasokan listrik yang memadai selama jam pelayanan; pemadaman singkat tidak menyebabkan kerusakan basis data karena setiap transaksi disimpan secara *atomic* |
| AT-03 | Basis data disimpan di dalam lingkungan fasilitas, sehingga seluruh fungsi operasional aplikasi dapat berjalan penuh tanpa koneksi internet |
| AT-04 | Bila digunakan oleh beberapa petugas secara bersamaan, seluruh perangkat berada dalam satu jaringan lokal (LAN) yang sama dengan satu perangkat berperan sebagai penyimpan basis data |
| AT-05 | Tersedia media penyimpanan terpisah (cakram eksternal atau berbagi jaringan) sebagai tujuan pencadangan otomatis yang dijadwalkan aplikasi |
| AT-06 | Koneksi internet tersedia sesekali dan tidak harus terus-menerus, sehingga sinkronisasi ke SATUSEHAT dapat dijalankan secara berkala; untuk pengembangan dan pengujian digunakan lingkungan *sandbox* SATUSEHAT, bukan lingkungan produksi |

**Asumsi pengguna**

| Kode | Asumsi |
| :--- | :--- |
| AP-01 | Seluruh pengguna mampu mengoperasikan komputer pada tingkat dasar (mengetik, menggunakan tetikus, mengelola berkas) |
| AP-02 | Pengguna adalah tenaga kesehatan atau tenaga administrasi resmi fasilitas yang memiliki wewenang mengakses data pasien sesuai perannya |
| AP-03 | Pengukuran tanda vital dilakukan menggunakan alat medis terpisah; sistem hanya menerima dan mengolah hasil yang dimasukkan secara manual, bukan membaca alat secara langsung |
| AP-04 | Keputusan klinis sepenuhnya berada di tangan tenaga medis; penanda risiko yang dihasilkan sistem berperan sebagai alat bantu pengingat, bukan sebagai diagnosis |
| AP-05 | Setiap pengguna memiliki akun sendiri dan tidak membagikan kredensialnya kepada pengguna lain |

### Batasan

**Batasan regulasi dan hukum**

| Kode | Batasan |
| :--- | :--- |
| BR-01 | Sistem harus tunduk pada Peraturan Menteri Kesehatan Nomor 24 Tahun 2022 tentang Rekam Medis, khususnya kewajiban menyelenggarakan rekam medis elektronik yang terhubung dengan platform nasional SATUSEHAT |
| BR-02 | Data pasien merupakan data pribadi bersifat spesifik menurut Undang-Undang Nomor 27 Tahun 2022 tentang Pelindungan Data Pribadi, sehingga akses dibatasi berdasarkan peran dan setiap perubahan data direkam pada jejak audit di dalam basis data. Antarmuka khusus untuk menelusuri jejak audit tersebut berada di luar lingkup |
| BR-03 | Kerahasiaan rekam medis wajib dijaga. Data pasien hanya boleh dikirimkan melalui kanal resmi yang diwajibkan regulasi, yaitu platform SATUSEHAT milik Kementerian Kesehatan, dan tidak boleh dikirimkan kepada pihak ketiga komersial mana pun maupun disimpan pada layanan awan pihak ketiga |
| BR-04 | Sistem tidak menggantikan kewenangan klinis tenaga medis dan tidak menerbitkan diagnosis secara mandiri |

**Batasan sumber daya**

| Kode | Batasan |
| :--- | :--- |
| BS-01 | Pengembangan dilakukan oleh 5 orang anggota kelompok dalam rentang waktu satu semester perkuliahan |
| BS-02 | Tidak tersedia anggaran untuk layanan berbayar, sehingga seluruh pustaka dan perkakas yang digunakan bersifat sumber terbuka atau gratis |
| BS-03 | Pengujian dilakukan menggunakan data sintetis (bukan data pasien sungguhan) untuk menghindari pelanggaran kerahasiaan rekam medis |
| BS-04 | Verifikasi kebutuhan dilakukan melalui studi literatur dan regulasi; wawancara lapangan dengan tenaga kesehatan dilakukan sejauh dapat dijangkau selama masa pengerjaan |

**Batasan ruang lingkup**

| Kode | Batasan |
| :--- | :--- |
| BL-01 | Sistem hanya mencakup pelayanan **rawat jalan**; rawat inap, gawat darurat, dan tindakan bedah berada di luar lingkup |
| BL-02 | Integrasi data eksternal dibatasi pada kanal resmi SATUSEHAT melalui mekanisme sinkronisasi tunda; integrasi dengan P-Care BPJS Kesehatan dan sistem informasi dinas kesehatan berada di luar lingkup, dan keluaran untuk keduanya disediakan sebagai berkas laporan yang diunggah secara manual |
| BL-03 | Sistem tidak menangani pemeriksaan laboratorium kompleks maupun radiologi; hanya hasil pemeriksaan sederhana yang dapat dicatat sebagai teks |
| BL-04 | Sistem tidak memproses transaksi pembayaran, klaim asuransi, maupun penggajian |
| BL-05 | Sistem digunakan untuk satu fasilitas kesehatan; sinkronisasi data antar-fasilitas berada di luar lingkup |
| BL-06 | Penanda risiko dibatasi pada penyakit tidak menular yang parameternya terukur rutin di puskesmas, yaitu hipertensi, obesitas, dan indikasi diabetes berdasarkan gula darah sewaktu. Pemantauan kesehatan ibu dan penilaian risiko di luar parameter tersebut berada di luar lingkup |
| BL-07 | Pasien tidak berinteraksi langsung dengan sistem; seluruh masukan data dilakukan oleh petugas fasilitas |

### Prioritas pengembangan

Lingkup di atas melampaui apa yang dapat diselesaikan sekaligus oleh lima orang dalam satu semester (BS-01). Karena itu pengembangan dibagi menjadi dua tahap.

**Tahap inti**, yang wajib selesai, mencakup seluruh alur pelayanan rawat jalan (aktivitas A01 sampai A16 pada Gambar 1) ditambah penyusunan daftar pantau dan pencatatan tindak lanjut (A21 dan A22). Rangkaian ini sudah membentuk satu siklus pelayanan yang utuh sekaligus memuat nilai unik utama perangkat lunak ini.

**Tahap lanjutan**, yang dikerjakan setelah tahap inti stabil, mencakup sisa aktivitas pada Gambar 2, yaitu pengelolaan akun dan data master (A17), pengelolaan persediaan obat (A18 sampai A20), pelaporan periodik (A23), sinkronisasi ke SATUSEHAT (A24 sampai A26), serta pencadangan terjadwal (A27).

Pembagian ini memastikan bahwa apabila waktu tidak mencukupi, yang tertunda adalah fitur pelengkap, bukan alur pelayanan inti.

---

# BAB 3: Spesifikasi Kebutuhan dan Proses Bisnis

## 3.1 Identifikasi Aktor

Aktor dibatasi pada empat peran pengguna yang **berinteraksi langsung dengan sistem** dalam pekerjaan sehari-harinya. Peran administratif seperti pengelolaan akun, data master, pelaporan, dan sinkronisasi digabungkan ke dalam Petugas Administrasi, karena pada puskesmas non rawat inap pekerjaan tersebut umumnya ditangani petugas yang sama dengan yang melayani loket pendaftaran.

| Aktor | Deskripsi |
| :--- | :--- |
| **Petugas Administrasi** | Pengguna yang bertugas di loket sekaligus menangani administrasi sistem. Bertanggung jawab mendaftarkan pasien, mencari data pasien, membuat kunjungan dan antrean, menyusun laporan periodik, mengelola akun pengguna beserta data master, serta menjalankan sinkronisasi data ke SATUSEHAT. Karakteristiknya adalah bekerja di bawah tekanan antrean pada jam sibuk, sehingga mengutamakan kecepatan pencarian data dan minimnya langkah untuk menyelesaikan satu pendaftaran. |
| **Perawat** | Tenaga kesehatan yang melakukan skrining awal sebelum pasien diperiksa dokter, sekaligus menindaklanjuti pasien yang ditandai berisiko. Bertanggung jawab mencatat keluhan awal dan tanda vital, membaca tren pengukuran pasien, serta mencatat hasil upaya tindak lanjut. Karakteristiknya adalah menangani banyak pasien dalam waktu singkat, sehingga membutuhkan formulir masukan yang ringkas dengan validasi nilai agar tidak terjadi salah ketik angka. |
| **Dokter** | Tenaga medis yang melakukan pemeriksaan, menegakkan diagnosis, menentukan tindakan, meresepkan obat, dan menjadwalkan kunjungan ulang. Karakteristiknya adalah membutuhkan gambaran riwayat pasien yang utuh dalam waktu singkat dan menuntut agar sistem tidak memperlambat proses pemeriksaan. Dokter merupakan pengguna dengan kewenangan tertinggi atas isi rekam medis pasien. |
| **Apoteker** | Tenaga kefarmasian yang menyiapkan serta menyerahkan obat kepada pasien dan mengelola persediaan obat puskesmas. Karakteristiknya adalah membutuhkan resep yang terbaca jelas dan informasi stok yang akurat, serta bertanggung jawab atas ketertelusuran pengeluaran obat. |

Selain keempat aktor pengguna di atas, model proses bisnis pada Subbab 3.4 memuat satu lajur tambahan yang mewakili perangkat lunak itu sendiri.

| Lajur sistem | Deskripsi |
| :--- | :--- |
| **Sistem SEHATI** | Bukan pengguna, melainkan perangkat lunak yang sedang dirancang. Lajur ini memuat aktivitas yang dijalankan sistem secara mandiri tanpa campur tangan manusia, seperti pencarian data, evaluasi ambang risiko, validasi stok, penyusunan daftar pantau, pengiriman data ke SATUSEHAT, dan pencadangan terjadwal. Lajur ini disertakan agar terlihat jelas pekerjaan mana yang beralih dari petugas kepada perangkat lunak. |

> **Catatan.** Pasien merupakan pemangku kepentingan utama yang memperoleh manfaat dari sistem, namun bukan aktor karena tidak berinteraksi langsung dengan perangkat lunak (lihat batasan BL-07). Kepala Puskesmas juga tidak dijadikan aktor, karena kebutuhannya terhadap laporan dan daftar pantau dipenuhi melalui keluaran yang disiapkan Petugas Administrasi. SATUSEHAT tidak dimodelkan sebagai lajur tersendiri karena merupakan sistem eksternal di luar batas perangkat lunak ini; pengiriman data kepadanya diperlakukan sebagai aktivitas milik Sistem SEHATI.

## 3.2 Kebutuhan Pengguna Awal

| ID | Aktor | Kebutuhan / Aktivitas | Tujuan / Nilai |
| :--- | :--- | :--- | :--- |
| US-01 | Petugas Administrasi | Mencari data pasien berdasarkan NIK, nama, atau nomor rekam medis | Data pasien ditemukan dalam hitungan detik tanpa perlu merangkainya dari rak berkas atau dari dua media terpisah |
| US-02 | Petugas Administrasi | Mendaftarkan pasien baru dan memperbarui data pasien yang berubah | Setiap pasien memiliki satu rekam medis elektronik yang datanya tetap mutakhir |
| US-03 | Petugas Administrasi | Membuat data kunjungan beserta poli tujuan dan nomor antreannya | Setiap kedatangan pasien tercatat, terhubung dengan riwayatnya, dan urutan pelayanan menjadi jelas |
| US-04 | Petugas Administrasi | Melihat rekapitulasi kunjungan dan diagnosis per periode lalu mengekspornya sebagai berkas laporan | Laporan periodik tersusun tanpa perlu menghitung dan mengetik ulang data |
| US-05 | Petugas Administrasi | Mengelola akun pengguna beserta hak aksesnya dan data master obat, poli, serta ambang nilai risiko | Akses data pasien terbatas pada petugas berwenang, dan sistem dapat disesuaikan dengan kebijakan fasilitas |
| US-06 | Petugas Administrasi | Menjalankan sinkronisasi data kunjungan ke SATUSEHAT ketika koneksi internet tersedia | Kewajiban pelaporan rekam medis elektronik terpenuhi tanpa membuat pelayanan bergantung pada internet |
| US-07 | Petugas Administrasi | Memperoleh cadangan basis data secara otomatis beserta peringatan bila pencadangan gagal | Rekam medis tidak hilang seluruhnya bila terjadi kerusakan perangkat penyimpanan |
| US-08 | Perawat | Melihat daftar antrean pasien yang menunggu skrining | Pemanggilan pasien berjalan tertib sesuai urutan |
| US-09 | Perawat | Mencatat keluhan awal dan tanda vital pasien, termasuk gula darah sewaktu bila tersedia, serta memperoleh peringatan otomatis bila nilainya di luar ambang normal | Hasil pengukuran tersimpan permanen dan pasien berisiko tidak terlewat meskipun antrean sedang padat |
| US-10 | Perawat | Melihat tren tanda vital pasien dari kunjungan-kunjungan sebelumnya | Perubahan kondisi pasien antar-waktu dapat dikenali sejak tahap skrining |
| US-11 | Perawat | Melihat daftar pantau pasien berisiko dan mencatat hasil upaya tindak lanjutnya | Pemantauan pasien berisiko benar-benar berlanjut dan tidak berhenti pada penandaan saja |
| US-12 | Dokter | Melihat rekam medis, riwayat kunjungan, dan penanda risiko pasien dalam satu tampilan | Keputusan klinis diambil berdasarkan gambaran kondisi pasien yang utuh, dan pasien berisiko langsung ditindaklanjuti pada kunjungan yang sama |
| US-13 | Dokter | Mencatat anamnesis, diagnosis, dan tindakan pada kunjungan berjalan | Hasil pemeriksaan terdokumentasi rapi dan terbaca oleh seluruh petugas |
| US-14 | Dokter | Menyusun resep elektronik dari daftar obat puskesmas sekaligus mengetahui ketersediaan stoknya | Kesalahan penulisan obat dihindari, dan penggantian obat karena stok kosong dilakukan saat pasien masih di ruang periksa |
| US-15 | Dokter | Menjadwalkan kunjungan ulang bagi pasien yang perlu dipantau | Pemantauan pasien berisiko berlanjut dan tidak berhenti pada satu kunjungan |
| US-16 | Apoteker | Melihat antrean resep yang masuk dari ruang pemeriksaan | Penyiapan obat dapat dimulai sebelum pasien tiba di apotek |
| US-17 | Apoteker | Menandai resep yang obatnya telah diserahkan kepada pasien | Status pelayanan setiap pasien terpantau hingga tuntas dan stok terpotong secara otomatis |
| US-18 | Apoteker | Mencatat penerimaan obat masuk serta memperoleh peringatan saat stok menipis atau mendekati kedaluwarsa | Persediaan obat tertelusur dan kekosongan obat dapat dicegah sebelum terjadi |

## 3.3 Deskripsi Aktivitas

Setiap aktivitas pada tabel berikut dipetakan **satu lawan satu** dengan satu simpul (*node*) pada activity diagram di Subbab 3.4. Kolom **Pelaku** menunjukkan lajur tempat simpul tersebut digambarkan, dan kolom **Diagram** menunjukkan gambar yang memuatnya. Simpul keputusan (*decision node*) tidak dihitung sebagai aktivitas karena tidak menghasilkan perubahan keadaan.

**Alur pelayanan rawat jalan (Gambar 1)**

| ID | Aktivitas | Pelaku | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- | :--- |
| A01 | Mencari Data Pasien | Petugas Administrasi | Petugas memasukkan NIK, nama, atau nomor rekam medis, lalu sistem menampilkan data pasien yang cocok | US-01 |
| A02 | Mendaftarkan atau Memperbarui Data Pasien | Petugas Administrasi | Petugas mengisi data diri pasien baru sehingga sistem menerbitkan nomor rekam medis, atau menyunting data pasien lama yang berubah | US-02 |
| A03 | Membuat Kunjungan dan Nomor Antrean | Petugas Administrasi | Petugas mencatat kedatangan pasien beserta poli tujuan, lalu sistem menerbitkan nomor antrean | US-03 |
| A04 | Menampilkan Antrean Skrining | Sistem SEHATI | Sistem menampilkan daftar pasien yang menunggu pemeriksaan tanda vital, terurut menurut nomor antrean | US-08 |
| A05 | Mencatat Keluhan Awal dan Tanda Vital | Perawat | Perawat memasukkan keluhan awal serta hasil pengukuran tekanan darah, berat badan, tinggi badan, suhu, nadi, dan gula darah sewaktu bila tersedia | US-09 |
| A06 | Mengevaluasi Ambang Risiko | Sistem SEHATI | Sistem membandingkan nilai yang baru dimasukkan terhadap ambang klinis dan terhadap riwayat pengukuran pasien pada kunjungan sebelumnya | US-09 |
| A07 | Menandai Kunjungan Berisiko | Sistem SEHATI | Sistem memberi penanda risiko penyakit tidak menular pada kunjungan yang nilainya berada di luar ambang normal | US-09 |
| A08 | Menampilkan Riwayat dan Tren Tanda Vital | Sistem SEHATI | Sistem menyajikan hasil pengukuran pasien dari kunjungan-kunjungan sebelumnya dalam bentuk grafik | US-10 |
| A09 | Meninjau Rekam Medis dan Penanda Risiko | Dokter | Dokter membaca data pasien, diagnosis lampau, riwayat obat, dan penanda risiko hasil skrining sebelum memulai pemeriksaan | US-12 |
| A10 | Melakukan Pemeriksaan dan Mencatat Diagnosis | Dokter | Dokter memeriksa pasien lalu mencatat anamnesis, diagnosis, dan tindakan pada kunjungan berjalan | US-13 |
| A11 | Menyusun Resep Elektronik | Dokter | Dokter memilih obat, dosis, dan aturan pakai dari data master obat puskesmas | US-14 |
| A12 | Memvalidasi Ketersediaan Stok Obat | Sistem SEHATI | Sistem memeriksa kecukupan stok setiap obat yang diresepkan dan memberi tahu dokter bila tidak mencukupi | US-14 |
| A13 | Menjadwalkan Kunjungan Ulang | Dokter | Dokter menetapkan rencana kunjungan ulang bagi pasien yang perlu dipantau | US-15 |
| A14 | Menampilkan Antrean Resep | Sistem SEHATI | Sistem meneruskan resep yang telah tervalidasi ke antrean pelayanan farmasi dan menampilkannya kepada apoteker | US-16 |
| A15 | Menyerahkan Obat dan Menjelaskan Aturan Pakai | Apoteker | Apoteker menyiapkan obat sesuai resep, menyerahkannya kepada pasien, lalu menandai resep sebagai selesai dilayani | US-17 |
| A16 | Menutup Kunjungan dan Mengantrekan Sinkronisasi | Sistem SEHATI | Sistem memotong stok obat sesuai jumlah yang diserahkan, menutup kunjungan, lalu menyusunnya menjadi bundel HL7 FHIR pada antrean sinkronisasi | US-06, US-17 |

**Alur kegiatan pendukung berkala (Gambar 2)**

| ID | Aktivitas | Pelaku | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- | :--- |
| A17 | Mengelola Akun Pengguna dan Data Master | Petugas Administrasi | Petugas menambah atau menonaktifkan akun beserta hak aksesnya, serta memperbarui data obat, poli, dan ambang nilai risiko | US-05 |
| A18 | Mencatat Penerimaan Obat Masuk | Apoteker | Apoteker memasukkan data obat yang diterima puskesmas beserta nomor bets dan tanggal kedaluwarsa | US-18 |
| A19 | Memperbarui Stok dan Memeriksa Ambang Persediaan | Sistem SEHATI | Sistem menambahkan jumlah obat yang diterima ke persediaan lalu membandingkannya terhadap ambang minimum dan tanggal kedaluwarsa | US-18 |
| A20 | Memunculkan Peringatan Persediaan | Sistem SEHATI | Sistem menampilkan peringatan bagi obat yang jumlahnya di bawah ambang minimum atau mendekati kedaluwarsa | US-18 |
| A21 | Menyusun Daftar Pantau Pasien Berisiko | Sistem SEHATI | Sistem mengumpulkan pasien yang pernah ditandai berisiko maupun yang dijadwalkan kunjungan ulang menjadi satu daftar | US-11 |
| A22 | Menindaklanjuti Pasien dan Mencatat Hasilnya | Perawat | Perawat menghubungi pasien pada daftar pantau lalu mencatat hasil upaya tersebut beserta status kedatangannya | US-11 |
| A23 | Menyusun dan Mengekspor Laporan Periodik | Petugas Administrasi | Petugas memilih periode, sistem menghitung rekapitulasi kunjungan dan diagnosis terbanyak, lalu menghasilkan berkas laporan yang dapat dicetak atau diunggah | US-04 |
| A24 | Menjalankan Sinkronisasi Data | Petugas Administrasi | Petugas memerintahkan pengiriman antrean sinkronisasi yang telah terkumpul | US-06 |
| A25 | Mengirim Bundel Data ke SATUSEHAT | Sistem SEHATI | Sistem mengirim bundel HL7 FHIR ke platform SATUSEHAT lalu menandai data yang berhasil terkirim | US-06 |
| A26 | Menyimpan Bundel sebagai Berkas Ekspor | Sistem SEHATI | Sistem menyimpan antrean sinkronisasi sebagai berkas apabila koneksi tidak tersedia, agar dapat diunggah dari lokasi lain yang berjaringan | US-06 |
| A27 | Menjalankan Pencadangan Basis Data | Sistem SEHATI | Sistem membuat salinan basis data menurut jadwal, menyimpannya pada media terpisah, dan memperingatkan pengguna bila pencadangan gagal | US-07 |

Seluruh 27 aktivitas di atas muncul sebagai simpul pada kedua diagram di Subbab 3.4, dan sebaliknya tidak ada simpul aktivitas pada diagram yang tidak tercantum pada tabel ini.

## 3.4 Model Proses Bisnis

Proses bisnis dimodelkan dalam dua *swimlane activity diagram*. Lajur pada kedua diagram hanya berisi **aktor yang didefinisikan pada Subbab 3.1** ditambah lajur **Sistem SEHATI**. Tidak ada lajur untuk pihak eksternal: SATUSEHAT berada di luar batas perangkat lunak ini, sehingga pengiriman data kepadanya digambarkan sebagai aktivitas milik Sistem SEHATI, bukan sebagai lajur tersendiri.

Setiap kotak aktivitas pada kedua diagram diberi label ID dan berpasangan **satu lawan satu** dengan satu baris pada tabel Deskripsi Aktivitas di Subbab 3.3. Bentuk belah ketupat merupakan simpul keputusan yang tidak memiliki ID, karena hanya menentukan percabangan dan tidak mengubah keadaan sistem.

### Gambar 1: alur pelayanan rawat jalan

Diagram pertama memodelkan perjalanan satu pasien sejak tiba di loket hingga menerima obat, memuat aktivitas A01 sampai A16. Seluruh aktor pengguna muncul di sini.

Ada tiga titik pada alur ini yang menunjukkan peningkatan dibandingkan proses manual maupun hibrida pada Subbab 1.2:

1. **Pencarian data pasien dilakukan sistem** (A01), menggantikan penelusuran berkas fisik di rak maupun perangkaian riwayat dari dua media (menutup kesenjangan G-01 dan G-02).
2. **Evaluasi ambang risiko dan penandaan kunjungan berjalan otomatis** (A06 dan A07), sehingga pengenalan pasien berisiko tidak lagi bergantung pada kejelian petugas. Inilah titik yang membedakan SEHATI dari sistem informasi puskesmas lain, karena G-03 dan G-04 tetap terbuka bahkan pada fasilitas yang sudah terdigitalisasi.
3. **Validasi stok obat terjadi saat resep disusun** (A12), bukan setelah pasien mengantre di apotek, sehingga penggantian obat dilakukan tanpa pasien perlu bolak-balik (menutup kesenjangan G-05).

<br>

<p align="center">
<img alt="Activity Diagram Alur Pelayanan Rawat Jalan Puskesmas dengan SEHATI" src="./assets/diagram/diagram-act-1.svg" width="90%">
</p>
<p align="center">
<i>Gambar 1. Activity Diagram alur pelayanan rawat jalan puskesmas dengan SEHATI</i>
</p>

<br>

**Penjelasan alur.** Petugas Administrasi mencari data pasien berdasarkan NIK atau kartu berobat (A01). Bila pasien belum terdaftar, petugas mendaftarkannya atau memperbarui datanya (A02), lalu membuat kunjungan beserta nomor antrean (A03). Sistem menampilkan antrean skrining (A04), dan Perawat mencatat keluhan awal beserta tanda vital (A05). Sistem mengevaluasi nilai tersebut terhadap ambang klinis dan riwayat pasien (A06); bila terlampaui, kunjungan ditandai berisiko (A07). Sistem lalu menyajikan riwayat dan tren tanda vital (A08).

Dokter meninjau rekam medis beserta penanda risiko (A09), melakukan pemeriksaan dan mencatat diagnosis (A10), kemudian menyusun resep elektronik (A11). Sistem memvalidasi ketersediaan stok (A12); bila tidak mencukupi, dokter kembali menyusun resep dengan obat pengganti. Dokter menjadwalkan kunjungan ulang bagi pasien yang perlu dipantau (A13), sistem menampilkan antrean resep kepada Apoteker (A14), Apoteker menyerahkan obat beserta penjelasan aturan pakai (A15), dan sistem menutup kunjungan sekaligus mengantrekannya untuk sinkronisasi (A16).

### Gambar 2: alur kegiatan pendukung berkala

Diagram kedua memodelkan rangkaian kegiatan yang dijalankan di luar jam pelayanan pasien, umumnya pada akhir hari atau akhir periode pelaporan. Diagram ini memuat aktivitas A17 sampai A27, dan di sinilah mekanisme *store-and-forward* yang menjadi nilai unik kedua SEHATI bekerja.

Titik penting pada alur ini adalah percabangan ketersediaan koneksi. Ketika internet tersedia, bundel data berstandar HL7 FHIR dikirim langsung ke SATUSEHAT (A25). Ketika tidak tersedia, bundel yang sama disimpan sebagai berkas ekspor (A26) sehingga dapat diunggah dari lokasi lain yang berjaringan. Kedua jalur bermuara pada hasil yang sama, dan tidak satu pun di antaranya menghentikan pelayanan di puskesmas.

<br>

<p align="center">
<img alt="Activity Diagram Alur Kegiatan Pendukung Berkala" src="./assets/diagram/diagram-act-2.svg" width="80%">
</p>
<p align="center">
<i>Gambar 2. Activity Diagram alur kegiatan pendukung berkala</i>
</p>

<br>

**Penjelasan alur.** Petugas Administrasi memperbarui akun pengguna dan data master (A17). Apoteker mencatat obat yang diterima puskesmas (A18), lalu sistem memperbarui stok sekaligus memeriksa ambang persediaan (A19); bila stok menipis atau mendekati kedaluwarsa, sistem memunculkan peringatan (A20). Sistem menyusun daftar pantau pasien berisiko (A21), dan Perawat menindaklanjuti pasien pada daftar tersebut serta mencatat hasilnya (A22). Petugas Administrasi menyusun dan mengekspor laporan periodik (A23), kemudian menjalankan sinkronisasi data (A24). Sistem mengirim bundel ke SATUSEHAT bila koneksi tersedia (A25) atau menyimpannya sebagai berkas ekspor bila tidak (A26), lalu menutup rangkaian dengan pencadangan basis data terjadwal (A27).

<br>

# Referensi

**Regulasi**
1. Peraturan Menteri Kesehatan Republik Indonesia Nomor 24 Tahun 2022 tentang Rekam Medis. https://peraturan.bpk.go.id/Details/245544/permenkes-no-24-tahun-2022
2. Undang-Undang Republik Indonesia Nomor 27 Tahun 2022 tentang Pelindungan Data Pribadi.
3. Surat Edaran Kementerian Kesehatan Republik Indonesia tanggal 15 April 2025 mengenai penerapan Rekam Medis Elektronik dan pengiriman data ke platform SATUSEHAT.

**Data dan statistik**

4. Badan Kebijakan Pembangunan Kesehatan, Kementerian Kesehatan Republik Indonesia. (2024). *Hasil Utama Survei Kesehatan Indonesia (SKI) 2023*. https://www.badankebijakan.kemkes.go.id/daftar-frequently-asked-question-seputar-hasil-utama-ski-2023/hasil-utama-ski-2023/
5. Kementerian Kesehatan Republik Indonesia. (2025). *Profil Kesehatan Indonesia 2024*. Jakarta: Kementerian Kesehatan RI. https://kemkes.go.id/id/profil-kesehatan-indonesia-2024
6. Kementerian Kesehatan Republik Indonesia. (2019). *Laporan Nasional Riskesdas 2018*. Jakarta: Lembaga Penerbit Badan Litbang Kesehatan. Digunakan sebagai pembanding tren terhadap SKI 2023. https://repository.badankebijakan.kemkes.go.id/id/eprint/3514/
7. United Nations. *Sustainable Development Goal 3: Ensure healthy lives and promote well-being for all at all ages*. https://sdgs.un.org/goals/goal3

8. Kementerian Kesehatan Republik Indonesia. *Kemenkes Resmi Luncurkan RME Terintegrasi SATUSEHAT*, peresmian SATUSEHAT RME pada 1 September 2026. https://infopublik.id/kategori/nasional-sosial-budaya/796623/kemenkes-resmi-luncurkan-rme-terintegrasi-satusehat
9. Badan Kebijakan Pembangunan Kesehatan, Kementerian Kesehatan Republik Indonesia. *Wajib Integrasi SATUSEHAT, Kemenkes Desak Percepatan RME di Fasyankes*. Memuat data 34.463 fasyankes terintegrasi per 27 Oktober 2025 serta kendala infrastruktur dan kesiapan sumber daya manusia. https://www.badankebijakan.kemkes.go.id/wajib-integrasi-satu-sehat-kemenkes-desak-percepatan-rme-di-fasyankes/

**Solusi dan standar teknis pembanding**

10. Kementerian Kesehatan Republik Indonesia. *SATUSEHAT Platform: Dokumentasi HL7 FHIR*. https://satusehat.kemkes.go.id/platform/docs/id/fhir/
11. Direktorat Jenderal Pelayanan Kesehatan, Kementerian Kesehatan Republik Indonesia. *ASRI (Aplikasi Sistem RME Indonesia)*, aplikasi rekam medis elektronik gratis bagi Tempat Praktik Mandiri Dokter dan Dokter Gigi. https://yankes.kemkes.go.id/asri
12. Kementerian Kesehatan Republik Indonesia. *Penyedia Sistem RME*, daftar sistem rekam medis elektronik termasuk SIKDA Generik untuk puskesmas. https://satusehat.kemkes.go.id/platform/system-rme-list
13. *Analisis Implementasi Sistem Informasi Kesehatan Daerah (SIKDA) Generik Guna Menunjang Efektivitas Rekam Medis Elektronik di UPTD Puskesmas Campaka*. J-REMI: Jurnal Rekam Medik dan Informasi Kesehatan. https://publikasi.polije.ac.id/j-remi/article/view/3956

**Perkakas**

14. Diagram UML: https://www.drawio.com/, https://staruml.io/
