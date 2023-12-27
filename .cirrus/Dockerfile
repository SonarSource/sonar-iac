ARG CIRRUS_AWS_ACCOUNT
FROM ${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest

ARG GO_VERSION
ARG PROTOC_VERSION
ARG MUSL_VERSION=1.2.4

USER root
# Install gcc, Go, protoc and musl.
# Static linking with musl reduces binary size (from ~10MB to ~6.6MB on Linux in the initial experiments).
RUN apt-get update && apt-get --no-install-recommends install -y gcc make && rm -rf -- /var/lib/apt/lists/* /var/cache/apt/archives/* && \
    wget https://dl.google.com/go/go${GO_VERSION}.linux-amd64.tar.gz >/dev/null 2>&1 && \
    tar xf go${GO_VERSION}.linux-amd64.tar.gz --directory=/opt && \
    wget https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOC_VERSION}/protoc-${PROTOC_VERSION}-linux-x86_64.zip >/dev/null 2>&1 && \
    unzip protoc-${PROTOC_VERSION}-linux-x86_64.zip -d /opt/protoc && \
    curl --proto "https" https://www.musl-libc.org/releases/musl-${MUSL_VERSION}.tar.gz --output musl-${MUSL_VERSION}.tar.gz && \
    tar xf musl-${MUSL_VERSION}.tar.gz && \
    cd musl-${MUSL_VERSION} && ./configure --prefix=/opt/musl --enable-gcc-wrapper=yes && make && make install

USER sonarsource

ENV PATH="/opt/go/bin:/opt/protoc/bin:/opt/musl/bin:${PATH}"
