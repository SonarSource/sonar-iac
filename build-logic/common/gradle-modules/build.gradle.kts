/*
 * SonarSource Cloud Native Gradle Modules
 * Copyright (C) 2024-2025 SonarSource SA
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
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(libs.develocity)
    implementation(libs.diffplug.spotless)
    implementation(libs.diffplug.blowdryer.setup)
    implementation(platform(libs.jackson.bom)) {
        because("Override the transitive dependency from jfrog plugin")
    }
    implementation(libs.jfrog.buildinfo.gradle) {
        exclude("ch.qos.logback", "logback-core")
    }
    implementation(libs.shadow)
}

gradlePlugin {
    plugins {
        create("org.sonarsource.cloud-native.common-settings") {
            id = "org.sonarsource.cloud-native.common-settings"
            implementationClass = "org.sonarsource.cloudnative.gradle.CommonSettingsPlugin"
        }
    }
}

spotless {
    kotlin {
        ktlint().setEditorConfigPath("$rootDir/.editorconfig")
        target("src/**/*.kt")
        licenseHeaderFile(rootProject.file("LICENSE_HEADER")).updateYearWithLatest(true)
    }

    val kotlinGradleDelimiter = "(package|import|plugins|pluginManagement|dependencyResolutionManagement|repositories) "
    kotlinGradle {
        ktlint().setEditorConfigPath("$rootDir/.editorconfig")
        target("*.gradle.kts", "src/**/*.gradle.kts")
        licenseHeaderFile(rootProject.file("LICENSE_HEADER"), kotlinGradleDelimiter).updateYearWithLatest(true)
    }
}
