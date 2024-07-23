FROM ubuntu:20.04

# Noncompliant@+2 {{Sort these package names alphanumerically.}}
# ^[sl=+2;sc=23;el=+7;ec=7]
RUN apt-get update && apt-get -V install -y \
    unzip \
    wget \
    curl \
    git \
    zip

# Noncompliant@+2 {{Sort these package names alphanumerically.}}
# ^[sl=+2;sc=23;el=+7;ec=7]
RUN apt-get update && apt-get -V install -y \
    unzip \
    wget \
    curl \
    git \
    zip && \
    rm -rf /var/lib/apt/lists/*

# Noncompliant@+2 {{Sort these package names alphanumerically.}}
# ^[sl=+2;sc=5;el=+7;ec=7]
RUN apt-get -V install -y \
    unzip \
    wget \
    curl \
    git \
    zip \
    || echo "Failed to install packages"

# FN; TODO SONARIAC-1557 Heredoc should be treated as multiple instructions
RUN <<EOF
apt-get update
apt-get -V install -y \
   unzip \
   wget \
   curl \
   git \
   zip
EOF

# Noncompliant@+1
RUN apk add --virtual \
    unzip \
    wget \
    curl \
    git \
    zip

# Noncompliant@+1
RUN pip install \
    numpy \
    pandas \
    matplotlib \
    seaborn

# Noncompliant@+2
# `-dev` suffix should be later in the list
RUN apk add --no-cache --virtual .build-deps  \
    		bzip2-dev \
    		coreutils \
    		dpkg-dev dpkg

# Noncompliant@+1
RUN pip install seaborn matplotlib pandas numpy

# Noncompliant@+1
RUN echo "Installing packages" && pip install seaborn matplotlib pandas numpy || echo "Failed to install packages"
#                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

# Compliant
RUN apt-get update && apt-get install -y \
    curl \
    git \
    unzip \
    wget \
    zip

# Compliant
RUN <<EOF
apt-get update
apt-get install -y --no-install-recommends curl git unzip wget zip
rm -rf /var/lib/apt/lists/*
EOF

# FN; TODO SONARIAC-1557 Heredoc should be treated as multiple instructions
RUN <<EOF
apt-get update
apt-get install -y --no-install-recommends unzip wget zip curl git
rm -rf /var/lib/apt/lists/*
EOF

# Compliant
RUN apk add --virtual \
    curl \
    git \
    unzip \
    wget \
    zip

# Compliant
RUN <<EOF
apk update
apk add git
EOF

# Compliant
RUN pip install \
    matplotlib \
    numpy \
    pandas \
    seaborn

# Compliant
RUN pip install -r requirements.txt

# Compliant; empty package list
RUN apt-get install -y

# Compliant; too few packages
RUN apk add zip bash
