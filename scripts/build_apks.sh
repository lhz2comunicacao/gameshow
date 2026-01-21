#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cd "$PROJECT_ROOT"

gradle assembleDebug assembleRelease

echo "\nArtifacts (debug):"
ls -1 app/build/outputs/apk/debug || true

echo "\nArtifacts (release):"
ls -1 app/build/outputs/apk/release || true
