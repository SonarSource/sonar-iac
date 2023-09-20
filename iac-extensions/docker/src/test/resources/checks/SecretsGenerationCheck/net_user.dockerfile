FROM ubuntu:22.04 as build

# no issue in non final stage
RUN net user username password


FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN net user username password
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN net user username "This should be kept secret"

# Noncompliant@+1
RUN net user username "This should be kept secret" /add

# Noncompliant@+1
RUN net user username "This should be kept secret" /delete

# Noncompliant@+1
RUN net user username 'This should be kept secret'

# Noncompliant@+1
RUN net user username "$PASSWORD"

# Noncompliant@+1
RUN net user username $PASSWORD /add

# Noncompliant@+1
RUN net user username "${PASSWORD}"

# Noncompliant@+1
RUN sudo net user username "${PASSWORD}"

# Noncompliant@+1
RUN net user username "${PASSWORD:-test}"

# Noncompliant@+1
RUN net user username "${PASSWORD:+test}"

# Noncompliant@+1
RUN net user username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)"

# Noncompliant@+1
RUN net user username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" /homedir:/home/username /active:yes

# Noncompliant@+1
RUN sudo net user username "${PASSWORD}" && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    sudo net user username "${PASSWORD}"


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN --mount=type=secret,id=mysecret,required net user username $(echo ${PASSWORD} | openssl passwd -6 -stdin)

# Compliant
RUN --mount=type=secret net user username $(cat C:\ProgramData\Docker\secrets\secret | openssl passwd -6 -stdin)
RUN --mount=type=secret,id=mysecret,required net user username $(cat C:\ProgramData\Docker\secrets\secret | openssl passwd -6 -stdin)

RUN net user username
RUN net user username *
RUN net user /add
RUN sudo net user username /delete
RUN net user username /active:yes /comment:"Foo Bar"
