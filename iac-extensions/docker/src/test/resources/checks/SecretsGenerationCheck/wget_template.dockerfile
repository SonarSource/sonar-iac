FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN wget --mirror --no-parent --flag="${PASSWORD}" https://example.com/somepath/

# Noncompliant@+1
RUN wget --user=guest --flag=MySuperPassword https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag="This should be kept secret" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag='This should be kept secret' https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag="$PASSWORD" https://example.com

# Noncompliant@+1
RUN wget --user=guest --flag="${PASSWORD}" https://example.com

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



# Compliant
RUN wget https://example.com
RUN wget --user=guest https://example.com
RUN wget --user=guest --ask-password https://example.com
