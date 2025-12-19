/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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

import org.gradle.api.logging.Logger

/**
 * Generator for Hadolint rules from GitHub README.
 * Parses the rules table from [readme on GitHub](https://github.com/hadolint/hadolint/blob/master/README.md)
 */
object HadolintRulesGenerator {
    private const val HADOLINT_DESCRIPTION_PREFIX =
        """This issue is raised by the rule \"%s\" from \"Hadolint\". """ +
            "This is not an issue raised by Sonar analyzers.<br/>"
    private const val SHELLCHECK_DESCRIPTION_PREFIX =
        """This issue is raised by the rule \"%s\" from \"ShellCheck\". """ +
            "This is not an issue raised by Sonar analyzers.<br/>"
    private const val WIKI_URL_BASE = "https://github.com/hadolint/hadolint/wiki"

    private val bugs = listOf(
        "DL3000",
        "DL3004",
        "DL3005",
        "DL3011",
        "DL3012",
        "DL3020",
        "DL3021",
        "DL3023",
        "DL3024",
        "DL3026",
        "DL3043",
        "DL3044",
        "DL3061",
        "DL4000",
        "DL4004",
        // shellcheck rules
        "SC1007",
        "SC1045",
        "SC1077",
        "SC1081",
        "SC1087"
    )

    private val securityHotspots = listOf("DL3002")

    /**
     * Extract all Hadolint and ShellCheck rules by parsing the README table.
     * @param logger Gradle logger for logging warnings and info
     * @param readme README.md content from Hadolint repository
     * @return List of Rule objects for all rules in the table
     */
    fun extractHadolintRules(
        logger: Logger,
        readme: String,
    ): List<Rule> {
        logger.lifecycle("Generating Hadolint rules from GitHub README...")

        val rules = parseRulesTable(logger, readme)

        logger.lifecycle("Extracted ${rules.size} rules from Hadolint README")
        return rules.sortedWith(
            compareBy(
                // Put ShellCheck rules at the end
                { if (it.id.startsWith("SC")) 1 else 0 },
                // ... and then sort alphabetically by rule ID
                { it.id }
            )
        ) + fallbackRule
    }

    /**
     * Parse the rules table from README markdown.
     * First finds the "## Rules" section, then parses the first table that follows.
     * Table format:
     * ```
     * | Rule | Default Severity | Description |
     * | [DL3000](link) | Error | Use absolute WORKDIR. |
     * ```
     */
    private fun parseRulesTable(
        logger: Logger,
        readme: String,
    ): List<Rule> {
        val config = MarkdownTableConfig(
            headerRegex = """^##\s+Rules""".toRegex(),
            parseMultipleTables = false
        )
        return parseMarkdownTable(readme, config, logger) { line -> parseTableRow(line) }
    }

    /**
     * Parse a single table row to extract rule information.
     * Row format: `| [ruleId](link) | severity | Description |`
     */
    private fun parseTableRow(line: String): Rule {
        val columns = line.trim().split("|")
            .map { it.trim() }
            .drop(1) // drop first empty element before first |
            .dropLast(1) // drop last empty element after last |

        require(columns.size == 3) {
            "Expected 3 columns in a row, but found ${columns.size} columns."
        }

        // Extract rule ID from first column: [DL3000](https://...)
        val ruleIdRegex = """\[([A-Z]{2}\d+)]""".toRegex()
        val ruleIdMatch = ruleIdRegex.find(columns[0]) ?: error(
            "Failed to extract rule ID from column: '${columns[0]}'"
        )
        val ruleId = ruleIdMatch.groupValues[1]

        val defaultSeverity = columns[1]

        val description = columns[2]
            // escape single backslashes
            .replace("""([^\\])\\([^"])""".toRegex(), """$1\\\\$2""")
            // escape a backslash within an escaped quote
            .replace("""([^\\])\\"""".toRegex(), """$1\\\\\\"""")
            // escape an unescaped quote
            .replace("""([^\\])"""".toRegex(), """$1\\"""")

        val wikiUrl = if (ruleId.startsWith("SC")) {
            "https://github.com/koalaman/shellcheck/wiki/$ruleId"
        } else {
            "$WIKI_URL_BASE/$ruleId"
        }

        // Determine tags and description prefix
        val (tags, descriptionPrefix) = if (ruleId.startsWith("SC")) {
            Pair(listOf("shellcheck", "hadolint"), SHELLCHECK_DESCRIPTION_PREFIX)
        } else {
            Pair(listOf("hadolint"), HADOLINT_DESCRIPTION_PREFIX)
        }

        val (type, attribute, softwareQuality, qualityImpact, severity) = classifyRule(ruleId, defaultSeverity)

        return Rule(
            id = ruleId,
            title = description,
            url = wikiUrl,
            description = descriptionPrefix.format(ruleId),
            tags = tags,
            type = type,
            severity = severity,
            attribute = attribute,
            softwareQuality = softwareQuality,
            qualityImpact = qualityImpact
        )
    }

    /**
     * Classify a rule based on its ID and default severity to determine type, attribute, quality, and impact.
     * @param ruleId Rule ID (e.g., DL3000, SC2086)
     * @param defaultSeverity Default severity from Hadolint (Ignore, Info, Warning, Error)
     * @return Tuple of (type, attribute, softwareQuality, qualityImpact, severity)
     */
    private fun classifyRule(
        ruleId: String,
        defaultSeverity: String,
    ): List<String> {
        val type = when {
            bugs.contains(ruleId) -> "BUG"
            securityHotspots.contains(ruleId) -> "SECURITY_HOTSPOT"
            else -> "CODE_SMELL"
        }

        val severity = when {
            bugs.contains(ruleId) -> "CRITICAL"
            ruleId.startsWith("SC") && type == "CODE_SMELL" -> "MAJOR"
            else -> when (defaultSeverity.lowercase()) {
                "error" -> "CRITICAL"
                "warning" -> "MAJOR"
                "info" -> "MINOR"
                "ignore" -> "INFO"
                else -> "MINOR"
            }
        }

        val (attribute, softwareQuality, qualityImpact) = when (type) {
            "BUG" -> Triple("LOGICAL", "RELIABILITY", "HIGH")
            "SECURITY_HOTSPOT" -> Triple("TRUSTWORTHY", "SECURITY", "MEDIUM")
            "CODE_SMELL" -> when (severity) {
                "INFO", "MINOR" -> Triple("CONVENTIONAL", "MAINTAINABILITY", "LOW")
                else -> Triple("CONVENTIONAL", "MAINTAINABILITY", "MEDIUM")
            }
            else -> Triple("CONVENTIONAL", "MAINTAINABILITY", "MEDIUM")
        }

        return listOf(type, attribute, softwareQuality, qualityImpact, severity)
    }

    private val fallbackRule = createFallbackRule(
        toolId = "hadolint",
        docUrl = WIKI_URL_BASE
    )
}
