FROM scratch

# Deletion of the wrong cache

# FN: shell form not supported in community edition
RUN apk add nginx && apt clean
# FNs: exec form with explicit shell cal not supported
RUN ["sh", "-c", "apt install nginx && apk cache clean"]
RUN ["sh", "-c", "apk add nginx && apt-get install wget && rm -rf /var/lib/apt/lists/*"]
RUN ["sh", "-c", "apt-get install wget && rm -rf /var/cache/apk/*"]

# All flagged
RUN ["sh", "-c", "apk add nginx && apt-get install wget && apk add wget"]
RUN ["sh", "-c", "apk add nginx\napt-get install wget\napt-get install wget"]
RUN ["sh", "-c", "apt-get install wget && aptitude install wget && rm -rf /var/lib/apt/lists/* && aptitude install wget"]

# Compliant
RUN ["sh", "-c", "apt install nginx && apt-get clean"]
RUN ["sh", "-c", "apt install nginx && aptitude clean"]
RUN ["sh", "-c", "apt-get install nginx && apt clean"]
RUN ["sh", "-c", "apt-get install nginx && aptitude clean"]
RUN ["sh", "-c", "aptitude install nginx && apt clean"]
RUN ["sh", "-c", "aptitude install nginx && apt-get clean"]

# Compliant - direct package installation
RUN ["apk", "add", "nginx.deb"]
RUN ["apt-get", "install", "wget.deb"]
RUN ["apk", "add", "wget.deb"]
RUN ["apk", "add", "nginx.apk"]
RUN ["apt-get", "install", "wget.apk"]
RUN ["apk", "add", "wget.apk"]
RUN ["apk", "add", "./nginx.other"]
RUN ["apt-get", "install", "./wget.other"]
RUN ["apk", "add", "./wget.other"]

# Noncompliant@+1
RUN ["apk", "add", "nginx.other"]
# Noncompliant@+1
RUN ["apt-get", "install", "wget.other"]
# Noncompliant@+1
RUN ["apk", "add", "wget.other"]

# Compliant - no package name / filepath provided
RUN ["apk", "add"]

RUN ["sh", "-c", "apk add nginx && apt-get install wget && rm -rf /var/cache/apk/* && apt-get clean"]
RUN ["sh", "-c", "apt-get install wget && apt-get install wget && aptitude install wget && rm -rf /var/lib/apt/lists/*"]

# Checking on partial paths
RUN ["sh", "-c", "apt-get install wget && rm -rf /var/lib/apt/lists"]
RUN ["sh", "-c", "apt-get install wget && rm -rf /var/lib/apt"]
RUN ["sh", "-c", "apt-get install wget && rm -rf /var/lib"]
RUN ["sh", "-c", "apt-get install wget && rm -rf /var"]
RUN ["sh", "-c", "apk add nginx && rm -rf /var/cache/apk"]
RUN ["sh", "-c", "apk add nginx && rm -rf /var/cache"]
RUN ["sh", "-c", "apk add nginx && rm -rf /var"]
RUN ["sh", "-c", "apk add nginx && rm -rf /etc/apk/cache"]
RUN ["sh", "-c", "apk add nginx && rm -rf /etc/apk"]
RUN ["sh", "-c", "apk add nginx && rm -rf /etc"]

RUN ["sh", "-c", "apt-get install wget && rm -rf /"]
RUN ["sh", "-c", "apt-get install wget && rm -rf /var/lib/apt/lists/"]
RUN ["sh", "-c", "apt-get install wget && rm -rf /var/lib/apt/lists/partial"]

RUN ["sh", "-c", "apt-get install wget\nrm -rf /var/lib/apt/lists/*"]

RUN ["sh", "-c", "apt-get install wget && rm -rf /var/lib/apt/lists/*"]

# Compliant - the cache is mounted
RUN --mount=type=cache,target=/etc/apk/cache ["apk", "add", "nginx"]
RUN --mount=type=cache,target=/etc/apk/cache \
    ["apk", "add", "nginx"]

