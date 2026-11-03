# Panduan Kerja SEHATI

Aturan kerja Tugas 9 (Implementasi). Sumbernya: slide Asistensi Akbar, [`K02_G09_Implementasi.md`](K02_G09_Implementasi.md) Bagian A, dan DPPL (`docs/M8/K02_G09_DPPL2.md`). Pembagian tugas ada di [`K02_G09_Pembagian-Tugas.md`](K02_G09_Pembagian-Tugas.md).

---

## 1. Alur Kerja Satu Tugas

Setup sekali di awal, supaya perintah `gh` mengarah ke repo kelompok dan bukan ke repo asisten:

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

Selesaikan konflik jika ada, jalankan `mvn test` dari folder `src/`, lalu buka PR:

```bash
git push -u origin feat/uc02-pasien
```

```bash
gh pr create --base develop
```

> **Awas (repo ini hasil fork).** Jika membuka PR lewat web, GitHub bisa memilih `RPL-24/IF2150-RPL` sebagai tujuan. Pastikan **base repository** adalah `angseleong/IF2150-RPL-K02-G09` dan **base** adalah `develop`. Jangan pernah mengirim PR ke repo asisten.

Setelah PR di-approve minimal satu anggota lain, klik **Create a merge commit**. Jangan menghapus branch sampai `v9.0` dirilis karena daftar branch menjadi bukti B.2.

---

## 2. Branch

| Branch | Isi | Aturan |
| :--- | :--- | :--- |
| `main` | Kode siap rilis dan penerima **Sync fork** dari asisten | Hanya berubah lewat Sync fork dan PR rilis dari `develop`. Tag `v9.x` menunjuk ke sini. |
| `develop` | Integrasi seluruh fitur | Tidak ada push langsung, semua lewat PR. |
| `feat/*` | Fitur baru | Dibuat dari `develop`, contoh `feat/uc02-pasien` |
| `fix/*` | Perbaikan bug | Contoh `fix/nomor-antrean-ganda` |
| `refactor/*` | Perbaikan struktur kode | Contoh `refactor/koneksi-basis-data` |
| `chore/*`, `docs/*`, `test/*` | Build, dokumen, pengujian | Contoh `chore/kerangka-maven`, `docs/readme-aplikasi` |

- Nama branch memakai huruf kecil dan tanda hubung.
- Satu branch untuk satu issue.
- Dilarang `git push --force` ke `main` dan `develop`.
- Setelah Sync fork dari asisten, gabungkan `main` ke `develop` lewat PR.

---

## 3. Semantic Commit

```text
<tipe>(<scope>): <deskripsi singkat>

[body opsional: alasan perubahan]
```

| Tipe | Kegunaan |
| :--- | :--- |
| `feat` | Fitur baru |
| `fix` | Perbaikan bug |
| `docs` | Dokumentasi |
| `style` | Format kode, tanpa mengubah logika |
| `refactor` | Perbaikan struktur tanpa fitur baru atau perbaikan bug |
| `test` | Menambah atau memperbaiki pengujian |
| `chore` | Build, scaffolding, dependensi |

**Scope:** `auth`, `pasien`, `kunjungan`, `skrining`, `risiko`, `pantau`, `pemeriksaan`, `resep`, `apotek`, `persediaan`, `laporan`, `sinkronisasi`, `konfigurasi`, `db`, `ui`, `build`, `m9`.

**Deskripsi:** Bahasa Indonesia, kata kerja dasar, huruf kecil, tanpa titik di akhir, sesuai riwayat commit repo ini.

| Baik | Buruk |
| :--- | :--- |
| `feat(pasien): tambah validasi NIK 16 digit` | `update` |
| `fix(kunjungan): cegah nomor antrean ganda saat simpan bersamaan` | `fixed stuff` |
| `refactor(db): pisahkan pembukaan koneksi ke KoneksiBasisData` | `feat: all` |

---

