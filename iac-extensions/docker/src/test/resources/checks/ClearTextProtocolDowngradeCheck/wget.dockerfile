FROM ubuntu:22.04

ENV OPENSSL3_URL "https://www.openssl.org/source/openssl-3.0.3.tar.gz"
ARG OPENSSL3_URL_2="https://www.openssl.org/source/openssl-3.0.3.tar.gz"
# Noncompliant@+1 {{Not disabling redirects might allow for redirections to insecure websites. Make sure it is safe here.}}
RUN wget https://might-redirect.example.com/install.sh -q -O - | sh
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN wget https://might-redirect.example.com/install.sh --max-redirect=1 -q -O - | sh

# Noncompliant@+1
RUN wget https://might-redirect.example.com/install.sh -q --max-redirect=1 -O - | sh


RUN wget https://might-redirect.example.com/install.sh --max-redirect=0 -q -O - | sh

RUN wget https://might-redirect.example.com/install.sh -q --max-redirect=0 -O - | sh

# Noncompliant@+1
RUN wget "https://cmake.org/files/v3.7/cmake-3.7.2-SHA-256.txt.asc"

# Noncompliant@+1
RUN wget 'https://cmake.org/files/v3.7/cmake-3.7.2-SHA-256.txt.asc'

# Noncompliant@+1
RUN wget https://cmake.org/files/v3.7/cmake-3.7.2-SHA-256.txt.asc

# Noncompliant@+1
RUN wget ${OPENSSL3_URL}

# Noncompliant@+1
RUN wget ${OPENSSL3_URL_2}

RUN foobar

