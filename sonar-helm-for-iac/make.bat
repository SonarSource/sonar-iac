@echo off
setlocal EnableDelayedExpansion

REM set "-euox" "pipefail"
SET GO_VERSION=1.21.1
SET DEFAULT_GO_BINARY_DIRECTORY="%GOPATH%/go}/bin"
SET DEFAULT_GO_BINARY="%DEFAULT_GO_BINARY_DIRECTORY%/go"
REM CALL :main "%@%"
CALL :main %*

EXIT /B %ERRORLEVEL%

:compile_binaries
go build -o target/classes/sonar-helm-for-iac-windows-amd64.exe
EXIT /B 0

:generate_test_report
go test -json > target/test-report.out
EXIT /B 0

:main
set argC=0
for %%x in (%*) do Set /A argC+=1
IF %argC% NEQ 1 (
  echo "Usage: %~0 build | clean | test"
  exit "0"
)

IF "%~1%" == "build" (
  CALL :compile_binaries
) ELSE IF "%~1%"=="test" (
  CALL :generate_test_report
) ELSE IF "%~1%"=="clean" (
  DEL  "target/classes/sonar-helm-for-iac-*"
  DEL  "test-report.out"
) ELSE (
  echo "Unrecognized command %~1%"
  exit "1"
)
exit "0"
EXIT /B 0



