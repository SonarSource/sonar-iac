/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
import de.undercouch.gradle.tasks.download.Download
import org.sonar.iac.CfnLintRulesGenerator.extractRules
import org.sonar.iac.asJson

plugins {
    id("org.sonarsource.cloud-native.code-style-conventions")
    id("org.sonarsource.iac.java-conventions")
    alias(libs.plugins.download)
}

description = "SonarSource IaC Analyzer :: Extensions :: Cloudformation"

dependencies {
    api(project(":iac-common"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.sonar.plugin.api.impl)
    testImplementation(libs.sonar.plugin.api.test.fixtures)
    testImplementation(libs.sslr.test)
    testImplementation(testFixtures(project(":iac-common")))
    testRuntimeOnly(libs.junit.platform.launcher)
}

val downloadCfnLintRules by tasks.registering(Download::class) {
    group = "build"
    description = "Download list of rules from the cfn-lint linter"

    src("https://raw.githubusercontent.com/aws-cloudformation/cfn-lint/main/docs/rules.md")
    dest(layout.buildDirectory.file("cfn-lint-rules.md"))
}

val generateCfnLintRules by tasks.registering(Task::class) {
    group = "build"
    description = "Generate the list of rules from the cfn-lint linter"
    dependsOn(downloadCfnLintRules)

    doFirst {
        val rules = extractRules(downloadCfnLintRules.get().dest.readText())
        val rulesFile = file("src/main/resources/org/sonar/l10n/cloudformation/rules/cfn-lint/rules.json")
        rulesFile.writeText(
            """
                |[
                ${rules.joinToString(separator = ",\n") { it.asJson(margin = 2) }}
                |]
            """.trimMargin().plus("\n")
        )
    }
}
