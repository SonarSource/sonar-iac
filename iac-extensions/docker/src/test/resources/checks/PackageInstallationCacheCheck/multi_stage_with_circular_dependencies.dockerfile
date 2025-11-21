FROM final AS build

# FN: shell form not supported in community edition
RUN apk add nginx

FROM build as final
COPY --from=build /usr/local/nginx/ /usr/local/nginx/

# Noncompliant@+1
RUN ["apk", "add", "wget"]
