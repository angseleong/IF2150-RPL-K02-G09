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

Sayangnya, kapasitas administratif puskesmas belum sebanding dengan beban tugas tersebut. Digitalisasi pencatatan juga belum merata. Meskipun rekam medis elektronik sudah diwajibkan sejak akhir 2023, Kementerian Kesehatan masih perlu menerbitkan surat edaran penegasan pada April 2025, sebuah indikasi bahwa kepatuhan di lapangan belum tuntas. Di banyak puskesmas, terutama di kabupaten dan daerah kepulauan yang infrastrukturnya terbatas, **pencatatan berbasis kertas** masih dipakai untuk rekam medis, pengelolaan antrean, resep, dan stok obat. Berkas rekam medis disimpan dalam rak *family folder*, ditulis tangan oleh dokter, lalu diketik ulang secara manual ketika laporan bulanan harus dikirim ke dinas kesehatan. Model kerja seperti ini menimbulkan tiga persoalan yang saling berkait: waktu tunggu pasien menjadi panjang, riwayat kesehatan pasien terputus antar-kunjungan, dan data agregat yang seharusnya bisa dipakai untuk deteksi dini justru tidak pernah terbentuk.

Dampak paling nyata dari terputusnya riwayat kesehatan pasien terlihat pada penanganan **Penyakit Tidak Menular (PTM)**. Survei Kesehatan Indonesia (SKI) 2023 mencatat prevalensi hipertensi pada penduduk berusia 18 tahun ke atas sebesar **30,8%** berdasarkan hasil pengukuran tekanan darah, sementara prevalensi berdasarkan diagnosis dokter hanya **8,6%**. Selisih lebih dari 22 poin persen ini berarti sebagian besar penderita hipertensi di Indonesia tidak pernah tahu bahwa dirinya sakit, sehingga tidak pernah menjalani pengobatan. Pola yang sama terjadi pada diabetes melitus: prevalensi berdasarkan pemeriksaan gula darah pada penduduk usia 15 tahun ke atas mencapai 11,7%, sedangkan yang terdiagnosis dokter hanya 2,2%.

Yang perlu digarisbawahi, celah tersebut nyaris tidak bergerak dalam lima tahun. Riset Kesehatan Dasar (Riskesdas) 2018 mencatat selisih sekitar 25 poin persen (34,1% berdasarkan pengukuran berbanding 8,8% berdasarkan diagnosis), dan pada SKI 2023 selisihnya masih sekitar 22 poin persen. Artinya penurunan prevalensi hipertensi tidak diiringi perbaikan berarti pada kemampuan sistem kesehatan mendeteksi penderitanya. Beban itu berlanjut sampai tahap pengobatan: di antara penduduk usia 15 tahun ke atas yang sudah terdiagnosis hipertensi, hanya 46,7% yang meminum obat secara teratur, sementara 36,4% meminum obat tidak teratur dan 16,9% tidak meminum obat sama sekali. Sebagian besar penderita ini sebenarnya *pernah* datang ke puskesmas dan *pernah* diukur tekanan darahnya, tetapi karena hasil pengukuran hanya dicatat di lembar kertas kunjungan hari itu, tidak ada mekanisme apa pun yang menghubungkan angka tinggi hari ini dengan angka tinggi tiga bulan lalu.

Persoalan serupa terjadi pada kesehatan ibu. Hasil Long Form Sensus Penduduk 2020 (BPS) menunjukkan Angka Kematian Ibu (AKI) Indonesia sebesar **189 per 100.000 kelahiran hidup**, masih jauh dari target SDGs 3.1 yaitu di bawah 70 per 100.000 kelahiran hidup pada 2030. Pemantauan kehamilan berisiko sangat bergantung pada kelengkapan dan kesinambungan catatan kunjungan, sesuatu yang sulit dijamin dengan berkas kertas yang mudah terselip atau hilang.

### Keterkaitan dengan SDGs

Kelompok kami memilih **SDG 3: *Good Health and Well-being* (Kehidupan Sehat dan Sejahtera)** sebagai landasan solusi. Secara spesifik, perangkat lunak yang kami usulkan menyasar tiga target berikut:

| Target SDG 3 | Rumusan Target | Keterkaitan dengan Solusi |
| :--- | :--- | :--- |
| **3.4** | Mengurangi sepertiga angka kematian dini akibat penyakit tidak menular melalui pencegahan dan pengobatan | Sistem menyimpan riwayat tanda vital lintas kunjungan dan secara otomatis menandai pasien yang hasil pengukurannya berada di luar ambang normal, sehingga deteksi dini tidak lagi bergantung pada ingatan petugas |
| **3.8** | Mencapai cakupan kesehatan semesta (*Universal Health Coverage*), termasuk akses pelayanan kesehatan dasar yang bermutu | Sistem memangkas waktu administrasi di FKTP sehingga waktu tenaga kesehatan dapat dialihkan untuk pelayanan, dan mutu pencatatan menjadi seragam |
| **3.1** | Mengurangi Angka Kematian Ibu hingga di bawah 70 per 100.000 kelahiran hidup | Sistem menandai kunjungan pemeriksaan kehamilan, mengevaluasi tanda bahaya seperti indikasi preeklampsia dari tekanan darah, serta menyusun daftar ibu hamil yang kunjungan pemeriksaannya belum lengkap agar dapat dikejar sebelum persalinan |

### Urgensi

Masalah ini sangat mendesak untuk segera diselesaikan, terutama karena adanya tuntutan regulasi. Peraturan Menteri Kesehatan Nomor 24 Tahun 2022, yang hingga saat ini masih berlaku, mewajibkan **seluruh** fasilitas pelayanan kesehatan (termasuk puskesmas, klinik, dan praktik mandiri) untuk menyelenggarakan Rekam Medis Elektronik (RME) selambat-lambatnya **31 Desember 2023**. Tenggat tersebut sudah terlewat lebih dari dua tahun, dan fasilitas yang belum siap berisiko mendapat sanksi administratif hingga rekomendasi pencabutan status akreditasi. Kementerian Kesehatan bahkan mempertegas kewajiban ini melalui surat edaran tertanggal 15 April 2025 yang menuntut penerapan RME secara penuh beserta pengiriman datanya ke platform SATUSEHAT.

