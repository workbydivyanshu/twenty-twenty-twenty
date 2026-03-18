#!/bin/bash
# Build script for Twenty Android APK
# Requires JDK 17 or 21 (JDK 25+ is NOT yet supported by Gradle ASM)

set -e

REQUIRED_JAVA="17"
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)

if [ "$JAVA_VERSION" != "$REQUIRED_JAVA" ] && [ "$JAVA_VERSION" -lt 17 ] 2>/dev/null; then
  echo "Error: JDK $REQUIRED_JAVA or 21 is required. Found JDK $JAVA_VERSION."
  echo "Please install JDK 17 or 21 and set JAVA_HOME accordingly."
  echo ""
  echo "Example:"
  echo "  export JAVA_HOME=/path/to/jdk-17"
  echo "  ./scripts/build-android.sh"
  exit 1
fi

echo "Building web app..."
npm run build

echo "Syncing Capacitor..."
npx cap sync android

echo "Building Android APK..."
cd android
./gradlew assembleDebug
cd ..

echo ""
echo "APK located at: android/app/build/outputs/apk/debug/app-debug.apk"
