import java.net.URI

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
pluginManagement {
    // Note: because of the way how settings are initialized, we cannot reuse functions defined later in this file.
    val artifactoryUsername = providers.environmentVariable("ARTIFACTORY_PRIVATE_USERNAME")
        .orElse(providers.environmentVariable("ARTIFACTORY_USERNAME"))
        .orElse(providers.gradleProperty("artifactoryUsername"))
    val artifactoryPassword = providers.environmentVariable("ARTIFACTORY_PRIVATE_PASSWORD")
        .orElse(providers.environmentVariable("ARTIFACTORY_ACCESS_TOKEN"))
        .orElse(providers.gradleProperty("artifactoryPassword"))

    repositories {
        if (artifactoryUsername.isPresent && artifactoryPassword.isPresent) {
            maven {
                name = "artifactory"
                url = uri("https://repox.jfrog.io/repox/plugins.gradle.org")

                authentication {
                    credentials {
                        username = artifactoryUsername.get()
                        password = artifactoryPassword.get()
                    }
                }
            }
        } else {
            mavenCentral()
            gradlePluginPortal()
        }
    }
}

plugins {
    id("com.gradle.develocity") version "4.0.3"
}

rootProject.name = "cloud-native-gradle-modules"
include("gradle-modules")

dependencyResolutionManagement {
    repositories {
        ifAuthenticatedOrElse(providers, { artifactoryUsername, artifactoryPassword ->
            repox("sonarsource", artifactoryUsername, artifactoryPassword, ::uri)
            repox("plugins.gradle.org", artifactoryUsername, artifactoryPassword, ::uri)
        }) {
            mavenCentral()
            gradlePluginPortal()
        }
    }
}

develocity {
    server.set("https://develocity-public.sonar.build/")
}

val isCI = System.getenv("CI") != null
buildCache {
    local {
        isEnabled = !isCI
    }
    remote(develocity.buildCache) {
        isEnabled = true
        isPush = isCI
    }
}

internal fun RepositoryHandler.repox(
    repository: String,
    artifactoryUsername: String,
    artifactoryPassword: String,
    uri: (Any) -> URI,
): MavenArtifactRepository =
    maven {
        name = "artifactory"
        url = uri("https://repox.jfrog.io/repox/$repository")

        authentication {
            credentials {
                username = artifactoryUsername
                password = artifactoryPassword
            }
        }
    }

internal fun ifAuthenticatedOrElse(
    providers: ProviderFactory,
    onAuthenticated: (artifactoryUsername: String, artifactoryPassword: String) -> Unit,
    onNotAuthenticated: () -> Unit,
) {
    // This authentication relies on env vars configured on Cirrus CI or on Gradle properties (`-P<prop>` flags or `gradle.properties` file)
    val artifactoryUsername = providers.environmentVariable("ARTIFACTORY_PRIVATE_USERNAME")
        .orElse(providers.environmentVariable("ARTIFACTORY_USERNAME"))
        .orElse(providers.gradleProperty("artifactoryUsername"))
    val artifactoryPassword = providers.environmentVariable("ARTIFACTORY_PRIVATE_PASSWORD")
        .orElse(providers.environmentVariable("ARTIFACTORY_ACCESS_TOKEN"))
        .orElse(providers.gradleProperty("artifactoryPassword"))

    if (artifactoryUsername.isPresent && artifactoryPassword.isPresent) {
        onAuthenticated(artifactoryUsername.get(), artifactoryPassword.get())
    } else {
        onNotAuthenticated()
    }
}
