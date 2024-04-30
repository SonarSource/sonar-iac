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
package org.sonar.iac.springconfig.plugin.visitors;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.AbstractHighlightingTest;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.iac.springconfig.parser.SpringConfigParser;
import org.sonar.iac.springconfig.plugin.SpringConfigExtension;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

class SpringConfigHighlightingVisitorTest extends AbstractHighlightingTest {
  protected SpringConfigHighlightingVisitorTest() {
    super(new SpringConfigHighlightingVisitor(), new SpringConfigParser());
  }

  @Test
  void shouldHighlightProperties() {
    highlight(
      // language=properties
      """
        foo=bar
        foo.bar=baz
        foo[0].bar=baz1
        foo[1].bar=baz2
        # comment
        """,
      Path.of("application.properties"),
      SpringConfigExtension.LANGUAGE_KEY);
    assertHighlighting(1, 0, 2, KEYWORD);
    assertHighlighting(1, 4, 6, STRING);
    assertHighlighting(2, 0, 6, KEYWORD);
    assertHighlighting(2, 8, 10, STRING);
    assertHighlighting(3, 0, 9, KEYWORD);
    assertHighlighting(3, 11, 14, STRING);
    assertHighlighting(4, 0, 9, KEYWORD);
    assertHighlighting(4, 11, 14, STRING);
    assertHighlighting(5, 0, 8, COMMENT);
  }

  @Test
  void shouldHighlightYaml() {
    highlight(
      // language=yaml
      """
        foo:
          baz:
          bar: baz
        # yaml comment
        foo-arr:
          - bar1
          - bar2
        """,
      Path.of("application.yaml"),
      YamlLanguage.KEY);
    assertHighlighting(3, 7, 9, STRING);
    assertHighlighting(4, 0, 7, COMMENT);
    assertHighlighting(6, 4, 7, STRING);
    assertHighlighting(7, 4, 7, STRING);
  }
}
