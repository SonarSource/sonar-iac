FROM scratch

# Noncompliant@+1 {{WORKDIR instruction should be used instead of cd command.}}
CMD cd /app/bin && ./start.sh
#   ^^^^^^^^^^^

# Noncompliant@+1
CMD unzip foobar.zip && cd foobar
#                       ^^^^^^^^^

# Noncompliant@+1
CMD cd /app/bin

# Noncompliant@+1
CMD cd ..

# Noncompliant@+1
CMD cd /tmp && unzip foobar.zip

# Noncompliant@+1
CMD cd -L /tmp && unzip foobar.zip

# Noncompliant@+1
CMD cd -L /tmp&& unzip foobar.zip

# Noncompliant@+1
CMD cd -L /tmp & unzip foobar.zip

# Noncompliant@+1
CMD cd -L /tmp || unzip foobar.zip

# Noncompliant@+1
CMD cd -L /tmp | unzip foobar.zip

# Noncompliant@+1
CMD cd -L /tmp ; unzip foobar.zip

# Noncompliant@+1
CMD cd "foo bar dir" ; unzip foobar.zip

# Noncompliant@+1
CMD cd 'foo bar dir' ; unzip foobar.zip

# Noncompliant@+1
CMD cd /tmp && \
  git clone https://github.com/SonarSource/sonarqube.git

# Noncompliant@+2
CMD unzip foobar.zip && \
  cd foobar
# ^^^^^^^^^

# Noncompliant@+2
# Noncompliant@+1
CMD cd /tmp && git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube

# Noncompliant@+1
CMD cd /tmp && \
  git clone https://github.com/SonarSource/sonarqube.git && \
  cd sonarqube
# Noncompliant@-1

# Noncompliant@+1
CMD cd /tool && \
  make && \
  cd /bin && \
  bash install.sh

# Noncompliant@+1
CMD ["cd", "/tmp", "&&", "/entrypoint.sh"]
#    ^^^^^^^^^^^^

# Noncompliant@+1
CMD ["/entrypoint.sh", "&&", "cd", "/tmp"]
#                            ^^^^^^^^^^^^


# TODO SONARIAC-1088 FP Command detector can't detect that `cd` is not first command
# Noncompliant@+1
CMD mkdir cd foo


# Compliant
CMD

# The cd command is in the midle
CMD git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube && ./start.sh

CMD git clone https://github.com/SonarSource/sonarqube.git && \
  cd sonarqube && \
  ./start.sh

CMD mkdir cd

CMD ["/entrypoint.sh", "&&", "cd", "/tmp", "&&", "run.sh"]

CMD executable && \
# com
  cd foo && \
# com
  parameters
