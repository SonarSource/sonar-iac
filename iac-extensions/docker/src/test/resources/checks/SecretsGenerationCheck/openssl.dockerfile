FROM ubuntu:22.04

# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
RUN openssl genrsa -des3 -passout pass:x -out server.pass.key 2048
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN openssl genrsa -des3 -passout pass:x -out server.pass.key 2048 && other command
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN openssl genrsa -des3 -passout pass:x -out server.pass.key 2048 | other command
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN openssl genrsa -des3 -passout pass:x -out server.pass.key 2048; other command
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN openssl genrsa -random1

# Noncompliant@+1
RUN openssl req -random1

# Noncompliant@+1
RUN openssl rsa -random1

# Noncompliant@+1
RUN openssl gendsa -random1

# Noncompliant@+1
RUN openssl ec -random1

# Noncompliant@+1
RUN openssl ecparam -random1

# Noncompliant@+1
RUN openssl x509 -random1

# Noncompliant@+1
RUN openssl genpkey -random1

# Noncompliant@+1
RUN openssl pkey -random1

# Noncompliant@+1
RUN openssl genrsa -des3 && other command && other
#   ^^^^^^^^^^^^^^^^^^^^

RUN openssl foobar -random1

RUN foobar

