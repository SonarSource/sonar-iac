package org.sonar.iac.buildutil

data class Rule(val id: String, val title: String, val description: String, val tags: List<String>, val type: String, val severity: String)

const val DESCRIPTION_PREFIX = "This issue is raised by the rule [%s] from \\\"AWS CloudFormation Linter\\\" (aka cfn-lint). This is not an issue raised by Sonar analyzers.<br/>" +
    "<br/>" +
    "AWS CloudFormation Linter Message: %s"

/**
 * Extract rules from the Markdown table in the [list of rules](https://github.com/aws-cloudformation/cfn-lint/blob/main/docs/rules.md#rules-1)
 */
fun extractRules(input: String): List<Rule> {
    val tableRows = input.lineSequence()
        .dropWhile { it != "## Rules" }
        .dropWhile { !it.startsWith("|") }
        .takeWhile { it.startsWith("|") }
        .drop(2)
    return tableRows.map(::extractRule).toList().plusElement(fallbackRule)
}

fun extractRule(line: String): Rule {
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
    return Rule(id, columns[2], columns[3], tags, type, severity)
}

fun Rule.asJson(margin: Int): String {
    return """
        {
          "key": "$id",
          "name": "$title",
          "url": "https://github.com/aws-cloudformation/cfn-lint/blob/main/docs/rules.md#rules-1",
          "tags": ${tags.takeIf { it.isNotEmpty() }?.joinToString(prefix = "[", postfix = "]", separator = ",") { "\"$it\"" } ?: "[]"},
          "description": "${DESCRIPTION_PREFIX.format(id, description)}",
          "constantDebtMinutes": 0,
          "type": "$type",
          "severity": "$severity"
        }
    """.trimIndent()
        .lineSequence()
        .joinToString(separator = "\n") { "|${" ".repeat(margin)}$it" }
}

val fallbackRule = Rule(
    id = "cfn-lint.fallback",
    title = "Cfn-lint Rule",
    description = "This reporting may be triggered by a custom cfn-lint rule or by a default cfn-lint rule that has not yet been added to the Sonar IaC analyzer.",
    tags = listOf("cfn-lint"),
    type = "CODE_SMELL",
    severity = "MAJOR"
)

private fun String.toCamelCase() = replace("[a-z][A-Z]".toRegex()) { it.value.first() + "-" + it.value.last().lowercase() }
