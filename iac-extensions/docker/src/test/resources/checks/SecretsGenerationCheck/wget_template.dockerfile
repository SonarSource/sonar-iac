FROM ubuntu:22.04 as build

# no issue in non final stage
RUN wget --user=guest --flag=MySuperPassword https://example.com


FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN wget --user=guest --flag=MySuperPassword https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag="This should be kept secret" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag='This should be kept secret' https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag="$PASSWORD" https://example.com

# Noncompliant@+1
RUN wget --user=guest "--flag=$PASSWORD" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag="${PASSWORD}" https://example.com

# Noncompliant@+1
RUN wget --user=guest "--flag=$PASSWORD" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag="${PASSWORD:-test}" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag="${PASSWORD:+test}" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag="$(echo ${PASSWORD} | openssl passwd -6 -stdin)" https://example.com

# Noncompliant@+1
RUN wget --flag="$PASSWORD" https://example.com

# Noncompliant@+1
RUN wget --mirror --no-parent --flag="${PASSWORD}" https://example.com/somepath/

# Noncompliant@+1
RUN wget --user=guest --flag=${PASSWORD:+test} https://example.com >> file.zip && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    wget --user=guest --flag=${PASSWORD:+test} https://example.com >> file.zip


# Space after flag instead of equals ==============

# Noncompliant@+1
RUN wget --user=guest --flag "This should be kept secret" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag 'This should be kept secret' https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag "$PASSWORD" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag "${PASSWORD}" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag "${PASSWORD:-test}" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag "${PASSWORD:+test}" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag $PASSWORD https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag ${PASSWORD} https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag ${PASSWORD:-test} https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag ${PASSWORD:+test} https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag ${PASSWORD:+test} https://example.com >> file.zip && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    wget --user=guest --flag ${PASSWORD:+test} https://example.com >> file.zip

# Noncompliant@+1
RUN --network=none wget --user=guest --flag ${PASSWORD} https://example.com

# Noncompliant@+1
RUN --mount=type=tmpfs wget --user=guest --flag $(echo ${PASSWORD} | openssl passwd -6 -stdin) https://example.com

# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN --mount=type=secret,id=mysecret,required wget --user=guest --flag $(echo ${PASSWORD} | openssl passwd -6 -stdin) https://example.com

# Compliant
RUN --mount=type=secret,id=mysecret,required wget --user=guest --flag $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) https://example.com
RUN wget https://example.com
RUN wget --user=guest https://example.com
RUN wget --user=guest --ask-password https://example.com