Di samping masalah regulasi, penundaan digitalisasi juga memperburuk celah deteksi dini Penyakit Tidak Menular (PTM). Dengan prevalensi hipertensi terukur sebesar 30,8% sementara yang terdiagnosis dokter hanya 8,6%, setiap data pasien yang tidak dicatat dengan baik berarti hilangnya kesempatan deteksi dini. Akibatnya, komplikasi penyakit seperti stroke atau gagal ginjal berisiko meningkat dan memakan biaya penanganan yang jauh lebih mahal. 

Meskipun kebutuhannya sangat mendesak, solusi yang tersedia saat ini belum sepenuhnya ramah untuk puskesmas kecil. Sebagian besar Sistem Informasi Manajemen Puskesmas (SIMPUS) komersial membebankan biaya langganan dan sangat bergantung pada internet yang stabil. Hal ini tentu menyulitkan puskesmas di daerah yang infrastrukturnya masih terbatas.

## 1.2 Analisis Kondisi Saat Ini

### Alur proses yang berjalan saat ini

Berdasarkan studi literatur dan laporan praktik pelayanan pada puskesmas non rawat inap yang masih menggunakan pencatatan manual, alur pelayanan rawat jalan berjalan sebagai berikut.

1. **Pendaftaran.** Pasien datang, mengantre di loket, lalu menyebutkan nama atau menyerahkan kartu berobat. Petugas mencari berkas rekam medis pasien secara manual di rak penyimpanan. Untuk pasien baru, petugas menuliskan data diri pada formulir kertas dan membuatkan berkas baru. Nomor antrean poli diberikan secara manual.
2. **Skrining awal.** Perawat memanggil pasien, mengukur tanda vital (tekanan darah, berat badan, tinggi badan, suhu), lalu menuliskan hasilnya pada lembar kunjungan hari itu.
3. **Pemeriksaan.** Dokter membaca berkas, melakukan pemeriksaan, lalu menuliskan anamnesis, diagnosis, dan tindakan dengan tulisan tangan. Resep ditulis pada lembar terpisah dan diserahkan kepada pasien.
4. **Farmasi.** Pasien membawa resep ke apotek puskesmas. Apoteker membaca resep, mengecek ketersediaan obat di lemari secara fisik, menyiapkan obat, lalu mencatat pengeluaran obat di buku stok.
5. **Pelaporan.** Pada akhir bulan, petugas merekap seluruh lembar kunjungan secara manual, lalu mengetik ulang angkanya ke dalam format laporan yang diminta dinas kesehatan.

### Kesenjangan (*gap*) yang teridentifikasi

Dari alur di atas, kami mengidentifikasi tujuh kesenjangan yang akan menjadi sasaran perangkat lunak kami.

| Kode | Kesenjangan | Penjelasan | Dampak |
| :--- | :--- | :--- | :--- |
| **G-01** | Pencarian berkas rekam medis lambat | Berkas dicari manual di rak berdasarkan nomor atau nama; kesalahan penempatan membuat berkas sulit ditemukan | Waktu tunggu pasien bertambah, antrean menumpuk di jam sibuk |
| **G-02** | Berkas hilang, rusak, atau tidak terbaca | Kertas rentan terhadap kelembapan dan kehilangan; tulisan tangan dokter sering sulit dibaca oleh apoteker | Risiko kesalahan pemberian obat dan riwayat pasien yang tidak dapat direkonstruksi |
| **G-03** | Riwayat kesehatan tidak berkesinambungan | Setiap kunjungan dicatat sebagai lembar terpisah tanpa mekanisme membandingkan antar-waktu | Tren kondisi pasien (misal tekanan darah yang terus naik) tidak pernah terlihat |
| **G-04** | Tidak ada mekanisme deteksi dini | Penandaan pasien berisiko sepenuhnya bergantung pada kejelian dan ingatan petugas | Pasien berisiko PTM lolos dari pemantauan, sejalan dengan temuan SKI 2023 |
| **G-05** | Stok obat tidak terpantau secara *real-time* | Pengecekan stok dilakukan secara fisik dan pencatatan dilakukan menyusul | Terjadi kekosongan obat mendadak dan obat kedaluwarsa yang tidak terdeteksi |
| **G-06** | Rekapitulasi laporan manual dan lambat | Data harus dibaca ulang dari ratusan lembar kertas, lalu diketik ulang | Laporan terlambat, rawan salah hitung, dan menyita waktu tenaga kesehatan |
| **G-07** | Solusi digital eksisting kurang sesuai konteks | SIMPUS komersial umumnya berbasis awan, berlangganan, dan mengasumsikan internet stabil | Puskesmas kecil dan daerah dengan internet terbatas tidak dapat mengadopsinya |

### Perbandingan dengan solusi yang sudah ada

