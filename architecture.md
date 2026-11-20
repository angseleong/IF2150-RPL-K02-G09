# Arsitektur — SEHATI (IF2150 K02 G09)

Sumber spesifikasi: [`docs/M8/K02_G09_DPPL2.md`](docs/M8/K02_G09_DPPL2.md) dan [`docs/M6/K02_G09_APL.md`](docs/M6/K02_G09_APL.md).

## 1. Gambar arsitektur

Pola **Layered Architecture** empat lapisan (dipilih karena cocok dengan kerangka ECB / DPPL):

```text
+-------------------+  boleh  +-------------------+  boleh  +---------------------+
| presentation      |  ->    | control           |  ->    | entity              |
| (C01-C16 Form)    |        | (C17-C29 Manager)|         | (C30-C55 Entity)    |
+-------------------+        +-------------------+         +---------------------+
        ^                          ^                            ^
        |                          |                            |
        | hanya memanggil          | hanya memanggil            | hanya antar entity
        | control + entity        | entity + dataaccess        |
+-------------------+        +-------------------+         +---------------------+
| dataaccess       |        | business/common  |         | database            |
| (C56-C64 Boundary |<-------| (KoneksiBasis   |<------- | (PostgreSQL 15)     |
|  + C15 SATUSEHATG|        |  Database,        |         |                     |
|   Gateway)        |        |  SesiAktif,       |         |                     |
+-------------------+        |  Pengaturan...)   |         |                     |
                            +-------------------+         +---------------------+
```

- Boundary (dataaccess) berdiri di antara control dan database, bukan antara control dan entity (aturan ECB).
- `Manager` tidak menyimpan `DataBoundary` sebagai atribut; ia membuka koneksi per operasi lalu meneruskannya.

## 2. Teknologi

| Komponen | Pilihan |
|---|---|
| Bahasa / runtime | Java 21 (LTS), `maven.compiler.release=21` |
| UI | JavaFX 21.0.5 (`javafx-controls`, `javafx-fxml`), JavaFX Maven plugin 0.0.8, main class `sehati.Main` |
| Basis data | PostgreSQL 15 via JDBC (`org.postgresql:postgresql:42.7.4`), Docker Compose (`postgres:15`), akun aplikasi `sehati_app` |
| Laporan | Apache PDFBox 3.0.3 |
| Auth / hash | jBCrypt 0.4 |
| Test | JUnit 5.11.3, Surefire 3.5.2 |
| Build | Maven, compiler plugin 3.13.0, surefire 3.5.2 |

## 3. Struktur folder

Root repo → `src/` adalah **root Maven project** (kode + `pom.xml` + `db/` + `src/main/...` + `tests/`):

```text
src/
├── pom.xml
├── docker-compose.yml
├── .env.example
├── db/                          # schema.sql, seed.sql, uji-kueri.sql, sandi-aplikasi.sh
├── src/
│   └── main/
│       ├── java/sehati/
│       │   ├── Main.java
│       │   ├── presentation/      # C01-C16: *Form (JavaFX + FXML)
│       │   ├── business/control/  # C17-C29: *Manager
│       │   ├── business/entity/   # C30-C55: *Entity
│       │   ├── dataaccess/        # C56-C64: *Boundary, SATUSEHATGateway, ResponsKirim
│       │   └── common/             # KoneksiBasisData, PengaturanAplikasi, SesiAktif
│       └── resources/sehati/
│           ├── fxml/              # satu FXML per Form
│           └── css/sehati.css
└── tests/
    └── sehati/                    # JUnit 5 (business, dataaccess, common, presentation)
```

Catatan: `README` aplikasi ada di `src/README.md` (wajib, issue #24).

## 4. Aturan antar lapisan (DPPL Subbab 6.1)

| Lapisan | Boleh memanggil | Dilarang |
|---|---|---|
| `presentation` | `control`, `entity` sebagai data tampilan | SQL, `java.sql`, `Data*Boundary`, aturan bisnis |
| `control` | `entity`, `dataaccess` | `javafx.*`, menulis SQL |
| `entity` | sesama `entity` | `javafx.*`, `java.sql`, `dataaccess` |
| `dataaccess` | `entity` | `javafx.*`, `control` |

Pemetaan kelas per lapisan (DPPL Tabel 4.1):

| Kelas | Nama | Lapisan |
|---|---|---|
| C01–C16 | `*Form` | presentation |
| C17–C29 | `*Manager` | control |
| C30–C55 | `*Entity` | entity |
| C56–C64 | `*Boundary` (+ C15 `SATUSEHATGateway`) | dataaccess |
| — | `BasisDataSEHATI` (database layer) | database |

Catatan: sembilan DataBoundary (C56 s.d. C64) + `SATUSEHATGateway` (C15) membentuk lapisan Data Access; komponen database `BasisDataSEHATI` berada di bawahnya.

## 5. Alur satu operasi

Form (presentation) → Manager (control) → `KoneksiBasisData.buka()` → `new DataBoundary(koneksi)` → Entity (business) → kueri lewat `PreparedStatement` di Boundary → COMMIT/ROLLBACK → `SesiAktif.idPenggunaSesi()` → audit lewat `DataAkunBoundary`.

- **Transaksi:** satu koneksi, auto-commit mati, COMMIT jika sukses, ROLLBACK jika gagal.
- **Penolakan:** `IllegalArgumentException` (input salah), `IllegalStateException` (keadaan data tidak mengizinkan). Pesan ditampilkan oleh Form.
- **Error teknis:** `SQLException` tidak ditelan; dicatat via `java.util.logging`, lalu error yang dapat dipahami user dikembalikan.
- **Log:** `java.util.logging` (`Logger` per kelas).

## 6. Basis data

- Schema PostgreSQL 15: 28 tabel dengan foreign key, `UNIQUE`, `CHECK`, indeks `GIN pg_trgm`.
- JSONB untuk data lintas sistem (KF20); MVCC + baris kunci untuk konkurensi (KF23, KNF11).
- Peran basis data: `sehati_app`.
- 78 kueri DPPL ber-ID: `Q_Cxx_xx` (mis. `Q_C59_01` s.d. `Q_C63_03`).
- 74 algoritma DPPL ber-ID: `Algo-Cxx-yy`.
- Seed fiktif; skema & seed via Docker Compose; uji kueri di `src/db/uji-kueri.sql`.

## 7. Integrasi eksternal

- **SATUSEHAT:** `SATUSEHATGateway` (C15) + `DataSinkronisasiBoundary` (C63) + `BundelFHIREntity` (C54); format FHIR R4; store-and-forward (sinkronisasi tunda); enkripsi AES-GCM + PBKDF2 600k (Algo-C63-01).
- **Laporan:** PDFBox (ekspor laporan periodik, UC10).
- **Akun:** jBCrypt (Algo-C30-01), sesi aktif, jejak audit.

## 8. Catatan implementasi

- Pengujian basis data di-gate `DB_UJI_URL` (opsional); CI menjalankan `mvn -B test` pada folder `src/` (lihat `.github/workflows/ci.yml`).
- Proses berat (query besar, render list panjang) tidak di JavaFX Application Thread; pakai `Task` dari `javafx.concurrent`.
