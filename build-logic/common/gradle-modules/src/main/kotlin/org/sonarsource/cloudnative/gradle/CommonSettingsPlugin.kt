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

import com.gradle.develocity.agent.gradle.DevelocityConfiguration
import javax.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.file.FileOperations
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.repositories

open class CommonSettingsPlugin
    @Inject
    constructor(
        private val fileOperations: FileOperations,
    ) : Plugin<Settings> {
        override fun apply(settings: Settings) {
            settings.configureRepositories()

            settings.applyPlugins()

            settings.gradle.allprojects {
                // this value is present on CI
                val buildNumber: String? = System.getProperty("buildNumber")
                val version = properties["version"] as String
                if (version.endsWith("-SNAPSHOT") && buildNumber != null) {
                    val versionSuffix = if (version.count { it == '.' } == 1) ".0.$buildNumber" else ".$buildNumber"
                    project.version = version.replace("-SNAPSHOT", versionSuffix).also {
                        logger.lifecycle("Project ${project.name} version set to $it")
                    }
                }
            }

            // TODO: this task is deprecated and should be removed once all projects are migrated to GHA
            settings.gradle.rootProject {
                tasks.register("storeProjectVersion") {
                    group = "build"
                    description = "Store the project version in a file to be used in CI caches"
                    inputs.property("version", this@rootProject.version)
                    val projectVersionFile =
                        file(
                            "${System.getenv(
                                "CIRRUS_WORKING_DIR"
                            )}/${System.getenv("PROJECT_VERSION_CACHE_DIR")}/evaluated_project_version.txt"
                        )
                    outputs.file(projectVersionFile)
                    outputs.cacheIf { true }

                    doLast {
                        projectVersionFile.writeText(this@rootProject.version.toString())
                    }
                }
            }

            settings.configureDevelocity()
        }

        private fun Settings.configureRepositories() {
            pluginManagement {
                repositories {
                    ifAuthenticatedOrElse(providers, { artifactoryUsername, artifactoryPassword ->
                        repox("sonarsource", artifactoryUsername, artifactoryPassword, fileOperations)
                    }) {
                        mavenCentral()
                        gradlePluginPortal()
                    }
                }
            }

            dependencyResolutionManagement {
                repositories {
                    ifAuthenticatedOrElse(providers, { artifactoryUsername, artifactoryPassword ->
                        repox("sonarsource", artifactoryUsername, artifactoryPassword, fileOperations)
                    }) {
                        mavenCentral()
                    }
                }
            }

            buildscript.repositories {
                ifAuthenticatedOrElse(providers, { artifactoryUsername, artifactoryPassword ->
                    repox("plugins.gradle.org", artifactoryUsername, artifactoryPassword, fileOperations)
                }) {
                    gradlePluginPortal()
                }
            }
        }

        private fun Settings.applyPlugins() {
            // Blowdryer is needed to provide immutable URLs for Spotless (e.g. the Eclipse formatter config)
            settings.pluginManager.apply("com.diffplug.blowdryerSetup")

            settings.pluginManager.apply("com.gradle.develocity")
        }

        private fun Settings.configureDevelocity() {
            val develocity = extensions.getByType<DevelocityConfiguration>()
            val isCI = System.getenv("CI") != null

            extensions.configure<DevelocityConfiguration> {
                server = "https://develocity.sonar.build"
                buildScan {
                    publishing {
                        onlyIf {
                            isCI
                        }
                    }

                    // See https://docs.github.com/en/actions/reference/workflows-and-actions/variables for GHA env vars reference
                    tag(if (isCI) "local" else "CI")
                    tag(System.getProperty("os.name"))
                    val branch = System.getenv("CIRRUS_BRANCH") ?: System.getenv("GITHUB_REF_NAME")
                    if (branch == "master") {
                        tag("master")
                    }
                    val isPr = (System.getenv("CIRRUS_PR")?.isBlank() == false)
                        .or(System.getenv("GITHUB_REF_NAME")?.endsWith("/merge") == true)
                    if (isPr) {
                        tag("PR")
                    }
                    value("Build Number", System.getenv("BUILD_NUMBER"))
                    value("Branch", branch)
                    val prNumber = if (isPr) {
                        System.getenv("CIRRUS_PR") ?: System.getenv("GITHUB_REF_NAME")?.substringBeforeLast("/merge")
                    } else {
                        null
                    }
                    value("PR", prNumber)

                    capture {
                        // `properties` task can log sensitive information, so we disable uploading of build logs for it
                        buildLogging = "properties" !in startParameter.taskNames
                    }
                }
            }

            buildCache {
                local {
                    isEnabled = !isCI
                }
                remote(develocity.buildCache) {
                    isEnabled = true
                    isPush = isCI
                }
            }
        }
    }
