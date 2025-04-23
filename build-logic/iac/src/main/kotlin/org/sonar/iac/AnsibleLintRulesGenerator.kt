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
package org.sonar.iac

import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText
import org.slf4j.LoggerFactory

object AnsibleLintRulesGenerator {
    private const val ANSIBLE_DESCRIPTION_PREFIX =
        "This issue is raised by the rule \"%s\" from \"Ansible Lint\" (aka ansible-lint). " +
            "This is not an issue raised by Sonar analyzers.<br/>"
    private const val RULE_URL_TEMPLATE = "https://ansible.readthedocs.io/projects/lint/rules/%s/"
    private const val RULE_URL_DEFAULT = "https://ansible.readthedocs.io/projects/lint/rules/"

    private val LOG = LoggerFactory.getLogger("some-logger")
    private val ID_REGEX = """\s++id\s*+=\s*+"(?<id>[\w-]*+)"""".toRegex()

    // Search for: shortdesc = "some title"
    private val SHORT_DESCRIPTION_REGEX = """shortdesc\s*+=\s*+"(?<title>[\w-.\s]*+)"""".toRegex()

    // Search for: description = "some description"
    private val DESCRIPTION_REGEX = """description\s*+=\s*+"(?<title>[\w\s-.`,:()!|>']*+)"""".toRegex()

    // Search for: description = (
    //  "some multiline"
    //  "title"
    //  )
    private val DESCRIPTION_REGEX_MULTILINE = """description\s*+=\s*+\(?\n*+\s*+("(?<titles>[\w\s-.`,:()!|>']*+)"\n?\s*+)*+\)?""".toRegex()

    // Extracts the string content: "some title"
    private val DESCRIPTION_STRING = """("(?<string>[\w\s-.`,:()!|>]*+)"\n?\s*+)""".toRegex()

    // titles for rules where shortdesc/description is missing or contains unexpected characters or is too long
    private val ruleTitles = mapOf(
        Pair("empty-string-compare", "Don't compare to empty string"),
        Pair("ignore-errors", "Use failed_when and specify error conditions instead of using ignore_errors"),
        Pair("jinja", "Rule that looks inside jinja2 templates"),
        Pair("key-order", "Ensure specific order of keys in mappings"),
        Pair("literal-compare", "Don't compare to literal True/False"),
        Pair("loop-var-prefix", "Looping inside roles has the risk of clashing with loops from user-playbooks"),
        Pair("meta-incorrect", "The meta/main.yml default values should be changed"),
        Pair("meta-video-links", "The meta/main.yml video_links should be formatted correctly"),
        Pair("no-changed-when", "Commands should not change things if nothing needs doing"),
        Pair("no-jinja-when", """The \"when\" is a raw Jinja2 expression, remove redundant \"{{ }}\" from variable(s)"""),
        Pair("no-same-owner", "Do not preserve the owner and group when transferring files across hosts"),
        Pair("package-latest", "Package installs should not use latest"),
        Pair("partial-become", """The \"become_user\" should have a corresponding \"become\" at the play or task level"""),
        Pair("playbook-extension", """Playbooks should have the \".yml\" or \".yaml\" extension"""),
        Pair("risky-file-permissions", "File permissions unset or incorrect"),
        Pair("risky-shell-pipe", "Shells that use pipes should set the pipefail option"),
        Pair("sanity", "Ignore entries in sanity ignore files must match an allow list"),
        Pair("schema", "Perform JSON Schema Validation for known lintable kinds"),
        Pair("syntax-check", "Ansible syntax check failed"),
        Pair("var-naming", "All variables should be named using only lowercase and underscores"),
        Pair("yaml", "Violations reported by yamllint")
    )

