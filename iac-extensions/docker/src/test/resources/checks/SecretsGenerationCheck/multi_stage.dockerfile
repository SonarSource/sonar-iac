FROM ubuntu:22.04 as build

# Noncompliant@+1
RUN curl --user me:password https://example.com

FROM build

# Noncompliant@+1
RUN curl --user me:password https://example.com
#   ^^^^^^^^^^^^^^^^^^^^^^^
