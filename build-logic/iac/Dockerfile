ARG GO_VERSION=1.23.4
# Possible values: ci, dev_custom_cert, dev
ARG BUILD_ENV=ci
# Placeholder; actual value is present on CI
ARG CIRRUS_AWS_ACCOUNT=unknown

# For local builds, build an image with Go and a dedicated user.
FROM golang:${GO_VERSION}-bookworm AS dev_base

# This value should match the host user's UID for volume mounts to work correctly.
ARG UID=1000

RUN groupadd --system --gid ${UID} sonarsource \
      && useradd --system --gid sonarsource --uid ${UID} --shell /bin/bash --create-home sonarsource \
      && mkdir -p /home/sonarsource/sonar-helm-for-iac

# If the custom certificate is not provided, use the base image.
# This mode can be activated by providing build argument `BUILD_ENV=dev`.
FROM dev_base AS dev_image

# Install a custom certificate from the build context into the image.
# This mode can be activated by providing build argument `BUILD_ENV=dev_custom_cert`.
FROM dev_base AS dev_custom_cert_image

ARG CA_CERT=Sonar-FGT-FW-TLS-Traffic-Inspection
ARG CERT_LOCATION=/usr/local/share/ca-certificates

ONBUILD COPY ${CA_CERT}.cer ${CERT_LOCATION}/${CA_CERT}.cer
ONBUILD WORKDIR ${CERT_LOCATION}
ONBUILD RUN cp ${CA_CERT}.cer ${CA_CERT}.crt && update-ca-certificates

# For CI, use a different base image and install Go manually.
# This mode can be activated by providing build argument `BUILD_ENV=ci`.
FROM ${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest AS ci_image

ARG GO_VERSION

USER root

RUN wget --max-redirect=0 https://dl.google.com/go/go${GO_VERSION}.linux-amd64.tar.gz >/dev/null 2>&1 && \
        tar xf go${GO_VERSION}.linux-amd64.tar.gz --directory=/opt

# Final stage using the base image from one of the previous stages.
FROM ${BUILD_ENV}_image

ARG GO_VERSION
ARG MUSL_VERSION=1.2.4
ARG GOLANG_CI_LINT_VERSION=1.62.2

USER root
# Additionally install gcc and musl.
# Static linking with musl reduces binary size (from ~10MB to ~6.6MB on Linux in the initial experiments).
ADD https://www.musl-libc.org/releases/musl-${MUSL_VERSION}.tar.gz musl-${MUSL_VERSION}.tar.gz
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    --mount=type=cache,target=/var/lib/apt,sharing=locked <<EOF
    apt-get update
    apt-get --no-install-recommends install -y ca-certificates gcc git make unzip
    tar xf musl-${MUSL_VERSION}.tar.gz
    cd musl-${MUSL_VERSION}
    ./configure --prefix=/opt/musl --enable-gcc-wrapper=yes
    make
    make install
EOF

USER sonarsource

RUN curl --proto "=https" -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b /home/sonarsource/go/bin v${GOLANG_CI_LINT_VERSION}

ENV PATH="/opt/go/bin:/opt/protoc/bin:/opt/musl/bin:/home/sonarsource/go/bin:${PATH}"
ENV GO_CROSS_COMPILE=1
