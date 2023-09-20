FROM ubuntu:22.04 as build

# no issue in non final stage
RUN usermod --password password username


FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN usermod --password password username
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN usermod --password "This should be kept secret" username

# Noncompliant@+1
RUN usermod --password 'This should be kept secret' username

# Noncompliant@+1
RUN usermod --password "$PASSWORD" username

# Noncompliant@+1
RUN usermod --password $PASSWORD username

# Noncompliant@+1
RUN usermod --password "${PASSWORD}" username

# Noncompliant@+1
RUN sudo usermod --password "${PASSWORD}" username

# Noncompliant@+1
RUN usermod --password "${PASSWORD:-test}" username

# Noncompliant@+1
RUN usermod --password "${PASSWORD:+test}" username

# Noncompliant@+1
RUN usermod --password "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" username

# Noncompliant@+1
RUN usermod --uid id --password "${PASSWORD}" username

# Noncompliant@+1
RUN usermod --password "${PASSWORD}" --skel path/to/template_directory --create-home username --useruser username


# Noncompliant@+1
RUN usermod --password "${PASSWORD}" username && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    usermod --password "${PASSWORD}" username


# Short flag version ==============

# Noncompliant@+1
RUN usermod -p MySuperPassword username

# Noncompliant@+1
RUN usermod -p "This should be kept secret" username

# Noncompliant@+1
RUN usermod -p 'This should be kept secret' username

# Noncompliant@+1
RUN usermod -p "$PASSWORD" username

# Noncompliant@+1
RUN usermod -p $PASSWORD username

# Noncompliant@+1
RUN usermod -p $PASSWORD username

# Noncompliant@+1
RUN sudo usermod -p $PASSWORD username

# Noncompliant@+1
RUN usermod -p "${PASSWORD}" username

# Noncompliant@+1
RUN usermod -p "${PASSWORD:-test}" username

# Noncompliant@+1
RUN usermod -p "${PASSWORD:+test}" username

# Noncompliant@+1
RUN usermod -p "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" username

# Noncompliant@+1
RUN usermod -h database_host -e "source filename.sql" --user=user -p "${PASSWORD}" username

# Noncompliant@+1
RUN usermod -p "${PASSWORD}" -h database_host -e "source filename.sql" --user=user username

# Noncompliant@+1
RUN usermod -p "${PASSWORD}" username && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    usermod -p "${PASSWORD}" username


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN --mount=type=secret,id=mysecret,required usermod -p $(echo ${PASSWORD} | openssl passwd -6 -stdin) username

# Compliant
RUN --mount=type=secret usermod --user=user -p $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) username
RUN --mount=type=secret,id=mysecret,required usermod -p $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) username

# It's wrong usage of usermod because those flags require an argument
RUN usermod --password
RUN usermod -p

RUN usermod username
RUN sudo usermod --login new_username username
RUN usermod --move-home --home path/to/new_home username
RUN usermod --append --groups group1,group2 username
