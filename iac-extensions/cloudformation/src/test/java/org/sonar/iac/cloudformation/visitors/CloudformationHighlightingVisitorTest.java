/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.parser.CloudformationParser;
import org.sonar.iac.common.testing.AbstractHighlightingTest;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

class CloudformationHighlightingVisitorTest extends AbstractHighlightingTest {

  public CloudformationHighlightingVisitorTest() {
    super(new CloudformationHighlightingVisitor(), new CloudformationParser());
  }

  @Test
  void single_line_comment() {
    highlight("  # Comment ");
    assertHighlighting(0, 1, null);
    assertHighlighting(2, 11, COMMENT);
  }

  @Test
  void scalar() {
    highlight("key");
    assertHighlighting(0, 2, STRING);
  }

  @Test
  void scalar_key() {
    highlight("key:");
    assertHighlighting(0, 2, KEYWORD);
    assertHighlighting(3, 3, null);
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
