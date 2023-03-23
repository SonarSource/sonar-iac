FROM scratch

## All sensitive subcommands
## --tls-max is set to 1.0 or 1.1

# Noncompliant@+1 {{Change this code to enforce TLS 1.2 or above.}}
RUN curl --tls-max 1.0 https://tls-v1-0.badssl.com:1010
#   ^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN curl --tls-max 1.1   https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl --tls-max "1.1" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl --tls-max '1.1' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN ["curl", "--tls-max", "1.1", "https://tls-v1-1.badssl.com:1011"]
# Noncompliant@+1
RUN curl -out out.txt -k --tls-max 1.1 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
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
RUN curl --data 'name=bob' --tls-max $TLS_VERSION_ENV     https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max ${TLS_VERSION_ENV}   https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max "${TLS_VERSION_ENV}" https://tls-v1-0.badssl.com:1010 --output path/to/file

ARG TLS_VERSION_ARG=1.0
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max $TLS_VERSION_ARG     https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max ${TLS_VERSION_ARG}   https://tls-v1-0.badssl.com:1010 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' --tls-max "${TLS_VERSION_ARG}" https://tls-v1-0.badssl.com:1010 --output path/to/file

## One of the following flags is set: --sslv2, -2, --sslv3, -3, --tlsv1.0, --tlsv1, -1, --tlsv1.1

# Noncompliant@+1 {{Change this code to enforce TLS 1.2 or above.}}
RUN curl --sslv2 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^^^^^^
# Noncompliant@+1
RUN curl "--sslv2" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl '--sslv2' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN ["curl", "--sslv2", "https://tls-v1-1.badssl.com:1011"]
# Noncompliant@+1
RUN other command && curl --data 'name=bob' --sslv2 --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file && other command
# Noncompliant@+2
RUN other command && \
    curl --data 'name=bob' --sslv2 --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file &&  \
    other command
# Noncompliant@+1
RUN <<-EOF curl --data 'name=bob' --sslv2 --request PUT https://tls-v1-1.badssl.com:1011
  some value
EOF

ENV PROTOCOL_SSLV2_ENV=--sslv2
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_SSLV2_ENV     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_SSLV2_ENV}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_SSLV2_ENV}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

ARG PROTOCOL_SSLV2_ARG=--sslv2
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_SSLV2_ARG     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_SSLV2_ARG}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_SSLV2_ARG}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

# Noncompliant@+1
RUN curl -2 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^
# Noncompliant@+1
RUN curl "-2" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl '-2' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN ["curl", "-2", "https://tls-v1-1.badssl.com:1011"]
# Noncompliant@+1
RUN other command; curl -2 https://tls-v1-1.badssl.com:1011; other command
# Noncompliant@+2
RUN other command  \
    curl --data 'name=bob' -2 --request PUT https://tls-v1-1.badssl.com:1011 \
    other command
# Noncompliant@+2
RUN <<-EOF
  curl -2 https://tls-v1-1.badssl.com:1011
EOF

ENV PROTOCOL_2_ENV=-2
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_2_ENV     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_2_ENV}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_2_ENV}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

ARG PROTOCOL_2_ARG=-2
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_2_ARG     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_2_ARG}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_2_ARG}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

# Noncompliant@+1
RUN curl --sslv3 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^^^^^^
# Noncompliant@+1
RUN curl "--sslv3" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl '--sslv3' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN ["curl", "--sslv3", "https://tls-v1-1.badssl.com:1011"]
# Noncompliant@+1
RUN other command | curl --sslv3 https://tls-v1-1.badssl.com:1011 | other command
# Noncompliant@+2
RUN other command  \
    curl --data 'name=bob' --sslv3 --request PUT https://tls-v1-1.badssl.com:1011 \
    other command
# Noncompliant@+1
RUN <<-EOF curl --data 'name=bob' --sslv3 --request PUT https://tls-v1-1.badssl.com:1011
  some value
EOF

ENV PROTOCOL_SSLV3_ENV=--sslv3
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_SSLV3_ENV    --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_SSLV3_ENV}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_SSLV3_ENV}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

