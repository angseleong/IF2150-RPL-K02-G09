# Workflow Tim — SEHATI (IF2150 K02 G09)

Sumber kebenaran: [`docs/M9/K02_G09_Panduan-Kerja.md`](docs/M9/K02_G09_Panduan-Kerja.md).

## 1. Alur kerja satu issue

Setup sekali di awal:

```bash
gh repo set-default angseleong/IF2150-RPL-K02-G09
```

Setiap mengerjakan satu issue:

```bash
git switch develop
git pull origin develop
git switch -c feat/uc02-pasien
```

Kerjakan dan commit sedikit demi sedikit. Sebelum membuka PR, tarik perubahan terbaru dari `develop`:

```bash
git pull origin develop
```

Selesaikan konflik jika ada, jalankan `mvn test` dari folder `src/`, lalu:

```bash
git push -u origin feat/uc02-pasien
gh pr create --base develop
```

- **Awas (repo ini hasil fork):** jika PR lewat web, GitHub bisa memilih `RPL-24/IF2150-RPL` sebagai tujuan. Pastikan **base repository** adalah `angseleong/IF2150-RPL-K02-G09` dan **base** adalah `develop`.
- Setelah PR di-approve minimal satu anggota lain, klik **Create a merge commit**. Jangan menghapus branch sampai `v9.0` dirilis (daftar branch menjadi bukti B.2).

## 2. Branch

| Branch | Isi | Aturan |
|---|---|---|
| `main` | Kode siap rilis, penerima **Sync fork** dari asisten. Hanya berubah lewat Sync fork dan PR rilis dari `develop`. Tag `v9.x` menunjuk ke sini. | |
| `develop` | Integrasi seluruh fitur. Tidak ada push langsung, semua lewat PR. | |
| `feat/*` | Fitur baru. Dibuat dari `develop`, contoh `feat/uc02-pasien`. | |
| `fix/*` | Perbaikan bug. Contoh `fix/nomor-antrean-ganda`. | |
| `refactor/*` | Perbaikan struktur. | |
| `chore/*`, `docs/*`, `test/*` | Build, dokumen, pengujian. | |

- Nama branch: huruf kecil + tanda hubung. Satu branch untuk satu issue. Dilarang `git push --force` ke `main` dan `develop`.
- Setelah Sync fork dari asisten, gabungkan `main` ke `develop` lewat PR.

## 3. Semantic commit

```text
<tipe>(<scope>): <deskripsi singkat>
```

| Tipe | Kegunaan |
|---|---|
| `feat` | Fitur baru |
| `fix` | Perbaikan bug |
| `docs` | Dokumentasi |
| `style` | Format kode, tanpa mengubah logika |
| `refactor` | Perbaikan struktur tanpa fitur baru/bisnis bug |
| `test` | Menambah/memperbaiki pengujian |
| `chore` | Build, scaffolding, dependensi |

Scope contoh: `auth`, `pasien`, `kunjungan`, `skrining`, `risiko`, `pantau`, `pemeriksaan`, `resep`, `apotek`, `persediaan`, `laporan`, `sinkronisasi`, `konfigurasi`, `db`, `ui`, `build`, `m9`.

Deskripsi: Bahasa Indonesia, kata kerja dasar, huruf kecil, tanpa titik di akhir.

Contoh baik: `feat(pasien): tambah validasi NIK 16 digit`, `fix(kunjungan): cegah nomor antrean ganda saat simpan bersamaan`.

## 4. Issue dan label

- Judul memakai awalan: `[FEAT]`, `[FIX]`, `[CHORE]`, `[DOCS]`, `[TEST]`.
- Isi wajib: ringkasan dan **Acceptance Criteria**. Template: `.github/ISSUE_TEMPLATE/tugas.md` (Tugas) / `bug.md` (Bug).
- Setiap issue punya satu assignee dan masuk milestone fase, misalnya `Fase 2 - Fitur (Minggu 8 Nov)`.
- Label: tipe `enhancement`/`bug`/`documentation`; prioritas `priority: high|medium|low`; status `status: in-progress`, `status: review`, `status: done`.

## 5. Pull Request dan Code Review

**Pembuat PR → reviewer utama:**

| Pembuat PR | Reviewer utama | Alasan |
|---|---|---|
| Axeleon | Steven | Steven memakai layanan audit dan sesi |
| Steven | Humam | Humam memakai data kunjungan dan antrean |
| Humam | Adnan | Adnan memakai hasil skrining dan riwayat tanda vital |
| Adnan | Malik | Resep Adnan dilayani oleh modul apotek Malik |
| Malik | Fachry | Apotek Malik menyimpan bundel FHIR untuk modul Fachry |
| Fachry | Axeleon | Sinkronisasi dan laporan melengkapi modul administrasi |

