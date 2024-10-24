plugins {
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
}

description = "SonarSource IaC Analyzer :: Extensions :: Kubernetes"

val common by sourceSets.creating

configurations {
    // Make `main` source set have same dependencies as `common`.
    getByName("implementation").extendsFrom(getByName("commonImplementation"))
    getByName("runtimeOnly").extendsFrom(getByName("commonRuntimeOnly"))
}

val commonConfiguration = configurations.create("common")
val commonJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("common")
    from(common.output)
}
artifacts.add(commonConfiguration.name, commonJar)

tasks.named<Jar>("jar") {
    from(common.output)
    // Because same packages exist in both source sets, package-info.java files will be duplicated
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    "commonImplementation"(projects.iacCommon)

    api(common.output)
    api(libs.sonar.lint.plugin.api)
    implementation(project(":sonar-helm-for-iac", "goBinaries"))
    implementation(project(":sonar-helm-for-iac"))
    implementation(libs.google.protobuf)

    testRuntimeOnly(project(":sonar-helm-for-iac", "goBinaries"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sonar.analyzer.test.commons)
    testImplementation(testFixtures(project(":iac-common")))
    testImplementation(libs.apache.commons.lang)
    testImplementation(libs.sonar.lint.analysis.engine)
}

tasks.named<JacocoReport>("jacocoTestReport") {
    sourceSets(common)
}
