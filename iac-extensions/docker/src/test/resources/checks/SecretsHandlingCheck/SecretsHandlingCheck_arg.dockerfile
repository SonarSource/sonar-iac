FROM foo


# Noncompliant@+1 {{Make sure that using ARG to handle a secret is safe here.}}
ARG ACCESS_TOKEN=ThisIsSomethingThatShouldProbablyBeSecret
#   ^^^^^^^^^^^^
# Noncompliant@+1
ARG TOKEN=AAAA
# Noncompliant@+1
ARG token=AAAA
# Noncompliant@+1
ARG ACCESS_TOKEN="hts://www.secrets.com"
# Noncompliant@+1
ARG MY_1_ACCESS_TOKEN=AAAA
# Noncompliant@+1
ARG ACCESS_TOKEN=${ACCESS_TOKEN}
#   ^^^^^^^^^^^^

ARG ACCESS_TOKEN=${RANDOM}

ARG CCC
ARG ${CCC}=BBB

ARG ACCESS_TOKEN=""
ARG ACCESS=AAAA
ARG NOACCESS_TOKEN=whatever
ARG ACCESS_TOKEN_PATH=${RANDOM}
ARG ACCESS_TOKEN=/usr/bin/:/bin/:/sbin/
ARG ACCESS_TOKEN="/run/secrets/token.json"
ARG ACCESS_TOKEN="/root/path/"
ARG ACCESS_TOKEN="./relative/path/"
ARG ACCESS_TOKEN "/run/secrets/token.json"${RANDOM}
ARG ACCESS_TOKEN=${ARG:-"/run/secrets/token.json"}
ARG ACCESS_MY_TOKEN=whatever
ARG ACCESS_TOKEN="https://www.secrets.com"

# Noncompliant@+1
ARG WEBHOOK_CREDENTIALS=AAAA
# Noncompliant@+1
ARG OAUTH2-PASS=AAAA
# Noncompliant@+1
ARG FtpPassword=AAAA
ARG ExpireFtpPassword=AAAA
ARG FtpMyPassword=AAAA

# Noncompliant@+1
ENV WEBHOOK_CREDENTIALS=$WEBHOOK_CREDENTIALS

ENV WEBHOOK_CREDENTIALS=FOO$WEBHOOK_CREDENTIALS
