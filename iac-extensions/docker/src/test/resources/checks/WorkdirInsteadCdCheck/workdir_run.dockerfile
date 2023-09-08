FROM scratch

# Noncompliant@+1 {{WORKDIR instruction should be used instead of cd command.}}
RUN cd /app/bin && ./start.sh
#   ^^^^^^^^^^^

# Noncompliant@+1
RUN unzip foobar.zip && cd foobar
#                       ^^^^^^^^^

# Noncompliant@+1
RUN cd /tmp && unzip foobar.zip

# Noncompliant@+1
RUN cd -L /tmp && unzip foobar.zip

# Noncompliant@+1
RUN cd -L /tmp&& unzip foobar.zip

# Noncompliant@+1
RUN cd -L /tmp & unzip foobar.zip

# Noncompliant@+1
RUN cd -L /tmp || unzip foobar.zip

# Noncompliant@+1
RUN cd -L /tmp | unzip foobar.zip

# Noncompliant@+1
RUN cd -L /tmp ; unzip foobar.zip

# Noncompliant@+1
RUN cd "foo bar dir" ; unzip foobar.zip

# Noncompliant@+1
RUN cd 'foo bar dir' ; unzip foobar.zip

# Noncompliant@+1
RUN cd /tmp && \
  git clone https://github.com/SonarSource/sonarqube.git

# Noncompliant@+2
RUN unzip foobar.zip && \
  cd foobar
# ^^^^^^^^^

# Noncompliant@+2
# Noncompliant@+1
RUN cd /tmp && git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube

# Noncompliant@+1
RUN cd /tmp && \
  git clone https://github.com/SonarSource/sonarqube.git && \
  cd sonarqube
# Noncompliant@-1

# Noncompliant@+1
RUN cd /tool && \
  make && \
  cd /bin && \
  bash install.sh

# Noncompliant@+1
RUN ["cd", "/tmp", "&&", "/entrypoint.sh"]
#    ^^^^^^^^^^^^

# Noncompliant@+1
RUN ["/entrypoint.sh", "&&", "cd", "/tmp"]
#                            ^^^^^^^^^^^^

# Noncompliant@+2
RUN <<"EOT"
  cd foo/bar
EOT


# TODO SONARIAC-1088 FP Command detector can't detect that `cd` is not first command
# Noncompliant@+1
RUN mkdir cd foo


# Compliant
RUN

# The cd command is in the midle
RUN git clone https://github.com/SonarSource/sonarqube.git && cd sonarqube && ./start.sh

RUN git clone https://github.com/SonarSource/sonarqube.git && \
  cd sonarqube && \
  ./start.sh

RUN mkdir cd

RUN ["/entrypoint.sh", "&&", "cd", "/tmp", "&&", "run.sh"]

RUN executable && \
# com
  cd foo && \
# com
  parameters
