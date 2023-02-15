FROM ubuntu:latest as jammy
# Noncompliant@+1 {{Make sure that using clear-text protocols is safe here.}}
RUN curl http://www.httpvshttps.com/
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^
RUN curl https://www.httpvshttps.com/
# Noncompliant@+1
RUN curl ftp://www.httpvshttps.com/
# Noncompliant@+1
RUN curl HTTP://www.httpvshttps.com/
RUN curl ftps://www.httpvshttps.com/
RUN curl http://
RUN curl fpt://
RUN curl ssh://foo-bar
RUN curl http://localhost
RUN curl http://localhost/my_folder/
RUN curl http://127.0.0.1
RUN curl http://127.0.0.1/my_folder/
RUN curl http://LOCALHOST
RUN curl http://0:0:0:0:0:0:0:1
# Noncompliant@+1
RUN curl http://0:0:0:0:0:0:0:42
RUN curl http://::1
# Noncompliant@+1
RUN curl http://1:1:1:1:1:1:1:1
# Noncompliant@+1
RUN curl http://10:20:30:40:50:60:70:80

# Noncompliant@+1
ENTRYPOINT command http://www.httpvshttps.com/
# Noncompliant@+1
CMD ["command", "http://www.httpvshttps.com/"]
# Noncompliant@+1
ADD hom* "http://www.httpvshttps.com/"
# Noncompliant@+1
ADD "http://www.httpvshttps.com/" /foo/
# Noncompliant@+1 2
ADD "http://www.httpvshttps.com/" "http://www.other.com/" /bar/

COPY "http://www.httpvshttps.com/" /bar/

RUN ls
