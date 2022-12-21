FROM foo


# Noncompliant@+1 {{Make sure that using ENV to handle a secret is safe here.}}
ENV ACCESS_TOKEN=${ACCESS_TOKEN}
#   ^^^^^^^^^^^^
# Noncompliant@+1
ENV MY_ACCESS_TOKEN=${ACCESS_TOKEN}
# Noncompliant@+1
ENV MY-ACCESS-TOKEN=${ACCESS_TOKEN}
# Noncompliant@+1
ENV MyAccessToken=${ACCESS_TOKEN}
# Noncompliant@+1
ENV ACCESS_TOKEN=ThisIsSomethingThatShouldProbablyBeSecret
# Noncompliant@+1
ENV TOKEN=AAAA
# Noncompliant@+1
ENV ACCESS_TOKEN ${ARG
# Noncompliant@+1
ENV ACCESS_TOKEN "hts://www.secrets.com"

ENV ACCESS_TOKEN=""
ENV ACCESS=AAAA
ENV NOACCESS_TOKEN=whatever
ENV ACCESS_TOKEN_PATH=${ACCESS_TOKEN}
ENV ACCESS_TOKEN=/usr/bin/:/bin/:/sbin/
ENV ACCESS_TOKEN "/run/secrets/token.json"
ENV ACCESS_TOKEN "/run/secrets/token.json"${ACCESS_TOKEN}
ENV ACCESS_TOKEN ${ARG:-"/run/secrets/token.json"}
ENV ACCESS_MY_TOKEN=whatever
ENV ACCESS_TOKEN "https://www.secrets.com"
