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
package org.sonarsource.cloudnative.gradle

import com.github.jk1.license.LicenseFileDetails
import com.github.jk1.license.ModuleData
import com.github.jk1.license.ProjectData
import com.github.jk1.license.render.ReportRenderer
import java.io.IOException
import java.net.URISyntaxException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.util.ArrayList

class AnalyzerLicensingPackagingRenderer(
    private val buildOutputDir: Path,
) : ReportRenderer {
    private var apacheLicenseFileName: String = "Apache-2.0.txt"
    private lateinit var generatedLicenseResourcesDirectory: Path
    private val licenseTitleToResourceFile: Map<String, String> = buildMap {
        put("Apache License, Version 2.0", apacheLicenseFileName)
        put("Apache License Version 2.0", apacheLicenseFileName)
        put("The Apache License, Version 2.0", apacheLicenseFileName)
        put("Apache 2", apacheLicenseFileName)
        put("Apache-2.0", apacheLicenseFileName)
        put("The Apache Software License, Version 2.0", apacheLicenseFileName)
        put("BSD-3-Clause", "BSD-3.txt")
        put("BSD", "BSD-2.txt")
        put("GNU LGPL 3", "GNU-LGPL-3.txt")
        put("Go License", "Go.txt")
        put("MIT License", "MIT.txt")
        put("MIT", "MIT.txt")
    }
    private val dependenciesWithUnusableLicenseFileInside: Set<String> = setOf(
        "com.fasterxml.jackson.dataformat.jackson-dataformat-smile",
        "com.fasterxml.jackson.dataformat.jackson-dataformat-yaml",
        "com.fasterxml.woodstox.woodstox-core",
        "org.codehaus.woodstox.stax2-api"
    )
    private val exceptions: ArrayList<String> = ArrayList()

    // Generate license files for all dependencies in the licenses folder
    override fun render(data: ProjectData) {
        generatedLicenseResourcesDirectory = buildOutputDir.resolve("licenses")
        try {
            generateDependencyFiles(data)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        if (exceptions.isNotEmpty()) {
            val exceptionLog = exceptions.joinToString(separator = "\n")
            throw RuntimeException("Exceptions occurred during license file generation:\n$exceptionLog")
        }
    }

    @Throws(IOException::class, URISyntaxException::class)
    private fun generateDependencyFiles(data: ProjectData) {
        for (dependency in data.allDependencies) {
            generateDependencyFile(dependency)
        }
    }

    /**
     * Generate a license file for a given dependency.
     * First we try to copy the license file included in the dependency itself in `copyIncludedLicenseFromDependency`
     * If there is no License file, or the dependency contains an unusable license file,
     * we try to derive the license from the pom in `findLicenseIdentifierInPomAndCopyFromResources`.
     * In this method we're looking for the identifier of the license, and we copy the corresponding license file from our resources.
     * The mapping (license identifier to resource file) is derived from the map `licenseTitleToResourceFile`.
     */
    @Throws(IOException::class, URISyntaxException::class)
    private fun generateDependencyFile(data: ModuleData) {
        val copyIncludedLicenseFile = copyIncludedLicenseFromDependency(data)
        if (copyIncludedLicenseFile.success) {
            return
        }

        val copyFromResources = findLicenseIdentifierInPomAndCopyFromResources(data)
        if (copyFromResources.success) {
            return
        }

        exceptions.add("${data.group}.${data.name}: ${copyIncludedLicenseFile.message}")
        exceptions.add("${data.group}.${data.name}: ${copyFromResources.message}")
    }

    @Throws(IOException::class)
    private fun copyIncludedLicenseFromDependency(data: ModuleData): Status {
        if (dependenciesWithUnusableLicenseFileInside.contains("${data.group}.${data.name}")) {
            return Status.failure("Excluded copying license from dependency as it's not the right one.")
        }

        val licenseFileDetails = data.licenseFiles.stream().flatMap { licenseFile -> licenseFile.fileDetails.stream() }
            .filter { file: LicenseFileDetails -> file.file.contains("LICENSE") }
            .findFirst()

        if (licenseFileDetails.isEmpty) {
            return Status.failure("No license file data found.")
        }

        copyLicenseFile(data, buildOutputDir.resolve(licenseFileDetails.get().file))
        return Status.success
    }

    @Throws(IOException::class, URISyntaxException::class)
    private fun findLicenseIdentifierInPomAndCopyFromResources(data: ModuleData): Status {
        val pomLicense = data.poms.stream().flatMap { pomData -> pomData.licenses.stream() }
            .findFirst()

        if (pomLicense.isEmpty) {
            return Status.failure("No license found in pom data.")
        }

        return copyLicenseFromResources(data, pomLicense.get().name)
    }

    @Throws(IOException::class)
    private fun copyLicenseFile(
        data: ModuleData,
        fileToCopy: Path,
    ): Status {
        // Modify to use LF line endings
        val normalizedFile = Files.readAllLines(fileToCopy).joinToString("\n")
        Files.write(
            generateLicensePath(data),
            normalizedFile.toByteArray(),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
        return Status.success
    }

    @Throws(IOException::class)
    private fun copyLicenseFromResources(
        data: ModuleData,
        licenseName: String,
    ): Status {
        val licenseResourceFileName = licenseTitleToResourceFile[licenseName]
        if (licenseResourceFileName == null) {
            return Status.failure("License file '$licenseName' could not be found.")
        }
        val resourceAsStream = AnalyzerLicensingPackagingRenderer::class.java.getResourceAsStream("/licenses/$licenseResourceFileName")
            ?: throw IOException("Resource not found for license: $licenseName")
        Files.copy(resourceAsStream, generateLicensePath(data), StandardCopyOption.REPLACE_EXISTING)
        return Status.success
    }

    private fun generateLicensePath(data: ModuleData): Path =
        generatedLicenseResourcesDirectory.resolve("${data.group}.${data.name}-LICENSE.txt")

    private data class Status(
        val success: Boolean,
        val message: String?,
    ) {
        companion object {
            var success: Status = Status(true, null)

            fun failure(message: String?): Status = Status(false, message)
        }
    }
}
