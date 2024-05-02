plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Docker"

dependencies {
    api(project(":iac-common"))

    testImplementation(libs.sslr.test)
    testImplementation(libs.sonar.analyzer.test.commons)
    testImplementation(testFixtures(project(":iac-common")))
}
