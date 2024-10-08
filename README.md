Code Quality and Security for Infrastructure-as-Code
==========
[![Build Status](https://api.cirrus-ci.com/github/SonarSource/sonar-iac.svg?branch=master)](https://cirrus-ci.com/github/SonarSource/sonar-iac)

This SonarSource project is a [static code analyzer](https://en.wikipedia.org/wiki/Static_program_analysis) for Infrastructure-as-Code (IaC) languages such as CloudFormation, Kubernetes, and Terraform.
It is a component of the [SonarQube](https://www.sonarqube.org/) platform, and it runs the IaC features on [SonarCloud](https://sonarcloud.io/).

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
  * AWS S3 Buckets
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

The main registration point of the plugin to the API is in `sonar-iac-plugin`. The analyses of the different languages are separated into "extensions", 
which get loaded by the main plugin class and are structured similarly to other analyzers (i.e., parser, visitors, checks, rule resources, etc.)

#### Using sonar-rule-api:

When using the [sonar-rule-api](https://github.com/SonarSource/sonar-rule-api) to generate or update metadata of rules, 
it has to be done in the different extension folders: to update/generate a rule
for AzureResourceManager, run sonar-rule-api in `iac-extensions/arm`, 
for CloudFormation, run sonar-rule-api in `iac-extensions/cloudformation`, 
for Docker, in `iac-extensions/docker`,
for Kubernetes in `iac-extensions/kubernetes` and 
for Terraform in `iac-extensions/terraform`.

Alternatively, execute the Gradle task `ruleApiUpdate` to update rule metadata for all extensions.

## Build & Test

### Requirements
* Java 17
* Go 1.21 and the following dependencies:
  * musl on Linux (`musl-gcc` should be present on `PATH`)
* Alternatively, Docker should be installed to perform the build of the Go part inside a container

#### Build and run unit tests:
```shell
./gradlew build
```

#### Build without running unit tests:

```shell
./gradlew build -x test
```

#### Fix code formatting issues

During the Gradle build, a spotless formatting check is executed.
This check can also be triggered manually with `./gradlew spotlessCheck`.
It checks if the code is correctly formatted using standard Sonar rules.
If your build failed, you can fix the formatting just by running:

```shell
./gradlew spotlessApply
```

#### Update rule description

Update all rule descriptions.

```shell
./gradlew ruleApiUpdate
```

Update all rule descriptions for a specific language.

```shell
./gradlew ruleApiUpdateArm
./gradlew ruleApiUpdateCloudformation
./gradlew ruleApiUpdateDocker
./gradlew ruleApiUpdateKubernetes
./gradlew ruleApiUpdateTerraform
```

#### Generate new rule description

To fetch static files for a rule SXXXX from RSPEC for one of the languages, execute the following command:
```shell
./gradlew ruleApiUpdateRuleArm -Prule=SXXXX
./gradlew ruleApiUpdateRuleCloudformation -Prule=SXXXX
./gradlew ruleApiUpdateRuleDocker -Prule=SXXXX
./gradlew ruleApiUpdateRuleKubernetes -Prule=SXXXX
./gradlew ruleApiUpdateRuleTerraform -Prule=SXXXX
```

