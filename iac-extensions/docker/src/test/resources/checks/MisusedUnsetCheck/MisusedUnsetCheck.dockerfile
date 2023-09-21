FROM scratch

ENV SECRET_VALUE=password SECRET_USER=admin
ARG SECRET_TOKEN=0x42f42f42f

# Noncompliant@+1 {{Use the ARG instruction or set and unset the environment variable in a single layer.}}
RUN ./script -p $SECRET_VALUE && unset SECRET_VALUE
#                                      ^^^^^^^^^^^^
# Noncompliant@+1
RUN ./script -p $SECRET_VALUE && sudo unset SECRET_VALUE
# Noncompliant@+1
RUN ./script -p $SECRET_VALUE && unset -v SECRET_VALUE
#                                         ^^^^^^^^^^^^
# Noncompliant@+1 2
RUN ./script -p $SECRET_VALUE && unset SECRET_VALUE SECRET_USER
# Noncompliant@+1
RUN ./script -p $SECRET_VALUE && unset LD_LIBRARY_PATH SECRET_USER
#                                                      ^^^^^^^^^^^
# Noncompliant@+4
RUN <<EOF
#!/usr/bin/env bash
./script -p $SECRET_VALUE
unset SECRET_VALUE
EOF

RUN ./script -t $SECRET_TOKEN && unset SECRET_TOKEN
RUN ./script -t $SECRET_TOKEN && sudo unset SECRET_TOKEN
RUN export PASSWORD=password && ./script -p $PASSWORD && unset PASSWORD
RUN PASSWORD=password && ./script -p $PASSWORD && unset -v PASSWORD
RUN unset -f foo
RUN ./script && unset -f foo && ./post-script
RUN unset PASSWORD$FOO PASSWORD${FOO}
RUN ["unset", "PASSWORD$FOO", "PASSWORD${FOO}"]
