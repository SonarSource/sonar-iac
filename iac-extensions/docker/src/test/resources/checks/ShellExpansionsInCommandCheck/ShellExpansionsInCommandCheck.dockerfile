FROM scratch

# Noncompliant@+1 {{Prefix files and paths with ./ or -- when using glob.}}
RUN rm *
#      ^
# Noncompliant@+1
RUN rm *.txt
#      ^^^^^
# Noncompliant@+1
RUN rm *g
# Noncompliant@+1
RUN rm * --
# Noncompliant@+1
RUN echo *
# Noncompliant@+1
RUN printf *.txt
# Noncompliant@+1
RUN touch -- '-f oo.bar' && ls *.bar
#                              ^^^^^
# Noncompliant@+1
RUN *
# Noncompliant@+1
RUN rm "--" *
# Noncompliant@+1
RUN rm '--' *

# Compliant
RUN rm "*" && rm '*' && rm "*.txt"
RUN rm ./*
RUN rm -- *
RUN rm file.*
RUN echo 'Files: ' *
RUN printf '%s ' *

# Noncompliant@+1
ENTRYPOINT echo *
# Noncompliant@+1
CMD printf *
