FROM ubuntu:22.04

# Noncompliant@+1 {{Make sure that installing unnecessary dependencies is safe here.}}
RUN apt install -y aptitude
#   ^^^^^^^^^^^^^^

# Noncompliant@+1
RUN apt -y install aptitude
#   ^^^^^^^^^^^^^^

# Noncompliant@+1
RUN apt update && apt install -y aptitude

# Noncompliant@+1
RUN apt-get install -y aptitude

# Noncompliant@+1
RUN aptitude install -y build-essential

# Noncompliant@+1
RUN apt install -y geary

# Non install command
RUN apt update

# Noncompliant@+1
RUN apt install -y

# Required flag is set
RUN apt install -y --no-install-recommends aptitude
RUN apt-get install -y --no-install-recommends aptitude
RUN aptitude install -y --without-recommends build-essential
RUN apt install -y --no-install-recommends geary

# Apt flag is set
RUN apt --random-flag install -y --no-install-recommends geary
# Noncompliant@+1
RUN apt --random-flag install geary

# Noncompliant@+2
RUN apt-get update && \
    apt-get install -y \
#   ^^^^^^^^^^^^^^^^^^
      bzr \
      cvs \
      git \
      mercurial \
      subversion \
    && rm -rf /var/lib/apt/lists/*

RUN foobar

RUN apt install -y $UNRESOLVED

# Noncompliant@+1
RUN apt install -y $UNRESOLVED && apt install -y geary
#                                 ^^^^^^^^^^^^^^

ARG RESOLVED=geary
# Noncompliant@+1
RUN apt install -y $RESOLVED
