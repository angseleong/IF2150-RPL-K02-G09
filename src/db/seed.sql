-- Data contoh SEHATI untuk pengembangan dan pengujian. Seluruh data FIKTIF.
-- NIK memakai awalan 9999 yang bukan kode wilayah, sehingga tidak mungkin milik orang sungguhan.
-- Tanggal ditulis relatif terhadap CURRENT_DATE saat seed dijalankan, jadi "hari ini" adalah tanggal volume dibuat.
--
-- Kata sandi seluruh akun contoh: sehati123 (hanya untuk pengembangan, disimpan sebagai hash bcrypt $2a$).
--
-- Cara menambah data: tambahkan baris baru di akhir bagian tabel terkait. Relasi dicari lewat kunci alami
-- (NIK pasien, nama poli, nama obat, kode ICD-10), sehingga tidak perlu menghitung ID.
--
-- Tahap pelayanan pada hari ini:
--   menunggu skrining     : Rina, Agus (Poli Umum), Dewi (Poli Gigi), ketiganya di antrean skrining
--   siap diperiksa dokter : Budi, sudah diskrining dan mendapat penanda hipertensi
--   resep di apotek       : Siti, resep "menunggu" di antrean apotek
--   bundel tertunda       : Hendra (kemarin, "tertunda") dan Maya (kemarin, "gagal kirim")

-- ============================================================================
-- pengguna: satu akun untuk tiap peran (Algo-C17-02), ditambah satu akun nonaktif untuk uji UC01
-- ============================================================================

INSERT INTO pengguna (nama_pengguna, kata_sandi_hash, peran, status) VALUES
    ('admin', '$2a$10$Jh2I4iwKOH45/FI6AtBqW.gVjGBUo1v14nMnr6lJq6QLQRArzimrO', 'Petugas Administrasi', 'aktif'),
    ('perawat', '$2a$10$p5gByg8Cn4ZlHzh7GOExM.AGq8vmoyVj/hw.1ItG68Oisb0SkdPBq', 'Perawat', 'aktif'),
    ('dokter', '$2a$10$Q87SmgQkKtP8ClIH1cIh7uPBAekUmdk93oMmXXVE2U8x10.Df5yJi', 'Dokter', 'aktif'),
    ('farmasi', '$2a$10$sU7O42ImP5PF8qgJnJ./x.65Z7yFcxZTwKNW.AdI..bfCY2nFuZZe', 'Petugas Farmasi', 'aktif'),
    ('perawat.nonaktif', '$2a$10$4cQbRwwLgXQwM.jzp./REOheD1ft78RO68GNsRtjftgrlpHBDQ8k2', 'Perawat', 'nonaktif');

-- ============================================================================
-- poli
-- ============================================================================

INSERT INTO poli (nama_poli) VALUES
    ('Poli Umum'),
    ('Poli Gigi'),
    ('Poli KIA'),
    ('Poli Lansia');

-- ============================================================================
-- ambang_risiko: nilai awal sesuai batas bawah kategori klinis (KF07)
-- ============================================================================

INSERT INTO ambang_risiko (jenis_risiko, nilai_batas) VALUES
    ('hipertensi', 140),
    ('obesitas', 30),
    ('indikasi diabetes', 200);

-- ============================================================================
-- kode_icd10
-- ============================================================================

