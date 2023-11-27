/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.kubernetes.plugin;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class HelmPreprocessorTest {

  @Test
  void shouldAddCommentLineNumber() {
    checkHelmProcessing(
      code("line1",
        "line2",
        "line3"),
      code("line1 #1",
        "line2 #2",
        "line3 #3"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"\n", "\r", "\r\n", "\u2028", "\u2029"})
  void shouldKeepProperCarriageReturn(String carriageReturn) {
    checkHelmProcessing(
      codeSpecific(carriageReturn,
        "line1",
        "",
        "",
        "line4"),
      codeSpecific(carriageReturn,
        "line1 #1",
        " #2",
        " #3",
        "line4 #4"));
  }

  @Test
  void shouldAddCommentLineNumberOnSingleLine() {
    checkHelmProcessing("line1", "line1 #1");
  }

  @Test
  void shouldAddCommentLineNumberOnEmptyFile() {
    checkHelmProcessing("", " #1");
  }

  @Test
  void shouldAddCommentLineNumberOnEmptyLine() {
    checkHelmProcessing(
      code("line1",
        "",
        "line3"),
      code("line1 #1",
        " #2",
        "line3 #3"));
  }

  @Test
  void shouldAddCommentLineNumberOnAlreadyCommentedLine() {
    checkHelmProcessing("line1 # some comment", "line1 # some comment #1");
  }

  void checkHelmProcessing(String source, String expect) {
    assertThat(HelmPreprocessor.addLineComments(source)).isEqualTo(expect);
  }

  String codeSpecific(String carriageReturn, String... lines) {
    return StringUtils.join(lines, carriageReturn);
  }

  String code(String... lines) {
    return codeSpecific("\n", lines);
  }
}
