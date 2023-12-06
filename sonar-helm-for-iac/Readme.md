Sonar Helm for IaC
==========

A help project written in GoLang for support evaluating and parsing Helm Chars in sonar-iac.

It is a small glue code for re-use the implementation of Helm Charts templates evaluations and parsing.

## Requirements
* Go 1.21.1
* gcc (mingw on Windows)
* protoc 25.0
* protoc-gen-go 1.31.0

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

### Compile protobuf to Go
```shell
mvn exec:exec@compile-protobuf-go
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

## Tips and Tricks

### Exception during build: "missing go.sum entry for module providing package"

```shell
go mod tidy
```
