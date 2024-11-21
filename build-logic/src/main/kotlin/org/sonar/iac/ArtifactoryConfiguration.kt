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

import org.gradle.api.provider.Property
import org.gradle.api.publish.maven.MavenPomLicense

interface ArtifactoryConfiguration {
    val artifactsToPublish: Property<String>
    val artifactsToDownload: Property<String>
    val repoKeyEnv: Property<String>
    val usernameEnv: Property<String>
    val passwordEnv: Property<String>
    var license: (MavenPomLicense.() -> Unit)?

    fun license(action: MavenPomLicense.() -> Unit) {
        license = action
    }
}
