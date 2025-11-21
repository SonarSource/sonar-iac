FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN htpasswd -b path/to/file username password

FROM ubuntu:22.04

ARG PASSWORD

# Only flag -b and 3 arguments

# Noncompliant@+1
RUN ["htpasswd", "-b", "path/to/file", "username", "password"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["htpasswd", "-b", "path/to/file", "username", "\"password\""]

# Noncompliant@+1
RUN ["htpasswd", "-b", "path/to/file", "username", "'password'"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "htpasswd -b path/to/file username $PASSWORD"]
RUN ["sh", "-c", "htpasswd -b path/to/file username \"$PASSWORD\""]
RUN ["sh", "-c", "htpasswd -b path/to/file username ${PASSWORD}"]
RUN ["sh", "-c", "htpasswd -b path/to/file username ${PASSWORD-test}"]
RUN ["sh", "-c", "htpasswd -b path/to/file username ${PASSWORD+test}"]
RUN ["sh", "-c", "htpasswd -b path/to/file username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\""]
RUN ["sh", "-c", "htpasswd -b path/to/file username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && htpasswd -b path/to/file username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\""]
RUN ["sh", "-c", "htpasswd -bc path/to/file username ${PASSWORD}"]
RUN ["sh", "-c", "htpasswd -b -c path/to/file username ${PASSWORD}"]
RUN ["sh", "-c", "htpasswd -c -b -s path/to/file username ${PASSWORD}"]
RUN ["sh", "-c", "htpasswd -cbs path/to/file username ${PASSWORD}"]

RUN ["sh", "-c", "htpasswd -p \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" ssh user@hostname"]


# Flags -n -b and 2 arguments =================

# Noncompliant@+1
RUN ["htpasswd", "-nb", "username", "password"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["htpasswd", "-nb", "username", "\"password\""]

# Noncompliant@+1
RUN ["htpasswd", "-nb", "username", "'password'"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "htpasswd -nb username $PASSWORD"]
RUN ["sh", "-c", "htpasswd -nb username \"$PASSWORD\""]
RUN ["sh", "-c", "htpasswd -nb username ${PASSWORD}"]
RUN ["sh", "-c", "htpasswd -nb username ${PASSWORD-test}"]
RUN ["sh", "-c", "htpasswd -nb username ${PASSWORD+test}"]
RUN ["sh", "-c", "htpasswd -nb username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\""]
RUN ["sh", "-c", "htpasswd -nb username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && htpasswd -nb username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\""]
RUN ["sh", "-c", "htpasswd -bn username \"$PASSWORD\""]
RUN ["sh", "-c", "htpasswd -nbm username \"$PASSWORD\""]
RUN ["sh", "-c", "htpasswd -n -b -m username \"$PASSWORD\""]
RUN ["sh", "-c", "htpasswd -n -b -m username \"$PASSWORD\""]

# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required htpasswd -b path/to/file username $(echo ${PASSWORD} | openssl passwd -6 -stdin)"]


# Compliant
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required htpasswd -b path/to/file username $(cat /run/secrets/mysecret | openssl passwd -6 -stdin)"]
# no password, valid examples
RUN ["htpasswd", "-n", "path/to/file", "username"]
RUN ["htpasswd", "-n", "-b", "-m", "username"]
RUN ["htpasswd", "-n", "-b", "-m", "-D", "username"]
RUN ["htpasswd", "-nmD", "path/to/file", "username"]
RUN ["htpasswd", "-c", "path/to/file", "username"]

# no password, invalid examples
RUN ["htpasswd", "-b", "path/to/file", "username"]
RUN ["htpasswd", "-nb", "username"]
RUN ["htpasswd", "-bn", "username"]
