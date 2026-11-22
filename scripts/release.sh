#!/usr/bin/env bash
# Optional local production signing. The normal publication path is GitHub Actions.
set -euo pipefail
source "$(dirname "$0")/env.sh"
cd "$PROJECT_ROOT"
if ! grep -q '^release.storeFile=' local.properties 2>/dev/null; then
  echo "Missing signing config in local.properties. See CONTRIBUTING.md." >&2
  exit 1
fi
./gradlew :app:assembleRelease --quiet
VERSION="$(cat version.txt)"
APK="app/build/outputs/apk/release/diceroller-$VERSION-release.apk"
apksigner verify --print-certs "$APK"
zipalign -c -p 4 "$APK"
IFS=. read -r MA MI PA <<< "$VERSION"
CODE=$((MA * 1000000 + MI * 1000 + PA))
BADGE="$(aapt2 dump badging "$APK" | sed -n 1p)"
grep -q "name='$RELEASE_APP_ID' versionCode='$CODE' versionName='$VERSION'" <<< "$BADGE"
mkdir -p dist
cp "$APK" "dist/$APP_SLUG-$VERSION.apk"
(cd dist && shasum -a 256 "$APP_SLUG-$VERSION.apk" > "$APP_SLUG-$VERSION.apk.sha256")
echo "Verified dist/$APP_SLUG-$VERSION.apk. Nothing was published."
