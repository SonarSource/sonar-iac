FROM scratch

# Test issue location
# FN: shell form not supported in community edition
RUN cd /tmp && ./entrypoint.sh

ENTRYPOINT unzip foobar.zip && cd foobar

# Noncompliant@+1
CMD ["cd", "/tmp", "&&", "/entrypoint.sh"]
#    ^^^^^^^^^^^^

# Noncompliant@+1
ENTRYPOINT ["/entrypoint.sh", "&&", "cd", "/tmp"]
#                                   ^^^^^^^^^^^^

# Currently we don't raise issues on any heredoc
RUN <<EOF
  cd foo/bar
EOF

RUN <<EOF
    apk add --no-cache openssl git
    cd /etc/nginx
    git init
EOF
