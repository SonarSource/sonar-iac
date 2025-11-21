ARG BUSYBOX_TAG="latest"
ARG SETTINGS
ARG SETTINGS_2="--default-settings"
ARG USER=builder

ARG FIRST_ARG="test"
ARG SECOND_ARG=${FIRST_ARG}

FROM busybox:$BUSYBOX_TAG
VOLUME ./run/setup

FROM busybox
# Noncompliant@+1 {{Include the ARG instruction in the build stage where it is used.}}
VOLUME ./run/setup $SETTINGS
#                  ^^^^^^^^^

FROM busybox
ARG SETTINGS
VOLUME ./run/setup $SETTINGS

FROM busybox
# Ensure that the Symbol table is not mixing this variable symbol in VOLUME with the one redeclared at in the previous stage in ARG.
# Noncompliant@+1
VOLUME ./run/setup $SETTINGS

FROM busybox
ARG SETTINGS_2
VOLUME ./run/setup $SETTINGS_2

FROM busybox
ARG SETTINGS_2
# Noncompliant@+1 {{Include the ARG instruction in the build stage where it is used.}}
VOLUME ./run/setup $SETTINGS $SETTINGS_2
#                  ^^^^^^^^^

FROM busybox
# Noncompliant@+1 {{Include the ARG instruction in the build stage where it is used.}}
USER $USER
#    ^^^^^
VOLUME ./run/setup

# Variables are neither global nor stage-local ARGs
FROM busybox
VOLUME ./run/setup $PATH
VOLUME ./run/setup $env:PATH
VOLUME SETUP_PATH=$PATH && ./run/setup $SETUP_PATH
