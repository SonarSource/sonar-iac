FROM scratch
ARG SENSITIVE_OPTION=k
ARG COMPLIANT_OPTION=other

## All sensitive options
# Noncompliant@+1 {{Disabling TLS certificate verification is security-sensitive.}}
  RUN curl -k https://expired.badssl.com
#     ^^^^^^^
# Noncompliant@+1
RUN curl --insecure https://expired.badssl.com
# Noncompliant@+1
RUN curl --proxy-insecure https://expired.badssl.com
# Noncompliant@+1
RUN curl --doh-insecure https://expired.badssl.com

## Other sensitive use cases
# Noncompliant@+1
RUN curl --random1 --random2 -k https://expired.badssl.com
# Noncompliant@+1
RUN other call && curl --random1 --random2 -k https://expired.badssl.com
# Noncompliant@+1
RUN curl -$SENSITIVE_OPTION https://expired.badssl.com

## Compliant
RUN curl https://www.sonarsource.com
RUN curl https://expired.badssl.com -k
RUN curl -insecure https://expired.badssl.com
RUN curl -$COMPLIANT_OPTION https://expired.badssl.com
RUN curl -$UNKNOWN https://expired.badssl.com
RUN curl -k=1 https://expired.badssl.com
# FN : not able to ignore option with arguments
RUN curl -out out.txt -k https://expired.badssl.com
