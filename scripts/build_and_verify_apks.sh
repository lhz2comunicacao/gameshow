#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$PROJECT_ROOT"

echo "[1/4] Limpando builds anteriores"
gradle clean

echo "[2/4] Compilando APKs debug e release"
gradle assembleDebug assembleRelease

echo "[3/4] Listando artefatos gerados"
find app/build/outputs/apk -type f -name "*.apk" -print

echo "[4/4] Hash dos APKs (SHA-256)"
if command -v sha256sum >/dev/null 2>&1; then
  sha256sum app/build/outputs/apk/**/*.apk 2>/dev/null || true
elif command -v shasum >/dev/null 2>&1; then
  shasum -a 256 app/build/outputs/apk/**/*.apk 2>/dev/null || true
else
  echo "sha256sum/shasum não disponível para calcular hash."
fi
