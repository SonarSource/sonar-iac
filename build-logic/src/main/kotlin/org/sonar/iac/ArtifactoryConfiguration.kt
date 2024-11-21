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
package org.sonar.iac

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPomLicense
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property

open class ArtifactoryConfiguration(
    objects: ObjectFactory,
) {
    val artifactsToPublish: Property<String> = objects.property()
    val artifactsToDownload: Property<String> = objects.property()
    val repoKeyEnv: Property<String> = objects.property()
    val usernameEnv: Property<String> = objects.property()
    val passwordEnv: Property<String> = objects.property()
    internal val license: MavenPomLicense = objects.newInstance()

    fun license(action: MavenPomLicense.() -> Unit) {
        action.invoke(license)
    }
}
