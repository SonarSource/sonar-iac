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

RUN this time the very long instruction is split on multiple lines, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
    aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \

RUN The multi line operator is not counted to check if we are exceding, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
    aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa

# Noncompliant@+2
RUN the very long instruction is on the next line \
    here is the very long instruction to report aaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+2
# Noncompliant@+2
RUN the very long instruction is both on the current and on the next line aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
    as mentionned this is also a very long instruction aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
# ^[sc=1;ec=129]@-1
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^@-1

# Noncompliant@+1
                                                                                                                        RUN a b c d e f g h i j k
#                                                                                                                       ^^^^^^^^^^^^^^^^^^^^^^^^^

# FN: We cannot detect a too long line in the case where there is no spaces befoer/after the multilne operator '\' and on the next line.
# This is because due to the preprocessing, it create a single token starting on the first line and ending on the next line, with a content
# concatenated and without spaces or multiline operator.
# This single token has insufficient data in the TextRange to determine the precise location of any of its characters.
RUN false positive because there is no spaces before and after the continous char and on the next line aaaaaaaaaaaaaaaaaaaaaaaaaa\
aaaaaaaaaaaaaaaaaaaaaaaaaaaaa

# Noncompliant@+1
  RUN the issue location is invalid and ignore the last strings for the sams reasons as stated before: the TextRange data available is insufficient aaaa\
aaa
# ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^@-1

# Noncompliant@+2
RUN we can still compute the right location on the ending line a\
aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa this is the rest of the very long instruction
# ^[sc=1;ec=129]

RUN weDontRaiseIfThere isNotEnoughWords aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa

RUN test \
  && wget -O python.tar.xz.asc "https://www.python.org/ftp/python/${PYTHON_VERSION%%[a-z]*}/Python-$PYTHON_VERSION.tar.xz.asc"

RUN Invoke-WebRequest "https://s3-us-west-2.amazonaws.com/grafana-releases/release/grafana-$($env:GRAFANA_VERSION).windows-amd64.zip"  -OutFile grafana.zip -UseBasicParsing;
