ARG JDK_VERSION=17
FROM public.ecr.aws/docker/library/eclipse-temurin:${JDK_VERSION}-jammy

ARG GO_VERSION=1.21.1
ARG PROTOC_VERSION=25.0
ARG MUSL_VERSION=1.2.4
ARG PROTOBUF_GO_VERSION=1.31.0

USER root
# Install gcc, Go, protoc and musl.
# Static linking with musl reduces binary size (from ~10MB to ~6.6MB on Linux in the initial experiments).
RUN apt-get update \
    && apt-get --no-install-recommends install -y gcc make \
    && rm -rf -- /var/lib/apt/lists/* /var/cache/apt/archives/*

RUN wget https://dl.google.com/go/go${GO_VERSION}.linux-amd64.tar.gz \
    && tar xf go${GO_VERSION}.linux-amd64.tar.gz --directory=/opt

RUN wget https://github.com/protocolbuffers/protobuf/releases/download/v${PROTOC_VERSION}/protoc-${PROTOC_VERSION}-linux-x86_64.zip >/dev/null 2>&1 \
    && unzip protoc-${PROTOC_VERSION}-linux-x86_64.zip -d /opt/protoc

RUN curl --proto "https" https://www.musl-libc.org/releases/musl-${MUSL_VERSION}.tar.gz --output musl-${MUSL_VERSION}.tar.gz \
    && tar xf musl-${MUSL_VERSION}.tar.gz \
    && cd musl-${MUSL_VERSION} \
    && ./configure --prefix=/opt/musl --enable-gcc-wrapper=yes \
    && make \
    && make install

USER sonarsource
RUN /opt/go/bin/go install google.golang.org/protobuf/cmd/protoc-gen-go@v${PROTOBUF_GO_VERSION}
ENV PATH="/opt/go/bin:/home/sonarsource/go/bin:/opt/protoc/bin:/opt/musl/bin:${PATH}"
