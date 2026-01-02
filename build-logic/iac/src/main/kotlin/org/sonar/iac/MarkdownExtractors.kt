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
 * Configuration for parsing a markdown table.
 *
 * @param headerRegex Regex to match a section header before the table.
 * @param parseMultipleTables If true, continues parsing all tables in the document after the header.
 *                           If false, stops after the first table.
 */
data class MarkdownTableConfig(
    val headerRegex: Regex,
    val parseMultipleTables: Boolean = false,
)

/**
 * Parse markdown tables from a document and extract structured data.
 *
 * @param T The type of data to extract from each row
 * @param content The markdown content to parse
 * @param config Configuration for table parsing behavior
 * @param logger Gradle logger for logging warnings
 * @param rowParser Function to parse a single table row. Returns null if the row should be skipped.
 *                  Throws an exception if parsing fails (will be logged and row skipped).
 * @return List of parsed data from all matching table rows
 */
fun <T> parseMarkdownTable(
    content: String,
    config: MarkdownTableConfig,
    logger: Logger,
    rowParser: (String) -> T?,
): List<T> {
    val lines = content.lines()

    val linesAfterFirstHeader = lines.dropWhile { !it.trim().matches(config.headerRegex) }

    return if (config.parseMultipleTables) {
        parseMultipleTables(linesAfterFirstHeader, config.headerRegex, logger, rowParser)
    } else {
        parseSingleTable(linesAfterFirstHeader, logger, rowParser)
    }
}

/**
 * Parse a single table from the lines.
 * Uses a functional approach with dropWhile/takeWhile for simple, single-table extraction.
 */
private fun <T> parseSingleTable(
    lines: List<String>,
    logger: Logger,
    rowParser: (String) -> T?,
): List<T> =
    lines
        // Drop lines until we find the table separator (starts with | and contains ---)
        .dropWhile { !(it.trim().startsWith("|") && it.contains("---")) }
        // Take all lines that are part of the table (start with |)
        .takeWhile { it.trim().startsWith("|") }
        // Skip the separator line itself
        .drop(1)
        .mapNotNull { line ->
            parseRow(line, logger, rowParser)
        }

/**
 * Parse multiple tables from the lines.
 * Splits the document into individual tables and parses each using parseSingleTable.
 */
private fun <T> parseMultipleTables(
    lines: List<String>,
    headerRegex: Regex,
    logger: Logger,
    rowParser: (String) -> T?,
): List<T> =
    splitIntoTables(lines, headerRegex).flatMap { tableLines ->
        parseSingleTable(tableLines, logger, rowParser)
    }

/**
 * Split a list of lines into separate table chunks.
 * Each chunk contains all lines belonging to one markdown table.
 * Uses headerRegex to detect the beginning of new tables.
 */
private fun splitIntoTables(
    lines: List<String>,
    headerRegex: Regex,
): List<List<String>> {
    data class TableAccumulator(
        val tables: List<List<String>> = emptyList(),
        val currentTable: List<String> = emptyList(),
        val inTable: Boolean = false,
    )

    val finalState =
        lines.fold(TableAccumulator()) { acc, line ->
            val trimmedLine = line.trim()
            val isHeader = trimmedLine.matches(headerRegex)
            val isTableLine = trimmedLine.startsWith("|") && line.count { it == '|' } >= 3
            val isSeparatorLine = isTableLine && trimmedLine.contains("---")

            when {
                // Header line: finalize current table and start the next one
                isHeader && acc.currentTable.isNotEmpty() ->
                    TableAccumulator(
                        tables = acc.tables + listOf(acc.currentTable),
                        currentTable = emptyList(),
                        inTable = false
                    )
                // Table line: add to current table
                isTableLine ->
                    TableAccumulator(
                        tables = acc.tables,
                        currentTable = acc.currentTable + line,
                        // Set inTable to true if we are already in a table body, or we are past the separator line (after header)
                        inTable = acc.inTable || !isSeparatorLine
                    )
                // Non-table, non-empty line after being in table: finalize current table
                acc.inTable && trimmedLine.isNotEmpty() ->
                    TableAccumulator(
                        tables = if (acc.currentTable.isNotEmpty()) acc.tables + listOf(acc.currentTable) else acc.tables,
                        currentTable = emptyList(),
                        inTable = false
                    )
                // Otherwise: keep current state
                else -> acc
            }
        }

    // Add final table if exists
    return if (finalState.currentTable.isNotEmpty()) {
        finalState.tables + listOf(finalState.currentTable)
    } else {
        finalState.tables
    }
}

private fun <T> parseRow(
    line: String,
    logger: Logger,
    rowParser: (String) -> T?,
): T? =
    try {
        rowParser(line)
    } catch (e: Exception) {
        logger.warn("Failed to parse markdown table row: $line", e)
        null
    }
