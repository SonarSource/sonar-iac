FROM scratch

## All sensitive subcommands
## SSLv2
# Noncompliant@+1 {{Change this code to enforce TLS 1.2 or above.}}
RUN wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN wget --secure-protocol "SSLv2" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --secure-protocol 'SSLv2' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --tries=10 --secure-protocol SSLv2 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN wget --secure-protocol SSLv2 --tries=10 https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol SSLv2 --request PUT https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user=username --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN other command wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN other command; wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010; other command
# Noncompliant@+1
RUN other command && wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010 && other command
# Noncompliant@+1
RUN other command | wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010 | other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010 && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010 \
      && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol=SSLv2   --request PUT https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol="SSLv2" --request PUT https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol='SSLv2' --request PUT https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "--secure-protocol=SSLv2" --request PUT https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" '--secure-protocol=SSLv2' --request PUT https://tls-v1-0.badssl.com:1010


# Noncompliant@+1
RUN other command | wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN ["wget", "--secure-protocol", "SSLv2", "https://tls-v1-0.badssl.com:1010"]
#       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
   RUN ["wget", "--secure-protocol=SSLv2", "https://tls-v1-0.badssl.com:1010"]
#       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN <<-EOF wget --secure-protocol SSLv2 https://tls-v1-1.badssl.com:1011
  some value
EOF
# Noncompliant@+1
RUN <<-EOF wget --secure-protocol=SSLv2 https://tls-v1-1.badssl.com:1011
  some value
EOF
# Noncompliant@+2
RUN <<-EOF
  wget --secure-protocol SSLv2 https://tls-v1-1.badssl.com:1011
EOF
# Noncompliant@+2
RUN <<-EOF
  wget --secure-protocol=SSLv2 https://tls-v1-1.badssl.com:1011
EOF

ENV SSLV2_VERSION_ENV=SSLv2
ENV FLAG_AND_SSLV2_VERSION_ENV="--secure-protocol=${SSLV2_VERSION_ENV}"
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol $SSLV2_VERSION_ENV     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol ${SSLV2_VERSION_ENV}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol "${SSLV2_VERSION_ENV}" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" $FLAG_AND_SSLV2_VERSION_ENV     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" ${FLAG_AND_SSLV2_VERSION_ENV}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "${FLAG_AND_SSLV2_VERSION_ENV}" https://tls-v1-0.badssl.com:1010

ARG SSLV2_VERSION_ARG=SSLv2
ARG FLAG_AND_SSLV2_VERSION_ARG="--secure-protocol=${SSLV2_VERSION_ARG}"
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol $SSLV2_VERSION_ARG     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol ${SSLV2_VERSION_ARG}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol "${SSLV2_VERSION_ARG}" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" $FLAG_AND_SSLV2_VERSION_ARG     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" ${FLAG_AND_SSLV2_VERSION_ARG}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "${FLAG_AND_SSLV2_VERSION_ARG}" https://tls-v1-0.badssl.com:1010

## SSLv3
# Noncompliant@+1 {{Change this code to enforce TLS 1.2 or above.}}
RUN wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN wget --secure-protocol "SSLv3" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --secure-protocol "SSLv3" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --tries=10 --secure-protocol SSLv3 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN wget --secure-protocol SSLv3 --tries=10 https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol SSLv3 --request PUT https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user=username --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN other command wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN other command; wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010; other command
# Noncompliant@+1
RUN other command && wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010 && other command
# Noncompliant@+1
RUN other command | wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010 | other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010 && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010 \
      && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol=SSLv3 https://tls-v1-0.badssl.com:1010 \
      && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol=SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol="SSLv3" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol='SSLv3' https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "--secure-protocol=SSLv3" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" '--secure-protocol=SSLv3' https://tls-v1-0.badssl.com:1010

# Noncompliant@+1
RUN other command | wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN ["wget", "--secure-protocol", "SSLv3", "https://tls-v1-0.badssl.com:1010"]
#       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN other command | wget --secure-protocol SSLv3 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN ["wget", "--secure-protocol=SSLv3", "https://tls-v1-0.badssl.com:1010"]
#       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN <<-EOF wget --secure-protocol SSLv3 https://tls-v1-1.badssl.com:1011
  some value
EOF
# Noncompliant@+1
RUN <<-EOF wget --secure-protocol=SSLv3 https://tls-v1-1.badssl.com:1011
  some value
EOF
# Noncompliant@+2
RUN <<-EOF
  wget --secure-protocol SSLv3 https://tls-v1-1.badssl.com:1011
EOF
# Noncompliant@+2
RUN <<-EOF
  wget --secure-protocol=SSLv3 https://tls-v1-1.badssl.com:1011
EOF

