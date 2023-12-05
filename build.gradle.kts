plugins {
  id("java-gradle-plugin")
  id("com.diffplug.spotless") version libs.versions.spotless.gradle.get()
  id("org.sonarsource.iac.artifactory-configuration")
  // id("org.sonarsource.text.rule-api")
  id("org.sonarsource.iac.sonarqube")
  id("com.diffplug.blowdryer")
}

spotless {
  encoding(Charsets.UTF_8)
  kotlinGradle {
    ktlint().setEditorConfigPath("$rootDir/.editorconfig")
    target("*.gradle.kts", "/build-logic/src/**/*.gradle.kts")
  }
}

tasks.artifactoryPublish { skip = true }
