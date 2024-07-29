FROM sratch

# -O flag first ====

# Noncompliant@+1 {{Replace this invocation of wget with the ADD instruction.}}
RUN wget -O /path/to/resource https://example.com/resource
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1 {{Replace this invocation of wget with the ADD instruction.}}
RUN wget -O /path/to/resource https://example.com/resource --limit-rate=100k && wget https://example.com/
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

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

# Noncompliant@+1
RUN wget -O - https://example.com/resource > download-redirect2.txt

# URL first and -O flag =======

# Noncompliant@+1
RUN wget https://example.com/resource -O /path/to/resource

# Noncompliant@+1
RUN wget https://example.com/resource --max-redirect=1 -O /path/to/resource

# Noncompliant@+1
RUN wget --limit-rate=100k https://example.com/resource --max-redirect=1 -O /path/to/resource

# Noncompliant@+1
RUN wget --limit-rate=100k https://example.com/resource -O /path/to/resource --max-redirect=1

# --output-document= or --output-document first ===========

# Noncompliant@+1
RUN wget --output-document=/path/to/resource https://example.com/resource

# Noncompliant@+1
RUN wget --limit-rate=100k --output-document=/path/to/resource https://example.com/resource

# Noncompliant@+1
RUN wget --output-document=/path/to/resource --limit-rate=100k https://example.com/resource

# Noncompliant@+1
RUN wget --output-document=/path/to/resource https://example.com/resource --limit-rate=100k

# Noncompliant@+1
RUN wget --no-check-certificate --output-document=/path/to/resource --max-redirect=1 https://example.com/resource --limit-rate=100k

# Noncompliant@+1
RUN wget --no-check-certificate --output-document=/path/to/resource --max-redirect=1 https://example.com/resource \
    --limit-rate=100k | apt-key add - && echo "success"

# Noncompliant@+1
RUN wget --output-document - https://example.com/resource > output.txt

# Noncompliant@+1
RUN wget --output-document downloaded4.txt https://example.com/resource

# URL first and --output-document= or --output-document ===========
# Noncompliant@+1
RUN wget https://example.com/resource --output-document=/path/to/resource

# Noncompliant@+1
RUN wget --limit-rate=100k https://example.com/resource --output-document=/path/to/resource

# Noncompliant@+1
RUN wget https://example.com/resource --limit-rate=100k --output-document=/path/to/resource

# Noncompliant@+1
RUN wget https://example.com/resource --output-document=/path/to/resource --limit-rate=100k

# Noncompliant@+1
RUN wget --limit-rate=100k https://example.com/resource --max-redirect=1 --output-document=/path/to/resource

# Noncompliant@+1
RUN wget --limit-rate=100k https://example.com/resource --max-redirect=1 --output-document=/path/to/resource --no-check-certificate

# TODO --load-cookies
# TODO auth headers

# Compliant because ADD doesn’t support authentication =======

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
RUN wget -O /path/to/resource https://example.com/resource --load-cookies=cookies.txt
RUN wget --load-cookies=cookies.txt -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource --load-cookies=cookies.txt https://example.com/resource

RUN wget --output-document /path/to/resource https://example.com/resource --http-user=user
RUN wget --output-document /path/to/resource https://example.com/resource --http-user=user --http-password=password
RUN wget --output-document /path/to/resource --http-user=user https://example.com/resource
RUN wget --output-document /path/to/resource --http-password=password https://example.com/resource
RUN wget --output-document /path/to/resource https://example.com/resource --http-password=password
RUN wget --output-document /path/to/resource https://example.com/resource --proxy-password=password
RUN wget --output-document /path/to/resource https://example.com/resource --proxy-user=user
RUN wget --output-document /path/to/resource https://example.com/resource --proxy-user=user --proxy-password=password
RUN wget --output-document /path/to/resource https://example.com/resource --proxy-user user --proxy-password password
RUN wget --proxy-user user --proxy-password password --output-document /path/to/resource https://example.com/resource
RUN wget --output-document=/path/to/resource --proxy-user user --proxy-password password https://example.com/resource
RUN wget --output-document /path/to/resource https://example.com/resource --load-cookies=cookies.txt
RUN wget --load-cookies=cookies.txt --output-document /path/to/resource https://example.com/resource
RUN wget --output-document /path/to/resource --load-cookies=cookies.txt https://example.com/resource

# Compliant no file save
RUN wget https://example.com/resource > file.html
