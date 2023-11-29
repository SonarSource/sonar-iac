ARG CIRRUS_AWS_ACCOUNT
FROM ${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest

USER root
RUN apt-get update && apt-get install -y gcc && rm -rf -- /var/lib/apt/lists/* /var/cache/apt/archives/*

RUN wget https://dl.google.com/go/go1.21.1.linux-amd64.tar.gz >/dev/null 2>&1 && \
    tar xf go1.21.1.linux-amd64.tar.gz --directory=/opt

USER sonarsource

RUN export PATH="/opt/go/bin:${PATH}"
