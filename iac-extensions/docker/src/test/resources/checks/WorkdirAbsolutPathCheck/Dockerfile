FROM scratch

WORKDIR /absolute/path/unix
WORKDIR "/absolute/path/unix"

WORKDIR C:/absolute/path/windows
WORKDIR "C:/absolute/path/windows"

WORKDIR C:\\absolute\\path\\windows
WORKDIR "C:\\absolute\\path\\windows"

# Noncompliant@+1 {{Use an absolute path instead of this relative path when defining the WORKDIR.}}
WORKDIR relative/path
#       ^^^^^^^^^^^^^

ARG MY_ABSOLUTE_PATH=/my/absolute/path
ARG MY_RELATIVE_PATH=my/relative/path

WORKDIR $MY_ABSOLUTE_PATH

# Noncompliant@+1
WORKDIR $MY_RELATIVE_PATH
#       ^^^^^^^^^^^^^^^^^

WORKDIR $UNKNOWN

WORKDIR /

# Noncompliant@+1
WORKDIR \\path
#       ^^^^^^

# Noncompliant@+1
WORKDIR .
#       ^

ONBUILD WORKDIR /absolute/path

# Noncompliant@+1
ONBUILD WORKDIR relative/path
#               ^^^^^^^^^^^^^


# Noncompliant@+1
WORKDIR not:/a/windows/absolute/path

WORKDIR ""

WORKDIR ~
WORKDIR ~user/some/path
WORKDIR ~/some/path

WORKDIR %temp%
WORKDIR "%SystemRoot%\System32"
WORKDIR "%CommonProgramFiles(x86)%"

# Noncompliant@+1
WORKDIR "%CommonProgramFiles(x87)%"

# Noncompliant@+1
WORKDIR "%notEndingInTilde"

# Noncompliant@+1
WORKDIR "notStartingWithTilde%"

