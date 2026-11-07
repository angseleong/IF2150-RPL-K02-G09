-- Uji kompilasi 78 kueri DPPL (Subbab 5.56 sampai 5.64) terhadap schema.sql dengan PREPARE.
-- PREPARE memeriksa sintaks, nama tabel dan kolom, serta tipe parameter tanpa menjalankan kueri.
-- Tanda ? pada DPPL ditulis sebagai $1, $2, dan seterusnya. Dijalankan dari folder src/:
--   docker compose exec db sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f /opt/sehati/uji-kueri.sql'

\set ON_ERROR_STOP on
\set QUIET on

\echo Q-C56-01
PREPARE q_c56_01 AS
    SELECT id_pengguna, nama_pengguna, kata_sandi_hash, peran, status
    FROM pengguna WHERE nama_pengguna = $1;

\echo Q-C56-02
PREPARE q_c56_02 AS
    SELECT id_pengguna, nama_pengguna, kata_sandi_hash, peran, status
    FROM pengguna WHERE id_pengguna = $1;

\echo Q-C56-03
PREPARE q_c56_03 AS
    SELECT EXISTS (SELECT 1 FROM pengguna WHERE nama_pengguna = $1);

\echo Q-C56-04
PREPARE q_c56_04 AS
    INSERT INTO pengguna (nama_pengguna, kata_sandi_hash, peran, status)
    VALUES ($1, $2, $3, $4) RETURNING id_pengguna;

\echo Q-C56-05
PREPARE q_c56_05 AS
    UPDATE pengguna SET kata_sandi_hash = $1, peran = $2, status = $3
    WHERE id_pengguna = $4;

\echo Q-C56-06
PREPARE q_c56_06 AS
    INSERT INTO sesi (id_sesi, id_pengguna, waktu_mulai, status_aktif)
    VALUES ($1, $2, $3, $4)
    ON CONFLICT (id_sesi) DO UPDATE SET status_aktif = EXCLUDED.status_aktif;

\echo Q-C56-07
PREPARE q_c56_07 AS
    UPDATE sesi SET status_aktif = FALSE
    WHERE id_pengguna = $1 AND status_aktif;

\echo Q-C56-08
PREPARE q_c56_08 AS
    INSERT INTO audit (id_pengguna, waktu, deskripsi) VALUES ($1, $2, $3);

\echo Q-C57-01
PREPARE q_c57_01 AS
    SELECT id_pasien, nik, nomor_rekam_medis, nama, tanggal_lahir,
    jenis_kelamin, alamat, nomor_telepon
    FROM pasien
    WHERE nik LIKE $1 OR nomor_rekam_medis ILIKE $2 OR nama ILIKE $3
    ORDER BY nama LIMIT 50;

\echo Q-C57-02
PREPARE q_c57_02 AS
    SELECT id_pasien, nik, nomor_rekam_medis, nama, tanggal_lahir,
    jenis_kelamin, alamat, nomor_telepon
    FROM pasien WHERE id_pasien = $1;

\echo Q-C57-03
PREPARE q_c57_03 AS
    SELECT EXISTS (SELECT 1 FROM pasien WHERE nik = $1);

\echo Q-C57-04
PREPARE q_c57_04 AS
    SELECT concat('RM-', lpad(nextval('seq_nomor_rm')::text, 6, '0'));

\echo Q-C57-05
PREPARE q_c57_05 AS
    INSERT INTO pasien (nik, nomor_rekam_medis, nama, tanggal_lahir,
    jenis_kelamin, alamat, nomor_telepon)
    VALUES ($1, $2, $3, $4, $5, $6, $7) RETURNING id_pasien;

\echo Q-C57-06
PREPARE q_c57_06 AS
    INSERT INTO rekam_medis (id_pasien) VALUES ($1) RETURNING id_rekam_medis;

\echo Q-C57-07
PREPARE q_c57_07 AS
    UPDATE pasien SET nik = $1, nama = $2, tanggal_lahir = $3, jenis_kelamin = $4,
    alamat = $5, nomor_telepon = $6
    WHERE id_pasien = $7;

\echo Q-C57-08
PREPARE q_c57_08 AS
    SELECT id_rekam_medis FROM rekam_medis WHERE id_pasien = $1;

