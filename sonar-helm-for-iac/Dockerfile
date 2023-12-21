FROM eclipse-temurin:17.0.9_9-jdk-jammy

ARG GO_VERSION=1.21.1
ARG PROTOC_VERSION=25.0
ARG MUSL_VERSION=1.2.4

USER root
# Install gcc, Go, protoc and musl.
# Static linking with musl reduces binary size (from ~10MB to ~6.6MB on Linux in the initial experiments).
RUN apt-get update && apt-get --no-install-recommends install -y gcc make unzip git && rm -rf -- /var/lib/apt/lists/* /var/cache/apt/archives/* && \
    wget https://dl.google.com/go/go${GO_VERSION}.linux-amd64.tar.gz >/dev/null 2>&1 && \
    tar xf go${GO_VERSION}.linux-amd64.tar.gz --directory=/opt && \
    wget https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOC_VERSION}/protoc-${PROTOC_VERSION}-linux-x86_64.zip >/dev/null 2>&1 && \
    unzip protoc-${PROTOC_VERSION}-linux-x86_64.zip -d /opt/protoc && \
    curl --proto "https" https://www.musl-libc.org/releases/musl-${MUSL_VERSION}.tar.gz --output musl-${MUSL_VERSION}.tar.gz && \
    tar xf musl-${MUSL_VERSION}.tar.gz && \
    cd musl-${MUSL_VERSION} && ./configure --prefix=/opt/musl --enable-gcc-wrapper=yes && make && make install


RUN groupadd --system --gid 1000 sonarsource \
      && useradd --system --gid sonarsource --uid 1000 --shell /bin/bash --create-home sonarsource
USER sonarsource
RUN mkdir -p /home/sonarsource/sonar-helm-for-iac
WORKDIR /home/sonarsource/sonar-helm-for-iac

ENV PATH="/opt/go/bin:/opt/protoc/bin:/opt/musl/bin:/home/sonarsource/go/bin:${PATH}"

RUN GIT_SSL_NO_VERIFY=1 /opt/go/bin/go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.31.0

ENTRYPOINT protoc -I=${PWD} -I=/opt/protoc/include --go_out=${PWD} template-evaluation.proto && \
  ./make.sh clean && \
  GO_CROSS_COMPILE=1 ./make.sh build && \
  ./make.sh test