INSERT INTO kode_icd10 (kode, deskripsi, bab) VALUES
    ('A09', 'Diare dan gastroenteritis yang diduga infeksi', 'I Penyakit infeksi dan parasit tertentu'),
    ('E11.9', 'Diabetes melitus tipe 2 tanpa komplikasi', 'IV Penyakit endokrin, nutrisi, dan metabolik'),
    ('E66.9', 'Obesitas, tidak spesifik', 'IV Penyakit endokrin, nutrisi, dan metabolik'),
    ('E78.5', 'Hiperlipidemia, tidak spesifik', 'IV Penyakit endokrin, nutrisi, dan metabolik'),
    ('I10', 'Hipertensi esensial (primer)', 'IX Penyakit sistem sirkulasi'),
    ('J00', 'Nasofaringitis akut (common cold)', 'X Penyakit sistem pernapasan'),
    ('J06.9', 'Infeksi saluran pernapasan atas akut, tidak spesifik', 'X Penyakit sistem pernapasan'),
    ('K02.9', 'Karies gigi, tidak spesifik', 'XI Penyakit sistem pencernaan'),
    ('K29.7', 'Gastritis, tidak spesifik', 'XI Penyakit sistem pencernaan'),
    ('K30', 'Dispepsia', 'XI Penyakit sistem pencernaan'),
    ('L30.9', 'Dermatitis, tidak spesifik', 'XII Penyakit kulit dan jaringan subkutan'),
    ('M54.5', 'Nyeri punggung bawah', 'XIII Penyakit sistem muskuloskeletal dan jaringan ikat'),
    ('R50.9', 'Demam, tidak spesifik', 'XVIII Gejala, tanda, dan temuan klinis abnormal'),
    ('R51', 'Sakit kepala', 'XVIII Gejala, tanda, dan temuan klinis abnormal'),
    ('Z00.0', 'Pemeriksaan medis umum', 'XXI Faktor yang memengaruhi status kesehatan'),
    ('Z34.9', 'Pengawasan kehamilan normal, tidak spesifik', 'XXI Faktor yang memengaruhi status kesehatan');

-- ============================================================================
-- obat, penerimaan_obat, bets_obat
-- Saldo obat.stok dihitung dari jumlah bets di akhir berkas, jadi cukup isi bets.
-- Glibenklamid di bawah ambang, Amlodipin punya bets yang kedaluwarsa < 90 hari, Asam Mefenamat kosong (KF17).
-- ============================================================================

INSERT INTO obat (nama_obat, sediaan, ambang_minimum) VALUES
    ('Parasetamol 500 mg', 'tablet', 100),
    ('Amoksisilin 500 mg', 'kapsul', 100),
    ('Amlodipin 5 mg', 'tablet', 60),
    ('Metformin 500 mg', 'tablet', 100),
    ('Glibenklamid 5 mg', 'tablet', 50),
    ('Kaptopril 25 mg', 'tablet', 50),
    ('Klorfeniramin Maleat 4 mg', 'tablet', 50),
    ('Antasida DOEN', 'tablet kunyah', 50),
    ('Oralit 200 ml', 'sachet', 30),
    ('Ibuprofen 400 mg', 'tablet', 50),
    ('Fero Sulfat 300 mg', 'tablet', 60),
    ('Asam Mefenamat 500 mg', 'tablet', 50);

INSERT INTO penerimaan_obat (nomor_faktur, tanggal_terima) VALUES
    ('FKT-CONTOH-0001', CURRENT_DATE - 60),
    ('FKT-CONTOH-0002', CURRENT_DATE - 20);

INSERT INTO bets_obat (id_obat, nomor_bets, jumlah, tanggal_kedaluwarsa, id_penerimaan)
SELECT o.id_obat, v.nomor_bets, v.jumlah, CURRENT_DATE + v.hari_kedaluwarsa, pn.id_penerimaan
FROM (VALUES
    ('Parasetamol 500 mg', 'PCT-A01', 300, 400, 'FKT-CONTOH-0001'),
    ('Amoksisilin 500 mg', 'AMX-A01', 200, 300, 'FKT-CONTOH-0001'),
    ('Amlodipin 5 mg', 'AML-A01', 40, 60, 'FKT-CONTOH-0001'),
    ('Amlodipin 5 mg', 'AML-B01', 150, 500, 'FKT-CONTOH-0002'),
    ('Metformin 500 mg', 'MET-A01', 250, 450, 'FKT-CONTOH-0001'),
    ('Glibenklamid 5 mg', 'GLB-A01', 30, 365, 'FKT-CONTOH-0001'),
    ('Kaptopril 25 mg', 'KAP-A01', 120, 420, 'FKT-CONTOH-0002'),
    ('Klorfeniramin Maleat 4 mg', 'CTM-A01', 200, 540, 'FKT-CONTOH-0001'),
    ('Antasida DOEN', 'ANT-A01', 150, 380, 'FKT-CONTOH-0002'),
    ('Oralit 200 ml', 'ORL-A01', 80, 700, 'FKT-CONTOH-0001'),
    ('Ibuprofen 400 mg', 'IBU-A01', 100, 330, 'FKT-CONTOH-0002'),
    ('Fero Sulfat 300 mg', 'FER-A01', 180, 600, 'FKT-CONTOH-0002')
) AS v (nama_obat, nomor_bets, jumlah, hari_kedaluwarsa, nomor_faktur)
JOIN obat o ON o.nama_obat = v.nama_obat
JOIN penerimaan_obat pn ON pn.nomor_faktur = v.nomor_faktur;

