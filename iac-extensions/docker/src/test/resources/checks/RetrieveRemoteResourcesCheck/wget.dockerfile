FROM scratch

# -O flag first ====

# Noncompliant@+1 {{Replace this invocation of "wget" with the ADD instruction.}}
RUN wget -O /path/to/resource https://example.com/resource
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1 {{Replace this invocation of "wget" with the ADD instruction.}}
RUN wget -O /path/to/resource https://example.com/resource --limit-rate=100k && wget https://example.com/
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1 {{Replace this invocation of "wget" with the ADD instruction.}}
RUN sudo wget -O /path/to/resource https://example.com/resource --limit-rate=100k && wget https://example.com/
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

ENV PHP_URL=https://exmple.com/php

# Noncompliant@+2
RUN if [ -n "$PHP_URL" ]; then \
      wget -O php.tar "$PHP_URL"; \
#     ^^^^^^^^^^^^^^^^^^^^^^^^^^^
	  echo "Success" \
	fi;

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

# Compliant: ADD doesn’t support authentication =======

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

RUN wget --header="Authorization: Bearer token" -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource  --header="Authorization: Bearer token" https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --header="Authorization: Bearer token"
RUN wget --limit-rate=100k -O /path/to/resource https://example.com/resource --header="Authorization: Bearer token"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --header="Authorization: Bearer token"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --max-redirect=1 --header="Authorization: Bearer token"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --max-redirect=1 --header="Authorization: Bearer token" --quiet

RUN wget --header="X-Auth-Token 123" -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource  --header="X-Auth-Token 123" https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --header="X-Auth-Token 123"
RUN wget --limit-rate=100k -O /path/to/resource https://example.com/resource --header="X-Auth-Token 123"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --header="X-Auth-Token 123"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --max-redirect=1 --header="X-Auth-Token 123"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --max-redirect=1 --header="X-Auth-Token 123" --quiet

RUN wget --header "Authorization: Bearer token" -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource  --header "Authorization: Bearer token" https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --header "Authorization: Bearer token"
RUN wget --limit-rate=100k -O /path/to/resource https://example.com/resource --header "Authorization: Bearer token"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --header "Authorization: Bearer token"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --max-redirect=1 --header "Authorization: Bearer token"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --max-redirect=1 --header "Authorization: Bearer token" --quiet

RUN wget --header "X-Auth-Token 123" -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource  --header "X-Auth-Token 123" https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --header "X-Auth-Token 123"
RUN wget --limit-rate=100k -O /path/to/resource https://example.com/resource --header "X-Auth-Token 123"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --header "X-Auth-Token 123"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --max-redirect=1 --header "X-Auth-Token 123"
RUN wget --limit-rate=100k -O /path/to/resource --no-check-certificate https://example.com/resource --max-redirect=1 --header "X-Auth-Token 123" --quiet

# Compliant: ADD doesn’t support HTTP request options =======

RUN wget --header="Content-Type: application/json" -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource --header="Content-Type: application/json" https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --header="Content-Type: application/json"

RUN wget --method=POST -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource --method=POST https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --method=POST

RUN wget --body-data='{"key":"value"}' -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource --body-data='{"key":"value"}' https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --body-data='{"key":"value"}'

RUN wget --referer=https://example.com/referer -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource --referer=https://example.com/referer https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --referer=https://example.com/referer

RUN wget --save-headers -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource --save-headers https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --save-headers

RUN wget --user-agent="Mozilla/5.0" -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource --user-agent="Mozilla/5.0" https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --user-agent="Mozilla/5.0"

RUN wget -U "Mozilla/5.0" -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource -U "Mozilla/5.0" https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource -U "Mozilla/5.0"

RUN wget --post-data="key=value" -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource --post-data="key=value" https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --post-data="key=value"

RUN wget --post-file=file.txt -O /path/to/resource https://example.com/resource
RUN wget -O /path/to/resource --post-file=file.txt https://example.com/resource
RUN wget -O /path/to/resource https://example.com/resource --post-file=file.txt

# Compliant: no file save
RUN wget https://example.com/resource > file.html

