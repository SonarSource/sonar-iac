FROM scratch

# Non compliant test cases
# FN: shell form not supported in community edition
RUN ./exec $test

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "./exec $test"]
RUN ["sh", "-c", "my random command $test with value"]
RUN ["sh", "-c", "$test$val"]
RUN ["sh", "-c", "$test\"other\""]
RUN ["sh", "-c", "del -$flag"]

RUN <<EOT
  $test some value
EOT


# Compliant test cases
RUN ["sh", "-c", "\"$test\""]
RUN ["sh", "-c", "./exec \"$test\""]
RUN ["sh", "-c", "my random command \"$test\" with value"]
RUN ["sh", "-c", "'$test'"]
RUN ["sh", "-c", "./exec '$test'"]
RUN ["sh", "-c", "my random command '$test' with value"]
RUN ["$test"]


# Other instructions supported by the rule
CMD ["sh", "-c", "$test"]
ENTRYPOINT $test


# Safe check on unsupported instructions
COPY $test


# Compliant because the original variable definition is enclosed in double quotes
ARG my_var_arg="my_value"
ENTRYPOINT $my_var_arg

ENV my_var_env="my_value"
ENTRYPOINT $my_var_env

ARG no_end_quote="my_value"ishere
ENTRYPOINT $no_end_quote

ARG no_start_quote=ishere"my_value"
ENTRYPOINT $no_start_quote
