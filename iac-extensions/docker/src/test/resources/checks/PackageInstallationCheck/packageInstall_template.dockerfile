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
RUN {$commandName} install -y {$safe-flag} aptitude
RUN {$commandName} install -y {$safe-flag} aptitude
RUN {$commandName} -y {$safe-flag} install aptitude


# Apt flag is set
RUN {$commandName} --random-flag install -y {$safe-flag} geary
RUN {$commandName} {$safe-flag} install -y --random-flag geary

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

RUN foobar

RUN {$commandName} install -y $UNRESOLVED

# Noncompliant@+1
RUN {$commandName} install -y $UNRESOLVED && {$commandName} install -y geary

ARG RESOLVED=geary
# Noncompliant@+1
RUN {$commandName} install -y $RESOLVED