-- ============================================================================
-- pasien dan rekam_medis
-- ============================================================================

INSERT INTO pasien (nik, nomor_rekam_medis, nama, tanggal_lahir, jenis_kelamin, alamat, nomor_telepon) VALUES
    ('9999010101680001', 'RM-000001', 'Budi Santoso', '1968-03-12', 'Laki-laki', 'Jl. Melati No. 1', '080000000001'),
    ('9999010101750002', 'RM-000002', 'Siti Aminah', '1975-07-21', 'Perempuan', 'Jl. Mawar No. 2', '080000000002'),
    ('9999010101950003', 'RM-000003', 'Rina Kartika', '1995-11-02', 'Perempuan', 'Jl. Anggrek No. 3', '080000000003'),
    ('9999010101880004', 'RM-000004', 'Agus Pratama', '1988-01-30', 'Laki-laki', 'Jl. Kenanga No. 4', NULL),
    ('9999010101010005', 'RM-000005', 'Dewi Lestari', '2001-05-17', 'Perempuan', 'Jl. Dahlia No. 5', '080000000005'),
    ('9999010101800006', 'RM-000006', 'Hendra Wijaya', '1980-09-09', 'Laki-laki', 'Jl. Kamboja No. 6', '080000000006'),
    ('9999010101930007', 'RM-000007', 'Maya Sari', '1993-12-25', 'Perempuan', 'Jl. Tulip No. 7', '080000000007'),
    ('9999010101100008', 'RM-000008', 'Yusuf Hidayat', '2010-04-08', 'Laki-laki', 'Jl. Teratai No. 8', NULL);

-- Satu rekam medis untuk setiap pasien (Q-C57-06).
INSERT INTO rekam_medis (id_pasien)
SELECT id_pasien FROM pasien WHERE NOT EXISTS (
    SELECT 1 FROM rekam_medis rm WHERE rm.id_pasien = pasien.id_pasien
);

-- ============================================================================
-- kunjungan: hari = selisih hari dari CURRENT_DATE
-- ============================================================================

INSERT INTO kunjungan (id_pasien, id_poli, tanggal, nomor_antrean, status)
SELECT p.id_pasien, po.id_poli, CURRENT_DATE + v.hari, v.nomor_antrean, v.status
FROM (VALUES
    ('9999010101680001', 'Poli Umum', -90, 3, 'selesai'),
    ('9999010101680001', 'Poli Umum', -60, 2, 'selesai'),
    ('9999010101750002', 'Poli Umum', -45, 4, 'selesai'),
    ('9999010101680001', 'Poli Umum', -30, 5, 'selesai'),
    ('9999010101930007', 'Poli KIA', -20, 2, 'selesai'),
    ('9999010101100008', 'Poli Gigi', -10, 1, 'selesai'),
    ('9999010101800006', 'Poli Umum', -1, 1, 'selesai'),
    ('9999010101930007', 'Poli KIA', -1, 1, 'selesai'),
    ('9999010101680001', 'Poli Umum', 0, 1, 'siap diperiksa dokter'),
    ('9999010101750002', 'Poli Umum', 0, 2, 'selesai diperiksa'),
    ('9999010101950003', 'Poli Umum', 0, 3, 'menunggu skrining'),
    ('9999010101880004', 'Poli Umum', 0, 4, 'menunggu skrining'),
    ('9999010101010005', 'Poli Gigi', 0, 1, 'menunggu skrining')
) AS v (nik, nama_poli, hari, nomor_antrean, status)
JOIN pasien p ON p.nik = v.nik
JOIN poli po ON po.nama_poli = v.nama_poli;

