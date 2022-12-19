FROM foo

# Noncompliant@+1 {{Make sure that exposing administration services is safe here.}}
EXPOSE 22
#      ^^

# Noncompliant@+1 {{Make sure that exposing administration services is safe here.}}
EXPOSE 22/tcp
#      ^^^^^^

# Noncompliant@+1
EXPOSE 23
# Noncompliant@+1
EXPOSE 3389
# Noncompliant@+1
EXPOSE 5800
# Noncompliant@+1
EXPOSE 5900

EXPOSE 80
EXPOSE 222
EXPOSE 33891

EXPOSE 22/udp

# Noncompliant@+1
EXPOSE 40 80 22 20
#            ^^

# Noncompliant@+1
EXPOSE 40 80 22/tcp 20
#            ^^^^^^

# Noncompliant@+1
EXPOSE 20-25

EXPOSE FOO
