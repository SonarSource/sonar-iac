FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN usermod --password secret username

FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN ["usermod", "--password", "password", "username"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["usermod", "--password", "\"This", "should", "be", "kept", "secret\"", "username"]

# Noncompliant@+1
RUN ["usermod", "--password", "'This", "should", "be", "kept", "secret'", "username"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "usermod --password \"$PASSWORD\" username"]
RUN ["sh", "-c", "usermod --password $PASSWORD username"]
RUN ["sh", "-c", "usermod --password \"${PASSWORD}\" username"]
RUN ["sh", "-c", "sudo usermod --password \"${PASSWORD}\" username"]
RUN ["sh", "-c", "usermod --password \"${PASSWORD:-test}\" username"]
RUN ["sh", "-c", "usermod --password \"${PASSWORD:+test}\" username"]
RUN ["sh", "-c", "usermod --password \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" username"]
RUN ["sh", "-c", "usermod --uid id --password \"${PASSWORD}\" username"]
RUN ["sh", "-c", "usermod --password \"${PASSWORD}\" --skel path/to/template_directory --create-home username --useruser username"]
RUN ["sh", "-c", "usermod --password \"${PASSWORD}\" username && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && usermod --password \"${PASSWORD}\" username"]


# Short flag version ==============

# Noncompliant@+1
RUN ["usermod", "-p", "MySuperPassword", "username"]

# Noncompliant@+1
RUN ["usermod", "-p", "\"This", "should", "be", "kept", "secret\"", "username"]

# Noncompliant@+1
RUN ["usermod", "-p", "'This", "should", "be", "kept", "secret'", "username"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "usermod -p \"$PASSWORD\" username"]
RUN ["sh", "-c", "usermod -p $PASSWORD username"]
RUN ["sh", "-c", "usermod -p $PASSWORD username"]
RUN ["sh", "-c", "sudo usermod -p $PASSWORD username"]
RUN ["sh", "-c", "usermod -p \"${PASSWORD}\" username"]
RUN ["sh", "-c", "usermod -p \"${PASSWORD:-test}\" username"]
RUN ["sh", "-c", "usermod -p \"${PASSWORD:+test}\" username"]
RUN ["sh", "-c", "usermod -p \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" username"]
RUN ["sh", "-c", "usermod -h database_host -e \"source filename.sql\" --user=user -p \"${PASSWORD}\" username"]
RUN ["sh", "-c", "usermod -p \"${PASSWORD}\" -h database_host -e \"source filename.sql\" --user=user username"]
RUN ["sh", "-c", "usermod -p \"${PASSWORD}\" username && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && usermod -p \"${PASSWORD}\" username"]


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required usermod -p $(echo ${PASSWORD} | openssl passwd -6 -stdin) username"]

# Compliant
RUN ["sh", "-c", "--mount=type=secret usermod --user=user -p $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) username"]
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required usermod -p $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) username"]

# It's wrong usage of usermod because those flags require an argument
RUN ["usermod", "--password"]
RUN ["usermod", "-p"]

RUN ["usermod", "username"]
RUN ["sudo", "usermod", "--login", "new_username", "username"]
RUN ["usermod", "--move-home", "--home", "path/to/new_home", "username"]
RUN ["usermod", "--append", "--groups", "group1,group2", "username"]
