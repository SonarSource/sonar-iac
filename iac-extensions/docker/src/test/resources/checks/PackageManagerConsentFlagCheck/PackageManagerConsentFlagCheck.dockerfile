FROM scratch

# Noncompliant@+1 {{Add consent flag so that this command doesn't require user confirmation.}}
RUN apt-get install libcurl
#   ^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN apt install libcurl
# Noncompliant@+1
RUN apt-get install --allow-downgrades libcurl
# Noncompliant@+1
RUN aptitude install libcurl
# Noncompliant@+1
RUN apt-get install -d libcurl
# Noncompliant@+1
RUN apt-get install -dmf libcurl
# Noncompliant@+1
RUN apt-get upgrade
# Noncompliant@+1
RUN apt-get dist-upgrade
# Noncompliant@+1
RUN gdebi /tmp/package.deb
# Noncompliant@+1
RUN gdebi -q
# Noncompliant@+1
RUN gdebi -o APT_OPTS /tmp/package.deb
# Noncompliant@+1
RUN gdebi /tmp/package.deb && other_command -n

RUN apt-get install -y libcurl
RUN apt-get update
RUN apt-get clean
RUN apt clean
RUN aptitude clean
RUN apt-get -y update
RUN apt install -y libcurl
RUN aptitude install -y libcurl
RUN apt-get install -tym libcurl
RUN apt-get install --yes libcurl
RUN gdebi -n /tmp/package.deb
RUN gdebi --non-interactive /tmp/package.deb
RUN gdebi /tmp/package.deb -n
RUN gdebi -o APT_OPTS /tmp/package.deb -n

# FN SONARIAC-1115 CommandDetector, detect command when some flag is missing
RUN apt-get install libcurl -y
RUN apt install libcurl -y
RUN aptitude install wget -y
