FROM ubuntu:22.04

# FN: shell form not supported in community edition
RUN npm install

# Noncompliant@+1
RUN ["pnpm", "i"]
#    ^^^^^^^^^^^

# Noncompliant@+1
RUN ["yarn", "install"]
# Noncompliant@+1
RUN ["yarn", "install", "--check-files"]
# Noncompliant@+1
RUN ["yarn"]
# Noncompliant@+1
RUN ["yarn", "--force"]

# Noncompliant@+1
RUN ["npm", "install", "--save", "--global"]

# Variable cannot be references in exec form + "sh -c" not supported
RUN ["sh", "-c", "npm install $UNRESOLVED && npm install"]

RUN ["npm", "install", "--ignore-scripts"]

RUN ["npm", "install", "--save", "--ignore-scripts", "--global"]

RUN ["npm", "install", "--ignore-scripts", "--save"]

RUN ["npm", "install", "--save", "--ignore-scripts"]

RUN ["pnpm", "install", "--ignore-scripts"]

RUN ["npm", "i", "--ignore-scripts"]

RUN ["yarn", "install", "--force", "--ignore-scripts"]
RUN ["yarn", "install", "--ignore-scripts", "--force"]
RUN ["yarn", "--ignore-scripts"]
RUN ["yarn", "--force", "--ignore-scripts"]
RUN ["yarn", "build"]
RUN ["yarn", "init"]
RUN ["foobar yarn"]
RUN ["yarn foobar"]
RUN ["foobar yarn foobar"]

RUN ["foobar"]

RUN ["npm", "install", "$UNRESOLVED"]

RUN ["apk", "add", "python3", "make", "g++", "nodejs", "yarn"]

# FNs: exec form with explicit shell invocation are not supported
RUN ["sh", "-c", "npm install;"]
RUN ["sh", "-c", "npm install && foobar"]
RUN ["sh", "-c", "foobar && npm install"]
RUN ["sh", "-c", "foobar && npm install --ignore-scripts"]
RUN ["sh", "-c", "npm install --ignore-scripts && foobar"]


# Use cases with YARN_ENABLE_SCRIPTS
FROM scratch
RUN ["sh", "-c", "YARN_ENABLE_SCRIPTS=false yarn install"]
RUN ["sh", "-c", "export YARN_ENABLE_SCRIPTS=false && yarn install"]

FROM scratch
ENV YARN_ENABLE_SCRIPTS=false
RUN ["yarn", "install"]

FROM scratch
# Noncompliant@+1
RUN ["yarn", "install"]
ENV YARN_ENABLE_SCRIPTS=false

FROM scratch
ENV YARN_ENABLE_SCRIPTS=false
RUN ["sh", "-c", "YARN_ENABLE_SCRIPTS=true yarn install"]

FROM scratch
ENV YARN_ENABLE_SCRIPTS=true
# Noncompliant@+1
RUN ["yarn", "install"]

FROM scratch
RUN ["sh", "-c", "YARN_ENABLE_SCRIPTS=true"]
RUN ["sh", "-c", "YARN_ENABLE_SCRIPTS=false yarn install"]

FROM scratch
RUN ["sh", "-c", "YARN_ENABLE_SCRIPTS=false"]
RUN ["sh", "-c", "YARN_ENABLE_SCRIPTS=true yarn install"]

FROM scratch
RUN ["sh", "-c", "YARN_ENABLE_SCRIPTS=false"]
# Noncompliant@+1
RUN ["yarn", "install"]
