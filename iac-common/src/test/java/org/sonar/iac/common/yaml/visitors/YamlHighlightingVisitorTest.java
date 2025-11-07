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
package org.sonar.iac.common.yaml.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.AbstractHighlightingTest;
import org.sonar.iac.common.yaml.YamlParser;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

class YamlHighlightingVisitorTest extends AbstractHighlightingTest {

  public YamlHighlightingVisitorTest() {
    super(new YamlHighlightingVisitor(), new YamlParser());
  }

  @Test
  void single_line_comment() {
    highlight("  # Comment ");
    assertHighlighting(0, 1, null);
    assertHighlighting(2, 11, COMMENT);
  }

  @Test
  void scalar() {
    highlight("value");
    assertHighlighting(0, 4, STRING);
  }

  @Test
  void scalar_key() {
    highlight("key: ");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 4, null);
  }

  @Test
  void scalar_key_scalar_value() {
    highlight("key: value");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 4, null);
    assertHighlighting(5, 9, STRING);
  }

  @Test
  void inline_comment() {
    highlight("key: value  # Comment");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 4, null);
    assertHighlighting(5, 9, STRING);
    assertHighlighting(12, 20, COMMENT);
  }

  @Test
  void scalar_key_sq_value() {
    highlight("key: 'value'");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 4, null);
    assertHighlighting(5, 11, STRING);
  }

  @Test
  void scalar_key_dq_value() {
    highlight("key: \"value\"");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 4, null);
    assertHighlighting(5, 11, STRING);
  }

  @Test
  void sq_key_scalar_value() {
    highlight("'key': value");
    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(5, 6, null);
    assertHighlighting(7, 11, STRING);
  }

  @Test
  void dq_key_scalar_value() {
    highlight("\"key\": value");
    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(5, 6, null);
    assertHighlighting(7, 11, STRING);
  }

  @Test
  void scalar_key_folded_value() {
    highlight("key: >\n  value");
    assertHighlighting(1, 0, 2, KEYWORD);
    assertHighlighting(1, 3, 4, null);
    assertHighlighting(1, 5, 6, STRING);
    assertHighlighting(2, 2, 6, STRING);
  }

  @Test
  void scalar_key_literal_value() {
    highlight("key: |\n  value");
    assertHighlighting(1, 0, 2, KEYWORD);
    assertHighlighting(1, 3, 4, null);
    assertHighlighting(1, 5, 6, STRING);
    assertHighlighting(2, 2, 6, STRING);
  }

  @Test
  void scalar_key_list_value() {
    highlight("key:\n  - value");
    assertHighlighting(1, 0, 2, KEYWORD);
    assertHighlighting(1, 3, 4, null);
    assertHighlighting(2, 0, 3, null);
    assertHighlighting(2, 4, 6, STRING);
  }
}
