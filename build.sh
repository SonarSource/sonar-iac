#!/bin/bash
set -euo pipefail

: "${SONAR_HOST_URL?}" "${SONAR_TOKEN?}"
: "${CI_BUILD_NUMBER?}" "${CIRRUS_BUILD_ID?}" "${CIRRUS_REPO_FULL_NAME?}" "${CIRRUS_CHANGE_IN_REPO?}"

INITIAL_VERSION=$(grep version gradle.properties | awk -F= '{print $2}')

./gradlew --no-daemon --console plain \
  -DbuildNumber="$CI_BUILD_NUMBER" \
  build sonar \
  -Dsonar.host.url="$SONAR_HOST_URL" \
  -Dsonar.token="$SONAR_TOKEN" \
  -Dsonar.projectVersion="$INITIAL_VERSION" \
  -Dsonar.analysis.buildNumber="$CI_BUILD_NUMBER" \
  -Dsonar.analysis.pipeline="$CIRRUS_BUILD_ID" \
  -Dsonar.analysis.sha1="$CIRRUS_CHANGE_IN_REPO" \
  -Dsonar.analysis.repository="$CIRRUS_REPO_FULL_NAME" \
  -Dsonar.organization=sonarsource
