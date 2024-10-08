FROM ubuntu:22.04

# For testing issue location
# Noncompliant@+1
RUN apt install -y aptitude
#   ^^^^^^^^^^^^^^

# Noncompliant@+1
RUN {$commandName} install -y

# Noncompliant@+1 {{Make sure automatically installing recommended packages is safe here.}}
RUN {$commandName} install -y aptitude

# Noncompliant@+1
RUN {$commandName} -y install aptitude

# Noncompliant@+1
RUN {$commandName} update && {$commandName} install -y aptitude

# Non install command
RUN {$commandName} update


# Required flag is set
RUN {$commandName} install -y {$safeFlag} aptitude
RUN {$commandName} install -y {$safeFlag} aptitude
RUN {$commandName} -y {$safeFlag} install aptitude

RUN {$commandName} update; \
    {$commandName} -y {$safeFlag} install aptitude


# Apt flag is set
RUN {$commandName} --random-flag install -y {$safeFlag} geary
RUN {$commandName} {$safeFlag} install -y --random-flag geary

# Noncompliant@+1
RUN {$commandName} --random-flag install geary

# Noncompliant@+2
RUN {$commandName} update && \
    {$commandName} install -y \
      bzr \
      cvs \
      git \
      mercurial \
      subversion \
    && rm -rf /var/lib/apt/lists/*

# Noncompliant@+3
RUN <<-EOF
    {$commandName} update && \
    {$commandName} install -y \
      bzr
EOF

RUN foobar

RUN {$commandName} install -y $UNRESOLVED

# Noncompliant@+1
RUN {$commandName} install -y $UNRESOLVED && {$commandName} install -y geary

ARG RESOLVED=geary
# Noncompliant@+1
RUN {$commandName} install -y $RESOLVED