## 4. Issue dan Label

- Judul memakai awalan: `[FEAT]`, `[FIX]`, `[CHORE]`, `[DOCS]`, atau `[TEST]`.
- Isi wajib: ringkasan dan **Acceptance Criteria** (syarat tugas dianggap selesai). Template tersedia saat membuat issue baru.
- Setiap issue punya satu penanggung jawab (*assignee*) dan masuk milestone fase yang sesuai, misalnya `Fase 2 - Fitur (Minggu 8 Nov)`.

| Kelompok | Label | Kapan dipakai |
| :--- | :--- | :--- |
| Tipe | `enhancement`, `bug`, `documentation` | Saat issue dibuat |
| Prioritas | `priority: high`, `priority: medium`, `priority: low` | Saat issue dibuat |
| Status | `status: in-progress` | Saat mulai dikerjakan |
| Status | `status: review` | Saat PR dibuka, ganti dari `in-progress` |
| Status | `status: done` | Saat PR di-merge |

Tulis `Closes #12` pada deskripsi PR. Issue tertutup otomatis saat PR masuk ke branch default.

---

## 5. Pull Request dan Code Review

**Pembuat PR:**

- Isi seluruh bagian template, termasuk bukti (tangkapan layar atau hasil test) dan perubahan dari DPPL.
- Jaga PR tetap kecil, satu issue satu PR.
- Minta review ke reviewer utama pada tabel berikut.

| Pembuat PR | Reviewer utama | Alasan |
| :--- | :--- | :--- |
| Axeleon | Steven | Steven memakai layanan audit dan sesi |
| Steven | Humam | Humam memakai data kunjungan dan antrean |
| Humam | Adnan | Adnan memakai hasil skrining dan riwayat tanda vital |
| Adnan | Malik | Resep Adnan dilayani oleh modul apotek Malik |
| Malik | Fachry | Apotek Malik menyimpan bundel FHIR untuk modul Fachry |
| Fachry | Axeleon | Sinkronisasi dan laporan melengkapi modul administrasi |

**Reviewer:** tanggapi dalam 12 jam. Jika reviewer utama berhalangan, anggota lain boleh menggantikan. Pembuat PR tidak boleh meng-approve PR sendiri.

Pakai salah satu: **Approve**, **Request Changes**, atau **Comment**. Periksa:

1. Apakah kode sudah sesuai requirement dan acceptance criteria?
2. Apakah ada kesalahan logika?
3. Apakah penamaan jelas dan sama dengan DPPL?
4. Apakah ada *code smell* (Subbab 6.5)?
5. Apakah ada masalah keamanan, performa, atau test yang kurang?

Jangan mempermasalahkan selera gaya seperti spasi atau tanda kutip. Hal itu urusan formatter.

---

## 6. Konvensi Kode

### 6.1 Arsitektur

*Layered Architecture* DPPL harus terlihat pada struktur kode. Kode Maven berada di dalam `src/`, sesuai ketentuan A.4.

```text
src/
├── README.md
├── pom.xml
├── docker-compose.yml
├── .env.example
├── db/                       # schema.sql, seed.sql
├── tests/sehati/             # pengujian JUnit 5
└── src/main/
    ├── java/sehati/
    │   ├── Main.java
    │   ├── presentation/     # C01-C14, C16: *Form
    │   ├── business/control/ # C17-C29: *Manager
    │   ├── business/entity/  # C30-C55: *Entity
    │   ├── dataaccess/       # C15, C56-C64: SATUSEHATGateway, Data*Boundary
    │   └── common/           # pendukung di luar DPPL: pengaturan, koneksi
    └── resources/sehati/
        ├── fxml/             # satu file per Form, contoh PasienForm.fxml
        └── css/
```

