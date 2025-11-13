ARG GO_VERSION=1.25.1
# Possible values: dev_custom_cert, dev
# Note: This Dockerfile is for local development only. GitHub Actions does not use it.
ARG BUILD_ENV=dev
ARG ARCH=amd64

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

# If you have issue like:
# failed to solve: failed to compute cache key: failed to calculate checksum of ref xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx::yyyyyyyyyyyyyyyyyyyyyyyyy: "/Sonar-CloudFlare-Inspection-Cert.cer": not found
# please copy the certificate file into `sonar-helm-for-iac` folder, or
# define `trafficInspection=false` property, e.g.: `./gradlew -PtrafficInspection=false build`
ARG CA_CERT=Sonar-CloudFlare-Inspection-Cert
ARG CERT_LOCATION=/usr/local/share/ca-certificates

ONBUILD COPY ${CA_CERT}.cer ${CERT_LOCATION}/${CA_CERT}.cer
ONBUILD WORKDIR ${CERT_LOCATION}
ONBUILD RUN cp ${CA_CERT}.cer ${CA_CERT}.crt && update-ca-certificates

# Final stage using the base image from one of the previous stages.
FROM ${BUILD_ENV}_image

ARG GO_VERSION
ARG GOLANG_CI_LINT_VERSION=2.4.0

USER sonarsource

RUN curl --proto "=https" -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b /home/sonarsource/go/bin v${GOLANG_CI_LINT_VERSION}

ENV PATH="/opt/go/bin:/opt/protoc/bin:/opt/musl/bin:/home/sonarsource/go/bin:${PATH}"
ENV GO_CROSS_COMPILE=1
