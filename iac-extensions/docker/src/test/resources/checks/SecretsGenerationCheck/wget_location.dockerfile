FROM ubuntu:22.04

ARG PASSWORD

# Checks for exact location

# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
RUN wget --user=guest --password="This should be kept secret" https://example.com
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
RUN wget --ftp-password="This should be kept secret" https://example.com
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
RUN wget --user=guest --http-password="This should be kept secret" https://example.com && \
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  unzip file.zip

# Noncompliant@+2 {{Change this code not to store a secret in the image.}}
RUN cd /tmp && \
  wget --user=guest --proxy-password="This should be kept secret" https://example.com
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