ARG PROTOCOL_SSLV3_ARG=--sslv3
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_SSLV3_ARG     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_SSLV3_ARG}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_SSLV3_ARG}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file


# Noncompliant@+1
RUN curl -3 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^
# Noncompliant@+1
RUN curl "-3" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl '-3' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN ["curl", "-3", "https://tls-v1-1.badssl.com:1011"]
# Noncompliant@+1
RUN other command && curl -3 https://tls-v1-1.badssl.com:1011 && other command
# Noncompliant@+2
RUN other command && \
    curl --data 'name=bob' -3 --request PUT https://tls-v1-1.badssl.com:1011 && \
    other command
# Noncompliant@+2
RUN <<-EOF
  curl --data 'name=bob' -3 --request PUT https://tls-v1-1.badssl.com:1011
EOF

ENV PROTOCOL_3_ENV=-3
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_3_ENV     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_3_ENV}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_3_ENV}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

ARG PROTOCOL_3_ARG=-3
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_3_ARG     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_3_ARG}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_3_ARG}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

# Noncompliant@+1
RUN curl --tlsv1.0 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^^^^^^^^
# Noncompliant@+1
RUN curl "--tlsv1.0" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl '--tlsv1.0' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN ["curl", "--tlsv1.0", "https://tls-v1-1.badssl.com:1011"]
# Noncompliant@+1
RUN other command; curl --tlsv1.0 https://tls-v1-1.badssl.com:1011; other command
# Noncompliant@+2
RUN other command && \
    curl --data 'name=bob' --tlsv1.0 --request PUT https://tls-v1-1.badssl.com:1011 && \
    other command
# Noncompliant@+1
RUN <<-EOF curl --tlsv1.0 https://tls-v1-1.badssl.com:1011
  some value
EOF

ENV PROTOCOL_TLSV10_END=--tlsv1.0
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_TLSV10_END     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_TLSV10_END}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_TLSV10_END}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

ARG PROTOCOL_TLSV10_ARG=--tlsv1.0
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_TLSV10_END     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_TLSV10_END}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_TLSV10_END}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

# Noncompliant@+1
RUN curl --tlsv1 https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl "--tlsv1" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl '--tlsv1' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN ["curl", "--tlsv1", "https://tls-v1-1.badssl.com:1011"]
#    ^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN other command | curl --tlsv1 https://tls-v1-1.badssl.com:1011 | other command
# Noncompliant@+2
RUN other command && \
    curl --data 'name=bob' --tlsv1 --request PUT https://tls-v1-1.badssl.com:1011 && \
    other command
# Noncompliant@+1
RUN <<-EOF curl --data 'name=bob' --tlsv1 --request PUT https://tls-v1-1.badssl.com:1011
  some value
EOF

ENV PROTOCOL_TLSV1_ENV=--tlsv1
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_TLSV1_ENV     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_TLSV1_ENV}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_TLSV1_ENV}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

ARG PROTOCOL_TLSV1_ARG=--tlsv1
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_TLSV1_ARG     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_TLSV1_ARG}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_TLSV1_ARG}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

# Noncompliant@+1
RUN curl -1 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^
# Noncompliant@+1
RUN curl "-1" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl '-1' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN ["curl", "-1", "https://tls-v1-1.badssl.com:1011"]
# Noncompliant@+1
RUN other command; curl -1 https://tls-v1-1.badssl.com:1011; other command
# Noncompliant@+2
RUN other command && \
    curl --data 'name=bob' -1 --request PUT https://tls-v1-1.badssl.com:1011 && \
    other command
# Noncompliant@+1
RUN <<-EOF curl --data 'name=bob' -1 https://tls-v1-1.badssl.com:1011
  some value
EOF

ENV PROTOCOL_1_END=-1
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_1_END     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_1_END}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_1_END}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

ARG PROTOCOL_1_ARG=-1
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_1_ARG     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_1_ARG}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_1_ARG}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

