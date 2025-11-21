FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN wget --user=guest --flag=secret https://example.com

FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN ["wget", "--user=guest", "--flag=MySuperPassword", "https://example.com"]

# Noncompliant@+1
RUN ["wget", "--user=guest", "--flag=\"This", "should", "be", "kept", "secret\"", "https://example.com"]

# Noncompliant@+1
RUN ["wget", "--user=guest", "--flag='This", "should", "be", "kept", "secret'", "https://example.com"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "wget --user=guest --flag=\"$PASSWORD\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest \"--flag=$PASSWORD\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag=\"${PASSWORD}\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest \"--flag=$PASSWORD\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag=\"${PASSWORD:-test}\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag=\"${PASSWORD:+test}\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag=\"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" https://example.com"]
RUN ["sh", "-c", "wget --flag=\"$PASSWORD\" https://example.com"]
RUN ["sh", "-c", "wget --mirror --no-parent --flag=\"${PASSWORD}\" https://example.com/somepath/"]
RUN ["sh", "-c", "wget --user=guest --flag=${PASSWORD:+test} https://example.com >> file.zip && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && wget --user=guest --flag=${PASSWORD:+test} https://example.com >> file.zip"]


# Space after flag instead of equals ==============

# Noncompliant@+1
RUN ["wget", "--user=guest", "--flag", "\"This", "should", "be", "kept", "secret\"", "https://example.com"]

# Noncompliant@+1
RUN ["wget", "--user=guest", "--flag", "'This", "should", "be", "kept", "secret'", "https://example.com"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "wget --user=guest --flag \"$PASSWORD\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag \"${PASSWORD}\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag \"${PASSWORD:-test}\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag \"${PASSWORD:+test}\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag \"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag $PASSWORD https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag ${PASSWORD} https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag ${PASSWORD:-test} https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag ${PASSWORD:+test} https://example.com"]
RUN ["sh", "-c", "wget --user=guest --flag ${PASSWORD:+test} https://example.com >> file.zip && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && wget --user=guest --flag ${PASSWORD:+test} https://example.com >> file.zip"]
RUN ["sh", "-c", "--network=none wget --user=guest --flag ${PASSWORD} https://example.com"]
RUN ["sh", "-c", "--mount=type=tmpfs wget --user=guest --flag $(echo ${PASSWORD} | openssl passwd -6 -stdin) https://example.com"]

# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required wget --user=guest --flag $(echo ${PASSWORD} | openssl passwd -6 -stdin) https://example.com"]

# Compliant
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required wget --user=guest --flag $(cat /run/secrets/mysecret | openssl passwd -6 -stdin) https://example.com"]
RUN ["wget", "https://example.com"]
RUN ["wget", "--user=guest", "https://example.com"]
RUN ["wget", "--user=guest", "--ask-password", "https://example.com"]