Aturan: reviewer menanggapi dalam 12 jam; jika berhalangan, anggota lain boleh menggantikan. Pembuat PR tidak boleh approve PR sendiri.

Gunakan **Approve**, **Request Changes**, atau **Comment**. Periksa: kesesuaian acceptance criteria, kesalahan logika, penamaan jelas & sama DPPL, code smell, masalah keamanan/performa/test.

## 6. Konvensi kode

- **Arsitektur:** Layered Architecture (empat lapisan) — lihat [`architecture.md`](architecture.md).
- **Akses data:** `Manager` membuka satu koneksi lewat `KoneksiBasisData.buka()` di awal operasi, lalu membuat `DataBoundary` dengan koneksi itu; `Manager` tidak menyimpan `DataBoundary` sebagai atribut.
- **Transaksi:** blok transaksi: satu koneksi dengan auto-commit mati; COMMIT sukses, ROLLBACK gagal.
- **Penolakan:** input salah → `IllegalArgumentException`; keadaan data tidak mengizinkan operasi → `IllegalStateException`; pesan ditampilkan oleh `Form`.
- **Kueri:** hanya di `DataBoundary`, selalu lewat `PreparedStatement`. Dilarang menyambung masukan pengguna ke teks SQL.
- **Log:** `java.util.logging`. Dilarang `System.out.println` pada kode yang di-merge.
- **Error teknis:** jangan telan `SQLException`; catat ke log, lalu tampilkan pesan yang dapat dipahami pengguna.

### 6.5 Code smell yang dihindari

God class/function → pisah tanggung jawab; hardcoded values → `.env` atau konstanta bernama; poor error handling → tangkap pengecualian + pesan informatif; mixing concerns → patuhi tabel lapisan; gaya tidak konsisten → `.editorconfig` + Reformat Code.

## 7. Komentar

- `/** ... */` untuk kelas DPPL & operasi publik `Manager`/`DataBoundary`/`Gateway`: kalimat singkat, cantumkan ID kelas, algoritma, kueri, atau KF.
- `// ...` untuk alasan di dalam metode: satu baris di atas kode yang dijelaskan, satu spasi setelah `//`.
- `// TODO(#12): ...` / `// FIXME(#12): ...` untuk kerja belum selesai/bug yang diketahui; nomor issue wajib tercantum.
- `-- ...` untuk file SQL; `<!-- ... -->` untuk FXML bila struktur layar tidak jelas.

## 8. Pengujian dan performa

- JUnit 5 di `src/tests/`, jalankan `mvn test` dari folder `src/`.
- Setiap `Manager` dan `Entity` dengan algoritma pada DPPL punya test jalur normal + satu jalur penolakan.
- Proses berat tidak boleh berjalan di JavaFX Application Thread; pakai `Task` dari `javafx.concurrent`.
- Urutan optimasi: buat yang bekerja, identifikasi bottleneck, optimasi yang perlu. Tanda periksa: UI membeku, kebocoran memori, kueri > 1 detik.

## 9. Keamanan repository

Repo **public**. Dilarang meng-commit password, token, `.env` asli, private key, data pribadi.

- Variabel baru di `src/.env.example` tanpa nilai rahasia.
- Seed hanya memakai data fiktif, termasuk NIK dan nomor telepon.

## 10. Dokumentasi harian

- **Logbook:** tambah baris milik sendiri di `docs/K02_G09_Logbook.md`, Milestone 9, setiap selesai bekerja.
- **AI usage:** catat di `docs/K02_G09_AI-Usage.md`, Milestone 9.
- **Perubahan dari DPPL:** tulis di PR (ringkasan dipindahkan ke Tabel B.1 pada tahap laporan).
- **Traceability:** isi baris kelas milik sendiri di Tabel B.5 di `docs/M9/K02_G09_Implementasi.md` dalam PR yang sama dengan kodenya.

## 11. Rilis

1. Buka PR dari `develop` ke `main`, merge setelah di-review.
2. Releases → **Draft a new release**, buat tag `v9.0` dengan target `main`. Revisi berikutnya: `v9.1`, `v9.2`, dst.
3. Yang dinilai adalah **tag terakhir sebelum Rabu, 11 November 2026, 21.50 WIB**.

Perubahan DPPL yang wajib dicatat: lihat Tabel B.1 di `docs/M9/K02_G09_Implementasi.md` dan bagian **Perubahan dari DPPL** di PR.
