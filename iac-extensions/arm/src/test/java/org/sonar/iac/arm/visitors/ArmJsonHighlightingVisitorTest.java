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
package org.sonar.iac.arm.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.ArmJsonParser;
import org.sonar.iac.common.testing.AbstractHighlightingTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.ANNOTATION;

class ArmJsonHighlightingVisitorTest extends AbstractHighlightingTest {

  protected ArmJsonHighlightingVisitorTest() {
    super(new ArmHighlightingVisitor(), new ArmJsonParser());
  }

  @Test
  void shouldNotThrowExceptionWhenHighlightingResourceDeclaration() {
    String code = """
      {
        "resources": [
          {
            "type": "Microsoft.Kusto/clusters",
            "apiVersion": "2022-12-29",
            "name": "myResource"
          }
        ]
      }""";
    assertThatNoException().isThrownBy(() -> highlight(code));
  }

  /**
   * The value of the {@code query} property is a single line in the file, but contains an escaped line break, so the expression parsed
   * out of it spans two lines. The Bicep tokens of that expression can't be mapped back onto the file and must not be highlighted.
   */
  @Test
  void shouldNotHighlightBicepTokensOfExpressionWithEscapedLineBreak() {
    String code = """
      {
      	"resources": [
      		{
      			"type": "Microsoft.OperationalInsights/workspaces/providers/alertRules",
      			"apiVersion": "2021-03-01-preview",
      			"name": "alertRule",
      			"properties": {
      				"query": "[concat('let x = 1;\\nlet y = ', environment().authentication.audiences[1], ';')]"
      			}
      		}
      	]
      }
      """;
    assertThatNoException().isThrownBy(() -> highlight(code));

    assertHighlighting(8, 4, 10, ANNOTATION);
    assertHighlighting(8, 11, 89, null);
    assertHighlighting(9, 0, 3, null);
  }

  /**
   * Comments of an expression sub-parsed out of a JSON string keep their position within that string, so highlighting them would target
   * the first line of the file. They must not be highlighted at all.
   */
  @Test
  void shouldNotHighlightCommentsInsideExpression() {
    String code = """
      {
        "resources": [
          {
            "type": "Microsoft.Kusto/clusters",
            "apiVersion": "2022-12-29",
            "name": "myResource",
            "properties": {
              "clusterName": "[concat('my', /* TODO rename */ 'Cluster')]"
            }
          }
        ]
      }""";
    assertThatNoException().isThrownBy(() -> highlight(code));

    assertHighlighting(1, 0, 0, null);
    assertHighlighting(8, 8, 20, ANNOTATION);
    assertHighlighting(8, 21, 67, null);
  }
}
