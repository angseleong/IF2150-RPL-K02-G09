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

Digitalisasi puskesmas sendiri sudah berjalan jauh. Kementerian Kesehatan mencatat 34.463 fasilitas pelayanan kesehatan telah terintegrasi dengan platform SATUSEHAT per 27 Oktober 2025, dan mayoritas puskesmas kini sudah meninggalkan pencatatan yang sepenuhnya manual. Namun kemajuan tersebut menyisakan dua persoalan yang justru menjadi titik berangkat perangkat lunak ini.

**Persoalan pertama, sebaran digitalisasi belum merata.** Kementerian Kesehatan sendiri mengakui bahwa hambatan yang tersisa adalah keterbatasan infrastruktur jaringan di daerah dan kesiapan sumber daya manusia. Puskesmas di wilayah terpencil masih menjalankan pencatatan kertas, atau menerapkan sistem hibrida dengan sebagian data di komputer dan sebagian lagi di lembar kertas. Model hibrida ini bahkan lebih menyulitkan penelusuran riwayat, karena catatan seorang pasien terbelah di dua media sehingga tidak ada satu pun yang memuat gambaran utuh.

**Persoalan kedua, dan inilah yang lebih mendasar, digitalisasi ternyata belum menutup celah deteksi dini.** Sistem informasi puskesmas yang tersedia umumnya bersifat pasif: ia menyimpan apa yang diketik dan menampilkannya kembali bila diminta, tetapi tidak pernah membandingkan hasil pemeriksaan seorang pasien antar-waktu. Akibatnya, meskipun angka pemeriksaan sudah tersimpan secara elektronik, data agregat yang seharusnya bisa dipakai untuk mengenali pasien berisiko tetap tidak pernah terbentuk.

Dampak paling nyata dari terputusnya riwayat kesehatan pasien terlihat pada penanganan **Penyakit Tidak Menular (PTM)**. Survei Kesehatan Indonesia (SKI) 2023 mencatat prevalensi hipertensi pada penduduk berusia 18 tahun ke atas sebesar **30,8%** berdasarkan hasil pengukuran tekanan darah, sementara prevalensi berdasarkan diagnosis dokter hanya **8,6%**. Selisih lebih dari 22 poin persen ini berarti sebagian besar penderita hipertensi di Indonesia tidak pernah tahu bahwa dirinya sakit, sehingga tidak pernah menjalani pengobatan. Pola yang sama terjadi pada diabetes melitus: prevalensi berdasarkan pemeriksaan gula darah pada penduduk usia 15 tahun ke atas mencapai 11,7%, sedangkan yang terdiagnosis dokter hanya 2,2%.

Yang perlu digarisbawahi, celah tersebut nyaris tidak bergerak dalam lima tahun. Riset Kesehatan Dasar (Riskesdas) 2018 mencatat selisih sekitar 25 poin persen (34,1% berdasarkan pengukuran berbanding 8,8% berdasarkan diagnosis), dan pada SKI 2023 selisihnya masih sekitar 22 poin persen. Artinya penurunan prevalensi hipertensi tidak diiringi perbaikan berarti pada kemampuan sistem kesehatan mendeteksi penderitanya. Beban itu berlanjut sampai tahap pengobatan: di antara penduduk usia 15 tahun ke atas yang sudah terdiagnosis hipertensi, hanya 46,7% yang meminum obat secara teratur, sementara 36,4% meminum obat tidak teratur dan 16,9% tidak meminum obat sama sekali. Sebagian besar penderita ini sebenarnya *pernah* datang ke puskesmas dan *pernah* diukur tekanan darahnya. Persoalannya, hasil pengukuran itu diperlakukan sebagai catatan kunjungan hari itu saja, baik ketika ditulis di lembar kertas maupun ketika diketik ke sistem elektronik yang tidak membandingkan nilai antar-waktu. Akibatnya tidak ada mekanisme apa pun yang menghubungkan angka tinggi hari ini dengan angka tinggi tiga bulan lalu.

### Keterkaitan dengan SDGs

Kelompok kami memilih **SDG 3: *Good Health and Well-being* (Kehidupan Sehat dan Sejahtera)** sebagai landasan solusi. Agar lingkup pengerjaan tetap terkendali, perangkat lunak yang kami usulkan menyasar dua target berikut secara terfokus, bukan menyentuh banyak target sekaligus secara dangkal:

| Target SDG 3 | Rumusan Target | Keterkaitan dengan Solusi |
| :--- | :--- | :--- |
| **3.4** | Mengurangi sepertiga angka kematian dini akibat penyakit tidak menular melalui pencegahan dan pengobatan | Sistem menyimpan riwayat tanda vital lintas kunjungan dan secara otomatis menandai pasien yang hasil pengukurannya berada di luar ambang normal, sehingga deteksi dini tidak lagi bergantung pada ingatan petugas |
| **3.8** | Mencapai cakupan kesehatan semesta (*Universal Health Coverage*), termasuk akses pelayanan kesehatan dasar yang bermutu | Sistem memangkas waktu administrasi di FKTP sehingga waktu tenaga kesehatan dapat dialihkan untuk pelayanan, dan mutu pencatatan menjadi seragam |

