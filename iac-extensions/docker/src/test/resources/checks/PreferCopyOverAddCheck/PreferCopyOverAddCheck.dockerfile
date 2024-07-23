FROM ubuntu:20.04

# Noncompliant@+1 {{Replace this ADD instruction with a COPY instruction.}}
ADD ./app /app
#   ^^^^^

# Noncompliant@+1
ADD ./app.exe /app
#   ^^^^^^^^^

# Noncompliant@+1
ADD source.java /app
#   ^^^^^^^^^^^

# Noncompliant@+1
ADD /apps/app /app

# Noncompliant@+1
ADD source.java https://example.com/app /app/
#   ^^^^^^^^^^^

# Noncompliant@+1
ADD app.tar.gz source.java /app/
#              ^^^^^^^^^^^

# Compliant; web resource
ADD https://example.com/app /app

# Compliant; multiple web resources
ADD https://example.com/app https://example.com/app2 /app/

# Compliant; local archive
ADD ./app.tar.gz /app

# Compliant; multiple local archives
ADD ./app.tar.gz ./app2.tar.gz /app/
