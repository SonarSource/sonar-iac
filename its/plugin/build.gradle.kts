plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
    id("org.sonarsource.iac.integration-test")
}

description = "SonarSource IaC Analyzer :: Integration Testing"

dependencies {
    "integrationTestImplementation"(project(":sonar-iac-plugin", configuration = "shadow"))
    "integrationTestImplementation"(libs.junit.jupiter)
    "integrationTestImplementation"(libs.assertj.core)
    "integrationTestImplementation"(libs.sonar.orchestrator.junit5)
    "integrationTestImplementation"(libs.sonar.ws)
    "integrationTestImplementation"(libs.sonar.lint.core)
    "integrationTestImplementation"(libs.sonar.plugin.api)
}
