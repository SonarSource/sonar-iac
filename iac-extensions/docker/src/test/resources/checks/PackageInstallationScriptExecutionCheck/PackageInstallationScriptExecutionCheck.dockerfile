FROM ubuntu:22.04

# Noncompliant@+1 {{Omitting "--ignore-scripts" can lead to the execution of shell scripts. Make sure it is safe here.}}
RUN npm install
#   ^^^^^^^^^^^

# Noncompliant@+1
RUN pnpm i

# Noncompliant@+1
RUN yarn install
# Noncompliant@+1
RUN yarn install --check-files
# Noncompliant@+1
RUN yarn
# Noncompliant@+1
RUN yarn --force

# Noncompliant@+1
RUN npm install;

# Noncompliant@+1
RUN npm install && foobar

# Noncompliant@+1
RUN foobar && npm install

# Noncompliant@+1
RUN npm install --save --global

# Noncompliant@+1
RUN npm install $UNRESOLVED && npm install

RUN npm install --ignore-scripts

RUN npm install --save --ignore-scripts --global

RUN npm install --ignore-scripts --save

RUN npm install --save --ignore-scripts

RUN foobar && npm install --ignore-scripts

RUN npm install --ignore-scripts && foobar

RUN pnpm install --ignore-scripts

RUN npm  i --ignore-scripts

RUN yarn install --force --ignore-scripts
RUN yarn install --ignore-scripts --force
RUN yarn --ignore-scripts
RUN yarn --force --ignore-scripts
RUN yarn build
RUN yarn init
RUN foobar yarn
RUN yarn foobar
RUN foobar yarn foobar

RUN foobar

RUN npm install $UNRESOLVED

RUN apk add python3 make g++ nodejs yarn

# Use cases with YARN_ENABLE_SCRIPTS
FROM scratch
RUN YARN_ENABLE_SCRIPTS=false yarn install
RUN export YARN_ENABLE_SCRIPTS=false && yarn install

FROM scratch
ENV YARN_ENABLE_SCRIPTS=false
RUN yarn install

FROM scratch
# Noncompliant@+1
RUN yarn install
ENV YARN_ENABLE_SCRIPTS=false

FROM scratch
ENV YARN_ENABLE_SCRIPTS=false
# Noncompliant@+1
RUN YARN_ENABLE_SCRIPTS=true yarn install

FROM scratch
ENV YARN_ENABLE_SCRIPTS=true
# Noncompliant@+1
RUN yarn install

FROM scratch
RUN YARN_ENABLE_SCRIPTS=true
RUN YARN_ENABLE_SCRIPTS=false yarn install

FROM scratch
RUN YARN_ENABLE_SCRIPTS=false
# Noncompliant@+1
RUN YARN_ENABLE_SCRIPTS=true yarn install

FROM scratch
RUN YARN_ENABLE_SCRIPTS=false
# Noncompliant@+1
RUN yarn install
