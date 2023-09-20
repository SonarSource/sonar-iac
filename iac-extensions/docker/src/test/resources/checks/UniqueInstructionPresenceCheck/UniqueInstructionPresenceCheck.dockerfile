FROM scratch

# Noncompliant@+1 {{Remove this CMD instruction which will be ignored.}}
  CMD myCommand
# ^^^^^^^^^^^^^

# Noncompliant@+1 {{Remove this ENTRYPOINT instruction which will be ignored.}}
  ENTRYPOINT myEntrypoint
# ^^^^^^^^^^^^^^^^^^^^^^^

CMD lastCommand
ENTRYPOINT lastEntrypoint

FROM scratch
RUN run1
RUN run2

FROM scratch
CMD onlyCommand
ENTRYPOINT onlyEntrypoint
