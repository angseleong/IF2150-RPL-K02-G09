-- Skema basis data SEHATI untuk PostgreSQL 15.
-- Nama tabel dan kolom mengikuti kesepakatan 4 BAB 5 DPPL. Kueri pemakainya ada di DPPL Subbab 5.56 sampai 5.64.
-- Dijalankan otomatis oleh docker-compose saat volume basis data baru dibuat.

-- Indeks trigram untuk pencarian parsial pasien dan kode ICD-10 (Algo-C57-01, Q-C59-11).
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================================================
-- Akun, sesi, dan audit (C56)
-- ============================================================================

CREATE TABLE pengguna (
    id_pengguna     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nama_pengguna   VARCHAR(50)  NOT NULL UNIQUE,
    kata_sandi_hash VARCHAR(60)  NOT NULL,
    peran           VARCHAR(30)  NOT NULL
        CHECK (peran IN ('Petugas Administrasi', 'Perawat', 'Dokter', 'Petugas Farmasi')),
    status          VARCHAR(10)  NOT NULL DEFAULT 'aktif'
        CHECK (status IN ('aktif', 'nonaktif'))
);

-- id_sesi dibangkitkan di Java sebagai UUID (Q-C56-06).
CREATE TABLE sesi (
    id_sesi      UUID      PRIMARY KEY,
    id_pengguna  INTEGER   NOT NULL REFERENCES pengguna (id_pengguna),
    waktu_mulai  TIMESTAMP NOT NULL,
    status_aktif BOOLEAN   NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_sesi_pengguna_aktif ON sesi (id_pengguna) WHERE status_aktif;

-- Tanpa foreign key ke pengguna karena id 0 dipakai untuk nama yang tidak dikenal (Q-C56-08, Algo-C17-01).
CREATE TABLE audit (
    id_audit    BIGINT    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_pengguna INTEGER   NOT NULL,
    waktu       TIMESTAMP NOT NULL,
    deskripsi   TEXT      NOT NULL
);

-- ============================================================================
-- Pasien dan rekam medis (C57)
-- ============================================================================

-- Sumber nomor rekam medis RM-000001 dan seterusnya (Q-C57-04).
CREATE SEQUENCE seq_nomor_rm;

CREATE TABLE pasien (
    id_pasien         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nik               VARCHAR(16)  NOT NULL UNIQUE CHECK (nik ~ '^[0-9]{16}$'),
    nomor_rekam_medis VARCHAR(16)  NOT NULL UNIQUE CHECK (nomor_rekam_medis ~ '^RM-[0-9]{6,}$'),
    nama              VARCHAR(100) NOT NULL,
    tanggal_lahir     DATE         NOT NULL,
    jenis_kelamin     VARCHAR(10)  NOT NULL CHECK (jenis_kelamin IN ('Laki-laki', 'Perempuan')),
    alamat            TEXT         NOT NULL,
    nomor_telepon     VARCHAR(20)
);

-- Pola '%kata%' tidak dapat memakai B-tree, jadi ketiga kolom pencarian memakai GIN trigram (Algo-C57-01, KNF01).
CREATE INDEX idx_pasien_nik_trgm ON pasien USING GIN (nik gin_trgm_ops);
CREATE INDEX idx_pasien_nama_trgm ON pasien USING GIN (nama gin_trgm_ops);
CREATE INDEX idx_pasien_nomor_rm_trgm ON pasien USING GIN (nomor_rekam_medis gin_trgm_ops);

-- Satu pasien memiliki tepat satu rekam medis (Q-C57-06).
CREATE TABLE rekam_medis (
    id_rekam_medis INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_pasien      INTEGER NOT NULL UNIQUE REFERENCES pasien (id_pasien)
);

-- Entri hasil RekamMedisEntity.tambahEntri(), hanya ditambah dan tidak pernah diubah (Q-C57-11).
CREATE TABLE catatan_rekam_medis (
    id_catatan     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_rekam_medis INTEGER   NOT NULL REFERENCES rekam_medis (id_rekam_medis),
    waktu          TIMESTAMP NOT NULL,
    isi            TEXT      NOT NULL
);

CREATE INDEX idx_catatan_rekam_medis ON catatan_rekam_medis (id_rekam_medis);

-- ============================================================================
-- Poli, kunjungan, dan antrean (C58)
-- ============================================================================

CREATE TABLE poli (
    id_poli         INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nama_poli       VARCHAR(50) NOT NULL UNIQUE,
    urutan_terakhir INTEGER     NOT NULL DEFAULT 0 CHECK (urutan_terakhir >= 0),
    tanggal_urutan  DATE        NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE kunjungan (
    id_kunjungan  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_pasien     INTEGER     NOT NULL REFERENCES pasien (id_pasien),
    id_poli       INTEGER     NOT NULL REFERENCES poli (id_poli),
    tanggal       DATE        NOT NULL,
    nomor_antrean INTEGER     NOT NULL CHECK (nomor_antrean > 0),
    status        VARCHAR(30) NOT NULL CHECK (status IN (
        'menunggu skrining', 'sedang diskrining', 'tidak hadir',
        'siap diperiksa dokter', 'selesai diperiksa', 'selesai'
    )),
    -- Pengaman kedua bila penguncian kunciPoli() terlewat: nomor antrean tidak ganda per poli per hari (KF03, KF23).
    UNIQUE (id_poli, tanggal, nomor_antrean)
);

-- Satu kunjungan aktif per pasien per hari. Loket kedua yang membuka bersamaan ditolak (Q-C58-05, Q-C58-07).
CREATE UNIQUE INDEX uq_kunjungan_aktif ON kunjungan (id_pasien, tanggal) WHERE status <> 'selesai';
-- Riwayat tanda vital dan rekam medis per pasien (Algo-C59-01).
CREATE INDEX idx_kunjungan_pasien_tanggal ON kunjungan (id_pasien, tanggal);
-- Rekapitulasi laporan per rentang tanggal (Q-C62-01 sampai Q-C62-03).
CREATE INDEX idx_kunjungan_tanggal ON kunjungan (tanggal);

-- Hanya dua baris, "skrining" dan "apotek", yang dibuat di akhir berkas ini (Algo-C58-01).
CREATE TABLE antrean (
    id_antrean INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jenis      VARCHAR(10) NOT NULL UNIQUE CHECK (jenis IN ('skrining', 'apotek'))
);

-- id_entri berisi id_kunjungan untuk antrean skrining dan id_resep untuk antrean apotek, sehingga tanpa foreign key.
CREATE TABLE antrean_entri (
    id_antrean INTEGER NOT NULL REFERENCES antrean (id_antrean),
    urutan     INTEGER NOT NULL CHECK (urutan > 0),
    id_entri   INTEGER NOT NULL,
    PRIMARY KEY (id_antrean, urutan)
);

-- ============================================================================
-- Skrining, risiko, dan pemeriksaan (C59)
-- ============================================================================

-- Satu kunjungan memiliki satu skrining (Q-C59-01).
CREATE TABLE skrining (
    id_skrining  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_kunjungan INTEGER   NOT NULL UNIQUE REFERENCES kunjungan (id_kunjungan),
    keluhan_awal TEXT      NOT NULL,
    waktu        TIMESTAMP NOT NULL
);

-- Rentang sama dengan Algo-C20-02 sebagai pengaman kedua (Q-C59-02, KNF02). Gula darah boleh kosong (KF05).
CREATE TABLE tanda_vital (
    id_skrining  INTEGER          PRIMARY KEY REFERENCES skrining (id_skrining),
    sistolik     INTEGER          NOT NULL CHECK (sistolik BETWEEN 50 AND 300),
    diastolik    INTEGER          NOT NULL CHECK (diastolik BETWEEN 30 AND 200),
    berat_badan  DOUBLE PRECISION NOT NULL CHECK (berat_badan BETWEEN 1 AND 350),
    tinggi_badan DOUBLE PRECISION NOT NULL CHECK (tinggi_badan BETWEEN 30 AND 250),
    suhu         DOUBLE PRECISION NOT NULL CHECK (suhu BETWEEN 30 AND 45),
    nadi         INTEGER          NOT NULL CHECK (nadi BETWEEN 20 AND 250),
    gula_darah   INTEGER          CHECK (gula_darah BETWEEN 20 AND 1000),
    CHECK (diastolik < sistolik)
);

-- Tiga jenis risiko tetap, hanya nilainya yang boleh diubah (Q-C59-07, KF07).
CREATE TABLE ambang_risiko (
    id_ambang    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    jenis_risiko VARCHAR(20)      NOT NULL UNIQUE
        CHECK (jenis_risiko IN ('hipertensi', 'obesitas', 'indikasi diabetes')),
    nilai_batas  DOUBLE PRECISION NOT NULL CHECK (nilai_batas > 0)
);

CREATE TABLE penanda_risiko (
    id_penanda   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_kunjungan INTEGER     NOT NULL REFERENCES kunjungan (id_kunjungan),
    jenis_risiko VARCHAR(20) NOT NULL CHECK (jenis_risiko IN ('hipertensi', 'obesitas', 'indikasi diabetes')),
    tanggal      DATE        NOT NULL,
    status       VARCHAR(10) NOT NULL DEFAULT 'aktif' CHECK (status IN ('aktif', 'dicabut'))
);

CREATE INDEX idx_penanda_kunjungan ON penanda_risiko (id_kunjungan);

-- Data master ICD-10, hanya dibaca aplikasi.
CREATE TABLE kode_icd10 (
    kode      VARCHAR(10)  PRIMARY KEY,
    deskripsi VARCHAR(255) NOT NULL,
    bab       VARCHAR(150) NOT NULL
);

CREATE INDEX idx_kode_icd10_deskripsi_trgm ON kode_icd10 USING GIN (deskripsi gin_trgm_ops);

-- UNIQUE id_kunjungan menjadi sasaran ON CONFLICT pada Q-C59-12.
CREATE TABLE pemeriksaan (
    id_pemeriksaan INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_kunjungan   INTEGER NOT NULL UNIQUE REFERENCES kunjungan (id_kunjungan),
    anamnesis      TEXT    NOT NULL,
    tindakan       TEXT    NOT NULL,
    terkunci       BOOLEAN NOT NULL DEFAULT FALSE
);

-- Diagnosis hanya dapat berupa kode ICD-10 yang terdaftar (Q-C59-13, KF10).
CREATE TABLE diagnosis (
    id_diagnosis   INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_pemeriksaan INTEGER      NOT NULL UNIQUE REFERENCES pemeriksaan (id_pemeriksaan),
    kode_icd10     VARCHAR(10)  NOT NULL REFERENCES kode_icd10 (kode),
    deskripsi      VARCHAR(255) NOT NULL
);

CREATE TABLE jadwal_kontrol (
    id_jadwal       INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_pasien       INTEGER     NOT NULL REFERENCES pasien (id_pasien),
    tanggal_rencana DATE        NOT NULL,
    status          VARCHAR(10) NOT NULL DEFAULT 'terjadwal' CHECK (status IN ('terjadwal', 'selesai'))
);

CREATE INDEX idx_jadwal_kontrol_terjadwal ON jadwal_kontrol (tanggal_rencana) WHERE status = 'terjadwal';

-- ============================================================================
-- Farmasi dan resep (C61)
-- ============================================================================

-- stok adalah saldo seluruh bets. CHECK menjadi pengaman terakhir terhadap stok negatif (Q-C61-04).
CREATE TABLE obat (
    id_obat        INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nama_obat      VARCHAR(100) NOT NULL,
    sediaan        VARCHAR(50)  NOT NULL,
    stok           INTEGER      NOT NULL DEFAULT 0 CHECK (stok >= 0),
    ambang_minimum INTEGER      NOT NULL DEFAULT 0 CHECK (ambang_minimum >= 0)
);

CREATE TABLE penerimaan_obat (
    id_penerimaan  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nomor_faktur   VARCHAR(50) NOT NULL,
    tanggal_terima DATE        NOT NULL
);

-- Kunci primer (id_obat, nomor_bets) menolak nomor bets yang sudah tercatat (Q-C61-07, Algo-C61-01).
CREATE TABLE bets_obat (
    id_obat             INTEGER     NOT NULL REFERENCES obat (id_obat),
    nomor_bets          VARCHAR(30) NOT NULL,
    jumlah              INTEGER     NOT NULL CHECK (jumlah >= 0),
    tanggal_kedaluwarsa DATE        NOT NULL,
    id_penerimaan       INTEGER     NOT NULL REFERENCES penerimaan_obat (id_penerimaan),
    PRIMARY KEY (id_obat, nomor_bets)
);

-- Peringatan bets menjelang kedaluwarsa (Q-C61-08).
CREATE INDEX idx_bets_obat_kedaluwarsa ON bets_obat (tanggal_kedaluwarsa) WHERE jumlah > 0;

CREATE TABLE resep (
    id_resep     INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_kunjungan INTEGER     NOT NULL REFERENCES kunjungan (id_kunjungan),
    waktu_masuk  TIMESTAMP   NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'menunggu'
        CHECK (status IN ('menunggu', 'selesai dilayani', 'dikembalikan')),
    catatan      TEXT
);

CREATE INDEX idx_resep_kunjungan ON resep (id_kunjungan);
-- Antrean resep apotek (Q-C61-12).
CREATE INDEX idx_resep_menunggu ON resep (waktu_masuk, id_resep) WHERE status = 'menunggu';

-- id_resep mewujudkan komposisi ResepEntity-ItemResepEntity (Q-C61-10).
CREATE TABLE item_resep (
    id_item      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_resep     INTEGER      NOT NULL REFERENCES resep (id_resep) ON DELETE CASCADE,
    id_obat      INTEGER      NOT NULL REFERENCES obat (id_obat),
    dosis        VARCHAR(50)  NOT NULL,
    jumlah       INTEGER      NOT NULL CHECK (jumlah > 0),
    aturan_pakai VARCHAR(100) NOT NULL
);

CREATE INDEX idx_item_resep_resep ON item_resep (id_resep, id_obat);

-- ============================================================================
-- Daftar pantau (C60)
-- ============================================================================

CREATE TABLE daftar_pantau (
    id_entri            INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_pasien           INTEGER     NOT NULL REFERENCES pasien (id_pasien),
    jenis_risiko        VARCHAR(20) NOT NULL
        CHECK (jenis_risiko IN ('hipertensi', 'obesitas', 'indikasi diabetes', 'kunjungan ulang')),
    status_tindak_lanjut VARCHAR(30) NOT NULL DEFAULT 'belum ditindaklanjuti'
        CHECK (status_tindak_lanjut IN ('belum ditindaklanjuti', 'perlu dihubungi ulang', 'selesai ditindaklanjuti')),
    id_penanda          INTEGER REFERENCES penanda_risiko (id_penanda),
    -- Setiap jadwal kontrol memicu tepat satu entri (Algo-C60-01).
    id_jadwal           INTEGER UNIQUE REFERENCES jadwal_kontrol (id_jadwal),
    -- Entri dipicu tepat satu penanda atau satu jadwal (Q-C60-02).
    CHECK ((id_penanda IS NULL) <> (id_jadwal IS NULL))
);

CREATE INDEX idx_daftar_pantau_terbuka ON daftar_pantau (id_entri)
    WHERE status_tindak_lanjut <> 'selesai ditindaklanjuti';

CREATE TABLE tindak_lanjut (
    id_tindak_lanjut  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_entri          INTEGER     NOT NULL REFERENCES daftar_pantau (id_entri),
    jenis_kontak      VARCHAR(20) NOT NULL CHECK (jenis_kontak IN ('telepon', 'pesan singkat', 'kunjungan rumah')),
    hasil_upaya       VARCHAR(20) NOT NULL CHECK (hasil_upaya IN ('terhubung', 'tidak terhubung')),
    status_kedatangan VARCHAR(20) NOT NULL
        CHECK (status_kedatangan IN ('akan datang', 'sudah datang', 'tidak dapat datang', 'belum diketahui')),
    waktu             TIMESTAMP   NOT NULL
);

CREATE INDEX idx_tindak_lanjut_entri ON tindak_lanjut (id_entri, waktu);

-- ============================================================================
-- Laporan, sinkronisasi, dan pencadangan (C62 sampai C64)
-- ============================================================================

CREATE TABLE laporan (
    id_laporan    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tanggal_mulai DATE       NOT NULL,
    tanggal_akhir DATE       NOT NULL,
    format        VARCHAR(3) NOT NULL CHECK (format IN ('PDF', 'CSV')),
    ringkasan     TEXT       NOT NULL,
    CHECK (tanggal_akhir >= tanggal_mulai)
);

-- JSONB menolak bundel yang bukan JSON utuh. Satu kunjungan menghasilkan satu bundel (Q-C63-01).
CREATE TABLE bundel_fhir (
    id_bundel    INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_kunjungan INTEGER     NOT NULL UNIQUE REFERENCES kunjungan (id_kunjungan),
    isi_bundel   JSONB       NOT NULL,
    status       VARCHAR(15) NOT NULL DEFAULT 'tertunda' CHECK (status IN ('tertunda', 'terkirim', 'gagal kirim')),
    alasan_gagal TEXT
);

CREATE INDEX idx_bundel_fhir_belum_terkirim ON bundel_fhir (id_bundel) WHERE status <> 'terkirim';

CREATE TABLE pencadangan (
    id_pencadangan INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    waktu          TIMESTAMP   NOT NULL,
    status         VARCHAR(20) NOT NULL CHECK (status IN ('berhasil', 'gagal', 'gagal verifikasi')),
    lokasi_media   TEXT        NOT NULL,
    ukuran_berkas  BIGINT      NOT NULL DEFAULT 0 CHECK (ukuran_berkas >= 0)
);

CREATE INDEX idx_pencadangan_waktu ON pencadangan (waktu DESC);

-- ============================================================================
-- Data wajib dan hak akses
-- ============================================================================

-- Dua antrean dibuat sekali saat basis data dipasang (Algo-C58-01).
INSERT INTO antrean (jenis) VALUES ('skrining'), ('apotek');

-- Aplikasi masuk dengan satu peran, sehati_app. Kata sandinya diatur db/sandi-aplikasi.sh dari .env.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'sehati_app') THEN
        CREATE ROLE sehati_app LOGIN;
    END IF;
END
$$;

GRANT USAGE ON SCHEMA public TO sehati_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO sehati_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO sehati_app;
-- Jejak audit tidak dapat diubah atau dihapus dari aplikasi (aturan 5 Subbab 5.56, KF16, KNF05).
REVOKE UPDATE, DELETE, TRUNCATE ON audit FROM sehati_app;
