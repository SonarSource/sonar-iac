FROM scratch
ARG SENSITIVE_OPTION=k
ARG COMPLIANT_OPTION=other

## All sensitive options
# FN: shell form not supported in community edition
  RUN wget --no-check-certificate https://expired.badssl.com

## Other sensitive use cases
# Noncompliant@+1 {{Enable server certificate validation on this SSL/TLS connection.}}
RUN ["wget", "--no-check-certificate", "https://expired.badssl.com"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

## Compliant
RUN ["wget", "https://www.sonarsource.com"]
RUN ["wget", "https://expired.badssl.com", "--no-check-certificate"]