ENV SSLV3_VERSION_ENV=SSLv3
ENV FLAG_SSLV3_VERSION_ENV="--secure-protocol=${SSLV3_VERSION_ENV}"
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol $SSLV3_VERSION_ENV     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol ${SSLV3_VERSION_ENV}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol "${SSLV3_VERSION_ENV}" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" $FLAG_SSLV3_VERSION_ENV     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" ${FLAG_SSLV3_VERSION_ENV}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "${FLAG_SSLV3_VERSION_ENV}" https://tls-v1-0.badssl.com:1010

ARG SSLV3_VERSION_ARG=SSLv3
ARG FLAG_AND_SSLV3_VERSION_ARG="--secure-protocol=SSLv3"
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol $SSLV3_VERSION_ARG     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol ${SSLV3_VERSION_ARG}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol "${SSLV3_VERSION_ARG}" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" $FLAG_AND_SSLV3_VERSION_ARG     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" ${FLAG_AND_SSLV3_VERSION_ARG}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "${FLAG_AND_SSLV3_VERSION_ARG}" https://tls-v1-0.badssl.com:1010

## TLSv1
# Noncompliant@+1 {{Change this code to enforce TLS 1.2 or above.}}
RUN wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN wget --secure-protocol "TLSv1" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --secure-protocol 'TLSv1' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --tries=10 --secure-protocol TLSv1 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN wget --secure-protocol TLSv1 --tries=10 https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol TLSv1 --request PUT https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user=username --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN other command wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN other command; wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010; other command
# Noncompliant@+1
RUN other command && wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010 && other command
# Noncompliant@+1
RUN other command | wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010 | other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010 && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010 \
      && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol=TLSv1 https://tls-v1-0.badssl.com:1010 \
      && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol="TLSv1" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol='TLSv1' https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "--secure-protocol=TLSv1" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" '--secure-protocol=TLSv1' https://tls-v1-0.badssl.com:1010

# Noncompliant@+1
RUN other command | wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN ["wget", "--secure-protocol", "TLSv1", "https://tls-v1-0.badssl.com:1010"]
#       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN other command | wget --secure-protocol TLSv1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN ["wget", "--secure-protocol=TLSv1", "https://tls-v1-0.badssl.com:1010"]
#       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN <<-EOF wget --secure-protocol TLSv1 https://tls-v1-1.badssl.com:1011
  some value
EOF
# Noncompliant@+2
RUN <<-EOF
  wget --secure-protocol TLSv1 https://tls-v1-1.badssl.com:1011
EOF

ENV TLSV1_VERSION_ENV=TLSv1
ENV FLAG_AND_TLSV1_VERSION_ENV="--secure-protocol=TLSv1"
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol $TLSV1_VERSION_ENV     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol ${TLSV1_VERSION_ENV}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol "${TLSV1_VERSION_ENV}" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" $FLAG_AND_TLSV1_VERSION_ENV     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" ${FLAG_AND_TLSV1_VERSION_ENV}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "${FLAG_AND_TLSV1_VERSION_ENV}" https://tls-v1-0.badssl.com:1010

ARG TLSV1_VERSION_ARG=TLSv1
ARG FLAG_AND_TLSV1_VERSION_ARG="--secure-protocol=${TLSV1_VERSION_ARG}"
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol $TLSV1_VERSION_ARG     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol ${TLSV1_VERSION_ARG}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol "${TLSV1_VERSION_ARG}" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" $FLAG_AND_TLSV1_VERSION_ARG     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" ${FLAG_AND_TLSV1_VERSION_ARG}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "${FLAG_AND_TLSV1_VERSION_ARG}" https://tls-v1-0.badssl.com:1010

## TLSv1_1
# Noncompliant@+1 {{Change this code to enforce TLS 1.2 or above.}}
RUN wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN wget --secure-protocol "TLSv1_1" https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --secure-protocol 'TLSv1_1' https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --tries=10 --secure-protocol TLSv1_1 https://tls-v1-1.badssl.com:1011
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN wget --secure-protocol TLSv1_1 --tries=10 https://tls-v1-1.badssl.com:1011
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol TLSv1_1 --request PUT https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user=username --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN other command wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN other command; wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010; other command
# Noncompliant@+1
RUN other command && wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010 && other command
# Noncompliant@+1
RUN other command | wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010 | other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010 && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010 \
      && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol=TLSv1_1 https://tls-v1-0.badssl.com:1010 \
      && other command
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol="TLSv1_1" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol='TLSv1_1' https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "--secure-protocol=TLSv1_1" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" '--secure-protocol=TLSv1_1' https://tls-v1-0.badssl.com:1010

# Noncompliant@+1
RUN other command | wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN ["wget", "--secure-protocol", "TLSv1_1", "https://tls-v1-0.badssl.com:1010"]
#       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN other command | wget --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
   RUN ["wget", "--secure-protocol=TLSv1_1", "https://tls-v1-0.badssl.com:1010"]
#       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN <<-EOF wget --secure-protocol TLSv1_1 https://tls-v1-1.badssl.com:1011
  some value
