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
