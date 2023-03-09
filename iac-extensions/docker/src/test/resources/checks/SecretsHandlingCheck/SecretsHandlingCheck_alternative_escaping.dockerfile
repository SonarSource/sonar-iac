# escape=`
FROM foo

# Noncompliant@+2 {{Make sure that using ENV to handle a secret is safe here.}}
EN`
V ACCESS_TOKEN=AAAAA

