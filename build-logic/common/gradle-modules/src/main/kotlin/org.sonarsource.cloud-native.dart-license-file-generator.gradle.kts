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
import java.net.URI
import java.nio.file.Files
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.sonarsource.cloudnative.gradle.DartLicenseGenerationConfig
import org.sonarsource.cloudnative.gradle.areDirectoriesEqual
import org.sonarsource.cloudnative.gradle.copyDirectory

/**
 * This plugin collects license files from third-party Dart runtime dependencies and places them
 * into a resources folder. It provides:
 * - A task to collect licenses from pub.dev packages referenced in pubspec.lock / package_config.json
 * - A validation task to ensure committed license files are up-to-date
 * - A task to regenerate the license files into the resources folder
 */

val dartLicenseConfig =
    extensions.findByType<DartLicenseGenerationConfig>()
        ?: extensions.create<DartLicenseGenerationConfig>("dartLicenseGenerationConfig")

dartLicenseConfig.buildDartLicenseFilesDir.convention(
    project.layout.buildDirectory.dir("dart-licenses").get().asFile
)
dartLicenseConfig.projectLicenseFile.convention(project.rootDir.resolve("LICENSE"))
dartLicenseConfig.packageConfigFile.convention(
    project.rootDir.resolve("analyzer/.dart_tool/package_config.json")
)
dartLicenseConfig.analyzerDir.convention(
    project.rootDir.resolve("analyzer")
)

val resourceLicenseDir = project.layout.projectDirectory.dir("src/main/resources/dart-licenses")

val collectDartLicenses = tasks.register("collectDartLicenses") {
    description = "Collects license files from Dart pub.dev dependencies"
    group = "licenses"
    var dartPubGetTasks = getTasksByName("dartPubGet", true)
    dependsOn(dartPubGetTasks)

    doLast {
        val nonDevPackages = parseNonDevPackages(dartLicenseConfig.analyzerDir.get())
        val packageRoots = parsePackageRoots(dartLicenseConfig.packageConfigFile.get())

        // Only include hosted packages (those resolved from pub.dev with file:// URIs in package_config.json)
        val runtimePackages = nonDevPackages.filter { packageRoots.containsKey(it) }

        val outputDir = dartLicenseConfig.buildDartLicenseFilesDir.get().resolve("THIRD_PARTY_LICENSES")
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        logger.lifecycle("Collecting licenses for ${runtimePackages.size} runtime Dart packages...")

        var collected = 0
        for (pkgName in runtimePackages.sorted()) {
            val pkgRoot = packageRoots[pkgName]!!

            val licenseFile = findLicenseFile(pkgRoot)
            if (licenseFile != null) {
                licenseFile.copyTo(outputDir.resolve("$pkgName-LICENSE.txt"), overwrite = true)
                collected++
            } else {
                logger.warn("No LICENSE file found for package: $pkgName (looked in $pkgRoot)")
            }
        }

        // Copy project license
        val projectLicense = dartLicenseConfig.projectLicenseFile.get()
        projectLicense.copyTo(
            dartLicenseConfig.buildDartLicenseFilesDir.get().resolve("LICENSE"),
            overwrite = true
        )

        logger.lifecycle("Collected $collected license files from ${runtimePackages.size} packages.")
    }
}

val validateDartLicenses = tasks.register("validateDartLicenseFiles") {
    description = "Validate that generated Dart license files match the committed ones"
    group = "validation"
    dependsOn(collectDartLicenses)

    doLast {
        val generated = dartLicenseConfig.buildDartLicenseFilesDir.get()
        val committed = resourceLicenseDir.asFile
        if (!areDirectoriesEqual(generated, committed, logger)) {
            throw GradleException(
                """
                [FAILURE] Dart license file validation failed!
                Generated license files differ from committed files at $resourceLicenseDir.
                To update the committed license files, run './gradlew generateDartLicenseResources' and commit the changes.
                """.trimIndent()
            )
        }
        logger.lifecycle("Dart license file validation succeeded: generated files match committed ones.")
    }
}

val generateDartLicenseResources = tasks.register("generateDartLicenseResources") {
    description = "Copies generated Dart license files to the resources directory"
    group = "licenses"
    dependsOn(collectDartLicenses)

    doLast {
        val generated = dartLicenseConfig.buildDartLicenseFilesDir.get()
        val destination = resourceLicenseDir.asFile
        Files.createDirectories(destination.toPath())
        copyDirectory(generated, destination, logger)
    }
}

tasks.named("validateLicenseFiles") {
    dependsOn(validateDartLicenses)
}

tasks.named("generateLicenseResources") {
    dependsOn(generateDartLicenseResources)
}

/**
 * Runs `dart pub deps --no-dev --style=compact` in the analyzer directory and parses the output
 * to extract non-dev dependency package names.
 *
 * Output format has lines like: `- package_name 1.0.0 [dep1 dep2]`
 */
fun parseNonDevPackages(analyzerDir: File): Set<String> {
    val process = ProcessBuilder("dart", "pub", "deps", "--no-dev", "--style=compact")
        .directory(analyzerDir)
        .redirectErrorStream(false)
        .start()
    val output = process.inputStream.bufferedReader().readText()
    val exitCode = process.waitFor()
    if (exitCode != 0) {
        val stderr = process.errorStream.bufferedReader().readText()
        error("dart pub deps failed with exit code $exitCode: $stderr")
    }

    val packageLineRegex = Regex("^- (\\S+) .+$")
    return output.lineSequence()
        .mapNotNull { packageLineRegex.find(it)?.groupValues?.get(1) }
        .toSet()
}

/**
 * Parses package_config.json to build a map of package name to its root directory.
 * Supports both absolute file:// URIs (hosted packages from pub.dev cache)
 * and relative paths (path-based/local packages).
 */
fun parsePackageRoots(packageConfigFile: File): Map<String, File> {
    val json = JsonSlurper().parse(packageConfigFile) as? Map<*, *>
        ?: error("Invalid package_config.json: expected a JSON object")
    val packages = json["packages"] as? List<*>
        ?: error("Invalid package_config.json: missing 'packages' array")

    val packageRoots = mutableMapOf<String, File>()
    for (entry in packages) {
        val pkg = entry as? Map<*, *> ?: continue
        val name = pkg["name"] as? String ?: continue
        val rootUri = pkg["rootUri"] as? String ?: continue

        val rootDir = if (rootUri.startsWith("file://")) {
            File(URI(rootUri))
        } else {
            packageConfigFile.parentFile.resolve(rootUri).canonicalFile
        }
        packageRoots[name] = rootDir
    }
    return packageRoots
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