| Lapisan | Boleh memanggil | Dilarang |
| :--- | :--- | :--- |
| `presentation` | `control`, `entity` sebagai data tampilan | SQL, `java.sql`, `Data*Boundary`, aturan bisnis |
| `control` | `entity`, `dataaccess` | `javafx.*`, menulis SQL |
| `entity` | sesama `entity` | `javafx.*`, `java.sql`, `dataaccess` |
| `dataaccess` | `entity` | `javafx.*`, `control` |

### 6.2 Penamaan

| Hal | Format | Contoh |
| :--- | :--- | :--- |
| Kelas, atribut, operasi DPPL | **Sama persis** dengan Tabel 4.1, Bahasa Indonesia | `KunjunganManager.bukaKunjungan()` |
| Kelas di luar DPPL | `PascalCase`, Bahasa Indonesia | `KoneksiBasisData` |
| Variabel dan metode | `camelCase` | `nomorAntrean` |
| Konstanta | `UPPER_SNAKE_CASE` | `BATAS_DIAM_MENIT` |
| Konstanta kueri | ID kueri DPPL | `Q_C59_01` |
| Paket | huruf kecil | `sehati.business.control` |
| `fx:id` pada FXML | `camelCase` dengan awalan jenis | `kolomNik`, `tombolSimpan` |
| Tabel dan kolom | `snake_case`, aturan BAB 5 DPPL | `bets_obat.tanggal_kedaluwarsa` |
| Kelas dan metode test | `<Kelas>Test`, `operasi_kondisi` | `PasienManagerTest.validasiNIK_menolak15Digit` |

Perubahan nama kelas, atribut, atau operasi DPPL wajib ditulis pada bagian **Perubahan dari DPPL** di PR.

### 6.3 Format

- Pakai **Reformat Code** IntelliJ sebelum commit. `.editorconfig` di root mengatur 4 spasi, UTF-8, LF, dan batas 120 kolom.
- Satu kelas publik per file.
- Hapus import yang tidak terpakai.

### 6.4 Aturan dari DPPL

- **Akses data:** `Manager` membuka satu koneksi pada awal operasi, lalu membuat `DataBoundary` dengan koneksi itu. `Manager` tidak menyimpan `DataBoundary` sebagai atribut.
- **Transaksi:** blok `MULAI TRANSAKSI` memakai satu koneksi dengan *auto-commit* mati, COMMIT jika berhasil, ROLLBACK jika gagal.
- **Penolakan:** masukan salah memakai `IllegalArgumentException`. Keadaan data yang tidak mengizinkan operasi memakai `IllegalStateException`. Pesannya ditampilkan oleh `Form`.
- **Kueri:** hanya di `DataBoundary` dan selalu lewat `PreparedStatement`. Jangan menyambung masukan pengguna ke teks SQL.
- **Log:** pakai `java.util.logging`. Dilarang `System.out.println` pada kode yang di-merge.
- **Error teknis:** jangan telan `SQLException`. Catat ke log, lalu tampilkan pesan yang dapat dipahami pengguna.

### 6.5 Code Smell yang Dihindari

| Masalah | Solusi |
| :--- | :--- |
| *God class/function* | Pisahkan tanggung jawab agar kode modular |
| *Hardcoded values* | Pakai `.env` atau konstanta bernama |
| *Poor error handling* | Tangani pengecualian dan beri pesan informatif |
| *Mixing concerns* | Patuhi tabel lapisan pada Subbab 6.1 |
| Gaya kode tidak konsisten | Pakai `.editorconfig` dan Reformat Code |

---

## 7. Cara Menulis Komentar

Komentar memakai Bahasa Indonesia. Nama kode sudah menjelaskan **apa**, jadi komentar menjelaskan **mengapa** dan **sumbernya di DPPL**.

