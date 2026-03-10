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
import com.github.jk1.license.render.ReportRenderer
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.sonarsource.cloudnative.gradle.AnalyzerLicensingPackagingRenderer
import org.sonarsource.cloudnative.gradle.LicenseGenerationConfig
import org.sonarsource.cloudnative.gradle.areDirectoriesEqual
import org.sonarsource.cloudnative.gradle.copyDirectory

plugins {
    id("com.github.jk1.dependency-license-report")
}

/**
 * This plugin is used for generating license files for third-party runtime-dependencies into the resources folder.
 * It provides a validation task to ensure that the license files in the resource folder are up-to-date.
 * It provides a task to regenerate the license files into the resources folder.
 */

val licenseGenerationConfig =
    extensions.findByType<LicenseGenerationConfig>()
        ?: extensions.create<LicenseGenerationConfig>("licenseGenerationConfig")

licenseGenerationConfig.projectLicenseFile.convention(
    project.layout.projectDirectory.asFile.parentFile.resolve("LICENSE.txt")
)

var buildLicenseReportDirectory = project.layout.buildDirectory.dir("reports/dependency-license")
var buildLicenseOutputToCopyDir = buildLicenseReportDirectory.get().dir("licenses")
var resourceLicenseDir = project.layout.projectDirectory.dir("src/main/resources/licenses")
var resourceThirdPartyDir = resourceLicenseDir.dir("THIRD_PARTY_LICENSES")

licenseReport {
    renderers = arrayOf<ReportRenderer>(AnalyzerLicensingPackagingRenderer(buildLicenseReportDirectory.get().asFile.toPath()))
    excludeGroups = arrayOf(project.group.toString(), project.group.toString().replace("com.sonarsource", "org.sonarsource"))
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
        if (!areDirectoriesEqual(buildLicenseOutputToCopyDir.asFile, resourceThirdPartyDir.asFile, logger)) {
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
    group = "licenses"
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

tasks.register("generateLicenseResources") {
    description = "Copies generated license files to the resources directory"
    group = "licenses"
    dependsOn("generateLicenseReport")

    doLast {
        val sonarLicenseFile = licenseGenerationConfig.projectLicenseFile.get()
        Files.createDirectories(resourceLicenseDir.asFile.toPath())
        Files.copy(
            sonarLicenseFile.toPath(),
            resourceLicenseDir.file("LICENSE.txt").asFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
        copyDirectory(buildLicenseReportDirectory.get().dir("licenses").asFile, resourceThirdPartyDir.asFile, logger)
    }
}
