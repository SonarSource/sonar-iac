plugins {
    id("org.sonarsource.iac.project-version-convention")
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
    id("java-library")
    id("java-test-fixtures")
}

description = "SonarSource IaC Analyzer :: Common"

dependencies {
    api(libs.sonar.plugin.api.impl)
    api(libs.sonar.analyzer.commons)
    api(libs.sslr)
    api(libs.minimaljson)
    api(libs.snakeyaml)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sonar.analyzer.test.commons)

    testFixturesImplementation(libs.junit.jupiter)
    testFixturesImplementation(libs.assertj.core)
    testFixturesImplementation(libs.mockito.core)
    testFixturesImplementation(libs.sonar.plugin.api.test.fixtures)
    testFixturesImplementation(libs.sonar.analyzer.test.commons)
}
