#!/usr/bin/env bash
# DriveFlow Driver - VersionCode & VersionName Bump Script
# Usage:
#   ./scripts/bump_version.sh [patch|minor|major]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
GRADLE_FILE="${ROOT_DIR}/app/build.gradle.kts"

if [ ! -f "${GRADLE_FILE}" ]; then
  echo "Error: ${GRADLE_FILE} not found!" >&2
  exit 1
fi

MODE="${1:-patch}"

# Read current versionCode and versionName
CURRENT_CODE=$(grep -E 'versionCode\s*=\s*[0-9]+' "${GRADLE_FILE}" | tr -dc '0-9')
CURRENT_NAME=$(grep -E 'versionName\s*=\s*"[^"]+"' "${GRADLE_FILE}" | sed -E 's/.*"([^"]+)".*/\1/')

if [ -z "${CURRENT_CODE}" ] || [ -z "${CURRENT_NAME}" ]; then
  echo "Error: Could not parse current version from ${GRADLE_FILE}" >&2
  exit 1
fi

NEW_CODE=$((CURRENT_CODE + 1))

# Parse semver parts
IFS='.' read -r MAJOR MINOR PATCH <<< "${CURRENT_NAME}.0.0"
MAJOR="${MAJOR:-1}"
MINOR="${MINOR:-0}"
PATCH="${PATCH:-0}"

case "${MODE}" in
  major)
    MAJOR=$((MAJOR + 1))
    MINOR=0
    PATCH=0
    ;;
  minor)
    MINOR=$((MINOR + 1))
    PATCH=0
    ;;
  patch|*)
    PATCH=$((PATCH + 1))
    ;;
esac

NEW_NAME="${MAJOR}.${MINOR}.${PATCH}"

echo "Bumping DriveFlow Driver version:"
echo "  versionCode: ${CURRENT_CODE} -> ${NEW_CODE}"
echo "  versionName: ${CURRENT_NAME} -> ${NEW_NAME}"

# Replace in app/build.gradle.kts
sed -i.bak -E "s/(versionCode\s*=\s*)[0-9]+/\1${NEW_CODE}/" "${GRADLE_FILE}"
sed -i.bak -E "s/(versionName\s*=\s*\")[^\"]+(\")/\1${NEW_NAME}\2/" "${GRADLE_FILE}"
rm -f "${GRADLE_FILE}.bak"

echo "Version successfully updated in ${GRADLE_FILE}."