### Urgensi

**Pertama, kewajiban regulasi sudah berjalan dan pemenuhannya terbukti tidak mudah.** Peraturan Menteri Kesehatan Nomor 24 Tahun 2022 mewajibkan seluruh fasilitas pelayanan kesehatan menyelenggarakan Rekam Medis Elektronik (RME) selambat-lambatnya 31 Desember 2023. Tenggat tersebut kemudian diundur hingga akhir 2025 karena berbagai hambatan implementasi, dan Kementerian Kesehatan masih perlu menerbitkan surat edaran penegasan pada 15 April 2025. Pengunduran dua tahun itu sendiri merupakan bukti bahwa penyediaan sistem yang benar-benar dapat dipakai di lapangan bukan perkara sepele, terutama bagi fasilitas dengan infrastruktur terbatas.

**Kedua, dan yang paling menentukan, digitalisasi terbukti tidak dengan sendirinya menutup celah deteksi dini.** Antara 2018 dan 2023 terjadi digitalisasi besar-besaran di sektor kesehatan: PMK 24/2022 terbit, SATUSEHAT diluncurkan, dan ribuan fasilitas terintegrasi. Namun celah antara prevalensi terukur dan prevalensi terdiagnosis hanya bergerak dari sekitar 25 poin persen menjadi sekitar 22 poin persen. Perlu ditegaskan bahwa SKI 2023 diukur setelah kewajiban RME berlaku, sehingga angka tersebut bukan potret puskesmas yang masih serba kertas, melainkan potret sistem kesehatan yang sudah mulai terdigitalisasi namun celahnya tetap menganga. Pemindahan catatan dari kertas ke layar saja jelas belum cukup.

**Ketiga, solusi yang tersedia belum menjawab kedua persoalan itu sekaligus.** Sistem Informasi Manajemen Puskesmas (SIMPUS) komersial membebankan biaya langganan dan bergantung pada internet yang stabil, sementara sistem yang disediakan pemerintah menghadapi kendala pembaruan dan tata kelola sebagaimana dilaporkan sejumlah penelitian implementasi. Yang lebih penting, hampir seluruhnya dirancang sebagai sistem pencatatan administratif, bukan sebagai sistem yang aktif memantau perubahan kondisi pasien antar-waktu.

## 1.2 Analisis Kondisi Saat Ini

### Alur proses yang berjalan saat ini

Alur berikut menggambarkan pelayanan rawat jalan pada puskesmas non rawat inap yang masih menjalankan pencatatan manual atau hibrida, disusun berdasarkan studi literatur dan laporan implementasi. Pada puskesmas yang sudah terdigitalisasi, langkah 1 sampai 4 memang berjalan lebih cepat, namun sifat pencatatannya tetap sama: setiap kunjungan berdiri sendiri dan tidak dibandingkan dengan kunjungan sebelumnya.

1. **Pendaftaran.** Pasien datang, mengantre di loket, lalu menyebutkan nama atau menyerahkan kartu berobat. Petugas mencari berkas rekam medis pasien secara manual di rak penyimpanan. Untuk pasien baru, petugas menuliskan data diri pada formulir kertas dan membuatkan berkas baru. Nomor antrean poli diberikan secara manual.
2. **Skrining awal.** Perawat memanggil pasien, mengukur tanda vital (tekanan darah, berat badan, tinggi badan, suhu), lalu menuliskan hasilnya pada lembar kunjungan hari itu.
3. **Pemeriksaan.** Dokter membaca berkas, melakukan pemeriksaan, lalu menuliskan anamnesis, diagnosis, dan tindakan dengan tulisan tangan. Resep ditulis pada lembar terpisah dan diserahkan kepada pasien.
4. **Farmasi.** Pasien membawa resep ke apotek puskesmas. Apoteker membaca resep, mengecek ketersediaan obat di lemari secara fisik, menyiapkan obat, lalu mencatat pengeluaran obat di buku stok.
5. **Pelaporan.** Pada akhir bulan, petugas merekap seluruh lembar kunjungan secara manual, lalu mengetik ulang angkanya ke dalam format laporan yang diminta dinas kesehatan.

### Kesenjangan (*gap*) yang teridentifikasi

Dari alur di atas, kami mengidentifikasi tujuh kesenjangan yang akan menjadi sasaran perangkat lunak kami.

Kolom terakhir menandai kepada siapa kesenjangan tersebut berlaku, karena tidak semuanya hilang begitu sebuah puskesmas terdigitalisasi. Perbedaan inilah yang menentukan arah solusi.

