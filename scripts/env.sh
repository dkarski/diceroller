#!/usr/bin/env bash
# Shared environment for project scripts. Sourced, not executed.
if [[ -z "${ANDROID_HOME:-}" && "$(uname -s)" == Darwin ]]; then
  export ANDROID_HOME="$HOME/Library/Android/sdk"
fi
if [[ -n "${KOMPAKT_JAVA_HOME:-}" ]]; then
  export JAVA_HOME="$KOMPAKT_JAVA_HOME"
elif [[ -z "${JAVA_HOME:-}" && -d "/Applications/Android Studio.app/Contents/jbr/Contents/Home" ]]; then
  export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
fi
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
if [[ -n "${ANDROID_HOME:-}" ]]; then
  BUILD_TOOLS_DIR="$(find "$ANDROID_HOME/build-tools" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | sort -V | tail -1)"
  export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$BUILD_TOOLS_DIR:$PATH"
fi

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export PROJECT_ROOT

# Application identity.
APP_ID="dev.dkarski.diceroller"
export APP_SLUG="diceroller"
export DEBUG_APP_ID="$APP_ID.debug"
export RELEASE_APP_ID="$APP_ID"
export MAIN_ACTIVITY="$APP_ID.MainActivity"

# Fails with a clear message when no device/emulator is connected.
require_device() {
  if ! adb get-state >/dev/null 2>&1; then
    echo "No device found. Connect the Kompakt over USB with USB debugging enabled (check: adb devices)." >&2
    exit 1
  fi
}