    private val multipleRules = mapOf(
        Pair(
            "args",
            listOf(
                "args[module]"
            )
        ),
        Pair(
            "complexity",
            listOf(
                "complexity[play]",
                "complexity[nesting]"
            )
        ),
        Pair(
            "fqcn",
            listOf(
                "fqcn[action]",
                "fqcn[action-core]",
                "fqcn[canonical]",
                "fqcn[deep]",
                "fqcn[keyword]"
            )
        ),
        Pair(
            "galaxy",
            listOf(
                "galaxy[invalid-dependency-version]",
                "galaxy[no-changelog]",
                "galaxy[no-runtime]",
                "galaxy[tags]",
                "galaxy[version-missing]",
                "galaxy[version-incorrect]"
            )
        ),
        Pair(
            "jinja",
            listOf(
                "jinja[invalid]",
                "jinja[spacing]"
            )
        ),
        Pair(
            "key-order",
            listOf(
                "key-order[play]",
                "key-order[task]"
            )
        ),
        Pair(
            "latest",
            listOf(
                "latest[git]",
                "latest[hg]"
            )
        ),
        Pair(
            "meta-runtime",
            listOf(
                "meta-runtime[invalid-version]",
                "meta-runtime[unsupported-version]"
            )
        ),
        Pair(
            "name",
            listOf(
                "name[casing]",
                "name[missing]",
                "name[play]",
                "name[prefix]",
                "name[template]"
            )
        ),
        Pair(
            "no-free-form",
            listOf(
                "no-free-form",
                "no-free-form[raw-non-string]",
                "no-free-form[raw]"
            )
        ),
        Pair(
            "partial-become",
            listOf(
                "partial-become[play]",
                "partial-become[task]"
            )
        ),
        Pair(
            "role-name",
            listOf(
                "role-name",
                "role-name[path]"
            )
        ),
        Pair(
            "run-once",
            listOf(
                "run-once[play]",
                "run-once[task]"
            )
        ),
        Pair(
            "sanity",
            listOf(
                "sanity[bad-ignore]",
                "sanity[cannot-ignore]"
            )
        ),
        Pair(
            "schema",
            listOf(
                "schema[ansible-lint-config]",
                "schema[ansible-navigator-config]",
                "schema[ansible-navigator]",
                "schema[changelog]",
                "schema[execution-environment]",
                "schema[galaxy]",
                "schema[inventory]",
                "schema[meta-runtime]",
                "schema[meta]",
                "schema[moves]",
                "schema[playbook]",
                "schema[requirements]",
                "schema[role-arg-spec]",
                "schema[rulebook]",
                "schema[tasks]",
                "schema[vars]"
            )
        ),
        Pair(
            "syntax-check",
            listOf(
                "syntax-check[empty-playbook]",
                "syntax-check[malformed]",
                "syntax-check[missing-file]",
                "syntax-check[no-file]",
                "syntax-check[specific]",
                "syntax-check[unknown-module]"
            )
        ),
        Pair(
            "var-naming",
            listOf(
                "var-naming[no-jinja]",
                "var-naming[no-keyword]",
                "var-naming[no-reserved]",
                "var-naming[no-role-prefix]",
                "var-naming[non-ascii]",
                "var-naming[non-string]",
                "var-naming[pattern]",
                "var-naming[read-only]"
            )
        ),
        Pair(
            "warning",
            listOf(
                "warning[raw-non-string]",
                "warning[outdated-tag]"
            )
        ),
        Pair(
            "yaml",
            listOf(
                "yaml[braces]",
                "yaml[brackets]",
                "yaml[colons]",
                "yaml[commas]",
                "yaml[comments]",
                "yaml[document-start]",
                "yaml[empty-lines]",
                "yaml[hyphens]",
                "yaml[indentation]",
                "yaml[key-duplicates]",
                "yaml[line-length]",
                "yaml[octal-values]",
                "yaml[trailing-spaces]",
                "yaml[truthy]"
            )
        )
    )

