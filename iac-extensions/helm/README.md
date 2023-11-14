## Setup
* Install Go version >= 1.19
* Configure environment to compile protobufs: https://protobuf.dev/getting-started/gotutorial/#compiling-protocol-buffers

## Build
* Run `mvn exec:exec@compile-template-wrapper` to build the dynamic library
* Run `mvn exec:exec@compile-protobuf-java` to generate the protobuf java classes
