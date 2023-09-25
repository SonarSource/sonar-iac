FROM scratch
ARG SENSITIVE_OPTION=k
ARG COMPLIANT_OPTION=other

## All sensitive options
# Noncompliant@+1 {{Enable server certificate validation on this SSL/TLS connection.}}
  RUN wget --no-check-certificate https://expired.badssl.com
#     ^^^^^^^^^^^^^^^^^^^^^^^^^^^

## Other sensitive use cases
# Noncompliant@+1
RUN wget --random1 --random2 --no-check-certificate https://expired.badssl.com

## Compliant
RUN wget https://www.sonarsource.com
RUN wget https://expired.badssl.com --no-check-certificate