    private val bugs = listOf("args", "deprecated-bare-vars", "no-free-form", "run-once")

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S6437
    private val vulnerabilityHighTrustworthy = listOf("no-log-password")

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S6430
    private val securityHotspotMediumComplete = listOf("partial-become")

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S2612
    private val securityHotspotMediumConventional = listOf("no-same-owner", "risky-file-permissions")

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S1155/java
    private val codeSmellsLowClear = listOf(
        "empty-string-compare",
        "galaxy",
        "ignore-errors",
        "jinja[invalid]",
        "literal-compare",
        "load-failure",
        "meta-incorrect",
        "sanity",
        "syntax-check",
        "yaml"
    )

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S2208/java
    private val codeSmellsHighClear = listOf("fqcn", "risky-octal")

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S6956
    private val codeSmellLowConventional = listOf("key-order", "name", "role-name", "var-naming")

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S7021
    private val codeSmellHighConventional = listOf("no-relative-paths")

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S1808
    private val codeSmellsLowFormatted = listOf("jinja[spacing]", "no-tabs")

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S6596
    private val codeSmellMediumLogical = listOf("galaxy-version-incorrect", "latest", "meta-runtime", "no-prompting", "package-latest")

    // Similar to: https://sonarsource.github.io/rspec/#/rspec/S116
    private val codeSmellLowIdentifiable = listOf("loop-var-prefix")

    /**
     * Extract rules from ansible-lint *.md files.
     * It is based on git submodule located in "private/its/sources/ansible/ansible-lint/src/ansiblelint/rules".
     * To update rules, please update the submodule commit.
     */
    fun extractAnsibleRules(): List<Rule> {
        val path = Path.of("private/its/sources/ansible/ansible-lint/src/ansiblelint/rules")
        val files = path.listDirectoryEntries("*.py")
            .filterNot { it.fileName.toString().startsWith("__") || it.fileName.toString() == "conftest.py" }
            .sorted()

        return files.flatMap(::extractRule).toList().plus(standardRules())
    }

    private fun extractRule(path: Path): List<Rule> {
        LOG.info("Process rule: $path")
        val text = path.readText()
        val id = ID_REGEX.find(text)!!.groups["id"]!!.value
        val title = readTitle(text, id)
        if (title.length > 200) {
            throw RuntimeException(
                "Title for rule $id is too long, max length is 200. " +
                    "Please set a title for the rule manually in the ruleTitles map."
            )
        }
        val description = ANSIBLE_DESCRIPTION_PREFIX.format(id)
        return multipleRules.getOrElse(id) { listOf(id) }
            .map {
                val (type, attribute, softwareQuality, qualityImpact) = extractAttributes(it)
                Rule(
                    id = it,
                    title = title,
                    url = url(it),
                    description = description,
                    tags = listOf("ansible-lint"),
                    type = type,
                    attribute = attribute,
                    softwareQuality = softwareQuality,
                    qualityImpact = qualityImpact
                )
            }
            .toList()
    }

    private fun url(id: String) = RULE_URL_TEMPLATE.format(idNoBrackets(id))

    private fun idNoBrackets(id: String) = id.substringBefore("[")

    /**
     * The title is extracted using following methods:
     * - Read from ruleTitles by using full id, e.g. "jinja[spacing]"
     * - Read from ruleTitles by using id without brackets, e.g. "jinja"
     * - Read from Python file the "shortdesc" property
     * - Read from Python file the "description" property, single line
     * - Read from Python file the "description" property, multiline
     */
    private fun readTitle(
        text: String,
        id: String,
    ): String =
        ruleTitles[id] ?: ruleTitles[idNoBrackets(id)] ?: SHORT_DESCRIPTION_REGEX.find(text)?.groups?.get("title")?.value
            ?: DESCRIPTION_REGEX.find(text)?.groups?.get("title")?.value
                ?.replace("  ", " ")
                ?.replace("``", "\\\"")
                ?.removeLastDot()
            ?: DESCRIPTION_REGEX_MULTILINE.findAll(text)
                .map { lines ->
                    DESCRIPTION_STRING.findAll(lines.value)
                        .map { it2 -> it2.groups["string"]?.value }.joinToString(" ")
                        .replace("  ", " ")
                        .replace("``", "\\\"")
                        .removeLastDot()
                }
                .joinToString("")

    private fun String.removeLastDot() = if (endsWith(".")) substring(0, length - 1) else this

