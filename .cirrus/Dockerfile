ARG CIRRUS_AWS_ACCOUNT
FROM ${CIRRUS_AWS_ACCOUNT}.dkr.ecr.eu-central-1.amazonaws.com/base:j17-latest

USER root
RUN apt-get update && apt-get install -y gcc && rm -rf -- /var/lib/apt/lists/* /var/cache/apt/archives/*
USER sonarsource
