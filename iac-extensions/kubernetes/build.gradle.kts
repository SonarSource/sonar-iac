/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
plugins {
    id("org.sonarsource.cloud-native.code-style-conventions")
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

    // Alternatively, `implementation(common.output)` could be used. However, it cannot be analyzed by the releasability check.
    implementation(project(":iac-extensions:kubernetes", configuration = "common"))
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
