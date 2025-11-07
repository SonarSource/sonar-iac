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
package org.sonarsource.cloudnative.gradle

import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.named

interface IntegrationTestExtension {
    /**
     * The directory containing the source files for the integration tests.
     * Will be treated as a task input for the integration test task to check if the task is up-to-date.
     */
    val testSources: DirectoryProperty

    /**
     * Additional configuration for the integration test task.
     */
    fun Project.configureTask(action: Test.() -> Unit) {
        tasks.named<Test>("integrationTest", action)
    }

    /**
     * Additional configuration for the integration test task.
     */
    fun Project.configureTask(action: Closure<*>) {
        tasks.named<Test>("integrationTest") {
            action.call(this)
        }
    }
}
