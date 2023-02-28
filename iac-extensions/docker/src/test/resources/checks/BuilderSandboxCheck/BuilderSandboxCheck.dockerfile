# syntax=docker/dockerfile:1-labs
FROM ubuntu:22.04

# Noncompliant@+1 {{Make sure that disabling the builder sandbox is safe here.}}
RUN --security=insecure cat /proc/self/status > /output.txt
#   ^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN --security=insecure ["cat", "/proc/self/status > /output.txt"]

RUN --security=sandbox cat /proc/self/status > /output.txt
RUN cat /proc/self/status > /output.txt
RUN --network=none pip install --find-links wheels mypackage

RUN --SECURITY=insecure cat /proc/self/status > /output.txt
RUN --security=INSECURE cat /proc/self/status > /output.txt

ARG security=insecure
# Noncompliant@+1
RUN --security=$security cat /proc/self/status > /output.txt
