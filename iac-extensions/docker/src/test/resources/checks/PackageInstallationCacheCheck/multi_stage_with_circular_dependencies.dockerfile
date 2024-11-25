FROM final AS build

# Noncompliant@+1
RUN apk add nginx

FROM build as final
COPY --from=build /usr/local/nginx/ /usr/local/nginx/

# Noncompliant@+1
RUN apk add wget
