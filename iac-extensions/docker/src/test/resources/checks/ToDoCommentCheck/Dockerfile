# Noncompliant@+1
# TODO fix me
FROM ubuntu

# todo is compliant
MAINTAINER bob

# Noncompliant@+1
#foo TODO bar
ADD a.jar /foo

# Noncompliant@+1
# TODOfix
USER bob

# Noncompliant@+1
#TODO
MAINTAINER bob

RUN apk update && \
	apk add --no-cache \
    # Noncompliant@+1
    # TODO clean up
	bash \
	gcc \
    # foo bar
	git