| Solusi | Kelebihan | Keterbatasan terhadap konteks masalah |
| :--- | :--- | :--- |
| **Pencatatan manual (kertas)** | Tidak butuh perangkat, tidak butuh pelatihan, tidak bergantung listrik | Seluruh kesenjangan G-01 sampai G-06 berasal dari model ini |
| **SIMPUS komersial berbasis awan** | Fitur lengkap, terintegrasi dengan sistem nasional, ada dukungan teknis | Berbayar per bulan, membutuhkan internet stabil, layanan terhenti saat koneksi putus (G-07) |
| **ASRI (RME gratis Kementerian Kesehatan)** | Gratis, resmi dari Kemenkes, dan sudah terhubung ke SATUSEHAT sejak awal | Berbasis awan sehingga tetap bergantung pada internet; sasaran utamanya praktik mandiri dan klinik kecil, bukan alur pelayanan puskesmas yang melibatkan banyak peran; masa penggunaan gratisnya berakhir 31 Desember 2026 (G-07) |
| **Spreadsheet mandiri (Excel)** | Gratis, sudah dikuasai sebagian petugas | Tidak ada validasi data, tidak ada relasi antar-tabel, tidak ada kendali akses, rawan tertimpa; tidak menyelesaikan G-03 dan G-04 |
| **SATUSEHAT (platform Kemenkes)** | Standar interoperabilitas nasional, memungkinkan pertukaran data antar-fasilitas | Merupakan platform pertukaran data, bukan aplikasi operasional harian; fasilitas tetap membutuhkan sistem sendiri untuk mencatat |

Perhatikan bahwa seluruh alternatif digital di atas, termasuk yang disediakan gratis oleh pemerintah, sama-sama menempatkan koneksi internet sebagai syarat beroperasi. Padahal justru fasilitas dengan jaringan paling buruklah yang paling membutuhkan pertolongan administratif. Kesimpulannya, celah yang belum terisi adalah **sistem operasional harian yang ringan, tetap berjalan penuh tanpa internet, mampu melakukan deteksi dini secara bawaan, namun tetap memenuhi kewajiban pelaporan elektronik**. Celah inilah yang akan diisi oleh perangkat lunak kami.

---

# BAB 2: Analisis Solusi

## 2.1 Deskripsi Perangkat Lunak

**SEHATI (Sistem Elektronik Pelayanan Kesehatan Terintegrasi)** adalah aplikasi *desktop* pengelolaan pelayanan rawat jalan untuk puskesmas dan klinik pratama. SEHATI menyatukan seluruh rantai pelayanan (mulai dari pendaftaran pasien, skrining tanda vital, pemeriksaan dokter, peresepan elektronik, penyerahan obat, hingga rekapitulasi laporan) ke dalam satu aplikasi yang dipakai bersama oleh seluruh petugas di satu fasilitas kesehatan.

### Gambaran dari sudut pandang pengguna

Aplikasi ini dirancang untuk mempermudah alur kerja seluruh petugas fasilitas kesehatan. **Petugas pendaftaran** tidak perlu lagi membongkar rak berkas secara manual, karena pencarian pasien bisa dilakukan secara instan lewat NIK atau nomor rekam medis untuk mencetak nomor antrean. Selanjutnya, **perawat** tinggal menginput data skrining tanda vital ke dalam formulir digital. Sistem akan langsung membandingkan nilai tersebut dengan batas normal dan menampilkan grafik riwayat kunjungan pasien.

Bagi **dokter**, seluruh riwayat kesehatan (mulai dari diagnosis lampau, obat yang dikonsumsi, hingga tren tekanan darah) akan langsung tersaji dalam satu layar sebelum pemeriksaan. Jika ada indikasi risiko penyakit tertentu, sistem akan langsung memberikan peringatan otomatis. Setelah diperiksa, dokter bisa langsung menyusun resep secara digital. Resep ini kemudian otomatis terkirim ke **apoteker** dalam bentuk teks yang jelas dan stoknya juga sudah divalidasi oleh sistem.

Pada akhirnya, pekerjaan **kepala puskesmas** juga menjadi jauh lebih ringan. Rekapitulasi laporan kunjungan maupun daftar pasien berisiko sudah dibuatkan secara otomatis oleh sistem tanpa perlu mengumpulkan dan mengetik ulang data dari kertas.

### Target platform dan alasan pemilihannya

SEHATI dikembangkan sebagai **aplikasi desktop** yang dipasang pada komputer di lingkungan puskesmas. Pemilihan ini didasarkan pada empat pertimbangan.

1. **Kemandirian terhadap koneksi internet.** Kesenjangan G-07 menunjukkan bahwa ketergantungan pada internet adalah penghalang utama adopsi di daerah. Aplikasi desktop dengan basis data lokal tetap berfungsi penuh saat jaringan terputus, suatu kondisi yang tidak boleh menghentikan pelayanan kesehatan.
2. **Kesesuaian dengan perangkat yang sudah tersedia.** Puskesmas umumnya sudah memiliki komputer atau laptop di loket pendaftaran dan ruang periksa. Aplikasi desktop yang ringan dapat memanfaatkan perangkat tersebut tanpa pengadaan baru.
3. **Kesesuaian dengan pola kerja.** Pendaftaran, pemeriksaan, dan farmasi dilakukan di meja kerja tetap dengan intensitas pengetikan yang tinggi. Antarmuka desktop dengan papan ketik penuh dan pintasan papan ketik lebih efisien untuk pola kerja seperti ini dibandingkan antarmuka sentuh.
4. **Kendali atas data sensitif.** Data rekam medis tersimpan di dalam lingkungan fasilitas itu sendiri, sehingga kendali dan tanggung jawab penyimpanan tetap berada di tangan fasilitas, sejalan dengan kewajiban menjaga kerahasiaan rekam medis.

### Nilai unik dan inovasi inti

Pembeda utama SEHATI dari SIMPUS pada umumnya terletak pada tiga hal berikut.

