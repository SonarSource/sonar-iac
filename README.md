Code Quality and Security for Infrastructure-as-Code
==========
[![Build Status](https://github.com/SonarSource/sonar-iac-enterprise/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/SonarSource/sonar-iac-enterprise/actions/workflows/build.yml?query=branch%3Amaster)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=SonarSource_sonar-iac&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=SonarSource_sonar-iac)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SonarSource_sonar-iac&metric=coverage)](https://sonarcloud.io/summary/new_code?id=SonarSource_sonar-iac)

This SonarSource project is a [static code analyzer](https://en.wikipedia.org/wiki/Static_program_analysis) for Infrastructure-as-Code (IaC) languages such as CloudFormation, Kubernetes, and Terraform.
It is a component of the [SonarQube Server](https://www.sonarqube.org/) platform, and it runs the IaC features on [SonarQube Cloud](https://sonarcloud.io/).

It enables developers to produce stable and easily supported [integrated code quality and security](https://www.sonarsource.com/solutions/for-developers/?utm_medium=referral&utm_source=github&utm_campaign=clean-code&utm_content=sonar-iac) by helping you find and correct vulnerabilities and code issues in your projects.

# Features
* 100+ rules
* Supports Azure Resource Manager JSON/Bicep
* Supports CloudFormation JSON/YAML
* Supports Kubernetes YAML and Helm Charts
* Supports Dockerfiles
* Supports Terraform for AWS
  * HCL native syntax for files named with a .tf suffix (JSON format not supported)
  * Terraform for Azure and GCP: coming soon
* Supports configuration files for Spring and Micronaut
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
This project is one analyzer/plugin that scans and raises issues on files associated with multiple languages.

The main registration point of the plugin to the API is in `sonar-iac-plugin`. The analyses of the different languages are separated into "extensions", 
which get loaded by the main plugin class and are structured similarly to other analyzers (i.e., parser, visitors, checks, rule resources, etc.)

## Build & Test

### Requirements
* Java 17
* Docker should be installed to perform the build of the Go part inside a container
  * In some environments, importing a custom certificate must be performed during the Docker build. Refer to the [dedicated readme](sonar-helm-for-iac/Readme.md#build-docker-image) for more details.
* Alternatively, to replicate CI setup and use system Go toolchain, set environment variable `CI=true`. Go 1.23 and the following dependencies are needed:
  * musl on Linux (`musl-gcc` should be present on `PATH`)

### Setup
To configure build dependencies, run the following command:

```bash
git submodule update --init -- build-logic/common
```
To always get the latest version of the build logic during git operations, set the following configuration:

```
git config submodule.recurse true
```
For more information see [README.md](https://github.com/SonarSource/cloud-native-gradle-modules/blob/master/README.md) of cloud-native-gradle-modules.

### Build and run unit tests:
```shell
./gradlew build
```

### Build without running unit tests:

```shell
./gradlew build -x test
```

### Certificate issue during the build
If you are behind a corporate proxy, you might encounter certificate issues during the build, with following error:

```text
ERROR: failed to solve: failed to compute cache key: failed to calculate checksum of ref xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx::yyyyyyyyyyyyyyyyyyyyyyyy: "/Sonar-FGT-FW-TLS-Traffic-Inspection.cer": not found
```

To fix the issue please copy the certificate to `sonar-helm-for-iac` directory or specify `-DtrafficInspection=false` property during the build:

```shell
./gradlew -DtrafficInspection=false build
```

### Fix code formatting issues

During the Gradle build, a spotless formatting check is executed.
This check can also be triggered manually with `./gradlew spotlessCheck`.
It checks if the code is correctly formatted using standard Sonar rules.
If your build failed, you can fix the formatting just by running:

```shell
./gradlew spotlessApply
```

### Fix license packaging issues
During the Gradle build, a license packaging check is executed.
This check can also be triggered manually with `./gradlew validateLicenseFiles`.
It checks if the license files of third party libraries are correctly packaged to the resource folder according to SonarSource standards.
Since sonar-iac bundles a go binary, we are also including the licenses of all used go dependencies.

If your build failed, you can fix the license packaging by running:

```shell
./gradlew generateLicenseResources
```

Note that this overwrites your current license files in the `resources/licenses` folder.

### Update rule description

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

### Generate new rule description

To fetch static files for a rule SXXXX from RSPEC for one of the languages, execute the following command:
```shell
./gradlew ruleApiGenerateRuleArm -Prule=SXXXX
./gradlew ruleApiGenerateRuleCloudformation -Prule=SXXXX
./gradlew ruleApiGenerateRuleDocker -Prule=SXXXX
./gradlew ruleApiGenerateRuleKubernetes -Prule=SXXXX
./gradlew ruleApiGenerateRuleTerraform -Prule=SXXXX
```

Additionally, an optional property `-Pbranch=<branch name>` can be set to fetch rule metadata from a specific branch.

### Generate metadata for external linter rules

To update rules from external linters (Hadolint, TFLint, CfnLint, Ansible Lint) from their upstream sources, execute the following command:

```shell
./gradlew generateExternalRules
```

This will update all external linter rules across all extensions. To update rules for a specific extension, run the task `generateExternalRules` on a specific Gradle subproject, for example :iac-extensions:terraform:generateExternalRules`.

These tasks automatically download the latest rule documentation from upstream sources and regenerate the `rules.json` files.

# License

Copyright 2021-2026 SonarSource.

SonarQube analyzers released after November 29, 2024, including patch fixes for prior versions,
are published under the [Sonar Source-Available License Version 1 (SSALv1)](LICENSE.txt).

See individual files for details that specify the license applicable to each file.
Files subject to the SSALv1 will be noted in their headers.