\echo Q-C57-09
PREPARE q_c57_09 AS
    SELECT d.id_diagnosis, d.kode_icd10, d.deskripsi
    FROM diagnosis d
    JOIN pemeriksaan pm ON pm.id_pemeriksaan = d.id_pemeriksaan
    JOIN kunjungan k ON k.id_kunjungan = pm.id_kunjungan
    WHERE k.id_pasien = $1 AND pm.terkunci
    ORDER BY k.tanggal DESC;

\echo Q-C57-10
PREPARE q_c57_10 AS
    SELECT k.tanggal, o.nama_obat, o.sediaan, ir.jumlah, ir.dosis, ir.aturan_pakai
    FROM item_resep ir
    JOIN resep r ON r.id_resep = ir.id_resep
    JOIN kunjungan k ON k.id_kunjungan = r.id_kunjungan
    JOIN obat o ON o.id_obat = ir.id_obat
    WHERE k.id_pasien = $1 AND r.status = 'selesai dilayani'
    ORDER BY k.tanggal DESC, o.nama_obat;

\echo Q-C57-11
PREPARE q_c57_11 AS
    INSERT INTO catatan_rekam_medis (id_rekam_medis, waktu, isi)
    VALUES ($1, $2, $3);

\echo Q-C58-01
PREPARE q_c58_01 AS
    SELECT id_poli, nama_poli, urutan_terakhir, tanggal_urutan
    FROM poli ORDER BY nama_poli;

\echo Q-C58-02
PREPARE q_c58_02 AS
    SELECT id_poli, nama_poli, urutan_terakhir, tanggal_urutan
    FROM poli WHERE id_poli = $1 FOR UPDATE;

\echo Q-C58-03
PREPARE q_c58_03 AS
    INSERT INTO poli (nama_poli, urutan_terakhir, tanggal_urutan)
    VALUES ($1, 0, CURRENT_DATE) RETURNING id_poli;

\echo Q-C58-04
PREPARE q_c58_04 AS
    UPDATE poli SET nama_poli = $1, urutan_terakhir = $2, tanggal_urutan = $3
    WHERE id_poli = $4;

\echo Q-C58-05
PREPARE q_c58_05 AS
    SELECT id_kunjungan, id_pasien, id_poli, tanggal, nomor_antrean, status
    FROM kunjungan
    WHERE id_pasien = $1 AND tanggal = $2 AND status <> 'selesai';

\echo Q-C58-06
PREPARE q_c58_06 AS
    SELECT id_kunjungan, id_pasien, id_poli, tanggal, nomor_antrean, status
    FROM kunjungan WHERE id_kunjungan = $1;

\echo Q-C58-07
PREPARE q_c58_07 AS
    INSERT INTO kunjungan (id_pasien, id_poli, tanggal, nomor_antrean, status)
    VALUES ($1, $2, $3, $4, $5) RETURNING id_kunjungan;

\echo Q-C58-08
PREPARE q_c58_08 AS
    UPDATE kunjungan SET status = $1 WHERE id_kunjungan = $2;

\echo Q-C58-09
PREPARE q_c58_09 AS
    SELECT id_antrean, jenis FROM antrean WHERE jenis = $1 FOR UPDATE;

\echo Q-C58-10
PREPARE q_c58_10 AS
    SELECT id_entri FROM antrean_entri WHERE id_antrean = $1 ORDER BY urutan;

\echo Q-C58-11
PREPARE q_c58_11 AS
    DELETE FROM antrean_entri WHERE id_antrean = $1;

\echo Q-C58-12
PREPARE q_c58_12 AS
    INSERT INTO antrean_entri (id_antrean, urutan, id_entri) VALUES ($1, $2, $3);

\echo Q-C59-01
PREPARE q_c59_01 AS
    INSERT INTO skrining (id_kunjungan, keluhan_awal, waktu) VALUES ($1, $2, $3)
    RETURNING id_skrining;

\echo Q-C59-02
PREPARE q_c59_02 AS
    INSERT INTO tanda_vital (id_skrining, sistolik, diastolik, berat_badan,
    tinggi_badan, suhu, nadi, gula_darah)
    VALUES ($1, $2, $3, $4, $5, $6, $7, $8);

\echo Q-C59-03
PREPARE q_c59_03 AS
    SELECT s.id_skrining, s.id_kunjungan, s.keluhan_awal, s.waktu,
    tv.sistolik, tv.diastolik, tv.berat_badan, tv.tinggi_badan,
    tv.suhu, tv.nadi, tv.gula_darah
    FROM skrining s JOIN tanda_vital tv ON tv.id_skrining = s.id_skrining
    WHERE s.id_kunjungan = $1;

