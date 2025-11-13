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
import com.github.jk1.license.render.ReportRenderer
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import org.sonarsource.cloudnative.gradle.AnalyzerLicensingPackagingRenderer

plugins {
    id("com.github.jk1.dependency-license-report")
}

/**
 * This plugin is used for generating license files for third-party dependencies into the resources folder.
 * It provides a validation task to ensure that the license files in the resource folder are up-to-date.
 * It provides a task to regenerate the license files into the resources folder.
 * This tasks expects the license of the analyzer to be present one level above the (project-)plugin directory.
 */
var buildLicenseReportDirectory = project.layout.buildDirectory.dir("reports/dependency-license")
var buildLicenseOutputToCopyDir = buildLicenseReportDirectory.get().dir("licenses")
var resourceLicenseDir = project.layout.projectDirectory.dir("src/main/resources/licenses")
var resourceThirdPartyDir = resourceLicenseDir.dir("THIRD_PARTY_LICENSES")

licenseReport {
    renderers = arrayOf<ReportRenderer>(AnalyzerLicensingPackagingRenderer(buildLicenseReportDirectory.get().asFile.toPath()))
}

tasks.named("check") {
    dependsOn("validateLicenseFiles")
}

tasks.register("validateLicenseFiles") {
    description = "Validate that generated license files match the committed ones"
    group = "validation"
    // generateLicenseReport is the task exposed by `com.github.jk1.dependency-license-report`
    dependsOn("generateLicenseReport")

    doLast {
        if (!areDirectoriesEqual(buildLicenseOutputToCopyDir.asFile, resourceThirdPartyDir.asFile)) {
            val message = """
                [FAILURE] License file validation failed!
                Generated license files differ from committed files at $resourceThirdPartyDir.
                To update the committed license files, run './gradlew generateLicenseResources' and commit the changes.
                
                Note: This will completely regenerate all license files under $resourceThirdPartyDir and remove any stale ones.
                """
            throw GradleException(message)
        } else {
            logger.lifecycle("License file validation succeeded: Generated license files match the committed ones.")
        }
    }
}

/**
 * An empty build service to serve as a synchronization point.
 * Because `com.github.jk1.dependency-license-report` is not able to run in parallel with Gradle 9.0,
 * we force tasks to never run in parallel by configuring this service.
 */
abstract class NoParallelService : BuildService<BuildServiceParameters.None>

// generateLicenseReport is the task exposed by `com.github.jk1.dependency-license-report`
tasks.named("generateLicenseReport") {
    usesService(
        gradle.sharedServices.registerIfAbsent("noParallelProvider", NoParallelService::class) {
            // generateLicenseReport requires single threaded run with Gradle 9.0
            maxParallelUsages = 1
        }
    )

    // I'm currently unsure how I could properly cache this, or if this isn't already handled?
    outputs.upToDateWhen {
        // To be on a safe side, always rerun the generator
        false
    }

    doFirst {
        // Clean up previous output to avoid stale files
        buildLicenseReportDirectory.get().asFile.deleteRecursively()
        Files.createDirectories(buildLicenseOutputToCopyDir.asFile.toPath())
    }
}

// Requires LICENSE.txt to be present one level above the (project-)plugin directory
tasks.register("generateLicenseResources") {
    description = "Copies generated license files to the resources directory"
    dependsOn("generateLicenseReport")

    doLast {
        val sonarLicenseFile = project.layout.projectDirectory.asFile.parentFile.resolve("LICENSE.txt")
        Files.createDirectories(resourceLicenseDir.asFile.toPath())
        Files.copy(
            sonarLicenseFile.toPath(),
            resourceLicenseDir.file("LICENSE.txt").asFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
        copyDirectory(buildLicenseReportDirectory.get().dir("licenses").asFile, resourceThirdPartyDir.asFile)
    }
}

/**
 * Compares two directories recursively to check for equality.
 * * Two directories are considered equal if they have the exact same
 * directory structure and all corresponding files have identical content.
 *
 * @param dir1 The first directory.
 * @param dir2 The second directory.
 * @return `true` if the directories are equal, `false` otherwise.
 */
fun areDirectoriesEqual(
    dir1: File,
    dir2: File,
): Boolean {
    if (!dir1.isDirectory || !dir2.isDirectory) {
        logger.warn("One or both paths are not directories.")
        return false
    }
    logger.lifecycle("Comparing directories: ${dir1.name} and ${dir2.name}")

    try {
        // 1. Walk both directory trees and map files by their relative path
        val files1 = dir1.walk()
            .filter { it.isFile }
            .associateBy { it.relativeTo(dir1) }

        val files2 = dir2.walk()
            .filter { it.isFile }
            .associateBy { it.relativeTo(dir2) }

        // 2. Compare the directory structure (based on file paths)
        if (files1.keys != files2.keys) {
            logger.warn("Directory structures do not match.")
            logger.warn("Files only in ${dir1.name}: ${files1.keys - files2.keys}")
            logger.warn("Files only in ${dir2.name}: ${files2.keys - files1.keys}")
            return false
        }

        // 3. Compare the content of each matching file
        for (relativePath in files1.keys) {
            val file1 = files1[relativePath]!!
            val file2 = files2[relativePath]!!

            // Quick check: compare file sizes first
            if (file1.length() != file2.length()) {
                logger.warn("File size mismatch: $relativePath")
                return false
            }

            // Full check: compare byte content
            val checksum1 = getFileChecksum(file1)
            val checksum2 = getFileChecksum(file2)
            if (checksum1 != checksum2) {
                logger.warn("File content mismatch: $relativePath")
                return false
            }
        }

        // If all checks pass, the directories are equal
        return true
    } catch (e: IOException) {
        logger.error("An error occurred during comparison: ${e.message}")
        return false
    }
}

fun getFileChecksum(file: File): String {
    val md = MessageDigest.getInstance("SHA-256")
    val digestBytes = md.digest(file.readBytes())

    // 4. Convert the byte array to a hexadecimal string
    // "%02x" formats each byte as two lowercase hex digits
    return digestBytes.joinToString("") { "%02x".format(it) }
}

fun copyDirectory(
    sourceDir: File,
    destinationDir: File,
) {
    val errors = mutableListOf<String>()

    destinationDir.deleteRecursively()
    sourceDir.copyRecursively(
        target = destinationDir,
        overwrite = true,
        onError = { file, exception ->
            logger.warn("Failed to copy $file: ${exception.message}")
            errors.add(file.name)
            OnErrorAction.SKIP // Skip this file and continue
        }
    )

    if (errors.isEmpty()) {
        logger.lifecycle("Directory ${sourceDir.name} copied successfully to ${destinationDir.name}")
    } else {
        throw GradleException("Failed to copy ${errors.size} files.")
    }
}
