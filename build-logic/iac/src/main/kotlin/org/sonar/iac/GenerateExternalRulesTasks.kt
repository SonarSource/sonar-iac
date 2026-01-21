/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Task name for generating or regenerating external linter's `rules.json` file.
 * This task should be defined in each subproject that generates rules from external linters.
 */
const val GENERATE_EXTERNAL_RULES_TASK_NAME = "generateExternalRules"

/**
 * Gradle task to generate Hadolint rules from a README file.
 */
abstract class GenerateHadolintRulesTask : DefaultTask() {
    /**
     * The Hadolint README.md file.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val readmeFile: RegularFileProperty

    @get:OutputFile
    abstract val rulesFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val readme = readmeFile.asFile.get().readText()
        val rules = HadolintRulesGenerator.extractHadolintRules(project.logger, readme)

        writeRulesJson(rules, rulesFile.asFile.get())

        logger.lifecycle("Generated ${rules.count()} Hadolint rules to ${rulesFile.asFile.get().path}")
    }
}

/**
 * Gradle task to generate TFLint rules from multiple ruleset README files.
 */
abstract class GenerateTfLintRulesTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val terraformReadmeFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val awsReadmeFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val azurermReadmeFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val googleReadmeFile: RegularFileProperty

    @get:OutputFile
    abstract val rulesFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val readmeFiles = mapOf(
            "terraform" to terraformReadmeFile.asFile.get().readText(),
            "aws" to awsReadmeFile.asFile.get().readText(),
            "azurerm" to azurermReadmeFile.asFile.get().readText(),
            "google" to googleReadmeFile.asFile.get().readText()
        )

        val rules = TfLintRulesGenerator.extractAllRules(project.logger, readmeFiles)

        writeRulesJson(rules, rulesFile.asFile.get())

        logger.lifecycle("Generated ${rules.size} TFLint rules to ${rulesFile.asFile.get().path}")
    }
}

/**
 * Gradle task to generate CfnLint rules from a Markdown rules file.
 */
abstract class GenerateCfnLintRulesTask : DefaultTask() {
    /**
     * The cfn-lint rules.md file.
     */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rulesMarkdownFile: RegularFileProperty

    @get:OutputFile
    abstract val rulesFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val rulesMarkdown = rulesMarkdownFile.asFile.get().readText()
        val rules = CfnLintRulesGenerator.extractRules(rulesMarkdown)

        writeRulesJson(rules, rulesFile.asFile.get())

        logger.lifecycle("Generated ${rules.size} CfnLint rules to ${rulesFile.asFile.get().path}")
    }
}

/**
 * Gradle task to generate Ansible Lint rules.
 *
 * Note: Ansible Lint rules are extracted from source code in a git submodule,
 * so this task doesn't have downloadable input files.
 */
abstract class GenerateAnsibleLintRulesTask : DefaultTask() {
    @get:OutputFile
    abstract val rulesFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val rules = AnsibleLintRulesGenerator.extractAnsibleRules()

        writeRulesJson(rules, rulesFile.asFile.get())

        logger.lifecycle("Generated ${rules.size} Ansible Lint rules to ${rulesFile.asFile.get().path}")
    }
}

/**
 * Gradle task to generate Actionlint rules.
 *
 * Actionlint rules are extracted from the actionlint Go source files
 * in the git submodule at private/its/sources/github-actions/actionlint.
 * To update rules, update the submodule commit.
 */
abstract class GenerateActionlintRulesTask : DefaultTask() {
    @get:OutputFile
    abstract val rulesFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val rules = ActionlintRulesGenerator.extractActionlintRules(project.rootDir)

        writeRulesJson(rules, rulesFile.asFile.get())

        logger.lifecycle("Generated ${rules.size} Actionlint rules to ${rulesFile.asFile.get().path}")
    }
}
