FROM ubuntu:22.04

# Noncompliant@+1 {{Revoke and change this secret, as it might be compromised.}}
RUN ssh-keygen -N "" -t dsa -b 1024 -f rsync-key
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ssh-keygen -random1 -N "" -random2 -t dsa -random3 -b 1024 -random4 -f rsync-key -random5

# Noncompliant@+1
RUN ssh-keygen -random1 random1Value -N "" -random2 -t dsa -random3 =random3Value -b 1024 -random4 -f rsync-key -random5

# Noncompliant@+1
RUN ssh-keygen -random1 random1Value -N "" -random2 -t dsa -random3 =random3Value -b 1024 -random4 -f rsync-key -random5 random5Option


RUN foobar