\echo Q-C59-04
PREPARE q_c59_04 AS
    SELECT tv.sistolik, tv.diastolik, tv.berat_badan, tv.tinggi_badan,
    tv.suhu, tv.nadi, tv.gula_darah
    FROM tanda_vital tv
    JOIN skrining s ON s.id_skrining = tv.id_skrining
    JOIN kunjungan k ON k.id_kunjungan = s.id_kunjungan
    WHERE k.id_pasien = $1 AND k.tanggal < CURRENT_DATE
    ORDER BY s.waktu DESC LIMIT $2;

\echo Q-C59-05
PREPARE q_c59_05 AS
    SELECT s.waktu, tv.sistolik, tv.diastolik, tv.berat_badan, tv.tinggi_badan,
    tv.suhu, tv.nadi, tv.gula_darah
    FROM tanda_vital tv
    JOIN skrining s ON s.id_skrining = tv.id_skrining
    JOIN kunjungan k ON k.id_kunjungan = s.id_kunjungan
    WHERE k.id_pasien = $1
    ORDER BY s.waktu;

\echo Q-C59-06
PREPARE q_c59_06 AS
    SELECT id_ambang, jenis_risiko, nilai_batas FROM ambang_risiko
    ORDER BY jenis_risiko;

\echo Q-C59-07
PREPARE q_c59_07 AS
    UPDATE ambang_risiko SET nilai_batas = $1 WHERE id_ambang = $2;

\echo Q-C59-08
PREPARE q_c59_08 AS
    INSERT INTO penanda_risiko (id_kunjungan, jenis_risiko, tanggal, status)
    VALUES ($1, $2, $3, $4) RETURNING id_penanda;

\echo Q-C59-09
PREPARE q_c59_09 AS
    UPDATE penanda_risiko SET status = $1 WHERE id_penanda = $2;

\echo Q-C59-10
PREPARE q_c59_10 AS
    SELECT id_penanda, id_kunjungan, jenis_risiko, tanggal, status
    FROM penanda_risiko
    WHERE id_kunjungan = $1 AND status = 'aktif'
    ORDER BY jenis_risiko;

\echo Q-C59-11
PREPARE q_c59_11 AS
    SELECT kode, deskripsi, bab FROM kode_icd10
    WHERE kode ILIKE $1 OR deskripsi ILIKE $2
    ORDER BY (kode ILIKE $3) DESC, kode LIMIT 20;

\echo Q-C59-12
PREPARE q_c59_12 AS
    INSERT INTO pemeriksaan (id_kunjungan, anamnesis, tindakan, terkunci)
    VALUES ($1, $2, $3, $4)
    ON CONFLICT (id_kunjungan) DO UPDATE
    SET anamnesis = EXCLUDED.anamnesis, tindakan = EXCLUDED.tindakan,
    terkunci = EXCLUDED.terkunci
    WHERE pemeriksaan.terkunci = FALSE
    RETURNING id_pemeriksaan;

\echo Q-C59-13
PREPARE q_c59_13 AS
    INSERT INTO diagnosis (id_pemeriksaan, kode_icd10, deskripsi) VALUES ($1, $2, $3)
    ON CONFLICT (id_pemeriksaan) DO UPDATE
    SET kode_icd10 = EXCLUDED.kode_icd10, deskripsi = EXCLUDED.deskripsi;

\echo Q-C59-14
PREPARE q_c59_14 AS
    SELECT id_pemeriksaan, id_kunjungan, anamnesis, tindakan, terkunci
    FROM pemeriksaan WHERE id_kunjungan = $1;

\echo Q-C59-15
PREPARE q_c59_15 AS
    INSERT INTO jadwal_kontrol (id_pasien, tanggal_rencana, status)
    VALUES ($1, $2, $3) RETURNING id_jadwal;

\echo Q-C59-16
PREPARE q_c59_16 AS
    UPDATE jadwal_kontrol SET status = $1 WHERE id_jadwal = $2;

\echo Q-C59-17
PREPARE q_c59_17 AS
    SELECT jk.id_jadwal, jk.id_pasien, jk.tanggal_rencana, jk.status
    FROM jadwal_kontrol jk
    WHERE jk.status = 'terjadwal'
    AND NOT EXISTS (SELECT 1 FROM daftar_pantau dp WHERE dp.id_jadwal = jk.id_jadwal)
    ORDER BY jk.tanggal_rencana, jk.id_jadwal;

