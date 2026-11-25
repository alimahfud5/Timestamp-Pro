# TIMESTAMP PHOTO PRO

Aplikasi Android native berbasis Kotlin & Jetpack Compose untuk menambahkan stempel waktu, alamat GPS, logo kustom, crop rasio, dan watermark dokumentasi lapangan beresolusi penuh.

## Fitur Utama

- **Editor Stempel Real-Time**: Waktu (Sekarang, Asli EXIF, Manual), teks kegiatan/petugas/catatan, posisi & perataan, ukuran font (`Tiny`–`Extra Large`), gaya font (`Digital`, `Sans`, `Serif`, `Tebal`, `Condensed`, `Timemark`).
- **Template Siap Pakai**: Termasuk **Timemark Verified (100% Akurat)**, **Patroli Polsek Semarang Selatan**, **Proyek & Konstruksi**, **Dokumentasi Resmi**, dan **Minimalis**.
- **Logo Kustom**: Tambah gambar/logo PNG transparan dengan pengaturan posisi (default kanan atas) dan ukuran fleksibel.
- **Edit & Crop Gambar**: Dukungan 11 rasio (`Original`, `Free Crop`, `1:1`, `4:3`, `3:4`, `16:9`, `9:16`, `4:5`, `5:4`, `3:2`, `2:3`).
- **Kamera & GPS Otomatis**: Integrasi langsung dengan kamera HP (`Masuk Camera`) serta deteksi GPS otomatis + isi cepat alamat.
- **Ekspor Resolusi Penuh & Riwayat**: Simpan ke `Pictures/TimestampPhotoPro` dengan metadata EXIF dan kelola riwayat melalui ikon Riwayat / Pengaturan.

## Cara Build & Deploy APK di GitHub

Repositori ini sudah siap build otomatis di GitHub melalui `.github/workflows/android-ci-release.yml` (JDK 21, Android SDK, Gradle 9.3.1).

### 1. Push Proyek ke GitHub

```bash
git init
git add .
git commit -m "Initial commit: TIMESTAMP PHOTO PRO"
git branch -M main
git remote add origin https://github.com/<username>/<nama-repo>.git
git push -u origin main
```

### 2. Download APK dari GitHub Actions (Tanpa Pengaturan Tambahan)

1. Buka repositori Anda di GitHub lalu pilih tab **Actions**.
2. Setiap *push* ke branch `main`/`master` otomatis menjalankan workflow **Android CI & Build APK**.
3. Anda juga dapat klik **Run workflow** kapan saja dari tab **Actions** (centang *Terbitkan GitHub Release beserta file APK* jika ingin langsung membuat halaman **Releases**).
4. Setelah selesai, unduh file **`TIMESTAMP_PHOTO_PRO-debug-apk`** di bagian **Artifacts** (siap langsung di-install di HP Android).

### 3. Rilis Otomatis via Tag Versi (`GitHub Releases`)

```bash
git tag v1.0.0
git push origin v1.0.0
```

### 4. Opsional: Signed Release APK & Play Store AAB

Untuk menghasilkan `TIMESTAMP_PHOTO_PRO-release.apk` dan `.aab` bertanda tangan produksi:

1. Buat keystore upload (alias `upload`) dengan menjalankan `bash scripts/create-release-keystore.sh`.
2. Masukkan 3 **Repository Secrets** di GitHub (**Settings → Secrets and variables → Actions**):

| Secret Name | Keterangan |
| :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` | Output Base64 dari file `my-upload-key.jks` |
| `STORE_PASSWORD` | Password keystore |
| `KEY_PASSWORD` | Password key alias `upload` |

## Build Lokal

Persyaratan: **JDK 21**, **Android SDK (API 36)**, dan **Gradle 9.3.1**.

```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```