| Kode | Kesenjangan | Penjelasan | Dampak | Berlaku pada |
| :--- | :--- | :--- | :--- | :--- |
| **G-01** | Pencarian data rekam medis lambat | Berkas dicari manual di rak, atau riwayat pasien harus dirangkai dari dua media pada puskesmas hibrida | Waktu tunggu pasien bertambah, antrean menumpuk di jam sibuk | Manual dan hibrida |
| **G-02** | Berkas hilang, rusak, atau tidak terbaca | Kertas rentan terhadap kelembapan dan kehilangan; tulisan tangan dokter sering sulit dibaca oleh apoteker | Risiko kesalahan pemberian obat dan riwayat pasien yang tidak dapat direkonstruksi | Manual dan hibrida |
| **G-03** | Riwayat kesehatan tidak berkesinambungan | Setiap kunjungan diperlakukan sebagai catatan yang berdiri sendiri, tanpa mekanisme membandingkan hasil antar-waktu, baik pada lembar kertas maupun pada basis data | Tren kondisi pasien seperti tekanan darah yang terus naik tidak pernah terlihat | **Seluruhnya, termasuk yang sudah digital** |
| **G-04** | Tidak ada mekanisme deteksi dini | Penandaan pasien berisiko sepenuhnya bergantung pada kejelian dan ingatan petugas, karena sistem tidak pernah memunculkannya sendiri | Pasien berisiko PTM lolos dari pemantauan, sejalan dengan celah 22 poin persen pada SKI 2023 | **Seluruhnya, termasuk yang sudah digital** |
| **G-05** | Stok obat tidak terpantau secara *real-time* | Pengecekan stok dilakukan secara fisik dan pencatatan dilakukan menyusul | Terjadi kekosongan obat mendadak dan obat kedaluwarsa yang tidak terdeteksi | Manual dan hibrida |
| **G-06** | Rekapitulasi laporan lambat dan rawan salah | Data harus dibaca ulang dari lembar kunjungan lalu diketik ulang ke format laporan | Laporan terlambat, rawan salah hitung, dan menyita waktu tenaga kesehatan | Manual dan hibrida |
| **G-07** | Solusi digital eksisting bersifat administratif dan mengandaikan jaringan stabil | Sistem yang tersedia berfokus pada penyimpanan dan penampilan ulang data, bukan pada pemantauan antar-waktu; sebagian besar juga berbasis awan atau berlangganan | Celah deteksi dini tetap terbuka meskipun fasilitas sudah terdigitalisasi, sementara puskesmas berjaringan terbatas kesulitan mengadopsinya sama sekali | **Seluruhnya, termasuk yang sudah digital** |

Perhatikan bahwa **G-03, G-04, dan G-07 tidak hilang meskipun sebuah puskesmas sudah sepenuhnya digital.** Ketiganya inilah inti permasalahan yang diangkat, sedangkan G-01, G-02, G-05, dan G-06 merupakan beban tambahan yang masih ditanggung puskesmas manual dan hibrida.

### Perbandingan dengan solusi yang sudah ada

| Solusi | Kelebihan | Keterbatasan terhadap konteks masalah |
| :--- | :--- | :--- |
| **Pencatatan manual (kertas)** | Tidak butuh perangkat, tidak butuh pelatihan, tidak bergantung listrik | Menanggung seluruh kesenjangan G-01 sampai G-06 sekaligus |
| **SIMPUS komersial berbasis awan** | Fitur lengkap, terintegrasi dengan sistem nasional, ada dukungan teknis | Berbayar per bulan, membutuhkan internet stabil, layanan terhenti saat koneksi putus (G-07) |
| **SIKDA Generik (sistem gratis Kemenkes untuk puskesmas)** | Gratis, resmi dari Kementerian Kesehatan, dan dirancang khusus untuk alur kerja puskesmas | Penelitian implementasi melaporkan kendala berupa keterbatasan sumber daya manusia terlatih, keterbatasan infrastruktur, pembaruan sistem yang lambat, dan tata kelola yang belum mapan. Sebagaimana SIMPUS pada umumnya, sistem ini bersifat administratif dan tidak melakukan pemantauan risiko antar-waktu (G-03, G-04) |
| **ASRI (RME gratis Kemenkes)** | Gratis dan sudah terhubung ke SATUSEHAT sejak awal | Diperuntukkan bagi Tempat Praktik Mandiri Dokter dan Dokter Gigi, bukan untuk alur pelayanan puskesmas yang melibatkan banyak peran sekaligus |
| **Spreadsheet mandiri (Excel)** | Gratis, sudah dikuasai sebagian petugas | Tidak ada validasi data, tidak ada relasi antar-tabel, tidak ada kendali akses, rawan tertimpa; tidak menyelesaikan G-03 dan G-04 |
| **SATUSEHAT (platform Kemenkes)** | Standar interoperabilitas nasional, memungkinkan pertukaran data antar-fasilitas | Merupakan platform pertukaran data, bukan aplikasi operasional harian; fasilitas tetap membutuhkan sistem sendiri untuk mencatat |

