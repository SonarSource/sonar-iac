FROM scratch
# Noncompliant@+1 {{Do not use host operating system namespaces.}}
  RUN --network=host command
#     ^^^^^^^^^^^^^^
# Noncompliant@+1
RUN --other=option --network=host command
# Noncompliant@+1
RUN --network=\
host command

RUN --network=other command
RUN --network= command
RUN --network command
RUN command
RUN --NETWORK=host command
RUN --network=HOST command
