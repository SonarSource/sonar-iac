FROM scratch

# -o flag ====================

# Noncompliant@+1 {{Replace this invocation of "curl" with the ADD instruction.}}
RUN curl -o output.txt https://example.com/resource
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1 {{Replace this invocation of "curl" with the ADD instruction.}}
RUN curl -L -o output.txt -s https://example.com/resource -k && cat output.txt
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^


# Noncompliant@+1
RUN sudo curl -L -o output.txt -s https://example.com/resource -k && cat output.txt
#        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

ENV PHP_URL=https://exmple.com/php

# Noncompliant@+2
RUN if [ -n "$PHP_URL" ]; then \
      curl -o php.tar "$PHP_URL"; \
#     ^^^^^^^^^^^^^^^^^^^^^^^^^^^
	  echo "Success" \
	fi;

# Noncompliant@+1
RUN curl -L -o output.txt https://example.com/resource

# Noncompliant@+1
RUN curl -o output.txt -L https://example.com/resource

# Noncompliant@+1
RUN curl -o output.txt https://example.com/resource -L

# Noncompliant@+1
RUN curl https://example.com/resource -o output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource -o output.txt

# Noncompliant@+1
RUN curl https://example.com/resource -L -o output.txt

# Noncompliant@+1
RUN curl https://example.com/resource -o output.txt -L

# Noncompliant@+1
RUN curl -L https://example.com/resource -s -o output.txt -k

# Noncompliant@+1
RUN curl -L https://example.com/resource -s -o output.txt -k 1> /dev/null

#The short "single-dash" form of the options, -d for example, may be used with or without a space between it

# Noncompliant@+1
RUN curl -ooutput.txt https://example.com/resource
# Noncompliant@+1
RUN curl -L -ooutput.txt -s https://example.com/resource -k && cat output.txt

# Noncompliant@+1
RUN curl -L -ooutput.txt https://example.com/resource

# Noncompliant@+1
RUN curl -ooutput.txt -L https://example.com/resource

# Noncompliant@+1
RUN curl -ooutput.txt https://example.com/resource -L

# Noncompliant@+1
RUN curl https://example.com/resource -ooutput.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource -ooutput.txt

# Noncompliant@+1
RUN curl https://example.com/resource -L -ooutput.txt

# Noncompliant@+1
RUN curl https://example.com/resource -ooutput.txt -L

# Noncompliant@+1
RUN curl -L https://example.com/resource -k -ooutput.txt -s

# Noncompliant@+1
RUN curl -L https://example.com/resource -k -ooutput.txt -s >> /dev/null

# --output flag ==============

# Noncompliant@+1
RUN curl -L --output output.txt https://example.com/resource

# Noncompliant@+1
RUN curl -L --output output.txt https://example.com/resource

# Noncompliant@+1
RUN curl --output output.txt -L https://example.com/resource

# Noncompliant@+1
RUN curl --output output.txt https://example.com/resource -L

# Noncompliant@+1
RUN curl -L --output output.txt -s https://example.com/resource -k

# Noncompliant@+1
RUN curl https://example.com/resource --output output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource --output output.txt

# Noncompliant@+1
RUN curl https://example.com/resource -L --output output.txt

# Noncompliant@+1
RUN curl https://example.com/resource --output output.txt -L

# Noncompliant@+1
RUN curl -L https://example.com/resource -s --output output.txt -k

# Noncompliant@+1
RUN curl -L https://example.com/resource -s --output output.txt -k > /dev/null


# -O flag ====================

# Noncompliant@+1
RUN curl -O https://example.com/resource

# Noncompliant@+1
RUN curl -L -O https://example.com/resource

# Noncompliant@+1
RUN curl -O -L https://example.com/resource

# Noncompliant@+1
RUN curl -O https://example.com/resource -L

# Noncompliant@+1
RUN curl -L -O -s https://example.com/resource -k

# Noncompliant@+1
RUN curl https://example.com/resource -O

# Noncompliant@+1
RUN curl -L https://example.com/resource -O

