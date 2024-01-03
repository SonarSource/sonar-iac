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
package org.sonar.iac.docker.visitors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.AbstractHighlightingTest;
import org.sonar.iac.docker.parser.DockerParser;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class DockerHighlightingVisitorTest extends AbstractHighlightingTest {

  protected DockerHighlightingVisitorTest() {
    super(new DockerHighlightingVisitor(), DockerParser.create());
  }

  @Test
  void instruction_keyword() {
    highlight("FROM foo");
    assertHighlighting(0, 3, KEYWORD);
    assertHighlighting(4, 8, null);
  }

  @Test
  void comment_before_instruction() {
    highlight(code("# Comment",
      "FROM foo"));
    assertHighlighting(1, 0, 8, COMMENT);
    assertHighlighting(2, 0, 3, KEYWORD);
    assertHighlighting(2, 4, 7, null);
  }

  @Test
  @Disabled("Will be fixed with SONARIAC-606")
  // TODO SONARIAC-606
  void comment_inside_instruction() {
    highlight(code("FROM \\",
      "# Comment",
      "foo"));
    assertHighlighting(1, 0, 3, KEYWORD);
    assertHighlighting(1, 4, 5, null);
    assertHighlighting(2, 0, 8, COMMENT);
    assertHighlighting(3, 0, 3, null);
  }

  @Test
  void from_instruction() {
    highlight("  FROM foo AS bar");
    assertHighlighting(0, 1, null);
    assertHighlighting(2, 5, KEYWORD);
    assertHighlighting(11, 12, KEYWORD);
    assertHighlighting(13, 16, null);
  }

}
