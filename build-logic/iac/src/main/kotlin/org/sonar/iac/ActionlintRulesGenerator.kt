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

import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import org.slf4j.LoggerFactory

/**
 * Generator for Actionlint rules by parsing Go source files.
 * It is based on git submodule located in "private/its/sources/github-actions/actionlint".
 * To update rules, please update the submodule commit.
 */
object ActionlintRulesGenerator {
    private const val DESCRIPTION_PREFIX =
        """This issue is raised by the rule "%s" from "actionlint". """ +
            "This is not an issue raised by Sonar analyzers.<br/>"
    private const val DOC_URL_BASE = "https://github.com/rhysd/actionlint/blob/main/docs/checks.md"

    private val LOG = LoggerFactory.getLogger("actionlint-rules-generator")

    // Regex to extract rule name from Go source: name: "rule-id",
    private val NAME_REGEX = """name:\s*"(?<name>[\w-]+)"""".toRegex()

    // Regex to extract rule description from Go source: desc: "description",
    // Handles escaped quotes like \"uses:\" in the description
    private val DESC_REGEX = """desc:\s*"(?<desc>(?:[^"\\]|\\.)*)"""".toRegex()

    // Rules that indicate likely bugs
    private val bugs = listOf(
        "syntax-check",
        "job-needs",
        "matrix",
        "action",
        "workflow-call"
    )

    // Rules that are security hotspots
    private val securityHotspots = listOf(
        "credentials"
    )

    // Rules that should have higher severity
    private val highSeverity = listOf(
        "syntax-check",
        "expression",
        "job-needs",
        "credentials"
    )

    // Regex to extract syntax-check rule from error.go: "syntax-check": {"syntax-check", "description"},
    private val SYNTAX_CHECK_REGEX = """"syntax-check":\s*\{"syntax-check",\s*"(?<desc>[^"]+)"\}""".toRegex()

    /**
     * Extract all Actionlint rules from Go source files.
     * @param rootDir The root directory of the project (typically project.rootDir)
     * @return List of Rule objects for all actionlint rules
     */
    fun extractActionlintRules(rootDir: java.io.File): List<Rule> {
        val basePath = rootDir.toPath().resolve("private/its/sources/github-actions/actionlint")
        val ruleFiles = basePath.listDirectoryEntries("rule_*.go")
            .filterNot { it.fileName.toString().endsWith("_test.go") }
            .sorted()

        val rules = ruleFiles.mapNotNull(::extractRule).toMutableList()

        // Add syntax-check rule which is defined in error.go, not in a rule_*.go file
        if (rules.none { it.id == "syntax-check" }) {
            val errorGoPath = basePath.resolve("error.go")
            val syntaxCheckDesc = extractSyntaxCheckRule(errorGoPath)
            if (syntaxCheckDesc != null) {
                rules.add(createRule("syntax-check", syntaxCheckDesc))
            } else {
                LOG.warn("Could not extract syntax-check rule from $errorGoPath")
            }
        }

        return rules.sortedBy { it.id } + fallbackRule
    }

    /**
     * Extract syntax-check rule description from error.go.
     */
    private fun extractSyntaxCheckRule(errorGoPath: Path): String? {
        val text = errorGoPath.readText()
        return SYNTAX_CHECK_REGEX.find(text)?.groups?.get("desc")?.value
    }

    /**
     * Extract a rule from a Go source file.
     * @param path Path to the rule_*.go file
     * @return Rule object or null if extraction fails
     */
    private fun extractRule(path: Path): Rule? {
        val text = path.readText()

        val name = NAME_REGEX.find(text)?.groups?.get("name")?.value
        if (name == null) {
            LOG.warn("Could not extract rule name from $path")
            return null
        }

        val title = DESC_REGEX.find(text)?.groups?.get("desc")?.value
        if (title == null) {
            LOG.warn("Could not extract description for rule '$name' from $path")
            return null
        }

        return createRule(name, title)
    }

    /**
     * Create a Rule object for a given actionlint rule.
     */
    private fun createRule(
        ruleId: String,
        title: String,
    ): Rule {
        val (type, attribute, softwareQuality, qualityImpact, severity) = classifyRule(ruleId)

        return Rule(
            id = ruleId,
            title = title,
            url = "$DOC_URL_BASE#$ruleId",
            description = DESCRIPTION_PREFIX.format(ruleId),
            tags = listOf("actionlint"),
            type = type,
            severity = severity,
            attribute = attribute,
            softwareQuality = softwareQuality,
            qualityImpact = qualityImpact
        )
    }

    /**
     * Classify a rule based on its ID to determine type, attribute, quality, and impact.
     * @param ruleId Rule ID (e.g., syntax-check, credentials)
     * @return Tuple of (type, attribute, softwareQuality, qualityImpact, severity)
     */
    private fun classifyRule(ruleId: String): List<String> {
        val type = when {
            bugs.contains(ruleId) -> "BUG"
            securityHotspots.contains(ruleId) -> "SECURITY_HOTSPOT"
            else -> "CODE_SMELL"
        }

        val severity = when {
            bugs.contains(ruleId) -> "CRITICAL"
            highSeverity.contains(ruleId) -> "MAJOR"
            ruleId.startsWith("deprecated") -> "MINOR"
            else -> "MAJOR"
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
        toolId = "actionlint",
        docUrl = DOC_URL_BASE
    )
}
