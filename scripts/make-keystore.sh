#!/usr/bin/env bash
# One-time per app: create this app's release keystore in ~/.kompakt and point local.properties at it.
# Keep this keystore safe: every future update of the app (also via InkApps) must be signed with it.
# One keystore per app, so a single app can be handed over without exposing the others.
set -euo pipefail
source "$(dirname "$0")/env.sh"

KEYSTORE="$HOME/.kompakt/$APP_SLUG.jks"
ALIAS="$APP_SLUG"
if [[ -f "$KEYSTORE" ]]; then
  echo "Keystore already exists: $KEYSTORE" >&2
  exit 1
fi

read -r -s -p "Keystore password (min 6 chars): " PASS; echo
mkdir -p "$(dirname "$KEYSTORE")"
chmod 700 "$(dirname "$KEYSTORE")"
keytool -genkeypair -v -keystore "$KEYSTORE" -alias "$ALIAS" -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "$PASS" -keypass "$PASS" -dname "CN=$APP_SLUG"
chmod 600 "$KEYSTORE"

cat >> "$PROJECT_ROOT/local.properties" <<EOF
release.storeFile=$KEYSTORE
release.storePassword=$PASS
release.keyAlias=$ALIAS
release.keyPassword=$PASS
EOF
echo "Keystore created and local.properties updated (local.properties is git-ignored)."
