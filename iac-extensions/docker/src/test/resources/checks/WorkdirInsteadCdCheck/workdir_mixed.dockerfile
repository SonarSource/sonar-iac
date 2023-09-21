FROM scratch

# Test issue location
# Noncompliant@+1 {{WORKDIR instruction should be used instead of cd command.}}
RUN cd /app/bin && ./start.sh
#   ^^^^^^^^^^^

# Noncompliant@+1
ENTRYPOINT unzip foobar.zip && cd foobar
#                              ^^^^^^^^^

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
