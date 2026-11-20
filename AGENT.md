# AGENT.md — Instruksi Agent AI & Workflow Kerja: SEHATI (IF2150 K02 G09)

Aplikasi: **SEHATI** — Sistem Elektronik Pelayanan Kesehatan Terintegrasi.
Kelompok: **K02 G09, Cumlaude** · Deadline: **Rabu, 11 November 2026, 21.50 WIB** · Rilis: tag **`v9.0`** di `main`.

Dokumen sumber kebenaran: [`docs/M9/K02_G09_Panduan-Kerja.md`](docs/M9/K02_G09_Panduan-Kerja.md) · [`docs/M8/K02_G09_DPPL2.md`](docs/M8/K02_G09_DPPL2.md) · [`docs/M9/K02_G09_Pembagian-Tugas.md`](docs/M9/K02_G09_Pembagian-Tugas.md) · [`docs/M9/K02_G09_Implementasi.md`](docs/M9/K02_G09_Implementasi.md).

> Aturan di bawah adalah ringkasan eksekutif. Apabila ada konflik antara file ini dan **Panduan Kerja**, **Panduan Kerja menang**.

## 1. Konteks proyek

| Informasi | Keterangan |
|---|---|
| Aplikasi | SEHATI — Sistem Elektronik Pelayanan Kesehatan Terintegrasi (desktop JavaFX, PostgreSQL 15) |
| Kelas / kelompok | K02 / G09, Cumlaude |
| Platform | Desktop (JavaFX), basis data PostgreSQL 15 |
| Deadline tugas 9 | Rabu, 11 November 2026, 21.50 WIB |
| Rilis | Tag `v9.0` di `main` (setelah PR `develop` → `main`) |
| Deadline fase finalisasi | README 12.00, laporan 18.00, tag v9.0 23.59 |

### Tim dan modul

| Anggota | GitHub | Modul | Use Case |
|---|---|---|---|
| Axeleon Justin Algianto | `angseleong` | Akses dan administrasi | UC01, UC12 |
| **Steven Vanako** | **`cepsssy`** | **Loket** | **UC02, UC03** |
| Ahmad Humam Isma | `1smam` | Skrining dan pemantauan | UC04, UC09 |
| Muhammad Adnan Kurniawan | `adnankurniawann` | Pemeriksaan dan resep | UC05, UC06 |
| Malik Arsyafiandra Madani | `malikarsya` | Farmasi | UC07, UC08 |
| Fachry Azriel Fajdwan | `rabsed1` | Pelaporan dan integrasi | UC10, UC11 |

Setiap anggota memegang dua use case dari awal sampai akhir: layar, `Manager`, `Entity`, kueri, dan test.

### Tugas user (Steven Vanako — `cepsssy`)

