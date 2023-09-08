FROM scratch

# Noncompliant@+1 {{WORKDIR instruction should be used instead of cd command.}}
ENTRYPOINT cd /app/bin && ./start.sh
#          ^^^^^^^^^^^

# Noncompliant@+1
ENTRYPOINT unzip foobar.zip && cd foobar
#                              ^^^^^^^^^

# Noncompliant@+1
ENTRYPOINT cd /tmp && unzip foobar.zip

# Noncompliant@+1
ENTRYPOINT cd -L /tmp && unzip foobar.zip

# Noncompliant@+1
ENTRYPOINT cd -L /tmp&& unzip foobar.zip

# Noncompliant@+1
ENTRYPOINT cd -L /tmp & unzip foobar.zip

# Noncompliant@+1
ENTRYPOINT cd -L /tmp || unzip foobar.zip

# Noncompliant@+1
ENTRYPOINT cd -L /tmp | unzip foobar.zip

# Noncompliant@+1
ENTRYPOINT cd -L /tmp ; unzip foobar.zip

# Noncompliant@+1
ENTRYPOINT cd "foo bar dir" ; unzip foobar.zip

# Noncompliant@+1
ENTRYPOINT cd 'foo bar dir' ; unzip foobar.zip

# Noncompliant@+1
ENTRYPOINT cd /tmp && \
  git clone https://github.com/SonarSource/sonarqube.git

# Noncompliant@+2
ENTRYPOINT unzip foobar.zip && \
  cd foobar
# ^^^^^^^^^

# Noncompliant@+2
# Noncompliant@+1
ENTRYPOINT cd /tmp && git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube

# Noncompliant@+1
ENTRYPOINT cd /tmp && \
  git clone https://github.com/SonarSource/sonarqube.git && \
  cd sonarqube
# Noncompliant@-1

# Noncompliant@+1
ENTRYPOINT cd /tool && \
  make && \
  cd /bin && \
  bash install.sh

# Noncompliant@+1
ENTRYPOINT ["cd", "/tmp", "&&", "/entrypoint.sh"]
#           ^^^^^^^^^^^^

# Noncompliant@+1
ENTRYPOINT ["/entrypoint.sh", "&&", "cd", "/tmp"]
#                                   ^^^^^^^^^^^^


# TODO SONARIAC-1088 FP Command detector can't detect that `cd` is not first command
# Noncompliant@+1
ENTRYPOINT mkdir cd foo


# Compliant
ENTRYPOINT

# The cd command is in the midle
ENTRYPOINT git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube && ./start.sh

ENTRYPOINT git clone https://github.com/SonarSource/sonarqube.git && \
  cd sonarqube && \
  ./start.sh

ENTRYPOINT mkdir cd

ENTRYPOINT ["/entrypoint.sh", "&&", "cd", "/tmp", "&&", "run.sh"]

ENTRYPOINT executable && \
# com
  cd foo && \
# com
  parameters
