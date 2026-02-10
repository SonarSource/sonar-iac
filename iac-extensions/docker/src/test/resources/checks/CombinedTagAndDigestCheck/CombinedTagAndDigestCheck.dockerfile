ARG IMAGE_NAME=my-image
ARG TAG=1.2.3
ARG DIGEST=sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d

# Noncompliant@+1 {{Use either the version tag or the digest for the image instead of both.}}
FROM my-image:1.2.3@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Compliant: Digest is only specified
FROM my-image@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d

# Compliant: Version tag is only specified
FROM my-image:1.2.3

# Noncompliant@+1 {{Use either the version tag or the digest for the image instead of both.}}
FROM my-image:1.2.3@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d as imageAlias

# Compliant: non compliance highlighted already in alias statement
FROM imageAlias

# Compliant: compliant as alias uses digest
FROM my-image@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d as imageAliasDigest

# Compliant: compliant as alias uses digest
FROM imageAlias
# Compliant: compliant as alias uses digest
FROM imageAlias as imageAnotherAlias
# Compliant: compliant as alias uses digest
FROM imageAnotherAlias

# Noncompliant@+1 {{Use either the version tag or the digest for the image instead of both.}}
FROM imageAlias:1.2.3@sha256:26c68657ccce2cb0a31b330cb0be2b5e108d467f641c62e13ab40cbec258c68d
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Compliant: compliant as alias uses version tag
FROM my-image:1.2.3 as imageAliasWithTag

# Compliant: compliant as alias uses version tag
FROM imageAlias

# Noncompliant@+1 {{Use either the version tag or the digest for the image instead of both.}}
FROM ${IMAGE_NAME}:${TAG}@${DIGEST}
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Compliant: Digest is only specified
FROM ${IMAGE_NAME}@${DIGEST}

# Compliant: Version tag is only specified
FROM ${IMAGE_NAME}:${TAG}

# Noncompliant@+1 {{Use either the version tag or the digest for the image instead of both.}}
FROM ${IMAGE_NAME}:${UNRESOLVED}@${DIGEST}
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1 {{Use either the version tag or the digest for the image instead of both.}}
FROM ${IMAGE_NAME}:${TAG}@${UNRESOLVED}
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1 {{Use either the version tag or the digest for the image instead of both.}}
FROM ${IMAGE_NAME}:${UNRESOLVED}@${UNRESOLVED}
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^