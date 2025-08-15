/*
 * SonarSource Cloud Native Gradle Modules
 * Copyright (C) 2024-2025 SonarSource SA
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
package org.sonarsource.cloudnative.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPomLicense
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property

/**
 * Settings for publication of the project
 */
open class PublishingConfiguration(
    objects: ObjectFactory,
) {
    val pomName: Property<String> = objects.property()
    val scmUrl: Property<String> = objects.property()
    internal val license: MavenPomLicense = objects.newInstance()

    fun license(action: MavenPomLicense.() -> Unit) {
        action.invoke(license)
    }
}

/**
 * Settings for connection to Artifactory
 */
interface ArtifactoryConfiguration {
    val buildName: Property<String>
    val artifactsToPublish: Property<String>
    val artifactsToDownload: Property<String>
    val repoKeyEnv: Property<String>
    val usernameEnv: Property<String>
    val passwordEnv: Property<String>

    /**
     * Set this to `true` to acknowledge that the publication is deliberately enabled for a project that is not a plugin.
     * Normally, only plugins should be published to Artifactory. If there is a specific reason to publish a non-plugin project,
     * this property acts a safety net to ensure that the user is aware of this decision.
     */
    val acknowledgePublicationOfNonPlugin: Property<Boolean>
}
