ARG SETTINGS
ARG SETTINGS_2="--default-settings"

FROM busybox
# Noncompliant@+1 {{Include the ARG instruction in the build stage where it is used.}}
RUN ./run/setup $SETTINGS
#               ^^^^^^^^^

FROM busybox
ARG SETTINGS
RUN ./run/setup $SETTINGS

FROM busybox
ARG SETTINGS_2
RUN ./run/setup $SETTINGS_2

FROM busybox
ARG SETTINGS_2
# Noncompliant@+1 {{Include the ARG instruction in the build stage where it is used.}}
RUN ./run/setup $SETTINGS $SETTINGS_2
#               ^^^^^^^^^

# Variables are neither global nor stage-local ARGs
FROM busybox
RUN ./run/setup $PATH
RUN ./run/setup $env:PATH
RUN SETUP_PATH=$PATH && ./run/setup $SETUP_PATH
