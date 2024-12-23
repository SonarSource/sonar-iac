/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.registerAllRuleApiTasks

plugins {
    alias(libs.plugins.spotless)
    id("org.sonarsource.iac.artifactory-configuration")
    id("org.sonarsource.iac.rule-api")
    id("org.sonarsource.iac.sonarqube")
    id("com.diffplug.blowdryer")
}

val kotlinGradleDelimiter = "(package|import|plugins|pluginManagement|dependencyResolutionManagement|repositories) "
spotless {
    // Mainly used to define spotless configuration for the build-logic
    encoding(Charsets.UTF_8)
    kotlinGradle {
        ktlint().setEditorConfigPath("$rootDir/.editorconfig")
        target("*.gradle.kts", "build-logic/*.gradle.kts", "/build-logic/src/**/*.gradle.kts", "/sonar-helm-for-iac/*.gradle.kts")
        licenseHeaderFile(
            rootProject.file("LICENSE_HEADER"),
            kotlinGradleDelimiter
        ).updateYearWithLatest(true)
    }
    kotlin {
        ktlint().setEditorConfigPath("$rootDir/.editorconfig")
        target("/build-logic/src/**/*.kt")
        licenseHeaderFile(rootProject.file("LICENSE_HEADER")).updateYearWithLatest(true)
    }
}

project(":iac-extensions:kubernetes") {
    sonar {
        properties {
            properties["sonar.sources"] as MutableCollection<String> +=
                this@project.layout.projectDirectory.dir("src/common/java").asFile.toString()
        }
    }
}

project(":sonar-helm-for-iac") {
    sonar {
        properties {
            property("sonar.sources", ".")
            property("sonar.inclusions", "**/*.go")
            property("sonar.exclusions", "**/build/**,**/org.sonar.iac.helm/**")
            property("sonar.tests", ".")
            property("sonar.test.inclusions", "**/*_test.go")
            property("sonar.go.tests.reportPaths", "build/test-report.json")
            property("sonar.go.coverage.reportPaths", "build/test-coverage.out")
            property("sonar.go.golangci-lint.reportPaths", "build/reports/golangci-lint-report.xml")
        }
    }
}

tasks.artifactoryPublish { skip = true }

// This configuration needs to be here and override in another modules, otherwise it doesn't work
artifactoryConfiguration {
    artifactsToPublish = "org.sonarsource.iac:sonar-iac-plugin:jar"
    artifactsToDownload = ""
    repoKeyEnv = "ARTIFACTORY_DEPLOY_REPO"
    usernameEnv = "ARTIFACTORY_DEPLOY_USERNAME"
    passwordEnv = "ARTIFACTORY_DEPLOY_PASSWORD"
}

registerAllRuleApiTasks()
