FROM scratch

# Noncompliant@+1 {{Remove this CMD instruction which will be ignored.}}
  CMD myCommand
# ^^^^^^^^^^^^^

# Noncompliant@+1 {{Remove this ENTRYPOINT instruction which will be ignored.}}
  ENTRYPOINT myEntrypoint
# ^^^^^^^^^^^^^^^^^^^^^^^

FROM scratch
RUN run1
RUN run2
# Noncompliant@+1
CMD cmd

# Compliants
CMD lastCmd
ENTRYPOINT lastEntrypoint
