FROM build2 AS build1

# FN: shell form not supported in community edition
RUN apk add nginx

FROM scratch AS build2

# Final stage depends transitively on this stage
# Noncompliant@+1
RUN ["apk", "add", "python3"]

FROM build1 AS final
COPY /usr/local/nginx/ /usr/local/nginx/
# Noncompliant@+1
RUN ["apk", "add", "wget"]
