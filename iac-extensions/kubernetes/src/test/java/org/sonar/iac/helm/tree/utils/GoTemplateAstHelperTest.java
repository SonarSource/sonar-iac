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

import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.tree.impl.ActionNodeImpl;
import org.sonar.iac.helm.tree.impl.CommandNodeImpl;
import org.sonar.iac.helm.tree.impl.FieldNodeImpl;
import org.sonar.iac.helm.tree.impl.GoTemplateTreeImpl;
import org.sonar.iac.helm.tree.impl.ListNodeImpl;
import org.sonar.iac.helm.tree.impl.PipeNodeImpl;
import org.sonar.iac.helm.tree.impl.TextNodeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class GoTemplateAstHelperTest {

  public static List<Arguments> textRanges() {
    return List.of(
      of(range(2, 0, 2, 36), "bigger than node"),
      of(range(2, 5, 2, 15), "shifted left from node"),
      of(range(2, 15, 2, 35), "shifted right from node"),
      of(range(2, 15, 2, 16), "smaller than node"));
  }

  @ParameterizedTest(name = "should find values by TextRange {1}")
  @MethodSource("textRanges")
  void shouldFindValuesByTextRange(TextRange textRange, String name) {
    String text = code("apiVersion: apps/v1 #1",
      "hostIPC: {{ .Values.hostIPC }} #2 #3");
    var textNode1 = new TextNodeImpl(0, 32, "apiVersion: apps/v1 #1\nhostIPC: ");
    var textNode2 = new TextNodeImpl(53, 7, " #2\n #3");
    var fieldNode = new FieldNodeImpl(42, 14, List.of("Values", "hostIPC"));
    var command = new CommandNodeImpl(35, 14, List.of(fieldNode));
    var pipeNode = new PipeNodeImpl(35, 14, List.of(), List.of(command));
    var actionNode = new ActionNodeImpl(35, 14, pipeNode);
    ListNodeImpl root = new ListNodeImpl(0, 14, List.of(textNode1, actionNode, textNode2));
    var goTemplateTree = new GoTemplateTreeImpl("name", "name", 0, root);

    var actual = GoTemplateAstHelper.findNodes(
      goTemplateTree,
      textRange,
      text);

    assertThat(actual).contains(new ValuePath("Values", "hostIPC"));
  }
}
