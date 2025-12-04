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
import org.sonarsource.cloudnative.gradle.RuleApiExtension
import org.sonarsource.cloudnative.gradle.ifAuthenticatedOrElse
import org.sonarsource.cloudnative.gradle.registerRuleApiGenerateTask
import org.sonarsource.cloudnative.gradle.registerRuleApiUpdateTask
import org.sonarsource.cloudnative.gradle.repox

val ruleApi: Configuration = configurations.create("ruleApi")
val ruleApiExtension = extensions.create<RuleApiExtension>("ruleApi")

repositories {
    ifAuthenticatedOrElse(providers, { artifactoryUsername, artifactoryPassword ->
        repox("sonarsource-private-releases", artifactoryUsername, artifactoryPassword, ruleApiExtension.fileOperations)
        repox("sonarsource", artifactoryUsername, artifactoryPassword, ruleApiExtension.fileOperations)
    }) {
        error("Downloading dependencies from sonarsource-private-releases requires authentication.")
    }
}

dependencies {
    ruleApi("com.sonarsource.rule-api:rule-api:2.17.0.5605")
    ruleApi("org.slf4j:slf4j-nop:1.7.36") {
        because(
            "To get rid of a warning. A logging backend is not needed, because the rule API logs everything important to stdout. " +
                "Slf4j logs contain only debug information"
        )
    }
}

project.afterEvaluate {
    val languageToSonarpediaDirectory = ruleApiExtension.languageToSonarpediaDirectory.get()
    val ruleApiUpdateTasks = mutableSetOf<TaskProvider<JavaExec>>()
    languageToSonarpediaDirectory.forEach { (language, sonarpediaDirectory) ->
        registerRuleApiUpdateTask(language, file(sonarpediaDirectory)).also { ruleApiUpdateTasks.add(it) }
        registerRuleApiGenerateTask(language, file(sonarpediaDirectory))
    }

    tasks.register("ruleApiUpdate") {
        description = "Update ALL rules description"
        group = "Rule API"
        ruleApiUpdateTasks.forEach { this.dependsOn(it) }
    }
}