**1. Pemantauan longitudinal dengan daftar pantau yang tertutup siklusnya (inovasi inti).**
Pembeda SEHATI bukan pada pemeriksaan ambang batas semata, melainkan pada apa yang terjadi sesudah ambang itu terlampaui. Setiap hasil pengukuran disimpan sebagai bagian dari satu rangkaian riwayat pasien, bukan sebagai catatan lepas per kunjungan. Sistem membandingkan nilai terbaru terhadap ambang klinis sekaligus terhadap pola pengukuran pasien pada kunjungan-kunjungan sebelumnya, sehingga tekanan darah tinggi yang berulang dapat dibedakan dari lonjakan sesaat. Pasien yang polanya mengarah ke risiko masuk ke dalam **daftar pantau**, dan daftar itu bukan sekadar tampilan: petugas mencatat upaya tindak lanjut serta status kedatangan pasien pada kunjungan ulang, sehingga pemantauan benar-benar tertutup siklusnya. Mekanisme yang sama dipakai untuk mengenali tanda bahaya pada ibu hamil. Dengan cara inilah celah antara pasien yang "pernah diukur" dan yang benar-benar "terdiagnosis lalu tertangani" (celah 30,8% versus 8,6% pada data SKI 2023) ditangani secara sistemik.

**2. Arsitektur *offline-first* dengan sinkronisasi tunda ke SATUSEHAT.**
Solusi yang tersedia saat ini memaksa fasilitas memilih di antara dua hal yang sama-sama buruk: sistem berbasis awan yang patuh regulasi tetapi berhenti melayani ketika jaringan putus, atau pencatatan lokal yang selalu jalan tetapi tidak pernah memenuhi kewajiban pelaporan. SEHATI menolak pilihan tersebut. Seluruh fungsi pelayanan berjalan penuh di atas basis data lokal tanpa memerlukan internet sama sekali. Setiap kunjungan yang ditutup otomatis disusun menjadi data berstandar **HL7 FHIR** lalu dimasukkan ke dalam **antrean sinkronisasi**. Ketika koneksi tersedia, kapan pun itu termasuk di luar jam pelayanan, antrean dikirim ke platform SATUSEHAT dan ditandai selesai. Bila koneksi tidak kunjung tersedia, antrean dapat diekspor sebagai berkas untuk diunggah dari lokasi lain yang berjaringan. Hasilnya, pelayanan tidak pernah berhenti karena internet, sementara kewajiban PMK 24/2022 tetap terpenuhi.

**3. Alur kerja terpadu dari pendaftaran hingga farmasi.**
Data mengalir dalam satu aplikasi tanpa penyalinan ulang antar-tahap. Resep yang disusun dokter langsung tervalidasi terhadap stok apotek pada saat penyusunan, sehingga penggantian obat karena stok kosong terjadi di ruang periksa, bukan setelah pasien mengantre di apotek.

### Ketahanan data

Memindahkan rekam medis dari kertas ke basis data lokal memunculkan risiko baru yang perlu dijawab secara jujur. Arsip kertas hilang satu per satu, sedangkan kerusakan satu media penyimpanan berpotensi melenyapkan seluruh rekam medis sekaligus. Karena itu pencadangan tidak diserahkan pada kedisiplinan petugas. SEHATI menjalankan pencadangan basis data secara otomatis menurut jadwal ke media penyimpanan terpisah, menampilkan status pencadangan terakhir pada layar utama, serta memperingatkan pengguna bila pencadangan gagal atau tertunda. Antrean sinkronisasi ke SATUSEHAT turut berperan sebagai salinan kedua yang berada di luar fasilitas.

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
| BR-01 | Sistem harus tunduk pada Peraturan Menteri Kesehatan Nomor 24 Tahun 2022 tentang Rekam Medis, khususnya kewajiban penyelenggaraan rekam medis elektronik |
| BR-02 | Data pasien merupakan data pribadi bersifat spesifik menurut Undang-Undang Nomor 27 Tahun 2022 tentang Pelindungan Data Pribadi, sehingga akses harus dibatasi berdasarkan peran dan setiap perubahan data harus terekam dalam jejak audit |
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
| BL-06 | Penanda risiko dibatasi pada dua kelompok: (a) penyakit tidak menular yang parameternya terukur rutin di puskesmas, yaitu hipertensi, obesitas, dan indikasi diabetes berdasarkan gula darah sewaktu; serta (b) tanda bahaya kehamilan yang dapat dikenali dari tekanan darah dan hasil pengukuran rutin. Sistem tidak melakukan penilaian risiko di luar parameter tersebut |
| BL-07 | Pasien tidak berinteraksi langsung dengan sistem; seluruh masukan data dilakukan oleh petugas fasilitas |

### Prioritas pengembangan

Lingkup di atas melampaui apa yang dapat diselesaikan sekaligus oleh lima orang dalam satu semester (BS-01). Karena itu pengembangan dibagi menjadi dua tahap.

**Tahap inti**, yang wajib selesai, mencakup modul pendaftaran dan data pasien, skrining awal, pemeriksaan, farmasi, serta pencadangan otomatis. Rangkaian ini sudah membentuk satu siklus pelayanan rawat jalan yang utuh dan dapat diuji secara menyeluruh.

**Tahap lanjutan**, yang dikerjakan setelah tahap inti stabil, mencakup pelaporan dan administrasi, pemantauan kesehatan ibu, tindak lanjut daftar pantau, serta sinkronisasi ke SATUSEHAT.

Pembagian ini memastikan bahwa apabila waktu tidak mencukupi, yang tertunda adalah fitur pelengkap, bukan alur pelayanan inti.

---

# BAB 3: Spesifikasi Kebutuhan dan Proses Bisnis

## 3.1 Identifikasi Aktor

