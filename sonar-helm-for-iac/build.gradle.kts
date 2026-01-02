/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
import com.google.protobuf.gradle.id
import de.undercouch.gradle.tasks.download.Download
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    id("org.sonarsource.cloud-native.code-style-conventions")
    id("org.sonarsource.cloud-native.java-conventions")
    id("org.sonarsource.cloud-native.go-binary-builder")
    alias(libs.plugins.google.protobuf)
    alias(libs.plugins.download)
}

description = "SonarSource IaC Analyzer :: Sonar Helm for IaC"

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

goBuild {
    dockerfile = layout.settingsDirectory.file("build-logic/iac/Dockerfile")
    additionalOutputFiles = objects.setProperty()
    dockerWorkDir = "/home/sonarsource/sonar-helm-for-iac"
}

val isCi: Boolean = System.getenv("CI")?.equals("true") == true

if (isCi) {
    // spotless is enabled only for CI, because spotless relies on Go installation being available on the machine and not in a container.
    // To ensure locally that the code is properly formatted, either run Gradle with `env CI=true`, or run `gofmt -w .` directly, or rely
    // on auto-formatting in Intellij Go plugin, which also calls `gofmt`.
    spotless {
        go {
            val goVersion = providers.environmentVariable("GO_VERSION")
                .orElse(providers.gradleProperty("goVersion"))
                .orNull ?: error("Either GO_VERSION env variable or goVersion property must be set")
            gofmt("go$goVersion")
            target("**/*.go")
            targetExclude("**/*.pb.go")
        }
    }
    tasks.named("check") {
        dependsOn("spotlessCheck")
    }
} else {
    spotless {
        java {
            // No Java sources in this project
            target("")
        }
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
