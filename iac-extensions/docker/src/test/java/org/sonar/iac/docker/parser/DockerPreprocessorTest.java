/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.docker.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class DockerPreprocessorTest {

  final DockerPreprocessor preprocessor = new DockerPreprocessor();

  @ParameterizedTest
  @CsvSource({
    "'foo\\\nbar'",
    "'foo\\\r\nbar'",
    "'foo\\\u2028bar'",
    "'foo\\\u2029bar'",
    "'foo\\\rbar'",
  })
  void processSingleEscapedLinebreak(String input) {
    String output = preprocessor.process(input);
    assertThat(output).isEqualTo("foobar");
  }

  @Test
  void processNoEscapedLinebreak() {
    String input = "foo\nbar";
    String output = preprocessor.process(input);
    assertThat(output).isEqualTo(input);
  }

  @Test
  void processNoLinebreak() {
    String input = "foo bar";
    String output = preprocessor.process(input);
    assertThat(output).isEqualTo(input);
  }

  @Test
  void processMultipleEscapedLinebreaks() {
    String input = "foo\\\nbar\\\npong";
    String output = preprocessor.process(input);
    assertThat(output).isEqualTo("foobarpong");
  }

  @Test
  void sourceLineAndColumnWithOneEscapedLinebreak() {
    preprocessor.process("foo\\\nbar");
    DockerPreprocessor.SourceOffset sourceOffset = preprocessor.sourceOffset();
    assertThat(sourceOffset.sourceLineAndColumnAt(2)).isEqualTo(new int[] {1,3});
    assertThat(sourceOffset.sourceLineAndColumnAt(3)).isEqualTo(new int[] {2,1});
  }
}
