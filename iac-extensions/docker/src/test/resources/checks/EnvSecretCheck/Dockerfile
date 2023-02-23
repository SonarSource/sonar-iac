FROM foo


ENV ACCESS_TOKEN=${ACCESS_TOKEN}
ENV access_token=${ACCESS_TOKEN}
ENV ACCESS_TOKEN=${access_token}
ENV access_token=${access_token}
ENV MY_ACCESS_TOKEN=${ACCESS_TOKEN}
ENV MY-ACCESS-TOKEN=${ACCESS_TOKEN}
ENV MyAccessToken=${ACCESS_TOKEN}

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


ARG CCC
ENV ${CCC}=BBB

ENV ACCESS_TOKEN=""
ENV ACCESS=AAAA
ENV NOACCESS_TOKEN=whatever
ENV ACCESS_TOKEN_PATH=${ACCESS_TOKEN}
ENV ACCESS_TOKEN=/usr/bin/:/bin/:/sbin/
ENV ACCESS_TOKEN="/run/secrets/token.json"
ENV ACCESS_TOKEN="/root/path/"
ENV ACCESS_TOKEN="./relative/path/"
ENV ACCESS_TOKEN "/run/secrets/token.json"${ACCESS_TOKEN}
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

# FN hard to do a PascalCase split with numbers
ENV Oauth2Pass=AAAA

ENV API_KEY=