| Aktor | Deskripsi |
| :--- | :--- |
| **Petugas Pendaftaran** | Pengguna yang bertugas di loket sebagai titik kontak pertama pasien. Bertanggung jawab mendaftarkan pasien baru, mencari data pasien lama, membuat data kunjungan, dan mengelola antrean poli. Karakteristiknya adalah bekerja di bawah tekanan antrean pada jam sibuk, sehingga mengutamakan kecepatan pencarian data dan minimnya jumlah langkah untuk menyelesaikan satu pendaftaran. |
| **Perawat** | Tenaga kesehatan yang melakukan skrining awal sebelum pasien diperiksa dokter. Bertanggung jawab mencatat keluhan awal dan hasil pengukuran tanda vital (tekanan darah, berat badan, tinggi badan, suhu, nadi). Karakteristiknya adalah menangani banyak pasien dalam waktu singkat dan membutuhkan formulir masukan yang ringkas serta memiliki validasi nilai agar tidak terjadi salah ketik angka. Peran ini juga mencakup bidan yang melayani pemeriksaan kehamilan. |
| **Dokter** | Tenaga medis yang melakukan pemeriksaan, menegakkan diagnosis, menentukan tindakan, dan meresepkan obat. Karakteristiknya adalah membutuhkan gambaran riwayat pasien yang utuh dalam waktu singkat, dan menuntut agar sistem tidak memperlambat proses pemeriksaan. Dokter merupakan pengguna dengan kewenangan tertinggi atas isi rekam medis pasien. |
| **Apoteker** | Tenaga kefarmasian yang menyiapkan dan menyerahkan obat kepada pasien serta mengelola persediaan obat puskesmas. Karakteristiknya adalah membutuhkan resep yang terbaca jelas dan informasi stok yang akurat, serta bertanggung jawab atas ketertelusuran pengeluaran obat. |
| **Kepala Puskesmas** | Pengguna yang bertanggung jawab atas kinerja fasilitas secara keseluruhan sekaligus berperan sebagai administrator sistem. Bertugas memantau laporan kunjungan, menindaklanjuti daftar pasien berisiko, serta mengelola akun pengguna dan data master (daftar obat, daftar poli, ambang risiko). Karakteristiknya adalah lebih banyak membaca ringkasan agregat dibandingkan data per pasien. |

> **Catatan.** Pasien merupakan pemangku kepentingan utama yang memperoleh manfaat dari sistem, namun bukan aktor karena tidak berinteraksi langsung dengan perangkat lunak (lihat batasan BL-07). Seluruh masukan data dilakukan oleh petugas fasilitas.

## 3.2 Kebutuhan Pengguna Awal

| ID | Aktor | Kebutuhan / Aktivitas | Tujuan / Nilai |
| :--- | :--- | :--- | :--- |
| US-01 | Petugas Pendaftaran | Mencari data pasien berdasarkan NIK, nama, atau nomor rekam medis | Berkas pasien ditemukan dalam hitungan detik tanpa perlu mencari di rak |
| US-02 | Petugas Pendaftaran | Mendaftarkan pasien baru beserta data diri dan data wali | Pasien baru langsung memiliki rekam medis elektronik sejak kunjungan pertama |
| US-03 | Petugas Pendaftaran | Membuat data kunjungan dan memilih poli tujuan | Setiap kedatangan pasien tercatat dan terhubung dengan riwayatnya |
| US-04 | Petugas Pendaftaran | Mencetak nomor antrean poli secara otomatis | Urutan pelayanan menjadi jelas dan tidak terjadi perselisihan antrean |
| US-05 | Petugas Pendaftaran | Memperbarui data diri pasien yang berubah | Data kontak dan alamat pasien tetap mutakhir untuk keperluan tindak lanjut |
| US-06 | Perawat | Melihat daftar antrean pasien yang menunggu skrining | Pemanggilan pasien berjalan tertib sesuai urutan |
| US-07 | Perawat | Mencatat keluhan awal dan hasil pengukuran tanda vital pasien, termasuk gula darah sewaktu bila pemeriksaannya tersedia | Hasil pengukuran tersimpan permanen dan langsung tersedia bagi dokter |
| US-08 | Perawat | Melihat grafik tren tanda vital pasien dari kunjungan sebelumnya | Perubahan kondisi pasien antar-waktu dapat dikenali sejak tahap skrining |
| US-09 | Perawat | Memperoleh peringatan otomatis saat nilai yang dimasukkan berada di luar ambang normal | Pasien berisiko tidak terlewat meskipun antrean sedang padat |
| US-10 | Dokter | Melihat rekam medis dan riwayat kunjungan pasien dalam satu tampilan | Keputusan klinis diambil berdasarkan gambaran kondisi pasien yang utuh |
| US-11 | Dokter | Melihat penanda risiko penyakit tidak menular pada pasien yang sedang diperiksa | Pasien berisiko dapat langsung ditindaklanjuti pada kunjungan yang sama |
| US-12 | Dokter | Mencatat anamnesis, diagnosis, dan tindakan pada kunjungan berjalan | Hasil pemeriksaan terdokumentasi rapi dan terbaca oleh seluruh petugas |
| US-13 | Dokter | Menyusun resep elektronik dengan pilihan obat dari daftar obat puskesmas | Kesalahan penulisan nama dan dosis obat dapat dihindari |
| US-14 | Dokter | Mengetahui ketersediaan stok obat pada saat menyusun resep | Penggantian obat karena stok kosong dilakukan saat pasien masih di ruang periksa |
| US-15 | Dokter | Menjadwalkan kunjungan ulang bagi pasien yang perlu dipantau | Pemantauan pasien berisiko berlanjut dan tidak berhenti pada satu kunjungan |
| US-16 | Apoteker | Melihat antrean resep yang masuk dari ruang pemeriksaan | Penyiapan obat dapat dimulai sebelum pasien tiba di apotek |
| US-17 | Apoteker | Menandai resep yang obatnya telah diserahkan kepada pasien | Status pelayanan setiap pasien terpantau hingga tuntas |
| US-18 | Apoteker | Mencatat penerimaan obat masuk beserta nomor bets dan tanggal kedaluwarsa | Persediaan obat tertelusur dan obat kedaluwarsa dapat dicegah beredar |
| US-19 | Apoteker | Memperoleh peringatan saat stok obat menipis atau mendekati kedaluwarsa | Pengadaan ulang dilakukan sebelum terjadi kekosongan obat |
| US-20 | Kepala Puskesmas | Melihat rekapitulasi jumlah kunjungan dan diagnosis terbanyak per periode | Laporan bulanan tersusun tanpa perlu menghitung ulang dari berkas kertas |
| US-21 | Kepala Puskesmas | Melihat daftar pasien yang ditandai berisiko penyakit tidak menular | Program pemantauan PTM dapat disasarkan pada pasien yang tepat |
| US-22 | Kepala Puskesmas | Mengekspor laporan ke dalam berkas yang dapat dicetak atau diunggah | Pelaporan ke dinas kesehatan berjalan cepat dan bebas salah ketik |
| US-23 | Kepala Puskesmas | Mengelola akun pengguna beserta hak aksesnya | Akses terhadap data pasien terbatas pada petugas yang berwenang |
| US-24 | Kepala Puskesmas | Mengelola data master obat, poli, dan ambang nilai risiko | Sistem dapat disesuaikan dengan kondisi dan kebijakan fasilitas |
| US-25 | Kepala Puskesmas | Melihat jejak audit perubahan data rekam medis | Setiap perubahan data dapat dipertanggungjawabkan sesuai ketentuan pelindungan data pribadi |
| US-26 | Perawat | Menandai kunjungan sebagai pemeriksaan kehamilan beserta usia kehamilannya | Riwayat pemeriksaan kehamilan tercatat berkesinambungan sejak kunjungan pertama |
| US-27 | Dokter | Memperoleh penanda tanda bahaya kehamilan ketika hasil pengukuran ibu hamil berada di luar ambang aman | Kehamilan berisiko seperti indikasi preeklampsia dikenali dan dirujuk lebih dini |
| US-28 | Kepala Puskesmas | Melihat daftar ibu hamil yang jumlah kunjungan pemeriksaannya belum lengkap | Ibu hamil yang putus pemantauan dapat dikejar sebelum mendekati persalinan |
| US-29 | Perawat | Mencatat hasil upaya tindak lanjut terhadap pasien pada daftar pantau | Pemantauan pasien berisiko terdokumentasi dan tidak berhenti pada penandaan saja |
| US-30 | Kepala Puskesmas | Menjalankan sinkronisasi data kunjungan ke SATUSEHAT ketika koneksi internet tersedia | Kewajiban pelaporan rekam medis elektronik terpenuhi tanpa membuat pelayanan bergantung pada internet |
| US-31 | Kepala Puskesmas | Memperoleh cadangan basis data secara otomatis dan terjadwal | Rekam medis tidak hilang seluruhnya bila terjadi kerusakan perangkat penyimpanan |

