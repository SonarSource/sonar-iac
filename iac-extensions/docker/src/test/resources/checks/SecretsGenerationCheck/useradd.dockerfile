FROM ubuntu:22.04 as build

# no issue in non final stage
RUN useradd --password password username


FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN useradd --password password username
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN useradd --password "This should be kept secret" username

# Noncompliant@+1
RUN useradd --password 'This should be kept secret' username

# Noncompliant@+1
RUN useradd --password "$PASSWORD" username

# Noncompliant@+1
RUN useradd --password $PASSWORD username

# Noncompliant@+1
RUN sudo useradd --password $PASSWORD username

# Noncompliant@+1
RUN useradd --password "${PASSWORD}" username

# Noncompliant@+1
RUN useradd --password "${PASSWORD:-test}" username

# Noncompliant@+1
RUN useradd --password "${PASSWORD:+test}" username

# Noncompliant@+1
RUN useradd --password "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" username

# Noncompliant@+1
RUN useradd --uid id --password "${PASSWORD}" username

# Noncompliant@+1
RUN useradd --password "${PASSWORD}" --skel path/to/template_directory --create-home username --useruser username


# Noncompliant@+1
RUN useradd --password "${PASSWORD}" username && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    useradd --password "${PASSWORD}" username


# Short flag version ==============

# Noncompliant@+1
RUN useradd -p MySuperPassword username

# Noncompliant@+1
RUN useradd -p "This should be kept secret" username

# Noncompliant@+1
RUN useradd -p 'This should be kept secret' username

# Noncompliant@+1
RUN useradd -p "$PASSWORD" username

# Noncompliant@+1
RUN useradd -p $PASSWORD username

# Noncompliant@+1
RUN useradd -p $PASSWORD username

# Noncompliant@+1
RUN useradd -p "${PASSWORD}" username

# Noncompliant@+1
RUN sudo useradd -p "${PASSWORD}" username

# Noncompliant@+1
RUN useradd -p "${PASSWORD:-test}" username

# Noncompliant@+1
RUN useradd -p "${PASSWORD:+test}" username

# Noncompliant@+1
RUN useradd -p "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" username

# Noncompliant@+1
RUN useradd -h database_host -e "source filename.sql" --user=user -p "${PASSWORD}" username

# Noncompliant@+1
RUN useradd -p "${PASSWORD}" -h database_host -e "source filename.sql" --user=user username

# Noncompliant@+1
RUN useradd -p "${PASSWORD}" username && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    useradd -p "${PASSWORD}" username


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN --mount=type=secret,id=mysecret,required useradd -p $(echo ${PASSWORD} | openssl passwd -6 -stdin) username

# Compliant
RUN --mount=type=secret useradd --user=user -p $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) username
RUN --mount=type=secret,id=mysecret,required useradd -p $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) username

# It's wrong usage of useradd because those flags require an argument
RUN useradd --password
RUN useradd -p

RUN useradd username
RUN sudo useradd username
RUN useradd --uid id username
RUN useradd --groups group1,group2 username
