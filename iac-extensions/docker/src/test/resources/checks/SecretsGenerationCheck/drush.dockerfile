FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN drush user:password username secret

FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN ["drush", "user:password", "username", "password"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["drush", "user:password", "username", "\"This", "should", "be", "kept", "secret\""]

# Noncompliant@+1
RUN ["drush", "user:password", "username", "'This", "should", "be", "kept", "secret'"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "drush user:password username \"$PASSWORD\""]
RUN ["sh", "-c", "drush user:password username \"${PASSWORD}\""]
RUN ["sh", "-c", "sudo drush user:password username \"${PASSWORD}\""]
RUN ["sh", "-c", "drush user:password username \"${PASSWORD:-test}\""]
RUN ["sh", "-c", "drush user:password username \"${PASSWORD:+test}\""]
RUN ["sh", "-c", "drush -v --yes user:password username \"$PASSWORD\""]
RUN ["sh", "-c", "drush user:password username \"$PASSWORD\" -v --yes"]
RUN ["sh", "-c", "drush user:password username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\""]
RUN ["sh", "-c", "drush user:password username \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" /homedir:/home/username /active:yes"]
RUN ["sh", "-c", "sudo drush user:password username \"${PASSWORD}\" && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && sudo drush user:password username \"${PASSWORD}\""]

# Alternative syntax for user:password ================

# Noncompliant@+1
RUN ["drush", "upwd", "username", "password"]

# Noncompliant@+1
RUN ["drush", "-v", "upwd", "username", "password"]

# Noncompliant@+1
RUN ["drush", "user-password", "username", "password"]

# In other drush commands there can be --password=PASSWORD flag =========

# Noncompliant@+1
RUN ["drush", "user:create", "newuser", "--mail='person@example.com'", "--password=\"password\""]

# Noncompliant@+1
RUN ["drush", "user:create", "newuser", "--mail='person@example.com'", "--password='password'"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "drush user:create newuser --password=\"$PASSWORD\""]
RUN ["sh", "-c", "drush user:create newuser --password=$PASSWORD"]
RUN ["sh", "-c", "drush user:create newuser \"--password=$PASSWORD\""]
RUN ["sh", "-c", "drush user:create newuser --password=\"${PASSWORD}\""]
RUN ["sh", "-c", "drush user:create newuser --password=\"${PASSWORD:-test}\""]
RUN ["sh", "-c", "drush user:create newuser --password=\"${PASSWORD:+test}\""]
RUN ["sh", "-c", "drush user:create newuser --password=\"${PASSWORD}\" -v --yes"]
RUN ["sh", "-c", "drush user:create newuser --password=\"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\""]
RUN ["sh", "-c", "drush user:create newuser --password=\"${PASSWORD}\" --mail='person@example.com'"]
RUN ["sh", "-c", "drush user:create newuser --password=\"${PASSWORD}\" && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && drush user:create newuser --password=\"${PASSWORD}\""]
RUN ["sh", "-c", "drush ucrt newuser \"--password=$PASSWORD\""]
RUN ["sh", "-c", "drush user-create newuser \"--password=$PASSWORD\""]
RUN ["sh", "-c", "drush foo newuser \"--password=$PASSWORD\""]


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required drush user:password username $(echo ${PASSWORD} | openssl passwd -6 -stdin)"]

# Compliant
RUN ["sh", "-c", "--mount=type=secret drush user:password username $(cat /run/secrets/mysecret | openssl passwd -6 -stdin)"]
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required drush user:password username $(cat /run/secrets/mysecret | openssl passwd -6 -stdin)"]


# It's wrong usage of usermod because those flags require an argument
RUN ["drush", "en", "modulename"]
RUN ["drush", "cc", "css-js"]

RUN ["drush", "upwd", "username"]
RUN ["drush", "user:create", "newuser"]
RUN ["sudo", "drush", "user:password", "username"]
