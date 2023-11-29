#! /usr/bin/env bash
set -euox pipefail

readonly GO_VERSION="${GO_VERSION:-1.21.1}"
readonly DEFAULT_GO_BINARY_DIRECTORY="${GOPATH:=${HOME}/go}/bin"
readonly DEFAULT_GO_BINARY="${DEFAULT_GO_BINARY_DIRECTORY}/go"

is_go_binary_the_expected_version() {
  if [[ "${#}" -ne 2 ]]; then
    echo "Usage: is_go_binary_the_expected_version <path/to/binary> <expected version>"
    exit 1
  fi
  local go_binary="${1}"
  local expected_version="${2}"
  bash -c "${go_binary} version" | grep --quiet "${expected_version}"
}

go_download_go() {
  if [[ "${#}" -ne 2 ]]; then
    echo "Usage: go_install_go <path/to/binary> <expected version>"
    exit 1
  fi
  local go_binary="${1}"
  local expected_version="${2}"
  bash -c "${go_binary} install golang.org/dl/go${go_version}@latest"
  go_binary="${DEFAULT_GO_BINARY_DIRECTORY}/go${go_version}"
  if [[ ! -f "${go_binary}" ]]; then
    if [[ -f "${DEFAULT_GO_BINARY}" ]] && is_go_binary_the_expected_version "${DEFAULT_GO_BINARY}" "${go_version}"; then
      go_binary="${DEFAULT_GO_BINARY}"
    else
      echo "Could not find designated go binary after download" >&2
      exit 1
    fi
  fi
  bash -c "${go_binary} download"
  echo "${go_binary}"
}

install_go() {
  if [[ "${#}" -ne 1 ]]; then
    echo "Usage: install_go <go version>" >&2
    exit 1
  fi

  local go_version="${1}"
  local go_binary
  local go_in_path

  go_in_path=$(command -v go)
  if [[ -n "${go_in_path}" ]]; then
    if is_go_binary_the_expected_version "${go_in_path}" "${go_version}"; then
      go_binary="${go_in_path}"
    else
      go_binary=$(go_download_go "${go_in_path}" "${go_version}")
    fi
  elif [[ -f "${DEFAULT_GO_BINARY}" ]]; then
    if is_go_binary_the_expected_version "${DEFAULT_GO_BINARY}" "${go_version}"; then
      go_binary="${DEFAULT_GO_BINARY}"
    else
      go_binary=$(go_download_go "${DEFAULT_GO_BINARY}" "${go_version}")
    fi
  else
    # Download go
    pushd "${HOME}" >&2
    local url="https://dl.google.com/go/go${go_version}.linux-amd64.tar.gz"
    curl --request GET "${url}" --output go.linux-amd64.tar.gz --silent
    tar xvf go.linux-amd64.tar.gz >/dev/null 2>&1
    if [[ ! -f "${DEFAULT_GO_BINARY}" ]]; then
      echo "Could not extract go from archive" >&2
      popd >&2
      exit 2
    fi
    popd >&2
    # Set up env variables for go
    export PATH="${PATH}:${DEFAULT_GO_BINARY_DIRECTORY}"
    go_binary="${DEFAULT_GO_BINARY}"
  fi
  echo "${go_binary}"
}

verifyLicenseHeader() {
  echo "Verify License Header"
  hasAllLicenseHeader=0
  for file in *.go; do
    [ -f "$file" ] || continue
    IFS= read -r line < "$file" || [ -n "$line" ] || continue
    case $line in
      ("// SonarQube IaC Plugin"*)
        ;;
      *)
        printf 'No license header: %s\n' "$file"
        hasAllLicenseHeader=1
        ;;
    esac
  done
  if [[ hasAllLicenseHeader -eq 1 ]]; then
    printf 'Please fix license headers'
    exit 1;
  fi
}

compile_binaries() {
  # Install the proper go version
  local path_to_binary
  path_to_binary=$(install_go "${GO_VERSION}")

  # Note: CGO_ENABLED is required to build with CGO, which is activated by `import "C"` in Go sources.
  # Note: Saving files in target/classes include files in JAR out of the box.
  if [ -n "${GOOS:-}" ]; then
    # GOOS is already set, so we perform build for it
    echo "Building for target OS: ${GOOS}"
    case "${GOOS}" in
      "darwin")
        for GOARCH in amd64 arm64; do
          CGO_ENABLED=1 go build -buildmode=c-shared -o target/classes/sonar-helm-for-iac-"${GOOS}"-"${GOARCH}"
        done
        ;;
      "linux"|"windows")
        GOARCH="amd64"
        CGO_ENABLED=1 go build -buildmode=c-shared -o target/classes/sonar-helm-for-iac-"$GOOS"-"$GOARCH"
        ;;
      *)
        echo "Unsupported GOOS: ${GOOS}"
        exit 1
        ;;
    esac
  else
    # GOOS and GOARCH are not set, so we build for the host system.
    GOOS=$(${path_to_binary} env GOOS)
    GOARCH=$(${path_to_binary} env GOARCH)
    echo "Building only for host architecture: ${GOOS}/${GOARCH}"
    # Note: CGO_ENABLED will be set to 1 automatically if GOOS/GOARCH match the current system, but we set it explicitly for consistency.
    CGO_ENABLED=1 ${path_to_binary} build -buildmode=c-shared -o target/classes/sonar-helm-for-iac-"$GOOS"-"$GOARCH"
  fi

  verifyLicenseHeader
}

generate_test_report() {
  # Install the proper go version
  local path_to_binary
  path_to_binary=$(install_go "${GO_VERSION}")
  # Test
  bash -c "${path_to_binary} test -coverprofile=target/test-coverage.out -json > target/test-report.out"
}


main() {
  if [[ "${#}" -ne 1 ]]; then
    echo "Usage: ${0} build | clean | test"
    exit 0
  fi
  local command="${1}"
  case "${command}" in
    build)
      compile_binaries
      ;;
    test)
      generate_test_report
      ;;
    clean)
      rm -f target/classes/sonar-helm-for-iac-*
      rm -f test-report.out
      ;;
    *)
      echo "Unrecognized command ${command}" >&2
      exit 1
      ;;
  esac
  exit 0
}

main "${@}"
