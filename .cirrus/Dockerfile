ARG CIRRUS_AWS_ACCOUNT
FROM ${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest

ARG GO_VERSION
ARG PROTOC_VERSION

USER root
RUN apt-get update && apt-get install -y gcc && rm -rf -- /var/lib/apt/lists/* /var/cache/apt/archives/*

RUN wget https://dl.google.com/go/go${GO_VERSION}.linux-amd64.tar.gz >/dev/null 2>&1 && \
    tar xf go${GO_VERSION}.linux-amd64.tar.gz --directory=/opt && \
    wget https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOC_VERSION}/protoc-${PROTOC_VERSION}-linux-x86_64.zip >/dev/null 2>&1 && \
    unzip protoc-${PROTOC_VERSION}-linux-x86_64.zip -d /opt/protoc

USER sonarsource

ENV PATH="/opt/go/bin:/opt/protoc/bin:${PATH}"
