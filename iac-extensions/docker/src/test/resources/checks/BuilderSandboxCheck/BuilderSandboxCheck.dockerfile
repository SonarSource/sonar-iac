# syntax=docker/dockerfile:1-labs
FROM ubuntu:22.04

# Noncompliant@+1 {{Make sure that disabling the builder sandbox is safe here.}}
RUN --security=insecure cat /proc/self/status > /output.txt
#   ^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN --security=insecure ["cat", "/proc/self/status > /output.txt"]

RUN --security=sandbox cat /proc/self/status > /output.txt


