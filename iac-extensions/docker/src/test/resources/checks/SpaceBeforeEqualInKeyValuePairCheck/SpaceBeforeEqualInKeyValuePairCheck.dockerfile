FROM scratch

ARG WITH_EQUAL="=val"
ARG JUST_EQUAL==
ARG WITHOUT_EQUAL="val"


# Non compliant use cases
# Noncompliant@+1 {{Remove space before the equal sign in the key-value pair, as it can lead to unexpected behavior.}}
LABEL VERSION =42
#     ^^^^^^^^^^^
# Noncompliant@+1
LABEL VERSION = 42
# Noncompliant@+1
LABEL VERSION =42 NAME=BOB
# Noncompliant@+1
LABEL EQUAL =
# Noncompliant@+1
LABEL VERSION =$WITH_EQUAL
# Noncompliant@+1
LABEL VERSION =$WITH_EQUAL
# Noncompliant@+1
LABEL VERSION =$JUST_EQUAL
# Noncompliant@+1
LABEL VERSION =$WITHOUT_EQUAL


# Compliant use cases
LABEL VERSION=42
LABEL VERSION=42 NAME=BOB
LABEL VERSION 42
LABEL VERSION $WITH_EQUAL
LABEL VERSION $JUST_EQUAL
LABEL VERSION $WITHOUT_EQUAL
LABEL VERSION "=42"


# Other supported instruction
# Noncompliant@+1
ENV VAR =val
