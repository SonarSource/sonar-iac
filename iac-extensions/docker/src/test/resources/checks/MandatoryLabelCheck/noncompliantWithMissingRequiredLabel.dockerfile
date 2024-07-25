# Noncompliant@+1 {{Add the missing label: "maintainer".}}
FROM scratch
# ^[sc=1;ec=12]
LABEL somethingelse="something"
LABEL maintaner="maintainerWithATypo"

FROM scratch
COPY maintainer maintainer
ONBUILD COPY maintainer maintainer
FROM scratch


