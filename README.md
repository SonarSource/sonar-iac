Code Quality and Security for Infrastructure-as-Code
==========
[![Build Status](https://api.cirrus-ci.com/github/SonarSource/sonar-iac.svg?branch=master)](https://cirrus-ci.com/github/SonarSource/sonar-iac)

This SonarSource project is a [static code analyser](https://en.wikipedia.org/wiki/Static_program_analysis) for Infrastructure-as-Code (IaC) languages such as CloudFormation and Terraform. 
It is a component of the [SonarQube](https://www.sonarqube.org/) platform and it runs the IaC features on [SonarCloud](https://sonarcloud.io/).

It allows to produce stable and easily supported code by helping you find and correct vulnerabilities and smells in your code.

# Features
* 30+ rules
* Supports CloudFormation JSON/YAML
* Supports Terraform for AWS
  * HCL native syntax for files named with a .tf suffix (JSON format not supported)
  * Terraform for Azure and GCP: coming soon
* Domains Covered: 
  * ASW S3 Buckets
  * Permissions
  * Encryption at Rest
  * Encryption at Transit (coming soon)
  * Traceability (coming soon)
* Metrics (number of lines, comments, etc.)
* Import of [cfn-lint](https://community.sonarsource.com/t/sonarcloud-can-scan-terraform-and-cloudformation-files-cfn-lint-support/48550) results

## Useful links

* [SonarSource Community Forum](https://community.sonarsource.com/)
* [Issue tracking](https://jira.sonarsource.com/projects/SONARIAC)

## Structure
This project is one analyzer/plugin that scans and raises issues on files associated with multiple languages. Currently, these languages are CloudFormation and Terraform.

The main point of registration of the plugin to the API is in `sonar-iac-plugin`. The analyses of the different languages is separated into "extensions" which get loaded by the
main plugin class, and which are structured in a way similar to other analyzers (i.e., parser, visitors, checks, rule resources, etc...).

#### Using sonar-rule-api:

When using the sonar-rule-api to generate or update metadata of rules it has to be done in the different extension folders: to update/generate rule for CloudFormation, run
 sonar-rule-api in `iac-extensions/cloudformation`, and for Terraform in `iac-extensions/terraform`.

## Build & Test
The project uses Maven as a build tool.
#### Build and run unit tests:
```shell
mvn clean install
```

#### Build without running unit tests:

```shell
mvn clean install -DskipTests
```

#### Ruling integration tests
These integration tests verify that, given a set of files, when the analyzer is run on them, all the expected issues get raised in a prepared SonarQube instance. 
The expected findings are saved in `its/ruling/src/test/resources/expected`. To run the ruling ITS:
- Make sure the project is built with the latest changes
- Load/update the analyzed files: `git submodule update --init`
- In `its/ruling` run:
  
  ```shell
  mvn clean verify -Pit-ruling
  ``` 

It is possible to keep the prepared SonarQube instance running to better inspect actual-vs-expected differences (if there are any) in the SQ UI. For this use:
  
  ```shell
  mvn clean verify -Pit-ruling -DkeepSonarqubeRunning=true
  ```
#### Plugin integration tests
These integration tests verify that the analyzer registers at and interacts with the SonarQube API correctly. For example: if file metrics get sent, and if all properties get registered.
To run them, in `ìts/plugin` run:

  ```shell
  mvn clean verify -Pit-plugin
  ```
## Remote debugging
Remote debugging cannot be done on an obfuscated build. If remote debugging is needed during development on a local machine, obfuscation can be disabled by building with:
```shell
 mvn clean install -DskipObfuscation
```
(It might be necessary to temporarily increase the plugin size enforcing in `sonar-iac-plugin/pom.xml` when building this way)