## 3.3 Deskripsi Aktivitas

Tabel berikut memuat seluruh aktivitas yang terdapat dalam SEHATI beserta penelusurannya terhadap *user story* pada Subbab 3.2. Aktivitas dikelompokkan mengikuti modul fungsional sistem.

**Modul Pendaftaran dan Data Pasien**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A01 | Mencari Data Pasien | Sistem menelusuri basis data berdasarkan NIK, nama, atau nomor rekam medis, lalu menampilkan data pasien yang cocok | US-01 |
| A02 | Mendaftarkan Pasien Baru | Sistem menerima data diri dan data wali pasien, menerbitkan nomor rekam medis, dan membuat berkas rekam medis elektronik baru | US-02 |
| A03 | Memperbarui Data Pasien | Sistem menyimpan perubahan data diri pasien dan mencatat perubahan tersebut pada jejak audit | US-05 |
| A04 | Membuat Data Kunjungan | Sistem mencatat kedatangan pasien beserta poli tujuan dan menghubungkannya dengan rekam medis pasien | US-03 |
| A05 | Menerbitkan Nomor Antrean Poli | Sistem menetapkan nomor antrean sesuai urutan pendaftaran pada poli yang dipilih dan menyiapkannya untuk dicetak | US-04 |

**Modul Skrining Awal**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A06 | Menampilkan Antrean Skrining | Sistem menampilkan daftar pasien yang telah terdaftar dan menunggu pemeriksaan tanda vital, terurut menurut nomor antrean | US-06 |
| A07 | Mencatat Keluhan Awal dan Tanda Vital | Sistem menerima keluhan awal serta hasil pengukuran tekanan darah, berat badan, tinggi badan, suhu, nadi, dan gula darah sewaktu bila tersedia, disertai validasi rentang nilai | US-07 |
| A08 | Menampilkan Tren Tanda Vital | Sistem menyajikan riwayat tanda vital pasien dari kunjungan-kunjungan sebelumnya dalam bentuk grafik | US-08 |
| A09 | Mengevaluasi Ambang Risiko | Sistem membandingkan nilai tanda vital terhadap ambang klinis dan terhadap riwayat pasien, lalu memunculkan peringatan bila berada di luar ambang normal | US-09 |

**Modul Pemeriksaan**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A10 | Menampilkan Rekam Medis dan Riwayat Kunjungan | Sistem menyajikan data pasien, diagnosis lampau, dan riwayat obat dalam satu tampilan bagi dokter | US-10 |
| A11 | Menampilkan Penanda Risiko PTM | Sistem menandai kunjungan yang hasil skriningnya berada di luar ambang normal sebagai berisiko penyakit tidak menular dan menampilkannya sebagai peringatan visual | US-11 |
| A12 | Mencatat Hasil Pemeriksaan | Sistem menyimpan anamnesis, diagnosis, dan tindakan yang dicatat dokter pada kunjungan berjalan | US-12 |
| A13 | Menyusun Resep Elektronik | Sistem menerima daftar obat, dosis, dan aturan pakai yang dipilih dokter dari data master obat puskesmas | US-13 |
| A14 | Memvalidasi Ketersediaan Stok Obat | Sistem memeriksa kecukupan stok setiap obat pada saat resep disusun dan memberi tahu dokter bila stok tidak mencukupi | US-14 |
| A15 | Menjadwalkan Kunjungan Ulang | Sistem mencatat rencana kunjungan ulang bagi pasien yang perlu dipantau dan memunculkannya pada daftar pantau | US-15 |

