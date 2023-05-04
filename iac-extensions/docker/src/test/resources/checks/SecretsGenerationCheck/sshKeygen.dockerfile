FROM ubuntu:22.04

# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
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

RUN ssh-keygen -N "" -t dsa $UNRESOLVED

RUN ssh-keygen -N "" -t dsa -f $UNRESOLVED

RUN ssh-keygen -N "" -t dsa -f opt1
RUN ssh-keygen -N "" -t dsa -f
RUN ssh-keygen -N "" -t dsa

RUN ssh-keygen -N "" -t dsa -b 1024 -f

# Noncompliant@+1
RUN ssh-keygen -N ''      -t dsa -b 1024 -f rsync-key

RUN ssh-keygen -N value   -t dsa -b 1024 -f rsync-key
RUN ssh-keygen -N "value" -t dsa -b 1024 -f rsync-key
RUN ssh-keygen -N ""value -t dsa -b 1024 -f rsync-key

ARG EMPTY=""
ARG NOT_EMPTY="value"

# Noncompliant@+1
RUN ssh-keygen -N $EMPTY     -t dsa -b 1024 -f rsync-key

RUN ssh-keygen -N $NOT_EMPTY -t dsa -b 1024 -f rsync-key
RUN ssh-keygen -N $UNKNOWN   -t dsa -b 1024 -f rsync-key


# Noncompliant@+1
RUN ssh-keygen -N "" -t dsa -f rsync-key -b 1024 $UNRESOLVED

RUN ssh-keygen -N ""

RUN ssh-keygen -N "" -t dsa -f rsync-key $UNRESOLVED -b 1024 -random1

RUN ssh-keygen -N "" -t dsa -b 1024 -b val -f rsync-key

RUN ssh-keygen -N "" -t dsa -b 1000 -f rsync-key

RUN ssh-keygen -N "" -t dsa -N val -b 1024 -f rsync-key

RUN foobar