\echo Q-C60-01
PREPARE q_c60_01 AS
    SELECT dp.id_entri, dp.id_pasien, dp.jenis_risiko, dp.status_tindak_lanjut,
    COALESCE(pr.tanggal, jk.tanggal_rencana) AS tanggal_pemicu
    FROM daftar_pantau dp
    LEFT JOIN penanda_risiko pr ON pr.id_penanda = dp.id_penanda
    LEFT JOIN jadwal_kontrol jk ON jk.id_jadwal = dp.id_jadwal
    WHERE dp.status_tindak_lanjut <> 'selesai ditindaklanjuti'
    ORDER BY tanggal_pemicu, dp.id_entri;

\echo Q-C60-02
PREPARE q_c60_02 AS
    INSERT INTO daftar_pantau (id_pasien, jenis_risiko, status_tindak_lanjut,
    id_penanda, id_jadwal)
    VALUES ($1, $2, $3, $4, $5) RETURNING id_entri;

\echo Q-C60-03
PREPARE q_c60_03 AS
    SELECT pr.id_penanda FROM penanda_risiko pr
    JOIN kunjungan k ON k.id_kunjungan = pr.id_kunjungan
    WHERE k.id_pasien = $1 AND pr.jenis_risiko = $2
    ORDER BY pr.id_penanda DESC LIMIT 1;

\echo Q-C60-04
PREPARE q_c60_04 AS
    SELECT jk.id_jadwal FROM jadwal_kontrol jk
    WHERE jk.id_pasien = $1 AND jk.status = 'terjadwal'
    AND NOT EXISTS (SELECT 1 FROM daftar_pantau dp WHERE dp.id_jadwal = jk.id_jadwal)
    ORDER BY jk.tanggal_rencana, jk.id_jadwal LIMIT 1;

\echo Q-C60-05
PREPARE q_c60_05 AS
    UPDATE daftar_pantau SET status_tindak_lanjut = $1 WHERE id_entri = $2;

\echo Q-C60-06
PREPARE q_c60_06 AS
    INSERT INTO tindak_lanjut (id_entri, jenis_kontak, hasil_upaya,
    status_kedatangan, waktu)
    VALUES ($1, $2, $3, $4, $5) RETURNING id_tindak_lanjut;

\echo Q-C61-01
PREPARE q_c61_01 AS
    SELECT id_obat, nama_obat, sediaan, stok, ambang_minimum
    FROM obat ORDER BY nama_obat;

\echo Q-C61-02
PREPARE q_c61_02 AS
    SELECT id_obat, nama_obat, sediaan, stok, ambang_minimum
    FROM obat WHERE id_obat = $1 FOR UPDATE;

\echo Q-C61-03
PREPARE q_c61_03 AS
    SELECT nomor_bets, id_obat, jumlah, tanggal_kedaluwarsa
    FROM bets_obat WHERE id_obat = $1 AND jumlah > 0
    ORDER BY tanggal_kedaluwarsa, nomor_bets FOR UPDATE;

\echo Q-C61-04
PREPARE q_c61_04 AS
    UPDATE obat SET stok = $1 WHERE id_obat = $2;

\echo Q-C61-05
PREPARE q_c61_05 AS
    UPDATE bets_obat SET jumlah = $1 WHERE id_obat = $2 AND nomor_bets = $3;

\echo Q-C61-06
PREPARE q_c61_06 AS
    INSERT INTO penerimaan_obat (nomor_faktur, tanggal_terima) VALUES ($1, $2)
    RETURNING id_penerimaan;

\echo Q-C61-07
PREPARE q_c61_07 AS
    INSERT INTO bets_obat (id_obat, nomor_bets, jumlah, tanggal_kedaluwarsa,
    id_penerimaan) VALUES ($1, $2, $3, $4, $5);

\echo Q-C61-08
PREPARE q_c61_08 AS
    SELECT o.id_obat, o.nama_obat, o.sediaan, o.stok, o.ambang_minimum
    FROM obat o
    WHERE o.stok < o.ambang_minimum
    OR EXISTS (SELECT 1 FROM bets_obat b WHERE b.id_obat = o.id_obat
    AND b.jumlah > 0 AND b.tanggal_kedaluwarsa <= CURRENT_DATE + $1::int)
    ORDER BY o.nama_obat;

