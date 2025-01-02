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
import org.sonar.iac.ArtifactoryConfiguration
import org.sonar.iac.signingCondition

plugins {
    id("com.jfrog.artifactory")
    signing
    `maven-publish`
}

val artifactoryConfiguration = extensions.create<ArtifactoryConfiguration>("artifactoryConfiguration")

publishing {
    publications.create<MavenPublication>("mavenJava") {
        pom {
            name.set("SonarSource IaC Analyzer")
            description.set(project.description)
            url.set("http://www.sonarqube.org/")
            organization {
                name.set("SonarSource")
                url.set("http://www.sonarsource.com/")
            }
            licenses {
                license {
                    name = artifactoryConfiguration.license.name
                    url = artifactoryConfiguration.license.url
                    distribution = artifactoryConfiguration.license.distribution
                    comments = artifactoryConfiguration.license.comments
                }
            }
            scm {
                url.set("https://github.com/SonarSource/sonar-iac")
            }
            developers {
                developer {
                    id.set("sonarsource-team")
                    name.set("SonarSource Team")
                }
            }
        }
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    setRequired {
        project.signingCondition()
    }
    sign(publishing.publications)
}

tasks.withType<Sign> {
    onlyIf {
        val artifactorySkip: Boolean = tasks.artifactoryPublish.get().skip
        !artifactorySkip && project.signingCondition()
    }
}
// `afterEvaluate` is required to inject configurable properties; see https://github.com/jfrog/artifactory-gradle-plugin/issues/71#issuecomment-1734977528
project.afterEvaluate {
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
                        "build.name" to "sonar-iac-enterprise",
                        "version" to project.version.toString(),
                        "build.number" to project.ext["buildNumber"].toString(),
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

        clientConfig.info.addEnvironmentProperty("PROJECT_VERSION", project.version.toString())
        clientConfig.info.buildName = "sonar-iac-enterprise"
        clientConfig.info.buildNumber = project.ext["buildNumber"].toString()
        clientConfig.isIncludeEnvVars = true
        clientConfig.envVarsExcludePatterns =
            "*password*,*PASSWORD*,*secret*,*MAVEN_CMD_LINE_ARGS*,sun.java.command," +
            "*token*,*TOKEN*,*LOGIN*,*login*,*key*,*KEY*,*PASSPHRASE*,*signing*"
    }
}