# Noncompliant@+1
RUN curl https://example.com/resource -L -O

# Noncompliant@+1
RUN curl https://example.com/resource -O -k

# Noncompliant@+1
RUN curl -L https://example.com/resource -k -O -s

# Noncompliant@+1
RUN curl -L -k -s https://example.com/resource -O

# Noncompliant@+1
RUN curl -L -k -s https://example.com/resource -L -k -s -O -L -k -s

# Noncompliant@+1
RUN curl -O https://example.com/resource > /dev/null

# Short version options that do not need any additional values can be used immediately next to each other
# e.g.: options -O, -L and -v at once as -OLv.

# Noncompliant@+1
RUN curl -LOv https://example.com/resource

# Noncompliant@+1
RUN curl -k -LOv https://example.com/resource

# Noncompliant@+1
RUN curl -LOv -k https://example.com/resource

# Noncompliant@+1
RUN curl -LOv https://example.com/resource -k

# Noncompliant@+1
RUN curl -k -LOv -s https://example.com/resource -f

# Noncompliant@+1
RUN curl https://example.com/resource -LOv

# Noncompliant@+1
RUN curl https://example.com/resource -LOv -k

# Noncompliant@+1
RUN curl https://example.com/resource -LOv -k 1>> /dev/null

# --remote-name ==============

# Noncompliant@+1
RUN curl --remote-name https://example.com/resource

# Noncompliant@+1
RUN curl -L --remote-name https://example.com/resource

# Noncompliant@+1
RUN curl --remote-name -L https://example.com/resource

# Noncompliant@+1
RUN curl --remote-name https://example.com/resource -L

# Noncompliant@+1
RUN curl -L --remote-name -s https://example.com/resource -k

# Noncompliant@+1
RUN curl --remote-name https://example.com/resource

# Noncompliant@+1
RUN curl https://example.com/resource --remote-name

# Noncompliant@+1
RUN curl -L https://example.com/resource --remote-name

# Noncompliant@+1
RUN curl https://example.com/resource -L --remote-name

# Noncompliant@+1
RUN curl https://example.com/resource --remote-name -L

# Noncompliant@+1
RUN curl -L https://example.com/resource -k --remote-name -s

# Noncompliant@+1
RUN curl -L -k -s https://example.com/resource --remote-name -L -k -s

# Noncompliant@+1
RUN curl -L -k -s https://example.com/resource --remote-name -L -k -s > log.txt

# --remote-name-all ==========

# Noncompliant@+1
RUN curl --remote-name-all https://example.com/resource

# Noncompliant@+1
RUN curl -L --remote-name-all https://example.com/resource

# Noncompliant@+1
RUN curl --remote-name-all -L https://example.com/resource

# Noncompliant@+1
RUN curl --remote-name-all https://example.com/resource -L

# Noncompliant@+1
RUN curl -L --remote-name-all -s https://example.com/resource -k

# Noncompliant@+1
RUN curl --remote-name-all https://example.com/resource

# Noncompliant@+1
RUN curl -L --remote-name-all https://example.com/resource

# Noncompliant@+1
RUN curl --remote-name-all -L https://example.com/resource

# Noncompliant@+1
RUN curl --remote-name-all https://example.com/resource -L

# Noncompliant@+1
RUN curl -L --remote-name-all -k https://example.com/resource -s

# Noncompliant@+1
RUN curl -L -k -s --remote-name-all -L -k -s  https://example.com/resource -L -k -s

# Noncompliant@+1
RUN curl -L -k -s --remote-name-all -L -k -s  https://example.com/resource -L -k -s >> log.txt

# redirect output ============

# Noncompliant@+1
RUN curl https://example.com/resource > output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource > output.txt

# Noncompliant@+1
RUN curl https://example.com/resource -L > output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource -k > output.txt

# Noncompliant@+1
RUN curl https://example.com/resource >> output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource >> output.txt

# Noncompliant@+1
RUN curl https://example.com/resource -L >> output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource -k >> output.txt