\echo Q-C61-09
PREPARE q_c61_09 AS
    INSERT INTO resep (id_kunjungan, waktu_masuk, status, catatan)
    VALUES ($1, $2, $3, $4) RETURNING id_resep;

\echo Q-C61-10
PREPARE q_c61_10 AS
    INSERT INTO item_resep (id_resep, id_obat, dosis, jumlah, aturan_pakai)
    VALUES ($1, $2, $3, $4, $5) RETURNING id_item;

\echo Q-C61-11
PREPARE q_c61_11 AS
    UPDATE resep SET status = $1, catatan = $2 WHERE id_resep = $3;

\echo Q-C61-12
PREPARE q_c61_12 AS
    SELECT id_resep, id_kunjungan, waktu_masuk, status, catatan FROM resep
    WHERE status = 'menunggu' ORDER BY waktu_masuk, id_resep;

\echo Q-C61-13
PREPARE q_c61_13 AS
    SELECT id_resep, id_kunjungan, waktu_masuk, status, catatan
    FROM resep WHERE id_resep = $1;

\echo Q-C61-14
PREPARE q_c61_14 AS
    SELECT id_item, id_resep, id_obat, dosis, jumlah, aturan_pakai
    FROM item_resep WHERE id_resep = ANY ($1)
    ORDER BY id_resep, id_obat;

\echo Q-C61-15
PREPARE q_c61_15 AS
    SELECT id_resep, id_kunjungan, waktu_masuk, status, catatan
    FROM resep WHERE id_resep = $1 FOR UPDATE;

\echo Q-C62-01
PREPARE q_c62_01 AS
    SELECT p.nama_poli, COUNT(*) AS jumlah
    FROM kunjungan k JOIN poli p ON p.id_poli = k.id_poli
    WHERE k.tanggal BETWEEN $1 AND $2
    GROUP BY p.nama_poli ORDER BY p.nama_poli;

\echo Q-C62-02
PREPARE q_c62_02 AS
    SELECT k.status, COUNT(*) AS jumlah FROM kunjungan k
    WHERE k.tanggal BETWEEN $1 AND $2
    GROUP BY k.status ORDER BY k.status;

\echo Q-C62-03
PREPARE q_c62_03 AS
    SELECT d.kode_icd10, i.deskripsi, COUNT(*) AS jumlah
    FROM diagnosis d
    JOIN pemeriksaan pm ON pm.id_pemeriksaan = d.id_pemeriksaan
    JOIN kunjungan k ON k.id_kunjungan = pm.id_kunjungan
    JOIN kode_icd10 i ON i.kode = d.kode_icd10
    WHERE k.tanggal BETWEEN $1 AND $2 AND pm.terkunci
    GROUP BY d.kode_icd10, i.deskripsi
    ORDER BY jumlah DESC, d.kode_icd10 LIMIT 10;

\echo Q-C62-04
PREPARE q_c62_04 AS
    INSERT INTO laporan (tanggal_mulai, tanggal_akhir, format, ringkasan)
    VALUES ($1, $2, $3, $4) RETURNING id_laporan;

\echo Q-C63-01
PREPARE q_c63_01 AS
    INSERT INTO bundel_fhir (id_kunjungan, isi_bundel, status, alasan_gagal)
    VALUES ($1, $2::jsonb, 'tertunda', NULL) RETURNING id_bundel;

\echo Q-C63-02
PREPARE q_c63_02 AS
    SELECT id_bundel, id_kunjungan, isi_bundel::text, status, alasan_gagal
    FROM bundel_fhir WHERE status IN ('tertunda', 'gagal kirim')
    ORDER BY id_bundel;

\echo Q-C63-03
PREPARE q_c63_03 AS
    UPDATE bundel_fhir SET status = $1, alasan_gagal = $2 WHERE id_bundel = $3;

\echo Q-C64-01
PREPARE q_c64_01 AS
    SELECT id_pencadangan, waktu, status, lokasi_media, ukuran_berkas
    FROM pencadangan ORDER BY waktu DESC LIMIT 1;

\echo Q-C64-02
PREPARE q_c64_02 AS
    INSERT INTO pencadangan (waktu, status, lokasi_media, ukuran_berkas)
    VALUES ($1, $2, $3, $4) RETURNING id_pencadangan;

DEALLOCATE ALL;
\echo 'Seluruh 78 kueri lolos PREPARE.'
