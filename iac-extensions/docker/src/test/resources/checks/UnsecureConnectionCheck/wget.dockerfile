FROM scratch
ARG SENSITIVE_OPTION=k
ARG COMPLIANT_OPTION=other

## All sensitive options
# Noncompliant@+1 {{Disabling TLS certificate verification is security-sensitive.}}
  RUN wget --no-check-certificate https://expired.badssl.com
#     ^^^^^^^^^^^^^^^^^^^^^^^^^^^

## Other sensitive use cases
# Noncompliant@+1
RUN wget --random1 --random2 --no-check-certificate https://expired.badssl.com

## Compliant
RUN wget https://www.sonarsource.com
