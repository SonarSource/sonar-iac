ARG IMAGE_NAME=my-image
ARG IMAGE_WITH_TAG=my-image:1.2.3
ARG LATEST_TAG=latest
ARG NON_LATEST_TAG=1.2.3
ARG IMAGE_WITH_DIGEST=my-image@sha256:06b5d30fabc1fc574f2ecab87375692299d45f8f190d9b71f512deb494114e1f
ARG IMAGE_WITH_TAG_AND_DIGEST=my-image:1.2.3@sha256:06b5d30fabc1fc574f2ecab87375692299d45f8f190d9b71f512deb494114e1f
ARG DIGEST=sha256:06b5d30fabc1fc574f2ecab87375692299d45f8f190d9b71f512deb494114e1f

# Noncompliant@+1 {{Add digest to this tag to pin the version of the base image.}}
FROM my-images:20.04
#    ^^^^^^^^^^^^^^^

# Noncompliant@+1
FROM ${IMAGE_NAME}:${NON_LATEST_TAG}
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
FROM ${IMAGE_WITH_TAG}
# Noncompliant@+1
FROM my-image:1.2.3 as baseWithTag

# Noncompliant@+2
FROM \
foobar:1.2.3

FROM ubuntu@sha256:06b5d30fabc1fc574f2ecab87375692299d45f8f190d9b71f512deb494114e1f

# Supported but undocumented feature of docker to have both tag and digest at the same time
FROM ubuntu:22.04@sha256:06b5d30fabc1fc574f2ecab87375692299d45f8f190d9b71f512deb494114e1f

FROM foobar@12313423
# Above is Compliant as the usage of a digest shouldn't raise anything for this rule

FROM ${UNRESOLVED}

FROM ${IMAGE_WITH_TAG}@${DIGEST}
FROM ${IMAGE_WITH_TAG_AND_DIGEST}
FROM ${IMAGE_WITH_DIGEST}
FROM ${IMAGE_NAME}@${DIGEST}


# Should not raise because the SpecificVersionTagCheck already raises in this cases
FROM my-image
FROM my-image:latest
FROM foobar
FROM foobar:latest
FROM --platform=foo bar
FROM foobar AS fb
FROM --platform=foo bar AS fb
FROM foobar:latest AS fb
FROM --platform=foo bar:latest
FROM --platform bar:latest
FROM --platform=foo bar:latest AS fb
FROM ${IMAGE_NAME}:${LATEST_TAG}
FROM ${IMAGE_NAME}
FROM \
foobar

FROM \
foobar:latest

# Compliant as aliases don't contain a tag / digest
FROM foobar:latest as baseWithoutTag
FROM baseWithTag
FROM baseWithTag as anotherAlias
FROM baseWithoutTag as builderWithoutTag


# Compliant, as this is not valid docker syntax but still parseable with our parser
FROM :
FROM :bar
FROM foo:
FROM foo@

# Compliant: scratch is a special image that cannot have tag/digest
FROM scratch

# Compliant, unresolved myBase reference should not raise an issue, could lead to a FP
FROM ${UNRESOLVED} as myBase
FROM myBase
