FROM foo


# Noncompliant@+1 {{Make sure that using ARG to handle a secret is safe here.}}
ARG ACCESS_TOKEN=ThisIsSomethingThatShouldProbablyBeSecret
#   ^^^^^^^^^^^^
# Noncompliant@+1
ARG access_token=ThisIsSomethingThatShouldProbablyBeSecret
# Noncompliant@+1
ARG TOKEN=AAAA
# Noncompliant@+1
ARG token=AAAA
# Noncompliant@+1
ARG MY_1_ACCESS_TOKEN=AAAA
# Noncompliant@+1
ARG ACCESS_TOKEN=${ACCESS_TOKEN}
#   ^^^^^^^^^^^^

# Noncompliant@+1
ARG ACCESS_TOKEN=""
# Noncompliant@+1
ARG ACCESS_TOKEN=${RANDOM}

ARG CCC
ARG ${CCC}=BBB
ARG ACCESS_TOKEN="hts://www.secrets.com"

# Noncompliant@+1
ARG WEBHOOK_CREDENTIALS=AAAA
# Noncompliant@+1
ARG OAUTH2-PASS=AAAA
# Noncompliant@+1
ARG FtpPassword=AAAA
ARG ExpireFtpPassword=AAAA
ARG FtpMyPassword=AAAA

# Only raise on sensitive varaible name is not resolved to safe value
ARG FOO="https://www.secrets.com"
ARG WEBHOOK_CREDENTIALS=$FOO

# Noncompliant@+1
ARG WEBHOOK_CREDENTIALS=$WEBHOOK_CREDENTIALS
# Noncompliant@+1
ARG WEBHOOK_CREDENTIALS=FOO$WEBHOOK_CREDENTIALS

# Noncompliant@+1
ARG JDBC_TOKEN=""

ARG WEBHOOK_SIZE=""
