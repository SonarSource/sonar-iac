# Noncompliant@+1 {{Scratch images run as root by default. Make sure it is safe here.}}
  FROM scratch
# ^^^^^^^^^^^^
  MAINTAINER bob

# Noncompliant@+1 {{The golang image runs with root as the default user. Make sure it is safe here.}}
  FROM golang
  MAINTAINER bob

# Noncompliant@+1 {{This image runs with root or containerAdministrator as the default user. Make sure it is safe here.}}
  FROM mcr.microsoft.com/test
  MAINTAINER bob

# Noncompliant@+1 {{This image might run with root as the default user. Make sure it is safe here.}}
  FROM unknown_image
  MAINTAINER bob

  FROM haproxy
  MAINTAINER bob

  FROM bitnami/test
  MAINTAINER bobbob

  FROM bitnami/
  MAINTAINER bob

  FROM haproxy
# Noncompliant@+1 {{Setting the default user as root might unnecessarily make the application unsafe. Make sure it is safe here.}}
  USER root
# ^^^^^^^^^

  FROM haproxy
  USER test

  FROM golang
  USER test

# Noncompliant@+1 {{This image runs with root or containerAdministrator as the default user. Make sure it is safe here.}}
  FROM mcr.microsoft.com/
  MAINTAINER bob

# Noncompliant@+1 {{This image might run with root as the default user. Make sure it is safe here.}}
  FROM mcr.microsoft.com
  MAINTAINER bob

# Noncompliant@+1 {{This image might run with root as the default user. Make sure it is safe here.}}
  FROM mcr.mAcrosoft.com/test
  MAINTAINER bob

# Noncompliant@+1 {{This image might run with root as the default user. Make sure it is safe here.}}
  FROM bOtnami/test
  MAINTAINER bobbob

# Noncompliant@+1 {{This image might run with root as the default user. Make sure it is safe here.}}
  FROM bitnami_test
  MAINTAINER bobbob

  FROM golang
  USER root
  USER test

  FROM golang
  USER test
# Noncompliant@+1 {{Setting the default user as root might unnecessarily make the application unsafe. Make sure it is safe here.}}
  USER root

  FROM golang
  ONBUILD USER test
