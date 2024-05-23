FROM ubuntu:22.04

# Noncompliant@+1 {{Not enforcing HTTPS here might allow for redirections to insecure websites. Make sure it is safe here.}}
RUN curl -L https://redirecttoinsecure.example.com
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN curl --location https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl -L --proto "=foo" https://redirecttoinsecure.example.com
# Noncompliant@+1
RUN curl --location --proto "=foo" https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl --proto "=foo" -L https://redirecttoinsecure.example.com
# Noncompliant@+1
RUN curl --proto "=foo" --location https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl -L --proto https://redirecttoinsecure.example.com
# Noncompliant@+1
RUN curl --location --proto https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl --proto -L https://redirecttoinsecure.example.com
# Noncompliant@+1
RUN curl --proto --location https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl -L --proto "=foobar" https://redirecttoinsecure.example.com
# Noncompliant@+1
RUN curl --location --proto "=foobar" https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl --proto "=foobar" -L https://redirecttoinsecure.example.com
# Noncompliant@+1
RUN curl --proto "=foobar" --location https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl -foo -L -foo --proto "=foobar" https://redirecttoinsecure.example.com
# Noncompliant@+1
RUN curl -foo --location -foo --proto "=foobar" https://redirecttoinsecure.example.com


# Noncompliant@+1
RUN curl --proto "=foobar" -foo -L -foo https://redirecttoinsecure.example.com
# Noncompliant@+1
RUN curl --proto "=foobar" -foo --location -foo https://redirecttoinsecure.example.com

# Noncompliant@+1
RUN curl --tlsv1.2 -sSf -L https://might-redirect.example.com/install.sh
# Noncompliant@+1
RUN curl --tlsv1.2 -sSf --location https://might-redirect.example.com/install.sh

# Noncompliant@+1
RUN curl --tlsv1.2 -sSfL https://might-redirect.example.com/install.sh
# Noncompliant@+1
RUN curl -sSfL https://might-redirect.example.com/install.sh
# Noncompliant@+1
RUN curl -sSfL --tlsv1.2 https://might-redirect.example.com/install.sh
# Noncompliant@+1
RUN curl -LsSf --tlsv1.2 https://might-redirect.example.com/install.sh
# Noncompliant@+1
RUN curl -sLSf --tlsv1.2 https://might-redirect.example.com/install.sh

RUN curl -L --proto "=foobar" http://redirecttoinsecure.example.com
RUN curl --location --proto "=foobar" http://redirecttoinsecure.example.com

RUN curl --proto -L "=foobar" http://redirecttoinsecure.example.com
RUN curl --proto --location "=foobar" http://redirecttoinsecure.example.com

RUN curl --proto "=https" -L https://redirecttoinsecure.example.com
RUN curl --proto "=https" --location https://redirecttoinsecure.example.com

RUN curl --proto '=https' -L https://redirecttoinsecure.example.com
RUN curl --proto '=https' --location https://redirecttoinsecure.example.com

RUN curl -L http://redirecttoinsecure.example.com
RUN curl --location http://redirecttoinsecure.example.com

RUN curl --proto "=https" -L https://redirecttoinsecure.example.com | true
RUN curl --proto "=https" --location https://redirecttoinsecure.example.com | true

RUN curl https://redirecttoinsecure.example.com

RUN foobar

