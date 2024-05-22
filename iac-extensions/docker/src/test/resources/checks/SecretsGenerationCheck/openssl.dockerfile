FROM ubuntu:22.04 as build

# no issue in non final stage
RUN openssl genrsa -des3 -passout pass:x -out server.pass.key 2048


FROM ubuntu:22.04

## openssl gen genrsa/gendsa/genpkey
# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
RUN openssl genrsa -des3 -passout pass:x -out server.pass.key 2048
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN openssl gendsa -des3 -passout pass:x -out server.pass.key 2048
# Noncompliant@+1
RUN openssl genpkey -des3 -passout pass:x -out server.pass.key 2048
# Noncompliant@+1
RUN openssl genrsa


## openssl req
# Noncompliant@+1
RUN openssl req -random1 -passout other
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN openssl req -random1 -passin
# Noncompliant@+1
RUN openssl req -random1 -new
# Noncompliant@+1
RUN openssl req -random1 -newkey
# Noncompliant@+1
RUN openssl req -random1 -key
# Noncompliant@+1
RUN openssl req -random1 -CAkey

RUN openssl req -random1


## openssl rsa
# Noncompliant@+1
RUN openssl rsa -random1
#   ^^^^^^^^^^^^^^^^^^^^
RUN openssl rsa -random1 -pubin
RUN openssl rsa -random1 -RSAPublicKey_in


## openssl ec/pkey
# Noncompliant@+1
RUN openssl ec -random1
#   ^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN openssl pkey -random1
#   ^^^^^^^^^^^^^^^^^^^^^
RUN openssl ec -random1 -pubin
RUN openssl pkey -random1 -pubin


## openssl ecparams/dsaparam
# Noncompliant@+1
RUN openssl ecparams -random1 -genkey myKey
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN openssl dsaparam -random1 -genkey myKey
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
RUN openssl ecparam -random1
RUN openssl dsaparam -random1


## openssl x509
# Noncompliant@+1
RUN openssl x509 -random1 -key myKey
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN openssl x509 -random1 -signkey myKey
# Noncompliant@+1
RUN openssl x509 -random1 -CAkey myKey

RUN openssl x509 -random1


## other

# Noncompliant@+1
RUN openssl genrsa -des3 -passout pass:x -out server.pass.key 2048 && other command
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN openssl genrsa -des3 -passout pass:x -out server.pass.key 2048 | other command
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN openssl genrsa -des3 -passout pass:x -out server.pass.key 2048; other command
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN openssl genrsa -des3 && other command && other
#   ^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN --mount=type=secret,id=mysecret,required openssl genrsa -random1

# Compliant
RUN openssl foobar -random1

RUN foobar

RUN openssl other_command with_param genrsa

