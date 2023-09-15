FROM scratch

# Noncompliant@+1
RUN mysql --password=MySuperPassword db_name
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN mysql --foobar --user=user -pMySuperPassword db_name
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN mysqladmin --user=user --password=$PASSWORD db_name
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN mysqladmin "-p$PASSWORD" db_name
#   ^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN mysqldump --user=user --password=MySuperPassword db_name \
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  unzip file.zip

# Noncompliant@+1
RUN mysqldump -p'my secret' db_name
#   ^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["mysqldump", "-p'my secret'", "db_name"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
