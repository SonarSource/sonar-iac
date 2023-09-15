FROM scratch

# Noncompliant@+1 {{Update cache and install packages in single RUN instruction.}}
RUN apk update
#   ^^^^^^^^^^
# Noncompliant@+1
RUN apt update
# Noncompliant@+1
RUN apt-get update
# Noncompliant@+1
RUN aptitude update
# Noncompliant@+1
RUN apt-get update && echo APT index updated
# Noncompliant@+1
RUN echo Preparing to apt-get install && apt-get update

# Noncompliant@+2
RUN <<EOF
apt-get update
EOF
# Noncompliant@+3
RUN <<EOF
echo Preparing to apt-get install
apt-get update
EOF
# Noncompliant@+2
RUN <<EOF
apt-get update
echo APT index updated
EOF

# Complaiant
RUN apk update && apk add libcurl
RUN apt update && apt install libcurl
RUN apt-get update && apt-get install libcurl
RUN aptitude update && aptitude install libcurl
RUN aptitude update && apt-get install libcurl
RUN <<EOF
apt-get update
apt-get install -y git
EOF
# This is a FN, but with curent implementation we can't disinguish strings from commands
RUN apt-get update && echo Ready for apt-get install
