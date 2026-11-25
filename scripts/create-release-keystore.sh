#!/usr/bin/env bash
set -euo pipefail

KEYSTORE_FILE="${1:-my-upload-key.jks}"
KEY_ALIAS="upload"

echo "Membuat keystore rilis Android: $KEYSTORE_FILE (alias: $KEY_ALIAS)"
keytool -genkeypair -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

echo ""
echo "Selesai! Salin teks Base64 di bawah ini ke GitHub Secret ANDROID_KEYSTORE_BASE64:"
echo "--------------------------------------------------------------------------------"
base64 -w 0 "$KEYSTORE_FILE" 2>/dev/null || base64 "$KEYSTORE_FILE"
echo ""
echo "--------------------------------------------------------------------------------"
echo "Tambahkan juga GitHub Secrets: STORE_PASSWORD dan KEY_PASSWORD."
