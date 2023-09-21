FROM scratch

# Noncompliant@+1 {{WORKDIR instruction should be used instead of cd command.}}
{$instructionName} cd /app/bin && ./start.sh

# Noncompliant@+1
{$instructionName} unzip foobar.zip && cd foobar

# Noncompliant@+1
{$instructionName} cd /app/bin

# Noncompliant@+1
{$instructionName} cd ..

# Noncompliant@+1
{$instructionName} cd /tmp && unzip foobar.zip

# Noncompliant@+1
{$instructionName} cd -L /tmp && unzip foobar.zip

# Noncompliant@+1
{$instructionName} cd -L /tmp&& unzip foobar.zip

# Noncompliant@+1
{$instructionName} cd -L /tmp & unzip foobar.zip

# Noncompliant@+1
{$instructionName} cd -L /tmp || unzip foobar.zip

# Noncompliant@+1
{$instructionName} cd -L /tmp | unzip foobar.zip

# Noncompliant@+1
{$instructionName} cd -L /tmp ; unzip foobar.zip

# Noncompliant@+1
{$instructionName} cd "foo bar dir" ; unzip foobar.zip

# Noncompliant@+1
{$instructionName} cd 'foo bar dir' ; unzip foobar.zip

# Noncompliant@+1
{$instructionName} cd /tmp && \
  git clone https://github.com/SonarSource/sonarqube.git

# Noncompliant@+2
{$instructionName} unzip foobar.zip && \
  cd foobar
# ^^^^^^^^^

# Noncompliant@+2
# Noncompliant@+1
{$instructionName} cd /tmp && git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube

# Noncompliant@+1
{$instructionName} cd /tmp && \
  git clone https://github.com/SonarSource/sonarqube.git && \
  cd sonarqube
# Noncompliant@-1

# Noncompliant@+1
{$instructionName} cd /tool && \
  make && \
  cd /bin && \
  bash install.sh

# Noncompliant@+1
{$instructionName} ["cd", "/tmp", "&&", "/entrypoint.sh"]

# Noncompliant@+1
{$instructionName} ["/entrypoint.sh", "&&", "cd", "/tmp"]


# TODO SONARIAC-1088 FP Command detector can't detect that `cd` is not first command
# Noncompliant@+1
{$instructionName} mkdir cd foo


# Compliant
{$instructionName}

# The cd command is in the midle
{$instructionName} git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube && ./start.sh

{$instructionName} git clone https://github.com/SonarSource/sonarqube.git && \
  cd sonarqube && \
  ./start.sh

{$instructionName} mkdir cd

{$instructionName} ["/entrypoint.sh", "&&", "cd", "/tmp", "&&", "run.sh"]

{$instructionName} executable && \
# com
  cd foo && \
# com
  parameters
