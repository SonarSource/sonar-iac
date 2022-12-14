  FROM test

# Compliants test cases
  RUN --mount=type=secret,id=build_secret ./installer.sh
  RUN --mount=type=secret,id=build_secret,mode=0640,required ./installer.sh
  RUN --mount=type=ssh ./installer.sh
  RUN --mount=type=ssh,mode=0640 ./installer.sh
  RUN --mount=type=common,id=build_secret,mode=0666 ./installer.sh

# Non compliants test cases
# Noncompliant@+1 {{Remove world permissions for this sensitive file.}}
  RUN --mount=type=secret,id=build_secret,mode=0666 ./installer.sh
#                                              ^^^^

# Noncompliant@+1 {{Remove world permissions for this sensitive agent.}}
  RUN --mount=type=ssh,mode=7 ./installer.sh
#                           ^

# Noncompliant@+1
  RUN --mount=id=build_secret,required,mode=0666,type=secret ./installer.sh


# Other values
  RUN --mount=type,id=build_secret,mode=0666 ./installer.sh
  RUN --mount=type=secret,id=build_secret,mode ./installer.sh
  RUN --mount=type,id=build_secret,mode ./installer.sh
  RUN --mount=type=secret,id=build_secret,mode=word ./installer.sh
  RUN --mount=id=build_secret,mode=word ./installer.sh
  RUN --mount=type=secret,id=build_secret ./installer.sh
  RUN --mount=id=build_secret ./installer.sh
  RUN --MOUNT=type=secret,id=build_secret,mode=0666 ./installer.sh
  RUN ./installer.sh
