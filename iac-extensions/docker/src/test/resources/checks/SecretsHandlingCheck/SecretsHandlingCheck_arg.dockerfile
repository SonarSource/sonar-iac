FROM foo


# Compliant: now filtered by SecretClassifier
ARG ACCESS_TOKEN=ThisIsSomethingThatShouldProbablyBeSecret
# Compliant: now filtered by SecretClassifier
ARG access_token=ThisIsSomethingThatShouldProbablyBeSecret
# Noncompliant@+1
ARG TOKEN=Rb7kZpQ2x
# Noncompliant@+1
ARG token=Rb7kZpQ2x
# Noncompliant@+1
ARG MY_1_ACCESS_TOKEN=Rb7kZpQ2x
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
ARG WEBHOOK_CREDENTIALS=Rb7kZpQ2x
# Noncompliant@+1
ARG OAUTH2-PASS=Rb7kZpQ2x
# Noncompliant@+1
ARG FtpPassword=Rb7kZpQ2x
ARG ExpireFtpPassword=Rb7kZpQ2x
ARG FtpMyPassword=Rb7kZpQ2x

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
