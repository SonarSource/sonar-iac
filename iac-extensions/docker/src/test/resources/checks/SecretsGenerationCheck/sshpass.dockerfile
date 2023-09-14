FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN sshpass -p password ssh user@hostname
#   ^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN sshpass -p "password" ssh user@hostname

# Noncompliant@+1
RUN sshpass -p 'password' ssh user@hostname

# Noncompliant@+1
RUN sshpass -p $PASSWORD ssh user@hostname

# Noncompliant@+1
RUN sshpass -p ${PASSWORD} ssh user@hostname

# Noncompliant@+1
RUN sshpass -p "$PASSWORD" ssh user@hostname

# Noncompliant@+1
RUN sshpass -p ${PASSWORD-test} ssh user@hostname

# Noncompliant@+1
RUN sshpass -p ${PASSWORD+test} ssh user@hostname

# Noncompliant@+1
RUN sshpass -p "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" ssh user@hostname

# Noncompliant@+1
RUN sshpass -p "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" ssh user@hostname && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    sshpass -p "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" ssh user@hostname


# No space after -p =================

# Noncompliant@+1
RUN sshpass -ppassword ssh user@hostname
#   ^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN sshpass -p"password" ssh user@hostname

# Noncompliant@+1
RUN sshpass -p'password' ssh user@hostname

# Noncompliant@+1
RUN sshpass -p$PASSWORD ssh user@hostname

# Noncompliant@+1
RUN sshpass -p"$PASSWORD" ssh user@hostname

# Noncompliant@+1
RUN sshpass -p"${PASSWORD}" ssh user@hostname

# Noncompliant@+1
RUN sshpass -p${PASSWORD-test} ssh user@hostname

# Noncompliant@+1
RUN sshpass -p${PASSWORD+test} ssh user@hostname

# Noncompliant@+1
RUN sshpass -p"$(echo ${PASSWORD} | openssl passwd -6 -stdin)" ssh user@hostname

# Noncompliant@+1
RUN sshpass -p"$(echo ${PASSWORD} | openssl passwd -6 -stdin)" ssh user@hostname && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    sshpass -p"$(echo ${PASSWORD} | openssl passwd -6 -stdin)" ssh user@hostname

# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN --mount=type=secret,id=mysecret,required sshpass -p $(echo ${PASSWORD} | openssl passwd -6 -stdin) ssh user@hostname


# Compliant
RUN --mount=type=secret,id=mysecret,required sshpass -p $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) ssh user@hostname
RUN sshpass -d 0 ssh user@hostname
RUN sshpass -d0 ssh user@hostname
RUN sshpass -fpath/to/file ssh user@hostname
RUN sshpass -f path/to/file ssh user@hostname
RUN sshpass -e ssh user@hostname
