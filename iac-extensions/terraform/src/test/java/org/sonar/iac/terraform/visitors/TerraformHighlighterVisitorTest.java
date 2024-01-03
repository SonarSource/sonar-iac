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
package org.sonar.iac.terraform.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.AbstractHighlightingTest;
import org.sonar.iac.terraform.parser.HclParser;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.ANNOTATION;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.CONSTANT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD_LIGHT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

class TerraformHighlighterVisitorTest extends AbstractHighlightingTest {

  public TerraformHighlighterVisitorTest() {
    super(new TerraformHighlightingVisitor(), new HclParser());
  }

  @Test
  void empty_input() {
    highlight("");
    assertHighlighting(1, 0, 0, null);
  }

  @Test
  void single_line_comment() {
    highlight("  // Comment ");
    assertHighlighting(0, 1, null);
    assertHighlighting(2, 12, COMMENT);
  }

  @Test
  void comment() {
    highlight("  /*Comment*/ ");
    assertHighlighting(0, 1, null);
    assertHighlighting(2, 12, COMMENT);
    assertHighlighting(13, 13, null);
  }

  @Test
  void multiline_comment() {
    highlight("/*\nComment\n*/ ");
    assertHighlighting(1, 0, 1, COMMENT);
    assertHighlighting(2, 0, 6, COMMENT);
    assertHighlighting(3, 0, 1, COMMENT);
    assertHighlighting(3, 2, 2, null);
  }

  @Test
  void block_type() {
    highlight("block \"label\" {}");
    assertHighlighting(0, 4, CONSTANT);
    assertHighlighting(6, 12, KEYWORD);
    assertHighlighting(13, 14, null);
  }

  @Test
  void string_literal() {
    highlight("a = \"abc\"");
    assertHighlighting(0, 0, ANNOTATION);
    assertHighlighting(1, 3, null);
    assertHighlighting(4, 8, STRING);
  }

  @Test
  void numeric_literal() {
    highlight("a = 12");
    assertHighlighting(0, 0, ANNOTATION);
    assertHighlighting(1, 3, null);
    assertHighlighting(4, 5, KEYWORD_LIGHT);
  }

  @Test
  void boolean_literal() {
    highlight("a = true");
    assertHighlighting(0, 0, ANNOTATION);
    assertHighlighting(1, 3, null);
    assertHighlighting(4, 5, CONSTANT);
  }

  @Test
  void null_literal() {
    highlight("a = null");
    assertHighlighting(0, 0, ANNOTATION);
    assertHighlighting(1, 3, null);
    assertHighlighting(4, 5, CONSTANT);
  }

  @Test
  void attribute_access() {
    highlight("a = foo.bar");
    assertHighlighting(0, 0, ANNOTATION);
    assertHighlighting(1, 3, null);
    assertHighlighting(4, 10, KEYWORD);
  }

  @Test
  void object() {
    highlight("tags = {git_commit = \"81738b80d571fa3034633690d13ffb460e1e7dea\"}");
    assertHighlighting(0, 3, ANNOTATION);
    assertHighlighting(4, 7, null);
    assertHighlighting(8, 17, ANNOTATION);
    assertHighlighting(18, 19, null);
    assertHighlighting(21, 62, STRING);
    assertHighlighting(63, 64, null);
  }

  @Test
  void template_expression() {
    highlight("name = \"terragoat-app-service-${var.environment}${random_integer.rnd_int.result}\"");
    assertHighlighting(0, 3, ANNOTATION);
    assertHighlighting(4, 7, null);
    assertHighlighting(8, 29, STRING);
    assertHighlighting(30, 31, null);
    assertHighlighting(32, 46, KEYWORD);
    assertHighlighting(47, 47, null);
  }
}
