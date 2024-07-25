FROM nonCompliantCaseSimpleCase
# Noncompliant@+1 {{Merge this RUN instruction with the consecutive ones.}}
RUN instruction 1
# ^[sc=1;ec=3]
RUN instruction 2
# ^[sc=1;ec=3]< {{consecutive RUN instruction}}
RUN instruction 3
# ^[sc=1;ec=3]< {{consecutive RUN instruction}}

FROM nonCompliantRaiseSeparateIssues
# Noncompliant@+1
RUN instruction 1
# ^[sc=1;ec=3]
RUN instruction 2
# ^[sc=1;ec=3]<
WORKDIR /root
# Noncompliant@+1
RUN instruction 3
# ^[sc=1;ec=3]
RUN instruction 4
# ^[sc=1;ec=3]<

FROM nonCompliantNotAtTheBeginningOrAtTheEnd
WORKDIR something
# Noncompliant@+1
RUN instruction 1
# ^[sc=1;ec=3]
RUN instruction 2
# ^[sc=1;ec=3]<
CMD something else

FROM nonCompliantMultipleForm
# Noncompliant@+1
RUN instruction 1
# ^[sc=1;ec=3]
RUN ["instruction", "2"]
# ^[sc=1;ec=3]<
RUN <<EOF
instruction 3
EOF
# ^[sc=1;ec=3]@-2<

FROM nonCompliantRealUseCase1
# Noncompliant@+1
RUN curl -SL "https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.gz" --output nodejs.tar.gz
# ^[sc=1;ec=3]
RUN echo "$NODE_DOWNLOAD_SHA nodejs.tar.gz" | sha256sum -c -
# ^[sc=1;ec=3]<
RUN tar -xzf "nodejs.tar.gz" -C /usr/local --strip-components=1
# ^[sc=1;ec=3]<
RUN rm nodejs.tar.gz
# ^[sc=1;ec=3]<
RUN ln -s /usr/local/bin/node /usr/local/bin/nodejs
# ^[sc=1;ec=3]<

FROM nonCompliantRealUseCase2
# Noncompliant@+1
RUN curl -SL "https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.gz" --output nodejs.tar.gz
# ^[sc=1;ec=3]
RUN echo "$NODE_DOWNLOAD_SHA nodejs.tar.gz" | sha256sum -c -
# ^[sc=1;ec=3]<

FROM compliantBecauseTheyAreSeparated
RUN instruction 1
WORKDIR /root
RUN instruction 2

FROM compliantBecauseWeCompareOnlyRootInstruction
RUN instruction 1
ONBUILD RUN instruction 2
RUN instruction 3

FROM compliantBecauseWeOnlyCheckRunInstruction
RUN instruction 1
CMD instruction 2
CMD instruction 3
RUN instruction 4

FROM compliantDontDetectRunInstructionInHeredoc
RUN <<EOF
RUN instruction 1
RUN instruction 2
RUN instruction 3
EOF

FROM compliantRealUseCase
RUN curl -SL "https://nodejs.org/dist/v${NODE_VERSION}/node-v${NODE_VERSION}-linux-x64.tar.gz" --output nodejs.tar.gz \
&& echo "$NODE_DOWNLOAD_SHA nodejs.tar.gz" | sha256sum -c - \
&& tar -xzf "nodejs.tar.gz" -C /usr/local --strip-components=1 \
&& rm nodejs.tar.gz \
&& ln -s /usr/local/bin/node /usr/local/bin/nodejs
