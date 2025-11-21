FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN curl --user user:password https://example.com

FROM ubuntu:22.04

ARG PASSWORD
ARG USER
ENV USER_AND_PASSWORD=me:pass

# Noncompliant@+1
RUN ["curl", "--user", "me:password", "https://example.com"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["curl", "--user", "me:password:may:contains:colon", "https://example.com"]

# Noncompliant@+1
RUN ["curl", "--user", "\"me:password\"", "https://example.com"]

# Noncompliant@+1
RUN ["curl", "--user", "'me:password'", "https://example.com"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "curl --user \"me:$PASSWORD\" https://example.com"]
RUN ["sh", "-c", "curl --user \"me:${PASSWORD}\" https://example.com"]
RUN ["sh", "-c", "curl --user \"${USER}:${PASSWORD}\" https://example.com"]
RUN ["sh", "-c", "curl --user \"${USER_AND_PASSWORD}\" https://example.com"]
RUN ["sh", "-c", "curl --user \"${USER}:${PASSWORD-test}\" https://example.com"]
RUN ["sh", "-c", "curl --user \"${USER}:${PASSWORD+test}\" https://example.com"]
RUN ["sh", "-c", "curl --user \"me:$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" https://example.com"]
RUN ["sh", "-c", "curl --user me:$(echo ${PASSWORD} | openssl passwd -6 -stdin) https://example.com"]

# Noncompliant@+1
RUN ["curl", "--data", "'{\"name\":\"bob\"}'", "--fail", "--user", "me:password", "https://example.com"]

# curl allows use --user multiple times
# Noncompliant@+1
RUN ["curl", "--data", "'{\"name\":\"bob\"}'", "--user", "me:password1", "--fail", "--user", "me:password2", "https://example.com"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "curl --user \"me:password\" https://example.com && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && curl --user \"me:password\" https://example.com"]
RUN ["sh", "-c", "curl -u \"me:$PASSWORD\" https://example.com"]
RUN ["sh", "-c", "curl -u \"me:${PASSWORD}\" https://example.com"]
RUN ["sh", "-c", "curl -u \"${USER}:${PASSWORD}\" https://example.com"]
RUN ["sh", "-c", "curl -u \"${USER_AND_PASSWORD}\" https://example.com"]
RUN ["sh", "-c", "curl -u \"${USER}:${PASSWORD-test}\" https://example.com"]
RUN ["sh", "-c", "curl -u \"${USER}:${PASSWORD+test}\" https://example.com"]
RUN ["sh", "-c", "curl -u \"me:$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" https://example.com"]
RUN ["sh", "-c", "curl -u me:$(echo ${PASSWORD} | openssl passwd -6 -stdin) https://example.com"]

RUN <<EOF
  curl --user "me:password" https://example.com
EOF

# For -u flag ==========================

# Noncompliant@+1
RUN ["curl", "-u", "me:password", "https://example.com"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["curl", "-u", "me:password:may:contains:colon", "https://example.com"]

# Noncompliant@+1
RUN ["curl", "-u", "\"me:password\"", "https://example.com"]

# Noncompliant@+1
RUN ["curl", "-u", "'me:password'", "https://example.com"]

# Noncompliant@+1
RUN ["curl", "--data", "'{\"name\":\"bob\"}'", "--fail", "-u", "me:password", "https://example.com"]

# curl allows use -u multiple times
# Noncompliant@+1
RUN ["curl", "--data", "'{\"name\":\"bob\"}'", "-u", "me:password1", "--fail", "-u", "me:password2", "https://example.com"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "curl -u \"me:password\" https://example.com && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && curl -u \"me:password\" https://example.com"]
RUN <<EOF
  curl -u "me:password" https://example.com
EOF

# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required curl --user $(echo ${PASSWORD} | openssl passwd -6 -stdin) https://example.com"]

# Compliant
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required curl --user $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) https://example.com"]
RUN ["curl", "--user", "usernameonly", "https://example.com"]
RUN ["curl", "-u", "usernameonly", "https://example.com"]
# $UNKNOWN is unknown, so do not raise an issue to avoid FP
RUN ["sh", "-c", "curl --user \"${UNKNOWN}\" https://example.com"]
RUN ["sh", "-c", "curl -u \"${UNKNOWN}\" https://example.com"]
RUN ["sh", "-c", "curl --user \"$UNKNOWN\" https://example.com"]
RUN ["sh", "-c", "curl -u \"$UNKNOWN\" https://example.com"]
RUN ["sh", "-c", "curl --user ${UNKNOWN} https://example.com"]
RUN ["sh", "-c", "curl -u ${UNKNOWN} https://example.com"]
RUN ["sh", "-c", "curl --user $UNKNOWN https://example.com"]
RUN ["sh", "-c", "curl -u $UNKNOWN https://example.com"]

RUN ["curl", "https://example.com"]
RUN ["curl", "--remote-name", "https://example.com"]
RUN ["curl", "--data", "'{\"name\":\"bob\"}'", "--header", "'Content-Type:", "application/json'", "https://example.com"]

