FROM scratch

ARG SENSITIVE_USER=other
ARG COMPLIANT_USER=root
ARG SENSITIVE_FILE=start.sh
ARG COMPLIANT_FILE=file.txt

## Issue: non-root user with risky shell script extension

# Noncompliant@+1
ADD --chown=other file.sh target/
# Noncompliant@+1
ADD --chown=other file.bash target/
# Noncompliant@+1
ADD --chown=other file.zsh target/
# Noncompliant@+1
ADD --chown=other file.fish target/

## Issue: non-root user with risky interpreted script extension

# Noncompliant@+1
ADD --chown=other file.py target/
# Noncompliant@+1
ADD --chown=other file.rb target/
# Noncompliant@+1
ADD --chown=other file.pl target/
# Noncompliant@+1
ADD --chown=other file.php target/

## Issue: non-root user with risky native executable extension

# Noncompliant@+1
ADD --chown=other file.bin target/
# Noncompliant@+1
ADD --chown=other file.elf target/
# Noncompliant@+1
ADD --chown=other libcustom.so target/

## Issue: non-root user with risky systemd unit extension

# Noncompliant@+1
ADD --chown=other svc.service target/
# Noncompliant@+1
ADD --chown=other svc.timer target/
# Noncompliant@+1
ADD --chown=other svc.socket target/

## Issue: non-root user with sensitive destination path (risky extension irrelevant)

# Noncompliant@+1
COPY --chown=other file.txt /etc/config
# Noncompliant@+1
COPY --chown=other file.txt /bin/tool
# Noncompliant@+1
COPY --chown=other file.txt /usr/share/file
# Noncompliant@+1
COPY --chown=other file.txt /dev/device
# Noncompliant@+1
COPY --chown=other file.txt /boot/grub.cfg
# Noncompliant@+1
COPY --chown=other file.txt /root/file
# Noncompliant@+1
COPY --chown=other file.txt /proc/something
# Noncompliant@+1
COPY --chown=other file.txt /sbin/tool
# Noncompliant@+1
COPY --chown=other file.txt /lib/lib.so.1
# Noncompliant@+1
COPY --chown=other libfoo.so /lib64/libfoo.so
# Noncompliant@+1
COPY --chown=other libbar.so /lib32/libbar.so
# subdirectory of sensitive path
# Noncompliant@+1
COPY --chown=other entrypoint.sh /usr/local/bin/entrypoint.sh

## Issue: ADD instruction with sensitive destination path

# Noncompliant@+1
ADD --chown=other file.txt /etc/config
# Noncompliant@+1
ADD --chown=other file.txt /usr/share/file

## Issue: non-root group (user is root) with risky extension or sensitive path

# Noncompliant@+1
ADD --chown=:group file.sh target/
# Noncompliant@+1
ADD --chown=:group file.txt /etc/config

## Error message and secondary locations
# Noncompliant@+1 {{Make sure the copied resource cannot be modified by a non-root user.}}
  ADD --chown=other file1.sh file2.sh  target/
#     ^^^^^^^^^^^^^> {{Sensitive file owner.}}
#                   ^^^^^^^^@-1
#                            ^^^^^^^^@-2< {{Other copied resource.}}

## With variables: resolved non-root user triggers, resolved root user does not
# Noncompliant@+1
ADD --chown=$SENSITIVE_USER file.sh target/
ADD --chown=$COMPLIANT_USER file.sh target/
ADD --chown=$UNKNOWN_USER   file.sh target/

## With variables: resolved risky file triggers, resolved safe file and unresolved do not
# Noncompliant@+1
ADD --chown=other $SENSITIVE_FILE target/
ADD --chown=other $COMPLIANT_FILE target/
ADD --chown=other $UNKNOWN_FILE   target/

## Compliant: root ownership (user)
ADD --chown=root               file.sh  target/
ADD --chown=root:root          file.sh  target/
ADD --chown=root:              file.sh  target/
ADD --chown=:root              file.sh  target/
ADD --chown=0                  file.sh  target/
ADD --chown=0:0                file.sh  target/
ADD --chown=0:root             file.sh  target/

## Compliant: no chown flag
ADD file.sh target/
COPY --chmod=755 start.sh /usr/local/bin/start.sh

## Compliant: empty/root-equivalent chown values
ADD --chown       file.sh target/
ADD --chown=      file.sh target/
ADD --chown=:     file.sh target/

## Compliant: non-risky extension AND non-sensitive destination path
ADD --chown=other file.txt   target/
COPY --chown=other config.yaml /app/config.yaml
COPY --chown=other dist/       /app/dist/
COPY --chown=other app.jar     /app/
COPY --chown=other app.conf    /app/

## Compliant: sensitive path but root ownership
COPY --chown=root config.txt /etc/config
COPY --chown=0    config.txt /bin/tool

## Compliant: non-root group (user is root), non-risky extension, non-sensitive path
COPY --chown=root:bar --chmod=604 foo.jar /app/
COPY --chown=root:bar --chmod=664 foo.jar /app/
COPY --chown=0:bar              foo.jar /app/

## Compliant: path looks similar to sensitive path but is not
COPY --chown=other file.txt /develop/app
COPY --chown=other file.txt /binary/app
COPY --chown=other file.txt /app/etc/config
