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

# Noncompliant@+1
RUN ssh-keygen -N "" -t dsa -f rsync-key -b 1024

# Noncompliant@+1
RUN ssh-keygen -N "" -f rsync-key -t dsa -b 1024

# Noncompliant@+1
RUN ssh-keygen -N "" -f rsync-key -b 1024 -t dsa

# Noncompliant@+1
RUN ssh-keygen -N "" -b 1024 -t dsa -f rsync-key

# Noncompliant@+1
RUN ssh-keygen -N "" -b 1024 -f rsync-key -t dsa



# Noncompliant@+1
RUN ssh-keygen -t dsa -b 1024 -f rsync-key -N ""

# Noncompliant@+1
RUN ssh-keygen -t dsa -b 1024 -N "" -f rsync-key

# Noncompliant@+1
RUN ssh-keygen -t dsa -f rsync-key -N "" -b 1024

# Noncompliant@+1
RUN ssh-keygen -t dsa -f rsync-key -b 1024 -N ""

# Noncompliant@+1
RUN ssh-keygen -t dsa -N "" -f rsync-key -b 1024

# Noncompliant@+1
RUN ssh-keygen -t dsa -N "" -b 1024 -f rsync-key




# Noncompliant@+1
RUN ssh-keygen -b 1024 -N "" -t dsa -f rsync-key

# Noncompliant@+1
RUN ssh-keygen -b 1024 -N "" -f rsync-key -t dsa

# Noncompliant@+1
RUN ssh-keygen -b 1024 -t dsa -N "" -f rsync-key

# Noncompliant@+1
RUN ssh-keygen -b 1024 -t dsa -f rsync-key -N ""

# Noncompliant@+1
RUN ssh-keygen -b 1024 -f rsync-key -t dsa -N ""

# Noncompliant@+1
RUN ssh-keygen -b 1024 -f rsync-key -N "" -t dsa



# Noncompliant@+1
RUN ssh-keygen -N "" -t dsa -b 1024 -f rsync-key

# Noncompliant@+1
RUN ssh-keygen -N "" -t dsa -f rsync-key -b 1024

# Noncompliant@+1
RUN ssh-keygen -N "" -f rsync-key -t dsa -b 1024

# Noncompliant@+1
RUN ssh-keygen -N "" -f rsync-key -b 1024 -t dsa

# Noncompliant@+1
RUN ssh-keygen -N "" -b 1024 -t dsa -f rsync-key

# Noncompliant@+1
RUN ssh-keygen -N "" -b 1024 -f rsync-key -t dsa

RUN ssh-keygen -N -t dsa -b 1024 -f rsync-key

RUN ssh-keygen -N "" -t =opt1 -b 1024 -f rsync-key

RUN ssh-keygen -N "" -t dsa -b 1000 -f rsync-key

RUN ssh-keygen -N "" -b 1000 -f rsync-key


RUN foobar

