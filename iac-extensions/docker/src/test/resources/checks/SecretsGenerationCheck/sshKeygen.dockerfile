FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN ssh-keygen -N "" -t dsa -b 1024 -f rsync-key

FROM ubuntu:22.04

# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
RUN ["ssh-keygen", "-N", "\"\"", "-t", "dsa", "-b", "1024", "-f", "rsync-key"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["ssh-keygen", "-randomFlag", "Q"]

# Noncompliant@+1
RUN ["ssh-keygen", "-randomFlag", "randomOption"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-l", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-l", "randomOption", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-randomFlag", "randomValue", "-l", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-randomFlag", "randomValue", "-l", "randomOption", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-l", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-l", "randomOption", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-l"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-l", "randomOption"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-F", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-F", "randomOption", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-H", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-H", "randomOption", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-R", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-R", "randomOption", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-r", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-r", "randomOption", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-k", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-k", "randomOption", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-Q", "-randomFlag", "randomValue"]

RUN ["ssh-keygen", "-randomFlag", "randomValue", "-Q", "randomOption", "-randomFlag", "randomValue"]

# Noncompliant@+1
RUN ["ssh-keygen", "-random1", "-N", "\"\"", "-random2", "-t", "dsa", "-random3", "-b", "1024", "-random4", "-f", "rsync-key", "-random5"]

# Noncompliant@+1
RUN ["ssh-keygen", "-random1", "random1Value", "-N", "\"\"", "-random2", "-t", "dsa", "-random3", "=random3Value", "-b", "1024", "-random4", "-f", "rsync-key", "-random5"]

# Noncompliant@+1
RUN ["ssh-keygen", "-random1", "random1Value", "-N", "\"\"", "-random2", "-t", "dsa", "-random3", "=random3Value", "-b", "1024", "-random4", "-f", "rsync-key", "-random5", "random5Option"]


# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-t", "dsa", "-f", "rsync-key", "-b", "1024"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-f", "rsync-key", "-t", "dsa", "-b", "1024"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-f", "rsync-key", "-b", "1024", "-t", "dsa"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-b", "1024", "-t", "dsa", "-f", "rsync-key"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-b", "1024", "-f", "rsync-key", "-t", "dsa"]



# Noncompliant@+1
RUN ["ssh-keygen", "-t", "dsa", "-b", "1024", "-f", "rsync-key", "-N", "\"\""]

# Noncompliant@+1
RUN ["ssh-keygen", "-t", "dsa", "-b", "1024", "-N", "\"\"", "-f", "rsync-key"]

# Noncompliant@+1
RUN ["ssh-keygen", "-t", "dsa", "-f", "rsync-key", "-N", "\"\"", "-b", "1024"]

# Noncompliant@+1
RUN ["ssh-keygen", "-t", "dsa", "-f", "rsync-key", "-b", "1024", "-N", "\"\""]

# Noncompliant@+1
RUN ["ssh-keygen", "-t", "dsa", "-N", "\"\"", "-f", "rsync-key", "-b", "1024"]

# Noncompliant@+1
RUN ["ssh-keygen", "-t", "dsa", "-N", "\"\"", "-b", "1024", "-f", "rsync-key"]




# Noncompliant@+1
RUN ["ssh-keygen", "-b", "1024", "-N", "\"\"", "-t", "dsa", "-f", "rsync-key"]

# Noncompliant@+1
RUN ["ssh-keygen", "-b", "1024", "-N", "\"\"", "-f", "rsync-key", "-t", "dsa"]

# Noncompliant@+1
RUN ["ssh-keygen", "-b", "1024", "-t", "dsa", "-N", "\"\"", "-f", "rsync-key"]

# Noncompliant@+1
RUN ["ssh-keygen", "-b", "1024", "-t", "dsa", "-f", "rsync-key", "-N", "\"\""]

# Noncompliant@+1
RUN ["ssh-keygen", "-b", "1024", "-f", "rsync-key", "-t", "dsa", "-N", "\"\""]

# Noncompliant@+1
RUN ["ssh-keygen", "-b", "1024", "-f", "rsync-key", "-N", "\"\"", "-t", "dsa"]



# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-t", "dsa", "-b", "1024", "-f", "rsync-key"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-t", "dsa", "-f", "rsync-key", "-b", "1024"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-f", "rsync-key", "-t", "dsa", "-b", "1024"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-f", "rsync-key", "-b", "1024", "-t", "dsa"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-b", "1024", "-t", "dsa", "-f", "rsync-key"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"", "-b", "1024", "-f", "rsync-key", "-t", "dsa"]


RUN ["sh", "-c", "ssh-keygen -N \"\" -t dsa $UNRESOLVED"]

RUN ["sh", "-c", "ssh-keygen -N \"\" -t dsa -f $UNRESOLVED"]


# Noncompliant@+1
RUN ["ssh-keygen", "-N", "''", "-t", "dsa", "-b", "1024", "-f", "rsync-key"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "value", "-t", "dsa", "-b", "1024", "-f", "rsync-key"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"value\"", "-t", "dsa", "-b", "1024", "-f", "rsync-key"]

# Noncompliant@+1
RUN ["ssh-keygen", "-N", "\"\"value", "-t", "dsa", "-b", "1024", "-f", "rsync-key"]

ARG EMPTY=""
ARG NOT_EMPTY="value"

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "ssh-keygen -N $EMPTY     -t dsa -b 1024 -f rsync-key"]
RUN ["sh", "-c", "ssh-keygen -N $NOT_EMPTY -t dsa -b 1024 -f rsync-key"]
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required ssh-keygen -N $NOT_EMPTY -t dsa -b 1024 -f rsync-key"]

RUN <<EOF
  ssh-keygen -N "" -b 1024 -f rsync-key -t dsa
  cd ..
EOF


RUN ["sh", "-c", "ssh-keygen -N $UNRESOLVED   -t dsa -b 1024 -f rsync-key"]

RUN ["sh", "-c", "ssh-keygen -N \"\" -t dsa -f rsync-key -b 1024 $UNRESOLVED"]

RUN ["foobar"]
