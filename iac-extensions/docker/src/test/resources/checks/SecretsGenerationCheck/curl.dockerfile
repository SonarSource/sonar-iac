FROM ubuntu:22.04

ARG PASSWORD
ARG USER
ENV USER_AND_PASSWORD=me:pass

# Noncompliant@+1
RUN curl --user me:password https://example.com
#   ^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN curl --user me:password:may:contains:colon https://example.com

# Noncompliant@+1
RUN curl --user "me:password" https://example.com

# Noncompliant@+1
RUN curl --user 'me:password' https://example.com

# Noncompliant@+1
RUN curl --user "me:$PASSWORD" https://example.com

# Noncompliant@+1
RUN curl --user "me:${PASSWORD}" https://example.com

# Noncompliant@+1
RUN curl --user "${USER}:${PASSWORD}" https://example.com

# Noncompliant@+1
RUN curl --user "${USER_AND_PASSWORD}" https://example.com

# Noncompliant@+1
RUN curl --user "${USER}:${PASSWORD-test}" https://example.com

# Noncompliant@+1
RUN curl --user "${USER}:${PASSWORD+test}" https://example.com

# Noncompliant@+1
RUN curl --user "me:$(echo ${PASSWORD} | openssl passwd -6 -stdin)" https://example.com

# Noncompliant@+1
RUN curl --user me:$(echo ${PASSWORD} | openssl passwd -6 -stdin) https://example.com

# Noncompliant@+1
RUN curl --data '{"name":"bob"}' --fail --user me:password https://example.com

# curl allows use --user multiple times
# Noncompliant@+1
RUN curl --data '{"name":"bob"}' --user me:password1 --fail --user me:password2 https://example.com

# Noncompliant@+1
RUN curl --user "me:password" https://example.com && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    curl --user "me:password" https://example.com

# Noncompliant@+2
RUN <<EOF
  curl --user "me:password" https://example.com
EOF

# For -u flag ==========================

# Noncompliant@+1
RUN curl -u me:password https://example.com
#   ^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN curl -u me:password:may:contains:colon https://example.com

# Noncompliant@+1
RUN curl -u "me:password" https://example.com

# Noncompliant@+1
RUN curl -u 'me:password' https://example.com

# Noncompliant@+1
RUN curl -u "me:$PASSWORD" https://example.com

# Noncompliant@+1
RUN curl -u "me:${PASSWORD}" https://example.com

# Noncompliant@+1
RUN curl -u "${USER}:${PASSWORD}" https://example.com

# Noncompliant@+1
RUN curl -u "${USER_AND_PASSWORD}" https://example.com

# Noncompliant@+1
RUN curl -u "${USER}:${PASSWORD-test}" https://example.com

# Noncompliant@+1
RUN curl -u "${USER}:${PASSWORD+test}" https://example.com

# Noncompliant@+1
RUN curl -u "me:$(echo ${PASSWORD} | openssl passwd -6 -stdin)" https://example.com

# Noncompliant@+1
RUN curl -u me:$(echo ${PASSWORD} | openssl passwd -6 -stdin) https://example.com

# Noncompliant@+1
RUN curl --data '{"name":"bob"}' --fail -u me:password https://example.com

# curl allows use -u multiple times
# Noncompliant@+1
RUN curl --data '{"name":"bob"}' -u me:password1 --fail -u me:password2 https://example.com

# Noncompliant@+1
RUN curl -u "me:password" https://example.com && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    curl -u "me:password" https://example.com

# Noncompliant@+2
RUN <<EOF
  curl -u "me:password" https://example.com
EOF


# Compliant
RUN curl --user usernameonly https://example.com
RUN curl -u usernameonly https://example.com
# $UNKNOWN is inknown, so do not raise an issue to avoid FP
RUN curl --user "${UNKNOWN}" https://example.com
RUN curl -u "${UNKNOWN}" https://example.com
RUN curl --user "$UNKNOWN" https://example.com
RUN curl -u "$UNKNOWN" https://example.com
RUN curl --user ${UNKNOWN} https://example.com
RUN curl -u ${UNKNOWN} https://example.com
RUN curl --user $UNKNOWN https://example.com
RUN curl -u $UNKNOWN https://example.com

RUN curl https://example.com
RUN curl --remote-name https://example.com
RUN curl --data '{"name":"bob"}' --header 'Content-Type: application/json' https://example.com

