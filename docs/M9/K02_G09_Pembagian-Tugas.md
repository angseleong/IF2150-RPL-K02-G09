# Pembagian Tugas Implementasi SEHATI

Tugas 9 IF2150 Rekayasa Perangkat Lunak, K02 G09 Cumlaude.

**Deadline:** Rabu, 11 November 2026, 21.50 WIB, lewat tag release pada repo public.

**Aturan kerja:** [`K02_G09_Panduan-Kerja.md`](K02_G09_Panduan-Kerja.md). **Papan tugas:** [issue urut dari yang pertama](https://github.com/angseleong/IF2150-RPL-K02-G09/issues?q=is%3Aissue%20is%3Aopen%20sort%3Acreated-asc) dan [milestone per fase](https://github.com/angseleong/IF2150-RPL-K02-G09/milestones).

---

## 1. Mulai dari Sini

1. Baca [Panduan Kerja](K02_G09_Panduan-Kerja.md), terutama Subbab 1, 3, dan 7.
2. Temukan nama sendiri pada tabel Urutan Kerja (Subbab 2.1) dan buka issue paling kiri.
3. Beri label `status: in-progress` lalu buat branch sesuai nama yang tertulis di issue.

---

## 2. Anggota dan Modul

Setiap anggota memegang dua use case dari awal sampai akhir: layar, `Manager`, `Entity`, kueri, dan test.

| Anggota | GitHub | Modul | Use Case | Issue |
| :--- | :--- | :--- | :--- | :--- |
| Axeleon Justin Algianto | `angseleong` | Akses dan administrasi | UC01, UC12 | [#3](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/3), [#4](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/4), [#10](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/10), [#21](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/21), [#26](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/26) |
| Steven Vanako | `cepsssy` | Loket | UC02, UC03 | [#5](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/5), [#11](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/11), [#12](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/12), [#22](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/22), [#24](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/24) |
| Ahmad Humam Isma | `1smam` | Skrining dan pemantauan | UC04, UC09 | [#6](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/6), [#13](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/13), [#18](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/18), [#25](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/25) |
| Muhammad Adnan Kurniawan | `adnankurniawann` | Pemeriksaan dan resep | UC05, UC06 | [#1](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/1), [#7](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/7), [#14](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/14), [#15](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/15) |
| Malik Arsyafiandra Madani | `malikarsya` | Farmasi | UC07, UC08 | [#2](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/2), [#8](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/8), [#16](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/16), [#17](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/17) |
| Fachry Azriel Fajdwani | `rabsed1` | Pelaporan dan integrasi | UC10, UC11 | [#9](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/9), [#19](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/19), [#20](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/20), [#23](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/23) |

Pertukaran tugas boleh selama disepakati. Ubah *assignee* pada issue dan perbarui dokumen ini dalam PR.

### 2.1 Urutan Kerja per Anggota

Kerjakan dari kiri ke kanan. Tanggal adalah batas merge ke `develop`. Issue dalam satu kolom dikerjakan berurutan sesuai tulisan.

| Anggota | Rabu 4 Nov | Jumat 6 Nov | Minggu 8 Nov | Senin 9 Nov | Selasa 10 Nov |
| :--- | :--- | :--- | :--- | :--- | :--- |
| Adnan | [#1](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/1) (pukul 12.00) | [#7](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/7) | [#14](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/14), [#15](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/15) | Perbaikan bug | Perbaikan bug |
| Malik | [#2](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/2) | [#8](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/8) | [#16](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/16), [#17](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/17) | Perbaikan bug | Perbaikan bug |
| Axeleon | [#3](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/3), setelah #1 | [#4](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/4) | [#10](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/10), [#21](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/21) | Perbaikan bug | [#26](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/26) |
| Steven | Baca DPPL modul sendiri | [#5](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/5) | [#11](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/11), [#12](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/12) | [#22](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/22) | [#24](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/24) |
| Humam | Baca DPPL modul sendiri | [#6](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/6) | [#13](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/13), [#18](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/18) | Perbaikan bug | [#25](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/25) |
| Fachry | Baca DPPL modul sendiri | [#9](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/9) | [#19](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/19), [#20](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/20) | [#23](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/23) | Perbaikan bug |

---

## 3. Jadwal

| Fase | Batas merge ke `develop` | Hasil |
| :--- | :--- | :--- |
| 0. Fondasi | Rabu, 4 Nov 2026 | Kerangka dan stub 64 kelas (pukul 12.00), skema dan seed, cangkang aplikasi |
| 1. Lapisan data bersama | Jumat, 6 Nov 2026 | `DataBoundary` yang dipakai lintas modul |
| 2. Fitur | Minggu, 8 Nov 2026 | UC01 sampai UC12. Setelah ini *feature freeze* |
| 3. Integrasi | Senin, 9 Nov 2026 | Uji alur utama, uji konkurensi, profiling, perbaikan bug |
| 4. Finalisasi | Selasa, 10 Nov 2026 | README (12.00), laporan (18.00), tag `v9.0` (23.59) |
| Cadangan | Rabu, 11 Nov 2026 | Revisi saja. Tag terakhir paling lambat 20.00, deadline 21.50 |

---

## 4. Cara Kerja Paralel

1. **Stub adalah kontrak.** [#1](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/1) membuat 64 kelas sesuai Tabel 4.1 DPPL. Setelah itu tidak ada yang menunggu nama kelas atau signature.
2. **Seed menggantikan modul sebelumnya.** [#2](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/2) menyediakan data pada tiap tahap pelayanan. Modul apotek bisa diuji dengan resep dari seed tanpa menunggu modul resep.
3. **Satu file, satu pemilik.** Hanya pemilik yang mengubah file kelasnya (Subbab 5). Butuh perubahan di kelas orang lain? Minta lewat komentar issue atau kerjakan bersama pemiliknya.
4. **Layanan bersama selesai lebih dulu.** Fase 1 memberi waktu modul lain memakai implementasi asli (Subbab 6).
5. **Perubahan signature wajib diumumkan.** Sampaikan di grup, tulis pada bagian Perubahan dari DPPL di PR, dan beri tahu pemakainya.

Pengecualian file bersama:

- `DataKlinisBoundary` dibagi per metode (Subbab 5.1).
- `db/seed.sql` boleh ditambah siapa saja. Tambahkan baris baru di akhir bagian tabel terkait.
- Tabel B.5, Logbook, dan AI Usage: ubah baris sendiri saja.

---

## 5. Kepemilikan Kelas

Seluruh 64 kelas DPPL memiliki pemilik. Pemilik mengisi logika kelas dan barisnya pada Tabel B.5.

| Pemilik | Form dan layar | Manager | Entity | Akses data |
| :--- | :--- | :--- | :--- | :--- |
| Axeleon | C01 LoginForm<br>C16 KonfigurasiForm | C17 AutentikasiManager<br>C29 KonfigurasiManager | C30 PenggunaEntity<br>C31 SesiEntity<br>C32 AuditEntity<br>C55 PencadanganEntity | C56 DataAkunBoundary<br>C64 DataPencadanganBoundary |
| Steven | C02 PasienForm<br>C03 KunjunganForm | C18 PasienManager<br>C19 KunjunganManager | C33 PasienEntity<br>C34 RekamMedisEntity<br>C35 KunjunganEntity<br>C36 PoliEntity<br>C37 AntreanEntity | C57 DataPasienBoundary<br>C58 DataKunjunganBoundary |
| Humam | C04 AntreanSkriningForm<br>C05 SkriningForm<br>C12 DaftarPantauForm | C20 SkriningManager<br>C21 RisikoManager<br>C26 PantauManager | C38 SkriningEntity<br>C39 TandaVitalEntity<br>C40 AmbangRisikoEntity<br>C41 PenandaRisikoEntity<br>C51 DaftarPantauEntity<br>C52 TindakLanjutEntity | C59 DataKlinisBoundary<br>C60 DataPantauBoundary |
| Adnan | C06 RekamMedisForm<br>C07 PemeriksaanForm<br>C08 ResepForm | C22 PemeriksaanManager<br>C23 ResepManager | C42 PemeriksaanEntity<br>C43 DiagnosisEntity<br>C44 KodeICD10Entity<br>C45 JadwalKontrolEntity<br>C46 ResepEntity<br>C47 ItemResepEntity | C59 DataKlinisBoundary |
| Malik | C09 AntreanResepForm<br>C10 PenyerahanObatForm<br>C11 PersediaanForm | C24 ApotekManager<br>C25 PersediaanManager | C48 ObatEntity<br>C49 BetsObatEntity<br>C50 PenerimaanObatEntity | C61 DataFarmasiBoundary |
| Fachry | C13 LaporanForm<br>C14 SinkronisasiForm | C27 LaporanManager<br>C28 SinkronisasiManager | C53 LaporanEntity<br>C54 BundelFHIREntity | C15 SATUSEHATGateway<br>C62 DataLaporanBoundary<br>C63 DataSinkronisasiBoundary |

### 5.1 Pembagian `DataKlinisBoundary` (C59)

| Pemilik | Metode | Kueri |
| :--- | :--- | :--- |
| Humam | `simpanSkrining`, `ambilSkrining`, `riwayatTandaVital`, `ambilAmbang`, `simpanAmbang`, `simpanPenanda`, `penandaKunjungan` | Q-C59-01 sampai Q-C59-10 |
| Adnan | `cariKodeICD10`, `simpanPemeriksaan`, `ambilPemeriksaan`, `simpanJadwalKontrol`, `ambilJadwalKontrol` | Q-C59-11 sampai Q-C59-17 |

---

## 6. Ketergantungan Antarmodul

| Penyedia | Layanan | Dipakai oleh | Issue |
| :--- | :--- | :--- | :--- |
| Axeleon | `simpanAudit`, sesi aktif, `periksaHakAkses` | Semua modul | [#4](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/4) |
| Steven | `DataPasienBoundary`, `DataKunjunganBoundary` | Humam, Adnan, Malik, Axeleon | [#5](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/5) |
| Humam | Skrining, riwayat tanda vital, penanda risiko, ambang | Adnan, Axeleon | [#6](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/6) |
| Adnan | `ambilJadwalKontrol` untuk `RisikoManager` | Humam | [#7](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/7) |
| Malik | `DataFarmasiBoundary` | Adnan, Axeleon | [#8](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/8) |
| Fachry | `simpanBundel`, format bundel FHIR | Malik | [#9](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/9) |

---

## 7. Daftar Issue

### Fase 0: Fondasi

| Issue | Judul | PIC | Target merge |
| :--- | :--- | :--- | :--- |
| [#1](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/1) | [CHORE] Kerangka proyek Maven dan stub 64 kelas DPPL | Adnan | Rabu 4 Nov 2026 12.00 |
| [#2](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/2) | [CHORE] Skema PostgreSQL, seed fiktif, dan docker-compose | Malik | Rabu 4 Nov 2026 |
| [#3](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/3) | [FEAT] Cangkang aplikasi JavaFX: navigasi, bilah aplikasi, tema, dan pintasan global | Axeleon | Rabu 4 Nov 2026 |

### Fase 1: Lapisan Data Bersama

| Issue | Judul | PIC | Target merge |
| :--- | :--- | :--- | :--- |
| [#4](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/4) | [FEAT] DataAkunBoundary, sesi, dan jejak audit | Axeleon | Jumat 6 Nov 2026 |
| [#5](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/5) | [FEAT] DataPasienBoundary dan DataKunjunganBoundary | Steven | Jumat 6 Nov 2026 |
| [#6](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/6) | [FEAT] DataKlinisBoundary bagian skrining dan risiko | Humam | Jumat 6 Nov 2026 |
| [#7](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/7) | [FEAT] DataKlinisBoundary bagian pemeriksaan | Adnan | Jumat 6 Nov 2026 |
| [#8](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/8) | [FEAT] DataFarmasiBoundary | Malik | Jumat 6 Nov 2026 |
| [#9](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/9) | [FEAT] DataSinkronisasiBoundary dan BundelFHIREntity | Fachry | Jumat 6 Nov 2026 |

### Fase 2: Fitur

| Issue | Judul | PIC | Target merge |
| :--- | :--- | :--- | :--- |
| [#10](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/10) | [FEAT] UC01 Masuk ke Aplikasi | Axeleon | Minggu 8 Nov 2026 |
| [#11](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/11) | [FEAT] UC02 Mengelola Data Pasien | Steven | Minggu 8 Nov 2026 |
| [#12](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/12) | [FEAT] UC03 Mengelola Kunjungan Pasien | Steven | Minggu 8 Nov 2026 |
| [#13](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/13) | [FEAT] UC04 Melakukan Skrining Awal | Humam | Minggu 8 Nov 2026 |
| [#14](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/14) | [FEAT] UC05 Melakukan Pemeriksaan Medis | Adnan | Minggu 8 Nov 2026 |
| [#15](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/15) | [FEAT] UC06 Menyusun Resep Elektronik | Adnan | Minggu 8 Nov 2026 |
| [#16](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/16) | [FEAT] UC07 Melayani Resep Obat | Malik | Minggu 8 Nov 2026 |
| [#17](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/17) | [FEAT] UC08 Mengelola Persediaan Obat | Malik | Minggu 8 Nov 2026 |
| [#18](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/18) | [FEAT] UC09 Mengelola Daftar Pantau Pasien | Humam | Minggu 8 Nov 2026 |
| [#19](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/19) | [FEAT] UC10 Menyusun Laporan Periodik | Fachry | Minggu 8 Nov 2026 |
| [#20](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/20) | [FEAT] UC11 Melakukan Sinkronisasi SATUSEHAT | Fachry | Minggu 8 Nov 2026 |
| [#21](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/21) | [FEAT] UC12 Mengelola Konfigurasi dan Basis Data Sistem | Axeleon | Minggu 8 Nov 2026 |

### Fase 3 dan 4: Integrasi dan Finalisasi

| Issue | Judul | PIC | Target merge |
| :--- | :--- | :--- | :--- |
| [#22](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/22) | [TEST] Uji alur utama dan konkurensi | Steven | Senin 9 Nov 2026 |
| [#23](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/23) | [TEST] Profiling fitur berat | Fachry | Senin 9 Nov 2026 |
| [#24](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/24) | [DOCS] README aplikasi di src/README.md | Steven | Selasa 10 Nov 2026 12.00 |
| [#25](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/25) | [DOCS] Laporan Implementasi, Logbook, dan AI Usage | Humam | Selasa 10 Nov 2026 18.00 |
| [#26](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/26) | [CHORE] Rilis v9.0 | Axeleon | Selasa 10 Nov 2026 23.59 |

---

## 8. Output yang Dikumpulkan

| Output | Lokasi | PIC | Issue |
| :--- | :--- | :--- | :--- |
| Kode program | `src/` | Pemilik modul | Fase 0 sampai 2 |
| README aplikasi | `src/README.md` | Steven | [#24](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/24) |
| Sampul, logo, dan identitas | `K02_G09_Implementasi.md` | Sudah terisi | - |
| B.1 Perubahan dari DPPL | `K02_G09_Implementasi.md` | Humam, dari setiap PR | [#25](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/25) |
| B.2 Bukti best practices | `K02_G09_Implementasi.md` | Humam | [#25](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/25) |
| B.2.1 Profiling | `K02_G09_Implementasi.md` | Fachry | [#23](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/23) |
| B.3 Kendala | `K02_G09_Implementasi.md` | Humam, masukan semua anggota | [#25](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/25) |
| B.4 Kontribusi anggota | `K02_G09_Implementasi.md` | Disepakati bersama | [#25](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/25) |
| B.5 Traceability | `K02_G09_Implementasi.md` | Pemilik kelas, diperiksa Humam | [#25](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/25) |
| Tag `v9.0` | Releases | Axeleon | [#26](https://github.com/angseleong/IF2150-RPL-K02-G09/issues/26) |