**Modul Farmasi**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A16 | Menampilkan Antrean Resep | Sistem meneruskan resep yang telah tervalidasi ke antrean pelayanan farmasi dan menampilkannya kepada apoteker | US-16 |
| A17 | Menutup Pelayanan Resep | Sistem menandai resep sebagai telah diserahkan kepada pasien dan menutup kunjungan yang bersangkutan | US-17 |
| A18 | Mengurangi Stok Obat | Sistem memotong jumlah persediaan obat sesuai jumlah yang diserahkan kepada pasien | US-17 |
| A19 | Mencatat Penerimaan Obat Masuk | Sistem menyimpan data obat yang diterima puskesmas beserta nomor bets dan tanggal kedaluwarsa | US-18 |
| A20 | Memantau Stok Menipis dan Kedaluwarsa | Sistem memunculkan peringatan bagi obat yang jumlahnya di bawah ambang minimum atau mendekati tanggal kedaluwarsa | US-19 |

**Modul Pelaporan dan Administrasi**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A21 | Menyusun Rekapitulasi Kunjungan | Sistem menghitung jumlah kunjungan dan diagnosis terbanyak pada periode tertentu secara otomatis dari data kunjungan | US-20 |
| A22 | Menampilkan Daftar Pantau Pasien Berisiko | Sistem mengumpulkan pasien yang pernah ditandai berisiko PTM menjadi satu daftar yang dapat ditindaklanjuti | US-21 |
| A23 | Mengekspor Laporan | Sistem menghasilkan berkas laporan yang dapat dicetak atau diunggah ke sistem dinas kesehatan | US-22 |
| A24 | Mengelola Akun Pengguna | Sistem menyediakan pembuatan, penyuntingan, dan penonaktifan akun pengguna beserta pengaturan hak aksesnya | US-23 |
| A25 | Mengelola Data Master | Sistem menyediakan pengelolaan data obat, poli, dan ambang nilai risiko agar dapat disesuaikan dengan kebijakan fasilitas | US-24 |
| A26 | Mencatat Jejak Audit | Sistem merekam setiap perubahan data rekam medis beserta pelaku dan waktunya, lalu menampilkannya kepada pengguna yang berwenang | US-25 |

**Modul Pemantauan Kesehatan Ibu**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A27 | Mencatat Kunjungan Pemeriksaan Kehamilan | Sistem menandai kunjungan sebagai pemeriksaan kehamilan, menyimpan usia kehamilan, dan menghitung urutan kunjungan pemeriksaan | US-26 |
| A28 | Mengevaluasi Tanda Bahaya Kehamilan | Sistem membandingkan tekanan darah dan hasil pengukuran rutin ibu hamil terhadap ambang aman, lalu memunculkan penanda risiko bila ambang tersebut terlampaui | US-27 |
| A29 | Menampilkan Daftar Pantau Ibu Hamil | Sistem mengumpulkan ibu hamil yang jumlah kunjungan pemeriksaannya belum lengkap atau yang pernah ditandai berisiko | US-28 |

**Modul Tindak Lanjut**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A30 | Mencatat Hasil Tindak Lanjut | Sistem menyimpan hasil upaya menghubungi pasien pada daftar pantau beserta status kedatangannya pada kunjungan ulang | US-29 |

**Modul Kepatuhan dan Ketahanan Data**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A31 | Mengantrekan Data Kunjungan untuk Sinkronisasi | Sistem menyusun data kunjungan yang telah ditutup menjadi bundel berstandar HL7 FHIR dan memasukkannya ke antrean sinkronisasi | US-30 |
| A32 | Mengirimkan Data ke SATUSEHAT | Sistem mengirim isi antrean sinkronisasi ke platform SATUSEHAT ketika koneksi tersedia, lalu menandai data yang berhasil terkirim | US-30 |
| A33 | Mengekspor Bundel Data untuk Pengiriman Manual | Sistem menyimpan antrean sinkronisasi sebagai berkas apabila koneksi tidak kunjung tersedia, agar dapat diunggah dari lokasi lain yang berjaringan | US-30 |
| A34 | Menjalankan Pencadangan Basis Data Terjadwal | Sistem membuat salinan basis data secara otomatis menurut jadwal, menyimpannya pada media terpisah, dan memperingatkan pengguna bila pencadangan gagal | US-31 |

Aktivitas A01 sampai A18 membentuk alur utama pelayanan rawat jalan yang divisualisasikan pada Gambar 1 di Subbab 3.4, sedangkan A27 dan A28 menyisip pada alur yang sama ketika kunjungan berjenis pemeriksaan kehamilan. Adapun A19 sampai A26, A29, A30, serta A31 sampai A34 dijalankan di luar alur pelayanan pasien, yaitu pada pengelolaan persediaan obat, penyusunan laporan, tindak lanjut daftar pantau, dan pemenuhan kewajiban pelaporan elektronik. Alur pelaporan dan sinkronisasi tersebut digambarkan pada Gambar 2.

## 3.4 Model Proses Bisnis

Diagram berikut memodelkan proses bisnis pelayanan rawat jalan di puskesmas ketika dijalankan dengan bantuan SEHATI. Diagram disusun dalam bentuk *swimlane activity diagram* dengan lima lajur: empat lajur untuk aktor manusia (Petugas Pendaftaran, Perawat, Dokter, Apoteker) dan satu lajur untuk sistem perangkat lunak.

Ada tiga titik dalam alur ini yang menunjukkan peningkatan efisiensi dibandingkan proses manual pada Subbab 1.2:

