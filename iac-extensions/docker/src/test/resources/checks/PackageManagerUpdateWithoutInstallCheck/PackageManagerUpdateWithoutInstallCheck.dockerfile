FROM scratch

# FN: shell form not supported in community edition
RUN apk update


# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "apt-get update && echo APT index updated"]
RUN ["sh", "-c", "echo Preparing to apt-get install && apt-get update"]
RUN ["sh", "-c", "apt-get install -y libcurl && apt-get update"]
RUN ["sh", "-c", "apt-get update"]
RUN ["sh", "-c", "echo Preparing to apt-get install\napt-get update"]
RUN ["sh", "-c", "apt-get update\necho APT index updated"]

# Noncompliant@+1
RUN ["apt", "update"]
#    ^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN ["apt-get", "update"]
# Noncompliant@+1
RUN ["aptitude", "update"]

# Compliant
RUN ["sh", "-c", "apk update && apk add libcurl"]
RUN ["sh", "-c", "apt update && apt install libcurl"]
RUN ["sh", "-c", "apt-get update && apt-get install libcurl"]
RUN ["sh", "-c", "aptitude update && aptitude install libcurl"]
RUN ["sh", "-c", "aptitude update && apt-get install libcurl"]
RUN ["sh", "-c", "apt-get update && apt install libcurl"]
RUN ["sh", "-c", "apt-get update && gdebi -n /tmp/package.deb"]
# Compliant, but mix of different package managers shouldn't be expected to occur in real files
RUN ["sh", "-c", "aptitude update && apk add libcurl"]
RUN ["sh", "-c", "apt-get update && echo APT index updated && apt install -y libcurl"]
RUN ["sh", "-c", "apt-get update; echo APT index updated; aptitude install -y libcurl"]
RUN ["sh", "-c", "apt-get update || echo Failed to update APT index; apt-get install -y libcurl || echo Failed to install"]
RUN ["sh", "-c", "apt-get update\napt-get install -y git"]
# Compliant example with one unresolved argument
RUN ["sh", "-c", "apt-get update && apt-get install -y ${MONGO_PACKAGE}=$MONGO_VERSION"]
# This is a FN, but with curent implementation we can't disinguish strings from commands
RUN ["sh", "-c", "apt-get update && echo Ready for apt-get install"]

# Compliant: `install` command is part of an argument
ARG APT_INSTALL="apt-get install --yes"
RUN ["sh", "-c", "apt-get update && $APT_INSTALL gnupg"]
# Compliant; for coverage
RUN ["sh", "-c", "apt-get update && apt-get install $UNRESOLVED"]
# Compliant: a case when `Argument.expressions` will contain mutliple expressions
ARG APT_INSTALL_SPACE="apt-get install --yes "
RUN ["sh", "-c", "apt-get update && ${APT_INSTALL_SPACE}gnupg"]
ARG APT_UPDATE="apt-get update"
RUN ["sh", "-c", "$APT_UPDATE && echo \"Skipping installation in this noncompliant example\""]
ARG COMMAND="apt-get"
RUN ["sh", "-c", "$COMMAND update && echo \"Skipping installation in this noncompliant example\""]
