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
package org.sonarsource.cloudnative.gradle

import java.io.File
import org.gradle.api.provider.Property

interface SwiftLicenseGenerationConfig {
    /** Directory where collected Swift license files are placed during build. */
    val buildSwiftLicenseFilesDir: Property<File>

    /** The project's own license file (LICENSE in repo root). */
    val projectLicenseFile: Property<File>

    /** Path to the analyzer directory (used to run `swift package show-dependencies`). */
    val analyzerDir: Property<File>
}
