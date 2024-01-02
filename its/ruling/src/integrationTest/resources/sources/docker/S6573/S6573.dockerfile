FROM scratch

RUN rm -rf *
RUN tar xfv *.tar

ARG FOO
# Compliant - rule shouldn't be checking asterisks inside a quoted string
RUN echo "* * * * * echo $FOO; ./cron-job" >> /etc/crontabs
RUN echo "rm *" >> ~/.forbidden-commands

RUN rm -rf -- *
RUN tar xfv ./*.tar
