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
    id("org.sonarsource.cloud-native.java-conventions")
    id("org.sonarsource.cloud-native.code-style-conventions")
    id("org.sonarsource.cloud-native.publishing-configuration")
    id("com.gradleup.shadow")
}

val cleanupTask = tasks.register<Delete>("cleanupOldVersion") {
    group = "build"
    description = "Clean up jars of old plugin version"

    // those variables need to be calculated in the configuration phase to enable Configuration cache
    // https://docs.gradle.org/8.12.1/userguide/configuration_cache.html#config_cache:requirements:disallowed_types
    var projectName = project.name
    var projectVersion = "${project.version}"
    delete(
        fileTree(layout.buildDirectory.dir("libs")).matching {
            include("$projectName-*.jar")
            exclude("$projectName-$projectVersion-*.jar")
        }
    )
}

artifacts {
    archives(tasks.shadowJar)
}

tasks.shadowJar {
    dependsOn(cleanupTask)
}
