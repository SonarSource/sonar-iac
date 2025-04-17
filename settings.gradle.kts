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
pluginManagement {
    includeBuild("build-logic/common")
    includeBuild("build-logic/iac")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.diffplug.blowdryerSetup") version "1.7.1"
    id("org.sonarsource.cloud-native.common-settings")
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "iac"

include(":iac-extensions")
include(":sonar-helm-for-iac")
include(":sonar-iac-plugin")

include(":iac-common")
include(":iac-extensions:terraform")
include(":iac-extensions:docker")
include(":iac-extensions:kubernetes")
include(":iac-extensions:arm")
include(":iac-extensions:cloudformation")
include(":iac-extensions:jvm-framework-config")

gradle.allprojects {
    // this value is present on CI
    val buildNumber: String? = System.getProperty("buildNumber")
    project.extra["buildNumber"] = buildNumber
    val version = properties["version"] as String
    if (version.endsWith("-SNAPSHOT") && buildNumber != null) {
        val versionSuffix = if (version.count { it == '.' } == 1) ".0.$buildNumber" else ".$buildNumber"
        project.version =
            version.replace("-SNAPSHOT", versionSuffix).also {
                logger.lifecycle("Project ${project.name} version set to $it")
            }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// "extraSettings.gradle" should not be renamed "settings.gradle" to not create a wrong project rootDir
var extraSettings = File(rootDir, "private/extraSettings.gradle.kts")
if (extraSettings.exists()) {
    apply(extraSettings)
}