-- ============================================================================
-- skrining dan tanda_vital
-- Budi memiliki tiga kunjungan sebelumnya dengan sistolik >= 140 untuk uji evaluasi risiko (Algo-C21-01).
-- ============================================================================

INSERT INTO skrining (id_kunjungan, keluhan_awal, waktu)
SELECT k.id_kunjungan, v.keluhan_awal, (CURRENT_DATE + v.hari) + v.jam
FROM (VALUES
    ('9999010101680001', -90, 'Kontrol tekanan darah', TIME '08:10'),
    ('9999010101680001', -60, 'Kontrol tekanan darah, kadang pusing', TIME '08:05'),
    ('9999010101750002', -45, 'Sering haus dan buang air kecil', TIME '08:30'),
    ('9999010101680001', -30, 'Kontrol tekanan darah', TIME '08:20'),
    ('9999010101930007', -20, 'Pemeriksaan kehamilan rutin', TIME '08:40'),
    ('9999010101100008', -10, 'Gigi geraham kanan bawah nyeri', TIME '09:00'),
    ('9999010101800006', -1, 'Demam dan batuk pilek sejak tiga hari', TIME '07:50'),
    ('9999010101930007', -1, 'Pemeriksaan kehamilan rutin', TIME '08:15'),
    ('9999010101680001', 0, 'Pusing dan tengkuk terasa berat', TIME '07:45'),
    ('9999010101750002', 0, 'Kontrol gula darah, badan lemas', TIME '07:55')
) AS v (nik, hari, keluhan_awal, jam)
JOIN pasien p ON p.nik = v.nik
JOIN kunjungan k ON k.id_pasien = p.id_pasien AND k.tanggal = CURRENT_DATE + v.hari;

INSERT INTO tanda_vital (id_skrining, sistolik, diastolik, berat_badan, tinggi_badan, suhu, nadi, gula_darah)
SELECT s.id_skrining, v.sistolik, v.diastolik, v.berat_badan, v.tinggi_badan, v.suhu, v.nadi, v.gula_darah
FROM (VALUES
    ('9999010101680001', -90, 148, 94, 82.0, 168.0, 36.6, 84, NULL::INTEGER),
    ('9999010101680001', -60, 150, 95, 83.0, 168.0, 36.7, 86, NULL),
    ('9999010101750002', -45, 128, 82, 67.0, 155.0, 36.6, 80, 215),
    ('9999010101680001', -30, 146, 92, 83.5, 168.0, 36.5, 82, 132),
    ('9999010101930007', -20, 110, 70, 58.0, 158.0, 36.6, 80, NULL),
    ('9999010101100008', -10, 108, 68, 45.0, 160.0, 36.6, 78, NULL),
    ('9999010101800006', -1, 118, 76, 65.0, 170.0, 38.2, 96, NULL),
    ('9999010101930007', -1, 112, 72, 59.0, 158.0, 36.5, 82, NULL),
    ('9999010101680001', 0, 152, 96, 84.0, 168.0, 36.8, 88, NULL),
    ('9999010101750002', 0, 130, 84, 68.0, 155.0, 36.7, 82, 238)
) AS v (nik, hari, sistolik, diastolik, berat_badan, tinggi_badan, suhu, nadi, gula_darah)
JOIN pasien p ON p.nik = v.nik
JOIN kunjungan k ON k.id_pasien = p.id_pasien AND k.tanggal = CURRENT_DATE + v.hari
JOIN skrining s ON s.id_kunjungan = k.id_kunjungan;

-- ============================================================================
-- penanda_risiko hasil skrining hari ini
-- ============================================================================

INSERT INTO penanda_risiko (id_kunjungan, jenis_risiko, tanggal, status)
SELECT k.id_kunjungan, v.jenis_risiko, k.tanggal, 'aktif'
FROM (VALUES
    ('9999010101680001', 0, 'hipertensi'),
    ('9999010101750002', 0, 'indikasi diabetes')
) AS v (nik, hari, jenis_risiko)
JOIN pasien p ON p.nik = v.nik
JOIN kunjungan k ON k.id_pasien = p.id_pasien AND k.tanggal = CURRENT_DATE + v.hari;

