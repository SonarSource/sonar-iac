FROM scratch

# Deletion of the wrong cache

# Noncompliant@+1{{Remove cache after installing packages.}}
RUN apk add nginx && apt clean
#   ^^^^^^^^^^^^^

# Noncompliant@+1{{Remove cache after installing packages.}}
RUN apt install nginx && apk cache clean
#   ^^^^^^^^^^^^^^^^^

# Noncompliant@+1{{Remove cache after installing packages.}}
RUN apk add nginx && apt-get install wget && rm -rf /var/lib/apt/lists/*
#   ^^^^^^^^^^^^^

# Noncompliant@+1
RUN apt-get install wget && rm -rf /var/cache/apk/*
#   ^^^^^^^^^^^^^^^^^^^^

# All flagged
# Noncompliant@+1 3
RUN apk add nginx && apt-get install wget && apk add wget

# Noncompliant@+6
# Noncompliant@+4
# Noncompliant@+2
RUN <<EOF
apk add nginx
apt-get install wget
apt-get install wget
EOF

# Noncompliant@+1
RUN apt-get install wget && aptitude install wget && rm -rf /var/lib/apt/lists/* && aptitude install wget

# Compliant
RUN apt install nginx && apt-get clean
RUN apt install nginx && aptitude clean
RUN apt-get install nginx && apt clean
RUN apt-get install nginx && aptitude clean
RUN aptitude install nginx && apt clean
RUN aptitude install nginx && apt-get clean

# Compliant - direct package installation
RUN apk add nginx.deb
RUN apt-get install wget.deb
RUN apk add wget.deb
RUN apk add nginx.apk
RUN apt-get install wget.apk
RUN apk add wget.apk
RUN apk add ./nginx.other
RUN apt-get install ./wget.other
RUN apk add ./wget.other

# Noncompliant@+1
RUN apk add nginx.other
# Noncompliant@+1
RUN apt-get install wget.other
# Noncompliant@+1
RUN apk add wget.other

# Compliant - no package name / filepath provided
RUN apk add

RUN apk add nginx && apt-get install wget && rm -rf /var/cache/apk/* && apt-get clean
RUN apt-get install wget && apt-get install wget && aptitude install wget && rm -rf /var/lib/apt/lists/*

RUN <<EOF
apt-get install wget
rm -rf /var/lib/apt/lists/*
EOF