1. **Baca DPPL modul sendiri (UC02/UC03):** selesai.
2. **#5 DataPasien/DataKunjunganBoundary:** PIC Steven, eksekusi diambil alih Humam (done).
3. **#11 UC02:** **selesai** — PR #45 (`feat/uc02-pasien`) **MERGED** 8 Nov 2026.
4. **#12 UC03:** **sedang di-review** — PR #51 (`feat/cangkang-aplikasi`) **OPEN**, reviewer `1smam` (Humam); db layer `DataKunjunganBoundary` + UI tiket antrean masih TODO.
5. **#22 [TEST] Uji alur utama dan konkurensi:** belum dikerjakan (Fase 3).
6. **#24 [DOCS] README aplikasi di `src/README.md`:** belum dikerjakan (Fase 4, batas 12.00 Selasa 10 Nov).
7. **Rilis v9.0:** PIC Axeleon (issue #26).

## 2. Status pekerjaan saat ini (per 10 November 2026)

- **PR #45** `[FEAT] UC02 Mengelola Data Pasien` (`feat/uc02-pasien` → `develop`): **MERGED** 2026-11-08. CI lulus: `Tests run: 99, Failures: 0, Errors: 0, Skipped: 27`; `PasienManagerTest` 12 uji / 4 skip (di-gate `DB_UJI_URL`); `PasienEntityTest` 2 uji.
- **PR #51** `feat(kunjungan): terbitkan nomor antrean dan kunci kunjungan aktif UC03` (`feat/cangkang-aplikasi` → `develop`): **OPEN**, dibuat 2026-11-10T00:49, penulis Steven, reviewer utama `1smam`. Isi: `PoliEntity`, `KunjunganEntity`, `AntreanEntity`, `KunjunganManager`; unit test + Tabel B.5. Catatan: **database layer dan UI layar tiket antrean masih TODO terpisah**.
- **Issue #12** `[FEAT] UC03 Mengelola Kunjungan Pasien`: open, label `status: review`, priority high, dibuat 2026-11-10T01:14 — menyusul PR #51.

## 3. Workflow kerja agent

### 3.1 Alur kerja satu issue

```bash
gh repo set-default angseleong/IF2150-RPL-K02-G09
git switch develop
git pull origin develop
git switch -c feat/uc02-pasien
```

Kerjakan + commit sedikit demi sedikit. Sebelum PR: `git pull origin develop`, selesaikan konflik, `mvn test` dari folder `src/`, lalu:

```bash
git push -u origin feat/uc02-pasien
gh pr create --base develop
```

- Base PR harus `develop` di `angseleong/IF2150-RPL-K02-G09`. Jangan PR ke repo asisten (`RPL-24/IF2150-RPL`).
- Setelah minimal satu approve: **Create a merge commit**. Jangan hapus branch sebelum `v9.0` dirilis.

### 3.2 Branch

`main` (rilis, Sync fork dari asisten), `develop` (integrasi, semua lewat PR), `feat/*`, `fix/*`, `refactor/*`, `chore/*`, `docs/*`, `test/*`. Nama branch huruf kecil + tanda hubung, satu branch per issue. Larangan `git push --force` ke `main`/`develop`.

### 3.3 Semantic commit

`<tipe>(<scope>): <deskripsi>` — tipe: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`. Scope: `auth`, `pasien`, `kunjungan`, `skrining`, `risiko`, `pantau`, `pemeriksaan`, `resep`, `apotek`, `persediaan`, `laporan`, `sinkronisasi`, `konfigurasi`, `db`, `ui`, `build`, `m9`. Deskripsi: Bahasa Indonesia, kata kerja dasar, huruf kecil, tanpa titik.

### 3.4 Issue dan PR

- Judul: `[FEAT]`, `[FIX]`, `[CHORE]`, `[DOCS]`, `[TEST]`. Isi wajib: ringkasan + Acceptance Criteria. Assignee + milestone fase. Label: tipe `enhancement`/`bug`/`documentation`; prioritas `priority: high|medium|low`; status `status: in-progress`, `status: review`, `status: done`.
- PR: isi template (Ringkasan, Closes, Bukti, Perubahan dari DPPL, Checklist). Tetap kecil, satu issue satu PR.
- Reviewer utama: Axeleon←Steven, Steven←Humam, Humam←Adnan, Adnan←Malik, Malik←Fachry, Fachry←Axeleon.

### 3.5 Rilis

PR `develop` → `main` setelah review; Releases → Draft; tag `v9.0` di `main`; revisi `v9.1`+; tag terakhir sebelum Rabu, 11 November 2026, 21.50 WIB yang dinilai.

## 4. Aturan jelas untuk agent (konvensi kode)

- **Arsitektur:** Layered Architecture empat lapisan (presentation, control, entity, dataaccess). Lihat [`architecture.md`](architecture.md).
- **Akses data:** `Manager` membuka satu koneksi lewat `KoneksiBasisData.buka()` di awal operasi, lalu membuat `DataBoundary` dengan koneksi itu. `Manager` **tidak** menyimpan `DataBoundary` sebagai atribut.
- **Transaksi:** satu koneksi, auto-commit mati, COMMIT sukses, ROLLBACK gagal.
- **Penolakan:** input salah → `IllegalArgumentException`; keadaan data tidak mengizinkan → `IllegalStateException`; pesan ditampilkan oleh `Form`.
- **Kueri:** hanya di `DataBoundary`, lewat `PreparedStatement`. Dilarang menyambung masukan pengguna ke teks SQL.
- **Log:** `java.util.logging`. Dilarang `System.out.println` pada kode yang di-merge.
- **Error teknis:** jangan telan `SQLException`; catat ke log, lalu tampilkan pesan yang dapat dipahami pengguna.

### 4.1 Penamaan

Kelas/operasi DPPL: **sama persis** Tabel 4.1 DPPL, Bahasa Indonesia. Kelas di luar DPPL: `PascalCase` Bahasa Indonesia. Variabel & metode: `camelCase`. Konstanta: `UPPER_SNAKE_CASE`. Konstanta kueri: ID kueri DPPL (`Q_Cxx_xx`). Package: huruf kecil. `fx:id`: `camelCase` awalan jenis. Tabel/kolom: `snake_case`. Test: `<Kelas>Test`, operasi kondisi: `operasi_kondisi`.

### 4.2 Format & komentar

- Format: `.editorconfig` (4 spasi, UTF-8, LF, batas 120 kolom). Satu kelas publik per file. Hapus import tidak terpakai. Reformat Code IntelliJ sebelum commit.
- Komentar: `/** */` untuk kelas DPPL & operasi publik (singkat, cantumkan ID kelas/algoritma/kueri/KF); `// ...` alasan di atas kode, satu spasi setelah `//`; `// TODO(#12):` / `// FIXME(#12):`; `--` SQL; `<!-- -->` FXML. Dilarang komentar di ujung baris kecuali konstanta.

### 4.3 Code smell

Hindari god class/function, hardcoded values, poor error handling, mixing concerns, gaya tidak konsisten.

### 4.4 Pengujian & performa

- JUnit 5 di `src/tests/`; `mvn test` dari folder `src/`. Setiap `Manager`/`Entity` ber-algoritma punya test jalur normal + 1 penolakan.
- Basis data di-gate `DB_UJI_URL`. CI: `mvn -B test` (folder `src/`).
- Proses berat di `Task` `javafx.concurrent`, bukan JavaFX Application Thread.

### 4.5 Security & dokumentasi harian

- Repo public: dilarang password/token/`.env` asli/private key/data pribadi. Variabel baru di `src/.env.example` tanpa nilai rahasia. Seed hanya data fiktif.
- Logbook: `docs/K02_G09_Logbook.md`; AI usage: `docs/K02_G09_AI-Usage.md`; Perubahan dari DPPL di PR; Traceability: Tabel B.5 di `docs/M9/K02_G09_Implementasi.md`.

## 5. Ringkasan aturan yang wajib diingat agent

- Branch per issue, dari `develop`, PR ke `develop` (`angseleong/IF2150-RPL-K02-G09`), merge commit setelah approve, jangan hapus branch sebelum `v9.0`.
- Semantic commit `<tipe>(<scope>): <deskripsi>`.
- Manager: satu koneksi lewat `KoneksiBasisData.buka()` → `DataBoundary(koneksi)`; tanpa atribut boundary di Manager.
- Transaksi: auto-commit mati → COMMIT/ROLLBACK. Penolakan: `IllegalArgumentException`/`IllegalStateException`. Kueri: `PreparedStatement` di Boundary. Log: `java.util.logging`.
- Test: JUnit 5, jalur normal + 1 penolakan; heavy di `Task`.
- Deadline: Rabu, 11 November 2026, 21.50 WIB (tag `v9.0`).