| Jenis | Dipakai untuk | Aturan |
| :--- | :--- | :--- |
| `/** ... */` | Kelas DPPL dan operasi publik `Manager`, `DataBoundary`, serta `Gateway` | Kalimat singkat, cantumkan ID kelas, algoritma, kueri, atau KF |
| `// ...` | Penjelasan alasan di dalam metode | Satu baris di **atas** kode yang dijelaskan, satu spasi setelah `//` |
| `// TODO(#12): ...` | Pekerjaan yang belum selesai | Wajib mencantumkan nomor issue |
| `// FIXME(#12): ...` | Bug yang diketahui | Wajib mencantumkan nomor issue |
| `-- ...` | File SQL | Satu baris di atas perintah |
| `<!-- ... -->` | File FXML | Hanya bila struktur layar tidak jelas |

Contoh:

```java
/**
 * C19 - Mengelola pembukaan kunjungan dan nomor antrean (UC03).
 */
public class KunjunganManager {

    /**
     * Menerbitkan nomor antrean berurutan per poli per hari (Algo-C19-02, KF03).
     */
    private int terbitkanNomorAntrean(int idPoli) {
        // Baris poli dikunci agar dua loket tidak mendapat nomor yang sama (KF23).
        PoliEntity poli = dataKunjungan.kunciPoli(idPoli);
        // TODO(#12): tampilkan nomor pada tiket antrean.
        return poli.ambilNomorBerikutnya(LocalDate.now());
    }
}
```

Hindari:

```java
i = i + 1;              // tambah i dengan 1
// simpan pasien
// dataPasien.simpanPasienLama(p);
/* komentar blok di dalam metode */
```

- Dilarang menyimpan kode yang dijadikan komentar. Riwayatnya sudah ada di Git.
- Dilarang menaruh komentar di ujung baris kode, kecuali pada tabel konstanta.
- Getter dan setter tidak perlu komentar.

---

## 8. Pengujian dan Performa

- Pakai JUnit 5 di `src/tests/`, jalankan dengan `mvn test` dari folder `src/`.
- Setiap `Manager` dan `Entity` yang memiliki algoritma pada DPPL punya test untuk jalur normal dan satu jalur penolakan.
- Urutan optimasi: buat yang bekerja, identifikasi *bottleneck*, optimasi yang perlu.
- Tanda perlu diperiksa: UI membeku saat memproses data, kebocoran memori, atau kueri lebih dari 1 detik.
- Proses berat tidak boleh berjalan di JavaFX Application Thread. Pakai `Task` dari `javafx.concurrent`.

---

## 9. Keamanan Repository

Repo ini **public**.

- Dilarang meng-commit password, token, `.env` asli, private key, dan data pribadi. Pelanggaran dapat mengurangi nilai.
- Variabel baru ditambahkan ke `src/.env.example` tanpa nilai rahasia.
- Seed hanya memakai data fiktif, termasuk NIK dan nomor telepon.
- Jika kredensial terlanjur ter-commit, cabut atau ganti kredensial itu. Menghapus commit saja tidak cukup.

---

## 10. Dokumentasi Harian

- **Logbook:** tambah baris milik sendiri pada `docs/K02_G09_Logbook.md`, Milestone 9, setiap selesai bekerja.
- **Penggunaan AI:** catat pada `docs/K02_G09_AI-Usage.md`, Milestone 9. Pembuat PR tetap bertanggung jawab memahami setiap baris kodenya.
- **Perubahan dari DPPL:** tulis di PR saat perubahan terjadi. Ringkasannya dipindahkan ke Tabel B.1 pada tahap laporan.
- **Traceability:** isi baris kelas milik sendiri pada Tabel B.5 di `docs/M9/K02_G09_Implementasi.md` dalam PR yang sama dengan kodenya.

---

## 11. Rilis

1. Buka PR dari `develop` ke `main`, lalu merge setelah di-review.
2. Buka **Releases → Draft a new release**, buat tag `v9.0` dengan target `main`. Revisi berikutnya `v9.1`, `v9.2`, dan seterusnya.
3. Yang dinilai adalah **tag terakhir sebelum Rabu, 11 November 2026, 21.50 WIB**.
