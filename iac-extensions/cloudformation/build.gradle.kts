plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Cloudformation"

dependencies {
    api(project(":iac-common"))

    testImplementation(libs.sslr.test)
    testImplementation(testFixtures(project(":iac-common")))
}
