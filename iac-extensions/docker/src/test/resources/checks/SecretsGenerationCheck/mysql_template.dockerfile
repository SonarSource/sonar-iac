FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN mysql --user=root --password=secret

FROM ubuntu:22.04

ARG PASSWORD

# Noncompliant@+1
RUN ["mysql", "--user=user", "--password=MySuperPassword", "db_name"]

# Noncompliant@+1
RUN ["mysql", "--user=user", "--password=\"This", "should", "be", "kept", "secret\"", "db_name"]

# Noncompliant@+1
RUN ["mysql", "--user=user", "--password='This", "should", "be", "kept", "secret'", "db_name"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "mysql --user=user --password=\"$PASSWORD\" db_name"]
RUN ["sh", "-c", "mysql --user=user --password=$PASSWORD db_name"]
RUN ["sh", "-c", "mysql --user=user \"--password=$PASSWORD\" db_name"]
RUN ["sh", "-c", "mysql --user=user --password=\"${PASSWORD}\" db_name"]
RUN ["sh", "-c", "mysql --user=user --password=\"${PASSWORD:-test}\" db_name"]
RUN ["sh", "-c", "mysql --user=user --password=\"${PASSWORD:+test}\" db_name"]
RUN ["sh", "-c", "mysql --user=user --password=\"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" db_name"]
RUN ["sh", "-c", "mysql -h database_host -e \"source filename.sql\" --user=user --password=\"${PASSWORD}\" db_name"]
RUN ["sh", "-c", "mysql --password=\"${PASSWORD}\" -h database_host -e \"source filename.sql\" --user=user db_name"]
RUN ["sh", "-c", "mysql --user=user --password=\"${PASSWORD}\" db_name && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && mysql --user=user --password=\"${PASSWORD}\" db_name"]


# Short flag version ==============

# Noncompliant@+1
RUN ["mysql", "--user=user", "-pMySuperPassword", "db_name"]

# Noncompliant@+1
RUN ["mysql", "--user=user", "-p\"This", "should", "be", "kept", "secret\"", "db_name"]

# Noncompliant@+1
RUN ["mysql", "--user=user", "-p'This", "should", "be", "kept", "secret'", "db_name"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "mysql --user=user -p\"$PASSWORD\" db_name"]
RUN ["sh", "-c", "mysql --user=user -p$PASSWORD db_name"]
RUN ["sh", "-c", "mysql --user=user \"-p$PASSWORD\" db_name"]
RUN ["sh", "-c", "mysql --user=user -p\"${PASSWORD}\" db_name"]
RUN ["sh", "-c", "mysql --user=user -p\"${PASSWORD:-test}\" db_name"]
RUN ["sh", "-c", "mysql --user=user -p\"${PASSWORD:+test}\" db_name"]
RUN ["sh", "-c", "mysql --user=user -p\"$(echo ${PASSWORD} | openssl passwd -6 -stdin)\" db_name"]
RUN ["sh", "-c", "mysql -h database_host -e \"source filename.sql\" --user=user -p\"${PASSWORD}\" db_name"]
RUN ["sh", "-c", "mysql -p\"${PASSWORD}\" -h database_host -e \"source filename.sql\" --user=user db_name"]
RUN ["sh", "-c", "mysql --user=user -p\"${PASSWORD}\" db_name && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && mysql --user=user -p\"${PASSWORD}\" db_name"]


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required mysql --user=user -p$(echo ${PASSWORD} | openssl passwd -6 -stdin) db_name"]

# Compliant
RUN ["sh", "-c", "--mount=type=secret mysql --user=user -p$(cat /run/secrets/mysecret | openssl passwd -6 -stdin) db_name"]
RUN ["sh", "-c", "--mount=type=secret,id=mysecret,required mysql --user=user -p$(cat /run/secrets/mysecret | openssl passwd -6 -stdin) db_name"]

# If you omit the password value following the --password or -p option on the command line, it prompts for one.
RUN ["mysql", "--password", "db_name"]
RUN ["mysql", "-p", "db_name"]

RUN ["mysql", "--user=user", "db_name"]
RUN ["mysql", "db_name"]
RUN ["mysql", "-h", "database_host", "database_name"]
RUN ["mysql", "-e", "\"source", "filename.sql\"", "database_name"]
