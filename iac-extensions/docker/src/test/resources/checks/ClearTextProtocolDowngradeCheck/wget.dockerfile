FROM ubuntu:22.04

# Noncompliant@+1 {{Not disabling redirects might allow for redirections to insecure websites. Make sure it is safe here.}}
RUN wget https://might-redirect.example.com/install.sh -q -O - | sh
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN wget https://might-redirect.example.com/install.sh --max-redirect=1 -q -O - | sh

# Noncompliant@+1
RUN wget https://might-redirect.example.com/install.sh -q --max-redirect=1 -O - | sh


RUN wget https://might-redirect.example.com/install.sh --max-redirect=0 -q -O - | sh

RUN wget https://might-redirect.example.com/install.sh -q --max-redirect=0 -O - | sh


RUN foobar

