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

object CfnLintRulesGenerator {
    private const val DESCRIPTION_PREFIX =
        "This issue is raised by the rule [%s] from \\\"AWS CloudFormation Linter\\\" (aka cfn-lint). " +
            "This is not an issue raised by Sonar analyzers.<br/>" +
            "<br/>" +
            "AWS CloudFormation Linter Message: %s"
    private const val RULE_URL = "https://github.com/aws-cloudformation/cfn-lint/blob/main/docs/rules.md#%s"

    /**
     * Extract rules from the Markdown table in the [list of rules](https://github.com/aws-cloudformation/cfn-lint/blob/main/docs/rules.md#rules-1)
     */
    fun extractRules(input: String): List<Rule> {
        val tableRows = input.lineSequence()
            .dropWhile { it != "## Rules" }
            .dropWhile { !it.startsWith("|") }
            // drop table header and columns formatting line
            .drop(2)
            .takeWhile { it.startsWith("|") }
        return tableRows.map(::extractRule).toList().plusElement(fallbackRule)
    }

    private fun extractRule(line: String): Rule {
        val columns = line.split("|").map { it.trim() }
        // IDs also contain links: `E0000<a ...></a>`
        val id = columns[1].trimStart('[').takeWhile { it != '<' }
        val type = when {
            id.startsWith("E") -> "BUG"
            else -> "CODE_SMELL"
        }
        val severity = when {
            id.startsWith("I") -> "INFO"
            else -> "MAJOR"
        }
        val tags = columns[6].split(",")
            .map { it.trim().trim('`') }
            .filterNot { it.isBlank() }
            // Plugin API requires tags to match `^[a-z0-9\+#\-\.]+$`
            .map { it.replace(" ", "-").replace("_", "-").toCamelCase() }
        val attribute = when {
            type == "BUG" -> "LOGICAL"
            else -> "CONVENTIONAL"
        }
        val softwareQuality = when {
            type == "BUG" -> "RELIABILITY"
            else -> "MAINTAINABILITY"
        }
        val qualitySeverity = when {
            severity == "INFO" -> "LOW"
            else -> "MEDIUM"
        }
        return Rule(
            id,
            columns[2],
            RULE_URL.format(id),
            DESCRIPTION_PREFIX.format(id, columns[3]),
            tags,
            type,
            severity,
            attribute,
            softwareQuality,
            qualitySeverity
        )
    }

    private val fallbackRule = createFallbackRule(
        toolId = "cfn-lint",
        toolName = "cfn-lint",
        docUrl = RULE_URL.format("")
    )

    private fun String.toCamelCase() = replace("[a-z][A-Z]".toRegex()) { it.value.first() + "-" + it.value.last().lowercase() }
}
