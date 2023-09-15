FROM ubuntu:22.04 as build

# no issue in non final stage
RUN htpasswd -b path/to/file username password


FROM ubuntu:22.04

ARG PASSWORD

# Only flag -b and 3 arguments

# Noncompliant@+1
RUN htpasswd -b path/to/file username password
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN htpasswd -b path/to/file username "password"

# Noncompliant@+1
RUN htpasswd -b path/to/file username 'password'

# Noncompliant@+1
RUN htpasswd -b path/to/file username $PASSWORD

# Noncompliant@+1
RUN htpasswd -b path/to/file username "$PASSWORD"

# Noncompliant@+1
RUN htpasswd -b path/to/file username ${PASSWORD}

# Noncompliant@+1
RUN htpasswd -b path/to/file username ${PASSWORD-test}

# Noncompliant@+1
RUN htpasswd -b path/to/file username ${PASSWORD+test}

# Noncompliant@+1
RUN htpasswd -b path/to/file username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)"

RUN htpasswd -p "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" ssh user@hostname

# Noncompliant@+1
RUN htpasswd -b path/to/file username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    htpasswd -b path/to/file username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)"

# Another variants of -b flag, but NOT -n, 3 arguments ======

# Noncompliant@+1
RUN htpasswd -bc path/to/file username ${PASSWORD}

# Noncompliant@+1
RUN htpasswd -b -c path/to/file username ${PASSWORD}

# Noncompliant@+1
RUN htpasswd -c -b -s path/to/file username ${PASSWORD}

# Noncompliant@+1
RUN htpasswd -cbs path/to/file username ${PASSWORD}


# Flags -n -b and 2 arguments =================

# Noncompliant@+1
RUN htpasswd -nb username password
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN htpasswd -nb username "password"

# Noncompliant@+1
RUN htpasswd -nb username 'password'

# Noncompliant@+1
RUN htpasswd -nb username $PASSWORD

# Noncompliant@+1
RUN htpasswd -nb username "$PASSWORD"

# Noncompliant@+1
RUN htpasswd -nb username ${PASSWORD}

# Noncompliant@+1
RUN htpasswd -nb username ${PASSWORD-test}

# Noncompliant@+1
RUN htpasswd -nb username ${PASSWORD+test}

# Noncompliant@+1
RUN htpasswd -nb username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)"

# Noncompliant@+1
RUN htpasswd -nb username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    htpasswd -nb username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)"

# Another variants of -nb flag ===========

# Noncompliant@+1
RUN htpasswd -bn username "$PASSWORD"

# Noncompliant@+1
RUN htpasswd -nbm username "$PASSWORD"

# Noncompliant@+1
RUN htpasswd -n -b -m username "$PASSWORD"

# Noncompliant@+1
RUN htpasswd -n -b -m username "$PASSWORD"

# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN --mount=type=secret,id=mysecret,required htpasswd -b path/to/file username $(echo ${PASSWORD} | openssl passwd -6 -stdin)


# Compliant
RUN --mount=type=secret,id=mysecret,required htpasswd -b path/to/file username $(cat /run/secrets/mysecret | openssl passwd -6 -stdin)
# no password, valid examples
RUN htpasswd -n path/to/file username
RUN htpasswd -n -b -m username
RUN htpasswd -n -b -m -D username
RUN htpasswd -nmD path/to/file username
RUN htpasswd -c path/to/file username

# no password, invalid examples
RUN htpasswd -b path/to/file username
RUN htpasswd -nb username
RUN htpasswd -bn username