RUN --mount=type=cache,target=/var/cache/apk ["apk", "add", "nginx"]
RUN --mount=type=cache,target=/var/cache/apk/subpath ["apk", "add", "nginx"]
RUN --mount=type=cache,target=/var/lib/apt/lists ["apt", "install", "nginx"]
RUN --mount=type=cache,target=/var/lib/apt/lists ["apt-get", "install", "nginx"]
RUN --mount=type=cache,target=/var/lib/apt ["apt-get", "install", "nginx"]
RUN --mount=type=cache,target=/var/lib/apt/lists ["aptitude", "install", "nginx"]
RUN --mount=type=cache,target=/var/cache/apt ["apt-get", "install", "nginx"]
RUN --mount=type=cache,target=/var/cache/apk,sharing=locked ["apk", "add", "nginx"]

ARG CACHE_TARGET_ARG=/var/cache/apt
ARG CACHE_ID_ARG=123

RUN --mount=type=cache,target=${CACHE_TARGET_ARG},sharing=locked ["apt-get", "install", "nginx"]
RUN --mount=type=cache,id=${CACHE_ID_ARG},target=/var/cache/apt,sharing=locked ["apt-get", "install", "nginx"]

ENV CACHE_TARGET_ENV=/var/cache/apt
ENV CACHE_ID_ENV=123
ENV CACHE=cache

RUN --mount=type=cache,target=${CACHE_TARGET_ENV},sharing=locked ["apt-get", "install", "nginx"]
RUN --mount=type=cache,id=${CACHE_ID_ENV},target=/var/cache/apt,sharing=locked ["apt-get", "install", "nginx"]

# Those are noncompliant because the `target` or `type` is unresolved
# Noncompliant@+1
RUN --mount=type=cache,target=${UNDEFINED},sharing=locked ["apt-get", "install", "nginx"]
# Noncompliant@+1
RUN --mount=type=${UNDEFINED},target=/var/cache/apt,sharing=locked ["apt-get", "install", "nginx"]
# Noncompliant@+1
RUN --mount=type=${UNDEFINED},target=${UNDEFINED},sharing=locked ["apt-get", "install", "nginx"]

RUN --mount=type=cache,id=${UNDEFINED},target=/var/cache/apt,sharing=locked ["apt-get", "install", "nginx"]
RUN --mount=type=cache,target=/var/cache/apt,sharing=${UNDEFINED} ["apt-get", "install", "nginx"]
RUN --mount=type=${CACHE},target=/var/cache/apt,sharing=${UNDEFINED} ["apt-get", "install", "nginx"]
RUN --mount=type=cache,target=/var/cache/apt,sharing=${UNDEFINED} \
    --mount=type=cache,target=${UNDEFINED} \
    ["apt-get", "install", "nginx"]
RUN --mount=type=cache,target=${UNDEFINED} \
    --mount=type=cache,target=/var/cache/apt \
    ["apt-get", "install", "nginx"]


# Sensitive because the mount type is not cache
# Noncompliant@+1
RUN --mount=type=other,target=/var/cache/apk ["apk", "add", "nginx"]

# Sensitive because the target is not the of any command cache
# Noncompliant@+1
RUN --mount=type=cache,target=/var/cache/other ["apk", "add", "nginx"]

# Sensitive because the target is for a different command cache
# Noncompliant@+1
RUN --mount=type=cache,target=/var/cache/apk ["apt", "install", "nginx"]

# Sensitive because there is no target
# Noncompliant@+1
RUN --mount=type=cache ["apk", "add", "nginx"]

# Sensitive because there is no type
# Noncompliant@+1
RUN --mount=target=/var/cache/apk ["apk", "add", "nginx"]

# Sensitive because the option is not mount
# Noncompliant@+1
RUN --other=target=/var/cache/apk ["apk", "add", "nginx"]

# Sensitive because no value associated to type or target
# Noncompliant@+1
RUN --mount=type,target=/var/cache/apk ["apk", "add", "nginx"]
# Noncompliant@+1
RUN --mount=type=cache,target ["apk", "add", "nginx"]

# Extra use case with invalid mount for split on '='
# Noncompliant@+1
RUN --mount==,target=/etc/apk/cache ["apk", "add", "nginx"]

# Extra use case with invalid mount for split on ','
# Noncompliant@+1
RUN --mount=, ["apk", "add", "nginx"]

