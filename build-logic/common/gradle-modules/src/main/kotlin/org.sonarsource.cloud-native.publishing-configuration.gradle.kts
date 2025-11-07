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
import org.gradle.kotlin.dsl.create
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask
import org.sonarsource.cloudnative.gradle.PublishingConfiguration
import org.sonarsource.cloudnative.gradle.signingCondition

plugins {
    signing
    `maven-publish`
    // Connection to artifactory is configured from the root project; applying it here enables publishing of this project
    id("com.jfrog.artifactory")
}

val publishingConfiguration = extensions.create<PublishingConfiguration>("publishingConfiguration")

publishing {
    publications.create<MavenPublication>("mavenJava") {
        pom {
            name = publishingConfiguration.pomName
            description = project.description
            url = "http://www.sonarqube.org/"
            organization {
                name = "SonarSource"
                url = "http://www.sonarsource.com/"
            }
            licenses {
                license {
                    name = publishingConfiguration.license.name
                    url = publishingConfiguration.license.url
                    distribution = publishingConfiguration.license.distribution
                    comments = publishingConfiguration.license.comments
                }
            }
            scm {
                url = publishingConfiguration.scmUrl
            }
            developers {
                developer {
                    id = "sonarsource-team"
                    name = "SonarSource Team"
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
        val artifactorySkip: Boolean = tasks.named<ArtifactoryTask>("artifactoryPublish").map { it.skip }.getOrElse(true)
        !artifactorySkip && project.signingCondition()
    }
}
