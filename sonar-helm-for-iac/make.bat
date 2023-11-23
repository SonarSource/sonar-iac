@echo off
setlocal EnableDelayedExpansion

set "-euox" "pipefail"
readonly "GO_VERSION=1.21.1"
readonly "DEFAULT_GO_BINARY_DIRECTORY="%GOPATH%/go}/bin""
readonly "DEFAULT_GO_BINARY="%DEFAULT_GO_BINARY_DIRECTORY%/go""
CALL :main "%@%"

EXIT /B %ERRORLEVEL%

:is_go_binary_the_expected_version
IF [[ "%#%" NEQ "2" "]]" (
  echo "Usage: is_go_binary_the_expected_version <path/to/binary> <expected version>"
  exit "1"
)
local "go_binary="%~1""
local "expected_version="%~2""
REM UNKNOWN: {"type":"Pipeline","commands":[{"type":"Command","name":{"text":"bash","type":"Word"},"suffix":[{"text":"-c","type":"Word"},{"text":"\"${go_binary} version\"","expansion":[{"loc":{"start":1,"end":12},"parameter":"go_binary","type":"ParameterExpansion"}],"type":"Word"}]},{"type":"Command","name":{"text":"grep","type":"Word"},"suffix":[{"text":"--quiet","type":"Word"},{"text":"\"${expected_version}\"","expansion":[{"loc":{"start":1,"end":19},"parameter":"expected_version","type":"ParameterExpansion"}],"type":"Word"}]}]}
EXIT /B 0

:go_download_go
IF [[ "%#%" NEQ "2" "]]" (
  echo "Usage: go_install_go <path/to/binary> <expected version>"
  exit "1"
)
local "go_binary="%~1""
local "expected_version="%~2""
bash "-c" "%go_binary% install golang.org/dl/go%go_version%@latest"
SET "go_binary=%DEFAULT_GO_BINARY_DIRECTORY%/go%go_version%"
IF [[ NOT "-f" "%go_binary%" "]]" (
  IF [[ "-f" "%DEFAULT_GO_BINARY%" "]]" && CALL :is_go_binary_the_expected_version "%DEFAULT_GO_BINARY%" , "%go_version%" (
    SET "go_binary=%DEFAULT_GO_BINARY%"
  ) ELSE (
    echo "Could not find designated go binary after download" REM UNKNOWN: {"type":"Redirect","op":{"text":">&","type":"greatand"},"file":{"text":"2","type":"Word"}}
    exit "1"
  )
)
bash "-c" "%go_binary% download"
echo "%go_binary%"
EXIT /B 0

:install_go
IF [[ "%#%" NEQ "1" "]]" (
  echo "Usage: install_go <go version>" REM UNKNOWN: {"type":"Redirect","op":{"text":">&","type":"greatand"},"file":{"text":"2","type":"Word"}}
  exit "1"
)

local "go_version="%~1""
local "go_binary"
local "go_in_path"

SET _INTERPOLATION_0=
FOR /f "delims=" %%a in ('command -v go') DO (SET "_INTERPOLATION_0=!_INTERPOLATION_0! %%a")
SET "go_in_path=!_INTERPOLATION_0:~1!"
IF [[ "-n" "!go_in_path!" "]]" (
  IF CALL :is_go_binary_the_expected_version "!go_in_path!" , "!go_version!" (
    SET "go_binary=!go_in_path!"
  ) ELSE (
    SET _INTERPOLATION_2=
    FOR /f "delims=" %%a in ('go_download_go "${go_in_path}" "${go_version}"') DO (SET "_INTERPOLATION_2=!_INTERPOLATION_2! %%a")
    SET "go_binary=!_INTERPOLATION_2:~1!"
  )
) ELSE (
  IF [[ "-f" "!DEFAULT_GO_BINARY!" "]]" (
    IF CALL :is_go_binary_the_expected_version "!DEFAULT_GO_BINARY!" , "!go_version!" (
      SET "go_binary=!DEFAULT_GO_BINARY!"
    ) ELSE (
      SET _INTERPOLATION_1=
      FOR /f "delims=" %%a in ('go_download_go "${DEFAULT_GO_BINARY}" "${go_version}"') DO (SET "_INTERPOLATION_1=!_INTERPOLATION_1! %%a")
      SET "go_binary=!_INTERPOLATION_1:~1!"
    )
  ) ELSE (
    pushd "!HOME!" REM UNKNOWN: {"type":"Redirect","op":{"text":">&","type":"greatand"},"file":{"text":"2","type":"Word"}}
    local "url="https://dl.google.com/go/go!go_version!.linux-amd64.tar.gz""
    curl "--request" "GET" "!url!" "--output" "go.linux-amd64.tar.gz" "--silent"
    tar "xvf" "go.linux-amd64.tar.gz" REM UNKNOWN: {"type":"Redirect","op":{"text":">","type":"great"},"file":{"text":"/dev/null","type":"Word"}} REM UNKNOWN: {"type":"Redirect","op":{"text":">&","type":"greatand"},"file":{"text":"1","type":"Word"},"numberIo":{"text":"2","type":"io_number"}}
    IF [[ NOT "-f" "!DEFAULT_GO_BINARY!" "]]" (
      echo "Could not extract go from archive" REM UNKNOWN: {"type":"Redirect","op":{"text":">&","type":"greatand"},"file":{"text":"2","type":"Word"}}
      popd REM UNKNOWN: {"type":"Redirect","op":{"text":">&","type":"greatand"},"file":{"text":"2","type":"Word"}}
      exit "2"
    )
    popd REM UNKNOWN: {"type":"Redirect","op":{"text":">&","type":"greatand"},"file":{"text":"2","type":"Word"}}
    export "PATH="!PATH!:!DEFAULT_GO_BINARY_DIRECTORY!""
    SET "go_binary=!DEFAULT_GO_BINARY!"
  )
)
echo "!go_binary!"
EXIT /B 0

:compile_binaries
local "path_to_binary"
SET _INTERPOLATION_3=
FOR /f "delims=" %%a in ('install_go "${GO_VERSION}"') DO (SET "_INTERPOLATION_3=!_INTERPOLATION_3! %%a")
SET "path_to_binary=!_INTERPOLATION_3:~1!"
bash "-c" "GOOS=darwin GOARCH=amd64 !path_to_binary! build -o target/classes/sonar-helm-for-iac-darwin-amd64"
bash "-c" "GOOS=darwin GOARCH=arm64 !path_to_binary! build -o target/classes/sonar-helm-for-iac-darwin-arm64"
bash "-c" "GOOS=linux GOARCH=amd64 !path_to_binary! build -o target/classes/sonar-helm-for-iac-linux-amd64"
bash "-c" "GOOS=windows GOARCH=amd64 !path_to_binary! build -o target/classes/sonar-helm-for-iac-windows-amd64.exe"
EXIT /B 0

:generate_test_report
local "path_to_binary"
SET _INTERPOLATION_4=
FOR /f "delims=" %%a in ('install_go "${GO_VERSION}"') DO (SET "_INTERPOLATION_4=!_INTERPOLATION_4! %%a")
SET "path_to_binary=!_INTERPOLATION_4:~1!"
bash "-c" "!path_to_binary! test -json > target/test-report.out"
EXIT /B 0

:main
IF [[ "!#!" NEQ "1" "]]" (
  echo "Usage: %~0 build | clean | test"
  exit "0"
)
local "command="%~1""
IF "!command!"=="build" (
  CALL :compile_binaries
) ELSE IF "!command!"=="test" (
  CALL :generate_test_report
) ELSE IF "!command!"=="clean" (
  DEL  "target/classes/sonar-helm-for-iac-*"
  DEL  "test-report.out"
) ELSE (
  echo "Unrecognized command !command!" REM UNKNOWN: {"type":"Redirect","op":{"text":">&","type":"greatand"},"file":{"text":"2","type":"Word"}}
  exit "1"
)
exit "0"
EXIT /B 0



