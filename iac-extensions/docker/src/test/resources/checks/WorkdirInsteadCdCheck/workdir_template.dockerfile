FROM scratch

# FN: shell form not supported in community edition
{$instructionName} cd /app/bin && ./start.sh

# FNs: exec form with explicit shell invocation are not supported
{$instructionName} ["sh", "-c", "unzip foobar.zip && cd foobar"]
{$instructionName} ["sh", "-c", "cd /tmp && unzip foobar.zip"]
{$instructionName} ["sh", "-c", "cd -L /tmp && unzip foobar.zip"]
{$instructionName} ["sh", "-c", "cd -L /tmp&& unzip foobar.zip"]
{$instructionName} ["sh", "-c", "cd -L /tmp & unzip foobar.zip"]
{$instructionName} ["sh", "-c", "cd -L /tmp || unzip foobar.zip"]
{$instructionName} ["sh", "-c", "cd -L /tmp | unzip foobar.zip"]
{$instructionName} ["sh", "-c", "cd -L /tmp ; unzip foobar.zip"]
{$instructionName} ["sh", "-c", "cd \"foo bar dir\" ; unzip foobar.zip"]
{$instructionName} ["sh", "-c", "cd 'foo bar dir' ; unzip foobar.zip"]
{$instructionName} ["sh", "-c", "cd /tmp && git clone https://github.com/SonarSource/sonarqube.git"]
{$instructionName} ["sh", "-c", "unzip foobar.zip && cd foobar"]
{$instructionName} ["sh", "-c", "cd /tmp && git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube"]
{$instructionName} ["sh", "-c", "cd /tmp && git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube"]
{$instructionName} ["sh", "-c", "cd /tool && make && cd /bin && bash install.sh"]
{$instructionName} ["sh", "-c", "cd /tmp && /entrypoint.sh"]
{$instructionName} ["sh", "-c", "/entrypoint.sh && cd /tmp"]

# Noncompliant@+1
{$instructionName} ["cd", "/app/bin"]

# Noncompliant@+1
{$instructionName} ["cd", ".."]


# TODO SONARIAC-1088 FP Command detector can't detect that `cd` is not first command
# Noncompliant@+1
{$instructionName} ["mkdir", "cd", "foo"]


# Compliant
{$instructionName}

# The cd command is in the midle
{$instructionName} ["sh", "-c", "git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube && ./start.sh"]

{$instructionName} ["sh", "-c", "git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube && ./start.sh"]

{$instructionName} ["mkdir", "cd"]

{$instructionName} ["sh", "-c", "/entrypoint.sh && cd /tmp && run.sh"]

{$instructionName} ["sh", "-c", "executable && cd foo && parameters"]
