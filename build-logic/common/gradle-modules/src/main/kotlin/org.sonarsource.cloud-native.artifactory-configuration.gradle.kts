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
import org.sonarsource.cloudnative.gradle.ArtifactoryConfiguration

plugins {
    // `maven-publish` is required for the `artifactory` plugin (or the root project shoudn't be published)
    `maven-publish`
    id("com.jfrog.artifactory")
}

// this value is present on CI
val buildNumber: String? = System.getProperty("buildNumber")

val artifactoryConfiguration = extensions.create<ArtifactoryConfiguration>("artifactoryConfiguration")
artifactoryConfiguration.acknowledgePublicationOfNonPlugin.convention(false)

val projectVersion = project.version as String

// `afterEvaluate` is required to inject configurable properties; see https://github.com/jfrog/artifactory-gradle-plugin/issues/71#issuecomment-1734977528
project.afterEvaluate {
    if (!artifactoryConfiguration.acknowledgePublicationOfNonPlugin.get() && project.path != ":" && "-plugin" !in project.path) {
        throw GradleException(
            "This build script configures Artifactory connection for a build of a SonarQube plugin. " +
                "This script must be applied either to the root project or in a \"-plugin\" project."
        )
    }

    artifactory {
        if (artifactoryConfiguration.artifactsToPublish.isPresent) {
            clientConfig.info.addEnvironmentProperty(
                "ARTIFACTS_TO_PUBLISH",
                artifactoryConfiguration.artifactsToPublish.get()
            )
            clientConfig.info.addEnvironmentProperty(
                "ARTIFACTS_TO_DOWNLOAD",
                artifactoryConfiguration.artifactsToDownload.getOrElse("")
            )
        }

        setContextUrl(System.getenv("ARTIFACTORY_URL"))
        // Note: `publish` should only be called once: https://github.com/jfrog/artifactory-gradle-plugin/issues/111
        publish {
            if (artifactoryConfiguration.repoKeyEnv.isPresent) {
                repository {
                    repoKey = System.getenv(artifactoryConfiguration.repoKeyEnv.get())
                    username = System.getenv(artifactoryConfiguration.usernameEnv.get())
                    password = System.getenv(artifactoryConfiguration.passwordEnv.get())
                }
            }
            defaults {
                publications("mavenJava")
                setProperties(
                    mapOf(
                        "build.name" to artifactoryConfiguration.buildName.get(),
                        "version" to projectVersion,
                        "build.number" to buildNumber,
                        "pr.branch.target" to System.getenv("PULL_REQUEST_BRANCH_TARGET"),
                        "pr.number" to System.getenv("PULL_REQUEST_NUMBER"),
                        "vcs.branch" to System.getenv("GIT_BRANCH"),
                        "vcs.revision" to System.getenv("GIT_COMMIT")
                    )
                )
                setPublishArtifacts(true)
                setPublishPom(true)
                setPublishIvy(false)
            }
        }

        clientConfig.info.addEnvironmentProperty("PROJECT_VERSION", projectVersion)
        clientConfig.info.buildName = artifactoryConfiguration.buildName.get()
        clientConfig.info.buildNumber = buildNumber
        clientConfig.isIncludeEnvVars = true
        clientConfig.envVarsExcludePatterns =
            "*password*,*PASSWORD*,*secret*,*MAVEN_CMD_LINE_ARGS*,sun.java.command," +
            "*token*,*TOKEN*,*LOGIN*,*login*,*key*,*KEY*,*PASSPHRASE*,*signing*"
    }
}
