FROM scratch

## All sensitive subcommands
# Noncompliant@+1 {{Change this code to enforce TLS 1.2 or above.}}
RUN curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010
#   ^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN curl --tls-max 1.1 https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl -out out.txt -k --tls-max 1.1 https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl --tls-max 1.1 -out out.txt -k https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max 1.0 --request PUT https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN curl --user myusername:mypassword --tls-max 1.0 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
   RUN curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN other command curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN other command; curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010; other command
# Noncompliant@+1
RUN other command && curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010 --output path/to/file && other command
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max 1.0 https://tls-v1-0.badssl.com:1010 --output path/to/file && other command
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max 1.0 https://tls-v1-0.badssl.com:1010 --output path/to/file \
      && other command

# Noncompliant@+1
RUN other command | curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
   RUN ["curl", "--tls-max", "1.0", "https://tls-v1-0.badssl.com:1010"]
#       ^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN <<-EOF curl --tls-max 1.1 https://tls-v1-1.badssl.com:1011
  some value
EOF
# Noncompliant@+2
RUN <<-EOF
  curl --tls-max 1.1 https://tls-v1-1.badssl.com:1011
EOF

ENV TLS_VERSION_ENV=1.1
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max $TLS_VERSION_ENV https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max ${TLS_VERSION_ENV} https://tls-v1-0.badssl.com:1010 --output path/to/file

ARG TLS_VERSION_ARG=1.0
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max $TLS_VERSION_ARG https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max ${TLS_VERSION_ARG} https://tls-v1-0.badssl.com:1010 --output path/to/file

## All compliant subcommands
RUN curl https://www.sonarsource.com
RUN curl --tlsv1.2 https://tls-v1-1.badssl.com:1011
RUN curl --data 'name=bob' --tls-max $UNKNOWN https://tls-v1-0.badssl.com:1010 --output path/to/file
