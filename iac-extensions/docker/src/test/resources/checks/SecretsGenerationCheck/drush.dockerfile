FROM ubuntu:22.04 as build

# no issue in non final stage
RUN drush user:password username password


FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN drush user:password username password
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN drush user:password username "This should be kept secret"

# Noncompliant@+1
RUN drush user:password username 'This should be kept secret'

# Noncompliant@+1
RUN drush user:password username "$PASSWORD"

# Noncompliant@+1
RUN drush user:password username "${PASSWORD}"

# Noncompliant@+1
RUN sudo drush user:password username "${PASSWORD}"

# Noncompliant@+1
RUN drush user:password username "${PASSWORD:-test}"

# Noncompliant@+1
RUN drush user:password username "${PASSWORD:+test}"

# Noncompliant@+1
RUN drush user:password username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)"

# Noncompliant@+1
RUN drush user:password username "$(echo ${PASSWORD} | openssl passwd -6 -stdin)" /homedir:/home/username /active:yes

# Noncompliant@+1
RUN sudo drush user:password username "${PASSWORD}" && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    sudo drush user:password username "${PASSWORD}"

# Alternative syntax for user:password ================

# Noncompliant@+1
RUN drush upwd username password

# Noncompliant@+1
RUN drush user-password username password

# In other drush commands there can be --password=PASSWORD flag =========

# Noncompliant@+1
RUN drush user:create newuser --mail='person@example.com' --password="password"

# Noncompliant@+1
RUN drush user:create newuser --mail='person@example.com' --password='password'

# Noncompliant@+1
RUN drush user:create newuser --password="$PASSWORD"

# Noncompliant@+1
RUN drush user:create newuser --password=$PASSWORD

# Noncompliant@+1
RUN drush user:create newuser "--password=$PASSWORD"

# Noncompliant@+1
RUN drush user:create newuser --password="${PASSWORD}"

# Noncompliant@+1
RUN drush user:create newuser --password="${PASSWORD:-test}"

# Noncompliant@+1
RUN drush user:create newuser --password="${PASSWORD:+test}"

# Noncompliant@+1
RUN drush user:create newuser --password="${PASSWORD}" -v --yes

# Noncompliant@+1
RUN drush user:create newuser --password="$(echo ${PASSWORD} | openssl passwd -6 -stdin)"

# Noncompliant@+1
RUN drush user:create newuser --password="${PASSWORD}" --mail='person@example.com'

# Noncompliant@+1
RUN drush user:create newuser --password="${PASSWORD}" && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    drush user:create newuser --password="${PASSWORD}"

# Afternative syntax for user:create ===============

# Noncompliant@+1
RUN drush ucrt newuser "--password=$PASSWORD"

# Noncompliant@+1
RUN drush user-create newuser "--password=$PASSWORD"

# We can safely assume that any command followed by --pasword=PWD is unsafe
# Noncompliant@+1
RUN drush foo newuser "--password=$PASSWORD"


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN --mount=type=secret,id=mysecret,required drush user:password username $(echo ${PASSWORD} | openssl passwd -6 -stdin)

# Compliant
RUN --mount=type=secret drush user:password username $(cat C:\ProgramData\Docker\secrets\secret | openssl passwd -6 -stdin)
RUN --mount=type=secret,id=mysecret,required drush user:password username $(cat C:\ProgramData\Docker\secrets\secret | openssl passwd -6 -stdin)

# It's wrong usage of usermod because those flags require an argument
RUN drush en modulename
RUN drush cc css-js

RUN drush user:create newuser
RUN sudo drush user:password username
