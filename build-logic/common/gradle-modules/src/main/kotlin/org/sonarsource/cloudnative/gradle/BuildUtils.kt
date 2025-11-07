/*
 * SonarSource Cloud Native Gradle Modules
 * Copyright (C) 2024-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonarsource.cloudnative.gradle

import java.io.File
import java.util.HashSet
import java.util.Locale
import java.util.jar.JarInputStream
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.logging.Logger
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Exec
import org.gradle.internal.os.OperatingSystem

fun isCi() = System.getenv("CI")?.equals("true") == true

fun Project.signingCondition(): Boolean {
    val branch = System.getenv("GITHUB_REF_NAME") ?: System.getenv("CIRRUS_BRANCH") ?: ""
    return (branch == "master" || branch.matches("branch-.+".toRegex())) &&
        gradle.taskGraph.hasTask(":artifactoryPublish")
}

internal fun RepositoryHandler.repox(
    repository: String,
    providers: ProviderFactory,
    fileOperations: FileOperations,
): MavenArtifactRepository =
    maven {
        name = "artifactory"
        url = fileOperations.uri("https://repox.jfrog.io/repox/$repository")

        // This authentication relies on env vars configured on Cirrus CI or on Gradle properties (`-P<prop>` flags or `gradle.properties` file)
        val artifactoryUsername = providers.environmentVariable("ARTIFACTORY_PRIVATE_USERNAME")
            .orElse(providers.environmentVariable("ARTIFACTORY_USERNAME"))
            .orElse(providers.gradleProperty("artifactoryUsername"))
        val artifactoryPassword = providers.environmentVariable("ARTIFACTORY_PRIVATE_PASSWORD")
            .orElse(providers.environmentVariable("ARTIFACTORY_ACCESS_TOKEN"))
            .orElse(providers.gradleProperty("artifactoryPassword"))

        if (artifactoryUsername.isPresent && artifactoryPassword.isPresent) {
            authentication {
                credentials {
                    username = artifactoryUsername.get()
                    password = artifactoryPassword.get()
                }
            }
        }
    }

/**
 * Configures this `Exec` task to call `make.sh` or `make.bat` depending on the operating system.
 */
fun Exec.callMake(arg: String) {
    if (OperatingSystem.current().isWindows) {
        commandLine("cmd", "/c", "make.bat", arg)
    } else {
        commandLine("./make.sh", arg)
    }
}

fun enforceJarSize(
    file: File,
    minSize: Long,
    maxSize: Long,
    logger: Logger,
) {
    val size = file.length()
    if (size < minSize) {
        throw GradleException("${file.path} size ($size) too small. Min is $minSize")
    } else if (size > maxSize) {
        throw GradleException("${file.path} size ($size) too large. Max is $maxSize")
    }
    logger.info("Artifact ${file.name} has size $size")
}

fun checkJarEntriesPathUniqueness(file: File) {
    val allNames = HashSet<String>()
    val duplicatedNames = HashSet<String>()
    file.inputStream().use { input ->
        JarInputStream(input).use { jarInput ->
            for (jarEntry in generateSequence { jarInput.nextJarEntry }) {
                if (!allNames.add(jarEntry.name)) {
                    duplicatedNames.add(jarEntry.name)
                }
            }
        }
    }
    if (duplicatedNames.isNotEmpty()) {
        throw GradleException("Duplicated entries in the jar: '${file.path}': ${duplicatedNames.joinToString(", ")}")
    }
}

fun Project.commitHashProvider(ref: String = "HEAD") =
    providers.exec {
        commandLine("git", "rev-parse", ref)
    }.standardOutput.asText

fun getPlatform(): String {
    val os = System.getProperty("os.name").lowercase(Locale.getDefault())
    return when {
        os.contains("mac") -> "darwin"
        os.contains("win") -> "windows"
        else -> "linux"
    }
}

fun getArchitecture(): String {
    val arch = System.getProperty("os.arch").lowercase(Locale.getDefault())
    return when {
        arch.contains("aarch64") -> "arm64"
        else -> "amd64"
    }
}
