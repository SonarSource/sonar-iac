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
import org.gradle.api.Project
import org.gradle.api.internal.file.FileOperations
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent

abstract class RuleApiExtension(
    objects: ObjectFactory,
    // This is a workaround for https://github.com/gradle/gradle/issues/13121 to access `FileOperations`.
    // After the corresponding issue is resolved, this can be simplified into an interface.
    val fileOperations: FileOperations,
) {
    val languageToSonarpediaDirectory: MapProperty<String, String> = objects.mapProperty<String, String>()
}

/**
 * An empty build service to serve as a synchronization point for rule-api tasks.
 * Because rule-api requires exclusive access to `$HOME/.sonar/rule-api/rspec`, we force tasks to never run in parallel
 * by configuring this service.
 */
abstract class RuleApiService : BuildService<BuildServiceParameters.None>

fun Project.registerRuleApiUpdateTask(
    language: String,
    sonarpediaLocation: File,
): TaskProvider<JavaExec> =
    registerRuleApiTask("ruleApiUpdate$language") {
        val branch = providers.gradleProperty("branch")
        description = "Update $language rules description"

        workingDir = sonarpediaLocation
        args(
            buildList {
                add("update")
                if (branch.isPresent) {
                    add("-branch")
                    add(branch.get())
                }
            }
        )
    }

fun Project.registerRuleApiGenerateTask(
    language: String,
    sonarpediaLocation: File,
): TaskProvider<JavaExec> =
    registerRuleApiTask("ruleApiGenerateRule$language") {
        val rule = providers.gradleProperty("rule")
        val branch = providers.gradleProperty("branch")
        description = "Update rule description for $language"

        workingDir = sonarpediaLocation
        args(
            buildList {
                add("generate")
                add("-rule")
                add(rule.getOrElse(""))
                if (branch.isPresent) {
                    add("-branch")
                    add(branch.get())
                }
            }
        )

        doFirst {
            if (!rule.isPresent) {
                error("To generate rule rspec, please provide -Prule=SXXXX")
            }
        }
    }

private fun Project.registerRuleApiTask(
    name: String,
    configure: JavaExec.() -> Unit,
): TaskProvider<JavaExec> =
    tasks.register<JavaExec>(name) {
        group = "Rule API"
        usesService(
            gradle.sharedServices.registerIfAbsent("ruleApiRepoProvider", RuleApiService::class) {
                // because rule-api requires exclusive access to `$HOME/.sonar/rule-api/rspec`, we force tasks to never run in parallel
                maxParallelUsages = 1
            }
        )
        classpath = configurations.getByName("ruleApi")
        mainClass = "com.sonarsource.ruleapi.Main"
        outputs.upToDateWhen {
            // As rule-api fetches data from rspec repo, we can't determine if the task is up-to-date
            false
        }

        configure(this)
    }
