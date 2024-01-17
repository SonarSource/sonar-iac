plugins {
    id("org.sonarsource.iac.project-version-convention")
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Cloudformation"

dependencies {
    api(project(":iac-common"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sslr.test)
    testImplementation(testFixtures(project(":iac-common")))
}
