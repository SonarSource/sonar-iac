ARG BUSYBOX_TAG="latest"
ARG SETTINGS
ARG SETTINGS_2="--default-settings"
ARG USER=builder

FROM busybox:$BUSYBOX_TAG
RUN ./run/setup

FROM busybox
# Noncompliant@+1 {{Include the ARG instruction in the build stage where it is used.}}
RUN ./run/setup $SETTINGS
#               ^^^^^^^^^

FROM busybox
ARG SETTINGS
RUN ./run/setup $SETTINGS

FROM busybox
# Ensure that the Symbol table is not mixing this variable symbol in RUN with the one redeclared at in the previous stage in ARG.
# Noncompliant@+1
RUN ./run/setup $SETTINGS

FROM busybox
ARG SETTINGS_2
RUN ./run/setup $SETTINGS_2

FROM busybox
ARG SETTINGS_2
# Noncompliant@+1 {{Include the ARG instruction in the build stage where it is used.}}
RUN ./run/setup $SETTINGS $SETTINGS_2
#               ^^^^^^^^^

FROM busybox
# Noncompliant@+1 {{Include the ARG instruction in the build stage where it is used.}}
USER $USER
#    ^^^^^
RUN ./run/setup

# Variables are neither global nor stage-local ARGs
FROM busybox
RUN ./run/setup $PATH
RUN ./run/setup $env:PATH
RUN SETUP_PATH=$PATH && ./run/setup $SETUP_PATH
