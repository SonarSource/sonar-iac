plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Kubernetes"

dependencies {
    api(project(":iac-common"))
    implementation(project(":sonar-helm-for-iac", "goBinaries"))
    implementation(project(":sonar-helm-for-iac"))
    implementation(libs.google.protobuf)
    implementation(libs.commons.compress)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(testFixtures(project(":iac-common")))
}