Perhatikan satu hal yang sama pada seluruh alternatif digital di atas, termasuk yang disediakan gratis oleh pemerintah: semuanya dirancang sebagai **sistem pencatatan yang pasif**. Sistem-sistem tersebut menyimpan hasil pemeriksaan dengan rapi dan menampilkannya kembali bila diminta, tetapi tidak satu pun yang secara aktif membandingkan hasil seorang pasien antar-waktu lalu memunculkannya sebagai pasien yang perlu dipantau. Inilah sebabnya digitalisasi yang sudah berjalan luas belum menutup celah deteksi pada data SKI 2023.

Kesimpulannya, celah yang belum terisi adalah **sistem yang secara aktif memantau perubahan kondisi pasien antar-waktu, tetap berjalan penuh tanpa internet sehingga dapat dipakai di fasilitas dengan jaringan terbatas, namun tetap memenuhi kewajiban pelaporan elektronik**. Celah inilah yang akan diisi oleh perangkat lunak kami.

---

# BAB 2: Analisis Solusi

## 2.1 Deskripsi Perangkat Lunak

**SEHATI (Sistem Elektronik Pelayanan Kesehatan Terintegrasi)** adalah aplikasi *desktop* pengelolaan pelayanan rawat jalan untuk puskesmas dan klinik pratama. SEHATI menyatukan seluruh rantai pelayanan (mulai dari pendaftaran pasien, skrining tanda vital, pemeriksaan dokter, peresepan elektronik, penyerahan obat, hingga rekapitulasi laporan) ke dalam satu aplikasi yang dipakai bersama oleh seluruh petugas di satu fasilitas kesehatan.

### Gambaran dari sudut pandang pengguna

Aplikasi ini dirancang untuk mempermudah alur kerja seluruh petugas fasilitas kesehatan. **Petugas pendaftaran** menemukan data pasien secara instan lewat NIK atau nomor rekam medis, lalu mencetak nomor antrean, tanpa perlu membongkar rak berkas maupun merangkai riwayat dari dua media pada puskesmas hibrida. Selanjutnya, **perawat** tinggal menginput data skrining tanda vital ke dalam formulir digital. Sistem akan langsung membandingkan nilai tersebut dengan batas normal dan menampilkan grafik riwayat kunjungan pasien.

Bagi **dokter**, seluruh riwayat kesehatan (mulai dari diagnosis lampau, obat yang dikonsumsi, hingga tren tekanan darah) akan langsung tersaji dalam satu layar sebelum pemeriksaan. Jika ada indikasi risiko penyakit tertentu, sistem akan langsung memberikan peringatan otomatis. Setelah diperiksa, dokter bisa langsung menyusun resep secara digital. Resep ini kemudian otomatis terkirim ke **apoteker** dalam bentuk teks yang jelas dan stoknya juga sudah divalidasi oleh sistem.

Pada akhirnya, pekerjaan **kepala puskesmas** juga menjadi jauh lebih ringan. Rekapitulasi laporan kunjungan maupun daftar pasien berisiko disusun otomatis oleh sistem, tanpa perlu mengumpulkan dan mengetik ulang data dari lembar kunjungan.

### Target platform dan alasan pemilihannya

SEHATI dikembangkan sebagai **aplikasi desktop** yang dipasang pada komputer di lingkungan puskesmas. Pemilihan ini didasarkan pada empat pertimbangan.

1. **Kemandirian terhadap koneksi internet.** Kesenjangan G-07 menunjukkan bahwa ketergantungan pada jaringan stabil menjadi penghalang adopsi di daerah, sejalan dengan pengakuan Kementerian Kesehatan mengenai keterbatasan infrastruktur. Aplikasi desktop dengan basis data lokal tetap berfungsi penuh saat jaringan terputus, suatu kondisi yang tidak boleh menghentikan pelayanan kesehatan.
2. **Kesesuaian dengan perangkat yang sudah tersedia.** Puskesmas umumnya sudah memiliki komputer atau laptop di loket pendaftaran dan ruang periksa. Aplikasi desktop yang ringan dapat memanfaatkan perangkat tersebut tanpa pengadaan baru.
3. **Kesesuaian dengan pola kerja.** Pendaftaran, pemeriksaan, dan farmasi dilakukan di meja kerja tetap dengan intensitas pengetikan yang tinggi. Antarmuka desktop dengan papan ketik penuh dan pintasan papan ketik lebih efisien untuk pola kerja seperti ini dibandingkan antarmuka sentuh.
4. **Kendali atas data sensitif.** Data rekam medis tersimpan di dalam lingkungan fasilitas itu sendiri, sehingga kendali dan tanggung jawab penyimpanan tetap berada di tangan fasilitas, sejalan dengan kewajiban menjaga kerahasiaan rekam medis.

