---
title: Docker
key: docker
---

## Supported Versions

| Version | Status              |
|---------|---------------------|
| 1.x     | Fully Supported     |
| labs    | Partially Supported |

## Language-Specific Properties

Discover and update the Docker [properties](/analysis/analysis-parameters/) in: **<!-- sonarcloud -->Project <!-- /sonarcloud -->[Administration > General Settings > Languages > Docker](/#sonarqube-admin#/admin/settings?category=Docker)**

## Relevant Limitations

### No NoSonar Support

Trailing comments are not permitted in Dockerfiles. For this reason, our Dockerfile parser does not support `NOSONAR` comments to suppress issues. Issues and hotspots must be reviewed in the UI.

### Missing Uniform Filename Convention

Dockerfiles can have all kinds of names and do not need a file extension. For this reason, it is difficult for the scanner and the analyzer to recognize all Dockerfiles. By default, all files named `Dockerfile`, `Dockerfile.*`, or `*.dockerfile` are considered Dockerfiles. If other conventions apply, these can be specified via the scanner property `sonar.lang.patterns.docker`.

## Related Pages

* [External Analyzer Reports](/#sonarqube-admin#/admin/settings?category=external+analyzers) ([Hadolint](https://github.com/hadolint/hadolint))
