FROM scratch

# Compliant
RUN this is a short instruction that is totally compliant\
a

# Noncompliant@+1 {{Split this RUN instruction line into multiple lines.}}
RUN this is a very long run instruction on y single line, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaa
# ^[sc=1;ec=129]

# Noncompliant@+1
RUN --an=issue --is=also --raised=when --it=is --about=options --aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=a
# ^[sc=1;ec=129]

RUN --but=weNeed --toHave=enoughOptions --for=this --aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa=a

# Noncompliant@+1
RUN lot of spaces on same line that is not compliant                                                                            a
# ^[sc=1;ec=129]

# Noncompliant@+4
RUN this time the very long instruction is split on multiple lines, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
    aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \

RUN The multi line operator is correctly counted to check if we are exceding, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
    aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa

# Noncompliant@+2
RUN the very long instruction is on the next line \
    here is the very long instruction to report aaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+2
# Noncompliant@+2
RUN the very long instruction is both on the current and on the next line aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
    as mentionned this is also a very long instruction aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
# ^[sc=1;ec=131]@-1
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^@-1

# Noncompliant@+1
                                                                                                                        RUN a b c d e f g h i j k
#                                                                                                                       ^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN totally a true positive despite there are no spaces before and after the backslash and on the next line aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\
aaaaaaaaaaaaaaaaaaaaaaaaaaaaa

# Noncompliant@+1
  RUN the issue location is invalid and ignore the last strings for the same reasons as stated before: the TextRange data available is insufficient aaaa\
aaa
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^@-1

# Noncompliant@+2
RUN we can still compute the right location on the ending line a\
aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa this is the rest of the very long instruction
# ^[sc=1;ec=129]

RUN we cannot rise on intermediary lines if the token start and end of the next line a\
aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\
aaaaaa

RUN weDontRaiseIfThere isNotEnoughWords aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa

RUN test \
  && wget -O python.tar.xz.asc "https://www.python.org/ftp/python/${PYTHON_VERSION%%[a-z]*}/Python-$PYTHON_VERSION.tar.xz.asc"

RUN Invoke-WebRequest "https://s3-us-west-2.amazonaws.com/grafana-releases/release/grafana-$($env:GRAFANA_VERSION).windows-amd64.zip"  -OutFile grafana.zip -UseBasicParsing;

# Compliant: contain URLs
RUN wget -O python.tar.xz.asc -firstFlag -secondFlag -thirdFlag -fourthFlag -fifthFlag "https://www.python.org/ftp/python/3.12.0/Python-3.12.0.tar.xz.asc"
RUN wget -O python.tar.xz.asc -firstFlag -secondFlag -thirdFlag -fourthFlag -fifthFlag "https://www.python.org:8080/ftp/python/3.12.0/Python-3.12.0.tar.xz.asc?key=value"
RUN wget -O python.tar.xz.asc -firstFlag -secondFlag -thirdFlag -fourthFlag -fifthFlag "http://www.python.org/ftp/python/3.12.0/Python-3.12.0.tar.xz.asc"
RUN wget -O python.tar.xz.asc -firstFlag -secondFlag -thirdFlag -fourthFlag -fifthFlag "ftp://www.python.org/ftp/python/3.12.0/Python-3.12.0.tar.xz.asc"
RUN wget -O file.txt -firstFlag -secondFlag -thirdFlag -fourthFlag -fifthFlag -sixthFlag -seventhFlag -eighthFlag "file:///path/to/your/file.txt"
RUN wget -O python.tar.xz.asc -firstFlag -secondFlag -thirdFlag "https://www.python.org/ftp/python/${PYTHON_VERSION%%[a-z]*}/Python-$PYTHON_VERSION.tar.xz.asc"
RUN wget -O python.tar.xz.asc -firstFlag -secondFlag -thirdFlag "https://www.python.org/ftp/python/${PYTHON_VERSION%%[a-z]*} \
    /Python-$PYTHON_VERSION.tar.xz.asc" -fourthFlag -fifthFlag -sixthFlag -seventhFlag -eighthFlag -ninthFlag -tenthFlag -eleventhFlag \
# Noncompliant@-1
RUN wget -O python.tar.xz.asc -firstFlag -secondFlag -thirdFlag "https://www.python.org/ftp/python/${PYTHON_VERSION%%[a-z]*/Python-$PYTHON_VERSION.tar.xz.asc}" \
    && -fourthFlag -fifthFlag -sixthFlag -seventhFlag -eighthFlag -ninthFlag -tenthFlag -eleventhFlag -twelfthFlag -thirteenthFlag $variable
# NonCompliant@-1

# FP, URL not detected
# Noncompliant@+1
RUN wget -O python.tar.xz.asc -firstFlag -secondFlag -thirdFlag -fourthFlag -fifthFlag "www.python.org/ftp/python/3.12.0/Python-3.12.0.tar.xz.asc"

# Noncompliant@+3
RUN \
  echo First line && \
  echo We should correctly raise on this long line even when there is a line break right after the RUN keyword. aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa && \
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
  echo Last line

# Noncompliant@+3
RUN \
\
echo Should correctly highlight a long line directly after an empty line. aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa && \
xx-verify --static /dockerfile-frontend

# Noncompliant@+2
RUN <<EOT
echo Should correctly highlight a long line at the beginning of a heredoc. aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
xx-verify --static /dockerfile-frontend
EOT