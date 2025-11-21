FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN net user username password

FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN ["net", "user", "username", "password"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["net", "user", "username", "\"This", "should", "be", "kept", "secret\""]

# Noncompliant@+1
RUN ["net", "user", "username", "\"This", "should", "be", "kept", "secret\"", "/add"]

# Noncompliant@+1
RUN ["net", "user", "username", "\"This", "should", "be", "kept", "secret\"", "/delete"]

# Noncompliant@+1
RUN ["net", "user", "username", "'This", "should", "be", "kept", "secret'"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "net user username \"$PASSWORD\""]
RUN ["sh", "-c", "net user username $PASSWORD /add"]
RUN ["sh", "-c", "net user username \"${PASSWORD}\""]
RUN ["sh", "-c", "sudo net user username \"${PASSWORD}\""]
RUN ["sh", "-c", "net user username \"${PASSWORD:-test}\""]
RUN ["sh", "-c", "net user username \"${PASSWORD:+test}\""]
RUN ["sh", "-c", "net user username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\""]
RUN ["sh", "-c", "net user username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" /homedir:/home/username /active:yes"]
RUN ["sh", "-c", "sudo net user username \"${PASSWORD}\" && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && sudo net user username \"${PASSWORD}\""]


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required net user username $(echo ${PASSWORD} | openssl passwd -6 -stdin)"]

# Compliant
RUN ["sh", "-c", "--mount=type=secret net user username $(cat C:\\ProgramData\\Docker\\secrets\\secret | openssl passwd -6 -stdin)"]
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required net user username $(cat C:\\ProgramData\\Docker\\secrets\\secret | openssl passwd -6 -stdin)"]

RUN ["net", "user", "username"]
RUN ["net", "user", "username", "*"]
RUN ["net", "user", "/add"]
RUN ["sudo", "net", "user", "username", "/delete"]
RUN ["net", "user", "username", "/active:yes", "/comment:\"Foo", "Bar\""]
