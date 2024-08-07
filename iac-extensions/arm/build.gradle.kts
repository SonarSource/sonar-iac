plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Azure Resource Manager"

dependencies {
    api(project(":iac-common"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sslr.test)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.analyzer.test.commons)
    testImplementation(libs.apache.commons.lang)
    testImplementation(testFixtures(project(":iac-common")))
}
