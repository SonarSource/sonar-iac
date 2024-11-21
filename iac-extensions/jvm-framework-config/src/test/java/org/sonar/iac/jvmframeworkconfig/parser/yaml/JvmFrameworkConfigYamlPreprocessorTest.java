/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.jvmframeworkconfig.parser.yaml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JvmFrameworkConfigYamlPreprocessorTest {
  @Test
  void shouldKeepValidYaml() {
    // language=yaml
    var code = """
      foo:
        bar: baz
      # block comment before
      ---
      # block comment after
      foo: bar
      --- foo: bar # inline comment
      --- !foo bar
      --- >
        foo
      --- |
        foo
      """;

    assertThat(new JvmFrameworkConfigYamlPreprocessor().preprocess(code)).isEqualTo(code);
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
    assertThat(new JvmFrameworkConfigYamlPreprocessor().preprocess(code)).isEqualTo(expected);
  }

  @Test
  void shouldTransformMavenSubstitutions() {
    // language=yaml
    var code = """
      foo: @maven.property@
      foo2: @maven.property@-suffix
      foo3: '@maven.property@'
      bar: prefix-@maven.property@-suffix
      bar2: prefix-@maven.property1@-@maven.property2@-suffix
      baz: |
        @maven.property@
      """;
    // language=yaml
    var expected = """
      foo: 'maven.property'
      foo2: 'maven.property-suffix'
      foo3: '@maven.property@'
      bar: prefix-@maven.property@-suffix
      bar2: prefix-@maven.property1@-@maven.property2@-suffix
      baz: |
        @maven.property@
      """;
    var preprocessor = new JvmFrameworkConfigYamlPreprocessor();

    assertThat(preprocessor.preprocess(code)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r", "\r\n", "\u2028", "\u2029"})
  void shouldTransformFilesWithDifferentLineTerminators(String lineTerminator) {
    var code = "foo: bar" + lineTerminator +
      "--- ###" + lineTerminator +
      "bar: foo" + lineTerminator;
    var expected = """
      foo: bar
      ---
      bar: foo
      """;

    assertThat(new JvmFrameworkConfigYamlPreprocessor().preprocess(code)).isEqualTo(expected);
  }
}
