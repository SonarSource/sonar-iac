# Noncompliant@+1 {{Replace `as` with upper case format `AS`.}}
  FROM ubuntu:latest as jammy
#                    ^^

# Noncompliant@+1 {{Replace `Maintainer` with upper case format `MAINTAINER`.}}
  Maintainer foo<foo@bar.test>
# ^^^^^^^^^^

# Noncompliant@+2 {{Replace `cmd` with upper case format `CMD`.}}
  HEALTHCHECK --interval=5m --timeout=3s \
    cmd curl -f http://localhost/ || exit 1
#   ^^^
