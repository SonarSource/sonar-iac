Sonar Helm for IaC
==========

A help project written in GoLang for support evaluating and parsing Helm Chars in sonar-iac.

It is a small glue code for re-use the implementation of Helm Charts templates evaluations and parsing.

## Requirements
* Docker

## The build

### Build Docker Image
```shell
mvn exec:exec@build-docker-image
```

### Execute Docker Image, generating Go code, build Go binaries, executing tests, validate license headers

```shell
mvn exec:exec@build-go-code
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

## Tips and Tricks

### Exception during build: "missing go.sum entry for module providing package"

```shell
go mod tidy
```
