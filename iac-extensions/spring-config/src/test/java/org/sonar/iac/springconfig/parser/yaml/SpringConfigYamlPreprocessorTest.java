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
package org.sonar.iac.springconfig.parser.yaml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpringConfigYamlPreprocessorTest {
  @Test
  void shouldKeepValidYaml() {
    var code = """
      foo:
        bar: baz
      # block comment before
      ---
      # block comment after
      foo: bar
      """;
    assertEquals(code, new SpringConfigYamlPreprocessor().preprocess(code));
  }

  @Test
  void shouldRemoveInlineCommentsAfterDocumentBreak() {
    // language=yaml
    var code = """
      foo: bar # comment
      --- ### comment ###
      ---     ### comment ###
      baz: qux
      --- baz: qux
      --- baz: qux # comment
      ---
      """;
    // language=yaml
    var expected = """
      foo: bar # comment
      ---
      ---
      baz: qux
      --- baz: qux
      --- baz: qux # comment
      ---
      """;
    assertEquals(expected, new SpringConfigYamlPreprocessor().preprocess(code));
  }
}
