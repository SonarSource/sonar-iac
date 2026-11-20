/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CfnLintRulesGeneratorTest {
    @Test
    fun `escape imported description text while preserving the HTML prefix`() {
        val markdown = """
            ## Rules
            | Rule | Name | Description | Functions | Source | Tags |
            | ---- | ---- | ----------- | --------- | ------ | ---- |
            | W2506 | ImageId | Value<AWS::EC2::Image::Id> & other types | | | resources |
        """.trimIndent()

        val rule = CfnLintRulesGenerator.extractRules(markdown).first()

        assertEquals("W2506", rule.id)
        assertTrue(rule.description.contains("<br/><br/>"))
        assertTrue(rule.description.endsWith("Value&lt;AWS::EC2::Image::Id&gt; &amp; other types"))
    }
}
