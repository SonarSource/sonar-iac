FROM ubuntu:22.04

ARG PASSWORD

# Checks for exact location

# FN: shell form not supported in community edition
RUN wget --ftp-password="secret" https://example.com

# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
RUN ["wget", "--ftp-password=\"This should be kept secret\"", "https://example.com"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "wget --user=guest --http-password=\"This should be kept secret\" https://example.com &&"]
RUN ["sh", "-c", "cd /tmp && wget --user=guest --proxy-password=\"This should be kept secret\" https://example.com"]

# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
RUN ["wget", "--user=guest", "--password='This should be kept secret'", "https://example.com"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
