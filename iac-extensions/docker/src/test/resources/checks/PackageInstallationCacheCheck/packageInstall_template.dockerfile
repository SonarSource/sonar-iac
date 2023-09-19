FROM scratch

# Noncompliant@+1
RUN {$commandName} {$installCommand} nginx

# Noncompliant@+1 2
RUN {$commandName} {$installCommand} nginx && {$commandName} {$installCommand} wget

# Noncompliant@+1
RUN sudo {$commandName} {$installCommand} nginx

# Noncompliant@+1
RUN someOtherCommand && {$commandName} {$installCommand} nginx && otherCommand

# Noncompliant@+1
RUN {$commandName} -flag {$installCommand} -flag nginx wget

# Noncompliant@+1
RUN {$commandName} -flag=value {$installCommand} -flag=value nginx wget

# Removal of cache in between installs, only second install should be reported
# Noncompliant@+1
RUN {$commandName} {$installCommand} nginx && rm -rf {$rmLocation} && {$commandName} {$installCommand} wget

# Cleaning of cache in between installs, only second install should be reported
# Noncompliant@+1
RUN {$commandName} {$installCommand} nginx && {$commandName} {$cacheCleanCommand} && {$commandName} {$installCommand} wget

# Cleaning of cache before install
# Noncompliant@+1
RUN {$commandName} {$cacheCleanCommand} && {$commandName} {$installCommand} wget

# Noncompliant@+3
RUN <<EOF
rm -rf {$rmLocation}
{$commandName} {$installCommand} nginx
EOF


# Different ways to remove the cache with wrong flags
# Noncompliant@+1
RUN {$commandName} {$installCommand} nginx && rm -r {$rmLocation}
# Noncompliant@+1
RUN {$commandName} {$installCommand} nginx && rm -f {$rmLocation}
# Noncompliant@+1
RUN {$commandName} {$installCommand} nginx && rm -f --interactive {$rmLocation}
# Noncompliant@+1
RUN {$commandName} {$installCommand} nginx && rm -r --interactive {$rmLocation}
# Noncompliant@+1
RUN {$commandName} {$installCommand} nginx && rm -r --interactive {$rmLocation}
# Noncompliant@+1
RUN {$commandName} {$installCommand} nginx && rm --verbose --interactive --preserve-root=all --no-preserve-root --one-file-system --interactive=never --dir {$rmLocation}

# Noncompliant@+4
RUN <<EOF
{$commandName} {$installCommand} wget
rm -rf {$rmLocation}
{$commandName} {$installCommand} nginx
EOF

# Compliant
RUN {$commandName} {$installCommand} nginx && rm -rf {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm -Rf {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm --force --recursive {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm --force -R {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm -xryfz {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm -fr {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm -xfyrz {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm -r -f {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm -R -f {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm -f -r {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm -x -f -x -r -x {$rmLocation}
RUN {$commandName} {$installCommand} nginx && rm -x -r -y -f -z {$rmLocation}
RUN {$commandName} {$installCommand} nginx && {$commandName} {$cacheCleanCommand}
RUN {$commandName} {$installCommand} nginx && {$commandName} {$installCommand} wget && {$commandName} {$cacheCleanCommand}

RUN <<EOF
{$commandName} {$installCommand} wget
rm -rf {$rmLocation}
{$commandName} {$installCommand} wget
{$commandName} {$cacheCleanCommand}
EOF