# Noncompliant@+1
RUN curl https://example.com/resource 1> output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource 1> output.txt

# Noncompliant@+1
RUN curl https://example.com/resource -L 1> output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource -k 1> output.txt

# Noncompliant@+1
RUN curl https://example.com/resource 1>> output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource 1>> output.txt

# Noncompliant@+1
RUN curl https://example.com/resource -L 1>> output.txt

# Noncompliant@+1
RUN curl -L https://example.com/resource -k 1>> output.txt

# Noncompliant@+1
RUN ["curl", "-L", "https://example.com/resource", "-s", "--output", "output.txt", "-k"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Compliant: contains auth ==

RUN curl --anyauth -o output.txt https://example.com/resource
RUN curl -o output.txt https://example.com/resource -L --anyauth
RUN curl -L --basic https://example.com/resource -o output.txt
RUN curl https://example.com/resource --basic -o output.txt -L
RUN curl --digest -L https://example.com/resource -s -o output.txt -k

RUN curl -ooutput.txt https://example.com/resource --digest
RUN curl -L --ntlm -ooutput.txt https://example.com/resource
RUN curl -ooutput.txt -L https://example.com/resource --ntlm
RUN curl -L --negotiate https://example.com/resource -ooutput.txt
RUN curl https://example.com/resource -ooutput.txt -L --negotiate

RUN curl -L --proxy-anyauth --output output.txt https://example.com/resource
RUN curl --output output.txt -L https://example.com/resource --proxy-anyauth
RUN curl --proxy-basic -L https://example.com/resource --output output.txt
RUN curl https://example.com/resource --output output.txt -L --proxy-basic

RUN curl -O --proxy-digest https://example.com/resource
RUN curl -O -L https://example.com/resource --proxy-digest
RUN curl -L --proxy-ntlm -O -s https://example.com/resource -k
RUN curl https://example.com/resource -L --proxy-ntlm -O
RUN curl -L --proxy-negotiate -k -s https://example.com/resource -L -k -s -O -L -k -s

RUN curl --proxy-user user:passwd -LOv https://example.com/resource
RUN curl -k -LOv https://example.com/resource --user user
RUN curl --user user -LOv -k https://example.com/resource
RUN curl -LOv https://example.com/resource -k --proxy-negotiate
RUN curl -k -LOv --proxy-negotiate -s https://example.com/resource -f

RUN curl --remote-name https://example.com/resource --proxy-negotiate
RUN curl --user user --remote-name https://example.com/resource -L
RUN curl --remote-name https://example.com/resource --user user
RUN curl -L -u user https://example.com/resource --remote-name
RUN curl https://example.com/resource --remote-name -u user -L
RUN curl -L --oauth2-bearer TOKEN https://example.com/resource -k --remote-name -s

RUN curl --remote-name-all https://example.com/resource --oauth2-bearer TOKEN
RUN curl --proxy-user user:passwd -L --remote-name-all -s https://example.com/resource -k
RUN curl -L --remote-name-all https://example.com/resource --proxy-user user:passwd
RUN curl -U user:password -L --remote-name-all -k https://example.com/resource -s

RUN curl https://example.com/resource -U user:password > output.txt
RUN curl --tlsuser user -L https://example.com/resource > output.txt
RUN curl https://example.com/resource --tlsuser user >> output.txt
RUN curl -L --proxy-tlspassword password https://example.com/resource >> output.txt
RUN curl https://example.com/resource --proxy-tlspassword password 1> output.txt
RUN curl --tlspassword password -L https://example.com/resource -k 1> output.txt
RUN curl -L https://example.com/resource --tlspassword password 1>> output.txt
RUN curl --proxy-tlspassword password -L https://example.com/resource -k 1>> output.txt
RUN curl -L https://example.com/resource --proxy-tlspassword password -k 1>> output.txt

RUN curl -L --proxy-tlsuser user -o output.txt https://example.com/resource
RUN curl -o output.txt -L https://example.com/resource --proxy-tlsuser user
RUN curl -o output.txt --tlsuser name https://example.com/resource -L
RUN curl https://example.com/resource -o output.txt --tlsuser name
RUN curl -b cookies.txt -L https://example.com/resource -o output.txt
RUN curl https://example.com/resource -L -o output.txt -b cookies.txt
RUN curl --cookie cookies.txt https://example.com/resource -o output.txt -L
RUN curl -L https://example.com/resource --cookie cookies.txt -s -o output.txt -k

RUN curl -L -c cookies.txt --output output.txt https://example.com/resource
RUN curl -L --output output.txt https://example.com/resource -c cookies.txt
RUN curl --cookie-jar cookies.txt --output output.txt -L https://example.com/resource
RUN curl --output output.txt --cookie-jar cookies.txt https://example.com/resource -L

RUN curl -H "Authorization: Bearer token" -L --output output.txt -s https://example.com/resource -k
RUN curl https://example.com/resource --output output.txt -H "Authorization: Bearer token"
RUN curl --header "Authorization: Bearer token" -L https://example.com/resource --output output.txt
RUN curl https://example.com/resource -L --header "Authorization: Bearer token" --output output.txt
RUN curl -H "X-Auth-Token 123" https://example.com/resource --output output.txt -L
RUN curl -L https://example.com/resource -s --output output.txt -k -H "X-Auth-Token 123"
RUN curl --header "X-Auth-Token 123" https://example.com/resource --output output.txt -L
RUN curl -L https://example.com/resource -s --output output.txt --header "X-Auth-Token 123" -k

# Compliant: ADD doesn’t support HTTP request options =======

RUN curl -o output.txt https://example.com/resource --data "param1=value1"
RUN curl --data "param1=value1" -o output.txt https://example.com/resource
RUN curl -o output.txt --data "param1=value1" https://example.com/resource
RUN curl -o output.txt --data "param1=value1" https://example.com/resource --data "param2=value2"
RUN curl -o output.txt https://example.com/resource -d "param1=value1"
RUN curl -o output.txt https://example.com/resource --data-ascii @file
RUN curl -o output.txt https://example.com/resource --data-binary @file
RUN curl -o output.txt https://example.com/resource --data-raw "param1=value1"
RUN curl -o output.txt https://example.com/resource --data-urlencode param1=value1
RUN curl -o output.txt https://example.com/resource --form param1=value1
RUN curl -o output.txt https://example.com/resource -F param1=value1
RUN curl -o output.txt https://example.com/resource --form-escape -F 'param1=value1'
RUN curl -o output.txt https://example.com/resource --form-string "param1=value1"
RUN curl -o output.txt https://example.com/resource --header "Content-Type: application/json"
RUN curl -o output.txt https://example.com/resource -H "Content-Type: application/json"
RUN curl -o output.txt https://example.com/resource --json '{"param1": "value1"}'
RUN curl -o output.txt https://example.com/resource --referer "https://example.com/referer"
RUN curl -o output.txt https://example.com/resource -e "https://example.com/referer"
RUN curl -o output.txt https://example.com/resource --request POST
RUN curl -o output.txt https://example.com/resource -X POST
RUN curl -o output.txt https://example.com/resource --user-agent "Mozilla/5.0"
RUN curl -o output.txt https://example.com/resource -A "Mozilla/5.0"

# Compliant: save in /dev/null
RUN curl https://example.com/resource > /dev/null
RUN curl -L https://example.com/resource >> /dev/null
RUN curl -L https://example.com/resource -k 1> /dev/null
RUN curl -L https://example.com/resource -k 1>> /dev/null

# Compliant: curl is part of a command chain and relies on previous commands
RUN VERSION=$(cat version.txt) && curl -o output.txt https://example.com/resource/$VERSION
RUN VERSION=1.2.3 && curl -o output.txt https://example.com/resource/$VERSION
RUN curl -o output.txt https://example.com/resource/$VERSION

ARG RESOURCE_VERSION=1.2.3
# Noncompliant@+1
RUN curl -o output.txt https://example.com/resource/$RESOURCE_VERSION
