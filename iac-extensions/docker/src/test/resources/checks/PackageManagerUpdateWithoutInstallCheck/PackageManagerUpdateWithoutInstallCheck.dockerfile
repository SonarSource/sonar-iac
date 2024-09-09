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
# Noncompliant@+1
RUN apt-get install -y libcurl && apt-get update

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
RUN apt-get update && apt install libcurl
# Compliant, but mix of different package managers shouldn't be expected to occur in real files
RUN aptitude update && apk add libcurl
RUN apt-get update && echo APT index updated && apt install -y libcurl
RUN apt-get update; echo APT index updated; aptitude install -y libcurl
RUN apt-get update || echo Failed to update APT index; apt-get install -y libcurl || echo Failed to install
RUN <<EOF
apt-get update
apt-get install -y git
EOF
# Compliant example with one unresolved argument
RUN apt-get update \
  && apt-get install -y ${MONGO_PACKAGE}=$MONGO_VERSION
# This is a FN, but with curent implementation we can't disinguish strings from commands
RUN apt-get update && echo Ready for apt-get install

# Compliant: `install` command is part of an argument
ARG APT_INSTALL="apt-get install --yes"
RUN apt-get update && $APT_INSTALL gnupg
# Compliant; for coverage
RUN apt-get update && apt-get install $UNRESOLVED
# Compliant: a case when `Argument.expressions` will contain mutliple expressions
ARG APT_INSTALL_SPACE="apt-get install --yes "
RUN apt-get update && ${APT_INSTALL_SPACE}gnupg
# Noncompliant@+2{{Update cache and install packages in single RUN instruction.}}
ARG APT_UPDATE="apt-get update"
RUN $APT_UPDATE && echo "Skipping installation in this noncompliant example"
#   ^^^^^^^^^^^
# Noncompliant@+2
ARG COMMAND="apt-get"
RUN $COMMAND update && echo "Skipping installation in this noncompliant example"
#   ^^^^^^^^^^^^^^^
