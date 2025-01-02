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
    id("org.sonarsource.iac.code-style-convention")
    id("org.sonarsource.iac.java-conventions")
    id("java-library")
    id("java-test-fixtures")
}

description = "SonarSource IaC Analyzer :: Common"

dependencies {
    api(libs.sonar.plugin.api)
    api(libs.sonar.analyzer.commons)
    api(libs.sslr)
    api(libs.minimaljson)
    api(libs.snakeyaml)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sonar.analyzer.test.commons)

    testFixturesImplementation(libs.junit.jupiter)
    testFixturesImplementation(libs.assertj.core)
    testFixturesImplementation(libs.mockito.core)
    testFixturesImplementation(libs.sonar.plugin.api.impl)
    testFixturesImplementation(libs.sonar.plugin.api.test.fixtures)
    testFixturesImplementation(libs.sonar.analyzer.test.commons)
    testFixturesImplementation(libs.apache.commons.lang)
}
