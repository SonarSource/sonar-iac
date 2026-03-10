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
import groovy.json.JsonSlurper
import java.io.File
import java.nio.file.Files
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.sonarsource.cloudnative.gradle.SwiftLicenseGenerationConfig
import org.sonarsource.cloudnative.gradle.areDirectoriesEqual
import org.sonarsource.cloudnative.gradle.copyDirectory

/**
 * This plugin collects license files from third-party Swift runtime dependencies and places them
 * into a resources folder. It provides:
 * - A task to collect licenses from Swift packages resolved by Swift Package Manager
 * - A validation task to ensure committed license files are up-to-date
 * - A task to regenerate the license files into the resources folder
 */

val swiftLicenseConfig =
    extensions.findByType<SwiftLicenseGenerationConfig>()
        ?: extensions.create<SwiftLicenseGenerationConfig>("swiftLicenseGenerationConfig")

swiftLicenseConfig.buildSwiftLicenseFilesDir.convention(
    project.layout.buildDirectory.dir("swift-licenses").get().asFile
)
swiftLicenseConfig.projectLicenseFile.convention(project.rootDir.resolve("LICENSE"))
swiftLicenseConfig.analyzerDir.convention(
    project.rootDir.resolve("analyzer")
)

val resourceLicenseDir = project.rootDir.resolve("sonar-swift-plugin/src/main/resources/swift-licenses")

/**
 * Data class representing a Swift package dependency from `swift package show-dependencies --format json`.
 */
data class SwiftPackageDep(
    val identity: String,
    val name: String,
    val url: String,
    val path: String,
)

val collectSwiftLicenses = tasks.register("collectSwiftLicenses") {
    description = "Collects license files from Swift Package Manager dependencies"
    group = "licenses"

    doLast {
        val packages = parseNonDevPackages(swiftLicenseConfig.analyzerDir.get())

        val outputDir = swiftLicenseConfig.buildSwiftLicenseFilesDir.get().resolve("THIRD_PARTY_LICENSES")
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        logger.lifecycle("Collecting licenses for ${packages.size} runtime Swift packages...")

        var collected = 0
        for (pkg in packages.sortedBy { it.identity }) {
            val pkgDir = File(pkg.path)

            val licenseFile = findLicenseFile(pkgDir)
            if (licenseFile != null) {
                licenseFile.copyTo(outputDir.resolve("${pkg.identity}-LICENSE.txt"), overwrite = true)
                collected++
            } else {
                logger.warn("No LICENSE file found for package: ${pkg.identity} (looked in ${pkg.path})")
            }
        }

        // Copy project license
        val projectLicense = swiftLicenseConfig.projectLicenseFile.get()
        projectLicense.copyTo(
            swiftLicenseConfig.buildSwiftLicenseFilesDir.get().resolve("LICENSE"),
            overwrite = true
        )

        logger.lifecycle("Collected $collected license files from ${packages.size} packages.")
    }
}

val validateSwiftLicenses = tasks.register("validateSwiftLicenseFiles") {
    description = "Validate that generated Swift license files match the committed ones"
    group = "validation"
    dependsOn(collectSwiftLicenses)

    doLast {
        val generated = swiftLicenseConfig.buildSwiftLicenseFilesDir.get()
        val committed = resourceLicenseDir
        if (!areDirectoriesEqual(generated, committed, logger)) {
            throw GradleException(
                """
                [FAILURE] Swift license file validation failed!
                Generated license files differ from committed files at $resourceLicenseDir.
                To update the committed license files, run './gradlew generateSwiftLicenseResources' and commit the changes.
                """.trimIndent()
            )
        }
        logger.lifecycle("Swift license file validation succeeded: generated files match committed ones.")
    }
}

val generateSwiftLicenseResources = tasks.register("generateSwiftLicenseResources") {
    description = "Copies generated Swift license files to the resources directory"
    group = "licenses"
    dependsOn(collectSwiftLicenses)

    doLast {
        val generated = swiftLicenseConfig.buildSwiftLicenseFilesDir.get()
        val destination = resourceLicenseDir
        Files.createDirectories(destination.toPath())
        copyDirectory(generated, destination, logger)
    }
}

tasks.named("validateLicenseFiles") {
    dependsOn(validateSwiftLicenses)
}

tasks.named("generateLicenseResources") {
    dependsOn(generateSwiftLicenseResources)
}

/**
 * Runs `swift package show-dependencies --format json` in the analyzer directory and parses the output
 * to extract non-dev dependency packages (all resolved packages, since Swift SPM dependencies
 * listed in Package.swift are used by production targets).
 *
 * Recursively flattens the dependency tree to include transitive dependencies.
 */
fun parseNonDevPackages(analyzerDir: File): Set<SwiftPackageDep> {
    val process = ProcessBuilder("swift", "package", "show-dependencies", "--format", "json")
        .directory(analyzerDir)
        .redirectErrorStream(false)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        val stderr = process.errorStream.bufferedReader().readText()
        error("swift package show-dependencies failed with exit code $exitCode: $stderr")
    }

    val json = JsonSlurper().parseText(output) as? Map<*, *>
        ?: error("Invalid output from swift package show-dependencies")

    val packages = mutableSetOf<SwiftPackageDep>()
    flattenDependencies(json, packages)
    return packages
}

/**
 * Recursively extracts all dependencies from the JSON tree produced by
 * `swift package show-dependencies --format json`.
 */
fun flattenDependencies(
    node: Map<*, *>,
    result: MutableSet<SwiftPackageDep>,
) {
    val deps = node["dependencies"] as? List<*> ?: return
    for (dep in deps) {
        val depMap = dep as? Map<*, *> ?: continue
        val identity = depMap["identity"] as? String ?: continue
        val name = depMap["name"] as? String ?: identity
        val url = depMap["url"] as? String ?: ""
        val path = depMap["path"] as? String ?: continue

        result.add(SwiftPackageDep(identity, name, url, path))
        flattenDependencies(depMap, result)
    }
}

/**
 * Finds a LICENSE file in the given directory.
 * Looks for common license file names: LICENSE, LICENSE.md, LICENSE.txt, LICENCE, etc.
 */
fun findLicenseFile(dir: File): File? {
    if (!dir.isDirectory) return null
    val candidates = listOf("LICENSE", "LICENSE.md", "LICENSE.txt", "LICENCE", "LICENCE.md", "LICENCE.txt")
    for (candidate in candidates) {
        val file = dir.resolve(candidate)
        if (file.isFile) return file
    }
    return null
}
