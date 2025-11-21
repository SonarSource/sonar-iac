FROM scratch

ARG SENSITIVE_PROGRAM=shasum
ARG COMPLIANT_PROGRAM=other
ARG SENSITIVE_FLAG_VALUE=1
ARG COMPLIANT_FLAG_VALUE=224

## All sensitive shasum commands
# FN: shell form not supported in community edition
RUN md5sum test.txt

# Noncompliant@+1
RUN ["shasum"]
# Noncompliant@+1
RUN ["shasum", "-a", "1"]
# Noncompliant@+1
RUN ["shasum", "--algorithm", "1"]

## Other sensitive cases
# Noncompliant@+1
RUN ["md5sum"]
# Noncompliant@+1 2
RUN ["shasum", "md5sum"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "echo \"d17b588f95b76ccb59064671bda61b589ec143d0  test.txt\" | sha1sum -c"]
RUN ["sh", "-c", "shasum -a $SENSITIVE_FLAG_VALUE"]

# Noncompliant@+1
RUN ["sh", "-c", "$SENSITIVE_PROGRAM"]

# Noncompliant@+1
RUN ["shasum", "-b", "-a", "1"]
# Noncompliant@+1
RUN ["shasum", "-b", "--algorithm", "1"]
# Noncompliant@+1
RUN ["shasum", "-b", "-algorithm", "224"]

## Compliant cases
RUN ["shasum", "-a", "224"]
RUN ["shasum", "--algorithm", "224"]
RUN ["sh", "-c", "$COMPLIANT_PROGRAM"]
RUN ["sh", "-c", "shasum -a $COMPLIANT_FLAG_VALUE"]
