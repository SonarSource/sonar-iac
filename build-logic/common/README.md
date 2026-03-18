# cloud-native-gradle-modules

Common Gradle modules for multiple projects

# Usage in a project
This repository is supposed to be used as a submodule in a project. The Gradle build should then be configured as an include build.

To include this repository as a submodule in a project, run the following command in the project's root directory:
```bash
git submodule add https://github.com/SonarSource/cloud-native-gradle-modules gradle/build-logic
```

Then, in the project's `settings.gradle.kts` file, add `includeBuild` to the `pluginManagement` block:
```kotlin
pluginManagement {
    includeBuild("gradle/build-logic")
}
```

Here we used `gradle/build-logic` as a directory, but the choice is arbitrary.
An example usage can be seen in the [Sonar-Go analyzer](https://github.com/SonarSource/sonar-go/blob/d4b923d43c3183927a32dc0956cbf4e4ec50d8a9/settings.gradle.kts#L17)

### Configure Git to Automatically Checkout Changes in the Submodule

When a newer version of the submodule is integrated into a remote branch, running `git pull` will not automatically update the submodule. Instead, Git will display it as changed, and `git status` will show a message like:
```text
modified:   gradle/build-logic (new commits)
```

To configure Git to automatically checkout changes in the submodule, run the following command:

```bash
git config submodule.recurse true
```

Optionally, run this command with `--global` to apply this configuration globally.

# Common use cases

## Publishing a sonar plugin

* Apply `artifactory-configuration` to the root project to set some defaults
* Apply `sonar-plugin` to the subproject that contains the plugin
* If necessary, re-apply `artifactory-configuration` to the subproject to override the defaults

## Building a Go executable

* Apply `go-binary-builder` script to the subproject that contains the Go code
  * It expects `make.sh` and `make.bat` scripts to be present in the same directory as `build.gradle[.kts]`
* Configure the `goBuild` extension
* Go-related tasks are automatically linked to `test`, `assemble`, `check`, and `build` tasks
* A configuration `goBinaries` is created, and it can be used to depend on the Go binaries like
  `implementation(projects(":go-subprojcet", "goBinaries"))`

## Generating license files for a plugin

The `license-file-generator` plugin generates license files for third-party runtime dependencies and bundles them into the plugin's resources folder.

### Setup

Apply the plugin to the subproject that packages the plugin (usually `sonar-<language>-plugin`):
```kotlin
plugins {
    id("org.sonarsource.cloud-native.license-file-generator")
}
```

### Configuration

The plugin exposes a `licenseGenerationConfig` extension with the following options:

| Property                     | Type                | Default                                                         | Description                                                              |
|------------------------------|---------------------|-----------------------------------------------------------------|--------------------------------------------------------------------------|
| `projectLicenseFile`         | `File`              | `../LICENSE.txt` (one level above the plugin project directory) | The project's own license file.                                          |
| `dependencyLicenseOverrides` | `Map<String, File>` | empty                                                           | Per-dependency license file overrides. Keys use the `group:name` format. |

Example configuration (Groovy DSL):
```groovy
licenseGenerationConfig {
    projectLicenseFile.set(file("../LICENSE.txt"))
    dependencyLicenseOverrides.put("com.salesforce:apex-jorje-lsp-minimized", file("../build-logic/common/gradle-modules/src/main/resources/licenses/BSD-3.txt"))
}
```

Example configuration (Kotlin DSL):
```kotlin
licenseGenerationConfig {
    projectLicenseFile.set(file("../LICENSE.txt"))
    dependencyLicenseOverrides.put("com.salesforce:apex-jorje-lsp-minimized", file("../build-logic/common/gradle-modules/src/main/resources/licenses/BSD-3.txt"))
}
```

Use `dependencyLicenseOverrides` when the plugin cannot automatically resolve the correct license for a dependency (e.g., the dependency jar does not include a license file and its POM license title is not recognized).

### Available bundled license files

The following license files are bundled in this module and can be referenced in overrides:
`0BSD.txt`, `Apache-2.0.txt`, `BSD-2.txt`, `BSD-3.txt`, `GNU-LGPL-3.txt`, `Go.txt`, `lgpl-2.1.txt`, `MIT.txt`

### Tasks

| Task                       | Description                                                                                                   |
|----------------------------|---------------------------------------------------------------------------------------------------------------|
| `generateLicenseReport`    | Generates license files into the build directory.                                                             |
| `generateLicenseResources` | Copies generated license files to `src/main/resources/licenses/`. Run this to update committed license files. |
| `validateLicenseFiles`     | Validates that committed license files match the generated ones. Runs as part of `check`.                     |

### Workflow

1. Apply the plugin and configure `licenseGenerationConfig` if needed
2. Run `./gradlew generateLicenseResources` to generate and copy license files into `src/main/resources/licenses/`
3. Commit the generated files
4. The `check` task will automatically validate that committed license files are up-to-date via `validateLicenseFiles`
