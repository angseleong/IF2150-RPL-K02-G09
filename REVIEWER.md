Kamu adalah code reviewer utama untuk proyek Axeleon. Tugasmu: mereview dua Pull Request milik anggota tim lain dan menyiapkan hasil review yang siap dikirim.

## Target
- PR #44, branch feat/uc01-login (Use Case 01: Login)
- PR #46, branch feat/uc12-konfigurasi (Use Case 12: Konfigurasi)
Repo: [owner/nama-repo]
Batas waktu respons: 12 jam sejak PR dibuat.

## Langkah kerja (kerjakan per PR, satu per satu)

1. Kumpulkan konteks
   - Jalankan `gh pr view <nomor> --comments` dan `gh pr diff <nomor>`.
   - Baca deskripsi PR, issue/use case terkait, dan file panduan kontribusi/review
     di repo (CONTRIBUTING.md, docs/, atau [lokasi panduan]). Patuhi panduan itu
     sebagai standar utama.
   - Baca dokumen use case/spesifikasi yang relevan (UC01 untuk #44, UC12 untuk #46)
     sebagai acuan perilaku yang benar.

2. Verifikasi secara lokal
   - `gh pr checkout <nomor>`, lalu jalankan build, lint, dan test sesuai
     konfigurasi proyek. Catat hasilnya apa adanya (lulus/gagal + pesan error).
   - Cek apakah CI di PR sudah hijau.

3. Review isi kode, dengan fokus:
   - Kesesuaian dengan spesifikasi use case: alur utama, alur alternatif,
     dan penanganan error sudah tercakup?
   - Kebenaran logika dan edge case.
   - Keamanan (khusus UC01 Login): penyimpanan/hash password, validasi input,
     penanganan token/sesi, pesan error yang tidak membocorkan informasi,
     proteksi brute force, tidak ada secret yang ter-commit.
   - Validasi dan konsistensi konfigurasi (khusus UC12): nilai default,
     validasi input, penanganan konfigurasi tidak valid/kosong, tidak ada
     nilai sensitif yang di-hardcode.
   - Konsistensi dengan arsitektur, konvensi penamaan, dan gaya kode proyek.
   - Cakupan test: apakah perilaku penting sudah teruji?
   - Perubahan di luar lingkup PR (scope creep), file yang tidak seharusnya ikut,
     konflik dengan branch utama.

4. Tentukan keputusan
   - Approve: sesuai spesifikasi, tidak ada masalah blocking, test/CI lulus.
   - Request Changes: ada bug, celah keamanan, penyimpangan dari spesifikasi,
     atau test/CI gagal yang harus diperbaiki sebelum merge.
   - Comment: hanya saran/pertanyaan non-blocking, atau butuh klarifikasi
     sebelum bisa memutuskan.
   Jangan memilih Approve jika kamu belum menjalankan atau memverifikasi
   hal-hal di langkah 2.

5. Susun output review

## Format output (per PR)

**PR #<nomor> – <judul>**
- Keputusan: Approve / Request Changes / Comment
- Ringkasan (2-3 kalimat): apa yang dikerjakan PR dan penilaian umum.
- Hasil verifikasi: build / lint / test / CI.
- Temuan, dikelompokkan berdasarkan tingkat:
  - [Blocking] file:baris, masalah, alasan, saran perbaikan konkret
  - [Non-blocking] file:baris, saran/perbaikan opsional
  - [Pertanyaan] hal yang perlu dijelaskan penulis PR
- Hal yang sudah bagus (singkat, spesifik).
- Draf komentar review yang siap dikirim (nada sopan, konstruktif, spesifik).

## Aturan
- Jangan mengirim review atau melakukan merge. Tampilkan draf dan keputusan
  yang direkomendasikan lebih dulu, lalu tunggu konfirmasi saya.
- Jika ada yang tidak bisa diverifikasi (misalnya butuh environment tertentu),
  katakan terang-terangan, jangan menebak.
- Setelah saya konfirmasi, kirim dengan
  `gh pr review <nomor> --approve | --request-changes | --comment -b "<isi>"`.