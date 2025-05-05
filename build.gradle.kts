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
import org.sonar.iac.collectIacExtensionNames
import org.sonar.iac.toCamelCase

plugins {
    id("org.sonarsource.cloud-native.code-style-conventions")
    id("org.sonarsource.cloud-native.artifactory-configuration")
    id("org.sonarsource.cloud-native.rule-api")
    // Note: if there are no plugins applied from `build-logic/iac`, utility methods won't be available as well.
    id("org.sonarsource.iac.sonarqube")
}

project(":iac-extensions:kubernetes") {
    sonar {
        properties {
            properties["sonar.sources"] as MutableCollection<String> +=
                this@project.layout.projectDirectory.dir("src/common/java").asFile.toString()
            property("sonar.sca.exclusions", "private/its/sources/**")
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

artifactoryConfiguration {
    buildName = providers.environmentVariable("CIRRUS_REPO_NAME").orElse("sonar-iac")
    artifactsToPublish = "org.sonarsource.iac:sonar-iac-plugin:jar"
    artifactsToDownload = ""
    repoKeyEnv = "ARTIFACTORY_DEPLOY_REPO"
    usernameEnv = "ARTIFACTORY_DEPLOY_USERNAME"
    passwordEnv = "ARTIFACTORY_DEPLOY_PASSWORD"
}

ruleApi {
    languageToSonarpediaDirectory = collectIacExtensionNames(
        // For jvm-framework-config, we don't (yet) have separate rules.
        exclusions = listOf("jvm-framework-config")
    ).associate { name ->
        name.toCamelCase() to "iac-extensions/$name/"
    }
}

spotless {
    java {
        // no Java sources in the root project
        target("")
    }
    kotlin {
        target("build-logic/iac/src/**/*.kt")
        ktlint().setEditorConfigPath("$rootDir/build-logic/common/.editorconfig")
        licenseHeaderFile(rootProject.file("LICENSE_HEADER")).updateYearWithLatest(true)
    }
    kotlinGradle {
        target("build-logic/iac/src/**/*.gradle.kts", "build-logic/iac/*.gradle.kts", "*.gradle.kts")
    }
}
