FROM ubuntu:22.04 as build

# no issue in non final stage
RUN x11vnc -storepasswd path/to/file


FROM ubuntu:22.04

ARG FILE_PATH

# Noncompliant@+1
RUN x11vnc -storepasswd
#   ^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN x11vnc -storepasswd path/to/file
#   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Noncompliant@+1
RUN x11vnc -storepasswd "path/to/file"

# Noncompliant@+1
RUN x11vnc -storepasswd 'path/to/file'

# Noncompliant@+1
RUN x11vnc -storepasswd "$FILE_PATH"

# Noncompliant@+1
RUN x11vnc -storepasswd $FILE_PATH

# Noncompliant@+1
RUN sudo x11vnc -storepasswd $FILE_PATH

# Noncompliant@+1
RUN x11vnc -storepasswd "${FILE_PATH}"

# Noncompliant@+1
RUN x11vnc -storepasswd "${FILE_PATH:-test}"

# Noncompliant@+1
RUN x11vnc -storepasswd "${FILE_PATH:+test}"

# Noncompliant@+1
RUN x11vnc -storepasswd "$(echo ${FILE_PATH} | openssl passwd -6 -stdin)"

# Noncompliant@+1
RUN x11vnc -forever -viewonly -storepasswd "${FILE_PATH}"

# Noncompliant@+1
RUN x11vnc -storepasswd "${FILE_PATH}" -forever -viewonly

# Noncompliant@+1
RUN x11vnc -storepasswd "${FILE_PATH}" && \
    unzip file.zip

# Noncompliant@+2
RUN cd /tmp && \
    x11vnc -storepasswd "${FILE_PATH}"

# Compliant
RUN x11vnc -shared
RUN x11vnc -forever -viewonly
RUN sudo x11vnc -forever -viewonly
RUN x11vnc -display :0.1
