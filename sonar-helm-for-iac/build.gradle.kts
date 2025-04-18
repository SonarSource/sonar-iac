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
import com.google.protobuf.gradle.id
import de.undercouch.gradle.tasks.download.Download
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("org.sonarsource.iac.java-conventions")
    id("com.diffplug.spotless")
    alias(libs.plugins.google.protobuf)
    alias(libs.plugins.download)
}

description = "SonarSource IaC Analyzer :: Sonar Helm for IaC"

val goBinaries: Configuration by configurations.creating
val goBinariesJar by tasks.registering(Jar::class) {
    group = "build"
    dependsOn("compileGoCode")
    archiveClassifier.set("binaries")
    from("build/executable")
}
artifacts.add(goBinaries.name, goBinariesJar)

val protocBinaryVersion = libs.versions.google.protobuf.go.get()
val downloadProtocGenGo by tasks.registering(Download::class) {
    group = "build"
    description = "Download an archive with the protoc-gen-go binary for the current platform."

    val os = DefaultNativePlatform.getCurrentOperatingSystem()
    val arch = DefaultNativePlatform.getCurrentArchitecture()
    val suffix = buildString {
        when {
            os.isLinux -> append("linux")
            os.isMacOsX -> append("darwin")
            os.isWindows -> append("windows")
            else -> throw IllegalStateException("Unsupported OS: $os")
        }
        append('.')
        when {
            arch.isAmd64 -> append("amd64")
            arch.isI386 -> append("386")
            arch.isArm64 -> append("arm64")
            else -> throw IllegalStateException("Unsupported architecture: $arch")
        }
        when {
            os.isWindows -> append(".zip")
            else -> append(".tar.gz")
        }
    }

    inputs.property("binaryVersion", protocBinaryVersion)
    src(
        "https://github.com/protocolbuffers/protobuf-go/releases/download/v$protocBinaryVersion/protoc-gen-go.v$protocBinaryVersion.$suffix"
    )
    dest(layout.buildDirectory.file("protoc-gen-go/protoc-gen-go.v$protocBinaryVersion.$suffix"))
    onlyIfModified(false)
    overwrite(false)
    outputs.cacheIf { true }
}

val extractProtocGenGo by tasks.registering(Copy::class) {
    group = "build"
    description = "Extract the protoc-gen-go binary from the downloaded archive."
    inputs.files(downloadProtocGenGo)

    val archiveFile = downloadProtocGenGo.get().dest
    val archiveTree = if (archiveFile.name.endsWith(".zip")) ::zipTree else ::tarTree
    from(archiveTree(archiveFile))
    into(layout.buildDirectory.dir("protoc-gen-go/bin"))
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.google.protobuf.asProvider().get()}"
    }
    plugins {
        id("go") {
            path = layout.buildDirectory.map {
                val extension = if (DefaultNativePlatform.getCurrentOperatingSystem().isWindows) ".exe" else ""
                it.file("protoc-gen-go/bin/protoc-gen-go$extension")
            }.get().asFile.absolutePath
        }
    }
    generateProtoTasks {
        all().configureEach {
            plugins {
                id("go") {
                    // Only the subdirectory is configurable, with the base directory being `build/generated/source/proto/main/go`.
                    // Result should be `$projectDir/src`.
                    outputSubDir = "../../../../../src"
                }
                dependsOn(extractProtocGenGo)
            }
        }
    }
}

val isCi: Boolean = System.getenv("CI")?.equals("true") ?: false

