FROM scratch

ARG SENSITIVE_PROGRAM=shasum
ARG COMPLIANT_PROGRAM=other
ARG SENSITIVE_FLAG_VALUE=1
ARG COMPLIANT_FLAG_VALUE=0

## All sensitive shasum commands
# Noncompliant@+1 {{Using weak hashing algorithms is security-sensitive.}}
  RUN md5sum test.txt
#     ^^^^^^
# Noncompliant@+1
RUN sha1sum test.txt
# Noncompliant@+1
RUN shasum
# Noncompliant@+1
RUN shasum -a 1

## Other sensitive cases
# Noncompliant@+1
RUN md5sum
# Noncompliant@+1 2
RUN shasum md5sum
# Noncompliant@+1
RUN echo "d17b588f95b76ccb59064671bda61b589ec143d0  test.txt" | sha1sum -c
# Noncompliant@+1
RUN $SENSITIVE_PROGRAM
# Noncompliant@+1
RUN shasum -a $SENSITIVE_FLAG_VALUE

## Compliant cases
RUN shasum -a 2
RUN $COMPLIANT_PROGRAM
RUN shasum -a $COMPLIANT_FLAG_VALUE
