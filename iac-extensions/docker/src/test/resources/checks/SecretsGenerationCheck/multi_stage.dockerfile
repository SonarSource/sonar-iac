FROM ubuntu:22.04 as build

# FN: shell form not supported in community edition
RUN curl --user user:password https://example.com

FROM build

# Noncompliant@+1
RUN ["curl", "--user", "me:password", "https://example.com"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
