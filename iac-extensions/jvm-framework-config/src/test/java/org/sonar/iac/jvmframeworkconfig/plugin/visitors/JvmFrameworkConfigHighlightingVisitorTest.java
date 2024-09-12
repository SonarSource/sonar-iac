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
package org.sonar.iac.jvmframeworkconfig.plugin.visitors;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.AbstractHighlightingTest;
import org.sonar.iac.common.yaml.YamlLanguage;
import org.sonar.iac.jvmframeworkconfig.parser.JvmFrameworkConfigParser;
import org.sonar.iac.jvmframeworkconfig.plugin.JvmFrameworkConfigExtension;

import static org.sonar.api.batch.sensor.highlighting.TypeOfText.COMMENT;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.KEYWORD;
import static org.sonar.api.batch.sensor.highlighting.TypeOfText.STRING;

class JvmFrameworkConfigHighlightingVisitorTest extends AbstractHighlightingTest {
  protected JvmFrameworkConfigHighlightingVisitorTest() {
    super(new JvmFrameworkConfigHighlightingVisitor(), new JvmFrameworkConfigParser());
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
      JvmFrameworkConfigExtension.LANGUAGE_KEY);
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
        key1:
          key2:
            key3: value
          key4: value
        """,
      Path.of("application.yaml"),
      YamlLanguage.KEY);
    assertHighlighting(1, 0, 2, KEYWORD);
    assertHighlighting(2, 2, 4, KEYWORD);
    assertHighlighting(3, 2, 4, KEYWORD);
    assertHighlighting(3, 7, 9, STRING);
    assertHighlighting(4, 0, 13, COMMENT);
    assertHighlighting(5, 0, 5, KEYWORD);
    assertHighlighting(6, 4, 7, STRING);
    assertHighlighting(7, 4, 7, STRING);
    assertHighlighting(8, 0, 3, KEYWORD);
    assertHighlighting(9, 2, 5, KEYWORD);
    assertHighlighting(10, 4, 7, KEYWORD);
    assertHighlighting(10, 10, 14, STRING);
    assertHighlighting(11, 2, 5, KEYWORD);
    assertHighlighting(11, 8, 12, STRING);
  }
}
