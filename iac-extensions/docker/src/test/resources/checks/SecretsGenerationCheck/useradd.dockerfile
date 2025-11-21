FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN useradd --password secret username

FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN ["useradd", "--password", "password", "username"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["useradd", "--password", "\"This", "should", "be", "kept", "secret\"", "username"]

# Noncompliant@+1
RUN ["useradd", "--password", "'This", "should", "be", "kept", "secret'", "username"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "useradd --password \"$PASSWORD\" username"]
RUN ["sh", "-c", "useradd --password $PASSWORD username"]
RUN ["sh", "-c", "sudo useradd --password $PASSWORD username"]
RUN ["sh", "-c", "useradd --password \"${PASSWORD}\" username"]
RUN ["sh", "-c", "useradd --password \"${PASSWORD:-test}\" username"]
RUN ["sh", "-c", "useradd --password \"${PASSWORD:+test}\" username"]
RUN ["sh", "-c", "useradd --password \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" username"]
RUN ["sh", "-c", "useradd --uid id --password \"${PASSWORD}\" username"]
RUN ["sh", "-c", "useradd --password \"${PASSWORD}\" --skel path/to/template_directory --create-home username --useruser username"]
RUN ["sh", "-c", "useradd --password \"${PASSWORD}\" username && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && useradd --password \"${PASSWORD}\" username"]


# Short flag version ==============

# Noncompliant@+1
RUN ["useradd", "-p", "MySuperPassword", "username"]

# Noncompliant@+1
RUN ["useradd", "-p", "\"This", "should", "be", "kept", "secret\"", "username"]

# Noncompliant@+1
RUN ["useradd", "-p", "'This", "should", "be", "kept", "secret'", "username"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "useradd -p \"$PASSWORD\" username"]
RUN ["sh", "-c", "useradd -p $PASSWORD username"]
RUN ["sh", "-c", "useradd -p $PASSWORD username"]
RUN ["sh", "-c", "useradd -p \"${PASSWORD}\" username"]
RUN ["sh", "-c", "sudo useradd -p \"${PASSWORD}\" username"]
RUN ["sh", "-c", "useradd -p \"${PASSWORD:-test}\" username"]
RUN ["sh", "-c", "useradd -p \"${PASSWORD:+test}\" username"]
RUN ["sh", "-c", "useradd -p \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" username"]
RUN ["sh", "-c", "useradd -h database_host -e \"source filename.sql\" --user=user -p \"${PASSWORD}\" username"]
RUN ["sh", "-c", "useradd -p \"${PASSWORD}\" -h database_host -e \"source filename.sql\" --user=user username"]
RUN ["sh", "-c", "useradd -p \"${PASSWORD}\" username && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && useradd -p \"${PASSWORD}\" username"]


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required useradd -p $(echo ${PASSWORD} | openssl passwd -6 -stdin) username"]

# Compliant
RUN ["sh", "-c", "--mount=type=secret useradd --user=user -p $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) username"]
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required useradd -p $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) username"]

# It's wrong usage of useradd because those flags require an argument
RUN ["useradd", "--password"]
RUN ["useradd", "-p"]
RUN ["useradd", "--password=myPassword"]
RUN ["useradd", "-p=myPassword"]

RUN ["useradd", "username"]
RUN ["sudo", "useradd", "username"]
RUN ["useradd", "--uid", "id", "username"]
RUN ["useradd", "--groups", "group1,group2", "username"]
