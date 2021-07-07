Code Quality and Security for Infrastructure as Code
==========
[![Build Status](https://api.cirrus-ci.com/github/SonarSource/sonar-iac.svg?branch=master)](https://cirrus-ci.com/github/SonarSource/sonar-iac)

## Useful links

* [Issue tracking](https://jira.sonarsource.com/projects/SONARIAC)

## Structure
TODO

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

## Obfuscation & Debugging

When built, the source code gets obfuscated with [yGuard](https://github.com/yWorks/yGuard). This can be of importance in the following two cases.
#### Reading an obfuscated stacktrace:
To deobfuscate a given stacktrace, the yGuard artifact created during build has to be retrieved. [These steps](https://xtranet-sonarsource.atlassian.net/wiki/spaces/DEV/pages/1620312105/Workaround+disabled+UI+access+to+Repox#Download-a-Yguard-file)
can be followed to retrieve this artifact. With this artifact and the yGuard jar, obfuscation can be done with:

```shell
java -jar yguard.jar artifact.xml
```
#### Remote debugging
Remote debugging cannot be done on an obfuscated build. If remote debugging is needed during development on a local machine, obfuscation can be disabled by building with:
```shell
 mvn clean install -DskipObfuscation
```
(It might be necessary to temporarily increase the plugin size enforcing in `sonar-iac-plugin/pom.xml` when building this way)

