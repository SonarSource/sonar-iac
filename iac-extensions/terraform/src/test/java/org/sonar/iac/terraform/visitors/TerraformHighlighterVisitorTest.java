/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.AbstractHighlightingTest;
import org.sonar.iac.terraform.parser.HclParser;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.CONSTANT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
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
    highlight("block {}");
    assertHighlighting(0, 4, KEYWORD);
    assertHighlighting(5, 7, null);
  }

  @Test
  void string_literal() {
    highlight("a = \"abc\"");
    assertHighlighting(0, 3, null);
    assertHighlighting(4, 8, STRING);
  }

  @Test
  void non_string_literal() {
    highlight("a = 12");
    assertHighlighting(0, 3, null);
    assertHighlighting(4, 5, CONSTANT);
  }
}
