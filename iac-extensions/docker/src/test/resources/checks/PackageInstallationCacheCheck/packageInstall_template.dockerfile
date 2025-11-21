FROM scratch

# FN: shell form not supported in community edition
RUN {$commandName} {$installCommand} nginx

# Noncompliant@+1
RUN ["sudo", "{$commandName}", "{$installCommand}", "nginx"]

# FNs: exec form with explicit shell cal not supported
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && {$commandName} {$installCommand} wget"]
RUN ["sh", "-c", "someOtherCommand && {$commandName} {$installCommand} nginx && otherCommand"]

# Noncompliant@+1
RUN ["{$commandName}", "-flag", "{$installCommand}", "-flag", "nginx", "wget"]

# Noncompliant@+1
RUN ["{$commandName}", "-flag=value", "{$installCommand}", "-flag=value", "nginx", "wget"]

# Removal of cache in between installs, only second install should be reported
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -rf {$rmLocation} && {$commandName} {$installCommand} wget"]

# Cleaning of cache in between installs, only second install should be reported
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && {$commandName} {$cacheCleanCommand} && {$commandName} {$installCommand} wget"]

# Cleaning of cache before install
RUN ["sh", "-c", "{$commandName} {$cacheCleanCommand} && {$commandName} {$installCommand} wget"]
RUN ["sh", "-c", "rm -rf {$rmLocation}\n{$commandName} {$installCommand} nginx"]


# Different ways to remove the cache with wrong flags
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -r {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -f {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -f --interactive {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -r --interactive {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -r --interactive {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm --verbose --interactive --preserve-root=all --no-preserve-root --one-file-system --interactive=never --dir {$rmLocation}"]

RUN ["sh", "-c", "{$commandName} {$installCommand} wget\nrm -rf {$rmLocation}\n{$commandName} {$installCommand} nginx"]

# Compliant
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -rf {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -Rf {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm --force --recursive {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm --force -R {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -xryfz {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -fr {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -xfyrz {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -r -f {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -R -f {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -f -r {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -x -f -x -r -x {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && rm -x -r -y -f -z {$rmLocation}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && {$commandName} {$cacheCleanCommand}"]
RUN ["sh", "-c", "{$commandName} {$installCommand} nginx && {$commandName} {$installCommand} wget && {$commandName} {$cacheCleanCommand}"]

RUN ["sh", "-c", "{$commandName} {$installCommand} wget\nrm -rf {$rmLocation}\n{$commandName} {$installCommand} wget\n{$commandName} {$cacheCleanCommand}"]
