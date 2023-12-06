/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.kubernetes.visitors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.common.testing.AbstractHighlightingTest;
import org.sonar.iac.common.yaml.YamlParser;
import org.sonar.iac.common.yaml.tree.FileTree;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD_LIGHT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

class KubernetesHighlightingVisitorTest extends AbstractHighlightingTest {

  private static final YamlParser mockParser = mock(YamlParser.class);
  private static final FileTree mockTree = mock(FileTree.class);

  public KubernetesHighlightingVisitorTest() {
    super(new KubernetesHighlightingVisitor(), mockParser);
  }

  @BeforeAll
  public static void init() {
    when(mockParser.parse(any(), any())).thenReturn(mockTree);
  }

  // cases to remember
  // port: !!int "80"
  // 'this value can include a sq with the following escape '' and now it ends '

  // The pipe | will start a multiline string that ends with a new key?
  // The folding operator > will start a multiline string that ends with a new key?

  // Values don't have to end in ' or " but can continue on the next line..

  @ParameterizedTest
  @ValueSource(strings = {
    // quoteless key
    "key: value",
    "key:  value",
    "key: 'value'",
    "key: 'va#lue'",
    "key: \"value\"",
    "key: \"val#ue\"",

    // single quoted key
    "'key': value",
    "'key':  value",
    "'key': 'value'",
    "'key': \"value\"",
    "'ke#y': value",

    // double quoted key
    "\"key\": value",
    "\"key\":  value",
    "\"ke: y\": value",
    "\"key\": 'value'",
    "\"key\": \"value\"",
    "\"key\": 'val: ue'",
    "\"key\": \"val: ue\"",
    "\"ke#y\": value",
    // invalid yaml, still "valid" highlighting
    "k: ey: value",
    "\"key\": val: ue",
  })
  void keyScalarValueShouldHighlight(String code) {
    highlight(code);

    int keyEndIndex = code.indexOf("y");
    if (code.charAt(keyEndIndex + 1) != ':') {
      // account for quotes
      keyEndIndex++;
    }
    int valueStartIndex = code.indexOf("v");
    if (code.charAt(valueStartIndex - 1) != ' ') {
      // account for quotes
      valueStartIndex--;
    }

    assertHighlighting(0, keyEndIndex, KEYWORD);
    assertHighlighting(keyEndIndex + 1, valueStartIndex - 1, null);
    assertHighlighting(valueStartIndex, code.length() - 1, STRING);
  }

  @Test
  @Disabled("Values without quotes don't support inclusion of '#' character")
  void test() {
    highlight("\"key\": value#Comment");
    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(5, 6, null);
    assertHighlighting(7, 19, STRING);
  }

  // ======================================================================================
  // WORKING AS INTENDED
  // ======================================================================================

  @Test
  void valueWithComment() {
    highlight(" - value # Comment");
    assertHighlighting(0, 2, null);
    assertHighlighting(3, 7, STRING);
    assertHighlighting(8, 8, null);
    assertHighlighting(9, 17, COMMENT);
  }

  @Test
  void singleLineComment() {
    highlight("  # Comment ");
    assertHighlighting(0, 1, null);
    assertHighlighting(2, 11, COMMENT);
  }

  @Test
  void keyValueWithComment() {
    highlight("key: value  # Comment");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 4, null);
    assertHighlighting(5, 9, STRING);
    assertHighlighting(12, 20, COMMENT);
  }

  @Test
  void scalar() {
    highlight("value");
    assertHighlighting(0, 4, STRING);
  }

  @Test
  void scalarWithColon() {
    // as the colon is not preceded by a whitespace, this should be interpreted as a single value
    highlight("key:value");
    assertHighlighting(0, 8, STRING);
  }

  @Test
  void scalar_key() {
    highlight("key: ");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 3, null);
  }

  @Test
  void scalar_key_folded_value() {
    highlight("key: >\n  value");
    assertHighlighting(1, 0, 2, KEYWORD);
    assertHighlighting(1, 3, 4, null);
    assertHighlighting(1, 5, 5, KEYWORD_LIGHT);
    assertHighlighting(2, 2, 6, STRING);
  }

  @Test
  void scalar_key_literal_value() {
    highlight("key: |\n  value");
    assertHighlighting(1, 0, 2, KEYWORD);
    assertHighlighting(1, 3, 4, null);
    assertHighlighting(1, 5, 5, KEYWORD_LIGHT);
    assertHighlighting(2, 2, 6, STRING);
  }

  @Test
  void scalar_key_list_value() {
    highlight("key:\n  - value \n -value2");
    assertHighlighting(1, 0, 2, KEYWORD);
    assertHighlighting(1, 3, 4, null);
    assertHighlighting(2, 0, 3, null);
    assertHighlighting(2, 4, 6, STRING);
    assertHighlighting(3, 2, 7, STRING);
  }

  @Test
  void keyWithFormattingTagQuotedValue() {
    highlight("\"key\": !!int \"80\"");
    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(5, 6, null);
    assertHighlighting(7, 11, KEYWORD_LIGHT);
    assertHighlighting(13, 16, STRING);
  }

  @Test
  void keyWithFormattingTagUnquotedValue() {
    highlight("\"key\": !!boolean true");
    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(5, 6, null);
    assertHighlighting(7, 15, KEYWORD_LIGHT);
    assertHighlighting(17, 20, STRING);
  }

  @Test
  void keyWithFormattingTagUnquotedValueWithComment() {
    highlight("\"key\": !!boolean true #Comment");
    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(5, 6, null);
    assertHighlighting(7, 15, KEYWORD_LIGHT);
    assertHighlighting(17, 20, STRING);
    assertHighlighting(22, 29, COMMENT);
  }

  @Test
  void keyWithCustomTag() {
    highlight("\"key\": !customTag value");
    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(5, 6, null);
    assertHighlighting(7, 16, KEYWORD_LIGHT);
    assertHighlighting(18, 22, STRING);
  }

  @Test
  void scalarTag() {
    highlight("  - !customTag");
    assertHighlighting(0, 3, null);
    assertHighlighting(4, 13, KEYWORD_LIGHT);
  }

  @Test
  void scalarTagWithComment() {
    highlight("  - !customTag #Comment");
    assertHighlighting(0, 3, null);
    assertHighlighting(4, 13, KEYWORD_LIGHT);
    assertHighlighting(15, 22, COMMENT);
  }

  // ======================================================================================
  // Limitations of the highlighting
  // ======================================================================================

  @Test
  @Disabled("Values without quotes don't support inclusion of '#' character")
  void quotelessValueWithHashCharacter() {
    highlight(" - value#Comment");
    assertHighlighting(0, 2, null);
    assertHighlighting(3, 16, STRING);
  }

  @Test
  @Disabled("Values without quotes don't support inclusion of '#' character")
  void quotelessKeyAndValueWithHashCharacter() {
    highlight("\"key\": value#Comment");
    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(5, 6, null);
    assertHighlighting(7, 19, STRING);
  }

  @Test
  @Disabled("Key without quotes don't support inclusion of '#' character")
  void quotelessKeyWithHashCharacter() {
    highlight("k#ey: ");
    assertHighlighting(0, 3, KEYWORD);
    assertHighlighting(4, 5, null);
  }

  @Test
  @Disabled("Key without quotes don't support inclusion of '#' character")
  void quotelessKeyWithHashCharacterAndValue() {
    highlight("k#ey: value");
    assertHighlighting(0, 3, KEYWORD);
    assertHighlighting(4, 5, null);
    assertHighlighting(6, 10, STRING);
  }
}
