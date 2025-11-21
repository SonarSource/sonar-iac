FROM ubuntu:22.04

ENV OPENSSL3_URL "https://www.openssl.org/source/openssl-3.0.3.tar.gz"
ARG OPENSSL3_URL_2="https://www.openssl.org/source/openssl-3.0.3.tar.gz"

# FN: shell form not supported in community edition
RUN wget https://might-redirect.example.com/install.sh -q -O - | sh

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "wget https://might-redirect.example.com/install.sh -q -O - | sh"]
RUN ["sh", "-c", "wget https://might-redirect.example.com/install.sh --max-redirect=1 -q -O - | sh"]
RUN ["sh", "-c", "wget https://might-redirect.example.com/install.sh -q --max-redirect=1 -O - | sh"]
RUN ["sh", "-c", "wget https://might-redirect.example.com/install.sh --max-redirect=0 -q -O - | sh"]
RUN ["sh", "-c", "wget https://might-redirect.example.com/install.sh -q --max-redirect=0 -O - | sh"]
RUN ["sh", "-c", "wget ${OPENSSL3_URL}"]
RUN ["sh", "-c", "wget ${OPENSSL3_URL_2}"]
RUN ["sh", "-c", "wget --quiet https://download.oracle.com/berkeley-db/$BDBVER.tar.gz; echo \"Done\""]

# Noncompliant@+1
RUN ["wget", "https://cmake.org/files/v3.7/cmake-3.7.2-SHA-256.txt.asc"]

# Noncompliant@+1
RUN ["wget", "https://cmake.org/files/v3.7/cmake-3.7.2-SHA-256.txt.asc"]

# Noncompliant@+1
RUN ["wget", "https://cmake.org/files/v3.7/cmake-3.7.2-SHA-256.txt.asc"]

# Noncompliant@+1
RUN ["wget", "--secure-protocol=TLSv1_2", "-q", "-O", "-", "https://might-redirect.example.com/install.sh"]
# Noncompliant@+1
RUN ["wget", "--secure-protocol=TLSv1_2", "https://might-redirect.example.com/install.sh", "-q", "-O", "-"]
# Noncompliant@+1
RUN ["wget", "--secure-protocol=TLSv1_2", "-q", "-O", "-", "--", "https://might-redirect.example.com/install.sh"]

RUN ["wget", "--max-redirect=0", "--secure-protocol=TLSv1_2", "-q", "-O", "-", "https://might-redirect.example.com/install.sh"]
RUN ["wget", "--secure-protocol=TLSv1_2", "-q", "-O", "-", "--max-redirect=0", "https://might-redirect.example.com/install.sh"]
RUN ["wget", "--secure-protocol=TLSv1_2", "https://might-redirect.example.com/install.sh", "-q", "-O", "-", "--max-redirect=0"]
RUN ["wget", "--secure-protocol=TLSv1_2", "https://might-redirect.example.com/install.sh", "--max-redirect=0", "-q", "-O", "-"]
RUN ["wget", "--secure-protocol=TLSv1_2", "-q", "-O", "-", "--max-redirect=0", "--", "https://might-redirect.example.com/install.sh"]
RUN ["wget", "--secure-protocol=TLSv1_2", "-q", "-O", "-", "--max-redirect=0", "https://might-redirect.example.com/install.sh"]
RUN ["wget", "--secure-protocol=TLSv1_2", "-q", "-O", "-", "--max-redirect=0", "https://might-redirect.example.com/install.sh"]
RUN ["wget", "--secure-protocol=TLSv1_2", "-q", "-O", "-", "--max-redirect=0", "https://might-redirect.example.com/install.sh"]
RUN ["wget", "--secure-protocol=TLSv1_2", "-q", "-O", "-", "--max-redirect=0", "https://might-redirect.example.com/install.sh"]

RUN ["foobar"]

ENV BDBVER db-4.8.30.NC