-- ============================================================================
-- pemeriksaan dan diagnosis
-- ============================================================================

INSERT INTO pemeriksaan (id_kunjungan, anamnesis, tindakan, terkunci)
SELECT k.id_kunjungan, v.anamnesis, v.tindakan, v.terkunci
FROM (VALUES
    ('9999010101680001', -90, 'Hipertensi lama, jarang kontrol', 'Edukasi diet rendah garam, terapi amlodipin', TRUE),
    ('9999010101680001', -60, 'Pusing hilang timbul, obat diminum teratur', 'Lanjutkan terapi amlodipin', TRUE),
    ('9999010101750002', -45, 'Poliuria dan polidipsia sejak satu bulan', 'Edukasi diet, terapi metformin', TRUE),
    ('9999010101680001', -30, 'Tidak ada keluhan baru', 'Lanjutkan terapi amlodipin', TRUE),
    ('9999010101930007', -20, 'Hamil 20 minggu, tidak ada keluhan', 'Pemeriksaan antenatal, suplemen zat besi', TRUE),
    ('9999010101100008', -10, 'Nyeri gigi 46 saat makan', 'Tumpatan sementara, analgesik', TRUE),
    ('9999010101800006', -1, 'Demam, batuk, pilek tiga hari', 'Terapi simptomatik dan antibiotik', TRUE),
    ('9999010101930007', -1, 'Hamil 24 minggu, tidak ada keluhan', 'Pemeriksaan antenatal, suplemen zat besi', TRUE),
    ('9999010101750002', 0, 'Gula darah sewaktu masih tinggi, lemas', 'Tambah glibenklamid, edukasi diet', TRUE)
) AS v (nik, hari, anamnesis, tindakan, terkunci)
JOIN pasien p ON p.nik = v.nik
JOIN kunjungan k ON k.id_pasien = p.id_pasien AND k.tanggal = CURRENT_DATE + v.hari;

INSERT INTO diagnosis (id_pemeriksaan, kode_icd10, deskripsi)
SELECT pm.id_pemeriksaan, i.kode, i.deskripsi
FROM (VALUES
    ('9999010101680001', -90, 'I10'),
    ('9999010101680001', -60, 'I10'),
    ('9999010101750002', -45, 'E11.9'),
    ('9999010101680001', -30, 'I10'),
    ('9999010101930007', -20, 'Z34.9'),
    ('9999010101100008', -10, 'K02.9'),
    ('9999010101800006', -1, 'J06.9'),
    ('9999010101930007', -1, 'Z34.9'),
    ('9999010101750002', 0, 'E11.9')
) AS v (nik, hari, kode)
JOIN pasien p ON p.nik = v.nik
JOIN kunjungan k ON k.id_pasien = p.id_pasien AND k.tanggal = CURRENT_DATE + v.hari
JOIN pemeriksaan pm ON pm.id_kunjungan = k.id_kunjungan
JOIN kode_icd10 i ON i.kode = v.kode;

-- ============================================================================
-- jadwal_kontrol
-- Budi belum punya entri daftar pantau (uji Algo-C21-02), Hendra sudah punya.
-- ============================================================================

INSERT INTO jadwal_kontrol (id_pasien, tanggal_rencana, status)
SELECT p.id_pasien, CURRENT_DATE + v.hari, v.status
FROM (VALUES
    ('9999010101680001', 14, 'terjadwal'),
    ('9999010101800006', 7, 'terjadwal')
) AS v (nik, hari, status)
JOIN pasien p ON p.nik = v.nik;

-- ============================================================================
-- resep dan item_resep: satu resep per kunjungan pada seed ini
-- ============================================================================

