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
package org.sonarsource.cloudnative.gradle

import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.SetProperty

const val GO_BINARY_OUTPUT_DIR = "build/executable"

interface GoBuild {
    val dockerfile: RegularFileProperty

    /**
     * Working directory inside the container, e.g. `/home/sonarsource/sonar-go-to-slang`.
     */
    val dockerWorkDir: Property<String>

    /**
     * Commands to run inside the container after `cd`-ing to the `dockerWorkDir`.
     * For each command in this list, a separate Gradle task wrapping `docker run` will be created.
     */
    val dockerCommands: MapProperty<String, String>

    val additionalOutputFiles: SetProperty<RegularFile>
}

val Project.goVersion get() = providers.environmentVariable("GO_VERSION")
    .orElse(providers.gradleProperty("goVersion"))
    .orNull ?: error("Either `GO_VERSION` env variable or `goVersion` Gradle property must be set")

val Project.goLangCiLintVersion get() = providers.environmentVariable("GOLANG_CI_LINT_VERSION")
    .orElse(providers.gradleProperty("goLangCiLintVersion"))
    .orNull ?: error("Either `GOLANG_CI_LINT_VERSION` env variable or `goLangCiLintVersion` Gradle property must be set")

val Project.isCrossCompile: Provider<String> get() = providers.environmentVariable("GO_CROSS_COMPILE").orElse("0")

fun Project.allGoSourcesAndMakeScripts(): FileTree =
    fileTree(projectDir).matching {
        include(
            "**/*.go",
            "**/go.mod",
            "**/go.sum",
            "make.bat",
            "make.sh"
        )
        exclude("build/**")
    }

fun Project.goSources(): FileTree =
    fileTree(projectDir).matching {
        include("**/*.go")
        exclude("build/**", "**/*_generated.go")
    }
