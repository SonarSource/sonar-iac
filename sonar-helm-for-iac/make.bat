@echo off
setlocal EnableDelayedExpansion

CALL :go_install_check
CALL :main %*

EXIT /B %ERRORLEVEL%

:compile_binaries
SET GOOS=windows
SET GOARCH=amd64
SET CGO_ENABLED=0
CALL go build -buildmode=exe -o build/executable/sonar-helm-for-iac-windows-amd64

EXIT /B 0

:generate_test_report
SET CGO_ENABLED=0
CALL go test ./... -json > build/test-report.out
EXIT /B 0

:go_install_check
WHERE /q go
IF ERRORLEVEL 1 (
    ECHO go is not installed
    EXIT "1"
)
EXIT /B

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
  DEL "build\executable\sonar-helm-for-iac-*"
  DEL "test-report.out"
) ELSE (
  echo "Unrecognized command %~1%"
  exit "1"
)
exit "0"
EXIT /B 0



