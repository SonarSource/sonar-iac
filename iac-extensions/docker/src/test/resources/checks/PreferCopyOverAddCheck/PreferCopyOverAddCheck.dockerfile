FROM ubuntu:20.04

ARG LOCAL_FILE=source.java
ARG REMOTE_FILE=https://example.com/app
ARG LOCAL_ARCHIVE=app.tar.gz

# Noncompliant@+1 {{Replace this ADD instruction with a COPY instruction.}}
ADD ./app /app
# ^[sc=1;ec=14]

# Noncompliant@+1
ADD ./app.exe /app

# Noncompliant@+1
ADD source.java /app

# Noncompliant@+1
ADD /apps/app /app

# Noncompliant@+1
ADD $LOCAL_FILE /app

# Compliant; mix of local and remote resources
ADD source.java https://example.com/app /app/

# Compliant; mix of regular files and archives
ADD app.tar.gz source.java /app/

# Compliant; web resource
ADD https://example.com/app /app

# Compliant; multiple web resources
ADD https://example.com/app https://example.com/app2 /app/

# Compliant; git resource
ADD git@git.example.com:foo/bar.git /bar

# Compliant; local archive
ADD ./app.tar.gz /app

# Compliant; multiple local archives
ADD ./app.tar.gz ./app2.tar.gz /app/

# Compliant
ADD $REMOTE_FILE /app

# Compliant
ADD $LOCAL_ARCHIVE /app

# Compliant
ADD $UNRESOLVED_VARIABLE /app
