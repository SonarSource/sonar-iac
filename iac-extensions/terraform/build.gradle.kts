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
import de.undercouch.gradle.tasks.download.Download
import org.sonar.iac.GENERATE_EXTERNAL_RULES_TASK_NAME
import org.sonar.iac.GenerateTfLintRulesTask
import org.sonar.iac.TfLintRulesGenerator

plugins {
    id("org.sonarsource.cloud-native.code-style-conventions")
    id("org.sonarsource.cloud-native.java-conventions")
    alias(libs.plugins.download)
}

description = "SonarSource IaC Analyzer :: Extensions :: Terraform"

dependencies {
    api(project(":iac-common"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sslr.test)
    testImplementation(libs.sonar.analyzer.test.commons)
    testImplementation(testFixtures(project(":iac-common")))
    testRuntimeOnly(libs.junit.platform.launcher)
}

val tfLintRulesetNameToDownloadTask = mapOf(
    "terraform" to registerTfLintDownloadTask("terraform"),
    "aws" to registerTfLintDownloadTask("aws", displayName = "AWS"),
    "azurerm" to registerTfLintDownloadTask("azurerm", displayName = "Azure RM", readmePath = "docs/README.md"),
    "google" to registerTfLintDownloadTask("google", displayName = "Google")
)

val generateTfLintRules by tasks.registering(GenerateTfLintRulesTask::class) {
    group = "build"
    description = "Generate the list of rules from all TFLint rulesets"
    dependsOn(tfLintRulesetNameToDownloadTask.values)

    terraformReadmeFile.set(tfLintRulesetNameToDownloadTask["terraform"]!!.get().dest)
    awsReadmeFile.set(tfLintRulesetNameToDownloadTask["aws"]!!.get().dest)
    azurermReadmeFile.set(tfLintRulesetNameToDownloadTask["azurerm"]!!.get().dest)
    googleReadmeFile.set(tfLintRulesetNameToDownloadTask["google"]!!.get().dest)
    rulesFile.set(file("src/main/resources/org/sonar/l10n/terraform/rules/tflint/rules.json"))
}

tasks.register(GENERATE_EXTERNAL_RULES_TASK_NAME) {
    group = "build"
    description = "Generate external linter rules for Terraform"
    dependsOn(generateTfLintRules)
}

private fun registerTfLintDownloadTask(
    rulesetName: String,
    displayName: String = rulesetName.replaceFirstChar { it.uppercase() },
    readmePath: String = "docs/rules/README.md",
) = tasks.register<Download>("downloadTfLintRuleset${rulesetName.replaceFirstChar { it.uppercase() }}") {
    group = "build"
    description = "Download TFLint $displayName ruleset README"

    val branch = TfLintRulesGenerator.getBranchForRuleset(rulesetName)

    src("https://raw.githubusercontent.com/terraform-linters/tflint-ruleset-$rulesetName/$branch/$readmePath")
    dest(layout.buildDirectory.file("tflint-$rulesetName-rules.md"))
}
