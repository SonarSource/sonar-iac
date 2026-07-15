FROM foo


ENV ACCESS_TOKEN=${RANDOM}
ENV access_token=${RANDOM}
ENV ACCESS_TOKEN=${RANDOM}
ENV access_token=${RANDOM}
ENV MY_ACCESS_TOKEN=${RANDOM}
ENV MY-ACCESS-TOKEN=${RANDOM}
ENV MyAccessToken=${RANDOM}

# Compliant: now filtered by SecretClassifier
ENV ACCESS_TOKEN=ThisIsSomethingThatShouldProbablyBeSecret
# Noncompliant@+1
ENV TOKEN=Rb7kZpQ2x
# Noncompliant@+1
ENV token=Rb7kZpQ2x
# Noncompliant@+1
ENV MY_1_ACCESS_TOKEN=Rb7kZpQ2x
# Compliant: now filtered by SecretClassifier (ACCESS_TOKEN referenced value is no longer a known secret)
ENV ACCESS_TOKEN=${ACCESS_TOKEN}


ARG CCC
ENV ${CCC}=BBB

ENV ACCESS_TOKEN=""
ENV ACCESS=Rb7kZpQ2x
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
ENV ACCESS_TOKEN="hts://www.secrets.com"

# Noncompliant@+1
ENV WEBHOOK_CREDENTIALS=Rb7kZpQ2x
# Noncompliant@+1
ENV OAUTH2-PASS=Rb7kZpQ2x
# Noncompliant@+1
ENV FtpPassword=Rb7kZpQ2x
ENV ExpireFtpPassword=Rb7kZpQ2x
ENV FtpMyPassword=Rb7kZpQ2x

# Noncompliant@+1
ENV WEBHOOK_CREDENTIALS=$WEBHOOK_CREDENTIALS
ENV WEBHOOK_CREDENTIALS=FOO$WEBHOOK_CREDENTIALS

# Only raise on sensitive varaible name is not resolved to safe value
ARG FOO="https://www.secrets.com"
ENV WEBHOOK_CREDENTIALS=$FOO

# FN hard to do a PascalCase split with numbers
ENV Oauth2Pass=Rb7kZpQ2x

ENV API_KEY=

# Variables containing PUBLIC in the name are not flagged
ENV NEXT_PUBLIC_ACCESS_TOKEN_KEY=Rb7kZpQ2x
ENV NEXT_PUBLIC_API_KEY=Rb7kZpQ2x
ENV PUBLIC_TOKEN=Rb7kZpQ2x

# Variable that contains PUBLIC but only as a substring of a different word
# Noncompliant@+1
ENV PUBLICATION_SECRET_KEY=Rb7kZpQ2x

# Variable where PUBLIC appears as a suffix — still flagged
# Noncompliant@+1
ENV NONPUBLIC_SECRET_KEY=Rb7kZpQ2x


ENV FAKE_SECRET_TOKEN=AAAA
# Compliant because the referenced value (AAAA) is not a real secret
ENV FAKE_SECRET_TOKEN=${FAKE_SECRET_TOKEN}

# Reference should be classified against the value at the time of reference, not a later reassignment
# Compliant: now filtered by SecretClassifier
ENV MY_SECRET_TOKEN=realVerySecretValue123
# Compliant: now filtered by SecretClassifier (MY_SECRET_TOKEN referenced value is no longer a known secret)
ENV MY_SECRET_TOKEN=${MY_SECRET_TOKEN}
ENV MY_SECRET_TOKEN=AAAA
