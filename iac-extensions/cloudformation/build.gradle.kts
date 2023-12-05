plugins {
  id("org.sonarsource.iac.code-style-convention")
  id("org.sonarsource.iac.java-conventions")
}

dependencies {
  api(project(":iac-common"))
  api(libs.sonar.analyzer.commons)
  api(libs.snakeyaml)

  testImplementation(project(":iac-common"))
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.assertj.core)
  testImplementation(libs.mockito.core)
  testImplementation(libs.sonar.plugin.api.test.fixtures)
  testImplementation(libs.sonar.plugin.api.impl)
  testImplementation(libs.sslr.test)
  testImplementation(libs.sonar.analyzer.test.commons)
  testImplementation(testFixtures(project(":iac-common")))

  compileOnly(libs.sonar.plugin.api)
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    exceptionFormat =
      org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL // log the full stack trace (default is the 1st line of the stack trace)
    events("skipped", "failed")
  }
}

description = "SonarSource IaC Analyzer :: Extensions :: Cloudformation"