INSERT INTO resep (id_kunjungan, waktu_masuk, status, catatan)
SELECT k.id_kunjungan, (CURRENT_DATE + v.hari) + v.jam, v.status, NULL
FROM (VALUES
    ('9999010101680001', -90, TIME '09:00', 'selesai dilayani'),
    ('9999010101680001', -60, TIME '08:55', 'selesai dilayani'),
    ('9999010101750002', -45, TIME '09:20', 'selesai dilayani'),
    ('9999010101680001', -30, TIME '09:05', 'selesai dilayani'),
    ('9999010101930007', -20, TIME '09:30', 'selesai dilayani'),
    ('9999010101100008', -10, TIME '09:45', 'selesai dilayani'),
    ('9999010101800006', -1, TIME '08:40', 'selesai dilayani'),
    ('9999010101930007', -1, TIME '09:10', 'selesai dilayani'),
    ('9999010101750002', 0, TIME '08:45', 'menunggu')
) AS v (nik, hari, jam, status)
JOIN pasien p ON p.nik = v.nik
JOIN kunjungan k ON k.id_pasien = p.id_pasien AND k.tanggal = CURRENT_DATE + v.hari;

INSERT INTO item_resep (id_resep, id_obat, dosis, jumlah, aturan_pakai)
SELECT r.id_resep, o.id_obat, v.dosis, v.jumlah, v.aturan_pakai
FROM (VALUES
    ('9999010101680001', -90, 'Amlodipin 5 mg', '1x1', 30, 'sesudah makan pagi'),
    ('9999010101680001', -60, 'Amlodipin 5 mg', '1x1', 30, 'sesudah makan pagi'),
    ('9999010101750002', -45, 'Metformin 500 mg', '2x1', 60, 'sesudah makan'),
    ('9999010101680001', -30, 'Amlodipin 5 mg', '1x1', 30, 'sesudah makan pagi'),
    ('9999010101930007', -20, 'Fero Sulfat 300 mg', '1x1', 30, 'malam sebelum tidur'),
    ('9999010101100008', -10, 'Parasetamol 500 mg', '3x1', 10, 'bila nyeri'),
    ('9999010101800006', -1, 'Parasetamol 500 mg', '3x1', 10, 'sesudah makan, bila demam'),
    ('9999010101800006', -1, 'Amoksisilin 500 mg', '3x1', 15, 'sesudah makan, dihabiskan'),
    ('9999010101930007', -1, 'Fero Sulfat 300 mg', '1x1', 30, 'malam sebelum tidur'),
    ('9999010101750002', 0, 'Metformin 500 mg', '2x1', 60, 'sesudah makan'),
    ('9999010101750002', 0, 'Glibenklamid 5 mg', '1x1', 30, 'sebelum makan pagi')
) AS v (nik, hari, nama_obat, dosis, jumlah, aturan_pakai)
JOIN pasien p ON p.nik = v.nik
JOIN kunjungan k ON k.id_pasien = p.id_pasien AND k.tanggal = CURRENT_DATE + v.hari
JOIN resep r ON r.id_kunjungan = k.id_kunjungan
JOIN obat o ON o.nama_obat = v.nama_obat;

-- ============================================================================
-- antrean_entri: skrining berisi id_kunjungan, apotek berisi id_resep (Algo-C58-01)
-- ============================================================================

INSERT INTO antrean_entri (id_antrean, urutan, id_entri)
SELECT a.id_antrean, v.urutan, k.id_kunjungan
FROM (VALUES
    (1, '9999010101950003'),
    (2, '9999010101880004'),
    (3, '9999010101010005')
) AS v (urutan, nik)
JOIN pasien p ON p.nik = v.nik
JOIN kunjungan k ON k.id_pasien = p.id_pasien AND k.tanggal = CURRENT_DATE
JOIN antrean a ON a.jenis = 'skrining';

INSERT INTO antrean_entri (id_antrean, urutan, id_entri)
SELECT a.id_antrean, v.urutan, r.id_resep
FROM (VALUES
    (1, '9999010101750002')
) AS v (urutan, nik)
JOIN pasien p ON p.nik = v.nik
JOIN kunjungan k ON k.id_pasien = p.id_pasien AND k.tanggal = CURRENT_DATE
JOIN resep r ON r.id_kunjungan = k.id_kunjungan AND r.status = 'menunggu'
JOIN antrean a ON a.jenis = 'apotek';

-- ============================================================================
-- daftar_pantau dan tindak_lanjut
-- ============================================================================

