ARG IMAGE_NAME=my-image
ARG IMAGE_WITH_TAG=my-image:1.2.3
ARG LATEST_TAG=latest
ARG NON_LATEST_TAG=1.2.3

# Noncompliant@+1
FROM --foo=bar foobar

# Noncompliant@+1 {{Use a specific version tag for the image.}}
FROM my-image
#    ^^^^^^^^
# Noncompliant@+1
FROM my-image:latest
#    ^^^^^^^^^^^^^^^

# Noncompliant@+1
FROM foobar
# Noncompliant@+1
FROM foobar:latest

# Noncompliant@+1
FROM --platform=foo bar
# Noncompliant@+1
FROM foobar AS fb
# Noncompliant@+1
FROM --platform=foo bar AS fb
# Noncompliant@+1
FROM foobar:latest AS fb
# Noncompliant@+1
FROM --platform=foo bar:latest
# Noncompliant@+1
FROM --platform bar:latest
# Noncompliant@+1
FROM --platform=foo bar:latest AS fb
# Noncompliant@+1
FROM foobar:latest as baseWithoutTag

# Noncompliant@+1
from foobar

# Noncompliant@+1
FROM ${IMAGE_NAME}:${LATEST_TAG}
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
FROM ${IMAGE_NAME}
#    ^^^^^^^^^^^^^

# Noncompliant@+2
FROM \
foobar

# Noncompliant@+2
FROM \
foobar:latest

# Compliant
FROM my-image:1.2.3 as baseWithTag
FROM my-image:1.2.3-alpine
FROM ubuntu:18.04 as local-toolchain-ubuntu18.04-manylinux2010
FROM foobar@12313423
# Above is Compliant as the usage of a digest shouldn't raise anything for this rule

FROM my-image:1.2.3 as baseWithTag
FROM baseWithTag
FROM baseWithTag as anotherAlias
FROM baseWithoutTag as builderWithoutTag
# All three above are Compliant because 'baseWithTag' and 'baseWithoutTag' are used as an alias


FROM ${IMAGE_NAME}:${NON_LATEST_TAG}
FROM ${IMAGE_WITH_TAG}
FROM ${UNRESOLVED}

FROM \
foobar:1.2.3

# Compliant, as this is not valid docker syntax but still parseable with our parser
FROM :
FROM :bar
FROM foo:

# Compliant: scratch is a special image that cannot have tag/digest
FROM scratch
FROM scratch:1.2.3
