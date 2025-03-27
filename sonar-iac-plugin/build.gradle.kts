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
import org.sonar.iac.enforceJarSize
import org.sonar.iac.registerCleanupTask

plugins {
    id("org.sonarsource.iac.java-conventions")
    id("org.sonarsource.iac.artifactory-configuration")
    id("org.sonarsource.cloud-native.code-style-conventions")
    id("com.gradleup.shadow")
}

description = "SonarSource IaC Analyzer :: Sonar Plugin"

dependencies {
    implementation(project(":iac-extensions:terraform"))
    implementation(project(":iac-extensions:cloudformation"))
    implementation(project(":iac-extensions:kubernetes"))
    implementation(project(":iac-extensions:docker"))
    implementation(project(":iac-extensions:arm"))
    implementation(project(":iac-extensions:jvm-framework-config"))
    implementation(project(":sonar-helm-for-iac", "goBinaries"))
    implementation(libs.sonar.analyzer.commons)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.archunit)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.analyzer.test.commons)
    testRuntimeOnly(libs.junit.platform.launcher)

    compileOnly(libs.sonar.plugin.api)
}

// used to be done by sonar-packaging maven plugin
tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Plugin-ChildFirstClassLoader" to "false",
                "Plugin-Class" to "org.sonar.plugins.iac.IacPlugin",
                "Plugin-Description" to "Code Analyzer for IaC",
                "Plugin-Developers" to "SonarSource Team",
                "Plugin-Display-Version" to version,
                "Plugin-Homepage" to "http://docs.sonarqube.org/display/PLUG/Plugin+Library/iac/sonar-iac-plugin",
                "Plugin-IssueTrackerUrl" to "https://jira.sonarsource.com/projects/SONARIAC",
                "Plugin-Key" to "iac",
                "Plugin-License" to "SSALv1",
                "Plugin-Name" to "IaC Code Quality and Security",
                "Plugin-Organization" to "SonarSource",
                "Plugin-OrganizationUrl" to "https://www.sonarsource.com",
                "Plugin-SourcesUrl" to "https://github.com/SonarSource/sonar-iac/sonar-iac-plugin",
                "Plugin-Version" to project.version,
                "Sonar-Version" to "8.9",
                "SonarLint-Supported" to "true",
                "Version" to project.version.toString(),
                "Jre-Min-Version" to java.sourceCompatibility.majorVersion
            )
        )
    }
}

val cleanupTask = registerCleanupTask()

tasks.shadowJar {
    dependsOn(cleanupTask)

    minimize()
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")
    exclude("META-INF/*.RSA")
    exclude("META-INF/*.SF")
    exclude("LICENSE*")
    exclude("NOTICE*")

    val isCrossCompile: Boolean = providers.environmentVariable("GO_CROSS_COMPILE").map { it == "1" }.getOrElse(true)
    val (minSize, maxSize) = if (isCrossCompile) {
        17_500_000L to 18_500_000L
    } else {
        8_000_000L to 9_000_000L
    }
    doLast {
        enforceJarSize(archiveFile.get().asFile, minSize, maxSize)
    }
}

artifacts {
    archives(tasks.shadowJar)
}

publishing {
    publications.withType<MavenPublication> {
        artifact(tasks.shadowJar) {
            // remove `-all` suffix from the fat jar
            classifier = null
        }
        artifact(tasks.sourcesJar)
        artifact(tasks.javadocJar)
    }
}

artifactoryConfiguration {
    license {
        name = "SSALv1"
        url = "https://sonarsource.com/license/ssal/"
        distribution = "repo"
    }
    artifactsToPublish = "org.sonarsource.iac:sonar-iac-plugin:jar"
    artifactsToDownload = ""
    repoKeyEnv = "ARTIFACTORY_DEPLOY_REPO"
    usernameEnv = "ARTIFACTORY_DEPLOY_USERNAME"
    passwordEnv = "ARTIFACTORY_DEPLOY_PASSWORD"
}
