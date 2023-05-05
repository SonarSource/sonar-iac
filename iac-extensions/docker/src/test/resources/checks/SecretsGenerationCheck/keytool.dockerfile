FROM ubuntu:22.04


# Noncompliant@+1 {{Change this code not to store a secret in the image.}}
RUN keytool -genseckey -noprompt -alias tomcat -keyalg RSA -keystore /usr/local/tomcat/.keystore -storepass changeit -keypass changeit -dname "CN=Lyngby, OU=ILoop, O=CFB, L=Christian, S=Ravn, C=DK"
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN keytool -random1 -genkeypair -random2

# Noncompliant@+1
RUN keytool -random1 opt1 -gencert -random2

# Noncompliant@+1
RUN keytool -random1 opt1 -genkey -random2

# Noncompliant@+1
RUN keytool -random1 -random2 opt2 -genkey -random3 opt3

# Noncompliant@+1
RUN keytool -random1 -random2 opt2 -genkey -random3 opt3 -random4

# Noncompliant@+1
RUN keytool -random1 -random2 opt2 -genkey -random3 -random4 opt4

# Noncompliant@+1
RUN apt install wget && keytool -genkey
#                       ^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN ["keytool", "-genseckey", "-random1"]
#    ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+2
RUN <<EOF
  keytool -genseckey -random1
EOF

RUN keytool -noprompt -alias tomcat -keyalg RSA -keystore /usr/local/tomcat/.keystore -storepass changeit -keypass changeit -dname "CN=Lyngby, OU=ILoop, O=CFB, L=Christian, S=Ravn, C=DK"

RUN keytool -random1 && unrelated_command -genkeypair

RUN keytool -random1 && -genkey

RUN foobar
