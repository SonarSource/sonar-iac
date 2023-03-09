FROM foo


ENV ACCESS_TOKEN=${RANDOM}
ENV access_token=${RANDOM}
ENV ACCESS_TOKEN=${RANDOM}
ENV access_token=${RANDOM}
ENV MY_ACCESS_TOKEN=${RANDOM}
ENV MY-ACCESS-TOKEN=${RANDOM}
ENV MyAccessToken=${RANDOM}

# Noncompliant@+1 {{Make sure that using ENV to handle a secret is safe here.}}
ENV ACCESS_TOKEN=ThisIsSomethingThatShouldProbablyBeSecret
# Noncompliant@+1
ENV TOKEN=AAAA
# Noncompliant@+1
ENV token=AAAA
# Noncompliant@+1
ENV ACCESS_TOKEN="hts://www.secrets.com"
# Noncompliant@+1
ENV MY_1_ACCESS_TOKEN=AAAA
# Noncompliant@+1
ENV ACCESS_TOKEN=${ACCESS_TOKEN}


ARG CCC
ENV ${CCC}=BBB

ENV ACCESS_TOKEN=""
ENV ACCESS=AAAA
ENV NOACCESS_TOKEN=whatever
ENV ACCESS_TOKEN_PATH=${RANDOM}
ENV ACCESS_TOKEN=/usr/bin/:/bin/:/sbin/
ENV ACCESS_TOKEN="/run/secrets/token.json"
ENV ACCESS_TOKEN="/root/path/"
ENV ACCESS_TOKEN="./relative/path/"
ENV ACCESS_TOKEN "/run/secrets/token.json"${RANDOM}
ENV ACCESS_TOKEN=${ARG:-"/run/secrets/token.json"}
ENV ACCESS_MY_TOKEN=whatever
ENV ACCESS_TOKEN="https://www.secrets.com"

# Noncompliant@+1
ENV WEBHOOK_CREDENTIALS=AAAA
# Noncompliant@+1
ENV OAUTH2-PASS=AAAA
# Noncompliant@+1
ENV FtpPassword=AAAA
ENV ExpireFtpPassword=AAAA
ENV FtpMyPassword=AAAA

# Noncompliant@+1
ENV WEBHOOK_CREDENTIALS=$WEBHOOK_CREDENTIALS
ENV WEBHOOK_CREDENTIALS=FOO$WEBHOOK_CREDENTIALS

# Only raise on sensitive varaible name is not resolved to safe value
ARG JWT_KEY="https://www.secrets.com"
ENV WEBHOOK_CREDENTIALS=$JWT_KEY

# FN hard to do a PascalCase split with numbers
ENV Oauth2Pass=AAAA

ENV API_KEY=