EOF
# Noncompliant@+1
RUN <<-EOF wget --secure-protocol=TLSv1_1 https://tls-v1-1.badssl.com:1011
  some value
EOF
# Noncompliant@+2
RUN <<-EOF
  wget --secure-protocol TLSv1_1 https://tls-v1-1.badssl.com:1011
EOF
# Noncompliant@+2
RUN <<-EOF
  wget --secure-protocol=TLSv1_1 https://tls-v1-1.badssl.com:1011
EOF

ENV TLSV11_VERSION_ENV=TLSv1_1
ENV FALG_AND_TLSV11_VERSION_ENV="--secure-protocol=${TLSV11_VERSION_ENV}"
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol $TLSV11_VERSION_ENV     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol ${TLSV11_VERSION_ENV}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol "${TLSV11_VERSION_ENV}" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" $FALG_AND_TLSV11_VERSION_ENV     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" ${FALG_AND_TLSV11_VERSION_ENV}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "${FALG_AND_TLSV11_VERSION_ENV}" https://tls-v1-0.badssl.com:1010

ARG TLSV11_VERSION_ARG=TLSv1_1
ARG FLAG_AND_TLSV11_VERSION_ARG="--secure-protocol=TLSv1_1"
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol $TLSV11_VERSION_ARG     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol ${TLSV11_VERSION_ARG}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" --secure-protocol "${TLSV11_VERSION_ARG}" https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" $FLAG_AND_TLSV11_VERSION_ARG     https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" ${FLAG_AND_TLSV11_VERSION_ARG}   https://tls-v1-0.badssl.com:1010
# Noncompliant@+1
RUN wget --user-agent "Mozilla" "${FLAG_AND_TLSV11_VERSION_ARG}" https://tls-v1-0.badssl.com:1010


## The current limitations of our parser
# The commands are not separated by `&&` or `|` or `;`, so the highlighting may be wrong
# Noncompliant@+1
RUN wget https://www.sonarsource.com && wget --user-agent "Mozilla" --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010
#                                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+3
RUN wget https://www.sonarsource.com && \
    other command && \
    wget --user-agent "Mozilla" --secure-protocol TLSv1_1 https://tls-v1-0.badssl.com:1010 \
    other command

# One string in EXEC FORM is not splited, so it's not detected
RUN ["wget --secure-protocol SSLv2 https://tls-v1-0.badssl.com:1010"]
RUN ["wget --secure-protocol=SSLv2 https://tls-v1-0.badssl.com:1010"]

# Flags in quotes or double quotes are not splited and detected
RUN wget "--secure-protocol SSLv2" https://tls-v1-1.badssl.com:1011
RUN wget '--secure-protocol SSLv2' https://tls-v1-1.badssl.com:1011


## All compliant subcommands
RUN wget https://www.sonarsource.com
RUN wget --secure-protocol TLSv1_2 https://tls-v1-1.badssl.com:1011
RUN wget --secure-protocol TLSv1_3 https://tls-v1-1.badssl.com:1011
RUN wget --secure-protocol auto    https://tls-v1-1.badssl.com:1011
RUN wget --secure-protocol PFS     https://tls-v1-1.badssl.com:1011
RUN wget --user-agent "Mozilla" --secure-protocol TLSv1_2  -b https://tls-v1-1.badssl.com:1011
RUN wget --user-agent "Mozilla" --secure-protocol TLSv1_3  -b https://tls-v1-1.badssl.com:1011
RUN wget --user-agent "Mozilla" --secure-protocol auto     -b https://tls-v1-1.badssl.com:1011
RUN wget --user-agent "Mozilla" --secure-protocol PFS      -b https://tls-v1-1.badssl.com:1011
RUN wget --user-agent "Mozilla" --secure-protocol $UNKNOWN    https://tls-v1-0.badssl.com:1010

ENV TLS_12_ENV=TLSv1_2
ARG TLS_12_ARG=TLSv1_2
ENV AUTO_ENV=auto
ARG AUTO_ARG=auto
ENV PFS_ENV=PFS
ARG PFS_ARG=PFS

RUN wget --user-agent "Mozilla" --secure-protocol $TLS_12_ENV   https://tls-v1-0.badssl.com:1010
RUN wget --user-agent "Mozilla" --secure-protocol ${TLS_12_ENV} https://tls-v1-0.badssl.com:1010
RUN wget --user-agent "Mozilla" --secure-protocol $AUTO_ENV     https://tls-v1-0.badssl.com:1010
RUN wget --user-agent "Mozilla" --secure-protocol ${AUTO_ENV}   https://tls-v1-0.badssl.com:1010
RUN wget --user-agent "Mozilla" --secure-protocol $PFS_ENV      https://tls-v1-0.badssl.com:1010
RUN wget --user-agent "Mozilla" --secure-protocol ${PFS_ENV}    https://tls-v1-0.badssl.com:1010