### Nilai unik dan inovasi inti

Pembeda utama SEHATI dari SIMPUS pada umumnya terletak pada tiga hal berikut.

**1. Pemantauan longitudinal dengan daftar pantau yang tertutup siklusnya (inovasi inti).**
Pembeda SEHATI bukan pada pemeriksaan ambang batas semata, melainkan pada apa yang terjadi sesudah ambang itu terlampaui. Setiap hasil pengukuran disimpan sebagai bagian dari satu rangkaian riwayat pasien, bukan sebagai catatan lepas per kunjungan. Sistem membandingkan nilai terbaru terhadap ambang klinis sekaligus terhadap pola pengukuran pasien pada kunjungan-kunjungan sebelumnya, sehingga tekanan darah tinggi yang berulang dapat dibedakan dari lonjakan sesaat. Pasien yang polanya mengarah ke risiko masuk ke dalam **daftar pantau**, dan daftar itu bukan sekadar tampilan: petugas mencatat upaya tindak lanjut serta status kedatangan pasien pada kunjungan ulang, sehingga pemantauan benar-benar tertutup siklusnya. Dengan cara inilah celah antara pasien yang "pernah diukur" dan yang benar-benar "terdiagnosis lalu tertangani" (celah 30,8% versus 8,6% pada data SKI 2023) ditangani secara sistemik.

**2. Arsitektur *offline-first* dengan sinkronisasi tunda ke SATUSEHAT.**
Fasilitas dengan jaringan terbatas kerap terjebak di antara dua pilihan yang sama-sama buruk: sistem berbasis awan yang patuh regulasi tetapi berhenti melayani ketika jaringan putus, atau pencatatan lokal seadanya yang selalu jalan tetapi tidak pernah memenuhi kewajiban pelaporan. SEHATI menolak pilihan tersebut. Seluruh fungsi pelayanan berjalan penuh di atas basis data lokal tanpa memerlukan internet sama sekali. Setiap kunjungan yang ditutup otomatis disusun menjadi data berstandar **HL7 FHIR** lalu dimasukkan ke dalam **antrean sinkronisasi**. Ketika koneksi tersedia, kapan pun itu termasuk di luar jam pelayanan, antrean dikirim ke platform SATUSEHAT dan ditandai selesai. Bila koneksi tidak kunjung tersedia, antrean dapat diekspor sebagai berkas untuk diunggah dari lokasi lain yang berjaringan. Hasilnya, pelayanan tidak pernah berhenti karena internet, sementara kewajiban PMK 24/2022 tetap terpenuhi.

**3. Alur kerja terpadu dari pendaftaran hingga farmasi.**
Data mengalir dalam satu aplikasi tanpa penyalinan ulang antar-tahap. Resep yang disusun dokter langsung tervalidasi terhadap stok apotek pada saat penyusunan, sehingga penggantian obat karena stok kosong terjadi di ruang periksa, bukan setelah pasien mengantre di apotek.

### Ketahanan data

Menyimpan seluruh rekam medis pada satu basis data lokal memunculkan risiko yang perlu dijawab secara jujur. Arsip kertas hilang satu per satu, sedangkan kerusakan satu media penyimpanan berpotensi melenyapkan seluruh rekam medis sekaligus. Karena itu pencadangan tidak diserahkan pada kedisiplinan petugas. SEHATI menjalankan pencadangan basis data secara otomatis menurut jadwal ke media penyimpanan terpisah, menampilkan status pencadangan terakhir pada layar utama, serta memperingatkan pengguna bila pencadangan gagal atau tertunda. Antrean sinkronisasi ke SATUSEHAT turut berperan sebagai salinan kedua yang berada di luar fasilitas.

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

**Tahap inti**, yang wajib selesai, mencakup modul pendaftaran dan data pasien, skrining awal, pemeriksaan, farmasi, serta pemantauan tindak lanjut. Rangkaian ini sudah membentuk satu siklus pelayanan rawat jalan yang utuh sekaligus memuat nilai unik utama perangkat lunak ini.

**Tahap lanjutan**, yang dikerjakan setelah tahap inti stabil, mencakup pelaporan dan administrasi, sinkronisasi ke SATUSEHAT, serta pencadangan terjadwal.

Pembagian ini memastikan bahwa apabila waktu tidak mencukupi, yang tertunda adalah fitur pelengkap, bukan alur pelayanan inti.

---

# BAB 3: Spesifikasi Kebutuhan dan Proses Bisnis

## 3.1 Identifikasi Aktor

Aktor dibatasi pada empat peran yang **berinteraksi langsung dengan sistem** dalam pekerjaan sehari-harinya. Peran administratif seperti pengelolaan akun, data master, pelaporan, dan sinkronisasi digabungkan ke dalam Petugas Administrasi, karena pada puskesmas non rawat inap pekerjaan tersebut umumnya ditangani petugas yang sama dengan yang melayani loket pendaftaran.

