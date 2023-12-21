plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
    id("org.sonarsource.iac.integration-test")
}

description = "SonarSource IaC Analyzer :: Integration Testing"

dependencies {
    "integrationTestImplementation"(project(":iac-extensions:terraform"))
    "integrationTestImplementation"(project(":iac-extensions:cloudformation"))
    "integrationTestImplementation"(project(":iac-extensions:kubernetes"))
    "integrationTestImplementation"(project(":iac-extensions:docker"))
    "integrationTestImplementation"(project(":iac-extensions:arm"))
    "integrationTestImplementation"(libs.junit.jupiter)
    "integrationTestImplementation"(libs.assertj.core)
    "integrationTestImplementation"(libs.sonar.analyzer.commons)
    "integrationTestImplementation"(libs.sonar.orchestrator)
    "integrationTestImplementation"(libs.sonar.orchestrator.junit5)
}

tasks.integrationTest {
    inputs.files("$projectDir/projects")
}