1. **Pencarian data pasien dilakukan oleh sistem**, menggantikan pencarian berkas fisik di rak (menutup kesenjangan G-01 dan G-02).
2. **Evaluasi ambang risiko dilakukan otomatis** setelah tanda vital dimasukkan, sehingga penandaan pasien berisiko tidak lagi bergantung pada kejelian petugas (menutup kesenjangan G-03 dan G-04).
3. **Validasi stok obat terjadi saat resep disusun**, bukan setelah pasien mengantre di apotek, sehingga penggantian obat dilakukan tanpa pasien perlu bolak-balik (menutup kesenjangan G-05).

Pada akhir alur, sistem memperbarui rekam medis dan rekapitulasi laporan secara langsung, sehingga tidak diperlukan lagi pengetikan ulang di akhir bulan (menutup kesenjangan G-06).

<br>

<p align="center">
<img alt="Activity Diagram Proses Pelayanan Rawat Jalan Puskesmas dengan SEHATI" src="./assets/diagram/diagram-act-1.svg" width="90%">
</p>
<p align="center">
<i>Gambar 1. Activity Diagram proses pelayanan rawat jalan puskesmas dengan SEHATI</i>
</p>

<br>

### Penjelasan alur

Nomor aktivitas dalam kurung merujuk pada tabel Deskripsi Aktivitas di Subbab 3.3.

1. **Pendaftaran.** Petugas Pendaftaran menerima pasien dan meminta NIK atau kartu berobat. Sistem mencari data pasien pada basis data (A01). Bila pasien belum terdaftar, petugas mendaftarkan pasien baru dan sistem membuatkan berkas rekam medis elektronik (A02). Selanjutnya petugas membuat data kunjungan (A04), dan sistem menyimpan kunjungan tersebut serta menampilkannya pada daftar antrean poli (A05, A06).
2. **Skrining.** Perawat memanggil pasien sesuai antrean, mencatat keluhan awal, dan memasukkan hasil pengukuran tanda vital (A07). Sistem menyimpan data tersebut lalu mengevaluasinya terhadap ambang risiko (A09). Bila terdapat nilai di luar ambang normal, sistem menandai kunjungan tersebut sebagai berisiko penyakit tidak menular (A11).
3. **Pemeriksaan.** Dokter membuka rekam medis pasien beserta riwayat kunjungan dan penanda risiko yang dihasilkan sistem (A10, A11), melakukan pemeriksaan, mencatat diagnosis dan tindakan (A12), lalu menyusun resep elektronik (A13). Sistem memeriksa ketersediaan stok setiap obat yang diresepkan (A14). Bila stok tidak mencukupi, dokter mengganti obat pada saat itu juga. Bila mencukupi, sistem meneruskan resep ke antrean pelayanan farmasi (A16).
4. **Farmasi.** Apoteker menyiapkan obat, menyerahkannya kepada pasien, dan menjelaskan aturan pakai.
5. **Penutupan.** Sistem mengurangi stok obat sesuai jumlah yang diserahkan (A18), menutup kunjungan (A17), memperbarui rekapitulasi laporan harian (A21), lalu memasukkan data kunjungan tersebut ke antrean sinkronisasi SATUSEHAT (A31).

Apabila kunjungan berjenis pemeriksaan kehamilan, perawat atau bidan menandainya pada tahap skrining beserta usia kehamilan (A27), dan sistem mengevaluasi tanda bahaya kehamilan pada tahap yang sama dengan evaluasi ambang risiko PTM (A28).

### Alur pelaporan dan sinkronisasi

Diagram kedua memodelkan aktivitas yang berjalan di luar alur pelayanan pasien, yaitu penyusunan laporan dan pemenuhan kewajiban pelaporan elektronik. Di sinilah aktor **Kepala Puskesmas** berinteraksi dengan sistem, dan di sinilah mekanisme *store-and-forward* yang menjadi nilai unik kedua SEHATI bekerja.

Titik penting pada alur ini adalah percabangan ketersediaan koneksi. Ketika internet tersedia, bundel data berstandar HL7 FHIR dikirim langsung ke SATUSEHAT. Ketika tidak tersedia, bundel yang sama disimpan sebagai berkas ekspor sehingga dapat diunggah dari lokasi lain yang berjaringan. Kedua jalur bermuara pada hasil yang sama, dan tidak satu pun di antaranya menghentikan pelayanan di puskesmas.

<br>

<p align="center">
<img alt="Activity Diagram Pelaporan dan Sinkronisasi Data ke SATUSEHAT" src="./assets/diagram/diagram-act-2.svg" width="70%">
</p>
<p align="center">
<i>Gambar 2. Activity Diagram pelaporan dan sinkronisasi data ke SATUSEHAT</i>
</p>

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
7. Badan Pusat Statistik. *Angka Kematian Ibu (AKI) Hasil Long Form SP2020 Menurut Provinsi*. https://www.bps.go.id/id/statistics-table/1/MjIxOSMx/angka-kematian-ibu-aki-maternal-mortality-rate-mmr-hasil-long-form-sp2020-menurut-provinsi-2020.html
8. United Nations. *Sustainable Development Goal 3: Ensure healthy lives and promote well-being for all at all ages*. https://sdgs.un.org/goals/goal3

**Solusi dan standar teknis pembanding**

9. Kementerian Kesehatan Republik Indonesia. *SATUSEHAT Platform: Dokumentasi HL7 FHIR*. https://satusehat.kemkes.go.id/platform/docs/id/fhir/
10. ASRI (Aplikasi Sistem RME Indonesia), aplikasi rekam medis elektronik gratis dari Kementerian Kesehatan yang terintegrasi dengan SATUSEHAT, tersedia tanpa biaya hingga 31 Desember 2026.

**Perkakas**

11. Diagram UML: https://www.drawio.com/, https://staruml.io/
