FROM scratch
ARG SENSITIVE_OPTION=k
ARG COMPLIANT_OPTION=other

## All sensitive options
# FN: shell form not supported in community edition
  RUN curl -k https://expired.badssl.com

# Noncompliant@+1 {{Enable server certificate validation on this SSL/TLS connection.}}
RUN ["curl", "-k", "https://expired.badssl.com"]
#    ^^^^^^^^^^^^
# Noncompliant@+1
RUN ["curl", "--proxy-insecure", "https://expired.badssl.com"]
# Noncompliant@+1
RUN ["curl", "--doh-insecure", "https://expired.badssl.com"]

## Other sensitive use cases
# Noncompliant@+1
RUN ["curl", "--random1", "--random2", "-k", "https://expired.badssl.com"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "other call && curl --random1 --random2 -k https://expired.badssl.com"]
RUN ["sh", "-c", "curl -$SENSITIVE_OPTION https://expired.badssl.com"]

## Compliant
RUN ["curl", "https://www.sonarsource.com"]
RUN ["curl", "https://expired.badssl.com", "-k"]
RUN ["curl", "-insecure", "https://expired.badssl.com"]
RUN ["sh", "-c", "curl -$COMPLIANT_OPTION https://expired.badssl.com"]
RUN ["sh", "-c", "curl -$UNKNOWN https://expired.badssl.com"]
RUN ["curl", "-k=1", "https://expired.badssl.com"]
# FN : not able to ignore option with arguments
RUN ["curl", "-out", "out.txt", "-k", "https://expired.badssl.com"]
