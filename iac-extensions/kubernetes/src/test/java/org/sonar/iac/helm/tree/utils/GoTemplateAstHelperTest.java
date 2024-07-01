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
package org.sonar.iac.helm.tree.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.utils.GoAstCreator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;

class GoTemplateAstHelperTest {

  @TempDir
  private static File tempDir;

  public static List<Arguments> textRanges() {
    return List.of(
      of(range(2, 0, 2, 36), "bigger than node"),
      of(range(2, 5, 2, 22), "shifted left from node"),
      of(range(2, 15, 2, 35), "shifted right from node"),
      of(range(2, 17, 2, 18), "smaller than node"));
  }

  @ParameterizedTest(name = "should find values by TextRange {1}")
  @MethodSource("textRanges")
  void shouldFindValuesByTextRange(TextRange textRange, String name) throws IOException {
    var sourceCode = """
      apiVersion: apps/v1 #1
      hostIPC: {{ .Values.hostIPC }} #2 #3""";
    var goTemplateTree = new GoAstCreator(tempDir).goAstFromSource(sourceCode, "", "name: test chart");

    var actual = GoTemplateAstHelper.findValuePaths(goTemplateTree, textRange);

    assertThat(actual).contains(new ValuePath("Values", "hostIPC"));
  }

  @Test
  void shouldFindValuesInToYaml() throws IOException {
    var sourceCode = """
      {{ toYaml (default .Values.initContainers.securityContext .Values.initSysctl.securityContext) | indent 12 }} #1
      #2""";
    var goTemplateTree = new GoAstCreator(tempDir).goAstFromSource(sourceCode, """
      initContainers:
        securityContext:
      initSysctl:
        securityContext:""", "name: test chart");

    var actual = GoTemplateAstHelper.findValuePaths(goTemplateTree, range(1, 0, 1, 108));

    assertThat(actual).contains(
      new ValuePath("Values", "initContainers", "securityContext"),
      new ValuePath("Values", "initSysctl", "securityContext"));
  }

  @Test
  void shouldFindValuesForTwoHelmExpressionsInRange() throws IOException {
    String sourceCode = """
      apiVersion: apps/v1 #1
      hostIPC: {{ .Values.hostIPC }}{{ .Values.foo.bar }} #2 #3""";
    var goTemplateTree = new GoAstCreator(tempDir).goAstFromSource(sourceCode, """
      hostIPC:
      foo:
        bar:""", "name: test chart");

    var actual = GoTemplateAstHelper.findValuePaths(goTemplateTree, range(2, 2, 2, 45));

    assertThat(actual).contains(new ValuePath("Values", "hostIPC"), new ValuePath("Values", "foo", "bar"));
  }
}
