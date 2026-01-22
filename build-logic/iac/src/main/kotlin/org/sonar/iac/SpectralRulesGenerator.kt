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
import kotlin.io.path.readText
import org.slf4j.LoggerFactory

object SpectralRulesGenerator {
    private const val SPECTRAL_DESCRIPTION_PREFIX =
        "This issue is raised by the rule \"%s\" from \"Spectral\". " +
            "This is not an issue raised by Sonar analyzers.<br/>"
    private const val RULE_URL_TEMPLATE = "https://meta.stoplight.io/docs/spectral/docs/reference/%s-rules.md#%s"
    private const val RULE_URL_DEFAULT = "https://docs.stoplight.io/docs/spectral/"

    private val LOG = LoggerFactory.getLogger("some-logger")
    private val RULE_PATTERN = """'([^']+)':\s*\{""".toRegex()
    private val DESCRIPTION_REGEX = """description:\s*'([^']+)'""".toRegex()

    // Map rule IDs to override titles if needed
    private val ruleTitles = mapOf<String, String>(
        // Add any title overrides here if needed
    )

    // Categorization for different rule types
    private val bugs = listOf<String>(
        // Add bug-type rules here
    )

    private val vulnerabilityHighTrustworthy = listOf(
        "no-eval-in-markdown",
        "no-script-tags-in-markdown",
        "arazzo-no-script-tags-in-markdown"
    )

    private val securityHotspotMediumComplete = listOf<String>(
        // Add security hotspot rules here
    )

    private val codeSmellsLowClear = listOf(
        "no-\$ref-siblings",
        "array-items",
        "typed-enum",
        "oas2-discriminator",
        "oas2-valid-schema-example",
        "oas2-valid-media-example",
        "oas2-schema",
        "oas3-valid-media-example",
        "oas3-valid-schema-example",
        "oas3-schema",
        "oas3-server-variables",
        "asyncapi-channel-parameters",
        "asyncapi-headers-schema-type-object",
        "asyncapi-3-headers-schema-type-object",
        "asyncapi-message-examples",
        "asyncapi-message-messageId-uniqueness",
        "asyncapi-operation-operationId-uniqueness",
        "asyncapi-operation-operationId",
        "asyncapi-payload-default",
        "asyncapi-payload-examples",
        "asyncapi-payload",
        "asyncapi-schema-default",
        "asyncapi-schema-examples",
        "asyncapi-schema",
        "asyncapi-server-variables",
        "asyncapi-tags-uniqueness",
        "asyncapi-3-tags-uniqueness",
        "asyncapi-3-document-resolved",
        "asyncapi-3-document-unresolved",
        "arazzo-document-schema",
        "arazzo-workflowId-unique",
        "arazzo-workflow-output-validation",
        "arazzo-workflow-stepId-unique",
        "arazzo-step-output-validation",
        "arazzo-step-parameters-validation",
        "arazzo-step-failure-actions-validation",
        "arazzo-step-success-actions-validation",
        "arazzo-workflow-depends-on-validation",
        "arazzo-step-success-criteria-validation",
        "arazzo-step-request-body-validation",
        "arazzo-step-validation"
    )

    private val codeSmellsHighClear = listOf(
        "operation-operationId-unique",
        "path-params",
        "openapi-tags-uniqueness"
    )

    private val codeSmellLowConventional = listOf(
        "contact-properties",
        "info-contact",
        "info-description",
        "info-license",
        "license-url",
        "openapi-tags-alphabetical",
        "openapi-tags",
        "operation-description",
        "operation-operationId",
        "operation-operationId-valid-in-url",
        "operation-singular-tag",
        "operation-tags",
        "path-declarations-must-exist",
        "path-keys-no-trailing-slash",
        "path-not-include-query",
        "tag-description",
        "oas2-api-host",
        "oas2-api-schemes",
        "oas2-host-not-example",
        "oas2-host-trailing-slash",
        "oas2-parameter-description",
        "oas2-anyOf",
        "oas2-oneOf",
        "oas2-unused-definition",
        "oas3-api-servers",
        "oas3-examples-value-or-externalValue",
        "oas3-parameter-description",
        "oas3-server-not-example.com",
        "oas3-server-trailing-slash",
        "oas3-unused-component",
        "asyncapi-channel-no-empty-parameter",
        "asyncapi-3-channel-no-empty-parameter",
        "asyncapi-channel-no-query-nor-fragment",
        "asyncapi-3-channel-no-query-nor-fragment",
        "asyncapi-channel-no-trailing-slash",
        "asyncapi-3-channel-no-trailing-slash",
        "asyncapi-info-contact-properties",
        "asyncapi-info-contact",
        "asyncapi-info-description",
        "asyncapi-info-license-url",
        "asyncapi-info-license",
        "asyncapi-operation-description",
        "asyncapi-3-operation-description",
        "asyncapi-parameter-description",
        "asyncapi-server-no-empty-variable",
        "asyncapi-3-server-no-empty-variable",
        "asyncapi-server-no-trailing-slash",
        "asyncapi-3-server-no-trailing-slash",
        "asyncapi-server-not-example-com",
        "asyncapi-3-server-not-example-com",
        "asyncapi-servers",
        "asyncapi-tag-description",
        "asyncapi-3-tag-description",
        "asyncapi-tags-alphabetical",
        "asyncapi-3-tags-alphabetical",
        "asyncapi-tags",
        "asyncapi-3-tags",
        "asyncapi-unused-components-schema",
        "asyncapi-unused-components-server",
        "arazzo-source-descriptions-type",
        "arazzo-workflow-workflowId",
        "arazzo-workflow-description",
        "arazzo-step-description",
        "arazzo-step-stepId"
    )

