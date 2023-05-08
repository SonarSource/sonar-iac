FROM scratch

ARG SENSITIVE_USER=other
ARG COMPLIANT_USER=root
ARG SENSITIVE_CHMOD=u+w
ARG COMPLIANT_CHMOD=u+r
ARG SENSITIVE_FILE=file.sh
ARG COMPLIANT_FILE=file.txt

# Noncompliant@+1
ADD  --chown=:group     --chmod=777 file.sh  target/

## Sensitive because user is not root
# Noncompliant@+1
ADD --chown=other              file.sh  target/
# Noncompliant@+1
ADD --chown=other              file.txt target/
# Noncompliant@+1
ADD --chown=other --chmod=u+x  file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+x  file.txt target/
# Noncompliant@+1
ADD --chown=other --chmod=u+w  file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+w  file.txt target/
# Noncompliant@+1
ADD --chown=other --chmod=u+wx file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+wx file.txt target/

## Error messages + locations
# Noncompliant@+1 {{Make sure no write permissions are assigned to the executable.}}
  ADD --chown=other --chmod=u+w file1.sh file2.sh  target/
#     ^^^^^^^^^^^^^> {{Sensitive file owner.}}
#                               ^^^^^^^^@-1
#                                        ^^^^^^^^@-2< {{Other executable file.}}

## Other sensitive use cases
# Noncompliant@+1
ADD  --chown=other      --chmod=200 file.sh  target/
# Noncompliant@+1
ADD  --chown=other      --chmod=    file.sh  target/
# Noncompliant@+1
ADD  --chown=other      --chmod     file.sh  target/
# Noncompliant@+1
ADD  --chown=other      --chmod=300 file.sh  target/
# Noncompliant@+1
ADD  --chown=other      --chmod=300 file.txt target/
# Noncompliant@+1
ADD  --chown=other      --chmod=744 file.sh  target/
# Noncompliant@+1
COPY --chown=other      --chmod=200 file.sh  target/

# Noncompliant@+1
ADD  --chown=other --chmod=400 file.sh  target/
# Noncompliant@+1
COPY --chown=other --chmod=400 file.sh  target/
# Noncompliant@+1
ADD  --chown=other --chmod=200 file.txt target/
# Noncompliant@+1
ADD  --chown=other --chmod=o+w file.sh  target/
# Noncompliant@+1
ADD  --chown=other --chmod=200 sh       target/

# Noncompliant@+1
ADD --chown=other --chmod=g+w          file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+w,g+w      file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=g+wx         file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+w,g+x      file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+w,g+x,o+x  file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+w,o+x      file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+wx,g+x     file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+wx,g+x,o+x file.sh  target/
# Noncompliant@+1
ADD --chown=other --chmod=u+wx,o+x     file.sh  target/

## With variables
# Noncompliant@+1
ADD --chown=$SENSITIVE_USER --chmod=u+w              file1.sh        target/
ADD --chown=$COMPLIANT_USER --chmod=u+w              file1.sh        target/
ADD --chown=$UNKNOWN_USER   --chmod=u+w              file1.sh        target/
# Noncompliant@+1
ADD --chown=other           --chmod=$SENSITIVE_CHMOD file1.sh        target/
# Noncompliant@+1
ADD --chown=other           --chmod=$COMPLIANT_CHMOD file1.sh        target/
# Noncompliant@+1
ADD --chown=other           --chmod=$UNKNOWN_CHMOD   file1.sh        target/
# Noncompliant@+1
ADD --chown=other           --chmod=u+w              $SENSITIVE_FILE target/
# Noncompliant@+1
ADD --chown=other           --chmod=u+w              $COMPLIANT_FILE target/
# Noncompliant@+1
ADD --chown=other           --chmod=u+w              $UNKNOWN_FILE   target/

## Other compliant use cases
ADD  --chown       --chmod=400 file.sh  target/
ADD  --chown=      --chmod=400 file.sh  target/
ADD  --chown=:     --chmod=400 file.sh  target/
ADD  --chown=:group     --chmod=200 file.sh  target/

## Use cases where user='root'/'0', group!='root'/'0' and group has different rights
COPY --chown=root:bar --chmod=604 foo.jar /

COPY --chown=root:bar --chmod=614 foo.jar /

COPY --chown=0:bar --chmod=614 foo.jar /

# Noncompliant@+1
COPY --chown=root:bar --chmod=624 foo.jar /

# Noncompliant@+1
COPY --chown=0:bar --chmod=624 foo.jar /

# Noncompliant@+1
COPY --chown=root:bar --chmod=634 foo.jar /

COPY --chown=root:bar --chmod=644 foo.jar /

COPY --chown=root:bar --chmod=654 foo.jar /

# Noncompliant@+1
COPY --chown=root:bar --chmod=664 foo.jar /

# Noncompliant@+1
COPY --chown=root:bar --chmod=674 foo.jar /


## Compliant use cases because of root user
ADD --chown=root               file.sh  target/
ADD --chown=root:root               file.sh  target/
ADD --chown=root:               file.sh  target/
ADD --chown=:root               file.sh  target/
ADD --chown=0               file.sh  target/
ADD --chown=0:0               file.sh  target/
ADD --chown=:0               file.sh  target/
ADD --chown=:0               file.sh  target/
ADD --chown=root:0               file.sh  target/
ADD --chown=0:root               file.sh  target/

COPY --chown=root:root foo.jar
COPY --chown=root: foo.jar
COPY --chown=:root foo.jar
COPY --chown=0 foo.jar

ADD --chown=root               file.sh  target/
ADD --chown=root               file.txt target/
ADD --chown=root  --chmod=u+x  file.sh  target/
ADD --chown=root  --chmod=u+x  file.txt target/
ADD --chown=root  --chmod=u+w  file.sh  target/
ADD --chown=root  --chmod=u+w  file.txt target/
ADD --chown=root  --chmod=u+wx file.sh  target/
ADD --chown=root  --chmod=u+wx file.txt target/

## Compliant because user doesn't change
ADD --chown=:              file.sh  target/

## Compliant, but should no lead to an exception of the check
COPY --chown=root:bar --chmod=614 fileName:withColon.sh /
