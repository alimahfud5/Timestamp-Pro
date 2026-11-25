# TIMESTAMP PHOTO PRO

Aplikasi Android native berbasis Kotlin & Jetpack Compose untuk menambahkan stempel waktu, alamat GPS, logo kustom, crop rasio, dan watermark dokumentasi lapangan beresolusi penuh.

## Fitur Utama

- **Editor Stempel Real-Time**: Waktu (Sekarang, Asli EXIF, Manual), teks kegiatan/petugas/catatan, posisi & perataan, ukuran font (`Tiny`–`Extra Large`), gaya font (`Digital`, `Sans`, `Serif`, `Tebal`, `Condensed`, `Timemark`).
- **Template Siap Pakai**: Termasuk **Timemark Verified (100% Akurat)**, **Patroli Polsek Semarang Selatan**, **Proyek & Konstruksi**, **Dokumentasi Resmi**, dan **Minimalis**.
- **Logo Kustom**: Tambah gambar/logo PNG transparan dengan pengaturan posisi (default kanan atas) dan ukuran fleksibel.
- **Edit & Crop Gambar**: Dukungan 11 rasio (`Original`, `Free Crop`, `1:1`, `4:3`, `3:4`, `16:9`, `9:16`, `4:5`, `5:4`, `3:2`, `2:3`).
- **Kamera & GPS Otomatis**: Integrasi langsung dengan kamera HP (`Masuk Camera`) serta deteksi GPS otomatis + isi cepat alamat.
- **Ekspor Resolusi Penuh & Riwayat**: Simpan ke `Pictures/TimestampPhotoPro` dengan metadata EXIF dan kelola riwayat melalui ikon Riwayat / Pengaturan.

## Cara Build Lokal

Persyaratan: **JDK 21**, **Android SDK (API 36)**, dan **Gradle 9.3.1**.

```bash
# Jalankan unit test
gradle :app:testDebugUnitTest

# Bangun Debug APK
gradle :app:assembleDebug
```

## Deploy & Rilis Otomatis di GitHub

Repositori ini sudah dilengkapi workflow GitHub Actions di `.github/workflows/android-ci-release.yml`.

### 1. Push Proyek ke GitHub

```bash
git init
git add .
git commit -m "Initial commit: TIMESTAMP PHOTO PRO"
git branch -M main
git remote add origin https://github.com/<username>/<nama-repo>.git
git push -u origin main
```

Setiap push ke branch `main` atau `master` akan otomatis menjalankan unit test dan menghasilkan artifact `TIMESTAMP_PHOTO_PRO-debug-apk` di tab **Actions**.

### 2. Rilis APK Otomatis via Tag (`GitHub Releases`)

```bash
git tag v1.0.0
git push origin v1.0.0
```

Push tag berawalan `v*` akan otomatis membuat halaman **GitHub Release** dan melampirkan file APK (dan AAB jika signing release dikonfigurasi).

### 3. Opsional: Signed Release APK & AAB

Untuk menghasilkan APK/AAB bertanda tangan produksi (`release`) di GitHub Actions, tambahkan **Repository Secrets** berikut pada menu **Settings → Secrets and variables → Actions**:

| Secret Name | Keterangan |
| :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` | Isi file keystore `.jks` (`keyAlias`: `upload`) dalam format Base64 (`base64 -w 0 my-upload-key.jks`) |
| `STORE_PASSWORD` | Password keystore |
| `KEY_PASSWORD` | Password key alias `upload` |
