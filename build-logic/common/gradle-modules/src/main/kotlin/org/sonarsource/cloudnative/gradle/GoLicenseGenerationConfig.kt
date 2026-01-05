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
import org.gradle.api.provider.SetProperty

interface GoLicenseGenerationConfig {
    /**
     * Name of the binaries files for which licenses should be packaged.
     */
    val packagedBinaries: SetProperty<String>

    /**
     * License file of the binary itself to be included in the generated license files.
     * Normally it is the LICENSE.txt file present in the root of the repository.
     */
    val binaryLicenseFile: Property<File>

    /**
     * Directory where the generated Go license files will be placed.
     */
    val buildGoLicenseFilesDir: Property<File>

    /**
     * Name of the Gradle task that generates the Go license files.
     */
    val generatingGoLicensesGradleTask: Property<String>
}