// CI - run the build of go code and protobuf with protoc and local make.sh/make.bat script
if (isCi) {
    // Define and trigger tasks in this order: clean, compile and test go code
    tasks.register<Exec>("cleanGoCode") {
        description = "Clean all compiled version of the go code."
        group = "build"

        callMake(this, "clean")
    }

    tasks.register<Exec>("compileGoCode") {
        description = "Compile the go code for the local system."
        group = "build"
        dependsOn("generateProto")

        inputs.property("GO_CROSS_COMPILE", System.getenv("GO_CROSS_COMPILE") ?: "0")
        inputs.files(
            fileTree(projectDir).matching {
                include(
                    "*.go",
                    "**/*.go",
                    "**/go.mod",
                    "**/go.sum",
                    "go.work",
                    "go.work.sum",
                    "make.bat",
                    "make.sh"
                )
                exclude("build/**")
            }
        )
        outputs.dir("build/executable")
        outputs.cacheIf { true }

        callMake(this, "build")
    }

    tasks.register<Exec>("lintGoCode") {
        description = "Run an external Go linter."
        group = "verification"

        val reportPath = layout.buildDirectory.file("reports/golangci-lint-report.xml")
        inputs.files(
            fileTree(projectDir).matching {
                include("src/**/*.go")
            }
        )
        outputs.files(reportPath)
        outputs.cacheIf { true }

        commandLine(
            "golangci-lint",
            "run",
            "--go=${requireNotNull(System.getenv("GO_VERSION")) { "Go version is unset in the environment" }}",
            "--out-format=checkstyle:${reportPath.get().asFile}"
        )
        // golangci-lint returns non-zero exit code if there are issues, we don't want to fail the build in this case.
        // A report with issues will be later ingested by SonarQube.
        isIgnoreExitValue = true
    }

    tasks.register<Exec>("testGoCode") {
        description = "Test the executable produced by the compile go code step."
        group = "build"

        dependsOn("compileGoCode")
        callMake(this, "test")
    }

    tasks.named("clean") {
        dependsOn("cleanGoCode")
    }

    tasks.named("assemble") {
        dependsOn("compileGoCode")
    }

    tasks.named("test") {
        dependsOn("testGoCode")
    }

    // spotless is enabled only for CI, because spotless relies on Go installation being available on the machine and not in a container.
    // To ensure locally that the code is properly formatted, either run Gradle with `env CI=true`, or run `gofmt -w .` directly, or rely
    // on auto-formatting in Intellij Go plugin, which also calls `gofmt`.
    spotless {
        go {
            val goVersion = providers.environmentVariable("GO_VERSION").getOrElse("1.23.4")
            gofmt("go$goVersion")
            target("**/*.go")
            targetExclude("**/*.pb.go")
        }
    }
    tasks.named("check") {
        dependsOn("spotlessCheck")
    }
}

fun callMake(
    execTask: Exec,
    arg: String,
) {
    if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
        execTask.commandLine("cmd", "/c", "make.bat", arg)
    } else {
        execTask.commandLine("./make.sh", arg)
    }
}

// Local - run the build of go code with docker images
if (!isCi) {
    tasks.register<Exec>("buildDockerImage") {
        description = "Build the docker image to build the go code."
        group = "build"

        inputs.file("$rootDir/build-logic/iac/Dockerfile")
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
            add(rootProject.file("build-logic/iac/Dockerfile").absolutePath)
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
            add("--platform")
            add("linux/amd64")
            add("-t")
            add("sonar-iac-helm-builder")
            add("--progress")
            add("plain")
            add("${project.projectDir}")
        }

        commandLine(arguments)
    }

    tasks.register<Exec>("compileGoCode") {
        description = "Build the go code from the docker image."
        group = "build"
        dependsOn("buildDockerImage")
        errorOutput = System.out

        inputs.files(
            fileTree(projectDir).matching {
                include(
                    "*.go",
                    "**/*.go",
                    "**/go.mod",
                    "**/go.sum",
                    "go.work",
                    "go.work.sum",
                    "proto/template-evaluation.proto",
                    "make.sh",
                    "make.bat"
                )
                exclude("build/**")
            }
        )
        outputs.dir("build/executable")
        outputs.cacheIf { true }

        val platform = getPlatform()
        val arch = getArchitecture()

        commandLine(
            "docker",
            "run",
            "--rm",
            "--network=host",
            "--platform",
            "linux/amd64",
            "--mount",
            "type=bind,source=${project.projectDir},target=/home/sonarsource/sonar-helm-for-iac",
            "--env",
            "GO_CROSS_COMPILE=${System.getenv("GO_CROSS_COMPILE") ?: "1"}",
            "sonar-iac-helm-builder",
            "bash",
            "-c",
            "cd /home/sonarsource/sonar-helm-for-iac && ./make.sh clean && ./make.sh build $platform $arch && ./make.sh test"
        )
    }

    tasks.named("assemble") {
        dependsOn("compileGoCode")
    }
}

dependencies {
    implementation(libs.google.protobuf)
}

sourceSets {
    main {
        proto {
            srcDir("proto")
            include("*.proto")
        }
    }
}

fun getPlatform(): String {
    val os = DefaultNativePlatform.getCurrentOperatingSystem()
    return when {
        os.isLinux -> "linux"
        os.isMacOsX -> "darwin"
        os.isWindows -> "windows"
        else -> error("Unsupported OS: $os")
    }
}

fun getArchitecture(): String {
    val arch = DefaultNativePlatform.getCurrentArchitecture()
    return when {
        arch.isAmd64 -> "amd64"
        arch.isArm64 -> "arm64"
        else -> error("Unsupported architecture: $arch")
    }
}