| Aktor | Deskripsi |
| :--- | :--- |
| **Petugas Administrasi** | Pengguna yang bertugas di loket sekaligus menangani administrasi sistem. Bertanggung jawab mendaftarkan pasien, mencari data pasien, membuat kunjungan dan antrean, menyusun laporan periodik, mengelola akun pengguna beserta data master, serta menjalankan sinkronisasi data ke SATUSEHAT. Karakteristiknya adalah bekerja di bawah tekanan antrean pada jam sibuk, sehingga mengutamakan kecepatan pencarian data dan minimnya langkah untuk menyelesaikan satu pendaftaran. |
| **Perawat** | Tenaga kesehatan yang melakukan skrining awal sebelum pasien diperiksa dokter, sekaligus menindaklanjuti pasien yang ditandai berisiko. Bertanggung jawab mencatat keluhan awal dan tanda vital, membaca tren pengukuran pasien, serta mencatat hasil upaya tindak lanjut. Karakteristiknya adalah menangani banyak pasien dalam waktu singkat, sehingga membutuhkan formulir masukan yang ringkas dengan validasi nilai agar tidak terjadi salah ketik angka. |
| **Dokter** | Tenaga medis yang melakukan pemeriksaan, menegakkan diagnosis, menentukan tindakan, meresepkan obat, dan menjadwalkan kunjungan ulang. Karakteristiknya adalah membutuhkan gambaran riwayat pasien yang utuh dalam waktu singkat dan menuntut agar sistem tidak memperlambat proses pemeriksaan. Dokter merupakan pengguna dengan kewenangan tertinggi atas isi rekam medis pasien. |
| **Apoteker** | Tenaga kefarmasian yang menyiapkan serta menyerahkan obat kepada pasien dan mengelola persediaan obat puskesmas. Karakteristiknya adalah membutuhkan resep yang terbaca jelas dan informasi stok yang akurat, serta bertanggung jawab atas ketertelusuran pengeluaran obat. |

> **Catatan.** Pasien merupakan pemangku kepentingan utama yang memperoleh manfaat dari sistem, namun bukan aktor karena tidak berinteraksi langsung dengan perangkat lunak (lihat batasan BL-07). Kepala Puskesmas juga tidak dijadikan aktor, karena kebutuhannya terhadap laporan dan daftar pantau dipenuhi melalui keluaran yang disiapkan Petugas Administrasi, bukan melalui interaksi langsung dengan sistem.

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

Tabel berikut memuat seluruh aktivitas yang terdapat dalam SEHATI beserta penelusurannya terhadap *user story* pada Subbab 3.2. Aktivitas dikelompokkan mengikuti modul fungsional sistem.

**Modul Pendaftaran dan Data Pasien**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A01 | Mencari Data Pasien | Sistem menelusuri basis data berdasarkan NIK, nama, atau nomor rekam medis, lalu menampilkan data pasien yang cocok | US-01 |
| A02 | Mengelola Data Pasien | Sistem menerima pendaftaran pasien baru, menerbitkan nomor rekam medis, serta menyimpan perubahan data pasien lama | US-02 |
| A03 | Membuat Kunjungan dan Nomor Antrean | Sistem mencatat kedatangan pasien beserta poli tujuan, menghubungkannya dengan rekam medis, lalu menerbitkan nomor antrean | US-03 |

**Modul Skrining Awal**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A04 | Menampilkan Antrean Skrining | Sistem menampilkan daftar pasien yang telah terdaftar dan menunggu pemeriksaan tanda vital, terurut menurut nomor antrean | US-08 |
| A05 | Mencatat Keluhan Awal dan Tanda Vital | Sistem menerima keluhan awal serta hasil pengukuran tekanan darah, berat badan, tinggi badan, suhu, nadi, dan gula darah sewaktu bila tersedia, disertai validasi rentang nilai | US-09 |
| A06 | Mengevaluasi Ambang Risiko | Sistem membandingkan nilai tanda vital terhadap ambang klinis dan terhadap riwayat pasien, memunculkan peringatan, lalu menandai kunjungan sebagai berisiko penyakit tidak menular | US-09 |
| A07 | Menampilkan Tren Tanda Vital | Sistem menyajikan riwayat tanda vital pasien dari kunjungan-kunjungan sebelumnya dalam bentuk grafik | US-10 |

