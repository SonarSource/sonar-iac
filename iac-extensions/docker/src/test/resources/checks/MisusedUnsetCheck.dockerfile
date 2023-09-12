FROM scratch

ENV SECRET_VALUE=password SECRET_USER=admin

# Noncompliant@+1 {{Use the ARG instruction or set & unset environment variable in a single layer.}}
RUN ./script -p $SECRET_VALUE && unset SECRET_VALUE
#                                      ^^^^^^^^^^^^
# Noncompliant@+1
RUN ./script -p $SECRET_VALUE && unset -v SECRET_VALUE
#                                         ^^^^^^^^^^^^
# Noncompliant@+2
# Noncompliant@+1
RUN ./script -p $SECRET_VALUE && unset SECRET_VALUE SECRET_USER
# Noncompliant@+1
RUN ./script -p $SECRET_VALUE && unset LD_LIBRARY_PATH SECRET_USER
#                                                      ^^^^^^^^^^^

RUN export PASSWORD=password && ./script -p $PASSWORD && unset PASSWORD
RUN PASSWORD=password && ./script -p $PASSWORD && unset -v PASSWORD
RUN unset -f foo
