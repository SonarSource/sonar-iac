package org.sonar.iac

import java.io.File
import org.gradle.api.Project
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent

/**
 * An empty build service to serve as a synchronization point for rule-api tasks.
 * Because rule-api requires exclusive access to `$HOME/.sonar/rule-api/rspec`, we force tasks to never run in parallel
 * by configuring this service.
 */
abstract class RuleApiService : BuildService<BuildServiceParameters.None>

fun Project.registerAllRuleApiTasks() {
    val iacExtensionNames =
        gradle.rootProject.allprojects.filter {
            it.path.startsWith(":iac-extensions:") &&
                // For jvm-framework-config, we don't (yet) have separate rules.
                it.name != "jvm-framework-config"
        }.map {
            it.name
        }

    val ruleApiUpdateTasks = iacExtensionNames.map {
        name -> registerRuleApiUpdateTask(name.toCamelCase(), file("$projectDir/iac-extensions/$name/"))
    }

    tasks.register("ruleApiUpdate") {
        description = "Update ALL rules description"
        group = "Rule API"
        ruleApiUpdateTasks.forEach { this.dependsOn(it) }
    }

    iacExtensionNames.forEach {
        name -> registerRuleApiGenerateTask(name.toCamelCase(), file("$projectDir/iac-extensions/$name/"))
    }
}

fun Project.registerRuleApiUpdateTask(suffix: String, sonarpediaLocation: File): TaskProvider<JavaExec> {
    return registerRuleApiTask("ruleApiUpdate$suffix") {
        description = "Update $suffix rules description"

        workingDir = sonarpediaLocation
        args("com.sonarsource.ruleapi.Main", "update")
    }
}

fun Project.registerRuleApiGenerateTask(suffix: String, sonarpediaLocation: File): TaskProvider<JavaExec> {
    val rule = providers.gradleProperty("rule")
    val branch = providers.gradleProperty("branch")
    return registerRuleApiTask("ruleApiGenerateRule$suffix") {
        description = "Update rule description for $suffix"

        workingDir = sonarpediaLocation
        args(
            buildList {
                add("com.sonarsource.ruleapi.Main")
                add("generate")
                add("-rule")
                add(rule.getOrElse(""))
                if (branch.isPresent) {
                    add("-branch")
                    add(branch.get())
                }
            }
        )
    }
}

fun Project.registerRuleApiTask(name: String, configure: JavaExec.() -> Unit): TaskProvider<JavaExec> =
    tasks.register<JavaExec>(name) {
        group = "Rule API"
        usesService(gradle.sharedServices.registerIfAbsent("ruleApiRepoProvider", RuleApiService::class) {
            // because rule-api requires exclusive access to `$HOME/.sonar/rule-api/rspec`, we force tasks to never run in parallel
            maxParallelUsages = 1
        })
        classpath = configurations.getByName("ruleApi")
        configure(this)
    }

private fun String.toCamelCase() = replace("-[a-z]".toRegex()) { it.value.last().uppercase() }.capitalized()