**Modul Pemeriksaan**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A08 | Menampilkan Rekam Medis dan Penanda Risiko | Sistem menyajikan data pasien, diagnosis lampau, riwayat obat, serta penanda risiko hasil skrining dalam satu tampilan bagi dokter | US-12 |
| A09 | Mencatat Hasil Pemeriksaan | Sistem menyimpan anamnesis, diagnosis, dan tindakan yang dicatat dokter pada kunjungan berjalan | US-13 |
| A10 | Menyusun Resep Elektronik | Sistem menerima daftar obat, dosis, dan aturan pakai yang dipilih dokter dari data master obat puskesmas | US-14 |
| A11 | Memvalidasi Ketersediaan Stok Obat | Sistem memeriksa kecukupan stok setiap obat pada saat resep disusun dan memberi tahu dokter bila stok tidak mencukupi | US-14 |
| A12 | Menjadwalkan Kunjungan Ulang | Sistem mencatat rencana kunjungan ulang bagi pasien yang perlu dipantau dan memunculkannya pada daftar pantau | US-15 |

**Modul Farmasi**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A13 | Menampilkan Antrean Resep | Sistem meneruskan resep yang telah tervalidasi ke antrean pelayanan farmasi dan menampilkannya kepada apoteker | US-16 |
| A14 | Menutup Pelayanan Resep | Sistem menandai resep sebagai telah diserahkan, memotong stok obat sesuai jumlah yang diserahkan, lalu menutup kunjungan | US-17 |
| A15 | Mencatat Penerimaan Obat | Sistem menyimpan data obat yang diterima puskesmas beserta nomor bets dan tanggal kedaluwarsa | US-18 |
| A16 | Memantau Stok Menipis dan Kedaluwarsa | Sistem memunculkan peringatan bagi obat yang jumlahnya di bawah ambang minimum atau mendekati tanggal kedaluwarsa | US-18 |

**Modul Pemantauan Tindak Lanjut**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A17 | Menampilkan Daftar Pantau | Sistem mengumpulkan pasien yang pernah ditandai berisiko maupun yang dijadwalkan kunjungan ulang menjadi satu daftar yang dapat ditindaklanjuti | US-11 |
| A18 | Mencatat Hasil Tindak Lanjut | Sistem menyimpan hasil upaya menghubungi pasien pada daftar pantau beserta status kedatangannya pada kunjungan ulang | US-11 |

**Modul Pelaporan dan Administrasi**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A19 | Menyusun Rekapitulasi Kunjungan | Sistem menghitung jumlah kunjungan dan diagnosis terbanyak pada periode tertentu secara otomatis dari data kunjungan | US-04 |
| A20 | Mengekspor Laporan | Sistem menghasilkan berkas laporan yang dapat dicetak atau diunggah ke sistem dinas kesehatan | US-04 |
| A21 | Mengelola Akun dan Data Master | Sistem menyediakan pengelolaan akun pengguna beserta hak aksesnya, serta data obat, poli, dan ambang nilai risiko | US-05 |

**Modul Kepatuhan dan Ketahanan Data**

| ID | Aktivitas | Penjelasan | ID User Story |
| :--- | :--- | :--- | :--- |
| A22 | Mengantrekan dan Mengirimkan Data ke SATUSEHAT | Sistem menyusun data kunjungan yang telah ditutup menjadi bundel berstandar HL7 FHIR, memasukkannya ke antrean sinkronisasi, lalu mengirimkannya ketika koneksi tersedia | US-06 |
| A23 | Mengekspor Bundel Data untuk Pengiriman Manual | Sistem menyimpan antrean sinkronisasi sebagai berkas apabila koneksi tidak kunjung tersedia, agar dapat diunggah dari lokasi lain yang berjaringan | US-06 |
| A24 | Menjalankan Pencadangan Basis Data Terjadwal | Sistem membuat salinan basis data secara otomatis menurut jadwal, menyimpannya pada media terpisah, dan memperingatkan pengguna bila pencadangan gagal | US-07 |

Aktivitas A01 sampai A14 membentuk alur utama pelayanan rawat jalan yang divisualisasikan pada Gambar 1 di Subbab 3.4. Adapun A15 sampai A24 dijalankan di luar alur pelayanan pasien, yaitu pada pengelolaan persediaan obat, tindak lanjut daftar pantau, penyusunan laporan, dan pemenuhan kewajiban pelaporan elektronik. Alur pelaporan dan sinkronisasinya digambarkan pada Gambar 2.

## 3.4 Model Proses Bisnis

Diagram berikut memodelkan proses bisnis pelayanan rawat jalan di puskesmas ketika dijalankan dengan bantuan SEHATI. Diagram disusun dalam bentuk *swimlane activity diagram* dengan lima lajur: empat lajur untuk seluruh aktor manusia yang didefinisikan pada Subbab 3.1 (Petugas Administrasi, Perawat, Dokter, Apoteker) dan satu lajur untuk sistem perangkat lunak.

Ada tiga titik dalam alur ini yang menunjukkan peningkatan dibandingkan proses manual maupun hibrida pada Subbab 1.2:

