FROM ubuntu:22.04

# Noncompliant@+1 {{Omitting --ignore-scripts can lead to the execution of shell scripts. Make sure it is safe here.}}
RUN npm install
#   ^^^^^^^^^^^

# Noncompliant@+1
RUN pnpm i

# Noncompliant@+1
RUN yarn install

# Noncompliant@+1
RUN yarn

# FN current bash parser doesn't have needed capabilities
RUN yarn && foo

# FN current bash parser doesn't have needed capabilities
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

RUN yarn init

RUN foobar

RUN npm install $UNRESOLVED

