plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Kubernetes"

dependencies {
    api(project(":iac-common"))
    api(libs.sonar.lint.plugin.api)
    implementation(project(":sonar-helm-for-iac", "goBinaries"))
    implementation(project(":sonar-helm-for-iac"))
    implementation(libs.google.protobuf)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sonar.analyzer.test.commons)
    testImplementation(testFixtures(project(":iac-common")))
    testImplementation(libs.apache.commons.lang)
    testImplementation("org.sonarsource.sonarlint.core:sonarlint-analysis-engine:9.3.1.74774")
}
