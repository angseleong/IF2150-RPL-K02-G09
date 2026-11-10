# SEHATI — Sistem Informasi Puskesmas

Aplikasi desktop untuk pelayanan puskesmas: pendaftaran pasien, kunjungan, skrining, pemeriksaan medis, resep elektronik, dan apotek, dengan integrasi SATUSEHAT.

## Teknologi

| Komponen | Versi |
| :--- | :--- |
| Java | 21 (maven.compiler.release) |
| JavaFX | 21.0.5 |
| PostgreSQL | 15 |
| Driver PostgreSQL (JDBC) | 42.7.4 |
| JUnit Jupiter | 5.11.3 |
| Maven | 3.9+ |
| PDFBox | 3.0.3 |
| jBCrypt | 0.4 |

## Prasyarat

- JDK 21 atau lebih baru (`JAVA_HOME` diarahkan ke JDK tersebut).
- Maven 3.9+.
- PostgreSQL 15 (lokal atau via Docker).
- (Opsional) Docker + Docker Compose untuk basis data.

## Instalasi

```bash
git clone https://github.com/angseleong/IF2150-RPL-K02-G09.git
cd IF2150-RPL-K02-G09/src
cp .env.example .env   # Windows: copy .env.example .env
```

Isi `.env` minimal:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=sehati
DB_USER=sehati_app
DB_PASSWORD=<kata sandi aplikasi>
DB_ADMIN_PASSWORD=<kata sandi admin basis data>
```

Buat basis data (Docker, dari folder `src/`):

```bash
docker compose up -d
```

Atau manual: jalankan `db/schema.sql` lalu `db/seed.sql` lalu `db/sandi-aplikasi.sh` pada PostgreSQL 15. Skema tabel ada di `db/schema.sql`.

## Konfigurasi

- `SATUSEHAT_URL`, `SATUSEHAT_TOKEN`: REST API sandbox SATUSEHAT (opsional, untuk sinkronisasi).
- `EKSPOR_FRASA_SANDI`: frasa sandi enkripsi berkas ekspor bundel (Algo-C63-01).
- `DB_UJI_URL`: URL JDBC lengkap untuk uji berbasis data, misalnya `jdbc:postgresql://localhost:5432/sehati?user=sehati_app&password=...`. Uji DB di-gate variabel ini.

## Menjalankan Aplikasi

```bash
cd src
mvn javafx:run
```

Atau build jar lalu jalankan:

```bash
mvn -q package
java -jar target/sehati-*.jar
```

## Menjalankan Test

```bash
cd src
mvn test                      # seluruh test (DB test dilewati bila DB_UJI_URL kosong)
mvn test -Dtest=AlurUtamaTest,KonkurensiTest   # uji alur + konkurensi (butuh DB_UJI_URL)
```

## Akun Uji (empat peran)

Kata sandi seluruh akun contoh: `sehati123`.

| Nama pengguna | Peran |
| :--- | :--- |
| `admin` | Petugas Administrasi |
| `perawat` | Perawat |
| `dokter` | Dokter |
| `farmasi` | Petugas Farmasi |
| `perawat.nonaktif` | Perawat (nonaktif) |

## Modul dan Pembagian Anggota

| Anggota (GitHub) | Modul | Use Case |
| :--- | :--- | :--- |
| Axeleon Justin Algianto (`angseleong`) | Akses dan administrasi | UC01, UC12 |
| Steven Vanako (`cepsssy`) | Loket | UC02, UC03 |
| Ahmad Humam Isma (`1smam`) | Skrining dan pemantauan | UC04, UC09 |
| Muhammad Adnan Kurniawan (`adnankurniawann`) | Pemeriksaan dan resep | UC05, UC06 |
| Malik Arsyafiandra Madani (`malikarsya`) | Farmasi | UC07, UC08 |
| Fachry Azriel Fajdwani (`rabsed1`) | Pelaporan dan integrasi | UC10, UC11 |

## Daftar Tabel

Skema lengkap (tabel, kolom, kendala, indeks) ada di `db/schema.sql`. Tabel utama: `pengguna`, `sesi`, `audit`, `pasien`, `rekam_medis`, `catatan_rekam_medis`, `poli`, `kunjungan`, `antrean`, `antrean_entri`, `skrining`, `tanda_vital`, `ambang_risiko`, `penanda_risiko`, `kode_icd10`, `pemeriksaan`, `diagnosis`, `jadwal_kontrol`, `obat`, `penerimaan_obat`, `bets_obat`, `resep`, `item_resep`, `daftar_pantau`, `tindak_lanjut`, `laporan`, `bundel_fhir`, `pencadangan`.

## Struktur Folder

```
src/
  src/main/java/sehati/   # kode aplikasi (presentation, business, dataaccess, common)
  src/main/resources/     # FXML, CSS
  src/tests/              # uji JUnit
  db/                     # schema.sql, seed.sql, uji-kueri.sql
  pom.xml
  docker-compose.yml
  .env.example
```