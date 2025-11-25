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
import org.gradle.kotlin.dsl.registering
import org.sonarsource.cloudnative.gradle.GO_BINARY_OUTPUT_DIR
import org.sonarsource.cloudnative.gradle.GO_LICENSES_OUTPUT_DIR
import org.sonarsource.cloudnative.gradle.GoBuild
import org.sonarsource.cloudnative.gradle.allGoSourcesAndMakeScripts
import org.sonarsource.cloudnative.gradle.callMake
import org.sonarsource.cloudnative.gradle.getArchitecture
import org.sonarsource.cloudnative.gradle.getPlatform
import org.sonarsource.cloudnative.gradle.goLangCiLintVersion
import org.sonarsource.cloudnative.gradle.goSources
import org.sonarsource.cloudnative.gradle.goVersion
import org.sonarsource.cloudnative.gradle.isCi
import org.sonarsource.cloudnative.gradle.isCrossCompile

plugins {
    id("org.sonarsource.cloud-native.go-docker-environment")
}

val goBinaries: Configuration by configurations.creating
val goBinariesJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("binaries")
    from(GO_BINARY_OUTPUT_DIR)
}
artifacts.add(goBinaries.name, goBinariesJar)

val goBuildExtension = extensions.findByType<GoBuild>() ?: extensions.create<GoBuild>("goBuild")
goBuildExtension.dockerWorkDir.convention("/home/sonarsource/${project.name}")
goBuildExtension.additionalOutputFiles.convention(emptySet())
goBuildExtension.dockerCommands.convention(
    mapOf(
        "dockerCompileGo" to "./make.sh clean && ./make.sh build ${getPlatform()} ${getArchitecture()} && ./make.sh test"
    )
)

if (isCi()) {
    val cleanGoCode by tasks.registering(Exec::class) {
        description = "Clean all compiled version of the go code."
        group = "build"

        callMake("clean")
    }

    val compileGo by tasks.registering(Exec::class) {
        description = "Compile the go code for the local system."
        group = "build"

        inputs.property("GO_CROSS_COMPILE", isCrossCompile)
        inputs.files(allGoSourcesAndMakeScripts())

        outputs.dir(GO_BINARY_OUTPUT_DIR)
        outputs.dir(GO_LICENSES_OUTPUT_DIR)
        outputs.files(goBuildExtension.additionalOutputFiles)
        outputs.cacheIf { true }

        environment("GO_CROSS_COMPILE", isCrossCompile.get())
        callMake("build")
    }
    goBinariesJar.configure { dependsOn(compileGo) }

    val goLangCiLint by tasks.registering(Exec::class) {
        description = "Run an external Go linter."
        group = "verification"

        val reportPath = layout.buildDirectory.file("reports/golangci-lint-report.xml")
        inputs.files(goSources())
        inputs.property("goVersion", goVersion)
        inputs.property("goLangCiLintVersion", goLangCiLintVersion)

        outputs.files(reportPath)
        outputs.cacheIf { true }

        commandLine(
            "golangci-lint",
            "run",
            // Don't limit the number of issues in the report
            "--max-issues-per-linter=0",
            "--max-same-issues=0",
            // Output format for SonarQube ingestion
            "--output.checkstyle.path",
            "${reportPath.get().asFile}"
        )
        // golangci-lint returns non-zero exit code if there are issues, we don't want to fail the build in this case.
        // A report with issues will be later ingested by SonarQube.
        isIgnoreExitValue = true
    }

    val testGoCode by tasks.registering(Exec::class) {
        description = "Test the executable produced by the compile go code step."
        group = "verification"

        dependsOn(compileGo)
        callMake("test")
    }

    tasks.named("clean") {
        dependsOn(cleanGoCode)
    }

    tasks.named("assemble") {
        dependsOn(compileGo)
    }

    tasks.named("test") {
        dependsOn(testGoCode)
    }

    tasks.named("check") {
        dependsOn(goLangCiLint)
    }

    rootProject.tasks.named("sonar") {
        // As the Go linter produces a report to be ingested by SonarQube, we need to add an explicit dependency to it.
        // See https://docs.sonarsource.com/sonarqube-server/latest/analyzing-source-code/scanners/sonarscanner-for-gradle/#task-dependencies
        dependsOn(goLangCiLint)
    }
} else {
    val dockerTaskNames = goBuildExtension.dockerCommands.map { it.keys }
    tasks.named("assemble") {
        dependsOn(dockerTaskNames)
    }
    goBinariesJar.configure {
        dependsOn(dockerTaskNames)
    }
}
