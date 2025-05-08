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
import org.gradle.kotlin.dsl.registering
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.sonarsource.cloudnative.gradle.GO_BINARY_OUTPUT_DIR
import org.sonarsource.cloudnative.gradle.GoBuild
import org.sonarsource.cloudnative.gradle.allGoSourcesAndMakeScripts
import org.sonarsource.cloudnative.gradle.callMake
import org.sonarsource.cloudnative.gradle.getArchitecture
import org.sonarsource.cloudnative.gradle.getPlatform
import org.sonarsource.cloudnative.gradle.goSources

val goBinaries: Configuration by configurations.creating
val goBinariesJar by tasks.registering(Jar::class) {
    group = "build"
    dependsOn("compileGo")
    archiveClassifier.set("binaries")
    from(GO_BINARY_OUTPUT_DIR)
}
artifacts.add(goBinaries.name, goBinariesJar)

val goVersion = providers.environmentVariable("GO_VERSION")
    .orElse(providers.gradleProperty("goVersion"))
    .orNull ?: error("Either `GO_VERSION` env variable or `goVersion` Gradle property must be set")
val isCrossCompile = providers.environmentVariable("GO_CROSS_COMPILE").orElse("0")
val isCi: Boolean = System.getenv("CI")?.equals("true") == true
val goBuildExtension = extensions.create("goBuild", GoBuild::class)
goBuildExtension.dockerWorkDir.convention("/home/sonarsource/${project.name}")
goBuildExtension.additionalOutputFiles.convention(emptySet())

if (isCi) {
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
        outputs.files(goBuildExtension.additionalOutputFiles)
        outputs.cacheIf { true }

        callMake("build")
    }

    val goLangCiLint by tasks.registering(Exec::class) {
        description = "Run an external Go linter."
        group = "verification"

        val reportPath = layout.buildDirectory.file("reports/golangci-lint-report.xml")
        inputs.files(goSources())
        inputs.property("goVersion", goVersion)

        outputs.files(reportPath)
        outputs.cacheIf { true }

        commandLine(
            "golangci-lint",
            "run",
            "--go=${inputs.properties["goVersion"]}",
            "--out-format=checkstyle:${reportPath.get().asFile}"
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
    val buildDockerImage by tasks.registering(Exec::class) {
        description = "Build the docker image to build the Go code."
        group = "build"

        inputs.file(goBuildExtension.dockerfile)
        inputs.file("$projectDir/go.mod")
        inputs.file("$projectDir/go.sum")
        // Task outputs are not set, because it is too difficult to check if image is built;
        // We can ignore Gradle caches here, because Docker takes care of its own caches anyway.
        errorOutput = System.out

        val uidProvider = objects.property<Long>()
        val os = DefaultNativePlatform.getCurrentOperatingSystem()
        if (os.isLinux || os.isMacOsX) {
            // UID of the user inside the container should match this of the host user, otherwise files from the host will be not accessible by the container.
            val uid = com.sun.security.auth.module.UnixSystem().uid
            uidProvider.set(uid)
        }

        val noTrafficInspection = "false" == System.getProperty("trafficInspection")

        val arguments = buildList {
            add("docker")
            add("buildx")
            add("build")
            add("--file")
            add(goBuildExtension.dockerfile.asFile.get().absolutePath)
            if (noTrafficInspection) {
                add("--build-arg")
                add("BUILD_ENV=dev")
            } else {
                add("--network=host")
                add("--build-arg")
                add("BUILD_ENV=dev_custom_cert")
            }
            if (uidProvider.isPresent) {
                add("--build-arg")
                add("UID=${uidProvider.get()}")
            }
            add("--build-arg")
            add("GO_VERSION=$goVersion")
            add("--platform")
            add("linux/amd64")
            add("-t")
            add("${project.name}-builder")
            add("--progress")
            add("plain")
            add("${project.projectDir}")
        }

        commandLine(arguments)
    }

    val compileGo by tasks.registering(Exec::class) {
        description = "Build the Go executable inside a Docker container."
        group = "build"
        dependsOn(buildDockerImage)
        errorOutput = System.out

        inputs.files(allGoSourcesAndMakeScripts())
        inputs.property("goCrossCompile", isCrossCompile)
        outputs.files(goBuildExtension.additionalOutputFiles)
        outputs.dir(GO_BINARY_OUTPUT_DIR)
        outputs.cacheIf { true }

        val platform = getPlatform()
        val arch = getArchitecture()

        val workDir = goBuildExtension.dockerWorkDir.get()
        commandLine(
            "docker",
            "run",
            "--rm",
            "--network=host",
            "--platform",
            "linux/amd64",
            "--mount",
            "type=bind,source=${project.projectDir},target=$workDir",
            "--env",
            "GO_CROSS_COMPILE=${inputs.properties["goCrossCompile"]}",
            "${project.name}-builder",
            "bash",
            "-c",
            "cd $workDir && ./make.sh clean && ./make.sh build $platform $arch && ./make.sh test"
        )
    }

    tasks.named("assemble") {
        dependsOn(compileGo)
    }
}
