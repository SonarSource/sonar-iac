# Is noncompliant depending on the requiredLabels
# Noncompliant@+1 {{Add the missing labels: "anotherMissingLabel", "missingLabel" and "testLabel".}}
FROM scratch
LABEL maintainer="maintainer" description="description"
LABEL someLabel="label" \
      Version=1.0

