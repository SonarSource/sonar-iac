/*
 * SonarSource Cloud Native Gradle Modules
 * Copyright (C) 2024-2026 SonarSource Sàrl
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
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.sonarsource.cloudnative.gradle.GO_BINARY_OUTPUT_DIR
import org.sonarsource.cloudnative.gradle.GO_LICENSES_OUTPUT_DIR
import org.sonarsource.cloudnative.gradle.GoBuild
import org.sonarsource.cloudnative.gradle.allGoSourcesAndMakeScripts
import org.sonarsource.cloudnative.gradle.crossCompileEnv
import org.sonarsource.cloudnative.gradle.goLangCiLintVersion
import org.sonarsource.cloudnative.gradle.goVersion
import org.sonarsource.cloudnative.gradle.isCi

val goBuildExtension = extensions.findByType<GoBuild>() ?: extensions.create<GoBuild>("goBuild")

val buildDockerImage by tasks.registering(Exec::class) {
    description = "Build the docker image for a pre-configured Go environment."
    group = "build"
    onlyIf { isCi().not() }

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
        add("--build-arg")
        add("GOLANG_CI_LINT_VERSION=$goLangCiLintVersion")
        add("--build-context")
        add("root=${project.rootDir.absolutePath}")
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

val dockerTasks = goBuildExtension.dockerCommands.map { tasksToCommands ->
    tasksToCommands.forEach { taskName, command ->
        tasks.register<Exec>(taskName) {
            description = "Build the Go executable inside a Docker container."
            group = "build"
            onlyIf { isCi().not() }
            dependsOn(buildDockerImage)
            errorOutput = System.out

            inputs.files(allGoSourcesAndMakeScripts())
            inputs.property("goCrossCompile", crossCompileEnv)
            outputs.files(goBuildExtension.additionalOutputFiles)
            outputs.dir(GO_BINARY_OUTPUT_DIR)
            outputs.dir(GO_LICENSES_OUTPUT_DIR)
            outputs.cacheIf { true }

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
                "--env",
                "GO_VERSION=$goVersion",
                "--env",
                "GOLANG_CI_LINT_VERSION=$goLangCiLintVersion",
                "${project.name}-builder",
                "bash",
                "-c",
                "cd $workDir && $command"
            )
        }
    }
}

project.afterEvaluate {
    // Register the tasks after value of `dockerCommands` is known
    dockerTasks.get()
}
