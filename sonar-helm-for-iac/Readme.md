Sonar Helm for IaC
==========

A help project written in GoLang for support evaluating and parsing Helm Chars in sonar-iac.

It is a small glue code for re-use the implementation of Helm Charts templates evaluations and parsing.

## Requirements
* Docker
* CA certificate for FortiClient traffic inspection

## The build

### Build Docker Image

Building the docker image locally requires the traffic inspection certificate to be located next to the Dockerfile.

```shell
../gradlew :sonar-helm-for-iac:buildDockerImage
```

In case you system does not require the certificate for traffic inspection set `trafficInspection=false` while running any Gradle task.

```shell
../gradlew -DtrafficInspection=false :sonar-helm-for-iac:buildDockerImage
```

### Execute Docker Image, generating Go code, build Go binaries, executing tests, validate license headers

```shell
../gradlew :sonar-helm-for-iac:compileGoCode
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

### Working on the modified `text/template` package

To make new changes and use them in sonar-iac, tag the relevant commit on the `SonarSource/go` repository with a new version number. Then, update the `go.mod` file in the `sonar-helm-for-iac` project to use the new version. Convention for version format is the following: `<Go version>-<increment>`, e.g. `1.21.8-3`.

```shell

## Tips and Tricks

### Exception during build: "missing go.sum entry for module providing package"

```shell
go mod tidy
```