INSERT INTO daftar_pantau (id_pasien, jenis_risiko, status_tindak_lanjut, id_penanda, id_jadwal)
SELECT k.id_pasien, pr.jenis_risiko, 'belum ditindaklanjuti', pr.id_penanda, NULL
FROM penanda_risiko pr
JOIN kunjungan k ON k.id_kunjungan = pr.id_kunjungan
WHERE pr.status = 'aktif';

INSERT INTO daftar_pantau (id_pasien, jenis_risiko, status_tindak_lanjut, id_penanda, id_jadwal)
SELECT jk.id_pasien, 'kunjungan ulang', 'perlu dihubungi ulang', NULL, jk.id_jadwal
FROM jadwal_kontrol jk
JOIN pasien p ON p.id_pasien = jk.id_pasien
WHERE p.nik = '9999010101800006';

INSERT INTO tindak_lanjut (id_entri, jenis_kontak, hasil_upaya, status_kedatangan, waktu)
SELECT dp.id_entri, 'telepon', 'tidak terhubung', 'belum diketahui', CURRENT_DATE + TIME '10:30'
FROM daftar_pantau dp
JOIN pasien p ON p.id_pasien = dp.id_pasien
WHERE p.nik = '9999010101800006' AND dp.jenis_risiko = 'kunjungan ulang';

-- ============================================================================
-- bundel_fhir: satu bundel untuk setiap kunjungan "selesai" (Algo-C24-01)
-- Isi bundel hanya kerangka minimal. Bundel lebih dari sehari lalu dianggap sudah terkirim.
-- ============================================================================

INSERT INTO bundel_fhir (id_kunjungan, isi_bundel, status, alasan_gagal)
SELECT
    k.id_kunjungan,
    jsonb_build_object(
        'resourceType', 'Bundle',
        'type', 'transaction',
        'entry', jsonb_build_array(jsonb_build_object(
            'resource', jsonb_build_object(
                'resourceType', 'Encounter',
                'status', 'finished',
                'identifier', jsonb_build_array(jsonb_build_object('value', k.id_kunjungan::TEXT)),
                'period', jsonb_build_object('start', k.tanggal::TEXT)
            )
        ))
    ),
    CASE
        WHEN k.tanggal < CURRENT_DATE - 1 THEN 'terkirim'
        WHEN p.nik = '9999010101930007' THEN 'gagal kirim'
        ELSE 'tertunda'
    END,
    CASE
        WHEN k.tanggal >= CURRENT_DATE - 1 AND p.nik = '9999010101930007'
            THEN 'Koneksi ke SATUSEHAT terputus (contoh)'
    END
FROM kunjungan k
JOIN pasien p ON p.id_pasien = k.id_pasien
WHERE k.status = 'selesai';

-- ============================================================================
-- pencadangan: pencadangan terakhir berhasil, sehingga dasbor tidak menampilkan peringatan (KF21)
-- ============================================================================

INSERT INTO pencadangan (waktu, status, lokasi_media, ukuran_berkas) VALUES
    ((CURRENT_DATE - 1) + TIME '21:00',
     'berhasil',
     '/media/cadangan/sehati_' || to_char(CURRENT_DATE - 1, 'YYYYMMDD') || '-2100.dump',
     1048576);

-- ============================================================================
-- Penyelarasan nilai turunan. Biarkan di akhir berkas.
-- ============================================================================

-- Saldo stok sama dengan jumlah seluruh bets.
UPDATE obat o
SET stok = COALESCE((SELECT SUM(b.jumlah) FROM bets_obat b WHERE b.id_obat = o.id_obat), 0);

-- Nomor antrean berikutnya melanjutkan nomor terakhir hari ini (Algo-C19-02).
UPDATE poli po
SET urutan_terakhir = COALESCE((
        SELECT MAX(k.nomor_antrean) FROM kunjungan k
        WHERE k.id_poli = po.id_poli AND k.tanggal = CURRENT_DATE
    ), 0),
    tanggal_urutan = CURRENT_DATE;

-- Nomor rekam medis baru melanjutkan nomor terbesar pada seed (Q-C57-04).
SELECT setval('seq_nomor_rm', (SELECT MAX(substring(nomor_rekam_medis FROM 4)::INTEGER) FROM pasien));
