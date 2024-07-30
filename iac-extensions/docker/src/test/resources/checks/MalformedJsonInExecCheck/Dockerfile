FROM scratch

# Checking any instructions that can contains shell forms
# Noncompliant@+1
CMD [ not a valid json
# Noncompliant@+1
RUN [ not a valid json
# Noncompliant@+1
ENTRYPOINT [ not a valid json
# Noncompliant@+1
ADD [ not a valid json
# Noncompliant@+1
COPY [ not a valid json

# Checking any instructions that contains a valid exec form
CMD [ "a", "valid", "json" ]
RUN [ "a", "valid", "json" ]
ENTRYPOINT [ "a", "valid", "json" ]
ADD [ "a", "valid", "json" ]
COPY [ "a", "valid", "json" ]

# Checking any instructions that contains a valid shell form
CMD a valid shell form
RUN a valid shell form
ENTRYPOINT a valid shell form
ADD a valid shell form
COPY a valid shell form

# Compliant because the command doesn't start with [
CMD \D [ "a", "valid", "json" ]

# Sensitive: any short strings after the valid json will prevent this
# Noncompliant@+1
CMD [ "a", "valid", "json" ] \D
# FP: the exclude logic is too simple and don't handle closing bracket ] in string
CMD [ "a", "[valid]", "json" ] \D

# Compliant: if there is too much data behind, consider it's intended and don't raise an issue
CMD [ "a", "valid", "json" ] too much data
CMD [ -f /etc/passwd ] && echo "File exists" || echo "File does not exist"

# Sensitive: missing comma preventings parsing as json
# Noncompliant@+1
CMD [ "a" "valid" "json" ]

# Extra use cases with variables
ENV var="value"
# Noncompliant@+1
CMD [ "invalid", $var ]
# FP: if we cannot resolve something, we don't raise
CMD [ "invalid", $unknwon ]
CMD [ "valid", "$var" ]
CMD [ "valid", "$unknwon" ]

# Sensitive: we detect the presence of quotes-like character and so raise an issue
# Noncompliant@+1
CMD [ "an", "invalid", json ]
# Noncompliant@+1
CMD [ 'some characters' ]
# Noncompliant@+1
CMD [ ‘some characters’ ]
# Noncompliant@+1
CMD [ “some characters” ]
# Noncompliant@+1
ENTRYPOINT [ "/bin/bash”, “-c” ]

# Compliant: no quote like character at all
CMD [Powershell-call]

# Compliant: no quote like character at all
CMD ]

