-- Seed besar untuk pengujian profil (issue #23).
-- Jalankan setelah src/db/schema.sql dan src/db/seed.sql.
-- Membuat 50.000 pasien, masing-masing dengan satu rekam medis,
-- satu kunjungan, satu pemeriksaan, dan satu diagnosis.
--
-- NIK sengaja memakai awalan 9998 supaya tidak bentrok dengan seed.sql
-- yang memakai awalan 9999. Script ini dirancang untuk dijalankan sekali.

BEGIN;

INSERT INTO pasien (nik, nomor_rekam_medis, nama, tanggal_lahir, jenis_kelamin, alamat, nomor_telepon)
SELECT
    '9998' || lpad(i::text, 12, '0'),
    'RM-9' || lpad(i::text, 6, '0'),
    'Pasien Uji ' || i,
    DATE '1950-01-01' + (i % 25000),
    CASE WHEN i % 2 = 0 THEN 'Laki-laki' ELSE 'Perempuan' END,
    'Jl. Uji No. ' || i,
    '0812' || lpad(i::text, 8, '0')
FROM generate_series(1, 50000) AS i;

INSERT INTO rekam_medis (id_pasien)
SELECT id_pasien
FROM pasien
WHERE nik LIKE '9998%';

WITH poli_urut AS (
    SELECT
        id_poli,
        row_number() OVER (ORDER BY id_poli) - 1 AS urutan,
        count(*) OVER () AS total
    FROM poli
)
INSERT INTO kunjungan (id_pasien, id_poli, tanggal, nomor_antrean, status)
SELECT
    p.id_pasien,
    pu.id_poli,
    DATE '2025-01-01' + (substr(p.nik, 5)::int % 365),
    substr(p.nik, 5)::int,
    CASE
        WHEN substr(p.nik, 5)::int % 10 = 0 THEN 'tidak hadir'
        ELSE 'selesai'
    END
FROM pasien p
JOIN poli_urut pu ON pu.urutan = (substr(p.nik, 5)::int % pu.total)
WHERE p.nik LIKE '9998%';

INSERT INTO pemeriksaan (id_kunjungan, anamnesis, tindakan, terkunci)
SELECT
    k.id_kunjungan,
    'Anamnesis uji untuk kunjungan ' || k.id_kunjungan,
    'Tindakan uji',
    true
FROM kunjungan k
JOIN pasien p ON p.id_pasien = k.id_pasien
WHERE p.nik LIKE '9998%';

WITH icd AS (
    SELECT
        kode,
        deskripsi,
        row_number() OVER (ORDER BY kode) - 1 AS urutan
    FROM kode_icd10
), total AS (
    SELECT count(*) AS n FROM kode_icd10
)
INSERT INTO diagnosis (id_pemeriksaan, kode_icd10, deskripsi)
SELECT
    pm.id_pemeriksaan,
    icd.kode,
    icd.deskripsi
FROM pemeriksaan pm
JOIN kunjungan k ON k.id_kunjungan = pm.id_kunjungan
JOIN pasien p ON p.id_pasien = k.id_pasien
JOIN total t ON true
JOIN icd ON icd.urutan = (substr(p.nik, 5)::int % t.n)
WHERE p.nik LIKE '9998%';

COMMIT;

ANALYZE;
