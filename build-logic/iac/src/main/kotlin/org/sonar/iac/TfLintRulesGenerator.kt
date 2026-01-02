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

import org.gradle.api.logging.Logger

/**
 * Generator for TFLint rules from multiple rulesets.
 * Supports terraform, aws, azurerm, and google rulesets.
 */
object TfLintRulesGenerator {
    /**
     * Description prefix added to each rule description.
     * Note: quotes in this string must be escaped to produce valid JSON.
     */
    private const val DESCRIPTION_PREFIX =
        """This issue is raised by the rule [%s] from \"TFLint\". This is not an issue raised by Sonar analyzers.<br/><br/>TFLint Message: %s"""

    /**
     * Marker text used in markdown table headers to identify and skip header rows.
     */
    private const val TABLE_HEADER_DESCRIPTION = "Description"

    data class RulesetConfig(
        val name: String,
        val baseUrl: String,
        val branch: String = "master",
    )

    val rulesets = listOf(
        RulesetConfig(
            name = "terraform",
            baseUrl = "https://github.com/terraform-linters/tflint-ruleset-terraform/blob/main/docs/rules/",
            branch = "main"
        ),
        RulesetConfig(
            name = "aws",
            baseUrl = "https://github.com/terraform-linters/tflint-ruleset-aws/blob/master/docs/rules/"
        ),
        RulesetConfig(
            name = "azurerm",
            baseUrl = "https://github.com/terraform-linters/tflint-ruleset-azurerm/blob/master/docs/rules/"
        ),
        RulesetConfig(
            name = "google",
            baseUrl = "https://github.com/terraform-linters/tflint-ruleset-google/blob/master/docs/rules/"
        )
    )

    /**
     * Get the branch name for a given ruleset.
     * This is used by build scripts to download the correct README files.
     */
    fun getBranchForRuleset(name: String): String = rulesets.first { it.name == name }.branch

    /**
     * Extract rules from all TFLint rulesets by parsing README.md files.
     * Each README contains a markdown table with rule IDs and descriptions.
     */
    fun extractAllRules(
        logger: Logger,
        readmeFiles: Map<String, String>,
    ): List<Rule> =
        rulesets.flatMap { config ->
            val readme = readmeFiles[config.name] ?: error("Missing README for ruleset: ${config.name}")
            extractRulesFromReadme(logger, readme, config)
        } + fallbackRule

    /**
     * Parse a README.md file to extract rule entries.
     * Some md files contain multiple tables, so we try to parse all of them.
     * Handles different table formats across rulesets.
     */
    private fun extractRulesFromReadme(
        logger: Logger,
        readme: String,
        config: RulesetConfig,
    ): List<Rule> {
        val individualRuleReadmeFiles = fetchGitHubDirectoryMarkdownFiles(config.baseUrl, logger)

        val tableConfig = MarkdownTableConfig(
            headerRegex = """\|(?:Rule|Name)\|.++""".toRegex(),
            // READMEs for tflint rulesets sometimes contain multiple tables
            parseMultipleTables = true
        )
        val rules = parseMarkdownTable(readme, tableConfig, logger) { line ->
            parseTableRow(line, config, individualRuleReadmeFiles)
        }

        return rules
    }

    /**
     * Parse a single table row to extract rule information.
     * Expected format: | rule_id | description (optional) | ... |
     */
    private fun parseTableRow(
        line: String,
        config: RulesetConfig,
        individualRuleReadmeFiles: Set<String>,
    ): Rule? {
        val columns = line.split("|")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (columns.size < 2) {
            return null
        }

        // First column is the rule ID (may contain a Markdown link)
        var ruleId = columns[0]
            .replace(Regex("\\[(.+?)]\\(.+?\\)"), "$1") // Extract text from a Markdown link
            .trim()

        // If there is a description, it is in the second column
        val description = columns[1]
            .replace(Regex("\\[(.+?)]\\(.+?\\)"), "$1") // Remove Markdown links
            .trim()
            .asSafeJson()

        if (ruleId.isEmpty() || description.isEmpty() || description == TABLE_HEADER_DESCRIPTION) {
            return null
        }

        // Clean up rule ID - remove any surrounding backticks or special chars
        ruleId = ruleId.replace("`", "").trim()

        val url = if (ruleId in individualRuleReadmeFiles) {
            "${config.baseUrl}$ruleId.md"
        } else {
            "${config.baseUrl}README.md"
        }

        val (type, attribute, softwareQuality, qualityImpact) = classifyRule(config.name, ruleId)

        return Rule(
            id = ruleId,
            title = ruleId.split("_")
                .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } },
            url = url,
            description = DESCRIPTION_PREFIX.format(ruleId, description),
            tags = listOf("tflint"),
            type = type,
            attribute = attribute,
            softwareQuality = softwareQuality,
            qualityImpact = qualityImpact
        )
    }

    /**
     * Classify a rule based on its ID to determine type and attributes.
     */
    private fun classifyRule(
        ruleset: String,
        ruleId: String,
    ): List<String> {
        val securityHotspots = listOf(
            "terraform_module_pinned_source",
            "terraform_module_version",
            "terraform_required_providers"
        )

        val codeSmells = listOf(
            "aws_acm_certificate_lifecycle",
            "aws_db_instance_previous_type",
            "aws_db_instance_default_parameter_group",
            "aws_elasticache_cluster_previous_type",
            "aws_elasticache_cluster_default_parameter_group",
            "aws_elasticache_replication_group_previous_type",
            "aws_elasticache_replication_group_default_parameter_group",
            "aws_instance_previous_type",
            "aws_iam_policy_document_gov_friendly_arns",
            "aws_iam_policy_gov_friendly_arns",
            "aws_iam_role_policy_gov_friendly_arns",
            "aws_lambda_function_deprecated_runtime",
            "aws_resource_missing_tags",
            "aws_s3_bucket_name"
        )

        return when {
            securityHotspots.any { ruleId.contains(it) } -> listOf("SECURITY_HOTSPOT", "TRUSTWORTHY", "SECURITY", "MEDIUM")
            ruleset == "terraform" || codeSmells.any { ruleId.contains(it) } ->
                listOf("CODE_SMELL", "CONVENTIONAL", "MAINTAINABILITY", "MEDIUM")
            else -> listOf("BUG", "LOGICAL", "RELIABILITY", "MEDIUM")
        }
    }

    private val fallbackRule = createFallbackRule(
        toolId = "tflint",
        toolName = "TFLint",
        docUrl = "https://github.com/terraform-linters/tflint"
    )
}
