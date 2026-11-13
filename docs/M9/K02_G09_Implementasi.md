<h1>
IF2150 REKAYASA PERANGKAT LUNAK
<br>
TUGAS 9
<br>
IMPLEMENTASI PERANGKAT LUNAK
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


**Deadline:** Jumat, 13 November 2026, 21.00 WIB (diundur dari Rabu, 11 November 2026, 21.50 WIB)

**Asistensi akbar:** Senin, 2 November 2026, 19.10-19.45 WIB


---

# BAGIAN B: Laporan Implementasi

## B.1 Perubahan dari DPPL

Catat semua perbedaan antara perancangan (DPPL) dan implementasi akhir. Jika tidak ada perubahan, tuliskan "Tidak ada perubahan".

Tabel B.1. Perubahan dari DPPL

| No | Bagian DPPL | Rancangan Awal | Implementasi Akhir | Alasan |
| :--- | :--- | :--- | :--- | :--- |
| 1 | C38 SkriningEntity | Atribut idSkrining, idKunjungan, keluhanAwal, waktu | Ditambah atribut `tandaVital` dan `getTandaVital()` | Subbab 5.38 menyebut tanda vital dipegang sebagai referensi, tetapi atributnya tidak ada di Tabel 4.1, sedangkan `simpanSkrining()` membutuhkannya |
| 2 | C39 TandaVitalEntity, C59 Q-C59-03 dan Q-C59-05 | Tidak ada atribut waktu, `s.waktu` dipilih tetapi tidak disimpan | Ditambah atribut `waktu: LocalDateTime` dengan getter dan setter, diisi dari `s.waktu` pada Q-C59-03 dan Q-C59-05 | Grafik tren UC05 (KF08) memakai waktu pengukuran sebagai sumbu-x dan `RekamMedisForm.seriTren` hanya memegang `List<TandaVitalEntity>`. Atas usul Adnan pada review #29 |
| 3 | C20 SkriningManager.muatAntrean(), panggilPasien(), lewatiPasien() | Antrean skrining tampil menurut `nomorAntrean` | Antrean tampil menurut urutan `AntreanEntity.daftarEntri`, dibatasi kunjungan hari ini (`aktifHariIni()`). `lewatiPasien()` hanya menerima kunjungan berstatus "sedang diskrining" | `nomorAntrean` berlaku per poli, sehingga baris teratas dapat berbeda dari pasien yang dipanggil dan pasien yang dilewati tidak terlihat pindah. Atas usul Adnan pada review #31 |
| 4 | C20 SkriningManager.simpanSkrining() | Algo-C20-01 tidak mengeluarkan kunjungan dari antrean | Setelah status menjadi "siap diperiksa dokter", kunjungan dikeluarkan dari antrean skrining lewat `AntreanEntity.keluarkan()` dan `simpanAntrean()` dalam transaksi yang sama | Tanpa ini antrean terus membesar dan penyegaran 10 detik membaca semua entri lama. Tabel 5.37 sudah menyediakan `keluarkan()` untuk UC04. Atas usul Adnan pada review #31 |
| 5 | C20 SkriningManager | `validasiRentang` private | Package-private | Agar dapat diuji unit tanpa refleksi |
| 6 | C20 SkriningManager | Tidak ada operasi identitas pasien | Tambahan `labelPasien(List<KunjunganEntity>)` | Form tidak boleh membaca DataBoundary, sedangkan I-04 dan I-05 menampilkan nama, RM, dan poli |
| 7 | C20 SkriningManager | `panggilPasien` memakai `AntreanEntity.ambilTerdepan()` | Pasien terdepan dicari dari antrean dan status kunjungan di manager | `ambilTerdepan()` mengembalikan KunjunganEntity dari daftar ID, padahal entity tidak dapat memuat dari basis data |
| 8 | C21 RisikoManager | `evaluasiRisiko(kunjungan, tv)` | Ditambah overload package-private yang menerima `Connection` | Penanda dan entri daftar pantau harus satu transaksi dengan penyimpanan skrining |
| 9 | C51 DaftarPantauEntity | Empat atribut pada Tabel 4.1 | Ditambah atribut tampilan `tanggalPemicu`, `namaPasien`, `nomorTelepon` | `saringDaftar()` perlu tanggal pemicu (Q-C60-01) dan identitas pasien, sedangkan entity tidak memuatnya |
| 10 | C12 DaftarPantauForm | `tampilkanDaftar()` memanggil `RisikoManager.muatEntriAktif()` | Memanggil `PantauManager.saringDaftar(null, null, null)` | Tabel membutuhkan nama dan telepon pasien yang dimuat `saringDaftar()` |
| 11 | C26 PantauManager.perbaruiStatus() | `perbaruiStatus(idEntri, status)` | `perbaruiStatus(dataPantau, entri, status)`, private | Pembaruan status berjalan dalam transaksi `catatTindakLanjut()` yang sama dengan koneksi milik Manager, dan entri sudah dimuat untuk pengecekan hasil upaya. Atas usul Adnan pada review #32 |
| 12 | C12 DaftarPantauForm | Atribut `penyaring` tanpa penggunaan yang dijelaskan | `penyaring` berisi ringkasan saringan aktif dan ditampilkan pada keterangan di atas tabel | Agar perawat melihat saringan yang sedang berlaku. Atas usul Adnan pada review #32 |
| 13 | C34 RekamMedisEntity (5.34) | Hanya `tambahEntri()`. Penampung entri baru tidak punya akses | Ditambah `getEntriBaru()` dan `kosongkanEntriBaru()` | `simpanRekamMedis()` perlu membaca entri baru lalu mengosongkannya setelah tersimpan (#38) |
| 14 | C57 `simpanPasien()` (5.57) | Nomor rekam medis selalu diisi Manager | Bila kosong, boundary mengambil dari `nomorRMBerikutnya()` | Mencegah pasien tersimpan tanpa nomor rekam medis (#38) |
| 15 | C57 dan C58 (5.57, 5.58) | Hanya SQLState 23505 dan 23514 yang menjadi `IllegalArgumentException` | Ditambah 23502 dan 23503 (isian wajib kosong, pasien atau poli tidak ada) | Pesan ke petugas lebih jelas daripada galat umum (#38) |
| 16 | C57 `ambilPasien`, C58 `kunciPoli` dan `ambilKunjungan` | Tidak menyebut hasil bila data tidak ada | Mengembalikan `null` | Sama dengan `kunjunganAktif` pada DPPL dan pola `ambilSkrining` (#38) |
| 17 | Pengantar BAB 6, butir 1 (bilah aplikasi) | Bilah ada pada setiap layar selain I-01 | Bilah tampil selama sesi aktif, termasuk pada dasbor di I-01. Layar masuk sebelum login tetap tanpa bilah | I-01 juga memuat dasbor, yang tanpa bilah tidak punya tombol Keluar dan indikator koneksi (KF22) (#28) |
| 18 | Tabel 4.1 | 64 kelas | Ditambah lima kelas pendukung: `Navigasi`, `BilahAplikasi`, `TabelPintasan`, `PanelPeringatan`, `PenandaKolom` | Merealisasikan kesepakatan tampilan pada pengantar BAB 6 (#28) |
| 19 | Kesepakatan 1 BAB 5 | `idPenggunaSesi` adalah `idPengguna` milik `SesiEntity` dari `AutentikasiManager`, tanpa tempat penyimpanan | Kelas pendukung `sehati.common.SesiAktif` memegang sesi aktif dan menyediakan `idPenggunaSesi()` | `Manager` tidak menyimpan state dan tidak memanggil `Manager` lain, sehingga butuh satu tempat bersama untuk sesi di komputer tersebut (#36) |
| 20 | Subbab 5.30, C30 | Konstruktor `PenggunaEntity baru(nama, hash, peran)` hanya disebut pada teks | Konstruktor `PenggunaEntity(String, String, String)` berstatus "aktif" dan `idPengguna` 0 | Merealisasikan teks Subbab 5.30 (#36) |
| 21 | Subbab 5.32, C32 | `AuditEntity` tidak memiliki operasi pengubah | Setter dan konstruktor kosong dari stub dihapus, atribut menjadi `final` | Catatan audit tidak dapat diubah dari kode aplikasi (KNF05) (#36) |
| 22 | C17 `bukaSesi` | `bukaSesi(p: PenggunaEntity)` | `bukaSesi(p, dataAkun)` | Sesi disimpan pada koneksi yang sama dengan autentikasi (kesepakatan 1 BAB 5) (#44) |
| 23 | C17 AutentikasiManager | Tidak ada operasi penutupan sesi | Ditambah `tutupSesi(habisWaktu: boolean)` dan konstanta `BATAS_DIAM_MENIT` | Algo-C31-01 meminta sesi disimpan lewat `DataAkunBoundary.simpanSesi()`, sedangkan `Form` tidak boleh memanggil `DataBoundary` (#44) |
| 24 | C01 `bukaLayar` | Pemeriksaan hak akses di `LoginForm` | Pemeriksaan di `Navigasi.buka()`, `LoginForm.bukaLayar()` menampilkan pesannya | Berlaku untuk semua layar, bukan hanya dari dasbor (KNF05) (#44) |
| 25 | `SesiAktif` (di luar DPPL) | Hanya `SesiEntity` | Ikut menyimpan peran | `SesiEntity` tidak punya atribut peran, sedangkan Subbab 5.1 meminta peran dibawa bersama sesi (#44) |
| 26 | C29 `muatDataMaster` | `muatDataMaster(): void` | Mengembalikan record `DataMaster` (akun, poli, obat, ambang) | Hasil harus sampai ke `KonfigurasiForm` untuk ditampilkan (#46) |
| 27 | C29 KonfigurasiManager | Hanya `simpanDataMaster(AmbangRisikoEntity)` | Ditambah `simpanPoli(idPoli, nama)` dan `tambahObat(ObatEntity)` | Tidak ada bagian sistem yang bisa mendaftarkan jenis obat atau poli baru. `simpanPoli()` pada C58 sudah dirancang "dari data master" tetapi belum ada pemanggilnya (#46) |
| 28 | C29 | Penjadwal tidak dirinci | Ditambah `jadwalkanPencadangan()`, aktif bila `PENCADANGAN_JAM` diisi | Agar hanya komputer server yang menjalankan pencadangan terjadwal (#46) |
| 29 | Algo-C29-01 | Panjang sandi dicek untuk akun baru | Juga dicek saat sandi akun lama diganti | Kriteria #21: sandi minimal 8 karakter (#46) |
| 30 | C56 DataAkunBoundary | Q-C56-01 sampai 08 | Ditambah Q-C56-09 `semuaPengguna()` | Daftar akun pada I-15-03 (#46) |
| 31 | C61 DataFarmasiBoundary | Q-C61-01 sampai 15 | Ditambah Q-C61-17 `tambahObat()` (stok awal 0) | Pendaftaran jenis obat baru (#46) |
| 32 | Algo-C64-01 | Sandi lewat berkas `.pgpass` | Sandi lewat variabel lingkungan `PGPASSWORD` pada proses `pg_dump` | Tidak perlu menyiapkan `.pgpass` per komputer. Sandi tetap tidak muncul di argumen perintah (#46) |
| 33 | Algo-C64-01 `jalankanPgDump` | Nama arsip `sehati_` + `yyyyMMdd-HHmm` + `.dump` | Format waktu `yyyyMMdd-HHmmss` | Pencadangan terjadwal dan manual pada menit yang sama tidak saling menimpa (#46) |
| 34 | UC12 normal langkah 2 | Arsip dibuat "dalam bentuk terenkripsi" | Arsip `pg_dump --format=custom` hanya terkompresi, tidak terenkripsi (sama dengan Algo-C64-01) | Arsip disimpan di media cadangan milik puskesmas. Enkripsi butuh kunci yang bila hilang membuat cadangan tidak dapat dipulihkan (#46) |
| 35 | Aturan 5 Subbab 5.56 | Aplikasi masuk sebagai `sehati_app` | Pemilik basis data `sehati_admin`, aplikasi tetap `sehati_app`. Kata sandi `sehati_app` diambil dari `DB_PASSWORD` oleh `db/sandi-aplikasi.sh` | Pembatasan audit tidak berlaku bila aplikasi masuk sebagai pemilik tabel. Kata sandi tidak boleh tertulis di repo (#33) |
| 36 | Q-C58-07 | Keunikan nomor antrean dijamin `kunciPoli()` | Ditambah `UNIQUE (id_poli, tanggal, nomor_antrean)` | Pengaman kedua untuk KF23 (#33) |
| 37 | Algo-C60-01 | Satu jadwal tepat satu entri | Ditambah `UNIQUE` pada `daftar_pantau.id_jadwal` | Pengaman kedua (#33) |
| 38 | Q-C58-10, Q-C59-02, Q-C57-11 | Kunci primer tidak tertulis | Kunci primer pada `antrean_entri`, `tanda_vital`, `catatan_rekam_medis` | Setiap tabel butuh kunci primer (#33) |
| 39 | C46 ResepEntity (5.46) | Komposisi `ItemResepEntity` disebut di keterangan, tanpa nama atribut | Atribut `daftarItem: List<ItemResepEntity>` beserta getter dan setter | `DataFarmasiBoundary` perlu mengisi dan menyimpan item resep (Q-C61-10, Q-C61-14), dan `ApotekManager.potongStok()` membacanya (Algo-C24-02) (#37) |
| 40 | C25 `tambahStok` (5.25) | `tambahStok(idObat, jumlah)` | `tambahStok(idObat, jumlah, dataFarmasi)` | Memakai koneksi transaksi `catatPenerimaan()` (aturan 1 BAB 5), sama seperti `ResepManager.validasiStok()` (#41) |
| 41 | Algo-C25-03 `terbitkanPeringatan` | Tanggal kedaluwarsa dalam format bawaan | Format `dd-MM-yyyy`, sama dengan I-11 | Konsisten dengan tanggal lain di layar (#41) |
| 42 | C24 ApotekManager (5.24) | Lima operasi untuk layar | Ditambah `muatPasienResep()`, `muatDataObat()`, dan `muatBetsResep()` | I-09 dan I-10 menampilkan nama pasien, nama obat, dan bets FEFO, sedangkan `layaniResep()` hanya mengembalikan `ResepEntity` (#42) |
| 43 | C61 (5.61) | Q-C61-01 sampai Q-C61-15 | Ditambah `ambilPasienResep()` dengan Q-C61-16 (JOIN resep, kunjungan, pasien) | Identitas pasien seluruh antrean diambil dalam satu kueri, bukan satu kueri per resep (#42) |
| 44 | Algo-C24-01 | Resep yang diserahkan tetap ada di `antrean_entri` | Resep yang diserahkan ikut dikeluarkan dari antrean apotek | `antrean_entri` apotek tidak terus bertambah, dan sama dengan perlakuan resep yang dikembalikan (#42) |
| 45 | UC07 alternatif 1 | `kembalikanKeDokter()` memakai `ambilResep()` | Memakai `kunciResep()` dalam transaksi | Pengembalian tidak menimpa resep yang baru saja diserahkan petugas lain (#42) |
| 46 | Algo-C24-02 | Tidak ada pemeriksaan sisa bets | Ditolak bila jumlah bets kurang dari saldo obat | Menjaga saldo obat tetap sama dengan jumlah bets (Subbab 5.48) (#42) |
| 47 | C24 `susunBundel` | `susunBundel(idKunjungan)` | Menerima kunjungan, resep, obat, dan boundary transaksi, lalu memakai `PenyusunBundelFHIR` | Memakai koneksi transaksi `serahkanObat()`, dan rincian format FHIR dipisah agar `ApotekManager` tidak menjadi *god class* (#42) |
| 48 | C10 PenyerahanObatForm (5.10) | Lima operasi | Ditambah `aturKonteks()` | Menerima identitas pasien, nama obat, dan penerima pesan hasil dari I-09 (#42) |
| 49 | C34 RekamMedisEntity | Atribut `idRekamMedis`, `daftarDiagnosis`, `riwayatObat` | Ditambah `pasien`, `skriningHariIni`, dan `penandaAktif: List<PenandaRisikoEntity>`, hanya untuk tampilan | `susunRingkasan()` harus menyajikan data diri dan penanda dalam satu ringkasan (KF09), tetapi tipe kembaliannya tidak punya tempat untuk keduanya (#39) |
| 50 | C22 PemeriksaanManager, C58 DataKunjunganBoundary | Tidak ada cara memilih pasien di I-06 | `muatKunjunganSiapPeriksa(): Map<KunjunganEntity, PasienEntity>` dan `kunjunganSiapPeriksa(tanggal)` dengan kueri baru Q-C58-13 | Dasbor membuka I-06 tanpa kunjungan, dan DPPL tidak punya antrean poli. Dokter memilih dari kunjungan hari ini yang sudah diskrining (#39) |
| 51 | C07 PemeriksaanForm | Tidak ada operasi penerima kunjungan | `buka(kunjungan, pasien)` | I-07 dibuka dari I-06 dan perlu tahu kunjungan yang diperiksa (#39) |
| 52 | C07, C22, C59 | Formulir I-07 selalu kosong saat dibuka | Ditambah `muatPemeriksaan()`, `muatDiagnosis()`, dan `ambilDiagnosis()` dengan kueri baru Q-C59-18. `buka()` memuat draf dan mengunci kolom bila sudah final | Isi lama tertimpa formulir kosong, dan alternatif "rekam medis sudah terkunci" tidak terwujud (review #39) (#39) |
| 53 | C22 `jadwalkanKontrol` | Selalu menyisipkan jadwal baru | Jadwal pasien yang sama pada tanggal yang sama tidak dibuat dua kali. Ditambah `validasiTanggalKontrol()` yang dipanggil I-07 sebelum menyimpan | Simpan berulang membuat jadwal ganda, dan tanggal salah baru ditolak setelah pemeriksaan tersimpan (review #39) (#39) |
| 54 | Algo-C06-01 | Sumbu-x memakai urutan kunjungan karena `TandaVitalEntity` tidak membawa tanggal | Sumbu-x memakai tanggal `TandaVitalEntity.waktu` | Atribut `waktu` sudah ditambahkan di #29, sesuai kriteria #14 (#39) |
| 55 | Algo-C22-01 | Kode ICD-10 kosong diperiksa setelah `ambilPemeriksaan()` | Diperiksa sebelum membuka koneksi | Penolakan masukan tidak perlu menunggu basis data. Hasilnya sama (#39) |
| 56 | C23 `validasiStok` | `-validasiStok(item): boolean` | `validasiStok(item, dataFarmasi): boolean`, package-private | Satu operasi Manager memakai satu koneksi (aturan 1 Subbab 5.56), dan uji `validasiStok` diminta issue #15 (#40) |
| 57 | Algo-C23-01, Algo-C23-02, Algo-C08-01 | Stok diperiksa per item, dan `tambahItem()` memanggil `validasiStok()` | Stok dibandingkan dengan total jumlah per obat di `kirimKeApotek()` dan `ResepForm`. Panggilan `validasiStok()` yang hasilnya tidak terpakai dihapus dari `tambahItem()` | Dua baris obat yang sama bisa melebihi stok bila diperiksa per item (#40) |
| 58 | C08 ResepForm | Tidak ada cara memperbaiki resep di layar | Tombol Hapus Item | Alternatif 1 UC06 meminta dokter memperbaiki resep yang stoknya kurang (#40) |
| 59 | C08 ResepForm | `tampilkanFormulir(kunjungan)` | Ditambah `tampilkanFormulir(kunjungan, pasien)` untuk identitas I-08-02 | `KunjunganEntity` tidak membawa nama pasien (#40) |
| 60 | C18 PasienManager | `terbitkanNomorRM()` tanpa argumen, `private` (Tabel 5.18.1) | `terbitkanNomorRM(DataPasienBoundary dataPasien)` | Nomor rekam medis hanya terbit sekali per transaksi lewat koneksi yang sama (#45) |
| 61 | Algo-C18-02 `perbaruiPasien()` | Hasil `ambilPasien()` langsung dipakai, tanpa memeriksa `idPasien` 0 atau data yang tidak ditemukan | Menolak `idPasien` 0 dan pasien yang tidak ditemukan dengan `IllegalArgumentException` | Pasien yang tidak ada tidak boleh menghasilkan galat teknis (#45) |
| 62 | C36 `ambilNomorBerikutnya` | `hariIni` tidak diperiksa | Menolak `hariIni` null dengan `IllegalArgumentException` | Penolakan masukan tidak valid sebelum nomor antrean diterbitkan (#51) |
| 63 | Algo-C15-01 SATUSEHATGateway | Pengiriman selalu ke SATUSEHAT, tanpa mode simulasi | Mode simulasi menjadi bawaan bila `SATUSEHAT_URL` kosong. Pengiriman dianggap sukses dengan kode `simulasi-<id>` | Aplikasi dapat dijalankan tanpa alamat SATUSEHAT (#49) |
| 64 | C15 `mintaToken()` | Token diminta dengan identitas klien dari berkas konfigurasi | Membaca ulang `SATUSEHAT_TOKEN` dari environment | Konfigurasi belum punya kredensial OAuth client (#49) |
| 65 | Algo-C24-01 `serahkanObat` | Tidak memeriksa status pemeriksaan | Menolak bila pemeriksaan kunjungan belum dikunci | Kunjungan ditutup saat obat diserahkan, jadi rekam medis yang belum final tidak bisa dikunci lagi (#55) |
| 66 | Algo-C22-02 `kunciRekamMedis` | Hanya mengunci dan menandai "selesai diperiksa" | Bila kunjungan tanpa resep, kunjungan ditutup dan bundel FHIR disimpan dalam transaksi yang sama | Selain apotek tidak ada langkah yang menutup kunjungan, sehingga kunjungan tanpa obat tidak pernah tersinkron (KF14) (#55) |
| 67 | Algo-C23-02 `kirimKeApotek` | Tidak memeriksa status kunjungan | Menolak kunjungan berstatus "selesai" | Resep untuk kunjungan yang sudah ditutup tidak bisa dilayani apotek (#55) |
| 68 | Q-C58-13 `kunjunganSiapPeriksa` | Hanya status "siap diperiksa dokter" | Ditambah kunjungan "selesai diperiksa" yang resepnya dikembalikan dan belum diganti | Resep yang dikembalikan harus kembali ke layar dokter (UC07 alternatif 1) (#55) |
| 69 | C61 | Q-C61-01 sampai 17 | Ditambah Q-C61-18 `ambilResepKunjungan()` | Dipakai `kunciRekamMedis()` dan alasan pengembalian di I-08 (#55) |
| 70 | C23 ResepManager | Tidak ada | Ditambah `alasanDikembalikan(idKunjungan)` | Menampilkan catatan apotek pada I-08 (#55) |
| 71 | C27 LaporanManager | Tidak ada | Ditambah `periksaKoneksi()` | Pemeriksaan ringan tanpa basis data untuk indikator bilah (KF22) (#55) |
| 72 | Bundel FHIR (`PenyusunBundelFHIR`) | `MedicationDispense` tanpa diagnosis | Ditambah `Condition` ICD-10 dan `MedicationRequest` | SKPL 2.4 butir 6 (#55) |
| 73 | Jejak audit | Hanya masuk, pasien, kunjungan, kunci rekam medis, persediaan, konfigurasi | Ditambah skrining, simpan pemeriksaan, kirim resep, penyerahan obat, pengembalian resep, tindak lanjut pantau. Pelaku dari `SesiAktif.idPenggunaAudit()` (0 bila tanpa sesi) | KF16: setiap perubahan data dicatat (#55) |
| 74 | I-02 tanggal lahir | `DatePicker` bawaan (ikut locale OS) | Konverter `dd-MM-yyyy` | Sama dengan layar lain dan tidak melempar galat pada teks salah (#55) |
| 75 | BAB 6, bilah aplikasi (KF22) | Logo teks, judul, pengguna, indikator koneksi, Keluar | Ditambah tombol Kembali dan tanda logo | Kembali sebelumnya hanya lewat Esc, sehingga pengguna tetikus tidak punya jalan pulang (#56) |
| 76 | I-01 Masuk dan Dasbor | Kartu masuk dan dasbor tampil berdampingan | Kartu masuk disembunyikan setelah masuk, dasbor tampil penuh dengan deskripsi tiap layar | Kartu masuk kosong tidak berguna setelah sesi aktif (#56) |
| 77 | I-01-07 Kartu menu (`LoginForm.DAFTAR_MENU`) | Kartu Kunjungan Pasien tampil di dasbor Petugas Administrasi | Kartu dihapus. Kunjungan dibuka dari I-02 lewat tombol Buka Kunjungan | I-03 butuh pasien terpilih (UC03 «include» UC02). Dibuka dari dasbor, pilihan poli kosong dan tombol tidak bisa dipakai |
| 78 | Algo-C03-01 | Tiket baru dan kunjungan aktif dibedakan dari status "menunggu skrining" | Form memanggil `KunjunganManager.kunjunganAktif()` lebih dulu, lalu nama poli diambil dari kunjungan itu | Kunjungan aktif yang belum diskrining tampil sebagai tiket baru dengan nama poli dari pilihan petugas (UC03 alternatif 1) |
| 79 | I-11-08, Algo-C25-02 | Tanggal kedaluwarsa harus setelah tanggal terima | Juga harus setelah hari ini | SKPL UC08 alternatif 2 menolak tanggal kedaluwarsa yang sudah lewat, termasuk bila tanggal terima dimundurkan |
| 80 | C17 AutentikasiManager, C56 (Q-C56-10) | Menonaktifkan akun hanya menutup baris sesi di basis data | Ditambah `AutentikasiManager.sesiDiputus()` dan `DataAkunBoundary.sesiMasihAktif()`. `Navigasi` memeriksanya tiap menit dan mengakhiri sesi yang sudah ditutup | SKPL UC12 alternatif 3 meminta sesi aktif pengguna yang dinonaktifkan ikut terputus |
| 81 | I-02, C18 PasienManager, C57 (Q-C57-12) | Tabel hasil kosong sampai kata kunci minimal 3 karakter | Saat kata kunci kurang dari 3 karakter, tabel menampilkan 50 pasien terakhir didaftarkan lewat `PasienManager.pasienTerbaru()` | Petugas langsung melihat pasien yang baru didaftarkan. Dibatasi 50 baris agar KNF01 tetap terpenuhi |
| 82 | C12 DaftarPantauForm, C26 `saringDaftar()` | `tampilkanDaftar()` menyebut penyaring status tindak lanjut, tetapi `saringDaftar(jenis, mulai, akhir)` tidak menerimanya | Ditambah `pilihStatus` pada I-12 dan parameter `statusTindakLanjut` pada `saringDaftar()` | KF18 meminta penyaring status tindak lanjut. Pilihannya hanya dua status terbuka karena Q-C60-01 tidak memuat entri yang sudah selesai |
| 83 | C21 `evaluasiRisiko()` | Setiap risiko yang terpenuhi selalu menyisipkan entri daftar pantau | Penanda tetap terbit per kunjungan, tetapi entri tidak disisipkan bila pasien masih punya entri terbuka untuk risiko yang sama (`sudahDipantau()`) | Pasien yang berisiko pada beberapa kunjungan muncul berkali-kali dan dihubungi lebih dari sekali |
| 84 | C22 `kunciRekamMedis()`, C45 `tandaiSelesai()`, C59 | `tandaiSelesai()` (Subbab 5.45) tidak punya pemanggil | `kunciRekamMedis()` menandai selesai jadwal kontrol pasien yang tanggalnya sudah tiba (kueri baru Q-C59-19 `jadwalJatuhTempo()`) dan menutup entri "kunjungan ulang" miliknya dalam transaksi yang sama | Pasien yang sudah diperiksa saat kontrol tidak lagi tampil di daftar pantau sebagai belum ditindaklanjuti. Dijalankan saat rekam medis dikunci, bukan saat kunjungan dibuka di loket, agar pasien yang akhirnya tidak hadir di skrining tetap ada di daftar pantau (review Axeleon di PR #62) |
| 85 | Algo-C29-01 `simpanAkun()` | Peran akun mana pun dapat diubah | Perubahan peran akun yang sedang dipakai ditolak | Petugas Administrasi dapat menurunkan perannya sendiri sehingga tidak ada lagi yang dapat membuka I-15, sama dengan alasan larangan menonaktifkan akun sendiri |
| 86 | C13 LaporanForm, C27 `hitungRekapitulasi()`, C62 Q-C62-01 sampai Q-C62-03 | Rekapitulasi hanya menurut rentang tanggal | Ditambah pilihan poli layanan pada I-13 dan parameter `idPoli` (null berarti semua poli) pada `hitungRekapitulasi()` dan `agregasiKunjungan()`. Ketiga kueri ditambah `AND (CAST(? AS INTEGER) IS NULL OR k.id_poli = ?)`. `LaporanEntity` ditambah atribut `namaPoli` agar nama berkas ekspor dan judul PDF memuat poli yang disaring | SKPL UC10 langkah 1 meminta penyaring poli layanan, tetapi DPPL belum memuatnya |
| 87 | C14 SinkronisasiForm, C28 `muatStatus()`, C63 | Tabel I-14 hanya memuat bundel tertunda dan gagal kirim | `muatStatus()` menambahkan 10 bundel terkirim terbaru lewat kueri baru Q-C63-04 `ambilTerkirimTerakhir()`. Jumlah tertunda tidak ikut menghitung bundel terkirim. Tabel dimuat ulang setelah Sinkronkan Sekarang agar riwayatnya tetap tampil (review Axeleon di PR #62) | SKPL UC11 langkah 1 meminta riwayat pengiriman terakhir |
| 88 | Kesepakatan BAB 5, KNF05 | Hak akses hanya diperiksa saat layar dibuka (`Navigasi.buka()`) | Ditambah `SesiAktif.wajibPeran()` yang dipanggil setiap operasi tulis `Manager` sebelum menyentuh basis data. Pencadangan terjadwal tidak diperiksa karena berjalan tanpa sesi | KNF05 meminta otorisasi berbasis peran pada setiap akses data, bukan hanya pada menu |
| 89 | C01 LoginForm (I-01-08) | Dasbor administrasi hanya menampilkan peringatan bila pencadangan gagal | Dasbor administrasi selalu menampilkan waktu, status, dan lokasi pencadangan terakhir, dan peringatan tetap muncul bila gagal | KF21 meminta informasi pencadangan terakhir tampil di dasbor administrasi |
| 90 | C09 AntreanResepForm, C24 ApotekManager, C61 | Antrean resep memuat waktu, pasien, obat, dan status | Ditambah kolom "Poli / Antrean" dari kueri baru Q-C61-19 `ambilAsalResep()` lewat `ApotekManager.muatAsalResep()` | SKPL UC07 langkah 1 meminta nomor antrean dan poli asal pada antrean resep |
| 91 | C07 PemeriksaanForm `kirimPemeriksaan()`, Algo-C22-02 `kunciRekamMedis()` | Teks diagnosis yang diketik bebas dibuang diam-diam, dan rekam medis dapat dikunci tanpa diagnosis | Simpan ditolak dengan pesan "Pilih kode ICD-10 dari daftar saran" selama kolom diagnosis berisi teks yang bukan kode terpilih. `kunciRekamMedis()` menolak dengan "Diagnosis ICD-10 belum diisi" bila pemeriksaan belum punya diagnosis | SKPL UC05 alternatif 2 dan KF10 meminta diagnosis disimpan sebagai kode ICD-10 standar. Temuan uji mandiri anggota |

## B.2 Bukti Penerapan Best Practices

Tabel B.2. Bukti Best Practices

| Praktik | Bukti | Keterangan |
| :--- | :--- | :--- |
| Branching | [Daftar branch](https://github.com/angseleong/IF2150-RPL-K02-G09/branches), [graph commit](https://github.com/angseleong/IF2150-RPL-K02-G09/network) | Satu branch fitur per issue (`feat/…`, `fix/…`, `test/…`, `docs/…`, `chore/…`) yang dibuat dari `develop` dan digabung lewat PR dengan *merge commit*. `main` hanya menerima rilis dan diberi tag `v9.0`. Terdapat 37 branch di remote. |
| Semantic commit | [Riwayat commit `develop`](https://github.com/angseleong/IF2150-RPL-K02-G09/commits/develop) | Sejak "init M9" (28-10-2026) sampai 13-11-2026 ada 192 commit non-merge dengan awalan `docs` 105, `feat` 47, `fix` 21, `chore` 9, dan `test` 7. Tiga commit tidak berawalan (`add ui`, `upadate laporan akhir`, dan satu commit lama dari M1 yang ikut terbawa). Contoh: `feat(pasien): isi DataPasienBoundary`, `fix(skrining): isi AntreanEntity, urutan antrean, batas Lewati, dan keluarkan setelah skrining`. |
| Track tasks | [Issues](https://github.com/angseleong/IF2150-RPL-K02-G09/issues?q=is%3Aissue) | 26 issue (#1 sampai #26): 3 *chore* (kerangka, skema, rilis), 7 fondasi dan boundary, 12 use case, 2 uji, dan 2 dokumen. Pembagian per anggota: Axeleon 5, Steven 5, Humam 4, Adnan 4, Malik 4, Fachry 4. Label `status: in-progress`, `status: review`, dan `status: done` dipakai pada PR, dan PR menutup issue lewat `Closes #n`. |
| Pull request dan review | [PR #29](https://github.com/angseleong/IF2150-RPL-K02-G09/pull/29), [PR #39](https://github.com/angseleong/IF2150-RPL-K02-G09/pull/39), [PR #46](https://github.com/angseleong/IF2150-RPL-K02-G09/pull/46) | Sampai 13-11-2026 ada 34 PR (#27 sampai #61) yang digabung ke `develop`, ditambah PR perbaikan hasil audit dan PR rilis ke `main`. Pasangan tinjauan yang disepakati: Axeleon ditinjau Steven, Steven ditinjau Humam, Humam ditinjau Adnan, Adnan ditinjau Malik, Malik ditinjau Fachry, dan Fachry ditinjau Axeleon. Dari 34 PR itu, 15 PR memiliki tinjauan tercatat di GitHub, misalnya #29 (Adnan meminta perubahan atribut `waktu`, galat `simpanPenanda`, dan pesan galat, lalu diperbaiki), #39 (ditinjau Malik), dan #46 (ditinjau Steven). 19 PR lainnya digabung tanpa tinjauan tercatat, dan 8 di antaranya digabung oleh pembuatnya sendiri tanpa tinjauan (#36, #52, #56, #57, #58, #59, #60, #61), sebagian besar karena dikejar tenggat. Ini menyimpang dari catatan asistensi butir 4, sehingga PR rilis ke `main` wajib ditinjau anggota lain sebelum digabung. |
| Pengujian | [`src/tests/`](https://github.com/angseleong/IF2150-RPL-K02-G09/tree/develop/src/tests), [Actions](https://github.com/angseleong/IF2150-RPL-K02-G09/actions) | JUnit 5, 58 kelas uji (termasuk kelas bersarang). Terakhir dijalankan 13-11-2026 setelah perbaikan audit: 328 test, 0 gagal, 0 galat. Tanpa `DB_UJI_URL` (seperti di CI) 86 uji basis data dilewati, dan dengan PostgreSQL uji seluruhnya berjalan tanpa ada yang dilewati. Yang diuji: entity (Algo-Cxx), validasi dan transaksi Manager, kueri boundary lewat JDBC pengganti, serta alur utama dan konkurensi dengan PostgreSQL (`AlurUtamaTest`, `KonkurensiTest`). Jalankan `cd src && mvn test`. CI GitHub Actions menjalankan `mvn -B test` pada setiap PR. |

### B.2.1 Profiling dan Optimasi

Pilih minimal satu fitur yang diprofil.

| Informasi | Keterangan |
| :--- | :--- |
| Fitur / proses yang diprofil | Rekapitulasi laporan periodik (Q-C62-01, Q-C62-02, Q-C62-03) dan pencarian pasien (Q-C57-01/KNF01). |
| Alat | PostgreSQL 14 dengan `EXPLAIN (ANALYZE, BUFFERS)` dan data sintetis 50.000 rekam medis dari `src/db/seed_besar.sql`. |
| Temuan (sebelum) | Pada rentang satu tahun penuh, Q-C62-01 37,052 ms, Q-C62-02 23,276 ms, dan Q-C62-03 167,205 ms. Ketiganya memakai sequential scan karena seluruh 50.000 baris `kunjungan` masuk ke rentang. Pada rentang satu bulan, Q-C62-01 3,689 ms, Q-C62-02 3,920 ms, dan Q-C62-03 46,145 ms, sudah memakai `idx_kunjungan_tanggal` atau index unik `(id_poli, tanggal, nomor_antrean)`. Pencarian pasien dengan kata kunci normal 0,283-0,485 ms memakai trigram. Kasus terburuk `%9998%` dan `%RM-9%` yang cocok dengan 50.000 baris tetap 24,529-39,334 ms. Semua operasi jauh di bawah batas KNF01 2 detik. |
| Perbaikan | Tidak ada perubahan indeks permanen. Indeks `diagnosis(kode_icd10)` sempat diuji, tetapi planner tetap memilih sequential scan. Waktu Q-C62-03 justru 169,897 ms pada rentang setahun dan 53,967 ms pada rentang sebulan, sehingga indeks tersebut dibatalkan. |
| Hasil (sesudah) | Tidak ada perubahan skema. Indeks yang sudah ada memadai. `idx_kunjungan_tanggal` dipakai untuk rentang sempit, sedangkan rentang setahun penuh memang lebih efisien dengan sequential scan. Indeks trigram menangani pencarian pasien. Operasi terlama 167,205 ms, masih jauh di bawah 2 detik. |

Jika tidak ada perbaikan yang perlu dilakukan, jelaskan hasil profiling dan alasan kenapa fitur tersebut sudah memadai.

## B.3 Kendala dan Catatan Penting

Tuliskan kendala yang ditemui, fitur yang belum sempurna (*known issues*), serta hal yang perlu diketahui asisten saat menjalankan atau menilai aplikasi (misalnya akun uji khusus, urutan menjalankan, atau batasan).

**Menjalankan dan menilai aplikasi (seluruh anggota):**
- Urutan menjalankan, isi `.env`, dan basis data ada di `src/README.md`: `docker compose up -d` dari folder `src/`, lalu `mvn javafx:run`. Akun uji `admin`, `perawat`, `dokter`, dan `farmasi` memakai kata sandi `sehati123`. Akun `perawat.nonaktif` dipakai untuk menguji penolakan akun nonaktif.
- Uji berbasis data (86 dari 328 test) dilewati bila `DB_UJI_URL` kosong. Variabel ini harus memakai akun pemilik basis data, karena uji membersihkan tabel yang tidak boleh diubah akun aplikasi.
- Tanpa `SATUSEHAT_URL`, sinkronisasi berjalan dalam mode simulasi dan tidak mengirim ke sandbox SATUSEHAT. Pengiriman nyata belum diuji karena tidak ada kredensial OAuth client.
- Arsip pencadangan `pg_dump` hanya terkompresi dan tidak terenkripsi (Tabel B.1). Ekspor bundel saat luring terenkripsi dengan `EKSPOR_FRASA_SANDI`.
- Pada profiling, seluruh operasi terukur di bawah batas 2 detik KNF01 pada 50.000 rekam medis (B.2.1).

**Audit dan uji menyeluruh (13-11-2026, Adnan):**
- Seluruh use case diperiksa ulang terhadap SKPL dan DPPL. Hasilnya diperbaiki dan dicatat pada Tabel B.1 nomor 82 sampai 91: penyaring status daftar pantau (KF18), entri daftar pantau ganda, jadwal kontrol yang tidak pernah selesai (KF12), larangan mengubah peran akun sendiri, penyaring poli laporan (UC10), riwayat pengiriman (UC11), pemeriksaan peran di setiap operasi tulis `Manager` (KNF05), informasi pencadangan di dasbor (KF21), kolom poli serta antrean pada antrean resep (UC07), dan penolakan diagnosis yang bukan kode ICD-10 (UC05 alternatif 2).
- Ke-15 layar dibuka dengan keempat akun uji pada basis data berisi seed, termasuk alur skrining, pemeriksaan, resep, dan penyerahan obat. Tangkapan layarnya diperiksa satu per satu. Tidak ada dialog galat, dan tampilan yang terpotong sudah dirapikan.
- Belum diuji: pengiriman nyata ke sandbox SATUSEHAT (tidak ada kredensial OAuth client), pemasangan di Windows (KNF10, pengujian dilakukan di Linux), dan pemulihan dari arsip `pg_dump` di komputer lain.

**Modul skrining dan pemantauan (Humam):**
- Kueri C59 dan C60 sudah dijalankan ke PostgreSQL oleh reviewer (Adnan) pada review #29, #31, dan #32. Alur UC04 dan UC09 dijalankan pada basis data yang sama, dan layar I-04, I-05, dan I-12 sudah dibuka tanpa galat.
- Dua komputer yang membuka daftar pantau bersamaan sebelumnya dapat bertabrakan pada `UNIQUE id_jadwal`. Sejak perbaikan audit, Q-C60-02 memakai `ON CONFLICT DO NOTHING`, dan `KonkurensiTest` menguji lima komputer yang membuka daftar pantau serentak. Komputer yang kalah cepat melihat entri tersebut pada penyegaran berikutnya.
- Entri antrean skrining hanya dibaca untuk kunjungan hari ini. Pasien "tidak hadir" dari hari sebelumnya tidak ikut ditampilkan.
- JDK yang tersedia di perangkat Humam adalah 17, sehingga pengujian lokal memakai `-Dmaven.compiler.release=17`. Pada JDK 21 tidak ada perbedaan perilaku.

## B.4 Kontribusi Anggota

Tabel B.4. Kontribusi Anggota

| NIM | Nama | Modul / Fitur yang Dikerjakan | Persentase Kontribusi |
| :--- | :--- | :--- | :--- |
| 13525008 | Malik Arsyafiandra Madani | Farmasi (UC07, UC08) | 16.67% |
| 13525032 | Ahmad Humam Isma | Skrining dan pemantauan (UC04, UC09) | 16.67% |
| 13525044 | Steven Vanako | Loket (UC02, UC03) | 16.67% |
| 13525071 | Muhammad Adnan Kurniawan | Pemeriksaan dan resep (UC05, UC06) | 16.67% |
| 13525074 | Axeleon Justin Algianto | Akses dan administrasi (UC01, UC12) | 16.67% |
| 13525110 | Fachry Azriel Fajdwani | Pelaporan dan integrasi (UC10, UC11) | 16.67% |

## B.5 Traceability Kelas Perancangan ke Implementasi

Pemetaan dari **setiap kelas perancangan** pada DPPL (Tabel 4.1) ke wujud implementasinya. Bentuk implementasi bebas (kelas, fungsi, komponen, *route/handler*, *stored procedure*, skrip, dsb.), tetapi tiap kelas perancangan harus muncul pada tabel ini. Jika implementasi berupa kelas dengan nama yang sama, tetap dicantumkan.

Tabel B.5. Traceability Kelas Perancangan ke Implementasi

| ID Kelas | Nama Kelas Perancangan | Bentuk Implementasi | Lokasi (File dan Nama) | Keterangan |
| :--- | :--- | :--- | :--- | :--- |
| C01 | LoginForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/LoginForm.java` - `LoginForm`, `LoginForm.fxml` | Algo-C01-01. Cek hak akses Algo-C01-02 dijalankan di `Navigasi.buka()` agar berlaku untuk semua layar |
| C02 | PasienForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/PasienForm.java` - `PasienForm`, `src/src/main/resources/sehati/fxml/PasienForm.fxml` | `kirimPencarian()` memakai `PauseTransition` 250 ms dan thread latar (Algo-C02-01). `kirimDataPasien()` mewujudkan Algo-C02-02. Pintasan Ctrl+S/F/N terpasang. Tombol Buka Kunjungan aktif untuk pasien tersimpan dan membuka C03 lewat `Navigasi` (B.1). |
| C03 | KunjunganForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/KunjunganForm.java` - `KunjunganForm`, `src/src/main/resources/sehati/fxml/KunjunganForm.fxml` | `buka()` memuat daftar poli di thread latar, `kirimPermintaanKunjungan()` mewujudkan Algo-C03-01: tombol dinonaktifkan selama permintaan, hasil status "menunggu skrining" menampilkan tiket, lainnya kunjungan aktif. Tiket berisi nama poli, nomor, tanggal, dan tombol cetak tiket yang memakai `javafx.print.PrinterJob`. |
| C04 | AntreanSkriningForm | Kelas JavaFX controller dengan FXML | `src/src/main/java/sehati/presentation/AntreanSkriningForm.java`, `src/src/main/resources/sehati/fxml/AntreanSkriningForm.fxml` | Antrean menyegar tiap 10 detik, F2 memanggil, F3 melewati. Akses data lewat `SkriningManager`. |
| C05 | SkriningForm | Kelas JavaFX controller dengan FXML | `src/src/main/java/sehati/presentation/SkriningForm.java`, `src/src/main/resources/sehati/fxml/SkriningForm.fxml` | Ctrl+S menyimpan, kolom bermasalah ditandai merah, penanda risiko memakai `PanelPeringatan`. |
| C06 | RekamMedisForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/RekamMedisForm.java`, `src/src/main/resources/sehati/fxml/RekamMedisForm.fxml` - `RekamMedisForm` | Atribut `idPasienTampil` dan `seriTren` dipakai `tampilkanGrafikTren()`. Kelima operasi DPPL diwujudkan apa adanya: `tampilkanGrafikTren()` memakai `LineChart` dengan tanggal kunjungan pada sumbu-x (Algo-C06-01), dan `aturModeBacaSaja()` menonaktifkan Periksa Pasien bagi peran selain Dokter. Tambahan: daftar pasien siap diperiksa di sisi kiri (B.1). |
| C07 | PemeriksaanForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/PemeriksaanForm.java`, `src/src/main/resources/sehati/fxml/PemeriksaanForm.fxml` - `PemeriksaanForm` | Atribut `isianAnamnesis` dan `kataKunciDiagnosis`. `kirimKataKunciDiagnosis()` dijalankan `PauseTransition` 300 ms, `kirimHasilPemeriksaan()` mewujudkan Algo-C07-01 dan terhubung ke Ctrl+S, `kirimFinal()` meminta konfirmasi lalu memanggil `kunciRekamMedis()`. Tambahan: `buka(kunjungan, pasien)` menerima kunjungan dari I-06 dan memuat draf tersimpan (B.1). |
| C08 | ResepForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/ResepForm.java`, `src/src/main/resources/sehati/fxml/ResepForm.fxml` - `ResepForm` | Atribut `isianItem` menampung item sampai dikirim, dan `indikatorStok` menyimpan indikator terakhir. `kirimItem()` mewujudkan Algo-C08-01, dan tombol Kirim ke Apotek aktif hanya bila semua item cukup stoknya. Tambahan: pencarian obat menyaring daftar `muatDataObat()`, tombol Hapus Item untuk memperbaiki resep, dan `tampilkanFormulir(kunjungan, pasien)` (B.1). |
| C09 | AntreanResepForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/AntreanResepForm.java`, `src/src/main/resources/sehati/fxml/AntreanResepForm.fxml` - `AntreanResepForm` | Atribut `daftarTampil`. `tampilkanAntrean()` dimuat ulang `Timeline` tiap 10 detik selama layar tampil, dan `pilihResep()` dipanggil tombol Layani, klik ganda, atau Enter. Tambahan: nama pasien dan obat dimuat sekali per penyegaran lewat `muatPasienResep()` dan `muatDataObat()` (B.1). |
| C10 | PenyerahanObatForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/PenyerahanObatForm.java`, `src/src/main/resources/sehati/fxml/PenyerahanObatForm.fxml` - `PenyerahanObatForm` | Atribut `rincianTampil` dan `catatanPengembalian`. `kirimKonfirmasi()` mewujudkan Algo-C10-01 dan terhubung ke Ctrl+Enter. Penolakan tampil di `PanelPeringatan`, lalu rincian dimuat ulang. Tambahan: `aturKonteks()` menerima identitas pasien, nama obat, dan penerima pesan dari I-09 (B.1). |
| C11 | PersediaanForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/PersediaanForm.java`, `src/src/main/resources/sehati/fxml/PersediaanForm.fxml` - `PersediaanForm` | Atribut `isianPenerimaan` dan `daftarPeringatan`. `kirimPenerimaan()` mewujudkan Algo-C11-01 dan terhubung ke tombol Simpan serta Ctrl+S. `tampilkanPesanKesalahan()` memetakan nama kolom dari pesan `catatPenerimaan()` ke `PenandaKolom`, dan `tampilkanPeringatan()` memakai `PanelPeringatan` berikon (KNF08). Pekerjaan basis data berjalan di `Task`. |
| C12 | DaftarPantauForm | Kelas JavaFX controller dengan FXML | `src/src/main/java/sehati/presentation/DaftarPantauForm.java`, `src/src/main/resources/sehati/fxml/DaftarPantauForm.fxml` | Penyaring, tabel, dan panel tindak lanjut pada satu layar. Esc menutup panel lebih dulu. |
| C13 | LaporanForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/LaporanForm.java`, `src/src/main/resources/sehati/fxml/LaporanForm.fxml` - `LaporanForm` | Algo-C13-01 pada `kirimPermintaanEkspor()`: meminta folder bila belum diberikan, lalu mengekspor di thread latar lewat `Task`. `kunciTombolEkspor()` menonaktifkan tombol PDF dan CSV. |
| C14 | SinkronisasiForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/SinkronisasiForm.java`, `src/src/main/resources/sehati/fxml/SinkronisasiForm.fxml` - `SinkronisasiForm` | Algo-C14-01 pada `kirimPermintaanSinkron()`. Status koneksi dan jumlah bundel tertunda diperbarui lewat `tampilkanStatus()`. `tampilkanTombolEkspor()` muncul saat luring. |
| C15 | SATUSEHATGateway | Kelas Java | `src/src/main/java/sehati/dataaccess/SATUSEHATGateway.java` - `SATUSEHATGateway` | Algo-C15-01 dan Algo-C15-02 lewat `java.net.http.HttpClient`. `periksaKoneksi()` dan `kirimBundel()` mengembalikan `ResponsKirim`. Mode simulasi bila `SATUSEHAT_URL` kosong (Tabel B.1). |
| C16 | KonfigurasiForm | Kelas Java + FXML | `src/src/main/java/sehati/presentation/KonfigurasiForm.java` - `KonfigurasiForm`, `KonfigurasiForm.fxml` | Algo-C16-01. Pencadangan manual berjalan di `Task` agar layar tidak beku |
| C17 | AutentikasiManager | Kelas Java | `src/src/main/java/sehati/business/control/AutentikasiManager.java` - `AutentikasiManager` | Algo-C17-01 dan Algo-C17-02, ditambah `tutupSesi()` untuk Keluar dan batas diam 15 menit |
| C18 | PasienManager | Kelas Java | `src/src/main/java/sehati/business/control/PasienManager.java` - `PasienManager` | `cariPasien()`, `daftarkanPasien()` (Algo-C18-01), `perbaruiPasien()` (Algo-C18-02), `validasiNIK()`. Satu koneksi per operasi. Audit lewat `SesiAktif.idPenggunaSesi()`. `terbitkanNomorRM(DataPasienBoundary)` agar nomor terbit sekali per transaksi. |
| C19 | KunjunganManager | Kelas Java | `src/src/main/java/sehati/business/control/KunjunganManager.java` - `KunjunganManager` | Tanpa atribut, sesuai DPPL. `bukaKunjungan()` mewujudkan Algo-C19-01: kunjungan aktif dicek sebelum transaksi, lalu dalam satu transaksi (auto-commit mati, COMMIT/ROLLBACK) menerbitkan nomor dengan `kunciPoli()` + `PoliEntity.ambilNomorBerikutnya()` + `simpanPoli()` (Algo-C19-02), memeriksa rekam medis lewat `DataPasienBoundary.ambilRekamMedis()`, menyimpan kunjungan status "menunggu skrining", memasukkan entri ke antrean jenis "skrining", dan mencatat audit. Penolakan `IllegalArgumentException`/`IllegalStateException`. |
| C20 | SkriningManager | Kelas | `src/src/main/java/sehati/business/control/SkriningManager.java` | Validasi rentang Algo-C20-02 berupa konstanta kelas. Tambahan `labelPasien()`. |
| C21 | RisikoManager | Kelas | `src/src/main/java/sehati/business/control/RisikoManager.java` | Inti penilaian risiko dipisah menjadi `jenisRisikoTerpenuhi()` agar dapat diuji tanpa basis data. |
| C22 | PemeriksaanManager | Kelas Java | `src/src/main/java/sehati/business/control/PemeriksaanManager.java` - `PemeriksaanManager` | Tanpa atribut, sesuai DPPL. Setiap operasi membuka koneksi lewat `KoneksiBasisData.buka()`. `simpanPemeriksaan()` mewujudkan Algo-C22-01, dan `kunciRekamMedis()` mewujudkan Algo-C22-02 dalam satu transaksi dengan pelaku audit dari `SesiAktif.idPenggunaSesi()`. Tambahan: `muatKunjunganSiapPeriksa()`, `muatPemeriksaan()`, `muatDiagnosis()`, dan `validasiTanggalKontrol()` (B.1). |
| C23 | ResepManager | Kelas Java | `src/src/main/java/sehati/business/control/ResepManager.java` - `ResepManager` | Tanpa atribut. `tambahItem()` mewujudkan Algo-C23-01. `kirimKeApotek()` mewujudkan Algo-C23-02: memeriksa ulang stok, lalu `simpanResep()` dan memasukkan resep ke antrean apotek dalam satu transaksi. `validasiStok()` menerima `DataFarmasiBoundary` agar satu operasi memakai satu koneksi, dan pada `kirimKeApotek()` memeriksa total jumlah per obat (B.1). |
| C24 | ApotekManager | Kelas Java | `src/src/main/java/sehati/business/control/ApotekManager.java` - `ApotekManager` | Tanpa atribut. `serahkanObat()` mewujudkan Algo-C24-01 dalam satu transaksi dengan `kunciResep()`, dan `potongStok()` mewujudkan Algo-C24-02 (FEFO). `susunBundel()` memakai `PenyusunBundelFHIR` untuk isi JSON FHIR R4. Tambahan: `muatPasienResep()`, `muatDataObat()`, `muatBetsResep()`, resep yang selesai ikut dikeluarkan dari antrean apotek, dan `kembalikanKeDokter()` memakai `kunciResep()` (B.1). |
| C25 | PersediaanManager | Kelas Java | `src/src/main/java/sehati/business/control/PersediaanManager.java` - `PersediaanManager` | Tanpa atribut. `catatPenerimaan()` mewujudkan Algo-C25-01 dalam satu transaksi dengan pelaku audit dari `SesiAktif.idPenggunaSesi()`, `validasiPenerimaan()` mewujudkan Algo-C25-02, dan `terbitkanPeringatan()` mewujudkan Algo-C25-03. `tambahStok()` menerima `DataFarmasiBoundary` agar memakai koneksi transaksi (B.1). |
| C26 | PantauManager | Kelas | `src/src/main/java/sehati/business/control/PantauManager.java` | Penyaringan murni dipisah menjadi `saring()`; `catat()` dapat diuji dengan pengganti DataBoundary. |
| C27 | LaporanManager | Kelas Java | `src/src/main/java/sehati/business/control/LaporanManager.java` - `LaporanManager` | `hitungRekapitulasi()` memvalidasi rentang tanggal, lalu `ekspor()` memanggil `DataLaporanBoundary`. Tiap operasi membuka satu koneksi, dan versi package-private yang menerima boundary dipakai uji. Tambahan `periksaKoneksi()` (Tabel B.1). |
| C28 | SinkronisasiManager | Kelas Java | `src/src/main/java/sehati/business/control/SinkronisasiManager.java` - `SinkronisasiManager` | Algo-C28-01 pada `kirimSemua()`: saat luring, bundel ditahan. Algo-C28-02 pada `eksporBundel()`. Atribut `daring` dan `jumlahTertunda` dibaca form lewat `isDaring()` dan `getJumlahTertunda()`. Gateway disuntikkan lewat `Supplier` agar dapat diuji. |
| C29 | KonfigurasiManager | Kelas Java | `src/src/main/java/sehati/business/control/KonfigurasiManager.java` - `KonfigurasiManager` | Algo-C29-01 sampai C29-03. `muatDataMaster()` mengembalikan `DataMaster`, ditambah `simpanPoli()`, `tambahObat()`, dan `jadwalkanPencadangan()` (Tabel B.1) |
| C30 | PenggunaEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PenggunaEntity.java` - `PenggunaEntity` | Algo-C30-01 lewat jBCrypt, ditambah konstruktor akun baru berstatus "aktif" |
| C31 | SesiEntity | Kelas Java | `src/src/main/java/sehati/business/entity/SesiEntity.java` - `SesiEntity` | Algo-C31-01. Sesi yang sedang aktif dipegang `sehati.common.SesiAktif` |
| C32 | AuditEntity | Kelas Java | `src/src/main/java/sehati/business/entity/AuditEntity.java` - `AuditEntity` | Algo-C32-01. Tanpa setter agar catatan tidak dapat diubah (KNF05) |
| C33 | PasienEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PasienEntity.java` - `PasienEntity` | Algo-C33-01 pada `perbarui()`; `idPasien` dan `nomorRekamMedis` tidak ikut berubah. |
| C34 | RekamMedisEntity | Kelas | `src/src/main/java/sehati/business/entity/RekamMedisEntity.java` | Algo-C34-01 diisi. Ditambah `getEntriBaru()` dan `kosongkanEntriBaru()` agar `simpanRekamMedis()` dapat membaca dan mengosongkan penampung entri baru. |
| C35 | KunjunganEntity | Kelas Java | `src/src/main/java/sehati/business/entity/KunjunganEntity.java` - `KunjunganEntity` | `perbaruiStatus()` mewujudkan Algo-C35-01 (validasi status dan kunci status selesai), dan `aktifHariIni()` menentukan kunjungan aktif hari ini. |
| C36 | PoliEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PoliEntity.java` - `PoliEntity` | `ambilNomorBerikutnya()` mewujudkan algoritma nomor antrean berurutan per poli per hari dengan mulai ulang saat hari berganti (KF03). |
| C37 | AntreanEntity | Kelas Java | `src/src/main/java/sehati/business/entity/AntreanEntity.java` - `AntreanEntity` | Algo-C37-01: `ambilTerdepan()` dan `pindahkanKeBelakang()` diisi untuk UC04. `tambahkan()` dari #40 dan `keluarkan()` dari #42. `keluarkan()` menolak entri yang tidak ada. |
| C38 | SkriningEntity | Kelas | `src/src/main/java/sehati/business/entity/SkriningEntity.java` | Ditambah atribut `tandaVital` dan `getTandaVital()` (Tabel B.1). |
| C39 | TandaVitalEntity | Kelas | `src/src/main/java/sehati/business/entity/TandaVitalEntity.java` | Algo-C39-01. Ditambah atribut `waktu` (Tabel B.1). |
| C40 | AmbangRisikoEntity | Kelas | `src/src/main/java/sehati/business/entity/AmbangRisikoEntity.java` | Algo-C40-01. |
| C41 | PenandaRisikoEntity | Kelas | `src/src/main/java/sehati/business/entity/PenandaRisikoEntity.java` | Konstanta `STATUS_AKTIF` dan `STATUS_DICABUT` ditambahkan. |
| C42 | PemeriksaanEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PemeriksaanEntity.java` - `PemeriksaanEntity` | Atribut sesuai Tabel 4.1 dengan getter dan setter. `kunci()` menolak penguncian kedua dengan `IllegalStateException`. |
| C43 | DiagnosisEntity | Kelas Java | `src/src/main/java/sehati/business/entity/DiagnosisEntity.java` - `DiagnosisEntity` | Atribut sesuai Tabel 4.1. `ringkas()` mengembalikan "kode - deskripsi". |
| C44 | KodeICD10Entity | Kelas Java | `src/src/main/java/sehati/business/entity/KodeICD10Entity.java` - `KodeICD10Entity` | Atribut sesuai Tabel 4.1. `cocok()` mewujudkan Algo-C44-01 tanpa membedakan huruf besar dan kecil. |
| C45 | JadwalKontrolEntity | Kelas Java | `src/src/main/java/sehati/business/entity/JadwalKontrolEntity.java` - `JadwalKontrolEntity` | Atribut sesuai Tabel 4.1, ditambah konstanta `STATUS_TERJADWAL` dan `STATUS_SELESAI`. `tandaiSelesai()` menolak jadwal yang tidak berstatus terjadwal. |
| C46 | ResepEntity | Kelas Java | `src/src/main/java/sehati/business/entity/ResepEntity.java` - `ResepEntity` | Atribut sesuai Tabel 4.1, dengan komposisi `daftarItem`. `perbaruiStatus()` mewujudkan Algo-C46-01 dengan konstanta `STATUS_MENUNGGU`, `STATUS_SELESAI_DILAYANI`, dan `STATUS_DIKEMBALIKAN`. |
| C47 | ItemResepEntity | Kelas Java | `src/src/main/java/sehati/business/entity/ItemResepEntity.java` - `ItemResepEntity` | Atribut sesuai Tabel 4.1. `cukupStok()` mengembalikan `stok >= jumlah`. |
| C48 | ObatEntity | Kelas Java | `src/src/main/java/sehati/business/entity/ObatEntity.java` - `ObatEntity` | Atribut sesuai Tabel 4.1. `kurangiStok()`, `tambahStok()`, dan `diBawahAmbang()` mewujudkan Algo-C48-01 dan hanya mengubah objek di memori; penulisan ke basis data lewat `DataFarmasiBoundary.simpanStok()`. |
| C49 | BetsObatEntity | Kelas Java | `src/src/main/java/sehati/business/entity/BetsObatEntity.java` - `BetsObatEntity` | Atribut sesuai Tabel 4.1. `kurangi()` dan `mendekatiKedaluwarsa()` mewujudkan Algo-C49-01; bets yang sudah lewat tanggalnya ikut terhitung menjelang kedaluwarsa. |
| C50 | PenerimaanObatEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PenerimaanObatEntity.java` - `PenerimaanObatEntity` | Atribut sesuai Tabel 4.1, dengan agregasi `daftarBets`. `totalJumlah()` mewujudkan Algo-C50-01. |
| C51 | DaftarPantauEntity | Kelas | `src/src/main/java/sehati/business/entity/DaftarPantauEntity.java` | Algo-C51-01. Ditambah atribut tampilan `tanggalPemicu`, `namaPasien`, `nomorTelepon` (Tabel B.1). |
| C52 | TindakLanjutEntity | Kelas | `src/src/main/java/sehati/business/entity/TindakLanjutEntity.java` | `terhubung()`. |
| C53 | LaporanEntity | Kelas Java | `src/src/main/java/sehati/business/entity/LaporanEntity.java` - `LaporanEntity` | Atribut sesuai Tabel 4.1 dengan getter dan setter. `kosong()` menandai rekapitulasi tanpa data pada rentang itu. |
| C54 | BundelFHIREntity | Kelas Java | `src/src/main/java/sehati/business/entity/BundelFHIREntity.java` - `BundelFHIREntity` | Algo-C54-01: `tandaiTerkirim()` dan `tandaiGagal()` menolak bundel yang sudah terkirim |
| C55 | PencadanganEntity | Kelas Java | `src/src/main/java/sehati/business/entity/PencadanganEntity.java` - `PencadanganEntity` | `berhasil()` hanya untuk status "berhasil" |
| C56 | DataAkunBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataAkunBoundary.java` - `DataAkunBoundary` | Q-C56-01 sampai Q-C56-08 sebagai konstanta `Q_C56_xx`, ditambah Q-C56-09 `semuaPengguna()` untuk daftar akun I-15 dan Q-C56-10 `sesiMasihAktif()` untuk memutus sesi akun yang dinonaktifkan (Tabel B.1) |
| C57 | DataPasienBoundary | Kelas | `src/src/main/java/sehati/dataaccess/DataPasienBoundary.java` | Q-C57-01 s.d. Q-C57-11 sebagai konstanta `Q_C57_xx`, ditambah Q-C57-12 `pasienTerbaru()` (Tabel B.1). Algo-C57-01 pada `polaPencarian()`. Pengerjaan Humam menggantikan Steven (#5). |
| C58 | DataKunjunganBoundary | Kelas | `src/src/main/java/sehati/dataaccess/DataKunjunganBoundary.java` | Q-C58-01 s.d. Q-C58-12 sebagai konstanta `Q_C58_xx`, ditambah Q-C58-13 `kunjunganSiapPeriksa()` (Tabel B.1). Algo-C58-01 pada `ambilAntrean()` dan `simpanAntrean()`. Pengerjaan Humam menggantikan Steven (#5). |
| C59 | DataKlinisBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataKlinisBoundary.java` - `DataKlinisBoundary` | Atribut `koneksi` diterima lewat konstruktor dari `Manager`. Bagian pemeriksaan: `cariKodeICD10`, `simpanPemeriksaan`, `ambilPemeriksaan`, `simpanJadwalKontrol`, dan `ambilJadwalKontrol` menjalankan konstanta `Q_C59_11` sampai `Q_C59_17` lewat `PreparedStatement`. `simpanPemeriksaan` mewujudkan Algo-C59-02 dan melempar `IllegalStateException` bila baris lama sudah terkunci. Tambahan: `ambilDiagnosis()` dengan kueri Q-C59-18 untuk memuat draf dan `jadwalJatuhTempo()` dengan kueri Q-C59-19 untuk menyelesaikan jadwal kontrol (B.1). |
| C60 | DataPantauBoundary | Kelas | `src/src/main/java/sehati/dataaccess/DataPantauBoundary.java` | Q-C60-01 s.d. Q-C60-06 sebagai konstanta `Q_C60_xx`, Algo-C60-01. Q-C60-02 memakai `ON CONFLICT (id_jadwal) DO NOTHING` agar dua komputer tidak membuat entri jadwal yang sama (B.3). |
| C61 | DataFarmasiBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataFarmasiBoundary.java` - `DataFarmasiBoundary` | Atribut `koneksi` diterima lewat konstruktor dari `Manager`. Sepuluh operasi publik menjalankan konstanta `Q_C61_01` sampai `Q_C61_15` lewat `PreparedStatement`. `ambilObat`, `ambilBets`, dan `kunciResep` memakai `FOR UPDATE` (KF23, KNF11). `simpanPenerimaan` mewujudkan Algo-C61-01 dan melempar `IllegalArgumentException("nomorBets")` untuk bets ganda. `simpanStok`, `simpanPenerimaan`, dan `simpanResep` memakai pola `transaksiSendiri` (aturan 1 Subbab 5.56). Item resep dibaca ke `ResepEntity.daftarItem`. Tambahan UC07: `ambilPasienResep()` menjalankan `Q_C61_16` agar identitas pasien seluruh antrean diambil dalam satu kueri (B.1). Ditambah Q-C61-17 `tambahObat()` untuk data master I-15, Q-C61-18 `ambilResepKunjungan()`, dan Q-C61-19 `ambilAsalResep()` untuk kolom poli dan antrean I-09 (Tabel B.1) |
| C62 | DataLaporanBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataLaporanBoundary.java` - `DataLaporanBoundary` | Q-C62-01 sampai Q-C62-04 sebagai konstanta `Q_C62_xx`, dengan penyaring poli opsional pada Q-C62-01 sampai Q-C62-03 (Tabel B.1). Algo-C62-01 membaca ketiga agregasi dalam satu transaksi hanya-baca REPEATABLE READ. Algo-C62-02 menulis CSV atau PDF (Apache PDFBox) secara atomik. |
| C63 | DataSinkronisasiBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataSinkronisasiBoundary.java` - `DataSinkronisasiBoundary` | Q-C63-01 s.d. Q-C63-03 sebagai konstanta `Q_C63_xx`, ditambah Q-C63-04 `ambilTerkirimTerakhir()` (Tabel B.1); `simpanBundel`, `ambilTertunda`, `perbaruiBundel` lewat `PreparedStatement`; `tulisArsipTerenkripsi` mewujudkan Algo-C63-01 (PBKDF2 600k + AES-GCM) |
| C64 | DataPencadanganBoundary | Kelas Java | `src/src/main/java/sehati/dataaccess/DataPencadanganBoundary.java` - `DataPencadanganBoundary` | Q-C64-01, Q-C64-02, Algo-C64-01, Algo-C64-02. Lokasi `pg_dump`, `pg_restore`, dan media diatur lewat `.env` |

Catatan:
- Satu kelas perancangan boleh diwujudkan oleh beberapa file/unit, dan satu file/unit boleh mewujudkan beberapa kelas perancangan. Tuliskan semuanya.
- Kelas perancangan yang **tidak diimplementasikan** atau **digabung/dipecah** tetap dicantumkan, dan alasannya dicatat di Bagian B.1.
