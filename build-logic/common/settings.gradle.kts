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
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.develocity") version "4.0.3"
}

rootProject.name = "cloud-native-gradle-modules"
include("gradle-modules")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

develocity {
    server.set("https://develocity.sonar.build")
}

val isCI = System.getenv("CI") != null
buildCache {
    local {
        isEnabled = !isCI
    }
    remote(develocity.buildCache) {
        isEnabled = true
        isPush = isCI
    }
}
