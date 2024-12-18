FROM scratch

# Non compliant test cases
# Noncompliant@+1 {{Surround this variable with double quotes; otherwise, it can lead to unexpected behavior.}}
RUN $test
#   ^^^^^

# Noncompliant@+1
RUN ./exec $test
# Noncompliant@+1
RUN my random command $test with value
# Noncompliant@+1 2
RUN $test$val
# Noncompliant@+1
RUN $test"other"
#   ^^^^^
# Noncompliant@+2
RUN <<EOT
  $test some value
# ^^^^^
EOT
# Noncompliant@+1
RUN del -$flag
#        ^^^^^


# Compliant test cases
RUN "$test"
RUN ./exec "$test"
RUN my random command "$test" with value
RUN '$test'
RUN ./exec '$test'
RUN my random command '$test' with value
RUN ["$test"]


# Other instructions supported by the rule
# Noncompliant@+1
CMD $test
# Noncompliant@+1
ENTRYPOINT $test


# Safe check on unsupported instructions
COPY $test


# Compliant because the original variable definition is enclosed in double quotes
ARG my_var_arg="my_value"
RUN $my_var_arg

ENV my_var_env="my_value"
RUN $my_var_env

ARG no_end_quote="my_value"ishere
# Noncompliant@+1
RUN $no_end_quote

ARG no_start_quote=ishere"my_value"
# Noncompliant@+1
RUN $no_start_quote
