FROM scratch

# FN: shell form not supported in community edition
RUN mysql --user=user --password=secret db_name

# Noncompliant@+1
RUN ["mysql", "--foobar", "--user=user", "-pMySuperPassword", "db_name"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "mysqladmin --user=user --password=$PASSWORD db_name"]
RUN ["sh", "-c", "mysqladmin \"-p$PASSWORD\" db_name"]

# Noncompliant@+1
RUN ["mysqldump", "--user=user", "--password=MySuperPassword", "db_name"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["mysqldump", "-p'my secret'", "db_name"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["mysqldump", "-p'my secret'", "db_name"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
