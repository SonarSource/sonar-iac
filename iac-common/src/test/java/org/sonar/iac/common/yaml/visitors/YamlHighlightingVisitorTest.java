/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
