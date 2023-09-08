FROM scratch

# Noncompliant@+1 {{Replace deprecated instructions with an up-to-date equivalent.}}
  MAINTAINER bob
# ^^^^^^^^^^^^^^

# Noncompliant@+1
MAINTAINER bob foobar

# Noncompliant@+1
MAINTAINER bob foobar <bob@foooobaaar.com>

# Compliant
RUN echo hello
ENV debug=0
ADD . .
COPY . .