# Noncompliant@+1
RUN curl --tlsv1.1 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^^^^^^^^
# Noncompliant@+1
RUN curl "--tlsv1.1" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN curl '--tlsv1.1' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN ["curl", "--tlsv1.1", "https://tls-v1-1.badssl.com:1011"]
# Noncompliant@+1
RUN other command | curl --tlsv1.1 https://tls-v1-1.badssl.com:1011 | other command
# Noncompliant@+2
RUN other command && \
    curl --data 'name=bob' --tlsv1.1 --request PUT https://tls-v1-1.badssl.com:1011 && \
    other command
# Noncompliant@+1
RUN <<-EOF curl --data 'name=bob' --tlsv1.1 --request PUT https://tls-v1-1.badssl.com:1011
  some value
EOF

ENV PROTOCOL_TLSV11_ENV=--tlsv1.1
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_TLSV11_ENV     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_TLSV11_ENV}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_TLSV11_ENV}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file

ARG PROTOCOL_TLSV11_ARG=--tlsv1.1
# Noncompliant@+1
RUN curl --data 'name=bob' $PROTOCOL_TLSV11_ARG     --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' ${PROTOCOL_TLSV11_ARG}   --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file
# Noncompliant@+1
RUN curl --data 'name=bob' "${PROTOCOL_TLSV11_ARG}" --request PUT https://tls-v1-1.badssl.com:1011 --output path/to/file


## The current limitations of our parser
# Noncompliant@+1
RUN curl https://www.sonarsource.com && curl --data 'name=bob' --tls-max 1.1 https://tls-v1-0.badssl.com:1010
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN curl https://www.sonarsource.com && \
    other command && \
    curl --data 'name=bob' --tls-max 1.1 https://tls-v1-0.badssl.com:1010 \
    other command

# One string in EXEC FORM is not splited, so it's not detected
RUN ["curl --data 'name=bob' --tls-max 1.0 https://tls-v1-0.badssl.com:1010"]

# Flags in quotes or double quotes are not splited and detected
RUN curl --data 'name=bob' "--tls-max 1.0" https://tls-v1-0.badssl.com:1010
RUN curl --data 'name=bob' '--tls-max 1.1' https://tls-v1-0.badssl.com:1010

## All compliant subcommands
RUN curl https://www.sonarsource.com
RUN curl --tls-max 1.2 https://tls-v1-1.badssl.com:1011
RUN curl --tlsv1.2 https://tls-v1-1.badssl.com:1011
RUN curl --tlsv1.3 https://tls-v1-1.badssl.com:1011
RUN curl -out out.txt -k --tlsv1.2 --request PUT https://tls-v1-1.badssl.com:1011
RUN curl -out out.txt -k --tlsv1.3 --request PUT https://tls-v1-1.badssl.com:1011
RUN curl --data 'name=bob' --tls-max $UNKNOWN https://tls-v1-0.badssl.com:1010 --output path/to/file

ENV TLS_MAX_12_ENV=1.2
ARG TLS_MAX_12_ARG=1.2
ENV PROTOCOL_TLSV12_ENV=--tlsv1.2
ARG PROTOCOL_TLSV13_ARG=--tlsv1.3

RUN curl --data 'name=bob' --tls-max $TLS_MAX_12_ENV   https://tls-v1-0.badssl.com:1010 --output path/to/file
RUN curl --data 'name=bob' --tls-max ${TLS_MAX_12_ENV} https://tls-v1-0.badssl.com:1010 --output path/to/file
RUN curl --data 'name=bob' --tls-max $TLS_MAX_12_ARG   https://tls-v1-0.badssl.com:1010 --output path/to/file
RUN curl --data 'name=bob' --tls-max ${TLS_MAX_12_ARG} https://tls-v1-0.badssl.com:1010 --output path/to/file
RUN curl --data 'name=bob' $PROTOCOL_TLSV12_ENV        https://tls-v1-0.badssl.com:1010 --output path/to/file
RUN curl --data 'name=bob' ${PROTOCOL_TLSV12_ENV}      https://tls-v1-0.badssl.com:1010 --output path/to/file
RUN curl --data 'name=bob' $PROTOCOL_TLSV13_ARG        https://tls-v1-0.badssl.com:1010 --output path/to/file
RUN curl --data 'name=bob' ${PROTOCOL_TLSV13_ARG}      https://tls-v1-0.badssl.com:1010 --output path/to/file
