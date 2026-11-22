#!/usr/bin/env bash
# Build the signed release APK (R8 + resource shrinking) and verify its signature.
# Needs release.* entries in local.properties (see README). The keystore lives outside the repo.
set -euo pipefail
source "$(dirname "$0")/env.sh"
cd "$PROJECT_ROOT"

if ! grep -q '^release.storeFile=' local.properties 2>/dev/null; then
  echo "Missing release signing config in local.properties. Run scripts/make-keystore.sh first." >&2
  exit 1
fi

./gradlew :app:assembleRelease --quiet
APK="app/build/outputs/apk/release/app-release.apk"
apksigner verify --print-certs "$APK" | head -3
mkdir -p dist
VERSION="$(aapt2 dump badging "$APK" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p")"
cp "$APK" "dist/$APP_SLUG-$VERSION.apk"
ls -lh "dist/$APP_SLUG-$VERSION.apk"
