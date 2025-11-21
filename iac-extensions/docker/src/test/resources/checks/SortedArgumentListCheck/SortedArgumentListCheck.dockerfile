FROM ubuntu:20.04

# FN: shell form not supported in community edition
RUN apt-get update && apt-get -V install -y unzip wget curl git zip && rm -rf /var/lib/apt/lists/*

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "apt-get update && apt-get -V install -y unzip wget curl git zip && rm -rf /var/lib/apt/lists/*"]
RUN ["sh", "-c", "apt-get -V install -y unzip wget curl git zip || echo \"Failed to install packages\""]
RUN ["sh", "-c", "echo \"Installing packages\" && pip install seaborn matplotlib pandas numpy || echo \"Failed to install packages\""]
# Compliant
RUN ["sh", "-c", "apt-get update && apt-get install -y curl git unzip wget zip"]
RUN ["sh", "-c", "apt-get install -y gcc > /dev/null"]
RUN ["sh", "-c", "apt-get install -y gcc &> /dev/null"]
RUN ["sh", "-c", "apt-get install -y zip | tee apt-get.log"]

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
RUN ["apk", "add", "--virtual", "unzip", "wget", "curl", "git", "zip"]

# Noncompliant@+1
RUN ["pip", "install", "numpy", "pandas", "matplotlib", "seaborn"]

# Noncompliant@+2
# `-dev` suffix should be later in the list
RUN ["apk", "add", "--no-cache", "--virtual", ".build-deps", "bzip2-dev", "coreutils", "dpkg-dev", "dpkg"]

# Noncompliant@+1
RUN ["pip", "install", "seaborn", "matplotlib", "pandas", "numpy"]

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
RUN ["apk", "add", "--virtual", "curl", "git", "unzip", "wget", "zip"]

# Compliant
RUN <<EOF
apk update
apk add git
EOF

# Compliant
RUN ["pip", "install", "matplotlib", "numpy", "pandas", "seaborn"]

# Compliant
RUN ["pip", "install", "-r", "requirements.txt"]

# Compliant; empty package list
RUN ["apt-get", "install", "-y"]

# Compliant; too few packages
RUN ["apk", "add", "zip", "bash"]