1. **Pencarian data pasien dilakukan oleh sistem**, menggantikan pencarian berkas fisik di rak (menutup kesenjangan G-01 dan G-02).
2. **Evaluasi ambang risiko dilakukan otomatis** setelah tanda vital dimasukkan, sehingga penandaan pasien berisiko tidak lagi bergantung pada kejelian petugas (menutup kesenjangan G-03 dan G-04). Inilah titik yang membedakan SEHATI dari sistem informasi puskesmas lain, karena G-03 dan G-04 tetap terbuka bahkan pada fasilitas yang sudah terdigitalisasi.
3. **Validasi stok obat terjadi saat resep disusun**, bukan setelah pasien mengantre di apotek, sehingga penggantian obat dilakukan tanpa pasien perlu bolak-balik (menutup kesenjangan G-05).

Pada akhir alur, sistem memperbarui rekam medis dan data rekapitulasi secara langsung, sehingga tidak diperlukan lagi pengetikan ulang di akhir bulan (menutup kesenjangan G-06).

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

1. **Pendaftaran.** Petugas Administrasi menerima pasien dan meminta NIK atau kartu berobat. Sistem mencari data pasien pada basis data (A01). Bila pasien belum terdaftar, petugas mendaftarkannya dan sistem membuatkan rekam medis elektronik baru (A02). Selanjutnya petugas membuat data kunjungan, dan sistem menerbitkan nomor antrean poli (A03).
2. **Skrining.** Perawat memanggil pasien sesuai antrean (A04), mencatat keluhan awal dan hasil pengukuran tanda vital (A05). Sistem mengevaluasi nilai tersebut terhadap ambang klinis dan riwayat pasien, lalu menandai kunjungan sebagai berisiko bila ambangnya terlampaui (A06).
3. **Pemeriksaan.** Dokter membuka rekam medis pasien beserta riwayat kunjungan dan penanda risiko yang dihasilkan sistem (A08), melakukan pemeriksaan, mencatat diagnosis dan tindakan (A09), lalu menyusun resep elektronik (A10). Sistem memeriksa ketersediaan stok setiap obat yang diresepkan (A11). Bila stok tidak mencukupi, dokter mengganti obat pada saat itu juga. Bila mencukupi, sistem meneruskan resep ke antrean pelayanan farmasi (A13).
4. **Farmasi.** Apoteker menyiapkan obat, menyerahkannya kepada pasien, dan menjelaskan aturan pakai.
5. **Penutupan.** Sistem menandai resep sebagai telah diserahkan, memotong stok obat, dan menutup kunjungan (A14), lalu memasukkan data kunjungan tersebut ke antrean sinkronisasi SATUSEHAT (A22).

### Alur pelaporan dan sinkronisasi

Diagram kedua memodelkan aktivitas yang berjalan di luar alur pelayanan pasien, yaitu penyusunan laporan dan pemenuhan kewajiban pelaporan elektronik. Di sinilah **Petugas Administrasi** menjalankan perannya sebagai pengelola sistem, dan di sinilah mekanisme *store-and-forward* yang menjadi nilai unik kedua SEHATI bekerja.

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
8. United Nations. *Sustainable Development Goal 3: Ensure healthy lives and promote well-being for all at all ages*. https://sdgs.un.org/goals/goal3

9. Badan Kebijakan Pembangunan Kesehatan, Kementerian Kesehatan Republik Indonesia. *Wajib Integrasi SATUSEHAT, Kemenkes Desak Percepatan RME di Fasyankes*. Memuat data 34.463 fasyankes terintegrasi per 27 Oktober 2025 serta kendala infrastruktur dan kesiapan sumber daya manusia. https://www.badankebijakan.kemkes.go.id/wajib-integrasi-satu-sehat-kemenkes-desak-percepatan-rme-di-fasyankes/

**Solusi dan standar teknis pembanding**

10. Kementerian Kesehatan Republik Indonesia. *SATUSEHAT Platform: Dokumentasi HL7 FHIR*. https://satusehat.kemkes.go.id/platform/docs/id/fhir/
11. Direktorat Jenderal Pelayanan Kesehatan, Kementerian Kesehatan Republik Indonesia. *ASRI (Aplikasi Sistem RME Indonesia)*, aplikasi rekam medis elektronik gratis bagi Tempat Praktik Mandiri Dokter dan Dokter Gigi. https://yankes.kemkes.go.id/asri
12. Kementerian Kesehatan Republik Indonesia. *Penyedia Sistem RME*, daftar sistem rekam medis elektronik termasuk SIKDA Generik untuk puskesmas. https://satusehat.kemkes.go.id/platform/system-rme-list
13. *Analisis Implementasi Sistem Informasi Kesehatan Daerah (SIKDA) Generik Guna Menunjang Efektivitas Rekam Medis Elektronik di UPTD Puskesmas Campaka*. J-REMI: Jurnal Rekam Medik dan Informasi Kesehatan. https://publikasi.polije.ac.id/j-remi/article/view/3956

**Perkakas**

14. Diagram UML: https://www.drawio.com/, https://staruml.io/