    private fun extractAttributes(id: String): List<String> {
        val idNoBrackets = idNoBrackets(id)
        return if (bugs.contains(id) || bugs.contains(idNoBrackets)) {
            listOf("BUG", "LOGICAL", "RELIABILITY", "MEDIUM")
        } else if (vulnerabilityHighTrustworthy.contains(id) || vulnerabilityHighTrustworthy.contains(idNoBrackets)) {
            listOf("VULNERABILITY", "TRUSTWORTHY", "SECURITY", "HIGH")
        } else if (securityHotspotMediumComplete.contains(id) || securityHotspotMediumComplete.contains(idNoBrackets)) {
            listOf("SECURITY_HOTSPOT", "COMPLETE", "SECURITY", "MEDIUM")
        } else if (securityHotspotMediumConventional.contains(id) || securityHotspotMediumConventional.contains(idNoBrackets)) {
            listOf("SECURITY_HOTSPOT", "CONVENTIONAL", "SECURITY", "MEDIUM")
        } else if (codeSmellsLowClear.contains(id) || codeSmellsLowClear.contains(idNoBrackets)) {
            listOf("CODE_SMELL", "CLEAR", "MAINTAINABILITY", "LOW")
        } else if (codeSmellsHighClear.contains(id) || codeSmellsHighClear.contains(idNoBrackets)) {
            listOf("CODE_SMELL", "CLEAR", "MAINTAINABILITY", "HIGH")
        } else if (codeSmellLowConventional.contains(id) || codeSmellLowConventional.contains(idNoBrackets)) {
            listOf("CODE_SMELL", "CONVENTIONAL", "MAINTAINABILITY", "LOW")
        } else if (codeSmellHighConventional.contains(id) || codeSmellHighConventional.contains(idNoBrackets)) {
            listOf("CODE_SMELL", "CONVENTIONAL", "MAINTAINABILITY", "HIGH")
        } else if (codeSmellsLowFormatted.contains(id) || codeSmellsLowFormatted.contains(idNoBrackets)) {
            listOf("CODE_SMELL", "FORMATTED", "MAINTAINABILITY", "LOW")
        } else if (codeSmellMediumLogical.contains(id) || codeSmellMediumLogical.contains(idNoBrackets)) {
            listOf("CODE_SMELL", "LOGICAL", "MAINTAINABILITY", "MEDIUM")
        } else if (codeSmellLowIdentifiable.contains(id) || codeSmellLowIdentifiable.contains(idNoBrackets)) {
            listOf("CODE_SMELL", "IDENTIFIABLE", "MAINTAINABILITY", "LOW")
        } else {
            listOf("CODE_SMELL", "CONVENTIONAL", "MAINTAINABILITY", "MEDIUM")
        }
    }

    private val fallbackRule = Rule(
        id = "ansible-lint.fallback",
        title = "Ansible Lint Rule",
        url = RULE_URL_DEFAULT,
        description = "This reporting may be triggered by a custom ansible-lint rule or by a default ansible-lint rule " +
            "that has not yet been added to the Sonar IaC analyzer",
        tags = listOf("ansible-lint"),
        type = "CODE_SMELL",
        severity = "MAJOR",
        attribute = "CONVENTIONAL",
        softwareQuality = "MAINTAINABILITY",
        qualityImpact = "MEDIUM"
    )

    private fun standardRules(): List<Rule> {
        val undocumentedRules = listOf(
            "internal-error",
            "load-failure",
            "load-failure[composererror]",
            "load-failure[filenotfounderror]",
            "load-failure[runtimeerror]",
            "load-failure[unicodedecodeerror]",
            "parser-error",
            "warning[outdated-tag]"
        )
            .map {
                Rule(
                    id = it,
                    title = "Failed to load or parse file",
                    url = RULE_URL_DEFAULT,
                    description = ANSIBLE_DESCRIPTION_PREFIX.format(it),
                    tags = listOf("ansible-lint"),
                    type = "CODE_SMELL",
                    severity = "MAJOR",
                    attribute = "CONVENTIONAL",
                    softwareQuality = "MAINTAINABILITY",
                    qualityImpact = "MEDIUM"
                )
            }
        return undocumentedRules.plusElement(fallbackRule)
    }
}
