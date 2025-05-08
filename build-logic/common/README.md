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
