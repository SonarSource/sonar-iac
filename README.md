Code Quality and Security for Infrastructure-as-Code
==========
[![Build Status](https://api.cirrus-ci.com/github/SonarSource/sonar-iac.svg?branch=master)](https://cirrus-ci.com/github/SonarSource/sonar-iac)

This SonarSource project is a [static code analyzer](https://en.wikipedia.org/wiki/Static_program_analysis) for Infrastructure-as-Code (IaC) languages such as CloudFormation, Kubernetes, and Terraform.
It is a component of the [SonarQube](https://www.sonarqube.org/) platform and it runs the IaC features on [SonarCloud](https://sonarcloud.io/).

It allows you to produce stable and easily supported [Clean Code](https://www.sonarsource.com/solutions/clean-code/?utm_medium=referral&utm_source=github&utm_campaign=clean-code&utm_content=sonar-iac) by helping you find and correct vulnerabilities and code smells in your projects.

# Features
* 100+ rules
* Supports Azure Resource Manager JSON
* Supports CloudFormation JSON/YAML
* Supports Kubernetes YAML
* Supports Dockerfiles
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
This project is one analyzer/plugin that scans and raises issues on files associated with multiple languages. Currently, these languages are CloudFormation, Kubernetes, and Terraform.

The main point of registration of the plugin to the API is in `sonar-iac-plugin`. The analyses of the different languages is separated into "extensions" which get loaded by the
main plugin class, and which are structured in a way similar to other analyzers (i.e., parser, visitors, checks, rule resources, etc...).

#### Using sonar-rule-api:

When using the [sonar-rule-api](https://github.com/SonarSource/sonar-rule-api) to generate or update metadata of rules it has to be done in the different extension folders: to update/generate rule 
for AzureResourceManager, run sonar-rule-api in `iac-extensions/arm`, 
for CloudFormation, run sonar-rule-api in `iac-extensions/cloudformation`, 
for Docker, in `iac-extensions/docker`,
for Kubernetes in `iac-extensions/kubernetes` and 
for Terraform in `iac-extensions/terraform`.

## Build & Test

### Requirements
* Java 11
* Maven
* Go 1.21.1
* musl on Linux (`musl-gcc` should be present on `PATH`)
* protoc 25.0
* protoc-gen-go 1.31.0

#### Build and run unit tests:
```shell
./gradlew build
```

#### Build without running unit tests:

```shell
./gradlew build -x test
```

#### Fix code formatting issues
During the `mvn install` the `spotless:check` is executed. 
This phase checks is the code is correctly formatted using common Sonar rules.
If your build failed, you can fix the formatting just by running:

```shell
./gradlew spotlessApply
```

#### Ruling integration tests
These integration tests verify that, given a set of files, when the analyzer is run on them, all the expected issues get raised in a prepared SonarQube instance. 
The expected findings are saved in `its/ruling/src/integrationTest/resources/expected`. To run the ruling ITS:
- Make sure the project is built with the latest changes
- Load/update the analyzed files: `git submodule update --init`
- In `its/ruling` run:
  
  ```shell
  ./gradlew :its:ruling:integrationTest
  ``` 

It is possible to keep the prepared SonarQube instance running to better inspect actual-vs-expected differences (if there are any) in the SQ UI. For this use:
  
  ```shell
  ./gradlew :its:ruling:integrationTest -DkeepSonarqubeRunning=true
  ```
#### Plugin integration tests
These integration tests verify that the analyzer registers at and interacts with the SonarQube API correctly. For example: if file metrics get sent, and if all properties get registered.
To run them, in `ìts/plugin` run:

  ```shell
  ./gradlew :its:plugin:integrationTest
  ```

