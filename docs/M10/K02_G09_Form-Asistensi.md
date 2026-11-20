# Form Asistensi

## Tugas Besar IF2150 - Rekayasa Perangkat Lunak

| Informasi | Keterangan |
| --- | --- |
| **Hari** | Senin |
| **Tanggal** | 16/11/2026 |
| **Kelas** | K02 |
| **Nomor Kelompok** | G09 |
| **Nama Kelompok** | Cumlaude |
| **Nama Perangkat Lunak** | SEHATI (Sistem Elektronik Pelayanan Kesehatan Terintegrasi) |
| **Dokumen** | K02_G09_HIUPL.md (Tugas 10 - Hasil Implementasi dan Uji Perangkat Lunak) |

### Anggota Kelompok

| NIM | Nama |
| --- | --- |
| 13525008 | Malik Arsyafiandra Madani |
| 13525032 | Ahmad Humam Isma |
| 13525044 | Steven Vanako |
| 13525071 | Muhammad Adnan Kurniawan |
| 13525074 | Axeleon Justin Algianto |
| 13525110 | Fachry Azriel Fajdwani |

### Catatan

| Catatan |
| --- |
| 1. Asistensi membahas isi HIUPL (Tugas 10): sampul dengan logo, identitas, dan tabel daftar perubahan, lalu BAB 1 sampai BAB 5. BAB 1 dan BAB 2 diambil dari milestone sebelumnya lalu disesuaikan: istilah baru (TC, UT, INT, *boundary*, *coverage*) dijelaskan, aturan penomoran ditambah pola ID pengujian, referensi minimal SKPL, APL, DPPL, dan Implementasi (T9) beserta dokumentasi *framework*, *library*, dan alat uji, dan setiap gambar diberi narasi. |
| 2. BAB 3 mendokumentasikan hasil implementasi: lingkungan (perangkat keras, OS, bahasa dan versi, *library*, DBMS, alat pengembangan, *version control*, alat uji), modul beserta nama file, antarmuka dengan ID I-XX yang sesuai DPPL, dan file lain. |
| 3. Setiap kelas DPPL diklasifikasikan Mendalam, Sedang, atau Tidak perlu menurut ciri kelasnya (logika bisnis, perhitungan, validasi, algoritma, integrasi eksternal, keamanan), bukan menurut seberapa penting kelas itu. Kelas Mendalam diuji pada setiap operasi publik dengan minimal 3 *test case* (normal, *boundary*, dan negatif) dan *coverage* minimal 70%. Kelas Sedang minimal 2 *test case* (normal dan negatif) dan boleh lewat uji integrasi. Kelas Tidak perlu cukup diberi alasan. |
| 4. Pengujian unit dan integrasi dilakukan otomatis, sedangkan use case dan sebagian KNF diuji manual dengan tangkapan layar. Uji integrasi memakai komponen nyata (bukan *mock*) untuk Controller ke Model, aplikasi ke basis data, dan aplikasi ke sistem eksternal. Setiap skenario normal dan alternatif SKPL minimal punya satu UCXX-TCYY, dan setiap KNF diuji dengan target terukur, metode dan alat, serta hasil aktual. Bukti setiap TC dilampirkan di Subbab 4.6, dan BAB 5 memetakan setiap KF ke KNF, use case, kelas, file, dan *test case*. |
| 5. Tanya jawab: uji integrasi sebaiknya memakai basis data uji terpisah atau basis data yang di-reset, dan data tiruan diperbolehkan. *Coverage* yang kurang ditambah dengan menambah *test case*. Kelas yang berubah dari DPPL diuji sesuai implementasi terakhir dan perubahannya dicatat. KNF yang tidak dapat diukur atau diwujudkan tetap dicantumkan beserta justifikasinya. |
| 6. Tenggat draf Selasa, 16 November 2026 pukul 21.00 WIB, dan tenggat final Jumat, 20 November 2026 pukul 21.00 WIB. |


## Dokumentasi

<!-- ![](./assets/foto-asistensi.jpg) -->
<p align="center">
  <img src="./assets/foto-asistensi.jpg" width="100%">
</p>

<p align="center">
  <i>Gambar 1. Dokumentasi kegiatan asistensi.</i>
</p>
