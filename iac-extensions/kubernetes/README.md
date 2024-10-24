# Sonar IaC Kubernetes extension
## Structure of this project
This project is split into three source sets to enable including Kubernetes checks into other extensions in cases, when Kubernetes configuration can be inlined into other IaC languages.
* `common`: contains Kubernetes checks and related model classes
* `main`: contains Kubernetes checks that require cross-file analysis, as well as Helm-specific checks, and also execution engine for Kubernetes/Helm projects
* `test`: contains all tests for both `common` and `main` source sets

### Implementation note
Though source sets are a common concept in Gradle, adding a custom source set requires additional configuration.
* `common` source set is automatically picked up by the `java` plugin
* `main` source set is explicitly configured to depend on the `common` source set by using `configurations.getByName("<main configuration>").extendsFrom("<common configuration")` and by adding a dependency `api(sourceSets.getByName("common").output)`
* The resulting jar is configured to include compiled `common` classes using an additional `from()` call
* Jacoco test report is configured to include the custom source set
* In the root `build.gradle.kts`, Sonar Gradle plugin is configured to include the custom source set

## Using Kubernetes checks in another IaC language
A dependency in form `project(":iac-extensions:kubernetes", configuration = "common")` gives access to the check classes. A wrapper specific to a target language should be provided.
