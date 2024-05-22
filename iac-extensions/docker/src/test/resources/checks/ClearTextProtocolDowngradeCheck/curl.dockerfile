FROM ubuntu:22.04

# Noncompliant@+1 {{Not enforcing HTTPS here might allow for redirections to insecure websites. Make sure it is safe here.}}
RUN curl -L https://redirecttoinsecure.example.com
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN curl --location https://redirecttoinsecure.example.com


# Noncompliant@+1
RUN curl -L --proto "=foo" https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl --proto "=foo" -L https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl -L --proto https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl --proto -L https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl -L --proto "=foobar" https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl --proto "=foobar" -L https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl -foo -L -foo --proto "=foobar" https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl --proto "=foobar" -foo -L -foo https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl --tlsv1.2 -sSf -L https://might-redirect.example.com/install.sh

RUN curl -L --proto "=foobar" http://redirecttoinsecure.example.com

RUN curl --proto -L "=foobar" http://redirecttoinsecure.example.com

RUN curl --proto "=https" -L https://redirecttoinsecure.example.com

RUN curl --proto '=https' -L https://redirecttoinsecure.example.com

RUN curl --location http://redirecttoinsecure.example.com

RUN curl -L http://redirecttoinsecure.example.com

RUN curl --proto "=https" -L https://redirecttoinsecure.example.com | true

RUN curl https://redirecttoinsecure.example.com

RUN foobar