    private val codeSmellMediumLogical = listOf(
        "operation-success-response",
        "oas2-operation-formData-consume-check",
        "operation-parameters",
        "operation-tag-defined",
        "duplicated-entry-in-enum",
        "oas2-operation-security-defined",
        "oas3-operation-security-defined",
        "oas3-callbacks-in-callbacks",
        "oas3_1-servers-in-webhook",
        "oas3_1-callbacks-in-webhook",
        "asyncapi-channel-servers",
        "asyncapi-3-channel-servers",
        "asyncapi-operation-security",
        "asyncapi-3-operation-security",
        "asyncapi-payload-unsupported-schemaFormat",
        "asyncapi-3-payload-unsupported-schemaFormat",
        "asyncapi-server-security",
        "asyncapi-latest-version",
        "arazzo-info-description",
        "arazzo-info-summary",
        "arazzo-workflow-summary",
        "arazzo-step-operationPath"
    )

    /**
     * Extract rules from Spectral TypeScript ruleset files.
     * It is based on git submodule located in "private/its/sources/spectral/spectral/packages/rulesets/src/".
     * To update rules, please update the submodule commit.
     */
    fun extractSpectralRules(): List<Rule> {
        val oasRules = extractRulesFromFile(
            Path.of("private/its/sources/spectral/spectral/packages/rulesets/src/oas/index.ts"),
            "openapi"
        )
        val asyncApiRules = extractRulesFromFile(
            Path.of("private/its/sources/spectral/spectral/packages/rulesets/src/asyncapi/index.ts"),
            "asyncapi"
        )
        val arazzoRules = extractRulesFromFile(
            Path.of("private/its/sources/spectral/spectral/packages/rulesets/src/arazzo/index.ts"),
            "arazzo"
        )

        return oasRules + asyncApiRules + arazzoRules + listOf(fallbackRule)
    }

    private fun extractRulesFromFile(
        path: Path,
        rulesetType: String,
    ): List<Rule> {
        LOG.info("Processing rules from: $path")
        val text = path.readText()

        val rules = mutableListOf<Rule>()

        // Find all rules in the format: 'rule-name': { ... }
        val matches = RULE_PATTERN.findAll(text)

        for (match in matches) {
            val ruleId = match.groupValues[1]

            // Skip if not a real rule (e.g., aliases or other properties)
            if (ruleId in listOf("documentationUrl", "formats", "aliases", "rules")) {
                continue
            }

            // Extract the rule block
            val ruleStart = match.range.first
            val ruleBlock = extractRuleBlock(text, ruleStart)

            if (ruleBlock != null) {
                val description = extractDescription(ruleBlock)
                if (description != null) {
                    val rule = createRule(ruleId, description, rulesetType)
                    rules.add(rule)
                    LOG.info("Extracted rule: $ruleId")
                }
            }
        }

        return rules
    }

    private fun extractRuleBlock(
        text: String,
        startPos: Int,
    ): String? {
        var braceCount = 0
        var inBlock = false
        var blockStart = -1

        for (i in startPos until text.length) {
            when (text[i]) {
                '{' -> {
                    if (!inBlock) {
                        blockStart = i
                        inBlock = true
                    }
                    braceCount++
                }

                '}' -> {
                    braceCount--
                    if (braceCount == 0 && inBlock) {
                        return text.substring(blockStart, i + 1)
                    }
                }
            }
        }

        return null
    }

    private fun extractDescription(ruleBlock: String): String? {
        val descMatch = DESCRIPTION_REGEX.find(ruleBlock)
        return descMatch?.groupValues?.get(1)
    }

    private fun createRule(
        id: String,
        description: String,
        rulesetType: String,
    ): Rule {
        // Escape quotes in the title
        val title = (ruleTitles[id] ?: description.take(200)).replace("\"", "\\\"")
        val url = RULE_URL_TEMPLATE.format(rulesetType, id)
        val formattedDescription = SPECTRAL_DESCRIPTION_PREFIX.format(id)
        val (type, attribute, softwareQuality, qualityImpact) = extractAttributes(id)

        return Rule(
            id = id,
            title = title,
            url = url,
            description = formattedDescription,
            tags = listOf("spectral"),
            type = type,
            attribute = attribute,
            softwareQuality = softwareQuality,
            qualityImpact = qualityImpact
        )
    }

    private fun extractAttributes(id: String): List<String> =
        when {
            bugs.contains(id) ->
                listOf("BUG", "LOGICAL", "RELIABILITY", "MEDIUM")

            vulnerabilityHighTrustworthy.contains(id) ->
                listOf("VULNERABILITY", "TRUSTWORTHY", "SECURITY", "HIGH")

            securityHotspotMediumComplete.contains(id) ->
                listOf("SECURITY_HOTSPOT", "COMPLETE", "SECURITY", "MEDIUM")

            codeSmellsLowClear.contains(id) ->
                listOf("CODE_SMELL", "CLEAR", "MAINTAINABILITY", "LOW")

            codeSmellsHighClear.contains(id) ->
                listOf("CODE_SMELL", "CLEAR", "MAINTAINABILITY", "HIGH")

            codeSmellLowConventional.contains(id) ->
                listOf("CODE_SMELL", "CONVENTIONAL", "MAINTAINABILITY", "LOW")

            codeSmellMediumLogical.contains(id) ->
                listOf("CODE_SMELL", "LOGICAL", "MAINTAINABILITY", "MEDIUM")

            else -> listOf("CODE_SMELL", "CONVENTIONAL", "MAINTAINABILITY", "MEDIUM")
        }

    private val fallbackRule = createFallbackRule(
        toolId = "spectral",
        toolName = "Spectral",
        docUrl = RULE_URL_DEFAULT
    )
}
