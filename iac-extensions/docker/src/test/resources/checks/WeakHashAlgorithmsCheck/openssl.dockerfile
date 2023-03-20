FROM scratch

## All sensitive openssl subcommands
# Noncompliant@+1 {{Using weak hashing algorithms is security-sensitive.}}
  RUN openssl md5 test.txt
#     ^^^^^^^^^^^
# Noncompliant@+1
RUN openssl sha1 test.txt
# Noncompliant@+1
RUN openssl rmd160 test.txt
# Noncompliant@+1
RUN openssl ripemd160 test.txt

## All sensitive dgst subcommand options
# Noncompliant@+1
RUN openssl dgst -md2 test.txt
# Noncompliant@+1
RUN openssl dgst -md4 test.txt
# Noncompliant@+1
RUN openssl dgst -md5 test.txt
# Noncompliant@+1
RUN openssl dgst -sha1 test.txt
# Noncompliant@+1
RUN openssl dgst -ripemd160 test.txt
# Noncompliant@+1
RUN openssl dgst -ripemd test.txt
# Noncompliant@+1
RUN openssl dgst -rmd160 test.txt

## Other valid sensitive usecase
# Noncompliant@+1
RUN openssl dgst -md2 -c test.txt
# Noncompliant@+1
  RUN openssl dgst -c -md2 test.txt
#     ^^^^^^^^^^^^^^^^^^^^
# FN : no support for flag with argument before the sensitive algorithms flag
RUN openssl dgst -out out.txt -md2 test.txt
# FN : no support for flag with argument before the sensitive algorithms flag
RUN openssl dgst -rand rand1.txt rand2.txt -md2 test.txt
# Noncompliant@+1
RUN something openssl dgst -md2 test.txt
# Noncompliant@+1
RUN other command; openssl md5 test.txt
# Noncompliant@+1 2
  RUN openssl md5 test.txt openssl md5 test.txt

## Other formats
# Noncompliant@+1
  RUN ["openssl", "dgst", "-md2", "-c", "test.txt"]
#      ^^^^^^^^^^^^^^^^^^^^^^^^^
# Noncompliant@+1
RUN <<-EOF openssl dgst -md2 test.txt
  some value
EOF
# Noncompliant@+2
RUN <<-EOF
  openssl dgst -md2 test.txt
EOF

## Multiple instruction in a single RUN instruction
# Noncompliant@+1
  RUN openssl md5 test.txt && other command
#     ^^^^^^^^^^^
# Noncompliant@+1
  RUN other command && openssl md5 test.txt
#                      ^^^^^^^^^^^
# Noncompliant@+1
RUN openssl md5 test.txt &&
# Noncompliant@+1
RUN && openssl md5 test.txt
# Noncompliant@+1
RUN openssl md5 test.txt | other command
# Noncompliant@+1
RUN openssl md5 test.txt ; other command
# Noncompliant@+1
RUN other command; openssl md5 test.txt

## Use case compliant
RUN openssl
RUN openssl test.txt -md2 dgst
RUN openssl dgst test.txt -md2
RUN openssl dgst -md2=thing test.txt
RUN openssl dgst --md2 test.txt
RUN openssl -md2 dgst test.txt
RUN openssl something dgst -md2 test.txt
RUN openssl digest -md2 test.txt
RUN openssl && digest && -md2 && test.txt
RUN openssl dgst md2 test.txt
# FN : we don't support shell/bash/powershell languages, we only try to parse/extract commands
RUN if ! [ "`openssl md5 test.txt | cut -d' ' -f2`" = "effc956eca1ae6e85e06670ca8e16e72" ]; then exit 1; fi
RUN openssl sha256 test.txt
RUN openssl test.txt
RUN ["openssl", "dgst", "-sha256", "test.txt"]
RUN <<-EOF
  openssl dgst -sha256 test.txt
EOF
