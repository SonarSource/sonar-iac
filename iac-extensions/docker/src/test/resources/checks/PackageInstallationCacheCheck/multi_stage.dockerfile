FROM scratch

# FN: shell form not supported in community edition
RUN apk add nginx

FROM scratch AS build

# Compliant, this is not part of the final image
RUN ["apk", "add", "nginx"]

FROM scratch
COPY --from=build /usr/local/nginx/ /usr/local/nginx/

# Noncompliant@+1
RUN ["apk", "add", "wget"]
