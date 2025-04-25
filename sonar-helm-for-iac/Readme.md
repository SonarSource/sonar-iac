Sonar Helm for IaC
==========

A help project written in GoLang for support evaluating and parsing Helm Chars in sonar-iac.

It is a small glue code for re-use the implementation of Helm Charts templates evaluations and parsing.

## Requirements
* Docker (specifically, Docker Buildx)
* CA certificate for FortiClient traffic inspection

## The build

### Build Docker Image

Building the docker image locally requires the traffic inspection certificate to be located in the directory of the Go subproject.

```shell
../gradlew :sonar-helm-for-iac:buildDockerImage
```

In case you system does not require the certificate for traffic inspection set `trafficInspection=false` while running any Gradle task.

```shell
../gradlew -DtrafficInspection=false :sonar-helm-for-iac:buildDockerImage
```

### Execute Docker Image, generating Go code, build Go binaries, executing tests, validate license headers

```shell
../gradlew :sonar-helm-for-iac:compileGonext to the Dockerfile.
```

## Execution

### The execution in Linux and MacOS 
```shell
./sonar-helm-for-iac
```

### The execution in Windows
```shell
./sonar-helm-for-iac.exe
```

## Modified `text/template` dependency

This project is using a modified version of the `text/template` package from the GoLang standard library. The source code can be found at [SonarSource/go](https://github.com/SonarSource/go). The version has the following modifications:
* Comment nodes are added into the AST by default
* Nodes are enhanced with `Length` field, and `Pos` field is replaced with `StartOffset`. This better represents node location and can serve for precise highlighting and not only for error reporting, like in the original library.

### Working on the modified `text/template` package

To make new changes and use them in sonar-iac, tag the relevant commit on the `SonarSource/go` repository with a new version number. Then, update the `go.mod` file in the `sonar-helm-for-iac` project to use the new version. Convention for version format is the following: `<Go version>-<increment>`, e.g. `1.21.8-3`.

## Tips and Tricks

### Failing cirrus build task with the following message:
```
Checking if any files are uncommitted in the Go code (this may happen to the generated code). 
In case of of failure, run ./gradlew generateProto locally and commit the generated files.
git diff --exit-code --name-only -- sonar-helm-for-iac/
sonar-helm-for-iac/src/org.sonar.iac.helm/ast.pb.go
sonar-helm-for-iac/src/org.sonar.iac.helm/template-evaluation.pb.go
```
Run the following command and commit the generated files:
```shell
./gradlew generateProto
```

### Exception during build: `missing go.sum entry for module providing package`

Run the following command:
```shell
go mod tidy
```

### Segmentation fault during Go build on MacOS Sequoia

On MacOs Sequoia 15.3.1, the following issues appear sometimes: `illegal instructions` or `reflect: /usr/local/go/pkg/tool/linux_amd64/asm: signal: segmentation fault`.
Disabling `Use Rosetta for x86_64/amd64 emulation on Apple Silicon` in Docker Desktop settings usually solve the problem. 
