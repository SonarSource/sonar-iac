FROM sratch

# Noncompliant@+1 {{Replace this invocation of wget with the ADD instruction.}}
RUN wget -O /path/to/resource https://example.com/resource
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN wget --max-redirect=1 -O /path/to/resource https://example.com/resource

# The space between the option accepting an argument and the argument may be omitted.
# Noncompliant@+1
RUN wget -Odownloaded.txt https://example.com/resource

# Noncompliant@+1
RUN wget -O /path/to/resource --max-redirect=1 https://example.com/resource

# Noncompliant@+1
RUN wget -O /path/to/resource --max-redirect=1 -r --tries=10 https://example.com/resource

# Noncompliant@+1
RUN wget -O /path/to/resource https://example.com/resource --max-redirect=1

RUN wget https://example.com/resource -O /path/to/resource

#Compliant because ADD doesn’t support authentication
RUN wget -O /path/to/resource https://example.com/resource --http-user=user
RUN wget -O /path/to/resource https://example.com/resource --http-user=user --http-password=password
RUN wget -O /path/to/resource --http-user=user https://example.com/resource
RUN wget -O /path/to/resource --http-password=password https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --http-password=password

RUN wget -O /path/to/resource https://example.com/resource --proxy-password=password
RUN wget -O /path/to/resource https://example.com/resource --proxy-user=user
RUN wget -O /path/to/resource https://example.com/resource --proxy-user=user --proxy-password=password
RUN wget -O /path/to/resource https://example.com/resource --proxy-user user --proxy-password password
RUN wget --proxy-user user --proxy-password password -O /path/to/resource https://example.com/resource
RUN wget -O/path/to/resource --proxy-user user --proxy-password password https://example.com/resource
#RUN wget -O /path/to/resource https://example.com/resource
