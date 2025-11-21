FROM ubuntu:22.04 as build

# no issue in non final stage
# FN: shell form not supported in community edition
RUN x11vnc -storepasswd path/to/file

FROM ubuntu:22.04

ARG FILE_PATH

# Noncompliant@+1
RUN ["x11vnc", "-storepasswd"]

# Noncompliant@+1
RUN ["x11vnc", "-storepasswd", "path/to/file"]

# Noncompliant@+1
RUN ["x11vnc", "-storepasswd", "path/to/file"]

# Noncompliant@+1
RUN ["x11vnc", "-storepasswd", "path/to/file"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "x11vnc -storepasswd \"$FILE_PATH\""]
RUN ["sh", "-c", "x11vnc -storepasswd $FILE_PATH"]
RUN ["sh", "-c", "sudo x11vnc -storepasswd $FILE_PATH"]
RUN ["sh", "-c", "x11vnc -storepasswd \"${FILE_PATH}\""]
RUN ["sh", "-c", "x11vnc -storepasswd \"${FILE_PATH:-test}\""]
RUN ["sh", "-c", "x11vnc -storepasswd \"${FILE_PATH:+test}\""]
RUN ["sh", "-c", "x11vnc -storepasswd \"$(echo ${FILE_PATH} | openssl passwd -6 -stdin)\""]
RUN ["sh", "-c", "x11vnc -storepasswd \"${FILE_PATH}\" && unzip file.zip"]
RUN ["sh", "-c", "cd /tmp && x11vnc -storepasswd \"${FILE_PATH}\""]

# Noncompliant@+1
RUN ["x11vnc", "-forever", "-viewonly", "-storepasswd", "${FILE_PATH}"]

# Noncompliant@+1
RUN ["x11vnc", "-storepasswd", "${FILE_PATH}", "-forever", "-viewonly"]

# Compliant
RUN ["x11vnc", "-shared"]
RUN ["x11vnc", "-forever", "-viewonly"]
RUN ["sudo", "x11vnc", "-forever", "-viewonly"]
RUN ["x11vnc", "-display", ":0.1"]
