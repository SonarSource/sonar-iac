plugins {
  id("org.sonarsource.iac.code-style-convention")
  id("org.sonarsource.iac.java-conventions")
  id("java-library")
  id("java-test-fixtures")
}

dependencies {
  api(libs.sonar.plugin.api.impl)
  api(libs.sonar.analyzer.commons)
  api(libs.sslr)
  api(libs.minimaljson)
  api(libs.snakeyaml)

  testImplementation(libs.junit.jupiter)
  testImplementation(libs.junit.jupiter.api)
  testImplementation(libs.assertj.core)
  testImplementation(libs.mockito.core)
  testImplementation(libs.sonar.plugin.api.test.fixtures)
  testImplementation(libs.sonar.analyzer.test.commons)
  testImplementation(libs.sslr.test)

  testFixturesImplementation(libs.junit.jupiter.api)
  testFixturesImplementation(libs.junit.jupiter)
  testFixturesImplementation(libs.assertj.core)
  testFixturesImplementation(libs.mockito.core)
  testFixturesImplementation(libs.sonar.plugin.api.test.fixtures)
  testFixturesImplementation(libs.sonar.analyzer.test.commons)

  compileOnly(libs.sonar.plugin.api)
  compileOnly(libs.slf4j.api)
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    exceptionFormat =
      org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL // log the full stack trace (default is the 1st line of the stack trace)
    events("skipped", "failed")
    // events("skipped", "failed", "passed")
  }
}

description = "SonarSource IaC Analyzer :: Common"

val testsJar by tasks.registering(Jar::class) {
  archiveClassifier.set("tests")
  from(sourceSets["test"].output)
}
