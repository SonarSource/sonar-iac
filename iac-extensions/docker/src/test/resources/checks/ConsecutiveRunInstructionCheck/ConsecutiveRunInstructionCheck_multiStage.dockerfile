FROM ubuntu:latest AS build
# Noncompliant@+1 {{Merge this RUN instruction with the consecutive ones.}}
RUN apt-get install -y curl
# ^[sc=1;ec=3]
RUN apt-get install -y nginx
# ^[sc=1;ec=3]< {{consecutive RUN instruction}}

FROM ubuntu:latest AS other
# Compliant, not part of the final image
RUN apt-get install -y curl
RUN apt-get install -y nginx

FROM build AS final

# Noncompliant@+1 {{Merge this RUN instruction with the consecutive ones.}}
RUN apt-get install -y curl
# ^[sc=1;ec=3]
RUN apt-get install -y nginx
# ^[sc=1;ec=3]< {{consecutive RUN instruction}}
