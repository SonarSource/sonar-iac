FROM ubuntu:22.04 as build

# no issue in non final stage
RUN mysql --user=user --password=password db_name


FROM ubuntu:22.04

ARG PASSWORD

#tmp
# Noncompliant@+1
RUN mysql --user=user "-p$PASSWORD" db_name

# Noncompliant@+1
RUN mysql --user=user --password=MySuperPassword db_name

# Noncompliant@+1
RUN mysql --user=user --password="This should be kept secret" db_name

# Noncompliant@+1
RUN mysql --user=user --password='This should be kept secret' db_name

# Noncompliant@+1
RUN mysql --user=user --password="$PASSWORD" db_name

# Noncompliant@+1
RUN mysql --user=user --password=$PASSWORD db_name

# Noncompliant@+1
RUN mysql --user=user "--password=$PASSWORD" db_name

# Noncompliant@+1
RUN mysql --user=user --password="${PASSWORD}" db_name

# Noncompliant@+1
RUN mysql --user=user --password="${PASSWORD:-test}" db_name

# Noncompliant@+1
RUN mysql --user=user --password="${PASSWORD:+test}" db_name

# Noncompliant@+1
RUN mysql --user=user --password="$(echo ${PASSWORD} | openssl passwd -6 -stdin)" db_name

# Noncompliant@+1
RUN mysql -h database_host -e "source filename.sql" --user=user --password="${PASSWORD}" db_name

# Noncompliant@+1
RUN mysql --password="${PASSWORD}" -h database_host -e "source filename.sql" --user=user db_name


# Noncompliant@+1
RUN mysql --user=user --password="${PASSWORD}" db_name && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    mysql --user=user --password="${PASSWORD}" db_name


# Short flag version ==============

# Noncompliant@+1
RUN mysql --user=user -pMySuperPassword db_name

# Noncompliant@+1
RUN mysql --user=user -p"This should be kept secret" db_name

# Noncompliant@+1
RUN mysql --user=user -p'This should be kept secret' db_name

# Noncompliant@+1
RUN mysql --user=user -p"$PASSWORD" db_name

# Noncompliant@+1
RUN mysql --user=user -p$PASSWORD db_name

# Noncompliant@+1
RUN mysql --user=user "-p$PASSWORD" db_name

# Noncompliant@+1
RUN mysql --user=user -p"${PASSWORD}" db_name

# Noncompliant@+1
RUN mysql --user=user -p"${PASSWORD:-test}" db_name

# Noncompliant@+1
RUN mysql --user=user -p"${PASSWORD:+test}" db_name

# Noncompliant@+1
RUN mysql --user=user -p"$(echo ${PASSWORD} | openssl passwd -6 -stdin)" db_name

# Noncompliant@+1
RUN mysql -h database_host -e "source filename.sql" --user=user -p"${PASSWORD}" db_name

# Noncompliant@+1
RUN mysql -p"${PASSWORD}" -h database_host -e "source filename.sql" --user=user db_name

# Noncompliant@+1
RUN mysql --user=user -p"${PASSWORD}" db_name && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    mysql --user=user -p"${PASSWORD}" db_name


# FN It is misuse of --mount=type=secret, but for now detection in sub shell is not possible
RUN --mount=type=secret,id=mysecret,required mysql --user=user -p$(echo ${PASSWORD} | openssl passwd -6 -stdin) db_name

# Compliant
RUN --mount=type=secret mysql --user=user -p$(cat /run/secrets/mysecret | openssl passwd -6 -stdin) db_name
RUN --mount=type=secret,id=mysecret,required mysql --user=user -p$(cat /run/secrets/mysecret | openssl passwd -6 -stdin) db_name

# If you omit the password value following the --password or -p option on the command line, it prompts for one.
RUN mysql --password db_name
RUN mysql -p db_name

RUN mysql --user=user db_name
RUN mysql db_name
RUN mysql -h database_host database_name
RUN mysql -e "source filename.sql" database_name
