Sonar Helm for IaC
==========

A help project written in GoLang for support evaluating and parsing Helm Chars in sonar-iac.

It is a small glue code for re-use the implementation of Helm Charts templates evaluations and parsing.

## Requirements

* Go 1.21.1

## The build

### The build using installed Go (Windows, Linux, MacOS)
```shell
go build
```

### The build if Go is not installed (Linux only and CI)
```shell
./make.sh build
```

## Run test

### Run tests using installed Go (Windows, Linux, MacOS)
```shell
go test
```

### The build if Go is not installed (Linux only and CI)
```shell
./make.sh test
```

## Execution

### The execution in Linux and MacOS 
```shell
./sonar-helm-for-iac
```

### The execution in Windows
```shell
sonar-helm-for-iac.exe
```

## Tricks and tips

### missing go.sum entry for module providing package

```shell
go mod tidy
```
